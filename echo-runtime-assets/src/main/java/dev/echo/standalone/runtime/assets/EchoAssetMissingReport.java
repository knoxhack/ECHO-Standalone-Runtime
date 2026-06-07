package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public record EchoAssetMissingReport(List<String> missingLogicalIds) {
    public EchoAssetMissingReport {
        Objects.requireNonNull(missingLogicalIds, "missingLogicalIds");
        missingLogicalIds = missingLogicalIds.stream().sorted().toList();
    }
}
