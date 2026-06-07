package dev.echo.standalone.runtime.client;

import org.lwjgl.glfw.GLFW;

import java.util.List;

enum EchoClientKeyAction {
    MOVE_FORWARD("move_forward", "Move Forward", GLFW.GLFW_KEY_W),
    MOVE_BACKWARD("move_backward", "Move Backward", GLFW.GLFW_KEY_S),
    STRAFE_LEFT("strafe_left", "Strafe Left", GLFW.GLFW_KEY_A),
    STRAFE_RIGHT("strafe_right", "Strafe Right", GLFW.GLFW_KEY_D),
    JUMP("jump", "Jump", GLFW.GLFW_KEY_SPACE),
    CROUCH("crouch", "Crouch", GLFW.GLFW_KEY_LEFT_SHIFT),
    SPRINT("sprint", "Sprint", GLFW.GLFW_KEY_LEFT_CONTROL),
    PAUSE("pause", "Pause", GLFW.GLFW_KEY_ESCAPE),
    OPEN_INVENTORY("open_inventory", "Inventory", GLFW.GLFW_KEY_E),
    DROP_ITEM("drop_item", "Drop Item", GLFW.GLFW_KEY_Q),
    SWAP_OFFHAND("swap_offhand", "Swap Offhand", GLFW.GLFW_KEY_F),
    DEBUG_OVERLAY("debug_overlay", "Debug Overlay", GLFW.GLFW_KEY_F3),
    SCREENSHOT("screenshot", "Screenshot", GLFW.GLFW_KEY_F2),
    TOGGLE_FULLSCREEN("toggle_fullscreen", "Toggle Fullscreen", GLFW.GLFW_KEY_F11),
    SAVE_SESSION("save_session", "Quick Save", GLFW.GLFW_KEY_P),
    LOAD_SESSION("load_session", "Quick Load", GLFW.GLFW_KEY_O),
    HOTBAR_1("hotbar_1", "Hotbar 1", GLFW.GLFW_KEY_1),
    HOTBAR_2("hotbar_2", "Hotbar 2", GLFW.GLFW_KEY_2),
    HOTBAR_3("hotbar_3", "Hotbar 3", GLFW.GLFW_KEY_3),
    HOTBAR_4("hotbar_4", "Hotbar 4", GLFW.GLFW_KEY_4),
    HOTBAR_5("hotbar_5", "Hotbar 5", GLFW.GLFW_KEY_5),
    HOTBAR_6("hotbar_6", "Hotbar 6", GLFW.GLFW_KEY_6),
    HOTBAR_7("hotbar_7", "Hotbar 7", GLFW.GLFW_KEY_7),
    HOTBAR_8("hotbar_8", "Hotbar 8", GLFW.GLFW_KEY_8),
    HOTBAR_9("hotbar_9", "Hotbar 9", GLFW.GLFW_KEY_9);

    private static final List<EchoClientKeyAction> CONTROLS_SCREEN_ACTIONS = List.of(
            MOVE_FORWARD,
            MOVE_BACKWARD,
            STRAFE_LEFT,
            STRAFE_RIGHT,
            JUMP,
            CROUCH,
            SPRINT,
            OPEN_INVENTORY,
            PAUSE,
            DROP_ITEM,
            SWAP_OFFHAND,
            DEBUG_OVERLAY,
            SCREENSHOT,
            TOGGLE_FULLSCREEN,
            SAVE_SESSION,
            LOAD_SESSION
    );

    private static final List<EchoClientKeyAction> HOTBAR_ACTIONS = List.of(
            HOTBAR_1,
            HOTBAR_2,
            HOTBAR_3,
            HOTBAR_4,
            HOTBAR_5,
            HOTBAR_6,
            HOTBAR_7,
            HOTBAR_8,
            HOTBAR_9
    );

    private final String id;
    private final String displayName;
    private final int defaultKey;

    EchoClientKeyAction(String id, String displayName, int defaultKey) {
        this.id = id;
        this.displayName = displayName;
        this.defaultKey = defaultKey;
    }

    String id() {
        return id;
    }

    String displayName() {
        return displayName;
    }

    int defaultKey() {
        return defaultKey;
    }

    static EchoClientKeyAction byId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        String normalized = id.trim().toLowerCase(java.util.Locale.ROOT);
        for (EchoClientKeyAction action : values()) {
            if (action.id.equals(normalized) || action.name().equalsIgnoreCase(normalized)) {
                return action;
            }
        }
        return null;
    }

    static List<EchoClientKeyAction> controlsScreenActions() {
        return CONTROLS_SCREEN_ACTIONS;
    }

    static List<EchoClientKeyAction> hotbarActions() {
        return HOTBAR_ACTIONS;
    }
}
