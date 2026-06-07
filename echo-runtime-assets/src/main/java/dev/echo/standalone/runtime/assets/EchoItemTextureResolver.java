package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EchoItemTextureResolver {
    private static final int MAX_MODEL_PARENT_DEPTH = 32;
    private static final Pattern STRING_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
    private static final Pattern LAYER_TEXTURE_KEY = Pattern.compile("layer(\\d+)");
    private static final Pattern NUMBER_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?)");

    private final EchoMinecraftAssetResolver resolver;

    public EchoItemTextureResolver(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public EchoItemTextureResolution resolve(String itemId) throws IOException {
        EchoItemTextureLayerResolution layers = resolveLayers(itemId);
        return firstTextureResolution(layers);
    }

    public EchoItemTextureResolution resolve(String itemId, Map<String, Double> itemPredicates) throws IOException {
        EchoItemTextureLayerResolution layers = resolveLayers(itemId, itemPredicates);
        return firstTextureResolution(layers);
    }

    private static EchoItemTextureResolution firstTextureResolution(EchoItemTextureLayerResolution layers) {
        Optional<String> textureId = layers.textureIds().stream().findFirst();
        return new EchoItemTextureResolution(
                layers.itemId(),
                layers.modelId(),
                textureId,
                layers.templateKind(),
                layers.parentChain(),
                layers.missingReason()
        );
    }

    public EchoItemTextureLayerResolution resolveLayers(String itemId) throws IOException {
        return resolveLayers(itemId, Map.of());
    }

    public EchoItemTextureLayerResolution resolveLayers(String itemId, Map<String, Double> itemPredicates) throws IOException {
        String normalizedItemId = normalizeId("minecraft", itemId);
        String[] itemParts = splitId(normalizedItemId, "item id");
        String modelId = itemParts[0] + ":item/" + itemParts[1];
        String selectedModelId = selectOverrideModel(modelId, itemPredicates).orElse(modelId);
        ModelTextureScan scan = scanModelTextures(selectedModelId);
        List<String> textureIds = selectTextureLayers(scan.textures(), scan.templateKind());
        if (!textureIds.isEmpty()) {
            return new EchoItemTextureLayerResolution(
                    normalizedItemId,
                    Optional.of(selectedModelId),
                    textureIds,
                    scan.templateKind(),
                    scan.parentChain(),
                    Optional.empty()
            );
        }

        String reason = scan.missingReason()
                .orElse(scan.textures().isEmpty() ? "model has no textures" : "model textures only contain unresolved references");
        return new EchoItemTextureLayerResolution(
                normalizedItemId,
                Optional.of(selectedModelId),
                List.of(),
                scan.templateKind(),
                scan.parentChain(),
                Optional.of(reason)
        );
    }

    private Optional<String> selectOverrideModel(String initialModelId, Map<String, Double> itemPredicates)
            throws IOException {
        Map<String, Double> normalizedPredicates = normalizePredicates(itemPredicates);
        if (normalizedPredicates.isEmpty()) {
            return Optional.empty();
        }
        String[] modelParts = splitId(normalizeId("minecraft", initialModelId), "model id");
        Optional<String> json = resolver.loadItemModel(modelParts[0], modelParts[1]);
        if (json.isEmpty()) {
            return Optional.empty();
        }
        List<ItemModelOverride> overrides = parseOverrides(json.get(), modelParts[0]);
        String selectedModel = "";
        for (ItemModelOverride override : overrides) {
            if (override.matches(normalizedPredicates)) {
                selectedModel = override.modelId();
            }
        }
        return selectedModel.isBlank()
                ? Optional.empty()
                : Optional.of(normalizeId(modelParts[0], selectedModel));
    }

    private ModelTextureScan scanModelTextures(String initialModelId) throws IOException {
        LinkedHashMap<String, TextureReference> textures = new LinkedHashMap<>();
        ArrayList<String> parentChain = new ArrayList<>();
        String current = initialModelId;
        String defaultNamespace = splitId(initialModelId, "model id")[0];
        ModelKind currentKind = modelKind(initialModelId, ModelKind.ITEM);

        for (int depth = 0; depth < MAX_MODEL_PARENT_DEPTH; depth++) {
            String normalizedModelId = normalizeId(defaultNamespace, current);
            if (parentChain.contains(normalizedModelId)) {
                return new ModelTextureScan(textures, parentChain, Optional.empty(), Optional.of("model parent cycle"));
            }
            parentChain.add(normalizedModelId);

            Optional<String> builtInTemplate = builtInTemplateKind(normalizedModelId);
            if (builtInTemplate.isPresent()) {
                return new ModelTextureScan(textures, parentChain, builtInTemplate, Optional.empty());
            }

            String[] modelParts = splitId(normalizedModelId, "model id");
            currentKind = modelKind(modelParts[1], currentKind);
            Optional<String> json = loadModel(modelParts[0], modelParts[1], currentKind);
            if (json.isEmpty()) {
                return new ModelTextureScan(
                        textures,
                        parentChain,
                        Optional.empty(),
                        Optional.of("missing model " + normalizedModelId)
                );
            }

            parseTextureObject(json.get(), modelParts[0]).forEach(textures::putIfAbsent);
            Optional<String> parentId = stringField(json.get(), "parent");
            if (parentId.isEmpty()) {
                return new ModelTextureScan(textures, parentChain, Optional.empty(), Optional.empty());
            }

            current = parentId.get();
            defaultNamespace = modelParts[0];
            currentKind = modelKind(current, currentKind);
        }

        return new ModelTextureScan(textures, parentChain, Optional.empty(), Optional.of("model parent chain too deep"));
    }

    private Optional<String> loadModel(String namespace, String modelId, ModelKind kind) throws IOException {
        return switch (kind) {
            case BLOCK -> resolver.loadBlockModel(namespace, modelId);
            case ITEM -> resolver.loadItemModel(namespace, modelId);
        };
    }

    private static ModelKind modelKind(String modelId, ModelKind fallback) {
        String path = stripNamespace(requireText(modelId, "modelId").replace('\\', '/'));
        if (path.startsWith("block/")) {
            return ModelKind.BLOCK;
        }
        if (path.startsWith("item/")) {
            return ModelKind.ITEM;
        }
        return fallback;
    }

    private static Map<String, TextureReference> parseTextureObject(String json, String namespace) {
        Optional<String> object = objectField(json, "textures");
        if (object.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, TextureReference> textures = new LinkedHashMap<>();
        Matcher matcher = STRING_FIELD.matcher(object.get());
        while (matcher.find()) {
            String key = unescapeJsonString(matcher.group(1));
            String value = unescapeJsonString(matcher.group(2));
            if (!key.isBlank() && !value.isBlank()) {
                textures.put(key, new TextureReference(namespace, value));
            }
        }
        return textures;
    }

    private static List<ItemModelOverride> parseOverrides(String json, String namespace) {
        Optional<String> array = arrayField(json, "overrides");
        if (array.isEmpty()) {
            return List.of();
        }
        ArrayList<ItemModelOverride> overrides = new ArrayList<>();
        String value = array.get();
        int index = 0;
        while (index < value.length()) {
            index = skipWhitespaceAndCommas(value, index);
            if (index >= value.length()) {
                break;
            }
            if (value.charAt(index) != '{') {
                index++;
                continue;
            }
            int end = matching(value, index, '{', '}');
            if (end < 0) {
                break;
            }
            String overrideObject = value.substring(index + 1, end);
            Optional<String> model = stringField(overrideObject, "model");
            Map<String, Double> predicates = parsePredicateObject(overrideObject);
            if (model.isPresent() && !predicates.isEmpty()) {
                overrides.add(new ItemModelOverride(normalizeId(namespace, model.orElseThrow()), predicates));
            }
            index = end + 1;
        }
        return List.copyOf(overrides);
    }

    private static Map<String, Double> parsePredicateObject(String overrideObject) {
        Optional<String> predicateObject = objectField(overrideObject, "predicate");
        if (predicateObject.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Double> predicates = new LinkedHashMap<>();
        Matcher matcher = NUMBER_FIELD.matcher(predicateObject.get());
        while (matcher.find()) {
            String key = normalizePredicateKey(unescapeJsonString(matcher.group(1)));
            if (key.isBlank()) {
                continue;
            }
            try {
                predicates.put(key, Double.parseDouble(matcher.group(2)));
            } catch (NumberFormatException ignored) {
            }
        }
        return Map.copyOf(predicates);
    }

    private static Map<String, Double> normalizePredicates(Map<String, Double> itemPredicates) {
        if (itemPredicates == null || itemPredicates.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Double> predicates = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : itemPredicates.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || !Double.isFinite(entry.getValue())) {
                continue;
            }
            String key = normalizePredicateKey(entry.getKey());
            if (!key.isBlank()) {
                predicates.put(key, entry.getValue());
            }
        }
        return Map.copyOf(predicates);
    }

    private static String normalizePredicateKey(String key) {
        return key == null ? "" : key.replace('\\', '/').trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static Optional<String> selectTexture(
            Map<String, TextureReference> textures,
            Optional<String> templateKind
    ) {
        String[] preferredKeys = preferredTextureKeys(templateKind.orElse(""));
        for (String key : preferredKeys) {
            Optional<String> textureId = resolveTextureKey(textures, key, new LinkedHashSet<>());
            if (textureId.isPresent()) {
                return textureId;
            }
        }
        for (String key : textures.keySet()) {
            Optional<String> textureId = resolveTextureKey(textures, key, new LinkedHashSet<>());
            if (textureId.isPresent()) {
                return textureId;
            }
        }
        return Optional.empty();
    }

    private static List<String> selectTextureLayers(
            Map<String, TextureReference> textures,
            Optional<String> templateKind
    ) {
        if (textures.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> textureIds = new LinkedHashSet<>();
        if (layeredItemTemplate(templateKind.orElse(""))) {
            ArrayList<String> layerKeys = new ArrayList<>();
            for (String key : textures.keySet()) {
                Matcher matcher = LAYER_TEXTURE_KEY.matcher(key);
                if (matcher.matches()) {
                    layerKeys.add(key);
                }
            }
            layerKeys.sort(java.util.Comparator.comparingInt(EchoItemTextureResolver::layerIndex));
            for (String key : layerKeys) {
                resolveTextureKey(textures, key, new LinkedHashSet<>()).ifPresent(textureIds::add);
            }
        }
        if (textureIds.isEmpty()) {
            selectTexture(textures, templateKind).ifPresent(textureIds::add);
        }
        return List.copyOf(textureIds);
    }

    private static boolean layeredItemTemplate(String templateKind) {
        return "generated".equals(templateKind)
                || "handheld".equals(templateKind)
                || "handheld_rod".equals(templateKind);
    }

    private static int layerIndex(String key) {
        Matcher matcher = LAYER_TEXTURE_KEY.matcher(key == null ? "" : key);
        if (!matcher.matches()) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return Integer.MAX_VALUE;
        }
    }

    private static Optional<String> resolveTextureKey(
            Map<String, TextureReference> textures,
            String key,
            Set<String> seen
    ) {
        TextureReference reference = textures.get(key);
        if (reference == null) {
            return Optional.empty();
        }
        String value = reference.value().trim();
        if (value.startsWith("#")) {
            String targetKey = value.substring(1);
            if (targetKey.isBlank() || !seen.add(targetKey)) {
                return Optional.empty();
            }
            return resolveTextureKey(textures, targetKey, seen);
        }
        return Optional.of(normalizeTextureId(reference.namespace(), value));
    }

    private static String[] preferredTextureKeys(String templateKind) {
        return switch (templateKind) {
            case "generated", "handheld", "handheld_rod" ->
                    new String[]{"layer0", "layer1", "layer2", "particle", "all", "side", "north"};
            case "cube_all" -> new String[]{"all", "side", "north", "particle", "up", "down", "south", "east", "west"};
            case "cube", "cube_column" -> new String[]{"all", "side", "north", "south", "east", "west", "up", "down", "end", "particle"};
            default -> new String[]{"layer0", "all", "side", "north", "front", "up", "top", "particle", "layer1", "layer2"};
        };
    }

    private static Optional<String> builtInTemplateKind(String modelId) {
        String[] parts = splitId(modelId, "model id");
        if (!"minecraft".equals(parts[0])) {
            return Optional.empty();
        }
        return switch (parts[1]) {
            case "item/generated" -> Optional.of("generated");
            case "item/handheld" -> Optional.of("handheld");
            case "item/handheld_rod" -> Optional.of("handheld_rod");
            case "block/cube_all" -> Optional.of("cube_all");
            case "block/cube" -> Optional.of("cube");
            case "block/cube_column" -> Optional.of("cube_column");
            default -> Optional.empty();
        };
    }

    private static Optional<String> stringField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = STRING_FIELD.matcher(json);
        while (matcher.find()) {
            if (fieldName.equals(unescapeJsonString(matcher.group(1)))) {
                return Optional.of(unescapeJsonString(matcher.group(2)));
            }
        }
        return Optional.empty();
    }

    private static Optional<String> objectField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Pattern keyPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:");
        Matcher matcher = keyPattern.matcher(json);
        while (matcher.find()) {
            int index = skipWhitespace(json, matcher.end());
            if (index < json.length() && json.charAt(index) == '{') {
                int end = matchingBrace(json, index);
                if (end > index) {
                    return Optional.of(json.substring(index + 1, end));
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> arrayField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Pattern keyPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:");
        Matcher matcher = keyPattern.matcher(json);
        while (matcher.find()) {
            int index = skipWhitespace(json, matcher.end());
            if (index < json.length() && json.charAt(index) == '[') {
                int end = matching(json, index, '[', ']');
                if (end > index) {
                    return Optional.of(json.substring(index + 1, end));
                }
            }
        }
        return Optional.empty();
    }

    private static int matchingBrace(String json, int start) {
        return matching(json, start, '{', '}');
    }

    private static int matching(String json, int start, char open, char close) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = start; index < json.length(); index++) {
            char current = json.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }
            if (current == '"') {
                inString = true;
            } else if (current == open) {
                depth++;
            } else if (current == close) {
                depth--;
                if (depth == 0) {
                    return index;
                }
            }
        }
        return -1;
    }

    private static int skipWhitespace(String value, int index) {
        int current = index;
        while (current < value.length() && Character.isWhitespace(value.charAt(current))) {
            current++;
        }
        return current;
    }

    private static int skipWhitespaceAndCommas(String value, int index) {
        int current = index;
        while (current < value.length()) {
            char ch = value.charAt(current);
            if (!Character.isWhitespace(ch) && ch != ',') {
                break;
            }
            current++;
        }
        return current;
    }

    private static String normalizeTextureId(String defaultNamespace, String textureId) {
        String normalized = requireText(textureId, "textureId").replace('\\', '/').trim();
        String namespace = requireText(defaultNamespace, "defaultNamespace");
        String path = normalized;
        int separator = normalized.indexOf(':');
        if (separator >= 0) {
            namespace = normalized.substring(0, separator);
            path = normalized.substring(separator + 1);
        }
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        return namespace + ":" + requireText(path, "texturePath");
    }

    private static String normalizeId(String defaultNamespace, String id) {
        String normalized = requireText(id, "id").replace('\\', '/').trim();
        int separator = normalized.indexOf(':');
        if (separator >= 0) {
            return requireText(normalized.substring(0, separator), "namespace")
                    + ":"
                    + requireText(normalized.substring(separator + 1), "path");
        }
        return requireText(defaultNamespace, "defaultNamespace") + ":" + normalized;
    }

    private static String stripNamespace(String value) {
        int separator = value.indexOf(':');
        return separator < 0 ? value : value.substring(separator + 1);
    }

    private static String[] splitId(String id, String label) {
        String normalized = requireText(id, label).replace('\\', '/').trim();
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator == normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid namespaced " + label + ": " + id);
        }
        return new String[]{normalized.substring(0, separator), normalized.substring(separator + 1)};
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

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private enum ModelKind {
        BLOCK,
        ITEM
    }

    private record TextureReference(String namespace, String value) {
        private TextureReference {
            namespace = requireText(namespace, "namespace");
            value = requireText(value, "value");
        }
    }

    private record ItemModelOverride(String modelId, Map<String, Double> predicates) {
        private ItemModelOverride {
            modelId = requireText(modelId, "modelId");
            predicates = predicates == null ? Map.of() : Map.copyOf(predicates);
        }

        boolean matches(Map<String, Double> itemPredicates) {
            if (predicates.isEmpty() || itemPredicates == null || itemPredicates.isEmpty()) {
                return false;
            }
            for (Map.Entry<String, Double> predicate : predicates.entrySet()) {
                Optional<Double> value = predicateValue(itemPredicates, predicate.getKey());
                if (value.isEmpty() || value.orElseThrow() + 1.0E-6D < predicate.getValue()) {
                    return false;
                }
            }
            return true;
        }

        private static Optional<Double> predicateValue(Map<String, Double> itemPredicates, String key) {
            Double exact = itemPredicates.get(key);
            if (exact != null) {
                return Optional.of(exact);
            }
            String unnamespaced = stripNamespace(key);
            exact = itemPredicates.get(unnamespaced);
            if (exact != null) {
                return Optional.of(exact);
            }
            exact = itemPredicates.get("minecraft:" + unnamespaced);
            return exact == null ? Optional.empty() : Optional.of(exact);
        }
    }

    private record ModelTextureScan(
            Map<String, TextureReference> textures,
            List<String> parentChain,
            Optional<String> templateKind,
            Optional<String> missingReason
    ) {
        private ModelTextureScan {
            textures = Map.copyOf(textures);
            parentChain = List.copyOf(parentChain);
            templateKind = templateKind == null ? Optional.empty() : templateKind;
            missingReason = missingReason == null ? Optional.empty() : missingReason;
        }
    }

    public record EchoItemTextureResolution(
            String itemId,
            Optional<String> modelId,
            Optional<String> textureId,
            Optional<String> templateKind,
            List<String> parentChain,
            Optional<String> missingReason
    ) {
        public EchoItemTextureResolution {
            itemId = requireText(itemId, "itemId");
            modelId = modelId == null ? Optional.empty() : modelId;
            textureId = textureId == null ? Optional.empty() : textureId;
            templateKind = templateKind == null ? Optional.empty() : templateKind;
            parentChain = parentChain == null ? List.of() : List.copyOf(parentChain);
            missingReason = missingReason == null ? Optional.empty() : missingReason;
        }

        public boolean resolved() {
            return textureId.isPresent();
        }

        public Optional<String> textureNamespace() {
            return textureId.map(value -> splitId(value, "texture id")[0]);
        }

        public Optional<String> texturePath() {
            return textureId.map(value -> splitId(value, "texture id")[1]);
        }
    }

    public record EchoItemTextureLayerResolution(
            String itemId,
            Optional<String> modelId,
            List<String> textureIds,
            Optional<String> templateKind,
            List<String> parentChain,
            Optional<String> missingReason
    ) {
        public EchoItemTextureLayerResolution {
            itemId = requireText(itemId, "itemId");
            modelId = modelId == null ? Optional.empty() : modelId;
            textureIds = textureIds == null ? List.of() : List.copyOf(textureIds);
            templateKind = templateKind == null ? Optional.empty() : templateKind;
            parentChain = parentChain == null ? List.of() : List.copyOf(parentChain);
            missingReason = missingReason == null ? Optional.empty() : missingReason;
        }

        public boolean resolved() {
            return !textureIds.isEmpty();
        }
    }
}
