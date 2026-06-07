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

public final class EchoRuntimeSignalOsExampleStoryParitySmokeHarness {
    private EchoRuntimeSignalOsExampleStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/signalosexample")),
                "SignalOS Example story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract arcaneCodex = EchoStoryAdapterCoreContract.arcaneCodexReference();
        EchoStoryModuleReference signalOsExample = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("signalosexample"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing signalosexample module reference"));

        require("data_drive_reading".equals(signalOsExample.behavior()),
                "SignalOS Example reference behavior should be data_drive_reading");
        require(signalOsExample.contentIds().contains(arcaneCodex.dataDrive().id()),
                "SignalOS Example reference should carry the Arcane Codex data-drive id");
        require(signalOsExample.contentIds().contains(arcaneCodex.archiveEntry().id()),
                "SignalOS Example reference should carry the Arcane Codex archive id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : signalOsExample.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path nativeModule = repoRoot.resolve(
                "addons/signalosexample/src/main/java/com/knoxhack/signalosexample/SignalOsExampleNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "SignalOS Example native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("signalosexample:data_drive/arcane_codex_demo"),
                "SignalOS Example native backend must name the Arcane Codex data drive");
        require(nativeText.contains("echogrimoire:archive/arcane_codex"),
                "SignalOS Example native backend must unlock the Grimoire Arcane Codex archive");
        require(nativeText.contains("echoarcanacore:story_flag/arcane_codex_unlocked"),
                "SignalOS Example native backend must set the Arcane Codex story flag");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "SignalOS Example native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.readDataDrive(arcaneCodex.dataDrive());
        require(runtime.dataDriveRead(), "standalone SignalOS Example data drive should be read");
        require(runtime.unlockedArchiveIds().contains(arcaneCodex.archiveEntry().id()),
                "standalone SignalOS Example data drive should unlock the Grimoire archive");
        require(Boolean.TRUE.equals(runtime.flags().get(arcaneCodex.storyFlag().id())),
                "standalone SignalOS Example data drive should set the Arcane Codex story flag");
        require(runtime.loreUpdates().contains("lore:" + arcaneCodex.dataDrive().id()),
                "standalone SignalOS Example should publish lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.unlockedArchiveIds().contains(arcaneCodex.archiveEntry().id()),
                "standalone SignalOS Example archive unlock should survive save/load");
        require(Boolean.TRUE.equals(restored.flags().get(arcaneCodex.storyFlag().id())),
                "standalone SignalOS Example story flag should survive save/load");

        System.out.println("signalos example story parity smoke PASS module=signalosexample dataDrive="
                + arcaneCodex.dataDrive().id());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
