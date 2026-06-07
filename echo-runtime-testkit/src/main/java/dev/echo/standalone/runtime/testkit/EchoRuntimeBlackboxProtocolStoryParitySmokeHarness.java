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

public final class EchoRuntimeBlackboxProtocolStoryParitySmokeHarness {
    private EchoRuntimeBlackboxProtocolStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echoblackboxprotocol")),
                "Blackbox Protocol story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract primeRoute = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryModuleReference blackbox = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echoblackboxprotocol"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echoblackboxprotocol module reference"));

        require("archive_unlock".equals(blackbox.behavior()),
                "Blackbox Protocol reference behavior should be archive_unlock");
        require(blackbox.contentIds().contains(primeRoute.archiveEntry().id()),
                "Blackbox Protocol reference should carry the Prime-route archive id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : blackbox.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path nativeModule = repoRoot.resolve(
                "addons/echoblackboxprotocol/src/main/java/com/knoxhack/echoblackboxprotocol/EchoBlackboxProtocolNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Blackbox native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("echoblackboxprotocol:archive/core_memory"),
                "Blackbox native backend must name the core memory archive contract");
        require(nativeText.contains("echoorbitalremnants:data_drive/orbital_blackbox"),
                "Blackbox native backend must bind the orbital blackbox data drive");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Blackbox native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.readDataDrive(primeRoute.dataDrive());
        require(runtime.dataDriveRead(), "standalone Blackbox data drive should be read");
        require(runtime.unlockedArchiveIds().contains(primeRoute.archiveEntry().id()),
                "standalone Blackbox behavior should unlock the core memory archive");
        require(runtime.loreUpdates().contains("lore:" + primeRoute.dataDrive().id()),
                "standalone Blackbox behavior should publish lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.unlockedArchiveIds().contains(primeRoute.archiveEntry().id()),
                "standalone Blackbox archive unlock should survive save/load");

        System.out.println("blackbox protocol story parity smoke PASS module=echoblackboxprotocol archive="
                + primeRoute.archiveEntry().id());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
