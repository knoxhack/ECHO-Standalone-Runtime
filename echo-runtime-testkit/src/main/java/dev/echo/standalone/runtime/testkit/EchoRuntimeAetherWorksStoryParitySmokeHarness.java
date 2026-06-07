package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryModuleReference;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStorySaveState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimeAetherWorksStoryParitySmokeHarness {
    private EchoRuntimeAetherWorksStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echoaetherworks")),
                "AetherWorks story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract arcaneCodex = EchoStoryAdapterCoreContract.arcaneCodexReference();
        EchoStoryModuleReference aetherWorks = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echoaetherworks"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echoaetherworks module reference"));

        require("presence_link".equals(aetherWorks.behavior()),
                "AetherWorks reference behavior should be presence_link");
        require(aetherWorks.contentIds().contains(arcaneCodex.presenceLink().id()),
                "AetherWorks reference should carry the Arcane Codex presence link id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : aetherWorks.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            require(entry.contentKind() == EchoAdapterCoreContentKind.DATA_COMPONENT,
                    contentId + " should be an AdapterCore data component");
            require(entry.domain() == EchoAdapterCoreDomain.STORY,
                    contentId + " should be exposed through the AdapterCore story domain");
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path descriptor = repoRoot.resolve("addons/echoaetherworks/src/main/resources/META-INF/echo.mod.json");
        String descriptorText = Files.readString(descriptor);
        require(descriptorText.contains("\"nativeEntrypoint\": \"com.knoxhack.echoaetherworks.EchoAetherWorksNativeModule\""),
                "AetherWorks descriptor must expose the native AdapterCore backend");

        Path nativeModule = repoRoot.resolve(
                "addons/echoaetherworks/src/main/java/com/knoxhack/echoaetherworks/EchoAetherWorksNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "AetherWorks native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("linkPresence(\"echoaetherworks:presence/aether_sync\""),
                "AetherWorks native backend must link the aether sync presence contract");
        require(nativeText.contains("AetherWorksApi.linkCodexSync"),
                "AetherWorks native backend must bind the codex sync hook");
        require(nativeText.contains("AetherWorksApi.publishSyncLore"),
                "AetherWorks native backend must bind the lore update hook");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "AetherWorks native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.linkPresence(arcaneCodex.presenceLink());
        require(runtime.presenceLinks().contains("echoaetherworks:presence/aether_sync=syncing_arcane_codex"),
                "standalone AetherWorks behavior should persist the aether sync presence state");
        require(runtime.loreUpdates().contains("presence:echoaetherworks:presence/aether_sync"),
                "standalone AetherWorks behavior should publish presence lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.presenceLinks().contains("echoaetherworks:presence/aether_sync=syncing_arcane_codex"),
                "standalone AetherWorks presence state should survive save/load");
        require(restored.loreUpdates().contains("presence:echoaetherworks:presence/aether_sync"),
                "standalone AetherWorks presence lore evidence should survive save/load");

        System.out.println("aetherworks story parity smoke PASS module=echoaetherworks presence="
                + arcaneCodex.presenceLink().id());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
