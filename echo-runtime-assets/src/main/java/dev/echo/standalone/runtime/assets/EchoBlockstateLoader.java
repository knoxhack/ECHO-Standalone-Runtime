package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EchoBlockstateLoader {
    private final EchoMinecraftAssetResolver resolver;

    public EchoBlockstateLoader(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public Optional<EchoBlockstateDefinition> load(String namespace, String blockId) throws IOException {
        return resolver.loadBlockstate(namespace, blockId)
                .map(json -> new EchoBlockstateDefinition(namespace, blockId, json));
    }

    public record EchoBlockstateDefinition(String namespace, String blockId, String json) {
        public EchoBlockstateDefinition {
            namespace = requireText(namespace, "namespace");
            blockId = requireText(blockId, "blockId");
            json = json == null ? "" : json;
        }

        public Optional<String> selectedModelId() {
            return EchoBlockstateModelSelector.firstModelId(json);
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
