package dev.echo.standalone.runtime.network;

import java.util.Locale;
import java.util.Objects;

public final class EchoNetworkHandshakeService {
    private final EchoNetworkPacketRegistry registry;
    private final EchoLocalNetworkTransport transport;
    private final EchoNetworkDiagnostics diagnostics;

    public EchoNetworkHandshakeService(
            EchoNetworkPacketRegistry registry,
            EchoLocalNetworkTransport transport,
            EchoNetworkDiagnostics diagnostics
    ) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.transport = Objects.requireNonNull(transport, "transport");
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    }

    public EchoNetworkHandshakeResult connect(
            EchoNetworkEndpoint clientEndpoint,
            EchoNetworkEndpoint serverEndpoint
    ) {
        Objects.requireNonNull(clientEndpoint, "clientEndpoint");
        Objects.requireNonNull(serverEndpoint, "serverEndpoint");
        if (clientEndpoint.role() != EchoNetworkRole.CLIENT) {
            throw new IllegalArgumentException("clientEndpoint must have CLIENT role");
        }
        if (serverEndpoint.role() != EchoNetworkRole.SERVER) {
            throw new IllegalArgumentException("serverEndpoint must have SERVER role");
        }

        EchoNetworkPacket request = send(
                registry.require("echo:handshake"),
                clientEndpoint,
                serverEndpoint,
                protocolPayload(clientEndpoint.protocol())
        );
        boolean accepted = serverEndpoint.protocol().compatibleWith(clientEndpoint.protocol());
        String reason = accepted ? "protocol accepted" : "protocol incompatible";
        EchoNetworkPacket response = send(
                registry.require("echo:handshake_ack"),
                serverEndpoint,
                clientEndpoint,
                "accepted=" + accepted
                        + ";serverVersion=" + serverEndpoint.protocol().version()
                        + ";features=" + serverEndpoint.protocol().featureCsv()
        );
        diagnostics.info("handshake " + (accepted ? "accepted" : "rejected")
                + " between " + clientEndpoint.endpointId() + " and " + serverEndpoint.endpointId());
        return new EchoNetworkHandshakeResult(
                accepted,
                clientEndpoint,
                serverEndpoint,
                request,
                response,
                reason
        );
    }

    private EchoNetworkPacket send(
            EchoNetworkPacketType packetType,
            EchoNetworkEndpoint source,
            EchoNetworkEndpoint target,
            String payload
    ) {
        long sequence = transport.nextSequence();
        return transport.send(new EchoNetworkPacket(
                packetId(sequence, packetType.kind()),
                packetType,
                source.endpointId(),
                target.endpointId(),
                payload,
                sequence
        ));
    }

    private static String protocolPayload(EchoNetworkProtocol protocol) {
        return "protocol=" + protocol.protocolId()
                + ";version=" + protocol.version()
                + ";minCompatibleVersion=" + protocol.minCompatibleVersion()
                + ";features=" + protocol.featureCsv();
    }

    private static String packetId(long sequence, EchoNetworkPacketKind kind) {
        return String.format(Locale.ROOT, "packet-%04d-%s", sequence, kind.name().toLowerCase(Locale.ROOT));
    }
}
