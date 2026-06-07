package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeSet;

public final class EchoAshfallStandaloneMissionRuntime {
    public static final String CONTRACT_ID = "echoashfallprotocol:ashfall_first_playable_loop";
    public static final String FIRST_MISSION_ID = "echoashfallprotocol:secure_crash_outpost";
    public static final String NEXT_MISSION_ID = "echoashfallprotocol:craft_scrap_knife";
    private static final String COMPLETE_FIRST_MISSION_OBJECTIVE =
            "echoashfallprotocol:secure_crash_outpost/complete_first_mission";
    private static final String UNLOCK_NEXT_OBJECTIVE =
            "echoashfallprotocol:secure_crash_outpost/unlock_next_objective";
    private static final String SAVE_RESTORE_OBJECTIVE =
            "echoashfallprotocol:secure_crash_outpost/save_load_continue";

    private final EchoGameplayMissionState mission;
    private boolean rewardGranted;
    private boolean nextObjectiveUnlocked;
    private boolean savedAndRestored;

    public EchoAshfallStandaloneMissionRuntime() {
        this(new EchoGameplayMissionState(
                FIRST_MISSION_ID,
                "Secure the Crash Outpost",
                objectives()));
    }

    private EchoAshfallStandaloneMissionRuntime(EchoGameplayMissionState mission) {
        this.mission = Objects.requireNonNull(mission, "mission");
    }

    public EchoGameplayMissionState mission() {
        return mission;
    }

    public boolean rewardGranted() {
        return rewardGranted;
    }

    public boolean nextObjectiveUnlocked() {
        return nextObjectiveUnlocked;
    }

    public boolean savedAndRestored() {
        return savedAndRestored;
    }

    public EchoGameplayInteractionResult apply(EchoObjectiveTrigger trigger, String target) {
        Objects.requireNonNull(trigger, "trigger");
        String normalizedTarget = EchoGameplayText.requireText(target, "target");
        EchoMissionRouteObjective objective = routeObjectives().stream()
                .filter(candidate -> candidate.trigger() == trigger)
                .filter(candidate -> candidate.target().equals(normalizedTarget))
                .findFirst()
                .orElse(null);
        if (objective == null) {
            return new EchoGameplayInteractionResult(
                    "ashfall:first_playable_loop",
                    false,
                    false,
                    0,
                    "unmatched_trigger");
        }
        String blockedReason = blockedReason(objective);
        if (!blockedReason.isBlank()) {
            return new EchoGameplayInteractionResult(
                    "ashfall:first_playable_loop",
                    false,
                    false,
                    0,
                    blockedReason);
        }
        boolean completed = mission.completeObjective(objective.objectiveId());
        if (completed && objective.objectiveId().equals(COMPLETE_FIRST_MISSION_OBJECTIVE)) {
            rewardGranted = true;
        }
        if (completed && objective.objectiveId().equals(UNLOCK_NEXT_OBJECTIVE)) {
            nextObjectiveUnlocked = true;
        }
        return new EchoGameplayInteractionResult(
                "ashfall:first_playable_loop",
                true,
                completed,
                completed ? 10 : 0,
                objective.objectiveId());
    }

    public Properties save() {
        Properties properties = new Properties();
        properties.setProperty("contractId", CONTRACT_ID);
        properties.setProperty("missionId", mission.missionId());
        properties.setProperty("rewardGranted", Boolean.toString(rewardGranted));
        properties.setProperty("nextObjectiveUnlocked", Boolean.toString(nextObjectiveUnlocked));
        for (EchoGameplayMissionObjective objective : mission.objectives()) {
            properties.setProperty("objective." + objective.objectiveId(), objective.status().name());
        }
        return properties;
    }

    public EchoGameplaySaveResult saveTo(EchoSaveRuntimeResult saves, String slotId, String transactionId)
            throws IOException {
        Objects.requireNonNull(saves, "saves");
        EchoSaveTransaction transaction = saves.beginTransaction(slotId, transactionId);
        String relativePath = "playable/mission.properties";
        transaction.writeText(relativePath, propertiesText(save()));
        return new EchoGameplaySaveResult(
                transaction.commit(Map.of(
                        "agent6MissionId", mission.missionId(),
                        "agent6MissionStatus", mission.status().name(),
                        "agent6RewardGranted", Boolean.toString(rewardGranted),
                        "agent6NextObjectiveUnlocked", Boolean.toString(nextObjectiveUnlocked))),
                List.of(relativePath));
    }

