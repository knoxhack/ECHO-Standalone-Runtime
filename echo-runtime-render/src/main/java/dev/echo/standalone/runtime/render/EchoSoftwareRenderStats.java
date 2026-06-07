package dev.echo.standalone.runtime.render;

import java.util.List;
import java.util.Objects;

public record EchoSoftwareRenderStats(
        List<EchoSoftwareRenderPass> passes,
        int clearCommands,
        int tileCommands,
        int spriteCommands,
        int uiCommands,
        int particleCommands,
        int debugCommands,
        int litPixels,
        int nonBackgroundPixels,
        long checksum
) {
    public EchoSoftwareRenderStats {
        Objects.requireNonNull(passes, "passes");
        passes = List.copyOf(passes);
        if (clearCommands < 0 || tileCommands < 0 || spriteCommands < 0 || uiCommands < 0
                || particleCommands < 0 || debugCommands < 0 || litPixels < 0 || nonBackgroundPixels < 0) {
            throw new IllegalArgumentException("render counters must not be negative");
        }
    }
}
