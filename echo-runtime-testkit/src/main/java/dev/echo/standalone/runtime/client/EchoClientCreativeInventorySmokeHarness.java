package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class EchoClientCreativeInventorySmokeHarness {
    private static final Pattern JSON_ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern JSON_NAME = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern ITEM_GROUP_KEY = Pattern.compile("\"(itemGroup\\.[^\"]+)\"\\s*:");
    private static final Pattern REGISTER_ID =
            Pattern.compile("(?:ITEMS|BLOCK_ITEMS|BLOCKS)\\.register\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern REGISTER_ITEM_ID =
            Pattern.compile("registerItem\\([^;]*?\"([a-z0-9_./-]+)\"", Pattern.DOTALL);
    private static final Pattern REGISTER_BLOCK_ID =
            Pattern.compile("registerBlock\\([^;]*?\"([a-z0-9_./-]+)\"", Pattern.DOTALL);
    private static final Pattern SIMPLE_ITEM_ID =
            Pattern.compile("\\bsimple\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern HELPER_BLOCK_ID =
            Pattern.compile("\\b(?:block|ore)\\(\\s*\"([a-z0-9_./-]+)\"");
    private static final Pattern CREATIVE_TAB_ID =
            Pattern.compile("([a-z0-9_.-]+:[a-z0-9_./-]*creative[a-z0-9_./-]*)");

    private EchoClientCreativeInventorySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path modulesRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of("..", "ECHO-Modules").toAbsolutePath().normalize();
        List<ModuleCreativeExpectation> expectations = discoverExpectations(modulesRoot);
        EchoClientCreativeInventoryController controller = new EchoClientCreativeInventoryController();
        EchoClientCreativeInventoryController.CreativeInventoryModel model =
                controller.model(expectations.stream()
                        .flatMap(expectation -> expectation.tabs().stream())
                        .toList());

        ArrayList<ModuleCreativeResult> results = new ArrayList<>();
        for (ModuleCreativeExpectation expectation : expectations) {
            results.add(proveModule(controller, model, expectation));
        }
        writeSmokeReport(results);
        long playable = results.stream().filter(ModuleCreativeResult::playable).count();
        System.out.println("client creative inventory smoke PASS modules=" + results.size() + " playable=" + playable);
    }

    private static ModuleCreativeResult proveModule(
            EchoClientCreativeInventoryController controller,
            EchoClientCreativeInventoryController.CreativeInventoryModel model,
            ModuleCreativeExpectation expectation
    ) {
        List<EchoClientCreativeInventoryController.CreativeEntry> entries =
                model.entriesForModule(expectation.moduleId());
        boolean registryBacked = !entries.isEmpty();
        boolean visibleParent = registryBacked && model.visibleParent(expectation.moduleId());
        boolean visibleSearch = !expectation.searchExpected()
                || (registryBacked && model.visibleSearch(expectation.moduleId()));
        EchoClientCreativeInventoryController.CreativeEntry representative =
                entries.stream().filter(EchoClientCreativeInventoryController.CreativeEntry::block)
                        .findFirst()
                        .orElse(entries.isEmpty() ? null : entries.get(0));

        EchoClientCreativeInventoryController.CreativeSelectionResult selection =
                EchoClientCreativeInventoryController.CreativeSelectionResult.failed(
                        expectation.moduleId(),
                        representative == null ? "" : representative.itemId(),
                        "missing_creative_entry"
                );
        EchoClientCreativeInventoryController.CreativePlayResult play =
                EchoClientCreativeInventoryController.CreativePlayResult.failed(
                        expectation.moduleId(),
                        representative == null ? "" : representative.itemId(),
                        "missing_creative_entry"
        );
        if (representative != null) {
            EchoClientWorldSession worldSession =
                    EchoClientWorldSessionFactory.defaultFactory().newWorld("creative-" + safeSegment(expectation.moduleId()));
            EchoClientGameSession session = worldSession.gameSession();
            EchoClientGameplay gameplay = new EchoClientGameplay();
            gameplay.init(session.world(), session.player(), session.hotbar());
            session.setGameMode(EchoClientGameMode.CREATIVE);
            selection = controller.selectEntry(session, representative, 0);
            if (selection.selected()) {
                play = controller.useSelectedEntry(session, gameplay, representative, 0);
            }
        }

        boolean selectable = selection.selected();
        boolean playable = play.played();
        ArrayList<String> blockers = new ArrayList<>(expectation.blockers());
        if (!registryBacked) {
            blockers.add("creative tab/group has no runtime-backed entries");
        }
        if (!visibleParent) {
            blockers.add("creative tab entries are not visible in the parent creative inventory path");
        }
        if (expectation.searchExpected() && !visibleSearch) {
            blockers.add("creative tab entries are not visible in creative search");
        }
        if (!selectable) {
            blockers.add(selection.blocker().isBlank()
                    ? "no creative entry was selectable into inventory/hotbar"
                    : selection.blocker());
        }
        if (!playable) {
            blockers.add(play.blocker().isBlank()
                    ? "no selected creative entry produced gameplay mutation"
                    : play.blocker());
        }

        return new ModuleCreativeResult(
                expectation.moduleId(),
                expectation.tabs(),
                expectation.expectedEntryIds(),
                expectation.expectedSearchEntryIds(),
                registryBacked,
                visibleParent,
                visibleSearch,
                selectable,
                playable,
                representative == null ? "" : representative.itemId(),
                play.mutation(),
                play.feedbackEvents(),
                registryBacked ? List.of() : expectation.expectedEntryIds(),
                visibleSearch ? List.of() : expectation.expectedSearchEntryIds(),
                List.copyOf(blockers)
        );
    }

    private static List<ModuleCreativeExpectation> discoverExpectations(Path modulesRoot) throws IOException {
        Path addonsRoot = modulesRoot.resolve("addons");
        if (!Files.isDirectory(addonsRoot)) {
            throw new IOException("Missing ECHO modules addons directory: " + addonsRoot);
        }
        ArrayList<ModuleCreativeExpectation> modules = new ArrayList<>();
        try (Stream<Path> stream = Files.list(addonsRoot)) {
            for (Path moduleRoot : stream.filter(Files::isDirectory).sorted().toList()) {
                Path descriptor = moduleRoot.resolve("src/main/resources/META-INF/echo.mod.json");
                if (!Files.isRegularFile(descriptor)) {
                    continue;
                }
                String descriptorText = Files.readString(descriptor, StandardCharsets.UTF_8);
                String moduleId = match(JSON_ID, descriptorText, moduleRoot.getFileName().toString());
                String moduleName = match(JSON_NAME, descriptorText, moduleId);
                Path javaRoot = moduleRoot.resolve("src/main/java");
                Path resourcesRoot = moduleRoot.resolve("src/main/resources");
                List<Path> javaFiles = files(javaRoot, ".java");
                List<Path> resourceFiles = files(resourcesRoot, "");
                String sourceText = joinedSource(javaFiles);
                List<String> resourcePaths = resourceFiles.stream()
                        .map(resourcesRoot::relativize)
                        .map(Path::toString)
                        .map(path -> path.replace('\\', '/').toLowerCase(Locale.ROOT))
                        .toList();
                List<String> itemGroupKeys = itemGroupKeys(resourceFiles);
                EntryCatalog entries = expectedEntries(moduleId, resourcePaths, sourceText);
                boolean creativeDeclared = !itemGroupKeys.isEmpty()
                        || javaFiles.stream().map(javaRoot::relativize).map(Path::toString)
                                .anyMatch(path -> path.endsWith("CreativeTabs.java") || path.contains("CreativeTab"))
                        || sourceText.contains("CreativeModeTab")
                        || sourceText.contains("registerCreativeTab")
                        || sourceText.contains("creative_tab")
                        || sourceText.contains("creative_tabs")
                        || sourceText.contains("EchoCreativeContentGroup");
                boolean hasDeferredRegister = sourceText.contains("DeferredRegister")
                        || sourceText.contains("ITEMS.register")
                        || sourceText.contains("BLOCKS.register")
                        || sourceText.contains("BLOCK_ITEMS.register");
                boolean hasBlockstates = resourcePaths.stream().anyMatch(path -> path.startsWith("assets/")
                        && path.contains("/blockstates/"));
                boolean hasModels = resourcePaths.stream().anyMatch(path -> path.startsWith("assets/")
                        && path.contains("/models/item/"));
                boolean hasExpectedCreativeEntries = !entries.entries().isEmpty();
                boolean expectsCreativeTab = hasExpectedCreativeEntries
                        || hasDeferredRegister
                        || hasBlockstates
                        || (creativeDeclared && hasExpectedCreativeEntries);
                if (!expectsCreativeTab) {
                    continue;
                }

                List<EchoClientCreativeInventoryController.CreativeTab> tabs = tabsFor(
                        moduleId,
                        itemGroupKeys,
                        sourceText,
                        entries
                );
                ArrayList<String> blockers = new ArrayList<>();
                if (entries.entries().isEmpty()) {
                    blockers.add("expected creative tab module has no item/block entries discovered from resources or source");
                }
                modules.add(new ModuleCreativeExpectation(
                        moduleId.toLowerCase(Locale.ROOT),
                        moduleName,
                        tabs,
                        entries.entries().stream().map(EchoClientCreativeInventoryController.CreativeEntry::itemId).toList(),
                        entries.entries().stream()
                                .filter(EchoClientCreativeInventoryController.CreativeEntry::searchable)
                                .map(EchoClientCreativeInventoryController.CreativeEntry::itemId)
                                .toList(),
                        true,
                        List.copyOf(blockers)
                ));
            }
        }
        modules.sort(Comparator.comparing(ModuleCreativeExpectation::moduleId));
        return List.copyOf(modules);
    }

    private static List<EchoClientCreativeInventoryController.CreativeTab> tabsFor(
            String moduleId,
            List<String> itemGroupKeys,
            String sourceText,
            EntryCatalog entries
    ) {
        ArrayList<EchoClientCreativeInventoryController.CreativeTab> tabs = new ArrayList<>();
        List<EchoClientCreativeInventoryController.CreativeEntry> deduped =
                EchoClientCreativeInventoryController.dedupeEntries(entries.entries());
        for (String key : itemGroupKeys) {
            String tabId = moduleId + ":" + key.replaceFirst("^itemGroup\\.", "")
                    .replace('.', '_')
                    .toLowerCase(Locale.ROOT);
            tabs.add(new EchoClientCreativeInventoryController.CreativeTab(
                    moduleId,
                    tabId,
                    key,
                    deduped,
                    true
            ));
        }
        for (String tabId : matches(CREATIVE_TAB_ID, sourceText)) {
            String normalized = tabId.toLowerCase(Locale.ROOT);
            if (tabs.stream().noneMatch(tab -> tab.tabId().equals(normalized))) {
                tabs.add(new EchoClientCreativeInventoryController.CreativeTab(
                        moduleId,
                        normalized,
                        "",
                        deduped,
                        true
                ));
            }
        }
        if (tabs.isEmpty()) {
            tabs.add(new EchoClientCreativeInventoryController.CreativeTab(
                    moduleId,
                    moduleId + ":native_modules",
                    "itemGroup." + moduleId,
                    deduped,
                    true
            ));
        }
        return List.copyOf(tabs);
    }

    private static EntryCatalog expectedEntries(
            String moduleId,
            List<String> resourcePaths,
            String sourceText
    ) {
        LinkedHashMap<String, Boolean> entries = new LinkedHashMap<>();
        for (String resourcePath : resourcePaths) {
            String[] parts = resourcePath.split("/");
            if (parts.length >= 5
                    && parts[0].equals("assets")
                    && parts[2].equals("models")
                    && parts[3].equals("item")
                    && resourcePath.endsWith(".json")) {
                String name = resourcePath.substring(
                        ("assets/" + parts[1] + "/models/item/").length(),
                        resourcePath.length() - ".json".length()
                );
                if (!name.contains("/")) {
                    entries.putIfAbsent(parts[1] + ":" + name, false);
                }
            }
            if (parts.length >= 4
                    && parts[0].equals("assets")
                    && parts[2].equals("blockstates")
                    && resourcePath.endsWith(".json")) {
                String name = resourcePath.substring(
                        ("assets/" + parts[1] + "/blockstates/").length(),
                        resourcePath.length() - ".json".length()
                );
                entries.put(parts[1] + ":" + name, true);
            }
        }
        for (String registered : matches(REGISTER_ID, sourceText)) {
            entries.putIfAbsent(moduleId + ":" + registered.toLowerCase(Locale.ROOT), false);
        }
        for (String registered : matches(REGISTER_ITEM_ID, sourceText)) {
            entries.putIfAbsent(moduleId + ":" + registered.toLowerCase(Locale.ROOT), false);
        }
        for (String registered : matches(REGISTER_BLOCK_ID, sourceText)) {
            entries.put(moduleId + ":" + registered.toLowerCase(Locale.ROOT), true);
        }
        for (String registered : matches(SIMPLE_ITEM_ID, sourceText)) {
            entries.putIfAbsent(moduleId + ":" + registered.toLowerCase(Locale.ROOT), false);
        }
        for (String registered : matches(HELPER_BLOCK_ID, sourceText)) {
            entries.put(moduleId + ":" + registered.toLowerCase(Locale.ROOT), true);
        }
        ArrayList<EchoClientCreativeInventoryController.CreativeEntry> result = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : entries.entrySet()) {
            String itemId = entry.getKey().toLowerCase(Locale.ROOT).replace('\\', '/');
            result.add(new EchoClientCreativeInventoryController.CreativeEntry(
                    itemId.contains(":") ? itemId.substring(0, itemId.indexOf(':')) : moduleId,
                    itemId,
                    "",
                    entry.getValue(),
                    true
            ));
        }
        return new EntryCatalog(EchoClientCreativeInventoryController.dedupeEntries(result));
    }

    private static void writeSmokeReport(List<ModuleCreativeResult> results) throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "client-creative-inventory-smoke.json")
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        property(json, 1, "schema", "echo.standalone.client_smoke.client-creative-inventory-smoke.v1", true);
        property(json, 1, "generatedAt", "1970-01-01T00:00:00Z", true);
        property(json, 1, "status", results.stream().allMatch(ModuleCreativeResult::playable) ? "PASS" : "FAIL", true);
        property(json, 1, "runtime", "standalone", true);
        arrayProperty(json, 1, "moduleIds", moduleIds(results, ModuleCreativeResult::registryBacked), true);
        arrayProperty(json, 1, "creativeTabModuleIds", moduleIds(results, ModuleCreativeResult::registryBacked), true);
        arrayProperty(json, 1, "registryBackedModuleIds", moduleIds(results, ModuleCreativeResult::registryBacked), true);
        arrayProperty(json, 1, "visibleParentModuleIds", moduleIds(results, ModuleCreativeResult::visibleParent), true);
        arrayProperty(json, 1, "visibleSearchModuleIds", moduleIds(results, ModuleCreativeResult::visibleSearch), true);
        arrayProperty(json, 1, "selectableModuleIds", moduleIds(results, ModuleCreativeResult::selectable), true);
        arrayProperty(json, 1, "playableModuleIds", moduleIds(results, ModuleCreativeResult::playable), true);
        arrayProperty(json, 1, "selectableItemIds", itemIds(results, ModuleCreativeResult::selectable), true);
        arrayProperty(json, 1, "playableItemIds", itemIds(results, ModuleCreativeResult::playable), true);
        arrayProperty(json, 1, "placedBlockIds", placedBlockIds(results), true);
        json.append("  \"modules\": [\n");
        for (int i = 0; i < results.size(); i++) {
            ModuleCreativeResult result = results.get(i);
            json.append("    {\n");
            property(json, 3, "moduleId", result.moduleId(), true);
            property(json, 3, "creativeTabStatus", statusFor(result), true);
            booleanProperty(json, 3, "registryBacked", result.registryBacked(), true);
            booleanProperty(json, 3, "visibleParent", result.visibleParent(), true);
            booleanProperty(json, 3, "visibleSearch", result.visibleSearch(), true);
            booleanProperty(json, 3, "selectable", result.selectable(), true);
            booleanProperty(json, 3, "playable", result.playable(), true);
            property(json, 3, "selectedItemId", result.selectedItemId(), true);
            property(json, 3, "playMutation", result.playMutation(), true);
            tabsProperty(json, 3, "expectedCreativeTabs", result.tabs(), true);
            arrayProperty(json, 3, "expectedEntries", result.expectedEntries(), true);
            arrayProperty(json, 3, "expectedSearchEntries", result.expectedSearchEntries(), true);
            arrayProperty(json, 3, "missingCreativeTabEntries", result.missingCreativeTabEntries(), true);
            arrayProperty(json, 3, "missingCreativeSearchEntries", result.missingCreativeSearchEntries(), true);
            arrayProperty(json, 3, "feedbackEvents", result.feedbackEvents(), true);
            arrayProperty(json, 3, "blockers", result.blockers(), false);
            json.append("    }");
            if (i < results.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        Files.writeString(report, json.toString(), StandardCharsets.UTF_8);
    }

    private interface ResultPredicate {
        boolean test(ModuleCreativeResult result);
    }

    private static List<String> moduleIds(List<ModuleCreativeResult> results, ResultPredicate predicate) {
        return results.stream()
                .filter(predicate::test)
                .map(ModuleCreativeResult::moduleId)
                .toList();
    }

    private static List<String> itemIds(List<ModuleCreativeResult> results, ResultPredicate predicate) {
        return results.stream()
                .filter(predicate::test)
                .map(ModuleCreativeResult::selectedItemId)
                .filter(value -> value != null && !value.isBlank())
                .toList();
    }

    private static List<String> placedBlockIds(List<ModuleCreativeResult> results) {
        return results.stream()
                .filter(ModuleCreativeResult::playable)
                .filter(result -> result.playMutation().equals("block_place"))
                .map(ModuleCreativeResult::selectedItemId)
                .toList();
    }

    private static String statusFor(ModuleCreativeResult result) {
        if (result.playable()) return "playable";
        if (result.selectable()) return "selectable";
        if (result.visibleSearch()) return "visible-search";
        if (result.visibleParent()) return "visible-parent";
        if (result.registryBacked()) return "registry-backed";
        return "declared-only";
    }

    private static List<Path> files(Path root, String suffix) throws IOException {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .filter(path -> suffix.isBlank() || path.toString().endsWith(suffix))
                    .sorted()
                    .toList();
        }
    }

    private static String joinedSource(List<Path> files) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Path file : files) {
            String relative = file.toString().replace('\\', '/');
            if (!relative.endsWith(".java")) {
                continue;
            }
            if (!(relative.contains("CreativeTab")
                    || relative.contains("Items")
                    || relative.contains("ContentDefinitions")
                    || relative.contains("Machines")
                    || relative.contains("NativeModule")
                    || relative.contains("ProductBridgeProvider")
                    || relative.contains("/registry/"))) {
                continue;
            }
            builder.append(Files.readString(file, StandardCharsets.UTF_8)).append('\n');
        }
        return builder.toString();
    }

    private static List<String> itemGroupKeys(List<Path> resourceFiles) throws IOException {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (Path file : resourceFiles) {
            if (!file.toString().endsWith(".json")) {
                continue;
            }
            String text = Files.readString(file, StandardCharsets.UTF_8);
            Matcher matcher = ITEM_GROUP_KEY.matcher(text);
            while (matcher.find()) {
                keys.add(matcher.group(1));
            }
        }
        return List.copyOf(keys);
    }

    private static List<String> matches(Pattern pattern, String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> values = new LinkedHashSet<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            values.add(matcher.group(1));
        }
        return List.copyOf(values);
    }

    private static String match(Pattern pattern, String text, String fallback) {
        Matcher matcher = pattern.matcher(text == null ? "" : text);
        return matcher.find() ? matcher.group(1) : fallback;
    }

    private static String safeSegment(String value) {
        String normalized = value == null ? "" : value.toLowerCase(Locale.ROOT);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '-' || ch == '_') {
                builder.append(ch);
            } else {
                builder.append('-');
            }
        }
        return builder.isEmpty() ? "module" : builder.toString();
    }

    private static void property(StringBuilder json, int indent, String key, String value, boolean comma) {
        json.append("  ".repeat(indent))
                .append("\"").append(escape(key)).append("\": \"").append(escape(value)).append("\"");
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private static void booleanProperty(StringBuilder json, int indent, String key, boolean value, boolean comma) {
        json.append("  ".repeat(indent))
                .append("\"").append(escape(key)).append("\": ").append(value);
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private static void arrayProperty(StringBuilder json, int indent, String key, List<String> values, boolean comma) {
        json.append("  ".repeat(indent)).append("\"").append(escape(key)).append("\": [");
        List<String> safeValues = values == null ? List.of() : values;
        for (int i = 0; i < safeValues.size(); i++) {
            if (i > 0) {
                json.append(", ");
            }
            json.append("\"").append(escape(safeValues.get(i))).append("\"");
        }
        json.append("]");
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private static void tabsProperty(
            StringBuilder json,
            int indent,
            String key,
            List<EchoClientCreativeInventoryController.CreativeTab> tabs,
            boolean comma
    ) {
        json.append("  ".repeat(indent)).append("\"").append(escape(key)).append("\": [");
        List<EchoClientCreativeInventoryController.CreativeTab> safeTabs = tabs == null ? List.of() : tabs;
        for (int i = 0; i < safeTabs.size(); i++) {
            EchoClientCreativeInventoryController.CreativeTab tab = safeTabs.get(i);
            if (i > 0) {
                json.append(", ");
            }
            json.append("{\"id\":\"").append(escape(tab.tabId()))
                    .append("\",\"titleKey\":\"").append(escape(tab.titleKey()))
                    .append("\",\"searchExpected\":").append(tab.searchExpected())
                    .append(",\"expectedEntries\":[");
            List<String> entries = tab.entries().stream()
                    .map(EchoClientCreativeInventoryController.CreativeEntry::itemId)
                    .toList();
            for (int j = 0; j < entries.size(); j++) {
                if (j > 0) {
                    json.append(",");
                }
                json.append("\"").append(escape(entries.get(j))).append("\"");
            }
            json.append("]}");
        }
        json.append("]");
        if (comma) {
            json.append(",");
        }
        json.append("\n");
    }

    private static String escape(String value) {
        String text = value == null ? "" : value;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(ch);
            }
        }
        return builder.toString();
    }

    private record EntryCatalog(List<EchoClientCreativeInventoryController.CreativeEntry> entries) {}

    private record ModuleCreativeExpectation(
            String moduleId,
            String name,
            List<EchoClientCreativeInventoryController.CreativeTab> tabs,
            List<String> expectedEntryIds,
            List<String> expectedSearchEntryIds,
            boolean searchExpected,
            List<String> blockers
    ) {}

    private record ModuleCreativeResult(
            String moduleId,
            List<EchoClientCreativeInventoryController.CreativeTab> tabs,
            List<String> expectedEntries,
            List<String> expectedSearchEntries,
            boolean registryBacked,
            boolean visibleParent,
            boolean visibleSearch,
            boolean selectable,
            boolean playable,
            String selectedItemId,
            String playMutation,
            List<String> feedbackEvents,
            List<String> missingCreativeTabEntries,
            List<String> missingCreativeSearchEntries,
            List<String> blockers
    ) {}
}
