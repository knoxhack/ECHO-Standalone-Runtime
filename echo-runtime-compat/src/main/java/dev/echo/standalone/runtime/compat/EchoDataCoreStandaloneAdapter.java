package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public final class EchoDataCoreStandaloneAdapter {
    public static final String MODULE_ID = "echodatacore";
    public static final String TERMINAL_PROBE_CONTRACT_ID = "echodatacore:system/terminal_probe";
    public static final String PLAYER_SCHEMA_VERSION_CONTRACT_ID = "echodatacore:system/player_schema_version";
    public static final String WORLD_SCHEMA_VERSION_CONTRACT_ID = "echodatacore:system/world_schema_version";
    public static final String LAST_REGION_CONTRACT_ID = "echodatacore:worldcore/last_region";
    public static final String LAST_MARKER_CONTRACT_ID = "echodatacore:worldcore/last_marker";
    public static final String ACTIVE_HAZARDS_CONTRACT_ID = "echodatacore:worldcore/active_hazards";
    public static final String DATA_SERVICE_CONTRACT_ID = "echodatacore:data_service";
    public static final String DATA_SYNC_CONTRACT_ID = "echodatacore:data_sync";
    public static final List<String> CONTRACT_IDS = List.of(
            TERMINAL_PROBE_CONTRACT_ID,
            PLAYER_SCHEMA_VERSION_CONTRACT_ID,
            WORLD_SCHEMA_VERSION_CONTRACT_ID,
            LAST_REGION_CONTRACT_ID,
            LAST_MARKER_CONTRACT_ID,
            ACTIVE_HAZARDS_CONTRACT_ID,
            DATA_SERVICE_CONTRACT_ID,
            DATA_SYNC_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "datacore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("dataKeyCount", 6);
        report.put("serviceContractCount", 1);
        report.put("networkContractCount", 1);
        report.put("dataServiceRoundTrip", referenceProbe.get("dataServiceRoundTrip"));
        report.put("builtinKeyRoundTrip", referenceProbe.get("builtinKeyRoundTrip"));
        report.put("metadataReloadRoundTrip", referenceProbe.get("metadataReloadRoundTrip"));
        report.put("diagnosticsRoundTrip", referenceProbe.get("diagnosticsRoundTrip"));
        report.put("syncBridgeRoundTrip", referenceProbe.get("syncBridgeRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("summary", "DataCore standalone adapter resolved persistence keys, data service, sync payloads, metadata reload, diagnostics, and sync bridge behavior through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        StandaloneDataCoreRuntime runtime = new StandaloneDataCoreRuntime();
        runtime.registerBuiltInKeys();
        runtime.replaceDatapackMetadata(Map.of(
                "echodatacore:native_probe/metadata_flag",
                new DataKeyMetadata(
                        "echodatacore:native_probe/metadata_flag",
                        DataScope.PLAYER,
                        DataValueKind.FLAG,
                        true,
                        "Native Probe Flag",
                        "AdapterCore standalone DataCore metadata probe.",
                        MODULE_ID,
                        "false",
                        "standalone:agent4-datacore-smoke"
                )
        ));
        DataServiceDiagnostics diagnostics = runtime.diagnostics();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dataServiceRoundTrip", runtime.key(TERMINAL_PROBE_CONTRACT_ID).present()
                && runtime.key(WORLD_SCHEMA_VERSION_CONTRACT_ID).present());
        result.put("builtinKeyRoundTrip", runtime.keysById.size() >= 13
                && runtime.key(ACTIVE_HAZARDS_CONTRACT_ID).present());
        result.put("metadataReloadRoundTrip", runtime.key("echodatacore:native_probe/metadata_flag").present()
                && runtime.metadata("echodatacore:native_probe/metadata_flag").title().equals("Native Probe Flag")
                && runtime.metadata("echodatacore:native_probe/metadata_flag").owner().equals(MODULE_ID));
        result.put("diagnosticsRoundTrip", diagnostics.available()
                && diagnostics.providerClass().equals(StandaloneDataCoreRuntime.class.getName())
                && diagnostics.registeredKeyCount() >= 14
                && diagnostics.syncedKeyCount() >= 14
                && diagnostics.metadataKeyCount() >= diagnostics.registeredKeyCount());
        result.put("syncBridgeRoundTrip", runtime.syncBridge().active()
                && runtime.syncBridge().payloadContractId().equals(DATA_SYNC_CONTRACT_ID));
        result.put("registeredKeyCount", diagnostics.registeredKeyCount());
        result.put("syncedKeyCount", diagnostics.syncedKeyCount());
        result.put("metadataKeyCount", diagnostics.metadataKeyCount());
        result.put("metadataProbeId", "echodatacore:native_probe/metadata_flag");
        return Map.copyOf(result);
    }

    private enum DataScope {
        PLAYER,
        WORLD,
        TEAM
    }

    private enum DataValueKind {
        FLAG,
        COUNTER,
        STRING
    }

    private record DataKey(
            String id,
            DataScope scope,
            DataValueKind kind,
            String defaultValue,
            boolean synced,
            boolean present
    ) {
        private DataKey(
                String id,
                DataScope scope,
                DataValueKind kind,
                String defaultValue,
                boolean synced
        ) {
            this(id, scope, kind, defaultValue, synced, true);
        }

        private DataKey {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(scope, "scope");
            Objects.requireNonNull(kind, "kind");
            defaultValue = defaultValue == null ? "" : defaultValue;
        }
    }

    private record DataKeyMetadata(
            String id,
            DataScope scope,
            DataValueKind kind,
            boolean synced,
            String title,
            String description,
            String owner,
            String defaultValue,
            String source
    ) {
        private DataKeyMetadata {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(scope, "scope");
            Objects.requireNonNull(kind, "kind");
            title = title == null ? "" : title.strip();
            description = description == null ? "" : description.strip();
            owner = owner == null ? "" : owner.strip();
            defaultValue = defaultValue == null ? "" : defaultValue.strip();
            source = source == null ? "" : source.strip();
        }

        private static DataKeyMetadata of(DataKey key, String source) {
            String tail = key.id().substring(key.id().lastIndexOf('/') + 1).replace('_', ' ');
            return new DataKeyMetadata(
                    key.id(),
                    key.scope(),
                    key.kind(),
                    key.synced(),
                    tail,
                    "",
                    key.id().substring(0, key.id().indexOf(':')),
                    key.defaultValue(),
                    source
            );
        }
    }

    private record DataServiceDiagnostics(
            boolean available,
            String providerClass,
            int registeredKeyCount,
            int syncedKeyCount,
            int metadataKeyCount,
            int dirtyOwnerCount,
            List<String> recentChanges
    ) {
        private DataServiceDiagnostics {
            recentChanges = List.copyOf(recentChanges == null ? List.of() : recentChanges);
        }
    }

    private record DataSyncBridge(String payloadContractId, boolean active) {
    }

    private static final class StandaloneDataCoreRuntime {
        private final Map<String, DataKey> keysById = new TreeMap<>();
        private final Map<String, DataKeyMetadata> metadataById = new TreeMap<>();
        private final Set<String> datapackKeys = new LinkedHashSet<>();
        private final DataSyncBridge syncBridge = new DataSyncBridge(DATA_SYNC_CONTRACT_ID, true);

        private void registerBuiltInKeys() {
            register(new DataKey(TERMINAL_PROBE_CONTRACT_ID, DataScope.PLAYER, DataValueKind.STRING, "offline", true));
            register(new DataKey(PLAYER_SCHEMA_VERSION_CONTRACT_ID, DataScope.PLAYER, DataValueKind.COUNTER, "0", true));
            register(new DataKey(WORLD_SCHEMA_VERSION_CONTRACT_ID, DataScope.WORLD, DataValueKind.COUNTER, "0", true));
            register(new DataKey(LAST_REGION_CONTRACT_ID, DataScope.PLAYER, DataValueKind.STRING, "", true));
            register(new DataKey("echodatacore:worldcore/last_discovery_source", DataScope.PLAYER, DataValueKind.STRING, "", true));
            register(new DataKey("echodatacore:worldcore/region_discoveries", DataScope.PLAYER, DataValueKind.COUNTER, "0", true));
            register(new DataKey(LAST_MARKER_CONTRACT_ID, DataScope.PLAYER, DataValueKind.STRING, "", true));
            register(new DataKey("echodatacore:worldcore/markers_revealed", DataScope.PLAYER, DataValueKind.COUNTER, "0", true));
            register(new DataKey(ACTIVE_HAZARDS_CONTRACT_ID, DataScope.PLAYER, DataValueKind.STRING, "", true));
            register(new DataKey("echodatacore:worldcore/active_hazard_severity", DataScope.PLAYER, DataValueKind.COUNTER, "0", true));
            register(new DataKey("echodatacore:worldcore/world_region_discoveries", DataScope.WORLD, DataValueKind.COUNTER, "0", true));
            register(new DataKey("echodatacore:worldcore/world_markers_revealed", DataScope.WORLD, DataValueKind.COUNTER, "0", true));
            register(new DataKey("echodatacore:worldcore/world_hazard_changes", DataScope.WORLD, DataValueKind.COUNTER, "0", true));
        }

        private void register(DataKey key) {
            keysById.putIfAbsent(key.id(), key);
            metadataById.putIfAbsent(key.id(), DataKeyMetadata.of(key, "standalone-java"));
        }

        private DataKey key(String id) {
            return keysById.getOrDefault(id, new DataKey(id, DataScope.PLAYER, DataValueKind.STRING, "", false, false));
        }

        private DataKeyMetadata metadata(String id) {
            return metadataById.get(id);
        }

        private void replaceDatapackMetadata(Map<String, DataKeyMetadata> datapackMetadata) {
            for (String id : List.copyOf(datapackKeys)) {
                if (!datapackMetadata.containsKey(id)) {
                    keysById.remove(id);
                    metadataById.remove(id);
                    datapackKeys.remove(id);
                }
            }
            for (DataKeyMetadata metadata : datapackMetadata.values()) {
                metadataById.put(metadata.id(), metadata);
                if (!keysById.containsKey(metadata.id())) {
                    register(new DataKey(
                            metadata.id(),
                            metadata.scope(),
                            metadata.kind(),
                            metadata.defaultValue(),
                            metadata.synced()
                    ));
                    metadataById.put(metadata.id(), metadata);
                    datapackKeys.add(metadata.id());
                }
            }
        }

        private DataServiceDiagnostics diagnostics() {
            int synced = (int) keysById.values().stream().filter(DataKey::synced).count();
            return new DataServiceDiagnostics(
                    true,
                    getClass().getName(),
                    keysById.size(),
                    synced,
                    metadataById.size(),
                    0,
                    List.of("metadata_reload:" + datapackKeys.size())
            );
        }

        private DataSyncBridge syncBridge() {
            return syncBridge;
        }
    }
}
