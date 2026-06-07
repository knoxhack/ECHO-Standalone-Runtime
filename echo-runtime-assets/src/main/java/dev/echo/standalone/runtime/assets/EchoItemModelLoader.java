package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EchoItemModelLoader {
    private final EchoMinecraftAssetResolver resolver;

    public EchoItemModelLoader(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public Optional<EchoItemModelDefinition> load(String namespace, String modelId) throws IOException {
        return resolver.loadItemModel(namespace, modelId)
                .map(json -> new EchoItemModelDefinition(namespace, modelId, json, EchoModelParentResolver.parentId(json)));
    }

    public record EchoItemModelDefinition(String namespace, String modelId, String json, Optional<String> parentId) {
        public EchoItemModelDefinition {
            namespace = requireText(namespace, "namespace");
            modelId = requireText(modelId, "modelId");
            json = json == null ? "" : json;
            parentId = parentId == null ? Optional.empty() : parentId;
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
