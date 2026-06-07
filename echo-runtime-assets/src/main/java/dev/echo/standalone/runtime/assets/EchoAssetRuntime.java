package dev.echo.standalone.runtime.assets;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public final class EchoAssetRuntime {
    private final List<EchoAssetMount> mounts;
    private final EchoAssetIndexBuilder indexBuilder;
    private final EchoAssetConflictDetector conflictDetector;
    private final EchoAssetMissingDetector missingDetector;

    public EchoAssetRuntime(List<EchoAssetMount> mounts) {
        this(
                mounts,
                new EchoAssetIndexBuilder(),
                new EchoAssetConflictDetector(),
                new EchoAssetMissingDetector()
        );
    }

    public EchoAssetRuntime(
            List<EchoAssetMount> mounts,
            EchoAssetIndexBuilder indexBuilder,
            EchoAssetConflictDetector conflictDetector,
            EchoAssetMissingDetector missingDetector
    ) {
        Objects.requireNonNull(mounts, "mounts");
        this.mounts = mounts.stream()
                .sorted(java.util.Comparator.comparingInt(EchoAssetMount::order))
                .toList();
        this.indexBuilder = Objects.requireNonNull(indexBuilder, "indexBuilder");
        this.conflictDetector = Objects.requireNonNull(conflictDetector, "conflictDetector");
        this.missingDetector = Objects.requireNonNull(missingDetector, "missingDetector");
    }

    public List<EchoAssetMount> mounts() {
        return mounts;
    }

    public EchoAssetRuntimeResult load(EchoRuntimeServiceRegistry services, List<String> requiredLogicalIds) throws IOException {
        Objects.requireNonNull(services, "services");
        EchoAssetIndex index = index();
        EchoAssetResolver resolver = new EchoAssetResolver(index, new EchoAssetLoader());
        List<EchoAssetConflict> conflicts = conflictDetector.detect(index);
        EchoAssetMissingReport missing = missingDetector.detect(index, requiredLogicalIds);
        EchoAssetRuntimeResult result = new EchoAssetRuntimeResult(mounts, index, resolver, conflicts, missing);
        services.register(EchoAssetRuntimeResult.class, result);
        services.register(EchoAssetIndex.class, index);
        services.register(EchoAssetResolver.class, resolver);
        services.register(EchoAssetMissingReport.class, missing);
        return result;
    }

    public EchoAssetIndex index() throws IOException {
        return indexBuilder.build(mounts);
    }
}
