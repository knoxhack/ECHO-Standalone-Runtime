package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.gameplay.EchoStandaloneRuntimeAdapterCoreGameplayBridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeAdapterCoreGameplayBridgeParitySmokeHarness {
    private EchoRuntimeAdapterCoreGameplayBridgeParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path markerPath = workspaceRoot.resolve("Echo/tmp/native-bootstrap-smoke/module-activation.json");
        String marker = Files.readString(markerPath, StandardCharsets.UTF_8);

        List<Map<String, Object>> standalone = EchoStandaloneRuntimeAdapterCoreGameplayBridge.executeAll("ashfall");
        require(standalone.size() == EchoStandaloneRuntimeAdapterCoreGameplayBridge.REQUIRED_EVENTS.size(),
                "standalone bridge should execute all required AdapterCore gameplay events");

        for (String event : EchoStandaloneRuntimeAdapterCoreGameplayBridge.REQUIRED_EVENTS) {
            String contract = "adaptercore.gameplay_handler." + event;
            Map<String, Object> execution = standalone.stream()
                    .filter(row -> event.equals(row.get("event")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("missing standalone execution for " + event));
            require(Boolean.TRUE.equals(execution.get("executed")),
                    "standalone execution did not run for " + event);
            require(Boolean.TRUE.equals(execution.get("liveGameplayHookVerified")),
                    "standalone execution was not marked verified for " + event);
            require(contract.equals(execution.get("adapterCoreContract")),
                    "standalone contract mismatch for " + event);
            require(marker.contains("\"adapterCoreContract\": \"" + contract + "\""),
                    "native marker missing matching AdapterCore contract " + contract);
            require(marker.contains("\"handler\": \"AshfallAdapterCoreGameplayHandlers." + event + "\""),
                    "native marker missing Ashfall native handler for " + event);
            require(marker.contains("\"standaloneRuntimeBackend\": \"EchoStandaloneRuntimeAdapterCoreGameplayBridge."
                            + event + "\""),
                    "native marker missing standalone backend descriptor for " + event);
        }

        Path reportPath = workspaceRoot.resolve(
                "Echo/echo-standalone-runtime/reports/echo/standalone/adaptercore-gameplay-bridge-parity-smoke.json");
        Files.createDirectories(reportPath.getParent());
        Files.writeString(reportPath, toJson(Map.of(
                "schema", "echo.standalone.adaptercore_gameplay_bridge_parity_smoke.v1",
                "status", "PASS",
                "packId", "ashfall",
                "nativeMarkerEvidence", workspaceRoot.relativize(markerPath).toString().replace('\\', '/'),
                "handlerCount", standalone.size(),
                "requiredHandlerCount", EchoStandaloneRuntimeAdapterCoreGameplayBridge.REQUIRED_EVENTS.size(),
                "adapterCoreContractsMatched", true,
                "echoNativeBackendMatched", true,
                "standaloneBackendExecuted", true,
                "events", standalone
        )) + "\n", StandardCharsets.UTF_8);

        System.out.println("adaptercore gameplay bridge parity smoke PASS handlers=" + standalone.size());
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
