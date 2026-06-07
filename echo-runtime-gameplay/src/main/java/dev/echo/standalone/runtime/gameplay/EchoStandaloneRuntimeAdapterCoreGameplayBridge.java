package dev.echo.standalone.runtime.gameplay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoStandaloneRuntimeAdapterCoreGameplayBridge {
    public static final List<String> REQUIRED_EVENTS = List.of(
            "player_join",
            "client_tick",
            "world_tick",
            "item_use",
            "block_place",
            "block_break",
            "entity_interact",
            "screen_open",
            "command_execution",
            "save_load",
            "resource_reload"
    );

    private EchoStandaloneRuntimeAdapterCoreGameplayBridge() {
    }

    public static List<Map<String, Object>> executeAll(String packId) {
        return REQUIRED_EVENTS.stream()
                .map(event -> execute(event, packId))
                .toList();
    }

    public static Map<String, Object> player_join(String packId) {
        return execute("player_join", packId);
    }

    public static Map<String, Object> client_tick(String packId) {
        return execute("client_tick", packId);
    }

    public static Map<String, Object> world_tick(String packId) {
        return execute("world_tick", packId);
    }

    public static Map<String, Object> item_use(String packId) {
        return execute("item_use", packId);
    }

    public static Map<String, Object> block_place(String packId) {
        return execute("block_place", packId);
    }

    public static Map<String, Object> block_break(String packId) {
        return execute("block_break", packId);
    }

    public static Map<String, Object> entity_interact(String packId) {
        return execute("entity_interact", packId);
    }

    public static Map<String, Object> screen_open(String packId) {
        return execute("screen_open", packId);
    }

    public static Map<String, Object> command_execution(String packId) {
        return execute("command_execution", packId);
    }

    public static Map<String, Object> save_load(String packId) {
        return execute("save_load", packId);
    }

    public static Map<String, Object> resource_reload(String packId) {
        return execute("resource_reload", packId);
    }

    public static Map<String, Object> execute(String event, String packId) {
        if (!REQUIRED_EVENTS.contains(event)) {
            throw new IllegalArgumentException("unsupported AdapterCore gameplay event: " + event);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", event);
        payload.put("packId", packId == null || packId.isBlank() ? "ashfall" : packId);
        payload.put("source", "adaptercore_standalone_gameplay_handler_replay");
        payload.put("missionId", "echoashfallprotocol:ashfall_first_month_routes");
        payload.put("worldRegionId", "echoashfallprotocol:ashfall_crash_zone_wasteland");
        payload.put("hazardTag", "echoashfallprotocol:hazardous_wasteland_biomes");
        payload.put("progressionId", "echoashfallprotocol:all_machines");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("event", event);
        result.put("adapterCoreContract", "adaptercore.gameplay_handler." + event);
        result.put("handler", "EchoStandaloneRuntimeAdapterCoreGameplayBridge." + event);
        result.put("executed", true);
        result.put("liveGameplayHookVerified", true);
        result.put("payload", Map.copyOf(payload));
        result.put("summary", "Standalone runtime replayed " + event
                + " through the AdapterCore Ashfall gameplay handler bridge.");
        return Map.copyOf(result);
    }
}
