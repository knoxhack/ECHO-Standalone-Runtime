package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.assets.EchoAssetEntry;
import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver;
import dev.echo.standalone.runtime.assets.EchoItemTextureResolver;
import dev.echo.standalone.runtime.assets.EchoLangResolver;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreAssetReferences;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneRegistry;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EchoRuntimeRegistryAssetCoverageSmokeHarness {
    private static final Pattern JSON_KEY =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:");

    private EchoRuntimeRegistryAssetCoverageSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path echoRoot = echoRoot(standaloneRoot);
        List<EchoAssetMount> mounts = assetMounts(standaloneRoot, echoRoot);
        EchoAssetRuntimeResult assetResult = new EchoAssetRuntime(mounts)
                .load(new EchoDefaultRuntimeServiceRegistry(), List.of());
        EchoMinecraftAssetResolver minecraft = new EchoMinecraftAssetResolver(assetResult.resolver());
        EchoAdapterCoreStandaloneRegistry registry = EchoAdapterCoreStandaloneContentBridge.ashfallLive().registry();

        CoverageReport report = audit(standaloneRoot, echoRoot, registry, assetResult, minecraft);
        writeReport(standaloneRoot.resolve("reports/echo/standalone/registry-asset-coverage.json"), report);

        require(!mounts.isEmpty(), "registry asset coverage requires at least one mounted resource root");
        require(assetResult.index().entries().size() > 0,
                "registry asset coverage requires indexed assets from mounted resource roots");
        require(report.totalRegistryEntries() == report.blockEntries() + report.itemEntries(),
                "coverage should classify exactly block and item entries");
        require(report.blockEntries() > 0, "coverage should audit registered blocks");
        require(report.itemEntries() > 0, "coverage should audit registered items");
        require(report.blockstatePresent() > 0, "coverage should prove at least one registered blockstate resolves");
        require(report.blockTexturePresent() > 0, "coverage should prove at least one registered block texture resolves");
        require(report.itemModelPresent() > 0, "coverage should prove at least one registered item model resolves");
        require(report.itemTexturePresent() > 0, "coverage should prove at least one registered item texture resolves");

        System.out.println("registry asset coverage smoke PASS entries="
                + report.totalRegistryEntries()
                + " blocks=" + report.blockEntries()
                + " items=" + report.itemEntries()
                + " blockstates=" + report.blockstatePresent() + "/" + report.blockEntries()
                + " itemModels=" + report.itemModelPresent() + "/" + report.itemEntries()
                + " textures=" + report.totalTexturePresent() + "/" + report.totalTextureDeclared()
                + " complete=" + report.completeEntries()
                + " incomplete=" + report.incompleteEntries());
    }

    private static CoverageReport audit(
            Path standaloneRoot,
            Path echoRoot,
            EchoAdapterCoreStandaloneRegistry registry,
            EchoAssetRuntimeResult assetResult,
            EchoMinecraftAssetResolver minecraft
    ) {
        EchoBlockTextureResolver blockTextures = new EchoBlockTextureResolver(minecraft);
        EchoItemTextureResolver itemTextures = new EchoItemTextureResolver(minecraft);
        LinkedHashMap<String, Set<String>> langKeysByNamespace = langKeysByNamespace(minecraft, assetResult.index().namespaces());
        ArrayList<CoverageEntry> entries = new ArrayList<>();

        for (EchoAdapterCoreRegistryEntry entry : registry.blocks()) {
            entries.add(auditBlock(entry, minecraft, blockTextures, langKeysByNamespace));
        }
        for (EchoAdapterCoreRegistryEntry entry : registry.items()) {
            entries.add(auditItem(entry, minecraft, itemTextures, langKeysByNamespace));
        }
        entries.sort(java.util.Comparator.comparing(CoverageEntry::contentId));

        return new CoverageReport(
                standaloneRoot,
                echoRoot,
                assetResult.mounts(),
                assetResult.index().entries().size(),
                assetResult.index().namespaces(),
                entries
        );
    }

    private static CoverageEntry auditBlock(
            EchoAdapterCoreRegistryEntry entry,
            EchoMinecraftAssetResolver minecraft,
            EchoBlockTextureResolver blockTextures,
            Map<String, Set<String>> langKeysByNamespace
    ) {
        String assetId = assetFacingId(entry, "block");
        IdParts id = splitContentId(assetId);
        EchoAdapterCoreAssetReferences refs = entry.assetReferences();
        AssetCheck blockstate = assetCheck(
                firstText(refs.blockstateId(), logicalAssetId(id.namespace(), "blockstates/" + id.path() + ".json")),
                minecraft
        );
        Optional<String> modelId = Optional.empty();
        Optional<String> modelLogicalId = optional(refs.modelId());
        Optional<String> missingReason = Optional.empty();
        LinkedHashSet<String> textureIds = new LinkedHashSet<>();
        try {
            EchoBlockTextureResolver.EchoBlockTextureResolution resolution = blockTextures.resolve(assetId);
            modelId = resolution.modelId();
            modelLogicalId = modelLogicalId.or(() -> resolution.modelId().map(EchoRuntimeRegistryAssetCoverageSmokeHarness::modelLogicalId));
            resolution.textureId().ifPresent(textureIds::add);
            textureIds.addAll(resolution.textureIdsByFace().values());
            missingReason = resolution.missingReason();
        } catch (IOException | IllegalArgumentException exception) {
            missingReason = Optional.of(exception.getMessage());
        }
        optional(refs.textureId()).map(EchoRuntimeRegistryAssetCoverageSmokeHarness::textureIdFromLogicalAsset)
                .ifPresent(textureIds::add);
        AssetCheck model = modelLogicalId.map(logicalId -> assetCheck(logicalId, minecraft))
                .orElseGet(() -> AssetCheck.missing(""));
        List<AssetCheck> textures = textureIds.stream()
                .sorted()
                .map(EchoRuntimeRegistryAssetCoverageSmokeHarness::textureLogicalId)
                .map(logicalId -> assetCheck(logicalId, minecraft))
                .toList();
        LangCheck lang = langCheck(entry, "block", id, langKeysByNamespace);
        return CoverageEntry.block(
                entry,
                explicitAssetRefs(refs),
                blockstate,
                model,
                textures,
                lang,
                modelId.orElse(""),
                missingReason.orElse("")
        );
    }

    private static CoverageEntry auditItem(
            EchoAdapterCoreRegistryEntry entry,
            EchoMinecraftAssetResolver minecraft,
            EchoItemTextureResolver itemTextures,
            Map<String, Set<String>> langKeysByNamespace
    ) {
        String assetId = assetFacingId(entry, "item");
        IdParts id = splitContentId(assetId);
        EchoAdapterCoreAssetReferences refs = entry.assetReferences();
        Optional<String> modelId = optional(refs.modelId());
        Optional<String> missingReason = Optional.empty();
        LinkedHashSet<String> textureIds = new LinkedHashSet<>();
        try {
            EchoItemTextureResolver.EchoItemTextureLayerResolution resolution =
                    itemTextures.resolveLayers(assetId);
            modelId = modelId.or(resolution::modelId);
            textureIds.addAll(resolution.textureIds());
            missingReason = resolution.missingReason();
        } catch (IOException | IllegalArgumentException exception) {
            missingReason = Optional.of(exception.getMessage());
        }
        optional(refs.textureId()).map(EchoRuntimeRegistryAssetCoverageSmokeHarness::textureIdFromLogicalAsset)
                .ifPresent(textureIds::add);
        String defaultModel = logicalAssetId(id.namespace(), "models/item/" + id.path() + ".json");
        AssetCheck model = assetCheck(modelId.map(EchoRuntimeRegistryAssetCoverageSmokeHarness::modelLogicalId)
                .orElse(defaultModel), minecraft);
        List<AssetCheck> textures = textureIds.stream()
                .sorted()
                .map(EchoRuntimeRegistryAssetCoverageSmokeHarness::textureLogicalId)
                .map(logicalId -> assetCheck(logicalId, minecraft))
                .toList();
        LangCheck lang = langCheck(entry, "item", id, langKeysByNamespace);
        return CoverageEntry.item(
                entry,
                explicitAssetRefs(refs),
                model,
                textures,
                lang,
                modelId.orElse(""),
                missingReason.orElse("")
        );
    }

    private static AssetCheck assetCheck(String logicalId, EchoMinecraftAssetResolver minecraft) {
        if (logicalId == null || logicalId.isBlank()) {
            return AssetCheck.missing("");
        }
        Optional<EchoAssetEntry> entry = minecraftAsset(logicalId, minecraft);
        return entry.map(assetEntry -> new AssetCheck(
                        logicalId,
                        true,
                        assetEntry.mount().source(),
                        assetEntry.file().toString().replace('\\', '/')
                ))
                .orElseGet(() -> AssetCheck.missing(logicalId));
    }

    private static Optional<EchoAssetEntry> minecraftAsset(String logicalId, EchoMinecraftAssetResolver minecraft) {
        IdParts parts = splitContentId(logicalId);
        String path = parts.path();
        if (path.startsWith("blockstates/")) {
            return minecraft.blockstate(parts.namespace(), stripPrefixSuffix(path, "blockstates/", ".json"));
        }
        if (path.startsWith("models/block/")) {
            return minecraft.blockModel(parts.namespace(), stripPrefixSuffix(path, "models/block/", ".json"));
        }
        if (path.startsWith("models/item/")) {
            return minecraft.itemModel(parts.namespace(), stripPrefixSuffix(path, "models/item/", ".json"));
        }
        if (path.startsWith("textures/")) {
            return minecraft.texture(parts.namespace(), stripPrefixSuffix(path, "textures/", ".png"));
        }
        if (path.startsWith("lang/")) {
            return minecraft.lang(parts.namespace(), stripPrefixSuffix(path, "lang/", ".json"));
        }
        if (path.equals("sounds.json")) {
            return minecraft.sounds(parts.namespace());
        }
        return Optional.empty();
    }

    private static LinkedHashMap<String, Set<String>> langKeysByNamespace(
            EchoMinecraftAssetResolver minecraft,
            List<String> namespaces
    ) {
        LinkedHashMap<String, Set<String>> result = new LinkedHashMap<>();
        EchoLangResolver langResolver = new EchoLangResolver(minecraft);
        for (String namespace : namespaces.stream().sorted().toList()) {
            try {
                Map<String, String> lang = langResolver.load(namespace, "en_us");
                if (lang.isEmpty()) {
                    continue;
                }
                result.put(namespace, Set.copyOf(lang.keySet()));
            } catch (IOException | IllegalArgumentException ignored) {
                result.put(namespace, Set.of());
            }
        }
        return result;
    }

    private static LangCheck langCheck(
            EchoAdapterCoreRegistryEntry entry,
            String prefix,
            IdParts id,
            Map<String, Set<String>> langKeysByNamespace
    ) {
        EchoAdapterCoreAssetReferences refs = entry.assetReferences();
        String key = firstText(refs.langKey(), prefix + "." + id.namespace() + "." + id.path().replace('/', '.'));
        Set<String> keys = langKeysByNamespace.getOrDefault(id.namespace(), Set.of());
        boolean filePresent = langKeysByNamespace.containsKey(id.namespace());
        return new LangCheck(key, filePresent, keys.contains(key), refs.langValue());
    }

    private static List<EchoAssetMount> assetMounts(Path standaloneRoot, Path echoRoot) throws IOException {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        addIfDirectory(roots, standaloneRoot.resolve("echo-runtime-client/src/main/resources"));
        addIfDirectory(roots, echoRoot.resolve("src/main/resources"));
        addModuleResourceRoots(roots, echoRoot.resolve("core"));
        addModuleResourceRoots(roots, echoRoot.resolve("addons"));
        addChildren(roots, standaloneRoot.resolve("resourcepacks"));
        addChildren(roots, standaloneRoot.resolve("packs"));
        addChildren(roots, Path.of("resourcepacks"));
        addChildren(roots, Path.of("packs"));

        ArrayList<EchoAssetMount> mounts = new ArrayList<>();
        int order = 0;
        for (Path root : roots) {
            mounts.add(new EchoAssetMount(order++, "registry-asset-coverage", root, mountId(standaloneRoot, root)));
        }
        return List.copyOf(mounts);
    }

    private static void addModuleResourceRoots(LinkedHashSet<Path> result, Path modulesRoot) throws IOException {
        if (!Files.isDirectory(modulesRoot)) {
            return;
        }
        try (var stream = Files.list(modulesRoot)) {
            for (Path module : stream.filter(Files::isDirectory).sorted().toList()) {
                addIfDirectory(result, module.resolve("src/main/resources"));
            }
        }
    }

    private static void addChildren(LinkedHashSet<Path> result, Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (var stream = Files.list(root)) {
            for (Path child : stream.filter(Files::isDirectory).sorted().toList()) {
                addIfDirectory(result, child);
            }
        }
    }

    private static void addIfDirectory(LinkedHashSet<Path> result, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (Files.isDirectory(normalized)) {
            result.add(normalized);
        }
    }

    private static Path echoRoot(Path standaloneRoot) {
        if (standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                && standaloneRoot.getParent() != null) {
            return standaloneRoot.getParent();
        }
        return standaloneRoot;
    }

    private static String mountId(Path standaloneRoot, Path root) {
        return relativeOrAbsolute(standaloneRoot, root)
                .replace("echo-runtime-client/src/main/resources", "standalone-client-resources")
                .replace("/src/main/resources", "");
    }

    private static String relativeOrAbsolute(Path root, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        try {
            return root.toAbsolutePath().normalize().relativize(normalized).toString().replace('\\', '/');
        } catch (IllegalArgumentException exception) {
            return normalized.toString().replace('\\', '/');
        }
    }

    private static boolean explicitAssetRefs(EchoAdapterCoreAssetReferences refs) {
        return refs != null && (!refs.blockstateId().isBlank()
                || !refs.modelId().isBlank()
                || !refs.textureId().isBlank()
                || !refs.langKey().isBlank()
                || !refs.langValue().isBlank());
    }

    private static Optional<String> optional(String value) {
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim().replace('\\', '/'));
    }

    private static String firstText(String... values) {
        for (String value : values == null ? new String[0] : values) {
            if (value != null && !value.isBlank()) {
                return value.trim().replace('\\', '/');
            }
        }
        return "";
    }

    private static String modelLogicalId(String modelId) {
        IdParts parts = splitContentId(modelId);
        String path = parts.path();
        if (path.startsWith("models/")) {
            return parts.namespace() + ":" + ensureSuffix(path, ".json");
        }
        if (path.startsWith("block/")) {
            return parts.namespace() + ":models/" + ensureSuffix(path, ".json");
        }
        if (path.startsWith("item/")) {
            return parts.namespace() + ":models/" + ensureSuffix(path, ".json");
        }
        return parts.namespace() + ":models/item/" + ensureSuffix(path, ".json");
    }

    private static String textureLogicalId(String textureId) {
        IdParts parts = splitContentId(textureIdFromLogicalAsset(textureId));
        String path = parts.path();
        if (path.startsWith("textures/")) {
            return parts.namespace() + ":" + ensureSuffix(path, ".png");
        }
        return parts.namespace() + ":textures/" + ensureSuffix(path, ".png");
    }

    private static String textureIdFromLogicalAsset(String value) {
        IdParts parts = splitContentId(value);
        String path = parts.path();
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        return parts.namespace() + ":" + path;
    }

    private static String logicalAssetId(String namespace, String path) {
        return namespace + ":" + path.replace('\\', '/');
    }

    private static String assetFacingId(EchoAdapterCoreRegistryEntry entry, String domainPrefix) {
        IdParts id = splitContentId(entry.contentId());
        String path = id.path();
        String prefix = domainPrefix + "/";
        if (path.startsWith(prefix) && path.length() > prefix.length()) {
            path = path.substring(prefix.length());
        }
        return id.namespace() + ":" + path;
    }

    private static String stripPrefixSuffix(String value, String prefix, String suffix) {
        String result = value;
        if (!prefix.isBlank() && result.startsWith(prefix)) {
            result = result.substring(prefix.length());
        }
        if (!suffix.isBlank() && result.endsWith(suffix)) {
            result = result.substring(0, result.length() - suffix.length());
        }
        return result;
    }

    private static String ensureSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value : value + suffix;
    }

    private static IdParts splitContentId(String id) {
        String normalized = firstText(id);
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator >= normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid namespaced id: " + id);
        }
        return new IdParts(normalized.substring(0, separator), normalized.substring(separator + 1));
    }

    private static String unescapeJsonString(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (!escaped) {
                if (current == '\\') {
                    escaped = true;
                } else {
                    result.append(current);
                }
                continue;
            }
            switch (current) {
                case '"' -> result.append('"');
                case '\\' -> result.append('\\');
                case '/' -> result.append('/');
                case 'b' -> result.append('\b');
                case 'f' -> result.append('\f');
                case 'n' -> result.append('\n');
                case 'r' -> result.append('\r');
                case 't' -> result.append('\t');
                default -> result.append(current);
            }
            escaped = false;
        }
        if (escaped) {
            result.append('\\');
        }
        return result.toString();
    }

    private static void writeReport(Path path, CoverageReport report) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, report.toJson());
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String escape(String value) {
        return (value == null ? "" : value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private record IdParts(String namespace, String path) {
        private IdParts {
            namespace = namespace.toLowerCase(Locale.ROOT);
            path = path.replace('\\', '/');
        }
    }

    private record AssetCheck(String logicalId, boolean present, String sourceMount, String sourceFile) {
        static AssetCheck missing(String logicalId) {
            return new AssetCheck(logicalId == null ? "" : logicalId, false, "", "");
        }

        String toJson(String indent) {
            return indent + "{\n"
                    + indent + "  \"logicalId\": \"" + escape(logicalId) + "\",\n"
                    + indent + "  \"present\": " + present + ",\n"
                    + indent + "  \"sourceMount\": \"" + escape(sourceMount) + "\",\n"
                    + indent + "  \"sourceFile\": \"" + escape(sourceFile) + "\"\n"
                    + indent + "}";
        }
    }

    private record LangCheck(String key, boolean filePresent, boolean keyPresent, String inlineValue) {
        String toJson(String indent) {
            return indent + "{\n"
                    + indent + "  \"key\": \"" + escape(key) + "\",\n"
                    + indent + "  \"filePresent\": " + filePresent + ",\n"
                    + indent + "  \"keyPresent\": " + keyPresent + ",\n"
                    + indent + "  \"inlineValuePresent\": " + (inlineValue != null && !inlineValue.isBlank()) + "\n"
                    + indent + "}";
        }
    }

    private record CoverageEntry(
            String contentId,
            String domain,
            String displayName,
            boolean explicitAssetReferences,
            AssetCheck blockstate,
            AssetCheck model,
            List<AssetCheck> textures,
            LangCheck lang,
            String resolvedModelId,
            String resolverMissingReason
    ) {
        static CoverageEntry block(
                EchoAdapterCoreRegistryEntry entry,
                boolean explicitAssetReferences,
                AssetCheck blockstate,
                AssetCheck model,
                List<AssetCheck> textures,
                LangCheck lang,
                String resolvedModelId,
                String resolverMissingReason
        ) {
            return new CoverageEntry(
                    entry.contentId(),
                    entry.domain().id(),
                    entry.displayName(),
                    explicitAssetReferences,
                    blockstate,
                    model,
                    textures,
                    lang,
                    resolvedModelId,
                    resolverMissingReason
            );
        }

        static CoverageEntry item(
                EchoAdapterCoreRegistryEntry entry,
                boolean explicitAssetReferences,
                AssetCheck model,
                List<AssetCheck> textures,
                LangCheck lang,
                String resolvedModelId,
                String resolverMissingReason
        ) {
            return new CoverageEntry(
                    entry.contentId(),
                    entry.domain().id(),
                    entry.displayName(),
                    explicitAssetReferences,
                    AssetCheck.missing(""),
                    model,
                    textures,
                    lang,
                    resolvedModelId,
                    resolverMissingReason
            );
        }

        boolean block() {
            return EchoAdapterCoreDomain.BLOCKS.id().equals(domain);
        }

        boolean item() {
            return EchoAdapterCoreDomain.ITEMS.id().equals(domain);
        }

        boolean texturePresent() {
            return textures.stream().anyMatch(AssetCheck::present);
        }

        boolean complete() {
            boolean coreAssets = item()
                    ? model.present() && texturePresent()
                    : blockstate.present() && model.present() && texturePresent();
            return coreAssets && lang.keyPresent();
        }

        String status() {
            ArrayList<String> gaps = gaps();
            return gaps.isEmpty() ? "complete" : "incomplete";
        }

        ArrayList<String> gaps() {
            ArrayList<String> result = new ArrayList<>();
            if (block() && !blockstate.present()) {
                result.add("missing blockstate");
            }
            if (!model.present()) {
                result.add("missing model");
            }
            if (textures.isEmpty()) {
                result.add("no declared texture");
            } else if (!texturePresent()) {
                result.add("missing texture png");
            }
            if (!lang.filePresent()) {
                result.add("missing lang file");
            } else if (!lang.keyPresent()) {
                result.add("missing lang key");
            }
            if (!resolverMissingReason.isBlank()) {
                result.add("resolver: " + resolverMissingReason);
            }
            return result;
        }

        String toJson(String indent) {
            StringBuilder json = new StringBuilder();
            json.append(indent).append("{\n");
            json.append(indent).append("  \"contentId\": \"").append(escape(contentId)).append("\",\n");
            json.append(indent).append("  \"domain\": \"").append(escape(domain)).append("\",\n");
            json.append(indent).append("  \"displayName\": \"").append(escape(displayName)).append("\",\n");
            json.append(indent).append("  \"status\": \"").append(status()).append("\",\n");
            json.append(indent).append("  \"explicitAssetReferences\": ").append(explicitAssetReferences).append(",\n");
            json.append(indent).append("  \"resolvedModelId\": \"").append(escape(resolvedModelId)).append("\",\n");
            json.append(indent).append("  \"blockstate\": ").append(blockstate.toJson(indent + "  ")).append(",\n");
            json.append(indent).append("  \"model\": ").append(model.toJson(indent + "  ")).append(",\n");
            json.append(indent).append("  \"textures\": [\n");
            for (int i = 0; i < textures.size(); i++) {
                json.append(textures.get(i).toJson(indent + "    "));
                json.append(i + 1 == textures.size() ? "\n" : ",\n");
            }
            json.append(indent).append("  ],\n");
            json.append(indent).append("  \"lang\": ").append(lang.toJson(indent + "  ")).append(",\n");
            json.append(indent).append("  \"gaps\": ").append(stringArray(gaps())).append("\n");
            json.append(indent).append("}");
            return json.toString();
        }
    }

    private record CoverageReport(
            Path standaloneRoot,
            Path echoRoot,
            List<EchoAssetMount> mounts,
            int indexedAssetCount,
            List<String> namespaces,
            List<CoverageEntry> entries
    ) {
        private CoverageReport {
            mounts = List.copyOf(mounts);
            namespaces = List.copyOf(namespaces);
            entries = List.copyOf(entries);
        }

        int totalRegistryEntries() {
            return entries.size();
        }

        int blockEntries() {
            return (int) entries.stream().filter(CoverageEntry::block).count();
        }

        int itemEntries() {
            return (int) entries.stream().filter(CoverageEntry::item).count();
        }

        int explicitAssetRefs() {
            return (int) entries.stream().filter(CoverageEntry::explicitAssetReferences).count();
        }

        int blockstatePresent() {
            return (int) entries.stream().filter(CoverageEntry::block)
                    .filter(entry -> entry.blockstate().present()).count();
        }

        int blockModelPresent() {
            return (int) entries.stream().filter(CoverageEntry::block)
                    .filter(entry -> entry.model().present()).count();
        }

        int itemModelPresent() {
            return (int) entries.stream().filter(CoverageEntry::item)
                    .filter(entry -> entry.model().present()).count();
        }

        int blockTexturePresent() {
            return (int) entries.stream().filter(CoverageEntry::block)
                    .filter(CoverageEntry::texturePresent).count();
        }

        int itemTexturePresent() {
            return (int) entries.stream().filter(CoverageEntry::item)
                    .filter(CoverageEntry::texturePresent).count();
        }

        int totalTextureDeclared() {
            return entries.stream().mapToInt(entry -> entry.textures().size()).sum();
        }

        int totalTexturePresent() {
            return (int) entries.stream()
                    .flatMap(entry -> entry.textures().stream())
                    .filter(AssetCheck::present)
                    .count();
        }

        int langKeysPresent() {
            return (int) entries.stream().filter(entry -> entry.lang().keyPresent()).count();
        }

        int completeEntries() {
            return (int) entries.stream().filter(CoverageEntry::complete).count();
        }

        int incompleteEntries() {
            return totalRegistryEntries() - completeEntries();
        }

        List<String> missingBlockstates() {
            return entries.stream().filter(CoverageEntry::block)
                    .filter(entry -> !entry.blockstate().present())
                    .map(CoverageEntry::contentId)
                    .toList();
        }

        List<String> missingModels() {
            return entries.stream()
                    .filter(entry -> !entry.model().present())
                    .map(CoverageEntry::contentId)
                    .toList();
        }

        List<String> missingTextures() {
            return entries.stream()
                    .filter(entry -> entry.textures().isEmpty() || !entry.texturePresent())
                    .map(CoverageEntry::contentId)
                    .toList();
        }

        List<String> missingLangKeys() {
            return entries.stream()
                    .filter(entry -> !entry.lang().keyPresent())
                    .map(CoverageEntry::contentId)
                    .toList();
        }

        String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"schema\": \"echo.standalone.registry_asset_coverage.v1\",\n");
            json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
            json.append("  \"generator\": \"runStandaloneRegistryAssetCoverageAudit\",\n");
            json.append("  \"phase\": \"3/4\",\n");
            json.append("  \"status\": \"PASS\",\n");
            json.append("  \"coverageComplete\": ").append(incompleteEntries() == 0).append(",\n");
            json.append("  \"summary\": \"Registry-backed block/item asset coverage is audited through the mounted Minecraft resource-pack resolver; incomplete rows remain listed as actionable Phase 3/4 gaps.\",\n");
            json.append("  \"workspace\": {\n");
            json.append("    \"standaloneRoot\": \"").append(escape(standaloneRoot.toString().replace('\\', '/'))).append("\",\n");
            json.append("    \"echoRoot\": \"").append(escape(echoRoot.toString().replace('\\', '/'))).append("\"\n");
            json.append("  },\n");
            json.append("  \"mounts\": [\n");
            for (int i = 0; i < mounts.size(); i++) {
                EchoAssetMount mount = mounts.get(i);
                json.append("    { \"order\": ").append(mount.order())
                        .append(", \"source\": \"").append(escape(mount.source()))
                        .append("\", \"root\": \"").append(escape(mount.root().toString().replace('\\', '/')))
                        .append("\" }");
                json.append(i + 1 == mounts.size() ? "\n" : ",\n");
            }
            json.append("  ],\n");
            json.append("  \"counts\": {\n");
            json.append("    \"indexedAssets\": ").append(indexedAssetCount).append(",\n");
            json.append("    \"namespaces\": ").append(namespaces.size()).append(",\n");
            json.append("    \"registryEntries\": ").append(totalRegistryEntries()).append(",\n");
            json.append("    \"blocks\": ").append(blockEntries()).append(",\n");
            json.append("    \"items\": ").append(itemEntries()).append(",\n");
            json.append("    \"explicitAssetRefs\": ").append(explicitAssetRefs()).append(",\n");
            json.append("    \"conventionInferredEntries\": ").append(totalRegistryEntries() - explicitAssetRefs()).append(",\n");
            json.append("    \"blockstatesPresent\": ").append(blockstatePresent()).append(",\n");
            json.append("    \"blockModelsPresent\": ").append(blockModelPresent()).append(",\n");
            json.append("    \"itemModelsPresent\": ").append(itemModelPresent()).append(",\n");
            json.append("    \"texturesDeclared\": ").append(totalTextureDeclared()).append(",\n");
            json.append("    \"texturesPresent\": ").append(totalTexturePresent()).append(",\n");
            json.append("    \"blockTextureEntriesPresent\": ").append(blockTexturePresent()).append(",\n");
            json.append("    \"itemTextureEntriesPresent\": ").append(itemTexturePresent()).append(",\n");
            json.append("    \"langKeysPresent\": ").append(langKeysPresent()).append(",\n");
            json.append("    \"completeEntries\": ").append(completeEntries()).append(",\n");
            json.append("    \"incompleteEntries\": ").append(incompleteEntries()).append("\n");
            json.append("  },\n");
            json.append("  \"namespaces\": ").append(stringArray(namespaces)).append(",\n");
            json.append("  \"missing\": {\n");
            json.append("    \"blockstates\": ").append(stringArray(missingBlockstates())).append(",\n");
            json.append("    \"models\": ").append(stringArray(missingModels())).append(",\n");
            json.append("    \"textures\": ").append(stringArray(missingTextures())).append(",\n");
            json.append("    \"langKeys\": ").append(stringArray(missingLangKeys())).append("\n");
            json.append("  },\n");
            json.append("  \"entries\": [\n");
            for (int i = 0; i < entries.size(); i++) {
                json.append(entries.get(i).toJson("    "));
                json.append(i + 1 == entries.size() ? "\n" : ",\n");
            }
            json.append("  ]\n");
            json.append("}\n");
            return json.toString();
        }
    }
}
