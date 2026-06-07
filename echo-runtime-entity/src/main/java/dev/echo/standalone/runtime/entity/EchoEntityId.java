package dev.echo.standalone.runtime.entity;

public record EchoEntityId(String value) {
    public EchoEntityId {
        value = EchoEntityText.requireText(value, "value");
    }

    public String fileSafeKey() {
        return value.replace(':', '_').replace('/', '_').replace('\\', '_');
    }
}
