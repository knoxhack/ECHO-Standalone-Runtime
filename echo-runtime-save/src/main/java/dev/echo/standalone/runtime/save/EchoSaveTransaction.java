package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class EchoSaveTransaction {
    private static final String FIXED_TIME = "1970-01-01T00:00:00Z";

    private final EchoSaveSlot slot;
    private final String transactionId;
    private final EchoSaveRecoveryJournal journal;
    private final EchoSaveBackupService backupService;
    private final EchoSaveManifestCodec manifestCodec;
    private final EchoSaveChecksum checksum;
    private final LinkedHashMap<String, String> stagedTextFiles = new LinkedHashMap<>();
    private final LinkedHashMap<String, byte[]> stagedBinaryFiles = new LinkedHashMap<>();
    private boolean completed;

    EchoSaveTransaction(
            EchoSaveSlot slot,
            String transactionId,
            EchoSaveRecoveryJournal journal,
            EchoSaveBackupService backupService,
            EchoSaveManifestCodec manifestCodec,
            EchoSaveChecksum checksum
    ) {
        this.slot = Objects.requireNonNull(slot, "slot");
        this.transactionId = EchoSavePaths.requireText(transactionId, "transactionId");
        this.journal = Objects.requireNonNull(journal, "journal");
        this.backupService = Objects.requireNonNull(backupService, "backupService");
        this.manifestCodec = Objects.requireNonNull(manifestCodec, "manifestCodec");
        this.checksum = Objects.requireNonNull(checksum, "checksum");
    }

    public EchoSaveTransaction writeText(String relativePath, String content) {
        ensureOpen();
        stagedTextFiles.put(
                EchoSavePaths.requireRelativePath(relativePath, "relativePath"),
                content == null ? "" : content
        );
        return this;
    }

    public EchoSaveTransaction writeBytes(String relativePath, byte[] content) {
        ensureOpen();
        stagedBinaryFiles.put(
                EchoSavePaths.requireRelativePath(relativePath, "relativePath"),
                content == null ? new byte[0] : content.clone()
        );
        return this;
    }

    public EchoSaveCommitResult commit(Map<String, String> metadata) throws IOException {
        ensureOpen();
        Objects.requireNonNull(metadata, "metadata");
        Path transactionRoot = slot.transactionsRoot().resolve(transactionId);
        Path stagedDataRoot = transactionRoot.resolve("staged-data");
        Path publishDataRoot = transactionRoot.resolve("publish-data");
        Path previousDataRoot = transactionRoot.resolve("previous-data");
        Path tempManifest = transactionRoot.resolve("manifest.json.tmp");
        EchoSaveFiles.deleteRecursive(transactionRoot);
        Files.createDirectories(stagedDataRoot);

        for (Map.Entry<String, String> entry : stagedTextFiles.entrySet()) {
            Path stagedFile = stagedDataRoot.resolve(entry.getKey());
            Files.createDirectories(stagedFile.getParent());
            Files.writeString(stagedFile, entry.getValue());
        }
        for (Map.Entry<String, byte[]> entry : stagedBinaryFiles.entrySet()) {
            Path stagedFile = stagedDataRoot.resolve(entry.getKey());
            Files.createDirectories(stagedFile.getParent());
            Files.write(stagedFile, entry.getValue());
        }
        journal.append(EchoSaveJournalEvent.STAGED, transactionId, "files=" + stagedFileCount());

        Optional<EchoSaveManifest> previousManifest = readPreviousManifest();
        Optional<EchoSaveBackup> backup = backupService.createBackupIfPresent(slot, transactionId, journal);

        ArrayList<String> backupIds = new ArrayList<>();
        previousManifest.ifPresent(manifest -> backupIds.addAll(manifest.backupIds()));
        backup.ifPresent(value -> backupIds.add(value.backupId()));

        TreeMap<String, String> mergedMetadata = new TreeMap<>(slot.profile().metadata());
        mergedMetadata.putAll(metadata);
        preparePublishData(stagedDataRoot, publishDataRoot);
        EchoSaveManifest manifest = new EchoSaveManifest(
                "echo.standalone.save_manifest.v1",
                slot.profile().profileId(),
                slot.slotId(),
                slot.profile().packId(),
                slot.profile().formatVersion(),
                previousManifest.map(EchoSaveManifest::createdAt).orElse(FIXED_TIME),
                FIXED_TIME,
                scanDataFiles(publishDataRoot),
                backupIds,
                mergedMetadata
        );
        manifestCodec.write(tempManifest, manifest);
        publishPreparedData(publishDataRoot, previousDataRoot, tempManifest);
        EchoSaveFiles.deleteRecursive(transactionRoot);
        completed = true;
        journal.append(EchoSaveJournalEvent.COMMITTED, transactionId, "files=" + stagedFileCount());
        return new EchoSaveCommitResult(slot, manifest, backup, stagedFileCount());
    }

    public void rollback() throws IOException {
        if (completed) {
            return;
        }
        EchoSaveFiles.deleteRecursive(slot.transactionsRoot().resolve(transactionId));
        completed = true;
        journal.append(EchoSaveJournalEvent.ROLLED_BACK, transactionId, "rollback");
    }

    public String transactionId() {
        return transactionId;
    }

    public EchoSaveSlot slot() {
        return slot;
    }

    private Optional<EchoSaveManifest> readPreviousManifest() throws IOException {
        if (!Files.isRegularFile(slot.manifestPath())) {
            return Optional.empty();
        }
        return Optional.of(manifestCodec.read(slot.manifestPath()));
    }

    private void preparePublishData(Path stagedDataRoot, Path publishDataRoot) throws IOException {
        EchoSaveFiles.deleteRecursive(publishDataRoot);
        if (Files.exists(slot.dataRoot())) {
            EchoSaveFiles.copyRecursive(slot.dataRoot(), publishDataRoot);
        } else {
            Files.createDirectories(publishDataRoot);
        }
        EchoSaveFiles.copyRecursive(stagedDataRoot, publishDataRoot);
    }

    private void publishPreparedData(Path publishDataRoot, Path previousDataRoot, Path tempManifest) throws IOException {
        boolean previousDataMoved = false;
        boolean publishedDataMoved = false;
        try {
            if (Files.exists(slot.dataRoot())) {
                Files.move(slot.dataRoot(), previousDataRoot);
                previousDataMoved = true;
            }
            Files.move(publishDataRoot, slot.dataRoot());
            publishedDataMoved = true;
            Files.move(tempManifest, slot.manifestPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            try {
                restorePreviousData(previousDataRoot, previousDataMoved, publishedDataMoved);
            } catch (IOException restoreException) {
                exception.addSuppressed(restoreException);
            }
            throw exception;
        }
    }

    private void restorePreviousData(
            Path previousDataRoot,
            boolean previousDataMoved,
            boolean publishedDataMoved
    ) throws IOException {
        if (previousDataMoved) {
            EchoSaveFiles.deleteRecursive(slot.dataRoot());
            Files.move(previousDataRoot, slot.dataRoot());
        } else if (publishedDataMoved) {
            EchoSaveFiles.deleteRecursive(slot.dataRoot());
        }
    }

    private ArrayList<EchoSaveFileState> scanDataFiles(Path dataRoot) throws IOException {
        ArrayList<EchoSaveFileState> files = new ArrayList<>();
        if (!Files.exists(dataRoot)) {
            return files;
        }
        try (var stream = Files.walk(dataRoot)) {
            for (Path path : stream.filter(Files::isRegularFile).sorted().toList()) {
                String relativePath = dataRoot.relativize(path).toString().replace('\\', '/');
                files.add(new EchoSaveFileState(
                        relativePath,
                        checksum.sha256(path),
                        Files.size(path)
                ));
            }
        }
        return files;
    }

    private void ensureOpen() {
        if (completed) {
            throw new IllegalStateException("Save transaction already completed: " + transactionId);
        }
    }

    private int stagedFileCount() {
        return stagedTextFiles.size() + stagedBinaryFiles.size();
    }
}
