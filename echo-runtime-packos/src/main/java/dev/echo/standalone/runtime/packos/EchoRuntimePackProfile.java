package dev.echo.standalone.runtime.packos;

import dev.echo.standalone.runtime.contracts.EchoRuntimeMode;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoRuntimePackProfile(
        String schema,
        String packId,
        String packName,
        String variant,
        EchoRuntimePackChannel channel,
        String runtimeVersion,
        List<String> enabledModules,
        List<String> enabledFeatures,
        Path lockfilePath,
        Map<String, String> saveCompatibility,
        List<String> assetPacks,
        List<String> dataPacks,
        String theme,
        EchoRuntimeMode launchMode,
        Path sourcePath
) {
    public EchoRuntimePackProfile {
        schema = requireText(schema, "schema");
        packId = requireText(packId, "packId");
        packName = requireText(packName, "packName");
        variant = requireText(variant, "variant");
        Objects.requireNonNull(channel, "channel");
        runtimeVersion = requireText(runtimeVersion, "runtimeVersion");
        Objects.requireNonNull(enabledModules, "enabledModules");
        Objects.requireNonNull(enabledFeatures, "enabledFeatures");
        Objects.requireNonNull(lockfilePath, "lockfilePath");
        Objects.requireNonNull(saveCompatibility, "saveCompatibility");
        Objects.requireNonNull(assetPacks, "assetPacks");
        Objects.requireNonNull(dataPacks, "dataPacks");
        theme = requireText(theme, "theme");
        Objects.requireNonNull(launchMode, "launchMode");
        Objects.requireNonNull(sourcePath, "sourcePath");
        enabledModules = enabledModules.stream().sorted().toList();
        enabledFeatures = enabledFeatures.stream().sorted().toList();
        saveCompatibility = Map.copyOf(new TreeMap<>(saveCompatibility));
        assetPacks = List.copyOf(assetPacks);
        dataPacks = List.copyOf(dataPacks);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
