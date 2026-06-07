package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public final class EchoAssetHotReload {
    private final EchoAssetRuntime runtime;

    public EchoAssetHotReload(EchoAssetRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    public EchoAssetReloadReport reload(EchoAssetIndex previous) throws IOException {
        Objects.requireNonNull(previous, "previous");
        EchoAssetIndex next = runtime.index();
        TreeSet<String> previousIds = new TreeSet<>(previous.entriesByLogicalId().keySet());
        TreeSet<String> nextIds = new TreeSet<>(next.entriesByLogicalId().keySet());

        TreeSet<String> added = new TreeSet<>(nextIds);
        added.removeAll(previousIds);

        TreeSet<String> removed = new TreeSet<>(previousIds);
        removed.removeAll(nextIds);

        TreeSet<String> changed = new TreeSet<>();
        for (String id : previousIds) {
            if (!nextIds.contains(id)) {
                continue;
            }
            EchoAssetEntry before = previous.resolve(id).orElseThrow();
            EchoAssetEntry after = next.resolve(id).orElseThrow();
            if (before.size() != after.size()
                    || !before.file().equals(after.file())
                    || before.mount().order() != after.mount().order()) {
                changed.add(id);
            }
        }
        return new EchoAssetReloadReport(
                next,
                added.stream().toList(),
                removed.stream().toList(),
                changed.stream().toList()
        );
    }
}
