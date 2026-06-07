package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoSaveCorruptionChecker {
    private final EchoSaveManifestCodec manifestCodec;
    private final EchoSaveChecksum checksum;

    public EchoSaveCorruptionChecker() {
        this(new EchoSaveManifestCodec(), new EchoSaveChecksum());
    }

    public EchoSaveCorruptionChecker(EchoSaveManifestCodec manifestCodec, EchoSaveChecksum checksum) {
        this.manifestCodec = Objects.requireNonNull(manifestCodec, "manifestCodec");
        this.checksum = Objects.requireNonNull(checksum, "checksum");
    }

    public EchoSaveCorruptionReport check(EchoSaveSlot slot, EchoSaveRecoveryJournal journal) throws IOException {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(journal, "journal");
        ArrayList<EchoSaveCorruptionIssue> issues = new ArrayList<>();
        int checkedFiles = 0;

        if (!Files.isRegularFile(slot.manifestPath())) {
            issues.add(new EchoSaveCorruptionIssue(
                    EchoSaveCorruptionSeverity.ERROR,
                    "MISSING_MANIFEST",
                    slot.manifestPath().toString(),
                    "Save slot manifest is missing"
            ));
        } else {
            try {
                EchoSaveManifest manifest = manifestCodec.read(slot.manifestPath());
                for (EchoSaveFileState file : manifest.files()) {
                    checkedFiles++;
                    var path = slot.dataRoot().resolve(file.relativePath());
                    if (!Files.isRegularFile(path)) {
                        issues.add(new EchoSaveCorruptionIssue(
                                EchoSaveCorruptionSeverity.ERROR,
                                "MISSING_FILE",
                                file.relativePath(),
                                "Manifest file is missing from save data"
                        ));
                    } else {
                        String actualChecksum = checksum.sha256(path);
                        if (!actualChecksum.equals(file.checksumSha256())) {
                            issues.add(new EchoSaveCorruptionIssue(
                                    EchoSaveCorruptionSeverity.ERROR,
                                    "CHECKSUM_MISMATCH",
                                    file.relativePath(),
                                    "Manifest checksum does not match save data"
                            ));
                        }
                    }
                }
            } catch (RuntimeException exception) {
                issues.add(new EchoSaveCorruptionIssue(
                        EchoSaveCorruptionSeverity.ERROR,
                        "INVALID_MANIFEST",
                        slot.manifestPath().toString(),
                        exception.getMessage()
                ));
            }
        }

        if (Files.isDirectory(slot.transactionsRoot()) && hasChildren(slot.transactionsRoot())) {
            issues.add(new EchoSaveCorruptionIssue(
                    EchoSaveCorruptionSeverity.WARNING,
                    "DIRTY_TRANSACTION",
                    slot.transactionsRoot().toString(),
                    "Unfinished transaction staging directory is present"
            ));
        }

        journal.append(EchoSaveJournalEvent.CORRUPTION_CHECKED, slot.slotId(), "issues=" + issues.size());
        List<EchoSaveJournalEntry> entries = journal.readAll();
        return new EchoSaveCorruptionReport(
                slot.profile().profileId(),
                slot.slotId(),
                issues.stream().noneMatch(issue -> issue.severity() == EchoSaveCorruptionSeverity.ERROR),
                checkedFiles,
                entries.size(),
                issues
        );
    }

    private static boolean hasChildren(java.nio.file.Path root) throws IOException {
        try (var stream = Files.list(root)) {
            return stream.findAny().isPresent();
        }
    }
}
