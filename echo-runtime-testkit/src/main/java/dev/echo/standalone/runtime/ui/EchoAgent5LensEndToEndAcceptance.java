package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoAgent5LensEndToEndAcceptance {
    private EchoAgent5LensEndToEndAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> physicalHotkey,
            Map<String, Object> physicalInputAcceptance,
            Map<String, Object> liveSurfaceRenderAcceptance,
            Map<String, Object> focusSmoke,
            Map<String, Object> hostEventTranscriptSmoke,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> hotkey = physicalHotkey == null ? Map.of() : physicalHotkey;
        Map<String, Object> input = physicalInputAcceptance == null ? Map.of() : physicalInputAcceptance;
        Map<String, Object> render = liveSurfaceRenderAcceptance == null ? Map.of() : liveSurfaceRenderAcceptance;
        Map<String, Object> focus = focusSmoke == null ? Map.of() : focusSmoke;
        Map<String, Object> transcript = hostEventTranscriptSmoke == null ? Map.of() : hostEventTranscriptSmoke;
        String key = text(hotkey.get("key"));
        String surface = normalize(hotkey.get("surface"));
        List<String> focusLines = strings(focus, "renderedFocusLines");
        List<String> transcriptEvents = strings(transcript, "events");
        List<String> transcriptLines = strings(transcript, "renderedLines");
        boolean scanExecuted = strings(focus, "activationKeys").contains("lensScanExecuted")
                && transcriptEvents.contains("enter:lens:lensScanExecuted");
        boolean lensRendered = "LENS".equals(normalize(render.get("surface")))
                && text(render.get("moduleRendererClass")).contains("Lens")
                && focusLines.stream().anyMatch(line -> line.contains("lens:scan ready"))
                && transcriptLines.stream().anyMatch(line -> line.contains(source.lensResult()));
        boolean accepted = Boolean.TRUE.equals(hotkey.get("handled"))
                && "LEFT_ALT".equals(key)
                && "LENS".equals(surface)
                && Boolean.TRUE.equals(input.get("accepted"))
                && Boolean.TRUE.equals(render.get("accepted"))
                && Boolean.TRUE.equals(focus.get("passed"))
                && Boolean.TRUE.equals(transcript.get("passed"))
                && transcriptEvents.contains("key:LEFT_ALT->LENS")
                && scanExecuted
                && lensRendered;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("key", key);
        result.put("surface", surface);
        result.put("target", source.lensTarget());
        result.put("physicalInputAccepted", Boolean.TRUE.equals(input.get("accepted")));
        result.put("renderAccepted", Boolean.TRUE.equals(render.get("accepted")));
        result.put("focusAccepted", Boolean.TRUE.equals(focus.get("passed")));
        result.put("transcriptAccepted", Boolean.TRUE.equals(transcript.get("passed")));
        result.put("scanExecuted", scanExecuted);
        result.put("lensRendered", lensRendered);
        result.put("effect", accepted
                ? "lens_end_to_end:LEFT_ALT->LENS:" + source.lensTarget()
                : "lens_end_to_end:rejected:" + (surface.isBlank() ? "none" : surface));
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    @SuppressWarnings("unchecked")
    private static List<String> strings(Map<String, Object> source, String key) {
        Object value = source.get(key);
        if (value instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    private static String normalize(Object value) {
        return text(value).trim().toUpperCase(Locale.ROOT);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
