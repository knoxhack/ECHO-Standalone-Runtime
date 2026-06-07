package dev.echo.standalone.runtime.contracts.story;

import java.util.List;
import java.util.Objects;

public record EchoStoryAdapterCoreContract(
        EchoArchiveEntry archiveEntry,
        EchoDataDrive dataDrive,
        EchoSignalMessage signalMessage,
        EchoStoryFlag storyFlag,
        EchoRelicEffect relicEffect,
        EchoSpell spell,
        EchoRitual ritual,
        EchoCurse curse,
        EchoRiftEvent riftEvent,
        EchoChapterUnlock chapterUnlock,
        EchoPresenceLink presenceLink
) {
    public EchoStoryAdapterCoreContract {
        Objects.requireNonNull(archiveEntry, "archiveEntry");
        Objects.requireNonNull(dataDrive, "dataDrive");
        Objects.requireNonNull(signalMessage, "signalMessage");
        Objects.requireNonNull(storyFlag, "storyFlag");
        Objects.requireNonNull(relicEffect, "relicEffect");
        Objects.requireNonNull(spell, "spell");
        Objects.requireNonNull(ritual, "ritual");
        Objects.requireNonNull(curse, "curse");
        Objects.requireNonNull(riftEvent, "riftEvent");
        Objects.requireNonNull(chapterUnlock, "chapterUnlock");
        Objects.requireNonNull(presenceLink, "presenceLink");
    }

    public static EchoStoryAdapterCoreContract signalOsReference() {
        EchoStoryFlag driveFlag = new EchoStoryFlag("signalos:story_flag/cache_secured", true);
        return new EchoStoryAdapterCoreContract(
                new EchoArchiveEntry(
                        "signalos:archive/field_cache",
                        "Field Cache Handoff",
                        "SignalOS",
                        List.of(
                                "Cache drive authenticated.",
                                "Archive opened for Index, Wiki, and Lore consumers."
                        )
                ),
                new EchoDataDrive(
                        "signalos:data_drive/handoff_drive",
                        "Handoff Drive",
                        List.of("signalos:archive/field_cache"),
                        List.of(driveFlag)
                ),
                new EchoSignalMessage(
                        "signalos:signal/secure_cache",
                        "SignalOS",
                        "Secure cache mission available.",
                        "signalos:mission/secure_cache"
                ),
                driveFlag,
                new EchoRelicEffect(
                        "echorelictech:relic_effect/echo_mirror",
                        "signalClarity",
                        2,
                        "signalos:archive/field_cache"
                ),
                new EchoSpell("echospellcore:spell/signal_pulse", "signalClarity", 1),
                new EchoRitual(
                        "echoritualcore:ritual/relic_stabilization",
                        "chapterStability",
                        1,
                        "echoritualcore:story_flag/relic_stabilized"
                ),
                new EchoCurse("echocursecore:curse/echo_rot", "signalClarity", -1),
                new EchoRiftEvent(
                        "echoriftworlds:rift_event/cache_echo",
                        "signalos:chapter/cache_handoff",
                        "signalos:story_flag/rift_seen"
                ),
                new EchoChapterUnlock(
                        "signalos:chapter/cache_handoff",
                        "Cache Handoff",
                        "signalos:story_flag/cache_secured"
                ),
                new EchoPresenceLink(
                        "echopresencelink:presence/signalos_cache",
                        "reading_signalos_archive",
                        "signalos:signal/secure_cache"
                )
        );
    }

    public static EchoStoryAdapterCoreContract primeRouteReference() {
        EchoStoryFlag driveFlag = new EchoStoryFlag("echoprimecore:story_flag/prime_route_unlocked", true);
        return new EchoStoryAdapterCoreContract(
                new EchoArchiveEntry(
                        "echoblackboxprotocol:archive/core_memory",
                        "Core Memory",
                        "Blackbox Protocol",
                        List.of(
                                "Blackbox memory recovered.",
                                "Prime route handoff is ready for Nexus, Orbital, and Stationfall."
                        )
                ),
                new EchoDataDrive(
                        "echoorbitalremnants:data_drive/orbital_blackbox",
                        "Orbital Blackbox Drive",
                        List.of("echoblackboxprotocol:archive/core_memory"),
                        List.of(driveFlag)
                ),
                new EchoSignalMessage(
                        "echonexusprotocol:signal/nexus_handoff",
                        "Nexus Protocol",
                        "Prime route handoff mission available.",
                        "echoprimecore:mission/prime_route"
                ),
                driveFlag,
                new EchoRelicEffect(
                        "echorelictech:relic_effect/echo_mirror",
                        "signalClarity",
                        2,
                        "echoblackboxprotocol:archive/core_memory"
                ),
                new EchoSpell("echospellcore:spell/signal_pulse", "signalClarity", 1),
                new EchoRitual(
                        "echoritualcore:ritual/relic_stabilization",
                        "chapterStability",
                        1,
                        "echoprimecore:story_flag/ritual_stabilized"
                ),
                new EchoCurse("echocursecore:curse/echo_rot", "signalClarity", -1),
                new EchoRiftEvent(
                        "echoriftworlds:rift_event/cache_echo",
                        "echostationfall:chapter/stationfall_route",
                        "echoprimecore:story_flag/rift_seen"
                ),
                new EchoChapterUnlock(
                        "echostationfall:chapter/stationfall_route",
                        "Stationfall Route",
                        "echoprimecore:story_flag/prime_route_unlocked"
                ),
                new EchoPresenceLink(
                        "echopresencelink:presence/prime_route",
                        "tracking_prime_route",
                        "echonexusprotocol:signal/nexus_handoff"
                )
        );
    }

    public static EchoStoryAdapterCoreContract arcaneCodexReference() {
        EchoStoryFlag driveFlag = new EchoStoryFlag("echoarcanacore:story_flag/arcane_codex_unlocked", true);
        return new EchoStoryAdapterCoreContract(
                new EchoArchiveEntry(
                        "echogrimoire:archive/arcane_codex",
                        "Arcane Codex",
                        "Grimoire",
                        List.of(
                                "Grimoire codex recovered.",
                                "Arcana Core, Arcane Index, AetherWorks, and RelicTech are ready to synchronize."
                        )
                ),
                new EchoDataDrive(
                        "signalosexample:data_drive/arcane_codex_demo",
                        "Arcane Codex Demo Drive",
                        List.of("echogrimoire:archive/arcane_codex"),
                        List.of(driveFlag)
                ),
                new EchoSignalMessage(
                        "echoarcanacore:signal/aether_wake",
                        "Arcana Core",
                        "Arcane codex synchronization mission available.",
                        "echoarcanacore:mission/arcane_codex_sync"
                ),
                driveFlag,
                new EchoRelicEffect(
                        "echorelictech:relic_effect/phase_anchor",
                        "aetherCharge",
                        2,
                        "echogrimoire:archive/arcane_codex"
                ),
                new EchoSpell("echospellcore:spell/signal_pulse", "aetherCharge", 1),
                new EchoRitual(
                        "echoritualcore:ritual/relic_stabilization",
                        "chapterStability",
                        1,
                        "echoarcanacore:story_flag/ritual_stabilized"
                ),
                new EchoCurse("echocursecore:curse/echo_rot", "aetherCharge", -1),
                new EchoRiftEvent(
                        "echoriftworlds:rift_event/cache_echo",
                        "echoarcaneindex:chapter/arcane_codex",
                        "echoarcanacore:story_flag/rift_seen"
                ),
                new EchoChapterUnlock(
                        "echoarcaneindex:chapter/arcane_codex",
                        "Arcane Codex",
                        "echoarcanacore:story_flag/arcane_codex_unlocked"
                ),
                new EchoPresenceLink(
                        "echoaetherworks:presence/aether_sync",
                        "syncing_arcane_codex",
                        "echoarcanacore:signal/aether_wake"
                )
        );
    }

    public static List<EchoStoryModuleReference> moduleReferences() {
        EchoStoryAdapterCoreContract signalOs = signalOsReference();
        EchoStoryAdapterCoreContract primeRoute = primeRouteReference();
        EchoStoryAdapterCoreContract arcaneCodex = arcaneCodexReference();
        return List.of(
                new EchoStoryModuleReference(
                        "signalos",
                        "SignalOS archives, data drives, terminal pages, missions, flags, and chapter unlocks",
                        "terminal_archive_unlock",
                        List.of(
                                signalOs.archiveEntry().id(),
                                signalOs.dataDrive().id(),
                                signalOs.signalMessage().id(),
                                signalOs.storyFlag().id(),
                                signalOs.signalMessage().missionId(),
                                signalOs.chapterUnlock().id()
                        )
                ),
                new EchoStoryModuleReference(
                        "echospellcore",
                        "spell definitions and spell usage",
                        "spell_usage",
                        List.of(signalOs.spell().id())
                ),
                new EchoStoryModuleReference(
                        "echoritualcore",
                        "ritual definitions and ritual activation",
                        "ritual_activation",
                        List.of(signalOs.ritual().id(), signalOs.ritual().unlockFlagId())
                ),
                new EchoStoryModuleReference(
                        "echocursecore",
                        "curse definitions and curse effects",
                        "curse_effect",
                        List.of(signalOs.curse().id())
                ),
                new EchoStoryModuleReference(
                        "echoriftworlds",
                        "rift data and rift triggers",
                        "rift_trigger",
                        List.of(signalOs.riftEvent().id(), signalOs.riftEvent().chapterId())
                ),
                new EchoStoryModuleReference(
                        "echoblackboxprotocol",
                        "Blackbox archive data and Prime-route handoff",
                        "archive_unlock",
                        List.of(primeRoute.archiveEntry().id())
                ),
                new EchoStoryModuleReference(
                        "echoorbitalremnants",
                        "Orbital data drive reading",
                        "data_drive_reading",
                        List.of(primeRoute.dataDrive().id(), primeRoute.archiveEntry().id())
                ),
                new EchoStoryModuleReference(
                        "echonexusprotocol",
                        "Nexus signal message and mission hook",
                        "mission_hook",
                        List.of(primeRoute.signalMessage().id(), primeRoute.signalMessage().missionId())
                ),
                new EchoStoryModuleReference(
                        "echoprimecore",
                        "Prime story flag persistence and mission state",
                        "story_flag_persistence",
                        List.of(primeRoute.storyFlag().id(), primeRoute.signalMessage().missionId())
                ),
                new EchoStoryModuleReference(
                        "echostationfall",
                        "Stationfall chapter data and chapter unlock",
                        "chapter_unlock",
                        List.of(primeRoute.chapterUnlock().id())
                ),
                new EchoStoryModuleReference(
                        "echopresencelink",
                        "presence link data",
                        "presence_link",
                        List.of(signalOs.presenceLink().id(), primeRoute.presenceLink().id())
                ),
                new EchoStoryModuleReference(
                        "echogrimoire",
                        "Grimoire archive data",
                        "archive_unlock",
                        List.of(arcaneCodex.archiveEntry().id())
                ),
                new EchoStoryModuleReference(
                        "signalosexample",
                        "SignalOS example data drive reading",
                        "data_drive_reading",
                        List.of(arcaneCodex.dataDrive().id(), arcaneCodex.archiveEntry().id())
                ),
                new EchoStoryModuleReference(
                        "echoarcanacore",
                        "Arcana signal message, story flag, and mission hook",
                        "mission_hook",
                        List.of(
                                arcaneCodex.signalMessage().id(),
                                arcaneCodex.storyFlag().id(),
                                arcaneCodex.signalMessage().missionId()
                        )
                ),
                new EchoStoryModuleReference(
                        "echorelictech",
                        "relic definitions and relic effects",
                        "relic_effect",
                        List.of(signalOs.relicEffect().id(), arcaneCodex.relicEffect().id())
                ),
                new EchoStoryModuleReference(
                        "echoarcaneindex",
                        "Arcane Index chapter data and chapter unlock",
                        "chapter_unlock",
                        List.of(arcaneCodex.chapterUnlock().id())
                ),
                new EchoStoryModuleReference(
                        "echoaetherworks",
                        "AetherWorks presence link data",
                        "presence_link",
                        List.of(arcaneCodex.presenceLink().id())
                )
        );
    }

    public List<String> contentIds() {
        return List.of(
                archiveEntry.id(),
                dataDrive.id(),
                signalMessage.id(),
                storyFlag.id(),
                relicEffect.id(),
                spell.id(),
                ritual.id(),
                curse.id(),
                riftEvent.id(),
                chapterUnlock.id(),
                presenceLink.id()
        );
    }
}
