package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoHoloMapStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoHoloMapParitySmokeHarness {
    private EchoRuntimeEchoHoloMapParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeSnapshot = executeNativeReferenceSnapshot(
                EchoHoloMapStandaloneAdapter.REFERENCE_ROUTE_ID,
                EchoHoloMapStandaloneAdapter.REFERENCE_REGION_ID);
        EchoHoloMapStandaloneAdapter standaloneAdapter = new EchoHoloMapStandaloneAdapter();
        Map<String, Object> standaloneSnapshot = standaloneAdapter.executeSnapshot(
                EchoHoloMapStandaloneAdapter.REFERENCE_ROUTE_ID,
                EchoHoloMapStandaloneAdapter.REFERENCE_REGION_ID);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceSnapshotPassed(nativeSnapshot),
                "native HoloMap reference snapshot should resolve the field route");
        require(standaloneAdapter.referenceSnapshotPassed(standaloneSnapshot),
                "standalone HoloMap snapshot should resolve the field route");
        require(Boolean.TRUE.equals(standaloneActivation.get("routeSnapshotExecuted")),
                "standalone activation should execute route snapshot");
        require(nativeSnapshot.get("adapterCoreContract").equals(standaloneSnapshot.get("adapterCoreContract")),
                "native and standalone map contracts should match");
        require(nativeSnapshot.get("routeId").equals(standaloneSnapshot.get("routeId")),
                "native and standalone route ids should match");
        require(nativeSnapshot.get("regionId").equals(standaloneSnapshot.get("regionId")),
                "native and standalone region ids should match");
        require(nativeSnapshot.get("terrainTiles").equals(standaloneSnapshot.get("terrainTiles")),
                "native and standalone terrain tiles should match");
        require(nativeSnapshot.get("markers").equals(standaloneSnapshot.get("markers")),
                "native and standalone markers should match");
        require(nativeSnapshot.get("routePoints").equals(standaloneSnapshot.get("routePoints")),
                "native and standalone route points should match");

        System.out.println("echoholomap parity smoke PASS contract="
                + nativeSnapshot.get("adapterCoreContract")
                + " route="
                + nativeSnapshot.get("routeId")
                + " markers="
                + ((List<?>) nativeSnapshot.get("markers")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceSnapshot(String routeId, String regionId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("adapterCoreContract", EchoHoloMapStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        snapshot.put("service", "echoholomap:map_service");
        snapshot.put("snapshotExecuted", true);
        snapshot.put("routeId", routeId);
        snapshot.put("regionId", regionId);
        snapshot.put("center", Map.of("x", 128, "z", -64, "zoom", 3));
        snapshot.put("terrainTiles", List.of(
                tile("ash_waste", 7, 5, "safe"),
                tile("ruined_road", 8, 5, "route"),
                tile("radiation_pocket", 9, 5, "hazard")
        ));
        snapshot.put("markers", List.of(
                marker("mission", "echoterminal:field_ops/first_ten_minutes", 128, -64),
                marker("hazard", "echoashfallprotocol:ash_storm", 160, -48),
                marker("waypoint", "echoholomap:waypoint/field_cache", 192, -32)
        ));
        snapshot.put("routePoints", List.of(
                point(128, -64, "start"),
                point(160, -48, "avoid_hazard"),
                point(192, -32, "field_cache")
        ));
        snapshot.put("visibleLayerIds", List.of(
                "echoholomap:terrain",
                "echoholomap:mission_markers",
                "echoholomap:hazards",
                "echoholomap:waypoints"
        ));
        snapshot.put("referenceBehavior", "holomap_route_snapshot_guides_first_power_cell");
        return Map.copyOf(snapshot);
    }

    private static boolean nativeReferenceSnapshotPassed(Map<String, Object> snapshot) {
        return Boolean.TRUE.equals(snapshot.get("snapshotExecuted"))
                && EchoHoloMapStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(snapshot.get("adapterCoreContract"))
                && EchoHoloMapStandaloneAdapter.REFERENCE_ROUTE_ID.equals(snapshot.get("routeId"))
                && EchoHoloMapStandaloneAdapter.REFERENCE_REGION_ID.equals(snapshot.get("regionId"))
                && list(snapshot.get("visibleLayerIds")).contains("echoholomap:mission_markers")
                && String.valueOf(snapshot.get("markers")).contains("echoashfallprotocol:ash_storm")
                && String.valueOf(snapshot.get("routePoints")).contains("field_cache");
    }

    private static Map<String, Object> tile(String biome, int x, int z, String state) {
        Map<String, Object> tile = new LinkedHashMap<>();
        tile.put("biome", biome);
        tile.put("x", x);
        tile.put("z", z);
        tile.put("state", state);
        return Map.copyOf(tile);
    }

    private static Map<String, Object> marker(String kind, String id, int x, int z) {
        Map<String, Object> marker = new LinkedHashMap<>();
        marker.put("kind", kind);
        marker.put("id", id);
        marker.put("x", x);
        marker.put("z", z);
        return Map.copyOf(marker);
    }

    private static Map<String, Object> point(int x, int z, String label) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("x", x);
        point.put("z", z);
        point.put("label", label);
        return Map.copyOf(point);
    }

    private static List<String> list(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
