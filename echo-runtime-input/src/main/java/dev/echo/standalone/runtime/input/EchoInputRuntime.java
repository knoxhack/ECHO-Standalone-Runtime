package dev.echo.standalone.runtime.input;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;

import java.util.Objects;

public final class EchoInputRuntime {
    public EchoInputRuntimeResult boot(
            EchoRuntimeServiceRegistry services,
            EchoUiRuntimeResult ui,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoEntityId playerId
    ) {
        Objects.requireNonNull(services, "services");
        EchoInputBindingMap bindings = EchoInputBindingMap.ashfallDefaults();
        EchoInputFocusState focus = new EchoInputFocusState();
        EchoInputRouter router = new EchoInputRouter(
                bindings,
                focus,
                ui,
                entities,
                gameplay,
                playerId
        );
        EchoInputRuntimeResult result = new EchoInputRuntimeResult(bindings, focus, router);
        services.register(EchoInputBindingMap.class, bindings);
        services.register(EchoInputFocusState.class, focus);
        services.register(EchoInputRouter.class, router);
        services.register(EchoInputRuntimeResult.class, result);
        return result;
    }
}
