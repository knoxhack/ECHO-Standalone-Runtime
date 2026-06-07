package dev.echo.standalone.runtime.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoLocalNetworkTransport {
    private final EchoNetworkDiagnostics diagnostics;
    private final ArrayList<EchoNetworkPacket> packets = new ArrayList<>();
    private long nextSequence = 1L;

    public EchoLocalNetworkTransport(EchoNetworkDiagnostics diagnostics) {
        this.diagnostics = Objects.requireNonNull(diagnostics, "diagnostics");
    }

    public synchronized long nextSequence() {
        return nextSequence++;
    }

    public synchronized EchoNetworkPacket send(EchoNetworkPacket packet) {
        Objects.requireNonNull(packet, "packet");
        packets.add(packet);
        diagnostics.info(
                "sent " + packet.type().kind().name() + " from "
                        + packet.sourceEndpointId() + " to " + packet.targetEndpointId(),
                packet
        );
        return packet;
    }

    public synchronized List<EchoNetworkPacket> packets() {
        return List.copyOf(packets);
    }

    public synchronized List<EchoNetworkPacket> packetsFor(String targetEndpointId) {
        String normalized = EchoNetworkText.requireText(targetEndpointId, "targetEndpointId");
        return packets.stream()
                .filter(packet -> packet.targetEndpointId().equals(normalized))
                .toList();
    }

    public synchronized int count() {
        return packets.size();
    }
}
