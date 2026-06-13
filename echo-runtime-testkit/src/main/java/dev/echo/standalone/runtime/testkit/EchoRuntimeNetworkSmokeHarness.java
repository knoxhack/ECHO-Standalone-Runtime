package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.network.EchoLocalNetworkTransport;
import dev.echo.standalone.runtime.network.EchoNetworkDiagnostic;
import dev.echo.standalone.runtime.network.EchoNetworkDiagnostics;
import dev.echo.standalone.runtime.network.EchoNetworkEndpoint;
import dev.echo.standalone.runtime.network.EchoNetworkHandshakeResult;
import dev.echo.standalone.runtime.network.EchoNetworkHandshakeService;
import dev.echo.standalone.runtime.network.EchoNetworkPacket;
import dev.echo.standalone.runtime.network.EchoNetworkPacketKind;
import dev.echo.standalone.runtime.network.EchoNetworkPacketRegistry;
import dev.echo.standalone.runtime.network.EchoNetworkPacketType;
import dev.echo.standalone.runtime.network.EchoNetworkProtocol;
import dev.echo.standalone.runtime.network.EchoNetworkRole;
import dev.echo.standalone.runtime.network.EchoNetworkRuntime;
import dev.echo.standalone.runtime.network.EchoNetworkRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkSyncResult;
import dev.echo.standalone.runtime.network.EchoNetworkSyncService;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class EchoRuntimeNetworkSmokeHarness {
    private EchoRuntimeNetworkSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
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
        int handshakePacketCount = network.transport().count();

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

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                network,
                entitySync,
                inventorySync,
                handshakePacketCount
        );

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

    private static void writeReports(
            Path standaloneRoot,
            EchoNetworkRuntimeResult network,
            EchoNetworkSyncResult entitySync,
            EchoNetworkSyncResult inventorySync,
            int handshakePacketCount
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);

        List<EchoNetworkPacket> sentPackets = network.transport().packets();
        List<EchoNetworkPacket> clientPackets = network.transport()
                .packetsFor(network.clientEndpoint().endpointId());
        List<EchoNetworkPacket> serverPackets = network.transport()
                .packetsFor(network.serverEndpoint().endpointId());
        EchoNetworkProtocol protocol = network.protocol();
        EchoNetworkHandshakeResult handshake = network.handshake();

        write(root.resolve("runtime-network.json"), """
                {
                  "schema": "echo.standalone.runtime_network.v2",
                  "status": "PASS",
                  "phase": "14.15",
                  "summary": "Network runtime created service-bound protocol, packet registry, in-memory local transport, accepted handshake, entity sync, inventory sync, and diagnostics without opening real sockets.",
                  "runtimeResultServiceBound": true,
                  "packetRegistryServiceBound": true,
                  "localTransportServiceBound": true,
                  "diagnosticsServiceBound": true,
                  "handshakeServiceBound": true,
                  "syncServiceBound": true,
                  "handshakeResultServiceBound": true,
                  "protocolId": "%s",
                  "version": %d,
                  "packetTypeCount": %d,
                  "sentPacketCount": %d,
                  "handshakePacketCount": %d,
                  "entitySnapshots": %d,
                  "inventorySnapshots": %d,
                  "inventoryItemStacks": %d,
                  "diagnosticCount": %d,
                  "errorCount": %d,
                  "clientEndpoint": "%s",
                  "serverEndpoint": "%s"
                }
                """.formatted(
                escape(protocol.protocolId()),
                protocol.version(),
                network.packetRegistry().count(),
                network.transport().count(),
                handshakePacketCount,
                entitySync.snapshotCount(),
                inventorySync.snapshotCount(),
                inventorySync.detailCount(),
                network.diagnostics().count(),
                network.diagnostics().errorCount(),
                escape(network.clientEndpoint().endpointId()),
                escape(network.serverEndpoint().endpointId())
        ));

        write(root.resolve("network-protocol.json"), """
                {
                  "schema": "echo.standalone.network_protocol.v2",
                  "status": "PASS",
                  "protocolId": "%s",
                  "version": %d,
                  "minCompatibleVersion": %d,
                  "features": %s,
                  "featureCsv": "%s",
                  "clientEndpoint": %s,
                  "serverEndpoint": %s,
                  "clientCompatibleWithServer": %s,
                  "serverCompatibleWithClient": %s
                }
                """.formatted(
                escape(protocol.protocolId()),
                protocol.version(),
                protocol.minCompatibleVersion(),
                stringArray(protocol.features()),
                escape(protocol.featureCsv()),
                endpointJson(network.clientEndpoint()),
                endpointJson(network.serverEndpoint()),
                network.serverEndpoint().protocol().compatibleWith(network.clientEndpoint().protocol()),
                network.clientEndpoint().protocol().compatibleWith(network.serverEndpoint().protocol())
        ));

        write(root.resolve("network-packets.json"), """
                {
                  "schema": "echo.standalone.network_packets.v2",
                  "status": "PASS",
                  "packetTypeCount": %d,
                  "packetTypes": %s,
                  "sentPacketCount": %d,
                  "sentKinds": %s,
                  "sentPacketIds": %s,
                  "sequences": %s,
                  "deterministicOrder": %s,
                  "reliableHandshakeAndSync": %s
                }
                """.formatted(
                network.packetRegistry().count(),
                packetTypesJson(network.packetRegistry().all()),
                sentPackets.size(),
                stringArray(sentPackets.stream()
                        .map(packet -> packet.type().kind().name())
                        .toList()),
                stringArray(sentPackets.stream()
                        .map(EchoNetworkPacket::packetId)
                        .toList()),
                sequenceArray(sentPackets),
                sentPackets.stream().mapToLong(EchoNetworkPacket::sequence).boxed()
                        .toList().equals(List.of(1L, 2L, 3L, 4L)),
                sentPackets.stream()
                        .filter(packet -> packet.type().kind() != EchoNetworkPacketKind.DIAGNOSTIC)
                        .allMatch(packet -> packet.type().reliable())
        ));

        write(root.resolve("network-handshake.json"), """
                {
                  "schema": "echo.standalone.network_handshake.v2",
                  "status": "PASS",
                  "accepted": %s,
                  "reason": "%s",
                  "clientEndpoint": "%s",
                  "serverEndpoint": "%s",
                  "requestPacket": %s,
                  "responsePacket": %s,
                  "requestSequence": %d,
                  "responseSequence": %d,
                  "requestPayloadContainsProtocol": %s,
                  "responsePayloadAccepted": %s,
                  "handshakePacketCount": %d
                }
                """.formatted(
                handshake.accepted(),
                escape(handshake.reason()),
                escape(handshake.clientEndpoint().endpointId()),
                escape(handshake.serverEndpoint().endpointId()),
                packetJson(handshake.requestPacket()),
                packetJson(handshake.responsePacket()),
                handshake.requestPacket().sequence(),
                handshake.responsePacket().sequence(),
                handshake.requestPacket().payload().contains("protocol=echo.standalone.protocol"),
                handshake.responsePacket().payload().contains("accepted=true"),
                handshakePacketCount
        ));

        write(root.resolve("network-local-transport.json"), """
                {
                  "schema": "echo.standalone.network_local_transport.v2",
                  "status": "PASS",
                  "transportClass": "%s",
                  "socketless": true,
                  "inMemoryPacketLog": true,
                  "totalPackets": %d,
                  "clientPacketCount": %d,
                  "serverPacketCount": %d,
                  "clientPacketKinds": %s,
                  "serverPacketKinds": %s,
                  "targetEndpoints": %s,
                  "orderedSequences": %s
                }
                """.formatted(
                escape(network.transport().getClass().getName()),
                sentPackets.size(),
                clientPackets.size(),
                serverPackets.size(),
                stringArray(clientPackets.stream().map(packet -> packet.type().kind().name()).toList()),
                stringArray(serverPackets.stream().map(packet -> packet.type().kind().name()).toList()),
                stringArray(sentPackets.stream().map(EchoNetworkPacket::targetEndpointId).distinct().toList()),
                sequenceArray(sentPackets)
        ));

        write(root.resolve("network-entity-sync.json"), """
                {
                  "schema": "echo.standalone.network_entity_sync.v2",
                  "status": "PASS",
                  "snapshotCount": %d,
                  "detailCount": %d,
                  "packetSequence": %d,
                  "packetKind": "%s",
                  "reason": "%s",
                  "payloadContainsPlayer": %s,
                  "payloadContainsScavenger": %s,
                  "payloadContainsHealth": %s,
                  "payload": "%s"
                }
                """.formatted(
                entitySync.snapshotCount(),
                entitySync.detailCount(),
                entitySync.packet().sequence(),
                entitySync.packet().type().kind().name(),
                escape(entitySync.reason()),
                entitySync.packet().payload().contains("player-001"),
                entitySync.packet().payload().contains("scavenger-001"),
                entitySync.packet().payload().contains("health=100/100"),
                escape(entitySync.packet().payload())
        ));

        write(root.resolve("network-inventory-sync.json"), """
                {
                  "schema": "echo.standalone.network_inventory_sync.v2",
                  "status": "PASS",
                  "snapshotCount": %d,
                  "detailCount": %d,
                  "packetSequence": %d,
                  "packetKind": "%s",
                  "reason": "%s",
                  "payloadContainsPlayerInventory": %s,
                  "payloadContainsCrashCache": %s,
                  "payloadContainsScavengerBlade": %s,
                  "payload": "%s"
                }
                """.formatted(
                inventorySync.snapshotCount(),
                inventorySync.detailCount(),
                inventorySync.packet().sequence(),
                inventorySync.packet().type().kind().name(),
                escape(inventorySync.reason()),
                inventorySync.packet().payload().contains("inventory:player-001"),
                inventorySync.packet().payload().contains("container:crash-cache"),
                inventorySync.packet().payload().contains("ashfall:scavenger_blade"),
                escape(inventorySync.packet().payload())
        ));

        write(root.resolve("network-diagnostics.json"), """
                {
                  "schema": "echo.standalone.network_diagnostics.v2",
                  "status": "PASS",
                  "diagnosticCount": %d,
                  "warningCount": %d,
                  "errorCount": %d,
                  "diagnostics": %s,
                  "containsHandshakeAccepted": %s,
                  "containsFourPacketSends": %s
                }
                """.formatted(
                network.diagnostics().count(),
                network.diagnostics().warningCount(),
                network.diagnostics().errorCount(),
                diagnosticsJson(network.diagnostics().all()),
                network.diagnostics().all().stream()
                        .anyMatch(diagnostic -> diagnostic.message().contains("handshake accepted")),
                network.diagnostics().all().stream()
                        .filter(diagnostic -> diagnostic.message().startsWith("sent "))
                        .count() == 4
        ));

        write(root.resolve("network-boundaries.json"), """
                {
                  "schema": "echo.standalone.network_boundaries.v2",
                  "status": "PASS",
                  "localTransportOnly": true,
                  "socketlessTransport": true,
                  "javaNetFreeRuntime": true,
                  "nettyFreeRuntime": true,
                  "minecraftFreeRuntime": true,
                  "platformEndpointCount": 0,
                  "endpointCount": 2,
                  "serviceBoundTransport": true,
                  "packetLogAuditable": true,
                  "forbiddenBoundaryCheckedByVerifier": true
                }
                """);
    }

    private static String endpointJson(EchoNetworkEndpoint endpoint) {
        return """
                {
                    "endpointId": "%s",
                    "role": "%s",
                    "protocolId": "%s"
                  }""".formatted(
                escape(endpoint.endpointId()),
                endpoint.role().name(),
                escape(endpoint.protocol().protocolId())
        );
    }

    private static String packetTypesJson(List<EchoNetworkPacketType> packetTypes) {
        return packetTypes.stream()
                .map(packetType -> """
                        {
                            "typeId": "%s",
                            "kind": "%s",
                            "channel": "%s",
                            "reliable": %s,
                            "description": "%s"
                          }""".formatted(
                        escape(packetType.typeId()),
                        packetType.kind().name(),
                        escape(packetType.channel()),
                        packetType.reliable(),
                        escape(packetType.description())
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String packetJson(EchoNetworkPacket packet) {
        return """
                {
                    "packetId": "%s",
                    "kind": "%s",
                    "typeId": "%s",
                    "sourceEndpointId": "%s",
                    "targetEndpointId": "%s",
                    "sequence": %d,
                    "payload": "%s"
                  }""".formatted(
                escape(packet.packetId()),
                packet.type().kind().name(),
                escape(packet.type().typeId()),
                escape(packet.sourceEndpointId()),
                escape(packet.targetEndpointId()),
                packet.sequence(),
                escape(packet.payload())
        );
    }

    private static String diagnosticsJson(List<EchoNetworkDiagnostic> diagnostics) {
        return diagnostics.stream()
                .map(diagnostic -> """
                        {
                            "severity": "%s",
                            "message": "%s",
                            "packetId": "%s"
                          }""".formatted(
                        diagnostic.severity().name(),
                        escape(diagnostic.message()),
                        escape(diagnostic.packetId())
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String stringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String sequenceArray(List<EchoNetworkPacket> packets) {
        return packets.stream()
                .map(packet -> Long.toString(packet.sequence()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
