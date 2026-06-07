package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoMissionDefinition(
        String id,
        String chapterId,
        String title,
        List<String> objectives,
        List<String> references,
        String sourceLogicalId
) {
    public EchoMissionDefinition {
        id = EchoDataPaths.requireText(id, "id");
        chapterId = EchoDataPaths.requireText(chapterId, "chapterId");
        title = EchoDataPaths.requireText(title, "title");
        Objects.requireNonNull(objectives, "objectives");
        Objects.requireNonNull(references, "references");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        objectives = objectives.stream().sorted().toList();
        references = references.stream().sorted().toList();
    }
}
