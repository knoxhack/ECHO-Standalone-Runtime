package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoAdapterCoreStandaloneRegistry {
    private final List<EchoAdapterCoreRegistryEntry> entries;
    private final Map<String, EchoAdapterCoreRegistryEntry> byContentId;
    private final Map<String, EchoAdapterCoreRegistryEntry> byAdapterKey;
    private final Map<String, EchoAdapterCoreRegistryEntry> byLiveVoxelId;
    private final Map<EchoAdapterCoreRuntimeKind, Map<String, EchoAdapterCoreRegistryEntry>> byRuntimeId;
    private final Map<EchoAdapterCoreDomain, List<EchoAdapterCoreRegistryEntry>> byDomain;

    public EchoAdapterCoreStandaloneRegistry(List<EchoAdapterCoreRegistryEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        this.entries = List.copyOf(entries);
        this.byContentId = new HashMap<>();
        this.byAdapterKey = new HashMap<>();
        this.byLiveVoxelId = new HashMap<>();
        this.byRuntimeId = new EnumMap<>(EchoAdapterCoreRuntimeKind.class);
        this.byDomain = new EnumMap<>(EchoAdapterCoreDomain.class);
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            byRuntimeId.put(runtimeKind, new HashMap<>());
        }
        indexEntries();
    }

    public List<EchoAdapterCoreRegistryEntry> entries() {
        return entries;
    }

    public List<EchoAdapterCoreContentBinding> bindings() {
        ArrayList<EchoAdapterCoreContentBinding> result = new ArrayList<>();
        for (EchoAdapterCoreRegistryEntry entry : entries) {
            result.add(entry.binding());
        }
        return List.copyOf(result);
    }

    public EchoAdapterCoreStandaloneRegistry withEntry(EchoAdapterCoreRegistryEntry entry) {
        Objects.requireNonNull(entry, "entry");
        return withEntries(List.of(entry));
    }

    public EchoAdapterCoreStandaloneRegistry withEntries(List<EchoAdapterCoreRegistryEntry> additionalEntries) {
        Objects.requireNonNull(additionalEntries, "additionalEntries");
        if (additionalEntries.isEmpty()) {
            return this;
        }
        ArrayList<EchoAdapterCoreRegistryEntry> merged = new ArrayList<>(entries);
        merged.addAll(additionalEntries);
        return new EchoAdapterCoreStandaloneRegistry(merged);
    }

    public EchoAdapterCoreStandaloneRegistry withEntriesReplacingContentIds(
            List<EchoAdapterCoreRegistryEntry> replacementEntries
    ) {
        Objects.requireNonNull(replacementEntries, "replacementEntries");
        if (replacementEntries.isEmpty()) {
            return this;
        }
        LinkedHashMap<String, EchoAdapterCoreRegistryEntry> merged = new LinkedHashMap<>();
        for (EchoAdapterCoreRegistryEntry entry : entries) {
            merged.put(entry.contentId(), entry);
        }
        for (EchoAdapterCoreRegistryEntry entry : replacementEntries) {
            if (!entry.liveVoxelId().isBlank()) {
                merged.entrySet().removeIf(existing ->
                        entry.liveVoxelId().equals(existing.getValue().liveVoxelId()));
            }
            merged.put(entry.contentId(), entry);
        }
        return new EchoAdapterCoreStandaloneRegistry(new ArrayList<>(merged.values()));
    }

    public int size() {
        return entries.size();
    }

    public int count(EchoAdapterCoreDomain domain) {
        return entriesForDomain(domain).size();
    }

    public List<EchoAdapterCoreRegistryEntry> entriesForDomain(EchoAdapterCoreDomain domain) {
        Objects.requireNonNull(domain, "domain");
        return byDomain.getOrDefault(domain, List.of());
    }

    public List<EchoAdapterCoreRegistryEntry> blocks() {
        return entriesForDomain(EchoAdapterCoreDomain.BLOCKS);
    }

    public List<EchoAdapterCoreRegistryEntry> items() {
        return entriesForDomain(EchoAdapterCoreDomain.ITEMS);
    }

    public Optional<EchoAdapterCoreRegistryEntry> findContentId(String contentId) {
        return Optional.ofNullable(byContentId.get(EchoCompatText.requireText(contentId, "contentId")));
    }

    public EchoAdapterCoreRegistryEntry requireContentId(String contentId) {
        return findContentId(contentId).orElseThrow(() ->
                new IllegalArgumentException("No AdapterCore registry entry for content id " + contentId));
    }

    public Optional<EchoAdapterCoreRegistryEntry> findAdapterKey(String adapterKey) {
        return Optional.ofNullable(byAdapterKey.get(EchoCompatText.requireText(adapterKey, "adapterKey")));
    }

    public Optional<EchoAdapterCoreRegistryEntry> findLiveVoxelId(String liveVoxelId) {
        return Optional.ofNullable(byLiveVoxelId.get(EchoCompatText.requireText(liveVoxelId, "liveVoxelId")));
    }

    public EchoVoxelBlock requireLiveVoxelBlock(String liveVoxelId) {
        return findLiveVoxelId(liveVoxelId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No AdapterCore live voxel entry for " + liveVoxelId))
                .requireVoxelBlock();
    }

    public Optional<EchoAdapterCoreRegistryEntry> findRuntimeId(
            EchoAdapterCoreRuntimeKind runtimeKind,
            String runtimeId
    ) {
        Objects.requireNonNull(runtimeKind, "runtimeKind");
        return Optional.ofNullable(byRuntimeId.get(runtimeKind)
                .get(EchoCompatText.requireText(runtimeId, "runtimeId")));
    }

    public boolean supportsAllAdapterCoreRuntimes() {
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            if (byRuntimeId.get(runtimeKind).isEmpty()) {
                return false;
            }
        }
        return entries.stream()
                .map(EchoAdapterCoreRegistryEntry::binding)
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes);
    }

    public String summary() {
        return size() + " entries, "
                + count(EchoAdapterCoreDomain.BLOCKS) + " blocks, "
                + count(EchoAdapterCoreDomain.ITEMS) + " items, "
                + count(EchoAdapterCoreDomain.WORLDGEN) + " worldgen, "
                + EchoAdapterCoreContractLock.requiredBetaDomains().stream()
                .filter(domain -> !entriesForDomain(domain).isEmpty())
                .count() + " beta domains";
    }

    private void indexEntries() {
        EnumMap<EchoAdapterCoreDomain, ArrayList<EchoAdapterCoreRegistryEntry>> domains =
                new EnumMap<>(EchoAdapterCoreDomain.class);
        for (EchoAdapterCoreRegistryEntry entry : entries) {
            putUnique(byContentId, entry.contentId(), entry, "content id");
            putUnique(byAdapterKey, entry.binding().adapterKey(), entry, "adapter key");
            if (!entry.liveVoxelId().isBlank()) {
                putUnique(byLiveVoxelId, entry.liveVoxelId(), entry, "live voxel id");
            }
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                putRuntimeAlias(
                        byRuntimeId.get(runtimeKind),
                        entry.idFor(runtimeKind),
                        entry
                );
            }
            domains.computeIfAbsent(entry.domain(), ignored -> new ArrayList<>()).add(entry);
        }
        for (Map.Entry<EchoAdapterCoreDomain, ArrayList<EchoAdapterCoreRegistryEntry>> entry : domains.entrySet()) {
            byDomain.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
    }

    private static void putUnique(
            Map<String, EchoAdapterCoreRegistryEntry> map,
            String key,
            EchoAdapterCoreRegistryEntry entry,
            String keyType
    ) {
        EchoAdapterCoreRegistryEntry previous = map.putIfAbsent(key, entry);
        if (previous != null) {
            throw new IllegalArgumentException("Duplicate AdapterCore " + keyType + ": " + key);
        }
    }

    private static void putRuntimeAlias(
            Map<String, EchoAdapterCoreRegistryEntry> map,
            String key,
            EchoAdapterCoreRegistryEntry entry
    ) {
        // Runtime registries such as NeoForge scope identifiers by registry type, so a biome and a
        // structure can legitimately share the same namespaced id. Content ids and adapter keys above
        // remain globally unique; this index is only an alias lookup.
        map.putIfAbsent(key, entry);
    }
}
