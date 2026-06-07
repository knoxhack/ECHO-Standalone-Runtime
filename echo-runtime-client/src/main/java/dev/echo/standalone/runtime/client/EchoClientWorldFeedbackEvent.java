package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelHit;

record EchoClientWorldFeedbackEvent(
        EchoClientWorldFeedbackKind kind,
        String sourceId,
        double x,
        double y,
        double z,
        double normalX,
        double normalY,
        double normalZ,
        int argb
) {
    EchoClientWorldFeedbackEvent {
        if (kind == null) {
            kind = EchoClientWorldFeedbackKind.BLOCK_BREAK;
        }
        sourceId = sourceId == null || sourceId.isBlank() ? "minecraft:air" : sourceId.trim();
        x = finite(x);
        y = finite(y);
        z = finite(z);
        normalX = finite(normalX);
        normalY = finite(normalY);
        normalZ = finite(normalZ);
        if (((argb >>> 24) & 0xFF) == 0) {
            argb = 0xFF9AA0A8;
        }
    }

    static EchoClientWorldFeedbackEvent blockBreak(EchoVoxelHit hit) {
        if (hit == null || hit.block() == null || hit.block().air()) {
            return null;
        }
        EchoVoxelBlock block = hit.block();
        return new EchoClientWorldFeedbackEvent(
                EchoClientWorldFeedbackKind.BLOCK_BREAK,
                block.id(),
                hit.x() + 0.5D,
                hit.y() + 0.5D,
                hit.z() + 0.5D,
                hit.normalX(),
                hit.normalY(),
                hit.normalZ(),
                argbForSource(block.id())
        );
    }

    static EchoClientWorldFeedbackEvent blockPlace(
            EchoVoxelBlockState state,
            int x,
            int y,
            int z,
            int normalX,
            int normalY,
            int normalZ
    ) {
        if (state == null || state.air()) {
            return null;
        }
        EchoVoxelBlock block = state.block();
        return new EchoClientWorldFeedbackEvent(
                EchoClientWorldFeedbackKind.BLOCK_PLACE,
                block.id(),
                x + 0.5D,
                y + 0.5D,
                z + 0.5D,
                normalX,
                normalY,
                normalZ,
                lighten(argbForSource(block.id()), 1.18D)
        );
    }

    private static int argbForSource(String sourceId) {
        int hash = sourceId == null ? 0 : sourceId.hashCode();
        int r = 92 + Math.floorMod(hash, 112);
        int g = 86 + Math.floorMod(hash >>> 8, 104);
        int b = 78 + Math.floorMod(hash >>> 16, 112);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int lighten(int argb, double factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = clampColor((int) Math.round(((argb >>> 16) & 0xFF) * factor));
        int g = clampColor((int) Math.round(((argb >>> 8) & 0xFF) * factor));
        int b = clampColor((int) Math.round((argb & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0.0D;
    }
}
