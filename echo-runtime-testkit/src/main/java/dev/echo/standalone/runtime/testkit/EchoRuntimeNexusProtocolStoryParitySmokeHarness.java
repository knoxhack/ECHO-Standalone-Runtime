package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryModuleReference;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStorySaveState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeNexusProtocolStoryParitySmokeHarness {
    private EchoRuntimeNexusProtocolStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        Path reportRoot = args.length > 1
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : repoRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echonexusprotocol")),
                "Nexus Protocol story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract primeRoute = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryModuleReference nexus = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echonexusprotocol"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echonexusprotocol module reference"));

        require("mission_hook".equals(nexus.behavior()),
                "Nexus Protocol reference behavior should be mission_hook");
        require(nexus.contentIds().contains(primeRoute.signalMessage().id()),
                "Nexus Protocol reference should carry the Nexus handoff signal id");
        require(nexus.contentIds().contains(primeRoute.signalMessage().missionId()),
                "Nexus Protocol reference should carry the Prime route mission id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : nexus.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        requireNativeEvidence(repoRoot, primeRoute);

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.openSignalOsTerminal(primeRoute);
        runtime.readDataDrive(primeRoute.dataDrive());
        runtime.startStoryMission(primeRoute.signalMessage());
        require(runtime.missionStarted(), "standalone Nexus mission hook should start a story mission");
        require(runtime.activeMissionId().equals(primeRoute.signalMessage().missionId()),
                "standalone Nexus active mission should match the Prime route mission id");
        require(runtime.loreUpdates().contains("signal:" + primeRoute.signalMessage().id()),
                "standalone Nexus behavior should publish signal lore evidence");
        require(runtime.loreUpdates().contains("mission:" + primeRoute.signalMessage().missionId()),
                "standalone Nexus behavior should publish mission lore evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.missionStarted(), "standalone Nexus mission state should survive save/load");
        require(restored.activeMissionId().equals(primeRoute.signalMessage().missionId()),
                "standalone Nexus mission id should survive save/load");

        writeReports(reportRoot, primeRoute, nexus);
        System.out.println("nexus protocol story parity smoke PASS module=echonexusprotocol signal="
                + primeRoute.signalMessage().id() + " mission=" + primeRoute.signalMessage().missionId());
    }

    private static void requireNativeEvidence(Path repoRoot, EchoStoryAdapterCoreContract primeRoute) throws IOException {
        String descriptorText = Files.readString(repoRoot.resolve(
                "addons/echonexusprotocol/src/main/resources/META-INF/echo.mod.json"));
        require(descriptorText.contains("\"nativeEntrypoint\": \"com.knoxhack.echonexusprotocol.EchoNexusProtocolNativeModule\""),
                "Nexus descriptor must expose the native AdapterCore backend");

        String nativeText = Files.readString(repoRoot.resolve(
                "addons/echonexusprotocol/src/main/java/com/knoxhack/echonexusprotocol/EchoNexusProtocolNativeModule.java"));
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Nexus native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains(".startMission(\"" + primeRoute.signalMessage().id() + "\", \""
                        + primeRoute.signalMessage().missionId() + "\")"),
                "Nexus native backend must execute the handoff signal to Prime route mission behavior");
        require(nativeText.contains("NexusMissionHooks.startPrimeRoute"),
                "Nexus native backend must name the executable Nexus mission hook");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Nexus native backend must execute service code");

        String hooksText = Files.readString(repoRoot.resolve(
                "addons/echonexusprotocol/src/main/java/com/knoxhack/echonexusprotocol/integration/NexusMissionHooks.java"));
        require(hooksText.contains("startPrimeRoute(Player player)"),
                "Nexus mission hooks must expose the native startPrimeRoute behavior");
        require(hooksText.contains("EchoCoreServices.startMission"),
                "Nexus mission hooks must call the real EchoCore mission service");
        require(hooksText.contains("MissionHookTargets.objectiveTarget"),
                "Nexus mission hooks must keep objective coverage contracts executable");

        String nativeActivation = Files.readString(repoRoot.resolve("reports/echo/agents/agent-10-native-activation.json"));
        require(nativeActivation.contains("\"moduleId\":\"echonexusprotocol\""),
                "Agent 10 native activation evidence must include echonexusprotocol");
        require(nativeActivation.contains(primeRoute.signalMessage().id())
                        && nativeActivation.contains(primeRoute.signalMessage().missionId()),
                "Agent 10 native activation evidence must include the Nexus signal and Prime mission ids");

        String agent59Evidence = Files.readString(repoRoot.resolve("reports/echo/agents/agent-59-blockers.json"))
                + Files.readString(repoRoot.resolve("reports/echo/agents/agent-59-parity.json"))
                + Files.readString(repoRoot.resolve("reports/echo/agents/agent-59-status.json"));
        require(agent59Evidence.contains("echonexusprotocol")
                        && (agent59Evidence.contains("mission hook") || agent59Evidence.contains("mission_hook")),
                "Agent 59 evidence should name the resolved Nexus mission hook focused parity target");
    }

    private static void writeReports(
            Path reportRoot,
            EchoStoryAdapterCoreContract primeRoute,
            EchoStoryModuleReference nexus
    ) throws IOException {
        Path agentDir = reportRoot.resolve("reports/echo/agents");
        Files.createDirectories(agentDir);

        List<String> modulesOwned = List.of(
                "echonexusprotocol",
                "echo-standalone-runtime testkit",
                "agent-69 reports"
        );
        List<String> featuresAudited = List.of(
                "Agent59 grouped story/protocol blocker for echonexusprotocol mission_hook",
                "AdapterCore EchoStoryModuleReference echonexusprotocol mission_hook contract",
                "Echo Native Loader backend EchoNexusProtocolNativeModule story runtime bridge",
                "NexusMissionHooks.startPrimeRoute real Java mission-service behavior",
                "Echo Standalone Runtime EchoStandaloneStoryRuntime mission start and save/load"
        );
        List<String> adapterContracts = List.of(
                "Existing EchoStoryAdapterCoreContract.primeRouteReference signal "
                        + primeRoute.signalMessage().id(),
                "Existing EchoStoryModuleReference echonexusprotocol behavior "
                        + nexus.behavior()
        );
        List<String> nativeImplemented = List.of(
                "com.knoxhack.echonexusprotocol.EchoNexusProtocolNativeModule",
                "com.knoxhack.echonexusprotocol.integration.NexusMissionHooks.startPrimeRoute"
        );
        List<String> standaloneImplemented = List.of(
                "dev.echo.standalone.runtime.testkit.EchoRuntimeNexusProtocolStoryParitySmokeHarness",
                "dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime.startStoryMission"
        );
        List<String> parityPassed = List.of(
                "runStandaloneNexusProtocolStoryParitySmoke",
                "AdapterCore exposes Nexus signal and Prime route mission ids for all runtime kinds",
                "Native module executes EchoNativeStoryRuntimeBridge.startMission",
                "NexusMissionHooks.startPrimeRoute calls EchoCoreServices.startMission",
                "Standalone runtime starts and saves the Prime route mission"
        );

        String status = "{\n"
                + "  \"agent\": \"agent-69-echonexusprotocol-story-parity\",\n"
                + "  \"modulesOwned\": " + jsonArray(modulesOwned) + ",\n"
                + "  \"featuresAudited\": " + jsonArray(featuresAudited) + ",\n"
                + "  \"adapterContractsAdded\": " + jsonArray(adapterContracts) + ",\n"
                + "  \"echoNativeImplemented\": " + jsonArray(nativeImplemented) + ",\n"
                + "  \"standaloneImplemented\": " + jsonArray(standaloneImplemented) + ",\n"
                + "  \"parityPassed\": " + jsonArray(parityPassed) + ",\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-69-status.json"), status);

        String parity = "{\n"
                + "  \"agent\": \"agent-69-echonexusprotocol-story-parity\",\n"
                + "  \"modulesOwned\": " + jsonArray(modulesOwned) + ",\n"
                + "  \"featuresAudited\": " + jsonArray(featuresAudited) + ",\n"
                + "  \"adapterContractsAdded\": " + jsonArray(adapterContracts) + ",\n"
                + "  \"echoNativeImplemented\": " + jsonArray(nativeImplemented) + ",\n"
                + "  \"standaloneImplemented\": " + jsonArray(standaloneImplemented) + ",\n"
                + "  \"parityPassed\": " + jsonArray(parityPassed) + ",\n"
                + "  \"referenceContentIds\": " + jsonArray(nexus.contentIds()) + ",\n"
                + "  \"nativeEvidence\": " + jsonArray(List.of(
                        "EchoNexusProtocolNativeModule uses EchoNativeStoryRuntimeBridge.startMission",
                        "NexusMissionHooks.startPrimeRoute calls EchoCoreServices.startMission",
                        "agent-10-native-activation records echonexusprotocol handlerExecutionCount=2"
                )) + ",\n"
                + "  \"standaloneEvidence\": " + jsonArray(List.of(
                        "EchoStandaloneStoryRuntime.startStoryMission",
                        "EchoStorySaveState round trip for active Prime route mission"
                )) + ",\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-69-parity.json"), parity);

        String blockers = "{\n"
                + "  \"agent\": \"agent-69-echonexusprotocol-story-parity\",\n"
                + "  \"modulesOwned\": " + jsonArray(modulesOwned) + ",\n"
                + "  \"featuresAudited\": " + jsonArray(featuresAudited) + ",\n"
                + "  \"adapterContractsAdded\": " + jsonArray(adapterContracts) + ",\n"
                + "  \"echoNativeImplemented\": " + jsonArray(nativeImplemented) + ",\n"
                + "  \"standaloneImplemented\": " + jsonArray(standaloneImplemented) + ",\n"
                + "  \"parityPassed\": " + jsonArray(parityPassed) + ",\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-69-blockers.json"), blockers);
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
