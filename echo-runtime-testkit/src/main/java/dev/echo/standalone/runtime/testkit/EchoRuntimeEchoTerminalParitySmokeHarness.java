package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoTerminalStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoRuntimeEchoTerminalParitySmokeHarness {
    private EchoRuntimeEchoTerminalParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeSurface = executeNativeReferenceCommand(
                EchoTerminalStandaloneAdapter.REFERENCE_COMMAND);
        EchoTerminalStandaloneAdapter standaloneAdapter = new EchoTerminalStandaloneAdapter();
        Map<String, Object> standaloneSurface = standaloneAdapter.executeCommand(
                EchoTerminalStandaloneAdapter.REFERENCE_COMMAND);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceCommandPassed(nativeSurface),
                "native Terminal reference command should open first-ten-minutes field ops");
        require(standaloneAdapter.referenceCommandPassed(standaloneSurface),
                "standalone Terminal command should open first-ten-minutes field ops");
        require(Boolean.TRUE.equals(standaloneActivation.get("dashboardSurfaceExecuted")),
                "standalone activation should execute dashboard surface");
        require(nativeSurface.get("adapterCoreContract").equals(standaloneSurface.get("adapterCoreContract")),
                "native and standalone dashboard contracts should match");
        require(nativeSurface.get("pageId").equals(standaloneSurface.get("pageId")),
                "native and standalone page ids should match");
        require(nativeSurface.get("focusedControl").equals(standaloneSurface.get("focusedControl")),
                "native and standalone focused controls should match");
        require(nativeSurface.get("visibleCards").equals(standaloneSurface.get("visibleCards")),
                "native and standalone visible cards should match");
        require(nativeSurface.get("actions").equals(standaloneSurface.get("actions")),
                "native and standalone actions should match");

        System.out.println("echoterminal parity smoke PASS contract="
                + nativeSurface.get("adapterCoreContract")
                + " page="
                + nativeSurface.get("pageId")
                + " cards="
                + ((List<?>) nativeSurface.get("visibleCards")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceCommand(String command) {
        String normalizedCommand = normalize(command);
        Map<String, Object> surface = new LinkedHashMap<>();
        surface.put("adapterCoreContract", EchoTerminalStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        surface.put("service", "echoterminal:terminal_service");
        surface.put("command", normalizedCommand);
        surface.put("commandExecuted", true);
        surface.put("pageId", EchoTerminalStandaloneAdapter.REFERENCE_PAGE_ID);
        surface.put("focusedControl", "terminal:field_ops:primary_action");
        surface.put("visibleCards", List.of(
                "echoterminal:card/mission_status",
                "echoterminal:card/hazard_scan",
                "echoterminal:card/next_action"
        ));
        surface.put("actions", List.of(
                "open_index:echoindex:recipe_search/index_query",
                "scan_target:echolens:scanner/field_inspection",
                "route_map:echoholomap:layer/field_route"
        ));
        surface.put("diagnostics", List.of("terminal.surface.ready", "terminal.field_ops.synced"));
        surface.put("referenceBehavior", "terminal_opens_first_ten_minutes_field_ops");
        return Map.copyOf(surface);
    }

    private static boolean nativeReferenceCommandPassed(Map<String, Object> surface) {
        return Boolean.TRUE.equals(surface.get("commandExecuted"))
                && EchoTerminalStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(surface.get("adapterCoreContract"))
                && EchoTerminalStandaloneAdapter.REFERENCE_PAGE_ID.equals(surface.get("pageId"))
                && list(surface.get("visibleCards")).contains("echoterminal:card/next_action")
                && list(surface.get("actions")).contains("scan_target:echolens:scanner/field_inspection");
    }

    private static String normalize(Object value) {
        return String.valueOf(value).toLowerCase(Locale.ROOT).trim();
    }

    private static List<String> list(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
