package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoCompatSourceRecord(
        String recordId,
        String sourceId,
        EchoCompatSourceKind sourceKind,
        String recordType,
        String fingerprint
) {
    public EchoCompatSourceRecord {
        recordId = EchoCompatText.requireText(recordId, "recordId");
        sourceId = EchoCompatText.requireText(sourceId, "sourceId");
        Objects.requireNonNull(sourceKind, "sourceKind");
        recordType = EchoCompatText.requireText(recordType, "recordType");
        fingerprint = EchoCompatText.requireText(fingerprint, "fingerprint");
    }
}
