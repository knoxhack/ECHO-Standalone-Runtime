package dev.echo.standalone.runtime.item;

public record EchoItemId(String value) {
    public EchoItemId {
        value = EchoItemText.requireText(value, "value");
    }

    public String fileSafeKey() {
        return value.replace(':', '_').replace('/', '_').replace('\\', '_');
    }
}
