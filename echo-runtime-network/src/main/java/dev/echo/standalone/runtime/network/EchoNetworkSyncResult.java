package dev.echo.standalone.runtime.network;

import java.util.Objects;

public record EchoNetworkSyncResult(
        EchoNetworkPacket packet,
        int snapshotCount,
        int detailCount,
        String reason
) {
    public EchoNetworkSyncResult {
        Objects.requireNonNull(packet, "packet");
        if (snapshotCount < 0) {
            throw new IllegalArgumentException("snapshotCount must not be negative");
        }
        if (detailCount < 0) {
            throw new IllegalArgumentException("detailCount must not be negative");
        }
        reason = EchoNetworkText.requireText(reason, "reason");
    }
}
