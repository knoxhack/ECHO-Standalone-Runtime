package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EchoSoundsJsonLoader {
    private static final Pattern STRING_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"");
    private static final Pattern BOOLEAN_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*(true|false)");

    private final EchoMinecraftAssetResolver resolver;

    public EchoSoundsJsonLoader(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public EchoSoundsDefinition load(String namespace) throws IOException {
        String normalizedNamespace = requireText(namespace, "namespace");
        Optional<String> json = resolver.loadSounds(normalizedNamespace);
        if (json.isEmpty()) {
            return new EchoSoundsDefinition(normalizedNamespace, List.of());
        }

        ArrayList<EchoSoundEventDefinition> events = new ArrayList<>();
        for (Map.Entry<String, String> entry : topLevelObjectEntries(json.get()).entrySet()) {
            String eventPath = unescapeJsonString(entry.getKey());
            String eventJson = entry.getValue();
            events.add(new EchoSoundEventDefinition(
                    normalizedNamespace + ":" + eventPath,
                    optionalStringField(eventJson, "subtitle").orElse(""),
                    booleanField(eventJson, "replace"),
                    parseSoundVariants(normalizedNamespace, eventJson)
            ));
        }
        return new EchoSoundsDefinition(normalizedNamespace, events);
    }

    private static List<EchoSoundVariant> parseSoundVariants(String namespace, String eventJson) {
        Optional<String> array = arrayField(eventJson, "sounds");
        if (array.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoSoundVariant> variants = new ArrayList<>();
        int index = 0;
        String value = array.get();
        while (index < value.length()) {
            index = skipWhitespaceAndCommas(value, index);
            if (index >= value.length()) {
                break;
            }
            char current = value.charAt(index);
            if (current == '"') {
                int end = closingQuote(value, index);
                if (end < 0) {
                    break;
                }
                String name = normalizeSoundName(namespace, unescapeJsonString(value.substring(index + 1, end)));
                variants.add(new EchoSoundVariant(name, "sound", false));
                index = end + 1;
            } else if (current == '{') {
                int end = matching(value, index, '{', '}');
                if (end < 0) {
                    break;
                }
                String object = value.substring(index + 1, end);
                String name = normalizeSoundName(
                        namespace,
                        optionalStringField(object, "name").orElse("")
                );
                if (!name.isBlank()) {
                    variants.add(new EchoSoundVariant(
                            name,
                            optionalStringField(object, "type").orElse("sound"),
                            booleanField(object, "stream")
                    ));
                }
                index = end + 1;
            } else {
                index++;
            }
        }
        return List.copyOf(variants);
    }

    private static Map<String, String> topLevelObjectEntries(String json) {
        LinkedHashMap<String, String> entries = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        int rootStart = skipWhitespace(json, 0);
        if (rootStart >= json.length() || json.charAt(rootStart) != '{') {
            return Map.of();
        }
        int rootEnd = matching(json, rootStart, '{', '}');
        if (rootEnd <= rootStart) {
            return Map.of();
        }
        int index = rootStart + 1;
        while (index < rootEnd) {
            index = skipWhitespaceAndCommas(json, index);
            if (index >= rootEnd || json.charAt(index) != '"') {
                break;
            }
            int keyEnd = closingQuote(json, index);
            if (keyEnd < 0) {
                break;
            }
            String key = json.substring(index + 1, keyEnd);
            int colon = skipWhitespace(json, keyEnd + 1);
            if (colon >= rootEnd || json.charAt(colon) != ':') {
                break;
            }
            int valueStart = skipWhitespace(json, colon + 1);
            if (valueStart >= rootEnd || json.charAt(valueStart) != '{') {
                index = valueStart + 1;
                continue;
            }
            int valueEnd = matching(json, valueStart, '{', '}');
            if (valueEnd < 0) {
                break;
            }
            entries.put(key, json.substring(valueStart + 1, valueEnd));
            index = valueEnd + 1;
        }
        return Map.copyOf(entries);
    }

    private static Optional<String> optionalStringField(String json, String fieldName) {
        Matcher matcher = STRING_FIELD.matcher(json == null ? "" : json);
        while (matcher.find()) {
            if (fieldName.equals(unescapeJsonString(matcher.group(1)))) {
                return Optional.of(unescapeJsonString(matcher.group(2)));
            }
        }
        return Optional.empty();
    }

    private static boolean booleanField(String json, String fieldName) {
        Matcher matcher = BOOLEAN_FIELD.matcher(json == null ? "" : json);
        while (matcher.find()) {
            if (fieldName.equals(unescapeJsonString(matcher.group(1)))) {
                return Boolean.parseBoolean(matcher.group(2));
            }
        }
        return false;
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

    private static int closingQuote(String value, int quoteStart) {
        boolean escaped = false;
        for (int index = quoteStart + 1; index < value.length(); index++) {
            char current = value.charAt(index);
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

    private static String normalizeSoundName(String namespace, String name) {
        String normalized = name == null ? "" : name.trim().replace('\\', '/');
        if (normalized.isBlank()) {
            return "";
        }
        if (normalized.indexOf(':') >= 0) {
            return normalized;
        }
        return requireText(namespace, "namespace") + ":" + normalized;
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

    public record EchoSoundsDefinition(String namespace, List<EchoSoundEventDefinition> events) {
        public EchoSoundsDefinition {
            namespace = requireText(namespace, "namespace");
            events = events == null ? List.of() : List.copyOf(events);
        }

        public Optional<EchoSoundEventDefinition> findEvent(String eventId) {
            if (eventId == null || eventId.isBlank()) {
                return Optional.empty();
            }
            return events.stream()
                    .filter(event -> event.eventId().equals(eventId))
                    .findFirst();
        }
    }

    public record EchoSoundEventDefinition(
            String eventId,
            String subtitle,
            boolean replace,
            List<EchoSoundVariant> sounds
    ) {
        public EchoSoundEventDefinition {
            eventId = requireText(eventId, "eventId");
            subtitle = subtitle == null ? "" : subtitle;
            sounds = sounds == null ? List.of() : List.copyOf(sounds);
        }
    }

    public record EchoSoundVariant(String name, String type, boolean stream) {
        public EchoSoundVariant {
            name = requireText(name, "name");
            type = type == null || type.isBlank() ? "sound" : type.trim();
        }
    }
}
