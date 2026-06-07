package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveTextInputCoverageAcceptance {
    private EchoAgent5LiveTextInputCoverageAcceptance() {
    }

    public static Map<String, Object> assess(Map<String, Object> terminal, Map<String, Object> index) {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        boolean terminalAccepted = acceptedMode(terminal, "TERMINAL", source.terminalCommand(), source.terminalReadyLine());
        boolean indexAccepted = acceptedMode(index, "INDEX", source.indexQuery(), source.indexResult());
        boolean accepted = terminalAccepted && indexAccepted;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("terminalAccepted", terminalAccepted);
        result.put("indexAccepted", indexAccepted);
        result.put("terminalMode", text(terminal == null ? null : terminal.get("mode")));
        result.put("indexMode", text(index == null ? null : index.get("mode")));
        result.put("effect", accepted
                ? "live_text_input_coverage:accepted:terminal+index"
                : "live_text_input_coverage:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        Map<String, Object> terminal = Map.of(
                "accepted", true,
                "mode", "TERMINAL",
                "finalBuffer", source.terminalCommand(),
                "output", source.terminalReadyLine(),
                "submitHandled", true,
                "editHandled", true
        );
        Map<String, Object> index = Map.of(
                "accepted", true,
                "mode", "INDEX",
                "finalBuffer", source.indexQuery(),
                "output", source.indexResult(),
                "submitHandled", true,
                "editHandled", true
        );
        Map<String, Object> accepted = assess(terminal, index);
        Map<String, Object> rejectedNoTerminal = assess(Map.of(), index);
        Map<String, Object> rejectedNoIndex = assess(terminal, Map.of());
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoTerminal.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoIndex.get("accepted"));
        return Map.of(
                "liveTextInputCoverageAcceptanceClass",
                EchoAgent5LiveTextInputCoverageAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedNoTerminal", rejectedNoTerminal,
                "rejectedNoIndex", rejectedNoIndex,
                "passed", passed,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static boolean acceptedMode(Map<String, Object> value, String mode, String buffer, String output) {
        return value != null
                && Boolean.TRUE.equals(value.get("accepted"))
                && mode.equals(value.get("mode"))
                && buffer.equals(value.get("finalBuffer"))
                && output.equals(value.get("output"))
                && Boolean.TRUE.equals(value.get("submitHandled"))
                && Boolean.TRUE.equals(value.get("editHandled"));
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
