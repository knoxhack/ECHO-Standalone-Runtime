package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.story.EchoSignalMessage;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryModuleReference;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStorySaveState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeArcanaCoreStoryParitySmokeHarness {
    private EchoRuntimeArcanaCoreStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echoarcanacore")),
                "Arcana Core story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract arcaneCodex = EchoStoryAdapterCoreContract.arcaneCodexReference();
        EchoStoryModuleReference arcanaCore = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echoarcanacore"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echoarcanacore module reference"));

        require("mission_hook".equals(arcanaCore.behavior()),
                "Arcana Core reference behavior should be mission_hook");
        require(arcanaCore.contentIds().contains(arcaneCodex.signalMessage().id()),
                "Arcana Core reference should carry the Aether Wake signal id");
        require(arcanaCore.contentIds().contains(arcaneCodex.storyFlag().id()),
                "Arcana Core reference should carry the Arcane Codex story flag id");
        require(arcanaCore.contentIds().contains(arcaneCodex.signalMessage().missionId()),
                "Arcana Core reference should carry the Arcane Codex mission id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : arcanaCore.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path nativeModule = repoRoot.resolve(
                "addons/echoarcanacore/src/main/java/com/knoxhack/echoarcanacore/EchoArcanaCoreNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Arcana Core native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("echoarcanacore:signal/aether_wake"),
                "Arcana Core native backend must name the Aether Wake signal");
        require(nativeText.contains("echoarcanacore:mission/arcane_codex_sync"),
                "Arcana Core native backend must start the Arcane Codex mission");
        require(nativeText.contains("echoarcanacore:story_flag/arcane_codex_unlocked"),
                "Arcana Core native backend must persist the Arcane Codex story flag");
        require(nativeText.contains("signalosexample:data_drive/arcane_codex_demo"),
                "Arcana Core native backend must bind the Arcane Codex data drive");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Arcana Core native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        List<EchoSignalMessage> terminalSignals = runtime.openSignalOsTerminal(arcaneCodex);
        require(runtime.terminalOpen(), "standalone Arcana Core terminal surface should open");
        require(terminalSignals.contains(arcaneCodex.signalMessage()),
                "standalone Arcana Core terminal should expose Aether Wake signal");

        runtime.readDataDrive(arcaneCodex.dataDrive());
        require(Boolean.TRUE.equals(runtime.flags().get(arcaneCodex.storyFlag().id())),
                "standalone Arcana Core data-drive read should set the Arcane Codex story flag");
        runtime.startStoryMission(arcaneCodex.signalMessage());
        require(runtime.missionStarted(), "standalone Arcana Core mission should start");
        require(arcaneCodex.signalMessage().missionId().equals(runtime.activeMissionId()),
                "standalone Arcana Core mission id should match AdapterCore reference");
        require(runtime.loreUpdates().contains("signal:" + arcaneCodex.signalMessage().id()),
                "standalone Arcana Core behavior should publish signal lore update evidence");
        require(runtime.loreUpdates().contains("mission:" + arcaneCodex.signalMessage().missionId()),
                "standalone Arcana Core behavior should publish mission lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(Boolean.TRUE.equals(restored.flags().get(arcaneCodex.storyFlag().id())),
                "standalone Arcana Core story flag should survive save/load");
        require(arcaneCodex.signalMessage().missionId().equals(restored.activeMissionId()),
                "standalone Arcana Core active mission should survive save/load");
        require(restored.missionStarted(), "standalone Arcana Core mission state should survive save/load");

        System.out.println("arcana core story parity smoke PASS module=echoarcanacore mission="
                + arcaneCodex.signalMessage().missionId());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
