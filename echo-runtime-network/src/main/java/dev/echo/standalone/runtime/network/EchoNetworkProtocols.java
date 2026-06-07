package dev.echo.standalone.runtime.network;

import java.util.List;

public final class EchoNetworkProtocols {
    private EchoNetworkProtocols() {
    }

    public static EchoNetworkProtocol standaloneDebug() {
        return new EchoNetworkProtocol(
                "echo.standalone.protocol",
                1,
                1,
                List.of("diagnostics", "entity_sync", "inventory_sync")
        );
    }
}
