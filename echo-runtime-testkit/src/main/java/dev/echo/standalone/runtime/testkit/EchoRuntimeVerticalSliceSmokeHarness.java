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

        writeReports(slice);

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

    private static void writeReports(EchoAshfallVerticalSliceResult slice) throws IOException {
        Path root = Path.of("reports", "echo", "standalone");
        Files.createDirectories(root);
        EchoAshfallVerticalSliceSummary summary = slice.summary();
        String summaryJson = summaryJson(summary);
        String missionStatus = slice.gameplay().mission().status().name();
        String screenId = escape(slice.ui().frame().screen().id());
        boolean terminalCleanExitLine = slice.ui().frame().screen().lines().contains("Exit: clean shutdown armed");
        boolean loadedMissionComplete = slice.saveRoundTrip().loadedSummary().contains("\"missionStatus\":\"COMPLETED\"");

        write(root.resolve("runtime-vertical-slice.json"), """
                {
                  "schema": "echo.standalone.runtime_vertical_slice.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "summary": "Ashfall vertical slice boots all standalone subsystems, completes the deterministic crash-site route, saves and reloads, then exits cleanly.",
                  "slice": %s,
                  "missionStatus": "%s",
                  "serviceBoundSubsystems": ["world", "entity", "item", "gameplay", "ui", "render", "audio", "network", "scripting", "compatibility", "save"],
                  "rendererClosed": %s,
                  "audioClosed": %s,
                  "cleanExit": %s
                }
                """.formatted(summaryJson, missionStatus, slice.rendererClosed(), slice.audioClosed(), slice.cleanExit()));
        write(root.resolve("vertical-slice-boot.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_boot.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "sliceId": "%s",
                  "serviceBound": true,
                  "subsystems": ["world", "entity", "item", "gameplay", "scripting", "compatibility", "ui", "render", "audio", "network", "save"],
                  "migrationSteps": %d,
                  "ruleMatches": %d
                }
                """.formatted(
                escape(summary.sliceId()),
                summary.migrationSteps(),
                summary.ruleMatches()
        ));
        write(root.resolve("vertical-slice-world.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_world.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "weatherProfile": "%s",
                  "hazardHealthDamage": %d,
                  "hazardIntensity": %s,
                  "hydration": %s,
                  "ashExposure": %s,
                  "heatStress": %s
                }
                """.formatted(
                escape(slice.weather().profileId()),
                slice.hazard().healthDamage(),
                fixed(summary.hazardIntensity()),
                fixed(summary.hydration()),
                fixed(summary.ashExposure()),
                fixed(summary.heatStress())
        ));
        write(root.resolve("vertical-slice-player.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_player.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "playerHealth": %d,
                  "cacheMovement": {
                    "moved": %s,
                    "to": "%s"
                  },
                  "hostileAttacks": %d,
                  "progressionExperience": %d,
                  "progressionLevel": %d
                }
                """.formatted(
                summary.playerHealth(),
                slice.cacheMovement().moved(),
                escape(slice.cacheMovement().to().key()),
                slice.hostileAi().attacks(),
                slice.gameplay().progression().experience(),
                slice.gameplay().progression().level()
        ));
        write(root.resolve("vertical-slice-terminal.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_terminal.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "screenId": "%s",
                  "lineCount": %d,
                  "cleanExitLinePresent": %s
                }
                """.formatted(
                screenId,
                slice.ui().frame().screen().lines().size(),
                terminalCleanExitLine
        ));
        write(root.resolve("vertical-slice-inventory.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_inventory.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "containers": %d,
                  "occupiedSlots": %d,
                  "inventorySyncSnapshots": %d,
                  "inventorySyncDetails": %d
                }
                """.formatted(
                summary.inventoryContainers(),
                summary.occupiedSlots(),
                slice.inventorySync().snapshotCount(),
                slice.inventorySync().detailCount()
        ));
        write(root.resolve("vertical-slice-hazard-meter.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_hazard_meter.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "hazardIntensity": %s,
                  "hydration": %s,
                  "ashExposure": %s,
                  "heatStress": %s,
                  "healthDamage": %d
                }
                """.formatted(
                fixed(summary.hazardIntensity()),
                fixed(summary.hydration()),
                fixed(summary.ashExposure()),
                fixed(summary.heatStress()),
                slice.hazard().healthDamage()
        ));
        write(root.resolve("vertical-slice-objectives.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_objectives.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "missionStatus": "%s",
                  "completedObjectives": %d,
                  "totalObjectives": %d,
                  "interactionCount": %d,
                  "notifications": %d
                }
                """.formatted(
                missionStatus,
                summary.completedObjectives(),
                summary.totalObjectives(),
                slice.interactions().size(),
                summary.notifications()
        ));
        write(root.resolve("vertical-slice-render-audio.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_render_audio.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "renderCommands": %d,
                  "renderFrames": %d,
                  "audioEvents": %d,
                  "audioDeviceOpen": %s,
                  "rendererClosed": %s,
                  "audioClosed": %s
                }
                """.formatted(
                summary.renderCommands(),
                slice.render().backend().frames().size(),
                summary.audioEvents(),
                slice.audio().backend().deviceOpen(),
                slice.rendererClosed(),
                slice.audioClosed()
        ));
        write(root.resolve("vertical-slice-network-sync.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_network_sync.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "handshakeAccepted": %s,
                  "networkPackets": %d,
                  "entitySnapshots": %d,
                  "entityDetails": %d,
                  "inventorySnapshots": %d,
                  "inventoryDetails": %d
                }
                """.formatted(
                slice.network().handshake().accepted(),
                summary.networkPackets(),
                slice.entitySync().snapshotCount(),
                slice.entitySync().detailCount(),
                slice.inventorySync().snapshotCount(),
                slice.inventorySync().detailCount()
        ));
        write(root.resolve("vertical-slice-save-load.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_save_load.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "filesWritten": %d,
                  "manifestFiles": %d,
                  "corruptionHealthy": %s,
                  "loadedMissionComplete": %s
                }
                """.formatted(
                slice.saveRoundTrip().commit().filesWritten(),
                slice.saveRoundTrip().loadedManifest().files().size(),
                slice.saveRoundTrip().corruptionReport().healthy(),
                loadedMissionComplete
        ));
        write(root.resolve("vertical-slice-clean-exit.json"), """
                {
                  "schema": "echo.standalone.vertical_slice_clean_exit.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVerticalSliceSmokeHarness",
                  "rendererClosed": %s,
                  "audioClosed": %s,
                  "summaryCleanExit": %s,
                  "cleanExit": %s
                }
                """.formatted(
                slice.rendererClosed(),
                slice.audioClosed(),
                summary.cleanExit(),
                slice.cleanExit()
        ));
    }

    private static String summaryJson(EchoAshfallVerticalSliceSummary summary) {
        return """
                {
                    "sliceId": "%s",
                    "completedObjectives": %d,
                    "totalObjectives": %d,
                    "playerHealth": %d,
                    "hazardIntensity": %s,
                    "hydration": %s,
                    "ashExposure": %s,
                    "heatStress": %s,
                    "inventoryContainers": %d,
                    "occupiedSlots": %d,
                    "renderCommands": %d,
                    "audioEvents": %d,
                    "networkPackets": %d,
                    "ruleMatches": %d,
                    "migrationSteps": %d,
                    "saveFiles": %d,
                    "notifications": %d,
                    "cleanExit": %s
                  }""".formatted(
                escape(summary.sliceId()),
                summary.completedObjectives(),
                summary.totalObjectives(),
                summary.playerHealth(),
                fixed(summary.hazardIntensity()),
                fixed(summary.hydration()),
                fixed(summary.ashExposure()),
                fixed(summary.heatStress()),
                summary.inventoryContainers(),
                summary.occupiedSlots(),
                summary.renderCommands(),
                summary.audioEvents(),
                summary.networkPackets(),
                summary.ruleMatches(),
                summary.migrationSteps(),
                summary.saveFiles(),
                summary.notifications(),
                summary.cleanExit()
        );
    }

    private static void write(Path path, String json) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, json);
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

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
