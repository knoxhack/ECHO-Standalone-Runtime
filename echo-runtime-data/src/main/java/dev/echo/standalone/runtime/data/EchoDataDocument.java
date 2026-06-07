package dev.echo.standalone.runtime.data;

import java.util.Map;
import java.util.Objects;

public record EchoDataDocument(
        String logicalId,
        String namespace,
        String category,
        String relativePath,
        String source,
        Map<String, Object> object
) {
    public EchoDataDocument {
        logicalId = EchoDataPaths.requireText(logicalId, "logicalId");
        namespace = EchoDataPaths.requireText(namespace, "namespace");
        category = EchoDataPaths.requireText(category, "category");
        relativePath = EchoDataPaths.requireText(relativePath, "relativePath");
        source = EchoDataPaths.requireText(source, "source");
        Objects.requireNonNull(object, "object");
        object = EchoDataObjects.copyObject(object);
    }
}
