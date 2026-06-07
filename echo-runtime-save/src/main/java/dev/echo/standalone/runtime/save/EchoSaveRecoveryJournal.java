package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoSaveRecoveryJournal {
    private final Path journalPath;

    public EchoSaveRecoveryJournal(Path journalPath) {
        this.journalPath = Objects.requireNonNull(journalPath, "journalPath")
                .toAbsolutePath()
                .normalize();
    }

    public Path journalPath() {
        return journalPath;
    }

    public synchronized EchoSaveJournalEntry append(
            EchoSaveJournalEvent event,
            String transactionId,
            String message
    ) throws IOException {
        List<EchoSaveJournalEntry> existing = readAll();
        EchoSaveJournalEntry entry = new EchoSaveJournalEntry(
                existing.size() + 1,
                event,
                transactionId,
                message
        );
        Files.createDirectories(journalPath.getParent());
        Files.writeString(
                journalPath,
                encode(entry) + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        return entry;
    }

    public synchronized List<EchoSaveJournalEntry> readAll() throws IOException {
        if (!Files.isRegularFile(journalPath)) {
            return List.of();
        }
        ArrayList<EchoSaveJournalEntry> entries = new ArrayList<>();
        for (String line : Files.readAllLines(journalPath)) {
            if (!line.isBlank()) {
                entries.add(decode(line));
            }
        }
        return List.copyOf(entries);
    }

    private static String encode(EchoSaveJournalEntry entry) {
        return "%06d|%s|%s|%s".formatted(
                entry.sequence(),
                entry.event().name(),
                clean(entry.transactionId()),
                clean(entry.message())
        );
    }

    private static EchoSaveJournalEntry decode(String line) {
        String[] parts = line.split("\\|", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid save journal line: " + line);
        }
        return new EchoSaveJournalEntry(
                Integer.parseInt(parts[0]),
                EchoSaveJournalEvent.valueOf(parts[1]),
                parts[2],
                parts[3]
        );
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace('|', '/').replace('\n', ' ');
    }
}
