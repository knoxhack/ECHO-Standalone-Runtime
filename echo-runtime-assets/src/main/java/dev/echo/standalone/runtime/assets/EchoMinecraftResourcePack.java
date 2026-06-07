package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public record EchoMinecraftResourcePack(
        String id,
        Path root,
        String source,
        boolean hasPackMetadata,
        String description,
        int packFormat,
        Set<String> namespaces,
        long textureCount,
        long blockTextureCount,
        long itemTextureCount,
        long animatedTextureMetadataCount,
        long modelCount,
        long blockModelCount,
        long itemModelCount,
        long blockstateCount,
        long langCount,
        long soundsJsonCount,
        long totalAssetCount,
        List<String> issues
) {
    public EchoMinecraftResourcePack {
        id = requireText(id, "id");
        Objects.requireNonNull(root, "root");
        source = requireText(source, "source");
        description = description == null ? "" : description;
        namespaces = Set.copyOf(new TreeSet<>(Objects.requireNonNull(namespaces, "namespaces")));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
        root = root.toAbsolutePath().normalize();
    }

    public static EchoMinecraftResourcePack scan(Path root) throws IOException {
        Objects.requireNonNull(root, "root");
        Path normalizedRoot = root.toAbsolutePath().normalize();
        if (Files.isRegularFile(normalizedRoot) && archivePack(normalizedRoot)) {
            return scanArchive(normalizedRoot);
        }
        if (!Files.isDirectory(normalizedRoot)) {
            return empty(normalizedRoot, "missing directory");
        }

        Path metadataPath = normalizedRoot.resolve("pack.mcmeta");
        boolean hasMetadata = Files.isRegularFile(metadataPath);
        String metadataText = hasMetadata ? Files.readString(metadataPath) : "";
        String description = hasMetadata ? extractString(metadataText, "description") : "";
        int packFormat = hasMetadata ? extractInt(metadataText, "pack_format") : 0;

        TreeSet<String> namespaces = new TreeSet<>();
        Counts counts = new Counts();
        Path assetsRoot = normalizedRoot.resolve("assets");
        if (Files.isDirectory(assetsRoot)) {
            try (var namespaceStream = Files.list(assetsRoot)) {
                for (Path namespaceRoot : namespaceStream.filter(Files::isDirectory).sorted().toList()) {
                    String namespace = namespaceRoot.getFileName().toString();
                    namespaces.add(namespace);
                    scanNamespace(namespaceRoot, counts);
                }
            }
        }

        java.util.ArrayList<String> issues = new java.util.ArrayList<>();
        if (!hasMetadata) {
            issues.add("missing pack.mcmeta");
        }
        if (namespaces.isEmpty()) {
            issues.add("no assets/<namespace> directories");
        }

        return new EchoMinecraftResourcePack(
                packId(normalizedRoot),
                normalizedRoot,
                normalizedRoot.toString(),
                hasMetadata,
                description,
                packFormat,
                namespaces,
                counts.textures,
                counts.blockTextures,
                counts.itemTextures,
                counts.animatedTextureMetadata,
                counts.models,
                counts.blockModels,
                counts.itemModels,
                counts.blockstates,
                counts.lang,
                counts.soundsJson,
                counts.total,
                issues
        );
    }

    private static EchoMinecraftResourcePack scanArchive(Path archive) throws IOException {
        boolean hasMetadata = false;
        String metadataText = "";
        TreeSet<String> namespaces = new TreeSet<>();
        Counts counts = new Counts();
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            ZipEntry metadata = zip.getEntry("pack.mcmeta");
            hasMetadata = metadata != null && !metadata.isDirectory();
            if (hasMetadata) {
                try (var stream = zip.getInputStream(metadata)) {
                    metadataText = new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
            }
            for (ZipEntry entry : zip.stream()
                    .filter(zipEntry -> !zipEntry.isDirectory())
                    .sorted(java.util.Comparator.comparing(ZipEntry::getName))
                    .toList()) {
                String name = entry.getName().replace('\\', '/');
                if (!name.startsWith("assets/")) {
                    continue;
                }
                String[] parts = name.split("/", 3);
                if (parts.length < 3 || parts[1].isBlank()) {
                    continue;
                }
                namespaces.add(parts[1]);
                scanAssetPath(parts[2], counts);
            }
        }
        String description = hasMetadata ? extractString(metadataText, "description") : "";
        int packFormat = hasMetadata ? extractInt(metadataText, "pack_format") : 0;
        java.util.ArrayList<String> issues = new java.util.ArrayList<>();
        if (!hasMetadata) {
            issues.add("missing pack.mcmeta");
        }
        if (namespaces.isEmpty()) {
            issues.add("no assets/<namespace> entries");
        }
        return new EchoMinecraftResourcePack(
                packId(archive),
                archive,
                archive.toString(),
                hasMetadata,
                description,
                packFormat,
                namespaces,
                counts.textures,
                counts.blockTextures,
                counts.itemTextures,
                counts.animatedTextureMetadata,
                counts.models,
                counts.blockModels,
                counts.itemModels,
                counts.blockstates,
                counts.lang,
                counts.soundsJson,
                counts.total,
                issues
        );
    }

    public String menuLabel() {
        if (totalAssetCount <= 0L) {
            return id + " - no Minecraft assets";
        }
        return id + " - " + namespaces.size() + " ns, " + textureCount + " textures";
    }

    public String detail() {
        return "models " + modelCount
                + " | blockstates " + blockstateCount
                + " | lang " + langCount
                + " | sounds " + soundsJsonCount;
    }

    private static EchoMinecraftResourcePack empty(Path root, String issue) {
        return new EchoMinecraftResourcePack(
                packId(root),
                root,
                root.toString(),
                false,
                "",
                0,
                Set.of(),
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                List.of(issue)
        );
    }

    private static void scanNamespace(Path namespaceRoot, Counts counts) throws IOException {
        try (var stream = Files.walk(namespaceRoot)) {
            for (Path file : stream.filter(Files::isRegularFile).sorted().toList()) {
                Path relative = namespaceRoot.relativize(file);
                String path = relative.toString().replace('\\', '/');
                scanAssetPath(path, counts);
            }
        }
    }

    private static void scanAssetPath(String path, Counts counts) {
        counts.total++;
        if (path.startsWith("textures/") && path.endsWith(".png")) {
            counts.textures++;
            if (path.startsWith("textures/block/")) {
                counts.blockTextures++;
            } else if (path.startsWith("textures/item/")) {
                counts.itemTextures++;
            }
        } else if (path.startsWith("textures/") && path.endsWith(".png.mcmeta")) {
            counts.animatedTextureMetadata++;
        } else if (path.startsWith("models/") && path.endsWith(".json")) {
            counts.models++;
            if (path.startsWith("models/block/")) {
                counts.blockModels++;
            } else if (path.startsWith("models/item/")) {
                counts.itemModels++;
            }
        } else if (path.startsWith("blockstates/") && path.endsWith(".json")) {
            counts.blockstates++;
        } else if (path.startsWith("lang/") && path.endsWith(".json")) {
            counts.lang++;
        } else if (path.equals("sounds.json")) {
            counts.soundsJson++;
        }
    }

    private static String extractString(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(quotedKey);
        if (keyIndex < 0) {
            return "";
        }
        int colon = json.indexOf(':', keyIndex + quotedKey.length());
        int firstQuote = colon < 0 ? -1 : json.indexOf('"', colon + 1);
        int secondQuote = firstQuote < 0 ? -1 : json.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) {
            return "";
        }
        return json.substring(firstQuote + 1, secondQuote);
    }

    private static String packId(Path root) {
        Path fileName = root.getFileName();
        if (fileName == null) {
            return root.toString();
        }
        if (!"resources".equals(fileName.toString())) {
            return fileName.toString();
        }
        Path main = root.getParent();
        Path src = main == null ? null : main.getParent();
        Path module = src == null ? null : src.getParent();
        return module == null || module.getFileName() == null ? fileName.toString() : module.getFileName().toString();
    }

    private static boolean archivePack(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".zip") || name.endsWith(".jar");
    }

    private static int extractInt(String json, String key) {
        String quotedKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(quotedKey);
        if (keyIndex < 0) {
            return 0;
        }
        int colon = json.indexOf(':', keyIndex + quotedKey.length());
        if (colon < 0) {
            return 0;
        }
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end <= start) {
            return 0;
        }
        return Integer.parseInt(json.substring(start, end));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static final class Counts {
        private long textures;
        private long blockTextures;
        private long itemTextures;
        private long animatedTextureMetadata;
        private long models;
        private long blockModels;
        private long itemModels;
        private long blockstates;
        private long lang;
        private long soundsJson;
        private long total;
    }
}
