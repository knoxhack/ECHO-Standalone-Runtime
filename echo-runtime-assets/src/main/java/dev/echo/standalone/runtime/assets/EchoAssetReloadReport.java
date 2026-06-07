package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public record EchoAssetReloadReport(
        EchoAssetIndex nextIndex,
        List<String> added,
        List<String> removed,
        List<String> changed
) {
    public EchoAssetReloadReport {
        Objects.requireNonNull(nextIndex, "nextIndex");
        Objects.requireNonNull(added, "added");
        Objects.requireNonNull(removed, "removed");
        Objects.requireNonNull(changed, "changed");
        added = added.stream().sorted().toList();
        removed = removed.stream().sorted().toList();
        changed = changed.stream().sorted().toList();
    }
}
