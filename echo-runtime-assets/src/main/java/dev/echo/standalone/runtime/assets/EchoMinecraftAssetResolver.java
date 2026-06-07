package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EchoMinecraftAssetResolver {
    private final EchoAssetResolver resolver;

    public EchoMinecraftAssetResolver(EchoAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public Optional<EchoAssetEntry> blockstate(String namespace, String blockId) {
        return resolve(namespace, "blockstates/" + ensureSuffix(stripNamespace(blockId), ".json"));
    }

    public Optional<EchoAssetEntry> blockModel(String namespace, String modelId) {
        return resolve(namespace, "models/block/" + ensureSuffix(modelPath("block", modelId), ".json"));
    }

    public Optional<EchoAssetEntry> itemModel(String namespace, String modelId) {
        return resolve(namespace, "models/item/" + ensureSuffix(modelPath("item", modelId), ".json"));
    }

    public Optional<EchoAssetEntry> texture(String namespace, String texturePath) {
        return resolve(namespace, "textures/" + ensureSuffix(stripNamespace(texturePath), ".png"));
    }

    public Optional<EchoAssetEntry> textureMetadata(String namespace, String texturePath) {
        return resolve(namespace, "textures/" + ensureSuffix(stripNamespace(texturePath), ".png") + ".mcmeta");
    }

    public Optional<EchoAssetEntry> lang(String namespace, String locale) {
        return resolve(namespace, "lang/" + ensureSuffix(locale, ".json"));
    }

    public Optional<EchoAssetEntry> sounds(String namespace) {
        return resolve(namespace, "sounds.json");
    }

    public Optional<String> loadBlockstate(String namespace, String blockId) throws IOException {
        return load(namespace, "blockstates/" + ensureSuffix(stripNamespace(blockId), ".json"));
    }

    public Optional<String> loadBlockModel(String namespace, String modelId) throws IOException {
        return load(namespace, "models/block/" + ensureSuffix(modelPath("block", modelId), ".json"));
    }

    public Optional<String> loadItemModel(String namespace, String modelId) throws IOException {
        return load(namespace, "models/item/" + ensureSuffix(modelPath("item", modelId), ".json"));
    }

    public Optional<String> loadTextureMetadata(String namespace, String texturePath) throws IOException {
        return load(namespace, "textures/" + ensureSuffix(stripNamespace(texturePath), ".png") + ".mcmeta");
    }

    public Optional<String> loadLang(String namespace, String locale) throws IOException {
        return load(namespace, "lang/" + ensureSuffix(locale, ".json"));
    }

    public java.util.List<String> loadAllLang(String namespace, String locale) throws IOException {
        return resolver.loadAllText(logicalId(namespace, "lang/" + ensureSuffix(locale, ".json")));
    }

    public Optional<String> loadSounds(String namespace) throws IOException {
        return load(namespace, "sounds.json");
    }

    private Optional<EchoAssetEntry> resolve(String namespace, String path) {
        return resolver.resolve(logicalId(namespace, path));
    }

    private Optional<String> load(String namespace, String path) throws IOException {
        return resolver.loadText(logicalId(namespace, path));
    }

    private static String logicalId(String namespace, String path) {
        return requireText(namespace, "namespace") + ":" + requireText(path, "path").replace('\\', '/');
    }

    private static String stripNamespace(String value) {
        String normalized = requireText(value, "value").replace('\\', '/');
        int separator = normalized.indexOf(':');
        return separator < 0 ? normalized : normalized.substring(separator + 1);
    }

    private static String modelPath(String kind, String modelId) {
        String path = stripNamespace(modelId);
        String prefix = kind + "/";
        return path.startsWith(prefix) ? path.substring(prefix.length()) : path;
    }

    private static String ensureSuffix(String value, String suffix) {
        String normalized = requireText(value, "value");
        return normalized.endsWith(suffix) ? normalized : normalized + suffix;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
