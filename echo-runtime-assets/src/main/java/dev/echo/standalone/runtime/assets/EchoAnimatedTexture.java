package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record EchoAnimatedTexture(
        String namespace,
        String texturePath,
        boolean animated,
        String metadataJson,
        int frameTimeTicks,
        boolean interpolate,
        List<EchoAnimationFrame> frames
) {
    private static final Pattern INTEGER_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*(-?\\d+)");
    private static final Pattern BOOLEAN_FIELD =
            Pattern.compile("\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"\\s*:\\s*(true|false)");

    public EchoAnimatedTexture {
        namespace = requireText(namespace, "namespace");
        texturePath = requireText(texturePath, "texturePath");
        metadataJson = metadataJson == null ? "" : metadataJson;
        frameTimeTicks = Math.max(1, frameTimeTicks);
        frames = frames == null ? List.of() : List.copyOf(frames);
    }

    public static EchoAnimatedTexture load(EchoMinecraftAssetResolver resolver, String namespace, String texturePath)
            throws IOException {
        Objects.requireNonNull(resolver, "resolver");
        Optional<String> metadata = resolver.loadTextureMetadata(namespace, texturePath);
        return fromMetadata(namespace, texturePath, metadata.orElse(""));
    }

    public static EchoAnimatedTexture fromMetadata(String namespace, String texturePath, String metadataJson) {
        String metadata = metadataJson == null ? "" : metadataJson;
        Optional<String> animation = objectField(metadata, "animation");
        int frameTimeTicks = animation.map(json -> intField(json, "frametime", 1)).orElse(1);
        boolean interpolate = animation.map(json -> booleanField(json, "interpolate")).orElse(false);
        List<EchoAnimationFrame> frames = animation.map(json -> parseFrames(json, frameTimeTicks)).orElse(List.of());
        return new EchoAnimatedTexture(
                namespace,
                texturePath,
                !metadata.isBlank(),
                metadata,
                frameTimeTicks,
                interpolate,
                frames
        );
    }

    public int effectiveFrameCount() {
        return frames.isEmpty() ? 0 : frames.size();
    }

    private static List<EchoAnimationFrame> parseFrames(String animationJson, int defaultFrameTimeTicks) {
        Optional<String> array = arrayField(animationJson, "frames");
        if (array.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoAnimationFrame> frames = new ArrayList<>();
        String value = array.get();
        int index = 0;
        while (index < value.length()) {
            index = skipWhitespaceAndCommas(value, index);
            if (index >= value.length()) {
                break;
            }
            char current = value.charAt(index);
            if (current == '{') {
                int end = matching(value, index, '{', '}');
                if (end < 0) {
                    break;
                }
                String object = value.substring(index + 1, end);
                int frameIndex = intField(object, "index", -1);
                if (frameIndex >= 0) {
                    frames.add(new EchoAnimationFrame(frameIndex, intField(object, "time", defaultFrameTimeTicks)));
                }
                index = end + 1;
            } else if (current == '-' || Character.isDigit(current)) {
                int end = index + 1;
                while (end < value.length() && Character.isDigit(value.charAt(end))) {
                    end++;
                }
                try {
                    int frameIndex = Integer.parseInt(value.substring(index, end));
                    if (frameIndex >= 0) {
                        frames.add(new EchoAnimationFrame(frameIndex, defaultFrameTimeTicks));
                    }
                } catch (NumberFormatException ignored) {
                    break;
                }
                index = end;
            } else {
                index++;
            }
        }
        return List.copyOf(frames);
    }

    private static int intField(String json, String fieldName, int fallback) {
        Matcher matcher = INTEGER_FIELD.matcher(json == null ? "" : json);
        while (matcher.find()) {
            if (fieldName.equals(unescapeJsonString(matcher.group(1)))) {
                try {
                    return Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException ignored) {
                    return fallback;
                }
            }
        }
        return fallback;
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

    private static Optional<String> objectField(String json, String fieldName) {
        if (json == null || fieldName == null || fieldName.isBlank()) {
            return Optional.empty();
        }
        Pattern keyPattern = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:");
        Matcher matcher = keyPattern.matcher(json);
        while (matcher.find()) {
            int index = skipWhitespace(json, matcher.end());
            if (index < json.length() && json.charAt(index) == '{') {
                int end = matching(json, index, '{', '}');
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

    public record EchoAnimationFrame(int index, int timeTicks) {
        public EchoAnimationFrame {
            if (index < 0) {
                throw new IllegalArgumentException("index must not be negative");
            }
            timeTicks = Math.max(1, timeTicks);
        }
    }
}
