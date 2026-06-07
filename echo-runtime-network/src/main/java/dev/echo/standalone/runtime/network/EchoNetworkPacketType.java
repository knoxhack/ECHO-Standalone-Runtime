package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkPacketType(
        String typeId,
        EchoNetworkPacketKind kind,
        String channel,
        boolean reliable,
        String description
) {
    public EchoNetworkPacketType {
        typeId = EchoNetworkText.requireText(typeId, "typeId");
        Objects.requireNonNull(kind, "kind");
        channel = EchoNetworkText.requireText(channel, "channel");
        description = EchoNetworkText.requireText(description, "description");
    }
}
