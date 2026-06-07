package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveSystemFlowAcceptanceSmoke {
    private EchoAgent5LiveSystemFlowAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> settings = object(EchoAgent5SettingsEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> pause = object(EchoAgent5PauseEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> recovery = object(EchoAgent5RecoveryEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> accepted = EchoAgent5LiveSystemFlowAcceptance.assess(settings, pause, recovery);
        Map<String, Object> rejectedNoSettings = EchoAgent5LiveSystemFlowAcceptance.assess(
                Map.of("accepted", false),
                pause,
                recovery
        );
        Map<String, Object> rejectedNoPause = EchoAgent5LiveSystemFlowAcceptance.assess(
                settings,
                Map.of("accepted", false),
                recovery
        );
        Map<String, Object> rejectedNoRecovery = EchoAgent5LiveSystemFlowAcceptance.assess(
                settings,
                pause,
                Map.of("accepted", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_system_flow:accepted:SETTINGS_ACTION/ESCAPE/RECOVERY_ACTION".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoSettings.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoPause.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRecovery.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveSystemFlowAcceptanceSmokeClass",
                EchoAgent5LiveSystemFlowAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoSettings", rejectedNoSettings);
        smoke.put("rejectedNoPause", rejectedNoPause);
        smoke.put("rejectedNoRecovery", rejectedNoRecovery);
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
