package dev.echo.standalone.runtime.network;

import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class EchoNetworkSyncService {
    private final EchoNetworkPacketRegistry registry;
    private final EchoLocalNetworkTransport transport;

    public EchoNetworkSyncService(
            EchoNetworkPacketRegistry registry,
            EchoLocalNetworkTransport transport
    ) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.transport = Objects.requireNonNull(transport, "transport");
    }

    public EchoNetworkSyncResult syncEntities(
            EchoEntityRuntimeResult entities,
            EchoNetworkEndpoint source,
            EchoNetworkEndpoint target
    ) {
        Objects.requireNonNull(entities, "entities");
        List<EchoEntitySyncSnapshot> snapshots = entities.store().all().stream()
                .map(EchoEntitySyncSnapshot::from)
                .toList();
        String payload = snapshots.stream()
                .map(EchoEntitySyncSnapshot::payload)
                .collect(Collectors.joining(";"));
        EchoNetworkPacket packet = send(
                registry.require("echo:entity_sync"),
                source,
                target,
                "entities=" + snapshots.size() + ";" + payload
        );
        return new EchoNetworkSyncResult(packet, snapshots.size(), 0, "entity snapshots replicated");
    }

    public EchoNetworkSyncResult syncInventories(
            EchoItemRuntimeResult items,
            EchoNetworkEndpoint source,
            EchoNetworkEndpoint target
    ) {
        Objects.requireNonNull(items, "items");
        List<EchoInventorySyncSnapshot> snapshots = items.inventoryStore().all().stream()
                .map(EchoInventorySyncSnapshot::from)
                .toList();
        int itemStackCount = snapshots.stream()
                .mapToInt(EchoInventorySyncSnapshot::itemStackCount)
                .sum();
        String payload = snapshots.stream()
                .map(EchoInventorySyncSnapshot::payload)
                .collect(Collectors.joining(";"));
        EchoNetworkPacket packet = send(
                registry.require("echo:inventory_sync"),
                source,
                target,
                "inventories=" + snapshots.size() + ";itemStacks=" + itemStackCount + ";" + payload
        );
        return new EchoNetworkSyncResult(
                packet,
                snapshots.size(),
                itemStackCount,
                "inventory snapshots replicated"
        );
    }

    private EchoNetworkPacket send(
            EchoNetworkPacketType packetType,
            EchoNetworkEndpoint source,
            EchoNetworkEndpoint target,
            String payload
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
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

    private static String packetId(long sequence, EchoNetworkPacketKind kind) {
        return String.format(Locale.ROOT, "packet-%04d-%s", sequence, kind.name().toLowerCase(Locale.ROOT));
    }
}
