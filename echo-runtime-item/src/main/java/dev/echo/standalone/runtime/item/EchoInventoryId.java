package dev.echo.standalone.runtime.item;

public record EchoInventoryId(String value) {
    public EchoInventoryId {
        value = EchoItemText.requireText(value, "value");
    }

    public String fileSafeKey() {
        return value.replace(':', '_').replace('/', '_').replace('\\', '_');
    }
}
