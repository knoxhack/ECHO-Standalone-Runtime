package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.audio.EchoAudioRuntime;
import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfiles;
import dev.echo.standalone.runtime.compat.EchoCompatRuntime;
import dev.echo.standalone.runtime.compat.EchoCompatRuntimeResult;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityAiTickResult;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementIntent;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayHazardResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayWeatherResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkRuntime;
import dev.echo.standalone.runtime.network.EchoNetworkRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkSyncResult;
import dev.echo.standalone.runtime.render.EchoRenderRuntime;
import dev.echo.standalone.runtime.render.EchoRenderRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderWindowSettings;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntime;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class EchoAshfallVerticalSliceRuntime {
    private static final String SAVE_SLOT = "vertical-slice-slot";

    public EchoAshfallVerticalSliceResult boot(
            EchoRuntimeServiceRegistry services,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(saveRoot, "saveRoot");

        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoScriptingRuntimeResult scripting = new EchoScriptingRuntime().createDebugRules(
                services,
                world,
                entities,
                items,
                gameplay
        );
        EchoCompatRuntimeResult compatibility = new EchoCompatRuntime().createDebugCompatibility(
                services,
                world,
                entities,
                items,
                gameplay
        );

        EchoEntityId playerId = entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Ashfall vertical slice requires a player entity"))
                .id();

        EchoGameplayWeatherResult weather = gameplay.weatherSystem().applyCurrentWeather();
        EchoGameplayHazardResult hazard = gameplay.hazardSystem().apply(playerId);
        EchoGameplayInteractionResult drinkWater = gameplay.interactionSystem().drinkWater(playerId);
        EchoGameplayInteractionResult terminal = gameplay.interactionSystem().activateTerminal(playerId);
        EchoEntityMovementResult cacheMovement = entities.movementSystem().move(
                entities.store(),
                new EchoEntityMovementIntent(playerId, 2, 1)
        );
        EchoGameplayInteractionResult salvage = gameplay.interactionSystem().salvageCrashCache(playerId);
        EchoEntityAiTickResult hostileAi = entities.aiSystem().tick(entities.store());
        List<EchoGameplayInteractionResult> interactions = List.of(drinkWater, terminal, salvage);

        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoStaticScreen(
                        "ashfall-vertical-slice-terminal",
                        "Ashfall Terminal",
                        terminalLines(gameplay, items, hazard),
                        "terminal:ashfall-vertical-slice"
                ),
                EchoUiTheme.defaultTerminal()
        );
        EchoRenderRuntimeResult render = new EchoRenderRuntime().createDebugRenderer(
                services,
                world,
                entities,
                gameplay,
                ui,
                EchoRenderWindowSettings.headlessDebug()
        );
        EchoAudioRuntimeResult audio = new EchoAudioRuntime().createDebugAudio(
                services,
                world,
                gameplay,
                ui.frame().screen().id(),
                EchoAudioVolumeProfiles.resolve(EchoAudioVolumeProfiles.ASHFALL_SURVIVAL_MIX_PROFILE_ID)
        );
        EchoNetworkRuntimeResult network = new EchoNetworkRuntime().createLocalDebugNetwork(services, entities, items);
        EchoNetworkSyncResult entitySync = network.syncService().syncEntities(
                entities,
                network.serverEndpoint(),
                network.clientEndpoint()
        );
        EchoNetworkSyncResult inventorySync = network.syncService().syncInventories(
                items,
                network.serverEndpoint(),
                network.clientEndpoint()
        );

        EchoAshfallVerticalSliceSaveRoundTrip saveRoundTrip = saveRoundTrip(
                services,
                saveRoot,
                world,
                entities,
                items,
                gameplay,
                hazard
        );

        render.backend().close();
        boolean rendererClosed = true;
        audio.backend().close();
        boolean audioClosed = audio.backend().diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.message().endsWith("backend closed"));
        boolean cleanExit = rendererClosed
                && audioClosed
                && render.backend().frames().size() == 1
                && !audio.backend().deviceOpen()
                && saveRoundTrip.corruptionReport().healthy()
                && network.handshake().accepted()
                && compatibility.migrationPlan().mutationStepCount() == 0;

        EchoAshfallVerticalSliceSummary summary = summary(
                gameplay,
                entities,
                items,
                render,
                audio,
                network,
                scripting,
                compatibility,
                saveRoundTrip,
                hazard,
                cleanExit
        );
        EchoAshfallVerticalSliceResult result = new EchoAshfallVerticalSliceResult(
                world,
                entities,
                items,
                gameplay,
                scripting,
                compatibility,
                weather,
                hazard,
                interactions,
                cacheMovement,
                hostileAi,
                ui,
                render,
                audio,
                network,
                entitySync,
                inventorySync,
                saveRoundTrip,
                summary,
                rendererClosed,
                audioClosed,
                cleanExit
        );
        services.register(EchoAshfallVerticalSliceResult.class, result);
        services.register(EchoAshfallVerticalSliceSummary.class, summary);
        services.register(EchoAshfallVerticalSliceSaveRoundTrip.class, saveRoundTrip);
        return result;
    }

    private static EchoAshfallVerticalSliceSaveRoundTrip saveRoundTrip(
            EchoRuntimeServiceRegistry services,
            Path saveRoot,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayRuntimeResult gameplay,
            EchoGameplayHazardResult hazard
    ) throws IOException {
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-vertical-slice",
                "Ashfall Vertical Slice",
                "echoashfallprotocol",
                1,
                saveRoot.resolve("profiles/ashfall-vertical-slice"),
                Map.of("phase", "14.18", "chapter", "ashfall")
        );
        EchoSaveRuntimeResult save = new EchoSaveRuntime().open(services, profile);
        EchoSaveTransaction transaction = save.beginTransaction(SAVE_SLOT, "tx-vertical-slice-001");
        String summaryJson = summaryJson(gameplay, entities, items, hazard);
        transaction.writeText("vertical-slice/summary.json", summaryJson);
        transaction.writeText("vertical-slice/player.json", playerJson(entities, gameplay));
        transaction.writeText("vertical-slice/inventory.json", inventoryJson(items));
        EchoSaveCommitResult commit = transaction.commit(Map.of(
                "slice", "ashfall",
                "missionStatus", gameplay.mission().status().name()
        ));
        EchoSaveManifest loadedManifest = save.readManifest(SAVE_SLOT);
        EchoSaveCorruptionReport corruptionReport = save.check(SAVE_SLOT);
        String loadedSummary = Files.readString(profile.slot(SAVE_SLOT)
                .dataRoot()
                .resolve("vertical-slice/summary.json"));
        return new EchoAshfallVerticalSliceSaveRoundTrip(
                save,
                commit,
                loadedManifest,
                corruptionReport,
                loadedSummary
        );
    }

    private static EchoAshfallVerticalSliceSummary summary(
            EchoGameplayRuntimeResult gameplay,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoRenderRuntimeResult render,
            EchoAudioRuntimeResult audio,
            EchoNetworkRuntimeResult network,
            EchoScriptingRuntimeResult scripting,
            EchoCompatRuntimeResult compatibility,
            EchoAshfallVerticalSliceSaveRoundTrip saveRoundTrip,
            EchoGameplayHazardResult hazard,
            boolean cleanExit
    ) {
        int playerHealth = entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow()
                .health()
                .currentHealth();
        return new EchoAshfallVerticalSliceSummary(
                "ashfall-vertical-slice",
                gameplay.mission().completedObjectiveCount(),
                gameplay.mission().objectiveCount(),
                playerHealth,
                hazard.hazardIntensity(),
                gameplay.survival().hydration(),
                gameplay.survival().ashExposure(),
                gameplay.survival().heatStress(),
                items.inventoryStore().count(),
                items.inventoryStore().occupiedSlots(),
                render.initialFrame().submittedCommandCount(),
                audio.initialEvents().size(),
                network.transport().count(),
                scripting.initialReport().matchedRules(),
                compatibility.migrationPlan().steps().size(),
                saveRoundTrip.loadedManifest().files().size(),
                gameplay.notifications().count(),
                cleanExit
        );
    }

    private static List<String> terminalLines(
            EchoGameplayRuntimeResult gameplay,
            EchoItemRuntimeResult items,
            EchoGameplayHazardResult hazard
    ) {
        return List.of(
                "Mission: " + gameplay.mission().title(),
                "Objective: " + gameplay.mission().status().name()
                        + " " + gameplay.mission().completedObjectiveCount()
                        + "/" + gameplay.mission().objectiveCount(),
                "Terminal: ONLINE",
                "Hazard meter: " + fixed(hazard.hazardIntensity()),
                "Hydration: " + fixed(gameplay.survival().hydration()),
                "Ash exposure: " + fixed(gameplay.survival().ashExposure()),
                "Inventory slots: " + items.inventoryStore().occupiedSlots() + " occupied",
                "Exit: clean shutdown armed"
        );
    }

    private static String summaryJson(
            EchoGameplayRuntimeResult gameplay,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayHazardResult hazard
    ) {
        return "{"
                + "\"missionStatus\":\"" + gameplay.mission().status().name() + "\","
                + "\"completedObjectives\":" + gameplay.mission().completedObjectiveCount() + ","
                + "\"totalObjectives\":" + gameplay.mission().objectiveCount() + ","
                + "\"playerHealth\":" + playerHealth(entities) + ","
                + "\"hazardIntensity\":" + fixed(hazard.hazardIntensity()) + ","
                + "\"hydration\":" + fixed(gameplay.survival().hydration()) + ","
                + "\"ashExposure\":" + fixed(gameplay.survival().ashExposure()) + ","
                + "\"heatStress\":" + fixed(gameplay.survival().heatStress()) + ","
                + "\"inventoryContainers\":" + items.inventoryStore().count() + ","
                + "\"occupiedSlots\":" + items.inventoryStore().occupiedSlots()
                + "}";
    }

    private static String playerJson(
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay
    ) {
        var player = entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow();
        return "{"
                + "\"entityId\":\"" + player.id().value() + "\","
                + "\"position\":\"" + player.worldPosition().key() + "\","
                + "\"health\":" + player.health().currentHealth() + ","
                + "\"missionStatus\":\"" + gameplay.mission().status().name() + "\""
                + "}";
    }

    private static String inventoryJson(EchoItemRuntimeResult items) {
        return "{"
                + "\"containers\":" + items.inventoryStore().count() + ","
                + "\"occupiedSlots\":" + items.inventoryStore().occupiedSlots()
                + "}";
    }

    private static int playerHealth(EchoEntityRuntimeResult entities) {
        return entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow()
                .health()
                .currentHealth();
    }

    private static String fixed(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
