package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoHudCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoHudCoreParitySmokeHarness {
    private EchoRuntimeEchoHudCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeSnapshot = executeNativeReferenceSnapshot(
                EchoHudCoreStandaloneAdapter.REFERENCE_MISSION_ID,
                EchoHudCoreStandaloneAdapter.REFERENCE_HAZARD_ID);
        EchoHudCoreStandaloneAdapter standaloneAdapter = new EchoHudCoreStandaloneAdapter();
        Map<String, Object> standaloneSnapshot = standaloneAdapter.executeSnapshot(
                EchoHudCoreStandaloneAdapter.REFERENCE_MISSION_ID,
                EchoHudCoreStandaloneAdapter.REFERENCE_HAZARD_ID);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceSnapshotPassed(nativeSnapshot),
                "native HUD reference snapshot should render mission and hazard state");
        require(standaloneAdapter.referenceSnapshotPassed(standaloneSnapshot),
                "standalone HUD snapshot should render mission and hazard state");
        require(Boolean.TRUE.equals(standaloneActivation.get("hudSnapshotExecuted")),
                "standalone activation should execute HUD snapshot");
        require(nativeSnapshot.get("adapterCoreContract").equals(standaloneSnapshot.get("adapterCoreContract")),
                "native and standalone HUD contracts should match");
        require(nativeSnapshot.get("missionId").equals(standaloneSnapshot.get("missionId")),
                "native and standalone mission ids should match");
        require(nativeSnapshot.get("hazardId").equals(standaloneSnapshot.get("hazardId")),
                "native and standalone hazard ids should match");
        require(nativeSnapshot.get("screenSafeArea").equals(standaloneSnapshot.get("screenSafeArea")),
                "native and standalone safe areas should match");
        require(nativeSnapshot.get("widgets").equals(standaloneSnapshot.get("widgets")),
                "native and standalone widgets should match");

        System.out.println("echohudcore parity smoke PASS contract="
                + nativeSnapshot.get("adapterCoreContract")
                + " widgets="
                + ((List<?>) nativeSnapshot.get("widgets")).size()
                + " mission="
                + nativeSnapshot.get("missionId"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceSnapshot(String missionId, String hazardId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("adapterCoreContract", EchoHudCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        snapshot.put("service", "echohudcore:hud_service");
        snapshot.put("snapshotExecuted", true);
        snapshot.put("missionId", missionId);
        snapshot.put("hazardId", hazardId);
        snapshot.put("screenSafeArea", Map.of(
                "left", 12,
                "top", 10,
                "right", 12,
                "bottom", 18
        ));
        snapshot.put("widgets", List.of(
                widget("echohudcore:mission_tracker", "top_left", List.of(
                        row("mission", missionId),
                        row("objective", "Craft a power cell"),
                        row("state", "active")
                )),
                widget("echohudcore:hazard_readout", "top_right", List.of(
                        row("hazard", hazardId),
                        row("severity", "warning"),
                        row("countermeasure", "seek_shelter")
                )),
                widget("echohudcore:compass_indicator", "bottom_center", List.of(
                        row("target", "echoterminal:field_ops/first_ten_minutes"),
                        row("distance", "128m"),
                        row("bearing", "NE")
                ))
        ));
        snapshot.put("visibleWidgetIds", List.of(
                "echohudcore:mission_tracker",
                "echohudcore:hazard_readout",
                "echohudcore:compass_indicator"
        ));
        snapshot.put("referenceBehavior", "hud_snapshot_tracks_mission_hazard_and_safe_area");
        return Map.copyOf(snapshot);
    }

    private static boolean nativeReferenceSnapshotPassed(Map<String, Object> snapshot) {
        return Boolean.TRUE.equals(snapshot.get("snapshotExecuted"))
                && EchoHudCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(snapshot.get("adapterCoreContract"))
                && EchoHudCoreStandaloneAdapter.REFERENCE_MISSION_ID.equals(snapshot.get("missionId"))
                && EchoHudCoreStandaloneAdapter.REFERENCE_HAZARD_ID.equals(snapshot.get("hazardId"))
                && list(snapshot.get("visibleWidgetIds")).contains("echohudcore:mission_tracker")
                && list(snapshot.get("visibleWidgetIds")).contains("echohudcore:hazard_readout")
                && String.valueOf(snapshot.get("widgets")).contains("seek_shelter");
    }

    private static Map<String, Object> widget(
            String id,
            String anchor,
            List<Map<String, String>> rows
    ) {
        Map<String, Object> widget = new LinkedHashMap<>();
        widget.put("id", id);
        widget.put("anchor", anchor);
        widget.put("rows", List.copyOf(rows));
        return Map.copyOf(widget);
    }

    private static Map<String, String> row(String key, String value) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("key", key);
        row.put("value", value);
        return Map.copyOf(row);
    }

    private static List<String> list(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
