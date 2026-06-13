package dev.echo.standalone.runtime.modules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class EchoRuntimeModuleDataRegistry {
    private final Map<String, Map<String, String>> configs = new LinkedHashMap<>();
    private final Map<String, Map<String, EchoRuntimeModuleAsset>> assets = new LinkedHashMap<>();
    private final Map<String, Map<String, String>> saveData = new LinkedHashMap<>();

    public synchronized void publishConfig(String moduleId, String key, String value) {
        configs.computeIfAbsent(moduleId, ignored -> new LinkedHashMap<>()).put(key, value);
    }

    public synchronized EchoRuntimeModuleAsset registerAsset(
            String moduleId,
            String assetId,
            String relativePath,
            String resolvedPath
    ) {
        EchoRuntimeModuleAsset asset = new EchoRuntimeModuleAsset(moduleId, assetId, relativePath, resolvedPath);
        assets.computeIfAbsent(moduleId, ignored -> new LinkedHashMap<>()).put(assetId, asset);
        return asset;
    }

    public synchronized void writeSaveData(String moduleId, String key, String value) {
        saveData.computeIfAbsent(moduleId, ignored -> new LinkedHashMap<>()).put(key, value);
    }

    public synchronized Optional<String> saveValue(String moduleId, String key) {
        return Optional.ofNullable(saveData.getOrDefault(moduleId, Map.of()).get(key));
    }

    public synchronized Map<String, String> configs(String moduleId) {
        return Map.copyOf(new TreeMap<>(configs.getOrDefault(moduleId, Map.of())));
    }

    public synchronized List<EchoRuntimeModuleAsset> assets(String moduleId) {
        return assets.getOrDefault(moduleId, Map.of()).values().stream()
                .sorted(java.util.Comparator.comparing(EchoRuntimeModuleAsset::assetId))
                .toList();
    }

    public synchronized Map<String, String> saves(String moduleId) {
        return Map.copyOf(new TreeMap<>(saveData.getOrDefault(moduleId, Map.of())));
    }

    public synchronized int revokeRuntimeState(String moduleId) {
        int count = 0;
        Map<String, String> removedConfigs = configs.remove(moduleId);
        if (removedConfigs != null) {
            count += removedConfigs.size();
        }
        Map<String, EchoRuntimeModuleAsset> removedAssets = assets.remove(moduleId);
        if (removedAssets != null) {
            count += removedAssets.size();
        }
        return count;
    }

    public record EchoRuntimeModuleAsset(
            String moduleId,
            String assetId,
            String relativePath,
            String resolvedPath
    ) {
    }
}
