package dev.echo.standalone.runtime.assets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EchoBlockstateModelSelector {
    private static final Pattern STRING_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");

    private EchoBlockstateModelSelector() {
    }

    public static Optional<String> firstModelId(String json) {
        return select(json).map(EchoBlockstateModelSelection::modelId);
    }

    public static Optional<String> firstModelId(String json, Map<String, String> stateProperties) {
        return select(json, stateProperties).map(EchoBlockstateModelSelection::modelId);
    }

    public static Optional<EchoBlockstateModelSelection> select(String json) {
        return select(json, Map.of());
    }

    public static Optional<EchoBlockstateModelSelection> select(String json, Map<String, String> stateProperties) {
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        Map<String, String> normalizedState = normalizedState(stateProperties);
        Optional<String> variants = valueField(json, "variants");
        if (variants.isPresent()) {
            Optional<EchoBlockstateModelSelection> model = selectFromVariants(variants.get(), normalizedState);
            if (model.isPresent()) {
                return model;
            }
        }
        Optional<String> multipart = valueField(json, "multipart");
        if (multipart.isPresent()) {
            Optional<EchoBlockstateModelSelection> model = selectFromMultipart(multipart.get(), normalizedState);
            if (model.isPresent()) {
                return model;
            }
        }
        return stringField(json, "model")
                .map(modelId -> new EchoBlockstateModelSelection(modelId, "fallback"));
    }

    public static List<EchoBlockstateModelSelection> selectAll(String json, Map<String, String> stateProperties) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        Map<String, String> normalizedState = normalizedState(stateProperties);
        Optional<String> variants = valueField(json, "variants");
        if (variants.isPresent()) {
            return select(json, normalizedState).stream().toList();
        }
        Optional<String> multipart = valueField(json, "multipart");
        if (multipart.isPresent()) {
            List<EchoBlockstateModelSelection> models = selectAllFromMultipart(multipart.get(), normalizedState);
            if (!models.isEmpty()) {
                return models;
            }
        }
        return stringField(json, "model")
                .map(modelId -> List.of(new EchoBlockstateModelSelection(modelId, "fallback")))
                .orElseGet(List::of);
    }

    private static Optional<EchoBlockstateModelSelection> selectFromVariants(
            String variantsValue,
            Map<String, String> stateProperties
    ) {
        String trimmed = variantsValue.trim();
        if (!trimmed.startsWith("{")) {
            return selectModelFromValue(trimmed, "variants");
        }
        List<JsonObjectEntry> entries = objectEntries(trimmed);
        if (!stateProperties.isEmpty()) {
            for (JsonObjectEntry entry : entries) {
                if (!entry.key().isEmpty() && variantKeyMatches(entry.key(), stateProperties)) {
                    Optional<EchoBlockstateModelSelection> model =
                            selectModelFromValue(entry.value(), "variants." + entry.key());
                    if (model.isPresent()) {
                        return model;
                    }
                }
            }
        }
        for (JsonObjectEntry entry : entries) {
            if (entry.key().isEmpty()) {
                Optional<EchoBlockstateModelSelection> model =
                        selectModelFromValue(entry.value(), "variants.default");
                if (model.isPresent()) {
                    return model;
                }
            }
        }
        for (JsonObjectEntry entry : entries) {
            Optional<EchoBlockstateModelSelection> model =
                    selectModelFromValue(entry.value(), "variants." + entry.key());
            if (model.isPresent()) {
                return model;
            }
        }
        return Optional.empty();
    }

    private static Optional<EchoBlockstateModelSelection> selectFromMultipart(
            String multipartValue,
            Map<String, String> stateProperties
    ) {
        String trimmed = multipartValue.trim();
        if (!trimmed.startsWith("[")) {
            return selectModelFromValue(trimmed, "multipart");
        }
        for (String part : arrayValues(trimmed)) {
            Optional<String> when = valueField(part, "when");
            if (!stateProperties.isEmpty() && when.isPresent() && !whenMatches(when.get(), stateProperties)) {
                continue;
            }
            Optional<String> apply = valueField(part, "apply");
            Optional<EchoBlockstateModelSelection> model = apply
                    .flatMap(value -> selectModelFromValue(value, "multipart.apply"))
                    .or(() -> selectModelFromValue(part, "multipart"));
            if (model.isPresent()) {
                return model;
            }
        }
        return Optional.empty();
    }

    private static List<EchoBlockstateModelSelection> selectAllFromMultipart(
            String multipartValue,
            Map<String, String> stateProperties
    ) {
        String trimmed = multipartValue.trim();
        if (!trimmed.startsWith("[")) {
            return selectModelsFromValue(trimmed, "multipart");
        }
        ArrayList<EchoBlockstateModelSelection> result = new ArrayList<>();
        for (String part : arrayValues(trimmed)) {
            Optional<String> when = valueField(part, "when");
            if (!stateProperties.isEmpty() && when.isPresent() && !whenMatches(when.get(), stateProperties)) {
                continue;
            }
            Optional<String> apply = valueField(part, "apply");
            List<EchoBlockstateModelSelection> models = apply
                    .map(value -> selectModelsFromValue(value, "multipart.apply"))
                    .orElseGet(() -> selectModelsFromValue(part, "multipart"));
            result.addAll(models);
        }
        return List.copyOf(result);
    }

    private static boolean variantKeyMatches(String key, Map<String, String> stateProperties) {
        String normalized = key == null ? "" : key.trim();
        if (normalized.isBlank()) {
            return stateProperties.isEmpty();
        }
        String[] clauses = normalized.split(",");
        for (String clause : clauses) {
            int separator = clause.indexOf('=');
            if (separator < 1 || separator == clause.length() - 1) {
                return false;
            }
            String property = clause.substring(0, separator).trim();
            String expected = clause.substring(separator + 1).trim();
            if (!expected.equals(stateProperties.get(property))) {
                return false;
            }
        }
        return true;
    }

    private static boolean whenMatches(String whenValue, Map<String, String> stateProperties) {
        String trimmed = whenValue == null ? "" : whenValue.trim();
        if (trimmed.isBlank()) {
            return true;
        }
        if (trimmed.startsWith("{")) {
            for (JsonObjectEntry entry : objectEntries(trimmed)) {
                String key = entry.key();
                if ("OR".equalsIgnoreCase(key)) {
                    boolean any = false;
                    for (String value : arrayValues(entry.value())) {
                        any = any || whenMatches(value, stateProperties);
                    }
                    if (!any) {
                        return false;
                    }
                } else if ("AND".equalsIgnoreCase(key)) {
                    for (String value : arrayValues(entry.value())) {
                        if (!whenMatches(value, stateProperties)) {
                            return false;
                        }
                    }
                } else if (!propertyConditionMatches(key, entry.value(), stateProperties)) {
                    return false;
                }
            }
            return true;
        }
        if (trimmed.startsWith("[")) {
            for (String value : arrayValues(trimmed)) {
                if (whenMatches(value, stateProperties)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    private static boolean propertyConditionMatches(
            String property,
            String expectedValue,
            Map<String, String> stateProperties
    ) {
        String actual = stateProperties.get(property);
        if (actual == null) {
            return false;
        }
        String trimmed = expectedValue == null ? "" : expectedValue.trim();
        if (trimmed.startsWith("\"")) {
            int end = matchingString(trimmed, 0);
            if (end > 0) {
                String expected = unescapeJsonString(trimmed.substring(1, end));
                return propertyValueMatches(actual, expected);
            }
        }
        if (trimmed.startsWith("[")) {
            for (String value : arrayValues(trimmed)) {
                if (propertyConditionMatches(property, value, stateProperties)) {
                    return true;
                }
            }
            return false;
        }
        return propertyValueMatches(actual, trimmed);
    }

    private static boolean propertyValueMatches(String actual, String expected) {
        String normalizedExpected = expected == null ? "" : expected.trim();
        if (normalizedExpected.contains("|")) {
            for (String choice : normalizedExpected.split("\\|")) {
                if (actual.equals(choice.trim())) {
                    return true;
                }
            }
            return false;
        }
        return actual.equals(normalizedExpected);
    }

    private static Optional<EchoBlockstateModelSelection> selectModelFromValue(String value, String source) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isBlank()) {
            return Optional.empty();
        }
        if (trimmed.startsWith("\"")) {
            int end = matchingString(trimmed, 0);
            if (end > 0) {
                return Optional.of(new EchoBlockstateModelSelection(
                        unescapeJsonString(trimmed.substring(1, end)),
                        source
                ));
            }
        }
        if (trimmed.startsWith("[")) {
            for (String element : arrayValues(trimmed)) {
                Optional<EchoBlockstateModelSelection> model = selectModelFromValue(element, source);
                if (model.isPresent()) {
                    return model;
                }
            }
            return Optional.empty();
        }
        return stringField(trimmed, "model")
                .map(modelId -> new EchoBlockstateModelSelection(
                        modelId,
                        source,
                        intField(trimmed, "x").orElse(0),
                        intField(trimmed, "y").orElse(0),
                        booleanField(trimmed, "uvlock").orElse(false)
                ));
    }

    private static List<EchoBlockstateModelSelection> selectModelsFromValue(String value, String source) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isBlank()) {
            return List.of();
        }
        if (trimmed.startsWith("[")) {
            ArrayList<EchoBlockstateModelSelection> result = new ArrayList<>();
            for (String element : arrayValues(trimmed)) {
                result.addAll(selectModelsFromValue(element, source));
            }
            return List.copyOf(result);
        }
        return selectModelFromValue(trimmed, source).stream().toList();
    }

    private static Optional<String> valueField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Pattern keyPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:");
        Matcher matcher = keyPattern.matcher(json);
        while (matcher.find()) {
            int start = skipWhitespace(json, matcher.end());
            int end = valueEnd(json, start);
            if (end > start) {
                return Optional.of(json.substring(start, end));
            }
        }
        return Optional.empty();
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

    private static Optional<Integer> intField(String json, String fieldName) {
        Optional<String> value = valueField(json, fieldName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value.get().trim()));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private static Optional<Boolean> booleanField(String json, String fieldName) {
        Optional<String> value = valueField(json, fieldName);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        String normalized = value.get().trim();
        if ("true".equalsIgnoreCase(normalized)) {
            return Optional.of(true);
        }
        if ("false".equalsIgnoreCase(normalized)) {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private static List<JsonObjectEntry> objectEntries(String objectValue) {
        String value = objectValue == null ? "" : objectValue.trim();
        if (value.length() < 2 || value.charAt(0) != '{') {
            return List.of();
        }
        int objectEnd = matchingBrace(value, 0);
        if (objectEnd < 0) {
            return List.of();
        }
        ArrayList<JsonObjectEntry> entries = new ArrayList<>();
        int index = skipWhitespace(value, 1);
        while (index < objectEnd) {
            if (value.charAt(index) == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            if (value.charAt(index) != '"') {
                break;
            }
            int keyEnd = matchingString(value, index);
            if (keyEnd <= index) {
                break;
            }
            String key = unescapeJsonString(value.substring(index + 1, keyEnd));
            int colon = skipWhitespace(value, keyEnd + 1);
            if (colon >= objectEnd || value.charAt(colon) != ':') {
                break;
            }
            int valueStart = skipWhitespace(value, colon + 1);
            int valueEnd = valueEnd(value, valueStart);
            if (valueEnd <= valueStart) {
                break;
            }
            entries.add(new JsonObjectEntry(key, value.substring(valueStart, valueEnd)));
            index = skipWhitespace(value, valueEnd);
        }
        return List.copyOf(entries);
    }

    private static List<String> arrayValues(String arrayValue) {
        String value = arrayValue == null ? "" : arrayValue.trim();
        if (value.length() < 2 || value.charAt(0) != '[') {
            return List.of();
        }
        int arrayEnd = matchingBracket(value, 0);
        if (arrayEnd < 0) {
            return List.of();
        }
        ArrayList<String> values = new ArrayList<>();
        int index = skipWhitespace(value, 1);
        while (index < arrayEnd) {
            if (value.charAt(index) == ',') {
                index = skipWhitespace(value, index + 1);
                continue;
            }
            int end = valueEnd(value, index);
            if (end <= index) {
                break;
            }
            values.add(value.substring(index, end));
            index = skipWhitespace(value, end);
        }
        return List.copyOf(values);
    }

    private static int valueEnd(String json, int start) {
        if (json == null || start < 0 || start >= json.length()) {
            return -1;
        }
        char current = json.charAt(start);
        if (current == '{') {
            int end = matchingBrace(json, start);
            return end < 0 ? -1 : end + 1;
        }
        if (current == '[') {
            int end = matchingBracket(json, start);
            return end < 0 ? -1 : end + 1;
        }
        if (current == '"') {
            int end = matchingString(json, start);
            return end < 0 ? -1 : end + 1;
        }
        int index = start;
        while (index < json.length()) {
            char ch = json.charAt(index);
            if (ch == ',' || ch == '}' || ch == ']') {
                break;
            }
            index++;
        }
        return index;
    }

    private static int matchingBrace(String json, int start) {
        return matchingDelimited(json, start, '{', '}');
    }

    private static int matchingBracket(String json, int start) {
        return matchingDelimited(json, start, '[', ']');
    }

    private static int matchingDelimited(String json, int start, char open, char close) {
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

    private static int matchingString(String json, int start) {
        boolean escaped = false;
        for (int index = start + 1; index < json.length(); index++) {
            char current = json.charAt(index);
            if (escaped) {
                escaped = false;
            } else if (current == '\\') {
                escaped = true;
            } else if (current == '"') {
                return index;
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

    private static Map<String, String> normalizedState(Map<String, String> stateProperties) {
        if (stateProperties == null || stateProperties.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        stateProperties.entrySet().stream()
                .filter(entry -> entry.getKey() != null
                        && !entry.getKey().isBlank()
                        && entry.getValue() != null
                        && !entry.getValue().isBlank())
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> result.put(entry.getKey().trim(), entry.getValue().trim()));
        return Map.copyOf(result);
    }

    public record EchoBlockstateModelSelection(
            String modelId,
            String source,
            int xRotationDegrees,
            int yRotationDegrees,
            boolean uvLock
    ) {
        public EchoBlockstateModelSelection(String modelId, String source) {
            this(modelId, source, 0, 0, false);
        }

        public EchoBlockstateModelSelection {
            modelId = requireText(modelId, "modelId");
            source = source == null || source.isBlank() ? "unknown" : source;
            xRotationDegrees = normalizeRotation(xRotationDegrees);
            yRotationDegrees = normalizeRotation(yRotationDegrees);
        }
    }

    private static int normalizeRotation(int degrees) {
        int normalized = Math.floorMod(degrees, 360);
        return normalized % 90 == 0 ? normalized : 0;
    }

    private record JsonObjectEntry(String key, String value) {
        private JsonObjectEntry {
            key = key == null ? "" : key;
            value = value == null ? "" : value;
        }
    }
}
