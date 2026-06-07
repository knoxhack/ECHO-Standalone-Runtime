package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public final class EchoAssetConflictDetector {
    public List<EchoAssetConflict> detect(EchoAssetIndex index) {
        Objects.requireNonNull(index, "index");
        return index.entriesByLogicalId().entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(entry -> new EchoAssetConflict(entry.getKey(), entry.getValue()))
                .sorted(java.util.Comparator.comparing(EchoAssetConflict::logicalId))
                .toList();
    }
}
