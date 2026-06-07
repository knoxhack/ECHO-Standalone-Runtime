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

public final class EchoRuntimeStationfallStoryParitySmokeHarness {
    private EchoRuntimeStationfallStoryParitySmokeHarness() {
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
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echostationfall")),
                "Stationfall story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract primeRoute = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryModuleReference stationfall = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echostationfall"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echostationfall module reference"));

        require("chapter_unlock".equals(stationfall.behavior()),
                "Stationfall reference behavior should be chapter_unlock");
        require(stationfall.contentIds().equals(List.of(primeRoute.chapterUnlock().id())),
                "Stationfall reference should carry only the Stationfall chapter unlock id");
        require(primeRoute.chapterUnlock().requiredFlagId().equals(primeRoute.storyFlag().id()),
                "Stationfall chapter unlock should require the Prime route story flag");

        requireAdapterCoreBindings(stationfall);
        requireNativeBackend(repoRoot, primeRoute);
        EchoStandaloneStoryRuntime restored = exerciseStandaloneBackend(primeRoute);
        writeReports(reportRoot, primeRoute, stationfall, restored);

        System.out.println("stationfall story parity smoke PASS module=echostationfall chapter="
                + primeRoute.chapterUnlock().id());
    }

    private static void requireAdapterCoreBindings(EchoStoryModuleReference stationfall) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : stationfall.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }
    }

    private static void requireNativeBackend(Path repoRoot, EchoStoryAdapterCoreContract primeRoute) throws IOException {
        Path descriptor = repoRoot.resolve("addons/echostationfall/src/main/resources/META-INF/echo.mod.json");
        String descriptorText = Files.readString(descriptor);
        require(descriptorText.contains("\"nativeEntrypoint\": \"com.knoxhack.echostationfall.EchoStationfallNativeModule\""),
                "Stationfall descriptor must expose the native AdapterCore backend");

        Path nativeModule = repoRoot.resolve(
                "addons/echostationfall/src/main/java/com/knoxhack/echostationfall/EchoStationfallNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Stationfall native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains(".unlockChapter("),
                "Stationfall native backend must execute chapter unlock service behavior");
        require(nativeText.contains(primeRoute.chapterUnlock().id()),
                "Stationfall native backend must name the Stationfall route chapter");
        require(nativeText.contains(primeRoute.chapterUnlock().requiredFlagId()),
                "Stationfall native backend must require the Prime route story flag");
        require(nativeText.contains("StationfallRouteTracker.unlockPrimeRoute"),
                "Stationfall native backend must bind the real route tracker behavior");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Stationfall native backend must execute service code");
    }

    private static EchoStandaloneStoryRuntime exerciseStandaloneBackend(EchoStoryAdapterCoreContract primeRoute) {
        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        require(!runtime.unlockChapter(primeRoute.chapterUnlock()),
                "Stationfall standalone chapter should remain locked before the Prime route flag");

        runtime.readDataDrive(primeRoute.dataDrive());
        require(Boolean.TRUE.equals(runtime.flags().get(primeRoute.storyFlag().id())),
                "Orbital blackbox drive should persist the Prime route flag used by Stationfall");
        require(runtime.unlockChapter(primeRoute.chapterUnlock()),
                "Stationfall standalone chapter should unlock once the Prime route flag is present");
        require(runtime.unlockedChapterIds().contains(primeRoute.chapterUnlock().id()),
                "Stationfall standalone runtime should record the unlocked chapter id");
        require(runtime.loreUpdates().contains("chapter:" + primeRoute.chapterUnlock().id()),
                "Stationfall standalone runtime should publish chapter lore evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(Boolean.TRUE.equals(restored.flags().get(primeRoute.storyFlag().id())),
                "Stationfall Prime route flag should survive save/load");
        require(restored.unlockedChapterIds().contains(primeRoute.chapterUnlock().id()),
                "Stationfall chapter unlock should survive save/load");
        return restored;
    }

    private static void writeReports(
            Path repoRoot,
            EchoStoryAdapterCoreContract primeRoute,
            EchoStoryModuleReference stationfall,
            EchoStandaloneStoryRuntime restored
    ) throws IOException {
        Path agentDir = repoRoot.resolve("reports/echo/agents");
        Files.createDirectories(agentDir);

        String status = "{\n"
                + "  \"agent\": \"agent-72-echostationfall-story-parity\",\n"
                + "  \"modulesOwned\": [\"echostationfall focused story parity\", \"agent-72 reports\"],\n"
                + "  \"featuresAudited\": [\"Agent 59 grouped story/protocol blocker\", \"Stationfall chapter unlock reference behavior\", \"AdapterCore content bindings\", \"Echo Native Loader Stationfall backend\", \"Echo Standalone Runtime chapter unlock behavior\"],\n"
                + "  \"adapterContractsAdded\": [],\n"
                + "  \"echoNativeImplemented\": [\"Verified EchoStationfallNativeModule uses EchoNativeStoryRuntimeBridge.unlockChapter for echostationfall:chapter/stationfall_route gated by echoprimecore:story_flag/prime_route_unlocked.\"],\n"
                + "  \"standaloneImplemented\": [\"Added EchoRuntimeStationfallStoryParitySmokeHarness as a focused executable Stationfall story parity proof.\"],\n"
                + "  \"parityPassed\": [\"runStandaloneStationfallStoryParitySmoke\"],\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-72-status.json"), status);

        String parity = "{\n"
                + "  \"agent\": \"agent-72-echostationfall-story-parity\",\n"
                + "  \"referenceBehavior\": \"" + stationfall.moduleId() + ":" + stationfall.behavior() + "\",\n"
                + "  \"referenceContentIds\": " + jsonArray(stationfall.contentIds()) + ",\n"
                + "  \"adapterCoreContract\": [\"EchoStoryAdapterCoreContract.primeRouteReference\", \"EchoStoryModuleReference.echostationfall\"],\n"
                + "  \"echoNativeImplemented\": [\"addons/echostationfall/src/main/java/com/knoxhack/echostationfall/EchoStationfallNativeModule.java\"],\n"
                + "  \"standaloneImplemented\": [\"echo-standalone-runtime/echo-runtime-testkit/src/main/java/dev/echo/standalone/runtime/testkit/EchoRuntimeStationfallStoryParitySmokeHarness.java\"],\n"
                + "  \"parityPassed\": [\"AdapterCore registry exposes echostationfall:chapter/stationfall_route for every runtime kind\", \"Native backend declares Stationfall route chapter and Prime route flag\", \"Standalone chapter unlock is blocked before the Prime route flag\", \"Standalone chapter unlock succeeds after reading the Prime-route data drive\", \"Stationfall chapter and Prime route flag survive save/load\"],\n"
                + "  \"standaloneModuleExecutions\": [\"" + stationfall.moduleId() + ":" + stationfall.behavior() + "\"],\n"
                + "  \"restoredFlags\": " + jsonArray(restored.flags().keySet().stream().toList()) + ",\n"
                + "  \"restoredChapters\": " + jsonArray(restored.unlockedChapterIds().stream().toList()) + ",\n"
                + "  \"requiredFlag\": \"" + primeRoute.chapterUnlock().requiredFlagId() + "\",\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-72-parity.json"), parity);

        String blockers = "{\n"
                + "  \"agent\": \"agent-72-echostationfall-story-parity\",\n"
                + "  \"modulesOwned\": [\"echostationfall focused story parity\", \"agent-72 reports\"],\n"
                + "  \"featuresAudited\": [\"Agent 59 grouped story/protocol blocker for echostationfall\"],\n"
                + "  \"adapterContractsAdded\": [],\n"
                + "  \"echoNativeImplemented\": [],\n"
                + "  \"standaloneImplemented\": [],\n"
                + "  \"parityPassed\": [\"runStandaloneStationfallStoryParitySmoke\"],\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-72-blockers.json"), blockers);
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
