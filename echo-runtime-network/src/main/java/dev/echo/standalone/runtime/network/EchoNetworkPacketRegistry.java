package dev.echo.standalone.runtime.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoNetworkPacketRegistry {
    private final LinkedHashMap<String, EchoNetworkPacketType> packetTypes = new LinkedHashMap<>();

    public synchronized void register(EchoNetworkPacketType packetType) {
        Objects.requireNonNull(packetType, "packetType");
        if (packetTypes.containsKey(packetType.typeId())) {
            throw new IllegalArgumentException("Duplicate network packet type id: " + packetType.typeId());
        }
        packetTypes.put(packetType.typeId(), packetType);
    }

    public synchronized Optional<EchoNetworkPacketType> find(String typeId) {
        String normalized = EchoNetworkText.requireText(typeId, "typeId");
        return Optional.ofNullable(packetTypes.get(normalized));
    }

    public synchronized EchoNetworkPacketType require(String typeId) {
        String normalized = EchoNetworkText.requireText(typeId, "typeId");
        EchoNetworkPacketType packetType = packetTypes.get(normalized);
        if (packetType == null) {
            throw new IllegalArgumentException("Unknown network packet type id: " + normalized);
        }
        return packetType;
    }

    public synchronized List<EchoNetworkPacketType> all() {
        return List.copyOf(packetTypes.values());
    }

    public synchronized List<EchoNetworkPacketType> byKind(EchoNetworkPacketKind kind) {
        Objects.requireNonNull(kind, "kind");
        return packetTypes.values().stream()
                .filter(packetType -> packetType.kind() == kind)
                .toList();
    }

    public synchronized int count() {
        return packetTypes.size();
    }
}
