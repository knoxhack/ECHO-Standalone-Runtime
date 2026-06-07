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

public final class EchoRuntimeAshfallPlayableMissionSmokeHarness {
    private EchoRuntimeAshfallPlayableMissionSmokeHarness() {
    }

    public static void main(String[] args) {
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
