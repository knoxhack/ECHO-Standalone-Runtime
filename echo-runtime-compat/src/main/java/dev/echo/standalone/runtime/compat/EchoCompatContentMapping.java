package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoCompatContentMapping(
        String mappingId,
        String sourceId,
        EchoCompatSourceKind sourceKind,
        String targetId,
        EchoCompatTargetKind targetKind,
        EchoCompatMappingStatus status,
        String notes
) {
    public EchoCompatContentMapping {
        mappingId = EchoCompatText.requireText(mappingId, "mappingId");
        sourceId = EchoCompatText.requireText(sourceId, "sourceId");
        Objects.requireNonNull(sourceKind, "sourceKind");
        targetId = EchoCompatText.requireText(targetId, "targetId");
        Objects.requireNonNull(targetKind, "targetKind");
        Objects.requireNonNull(status, "status");
        notes = EchoCompatText.optionalText(notes);
    }
}
