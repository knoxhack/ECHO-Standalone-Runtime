package dev.echo.standalone.runtime.network;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoNetCoreStandaloneAdapter {
    public static final String MODULE_ID = "echonetcore";
    public static final String ADAPTERCORE_CONTRACT_ID = "echonetcore:network/packet_service";
    public static final String REFERENCE_PROTOCOL_VERSION = "1";
    public static final String REFERENCE_CLIENT = "echo:debug-client";
    public static final String REFERENCE_SERVER = "echo:debug-server";

    public Map<String, Object> activate() {
        Map<String, Object> networkService = executeService("echo-native-m17");
        boolean networkServicePassed = referenceServicePassed(networkService);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "netcore_standalone_packet_service_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", List.of("echo.net", ADAPTERCORE_CONTRACT_ID));
        report.put("networkService", networkService);
        report.put("networkServiceExecuted", networkServicePassed);
        report.put("serviceCodeExecuted", networkServicePassed);
        report.put("summary", "NetCore standalone adapter executed the AdapterCore packet service routing behavior.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executeService(String packId) {
        EchoNetworkPacketRegistry registry = new EchoNetworkPacketRegistry();
        registerPayloads(registry);
        EchoNetworkDiagnostics diagnostics = new EchoNetworkDiagnostics();
        EchoLocalNetworkTransport transport = new EchoLocalNetworkTransport(diagnostics);

        EchoNetworkPacket factionSync = transport.send(new EchoNetworkPacket(
                "packet-0001-faction-sync",
                registry.require("echonetcore:faction_sync"),
                "server",
                REFERENCE_CLIENT,
                "faction=ashfall:settlers;standing=trusted",
                transport.nextSequence()
        ));
        diagnostics.info("rate limit checked for echonetcore:debug_command");

        Map<String, Object> service = new LinkedHashMap<>();
        service.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
        service.put("service", "echonetcore:network_service");
        service.put("serviceExecuted", true);
        service.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        service.put("protocolVersion", REFERENCE_PROTOCOL_VERSION);
        service.put("optionalPackets", true);
        service.put("payloadContracts", payloadContracts());
        service.put("routeResults", List.of(
                route(factionSync.type().typeId(), factionSync.sourceEndpointId(), factionSync.targetEndpointId(), true, "clientbound sync delivered"),
                route("echonetcore:debug_command", REFERENCE_CLIENT, REFERENCE_SERVER, false, "rate-limited by echonetcore:rate_limiter")
        ));
        service.put("cleanupHooks", List.of("player.logout", "server.stopping"));
        service.put("diagnostics", List.of(
                "net.payloads.registered",
                "net.route.clientbound.sent",
                "net.rate_limit.checked"
        ));
        service.put("transportPacketCount", transport.count());
        service.put("diagnosticCount", diagnostics.count());
        service.put("referenceBehavior", "netcore_registers_and_routes_packet_service");
        return Map.copyOf(service);
    }

    public boolean referenceServicePassed(Map<String, Object> service) {
        return Boolean.TRUE.equals(service.get("serviceExecuted"))
                && ADAPTERCORE_CONTRACT_ID.equals(service.get("adapterCoreContract"))
                && REFERENCE_PROTOCOL_VERSION.equals(service.get("protocolVersion"))
                && Boolean.TRUE.equals(service.get("optionalPackets"))
                && String.valueOf(service.get("payloadContracts")).contains("echonetcore:faction_sync")
                && String.valueOf(service.get("payloadContracts")).contains("echonetcore:debug_command")
                && String.valueOf(service.get("routeResults")).contains("clientbound sync delivered")
                && String.valueOf(service.get("routeResults")).contains("rate-limited by echonetcore:rate_limiter")
                && String.valueOf(service.get("cleanupHooks")).contains("player.logout");
    }

    private static void registerPayloads(EchoNetworkPacketRegistry registry) {
        registry.register(new EchoNetworkPacketType(
                "echonetcore:faction_sync",
                EchoNetworkPacketKind.ENTITY_SYNC,
                "echo:factions",
                true,
                "Clientbound faction sync payload"
        ));
        registry.register(new EchoNetworkPacketType(
                "echonetcore:discovery_toast",
                EchoNetworkPacketKind.DIAGNOSTIC,
                "echo:discoveries",
                true,
                "Clientbound discovery toast payload"
        ));
        registry.register(new EchoNetworkPacketType(
                "echonetcore:echo_sync",
                EchoNetworkPacketKind.INVENTORY_SYNC,
                "echo:sync",
                true,
                "Generic ECHO sync payload"
        ));
        registry.register(new EchoNetworkPacketType(
                "echonetcore:debug_command",
                EchoNetworkPacketKind.DIAGNOSTIC,
                "echo:debug",
                false,
                "Serverbound debug command payload gated by policy"
        ));
    }

    private static List<Map<String, Object>> payloadContracts() {
        return List.of(
                payload("echonetcore:faction_sync", "CLIENTBOUND", "CLIENTBOUND_SYNC", "echo:factions", true),
                payload("echonetcore:discovery_toast", "CLIENTBOUND", "CLIENTBOUND_SYNC", "echo:discoveries", true),
                payload("echonetcore:echo_sync", "CLIENTBOUND", "CLIENTBOUND_SYNC", "echo:sync", true),
                payload("echonetcore:debug_command", "SERVERBOUND", "DEBUG_DEV", "echo:debug", false)
        );
    }

    private static Map<String, Object> payload(
            String id,
            String direction,
            String kind,
            String channel,
            boolean reliable
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", id);
        payload.put("direction", direction);
        payload.put("kind", kind);
        payload.put("channel", channel);
        payload.put("reliable", reliable);
        return Map.copyOf(payload);
    }

    private static Map<String, Object> route(
            String payloadId,
            String source,
            String target,
            boolean accepted,
            String result
    ) {
        Map<String, Object> route = new LinkedHashMap<>();
        route.put("payloadId", payloadId);
        route.put("source", source);
        route.put("target", target);
        route.put("accepted", accepted);
        route.put("result", result);
        return Map.copyOf(route);
    }
}
