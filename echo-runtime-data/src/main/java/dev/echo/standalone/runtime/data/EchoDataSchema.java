package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoDataSchema(
        String schemaId,
        String registryId,
        List<String> requiredFields,
        String sourceLogicalId
) {
    public EchoDataSchema {
        schemaId = EchoDataPaths.requireText(schemaId, "schemaId");
        registryId = EchoDataPaths.requireText(registryId, "registryId");
        Objects.requireNonNull(requiredFields, "requiredFields");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        requiredFields = requiredFields.stream().sorted().toList();
    }
}
