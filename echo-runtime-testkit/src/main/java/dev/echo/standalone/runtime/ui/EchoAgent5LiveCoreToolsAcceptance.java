package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveCoreToolsAcceptance {
    private EchoAgent5LiveCoreToolsAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> terminalEndToEndAcceptance,
            Map<String, Object> indexEndToEndAcceptance,
            Map<String, Object> lensEndToEndAcceptance
    ) {
        Map<String, Object> terminal = terminalEndToEndAcceptance == null ? Map.of() : terminalEndToEndAcceptance;
        Map<String, Object> index = indexEndToEndAcceptance == null ? Map.of() : indexEndToEndAcceptance;
        Map<String, Object> lens = lensEndToEndAcceptance == null ? Map.of() : lensEndToEndAcceptance;
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        boolean terminalAccepted = Boolean.TRUE.equals(terminal.get("accepted"))
                && "terminal_end_to_end:M->TERMINAL:status".equals(terminal.get("effect"))
                && "M".equals(terminal.get("key"))
                && "TERMINAL".equals(terminal.get("surface"))
                && "status".equals(terminal.get("command"))
                && Boolean.TRUE.equals(terminal.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(terminal.get("renderAccepted"))
                && Boolean.TRUE.equals(terminal.get("focusAccepted"))
                && Boolean.TRUE.equals(terminal.get("editingAccepted"))
                && Boolean.TRUE.equals(terminal.get("transcriptAccepted"))
                && Boolean.TRUE.equals(terminal.get("commandExecuted"))
                && Boolean.TRUE.equals(terminal.get("terminalRendered"));
        boolean indexAccepted = Boolean.TRUE.equals(index.get("accepted"))
                && "index_end_to_end:G->INDEX:ashfall".equals(index.get("effect"))
                && "G".equals(index.get("key"))
                && "INDEX".equals(index.get("surface"))
                && "ashfall".equals(index.get("query"))
                && Boolean.TRUE.equals(index.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(index.get("renderAccepted"))
                && Boolean.TRUE.equals(index.get("focusAccepted"))
                && Boolean.TRUE.equals(index.get("editingAccepted"))
                && Boolean.TRUE.equals(index.get("transcriptAccepted"))
                && Boolean.TRUE.equals(index.get("searchExecuted"))
                && Boolean.TRUE.equals(index.get("indexRendered"));
        boolean lensAccepted = Boolean.TRUE.equals(lens.get("accepted"))
                && ("lens_end_to_end:LEFT_ALT->LENS:" + source.lensTarget()).equals(lens.get("effect"))
                && "LEFT_ALT".equals(lens.get("key"))
                && "LENS".equals(lens.get("surface"))
                && source.lensTarget().equals(lens.get("target"))
                && Boolean.TRUE.equals(lens.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(lens.get("renderAccepted"))
                && Boolean.TRUE.equals(lens.get("focusAccepted"))
                && Boolean.TRUE.equals(lens.get("transcriptAccepted"))
                && Boolean.TRUE.equals(lens.get("scanExecuted"))
                && Boolean.TRUE.equals(lens.get("lensRendered"));
        boolean accepted = terminalAccepted && indexAccepted && lensAccepted;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("terminalAccepted", terminalAccepted);
        result.put("indexAccepted", indexAccepted);
        result.put("lensAccepted", lensAccepted);
        result.put("terminalCommand", String.valueOf(terminal.getOrDefault("command", "")));
        result.put("indexQuery", String.valueOf(index.getOrDefault("query", "")));
        result.put("lensTarget", String.valueOf(lens.getOrDefault("target", "")));
        result.put("effect", accepted
                ? "live_core_tools:accepted:M/G/LEFT_ALT"
                : "live_core_tools:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }
}
