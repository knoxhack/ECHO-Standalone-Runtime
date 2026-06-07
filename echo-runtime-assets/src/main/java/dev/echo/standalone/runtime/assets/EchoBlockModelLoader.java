package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EchoBlockModelLoader {
    private final EchoMinecraftAssetResolver resolver;

    public EchoBlockModelLoader(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public Optional<EchoModelDefinition> load(String namespace, String modelId) throws IOException {
        return resolver.loadBlockModel(namespace, modelId)
                .map(json -> new EchoModelDefinition(namespace, modelId, json, EchoModelParentResolver.parentId(json)));
    }

    public record EchoModelDefinition(String namespace, String modelId, String json, Optional<String> parentId) {
        public EchoModelDefinition {
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
