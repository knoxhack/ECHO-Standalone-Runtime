package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveScreenStackStabilityAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";

    private EchoAgent5LiveScreenStackStabilityAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> stack = EchoAgent5ScreenStackSmoke.capture(
                SCREEN_CLASS,
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );
        Map<String, Object> lifecycle = EchoAgent5ScreenLifecycleSmoke.capture(
                SCREEN_CLASS,
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );
        Map<String, Object> interaction = EchoAgent5UiHostInteractionSmoke.run(
                SCREEN_CLASS,
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );
        Map<String, Object> accepted = EchoAgent5LiveScreenStackStabilityAcceptance.assess(
                stack,
                lifecycle,
                interaction
        );
        Map<String, Object> rejectedNoStack = EchoAgent5LiveScreenStackStabilityAcceptance.assess(
                Map.of("passed", false),
                lifecycle,
                interaction
        );
        Map<String, Object> rejectedNoLifecycle = EchoAgent5LiveScreenStackStabilityAcceptance.assess(
                stack,
                Map.of("passed", false),
                interaction
        );
        Map<String, Object> rejectedNoInteraction = EchoAgent5LiveScreenStackStabilityAcceptance.assess(
                stack,
                lifecycle,
                Map.of("passed", false, "steps", java.util.List.of())
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_screen_stack_stability:accepted:10-surfaces:no-crash".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoStack.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoLifecycle.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoInteraction.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveScreenStackStabilityAcceptanceSmokeClass",
                EchoAgent5LiveScreenStackStabilityAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoStack", rejectedNoStack);
        smoke.put("rejectedNoLifecycle", rejectedNoLifecycle);
        smoke.put("rejectedNoInteraction", rejectedNoInteraction);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
