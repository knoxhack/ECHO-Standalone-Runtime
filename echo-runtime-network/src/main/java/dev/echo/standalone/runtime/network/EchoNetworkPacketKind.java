package dev.echo.standalone.runtime.network;

public enum EchoNetworkPacketKind {
    HANDSHAKE,
    HANDSHAKE_ACK,
    ENTITY_SYNC,
    INVENTORY_SYNC,
    DIAGNOSTIC
}
