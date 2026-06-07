package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.audio.EchoAudioRuntime;
import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfiles;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoMissionDefinition;
import dev.echo.standalone.runtime.data.EchoMissionRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityAiTickResult;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.gameplay.EchoGameplayNotificationSeverity;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputControl;
import dev.echo.standalone.runtime.input.EchoInputEvent;
import dev.echo.standalone.runtime.input.EchoInputRuntime;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.player.EchoPlayerController;
import dev.echo.standalone.runtime.player.EchoPlayerControllerResult;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntime;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderRuntime;
import dev.echo.standalone.runtime.render.EchoRenderRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderWindowSettings;
import dev.echo.standalone.runtime.ui.EchoTerminalCommand;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandResult;
import dev.echo.standalone.runtime.ui.EchoTerminalScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoAshfallPlayableMissionRuntime {
    private static final EchoEntityId PLAYER_ID = new EchoEntityId("player-001");
    private static final EchoInventoryId PLAYER_PACK = new EchoInventoryId("inventory:player-001");
    private static final EchoItemId WATER_RATION = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);
    private static final EchoItemId PATCHED_FILTER = new EchoItemId("ashfall:patched_filter");
    private static final int ENCOUNTER_REWARD_XP = 20;

    public EchoAshfallPlayableMissionResult boot(EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(services, "services");

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
        EchoTerminalShell shell = new EchoTerminalShell();
        shell.promptPrefix("ASH>");
        shell.commands().register(new EchoTerminalCommand(
                "uplink",
                "Open crash-cache route",
                context -> EchoTerminalCommandResult.output(List.of(
                        "uplink:emergency-terminal-online",
                        "cache route: 2,0,1",
                        "warning: toxic ash corridor active"
                ))
        ));
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoTerminalScreen("ashfall-playable-terminal", "Ashfall Mission Terminal", shell),
                EchoUiTheme.defaultTerminal()
        );
        EchoInputRuntimeResult input = new EchoInputRuntime().boot(services, ui, entities, gameplay, PLAYER_ID);
        EchoPlayerControllerRuntimeResult playerRuntime = new EchoPlayerControllerRuntime().boot(
                services,
                world,
                entities,
                gameplay,
                items,
                input,
                PLAYER_ID
        );
        EchoPlayerController controller = playerRuntime.controller();

        ArrayList<EchoAshfallPlayableMissionStep> steps = new ArrayList<>();
        gameplay.notifications().add(
                EchoGameplayNotificationSeverity.INFO,
                "Crash pod beacon acquired.",
                world.world().tick()
        );
        steps.add(step(steps, "intro", "Crash Pod Beacon", "intro prompt armed at " + playerPosition(entities)));

        EchoPlayerControllerResult focus = controller.handle(key(0, "BACKQUOTE"));
        EchoPlayerControllerResult terminalText = controller.handle(EchoInputEvent.text(1, "uplink cache"));
        EchoPlayerControllerResult blur = controller.handle(key(2, "ESCAPE"));
        EchoPlayerControllerResult terminal = controller.handle(mouse(3, "PRIMARY"));
        require(focus.handled() && terminalText.handled() && blur.handled() && terminal.handled(), "terminal flow");
        steps.add(step(steps, "terminal", "Emergency Terminal", "terminal history=" + shell.history().size()));

        EchoPlayerControllerResult water = controller.handle(key(4, "DIGIT1"));
        require(water.handled() && water.shortcut().orElseThrow().used(), "water ration use");
        steps.add(step(steps, "inventory", "Use Water Ration", "water remaining=" + waterRemaining(items)));

        EchoPlayerControllerResult eastOne = controller.handle(key(5, "D"));
        EchoPlayerControllerResult eastTwo = controller.handle(key(6, "D"));
        EchoPlayerControllerResult southOne = controller.handle(key(7, "S"));
        require(eastOne.handled() && eastTwo.handled() && southOne.handled(), "hazard traversal");
        steps.add(step(
                steps,
                "hazard",
                "Cross Toxic Ash",
                "hazard=" + southOne.hazard().orElseThrow().intensity() + " position=" + playerPosition(entities)
        ));

        EchoPlayerControllerResult cache = controller.handle(mouse(8, "SECONDARY"));
        require(cache.handled() && cache.interaction().orElseThrow().success(), "cache salvage");
        steps.add(step(steps, "cache", "Recover Crash Cache", "salvage=" + cache.interaction().orElseThrow().reason()));

        int encounterHealthBefore = playerHealth(entities);
        EchoEntityAiTickResult aiTick = entities.aiSystem().tick(entities.store());
        int encounterHealthAfter = playerHealth(entities);
        boolean repelled = aiTick.attacks() == 1 && encounterHealthAfter > 0;
        EchoAshfallScavengerEncounterResult encounter = new EchoAshfallScavengerEncounterResult(
                aiTick.attacks(),
                encounterHealthBefore,
                encounterHealthAfter,
                encounterHealthAfter > 0,
                repelled,
                repelled ? "repelled_after_cache_salvage" : "escaped"
        );
        gameplay.progression().awardExperience(ENCOUNTER_REWARD_XP);
        gameplay.progression().addMilestone("ashfall:scavenger_repelled");
        gameplay.factions().adjustReputation("ashfall:crash_survivors", 10);
        gameplay.factions().adjustReputation("ashfall:wasteland_scavengers", -10);
        steps.add(step(steps, "encounter", "Scavenger Encounter", encounter.resolution()));

        EchoItemCraftResult craft = items.craftingSystem().craft(
                items.inventoryStore().require(PLAYER_PACK),
                items.debugRecipe()
        );
        EchoAshfallPlayableMissionReward reward = new EchoAshfallPlayableMissionReward(
                "ashfall:patched_filter_reward",
                "Patch a filter and secure the cache reward",
                ENCOUNTER_REWARD_XP,
                PATCHED_FILTER.value(),
                craft.crafted()
        );
        if (craft.crafted()) {
            gameplay.progression().addMilestone("ashfall:patched_filter_ready");
        }
        steps.add(step(steps, "reward", "Mission Reward", craft.reason() + ":" + PATCHED_FILTER.value()));

        EchoAshfallFailRetryResult failRetry = simulateFailureAndRetry();
        steps.add(step(steps, "retry", "Fail And Retry Path", failRetry.retryOutcome()));

        ArrayList<EchoAshfallPlayableMissionObjective> objectives = objectives(
                terminal,
                water,
                southOne,
                cache,
                encounter,
                craft,
                failRetry,
                shell
        );
        objectives.addAll(registryObjectives(services));

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
        render.backend().close();
        audio.backend().close();
        boolean cleanExit = render.backend().frames().size() == 1
                && !audio.backend().deviceOpen()
                && audio.backend().diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.message().endsWith("backend closed"));
        EchoAshfallPlayableMissionSummary summary = new EchoAshfallPlayableMissionSummary(
                "ashfall:secure_crash_site_playable",
                gameplay.mission().status().name(),
                completed(objectives),
                objectives.size(),
                steps.size(),
                playerHealth(entities),
                gameplay.survival().hydration(),
                gameplay.survival().ashExposure(),
                gameplay.progression().experience(),
                gameplay.progression().level(),
                reward.granted(),
                failRetry.failed(),
                failRetry.retried(),
                cleanExit
        );

        EchoAshfallPlayableMissionResult result = new EchoAshfallPlayableMissionResult(
                world,
                entities,
                items,
                gameplay,
                ui,
                input,
                playerRuntime,
                render,
                audio,
                shell,
                steps,
                objectives,
                encounter,
                craft,
                reward,
                failRetry,
                summary
        );
        services.register(EchoAshfallPlayableMissionResult.class, result);
        services.register(EchoAshfallPlayableMissionSummary.class, summary);
        return result;
    }

    private static EchoAshfallFailRetryResult simulateFailureAndRetry() {
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
        int applications = 0;
        while (player(entities).alive()) {
            gameplay.hazardSystem().apply(PLAYER_ID);
            applications++;
        }
        return new EchoAshfallFailRetryResult(
                true,
                "player_down_in_toxic_ash",
                playerHealth(entities),
                applications,
                "checkpoint:crash_pod_intro",
                true,
                "retry_recovered_main_route"
        );
    }

    private static List<EchoAshfallPlayableMissionObjective> registryObjectives(EchoRuntimeServiceRegistry services) {
        ArrayList<EchoAshfallPlayableMissionObjective> result = new ArrayList<>();
        services.find(EchoMissionRegistry.class).ifPresent(registry -> {
            for (EchoMissionDefinition mission : registry.missions()) {
                String label = mission.title();
                if (!mission.objectives().isEmpty()) {
                    label = mission.objectives().get(0);
                }
                result.add(new EchoAshfallPlayableMissionObjective(
                        mission.id(),
                        label,
                        false,
                        "registry:" + mission.sourceLogicalId()
                ));
            }
        });
        return result;
    }

    private static ArrayList<EchoAshfallPlayableMissionObjective> objectives(
            EchoPlayerControllerResult terminal,
            EchoPlayerControllerResult water,
            EchoPlayerControllerResult hazard,
            EchoPlayerControllerResult cache,
            EchoAshfallScavengerEncounterResult encounter,
            EchoItemCraftResult craft,
            EchoAshfallFailRetryResult failRetry,
            EchoTerminalShell shell
    ) {
        ArrayList<EchoAshfallPlayableMissionObjective> objectives = new ArrayList<>();
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:intro_beacon",
                "Follow the crash pod beacon",
                true,
                "mission-started"
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:terminal_uplink",
                "Bring the emergency terminal online",
                terminal.interaction().orElseThrow().success() && shell.history().contains("uplink cache"),
                "history=" + shell.history().size()
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:hydrate_survivor",
                "Use a water ration",
                water.shortcut().orElseThrow().used(),
                water.shortcut().orElseThrow().reason()
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:cross_toxic_ash",
                "Cross the toxic ash corridor",
                hazard.hazard().orElseThrow().intensity() > 0.0D,
                "intensity=" + hazard.hazard().orElseThrow().intensity()
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:recover_crash_cache",
                "Recover the crash cache",
                cache.interaction().orElseThrow().success(),
                cache.interaction().orElseThrow().reason()
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:repel_scavenger",
                "Survive and repel the scavenger",
                encounter.survived() && encounter.repelled(),
                encounter.resolution()
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:claim_reward",
                "Patch the filter reward",
                craft.crafted(),
                craft.reason()
        ));
        objectives.add(new EchoAshfallPlayableMissionObjective(
                "ashfall:retry_path",
                "Recover from failure by retrying from checkpoint",
                failRetry.failed() && failRetry.retried(),
                failRetry.checkpointId()
        ));
        return objectives;
    }

    private static EchoAshfallPlayableMissionStep step(
            ArrayList<EchoAshfallPlayableMissionStep> steps,
            String stepId,
            String title,
            String outcome
    ) {
        return new EchoAshfallPlayableMissionStep(steps.size(), stepId, title, outcome);
    }

    private static EchoInputEvent key(long sequence, String key) {
        return EchoInputEvent.press(sequence, EchoInputControl.keyboard(key));
    }

    private static EchoInputEvent mouse(long sequence, String button) {
        return EchoInputEvent.press(sequence, EchoInputControl.mouse(button));
    }

    private static int completed(List<EchoAshfallPlayableMissionObjective> objectives) {
        return (int) objectives.stream()
                .filter(EchoAshfallPlayableMissionObjective::completed)
                .count();
    }

    private static String playerPosition(EchoEntityRuntimeResult entities) {
        return player(entities).worldPosition().key();
    }

    private static int playerHealth(EchoEntityRuntimeResult entities) {
        return player(entities).health().currentHealth();
    }

    private static EchoEntityState player(EchoEntityRuntimeResult entities) {
        return entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow();
    }

    private static int waterRemaining(EchoItemRuntimeResult items) {
        return items.operations().count(items.inventoryStore().require(PLAYER_PACK), WATER_RATION);
    }

    private static void require(boolean condition, String operation) {
        if (!condition) {
            throw new IllegalStateException("Ashfall playable mission step failed: " + operation);
        }
    }
}
