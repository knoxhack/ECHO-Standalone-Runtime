package dev.echo.standalone.runtime.input;

import java.util.Objects;

public record EchoInputRuntimeResult(
        EchoInputBindingMap bindings,
        EchoInputFocusState focus,
        EchoInputRouter router
) {
    public EchoInputRuntimeResult {
        Objects.requireNonNull(bindings, "bindings");
        Objects.requireNonNull(focus, "focus");
        Objects.requireNonNull(router, "router");
    }

    public EchoInputRouteResult dispatch(EchoInputEvent event) {
        return router.route(event);
    }
}
