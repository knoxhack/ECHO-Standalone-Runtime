package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.contracts.story.EchoChapterUnlock;
import dev.echo.standalone.runtime.contracts.story.EchoCurse;
import dev.echo.standalone.runtime.contracts.story.EchoDataDrive;
import dev.echo.standalone.runtime.contracts.story.EchoPresenceLink;
import dev.echo.standalone.runtime.contracts.story.EchoRelicEffect;
import dev.echo.standalone.runtime.contracts.story.EchoRiftEvent;
import dev.echo.standalone.runtime.contracts.story.EchoRitual;
import dev.echo.standalone.runtime.contracts.story.EchoSignalMessage;
import dev.echo.standalone.runtime.contracts.story.EchoSpell;
import dev.echo.standalone.runtime.contracts.story.EchoStoryAdapterCoreContract;
import dev.echo.standalone.runtime.contracts.story.EchoStoryFlag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EchoStandaloneStoryRuntime {
    private boolean terminalOpen;
    private boolean dataDriveRead;
    private boolean missionStarted;
    private String activeMissionId = "";
    private final Set<String> unlockedArchiveIds = new LinkedHashSet<>();
    private final Set<String> unlockedChapterIds = new LinkedHashSet<>();
    private final Map<String, Boolean> flags = new LinkedHashMap<>();
    private final Map<String, Integer> gameplayStats = new LinkedHashMap<>();
    private final ArrayList<String> loreUpdates = new ArrayList<>();
    private final ArrayList<String> presenceLinks = new ArrayList<>();

    public static EchoStandaloneStoryRuntime load(EchoStorySaveState saveState) {
        EchoStandaloneStoryRuntime runtime = new EchoStandaloneStoryRuntime();
        runtime.terminalOpen = saveState.terminalOpen();
        runtime.dataDriveRead = saveState.dataDriveRead();
        runtime.missionStarted = saveState.missionStarted();
        runtime.activeMissionId = saveState.activeMissionId();
        runtime.unlockedArchiveIds.addAll(saveState.unlockedArchiveIds());
        runtime.unlockedChapterIds.addAll(saveState.unlockedChapterIds());
        runtime.flags.putAll(saveState.flags());
        runtime.gameplayStats.putAll(saveState.gameplayStats());
        runtime.loreUpdates.addAll(saveState.loreUpdates());
        runtime.presenceLinks.addAll(saveState.presenceLinks());
        return runtime;
    }

    public List<EchoSignalMessage> openSignalOsTerminal(EchoStoryAdapterCoreContract contract) {
        terminalOpen = true;
        addLoreUpdate("terminal:signalos");
        addLoreUpdate("index:signalos");
        addLoreUpdate("signal:" + contract.signalMessage().id());
        return List.of(contract.signalMessage());
    }

    public void readDataDrive(EchoDataDrive drive) {
        dataDriveRead = true;
        unlockedArchiveIds.addAll(drive.archiveEntryIds());
        for (EchoStoryFlag flag : drive.flagsToSet()) {
            flags.put(flag.id(), flag.value());
        }
        addLoreUpdate("wiki:signalos/data_drives");
        addLoreUpdate("lore:" + drive.id());
    }

    public void startStoryMission(EchoSignalMessage message) {
        requireTerminalOpen();
        missionStarted = true;
        activeMissionId = message.missionId();
        addLoreUpdate("signal:" + message.id());
        addLoreUpdate("mission:" + activeMissionId);
    }

    public void applyRelicEffect(EchoRelicEffect relicEffect) {
        requireArchiveUnlocked(relicEffect.archiveEntryId());
        mutateStat(relicEffect.gameplayStat(), relicEffect.delta());
        addLoreUpdate("relic:" + relicEffect.id());
    }

    public void castSpell(EchoSpell spell) {
        mutateStat(spell.gameplayStat(), spell.delta());
        addLoreUpdate("spell:" + spell.id());
    }

    public void activateRitual(EchoRitual ritual) {
        mutateStat(ritual.gameplayStat(), ritual.delta());
        flags.put(ritual.unlockFlagId(), true);
        addLoreUpdate("ritual:" + ritual.id());
    }

    public void applyCurse(EchoCurse curse) {
        mutateStat(curse.gameplayStat(), curse.delta());
        addLoreUpdate("curse:" + curse.id());
    }

    public void triggerRift(EchoRiftEvent riftEvent) {
        flags.put(riftEvent.unlockFlagId(), true);
        addLoreUpdate("rift:" + riftEvent.id());
        addLoreUpdate("chapter-route:" + riftEvent.chapterId());
    }

    public boolean unlockChapter(EchoChapterUnlock chapterUnlock) {
        if (!Boolean.TRUE.equals(flags.get(chapterUnlock.requiredFlagId()))) {
            return false;
        }
        unlockedChapterIds.add(chapterUnlock.id());
        addLoreUpdate("chapter:" + chapterUnlock.id());
        return true;
    }

    public void linkPresence(EchoPresenceLink presenceLink) {
        presenceLinks.add(presenceLink.id() + "=" + presenceLink.state());
        addLoreUpdate("presence:" + presenceLink.id());
    }

    public EchoStorySaveState save() {
        return new EchoStorySaveState(
                terminalOpen,
                dataDriveRead,
                missionStarted,
                activeMissionId,
                List.copyOf(unlockedArchiveIds),
                List.copyOf(unlockedChapterIds),
                Map.copyOf(flags),
                Map.copyOf(gameplayStats),
                List.copyOf(loreUpdates),
                List.copyOf(presenceLinks)
        );
    }

    public boolean terminalOpen() {
        return terminalOpen;
    }

    public boolean dataDriveRead() {
        return dataDriveRead;
    }

    public boolean missionStarted() {
        return missionStarted;
    }

    public String activeMissionId() {
        return activeMissionId;
    }

    public Set<String> unlockedArchiveIds() {
        return Set.copyOf(unlockedArchiveIds);
    }

    public Set<String> unlockedChapterIds() {
        return Set.copyOf(unlockedChapterIds);
    }

    public Map<String, Boolean> flags() {
        return Map.copyOf(flags);
    }

    public Map<String, Integer> gameplayStats() {
        return Map.copyOf(gameplayStats);
    }

    public List<String> loreUpdates() {
        return List.copyOf(loreUpdates);
    }

    public List<String> presenceLinks() {
        return List.copyOf(presenceLinks);
    }

    private void requireTerminalOpen() {
        if (!terminalOpen) {
            throw new IllegalStateException("SignalOS terminal must be open before starting a story mission");
        }
    }

    private void requireArchiveUnlocked(String archiveEntryId) {
        if (!unlockedArchiveIds.contains(archiveEntryId)) {
            throw new IllegalStateException("Archive entry is locked: " + archiveEntryId);
        }
    }

    private void mutateStat(String stat, int delta) {
        gameplayStats.merge(stat, delta, Integer::sum);
    }

    private void addLoreUpdate(String update) {
        if (!loreUpdates.contains(update)) {
            loreUpdates.add(update);
        }
    }
}
