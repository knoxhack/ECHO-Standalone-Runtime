package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public final class EchoAssetMissingDetector {
    public EchoAssetMissingReport detect(EchoAssetIndex index, List<String> requiredLogicalIds) {
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(requiredLogicalIds, "requiredLogicalIds");
        List<String> missing = requiredLogicalIds.stream()
                .sorted()
                .filter(id -> index.resolve(id).isEmpty())
                .toList();
        return new EchoAssetMissingReport(missing);
    }
}
