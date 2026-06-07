package dev.echo.standalone.runtime.packos;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoRuntimePackLockfile(
        String schema,
        String packId,
        String runtimeVersion,
        Map<String, String> lockedModules,
        List<String> lockedFeatures,
        Path sourcePath
) {
    public EchoRuntimePackLockfile {
        schema = requireText(schema, "schema");
        packId = requireText(packId, "packId");
        runtimeVersion = requireText(runtimeVersion, "runtimeVersion");
        Objects.requireNonNull(lockedModules, "lockedModules");
        Objects.requireNonNull(lockedFeatures, "lockedFeatures");
        Objects.requireNonNull(sourcePath, "sourcePath");
        lockedModules = Map.copyOf(new TreeMap<>(lockedModules));
        lockedFeatures = lockedFeatures.stream().sorted().toList();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
