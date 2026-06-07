package dev.echo.standalone.runtime.input;

import java.util.Objects;

public record EchoInputControl(EchoInputDeviceType deviceType, String code) {
    public EchoInputControl {
        Objects.requireNonNull(deviceType, "deviceType");
        code = EchoInputText.requireText(code, "code").toUpperCase();
    }

    public static EchoInputControl keyboard(String code) {
        return new EchoInputControl(EchoInputDeviceType.KEYBOARD, code);
    }

    public static EchoInputControl mouse(String code) {
        return new EchoInputControl(EchoInputDeviceType.MOUSE, code);
    }

    public static EchoInputControl gamepad(String code) {
        return new EchoInputControl(EchoInputDeviceType.GAMEPAD, code);
    }

    public String stableId() {
        return deviceType.name().toLowerCase() + ":" + code.toLowerCase();
    }
}
