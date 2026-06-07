package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.Objects;

public final class EchoStandaloneVoxelHudFramebufferRuntime {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public EchoStandaloneVoxelHudFramebufferResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
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
        hotbar.select(0);
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        mission.useWaterRation(bridge.survivalProfile());
        EchoVoxelHudOverlay overlay = new EchoVoxelHudOverlay(
                hotbar,
                mission,
                bridge.runtimeSummary(),
                "echo:opengl_client_framebuffer_presenter",
                bridge.registrySummary(),
                "rusted debris @ crosshair",
                "mining progress 68 percent / block crack preview",
                "spawn yaw=" + Math.round(world.spawnYawDegrees()),
                "grounded/sprint-ready",
                bridge.bindingCoverageSummary(),
                "opengl game presenter",
                "framebuffer upload ready",
                true,
                true,
                true,
                world.loadedChunkCount(),
                false,
                false,
                false,
                false,
                "ECHO Ashfall",
                java.util.List.of(
                        "Enter / click: New Game",
                        "C: Continue ashfall-camp-01 (manual)",
                        "Esc: pause menu"
                )
        );
        EchoAshfallPlayerFeedback feedback = EchoAshfallPlayerFeedback.from(
                mission,
                hotbar,
                true,
                "water ration used: hydration restored / consumed_one"
        );
        EchoAshfallLiveMissionState warningMission = EchoAshfallLiveMissionState.restored(
                false,
                false,
                false,
                false,
                false,
                true,
                false,
                false,
                false,
                false,
                0,
                0,
                0,
                18,
                10.0D,
                7.0D,
                82.0D,
                "place blocked by player body @ 0,0,0"
        );
        EchoAshfallPlayerFeedback warningFeedback = EchoAshfallPlayerFeedback.from(
                warningMission,
                hotbar,
                false,
                "place blocked by player body @ 0,0,0"
        );
        EchoAshfallLiveMissionState lowPowerMission = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                1,
                1,
                0,
                100,
                72.0D,
                64.0D,
                0.0D,
                "terminal online: power required"
        );
        EchoAshfallPlayerFeedback lowPowerFeedback = EchoAshfallPlayerFeedback.from(
                lowPowerMission,
                hotbar,
                true,
                "terminal online: power required"
        );
        EchoVoxelFramebuffer base = new EchoVoxelSoftwareRenderer().render(world, camera, WIDTH, HEIGHT);
        EchoVoxelFramebuffer hud = new EchoVoxelHudFramebufferCompositor().composite(base, overlay);
        int breakFeedbackChangedPixels = changedPixels(
                base,
                hud,
                WIDTH / 2 - 88,
                HEIGHT / 2 - 72,
                176,
                128
        );
        int heldItemChangedPixels = changedPixels(
                base,
                hud,
                WIDTH - 230,
                HEIGHT - 232,
                218,
                150
        );
        int actionParticlesChangedPixels = changedPixels(
                base,
                hud,
                WIDTH / 2 + 34,
                HEIGHT / 2 - 112,
                154,
                132
        );
        return new EchoStandaloneVoxelHudFramebufferResult(
                "echo:opengl-visible-hud-framebuffer",
                hud.width(),
                hud.height(),
                bridge.supportsAllAdapterCoreRuntimes(),
                base.width() == hud.width() && base.height() == hud.height() && base.argb().length == hud.argb().length,
                base.blocksVisited() == hud.blocksVisited() && base.facesDrawn() == hud.facesDrawn(),
                base.checksum() != hud.checksum(),
                changedPixels(base, hud, 0, 0, hud.width(), Math.min(132, hud.height())),
                changedPixels(base, hud, 0, Math.max(0, hud.height() - 164), hud.width(), Math.min(164, hud.height())),
                heldItemChangedPixels,
                breakFeedbackChangedPixels,
                actionParticlesChangedPixels,
                (int) hotbar.slots().stream().filter(slot -> !slot.empty()).count(),
                heldItemChangedPixels > 1_400
                        && !hotbar.selected().empty()
                        && feedback.selectedHotbarItem().contains(hotbar.selected().label()),
                breakFeedbackChangedPixels > 1_000
                        && overlay.actionLabel().contains("mining")
                        && overlay.targetLabel().contains("rusted debris"),
                actionParticlesChangedPixels > 240
                        && overlay.actionLabel().contains("mining")
                        && overlay.targetAvailable(),
                feedback.coversPlayerHud()
                        && feedback.currentObjective().equals(mission.nextObjective())
                        && feedback.currentHint().equals(mission.currentHint())
                        && feedback.selectedHotbarItem().contains(hotbar.selected().label())
                        && feedback.toolDurability().contains("hardness"),
                warningFeedback.warningStates().contains("health critical")
                        && warningFeedback.warningStates().contains("hydration low")
                        && warningFeedback.warningStates().contains("low water")
                        && warningFeedback.warningStates().contains("hunger low")
                        && warningFeedback.warningStates().contains("low food")
                        && warningFeedback.warningStates().contains("ash exposure high")
                        && warningFeedback.warningStates().contains("ash exposure rising")
                        && warningFeedback.warningStates().contains("shelter missing")
                        && warningFeedback.warningStates().contains("shelter unsafe")
                        && warningFeedback.warningStates().contains("no target")
                        && warningFeedback.warningStates().contains("terminal offline")
                        && warningFeedback.warningStates().contains("cannot place"),
                mission.requiredObjectiveLabels().size() >= 8
                        && mission.optionalObjectiveLabels().size() >= 4
                        && mission.completedHistory().contains("Crash beacon tracked")
                        && !mission.terminalNotes().isEmpty()
                        && !mission.extractionStatus().isBlank(),
                terminalStateCoverageReady()
                        && lowPowerFeedback.warningStates().contains("power required"),
                base.checksum(),
                hud.checksum(),
                (long) hud.width() * (long) hud.height() * Integer.BYTES
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
