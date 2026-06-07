package dev.echo.standalone.runtime.ui;

import dev.echo.standalone.runtime.gameplay.EchoAshfallStandaloneMissionRuntime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoAshfallMissionUiBridge {
    private final EchoAshfallStandaloneMissionRuntime missionRuntime;

    public EchoAshfallMissionUiBridge(EchoAshfallStandaloneMissionRuntime missionRuntime) {
        this.missionRuntime = Objects.requireNonNull(missionRuntime, "missionRuntime");
    }

    public void registerTerminalCommands(EchoTerminalCommandRegistry commands) {
        Objects.requireNonNull(commands, "commands");
        commands.register(new EchoTerminalCommand("mission", "Show and advance the Ashfall mission route", context ->
                executeMissionCommand(context.args())));
    }

    public EchoTerminalCommandResult executeMissionCommand(List<String> args) {
        List<String> safeArgs = args == null ? List.of() : List.copyOf(args);
        if (safeArgs.isEmpty() || "status".equalsIgnoreCase(safeArgs.getFirst())) {
            return EchoTerminalCommandResult.output(terminalStatusLines());
        }
        String action = safeArgs.getFirst().toLowerCase(java.util.Locale.ROOT);
        boolean advanced = switch (action) {
            case "open", "scan", "salvage", "recover", "complete" -> false;
            default -> false;
        };
        ArrayList<String> lines = new ArrayList<>(terminalStatusLines());
        lines.add(advanced
                ? "mission command accepted: " + action
                : "mission command ignored: gameplay actions required for " + action);
        return EchoTerminalCommandResult.output(lines);
    }

    public EchoUiSurface missionHudSurface() {
        return new EchoUiSurface(
                "echoashfallprotocol:agent6_mission_hud",
                "Ashfall Mission",
                List.of(
                        missionRuntime.mission().title(),
                        "status=" + missionRuntime.mission().status().name(),
                        "progress=" + missionRuntime.mission().completedObjectiveCount()
                                + "/" + missionRuntime.mission().objectiveCount(),
                        "rewardGranted=" + missionRuntime.rewardGranted(),
                        "nextObjectiveUnlocked=" + missionRuntime.nextObjectiveUnlocked()),
                "hud/mission");
    }

    private List<String> terminalStatusLines() {
        return List.of(
                "mission.id=" + missionRuntime.mission().missionId(),
                "mission.status=" + missionRuntime.mission().status().name(),
                "mission.progress=" + missionRuntime.mission().completedObjectiveCount()
                        + "/" + missionRuntime.mission().objectiveCount(),
                "mission.rewardGranted=" + missionRuntime.rewardGranted(),
                "mission.nextObjective=" + (missionRuntime.nextObjectiveUnlocked()
                        ? EchoAshfallStandaloneMissionRuntime.NEXT_MISSION_ID
                        : "locked"));
    }
}
