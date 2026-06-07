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

public final class EchoRuntimeArcaneIndexStoryParitySmokeHarness {
    private EchoRuntimeArcaneIndexStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echoarcaneindex")),
                "Arcane Index story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract arcaneCodex = EchoStoryAdapterCoreContract.arcaneCodexReference();
        EchoStoryModuleReference arcaneIndex = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echoarcaneindex"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echoarcaneindex module reference"));

        require("chapter_unlock".equals(arcaneIndex.behavior()),
                "Arcane Index reference behavior should be chapter_unlock");
        require(arcaneIndex.contentIds().contains(arcaneCodex.chapterUnlock().id()),
                "Arcane Index reference should carry the Arcane Codex chapter id");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : arcaneIndex.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path nativeModule = repoRoot.resolve(
                "addons/echoarcaneindex/src/main/java/com/knoxhack/echoarcaneindex/EchoArcaneIndexNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Arcane Index native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("echoarcaneindex:chapter/arcane_codex"),
                "Arcane Index native backend must name the Arcane Codex chapter id");
        require(nativeText.contains("echoarcanacore:story_flag/arcane_codex_unlocked"),
                "Arcane Index native backend must gate on the Arcane Codex story flag");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Arcane Index native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        require(!runtime.unlockChapter(arcaneCodex.chapterUnlock()),
                "standalone Arcane Index chapter should remain locked before the story flag");
        runtime.readDataDrive(arcaneCodex.dataDrive());
        require(runtime.unlockChapter(arcaneCodex.chapterUnlock()),
                "standalone Arcane Index chapter should unlock after the Arcane Codex flag");
        require(runtime.unlockedChapterIds().contains(arcaneCodex.chapterUnlock().id()),
                "standalone Arcane Index chapter id should be recorded");
        require(runtime.loreUpdates().contains("chapter:" + arcaneCodex.chapterUnlock().id()),
                "standalone Arcane Index should publish chapter lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.unlockedChapterIds().contains(arcaneCodex.chapterUnlock().id()),
                "standalone Arcane Index chapter unlock should survive save/load");

        System.out.println("arcane index story parity smoke PASS module=echoarcaneindex chapter="
                + arcaneCodex.chapterUnlock().id());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
