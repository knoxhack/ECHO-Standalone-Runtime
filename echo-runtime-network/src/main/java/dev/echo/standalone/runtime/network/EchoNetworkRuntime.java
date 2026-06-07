package dev.echo.standalone.runtime.network;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;

import java.util.Objects;

public final class EchoNetworkRuntime {
    public EchoNetworkRuntimeResult createLocalDebugNetwork(
            EchoRuntimeServiceRegistry services,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");

        EchoNetworkProtocol protocol = EchoNetworkProtocols.standaloneDebug();
        EchoNetworkPacketRegistry registry = new EchoNetworkPacketRegistry();
        registerPacketTypes(registry);

        EchoNetworkDiagnostics diagnostics = new EchoNetworkDiagnostics();
        EchoLocalNetworkTransport transport = new EchoLocalNetworkTransport(diagnostics);
        EchoNetworkEndpoint clientEndpoint = new EchoNetworkEndpoint(
                "echo:debug-client",
                EchoNetworkRole.CLIENT,
                protocol
        );
        EchoNetworkEndpoint serverEndpoint = new EchoNetworkEndpoint(
                "echo:debug-server",
                EchoNetworkRole.SERVER,
                protocol
        );
        EchoNetworkHandshakeService handshakeService = new EchoNetworkHandshakeService(
                registry,
                transport,
                diagnostics
        );
        EchoNetworkSyncService syncService = new EchoNetworkSyncService(registry, transport);
        EchoNetworkHandshakeResult handshake = handshakeService.connect(clientEndpoint, serverEndpoint);

        EchoNetworkRuntimeResult result = new EchoNetworkRuntimeResult(
                protocol,
                registry,
                clientEndpoint,
                serverEndpoint,
                transport,
                diagnostics,
                handshakeService,
                syncService,
                handshake
        );
        services.register(EchoNetworkRuntimeResult.class, result);
        services.register(EchoNetworkPacketRegistry.class, registry);
        services.register(EchoLocalNetworkTransport.class, transport);
        services.register(EchoNetworkDiagnostics.class, diagnostics);
        services.register(EchoNetworkHandshakeService.class, handshakeService);
        services.register(EchoNetworkSyncService.class, syncService);
        services.register(EchoNetworkHandshakeResult.class, handshake);
        return result;
    }

    private static void registerPacketTypes(EchoNetworkPacketRegistry registry) {
        registry.register(new EchoNetworkPacketType(
                "echo:handshake",
                EchoNetworkPacketKind.HANDSHAKE,
                "echo:session",
                true,
                "Client protocol offer"
        ));
        registry.register(new EchoNetworkPacketType(
                "echo:handshake_ack",
                EchoNetworkPacketKind.HANDSHAKE_ACK,
                "echo:session",
                true,
                "Server protocol acknowledgement"
        ));
        registry.register(new EchoNetworkPacketType(
                "echo:entity_sync",
                EchoNetworkPacketKind.ENTITY_SYNC,
                "echo:sync/entities",
                true,
                "Authoritative entity snapshot replication"
        ));
        registry.register(new EchoNetworkPacketType(
                "echo:inventory_sync",
                EchoNetworkPacketKind.INVENTORY_SYNC,
                "echo:sync/inventories",
                true,
                "Authoritative inventory snapshot replication"
        ));
        registry.register(new EchoNetworkPacketType(
                "echo:diagnostic",
                EchoNetworkPacketKind.DIAGNOSTIC,
                "echo:diagnostics",
                false,
                "In-memory network diagnostic event"
        ));
    }
}
