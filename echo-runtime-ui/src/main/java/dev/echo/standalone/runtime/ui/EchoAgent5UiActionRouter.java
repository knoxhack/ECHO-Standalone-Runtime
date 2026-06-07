package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5UiActionRouter {
    private EchoAgent5UiActionRouter() {
    }

    public static String focusPath(String mode, String previousMode) {
        String normalizedMode = normalizeMode(mode);
        return switch (normalizedMode) {
            case "TERMINAL" -> "terminal:input";
            case "INDEX" -> "index:search";
            case "LENS" -> "lens:scan";
            case "RECOVERY" -> "recovery:recover";
            case "PAUSE" -> "pause:resume:" + fallbackPreviousMode(previousMode);
            default -> normalizedMode.toLowerCase(java.util.Locale.ROOT) + ":surface";
        };
    }

    public static Map<String, Object> routeInitialFocus(String mode, String previousMode) {
        String normalizedMode = normalizeMode(mode);
        String focusedControl = focusPath(normalizedMode, previousMode);
        return handled(Map.of(
                "focusedControl", focusedControl,
                "initialFocusRouted", true,
                "effect", "focus:initial:" + normalizedMode.toLowerCase(java.util.Locale.ROOT)
        ));
    }

    public static Map<String, Object> routeCharacter(
            String mode,
            String focusedControl,
            String terminalBuffer,
            String indexBuffer,
            char codePoint
    ) {
        if (codePoint < 32 || codePoint == 127) {
            return ignored("character:control");
        }
        String normalizedMode = normalizeMode(mode);
        if ("TERMINAL".equals(normalizedMode) && "terminal:input".equals(focusedControl)) {
            return handled(Map.of(
                    "targetBuffer", "terminalBuffer",
                    "value", (normalize(terminalBuffer) + codePoint).strip(),
                    "effect", "terminal-character"
            ));
        }
        if ("INDEX".equals(normalizedMode) && "index:search".equals(focusedControl)) {
            return handled(Map.of(
                    "targetBuffer", "indexBuffer",
                    "value", (normalize(indexBuffer) + codePoint).strip(),
                    "effect", "index-character"
            ));
        }
        return ignored("character:unfocused");
    }

    public static Map<String, Object> routeKey(String keyName, String mode, String previousMode) {
        String normalizedKey = normalize(keyName).toUpperCase(java.util.Locale.ROOT);
        String normalizedMode = normalizeMode(mode);
        return switch (normalizedKey) {
            case "ESCAPE" -> handled(Map.of(
                    "destinationMode", "PAUSE".equals(normalizedMode) ? fallbackPreviousMode(previousMode) : "PAUSE",
                    "destinationPreviousMode", "PAUSE".equals(normalizedMode) ? EchoAgent5UiReference.WIKI_SCREEN : normalizedMode,
                    "effect", "route:escape"
            ));
            case "M" -> route("TERMINAL");
            case "G", "R", "U" -> route("INDEX");
            case "B" -> "INDEX".equals(normalizedMode)
                    ? route("INDEX")
                    : handled(Map.of(
                            "destinationMode", "ASHFALL_DRONE",
                            "destinationPreviousMode", previousMode == null ? EchoAgent5UiReference.WIKI_SCREEN : previousMode,
                            "ashfallDroneKey", normalizedKey,
                            "effect", "route:ashfall_drone:" + normalizedKey
                    ));
            case "LEFT_ALT" -> route("LENS");
            case "J", "K", "RIGHT_BRACKET", "LEFT_BRACKET", "BACKSLASH" -> route("HOLOMAP");
            case "N" -> handled(Map.of(
                    "destinationMode", "SIGNALOS",
                    "destinationPreviousMode", normalizedMode,
                    "signalOsTerminalActive", true,
                    "effect", "route:signalos"
            ));
            case "X", "C", "Y", "Z" -> handled(Map.of(
                    "destinationMode", "ASHFALL_DRONE",
                    "destinationPreviousMode", previousMode == null ? EchoAgent5UiReference.WIKI_SCREEN : previousMode,
                    "ashfallDroneKey", normalizedKey,
                    "effect", "route:ashfall_drone:" + normalizedKey
            ));
            default -> ignored("route:unmapped:" + normalizedKey);
        };
    }

    public static Map<String, Object> routeEditKey(
            String keyName,
            String mode,
            String focusedControl,
            String terminalBuffer,
            String indexBuffer
    ) {
        String normalizedKey = normalize(keyName).toUpperCase(java.util.Locale.ROOT);
        String normalizedMode = normalizeMode(mode);
        if (!"BACKSPACE".equals(normalizedKey)) {
            return ignored("edit:unmapped:" + normalizedKey);
        }
        if ("TERMINAL".equals(normalizedMode) && "terminal:input".equals(focusedControl)) {
            return handled(Map.of(
                    "targetBuffer", "terminalBuffer",
                    "value", removeLast(terminalBuffer),
                    "effect", "terminal-backspace"
            ));
        }
        if ("INDEX".equals(normalizedMode) && "index:search".equals(focusedControl)) {
            return handled(Map.of(
                    "targetBuffer", "indexBuffer",
                    "value", removeLast(indexBuffer),
                    "effect", "index-backspace"
            ));
        }
        return ignored("edit:unfocused");
    }

    public static Map<String, Object> routeMouseClick(
            String mode,
            String previousMode,
            Map<String, Object> state,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        String normalizedMode = normalizeMode(mode);
        String focusedControl = focusPath(normalizedMode, previousMode);
        Map<String, Object> clickState = new LinkedHashMap<>();
        if (state != null) {
            clickState.putAll(state);
        }
        clickState.put("focusedControl", focusedControl);
        clickState.put("mouseRouted", true);

        Map<String, Object> action = activate(normalizedMode, clickState, source);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("focusedControl", focusedControl);
        result.put("mouseRouted", true);
        result.put("effect", "mouse:focus:" + normalizedMode.toLowerCase(java.util.Locale.ROOT));
        if (Boolean.TRUE.equals(action.get("handled"))) {
            result.put("outputKey", action.get("outputKey"));
            result.put("output", action.get("output"));
            result.put("executedKey", action.get("executedKey"));
            result.put("effects", action.get("effects"));
            result.put("effect", "mouse:activate:" + normalizedMode.toLowerCase(java.util.Locale.ROOT));
        }
        return handled(result);
    }

    public static Map<String, Object> routeListNavigation(
            String keyName,
            String mode,
            int selectedIndex,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        String normalizedKey = normalize(keyName).toUpperCase(java.util.Locale.ROOT);
        String normalizedMode = normalizeMode(mode);
        List<String> options = listOptions(normalizedMode, source);
        if (options.isEmpty()) {
            return ignored("list:unsupported:" + normalizedMode);
        }
        int current = clampIndex(selectedIndex, options.size());
        int next = switch (normalizedKey) {
            case "UP" -> current == 0 ? options.size() - 1 : current - 1;
            case "DOWN" -> current == options.size() - 1 ? 0 : current + 1;
            default -> -1;
        };
        if (next < 0) {
            return ignored("list:unmapped:" + normalizedKey);
        }
        return handled(Map.of(
                "selectedIndex", next,
                "selectedOption", options.get(next),
                "optionCount", options.size(),
                "effect", "list:" + normalizedMode.toLowerCase(java.util.Locale.ROOT) + ":" + normalizedKey.toLowerCase(java.util.Locale.ROOT),
                "focusPath", focusPath(normalizedMode, EchoAgent5UiReference.WIKI_SCREEN)
        ));
    }

    public static Map<String, Object> routeNotificationDismiss(Object notifications, EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        List<Map<String, Object>> queue = notificationList(notifications, source);
        if (queue.isEmpty()) {
            return ignored("notification:empty");
        }
        Map<String, Object> dismissed = queue.get(0);
        List<Map<String, Object>> remaining = queue.stream().skip(1).map(Map::copyOf).toList();
        return handled(Map.of(
                "dismissedId", String.valueOf(dismissed.get("id")),
                "dismissedMessage", String.valueOf(dismissed.get("message")),
                "remainingNotifications", remaining,
                "remainingMessages", remaining.stream()
                        .map(notification -> String.valueOf(notification.get("message")))
                        .toList(),
                "effect", "notification:dismiss-oldest"
        ));
    }

    public static Map<String, Object> routeSettingsAdjustment(String selectedOption, double hudScale, boolean subtitles) {
        String option = normalize(selectedOption).toUpperCase(java.util.Locale.ROOT);
        if ("HUD SCALE".equals(option)) {
            double nextScale = hudScale >= 1.25D ? 1.0D : 1.25D;
            return handled(Map.of(
                    "settingsHudScale", nextScale,
                    "settingsSubtitles", subtitles,
                    "settingsAppliedOption", "HUD Scale",
                    "settingsOutput", "HUD scale " + nextScale,
                    "effect", "settings:hud_scale"
            ));
        }
        if ("SUBTITLES".equals(option)) {
            boolean nextSubtitles = !subtitles;
            return handled(Map.of(
                    "settingsHudScale", hudScale,
                    "settingsSubtitles", nextSubtitles,
                    "settingsAppliedOption", "Subtitles",
                    "settingsOutput", "Subtitles " + (nextSubtitles ? "enabled" : "disabled"),
                    "effect", "settings:subtitles"
            ));
        }
        return ignored("settings:unsupported:" + option);
    }

    public static Map<String, Object> routePauseOption(String selectedOption, String previousMode) {
        String option = normalize(selectedOption);
        if (option.isBlank()) {
            option = "Resume";
        }
        String normalizedOption = option.toUpperCase(java.util.Locale.ROOT);
        String resumeTarget = fallbackPreviousMode(previousMode);
        return switch (normalizedOption) {
            case "RESUME" -> handled(Map.of(
                    "destinationMode", resumeTarget,
                    "destinationPreviousMode", EchoAgent5UiReference.WIKI_SCREEN,
                    "selectedOption", "Resume",
                    "effect", "pause:resume"
            ));
            case "SETTINGS" -> handled(Map.of(
                    "destinationMode", "SETTINGS",
                    "destinationPreviousMode", "PAUSE",
                    "selectedOption", "Settings",
                    "effect", "pause:settings"
            ));
            case "QUIT TO MAIN MENU" -> handled(Map.of(
                    "destinationMode", "MAIN_MENU",
                    "destinationPreviousMode", "PAUSE",
                    "selectedOption", "Quit to Main Menu",
                    "effect", "pause:main_menu"
            ));
            default -> ignored("pause:unsupported:" + normalizedOption);
        };
    }

    public static Map<String, Object> routeMainMenuOption(String selectedOption) {
        String option = normalize(selectedOption);
        if (option.isBlank()) {
            option = "Continue";
        }
        String normalizedOption = option.toUpperCase(java.util.Locale.ROOT);
        return switch (normalizedOption) {
            case "CONTINUE" -> handled(Map.of(
                    "destinationMode", EchoAgent5UiReference.WIKI_SCREEN,
                    "destinationPreviousMode", EchoAgent5UiReference.MAIN_MENU_SCREEN,
                    "selectedOption", "Continue",
                    "mainMenuOutput", "Continue selected: opening Wiki",
                    "quitRequested", false,
                    "effect", "main_menu:continue"
            ));
            case "NEW ASHFALL RUN" -> handled(Map.of(
                    "destinationMode", EchoAgent5UiReference.MISSION_LOG_SCREEN,
                    "destinationPreviousMode", EchoAgent5UiReference.MAIN_MENU_SCREEN,
                    "selectedOption", "New Ashfall Run",
                    "mainMenuOutput", "New Ashfall Run selected: opening Mission Log",
                    "quitRequested", false,
                    "effect", "main_menu:new_run"
            ));
            case "SETTINGS" -> handled(Map.of(
                    "destinationMode", EchoAgent5UiReference.SETTINGS_SCREEN,
                    "destinationPreviousMode", EchoAgent5UiReference.MAIN_MENU_SCREEN,
                    "selectedOption", "Settings",
                    "mainMenuOutput", "Settings selected: opening Settings",
                    "quitRequested", false,
                    "effect", "main_menu:settings"
            ));
            case "QUIT" -> handled(Map.of(
                    "destinationMode", EchoAgent5UiReference.MAIN_MENU_SCREEN,
                    "destinationPreviousMode", EchoAgent5UiReference.MAIN_MENU_SCREEN,
                    "selectedOption", "Quit",
                    "mainMenuOutput", "Quit selected: native quit requested",
                    "quitRequested", true,
                    "effect", "main_menu:quit_requested"
            ));
            default -> ignored("main_menu:unsupported:" + normalizedOption);
        };
    }

    public static Map<String, Object> routeHudUpdate(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hud = source.hudValues();
        int currentHealth = integer(state == null ? null : state.get("hudHealth"), integer(hud.get("health"), 92));
        int nextHealth = Math.max(0, currentHealth - 7);
        String hazard = String.valueOf(hud.get("hazard"));
        return handled(Map.of(
                "hudHealth", nextHealth,
                "hudHazard", hazard,
                "hudMission", EchoAgent5UiReference.ACTIVE_MISSION_OBJECTIVE,
                "hudUpdateOutput", "HUD updated: health " + nextHealth + " hazard " + hazard,
                "effect", "hud:update:health_hazard_mission"
        ));
    }

    public static Map<String, Object> routeCameraCinematicFrame(
            Map<String, Object> state,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> camera = source.cameraValues();
        Map<String, Object> cinematic = source.cinematicValues();
        int currentFrame = integer(state == null ? null : state.get("cinematicFrame"), 0);
        int nextFrame = currentFrame + 1;
        String cameraMode = String.valueOf(camera.get("mode"));
        String cue = String.valueOf(cinematic.get("cue"));
        return handled(Map.of(
                "cameraMode", cameraMode,
                "cameraFov", integer(camera.get("fov"), 72),
                "cameraTarget", String.valueOf(camera.get("target")),
                "cinematicCue", cue,
                "cinematicFrame", nextFrame,
                "cinematicLetterbox", Boolean.TRUE.equals(cinematic.get("letterbox")),
                "cinematicSubtitle", String.valueOf(cinematic.get("subtitle")),
                "cinematicOutput", "Camera " + cameraMode + " frame " + nextFrame + " cue " + cue,
                "effect", "camera_cinematic:frame:" + cue
        ));
    }

    public static Map<String, Object> routeMissionLogUpdate(Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> mission = source.missionLogValues();
        double currentProgress = doubleValue(state == null ? null : state.get("missionProgress"), 0.25D);
        double nextProgress = currentProgress >= 0.5D ? currentProgress : 0.5D;
        return handled(Map.of(
                "missionId", String.valueOf(mission.get("missionId")),
                "missionTitle", String.valueOf(mission.get("title")),
                "missionObjective", String.valueOf(mission.get("objective")),
                "missionProgress", nextProgress,
                "missionStatus", EchoAgent5UiReference.ACTIVE_MISSION_UPDATED_STATUS,
                "missionUpdateLine", EchoAgent5UiReference.ACTIVE_MISSION_UPDATE_LINE,
                "effect", "mission:update:" + mission.get("missionId")
        ));
    }

    public static Map<String, Object> activate(String mode, Map<String, Object> state, EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        String normalizedMode = normalizeMode(mode);
        String focusedControl = string(state, "focusedControl", "");
        return switch (normalizedMode) {
            case "TERMINAL" -> activateTerminal(focusedControl, string(state, "terminalBuffer", ""), source);
            case "INDEX" -> activateIndex(focusedControl, string(state, "indexBuffer", ""), source);
            case "LENS" -> activateLens(focusedControl, source);
            case "RECOVERY" -> activateRecovery(focusedControl, source);
            default -> ignored("activate:unsupported:" + normalizedMode);
        };
    }

    private static Map<String, Object> activateTerminal(
            String focusedControl,
            String terminalBuffer,
            EchoAgent5UiDataSources source
    ) {
        if (!"terminal:input".equals(focusedControl) || !source.terminalCommand().equalsIgnoreCase(terminalBuffer)) {
            return ignored("terminal:unhandled");
        }
        return action(
                "terminalOutput",
                "terminalCommandExecuted",
                source.terminalReadyLine(),
                "terminal-command:" + source.terminalCommand()
        );
    }

    private static Map<String, Object> activateIndex(
            String focusedControl,
            String indexBuffer,
            EchoAgent5UiDataSources source
    ) {
        if (!"index:search".equals(focusedControl) || !source.indexQuery().equalsIgnoreCase(indexBuffer)) {
            return ignored("index:unhandled");
        }
        return action(
                "indexOutput",
                "indexSearchExecuted",
                source.indexResult(),
                "index-search:" + source.indexQuery()
        );
    }

    private static Map<String, Object> activateLens(String focusedControl, EchoAgent5UiDataSources source) {
        if (!"lens:scan".equals(focusedControl)) {
            return ignored("lens:not-focused");
        }
        return action(
                "lensOutput",
                "lensScanExecuted",
                source.lensResult(),
                "lens-scan:" + source.lensTarget()
        );
    }

    private static Map<String, Object> activateRecovery(String focusedControl, EchoAgent5UiDataSources source) {
        if (!"recovery:recover".equals(focusedControl)) {
            return ignored("recovery:not-focused");
        }
        return action(
                "recoveryOutput",
                "recoveryActionExecuted",
                "Status: " + EchoAgent5UiReference.RECOVERY_STATUS
                        + "    Health: " + source.deathRecoveryValues(EchoAgent5UiReference.RECOVERY_STATUS).get("restoredHealth"),
                "death-recovery:" + EchoAgent5UiReference.RECOVERY_POINT
        );
    }

    private static Map<String, Object> route(String mode) {
        return handled(Map.of(
                "destinationMode", mode,
                "destinationPreviousMode", EchoAgent5UiReference.WIKI_SCREEN,
                "effect", "route:" + mode.toLowerCase(java.util.Locale.ROOT)
        ));
    }

    private static Map<String, Object> action(
            String outputKey,
            String executedKey,
            String output,
            String effect
    ) {
        return handled(Map.of(
                "outputKey", outputKey,
                "output", output,
                "executedKey", executedKey,
                "effects", java.util.List.of(effect)
        ));
    }

    private static List<String> listOptions(String mode, EchoAgent5UiDataSources source) {
        return switch (mode) {
            case "MAIN_MENU" -> source.mainMenuOptions();
            case "PAUSE" -> source.pauseOptions();
            case "SETTINGS" -> List.of("Profile", "Theme", "Input Mode", "HUD Scale", "Subtitles");
            default -> List.of();
        };
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> notificationList(Object value, EchoAgent5UiDataSources source) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(entry -> (Map<String, Object>) entry)
                    .map(Map::copyOf)
                    .toList();
        }
        return source.notifications();
    }

    private static int clampIndex(int selectedIndex, int size) {
        if (size <= 0) {
            return 0;
        }
        if (selectedIndex < 0) {
            return 0;
        }
        if (selectedIndex >= size) {
            return size - 1;
        }
        return selectedIndex;
    }

    private static int integer(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static Map<String, Object> handled(Map<String, Object> values) {
        Map<String, Object> result = new LinkedHashMap<>(values);
        result.put("handled", true);
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        result.put("routerClass", EchoAgent5UiActionRouter.class.getSimpleName());
        return Map.copyOf(result);
    }

    private static Map<String, Object> ignored(String reason) {
        return Map.of(
                "handled", false,
                "reason", reason,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true,
                "routerClass", EchoAgent5UiActionRouter.class.getSimpleName()
        );
    }

    private static String string(Map<String, Object> values, String key, String fallback) {
        if (values == null) {
            return fallback;
        }
        Object value = values.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String removeLast(String value) {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            return "";
        }
        return normalized.substring(0, normalized.length() - 1);
    }

    private static double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String string) {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static String normalizeMode(String mode) {
        String normalized = normalize(mode).toUpperCase(java.util.Locale.ROOT);
        return normalized.isBlank() ? "TERMINAL" : normalized;
    }

    private static String fallbackPreviousMode(String previousMode) {
        return previousMode == null || previousMode.isBlank() ? EchoAgent5UiReference.WIKI_SCREEN : previousMode;
    }
}
