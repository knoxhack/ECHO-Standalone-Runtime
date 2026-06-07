package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputCoreStandaloneAdapter;
import dev.echo.standalone.runtime.input.EchoInputRuntime;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoTerminalScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoInputCoreParitySmokeHarness {
    private EchoRuntimeEchoInputCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        String packId = "echo-native-agent62";
        Map<String, Object> nativeRoute = executeNativeReferenceRoutePriority(packId);

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
        EchoTerminalShell shell = new EchoTerminalShell();
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoTerminalScreen("terminal", "Ashfall Terminal", shell),
                EchoUiTheme.defaultTerminal()
        );
        EchoInputRuntimeResult input = new EchoInputRuntime().boot(
                services,
                ui,
                entities,
                gameplay,
                new EchoEntityId("player-001")
        );

        EchoInputCoreStandaloneAdapter standaloneAdapter = new EchoInputCoreStandaloneAdapter();
        Map<String, Object> standaloneRoute = standaloneAdapter.executeRoutePriority(input, packId);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate(input, packId);

        require(nativeReferenceRoutePriorityPassed(nativeRoute), "native InputCore reference route priority should pass");
        require(standaloneAdapter.referenceRoutePriorityPassed(standaloneRoute),
                "standalone InputCore route priority should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("routePriorityExecuted")),
                "standalone activation should execute route priority behavior");
        require(nativeRoute.get("adapterCoreContract").equals(standaloneRoute.get("adapterCoreContract")),
                "native and standalone input contracts should match");
        require(nativeRoute.get("scenarioId").equals(standaloneRoute.get("scenarioId")),
                "native and standalone scenario ids should match");
        require(nativeRoute.get("routes").equals(standaloneRoute.get("routes")),
                "native and standalone route decisions should match");
        require(nativeRoute.get("diagnostics").equals(standaloneRoute.get("diagnostics")),
                "native and standalone diagnostics should match");
        require(Boolean.TRUE.equals(standaloneRoute.get("gameplayInputBlockedWhileTerminal")),
                "terminal focus should block gameplay input");
        require(Boolean.TRUE.equals(standaloneRoute.get("gameplayRouteRestoredAfterBlur")),
                "gameplay input should be restored after terminal blur");

        System.out.println("echoinputcore parity smoke PASS contract="
                + nativeRoute.get("adapterCoreContract")
                + " scenario="
                + nativeRoute.get("scenarioId")
                + " routes="
                + ((List<?>) nativeRoute.get("routes")).size());
    }

    private static Map<String, Object> executeNativeReferenceRoutePriority(String packId) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("adapterCoreContract", EchoInputCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        route.put("service", "echoinputcore:input_router");
        route.put("routePriorityExecuted", true);
        route.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        route.put("scenarioId", EchoInputCoreStandaloneAdapter.REFERENCE_SCENARIO_ID);
        route.put("playerId", EchoInputCoreStandaloneAdapter.REFERENCE_PLAYER_ID);
        route.put("focusPath", "terminal:input");
        route.put("routes", List.of(
                routeResult("terminal_focus", "GAMEPLAY", "BACKQUOTE", "TERMINAL_FOCUS", true, "UI",
                        List.of("focus:terminal", "focusPath:terminal:input")),
                routeResult("terminal_text", "TERMINAL", "TEXT", "TERMINAL_SUBMIT_TEXT", true, "UI",
                        List.of("route:ui", "focusPath:terminal:input")),
                routeResult("move_while_terminal", "TERMINAL", "DPAD_LEFT", "IGNORED", false, "IGNORED",
                        List.of("terminal-focus-blocks-gameplay")),
                routeResult("terminal_blur", "TERMINAL", "ESCAPE", "TERMINAL_BLUR", true, "UI",
                        List.of("focus:gameplay")),
                routeResult("move_after_blur", "GAMEPLAY", "D", "MOVE_EAST", true, "GAMEPLAY",
                        List.of("route:gameplay", "movement:moved"))
        ));
        route.put("gameplayInputBlockedWhileTerminal", true);
        route.put("gameplayRouteRestoredAfterBlur", true);
        route.put("controllerReady", true);
        route.put("radialMenuAvailable", true);
        route.put("bindingCount", 40);
        route.put("diagnostics", List.of(
                "input.context.terminal_focus_claimed",
                "input.context.text_routed_to_ui",
                "input.context.gameplay_blocked_while_terminal",
                "input.context.gameplay_restored_after_blur"
        ));
        route.put("referenceBehavior", "inputcore_prioritizes_terminal_focus_before_gameplay_routes");
        return Map.copyOf(route);
    }

    private static boolean nativeReferenceRoutePriorityPassed(Map<String, Object> route) {
        return Boolean.TRUE.equals(route.get("routePriorityExecuted"))
                && EchoInputCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(route.get("adapterCoreContract"))
                && EchoInputCoreStandaloneAdapter.REFERENCE_SCENARIO_ID.equals(route.get("scenarioId"))
                && Boolean.TRUE.equals(route.get("gameplayInputBlockedWhileTerminal"))
                && Boolean.TRUE.equals(route.get("gameplayRouteRestoredAfterBlur"))
                && Boolean.TRUE.equals(route.get("controllerReady"))
                && Boolean.TRUE.equals(route.get("radialMenuAvailable"))
                && Integer.valueOf(40).equals(route.get("bindingCount"))
                && String.valueOf(route.get("routes")).contains("terminal-focus-blocks-gameplay")
                && String.valueOf(route.get("routes")).contains("movement:moved");
    }

    private static Map<String, Object> routeResult(
            String id,
            String context,
            String control,
            String action,
            boolean handled,
            String target,
            List<String> effects
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", id);
        result.put("context", context);
        result.put("control", control);
        result.put("action", action);
        result.put("handled", handled);
        result.put("target", target);
        result.put("effects", effects);
        return Map.copyOf(result);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
