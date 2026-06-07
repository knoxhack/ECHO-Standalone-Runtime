package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityMovementIntent;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoFactionRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayHazardResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionState;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
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
import java.nio.file.Files;
import java.nio.file.Path;
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
        require(items.operations().count(
                items.inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                waterId
        ) == 1, "water interaction should consume one ration");
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
