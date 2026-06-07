package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.gameplay.EchoAshfallStandaloneMissionRuntime;
import dev.echo.standalone.runtime.gameplay.EchoAshfallStandaloneMissionRuntime.EchoMissionRouteObjective;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplaySaveResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoAshfallMissionUiBridge;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandContext;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandRegistry;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandResult;
import dev.echo.standalone.runtime.ui.EchoUiSurface;
import dev.echo.standalone.runtime.ui.EchoUiTheme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeAgent6AshfallGameplayParitySmokeHarness {
    private static final String REPORT_PATH = "reports/echo/standalone/agent6-ashfall-executable-parity.json";

    private EchoRuntimeAgent6AshfallGameplayParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoAshfallStandaloneMissionRuntime runtime = new EchoAshfallStandaloneMissionRuntime();
        List<EchoMissionRouteObjective> objectives = EchoAshfallStandaloneMissionRuntime.routeObjectives();

        require(objectives.size() == 17, "Agent 6 beta route should expose the full first playable loop");
        require(EchoAshfallStandaloneMissionRuntime.betaRoute().equals(List.of(
                        "new_game",
                        "spawn_in_drop_pod",
                        "starter_kit",
                        "read_field_manual",
                        "collect_scrap_metal",
                        "place_ash_campfire",
                        "craft_basic_tool",
                        "use_basic_tool",
                        "place_water_purifier",
                        "insert_dirty_water",
                        "power_water_purifier",
                        "receive_clean_water",
                        "use_scanner",
                        "discover_recovery_cache",
                        "open_recovery_cache",
                        "complete_first_mission",
                        "unlock_next_objective",
                        "save_load_continue")),
                "Agent 6 beta route should match the first playable loop order");
        EchoGameplayInteractionResult fakeCompletion = new EchoAshfallStandaloneMissionRuntime().apply(
                EchoAshfallStandaloneMissionRuntime.EchoObjectiveTrigger.MISSION_COMPLETED,
                EchoAshfallStandaloneMissionRuntime.FIRST_MISSION_ID);
        require(!fakeCompletion.success(), "mission completion must reject command-only shortcut completion");
        require("missing_playable_actions".equals(fakeCompletion.reason()),
                "fake completion should explain that playable actions are missing");
        for (int index = 0; index < objectives.size(); index++) {
            EchoMissionRouteObjective objective = objectives.get(index);
            EchoGameplayInteractionResult result = runtime.apply(objective.trigger(), objective.target());
            require(result.success(), "route trigger should be accepted: " + objective.objectiveId());
            require(result.objectiveCompleted(), "route trigger should complete objective: " + objective.objectiveId());
            require(runtime.mission().completedObjectiveCount() == index + 1,
                    "completed objective count should advance for " + objective.objectiveId());
        }

        require(runtime.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "first mission should complete after beta route");
        require(runtime.mission().completedObjectiveCount() == runtime.mission().objectiveCount(),
                "all first mission objectives should complete");
        require(runtime.rewardGranted(), "mission reward should be granted");
        require(runtime.nextObjectiveUnlocked(), "next objective should unlock");

        EchoAshfallStandaloneMissionRuntime commandRuntime = new EchoAshfallStandaloneMissionRuntime();
        EchoAshfallMissionUiBridge uiBridge = new EchoAshfallMissionUiBridge(commandRuntime);
        EchoTerminalCommandRegistry commands = new EchoTerminalCommandRegistry();
        uiBridge.registerTerminalCommands(commands);
        EchoTerminalCommandResult terminalResult = commands.execute(new EchoTerminalCommandContext(
                EchoUiTheme.defaultTerminal(),
                "mission open",
                "mission",
                List.of("open")));
        require(terminalResult.outputLines().stream()
                        .anyMatch(line -> line.contains("mission command ignored: gameplay actions required for open")),
                "standalone terminal mission command should refuse to advance gameplay objectives");
        require(commandRuntime.mission().completedObjectiveCount() == 0,
                "terminal mission command should not complete objectives on a fresh route");
        EchoUiSurface hud = new EchoAshfallMissionUiBridge(runtime).missionHudSurface();
        require(hud.lines().stream().anyMatch(line -> line.equals("status=COMPLETED")),
                "mission HUD should display completed state");
        require(hud.lines().stream().anyMatch(line -> line.equals("progress=17/17")),
                "mission HUD should display completed objective progress");

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        Path fixtureRoot = Files.createTempDirectory("echo-agent6-ashfall-parity");
        EchoSaveProfile saveProfile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "agent6-ashfall",
                "Agent 6 Ashfall",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/agent6-ashfall"),
                Map.of("agent", "6", "contract", EchoAshfallStandaloneMissionRuntime.CONTRACT_ID)
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(services, saveProfile);
        EchoGameplaySaveResult saved = runtime.saveTo(saves, "agent6-slot", "agent6-tx-001");
        require(saved.commit().filesWritten() == 1, "Agent 6 save should write mission properties");
        require(saved.writtenPaths().contains("playable/mission.properties"),
                "Agent 6 save should write playable mission state");
        EchoSaveManifest manifest = saves.readManifest("agent6-slot");
        require(manifest.file("playable/mission.properties").isPresent(),
                "save manifest should track Agent 6 mission state");
        EchoSaveCorruptionReport saveCheck = saves.check("agent6-slot");
        require(saveCheck.healthy(), "Agent 6 mission save should pass corruption check");

        EchoAshfallStandaloneMissionRuntime restored = EchoAshfallStandaloneMissionRuntime.restoreFrom(
                saves,
                "agent6-slot");
        require(restored.savedAndRestored(), "restored Agent 6 runtime should mark save/load continuation");
        require(restored.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "restored Agent 6 mission status should stay completed");
        require(restored.mission().completedObjectiveCount() == 17,
                "restored Agent 6 mission should preserve completed objectives");
        require(restored.rewardGranted(), "restored Agent 6 runtime should preserve reward grant");
        require(restored.nextObjectiveUnlocked(), "restored Agent 6 runtime should preserve progression unlock");

        Path report = Path.of(REPORT_PATH);
        Files.createDirectories(report.getParent());
        Files.writeString(report, reportJson(runtime, restored, terminalResult, hud, saved));

        System.out.println("agent6 ashfall gameplay parity PASS objectives="
                + runtime.mission().completedObjectiveCount()
                + "/"
                + runtime.mission().objectiveCount()
                + " terminal=true hud=true saved=true restored="
                + restored.savedAndRestored());
    }

    private static String reportJson(
            EchoAshfallStandaloneMissionRuntime runtime,
            EchoAshfallStandaloneMissionRuntime restored,
            EchoTerminalCommandResult terminalResult,
            EchoUiSurface hud,
            EchoGameplaySaveResult saved) {
        return """
                {
                  "schema": "echo.agent6.ashfall_executable_parity.v1",
                  "agent": "agent-6-ashfall-gameplay",
                  "status": "PASS",
                  "contract": "%s",
                  "missionId": "%s",
                  "completedObjectives": %d,
                  "objectiveCount": %d,
                  "terminalMissionCommandAdvancesGameplay": %s,
                  "hudDisplaysMission": %s,
                  "rewardGranted": %s,
                  "progressionUnlockSaved": %s,
                  "reloadPreservesState": %s,
                  "commandShortcutRejected": true,
                  "savedPaths": ["%s"]
                }
                """.formatted(
                EchoAshfallStandaloneMissionRuntime.CONTRACT_ID,
                runtime.mission().missionId(),
                runtime.mission().completedObjectiveCount(),
                runtime.mission().objectiveCount(),
                false,
                hud.lines().stream().anyMatch(line -> line.equals("progress=17/17")),
                restored.rewardGranted(),
                restored.nextObjectiveUnlocked(),
                restored.savedAndRestored(),
                saved.writtenPaths().getFirst());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
