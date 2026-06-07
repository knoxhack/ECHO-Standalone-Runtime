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

public final class EchoRuntimeOrbitalRemnantsStoryParitySmokeHarness {
    private EchoRuntimeOrbitalRemnantsStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echoorbitalremnants")),
                "Orbital Remnants story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract primeRoute = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryModuleReference orbital = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echoorbitalremnants"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echoorbitalremnants module reference"));

        require("data_drive_reading".equals(orbital.behavior()),
                "Orbital Remnants reference behavior should be data_drive_reading");
        require(orbital.contentIds().contains(primeRoute.dataDrive().id()),
                "Orbital Remnants reference should carry the orbital data-drive id");
        require(orbital.contentIds().contains(primeRoute.archiveEntry().id()),
                "Orbital Remnants reference should carry the archive unlocked by the data drive");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : orbital.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path nativeModule = repoRoot.resolve(
                "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants/EchoOrbitalRemnantsNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Orbital native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("echoorbitalremnants:data_drive/orbital_blackbox"),
                "Orbital native backend must name the orbital blackbox data drive");
        require(nativeText.contains("echoblackboxprotocol:archive/core_memory"),
                "Orbital native backend must unlock the Blackbox core memory archive");
        require(nativeText.contains("echoprimecore:story_flag/prime_route_unlocked"),
                "Orbital native backend must persist the Prime route story flag");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Orbital native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.readDataDrive(primeRoute.dataDrive());
        require(runtime.dataDriveRead(), "standalone Orbital data drive should be read");
        require(runtime.unlockedArchiveIds().contains(primeRoute.archiveEntry().id()),
                "standalone Orbital data drive should unlock the Blackbox archive");
        require(Boolean.TRUE.equals(runtime.flags().get(primeRoute.storyFlag().id())),
                "standalone Orbital data drive should persist the Prime route story flag");
        require(runtime.loreUpdates().contains("lore:" + primeRoute.dataDrive().id()),
                "standalone Orbital behavior should publish lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.unlockedArchiveIds().contains(primeRoute.archiveEntry().id()),
                "standalone Orbital archive unlock should survive save/load");
        require(Boolean.TRUE.equals(restored.flags().get(primeRoute.storyFlag().id())),
                "standalone Orbital Prime route flag should survive save/load");

        System.out.println("orbital remnants story parity smoke PASS module=echoorbitalremnants dataDrive="
                + primeRoute.dataDrive().id());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
