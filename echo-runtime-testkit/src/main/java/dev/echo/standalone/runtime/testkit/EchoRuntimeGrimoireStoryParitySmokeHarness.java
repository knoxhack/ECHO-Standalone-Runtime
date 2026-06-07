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

public final class EchoRuntimeGrimoireStoryParitySmokeHarness {
    private EchoRuntimeGrimoireStoryParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echogrimoire")),
                "Grimoire story parity requires the ECHO repo root");

        EchoStoryAdapterCoreContract arcaneCodex = EchoStoryAdapterCoreContract.arcaneCodexReference();
        EchoStoryModuleReference grimoire = EchoStoryAdapterCoreContract.moduleReferences().stream()
                .filter(reference -> reference.moduleId().equals("echogrimoire"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("missing echogrimoire module reference"));

        require("archive_unlock".equals(grimoire.behavior()),
                "Grimoire reference behavior should be archive_unlock");
        require(grimoire.contentIds().contains(arcaneCodex.archiveEntry().id()),
                "Grimoire reference should carry the Arcane Codex archive id");
        require("Grimoire".equals(arcaneCodex.archiveEntry().source()),
                "Arcane Codex archive source should remain Grimoire");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (String contentId : grimoire.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }

        Path entriesSource = repoRoot.resolve(
                "addons/echogrimoire/src/main/java/com/knoxhack/echogrimoire/integration/GrimoireEntries.java");
        String entriesText = Files.readString(entriesSource);
        require(entriesText.contains("what_is_aether_signal"),
                "Grimoire reference entries should include the Aether Signal starter archive");
        require(entriesText.contains("forbidden_unwritten_one"),
                "Grimoire reference entries should include warning-gated forbidden pages");
        require(entriesText.contains("VeilboundBridgeCatalog.allEntries()"),
                "Grimoire reference entries should mirror Veilbound bridge catalog records");

        Path missionSource = repoRoot.resolve(
                "addons/echogrimoire/src/main/java/com/knoxhack/echogrimoire/integration/GrimoireMissionIntegration.java");
        String missionText = Files.readString(missionSource);
        require(missionText.contains("MissionHookTargets.objectiveTarget(EchoGrimoire.MODID, id(\"read_first_entry\"), \"read\")"),
                "Grimoire mission integration should bind the read_first_entry objective hook");
        require(missionText.contains("MissionObjectiveType.UNLOCK_RESEARCH"),
                "Grimoire mission integration should use the research/archive unlock objective type");

        Path nativeModule = repoRoot.resolve(
                "addons/echogrimoire/src/main/java/com/knoxhack/echogrimoire/EchoGrimoireNativeModule.java");
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeStoryRuntimeBridge"),
                "Grimoire native backend must use EchoNativeStoryRuntimeBridge");
        require(nativeText.contains("echogrimoire:archive/arcane_codex"),
                "Grimoire native backend must name the Arcane Codex archive contract");
        require(nativeText.contains("signalosexample:data_drive/arcane_codex_demo"),
                "Grimoire native backend must bind the Arcane Codex demo data drive");
        require(nativeText.contains("echoarcanacore:story_flag/arcane_codex_unlocked"),
                "Grimoire native backend must persist the Arcana Core codex story flag");
        require(nativeText.contains("serviceCodeExecuted\", true"),
                "Grimoire native backend must execute service code");

        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.readDataDrive(arcaneCodex.dataDrive());
        require(runtime.dataDriveRead(), "standalone Grimoire codex data drive should be read");
        require(runtime.unlockedArchiveIds().contains(arcaneCodex.archiveEntry().id()),
                "standalone Grimoire behavior should unlock the Arcane Codex archive");
        require(Boolean.TRUE.equals(runtime.flags().get(arcaneCodex.storyFlag().id())),
                "standalone Grimoire behavior should persist the Arcane Codex story flag");
        require(runtime.loreUpdates().contains("lore:" + arcaneCodex.dataDrive().id()),
                "standalone Grimoire behavior should publish lore update evidence");

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.unlockedArchiveIds().contains(arcaneCodex.archiveEntry().id()),
                "standalone Grimoire archive unlock should survive save/load");
        require(Boolean.TRUE.equals(restored.flags().get(arcaneCodex.storyFlag().id())),
                "standalone Grimoire Arcane Codex flag should survive save/load");

        System.out.println("grimoire story parity smoke PASS module=echogrimoire archive="
                + arcaneCodex.archiveEntry().id());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
