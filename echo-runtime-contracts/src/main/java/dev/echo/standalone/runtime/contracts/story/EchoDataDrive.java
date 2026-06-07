package dev.echo.standalone.runtime.contracts.story;

import java.util.List;

public record EchoDataDrive(
        String id,
        String label,
        List<String> archiveEntryIds,
        List<EchoStoryFlag> flagsToSet
) {
    public EchoDataDrive {
        id = EchoStoryText.requireText(id, "id");
        label = EchoStoryText.requireText(label, "label");
        archiveEntryIds = List.copyOf(archiveEntryIds);
        flagsToSet = List.copyOf(flagsToSet);
    }
}
