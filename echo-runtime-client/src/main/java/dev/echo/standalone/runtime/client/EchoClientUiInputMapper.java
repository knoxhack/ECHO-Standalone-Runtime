package dev.echo.standalone.runtime.client;

import org.lwjgl.glfw.GLFW;

final class EchoClientUiInputMapper {
    EchoClientScreenCommand poll(
            EchoClientInput input,
            EchoClientScreenController screens,
            boolean hasSession,
            int width,
            int height,
            double uiScale
    ) {
        input.updatePointer();
        double pointerX = scaledPointer(input.pointerX(), uiScale);
        double pointerY = scaledPointer(input.pointerY(), uiScale);
        if (screens.modalOpen()) {
            EchoClientScreenCommand pointerCommand = screens.handleModalPointer(
                    pointerX,
                    pointerY,
                    input.consumeUiPrimaryClick(),
                    width,
                    height
            );
            if (pointerCommand != EchoClientScreenCommand.NONE) {
                return pointerCommand;
            }
            if (input.consumeKeyPress(GLFW.GLFW_KEY_ESCAPE)) {
                return screens.escapeCommand();
            }
            if (input.consumeKeyPress(GLFW.GLFW_KEY_LEFT) || input.consumeKeyPress(GLFW.GLFW_KEY_A)) {
                return screens.handleModalNavigation(-1);
            }
            if (input.consumeKeyPress(GLFW.GLFW_KEY_RIGHT) || input.consumeKeyPress(GLFW.GLFW_KEY_D)) {
                return screens.handleModalNavigation(1);
            }
            if (input.consumeKeyPress(GLFW.GLFW_KEY_ENTER) || input.consumeKeyPress(GLFW.GLFW_KEY_KP_ENTER)
                    || input.consumeKeyPress(GLFW.GLFW_KEY_SPACE)) {
                return screens.confirmModalSelection();
            }
            return EchoClientScreenCommand.NONE;
        }
        if (screens.textEditing()) {
            if (input.consumeKeyPress(GLFW.GLFW_KEY_ESCAPE)
                    || input.consumeKeyPress(GLFW.GLFW_KEY_ENTER)
                    || input.consumeKeyPress(GLFW.GLFW_KEY_KP_ENTER)) {
                screens.stopTextEditing(hasSession);
                return EchoClientScreenCommand.NONE;
            }
            screens.handleTextInput(input.consumeTextCharacters(), input.consumeBackspace(), hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (screens.keyRebindActive()) {
            if (input.consumeKeyPress(GLFW.GLFW_KEY_ESCAPE)) {
                screens.cancelKeyRebind(hasSession);
                return EchoClientScreenCommand.NONE;
            }
            int key = input.consumeFirstKeyPress(EchoClientKeyBindings.configurableKeys());
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                screens.finishKeyRebind(key, hasSession);
            }
            return EchoClientScreenCommand.NONE;
        }
        EchoClientScreenCommand pointerCommand = screens.handlePointer(
                pointerX,
                pointerY,
                input.consumeUiPrimaryClick(),
                input.uiPrimaryDown(),
                width,
                height,
                hasSession
        );
        if (pointerCommand != EchoClientScreenCommand.NONE) {
            return pointerCommand;
        }
        int scroll = input.consumeUiScrollDelta();
        if (scroll != 0) {
            screens.scrollSelection(-scroll, hasSession, height);
            return EchoClientScreenCommand.NONE;
        }
        if (input.consumeKeyPress(GLFW.GLFW_KEY_ESCAPE)) {
            return screens.escapeCommand();
        }
        if (input.consumeKeyPress(GLFW.GLFW_KEY_LEFT) || input.consumeKeyPress(GLFW.GLFW_KEY_A)) {
            screens.editSelectedControl(-1, hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (input.consumeKeyPress(GLFW.GLFW_KEY_RIGHT) || input.consumeKeyPress(GLFW.GLFW_KEY_D)) {
            screens.editSelectedControl(1, hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (input.consumeKeyPress(GLFW.GLFW_KEY_UP) || input.consumeKeyPress(GLFW.GLFW_KEY_W)) {
            screens.moveSelection(-1, hasSession, height);
            return EchoClientScreenCommand.NONE;
        }
        if (input.consumeKeyPress(GLFW.GLFW_KEY_DOWN) || input.consumeKeyPress(GLFW.GLFW_KEY_S)) {
            screens.moveSelection(1, hasSession, height);
            return EchoClientScreenCommand.NONE;
        }
        if (input.consumeKeyPress(GLFW.GLFW_KEY_ENTER) || input.consumeKeyPress(GLFW.GLFW_KEY_KP_ENTER)
                || input.consumeKeyPress(GLFW.GLFW_KEY_SPACE)) {
            return screens.activateSelection(hasSession);
        }
        return EchoClientScreenCommand.NONE;
    }

    private static double scaledPointer(double value, double uiScale) {
        double safeScale = uiScale <= 0.0D ? 1.0D : uiScale;
        return value / safeScale;
    }
}
