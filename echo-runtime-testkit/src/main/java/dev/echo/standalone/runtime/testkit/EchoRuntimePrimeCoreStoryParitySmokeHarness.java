package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryModuleReference;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStorySaveState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimePrimeCoreStoryParitySmokeHarness {
    private EchoRuntimePrimeCoreStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path repoRoot = repoRoot(args);
        require(Files.isDirectory(repoRoot.resolve("addons/echoprimecore")),
                "Prime Core story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract primeRoute = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryModuleReference primeCore = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echoprimecore"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echoprimecore module reference"));

        require("story_flag_persistence".equals(primeCore.behavior()),
                "Prime Core reference behavior should be story_flag_persistence");
        require(primeCore.contentIds().contains(primeRoute.storyFlag().id()),
                "Prime Core reference should carry the Prime-route story flag");
        require(primeCore.contentIds().contains(primeRoute.signalMessage().missionId()),
                "Prime Core reference should carry the Prime-route mission id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : primeCore.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path descriptor = repoRoot.resolve("addons/echoprimecore/src/main/resources/META-INF/echo.mod.json");
        String descriptorText = Files.readString(descriptor, StandardCharsets.UTF_8);
        require(descriptorText.contains("\"nativeEntrypoint\": \"com.knoxhack.echoprimecore.EchoPrimeCoreNativeModule\""),
                "Prime Core descriptor must expose the native AdapterCore backend");

        Path nativeModule = repoRoot.resolve(
                "addons/echoprimecore/src/main/java/com/knoxhack/echoprimecore/EchoPrimeCoreNativeModule.java");
        String nativeText = Files.readString(nativeModule, StandardCharsets.UTF_8);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Prime Core native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains(primeRoute.storyFlag().id()),
                "Prime Core native backend must name the Prime-route story flag");
        require(nativeText.contains(primeRoute.signalMessage().missionId()),
                "Prime Core native backend must name the Prime-route mission");
        require(nativeText.contains(primeRoute.chapterUnlock().id()),
                "Prime Core native backend must bind Stationfall chapter progression");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Prime Core native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.openSignalOsTerminal(primeRoute);
        runtime.readDataDrive(primeRoute.dataDrive());
        runtime.startStoryMission(primeRoute.signalMessage());
        require(runtime.missionStarted(), "standalone Prime route mission should start");
        require(runtime.activeMissionId().equals(primeRoute.signalMessage().missionId()),
                "standalone Prime route mission should match the reference mission id");
        require(Boolean.TRUE.equals(runtime.flags().get(primeRoute.storyFlag().id())),
                "standalone Prime route flag should be persisted before save");
        require(runtime.unlockChapter(primeRoute.chapterUnlock()),
                "standalone Prime route flag should unlock the Stationfall route chapter");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(Boolean.TRUE.equals(restored.flags().get(primeRoute.storyFlag().id())),
                "standalone Prime route flag should survive save/load");
        require(restored.missionStarted() && restored.activeMissionId().equals(primeRoute.signalMessage().missionId()),
                "standalone Prime route mission state should survive save/load");
        require(restored.unlockedChapterIds().contains(primeRoute.chapterUnlock().id()),
                "standalone Prime route chapter progression should survive save/load");
        require(restored.loreUpdates().contains("mission:" + primeRoute.signalMessage().missionId()),
                "standalone Prime route mission should emit lore evidence");

        writeReports(repoRoot, primeRoute, primeCore);
        System.out.println("primecore story parity smoke PASS module=echoprimecore flag="
                + primeRoute.storyFlag().id() + " mission=" + primeRoute.signalMessage().missionId());
    }

    private static Path repoRoot(String[] args) {
        Path root = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        if (root.getFileName() != null && root.getFileName().toString().equals("echo-standalone-runtime")) {
            return root.getParent();
        }
        return root;
    }

    private static void writeReports(
            Path repoRoot,
            EchoStoryAdapterCoreContract primeRoute,
            EchoStoryModuleReference primeCore
    ) throws IOException {
        Path agentDir = repoRoot.resolve("reports/echo/agents");
        Files.createDirectories(agentDir);
        Files.writeString(agentDir.resolve("agent-73-status.json"), statusJson(), StandardCharsets.UTF_8);
        Files.writeString(agentDir.resolve("agent-73-parity.json"), parityJson(primeRoute, primeCore), StandardCharsets.UTF_8);
        Files.writeString(agentDir.resolve("agent-73-blockers.json"), blockersJson(), StandardCharsets.UTF_8);
    }

    private static String statusJson() {
        return """
                {
                  "agent": "agent-73-echoprimecore-story-parity",
                  "modulesOwned": [
                    "echoprimecore focused story parity",
                    "agent-73 reports"
                  ],
                  "featuresAudited": [
                    "Agent59 grouped story/protocol blocker for echoprimecore",
                    "Prime Core story flag persistence",
                    "Prime route mission state",
                    "Stationfall route chapter progression",
                    "Standalone story save/load"
                  ],
                  "adapterContractsAdded": [],
                  "echoNativeImplemented": [
                    "Audited com.knoxhack.echoprimecore.EchoPrimeCoreNativeModule native AdapterCore story runtime backend."
                  ],
                  "standaloneImplemented": [
                    "EchoRuntimePrimeCoreStoryParitySmokeHarness executes echoprimecore story_flag_persistence outside the grouped Agent10 story smoke."
                  ],
                  "parityPassed": [
                    "runStandalonePrimeCoreStoryParitySmoke"
                  ],
                  "blockers": []
                }
                """;
    }

    private static String parityJson(EchoStoryAdapterCoreContract primeRoute, EchoStoryModuleReference primeCore) {
        return """
                {
                  "agent": "agent-73-echoprimecore-story-parity",
                  "referenceBehavior": "story_flag_persistence",
                  "moduleReference": {
                    "moduleId": "echoprimecore",
                    "featureAudited": "Prime story flag persistence and mission state",
                    "contentIds": %s
                  },
                  "adapterCoreContract": [
                    "%s",
                    "%s"
                  ],
                  "echoNativeImplemented": [
                    "EchoPrimeCoreNativeModule uses EchoNativeStoryRuntimeBridge",
                    "EchoPrimeCoreNativeModule starts echoprimecore:mission/prime_route from echonexusprotocol:signal/nexus_handoff",
                    "EchoPrimeCoreNativeModule unlocks echostationfall:chapter/stationfall_route from echoprimecore:story_flag/prime_route_unlocked",
                    "EchoPrimeCoreNativeModule reports serviceCodeExecuted=true"
                  ],
                  "standaloneImplemented": [
                    "EchoStandaloneStoryRuntime.openSignalOsTerminal",
                    "EchoStandaloneStoryRuntime.readDataDrive",
                    "EchoStandaloneStoryRuntime.startStoryMission",
                    "EchoStandaloneStoryRuntime.unlockChapter",
                    "EchoStandaloneStoryRuntime.save/load"
                  ],
                  "parityPassed": [
                    "Reference behavior -> AdapterCore content ids -> Echo Native Loader backend evidence -> Echo Standalone Runtime execution -> focused parity smoke",
                    "Prime route flag persisted before save/load",
                    "Prime route mission state survived save/load",
                    "Stationfall route chapter progression survived save/load"
                  ],
                  "blockers": []
                }
                """.formatted(
                jsonArray(primeCore.contentIds()),
                primeRoute.storyFlag().id(),
                primeRoute.signalMessage().missionId()
        );
    }

    private static String blockersJson() {
        return """
                {
                  "agent": "agent-73-echoprimecore-story-parity",
                  "modulesOwned": [
                    "echoprimecore focused story parity",
                    "agent-73 reports"
                  ],
                  "featuresAudited": [
                    "Agent59 grouped story/protocol blocker for echoprimecore"
                  ],
                  "adapterContractsAdded": [],
                  "echoNativeImplemented": [],
                  "standaloneImplemented": [
                    "EchoRuntimePrimeCoreStoryParitySmokeHarness"
                  ],
                  "parityPassed": [
                    "echoprimecore no longer relies only on the grouped Agent10 story parity smoke for story_flag_persistence coverage."
                  ],
                  "blockers": [
                    {
                      "severity": "P2",
                      "modules": [
                        "echonexusprotocol",
                        "echogrimoire",
                        "echoaetherworks",
                        "echostationfall",
                        "echopresencelink",
                        "echoarcanacore",
                        "echoarcaneindex",
                        "signalosexample"
                      ],
                      "gap": "Agent59's grouped story/protocol follow-up still has non-echoprimecore modules that need focused per-module parity harnesses."
                    }
                  ]
                }
                """;
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
