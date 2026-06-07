package dev.echo.standalone.runtime.world;

public record EchoWorldChunkId(int x, int z) {
    public String key() {
        return x + "," + z;
    }

    public String fileSafeKey() {
        return x + "_" + z;
    }
}
