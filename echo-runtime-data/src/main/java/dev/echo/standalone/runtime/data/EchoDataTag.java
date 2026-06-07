package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoDataTag(
        String id,
        String registryId,
        List<String> values,
        String sourceLogicalId
) {
    public EchoDataTag {
        id = EchoDataPaths.requireText(id, "id");
        registryId = EchoDataPaths.requireText(registryId, "registryId");
        Objects.requireNonNull(values, "values");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        values = values.stream().sorted().toList();
    }
}
