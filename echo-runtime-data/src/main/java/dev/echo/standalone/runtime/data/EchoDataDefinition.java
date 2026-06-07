package dev.echo.standalone.runtime.data;

import java.util.Map;
import java.util.Objects;

public record EchoDataDefinition(
        String id,
        String registryId,
        String sourceLogicalId,
        Map<String, Object> fields
) {
    public EchoDataDefinition {
        id = EchoDataPaths.requireText(id, "id");
        registryId = EchoDataPaths.requireText(registryId, "registryId");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        Objects.requireNonNull(fields, "fields");
        fields = EchoDataObjects.copyObject(fields);
    }
}
