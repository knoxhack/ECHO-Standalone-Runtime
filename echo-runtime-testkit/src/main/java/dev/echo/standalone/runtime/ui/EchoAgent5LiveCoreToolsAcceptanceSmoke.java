package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveCoreToolsAcceptanceSmoke {
    private EchoAgent5LiveCoreToolsAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> terminal = object(EchoAgent5TerminalEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> index = object(EchoAgent5IndexEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> lens = object(EchoAgent5LensEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> accepted = EchoAgent5LiveCoreToolsAcceptance.assess(terminal, index, lens);
        Map<String, Object> rejectedNoTerminal = EchoAgent5LiveCoreToolsAcceptance.assess(
                Map.of("accepted", false),
                index,
                lens
        );
        Map<String, Object> rejectedNoIndex = EchoAgent5LiveCoreToolsAcceptance.assess(
                terminal,
                Map.of("accepted", false),
                lens
        );
        Map<String, Object> rejectedNoLens = EchoAgent5LiveCoreToolsAcceptance.assess(
                terminal,
                index,
                Map.of("accepted", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_core_tools:accepted:M/G/LEFT_ALT".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoTerminal.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoIndex.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoLens.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveCoreToolsAcceptanceSmokeClass",
                EchoAgent5LiveCoreToolsAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoTerminal", rejectedNoTerminal);
        smoke.put("rejectedNoIndex", rejectedNoIndex);
        smoke.put("rejectedNoLens", rejectedNoLens);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
