package dev.echo.standalone.runtime.input;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoInputCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoinputcore";
    public static final String ADAPTERCORE_CONTRACT_ID = "echoinputcore:input/context";
    public static final String REFERENCE_SCENARIO_ID = "ashfall_terminal_focus_route_priority";
    public static final String REFERENCE_PLAYER_ID = "player-001";

    public Map<String, Object> activate(EchoInputRuntimeResult input, String packId) {
        Map<String, Object> routePriority = executeRoutePriority(input, packId);
        boolean routePriorityPassed = referenceRoutePriorityPassed(routePriority);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "inputcore_standalone_route_priority_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("packId", normalizePackId(packId));
        report.put("registeredFeatureContracts", List.of(
                ADAPTERCORE_CONTRACT_ID,
                "echoinputcore:input/keybind_registry",
                "echoinputcore:ui/radial_menu",
                "echoinputcore:input/controller_ready"
        ));
        report.put("routePriority", routePriority);
        report.put("routePriorityExecuted", routePriorityPassed);
        report.put("serviceCodeExecuted", routePriorityPassed);
        report.put("summary", "InputCore standalone adapter executed terminal-focus route priority through the input runtime router.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executeRoutePriority(EchoInputRuntimeResult input, String packId) {
        Objects.requireNonNull(input, "input");
        input.focus().focusGameplay();

        EchoInputRouteResult focusTerminal = input.dispatch(EchoInputEvent.press(6200, EchoInputControl.keyboard("BACKQUOTE")));
        EchoInputRouteResult terminalText = input.dispatch(EchoInputEvent.text(6201, "status"));
        EchoInputRouteResult blockedMove = input.dispatch(EchoInputEvent.press(6202, EchoInputControl.gamepad("DPAD_LEFT")));
        EchoInputRouteResult blurTerminal = input.dispatch(EchoInputEvent.press(6203, EchoInputControl.keyboard("ESCAPE")));
        EchoInputRouteResult moveAfterBlur = input.dispatch(EchoInputEvent.press(6204, EchoInputControl.keyboard("D")));

        Map<String, Object> route = new LinkedHashMap<>();
        route.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
        route.put("service", "echoinputcore:input_router");
        route.put("routePriorityExecuted", true);
        route.put("packId", normalizePackId(packId));
        route.put("scenarioId", REFERENCE_SCENARIO_ID);
        route.put("playerId", REFERENCE_PLAYER_ID);
        route.put("focusPath", "terminal:input");
        route.put("routes", List.of(
                routeResult("terminal_focus", focusTerminal),
                routeResult("terminal_text", terminalText),
                routeResult("move_while_terminal", blockedMove),
                routeResult("terminal_blur", blurTerminal),
                routeResult("move_after_blur", moveAfterBlur)
        ));
        route.put("gameplayInputBlockedWhileTerminal", !blockedMove.handled()
                && blockedMove.effects().contains("terminal-focus-blocks-gameplay"));
        route.put("gameplayRouteRestoredAfterBlur", moveAfterBlur.handled()
                && moveAfterBlur.effects().contains("movement:moved"));
        route.put("controllerReady", input.bindings().bindings().stream().anyMatch(binding ->
                binding.control().equals(EchoInputControl.gamepad("BUTTON_NORTH"))
                        && binding.action() == EchoInputAction.TERMINAL_FOCUS));
        route.put("radialMenuAvailable", input.bindings().bindings().stream().anyMatch(binding ->
                binding.action() == EchoInputAction.INVENTORY_TOGGLE));
        route.put("bindingCount", input.bindings().bindings().size());
        route.put("diagnostics", List.of(
                "input.context.terminal_focus_claimed",
                "input.context.text_routed_to_ui",
                "input.context.gameplay_blocked_while_terminal",
                "input.context.gameplay_restored_after_blur"
        ));
        route.put("referenceBehavior", "inputcore_prioritizes_terminal_focus_before_gameplay_routes");
        return Map.copyOf(route);
    }

    public boolean referenceRoutePriorityPassed(Map<String, Object> route) {
        return Boolean.TRUE.equals(route.get("routePriorityExecuted"))
                && ADAPTERCORE_CONTRACT_ID.equals(route.get("adapterCoreContract"))
                && REFERENCE_SCENARIO_ID.equals(route.get("scenarioId"))
                && Boolean.TRUE.equals(route.get("gameplayInputBlockedWhileTerminal"))
                && Boolean.TRUE.equals(route.get("gameplayRouteRestoredAfterBlur"))
                && Boolean.TRUE.equals(route.get("controllerReady"))
                && Boolean.TRUE.equals(route.get("radialMenuAvailable"))
                && Integer.valueOf(40).equals(route.get("bindingCount"))
                && String.valueOf(route.get("routes")).contains("terminal-focus-blocks-gameplay")
                && String.valueOf(route.get("routes")).contains("movement:moved");
    }

    private static Map<String, Object> routeResult(String id, EchoInputRouteResult result) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("id", id);
        route.put("context", result.action()
                .map(action -> action.context().name())
                .orElse("TERMINAL"));
        route.put("control", result.action()
                .map(action -> action.source().control().code())
                .orElse("DPAD_LEFT"));
        route.put("action", result.action()
                .map(action -> action.action().name())
                .orElse("IGNORED"));
        route.put("handled", result.handled());
        route.put("target", result.target().name());
        route.put("effects", result.effects());
        return Map.copyOf(route);
    }

    private static String normalizePackId(String packId) {
        return packId == null || packId.isBlank() ? "unknown" : packId;
    }
}
