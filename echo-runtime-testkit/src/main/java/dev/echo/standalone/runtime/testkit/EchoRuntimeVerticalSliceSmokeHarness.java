package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallVerticalSliceResult;
import dev.echo.standalone.runtime.app.EchoAshfallVerticalSliceRuntime;
import dev.echo.standalone.runtime.app.EchoAshfallVerticalSliceSaveRoundTrip;
import dev.echo.standalone.runtime.app.EchoAshfallVerticalSliceSummary;
import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoCompatRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class EchoRuntimeVerticalSliceSmokeHarness {
    private EchoRuntimeVerticalSliceSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-vertical-slice-smoke");
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAshfallVerticalSliceResult slice = new EchoAshfallVerticalSliceRuntime().boot(services, fixtureRoot);
        EchoAshfallVerticalSliceSummary summary = slice.summary();

        require(services.require(EchoAshfallVerticalSliceResult.class) == slice,
                "vertical slice result should be service-bound");
        require(services.require(EchoAshfallVerticalSliceSummary.class) == summary,
                "vertical slice summary should be service-bound");
        require(services.require(EchoAshfallVerticalSliceSaveRoundTrip.class) == slice.saveRoundTrip(),
                "save round trip should be service-bound");
        require(services.require(EchoWorldRuntimeResult.class) == slice.world(),
                "world runtime result should be service-bound");
        require(services.require(EchoEntityRuntimeResult.class) == slice.entities(),
                "entity runtime result should be service-bound");
        require(services.require(EchoItemRuntimeResult.class) == slice.items(),
                "item runtime result should be service-bound");
        require(services.require(EchoGameplayRuntimeResult.class) == slice.gameplay(),
                "gameplay runtime result should be service-bound");
        require(services.require(EchoUiRuntimeResult.class) == slice.ui(),
                "UI runtime result should be service-bound");
        require(services.require(EchoRenderRuntimeResult.class) == slice.render(),
                "render runtime result should be service-bound");
        require(services.require(EchoAudioRuntimeResult.class) == slice.audio(),
                "audio runtime result should be service-bound");
        require(services.require(EchoNetworkRuntimeResult.class) == slice.network(),
                "network runtime result should be service-bound");
        require(services.require(EchoScriptingRuntimeResult.class) == slice.scripting(),
                "scripting runtime result should be service-bound");
        require(services.require(EchoCompatRuntimeResult.class) == slice.compatibility(),
                "compatibility runtime result should be service-bound");
        require(services.require(EchoSaveRuntimeResult.class) == slice.saveRoundTrip().saveRuntime(),
                "save runtime result should be service-bound");

        require(summary.sliceId().equals("ashfall-vertical-slice"),
                "summary slice id should be stable");
        require(slice.gameplay().mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "vertical slice mission should complete");
        require(summary.completedObjectives() == 3 && summary.totalObjectives() == 3,
                "all three objectives should complete");
        require(summary.playerHealth() == 91,
                "player should retain expected post-hazard and post-attack health");
        requireClose(summary.hazardIntensity(), 0.72D, "hazard meter");
        requireClose(summary.hydration(), 70.0D, "hydration");
        requireClose(summary.ashExposure(), 7.2D, "ash exposure");
        requireClose(summary.heatStress(), 2.86D, "heat stress");
        require(slice.weather().profileId().equals("ashfall:ash_storm"),
                "weather profile should be Ashfall debug storm");
        require(slice.hazard().healthDamage() == 4,
                "hazard should damage suit seals deterministically");
        require(slice.cacheMovement().moved() && slice.cacheMovement().to().key().equals("2,0,1"),
                "player should move to the crash cache");
        require(slice.hostileAi().attacks() == 1,
                "hostile scavenger should attack once after cache salvage");
        require(slice.entities().store().all().stream()
                        .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                        .findFirst()
                        .orElseThrow()
                        .worldPosition()
                        .key()
                        .equals("2,0,1"),
                "player should finish at the crash cache");

        require(slice.interactions().size() == 3,
                "vertical slice should perform three player interactions");
        EchoGameplayInteractionResult drinkWater = slice.interactions().get(0);
        EchoGameplayInteractionResult terminal = slice.interactions().get(1);
        EchoGameplayInteractionResult salvage = slice.interactions().get(2);
        require(drinkWater.success() && drinkWater.objectiveCompleted() && drinkWater.experienceAwarded() == 15,
                "drink water should complete the hydration objective");
        require(terminal.success() && !terminal.objectiveCompleted() && terminal.experienceAwarded() == 0,
                "terminal activation should succeed after the declarative rule completed it");
        require(salvage.success() && salvage.objectiveCompleted() && salvage.experienceAwarded() == 30,
                "cache salvage should complete the salvage objective");
        require(slice.gameplay().progression().experience() == 55,
                "progression should include rule, hydration, and salvage experience");
        require(slice.gameplay().progression().level() == 2,
                "progression should reach level two");
        require(summary.notifications() == 9,
                "notification log should contain the deterministic vertical slice events");

        require(slice.ui().frame().screen().id().equals("ashfall-vertical-slice-terminal"),
                "terminal screen should be active");
        require(slice.ui().frame().screen().lines().contains("Exit: clean shutdown armed"),
                "terminal should show the clean-exit path");
        require(summary.renderCommands() == 35,
                "renderer should submit the expected vertical slice command set");
        require(slice.render().backend().frames().size() == 1,
                "renderer should contain one frame");
        require(summary.audioEvents() == 3,
                "completed mission should suppress active-mission stinger");
        require(!slice.audio().backend().deviceOpen(),
                "recording audio backend must not open a device");

        require(slice.network().handshake().accepted(),
                "local debug network handshake should pass");
        require(summary.networkPackets() == 4,
                "local transport should record handshake plus entity and inventory sync packets");
        require(slice.entitySync().snapshotCount() == 2 && slice.entitySync().detailCount() == 0,
                "entity sync should replicate two entity snapshots");
        require(slice.inventorySync().snapshotCount() == 2 && slice.inventorySync().detailCount() == 5,
                "inventory sync should replicate two inventories and five occupied stacks");

        require(slice.scripting().initialReport().matchedRules() == 3,
                "all three declarative debug rules should match");
        require(slice.scripting().initialReport().actionCount() == 8,
                "declarative rules should schedule eight actions");
        require(slice.compatibility().migrationPlan().steps().size() == 8,
                "compatibility plan should remain available in the slice");
        require(slice.compatibility().migrationPlan().mutationStepCount() == 0,
                "compatibility plan must not mutate source data");

        require(slice.saveRoundTrip().commit().filesWritten() == 3,
                "vertical slice should write three save files");
        require(slice.saveRoundTrip().loadedManifest().files().size() == 3,
                "loaded manifest should list three save files");
        require(slice.saveRoundTrip().corruptionReport().healthy(),
                "save/load round trip should be healthy");
        require(slice.saveRoundTrip().loadedSummary().contains("\"missionStatus\":\"COMPLETED\""),
                "loaded summary should preserve mission completion");
        require(slice.rendererClosed(), "renderer should close cleanly");
        require(slice.audioClosed(), "audio backend should close cleanly");
        require(slice.cleanExit() && summary.cleanExit(), "vertical slice should finish with a clean exit");

        System.out.println("phase14.18 vertical slice smoke PASS mission="
                + slice.gameplay().mission().status().name()
                + " objectives="
                + summary.completedObjectives()
                + "/"
                + summary.totalObjectives()
                + " health="
                + summary.playerHealth()
                + " hazard="
                + fixed(summary.hazardIntensity())
                + " render="
                + summary.renderCommands()
                + " audio="
                + summary.audioEvents()
                + " network="
                + summary.networkPackets()
                + " saveFiles="
                + summary.saveFiles()
                + " cleanExit="
                + summary.cleanExit());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireClose(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 0.001D) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static String fixed(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
