package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkPacket(
        String packetId,
        EchoNetworkPacketType type,
        String sourceEndpointId,
        String targetEndpointId,
        String payload,
        long sequence
) {
    public EchoNetworkPacket {
        packetId = EchoNetworkText.requireText(packetId, "packetId");
        Objects.requireNonNull(type, "type");
        sourceEndpointId = EchoNetworkText.requireText(sourceEndpointId, "sourceEndpointId");
        targetEndpointId = EchoNetworkText.requireText(targetEndpointId, "targetEndpointId");
        payload = EchoNetworkText.requireText(payload, "payload");
        if (sequence <= 0) {
            throw new IllegalArgumentException("sequence must be positive");
        }
    }
}
