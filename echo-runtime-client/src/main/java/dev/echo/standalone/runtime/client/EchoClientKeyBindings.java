package dev.echo.standalone.runtime.client;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

final class EchoClientKeyBindings {
    private final EnumMap<EchoClientKeyAction, Integer> keys;

    private EchoClientKeyBindings(Map<EchoClientKeyAction, Integer> keys) {
        this.keys = defaultMap();
        if (keys != null) {
            for (Map.Entry<EchoClientKeyAction, Integer> entry : keys.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null && validKey(entry.getValue())) {
                    this.keys.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    static EchoClientKeyBindings defaults() {
        return new EchoClientKeyBindings(Map.of());
    }

    static EchoClientKeyBindings decode(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return defaults();
        }
        EnumMap<EchoClientKeyAction, Integer> parsed = defaultMap();
        for (String token : encoded.split("[;,]")) {
            if (token.isBlank()) {
                continue;
            }
            String[] parts = token.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            EchoClientKeyAction action = EchoClientKeyAction.byId(parts[0]);
            if (action == null) {
                continue;
            }
            parsed.put(action, parseKey(parts[1], action.defaultKey()));
        }
        return new EchoClientKeyBindings(parsed);
    }

    EchoClientKeyBindings normalized() {
        return new EchoClientKeyBindings(keys);
    }

    EchoClientKeyBindings withKey(EchoClientKeyAction action, int glfwKey) {
        if (action == null || !validKey(glfwKey)) {
            return this;
        }
        EnumMap<EchoClientKeyAction, Integer> next = new EnumMap<>(keys);
        next.put(action, glfwKey);
        return new EchoClientKeyBindings(next);
    }

    int key(EchoClientKeyAction action) {
        if (action == null) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        return keys.getOrDefault(action, action.defaultKey());
    }

    boolean matches(EchoClientKeyAction action, int glfwKey) {
        return validKey(glfwKey) && key(action) == glfwKey;
    }

    static List<Integer> configurableKeys() {
        ArrayList<Integer> keys = new ArrayList<>();
        for (int key = GLFW.GLFW_KEY_A; key <= GLFW.GLFW_KEY_Z; key++) {
            keys.add(key);
        }
        for (int key = GLFW.GLFW_KEY_0; key <= GLFW.GLFW_KEY_9; key++) {
            keys.add(key);
        }
        keys.add(GLFW.GLFW_KEY_SPACE);
        keys.add(GLFW.GLFW_KEY_ENTER);
        keys.add(GLFW.GLFW_KEY_TAB);
        keys.add(GLFW.GLFW_KEY_BACKSPACE);
        keys.add(GLFW.GLFW_KEY_LEFT_SHIFT);
        keys.add(GLFW.GLFW_KEY_RIGHT_SHIFT);
        keys.add(GLFW.GLFW_KEY_LEFT_CONTROL);
        keys.add(GLFW.GLFW_KEY_RIGHT_CONTROL);
        keys.add(GLFW.GLFW_KEY_LEFT_ALT);
        keys.add(GLFW.GLFW_KEY_RIGHT_ALT);
        keys.add(GLFW.GLFW_KEY_UP);
        keys.add(GLFW.GLFW_KEY_DOWN);
        keys.add(GLFW.GLFW_KEY_LEFT);
        keys.add(GLFW.GLFW_KEY_RIGHT);
        for (int key = GLFW.GLFW_KEY_F1; key <= GLFW.GLFW_KEY_F12; key++) {
            keys.add(key);
        }
        return List.copyOf(keys);
    }

    String label(EchoClientKeyAction action) {
        return keyLabel(key(action));
    }

    String hotbarSummary() {
        StringJoiner joiner = new StringJoiner(" ");
        for (EchoClientKeyAction action : EchoClientKeyAction.hotbarActions()) {
            joiner.add(label(action));
        }
        return joiner.toString();
    }

    String encode() {
        StringJoiner joiner = new StringJoiner(";");
        for (EchoClientKeyAction action : EchoClientKeyAction.values()) {
            joiner.add(action.id() + "=" + keyToken(key(action)));
        }
        return joiner.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EchoClientKeyBindings that)) {
            return false;
        }
        return keys.equals(that.keys);
    }

