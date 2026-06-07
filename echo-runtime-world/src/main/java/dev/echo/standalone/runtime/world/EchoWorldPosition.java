package dev.echo.standalone.runtime.world;

public record EchoWorldPosition(int x, int y, int z) {
    public String key() {
        return x + "," + y + "," + z;
    }
}
