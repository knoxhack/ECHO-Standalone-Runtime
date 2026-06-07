package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.network.EchoLocalNetworkTransport;
import dev.echo.standalone.runtime.network.EchoNetworkDiagnostics;
import dev.echo.standalone.runtime.network.EchoNetworkHandshakeResult;
import dev.echo.standalone.runtime.network.EchoNetworkHandshakeService;
import dev.echo.standalone.runtime.network.EchoNetworkPacketKind;
import dev.echo.standalone.runtime.network.EchoNetworkPacketRegistry;
import dev.echo.standalone.runtime.network.EchoNetworkRole;
import dev.echo.standalone.runtime.network.EchoNetworkRuntime;
import dev.echo.standalone.runtime.network.EchoNetworkRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkSyncResult;
import dev.echo.standalone.runtime.network.EchoNetworkSyncService;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

public final class EchoRuntimeNetworkSmokeHarness {
    private EchoRuntimeNetworkSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoNetworkRuntimeResult network = new EchoNetworkRuntime().createLocalDebugNetwork(
                services,
                entities,
                items
        );

        require(services.require(EchoNetworkRuntimeResult.class) == network,
                "network runtime result should be service-bound");
        require(services.require(EchoNetworkPacketRegistry.class) == network.packetRegistry(),
                "network packet registry should be service-bound");
        require(services.require(EchoLocalNetworkTransport.class) == network.transport(),
                "local network transport should be service-bound");
        require(services.require(EchoNetworkDiagnostics.class) == network.diagnostics(),
                "network diagnostics should be service-bound");
        require(services.require(EchoNetworkHandshakeService.class) == network.handshakeService(),
                "handshake service should be service-bound");
        require(services.require(EchoNetworkSyncService.class) == network.syncService(),
                "sync service should be service-bound");
        require(services.require(EchoNetworkHandshakeResult.class) == network.handshake(),
                "handshake result should be service-bound");

        require(network.protocol().protocolId().equals("echo.standalone.protocol"),
                "debug protocol id should be stable");
        require(network.protocol().version() == 1, "debug protocol version should be one");
        require(network.protocol().features().contains("entity_sync"),
                "debug protocol should advertise entity sync");
        require(network.protocol().features().contains("inventory_sync"),
                "debug protocol should advertise inventory sync");
        require(network.clientEndpoint().role() == EchoNetworkRole.CLIENT,
                "client endpoint role should be CLIENT");
        require(network.serverEndpoint().role() == EchoNetworkRole.SERVER,
                "server endpoint role should be SERVER");
        require(network.packetRegistry().count() == 5,
                "network packet registry should contain five packet types");
        require(network.packetRegistry().byKind(EchoNetworkPacketKind.HANDSHAKE).size() == 1,
                "registry should contain one handshake packet type");
        require(network.packetRegistry().byKind(EchoNetworkPacketKind.HANDSHAKE_ACK).size() == 1,
                "registry should contain one handshake ack packet type");
        require(network.packetRegistry().byKind(EchoNetworkPacketKind.ENTITY_SYNC).size() == 1,
                "registry should contain one entity sync packet type");
        require(network.packetRegistry().byKind(EchoNetworkPacketKind.INVENTORY_SYNC).size() == 1,
                "registry should contain one inventory sync packet type");
        require(network.packetRegistry().byKind(EchoNetworkPacketKind.DIAGNOSTIC).size() == 1,
                "registry should contain one diagnostic packet type");

        require(network.handshake().accepted(), "debug handshake should be accepted");
        require(network.handshake().requestPacket().sequence() == 1L,
                "handshake request should be first packet");
        require(network.handshake().responsePacket().sequence() == 2L,
                "handshake response should be second packet");
        require(network.handshake().requestPacket().payload().contains("protocol=echo.standalone.protocol"),
                "handshake request should include protocol id");
        require(network.handshake().responsePacket().payload().contains("accepted=true"),
                "handshake response should accept the session");
        require(network.transport().count() == 2,
                "handshake should send exactly two packets before sync");

        EchoNetworkSyncResult entitySync = network.syncService().syncEntities(
                entities,
                network.serverEndpoint(),
                network.clientEndpoint()
        );
        EchoNetworkSyncResult inventorySync = network.syncService().syncInventories(
                items,
                network.serverEndpoint(),
                network.clientEndpoint()
        );

        require(entitySync.snapshotCount() == 2, "entity sync should contain two snapshots");
        require(entitySync.packet().sequence() == 3L, "entity sync should be third packet");
        require(entitySync.packet().payload().contains("player-001"),
                "entity sync payload should contain player entity");
        require(entitySync.packet().payload().contains("scavenger-001"),
                "entity sync payload should contain scavenger entity");
        require(entitySync.packet().payload().contains("health=100/100"),
                "entity sync payload should include player health");
        require(inventorySync.snapshotCount() == 2,
                "inventory sync should contain two inventory snapshots");
        require(inventorySync.detailCount() == 5,
                "inventory sync should contain five item stacks");
        require(inventorySync.packet().sequence() == 4L,
                "inventory sync should be fourth packet");
        require(inventorySync.packet().payload().contains("inventory:player-001"),
                "inventory sync payload should contain player inventory");
        require(inventorySync.packet().payload().contains("container:crash-cache"),
                "inventory sync payload should contain crash cache");
        require(inventorySync.packet().payload().contains("ashfall:scavenger_blade"),
                "inventory sync payload should contain scavenger blade");

        require(network.transport().count() == 4,
                "sync should bring total sent packets to four");
        require(network.transport().packetsFor(network.clientEndpoint().endpointId()).size() == 3,
                "client should receive ack, entity sync, and inventory sync packets");
        require(network.transport().packetsFor(network.serverEndpoint().endpointId()).size() == 1,
                "server should receive the handshake request");
        require(network.diagnostics().count() == 5,
                "diagnostics should include four sends and one handshake result");
        require(network.diagnostics().errorCount() == 0,
                "debug network should have no error diagnostics");

        System.out.println("phase14.15 network runtime smoke PASS protocol="
                + network.protocol().protocolId()
                + " packets="
                + network.transport().count()
                + " registry="
                + network.packetRegistry().count()
                + " entities="
                + entitySync.snapshotCount()
                + " inventories="
                + inventorySync.snapshotCount()
                + " diagnostics="
                + network.diagnostics().count());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
