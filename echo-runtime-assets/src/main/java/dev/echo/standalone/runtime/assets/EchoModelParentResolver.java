package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EchoModelParentResolver {
    private final EchoMinecraftAssetResolver resolver;

    public EchoModelParentResolver(EchoMinecraftAssetResolver resolver) {
        this.resolver = java.util.Objects.requireNonNull(resolver, "resolver");
    }

    public EchoModelParentChain resolveBlockModel(String namespace, String modelId) throws IOException {
        ArrayList<String> visited = new ArrayList<>();
        ArrayList<String> missing = new ArrayList<>();
        String current = modelId;
        for (int depth = 0; depth < 32; depth++) {
            String normalized = normalizeModelId(namespace, current);
            if (visited.contains(normalized)) {
                return new EchoModelParentChain(visited, missing, true);
            }
            visited.add(normalized);
            String[] parts = splitId(normalized);
            Optional<String> json = resolver.loadBlockModel(parts[0], parts[1]);
            if (json.isEmpty()) {
                missing.add(normalized);
                return new EchoModelParentChain(visited, missing, false);
            }
            Optional<String> parent = parentId(json.get());
            if (parent.isEmpty()) {
                return new EchoModelParentChain(visited, missing, false);
            }
            current = parent.get();
        }
        return new EchoModelParentChain(visited, missing, true);
    }

    public static Optional<String> parentId(String json) {
        return stringField(json, "parent");
    }

    public static Optional<String> stringField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return Optional.empty();
        }
        int colon = json.indexOf(':', keyIndex + key.length());
        int firstQuote = colon < 0 ? -1 : json.indexOf('"', colon + 1);
        int secondQuote = firstQuote < 0 ? -1 : json.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) {
            return Optional.empty();
        }
        return Optional.of(json.substring(firstQuote + 1, secondQuote));
    }

    private static String normalizeModelId(String namespace, String modelId) {
        if (modelId.contains(":")) {
            return modelId;
        }
        return namespace + ":" + modelId;
    }

    private static String[] splitId(String id) {
        int separator = id.indexOf(':');
        if (separator < 1 || separator == id.length() - 1) {
            throw new IllegalArgumentException("Invalid namespaced model id: " + id);
        }
        return new String[]{id.substring(0, separator), id.substring(separator + 1)};
    }

    public record EchoModelParentChain(List<String> visitedModelIds, List<String> missingModelIds, boolean cycleDetected) {
        public EchoModelParentChain {
            visitedModelIds = List.copyOf(visitedModelIds);
            missingModelIds = List.copyOf(missingModelIds);
        }
    }
}
