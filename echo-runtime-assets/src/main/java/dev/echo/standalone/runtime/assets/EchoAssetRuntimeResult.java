package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public record EchoAssetRuntimeResult(
        List<EchoAssetMount> mounts,
        EchoAssetIndex index,
        EchoAssetResolver resolver,
        List<EchoAssetConflict> conflicts,
        EchoAssetMissingReport missingReport
) {
    public EchoAssetRuntimeResult {
        Objects.requireNonNull(mounts, "mounts");
        Objects.requireNonNull(index, "index");
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(conflicts, "conflicts");
        Objects.requireNonNull(missingReport, "missingReport");
        mounts = List.copyOf(mounts);
        conflicts = List.copyOf(conflicts);
    }
}