    @Override
    public int hashCode() {
        return keys.hashCode();
    }

    private static EnumMap<EchoClientKeyAction, Integer> defaultMap() {
        EnumMap<EchoClientKeyAction, Integer> defaults = new EnumMap<>(EchoClientKeyAction.class);
        for (EchoClientKeyAction action : EchoClientKeyAction.values()) {
            defaults.put(action, action.defaultKey());
        }
        return defaults;
    }

    private static boolean validKey(int glfwKey) {
        return glfwKey != GLFW.GLFW_KEY_UNKNOWN;
    }

    private static int parseKey(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        if (normalized.startsWith("KEY_")) {
            try {
                return Integer.parseInt(normalized.substring("KEY_".length()));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        if (normalized.length() == 1) {
            char c = normalized.charAt(0);
            if (c >= 'A' && c <= 'Z') {
                return GLFW.GLFW_KEY_A + (c - 'A');
            }
            if (c >= '0' && c <= '9') {
                return GLFW.GLFW_KEY_0 + (c - '0');
            }
        }
        return switch (normalized) {
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "ESC", "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "ENTER", "RETURN" -> GLFW.GLFW_KEY_ENTER;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "LEFT_SHIFT", "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RIGHT_SHIFT", "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LEFT_CONTROL", "LEFT_CTRL", "LCTRL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "RIGHT_CONTROL", "RIGHT_CTRL", "RCTRL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LEFT_ALT", "LALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "RIGHT_ALT", "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "F1" -> GLFW.GLFW_KEY_F1;
            case "F2" -> GLFW.GLFW_KEY_F2;
            case "F3" -> GLFW.GLFW_KEY_F3;
            case "F4" -> GLFW.GLFW_KEY_F4;
            case "F5" -> GLFW.GLFW_KEY_F5;
            case "F6" -> GLFW.GLFW_KEY_F6;
            case "F7" -> GLFW.GLFW_KEY_F7;
            case "F8" -> GLFW.GLFW_KEY_F8;
            case "F9" -> GLFW.GLFW_KEY_F9;
            case "F10" -> GLFW.GLFW_KEY_F10;
            case "F11" -> GLFW.GLFW_KEY_F11;
            case "F12" -> GLFW.GLFW_KEY_F12;
            default -> fallback;
        };
    }

    private static String keyToken(int glfwKey) {
        String label = keyLabel(glfwKey).toUpperCase(java.util.Locale.ROOT)
                .replace(' ', '_');
        return label.startsWith("KEY_") ? label : label;
    }

    static String keyLabel(int glfwKey) {
        if (glfwKey >= GLFW.GLFW_KEY_A && glfwKey <= GLFW.GLFW_KEY_Z) {
            return Character.toString((char) ('A' + glfwKey - GLFW.GLFW_KEY_A));
        }
        if (glfwKey >= GLFW.GLFW_KEY_0 && glfwKey <= GLFW.GLFW_KEY_9) {
            return Character.toString((char) ('0' + glfwKey - GLFW.GLFW_KEY_0));
        }
        return switch (glfwKey) {
            case GLFW.GLFW_KEY_SPACE -> "Space";
            case GLFW.GLFW_KEY_ESCAPE -> "Esc";
            case GLFW.GLFW_KEY_ENTER -> "Enter";
            case GLFW.GLFW_KEY_TAB -> "Tab";
            case GLFW.GLFW_KEY_BACKSPACE -> "Backspace";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "Left Shift";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "Right Shift";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "Left Ctrl";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "Right Ctrl";
            case GLFW.GLFW_KEY_LEFT_ALT -> "Left Alt";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "Right Alt";
            case GLFW.GLFW_KEY_UP -> "Up";
            case GLFW.GLFW_KEY_DOWN -> "Down";
            case GLFW.GLFW_KEY_LEFT -> "Left";
            case GLFW.GLFW_KEY_RIGHT -> "Right";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            default -> "Key " + glfwKey;
        };
    }
}
