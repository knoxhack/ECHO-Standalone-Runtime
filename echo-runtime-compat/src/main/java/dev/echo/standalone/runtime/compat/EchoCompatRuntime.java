package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;
import java.util.Objects;

public final class EchoCompatRuntime {
    public EchoCompatRuntimeResult createDebugCompatibility(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayRuntimeResult gameplay
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");

        EchoRuntimeCompatibilityAdapterBoundary boundary = new EchoRuntimeCompatibilityAdapterBoundary();
        EchoAdapterCoreStandaloneContentBridge adapterCoreBridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoAdapterCoreStandaloneRegistry adapterCoreRegistry = adapterCoreBridge.registry();
        EchoCompatDiagnostics diagnostics = new EchoCompatDiagnostics();
        diagnostics.info("runtime", "compatibility adapter boundary initialized");
        diagnostics.info("adaptercore", "registered " + adapterCoreBridge.readyBindingCount()
                + "/" + adapterCoreBridge.bindingCount() + " live bindings for "
                + adapterCoreBridge.runtimeSummary() + " with " + adapterCoreBridge.rendererSummary()
                + " and registry " + adapterCoreRegistry.summary());
        EchoCompatMappingRegistry registry = new EchoCompatMappingRegistry();
        registerDebugMappings(registry);
        diagnostics.info("mapping-registry", "registered " + registry.count() + " compatibility mappings");
        List<EchoCompatSourceRecord> sourceRecords = debugSourceRecords();
        EchoCompatMigrationPolicy policy = EchoCompatMigrationPolicy.manualPlanOnly();
        EchoCompatTargetValidator targetValidator = new EchoCompatTargetValidator();
        EchoCompatValidationResult validation = targetValidator.validate(registry, world, entities, items, gameplay);
        EchoCompatMigrationPlanner migrationPlanner = new EchoCompatMigrationPlanner();
        EchoCompatMigrationPlan migrationPlan = migrationPlanner.plan(
                "echo-owned-neoforge-ashfall",
                "echo-standalone-ashfall",
                registry,
                sourceRecords,
                validation,
                policy,
                diagnostics
        );

        EchoCompatRuntimeResult result = new EchoCompatRuntimeResult(
                boundary,
                registry,
                sourceRecords,
                policy,
                targetValidator,
                validation,
                migrationPlanner,
                migrationPlan,
                diagnostics,
                adapterCoreBridge,
                adapterCoreRegistry
        );
        services.register(EchoCompatRuntimeResult.class, result);
        services.register(EchoRuntimeCompatibilityAdapterBoundary.class, boundary);
        services.register(EchoAdapterCoreStandaloneContentBridge.class, adapterCoreBridge);
        services.register(EchoAdapterCoreStandaloneRegistry.class, adapterCoreRegistry);
        services.register(EchoCompatMappingRegistry.class, registry);
        services.register(EchoCompatMigrationPolicy.class, policy);
        services.register(EchoCompatTargetValidator.class, targetValidator);
        services.register(EchoCompatValidationResult.class, validation);
        services.register(EchoCompatMigrationPlanner.class, migrationPlanner);
        services.register(EchoCompatMigrationPlan.class, migrationPlan);
        services.register(EchoCompatDiagnostics.class, diagnostics);
        return result;
    }

    private static void registerDebugMappings(EchoCompatMappingRegistry registry) {
        registry.register(mapping(
                "map-clean-water-bottle",
                "echoashfallprotocol:item/clean_water_bottle",
                "echoashfallprotocol:clean_water_bottle",
                EchoCompatTargetKind.STANDALONE_ITEM,
                EchoCompatMappingStatus.SUPPORTED,
                "owned item id is shared by NeoForge and standalone item definition"
        ));
        registry.register(mapping(
                "map-scrap-metal",
                "echoashfallprotocol:item/scrap_metal",
                "echoashfallprotocol:scrap_metal",
                EchoCompatTargetKind.STANDALONE_ITEM,
                EchoCompatMappingStatus.SUPPORTED,
                "owned material id is shared by NeoForge and standalone item definition"
        ));
        registry.register(mapping(
                "map-crash-site-region",
                "echoashfallprotocol:world_region/crash_site",
                "ashfall:crash_site",
                EchoCompatTargetKind.STANDALONE_WORLD_REGION,
                EchoCompatMappingStatus.SUPPORTED,
                "owned region id maps to standalone debug region"
        ));
        registry.register(mapping(
                "map-toxic-ash-hazard",
                "echoashfallprotocol:world_hazard/toxic_ash",
                "ashfall:toxic_ash",
                EchoCompatTargetKind.STANDALONE_WORLD_HAZARD,
                EchoCompatMappingStatus.SUPPORTED,
                "owned hazard id maps to standalone hazard"
        ));
        registry.register(mapping(
                "map-hostile-scavenger",
                "echoashfallprotocol:entity/hostile_scavenger",
                "ashfall:hostile_scavenger",
                EchoCompatTargetKind.STANDALONE_ENTITY,
                EchoCompatMappingStatus.SUPPORTED,
                "owned entity id maps to standalone entity definition"
        ));
        registry.register(mapping(
                "map-secure-crash-site",
                "echoashfallprotocol:mission/secure_crash_site",
                "ashfall:secure_crash_site",
                EchoCompatTargetKind.STANDALONE_GAMEPLAY_MISSION,
                EchoCompatMappingStatus.SUPPORTED,
                "owned mission id maps to standalone mission state"
        ));
        registry.register(mapping(
                "review-player-progress-save",
                "echoashfallprotocol:save/player_progress_v1",
                "echo:manual_save_review/player_progress",
                EchoCompatTargetKind.STANDALONE_SAVE_RECORD,
                EchoCompatMappingStatus.MANUAL_REVIEW,
                "player save payloads require manual review and backup before migration"
        ));
    }

    private static EchoCompatContentMapping mapping(
            String mappingId,
            String sourceId,
            String targetId,
            EchoCompatTargetKind targetKind,
            EchoCompatMappingStatus status,
            String notes
    ) {
        return new EchoCompatContentMapping(
                mappingId,
                sourceId,
                EchoCompatSourceKind.ECHO_OWNED_MINECRAFT_NEOFORGE,
                targetId,
                targetKind,
                status,
                notes
        );
    }

    private static List<EchoCompatSourceRecord> debugSourceRecords() {
        return List.of(
                source("source-001", "echoashfallprotocol:item/clean_water_bottle", "item", "sha256:clean-water-bottle"),
                source("source-002", "echoashfallprotocol:item/scrap_metal", "item", "sha256:scrap-metal"),
                source("source-003", "echoashfallprotocol:world_region/crash_site", "world_region", "sha256:crash-site"),
                source("source-004", "echoashfallprotocol:world_hazard/toxic_ash", "world_hazard", "sha256:toxic-ash"),
                source("source-005", "echoashfallprotocol:entity/hostile_scavenger", "entity", "sha256:hostile-scavenger"),
                source("source-006", "echoashfallprotocol:mission/secure_crash_site", "mission", "sha256:secure-crash-site"),
                source("source-007", "echoashfallprotocol:save/player_progress_v1", "save_record", "sha256:player-progress")
        );
    }

    private static EchoCompatSourceRecord source(
            String recordId,
            String sourceId,
            String recordType,
            String fingerprint
    ) {
        return new EchoCompatSourceRecord(
                recordId,
                sourceId,
                EchoCompatSourceKind.ECHO_OWNED_MINECRAFT_NEOFORGE,
                recordType,
                fingerprint
        );
    }
}
