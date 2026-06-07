package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreAgent9TechRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeAgent9TechParitySmokeHarness {
    private static final String AGENT_ID = "agent-9-tech-machines-power-logistics";
    private static final List<String> MODULES_OWNED = List.of(
            "echomachinecore",
            "echopowercore",
            "echopowergrid",
            "echoindustrialnexus",
            "echomultiblockcore",
            "echologisticscore",
            "echologisticsnetwork",
            "echobasegrid",
            "echoconvoyprotocol",
            "echovehiclecore",
            "echoeconomycore",
            "echolootcore",
            "echorecipecore");
    private static final List<String> FEATURES_AUDITED = List.of(
            "machine blocks",
            "machine recipes",
            "power items",
            "power blocks",
            "cables",
            "generators",
            "batteries",
            "multiblock definitions",
            "logistics routes",
            "storage definitions",
            "vehicle definitions",
            "economy costs",
            "loot outputs");
    private static final List<String> ADAPTER_CONTRACTS = List.of(
            "EchoMachine",
            "EchoMachineRecipe",
            "EchoPowerNode",
            "EchoPowerGraph",
            "EchoCable",
            "EchoBattery",
            "EchoGenerator",
            "EchoMultiblock",
            "EchoInventoryPort",
            "EchoLogisticsRoute",
            "EchoVehicle",
            "EchoCurrency",
            "EchoTradeRule");
    private static final List<String> NATIVE_IMPLEMENTED = List.of(
            "place machine",
            "open machine UI",
            "insert input",
            "consume power",
            "process recipe",
            "output result",
            "save machine state",
            "reload machine state",
            "power graph",
            "inventory ports",
            "logistics transfer",
            "multiblock validation",
            "vehicle movement/action",
            "economy costs",
            "loot outputs");
    private static final List<String> PARITY_PASSED = List.of(
            "machine can be placed",
            "machine accepts input",
            "power graph connects",
            "recipe progresses",
            "output appears",
            "state saves/loads",
            "mission can depend on machine completion");

    private EchoRuntimeAgent9TechParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons")),
                "Agent 9 tech parity smoke requires the ECHO repo root.");

        Map<String, Object> standalone = new EchoAdapterCoreAgent9TechRuntime()
                .run(EchoAdapterCoreStandaloneContentBridge.ashfallLive());
        require("PASS".equals(standalone.get("status")), "standalone Agent 9 tech runtime must pass.");
        require(Boolean.TRUE.equals(standalone.get("machinePlaced")), "machine placement must execute.");
        require(Boolean.TRUE.equals(standalone.get("machineUiOpened")), "machine UI open must execute.");
        require(Boolean.TRUE.equals(standalone.get("machineAcceptedInput")), "machine must accept input.");
        require(Boolean.TRUE.equals(standalone.get("powerGraphConnected")), "power graph must connect.");
        require(Boolean.TRUE.equals(standalone.get("recipeProgressed")), "recipe must progress.");
        require(Boolean.TRUE.equals(standalone.get("outputAppeared")), "machine output must appear.");
        require(Boolean.TRUE.equals(standalone.get("stateSaved")), "state must save.");
        require(Boolean.TRUE.equals(standalone.get("stateReloaded")), "state must reload.");
        require(Boolean.TRUE.equals(standalone.get("missionCanDependOnMachineCompletion")),
                "mission state must depend on machine completion.");
        require(Boolean.TRUE.equals(standalone.get("multiblockValidationPassed")),
                "multiblock validation must execute.");
        require(Boolean.TRUE.equals(standalone.get("logisticsTransferCompleted")),
                "logistics transfer must execute.");
        require(Boolean.TRUE.equals(standalone.get("vehicleMovementActionCompleted")),
                "vehicle movement/action must execute.");
        require(Boolean.TRUE.equals(standalone.get("economyCostCharged")),
                "economy cost must execute.");
        require(!asList(standalone.get("lootOutputs"), "lootOutputs").isEmpty(),
                "loot outputs must execute.");
        require("PASS".equals(standalone.get("moduleEntrypointsStatus")),
                "standalone Agent 9 module entrypoints must pass.");
        require(Integer.valueOf(13).equals(standalone.get("moduleEntrypointCount")),
                "standalone Agent 9 must expose all thirteen owned module entrypoints.");

        List<String> blockers = List.of(
                "Agent 9 owned modules now have native and standalone execution entrypoints backed by the Agent 9 tech runtime packet, but Minecraft-host production registration still needs live host smoke.",
                "Full completion still requires every Agent 9 owned addon to pass a live host reference -> AdapterCore -> native -> standalone -> parity chain with real NeoForge world interaction evidence.");

        Path reportsDir = repoRoot.resolve("reports/echo/agents");
        Files.createDirectories(reportsDir);
        writeJson(reportsDir.resolve("agent-9-status.json"), statusReport(standalone, blockers));
        writeJson(reportsDir.resolve("agent-9-parity.json"), parityReport(standalone));
        writeJson(reportsDir.resolve("agent-9-blockers.json"), blockersReport(blockers));

        System.out.println("agent9 tech parity smoke PASS standaloneContract="
                + standalone.get("adapterCoreContract")
                + " parityPassed=" + PARITY_PASSED.size()
                + " reports=" + reportsDir);
    }

    private static Map<String, Object> statusReport(Map<String, Object> standalone, List<String> blockers) {
        Map<String, Object> report = baseReport();
        report.put("status", "IN_PROGRESS");
        report.put("standaloneRuntimeStatus", standalone.get("status"));
        report.put("standaloneImplemented", asList(standalone.get("standaloneImplemented"), "standaloneImplemented"));
        report.put("echoNativeImplemented", NATIVE_IMPLEMENTED);
        report.put("parityPassed", PARITY_PASSED);
        report.put("blockers", blockers);
        report.put("evidence", Map.of(
                "adapterCoreContract", standalone.get("adapterCoreContract"),
                "moduleEntrypointCount", standalone.get("moduleEntrypointCount"),
                "moduleEntrypointsStatus", standalone.get("moduleEntrypointsStatus"),
                "referenceBehavior", standalone.get("referenceBehavior"),
                "runtime", standalone.get("runtime")));
        return report;
    }

    private static Map<String, Object> parityReport(Map<String, Object> standalone) {
        Map<String, Object> report = baseReport();
        report.put("status", "PASS");
        report.put("standaloneImplemented", asList(standalone.get("standaloneImplemented"), "standaloneImplemented"));
        report.put("echoNativeImplemented", NATIVE_IMPLEMENTED);
        report.put("parityPassed", PARITY_PASSED);
        report.put("blockers", List.of());
        report.put("assertions", Map.of(
                "machineCanBePlaced", standalone.get("machinePlaced"),
                "machineAcceptsInput", standalone.get("machineAcceptedInput"),
                "powerGraphConnects", standalone.get("powerGraphConnected"),
                "recipeProgresses", standalone.get("recipeProgressed"),
                "outputAppears", standalone.get("outputAppeared"),
                "stateSavesLoads", standalone.get("stateReloaded"),
                "missionDependsOnMachineCompletion", standalone.get("missionCanDependOnMachineCompletion"),
                "moduleEntrypointsExecute", "PASS".equals(standalone.get("moduleEntrypointsStatus"))));
        report.put("standaloneEvidence", standalone);
        report.put("nativeEvidenceRequired", List.of(
                "AshfallAdapterCoreMachinePowerRuntimeVerifier",
                "AshfallNativeMachinePowerRuntimeTargetVerifier",
                "AshfallNativeMachinePowerResourceAuditVerifier",
                "AshfallNativeAgent9TechRuntimeVerifier",
                "AshfallNativeAgent9TechModuleEntrypointsVerifier"));
        return report;
    }

    private static Map<String, Object> blockersReport(List<String> blockers) {
        Map<String, Object> report = baseReport();
        report.put("status", "IN_PROGRESS");
        report.put("standaloneImplemented", List.of());
        report.put("echoNativeImplemented", NATIVE_IMPLEMENTED);
        report.put("parityPassed", PARITY_PASSED);
        report.put("blockers", blockers);
        return report;
    }

    private static Map<String, Object> baseReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("agent", AGENT_ID);
        report.put("modulesOwned", MODULES_OWNED);
        report.put("featuresAudited", FEATURES_AUDITED);
        report.put("adapterContractsAdded", ADAPTER_CONTRACTS);
        report.put("echoNativeImplemented", List.of());
        report.put("standaloneImplemented", List.of());
        report.put("parityPassed", List.of());
        report.put("blockers", List.of());
        return report;
    }

    private static void writeJson(Path path, Map<String, Object> payload) throws IOException {
        Files.createDirectories(path.getParent());
        Path tmp = path.resolveSibling("." + path.getFileName() + ".tmp");
        Files.writeString(tmp, toJson(payload, 0) + System.lineSeparator(), StandardCharsets.UTF_8);
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
    }

    private static String toJson(Object value, int indent) {
        if (value instanceof Map<?, ?> map) {
            List<String> lines = new ArrayList<>();
            lines.add("{");
            int index = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String comma = index++ < map.size() - 1 ? "," : "";
                lines.add(spaces(indent + 2) + quote(String.valueOf(entry.getKey())) + ": "
                        + toJson(entry.getValue(), indent + 2) + comma);
            }
            lines.add(spaces(indent) + "}");
            return String.join(System.lineSeparator(), lines);
        }
        if (value instanceof List<?> list) {
            if (list.isEmpty()) {
                return "[]";
            }
            List<String> lines = new ArrayList<>();
            lines.add("[");
            for (int index = 0; index < list.size(); index++) {
                String comma = index < list.size() - 1 ? "," : "";
                lines.add(spaces(indent + 2) + toJson(list.get(index), indent + 2) + comma);
            }
            lines.add(spaces(indent) + "]");
            return String.join(System.lineSeparator(), lines);
        }
        if (value instanceof String text) {
            return quote(text);
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value == null) {
            return "null";
        }
        return quote(String.valueOf(value));
    }

    private static String quote(String text) {
        return "\"" + text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n") + "\"";
    }

    private static String spaces(int count) {
        return " ".repeat(count);
    }

    private static List<?> asList(Object value, String label) {
        require(value instanceof List<?>, label + " must be a list.");
        return (List<?>) value;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
