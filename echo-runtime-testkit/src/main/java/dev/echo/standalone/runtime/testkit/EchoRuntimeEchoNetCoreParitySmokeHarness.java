package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.network.EchoNetCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoNetCoreParitySmokeHarness {
    private EchoRuntimeEchoNetCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeService = executeNativeReferenceService("echo-native-m17");
        EchoNetCoreStandaloneAdapter standaloneAdapter = new EchoNetCoreStandaloneAdapter();
        Map<String, Object> standaloneService = standaloneAdapter.executeService("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceServicePassed(nativeService), "native NetCore reference service should pass");
        require(standaloneAdapter.referenceServicePassed(standaloneService), "standalone NetCore service should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("networkServiceExecuted")),
                "standalone activation should execute network service");
        require(nativeService.get("adapterCoreContract").equals(standaloneService.get("adapterCoreContract")),
                "native and standalone network contracts should match");
        require(nativeService.get("protocolVersion").equals(standaloneService.get("protocolVersion")),
                "native and standalone protocol versions should match");
        require(nativeService.get("optionalPackets").equals(standaloneService.get("optionalPackets")),
                "native and standalone optional packet settings should match");
        require(nativeService.get("payloadContracts").equals(standaloneService.get("payloadContracts")),
                "native and standalone payload contracts should match");
        require(nativeService.get("routeResults").equals(standaloneService.get("routeResults")),
                "native and standalone route results should match");
        require(nativeService.get("cleanupHooks").equals(standaloneService.get("cleanupHooks")),
                "native and standalone cleanup hooks should match");
        require(nativeService.get("diagnostics").equals(standaloneService.get("diagnostics")),
                "native and standalone diagnostics should match");
        require(Integer.valueOf(1).equals(standaloneService.get("transportPacketCount")),
                "standalone network service should route one packet through local transport");

        System.out.println("echonetcore parity smoke PASS contract="
                + nativeService.get("adapterCoreContract")
                + " payloads="
                + ((List<?>) nativeService.get("payloadContracts")).size()
                + " routes="
                + ((List<?>) nativeService.get("routeResults")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceService(String packId) {
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("adapterCoreContract", EchoNetCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        service.put("service", "echonetcore:network_service");
        service.put("serviceExecuted", true);
        service.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        service.put("protocolVersion", EchoNetCoreStandaloneAdapter.REFERENCE_PROTOCOL_VERSION);
        service.put("optionalPackets", true);
        service.put("payloadContracts", payloadContracts());
        service.put("routeResults", List.of(
                route("echonetcore:faction_sync", "server", EchoNetCoreStandaloneAdapter.REFERENCE_CLIENT, true, "clientbound sync delivered"),
                route("echonetcore:debug_command", EchoNetCoreStandaloneAdapter.REFERENCE_CLIENT, EchoNetCoreStandaloneAdapter.REFERENCE_SERVER, false, "rate-limited by echonetcore:rate_limiter")
        ));
        service.put("cleanupHooks", List.of("player.logout", "server.stopping"));
        service.put("diagnostics", List.of(
                "net.payloads.registered",
                "net.route.clientbound.sent",
                "net.rate_limit.checked"
        ));
        service.put("referenceBehavior", "netcore_registers_and_routes_packet_service");
        return Map.copyOf(service);
    }

    private static boolean nativeReferenceServicePassed(Map<String, Object> service) {
        return Boolean.TRUE.equals(service.get("serviceExecuted"))
                && EchoNetCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(service.get("adapterCoreContract"))
                && EchoNetCoreStandaloneAdapter.REFERENCE_PROTOCOL_VERSION.equals(service.get("protocolVersion"))
                && Boolean.TRUE.equals(service.get("optionalPackets"))
                && String.valueOf(service.get("payloadContracts")).contains("echonetcore:faction_sync")
                && String.valueOf(service.get("payloadContracts")).contains("echonetcore:debug_command")
                && String.valueOf(service.get("routeResults")).contains("clientbound sync delivered")
                && String.valueOf(service.get("routeResults")).contains("rate-limited by echonetcore:rate_limiter")
                && String.valueOf(service.get("cleanupHooks")).contains("player.logout");
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
