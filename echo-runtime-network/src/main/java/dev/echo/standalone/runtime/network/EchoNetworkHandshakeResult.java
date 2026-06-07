package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkHandshakeResult(
        boolean accepted,
        EchoNetworkEndpoint clientEndpoint,
        EchoNetworkEndpoint serverEndpoint,
        EchoNetworkPacket requestPacket,
        EchoNetworkPacket responsePacket,
        String reason
) {
    public EchoNetworkHandshakeResult {
        Objects.requireNonNull(clientEndpoint, "clientEndpoint");
        Objects.requireNonNull(serverEndpoint, "serverEndpoint");
        Objects.requireNonNull(requestPacket, "requestPacket");
        Objects.requireNonNull(responsePacket, "responsePacket");
        reason = EchoNetworkText.requireText(reason, "reason");
    }
}
