package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.story.EchoPresenceLink;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryModuleReference;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStorySaveState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimePresenceLinkStoryParitySmokeHarness {
    private EchoRuntimePresenceLinkStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echopresencelink")),
                "Presence Link story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract signalOs = EchoStoryAdapterCoreContract.signalOsReference();
        EchoStoryAdapterCoreContract primeRoute = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryModuleReference presence = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echopresencelink"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echopresencelink module reference"));

        require("presence_link".equals(presence.behavior()),
                "Presence Link reference behavior should be presence_link");
        require(presence.contentIds().contains(signalOs.presenceLink().id()),
                "Presence Link reference should carry the SignalOS cache presence id");
        require(presence.contentIds().contains(primeRoute.presenceLink().id()),
                "Presence Link reference should carry the Prime route presence id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : presence.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path nativeModule = repoRoot.resolve(
                "addons/echopresencelink/src/main/java/com/knoxhack/echopresencelink/EchoPresenceLinkNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Presence Link native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("echopresencelink:presence/signalos_cache"),
                "Presence Link native backend must name the SignalOS cache presence id");
        require(nativeText.contains("echopresencelink:presence/prime_route"),
                "Presence Link native backend must name the Prime route presence id");
        require(nativeText.contains("reading_signalos_archive"),
                "Presence Link native backend must bind SignalOS cache presence state");
        require(nativeText.contains("tracking_prime_route"),
                "Presence Link native backend must bind Prime route presence state");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Presence Link native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        for (EchoPresenceLink presenceLink : List.of(signalOs.presenceLink(), primeRoute.presenceLink())) {
            runtime.linkPresence(presenceLink);
            require(runtime.presenceLinks().contains(presenceLink.id() + "=" + presenceLink.state()),
                    "standalone Presence Link should publish " + presenceLink.id());
            require(runtime.loreUpdates().contains("presence:" + presenceLink.id()),
                    "standalone Presence Link should publish lore update for " + presenceLink.id());
        }

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        for (EchoPresenceLink presenceLink : List.of(signalOs.presenceLink(), primeRoute.presenceLink())) {
            require(restored.presenceLinks().contains(presenceLink.id() + "=" + presenceLink.state()),
                    "standalone Presence Link state should survive save/load for " + presenceLink.id());
        }

        System.out.println("presence link story parity smoke PASS module=echopresencelink presences="
                + String.join(",", presence.contentIds()));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