    public static EchoAshfallStandaloneMissionRuntime restoreFrom(EchoSaveRuntimeResult saves, String slotId)
            throws IOException {
        Objects.requireNonNull(saves, "saves");
        Path path = saves.profile().slot(slotId).dataRoot().resolve("playable/mission.properties");
        Properties properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        return restore(properties);
    }

    public static EchoAshfallStandaloneMissionRuntime restore(Properties properties) {
        Objects.requireNonNull(properties, "properties");
        EchoAshfallStandaloneMissionRuntime runtime = new EchoAshfallStandaloneMissionRuntime();
        for (EchoMissionRouteObjective objective : routeObjectives()) {
            String status = properties.getProperty("objective." + objective.objectiveId(), "");
            if (EchoGameplayObjectiveStatus.COMPLETED.name().equals(status)) {
                runtime.mission.completeObjective(objective.objectiveId());
            }
        }
        runtime.rewardGranted = Boolean.parseBoolean(properties.getProperty("rewardGranted", "false"));
        runtime.nextObjectiveUnlocked = Boolean.parseBoolean(properties.getProperty("nextObjectiveUnlocked", "false"));
        runtime.savedAndRestored = true;
        return runtime;
    }

    public Map<String, Object> snapshot() {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("contractId", CONTRACT_ID);
        snapshot.put("missionId", mission.missionId());
        snapshot.put("missionStatus", mission.status().name());
        snapshot.put("completedObjectives", mission.completedObjectiveCount());
        snapshot.put("objectiveCount", mission.objectiveCount());
        snapshot.put("rewardGranted", rewardGranted);
        snapshot.put("nextObjectiveUnlocked", nextObjectiveUnlocked);
        snapshot.put("savedAndRestored", savedAndRestored);
        return Map.copyOf(snapshot);
    }

    public static List<String> betaRoute() {
        return List.of(
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
                "save_load_continue");
    }

