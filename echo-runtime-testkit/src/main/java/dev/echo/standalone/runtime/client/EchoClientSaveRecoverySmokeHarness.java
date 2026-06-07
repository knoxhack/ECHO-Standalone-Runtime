package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientSaveRecoverySmokeHarness {
    private EchoClientSaveRecoverySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-save-recovery-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        services.startNewWorld("recovery-good", "Recovery Good");
        String goodSlotId = services.worldSession().slotId();
        services.startNewWorld("recovery-data", "Recovery Data");
        String corruptDataSlotId = services.worldSession().slotId();
        Path corruptDataPath = saveRoot.resolve("slots")
                .resolve(corruptDataSlotId)
                .resolve("data")
                .resolve(EchoClientGameplaySaveCodec.PLAYER_PATH);
        Files.writeString(corruptDataPath, "corrupt tracked player data");

        String corruptSlotId = "corrupt-manifest";
        Path corruptSlotRoot = saveRoot.resolve("slots").resolve(corruptSlotId);
        Files.createDirectories(corruptSlotRoot.resolve("data"));
        Files.writeString(corruptSlotRoot.resolve("manifest.json"), "{ invalid manifest json");

        List<EchoClientSaveSlotSummary> summaries = services.saveSlotSummaries();
        EchoClientSaveSlotSummary goodSlot = summaries.stream()
                .filter(slot -> slot.slotId().equals(goodSlotId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Good save slot should remain listed"));
        EchoClientSaveSlotSummary corruptDataSlot = summaries.stream()
                .filter(slot -> slot.slotId().equals(corruptDataSlotId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Data-corrupt save slot should be listed for recovery"));
        EchoClientSaveSlotSummary corruptSlot = summaries.stream()
                .filter(slot -> slot.slotId().equals(corruptSlotId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Corrupt save slot should be listed for recovery"));
        require(goodSlot.loadableInMemory() && !goodSlot.recoveryRequired(),
                "Good save should remain loadable while another slot is corrupt");
        require(corruptSlot.recoveryRequired()
                        && !corruptSlot.loadableInMemory()
                        && corruptSlot.menuLabel().contains("Recovery required")
                        && corruptSlot.detail().contains("manifest unreadable"),
                "Corrupt manifest should produce a recovery summary instead of aborting the scan");
        require(corruptDataSlot.recoveryRequired()
                        && !corruptDataSlot.loadableInMemory()
                        && corruptDataSlot.menuLabel().contains("Recovery required")
                        && corruptDataSlot.detail().contains("CHECKSUM_MISMATCH")
                        && corruptDataSlot.detail().contains(EchoClientGameplaySaveCodec.PLAYER_PATH),
                "Data-corrupt save should produce a checksum recovery summary instead of remaining loadable");

        EchoClientScreenController screens = new EchoClientScreenController();
        screens.showMainMenu(true);
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORLD_SELECT, true),
                "Load Game should open World Select");
        screens.updateSaveSlots(summaries, services.saveSlotError());
        require(screens.selectedSaveSlotId().equals(goodSlotId),
                "World Select continue target should remain the loadable save");

        selectLabel(screens, corruptDataSlot.menuLabel(), false);
        EchoClientScreenSnapshot dataRecoverySnapshot = screens.snapshot(true);
        require(screens.selectedManageSaveSlotId().equals(corruptDataSlotId),
                "World Select should allow selecting the data-corrupt slot for management");
        require(screens.selectedSaveSlotId().equals(goodSlotId),
                "Selecting a data-corrupt slot must not make it the continue target");
        require(option(dataRecoverySnapshot, corruptDataSlot.menuLabel()).enabled()
                        && option(dataRecoverySnapshot, corruptDataSlot.menuLabel()).command() == EchoClientScreenCommand.NONE,
                "Data-corrupt save row should be selectable but not continue-able");
        require(optionLabelContains(dataRecoverySnapshot, "CHECKSUM_MISMATCH"),
                "World Select should expose the checksum mismatch recovery issue");
        require(!option(dataRecoverySnapshot, "Backup And Migration").enabled(),
                "Data-corrupt recovery slot should not offer backup/migration");
        require(!option(dataRecoverySnapshot, "Rename World").enabled(),
                "Data-corrupt recovery slot should not offer rename");
        require(option(dataRecoverySnapshot, "Delete World").enabled(),
                "Data-corrupt recovery slot should offer delete");

        selectLabel(screens, corruptSlot.menuLabel(), false);
        EchoClientScreenSnapshot recoverySnapshot = screens.snapshot(true);
        require(screens.selectedManageSaveSlotId().equals(corruptSlotId),
                "World Select should allow selecting the corrupt slot for management");
        require(screens.selectedSaveSlotId().equals(goodSlotId),
                "Selecting a corrupt slot must not make it the continue target");
        require(option(recoverySnapshot, corruptSlot.menuLabel()).enabled()
                        && option(recoverySnapshot, corruptSlot.menuLabel()).command() == EchoClientScreenCommand.NONE,
                "Corrupt save row should be selectable but not continue-able");
        require(optionLabelPrefix(recoverySnapshot, "Review Recovery required"),
                "World Select should expose recovery review details for the corrupt slot");
        require(!option(recoverySnapshot, "Backup And Migration").enabled(),
                "Recovery slot should not offer backup/migration until its manifest is readable");
        require(!option(recoverySnapshot, "Rename World").enabled(),
                "Recovery slot should not offer rename until its manifest is readable");
        require(option(recoverySnapshot, "Delete World").enabled(),
                "Recovery slot should offer delete so the broken world can be cleared");

        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientGameplayRuntimeController gameplayRuntime =
                new EchoClientGameplayRuntimeController(services, screens, worldSessions);
        EchoClientCommandController commands =
                new EchoClientCommandController(services, screens, worldSessions, gameplayRuntime, new RecordingHost());
        require(commands.execute(EchoClientScreenCommand.DELETE_SELECTED_WORLD),
                "Delete World should delete the selected recovery slot");
        require(!Files.exists(corruptSlotRoot),
                "Deleting a recovery slot should remove its broken slot directory");
        require(services.saveSlotSummaries().stream()
                        .anyMatch(slot -> slot.slotId().equals(goodSlotId) && slot.loadableInMemory()),
                "Deleting the recovery slot should leave the good save loadable");

        System.out.println("client save recovery smoke PASS good=" + goodSlotId + " recovered=" + corruptSlotId);
    }

    private static EchoClientScreenOption option(EchoClientScreenSnapshot snapshot, String label) {
        return snapshot.options().stream()
                .filter(option -> option.label().equals(label))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Expected option " + label));
    }

    private static boolean optionLabelPrefix(EchoClientScreenSnapshot snapshot, String prefix) {
        return snapshot.options().stream().anyMatch(option -> option.label().startsWith(prefix));
    }

    private static boolean optionLabelContains(EchoClientScreenSnapshot snapshot, String text) {
        return snapshot.options().stream().anyMatch(option -> option.label().contains(text));
    }

    private static void selectLabel(EchoClientScreenController screens, String label, boolean hasSession) {
        for (int attempt = 0; attempt < 64; attempt++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(hasSession);
            if (snapshot.selectedIndex() >= 0
                    && snapshot.selectedIndex() < snapshot.options().size()
                    && snapshot.options().get(snapshot.selectedIndex()).label().equals(label)) {
                return;
            }
            screens.moveSelection(1, hasSession, 720);
        }
        throw new AssertionError("Could not select " + label);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private static final class RecordingHost implements EchoClientCommandController.Host {
        @Override
        public void attachSession() {
        }

        @Override
        public void beginSaving() {
        }

        @Override
        public void unlockCursor() {
        }

        @Override
        public void requestClose() {
        }

        @Override
        public void reloadMinecraftAssets(boolean rebuildAtlas) {
        }
    }
}
