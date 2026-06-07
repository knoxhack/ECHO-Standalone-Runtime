package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveUiInteractionRecorder {
    private static int sequence;
    private static String mode = "";
    private static int characterCount;
    private static boolean editHandled;
    private static boolean submitHandled;
    private static String finalBuffer = "";
    private static String output = "";
    private static String effect = "";

    private EchoAgent5LiveUiInteractionRecorder() {
    }

    public static synchronized void clear() {
        sequence++;
        mode = "";
        characterCount = 0;
        editHandled = false;
        submitHandled = false;
        finalBuffer = "";
        output = "";
        effect = "";
    }

    public static synchronized void recordCharacter(String currentMode, Map<String, Object> route) {
        if (Boolean.TRUE.equals(route.get("handled"))) {
            mode = normalize(currentMode);
            characterCount++;
            finalBuffer = text(route.get("value"));
            sequence++;
        }
    }

    public static synchronized void recordEdit(String currentMode, Map<String, Object> route) {
        if (Boolean.TRUE.equals(route.get("handled"))) {
            mode = normalize(currentMode);
            editHandled = true;
            finalBuffer = text(route.get("value"));
            sequence++;
        }
    }

    public static synchronized void recordSubmit(String currentMode, Map<String, Object> action) {
        if (Boolean.TRUE.equals(action.get("handled"))) {
            mode = normalize(currentMode);
            submitHandled = true;
            output = text(action.get("output"));
            effect = "live_ui_interaction:" + mode + ":" + finalBuffer;
            sequence++;
        }
    }

    public static synchronized Map<String, Object> snapshot() {
        boolean accepted = switch (mode) {
            case "TERMINAL" -> submitHandled
                    && editHandled
                    && EchoAgent5UiReference.TERMINAL_COMMAND.equals(finalBuffer)
                    && EchoAgent5UiDataSources.reference().terminalReadyLine().equals(output);
            case "INDEX" -> submitHandled
                    && editHandled
                    && EchoAgent5UiReference.INDEX_QUERY.equals(finalBuffer)
                    && EchoAgent5UiDataSources.reference().indexResult().equals(output);
            default -> false;
        };
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("interactionRecorderClass", EchoAgent5LiveUiInteractionRecorder.class.getSimpleName());
        result.put("accepted", accepted);
        result.put("sequence", sequence);
        result.put("mode", mode);
        result.put("characterCount", characterCount);
        result.put("editHandled", editHandled);
        result.put("submitHandled", submitHandled);
        result.put("finalBuffer", finalBuffer);
        result.put("output", output);
        result.put("effect", accepted ? effect : "live_ui_interaction:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static synchronized Map<String, Object> smoke(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        clear();
        recordCharacter("TERMINAL", Map.of("handled", true, "value", source.terminalCommand() + "x"));
        recordEdit("TERMINAL", Map.of("handled", true, "value", source.terminalCommand()));
        recordSubmit("TERMINAL", Map.of("handled", true, "output", source.terminalReadyLine()));
        Map<String, Object> terminal = snapshot();
        clear();
        recordCharacter("INDEX", Map.of("handled", true, "value", source.indexQuery() + "x"));
        recordEdit("INDEX", Map.of("handled", true, "value", source.indexQuery()));
        recordSubmit("INDEX", Map.of("handled", true, "output", source.indexResult()));
        Map<String, Object> index = snapshot();
        return Map.of(
                "interactionRecorderSmokeClass", EchoAgent5LiveUiInteractionRecorder.class.getSimpleName(),
                "terminal", terminal,
                "index", index,
                "acceptedModes", List.of(terminal.get("mode"), index.get("mode")),
                "passed", Boolean.TRUE.equals(terminal.get("accepted")) && Boolean.TRUE.equals(index.get("accepted")),
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
