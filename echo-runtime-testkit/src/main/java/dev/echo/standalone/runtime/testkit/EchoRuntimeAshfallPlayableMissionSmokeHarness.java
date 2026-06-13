package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallFailRetryResult;
import dev.echo.standalone.runtime.app.EchoAshfallPlayableMissionResult;
import dev.echo.standalone.runtime.app.EchoAshfallPlayableMissionRuntime;
import dev.echo.standalone.runtime.app.EchoAshfallPlayableMissionSummary;
import dev.echo.standalone.runtime.app.EchoAshfallScavengerEncounterResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class EchoRuntimeAshfallPlayableMissionSmokeHarness {
    private EchoRuntimeAshfallPlayableMissionSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAshfallPlayableMissionResult mission = new EchoAshfallPlayableMissionRuntime().boot(services);
        EchoAshfallPlayableMissionSummary summary = mission.summary();

        require(services.require(EchoAshfallPlayableMissionResult.class) == mission,
                "playable mission result should be service-bound");
        require(services.require(EchoAshfallPlayableMissionSummary.class) == summary,
                "playable mission summary should be service-bound");

        require(summary.missionId().equals("ashfall:secure_crash_site_playable"),
                "mission id should be stable");
        require(summary.status().equals(EchoGameplayMissionStatus.COMPLETED.name()),
                "expanded mission should complete the gameplay mission");
        require(summary.completedObjectives() == 8 && summary.totalObjectives() == 8,
                "all expanded mission objectives should complete: " + mission.objectives());
        require(summary.stepCount() == 8, "expanded mission should execute eight deterministic steps");
        require(summary.playerHealth() == 83, "player should survive hazard traversal and scavenger attack");
        requireClose(summary.hydration(), 70.0D, "hydration");
        requireClose(summary.ashExposure(), 21.6D, "ash exposure");
        require(summary.experience() == 90, "expanded mission should award deterministic experience");
        require(summary.level() == 2, "expanded mission should reach level two");
        require(summary.rewardGranted(), "expanded mission reward should be granted");
        require(summary.failBranchCovered() && summary.retryRecovered(),
                "expanded mission should cover fail and retry path");
        require(summary.cleanExit(), "expanded mission render/audio backends should close cleanly");

        require(mission.terminalShell().history().contains("uplink cache"),
                "terminal flow should submit the uplink command");
        require(mission.terminalShell().outputLines().stream().anyMatch(line -> line.contains("cache route: 2,0,1")),
                "terminal flow should expose the cache route");
        require(mission.objectives().stream().allMatch(objective -> objective.completed()),
                "objective list should contain only completed objectives");
        require(mission.objectives().stream().anyMatch(objective ->
                        objective.objectiveId().equals("ashfall:repel_scavenger")),
                "scavenger objective should be represented");
        require(mission.objectives().stream().anyMatch(objective ->
                        objective.objectiveId().equals("ashfall:retry_path")),
                "retry objective should be represented");

        EchoAshfallScavengerEncounterResult encounter = mission.encounter();
        require(encounter.attacks() == 1, "scavenger should attack once");
        require(encounter.playerHealthBefore() == 88 && encounter.playerHealthAfter() == 83,
                "scavenger attack should deal deterministic damage");
        require(encounter.survived() && encounter.repelled(),
                "scavenger should be survived and repelled");
        require(encounter.resolution().equals("repelled_after_cache_salvage"),
                "encounter should use the cache salvage resolution");

        EchoAshfallFailRetryResult failRetry = mission.failRetry();
        require(failRetry.failed(), "failure branch should fail");
        require(failRetry.failureReason().equals("player_down_in_toxic_ash"),
                "failure branch should be toxic ash");
        require(failRetry.failureHealth() == 0, "failure branch should down the player");
        require(failRetry.hazardApplications() == 25, "failure branch should apply hazard deterministically");
        require(failRetry.retried() && failRetry.retryOutcome().equals("retry_recovered_main_route"),
                "retry branch should recover to the main route");

        require(mission.rewardCraft().crafted(), "patched filter should be crafted as reward");
        require(mission.items().operations().count(
                mission.items().inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                new EchoItemId("ashfall:patched_filter")
        ) == 1, "player pack should contain one patched filter reward");
        require(mission.gameplay().progression().milestones().contains("ashfall:scavenger_repelled"),
                "progression should record scavenger milestone");
        require(mission.gameplay().progression().milestones().contains("ashfall:patched_filter_ready"),
                "progression should record reward milestone");
        require(mission.gameplay().factions().require("ashfall:crash_survivors").reputation() == 25,
                "survivor reputation should include encounter reward");
        require(mission.gameplay().factions().require("ashfall:wasteland_scavengers").reputation() == -40,
                "scavenger reputation should include encounter consequence");
        require(mission.render().backend().frames().size() == 1,
                "expanded mission should render one frame");
        require(mission.audio().initialEvents().size() == 3,
                "completed mission should not queue active-mission stinger");
        require(!mission.audio().backend().deviceOpen(),
                "audio device should be closed after mission shutdown");

        writeReports(Path.of(".").toAbsolutePath().normalize(), mission);
        System.out.println("phase15.7 ashfall playable mission smoke PASS objectives="
                + summary.completedObjectives()
                + "/"
                + summary.totalObjectives()
                + " steps="
                + summary.stepCount()
                + " health="
                + summary.playerHealth()
                + " xp="
                + summary.experience()
                + " retry="
                + summary.retryRecovered()
                + " reward="
                + mission.reward().itemId());
    }

    private static void writeReports(Path standaloneRoot, EchoAshfallPlayableMissionResult mission) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        EchoAshfallPlayableMissionSummary summary = mission.summary();
        EchoAshfallScavengerEncounterResult encounter = mission.encounter();
        EchoAshfallFailRetryResult retry = mission.failRetry();

        write(root.resolve("runtime-ashfall-playable-mission.json"), """
                {
                  "schema": "echo.standalone.runtime_ashfall_playable_mission.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "missionId": "%s",
                  "missionStatus": "%s",
                  "completedObjectives": %d,
                  "totalObjectives": %d,
                  "stepCount": %d,
                  "playerHealth": %d,
                  "hydration": %.3f,
                  "ashExposure": %.3f,
                  "experience": %d,
                  "level": %d,
                  "rewardGranted": %s,
                  "failBranchCovered": %s,
                  "retryRecovered": %s,
                  "cleanExit": %s,
                  "terminalCommandSubmitted": %s,
                  "terminalCacheRouteVisible": %s,
                  "scavengerRepelled": %s,
                  "patchedFilterRewardPresent": %s,
                  "renderFrames": %d,
                  "audioDeviceClosed": %s,
                  "steps": %s
                }
                """.formatted(
                escape(summary.missionId()),
                escape(summary.status()),
                summary.completedObjectives(),
                summary.totalObjectives(),
                summary.stepCount(),
                summary.playerHealth(),
                summary.hydration(),
                summary.ashExposure(),
                summary.experience(),
                summary.level(),
                summary.rewardGranted(),
                summary.failBranchCovered(),
                summary.retryRecovered(),
                summary.cleanExit(),
                mission.terminalShell().history().contains("uplink cache"),
                mission.terminalShell().outputLines().stream().anyMatch(line -> line.contains("cache route: 2,0,1")),
                encounter.repelled(),
                mission.reward().granted() && "ashfall:patched_filter".equals(mission.reward().itemId()),
                mission.render().backend().frames().size(),
                !mission.audio().backend().deviceOpen(),
                jsonSteps(mission.steps())
        ));

        write(root.resolve("ashfall-playable-intro.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_intro.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "missionId": "%s",
                  "firstStepId": "%s",
                  "firstStepTitle": "%s",
                  "introPromptArmed": %s,
                  "notificationArmed": true,
                  "playerHealth": %d,
                  "hydration": %.3f
                }
                """.formatted(
                escape(summary.missionId()),
                escape(mission.steps().get(0).stepId()),
                escape(mission.steps().get(0).title()),
                mission.steps().get(0).outcome().contains("intro prompt armed"),
                summary.playerHealth(),
                summary.hydration()
        ));

        write(root.resolve("ashfall-playable-objectives.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_objectives.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "missionId": "%s",
                  "completedObjectives": %d,
                  "totalObjectives": %d,
                  "allCompleted": %s,
                  "requiredObjectiveIds": %s,
                  "objectives": %s
                }
                """.formatted(
                escape(summary.missionId()),
                summary.completedObjectives(),
                summary.totalObjectives(),
                summary.completedObjectives() == summary.totalObjectives(),
                jsonArray(List.of(
                        "ashfall:intro_beacon",
                        "ashfall:terminal_uplink",
                        "ashfall:hydrate_survivor",
                        "ashfall:cross_toxic_ash",
                        "ashfall:recover_crash_cache",
                        "ashfall:repel_scavenger",
                        "ashfall:claim_reward",
                        "ashfall:retry_path"
                )),
                jsonObjectives(mission.objectives())
        ));

        write(root.resolve("ashfall-playable-terminal-flow.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_terminal_flow.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "screenId": "%s",
                  "history": %s,
                  "outputLines": %s,
                  "uplinkCommandSubmitted": %s,
                  "cacheRouteVisible": %s,
                  "terminalStepOutcome": "%s"
                }
                """.formatted(
                escape(mission.ui().frame().screen().id()),
                jsonArray(mission.terminalShell().history()),
                jsonArray(mission.terminalShell().outputLines()),
                mission.terminalShell().history().contains("uplink cache"),
                mission.terminalShell().outputLines().stream().anyMatch(line -> line.contains("cache route: 2,0,1")),
                escape(stepOutcome(mission, "terminal"))
        ));

        write(root.resolve("ashfall-playable-inventory.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_inventory.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "waterRationUsed": true,
                  "waterStepOutcome": "%s",
                  "rewardCrafted": %s,
                  "craftedItemId": "%s",
                  "patchedFilterCount": %d,
                  "inventoryMilestones": %s
                }
                """.formatted(
                escape(stepOutcome(mission, "inventory")),
                mission.rewardCraft().crafted(),
                escape(mission.reward().itemId()),
                mission.items().operations().count(
                        mission.items().inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                        new EchoItemId("ashfall:patched_filter")
                ),
                jsonArray(mission.gameplay().progression().milestones().stream()
                        .filter(milestone -> milestone.contains("patched_filter") || milestone.contains("scavenger"))
                        .toList())
        ));

        write(root.resolve("ashfall-playable-hazard-zones.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_hazard_zones.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "hazardStepOutcome": "%s",
                  "playerHealthAfterRoute": %d,
                  "hydrationAfterRoute": %.3f,
                  "ashExposureAfterRoute": %.3f,
                  "failBranchReason": "%s",
                  "failBranchHazardApplications": %d,
                  "retryRecovered": %s
                }
                """.formatted(
                escape(stepOutcome(mission, "hazard")),
                summary.playerHealth(),
                summary.hydration(),
                summary.ashExposure(),
                escape(retry.failureReason()),
                retry.hazardApplications(),
                retry.retried()
        ));

        write(root.resolve("ashfall-playable-scavenger-encounter.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_scavenger_encounter.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "attacks": %d,
                  "playerHealthBefore": %d,
                  "playerHealthAfter": %d,
                  "survived": %s,
                  "repelled": %s,
                  "resolution": "%s",
                  "survivorReputation": %d,
                  "scavengerReputation": %d
                }
                """.formatted(
                encounter.attacks(),
                encounter.playerHealthBefore(),
                encounter.playerHealthAfter(),
                encounter.survived(),
                encounter.repelled(),
                escape(encounter.resolution()),
                mission.gameplay().factions().require("ashfall:crash_survivors").reputation(),
                mission.gameplay().factions().require("ashfall:wasteland_scavengers").reputation()
        ));

        write(root.resolve("ashfall-playable-rewards.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_rewards.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "rewardId": "%s",
                  "label": "%s",
                  "experienceAwarded": %d,
                  "totalExperience": %d,
                  "level": %d,
                  "itemId": "%s",
                  "granted": %s,
                  "craftReason": "%s"
                }
                """.formatted(
                escape(mission.reward().rewardId()),
                escape(mission.reward().label()),
                mission.reward().experienceAwarded(),
                summary.experience(),
                summary.level(),
                escape(mission.reward().itemId()),
                mission.reward().granted(),
                escape(mission.rewardCraft().reason())
        ));

        write(root.resolve("ashfall-playable-fail-retry.json"), """
                {
                  "schema": "echo.standalone.ashfall_playable_fail_retry.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "failed": %s,
                  "failureReason": "%s",
                  "failureHealth": %d,
                  "hazardApplications": %d,
                  "checkpointId": "%s",
                  "retried": %s,
                  "retryOutcome": "%s"
                }
                """.formatted(
                retry.failed(),
                escape(retry.failureReason()),
                retry.failureHealth(),
                retry.hazardApplications(),
                escape(retry.checkpointId()),
                retry.retried(),
                escape(retry.retryOutcome())
        ));
    }

    private static String stepOutcome(EchoAshfallPlayableMissionResult mission, String stepId) {
        return mission.steps().stream()
                .filter(step -> step.stepId().equals(stepId))
                .map(step -> step.outcome())
                .findFirst()
                .orElse("");
    }

    private static String jsonSteps(List<dev.echo.standalone.runtime.app.EchoAshfallPlayableMissionStep> steps) {
        return steps.stream()
                .map(step -> "{\"index\": " + step.index()
                        + ", \"stepId\": \"" + escape(step.stepId())
                        + "\", \"title\": \"" + escape(step.title())
                        + "\", \"outcome\": \"" + escape(step.outcome()) + "\"}")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String jsonObjectives(List<dev.echo.standalone.runtime.app.EchoAshfallPlayableMissionObjective> objectives) {
        return objectives.stream()
                .map(objective -> "{\"objectiveId\": \"" + escape(objective.objectiveId())
                        + "\", \"label\": \"" + escape(objective.label())
                        + "\", \"completed\": " + objective.completed()
                        + ", \"proof\": \"" + escape(objective.proof()) + "\"}")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
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

    private static void requireClose(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 0.001D) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
