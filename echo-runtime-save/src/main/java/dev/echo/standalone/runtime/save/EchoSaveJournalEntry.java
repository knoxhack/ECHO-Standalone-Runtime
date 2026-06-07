package dev.echo.standalone.runtime.save;

import java.util.Objects;

public record EchoSaveJournalEntry(
        int sequence,
        EchoSaveJournalEvent event,
        String transactionId,
        String message
) {
    public EchoSaveJournalEntry {
        if (sequence < 1) {
            throw new IllegalArgumentException("sequence must be positive");
        }
        Objects.requireNonNull(event, "event");
        transactionId = transactionId == null ? "" : transactionId;
        message = message == null ? "" : message;
    }
}
