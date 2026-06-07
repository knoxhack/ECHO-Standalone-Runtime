package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

public final class EchoSaveBackupService {
    private final EchoSaveChecksum checksum;

    public EchoSaveBackupService() {
        this(new EchoSaveChecksum());
    }

    public EchoSaveBackupService(EchoSaveChecksum checksum) {
        this.checksum = Objects.requireNonNull(checksum, "checksum");
    }

    public Optional<EchoSaveBackup> createBackupIfPresent(
            EchoSaveSlot slot,
            String transactionId,
            EchoSaveRecoveryJournal journal
    ) throws IOException {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(journal, "journal");
        if (!Files.isRegularFile(slot.manifestPath())) {
            return Optional.empty();
        }

        String backupId = slot.slotId() + "-" + EchoSavePaths.requireText(transactionId, "transactionId");
        Path backupRoot = slot.profile().root().resolve("backups").resolve(backupId);
        EchoSaveFiles.deleteRecursive(backupRoot);
        Files.createDirectories(backupRoot);
        Files.copy(slot.manifestPath(), backupRoot.resolve("manifest.json"));
        EchoSaveFiles.copyRecursive(slot.dataRoot(), backupRoot.resolve("data"));
        int dataFileCount = countFiles(backupRoot.resolve("data"));
        EchoSaveBackup backup = new EchoSaveBackup(
                backupId,
                slot.slotId(),
                backupRoot,
                checksum.sha256(backupRoot.resolve("manifest.json")),
                dataFileCount
        );
        journal.append(EchoSaveJournalEvent.BACKUP_CREATED, transactionId, backupId);
        return Optional.of(backup);
    }

    public EchoSaveBackup restoreBackup(
            EchoSaveSlot slot,
            String backupId,
            String restoreTransactionId,
            EchoSaveRecoveryJournal journal
    ) throws IOException {
        Objects.requireNonNull(slot, "slot");
        backupId = EchoSavePaths.requireText(backupId, "backupId");
        restoreTransactionId = EchoSavePaths.requireText(restoreTransactionId, "restoreTransactionId");
        Objects.requireNonNull(journal, "journal");

        Path backupRoot = slot.profile().root().resolve("backups").resolve(backupId);
        Path backupManifest = backupRoot.resolve("manifest.json");
        Path backupData = backupRoot.resolve("data");
        if (!Files.isRegularFile(backupManifest)) {
            throw new IOException("Cannot restore missing save backup manifest: " + backupManifest);
        }

        Files.createDirectories(slot.root());
        EchoSaveFiles.deleteRecursive(slot.dataRoot());
        Files.copy(backupManifest, slot.manifestPath(), StandardCopyOption.REPLACE_EXISTING);
        EchoSaveFiles.copyRecursive(backupData, slot.dataRoot());

        EchoSaveBackup restored = new EchoSaveBackup(
                backupId,
                slot.slotId(),
                backupRoot,
                checksum.sha256(backupManifest),
                countFiles(backupData)
        );
        journal.append(EchoSaveJournalEvent.BACKUP_RESTORED, restoreTransactionId, backupId);
        return restored;
    }

    private static int countFiles(Path root) throws IOException {
        if (!Files.exists(root)) {
            return 0;
        }
        try (var stream = Files.walk(root)) {
            return (int) stream.filter(Files::isRegularFile).count();
        }
    }
}
