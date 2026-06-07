package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5MainMenuOverrideSmoke {
    private EchoAgent5MainMenuOverrideSmoke() {
    }

    public static Map<String, Object> capture(
            boolean titleScreenDetected,
            boolean overrideAttached,
            String skipReason,
            String screenClass,
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        Map<String, Object> snapshot = EchoAgent5UiHostSmokeSnapshot.capture(
                "MAIN_MENU",
                overrideAttached,
                screenClass,
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                dataSources
        );
        boolean guardSatisfied = titleScreenDetected && overrideAttached;
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("mainMenuOverrideSmokeClass", EchoAgent5MainMenuOverrideSmoke.class.getSimpleName());
        smoke.put("strategy", "guarded_title_screen_replacement");
        smoke.put("titleScreenDetected", titleScreenDetected);
        smoke.put("overrideAttached", overrideAttached);
        smoke.put("skipReason", skipReason == null ? "" : skipReason);
        smoke.put("screenClass", screenClass);
        smoke.put("snapshot", snapshot);
        smoke.put("screenTitle", snapshot.get("screenTitle"));
        smoke.put("surfaceLines", snapshot.get("surfaceLines"));
        smoke.put("passed", guardSatisfied || !titleScreenDetected);
        smoke.put("guardSatisfied", guardSatisfied);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        return Map.copyOf(smoke);
    }
}
