package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAgentCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoCommunityBridgeContracts;
import dev.echo.standalone.runtime.compat.EchoModuleGraphStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoNeutralContractCoresStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoRemainingSystemsStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoReportCoreStandaloneAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent1RemainingSystemContractParitySmokeHarness {
    private EchoAgent1RemainingSystemContractParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path markerPath = resolveNativeMarker(workspaceRoot);
        String marker = Files.readString(markerPath, StandardCharsets.UTF_8);
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();

        List<Map<String, Object>> modules = new ArrayList<>();
        modules.add(verifyActivationAdapter(workspaceRoot, marker, bridge,
                "echoagentcore",
                EchoAgentCoreStandaloneAdapter.CONTRACT_IDS,
                "addons/echoagentcore/src/main/java/com/knoxhack/echo/agentcore/EchoAgentCoreNativeModule.java",
                new EchoAgentCoreStandaloneAdapter().activate(bridge)));
        modules.add(verifyCommunityBridge(workspaceRoot, marker, bridge));
        modules.add(verifyActivationAdapter(workspaceRoot, marker, bridge,
                "echomodulegraph",
                EchoModuleGraphStandaloneAdapter.CONTRACT_IDS,
                "addons/echomodulegraph/src/main/java/com/knoxhack/echo/modulegraph/EchoModuleGraphNativeModule.java",
                new EchoModuleGraphStandaloneAdapter().activate(bridge)));
        modules.add(verifyActivationAdapter(workspaceRoot, marker, bridge,
                "echoreportcore",
                EchoReportCoreStandaloneAdapter.CONTRACT_IDS,
                "addons/echoreportcore/src/main/java/com/knoxhack/echo/reportcore/EchoReportCoreNativeModule.java",
                new EchoReportCoreStandaloneAdapter().activate(bridge)));

        Map<String, List<String>> neutralContracts = contractsByModule(
                EchoNeutralContractCoresStandaloneAdapter.CONTRACTS.stream()
                        .map(spec -> Map.entry(spec.moduleId(), spec.contentId()))
                        .toList());
        Map<String, Object> neutralActivation =
                new EchoNeutralContractCoresStandaloneAdapter().activate(bridge);
        modules.add(verifyBatchAdapter(workspaceRoot, marker, bridge, neutralActivation, "echoeventcore",
                neutralContracts.get("echoeventcore"),
                "addons/echoeventcore/src/main/java/com/knoxhack/echo/eventcore/EchoEventCoreNativeModule.java"));
        modules.add(verifyBatchAdapter(workspaceRoot, marker, bridge, neutralActivation, "echoguidecore",
                neutralContracts.get("echoguidecore"),
                "addons/echoguidecore/src/main/java/com/knoxhack/echo/guidecore/EchoGuideCoreNativeModule.java"));
        modules.add(verifyBatchAdapter(workspaceRoot, marker, bridge, neutralActivation, "echonotificationcore",
                neutralContracts.get("echonotificationcore"),
                "addons/echonotificationcore/src/main/java/com/knoxhack/echo/notificationcore/EchoNotificationCoreNativeModule.java"));

        Map<String, List<String>> remainingContracts = contractsByModule(
                EchoRemainingSystemsStandaloneAdapter.CONTRACTS.stream()
                        .map(spec -> Map.entry(spec.moduleId(), spec.contentId()))
                        .toList());
        Map<String, Object> remainingActivation =
                new EchoRemainingSystemsStandaloneAdapter().activate(bridge);
        modules.add(verifyBatchAdapter(workspaceRoot, marker, bridge, remainingActivation, "echoprogressioncore",
                remainingContracts.get("echoprogressioncore"),
                "addons/echoprogressioncore/src/main/java/com/knoxhack/echo/progressioncore/EchoProgressionCoreNativeModule.java"));
        modules.add(verifyBatchAdapter(workspaceRoot, marker, bridge, remainingActivation, "echoscriptcore",
                remainingContracts.get("echoscriptcore"),
                "addons/echoscriptcore/src/main/java/com/knoxhack/echo/scriptcore/EchoScriptCoreNativeModule.java"));
        modules.add(verifyBatchAdapter(workspaceRoot, marker, bridge, remainingActivation, "echotutorialcore",
                remainingContracts.get("echotutorialcore"),
                "addons/echotutorialcore/src/main/java/com/knoxhack/echotutorialcore/EchoTutorialCoreNativeModule.java"));

        List<Map<String, Object>> behaviorParity = modules.stream()
                .map(module -> behaviorCheck(
                        String.valueOf(module.get("moduleId")),
                        "native_activation_marker_and_standalone_adaptercore_contract_registration",
                        "checkCount",
                        module.get("contractCount"),
                        "nativeSourceEvidence",
                        module.get("nativeSourceEvidence"),
                        "nativeMarkerActivated",
                        module.get("nativeMarkerActivated"),
                        "standaloneRuntimeCodeExecuted",
                        module.get("standaloneRuntimeCodeExecuted")))
                .toList();
        int contractCount = modules.stream()
                .mapToInt(module -> ((Number) module.get("contractCount")).intValue())
                .sum();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schema", "echo.standalone.agent1_remaining_system_contract_parity_smoke.v1");
        report.put("status", "PASS");
        report.put("scope", "Agent 1 strict parity evidence for remaining system modules with native activation marker, native source service contracts, and standalone AdapterCore execution.");
        report.put("nativeMarkerEvidence", workspaceRoot.relativize(markerPath).toString().replace('\\', '/'));
        report.put("moduleCount", modules.size());
        report.put("contractCount", contractCount);
        report.put("behaviorParityCheckCount", contractCount);
        report.put("behaviorParity", behaviorParity);
        report.put("modules", modules);

        Path reportPath = workspaceRoot.resolve(
                "Echo/echo-standalone-runtime/reports/echo/standalone/agent1-remaining-system-contract-parity-smoke.json");
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, toJson(report) + "\n", StandardCharsets.UTF_8);
        System.out.println("agent1 remaining system contract parity smoke PASS modules="
                + modules.size() + " contracts=" + contractCount);
    }

    private static Map<String, Object> verifyActivationAdapter(
            Path workspaceRoot,
            String marker,
            EchoAdapterCoreStandaloneContentBridge bridge,
            String moduleId,
            List<String> contractIds,
            String nativeSourceRel,
            Map<String, Object> standaloneActivation
    ) throws IOException {
        require(Boolean.TRUE.equals(standaloneActivation.get("activated")),
                moduleId + " standalone adapter did not activate");
        require(Boolean.TRUE.equals(standaloneActivation.get("standaloneRuntimeCodeExecuted")),
                moduleId + " standalone runtime code did not execute");
        require(Boolean.TRUE.equals(standaloneActivation.get("allRuntimeAliasesRegistered")),
                moduleId + " standalone adapter did not register every runtime alias");
        require(Integer.valueOf(contractIds.size()).equals(standaloneActivation.get("logicalRegistrationCount")),
                moduleId + " standalone contract count mismatch");
        return verifyModule(workspaceRoot, marker, bridge, moduleId, contractIds, nativeSourceRel);
    }

    private static Map<String, Object> verifyCommunityBridge(
            Path workspaceRoot,
            String marker,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) throws IOException {
        Map<String, Object> probe = EchoCommunityBridgeContracts.referenceProbe();
        require(EchoCommunityBridgeContracts.referenceProbePassed(probe),
                "CommunityBridge standalone reference probe failed");
        require(EchoCommunityBridgeContracts.launcherChatLine("launcher", "User", "/op Knox").isEmpty(),
                "CommunityBridge standalone bridge must block slash-command relay");
        return verifyModule(workspaceRoot, marker, bridge, "echocommunitybridge",
                EchoCommunityBridgeContracts.CONTRACT_IDS,
                "addons/echocommunitybridge/src/main/java/com/knoxhack/echocommunitybridge/EchoCommunityBridgeNativeModule.java");
    }

    private static Map<String, Object> verifyBatchAdapter(
            Path workspaceRoot,
            String marker,
            EchoAdapterCoreStandaloneContentBridge bridge,
            Map<String, Object> batchActivation,
            String moduleId,
            List<String> contractIds,
            String nativeSourceRel
    ) throws IOException {
        require(Boolean.TRUE.equals(batchActivation.get("activated")),
                moduleId + " batch standalone adapter did not activate");
        require(Boolean.TRUE.equals(batchActivation.get("standaloneRuntimeCodeExecuted")),
                moduleId + " batch standalone runtime code did not execute");
        require(Boolean.TRUE.equals(batchActivation.get("allRuntimeAliasesRegistered")),
                moduleId + " batch standalone adapter did not register every runtime alias");
        require(Boolean.TRUE.equals(batchActivation.get("featureContractRoundTrip")),
                moduleId + " batch standalone adapter did not exercise feature contracts");
        return verifyModule(workspaceRoot, marker, bridge, moduleId, contractIds, nativeSourceRel);
    }

    private static Map<String, Object> verifyModule(
            Path workspaceRoot,
            String marker,
            EchoAdapterCoreStandaloneContentBridge bridge,
            String moduleId,
            List<String> contractIds,
            String nativeSourceRel
    ) throws IOException {
        require(contractIds != null && !contractIds.isEmpty(),
                moduleId + " has no standalone contract ids");
        String markerSlice = nativeMarkerSlice(marker, moduleId);
        require(markerSlice.contains("\"nativeModuleActivated\": true"),
                "native marker did not activate " + moduleId);
        String nativeModule = nativeActivationSlice(marker, moduleId);
        require(nativeModule.contains("\"moduleId\": \"" + moduleId + "\""),
                "native activation marker missing module " + moduleId);
        require(nativeModule.contains("\"serviceCodeExecuted\": true"),
                "native activation marker did not record service execution for " + moduleId);
        require(nativeModule.contains("\"logicalRegistrationCount\": " + contractIds.size()),
                "native activation marker did not record matching contract count for " + moduleId);

        Path nativeSource = workspaceRoot.resolve("Echo").resolve(nativeSourceRel).normalize();
        String source = readNativeModuleJavaText(workspaceRoot, nativeSourceRel);
        require(source.contains("serviceCodeExecuted"),
                moduleId + " native source does not record service code execution");
        require(source.contains("activateNative"),
                moduleId + " native source does not expose activateNative");
        for (String contractId : contractIds) {
            require(source.contains(contractId),
                    moduleId + " native source missing contract " + contractId);
            EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contractId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contractId + " has unregistered runtime alias " + runtimeKind.adapterId());
            }
        }

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", moduleId);
        row.put("status", "PASS");
        row.put("scope", "native_activation_marker_source_contracts_vs_standalone_adaptercore_contract_registration");
        row.put("contractCount", contractIds.size());
        row.put("contracts", contractIds);
        row.put("nativeMarkerActivated", true);
        row.put("nativeSourceServiceCodeDeclared", true);
        row.put("nativeServiceCodeExecuted", true);
        row.put("nativeSourceEvidence", "Echo/" + nativeSourceRel);
        row.put("standaloneRuntimeCodeExecuted", true);
        row.put("allRuntimeAliasesRegistered", true);
        return Map.copyOf(row);
    }

    private static String readNativeModuleJavaText(Path workspaceRoot, String nativeSourceRel) throws IOException {
        Path nativeSource = workspaceRoot.resolve("Echo").resolve(nativeSourceRel).normalize();
        String normalized = nativeSourceRel.replace('\\', '/');
        int addonEnd = normalized.indexOf("/src/main/java/");
        require(addonEnd > 0, "native source path is not inside an addon source tree: " + nativeSourceRel);
        Path sourceRoot = workspaceRoot.resolve("Echo").resolve(normalized.substring(0, addonEnd + "/src/main/java".length())).normalize();
        StringBuilder text = new StringBuilder(Files.readString(nativeSource, StandardCharsets.UTF_8));
        try (var stream = Files.walk(sourceRoot)) {
            for (Path file : stream
                    .filter(path -> path.toString().endsWith(".java"))
                    .sorted()
                    .toList()) {
                if (!file.equals(nativeSource)) {
                    text.append('\n').append(Files.readString(file, StandardCharsets.UTF_8));
                }
            }
        }
        return text.toString();
    }

    private static Map<String, List<String>> contractsByModule(List<Map.Entry<String, String>> entries) {
        Map<String, List<String>> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : entries) {
            out.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue());
        }
        return out;
    }

    private static Path resolveNativeMarker(Path workspaceRoot) {
        List<Path> candidates = List.of(
                workspaceRoot.resolve("Echo/echo-native-platform/fixtures/ashfall/isolated-runtime/game/echo-native/module-activation.json"),
                workspaceRoot.resolve("Echo/tmp/native-bootstrap-smoke/module-activation.json")
        );
        return candidates.stream()
                .filter(Files::exists)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("missing native module activation marker"));
    }

    private static String nativeMarkerSlice(String marker, String moduleId) {
        int moduleIndex = marker.indexOf("\"id\": \"" + moduleId + "\"");
        require(moduleIndex >= 0, "native marker missing module " + moduleId);
        int start = marker.lastIndexOf("\n    {", moduleIndex);
        if (start < 0) {
            start = marker.lastIndexOf('{', moduleIndex);
        }
        int nextActivation = marker.indexOf("\n    {", start + 1);
        int end = nextActivation >= 0 ? nextActivation : marker.length();
        return marker.substring(Math.max(0, start), Math.max(start, end));
    }

    private static String nativeActivationSlice(String marker, String moduleId) {
        int moduleIndex = marker.indexOf("\"moduleId\": \"" + moduleId + "\"");
        require(moduleIndex >= 0, "native activation marker missing module " + moduleId);
        int start = marker.lastIndexOf("\n    {", moduleIndex);
        if (start < 0) {
            start = marker.lastIndexOf('{', moduleIndex);
        }
        int nextActivation = marker.indexOf("\n    {\n      \"activated\"", start + 1);
        int end = nextActivation >= 0 ? nextActivation : marker.length();
        return marker.substring(Math.max(0, start), Math.max(start, end));
    }

    private static Map<String, Object> behaviorCheck(
            String moduleId,
            String behavior,
            Object... evidencePairs
    ) {
        Map<String, Object> evidence = new LinkedHashMap<>();
        for (int index = 0; index < evidencePairs.length; index += 2) {
            evidence.put(String.valueOf(evidencePairs[index]), evidencePairs[index + 1]);
        }
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", moduleId);
        row.put("behavior", behavior);
        row.put("status", "PASS");
        row.put("scope", "native_marker_and_source_vs_standalone_adaptercore_contract_parity");
        row.put("evidence", evidence);
        return Map.copyOf(row);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                out.append('\n')
                        .append("  ")
                        .append(toJson(String.valueOf(entry.getKey())))
                        .append(": ")
                        .append(indent(toJson(entry.getValue())));
                first = false;
            }
            if (!map.isEmpty()) {
                out.append('\n');
            }
            return out.append('}').toString();
        }
        if (value instanceof Collection<?> collection) {
            StringBuilder out = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    out.append(',');
                }
                out.append('\n').append("  ").append(indent(toJson(item)));
                first = false;
            }
            if (!collection.isEmpty()) {
                out.append('\n');
            }
            return out.append(']').toString();
        }
        return toJson(String.valueOf(value));
    }

    private static String indent(String value) {
        return value.replace("\n", "\n  ");
    }
}
