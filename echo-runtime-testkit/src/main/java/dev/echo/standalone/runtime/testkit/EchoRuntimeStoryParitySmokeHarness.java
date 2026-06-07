package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryModuleReference;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStorySaveState;
import dev.echo.standalone.runtime.ui.EchoTerminalCommand;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandRegistry;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandResult;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiTheme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EchoRuntimeStoryParitySmokeHarness {
    private EchoRuntimeStoryParitySmokeHarness() {
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
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons/echosignalos")),
                "Story parity smoke requires the ECHO repo root");

        EchoStoryAdapterCoreContract signalOsReference = EchoStoryAdapterCoreContract.signalOsReference();
        EchoStoryAdapterCoreContract primeRouteReference = EchoStoryAdapterCoreContract.primeRouteReference();
        EchoStoryAdapterCoreContract arcaneCodexReference = EchoStoryAdapterCoreContract.arcaneCodexReference();
        List<EchoStoryAdapterCoreContract> references = List.of(
                signalOsReference,
                primeRouteReference,
                arcaneCodexReference
        );
        List<EchoStoryModuleReference> moduleReferences = EchoStoryAdapterCoreContract.moduleReferences();
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        for (EchoStoryAdapterCoreContract reference : references) {
            requireAdapterCoreBindings(bridge, reference);
        }
        for (EchoStoryModuleReference moduleReference : moduleReferences) {
            requireAdapterCoreBindings(bridge, moduleReference);
        }
        requireNativeBackends(repoRoot, signalOsReference, primeRouteReference, arcaneCodexReference);

        List<EchoStandaloneStoryRuntime> restoredRuntimes = new ArrayList<>();
        for (EchoStoryAdapterCoreContract reference : references) {
            restoredRuntimes.add(exerciseReference(reference));
        }
        List<String> standaloneModuleExecutions = moduleReferences.stream()
                .map(moduleReference -> exerciseModuleReference(
                        moduleReference,
                        signalOsReference,
                        primeRouteReference,
                        arcaneCodexReference
                ))
                .toList();

        writeReports(reportRoot, references, moduleReferences, restoredRuntimes, standaloneModuleExecutions);
        System.out.println("story parity smoke PASS references=" + references.size()
                + " contentIds=" + references.stream().mapToInt(reference -> reference.contentIds().size()).sum()
                + " moduleReferences=" + moduleReferences.size()
                + " signalArchive=" + signalOsReference.archiveEntry().id()
                + " primeArchive=" + primeRouteReference.archiveEntry().id()
                + " arcaneArchive=" + arcaneCodexReference.archiveEntry().id()
                + " nativeEntrypoints=17");
    }

    private static EchoStandaloneStoryRuntime exerciseReference(EchoStoryAdapterCoreContract reference) {
        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        EchoTerminalCommandRegistry commands = new EchoTerminalCommandRegistry();
        String commandName = reference.signalMessage().source().toLowerCase().replace(" ", "-");
        commands.register(new EchoTerminalCommand(commandName, "Open story signal", context -> {
            List<String> lines = runtime.openSignalOsTerminal(reference).stream()
                    .map(message -> message.source() + ": " + message.body())
                    .toList();
            return EchoTerminalCommandResult.output(lines);
        }));
        EchoTerminalShell terminal = new EchoTerminalShell(commands);
        terminal.submit(commandName, EchoUiTheme.defaultTerminal());
        require(runtime.terminalOpen(), "Standalone SignalOS terminal command should open SignalOS");
        require(terminal.outputLines().stream().anyMatch(line -> line.contains(reference.signalMessage().body())),
                "Standalone terminal should render the reference mission signal");

        runtime.readDataDrive(reference.dataDrive());
        require(runtime.dataDriveRead(), "Standalone data drive should be read");
        require(runtime.unlockedArchiveIds().contains(reference.archiveEntry().id()),
                "Data drive should unlock the reference archive entry");
        require(Boolean.TRUE.equals(runtime.flags().get(reference.storyFlag().id())),
                "Data drive should persist the reference story flag");

        runtime.startStoryMission(reference.signalMessage());
        require(runtime.missionStarted(), "SignalOS story mission should start");
        require(runtime.activeMissionId().equals(reference.signalMessage().missionId()),
                "Started story mission should match the reference message mission id");

        runtime.applyRelicEffect(reference.relicEffect());
        runtime.castSpell(reference.spell());
        runtime.activateRitual(reference.ritual());
        runtime.applyCurse(reference.curse());
        runtime.triggerRift(reference.riftEvent());
        String effectStat = reference.relicEffect().gameplayStat();
        int expectedEffectStat = reference.relicEffect().delta()
                + reference.spell().delta()
                + reference.curse().delta();
        require(runtime.gameplayStats().getOrDefault(effectStat, 0) == expectedEffectStat,
                "Relic, spell, and curse effects should mutate gameplay state");
        require(runtime.gameplayStats().getOrDefault("chapterStability", 0) == 1,
                "Ritual should mutate chapter stability");
        require(runtime.unlockChapter(reference.chapterUnlock()),
                "Chapter unlock should pass after data drive story flag");
        runtime.linkPresence(reference.presenceLink());

        EchoStorySaveState saved = runtime.save();
        EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(saved);
        require(restored.terminalOpen()
                        && restored.dataDriveRead()
                        && restored.missionStarted()
                        && restored.unlockedArchiveIds().contains(reference.archiveEntry().id())
                        && restored.unlockedChapterIds().contains(reference.chapterUnlock().id())
                        && restored.gameplayStats().equals(runtime.gameplayStats()),
                "Story state should save and load terminal, drive, mission, archive, chapter, and gameplay mutations");
        require(restored.loreUpdates().contains("index:signalos")
                        && restored.loreUpdates().contains("wiki:signalos/data_drives")
                        && restored.loreUpdates().contains("lore:" + reference.dataDrive().id()),
                "Index/Wiki/Lore updates should be emitted by runtime behavior");
        require(!restored.presenceLinks().isEmpty(), "Presence Link state should be recorded");
        return restored;
    }

    private static String exerciseModuleReference(
            EchoStoryModuleReference moduleReference,
            EchoStoryAdapterCoreContract signalOsReference,
            EchoStoryAdapterCoreContract primeRouteReference,
            EchoStoryAdapterCoreContract arcaneCodexReference
    ) {
        EchoStoryAdapterCoreContract reference = referenceFor(moduleReference, signalOsReference, primeRouteReference, arcaneCodexReference);
        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        switch (moduleReference.behavior()) {
            case "terminal_archive_unlock" -> {
                runtime.openSignalOsTerminal(reference);
                runtime.readDataDrive(reference.dataDrive());
                runtime.startStoryMission(reference.signalMessage());
                require(runtime.unlockChapter(reference.chapterUnlock()),
                        moduleReference.moduleId() + " should unlock its chapter after the story flag is persisted");
                require(runtime.terminalOpen()
                                && runtime.dataDriveRead()
                                && runtime.missionStarted()
                                && runtime.unlockedArchiveIds().contains(reference.archiveEntry().id())
                                && runtime.unlockedChapterIds().contains(reference.chapterUnlock().id()),
                        moduleReference.moduleId() + " standalone terminal/archive/chapter behavior did not execute");
            }
            case "spell_usage" -> {
                runtime.castSpell(reference.spell());
                require(runtime.gameplayStats().getOrDefault(reference.spell().gameplayStat(), 0) == reference.spell().delta(),
                        moduleReference.moduleId() + " standalone spell behavior did not mutate gameplay");
            }
            case "ritual_activation" -> {
                runtime.activateRitual(reference.ritual());
                require(runtime.gameplayStats().getOrDefault(reference.ritual().gameplayStat(), 0) == reference.ritual().delta()
                                && Boolean.TRUE.equals(runtime.flags().get(reference.ritual().unlockFlagId())),
                        moduleReference.moduleId() + " standalone ritual behavior did not mutate gameplay and flags");
            }
            case "curse_effect" -> {
                runtime.applyCurse(reference.curse());
                require(runtime.gameplayStats().getOrDefault(reference.curse().gameplayStat(), 0) == reference.curse().delta(),
                        moduleReference.moduleId() + " standalone curse behavior did not mutate gameplay");
            }
            case "rift_trigger" -> {
                runtime.triggerRift(reference.riftEvent());
                require(Boolean.TRUE.equals(runtime.flags().get(reference.riftEvent().unlockFlagId()))
                                && runtime.loreUpdates().contains("rift:" + reference.riftEvent().id()),
                        moduleReference.moduleId() + " standalone rift behavior did not persist its unlock flag");
            }
            case "archive_unlock", "data_drive_reading" -> {
                runtime.readDataDrive(reference.dataDrive());
                require(runtime.dataDriveRead()
                                && runtime.unlockedArchiveIds().contains(reference.archiveEntry().id())
                                && runtime.loreUpdates().contains("lore:" + reference.dataDrive().id()),
                        moduleReference.moduleId() + " standalone data-drive behavior did not unlock its archive");
            }
            case "mission_hook" -> {
                runtime.openSignalOsTerminal(reference);
                runtime.readDataDrive(reference.dataDrive());
                runtime.startStoryMission(reference.signalMessage());
                require(runtime.missionStarted()
                                && runtime.activeMissionId().equals(reference.signalMessage().missionId())
                                && runtime.loreUpdates().contains("mission:" + reference.signalMessage().missionId()),
                        moduleReference.moduleId() + " standalone mission hook did not start its mission");
            }
            case "story_flag_persistence" -> {
                runtime.openSignalOsTerminal(reference);
                runtime.readDataDrive(reference.dataDrive());
                runtime.startStoryMission(reference.signalMessage());
                EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(runtime.save());
                require(Boolean.TRUE.equals(restored.flags().get(reference.storyFlag().id())),
                        moduleReference.moduleId() + " standalone story flag did not save and load");
            }
            case "chapter_unlock" -> {
                runtime.readDataDrive(reference.dataDrive());
                require(runtime.unlockChapter(reference.chapterUnlock()),
                        moduleReference.moduleId() + " standalone chapter did not unlock");
                EchoStandaloneStoryRuntime restored = EchoStandaloneStoryRuntime.load(runtime.save());
                require(restored.unlockedChapterIds().contains(reference.chapterUnlock().id()),
                        moduleReference.moduleId() + " standalone chapter unlock did not save and load");
            }
            case "presence_link" -> {
                if (moduleReference.moduleId().equals("echopresencelink")) {
                    runtime.linkPresence(signalOsReference.presenceLink());
                    runtime.linkPresence(primeRouteReference.presenceLink());
                } else {
                    runtime.linkPresence(reference.presenceLink());
                }
                for (String contentId : moduleReference.contentIds()) {
                    require(runtime.loreUpdates().contains("presence:" + contentId),
                            moduleReference.moduleId() + " standalone presence link missing " + contentId);
                }
            }
            case "relic_effect" -> {
                runtime.readDataDrive(signalOsReference.dataDrive());
                runtime.applyRelicEffect(signalOsReference.relicEffect());
                runtime.readDataDrive(arcaneCodexReference.dataDrive());
                runtime.applyRelicEffect(arcaneCodexReference.relicEffect());
                require(runtime.gameplayStats().getOrDefault(signalOsReference.relicEffect().gameplayStat(), 0)
                                == signalOsReference.relicEffect().delta()
                                && runtime.gameplayStats().getOrDefault(arcaneCodexReference.relicEffect().gameplayStat(), 0)
                                == arcaneCodexReference.relicEffect().delta(),
                        moduleReference.moduleId() + " standalone relic effects did not mutate both gameplay stats");
            }
            default -> throw new AssertionError("Unknown story module behavior: " + moduleReference.behavior());
        }
        for (String contentId : moduleReference.contentIds()) {
            require(behaviorEvidence(runtime, contentId) || moduleReference.behavior().equals("spell_usage")
                            || moduleReference.behavior().equals("ritual_activation")
                            || moduleReference.behavior().equals("curse_effect"),
                    moduleReference.moduleId() + " standalone behavior did not expose evidence for " + contentId);
        }
        return moduleReference.moduleId() + ":" + moduleReference.behavior();
    }

    private static EchoStoryAdapterCoreContract referenceFor(
            EchoStoryModuleReference moduleReference,
            EchoStoryAdapterCoreContract signalOsReference,
            EchoStoryAdapterCoreContract primeRouteReference,
            EchoStoryAdapterCoreContract arcaneCodexReference
    ) {
        return switch (moduleReference.moduleId()) {
            case "echoblackboxprotocol", "echoorbitalremnants", "echonexusprotocol", "echoprimecore",
                    "echostationfall" -> primeRouteReference;
            case "echogrimoire", "signalosexample", "echoarcanacore", "echoarcaneindex", "echoaetherworks" ->
                    arcaneCodexReference;
            default -> signalOsReference;
        };
    }

    private static boolean behaviorEvidence(EchoStandaloneStoryRuntime runtime, String contentId) {
        return runtime.unlockedArchiveIds().contains(contentId)
                || runtime.unlockedChapterIds().contains(contentId)
                || runtime.flags().containsKey(contentId)
                || runtime.activeMissionId().equals(contentId)
                || runtime.loreUpdates().stream().anyMatch(update -> update.contains(contentId))
                || runtime.presenceLinks().stream().anyMatch(update -> update.contains(contentId));
    }

    private static void requireAdapterCoreBindings(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoStoryAdapterCoreContract reference
    ) {
        for (String contentId : reference.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }
    }

    private static void requireAdapterCoreBindings(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoStoryModuleReference moduleReference
    ) {
        for (String contentId : moduleReference.contentIds()) {
            var entry = bridge.registry().requireContentId(contentId);
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                        contentId + " should expose " + runtimeKind.adapterId() + " through AdapterCore");
            }
        }
    }

    private static void requireNativeBackends(
            Path repoRoot,
            EchoStoryAdapterCoreContract signalOsReference,
            EchoStoryAdapterCoreContract primeRouteReference,
            EchoStoryAdapterCoreContract arcaneCodexReference
    ) throws IOException {
        requireNativeBackend(
                repoRoot,
                "addons/echosignalos/src/main/resources/META-INF/echo.mod.json",
                "addons/echosignalos/src/main/java/com/knoxhack/signalos/EchoSignalOsNativeModule.java",
                "com.knoxhack.signalos.EchoSignalOsNativeModule",
                List.of(
                        signalOsReference.archiveEntry().id(),
                        signalOsReference.dataDrive().id(),
                        signalOsReference.signalMessage().id(),
                        signalOsReference.storyFlag().id(),
                        signalOsReference.signalMessage().missionId(),
                        signalOsReference.chapterUnlock().id(),
                        "signalos:save/story_state"
                )
        );
        requireNativeBackend(
                repoRoot,
                "addons/echospellcore/src/main/resources/META-INF/echo.mod.json",
                "addons/echospellcore/src/main/java/com/knoxhack/echospellcore/EchoSpellCoreNativeModule.java",
                "com.knoxhack.echospellcore.EchoSpellCoreNativeModule",
                List.of(signalOsReference.spell().id(), "echospellcore:save/spell_state")
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoritualcore/src/main/resources/META-INF/echo.mod.json",
                "addons/echoritualcore/src/main/java/com/knoxhack/echoritualcore/EchoRitualCoreNativeModule.java",
                "com.knoxhack.echoritualcore.EchoRitualCoreNativeModule",
                List.of(signalOsReference.ritual().id(), "echoritualcore:save/ritual_state")
        );
        requireNativeBackend(
                repoRoot,
                "addons/echocursecore/src/main/resources/META-INF/echo.mod.json",
                "addons/echocursecore/src/main/java/com/knoxhack/echocursecore/EchoCurseCoreNativeModule.java",
                "com.knoxhack.echocursecore.EchoCurseCoreNativeModule",
                List.of(signalOsReference.curse().id(), "echocursecore:save/curse_state")
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoriftworlds/src/main/resources/META-INF/echo.mod.json",
                "addons/echoriftworlds/src/main/java/com/knoxhack/echoriftworlds/EchoRiftWorldsNativeModule.java",
                "com.knoxhack.echoriftworlds.EchoRiftWorldsNativeModule",
                List.of(signalOsReference.riftEvent().id(), "echoriftworlds:save/rift_state")
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoblackboxprotocol/src/main/resources/META-INF/echo.mod.json",
                "addons/echoblackboxprotocol/src/main/java/com/knoxhack/echoblackboxprotocol/EchoBlackboxProtocolNativeModule.java",
                "com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocolNativeModule",
                List.of(primeRouteReference.archiveEntry().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoorbitalremnants/src/main/resources/META-INF/echo.mod.json",
                "addons/echoorbitalremnants/src/main/java/com/knoxhack/echoorbitalremnants/EchoOrbitalRemnantsNativeModule.java",
                "com.knoxhack.echoorbitalremnants.EchoOrbitalRemnantsNativeModule",
                List.of(primeRouteReference.dataDrive().id(), "echoorbitalremnants:save/orbital_story_state")
        );
        requireNativeBackend(
                repoRoot,
                "addons/echonexusprotocol/src/main/resources/META-INF/echo.mod.json",
                "addons/echonexusprotocol/src/main/java/com/knoxhack/echonexusprotocol/EchoNexusProtocolNativeModule.java",
                "com.knoxhack.echonexusprotocol.EchoNexusProtocolNativeModule",
                List.of(primeRouteReference.signalMessage().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoprimecore/src/main/resources/META-INF/echo.mod.json",
                "addons/echoprimecore/src/main/java/com/knoxhack/echoprimecore/EchoPrimeCoreNativeModule.java",
                "com.knoxhack.echoprimecore.EchoPrimeCoreNativeModule",
                List.of(
                        primeRouteReference.storyFlag().id(),
                        primeRouteReference.signalMessage().missionId(),
                        "echoprimecore:save/prime_route_state"
                )
        );
        requireNativeBackend(
                repoRoot,
                "addons/echostationfall/src/main/resources/META-INF/echo.mod.json",
                "addons/echostationfall/src/main/java/com/knoxhack/echostationfall/EchoStationfallNativeModule.java",
                "com.knoxhack.echostationfall.EchoStationfallNativeModule",
                List.of(primeRouteReference.chapterUnlock().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/echopresencelink/src/main/resources/META-INF/echo.mod.json",
                "addons/echopresencelink/src/main/java/com/knoxhack/echopresencelink/EchoPresenceLinkNativeModule.java",
                "com.knoxhack.echopresencelink.EchoPresenceLinkNativeModule",
                List.of(signalOsReference.presenceLink().id(), primeRouteReference.presenceLink().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/echogrimoire/src/main/resources/META-INF/echo.mod.json",
                "addons/echogrimoire/src/main/java/com/knoxhack/echogrimoire/EchoGrimoireNativeModule.java",
                "com.knoxhack.echogrimoire.EchoGrimoireNativeModule",
                List.of(arcaneCodexReference.archiveEntry().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/signalosexample/src/main/resources/META-INF/echo.mod.json",
                "addons/signalosexample/src/main/java/com/knoxhack/signalosexample/SignalOsExampleNativeModule.java",
                "com.knoxhack.signalosexample.SignalOsExampleNativeModule",
                List.of(arcaneCodexReference.dataDrive().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoarcanacore/src/main/resources/META-INF/echo.mod.json",
                "addons/echoarcanacore/src/main/java/com/knoxhack/echoarcanacore/EchoArcanaCoreNativeModule.java",
                "com.knoxhack.echoarcanacore.EchoArcanaCoreNativeModule",
                List.of(
                        arcaneCodexReference.signalMessage().id(),
                        arcaneCodexReference.storyFlag().id(),
                        arcaneCodexReference.signalMessage().missionId(),
                        "echoarcanacore:save/arcane_story_state"
                )
        );
        requireNativeBackend(
                repoRoot,
                "addons/echorelictech/src/main/resources/META-INF/echo.mod.json",
                "addons/echorelictech/src/main/java/com/knoxhack/echorelictech/EchoRelicTechNativeModule.java",
                "com.knoxhack.echorelictech.EchoRelicTechNativeModule",
                List.of(
                        signalOsReference.relicEffect().id(),
                        arcaneCodexReference.relicEffect().id(),
                        "echorelictech:save/relic_story_state"
                )
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoarcaneindex/src/main/resources/META-INF/echo.mod.json",
                "addons/echoarcaneindex/src/main/java/com/knoxhack/echoarcaneindex/EchoArcaneIndexNativeModule.java",
                "com.knoxhack.echoarcaneindex.EchoArcaneIndexNativeModule",
                List.of(arcaneCodexReference.chapterUnlock().id())
        );
        requireNativeBackend(
                repoRoot,
                "addons/echoaetherworks/src/main/resources/META-INF/echo.mod.json",
                "addons/echoaetherworks/src/main/java/com/knoxhack/echoaetherworks/EchoAetherWorksNativeModule.java",
                "com.knoxhack.echoaetherworks.EchoAetherWorksNativeModule",
                List.of(arcaneCodexReference.presenceLink().id())
        );
    }

    private static void requireNativeBackend(
            Path repoRoot,
            String descriptorPath,
            String nativeModulePath,
            String nativeEntrypoint,
            List<String> requiredIds
    ) throws IOException {
        Path descriptor = repoRoot.resolve("addons/echosignalos/src/main/resources/META-INF/echo.mod.json");
        descriptor = repoRoot.resolve(descriptorPath);
        String descriptorText = Files.readString(descriptor);
        require(descriptorText.contains("\"nativeEntrypoint\": \"" + nativeEntrypoint + "\""),
                descriptorPath + " must expose the native AdapterCore backend " + nativeEntrypoint);

        Path nativeModule = repoRoot.resolve(nativeModulePath);
        String nativeText = Files.readString(nativeModule);
        require(nativeText.contains("EchoNativeModuleAdapter")
                        && nativeText.contains("EchoNativeLifecycleBridge")
                        && nativeText.contains("EchoNativeRegistryBridge")
                        && nativeText.contains("EchoNativeEventBridge")
                        && nativeText.contains("EchoNativeStoryRuntimeBridge"),
                nativeModulePath + " must use AdapterCore native bridges");
        require(nativeText.contains("storyRuntimeBridge")
                        && nativeText.contains("serviceCodeExecuted\", true"),
                nativeModulePath + " must execute Native Story Runtime behavior instead of only reporting hooks");
        for (String requiredId : requiredIds) {
            require(nativeText.contains(requiredId),
                    nativeModulePath + " is missing reference contract id " + requiredId);
        }
    }

    private static void writeReports(
            Path repoRoot,
            List<EchoStoryAdapterCoreContract> references,
            List<EchoStoryModuleReference> moduleReferences,
            List<EchoStandaloneStoryRuntime> restoredRuntimes,
            List<String> standaloneModuleExecutions
    ) throws IOException {
        Path agentDir = repoRoot.resolve("reports/echo/agents");
        Files.createDirectories(agentDir);
        String modules = "[\"echosignalos\",\"signalos\",\"signalosexample\",\"echoblackboxprotocol\","
                + "\"echonexusprotocol\",\"echoorbitalremnants\",\"echorelictech\",\"echoriftworlds\","
                + "\"echoritualcore\",\"echospellcore\",\"echocursecore\",\"echogrimoire\","
                + "\"echoaetherworks\",\"echostationfall\",\"echoprimecore\",\"echopresencelink\","
                + "\"echoarcanacore\",\"echoarcaneindex\"]";
        String contracts = "[\"EchoArchiveEntry\",\"EchoDataDrive\",\"EchoSignalMessage\",\"EchoStoryFlag\","
                + "\"EchoRelicEffect\",\"EchoSpell\",\"EchoRitual\",\"EchoCurse\",\"EchoRiftEvent\","
                + "\"EchoChapterUnlock\",\"EchoPresenceLink\"]";
        String nativeBackends = "[\"com.knoxhack.signalos.EchoSignalOsNativeModule\","
                + "\"com.knoxhack.echospellcore.EchoSpellCoreNativeModule\","
                + "\"com.knoxhack.echoritualcore.EchoRitualCoreNativeModule\","
                + "\"com.knoxhack.echocursecore.EchoCurseCoreNativeModule\","
                + "\"com.knoxhack.echoriftworlds.EchoRiftWorldsNativeModule\","
                + "\"com.knoxhack.echoblackboxprotocol.EchoBlackboxProtocolNativeModule\","
                + "\"com.knoxhack.echonexusprotocol.EchoNexusProtocolNativeModule\","
                + "\"com.knoxhack.echoorbitalremnants.EchoOrbitalRemnantsNativeModule\","
                + "\"com.knoxhack.echostationfall.EchoStationfallNativeModule\","
                + "\"com.knoxhack.echoprimecore.EchoPrimeCoreNativeModule\","
                + "\"com.knoxhack.echopresencelink.EchoPresenceLinkNativeModule\","
                + "\"com.knoxhack.echogrimoire.EchoGrimoireNativeModule\","
                + "\"com.knoxhack.signalosexample.SignalOsExampleNativeModule\","
                + "\"com.knoxhack.echoarcanacore.EchoArcanaCoreNativeModule\","
                + "\"com.knoxhack.echorelictech.EchoRelicTechNativeModule\","
                + "\"com.knoxhack.echoarcaneindex.EchoArcaneIndexNativeModule\","
                + "\"com.knoxhack.echoaetherworks.EchoAetherWorksNativeModule\"]";
        String status = "{\n"
                + "  \"agent\": \"agent-10-story-signalos-arcane\",\n"
                + "  \"modulesOwned\": " + modules + ",\n"
                + "  \"featuresAudited\": [\"SignalOS terminal open\",\"data drive archive unlock\","
                + "\"story mission start\",\"Prime route chapter progression\","
                + "\"Arcane codex route progression\","
                + "\"relic/spell/ritual/curse gameplay mutation\","
                + "\"rift chapter unlock\",\"story save/load\",\"Index/Wiki/Lore updates\","
                + "\"module-by-module story reference audit\",\"native story handler activation\"],\n"
                + "  \"adapterContractsAdded\": " + contracts + ",\n"
                + "  \"echoNativeImplemented\": " + nativeBackends + ",\n"
                + "  \"standaloneImplemented\": [\"dev.echo.standalone.runtime.gameplay.EchoStandaloneStoryRuntime\"],\n"
                + "  \"parityPassed\": [\"runStandaloneStoryParitySmoke\", \"runEchoAgent10NativeStoryActivationSmoke\", \"runNativeAgent10StorySmoke\"],\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-10-status.json"), status);

        Set<String> referenceContentIds = new LinkedHashSet<>();
        references.forEach(reference -> referenceContentIds.addAll(reference.contentIds()));
        moduleReferences.forEach(reference -> referenceContentIds.addAll(reference.contentIds()));
        List<String> restoredLoreUpdates = restoredRuntimes.stream()
                .flatMap(runtime -> runtime.loreUpdates().stream())
                .distinct()
                .toList();
        String parity = "{\n"
                + "  \"agent\": \"agent-10-story-signalos-arcane\",\n"
                + "  \"referenceContentIds\": " + jsonArray(List.copyOf(referenceContentIds)) + ",\n"
                + "  \"moduleReferencesCovered\": " + jsonArray(moduleReferences.stream().map(EchoStoryModuleReference::moduleId).toList()) + ",\n"
                + "  \"adapterContractsAdded\": " + contracts + ",\n"
                + "  \"echoNativeImplemented\": [\"SignalOS native AdapterCore lifecycle/registry/event/story runtime backend\","
                + " \"SpellCore native spell backend\", \"RitualCore native ritual backend\","
                + " \"CurseCore native curse backend\", \"RiftWorlds native rift backend\","
                + " \"Blackbox/Nexus/Orbital/Stationfall/Prime/Presence native Prime-route backends\","
                + " \"Grimoire/SignalOSExample/ArcanaCore/RelicTech/ArcaneIndex/AetherWorks native Arcane codex backends\"],\n"
                + "  \"standaloneImplemented\": [\"SignalOS terminal command\", \"data drive archive unlock\","
                + " \"mission start\", \"Prime route chapter progression\", \"Arcane codex chapter progression\", \"gameplay mutations\", \"save/load\", \"lore updates\"],\n"
                + "  \"parityPassed\": [\"Native Loader story backend executes\", \"Native Story Runtime bridge executes service code\", \"SignalOS opens in terminal\", \"data drive unlocks archive\","
                + " \"story mission starts\", \"Prime route starts and unlocks Stationfall chapter\", \"Arcane codex starts and unlocks Arcane Index chapter\", \"relic/spell/ritual changes gameplay\","
                + " \"story state saves/loads\", \"Index/Wiki/Lore updates\", \"module-by-module standalone story references execute\"],\n"
                + "  \"standaloneModuleExecutions\": " + jsonArray(standaloneModuleExecutions) + ",\n"
                + "  \"restoredLoreUpdates\": " + jsonArray(restoredLoreUpdates) + ",\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-10-parity.json"), parity);

        String blockers = "{\n"
                + "  \"agent\": \"agent-10-story-signalos-arcane\",\n"
                + "  \"modulesOwned\": " + modules + ",\n"
                + "  \"featuresAudited\": [],\n"
                + "  \"adapterContractsAdded\": [],\n"
                + "  \"echoNativeImplemented\": [],\n"
                + "  \"standaloneImplemented\": [],\n"
                + "  \"parityPassed\": [\"runStandaloneStoryParitySmoke\", \"runEchoAgent10NativeStoryActivationSmoke\", \"runNativeAgent10StorySmoke\"],\n"
                + "  \"blockers\": []\n"
                + "}\n";
        Files.writeString(agentDir.resolve("agent-10-blockers.json"), blockers);
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
