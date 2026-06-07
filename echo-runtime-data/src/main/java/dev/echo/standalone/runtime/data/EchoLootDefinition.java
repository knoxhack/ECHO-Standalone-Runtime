package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoLootDefinition(
        String id,
        List<String> entries,
        String sourceLogicalId
) {
    public EchoLootDefinition {
        id = EchoDataPaths.requireText(id, "id");
        Objects.requireNonNull(entries, "entries");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        entries = entries.stream().sorted().toList();
    }
}
