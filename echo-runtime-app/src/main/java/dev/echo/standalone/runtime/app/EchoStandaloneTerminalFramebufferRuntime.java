package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.List;
import java.util.Objects;

public final class EchoStandaloneTerminalFramebufferRuntime {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public EchoStandaloneTerminalFramebufferResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorld world = sessionProfile.generate(42L, 0);
        world = sessionProfile.streamer().streamAround(world, world.spawnX(), world.spawnZ());
        EchoVoxelCamera camera = new EchoVoxelCamera(
                world.spawnX(),
                world.spawnY() + 1.2D,
                world.spawnZ(),
                world.spawnYawDegrees(),
                -22.0D,
                70.0D
        );
        EchoVoxelPlayerHotbar hotbar = sessionProfile.newStarterHotbar();
        hotbar.add(bridge.waterRationItem(), 2);
        EchoAshfallLiveMissionState mission = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                1,
                1,
                1,
                100,
                86.0D,
                78.0D,
                12.0D,
                "terminal online: cache route unlocked"
        );

        EchoVoxelHudOverlay overlay = new EchoVoxelHudOverlay(
                hotbar,
                mission,
                bridge.runtimeSummary(),
                "echo:opengl_client_framebuffer_presenter",
                bridge.registrySummary(),
                "field terminal @ 3,4,3",
                "terminal framebuffer smoke",
                "spawn yaw=" + Math.round(world.spawnYawDegrees()),
                "terminal-open",
                bridge.bindingCoverageSummary(),
                "opengl game presenter",
                "framebuffer upload ready",
                true,
                true,
                true,
                world.loadedChunkCount(),
                true,
                false,
                true,
                false,
                "Emergency Terminal",
                List.of(
                        "Enter / click / Esc: Back to game",
                        "Cache route: unlocked after terminal sync",
                        "Mission log: ashfall emergency channel",
                        "F5: Manual Save"
                )
        );
        EchoVoxelFramebuffer base = new EchoVoxelSoftwareRenderer().render(world, camera, WIDTH, HEIGHT);
        EchoVoxelFramebuffer terminal = new EchoVoxelHudFramebufferCompositor().composite(base, overlay);
        return new EchoStandaloneTerminalFramebufferResult(
                "echo:opengl-visible-terminal-framebuffer",
                terminal.width(),
                terminal.height(),
                bridge.supportsAllAdapterCoreRuntimes(),
                base.width() == terminal.width()
                        && base.height() == terminal.height()
                        && base.argb().length == terminal.argb().length,
                base.checksum() != terminal.checksum(),
                changedPixels(
                        base,
                        terminal,
                        Math.max(0, WIDTH / 2 - 280),
                        Math.max(0, HEIGHT / 2 - 152),
                        Math.min(560, WIDTH),
                        Math.min(304, HEIGHT)
                ),
                mission.terminalOnline(),
                terminalStateCoverageReady(),
                terminalDiagnosticsReady(),
                extractionAuthorizedReady(),
                base.checksum(),
                terminal.checksum()
        );
    }

    private static boolean terminalStateCoverageReady() {
        return EchoAshfallLiveMissionState.restored(
                false, false, false, false, false, false, false, false, false, false,
                2, 2, 0, 100, 72.0D, 64.0D, 0.0D, "new"
        ).terminalState().equals("OFFLINE")
                && EchoAshfallLiveMissionState.restored(
                true, true, false, false, false, false, false, false, false, false,
                2, 2, 0, 100, 72.0D, 64.0D, 0.0D, "scanned"
        ).terminalState().equals("DAMAGED")
                && EchoAshfallLiveMissionState.restored(
                true, true, true, false, false, false, false, false, false, false,
                2, 2, 0, 100, 72.0D, 64.0D, 0.0D, "terminal"
        ).terminalState().equals("LOW POWER")
                && EchoAshfallLiveMissionState.restored(
                true, true, true, true, true, true, true, true, true, false,
                1, 1, 0, 100, 90.0D, 90.0D, 0.0D, "power"
        ).terminalState().equals("ONLINE")
                && EchoAshfallLiveMissionState.restored(
                true, true, true, true, true, true, true, true, true, true,
                1, 1, 0, 100, 90.0D, 90.0D, 0.0D, "extracted"
        ).terminalState().equals("EXTRACTION AUTHORIZED");
    }

    private static boolean terminalDiagnosticsReady() {
        EchoAshfallLiveMissionState diagnostics = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                1,
                1,
                1,
                100,
                86.0D,
                78.0D,
                12.0D,
                "power diagnostics requested"
        );
        return diagnostics.terminalNotes().stream().anyMatch(note -> note.contains("Power diagnostics"))
                && diagnostics.terminalNotes().stream().anyMatch(note -> note.startsWith("Extraction:"));
    }

    private static boolean extractionAuthorizedReady() {
        EchoAshfallLiveMissionState extracted = EchoAshfallLiveMissionState.restored(
                true, true, true, true, true, true, true, true, true, true,
                1, 1, 0, 100, 90.0D, 90.0D, 0.0D, "extracted"
        );
        return extracted.terminalState().equals("EXTRACTION AUTHORIZED")
                && extracted.extractionStatus().equals("EXTRACTED");
    }

    private static int changedPixels(
            EchoVoxelFramebuffer before,
            EchoVoxelFramebuffer after,
            int x,
            int y,
            int width,
            int height
    ) {
        int startX = clamp(x, 0, before.width());
        int startY = clamp(y, 0, before.height());
        int endX = clamp(x + width, startX, before.width());
        int endY = clamp(y + height, startY, before.height());
        int changed = 0;
        int[] beforePixels = before.argb();
        int[] afterPixels = after.argb();
        for (int row = startY; row < endY; row++) {
            int offset = row * before.width();
            for (int column = startX; column < endX; column++) {
                if (beforePixels[offset + column] != afterPixels[offset + column]) {
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
