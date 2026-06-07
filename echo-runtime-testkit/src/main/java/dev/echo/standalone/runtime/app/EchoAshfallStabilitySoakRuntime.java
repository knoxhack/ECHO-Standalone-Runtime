package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.input.EchoInputContext;
import dev.echo.standalone.runtime.input.EchoInputFocusState;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerStep;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoAshfallStabilitySoakRuntime {
    private static final int SIMULATED_MINUTES = 60;
    private static final int STEPS = 720;
    private static final double STEP_SECONDS = 5.0D;
    private static final int FRAME_INTERVAL = 12;
    private static final int SAVE_INTERVAL = 60;
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public EchoAshfallStabilitySoakResult run(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(bridge, "bridge");
        Objects.requireNonNull(saveRoot, "saveRoot");
        Files.createDirectories(saveRoot);

        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorldStreamer streamer = sessionProfile.streamer();
        EchoVoxelWorld world = sessionProfile.generate(42L, 0);
        world = streamer.streamAround(world, world.spawnX(), world.spawnZ());
        int chunkCountStart = world.loadedChunkCount();
        EchoVoxelPlayerController player = EchoVoxelPlayerController.spawnAt(
                world,
                world.spawnX(),
                world.spawnZ(),
                world.spawnYawDegrees(),
                -24.0D
        );
        EchoVoxelPlayerHotbar hotbar = sessionProfile.newStarterHotbar();
        hotbar.add(bridge.shelterAnchorBlock(), 2);
        hotbar.add(bridge.waterRationItem(), 4);
        hotbar.add(bridge.fieldRationItem(), 4);
        hotbar.add(bridge.emergencyScannerItem(), 1);
        EchoAshfallLiveMissionState mission = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                4,
                4,
                1,
                100,
                100.0D,
                100.0D,
                8.0D,
                2,
                5,
                2,
                0.0D,
                "stability soak: route active"
        );

        EchoVoxelSoftwareRenderer renderer = new EchoVoxelSoftwareRenderer();
        EchoVoxelHudFramebufferCompositor compositor = new EchoVoxelHudFramebufferCompositor();
        EchoSaveRuntimeResult save = EchoStandaloneLiveSessionSaveRuntime.openSave(saveRoot);
        EchoStandaloneGameShellState shell = EchoStandaloneGameShellState.titleNoSave().startNewGame();
        HashSet<Long> checksums = new HashSet<>();

        gcQuietly();
        long memoryStart = usedMemory();
        long maxInputLatency = 0L;
        int framesRendered = 0;
        int savesWritten = 0;
        int restoresVerified = 0;
        int minUniqueColors = Integer.MAX_VALUE;
        int minFacesDrawn = Integer.MAX_VALUE;
        int maxWhitePixels = 0;
        boolean dimensionsStable = true;
        boolean noBlankFrames = true;
        boolean noWhiteFrames = true;
        boolean inventorySpamStable = true;
        boolean terminalSpamStable = true;
        boolean pauseResumeStable = true;
        boolean focusLossRestoreStable = true;
        boolean repeatedSaveLoadStable = true;

        for (int step = 1; step <= STEPS; step++) {
            long inputStart = System.nanoTime();
            EchoVoxelPlayerInput input = inputFor(step);
            EchoVoxelPlayerStep playerStep = player.tick(world, input, 0.1D);
            long latency = System.nanoTime() - inputStart;
            maxInputLatency = Math.max(maxInputLatency, latency);
            world = streamer.streamAround(world, playerStep.current().x(), playerStep.current().z());
            mission.tick(world, playerStep.current(), playerStep.moved(), STEP_SECONDS,
                    bridge.hazardTable(), bridge.shelterProfile(), bridge.survivalProfile());
            if (step % 180 == 0) {
                mission.useWaterRation(bridge.survivalProfile());
                mission.useFoodRation(bridge.survivalProfile());
            }

            if (step % 15 == 0) {
                EchoStandaloneGameShellState inventory = shell.openInventory();
                inventorySpamStable &= inventory.overlayVisible()
                        && !inventory.gameplayActive()
                        && inventory.closeInventory().gameplayActive();
            }
            if (step % 20 == 0) {
                EchoStandaloneGameShellState terminal = shell.openTerminal();
                terminalSpamStable &= terminal.overlayVisible()
                        && !terminal.gameplayActive()
                        && terminal.closeTerminal().gameplayActive();
            }
            if (step % 24 == 0) {
                EchoStandaloneGameShellState paused = shell.pause();
                pauseResumeStable &= paused.overlayVisible()
                        && !paused.gameplayActive()
                        && paused.resume().gameplayActive();
            }
            if (step % 90 == 0) {
                focusLossRestoreStable &= simulateWindowFocusRoundTrip(shell);
            }

            if (step % FRAME_INTERVAL == 0) {
                EchoVoxelFramebuffer base = renderer.render(world, player.state().camera(), WIDTH, HEIGHT);
                EchoVoxelFramebuffer visible = compositor.composite(base, overlay(bridge, hotbar, mission, world, step));
                framesRendered++;
                checksums.add(visible.checksum());
                dimensionsStable &= visible.width() == WIDTH
                        && visible.height() == HEIGHT
                        && visible.argb().length == WIDTH * HEIGHT;
                int uniqueColors = visible.uniqueColorCount();
                int whitePixels = whitePixels(visible);
                minUniqueColors = Math.min(minUniqueColors, uniqueColors);
                minFacesDrawn = Math.min(minFacesDrawn, base.facesDrawn());
                maxWhitePixels = Math.max(maxWhitePixels, whitePixels);
                noBlankFrames &= visible.checksum() != 0L && uniqueColors >= 40 && base.facesDrawn() >= 400;
                noWhiteFrames &= whitePixels < 6_000;
            }

            if (step % SAVE_INTERVAL == 0) {
                EchoVoxelFramebuffer frame = renderer.render(world, player.state().camera(), WIDTH, HEIGHT);
                String transactionId = "tx-soak-" + String.format("%03d", savesWritten + 1);
                EchoSaveCommitResult commit = EchoStandalonePlayableVoxelSaveCodec.writeLiveSnapshot(
                        save,
                        EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID,
                        transactionId,
                        player.state(),
                        hotbar,
                        mission,
                        List.of(),
                        frame,
                        Map.of(
                                "saveKind", step == STEPS ? "manual" : "autosave",
                                "runtime", "standalone",
                                "qa", "stability_soak",
                                "simulatedSecond", String.valueOf(Math.round(step * STEP_SECONDS))
                        )
                );
                savesWritten++;
                EchoSaveManifest manifest = save.readManifest(commit.slot().slotId());
                EchoStandalonePlayableVoxelSaveSnapshot restoredSnapshot = EchoStandalonePlayableVoxelSaveCodec.restoreSnapshot(
                        bridge,
                        save,
                        manifest
                );
                boolean restored = restoredSnapshot.mission().terminalOnline()
                        && restoredSnapshot.mission().powerRepaired()
                        && restoredSnapshot.hotbar().selectedSlot() == hotbar.selectedSlot()
                        && save.check(commit.slot().slotId()).healthy();
                repeatedSaveLoadStable &= restored;
                if (restored) {
                    restoresVerified++;
                }
            }
        }

        gcQuietly();
        long memoryEnd = usedMemory();
        long memoryGrowth = Math.max(0L, memoryEnd - memoryStart);
        long saveBytes = directoryBytes(save.profile().root());
        boolean memoryStable = memoryGrowth < 128L * 1024L * 1024L;
        boolean frameStable = dimensionsStable && noBlankFrames && noWhiteFrames;
        boolean chunkStreamingStable = world.loadedChunkCount() >= chunkCountStart;
        boolean missionAlive = mission.playerHealth() > 0 && !mission.status().equals("FAILED");

        return new EchoAshfallStabilitySoakResult(
                SIMULATED_MINUTES,
                STEPS,
                framesRendered,
                savesWritten,
                restoresVerified,
                memoryStable,
                memoryGrowth,
                frameStable,
                minUniqueColors == Integer.MAX_VALUE ? 0 : minUniqueColors,
                minFacesDrawn == Integer.MAX_VALUE ? 0 : minFacesDrawn,
                maxWhitePixels,
                checksums.size(),
                chunkCountStart,
                world.loadedChunkCount(),
                saveBytes,
                maxInputLatency,
                chunkStreamingStable,
                repeatedSaveLoadStable,
                inventorySpamStable,
                terminalSpamStable,
                pauseResumeStable,
                focusLossRestoreStable,
                missionAlive,
                "minutes=" + SIMULATED_MINUTES
                        + " steps=" + STEPS
                        + " frames=" + framesRendered
                        + " saves=" + savesWritten
                        + " restores=" + restoresVerified
                        + " memGrowth=" + memoryGrowth
                        + " frameStable=" + frameStable
                        + " colors=" + (minUniqueColors == Integer.MAX_VALUE ? 0 : minUniqueColors)
                        + " faces=" + (minFacesDrawn == Integer.MAX_VALUE ? 0 : minFacesDrawn)
                        + " white=" + maxWhitePixels
                        + " checksums=" + checksums.size()
                        + " chunks=" + chunkCountStart + "->" + world.loadedChunkCount()
                        + " saveBytes=" + saveBytes
                        + " maxInputNs=" + maxInputLatency
                        + " saveLoad=" + repeatedSaveLoadStable
                        + " inventorySpam=" + inventorySpamStable
                        + " terminalSpam=" + terminalSpamStable
                        + " pauseResume=" + pauseResumeStable
                        + " focusRestore=" + focusLossRestoreStable
                        + " missionAlive=" + missionAlive
        );
    }

    private static boolean simulateWindowFocusRoundTrip(EchoStandaloneGameShellState shell) {
        EchoInputFocusState focus = new EchoInputFocusState();
        focus.focusGameplay();

        EchoStandaloneGameShellState focusLost = shell.pause();
        focus.focusUi("window:focus-lost");
        boolean lostSafe = focusLost.overlayVisible()
                && !focusLost.gameplayActive()
                && focus.activeContext() == EchoInputContext.UI
                && focus.focusPath().equals("window:focus-lost")
                && !focus.terminalFocused();

        focus.focusGameplay();
        EchoStandaloneGameShellState restored = focusLost.resume();
        boolean restoredSafe = restored.gameplayActive()
                && !restored.overlayVisible()
                && focus.activeContext() == EchoInputContext.GAMEPLAY
                && focus.focusPath().equals("gameplay:world");
        return lostSafe && restoredSafe;
    }

    private static EchoVoxelPlayerInput inputFor(int step) {
        return new EchoVoxelPlayerInput(
                step % 4 != 0,
                step % 31 == 0,
                step % 17 == 0,
                step % 19 == 0,
                false,
                step % 29 == 0,
                step % 5 != 0,
                step % 9 == 0 ? 2.0D : 0.25D,
                step % 13 == 0 ? -0.5D : 0.0D
        );
    }

    private static EchoVoxelHudOverlay overlay(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoVoxelPlayerHotbar hotbar,
            EchoAshfallLiveMissionState mission,
            EchoVoxelWorld world,
            int step
    ) {
        return new EchoVoxelHudOverlay(
                hotbar,
                mission,
                bridge.runtimeSummary(),
                "echo:software-presenter+opengl-target",
                bridge.registrySummary(),
                "soak step " + step,
                "60 minute stability soak",
                "soak/chunk-stream",
                "playing",
                bridge.bindingCoverageSummary(),
                "software fallback visible",
                "opengl target armed",
                true,
                true,
                true,
                world.loadedChunkCount(),
                false,
                false,
                false,
                false,
                "",
                List.of()
        );
    }

    private static int whitePixels(EchoVoxelFramebuffer framebuffer) {
        int white = 0;
        for (int color : framebuffer.argb()) {
            int red = (color >>> 16) & 0xFF;
            int green = (color >>> 8) & 0xFF;
            int blue = color & 0xFF;
            if (red >= 245 && green >= 245 && blue >= 245) {
                white++;
            }
        }
        return white;
    }

    private static long directoryBytes(Path root) throws IOException {
        if (!Files.exists(root)) {
            return 0L;
        }
        try (var stream = Files.walk(root)) {
            return stream.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException exception) {
                            throw new IllegalStateException(exception);
                        }
                    })
                    .sum();
        }
    }

    private static long usedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static void gcQuietly() {
        System.gc();
        try {
            Thread.sleep(25L);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }
}
