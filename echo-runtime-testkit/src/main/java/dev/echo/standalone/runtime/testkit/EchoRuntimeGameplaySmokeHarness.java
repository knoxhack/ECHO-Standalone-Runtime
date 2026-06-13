package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityMovementIntent;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoFactionStanding;
import dev.echo.standalone.runtime.gameplay.EchoFactionRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayHazardResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionObjective;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionState;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayNotification;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplaySaveResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayWeatherResult;
import dev.echo.standalone.runtime.gameplay.EchoNotificationLog;
import dev.echo.standalone.runtime.gameplay.EchoProgressionState;
import dev.echo.standalone.runtime.gameplay.EchoSurvivalState;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeGameplaySmokeHarness {
    private EchoRuntimeGameplaySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
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
        EchoEntityId playerId = new EchoEntityId("player-001");
        EchoItemId waterId = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);

        require(services.require(EchoGameplayRuntimeResult.class) == gameplay,
                "gameplay runtime result should be service-bound");
        require(services.require(EchoGameplayMissionState.class) == gameplay.mission(),
                "mission state should be service-bound");
        require(services.require(EchoSurvivalState.class) == gameplay.survival(),
                "survival state should be service-bound");
        require(services.require(EchoProgressionState.class) == gameplay.progression(),
                "progression state should be service-bound");
        require(services.require(EchoFactionRuntime.class) == gameplay.factions(),
                "faction runtime should be service-bound");
        require(services.require(EchoNotificationLog.class) == gameplay.notifications(),
                "notification log should be service-bound");
        require(gameplay.mission().objectiveCount() == 3, "debug mission should contain three objectives");
        require(gameplay.factions().count() == 2, "debug gameplay should contain two factions");
        require(gameplay.notifications().count() == 1, "debug gameplay should start with one notification");

        EchoGameplayHazardResult hazard = gameplay.hazardSystem().apply(playerId);
        require(hazard.hazardIntensity() == 0.72D, "origin should be inside toxic ash hazard");
        require(hazard.healthDamage() == 4, "toxic ash hazard should damage player");
        require(entities.store().require(playerId).health().currentHealth() == 96,
                "player health should reflect hazard damage");
        requireDouble(gameplay.survival().ashExposure(), 7.20D, "hazard should increase ash exposure");

        EchoGameplayWeatherResult weather = gameplay.weatherSystem().applyCurrentWeather();
        require(weather.profileId().equals("ashfall:ash_storm"), "debug weather profile should be ash storm");
        requireDouble(gameplay.survival().heatStress(), 2.86D, "ash storm should increase heat stress");

        EchoGameplayInteractionResult water = gameplay.interactionSystem().drinkWater(playerId);
        require(water.success(), "water interaction should succeed");
        require(water.objectiveCompleted(), "water interaction should complete hydration objective");
        int waterRemainingAfterDrink = items.operations().count(
                items.inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                waterId
        );
        require(waterRemainingAfterDrink == 1, "water interaction should consume one ration");
        requireDouble(gameplay.survival().hydration(), 70.00D, "water should increase hydration");

        EchoGameplayInteractionResult terminal = gameplay.interactionSystem().activateTerminal(playerId);
        require(terminal.success(), "terminal interaction should succeed at terminal position");
        require(terminal.objectiveCompleted(), "terminal interaction should complete terminal objective");

        EchoEntityMovementResult movedToCache = entities.movementSystem().move(
                entities.store(),
                new EchoEntityMovementIntent(playerId, 2, 1)
        );
        require(movedToCache.moved(), "player should move to crash cache");
        require(entities.store().require(playerId).worldPosition().equals(new EchoWorldPosition(2, 0, 1)),
                "player should stand at crash cache position");

        EchoGameplayInteractionResult cache = gameplay.interactionSystem().salvageCrashCache(playerId);
        require(cache.success(), "cache interaction should succeed at crash cache position");
        require(cache.objectiveCompleted(), "cache interaction should complete salvage objective");

        require(gameplay.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "all gameplay objectives should complete mission");
        require(gameplay.mission().completedObjectiveCount() == 3,
                "all three gameplay objectives should be complete");
        require(gameplay.mission().progressPercent() == 100,
                "mission progress should be complete");
        require(gameplay.progression().experience() == 70,
                "gameplay interactions should award deterministic experience");
        require(gameplay.progression().level() == 2,
                "deterministic experience should advance progression level");
        require(gameplay.progression().milestones().contains("mission_secure_crash_site"),
                "mission completion milestone should be recorded");
        require(gameplay.factions().require("ashfall:crash_survivors").reputation() == 15,
                "survivor faction reputation should increase after cache salvage");
        require(gameplay.factions().require("ashfall:wasteland_scavengers").reputation() == -30,
                "scavenger faction reputation should decrease after cache salvage");
        require(gameplay.factions().require("ashfall:wasteland_scavengers").hostile(),
                "scavenger faction should remain hostile");
        require(gameplay.notifications().count() == 7,
                "gameplay loop should record seven deterministic notifications");

        Path fixtureRoot = Files.createTempDirectory("echo-runtime-gameplay-smoke");
        EchoSaveProfile saveProfile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-gameplay",
                "Ashfall Gameplay",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/ashfall-gameplay"),
                Map.of("phase", "14.12")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(services, saveProfile);
        EchoGameplaySaveResult saved = gameplay.saveHook().save(saves, "slot-gameplay", "tx-gameplay-001");
        require(saved.commit().filesWritten() == 6, "gameplay save hook should write six files");
        for (String path : saved.writtenPaths()) {
            require(path.startsWith("gameplay/"), "gameplay save paths should stay under gameplay/");
        }

        EchoSaveManifest manifest = saves.readManifest("slot-gameplay");
        require(manifest.file("gameplay/summary.json").isPresent(), "manifest should track gameplay summary");
        require(manifest.file("gameplay/mission.json").isPresent(), "manifest should track mission");
        require(manifest.file("gameplay/survival.json").isPresent(), "manifest should track survival");
        require(manifest.file("gameplay/progression.json").isPresent(), "manifest should track progression");
        require(manifest.file("gameplay/factions.json").isPresent(), "manifest should track factions");
        require(manifest.file("gameplay/notifications.json").isPresent(), "manifest should track notifications");
        EchoSaveCorruptionReport saveCheck = saves.check("slot-gameplay");
        require(saveCheck.healthy(), "gameplay save should pass corruption check");

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                gameplay,
                entities,
                items,
                playerId,
                hazard,
                weather,
                water,
                terminal,
                movedToCache,
                cache,
                waterRemainingAfterDrink,
                saved,
                manifest,
                saveCheck
        );

        System.out.println("phase14.12 gameplay runtime smoke PASS missions=1 objectives="
                + gameplay.mission().objectiveCount()
                + " completed="
                + gameplay.mission().completedObjectiveCount()
                + " level="
                + gameplay.progression().level()
                + " notifications="
                + gameplay.notifications().count()
                + " savedFiles="
                + saved.writtenPaths().size());
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoGameplayRuntimeResult gameplay,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoEntityId playerId,
            EchoGameplayHazardResult hazard,
            EchoGameplayWeatherResult weather,
            EchoGameplayInteractionResult water,
            EchoGameplayInteractionResult terminal,
            EchoEntityMovementResult movedToCache,
            EchoGameplayInteractionResult cache,
            int waterRemainingAfterDrink,
            EchoGameplaySaveResult saved,
            EchoSaveManifest manifest,
            EchoSaveCorruptionReport saveCheck
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);

        int playerHealth = entities.store().require(playerId).health().currentHealth();
        int finalWaterRemaining = items.operations().count(
                items.inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID)
        );

        write(root.resolve("runtime-gameplay.json"), """
                {
                  "schema": "echo.standalone.runtime_gameplay.v2",
                  "status": "PASS",
                  "phase": "14.12",
                  "summary": "Gameplay runtime connected world, entity, item, survival, mission, faction, notification, interaction, and save systems into a deterministic Ashfall survival route.",
                  "serviceBound": true,
                  "missionBound": true,
                  "survivalBound": true,
                  "progressionBound": true,
                  "factionRuntimeBound": true,
                  "notificationLogBound": true,
                  "missionId": "%s",
                  "missionStatus": "%s",
                  "objectiveCount": %d,
                  "completedObjectiveCount": %d,
                  "progressPercent": %d,
                  "progressionLevel": %d,
                  "notificationCount": %d,
                  "playerHealth": %d,
                  "saveHealthy": %s
                }
                """.formatted(
                escape(gameplay.mission().missionId()),
                gameplay.mission().status(),
                gameplay.mission().objectiveCount(),
                gameplay.mission().completedObjectiveCount(),
                gameplay.mission().progressPercent(),
                gameplay.progression().level(),
                gameplay.notifications().count(),
                playerHealth,
                saveCheck.healthy()
        ));

        write(root.resolve("gameplay-missions.json"), """
                {
                  "schema": "echo.standalone.gameplay_missions.v2",
                  "status": "PASS",
                  "missionCount": 1,
                  "missionId": "%s",
                  "title": "%s",
                  "missionStatus": "%s",
                  "objectiveCount": %d,
                  "completedObjectiveCount": %d,
                  "progressPercent": %d,
                  "missionCompleted": %s,
                  "completionMilestone": "%s"
                }
                """.formatted(
                escape(gameplay.mission().missionId()),
                escape(gameplay.mission().title()),
                gameplay.mission().status(),
                gameplay.mission().objectiveCount(),
                gameplay.mission().completedObjectiveCount(),
                gameplay.mission().progressPercent(),
                gameplay.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "mission_secure_crash_site"
        ));

        write(root.resolve("gameplay-objectives.json"), """
                {
                  "schema": "echo.standalone.gameplay_objectives.v2",
                  "status": "PASS",
                  "objectiveCount": %d,
                  "completedObjectiveCount": %d,
                  "objectives": %s,
                  "hydrateCompleted": %s,
                  "terminalCompleted": %s,
                  "salvageCompleted": %s
                }
                """.formatted(
                gameplay.mission().objectiveCount(),
                gameplay.mission().completedObjectiveCount(),
                objectivesJson(gameplay.mission().objectives()),
                gameplay.mission().objective("ashfall:hydrate_survivor").orElseThrow().completed(),
                gameplay.mission().objective("ashfall:activate_terminal").orElseThrow().completed(),
                gameplay.mission().objective("ashfall:salvage_cache").orElseThrow().completed()
        ));

        write(root.resolve("gameplay-progression.json"), """
                {
                  "schema": "echo.standalone.gameplay_progression.v2",
                  "status": "PASS",
                  "experience": %d,
                  "level": %d,
                  "milestones": %s,
                  "missionMilestoneRecorded": %s,
                  "deterministicLevelUp": %s
                }
                """.formatted(
                gameplay.progression().experience(),
                gameplay.progression().level(),
                jsonStringArray(gameplay.progression().milestones()),
                gameplay.progression().milestones().contains("mission_secure_crash_site"),
                gameplay.progression().experience() == 70 && gameplay.progression().level() == 2
        ));

        write(root.resolve("gameplay-hazards.json"), """
                {
                  "schema": "echo.standalone.gameplay_hazards.v2",
                  "status": "PASS",
                  "entityId": "%s",
                  "hazardIntensity": %s,
                  "exposureDelta": %s,
                  "healthDamage": %d,
                  "playerHealthAfterHazard": %d,
                  "ashExposureAfterHazard": %s,
                  "toxicAshApplied": %s
                }
                """.formatted(
                escape(hazard.entityId().value()),
                Double.toString(hazard.hazardIntensity()),
                Double.toString(hazard.exposureDelta()),
                hazard.healthDamage(),
                playerHealth,
                Double.toString(gameplay.survival().ashExposure()),
                hazard.hazardIntensity() == 0.72D && hazard.healthDamage() == 4
        ));

        write(root.resolve("gameplay-weather.json"), """
                {
                  "schema": "echo.standalone.gameplay_weather.v2",
                  "status": "PASS",
                  "profileId": "%s",
                  "ashDensity": %s,
                  "heatStressDelta": %s,
                  "visibility": %s,
                  "heatStressAfterWeather": %s,
                  "ashStormApplied": %s
                }
                """.formatted(
                escape(weather.profileId()),
                Double.toString(weather.ashDensity()),
                Double.toString(weather.heatStressDelta()),
                Double.toString(weather.visibility()),
                Double.toString(gameplay.survival().heatStress()),
                weather.profileId().equals("ashfall:ash_storm")
        ));

        write(root.resolve("gameplay-survival.json"), """
                {
                  "schema": "echo.standalone.gameplay_survival.v2",
                  "status": "PASS",
                  "playerId": "%s",
                  "hydration": %s,
                  "ashExposure": %s,
                  "heatStress": %s,
                  "waterRemainingAfterDrink": %d,
                  "waterRemainingFinal": %d,
                  "hazardAndWeatherApplied": %s,
                  "waterInteractionRaisedHydration": %s
                }
                """.formatted(
                escape(gameplay.survival().playerId().value()),
                Double.toString(gameplay.survival().hydration()),
                Double.toString(gameplay.survival().ashExposure()),
                Double.toString(gameplay.survival().heatStress()),
                waterRemainingAfterDrink,
                finalWaterRemaining,
                gameplay.survival().ashExposure() == 7.2D && gameplay.survival().heatStress() == 2.86D,
                gameplay.survival().hydration() == 70.0D && waterRemainingAfterDrink == 1
        ));

        write(root.resolve("gameplay-factions.json"), """
                {
                  "schema": "echo.standalone.gameplay_factions.v2",
                  "status": "PASS",
                  "factionCount": %d,
                  "factions": %s,
                  "survivorReputation": %d,
                  "scavengerReputation": %d,
                  "scavengerHostile": %s,
                  "cacheSalvageAdjustedReputation": %s
                }
                """.formatted(
                gameplay.factions().count(),
                factionsJson(gameplay.factions().all()),
                gameplay.factions().require("ashfall:crash_survivors").reputation(),
                gameplay.factions().require("ashfall:wasteland_scavengers").reputation(),
                gameplay.factions().require("ashfall:wasteland_scavengers").hostile(),
                gameplay.factions().require("ashfall:crash_survivors").reputation() == 15
                        && gameplay.factions().require("ashfall:wasteland_scavengers").reputation() == -30
        ));

        write(root.resolve("gameplay-interactions.json"), """
                {
                  "schema": "echo.standalone.gameplay_interactions.v2",
                  "status": "PASS",
                  "water": %s,
                  "terminal": %s,
                  "moveToCache": %s,
                  "cache": %s,
                  "allInteractionsSucceeded": %s,
                  "cachePosition": %s,
                  "waterRemainingAfterDrink": %d,
                  "waterRemainingFinal": %d
                }
                """.formatted(
                interactionJson(water),
                interactionJson(terminal),
                movementJson(movedToCache),
                interactionJson(cache),
                water.success() && terminal.success() && movedToCache.moved() && cache.success(),
                positionJson(movedToCache.to()),
                waterRemainingAfterDrink,
                finalWaterRemaining
        ));

        write(root.resolve("gameplay-notifications.json"), """
                {
                  "schema": "echo.standalone.gameplay_notifications.v2",
                  "status": "PASS",
                  "notificationCount": %d,
                  "notifications": %s,
                  "firstNotification": "%s",
                  "lastNotification": "%s",
                  "deterministicOrder": %s
                }
                """.formatted(
                gameplay.notifications().count(),
                notificationsJson(gameplay.notifications().all()),
                escape(gameplay.notifications().all().get(0).message()),
                escape(gameplay.notifications().all().get(gameplay.notifications().count() - 1).message()),
                gameplay.notifications().all().get(0).notificationId().equals("notification-001")
                        && gameplay.notifications().all().get(gameplay.notifications().count() - 1)
                        .notificationId().equals("notification-007")
        ));

        write(root.resolve("gameplay-save-hooks.json"), """
                {
                  "schema": "echo.standalone.gameplay_save_hooks.v2",
                  "status": "PASS",
                  "slotId": "%s",
                  "transactionId": "tx-gameplay-001",
                  "filesWritten": %d,
                  "writtenPaths": %s,
                  "manifestTrackedSummary": %s,
                  "manifestTrackedMission": %s,
                  "manifestTrackedSurvival": %s,
                  "manifestTrackedProgression": %s,
                  "manifestTrackedFactions": %s,
                  "manifestTrackedNotifications": %s,
                  "corruptionHealthy": %s,
                  "checkedFiles": %d,
                  "journalEntries": %d
                }
                """.formatted(
                escape(manifest.slotId()),
                saved.commit().filesWritten(),
                jsonStringArray(saved.writtenPaths()),
                manifest.file("gameplay/summary.json").isPresent(),
                manifest.file("gameplay/mission.json").isPresent(),
                manifest.file("gameplay/survival.json").isPresent(),
                manifest.file("gameplay/progression.json").isPresent(),
                manifest.file("gameplay/factions.json").isPresent(),
                manifest.file("gameplay/notifications.json").isPresent(),
                saveCheck.healthy(),
                saveCheck.checkedFiles(),
                saveCheck.journalEntries()
        ));
    }

    private static String objectivesJson(List<EchoGameplayMissionObjective> objectives) {
        return objectives.stream()
                .map(objective -> """
                        {
                          "objectiveId": "%s",
                          "label": "%s",
                          "status": "%s",
                          "progress": %d,
                          "targetProgress": %d,
                          "completed": %s
                        }""".formatted(
                        escape(objective.objectiveId()),
                        escape(objective.label()),
                        objective.status(),
                        objective.progress(),
                        objective.targetProgress(),
                        objective.completed()
                ).strip())
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String factionsJson(List<EchoFactionStanding> factions) {
        return factions.stream()
                .map(faction -> """
                        {
                          "factionId": "%s",
                          "displayName": "%s",
                          "reputation": %d,
                          "hostile": %s
                        }""".formatted(
                        escape(faction.factionId()),
                        escape(faction.displayName()),
                        faction.reputation(),
                        faction.hostile()
                ).strip())
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String notificationsJson(List<EchoGameplayNotification> notifications) {
        return notifications.stream()
                .map(notification -> """
                        {
                          "notificationId": "%s",
                          "severity": "%s",
                          "message": "%s",
                          "tick": %d
                        }""".formatted(
                        escape(notification.notificationId()),
                        notification.severity(),
                        escape(notification.message()),
                        notification.tick()
                ).strip())
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String interactionJson(EchoGameplayInteractionResult result) {
        return """
                {
                  "interactionId": "%s",
                  "success": %s,
                  "objectiveCompleted": %s,
                  "experienceAwarded": %d,
                  "reason": "%s"
                }""".formatted(
                escape(result.interactionId()),
                result.success(),
                result.objectiveCompleted(),
                result.experienceAwarded(),
                escape(result.reason())
        ).strip();
    }

    private static String movementJson(EchoEntityMovementResult result) {
        return """
                {
                  "entityId": "%s",
                  "from": %s,
                  "to": %s,
                  "moved": %s,
                  "reason": "%s"
                }""".formatted(
                escape(result.entityId().value()),
                positionJson(result.from()),
                positionJson(result.to()),
                result.moved(),
                escape(result.reason())
        ).strip();
    }

    private static String positionJson(EchoWorldPosition position) {
        return """
                {"x": %d, "y": %d, "z": %d, "key": "%s"}""".formatted(
                position.x(),
                position.y(),
                position.z(),
                escape(position.key())
        ).trim();
    }

    private static String jsonStringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireDouble(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > 0.001D) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }
}