    public static List<EchoMissionRouteObjective> routeObjectives() {
        return List.of(
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/spawn_in_drop_pod",
                        EchoObjectiveTrigger.PLAYER_SPAWNED,
                        "echoashfallprotocol:drop_pod"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/starter_kit",
                        EchoObjectiveTrigger.ITEM_COLLECTED,
                        "echoashfallprotocol:starter_kit"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/read_field_manual",
                        EchoObjectiveTrigger.ITEM_USED,
                        "echoashfallprotocol:field_manual"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/collect_scrap_metal",
                        EchoObjectiveTrigger.ITEM_COLLECTED,
                        "echoashfallprotocol:scrap_metal"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/place_ash_campfire",
                        EchoObjectiveTrigger.BLOCK_PLACED,
                        "echoashfallprotocol:ash_campfire"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/craft_basic_tool",
                        EchoObjectiveTrigger.RECIPE_CRAFTED,
                        "echoashfallprotocol:scrap_knife"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/use_basic_tool",
                        EchoObjectiveTrigger.BLOCK_BROKEN,
                        "echoashfallprotocol:rusted_metal_debris"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/place_water_purifier",
                        EchoObjectiveTrigger.BLOCK_PLACED,
                        "echoashfallprotocol:water_purifier"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/insert_dirty_water",
                        EchoObjectiveTrigger.ITEM_USED,
                        "echoashfallprotocol:dirty_water_bottle"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/power_water_purifier",
                        EchoObjectiveTrigger.MACHINE_POWERED,
                        "echoashfallprotocol:water_purifier"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/receive_clean_water",
                        EchoObjectiveTrigger.MACHINE_OUTPUT_CREATED,
                        "echoashfallprotocol:clean_water_bottle"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/use_scanner",
                        EchoObjectiveTrigger.SCANNER_USED,
                        "echoashfallprotocol:portable_signal_scanner"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/discover_recovery_cache",
                        EchoObjectiveTrigger.REGION_ENTERED,
                        "echoashfallprotocol:recovery_cache"),
                new EchoMissionRouteObjective(
                        "echoashfallprotocol:secure_crash_outpost/open_recovery_cache",
                        EchoObjectiveTrigger.TERMINAL_OPENED,
                        "echoashfallprotocol:recovery_cache"),
                new EchoMissionRouteObjective(
                        COMPLETE_FIRST_MISSION_OBJECTIVE,
                        EchoObjectiveTrigger.MISSION_COMPLETED,
                        "echoashfallprotocol:secure_crash_outpost"),
                new EchoMissionRouteObjective(
                        UNLOCK_NEXT_OBJECTIVE,
                        EchoObjectiveTrigger.MISSION_OBJECTIVE_COMPLETED,
                        NEXT_MISSION_ID),
                new EchoMissionRouteObjective(
                        SAVE_RESTORE_OBJECTIVE,
                        EchoObjectiveTrigger.SAVE_RESTORED,
                        CONTRACT_ID));
    }

    private String blockedReason(EchoMissionRouteObjective objective) {
        if (objective.objectiveId().equals(COMPLETE_FIRST_MISSION_OBJECTIVE)
                && !allObjectivesBefore(COMPLETE_FIRST_MISSION_OBJECTIVE)) {
            return "missing_playable_actions";
        }
        if (objective.objectiveId().equals(UNLOCK_NEXT_OBJECTIVE)
                && !objectiveCompleted(COMPLETE_FIRST_MISSION_OBJECTIVE)) {
            return "mission_not_completed";
        }
        if (objective.objectiveId().equals(SAVE_RESTORE_OBJECTIVE)
                && !objectiveCompleted(UNLOCK_NEXT_OBJECTIVE)) {
            return "next_objective_locked";
        }
        return "";
    }

    private boolean allObjectivesBefore(String objectiveId) {
        for (EchoMissionRouteObjective objective : routeObjectives()) {
            if (objective.objectiveId().equals(objectiveId)) {
                return true;
            }
            if (!objectiveCompleted(objective.objectiveId())) {
                return false;
            }
        }
        return false;
    }

    private boolean objectiveCompleted(String objectiveId) {
        return mission.objective(objectiveId)
                .map(EchoGameplayMissionObjective::completed)
                .orElse(false);
    }

    private static List<EchoGameplayMissionObjective> objectives() {
        return routeObjectives().stream()
                .map(objective -> new EchoGameplayMissionObjective(
                        objective.objectiveId(),
                        objective.objectiveId().substring(objective.objectiveId().lastIndexOf('/') + 1),
                        EchoGameplayObjectiveStatus.ACTIVE,
                        0,
                        1))
                .toList();
    }

    private static String propertiesText(Properties properties) {
        StringBuilder builder = new StringBuilder();
        for (String key : new TreeSet<>(properties.stringPropertyNames())) {
            builder.append(escapeProperty(key, true))
                    .append("=")
                    .append(escapeProperty(properties.getProperty(key), false))
                    .append("\n");
        }
        return builder.toString();
    }

    private static String escapeProperty(String value, boolean key) {
        StringBuilder builder = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '\\' -> builder.append("\\\\");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                case '\f' -> builder.append("\\f");
                case ' ', ':', '=', '#', '!' -> {
                    if (key || index == 0) {
                        builder.append('\\');
                    }
                    builder.append(character);
                }
                default -> builder.append(character);
            }
        }
        return builder.toString();
    }

    public enum EchoObjectiveTrigger {
        PLAYER_SPAWNED("player.spawned"),
        ITEM_USED("player.item_used"),
        ITEM_COLLECTED("player.item_collected"),
        RECIPE_CRAFTED("player.recipe_crafted"),
        BLOCK_PLACED("player.block_placed"),
        BLOCK_BROKEN("player.block_broken"),
        TERMINAL_OPENED("player.terminal_opened"),
        LENS_SCANNED("player.scanner_used"),
        SCANNER_USED("player.scanner_used"),
        REGION_ENTERED("player.region_entered"),
        HAZARD_SURVIVED("hazard.survived"),
        ENTITY_DEFEATED("entity.defeated"),
        MACHINE_POWERED("player.machine_powered"),
        MACHINE_OUTPUT_CREATED("machine.output_created"),
        MISSION_OBJECTIVE_COMPLETED("mission.objective_completed"),
        MISSION_COMPLETED("mission.completed"),
        SAVE_RESTORED("save.restored");

        private final String id;

        EchoObjectiveTrigger(String id) {
            this.id = id;
        }

        public String id() {
            return id;
        }
    }

    public record EchoMissionRouteObjective(
            String objectiveId,
            EchoObjectiveTrigger trigger,
            String target) {
    }
}
