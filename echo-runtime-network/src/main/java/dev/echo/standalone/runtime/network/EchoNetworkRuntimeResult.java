package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkRuntimeResult(
        EchoNetworkProtocol protocol,
        EchoNetworkPacketRegistry packetRegistry,
        EchoNetworkEndpoint clientEndpoint,
        EchoNetworkEndpoint serverEndpoint,
        EchoLocalNetworkTransport transport,
        EchoNetworkDiagnostics diagnostics,
        EchoNetworkHandshakeService handshakeService,
        EchoNetworkSyncService syncService,
        EchoNetworkHandshakeResult handshake
) {
    public EchoNetworkRuntimeResult {
        Objects.requireNonNull(protocol, "protocol");
        Objects.requireNonNull(packetRegistry, "packetRegistry");
        Objects.requireNonNull(clientEndpoint, "clientEndpoint");
        Objects.requireNonNull(serverEndpoint, "serverEndpoint");
        Objects.requireNonNull(transport, "transport");
        Objects.requireNonNull(diagnostics, "diagnostics");
        Objects.requireNonNull(handshakeService, "handshakeService");
        Objects.requireNonNull(syncService, "syncService");
        Objects.requireNonNull(handshake, "handshake");
    }
}
