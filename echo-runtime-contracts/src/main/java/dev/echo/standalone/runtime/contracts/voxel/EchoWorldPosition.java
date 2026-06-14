package dev.echo.standalone.runtime.contracts.voxel;

import java.util.Objects;

/**
 * Immutable integer block position in world coordinates.
 *
 * <p>This type intentionally avoids any Minecraft or LWJGL class references.
 */
public record EchoWorldPosition(int x, int y, int z) {

    public EchoWorldPosition {
        // No validation: positions may reference unloaded chunks.
    }

    public EchoWorldPosition offset(int dx, int dy, int dz) {
        return new EchoWorldPosition(x + dx, y + dy, z + dz);
    }

    public EchoWorldPosition north() {
        return offset(0, 0, -1);
    }

    public EchoWorldPosition south() {
        return offset(0, 0, 1);
    }

    public EchoWorldPosition east() {
        return offset(1, 0, 0);
    }

    public EchoWorldPosition west() {
        return offset(-1, 0, 0);
    }

    public EchoWorldPosition up() {
        return offset(0, 1, 0);
    }

    public EchoWorldPosition down() {
        return offset(0, -1, 0);
    }

    public long packed() {
        return (((long) x) << 42) | (((long) y) << 21) | (z & 0x1FFFFFL);
    }

    public static EchoWorldPosition unpack(long packed) {
        int x = (int) (packed >> 42);
        int y = (int) ((packed >> 21) & 0x1FFFFFL);
        int z = (int) (packed & 0x1FFFFFL);
        return new EchoWorldPosition(x, y, z);
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + ", " + z + "]";
    }
}
