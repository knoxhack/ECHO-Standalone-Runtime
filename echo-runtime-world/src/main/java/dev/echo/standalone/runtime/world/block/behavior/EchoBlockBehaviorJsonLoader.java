package dev.echo.standalone.runtime.world.block.behavior;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads {@link EchoBlockBehavior} records from simple JSON objects.
 *
 * <p>This loader is intentionally minimal and does not depend on a JSON library. It recognizes
 * numeric and string fields and falls back to defaults for missing keys.
 */
public final class EchoBlockBehaviorJsonLoader {

    private EchoBlockBehaviorJsonLoader() {
    }

    public static EchoBlockBehavior load(String blockId, String json) {
        if (blockId == null || blockId.isBlank()) {
            throw new IllegalArgumentException("blockId must not be blank");
        }
        double hardness = readDouble(json, "hardness", 0.0D);
        double blastResistance = readDouble(json, "blastResistance", 0.0D);
        String harvestTool = readString(json, "harvestTool", "");
        int harvestLevel = readInt(json, "harvestLevel", 0);
        int lightEmission = readInt(json, "lightEmission", 0);
        int lightOpacity = readInt(json, "lightOpacity", 15);
        double friction = readDouble(json, "friction", 0.6D);
        double jumpFactor = readDouble(json, "jumpFactor", 1.0D);
        double speedFactor = readDouble(json, "speedFactor", 1.0D);
        boolean randomTick = readBoolean(json, "randomTick", false);
        boolean solid = readBoolean(json, "solid", true);
        boolean opaque = readBoolean(json, "opaque", true);
        boolean requiresTool = readBoolean(json, "requiresTool", false);
        boolean blocksMotion = readBoolean(json, "blocksMotion", solid);
        boolean flammable = readBoolean(json, "flammable", false);
        int fireSpreadSpeed = readInt(json, "fireSpreadSpeed", 0);

        return new EchoBlockBehavior(
                blockId, hardness, blastResistance, harvestTool, harvestLevel,
                lightEmission, lightOpacity, friction, jumpFactor, speedFactor,
                randomTick, solid, opaque, requiresTool, blocksMotion, flammable, fireSpreadSpeed
        );
    }

    public static EchoBlockBehavior load(String blockId, InputStream in) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return load(blockId, sb.toString());
        }
    }

    private static double readDouble(String json, String key, double defaultValue) {
        String value = extractValue(json, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int readInt(String json, String key, int defaultValue) {
        String value = extractValue(json, key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean readBoolean(String json, String key, boolean defaultValue) {
        String value = extractValue(json, key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }

    private static String readString(String json, String key, String defaultValue) {
        String value = extractString(json, key);
        return value == null ? defaultValue : value;
    }

    private static String extractValue(String json, String key) {
        // Match the key followed by colon and optional whitespace, then capture the value
        // until the next comma or closing brace.
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([^,}]+)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).trim();
    }

    private static String extractString(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }
}
