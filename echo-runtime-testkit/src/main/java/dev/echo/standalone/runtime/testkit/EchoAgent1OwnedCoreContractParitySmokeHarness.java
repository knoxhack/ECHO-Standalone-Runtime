package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoPlatformCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoRuntimeGuardStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoSchemaCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoValidationCoreStandaloneAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent1OwnedCoreContractParitySmokeHarness {
    private EchoAgent1OwnedCoreContractParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path markerPath = resolveNativeMarker(workspaceRoot);
        String marker = Files.readString(markerPath, StandardCharsets.UTF_8);
        Path adapterCoreSelfParityPath = workspaceRoot.resolve(
                "Echo/reports/echo/adaptercore/echoadaptercore-registry-backend-parity-smoke.json");
        String adapterCoreSelfParity = Files.readString(adapterCoreSelfParityPath, StandardCharsets.UTF_8);
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();

        List<Map<String, Object>> modules = List.of(
                verifyAdapterCoreSelf(adapterCoreSelfParity, workspaceRoot.relativize(adapterCoreSelfParityPath)
                                .toString().replace('\\', '/'),
                        new EchoAdapterCoreStandaloneAdapter().activate()),
                verify(marker, "echocore", EchoCoreStandaloneAdapter.CONTRACT_IDS,
                        new EchoCoreStandaloneAdapter().activate(bridge)),
                verify(marker, "echoruntimeguard", EchoRuntimeGuardStandaloneAdapter.CONTRACT_IDS,
                        new EchoRuntimeGuardStandaloneAdapter().activate(bridge)),
                verify(marker, "echoplatformcore", EchoPlatformCoreStandaloneAdapter.CONTRACT_IDS,
                        new EchoPlatformCoreStandaloneAdapter().activate(bridge)),
                verify(marker, "echoschemacore", EchoSchemaCoreStandaloneAdapter.CONTRACT_IDS,
                        new EchoSchemaCoreStandaloneAdapter().activate(bridge)),
                verify(marker, "echovalidationcore", EchoValidationCoreStandaloneAdapter.CONTRACT_IDS,
                        new EchoValidationCoreStandaloneAdapter().activate(bridge))
        );
        List<Map<String, Object>> behaviorParity = modules.stream()
                .flatMap(module -> mapList(module.get("behaviorParity")).stream())
                .toList();
        int behaviorParityCheckCount = behaviorParity.stream()
                .mapToInt(EchoAgent1OwnedCoreContractParitySmokeHarness::behaviorCheckCount)
                .sum();
        int contractCount = modules.stream()
                .mapToInt(module -> ((Number) module.get("contractCount")).intValue())
                .sum();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schema", "echo.standalone.agent1_owned_core_contract_parity_smoke.v1");
        report.put("status", "PASS");
        report.put("scope", "Agent 1 owned core contract-registration parity only; deeper behavior parity remains tracked separately.");
        report.put("nativeMarkerEvidence", workspaceRoot.relativize(markerPath).toString().replace('\\', '/'));
        report.put("moduleCount", modules.size());
        report.put("contractCount", contractCount);
        report.put("behaviorParityCheckCount", behaviorParityCheckCount);
        report.put("behaviorParity", behaviorParity);
        report.put("modules", modules);

        Path reportPath = workspaceRoot.resolve(
                "Echo/echo-standalone-runtime/reports/echo/standalone/agent1-owned-core-contract-parity-smoke.json");
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, toJson(report) + "\n", StandardCharsets.UTF_8);
        System.out.println("agent1 owned core contract parity smoke PASS modules="
                + modules.size() + " contracts=" + contractCount);
    }

    private static Map<String, Object> verifyAdapterCoreSelf(
            String parityReport,
            String parityEvidence,
            Map<String, Object> standaloneActivation
    ) {
        require(Boolean.TRUE.equals(standaloneActivation.get("activated")),
                "echoadaptercore standalone adapter did not activate");
        require(Boolean.TRUE.equals(standaloneActivation.get("adapterCoreUsed")),
                "echoadaptercore standalone adapter did not use AdapterCore");
        require(Boolean.TRUE.equals(standaloneActivation.get("standaloneRuntimeCodeExecuted")),
                "echoadaptercore standalone runtime code did not execute");
        require(Boolean.TRUE.equals(standaloneActivation.get("allRuntimeAliasesRegistered")),
                "echoadaptercore standalone adapter did not register all runtime aliases");
        require(Integer.valueOf(EchoAdapterCoreStandaloneAdapter.CONTRACT_IDS.size())
                        .equals(standaloneActivation.get("logicalRegistrationCount")),
                "echoadaptercore standalone contract count mismatch");
        require(parityReport.contains("\"status\": \"PASS\""),
                "echoadaptercore registry backend parity report is not PASS");
        require(parityReport.contains("\"moduleId\": \"echoadaptercore\""),
                "echoadaptercore registry backend parity report has wrong module id");
        require(parityReport.contains("\"nativeBackend\": \"echo-native-loader\""),
                "echoadaptercore registry backend parity report missing native loader backend");
        require(parityReport.contains("\"standaloneBackend\": \"echo-standalone-runtime\""),
                "echoadaptercore registry backend parity report missing standalone backend");
        require(parityReport.contains("\"failedChecks\": []"),
                "echoadaptercore registry backend parity report has failed checks");
        int passedCheckCount = numberAfter(parityReport, "\"passedCheckCount\": ");
        require(passedCheckCount > 0,
                "echoadaptercore registry backend parity report did not execute checks");

        List<Map<String, Object>> behaviorParity = List.of(behaviorCheck(
                "echoadaptercore",
                "adaptercore_registry_backend_resolution",
                "checkCount",
                passedCheckCount,
                "nativeBackend",
                "echo-native-loader",
                "standaloneBackend",
                "echo-standalone-runtime",
                "parityEvidence",
                parityEvidence
        ));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("moduleId", "echoadaptercore");
        row.put("status", "PASS");
        row.put("scope", "contract_registration_and_backend_resolution_parity");
        row.put("contractCount", EchoAdapterCoreStandaloneAdapter.CONTRACT_IDS.size());
        row.put("contracts", EchoAdapterCoreStandaloneAdapter.CONTRACT_IDS);
        row.put("nativeActivationStage", "adaptercore_runtime_backends_parity_active");
        row.put("standaloneActivationStage", standaloneActivation.get("activationStage"));
        row.put("standaloneRuntimeCodeExecuted", true);
        row.put("nativeServiceCodeExecuted", true);
        row.put("allRuntimeAliasesRegistered", true);
        row.put("behaviorParity", behaviorParity);
        return Map.copyOf(row);
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

    private static Map<String, Object> verify(
            String marker,
            String moduleId,
            List<String> contractIds,
            Map<String, Object> standaloneActivation
    ) {
        require(Boolean.TRUE.equals(standaloneActivation.get("activated")),
                moduleId + " standalone adapter did not activate");
        require(Boolean.TRUE.equals(standaloneActivation.get("adapterCoreUsed")),
                moduleId + " standalone adapter did not use AdapterCore");
        require(Boolean.TRUE.equals(standaloneActivation.get("standaloneRuntimeCodeExecuted")),
                moduleId + " standalone runtime code did not execute");
        require(Boolean.TRUE.equals(standaloneActivation.get("allRuntimeAliasesRegistered")),
                moduleId + " standalone adapter did not register all runtime aliases");
        String nativeModule = nativeModuleSlice(marker, moduleId);
        require(nativeModule.contains("\"moduleId\": \"" + moduleId + "\""),
                "native marker missing module " + moduleId);
        require(nativeModule.contains("\"activationStage\": \"" + nativeActivationStage(moduleId) + "\""),
                "native marker missing activation stage for " + moduleId);
        require(nativeModule.contains("\"serviceCodeExecuted\": true"),
                "native marker did not record service execution for " + moduleId);
        require(nativeModule.contains("\"logicalRegistrationCount\": " + contractIds.size()),
                "native marker did not record matching contract count for " + moduleId);
        for (String contractId : contractIds) {
            require(nativeModule.contains("\"" + contractId + "\""),
                    "native marker missing contract " + contractId);
        }

        Map<String, Object> row = new LinkedHashMap<>();
        List<Map<String, Object>> behaviorParity = behaviorParityChecks(moduleId, nativeModule, standaloneActivation);
        row.put("moduleId", moduleId);
        row.put("status", "PASS");
        row.put("scope", "contract_registration_parity");
        row.put("contractCount", contractIds.size());
        row.put("contracts", contractIds);
        row.put("nativeActivationStage", nativeActivationStage(moduleId));
        row.put("standaloneActivationStage", standaloneActivation.get("activationStage"));
        row.put("standaloneRuntimeCodeExecuted", true);
        row.put("nativeServiceCodeExecuted", true);
        row.put("allRuntimeAliasesRegistered", true);
        row.put("behaviorParity", behaviorParity);
        return Map.copyOf(row);
    }

    private static List<Map<String, Object>> behaviorParityChecks(
            String moduleId,
            String nativeModule,
            Map<String, Object> standaloneActivation
    ) {
        if ("echocore".equals(moduleId)) {
            boolean nativeServiceRegistryRoundTrip = booleanAfter(nativeModule, "\"serviceRegistryRoundTrip\": ");
            require(Boolean.valueOf(nativeServiceRegistryRoundTrip).equals(standaloneActivation.get("serviceRegistryRoundTrip")),
                    "EchoCore native/standalone service registry round trip mismatch");
            require(nativeServiceRegistryRoundTrip,
                    "EchoCore service registry round trip must execute successfully in both runtimes");
            return List.of(behaviorCheck(
                    moduleId,
                    "service_registry_round_trip",
                    "serviceRegistryRoundTrip",
                    nativeServiceRegistryRoundTrip
            ));
        }
        if ("echoruntimeguard".equals(moduleId)) {
            int nativeCalledCallbacks = numberAfter(nativeModule, "\"calledCallbackCount\": ");
            int nativeExpectedCallbacks = numberAfter(nativeModule, "\"expectedCallbackCount\": ");
            boolean nativeAllRequiredCallbacksCalled = booleanAfter(nativeModule, "\"allRequiredCallbacksCalled\": ");
            require(Integer.valueOf(nativeCalledCallbacks).equals(standaloneActivation.get("calledCallbackCount")),
                    "RuntimeGuard native/standalone called lifecycle callback count mismatch");
            require(Integer.valueOf(nativeExpectedCallbacks).equals(standaloneActivation.get("expectedCallbackCount")),
                    "RuntimeGuard native/standalone expected lifecycle callback count mismatch");
            require(Boolean.valueOf(nativeAllRequiredCallbacksCalled)
                            .equals(standaloneActivation.get("allRequiredCallbacksCalled")),
                    "RuntimeGuard native/standalone lifecycle callback completion mismatch");
            require(nativeAllRequiredCallbacksCalled,
                    "RuntimeGuard native lifecycle dispatch did not call every callback");
            require(Boolean.TRUE.equals(standaloneActivation.get("allRequiredCallbacksCalled")),
                    "RuntimeGuard standalone lifecycle dispatch did not call every callback");
            require(nativeCalledCallbacks == nativeExpectedCallbacks,
                    "RuntimeGuard native lifecycle dispatch did not call every callback");
            return List.of(behaviorCheck(
                    moduleId,
                    "runtimeguard_lifecycle_callback_sequence",
                    "calledCallbackCount",
                    nativeCalledCallbacks,
                    "expectedCallbackCount",
                    nativeExpectedCallbacks,
                    "allRequiredCallbacksCalled",
                    nativeAllRequiredCallbacksCalled
            ));
        }
        if ("echoplatformcore".equals(moduleId)) {
            int nativeFeatureCount = numberAfter(nativeModule, "\"platformFeatureCount\": ");
            int nativePermissionCount = numberAfter(nativeModule, "\"platformPermissionCount\": ");
            require(Integer.valueOf(nativeFeatureCount).equals(standaloneActivation.get("platformFeatureCount")),
                    "PlatformCore native/standalone platform feature count mismatch");
            require(Integer.valueOf(nativePermissionCount).equals(standaloneActivation.get("platformPermissionCount")),
                    "PlatformCore native/standalone platform permission count mismatch");
            return List.of(behaviorCheck(
                    moduleId,
                    "platform_catalog_counts",
                    "platformFeatureCount",
                    nativeFeatureCount,
                    "platformPermissionCount",
                    nativePermissionCount
            ));
        }
        if ("echoschemacore".equals(moduleId)) {
            int nativeSchemaCount = numberAfter(nativeModule, "\"builtinSchemaCount\": ");
            int standaloneSchemaCount = ((Number) standaloneActivation.get("builtinSchemaCount")).intValue();
            List<String> nativeSchemaKinds = stringsInArray(nativeModule, "schemaKinds");
            List<?> standaloneSchemaKinds = list(standaloneActivation.get("schemaKinds"));
            require(nativeSchemaCount == standaloneSchemaCount,
                    "SchemaCore native/standalone schema descriptor count mismatch");
            require(nativeSchemaKinds.equals(standaloneSchemaKinds),
                    "SchemaCore native/standalone schema kind catalog mismatch");
            return List.of(behaviorCheck(
                    moduleId,
                    "schema_descriptor_catalog",
                    "builtinSchemaCount",
                    nativeSchemaCount,
                    "schemaKinds",
                    nativeSchemaKinds
            ));
        }
        if ("echovalidationcore".equals(moduleId)) {
            int nativeRuleCount = numberAfter(nativeModule, "\"validationRuleCount\": ");
            int nativeDiagnosticCount = numberAfter(nativeModule, "\"diagnosticCount\": ");
            String nativeHighestSeverity = stringAfter(nativeModule, "\"highestSeverity\": \"");
            require(Integer.valueOf(nativeRuleCount).equals(standaloneActivation.get("validationRuleCount")),
                    "ValidationCore native/standalone validation rule count mismatch");
            require(Integer.valueOf(nativeDiagnosticCount).equals(standaloneActivation.get("diagnosticCount")),
                    "ValidationCore native/standalone diagnostic count mismatch");
            require(nativeHighestSeverity.equals(standaloneActivation.get("highestSeverity")),
                    "ValidationCore native/standalone highest severity mismatch");
            return List.of(behaviorCheck(
                    moduleId,
                    "clean_validation_report",
                    "validationRuleCount",
                    nativeRuleCount,
                    "diagnosticCount",
                    nativeDiagnosticCount,
                    "highestSeverity",
                    nativeHighestSeverity
            ));
        }
        return List.of();
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
        row.put("scope", "native_marker_vs_standalone_reference_behavior");
        row.put("evidence", evidence);
        return Map.copyOf(row);
    }

    private static String nativeModuleSlice(String marker, String moduleId) {
        int moduleIndex = marker.indexOf("\"moduleId\": \"" + moduleId + "\"");
        require(moduleIndex >= 0, "native marker missing module " + moduleId);
        int start = marker.lastIndexOf("\n    {", moduleIndex);
        if (start < 0) {
            start = marker.lastIndexOf('{', moduleIndex);
        }
        int nextActivation = marker.indexOf("\n    {\n      \"activated\"", start + 1);
        int end = nextActivation >= 0 ? nextActivation : marker.length();
        return marker.substring(Math.max(0, start), Math.max(start, end));
    }

    private static String nativeActivationStage(String moduleId) {
        return switch (moduleId) {
            case "echocore" -> "echocore_native_contract_active";
            case "echoruntimeguard" -> "runtimeguard_native_contract_active";
            case "echoplatformcore" -> "platformcore_native_contract_active";
            case "echoschemacore" -> "schemacore_native_contract_active";
            case "echovalidationcore" -> "validationcore_native_contract_active";
            default -> throw new IllegalArgumentException("unsupported Agent 1 module: " + moduleId);
        };
    }

    private static int numberAfter(String text, String marker) {
        int index = text.indexOf(marker);
        require(index >= 0, "missing numeric marker " + marker);
        int start = index + marker.length();
        int end = start;
        while (end < text.length() && Character.isDigit(text.charAt(end))) {
            end++;
        }
        return Integer.parseInt(text.substring(start, end));
    }

    private static String stringAfter(String text, String marker) {
        int index = text.indexOf(marker);
        require(index >= 0, "missing string marker " + marker);
        int start = index + marker.length();
        int end = text.indexOf('"', start);
        require(end >= start, "unterminated string marker " + marker);
        return text.substring(start, end);
    }

    private static boolean booleanAfter(String text, String marker) {
        int index = text.indexOf(marker);
        require(index >= 0, "missing boolean marker " + marker);
        int start = index + marker.length();
        if (text.startsWith("true", start)) {
            return true;
        }
        if (text.startsWith("false", start)) {
            return false;
        }
        throw new AssertionError("invalid boolean marker " + marker);
    }

    private static List<String> stringsInArray(String text, String key) {
        String marker = "\"" + key + "\": [";
        int start = text.indexOf(marker);
        require(start >= 0, "missing array marker " + key);
        int arrayStart = start + marker.length();
        int arrayEnd = text.indexOf(']', arrayStart);
        require(arrayEnd >= arrayStart, "unterminated array marker " + key);
        String array = text.substring(arrayStart, arrayEnd);
        return java.util.regex.Pattern.compile("\"([^\"]+)\"")
                .matcher(array)
                .results()
                .map(match -> match.group(1))
                .toList();
    }

    private static List<?> list(Object value) {
        return value instanceof List<?> list ? list : List.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(Map.class::isInstance)
                .map(item -> (Map<String, Object>) item)
                .toList();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static int behaviorCheckCount(Map<String, Object> behavior) {
        Object evidenceValue = behavior.get("evidence");
        if (evidenceValue instanceof Map<?, ?> evidence
                && evidence.get("checkCount") instanceof Number checkCount) {
            return checkCount.intValue();
        }
        return 1;
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
