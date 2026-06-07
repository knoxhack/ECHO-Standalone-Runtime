package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5GeneratedScreenHostSmoke {
    private EchoAgent5GeneratedScreenHostSmoke() {
    }

    public static void main(String[] args) {
        Map<String, Object> report = capture();
        if (!Boolean.TRUE.equals(report.get("passed"))) {
            throw new AssertionError(String.valueOf(report));
        }
        System.out.println("agent5 generated screen host smoke PASS surfaces=11");
    }

    public static Map<String, Object> capture() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        EchoAgent5GeneratedScreenHost host = new EchoAgent5GeneratedScreenHost(source);
        List<String> checks = new ArrayList<>();
        List<String> renderedTitles = new ArrayList<>();
        List<String> renderedModes = new ArrayList<>();

        host.open("TERMINAL");
        checks.add(check("terminalInitialFocus", "terminal:input".equals(host.state("focusedControl"))));
        host.type(source.terminalCommand() + "x");
        host.key("BACKSPACE");
        checks.add(check("terminalBufferEdited", source.terminalCommand().equals(host.state("terminalBuffer"))));
        host.mouseClick();
        checks.add(check("terminalCommandExecuted", Boolean.TRUE.equals(host.state("terminalCommandExecuted"))));
        checks.add(check("terminalRenderedOutput", host.renderedText().contains(source.terminalReadyLine())));

        host.open("INDEX");
        host.type(source.indexQuery() + "x");
        host.key("BACKSPACE");
        checks.add(check("indexBufferEdited", source.indexQuery().equals(host.state("indexBuffer"))));
        host.mouseClick();
        checks.add(check("indexSearchExecuted", Boolean.TRUE.equals(host.state("indexSearchExecuted"))));
        checks.add(check("indexRenderedResult", host.renderedText().contains(source.indexResult())));

        host.open("LENS");
        host.key("ENTER");
        checks.add(check("lensScanExecuted", Boolean.TRUE.equals(host.state("lensScanExecuted"))));
        checks.add(check("lensRenderedTarget", host.renderedText().contains(source.lensTarget())));

        host.open("MISSION_LOG");
        host.key("ENTER");
        checks.add(check("missionLogUpdated", EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS.equals(
                host.state("missionStatus"))));
        checks.add(check("missionLogRenderedProgress", host.renderedText().contains("Progress: 50%")));

        host.open("SETTINGS");
        host.key("DOWN");
        host.key("DOWN");
        checks.add(check("settingsSelectedInputMode", "Input Mode".equals(host.state("selectedOption"))));
        host.key("DOWN");
        host.key("ENTER");
        checks.add(check("settingsHudScaleAdjusted", Double.valueOf(1.25D).equals(host.state("settingsHudScale"))));
        host.key("DOWN");
        host.key("ENTER");
        checks.add(check("settingsSubtitlesToggled", Boolean.FALSE.equals(host.state("settingsSubtitles"))));

        host.open("HOLOMAP");
        checks.add(check("holomapRendered", host.renderedText().contains(String.valueOf(source.holomapValues().get("marker")))));
        host.open("WIKI");
        checks.add(check("wikiRendered", host.renderedText().contains(String.valueOf(source.wikiValues().get("link")))));

        host.open("MAIN_MENU");
        host.key("DOWN");
        host.key("DOWN");
        host.key("ENTER");
        checks.add(check("mainMenuRoutesSettings", "SETTINGS".equals(host.mode())));

        host.open("TERMINAL");
        host.key("N");
        checks.add(check("notificationDismissed", host.renderedText()
                .contains("Notifications: " + EchoAgent5UiReference.ACTIVE_MISSION_TITLE)));
        host.open("RECOVERY");
        checks.add(check("recoverySurfaceRoute", "RECOVERY".equals(host.mode())));
        host.key("ENTER");
        checks.add(check("recoveryExecuted", Boolean.TRUE.equals(host.state("recoveryActionExecuted"))));
        checks.add(check("recoveryRenderedStatus", host.renderedText().contains(EchoAgent5UiReference.RECOVERY_STATUS)));

        host.open("HUD");
        host.key("ENTER");
        checks.add(check("hudUpdated", Integer.valueOf(93).equals(host.state("hudHealth"))));
        checks.add(check("hudRenderedWarning", host.renderedText()
                .contains(String.valueOf(source.hudValues().get("hazard")))));

        host.open("TERMINAL");
        host.key("ESCAPE");
        checks.add(check("escapeRoutesPause", "PAUSE".equals(host.mode())));
        host.key("ESCAPE");
        checks.add(check("pauseResumesPrevious", "TERMINAL".equals(host.mode())));

        for (String mode : expectedModes()) {
            Map<String, Object> render = host.open(mode);
            renderedModes.add(mode);
            renderedTitles.add(String.valueOf(render.get("screenTitle")));
            checks.add(check("rendered:" + mode, host.renderedText().contains("ECHO NATIVE // " + mode)
                    || renderedTitles.get(renderedTitles.size() - 1).contains("ECHO NATIVE // " + mode)));
        }

        boolean passed = checks.stream().allMatch(entry -> entry.endsWith("=PASS"))
                && renderedModes.containsAll(expectedModes())
                && renderedTitles.stream().anyMatch(title -> title.contains("ECHO NATIVE // HUD"))
                && renderedTitles.stream().anyMatch(title -> title.contains("ECHO NATIVE // TERMINAL"));

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("generatedScreenHostSmokeClass", EchoAgent5GeneratedScreenHostSmoke.class.getSimpleName());
        report.put("generatedHostClass", EchoAgent5GeneratedScreenHost.class.getSimpleName());
        report.put("nativeReferenceClass", "dev.echo.nativeplatform.generated.EchoNativeDashboardScreen");
        report.put("standaloneReferenceClass", "dev.echo.standalone.runtime.ui.EchoAgent5GeneratedScreenHost");
        report.put("adapterCoreContract", "echo-standalone-runtime:agent5_generated_screen_host_parity");
        report.put("renderedModes", List.copyOf(renderedModes));
        report.put("renderedTitles", List.copyOf(renderedTitles));
        report.put("checks", List.copyOf(checks));
        report.put("adapterCoreBridge", true);
        report.put("serviceCodeExecuted", passed);
        report.put("passed", passed);
        report.put("effect", passed
                ? "generated_screen_host:accepted:11-surfaces"
                : "generated_screen_host:rejected");
        return Map.copyOf(report);
    }

    private static String check(String name, boolean passed) {
        return name + "=" + (passed ? "PASS" : "FAIL");
    }

    private static List<String> expectedModes() {
        return List.of(
                "TERMINAL",
                "INDEX",
                "LENS",
                "MISSION_LOG",
                "SETTINGS",
                "PAUSE",
                "RECOVERY",
                "HOLOMAP",
                "WIKI",
                "MAIN_MENU",
                "HUD"
        );
    }

    private static final class EchoAgent5GeneratedScreenHost {
        private final EchoAgent5UiDataSources source;
        private final Map<String, Object> state = new LinkedHashMap<>();
        private String mode = "TERMINAL";
        private String previousMode = EchoAgent5UiReference.WIKI_SCREEN;
        private List<Map<String, Object>> notifications;

        private EchoAgent5GeneratedScreenHost(EchoAgent5UiDataSources source) {
            this.source = source == null ? EchoAgent5UiDataSources.reference() : source;
            this.notifications = new ArrayList<>(this.source.notifications());
        }

        private Map<String, Object> open(String nextMode) {
            previousMode = mode;
            mode = normalizeMode(nextMode);
            state.clear();
            state.put("previousMode", previousMode);
            state.put("notifications", List.copyOf(notifications));
            state.putAll(EchoAgent5UiActionRouter.routeInitialFocus(mode, previousMode));
            seedModeState();
            return render();
        }

        private void type(String value) {
            for (int index = 0; index < value.length(); index++) {
                Map<String, Object> typed = EchoAgent5UiActionRouter.routeCharacter(
                        mode,
                        String.valueOf(state.getOrDefault("focusedControl", "")),
                        String.valueOf(state.getOrDefault("terminalBuffer", "")),
                        String.valueOf(state.getOrDefault("indexBuffer", "")),
                        value.charAt(index)
                );
                merge(typed);
            }
        }

        private void key(String keyName) {
            String normalizedKey = normalizeKey(keyName);
            if ("BACKSPACE".equals(normalizedKey)) {
                merge(EchoAgent5UiActionRouter.routeEditKey(
                        normalizedKey,
                        mode,
                        String.valueOf(state.getOrDefault("focusedControl", "")),
                        String.valueOf(state.getOrDefault("terminalBuffer", "")),
                        String.valueOf(state.getOrDefault("indexBuffer", ""))
                ));
                return;
            }
            if ("ENTER".equals(normalizedKey)) {
                handleEnter();
                return;
            }
            if ("UP".equals(normalizedKey) || "DOWN".equals(normalizedKey)) {
                Map<String, Object> nav = EchoAgent5UiActionRouter.routeListNavigation(
                        normalizedKey,
                        mode,
                        integer(state.get("selectedIndex"), 0),
                        source
                );
                merge(nav);
                return;
            }
            if ("N".equals(normalizedKey)) {
                Map<String, Object> dismiss = EchoAgent5UiActionRouter.routeNotificationDismiss(notifications, source);
                merge(dismiss);
                notifications = maps(dismiss.get("remainingNotifications"));
                state.put("notifications", List.copyOf(notifications));
                return;
            }
            Map<String, Object> route = EchoAgent5UiActionRouter.routeKey(normalizedKey, mode, previousMode);
            if (Boolean.TRUE.equals(route.get("handled"))) {
                previousMode = String.valueOf(route.get("destinationPreviousMode"));
                mode = modeFromDestination(String.valueOf(route.get("destinationMode")));
                state.clear();
                state.put("previousMode", previousMode);
                state.put("notifications", List.copyOf(notifications));
                state.putAll(EchoAgent5UiActionRouter.routeInitialFocus(mode, previousMode));
                seedModeState();
            }
        }

        private void mouseClick() {
            merge(EchoAgent5UiActionRouter.routeMouseClick(mode, previousMode, state, source));
        }

        private void handleEnter() {
            switch (mode) {
                case "TERMINAL", "INDEX", "LENS", "RECOVERY" -> merge(EchoAgent5UiActionRouter.activate(mode, state, source));
                case "MISSION_LOG" -> merge(EchoAgent5UiActionRouter.routeMissionLogUpdate(state, source));
                case "SETTINGS" -> merge(EchoAgent5UiActionRouter.routeSettingsAdjustment(
                        String.valueOf(state.getOrDefault("selectedOption", "")),
                        doubleValue(state.get("settingsHudScale"), 1.0D),
                        booleanValue(state.get("settingsSubtitles"), true)
                ));
                case "PAUSE" -> routeFrom(EchoAgent5UiActionRouter.routePauseOption(
                        String.valueOf(state.getOrDefault("selectedOption", "Resume")),
                        previousMode
                ));
                case "MAIN_MENU" -> routeFrom(EchoAgent5UiActionRouter.routeMainMenuOption(
                        String.valueOf(state.getOrDefault("selectedOption", "Continue"))
                ));
                case "HUD" -> merge(EchoAgent5UiActionRouter.routeHudUpdate(state, source));
                default -> {
                }
            }
        }

        private void routeFrom(Map<String, Object> route) {
            if (!Boolean.TRUE.equals(route.get("handled"))) {
                merge(route);
                return;
            }
            previousMode = String.valueOf(route.get("destinationPreviousMode"));
            mode = modeFromDestination(String.valueOf(route.get("destinationMode")));
            state.clear();
            state.putAll(route);
            state.put("previousMode", previousMode);
            state.put("notifications", List.copyOf(notifications));
            state.putAll(EchoAgent5UiActionRouter.routeInitialFocus(mode, previousMode));
            seedModeState();
        }

        private Map<String, Object> render() {
            return EchoAgent5UiScreenHostModel.render(
                    mode,
                    state,
                    "ashfall",
                    12,
                    3,
                    2,
                    1,
                    source
            );
        }

        private String renderedText() {
            Map<String, Object> model = render();
            List<String> lines = new ArrayList<>();
            lines.add(String.valueOf(model.get("screenTitle")));
            lines.addAll(strings(model.get("headerLines")));
            lines.addAll(strings(model.get("surfaceLines")));
            lines.add(String.valueOf(model.get("footerLine")));
            return String.join("\n", lines);
        }

        private Object state(String key) {
            return state.get(key);
        }

        private String mode() {
            return mode;
        }

        private void seedModeState() {
            switch (mode) {
                case "MISSION_LOG" -> {
                    Map<String, Object> mission = source.missionLogValues();
                    state.put("missionStatus", mission.get("status"));
                    state.put("missionProgress", mission.get("progress"));
                }
                case "SETTINGS" -> {
                    Map<String, Object> settings = source.settingsValues();
                    state.put("settingsHudScale", settings.get("hudScale"));
                    state.put("settingsSubtitles", settings.get("subtitles"));
                    state.put("selectedIndex", 0);
                    state.put("selectedOption", "Profile");
                }
                case "PAUSE" -> {
                    state.put("selectedIndex", 0);
                    state.put("selectedOption", "Resume");
                }
                case "MAIN_MENU" -> {
                    state.put("selectedIndex", 0);
                    state.put("selectedOption", "Continue");
                }
                case "HUD" -> {
                    Map<String, Object> hud = source.hudValues();
                    state.put("hudHealth", hud.get("health"));
                    state.put("hudHazard", hud.get("hazard"));
                    state.put("hudMission", hud.get("mission"));
                }
                default -> {
                }
            }
        }

        private void merge(Map<String, Object> values) {
            if (values == null) {
                return;
            }
            Object targetBuffer = values.get("targetBuffer");
            if ("terminalBuffer".equals(targetBuffer) || "indexBuffer".equals(targetBuffer)) {
                state.put(String.valueOf(targetBuffer), values.get("value"));
            }
            Object outputKey = values.get("outputKey");
            if (outputKey instanceof String key && !key.isBlank()) {
                state.put(key, values.get("output"));
            }
            Object executedKey = values.get("executedKey");
            if (executedKey instanceof String key && !key.isBlank()) {
                state.put(key, true);
            }
            state.putAll(values);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(entry -> (Map<String, Object>) entry)
                    .map(Map::copyOf)
                    .toList();
        }
        return List.of();
    }

    private static int integer(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return fallback;
    }

    private static double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return fallback;
    }

    private static boolean booleanValue(Object value, boolean fallback) {
        return value instanceof Boolean bool ? bool : fallback;
    }

    private static String normalizeMode(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        return normalized.isBlank() ? "TERMINAL" : normalized;
    }

    private static String modeFromDestination(String value) {
        return switch (value) {
            case EchoAgent5UiReference.TERMINAL_SCREEN -> "TERMINAL";
            case EchoAgent5UiReference.INDEX_SCREEN -> "INDEX";
            case EchoAgent5UiReference.LENS_SCREEN -> "LENS";
            case EchoAgent5UiReference.MISSION_LOG_SCREEN -> "MISSION_LOG";
            case EchoAgent5UiReference.SETTINGS_SCREEN -> "SETTINGS";
            case EchoAgent5UiReference.PAUSE_FLOW_SCREEN -> "PAUSE";
            case EchoAgent5UiReference.DEATH_RECOVERY_SCREEN -> "RECOVERY";
            case EchoAgent5UiReference.HOLOMAP_SCREEN -> "HOLOMAP";
            case EchoAgent5UiReference.WIKI_SCREEN -> "WIKI";
            case EchoAgent5UiReference.MAIN_MENU_SCREEN -> "MAIN_MENU";
            case EchoAgent5UiReference.HUD_LAYER -> "HUD";
            default -> normalizeMode(value);
        };
    }

    private static String normalizeKey(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
        return "ESC".equals(normalized) ? "ESCAPE" : normalized;
    }
}
