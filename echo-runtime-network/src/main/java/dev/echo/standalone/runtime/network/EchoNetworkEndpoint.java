package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkEndpoint(
        String endpointId,
        EchoNetworkRole role,
        EchoNetworkProtocol protocol
) {
    public EchoNetworkEndpoint {
        endpointId = EchoNetworkText.requireText(endpointId, "endpointId");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(protocol, "protocol");
    }
}
