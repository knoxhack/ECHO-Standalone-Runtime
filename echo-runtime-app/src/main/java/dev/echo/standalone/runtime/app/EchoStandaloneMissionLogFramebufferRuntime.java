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

public final class EchoStandaloneMissionLogFramebufferRuntime {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public EchoStandaloneMissionLogFramebufferResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
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
        EchoStandalonePlayableVoxelSession playable = new EchoStandalonePlayableVoxelRuntime().play(bridge);
        EchoVoxelPlayerHotbar hotbar = playable.hotbar();
        EchoAshfallLiveMissionState mission = playable.mission();

        EchoVoxelHudOverlay overlay = new EchoVoxelHudOverlay(
                hotbar,
                mission,
                bridge.runtimeSummary(),
                "echo:opengl_client_framebuffer_presenter",
                bridge.registrySummary(),
                "mission objectives",
                "mission log framebuffer smoke",
                "spawn yaw=" + Math.round(world.spawnYawDegrees()),
                "mission-log-open",
                bridge.bindingCoverageSummary(),
                "opengl game presenter",
                "framebuffer upload ready",
                true,
                true,
                true,
                world.loadedChunkCount(),
                true,
                false,
                false,
                true,
                "Mission Log",
                List.of(
                        "L / Enter / click / Esc: Back to game",
                        "Objective chain: shelter, scanner, terminal, water, food, ash, scavenge, cache, power, extract",
                        "Status values come from the live Ashfall mission state",
                        "F5: Manual Save"
                )
        );
        EchoVoxelFramebuffer base = new EchoVoxelSoftwareRenderer().render(world, camera, WIDTH, HEIGHT);
        EchoVoxelFramebuffer missionLog = new EchoVoxelHudFramebufferCompositor().composite(base, overlay);
        return new EchoStandaloneMissionLogFramebufferResult(
                "echo:opengl-visible-mission-log-framebuffer",
                missionLog.width(),
                missionLog.height(),
                bridge.supportsAllAdapterCoreRuntimes(),
                base.width() == missionLog.width()
                        && base.height() == missionLog.height()
                        && base.argb().length == missionLog.argb().length,
                base.checksum() != missionLog.checksum(),
                changedPixels(
                        base,
                        missionLog,
                        Math.max(0, WIDTH / 2 - 280),
                        Math.max(0, HEIGHT / 2 - 152),
                        Math.min(560, WIDTH),
                        Math.min(304, HEIGHT)
                ),
                mission.totalObjectives(),
                mission.completedObjectives(),
                mission.totalObjectives(),
                mission.requiredObjectiveLabels().size() >= 50
                        && mission.requiredObjectiveLabels().contains("Repair power node"),
                mission.optionalObjectiveLabels().contains("Use water ration")
                        && mission.optionalObjectiveLabels().contains("Scavenge survival supplies"),
                mission.completedHistory().size() >= 15
                        && mission.completedHistory().contains("Power node repaired"),
                !mission.currentHint().isBlank()
                        && mission.nextObjective().equals("Mission complete"),
                mission.terminalNotes().stream().anyMatch(note -> note.startsWith("Terminal state:"))
                        && mission.terminalNotes().stream().anyMatch(note -> note.startsWith("Hint:")),
                !mission.extractionStatus().isBlank()
                        && mission.extractionStatus().equals("EXTRACTED"),
                base.checksum(),
                missionLog.checksum()
        );
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
