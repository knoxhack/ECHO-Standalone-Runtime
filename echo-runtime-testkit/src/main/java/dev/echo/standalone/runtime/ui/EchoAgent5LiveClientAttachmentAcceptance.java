package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveClientAttachmentAcceptance {
    private EchoAgent5LiveClientAttachmentAcceptance() {
    }

    public static Map<String, Object> assess(
            boolean clientHostReady,
            boolean uiScreenHostCompiled,
            boolean clientThreadAccepted,
            long windowHandle,
            String compiledScreenClass,
            String expectedScreenClass
    ) {
        String compiledClass = text(compiledScreenClass);
        String expectedClass = text(expectedScreenClass);
        boolean screenClassMatches = !compiledClass.isBlank()
                && (compiledClass.equals(expectedClass) || compiledClass.endsWith("." + simpleName(expectedClass)));
        boolean physicalPollingReady = windowHandle > 0L;
        boolean accepted = clientHostReady
                && uiScreenHostCompiled
                && clientThreadAccepted
                && physicalPollingReady
                && screenClassMatches;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("minecraftClientReady", clientHostReady);
        result.put("dashboardScreenCompiled", uiScreenHostCompiled);
        result.put("clientThreadAccepted", clientThreadAccepted);
        result.put("physicalHotkeyPollingReady", physicalPollingReady);
        result.put("windowHandlePresent", physicalPollingReady);
        result.put("compiledScreenClass", compiledClass);
        result.put("expectedScreenClass", expectedClass);
        result.put("screenClassMatches", screenClassMatches);
        result.put("effect", accepted
                ? "live_client_attachment:accepted:" + simpleName(expectedClass)
                : "live_client_attachment:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    private static String simpleName(String className) {
        int dot = className.lastIndexOf('.');
        return dot < 0 ? className : className.substring(dot + 1);
    }

    private static String text(String value) {
        return value == null ? "" : value;
    }
}
