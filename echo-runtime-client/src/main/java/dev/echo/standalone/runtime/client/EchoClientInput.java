package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

/**
 * Polls GLFW input and produces {@link EchoVoxelPlayerInput} each frame.
 * Supports mouse-lock (GLFW_CURSOR_DISABLED) for camera look and WASD movement.
 */
final class EchoClientInput implements EchoClientGameplayInput {
    private static final int DEFAULT_MOUSE_SENSITIVITY_PERCENT = 50;
    private static final double MOUSE_LOOK_SENSITIVITY = 0.15D;

    private final long window;
    private double lastMouseX;
    private double lastMouseY;
    private boolean firstMouse = true;
    private boolean cursorLocked = false;
    private final Set<Integer> pressedKeys = new HashSet<>();

    // Action triggers (single-shot per press)
    private boolean breakTriggered = false;
    private boolean placeTriggered = false;
    private boolean saveTriggered = false;
    private boolean loadTriggered = false;
    private int hotbarDelta = 0;
    private double pointerX;
    private double pointerY;
    private int mouseSensitivityPercent = DEFAULT_MOUSE_SENSITIVITY_PERCENT;
    private boolean rawMouseInput = true;
    private EchoClientKeyBindings keyBindings = EchoClientKeyBindings.defaults();

    EchoClientInput(long window) {
        this.window = window;
        setCursorLocked(false);
        GLFW.glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (action == GLFW.GLFW_PRESS) {
                if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) breakTriggered = true;
                if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) placeTriggered = true;
            }
        });
        GLFW.glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
            hotbarDelta += (int) Math.signum(yoffset);
        });
        applyRawMouseInputMode();
    }

    EchoVoxelPlayerInput poll(double dt) {
        // Mouse look (cursor disabled → raw deltas)
        updatePointer();

        double yawDelta = 0.0;
        double pitchDelta = 0.0;
        if (cursorLocked) {
            if (firstMouse) {
                lastMouseX = pointerX;
                lastMouseY = pointerY;
                firstMouse = false;
            }
            yawDelta = yawDeltaFromMouseDelta(pointerX - lastMouseX, mouseSensitivityPercent);
            pitchDelta = pitchDeltaFromMouseDelta(pointerY - lastMouseY, mouseSensitivityPercent);
            lastMouseX = pointerX;
            lastMouseY = pointerY;
        }

        if (!cursorLocked) {
            return EchoVoxelPlayerInput.idle();
        }

        // Keyboard movement
        boolean forward = key(EchoClientKeyAction.MOVE_FORWARD);
        boolean backward = key(EchoClientKeyAction.MOVE_BACKWARD);
        boolean strafeLeft = key(EchoClientKeyAction.STRAFE_LEFT);
        boolean strafeRight = key(EchoClientKeyAction.STRAFE_RIGHT);
        boolean jump = key(EchoClientKeyAction.JUMP);
        boolean crouch = key(EchoClientKeyAction.CROUCH);
        boolean sprint = key(EchoClientKeyAction.SPRINT);

        // Save / Load
        if (key(EchoClientKeyAction.SAVE_SESSION)) {
            if (!saveTriggered) {
                saveTriggered = true;
            }
        } else {
            saveTriggered = false;
        }
        if (key(EchoClientKeyAction.LOAD_SESSION)) {
            if (!loadTriggered) {
                loadTriggered = true;
            }
        } else {
            loadTriggered = false;
        }

        return new EchoVoxelPlayerInput(
                forward, backward, strafeLeft, strafeRight,
                jump, crouch, sprint,
                yawDelta, pitchDelta
        );
    }

    void updatePointer() {
        double[] mx = new double[1];
        double[] my = new double[1];
        GLFW.glfwGetCursorPos(window, mx, my);
        pointerX = mx[0];
        pointerY = my[0];
    }

    double pointerX() {
        return pointerX;
    }

    double pointerY() {
        return pointerY;
    }

    @Override
    public boolean isCursorLocked() {
        return cursorLocked;
    }

    void setCursorLocked(boolean locked) {
        if (cursorLocked == locked) {
            return;
        }
        cursorLocked = locked;
        firstMouse = true;
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR,
                cursorLocked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
        applyRawMouseInputMode();
    }

    void setMouseSensitivityPercent(int mouseSensitivityPercent) {
        this.mouseSensitivityPercent = clampPercent(mouseSensitivityPercent);
    }

    void setRawMouseInput(boolean rawMouseInput) {
        if (this.rawMouseInput == rawMouseInput) {
            return;
        }
        this.rawMouseInput = rawMouseInput;
        applyRawMouseInputMode();
    }

    void setKeyBindings(EchoClientKeyBindings keyBindings) {
        this.keyBindings = keyBindings == null ? EchoClientKeyBindings.defaults() : keyBindings.normalized();
    }

    boolean consumeKeyPress(int glfwKey) {
        boolean down = GLFW.glfwGetKey(window, glfwKey) == GLFW.GLFW_PRESS;
        boolean wasDown = pressedKeys.contains(glfwKey);
        if (down) {
            pressedKeys.add(glfwKey);
            return !wasDown;
        }
        pressedKeys.remove(glfwKey);
        return false;
    }

    void clearGameplayTriggers() {
        breakTriggered = false;
        placeTriggered = false;
        saveTriggered = false;
        loadTriggered = false;
        hotbarDelta = 0;
    }

    @Override
    public boolean consumeBreak() {
        boolean v = breakTriggered;
        breakTriggered = false;
        return v;
    }

    @Override
    public boolean consumePlace() {
        boolean v = placeTriggered;
        placeTriggered = false;
        return v;
    }

    boolean consumeUiPrimaryClick() {
        return consumeBreak();
    }

    boolean consumeUiSecondaryClick() {
        return consumePlace();
    }

    boolean uiPrimaryDown() {
        return mouseButton(GLFW.GLFW_MOUSE_BUTTON_LEFT);
    }

    boolean uiSecondaryDown() {
        return mouseButton(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
    }

    boolean consumeSave() {
        boolean v = saveTriggered;
        saveTriggered = false;
        return v;
    }

    boolean consumeLoad() {
        boolean v = loadTriggered;
        loadTriggered = false;
        return v;
    }

    int consumeHotbarDelta() {
        int v = hotbarDelta;
        hotbarDelta = 0;
        return v;
    }

    int consumeUiScrollDelta() {
        return consumeHotbarDelta();
    }

    int consumeHotbarSlotKeyPress() {
        int slot = 0;
        for (EchoClientKeyAction action : EchoClientKeyAction.hotbarActions()) {
            if (consumeKeyPress(action)) {
                return slot;
            }
            slot++;
        }
        return -1;
    }

    boolean consumeScreenshot() {
        return consumeKeyPress(EchoClientKeyAction.SCREENSHOT);
    }

    boolean consumeToggleFullscreen() {
        return consumeKeyPress(EchoClientKeyAction.TOGGLE_FULLSCREEN);
    }

    boolean consumeSlotGridClose() {
        return consumeKeyPress(EchoClientKeyAction.PAUSE)
                || consumeKeyPress(EchoClientKeyAction.OPEN_INVENTORY);
    }

    boolean consumePause() {
        return consumeKeyPress(EchoClientKeyAction.PAUSE);
    }

    boolean consumeOpenInventory() {
        return consumeKeyPress(EchoClientKeyAction.OPEN_INVENTORY);
    }

    boolean consumeDebugOverlay() {
        return consumeKeyPress(EchoClientKeyAction.DEBUG_OVERLAY);
    }

    boolean consumeSwapOffhand() {
        return consumeKeyPress(EchoClientKeyAction.SWAP_OFFHAND);
    }

    boolean consumeDropItem() {
        return consumeKeyPress(EchoClientKeyAction.DROP_ITEM);
    }

    boolean shiftDown() {
        return key(GLFW.GLFW_KEY_LEFT_SHIFT) || key(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    boolean controlDown() {
        return key(GLFW.GLFW_KEY_LEFT_CONTROL) || key(GLFW.GLFW_KEY_RIGHT_CONTROL);
    }

    boolean consumeBackspace() {
        return consumeKeyPress(GLFW.GLFW_KEY_BACKSPACE);
    }

    String consumeTextCharacters() {
        StringBuilder builder = new StringBuilder();
        for (int key = GLFW.GLFW_KEY_0; key <= GLFW.GLFW_KEY_9; key++) {
            if (consumeKeyPress(key)) {
                builder.append((char) ('0' + key - GLFW.GLFW_KEY_0));
            }
        }
        for (int key = GLFW.GLFW_KEY_A; key <= GLFW.GLFW_KEY_Z; key++) {
            if (consumeKeyPress(key)) {
                builder.append((char) ('a' + key - GLFW.GLFW_KEY_A));
            }
        }
        if (consumeKeyPress(GLFW.GLFW_KEY_MINUS)) {
            builder.append('-');
        }
        if (consumeKeyPress(GLFW.GLFW_KEY_SPACE)) {
            builder.append(' ');
        }
        return builder.toString();
    }

    @Override
    public int selectedHotbarSlot(int current) {
        int slot = 0;
        for (EchoClientKeyAction action : EchoClientKeyAction.hotbarActions()) {
            if (key(action)) {
                return slot;
            }
            slot++;
        }
        int delta = consumeHotbarDelta();
        if (delta != 0) {
            int next = current + delta;
            if (next < 0) next = 8;
            if (next > 8) next = 0;
            return next;
        }
        return current;
    }

    private boolean key(int glfwKey) {
        return GLFW.glfwGetKey(window, glfwKey) == GLFW.GLFW_PRESS;
    }

    private boolean key(EchoClientKeyAction action) {
        return key(keyBindings.key(action));
    }

    private boolean consumeKeyPress(EchoClientKeyAction action) {
        return consumeKeyPress(keyBindings.key(action));
    }

    private boolean mouseButton(int glfwButton) {
        return GLFW.glfwGetMouseButton(window, glfwButton) == GLFW.GLFW_PRESS;
    }

    static double yawDeltaFromMouseDelta(double mouseDeltaX) {
        return yawDeltaFromMouseDelta(mouseDeltaX, DEFAULT_MOUSE_SENSITIVITY_PERCENT);
    }

    static double yawDeltaFromMouseDelta(double mouseDeltaX, int mouseSensitivityPercent) {
        return mouseDeltaX * mouseLookSensitivity(mouseSensitivityPercent);
    }

    static double pitchDeltaFromMouseDelta(double mouseDeltaY) {
        return pitchDeltaFromMouseDelta(mouseDeltaY, DEFAULT_MOUSE_SENSITIVITY_PERCENT);
    }

    static double pitchDeltaFromMouseDelta(double mouseDeltaY, int mouseSensitivityPercent) {
        return -mouseDeltaY * mouseLookSensitivity(mouseSensitivityPercent);
    }

    static double mouseLookSensitivity(int mouseSensitivityPercent) {
        return MOUSE_LOOK_SENSITIVITY * (0.2D + clampPercent(mouseSensitivityPercent) * 0.016D);
    }

    static int rawMouseInputMode(boolean cursorLocked, boolean rawMouseInput, boolean supported) {
        return cursorLocked && rawMouseInput && supported ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE;
    }

    private void applyRawMouseInputMode() {
        if (!GLFW.glfwRawMouseMotionSupported()) {
            return;
        }
        GLFW.glfwSetInputMode(
                window,
                GLFW.GLFW_RAW_MOUSE_MOTION,
                rawMouseInputMode(cursorLocked, rawMouseInput, true)
        );
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
