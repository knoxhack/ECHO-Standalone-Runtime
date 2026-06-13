package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRenderTarget;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeContentRegistry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneRegistry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoCompatContentMapping;
import dev.echo.standalone.runtime.compat.EchoCompatDiagnostic;
import dev.echo.standalone.runtime.compat.EchoCompatDiagnostics;
import dev.echo.standalone.runtime.compat.EchoCompatMappingRegistry;
import dev.echo.standalone.runtime.compat.EchoCompatMigrationActionKind;
import dev.echo.standalone.runtime.compat.EchoCompatMigrationPlan;
import dev.echo.standalone.runtime.compat.EchoCompatMigrationPlanner;
import dev.echo.standalone.runtime.compat.EchoCompatMigrationPolicy;
import dev.echo.standalone.runtime.compat.EchoCompatMigrationStep;
import dev.echo.standalone.runtime.compat.EchoCompatRecipeItemBridge;
import dev.echo.standalone.runtime.compat.EchoCompatRuntime;
import dev.echo.standalone.runtime.compat.EchoCompatRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoCompatSourceRecord;
import dev.echo.standalone.runtime.compat.EchoCompatTargetValidator;
import dev.echo.standalone.runtime.compat.EchoCompatValidationIssue;
import dev.echo.standalone.runtime.compat.EchoCompatValidationResult;
import dev.echo.standalone.runtime.compat.EchoNeoForgeMetadataScanResult;
import dev.echo.standalone.runtime.compat.EchoNeoForgeMetadataScanner;
import dev.echo.standalone.runtime.compat.EchoNeoForgeModCandidate;
import dev.echo.standalone.runtime.compat.EchoRuntimeCompatibilityAdapterBoundary;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeCompatSmokeHarness {
    private EchoRuntimeCompatSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoCompatRuntimeResult compat = new EchoCompatRuntime().createDebugCompatibility(
                services,
                world,
                entities,
                items,
                gameplay
        );

        require(services.require(EchoCompatRuntimeResult.class) == compat,
                "compat runtime result should be service-bound");
        require(services.require(EchoRuntimeCompatibilityAdapterBoundary.class) == compat.boundary(),
                "compat boundary should be service-bound");
        require(services.require(EchoAdapterCoreStandaloneContentBridge.class) == compat.adapterCoreBridge(),
                "AdapterCore standalone bridge should be service-bound");
        require(services.require(EchoAdapterCoreStandaloneRegistry.class) == compat.adapterCoreRegistry(),
                "AdapterCore standalone registry should be service-bound");
        require(services.require(EchoCompatMappingRegistry.class) == compat.mappingRegistry(),
                "mapping registry should be service-bound");
        require(services.require(EchoCompatMigrationPolicy.class) == compat.migrationPolicy(),
                "migration policy should be service-bound");
        require(services.require(EchoCompatTargetValidator.class) == compat.targetValidator(),
                "target validator should be service-bound");
        require(services.require(EchoCompatValidationResult.class) == compat.targetValidation(),
                "target validation should be service-bound");
        require(services.require(EchoCompatMigrationPlanner.class) == compat.migrationPlanner(),
                "migration planner should be service-bound");
        require(services.require(EchoCompatMigrationPlan.class) == compat.migrationPlan(),
                "migration plan should be service-bound");
        require(services.require(EchoCompatDiagnostics.class) == compat.diagnostics(),
                "compat diagnostics should be service-bound");

        Path metadataFixture = Files.createTempDirectory("echo-neoforge-metadata-smoke");
        writeText(metadataFixture.resolve("ashfall/META-INF/neoforge.mods.toml"), """
                license="All Rights Reserved"

                [[mods]]
                modId="echoashfallprotocol"
                version="1.0.0"
                displayName="ECHO: Ashfall Protocol"
                description='''
                Compatibility metadata fixture for the standalone runtime.
                '''

                [[dependencies.echoashfallprotocol]]
                modId="neoforge"
                type="required"
                versionRange="[26.1.2.29-beta,)"
                ordering="NONE"
                side="BOTH"

                [[dependencies.echoashfallprotocol]]
                modId="minecraft"
                type="required"
                versionRange="[26.1.2,26.2)"
                ordering="NONE"
                side="BOTH"

                [[dependencies.echoashfallprotocol]]
                modId="echocore"
                type="required"
                reason="Provides shared ECHO addon and service APIs."
                versionRange="[1.0.0,)"
                ordering="AFTER"
                side="BOTH"

                [[dependencies.echoashfallprotocol]]
                modId="echoworldcore"
                type="optional"
                reason="Registers Ashfall world regions and hazards into WorldCore when present."
                versionRange="[1.0.0,)"
                ordering="NONE"
                side="BOTH"
                """);
        EchoNeoForgeMetadataScanResult metadataScan = new EchoNeoForgeMetadataScanner()
                .scan(List.of(metadataFixture));
        require(metadataScan.errorCount() == 0,
                "NeoForge metadata compatibility scan should not produce errors");
        require(metadataScan.candidateCount() == 1,
                "NeoForge metadata fixture should produce one compatibility candidate");
        EchoNeoForgeModCandidate ashfallCandidate = metadataScan.find("echoashfallprotocol")
                .orElseThrow(() -> new AssertionError("Ashfall NeoForge metadata candidate should be discovered"));
        require(ashfallCandidate.runtimeStatus().equals("runtime-disabled-with-reason"),
                "NeoForge metadata candidate should be disabled with an explicit runtime reason");
        require(ashfallCandidate.runtimeReason().contains("no classloader or module code execution"),
                "NeoForge metadata candidate should explain that it is diagnostics-only");
        require(ashfallCandidate.platformDependencies().size() == 2,
                "NeoForge metadata candidate should preserve platform dependency rows");
        require(ashfallCandidate.nonPlatformRequiredDependencies().stream()
                        .anyMatch(dependency -> dependency.modId().equals("echocore")
                                && dependency.ordering().equals("AFTER")
                                && dependency.reason().contains("shared ECHO")),
                "NeoForge metadata candidate should preserve required ECHO dependency reason and ordering");
        require(ashfallCandidate.optionalDependencies().stream()
                        .anyMatch(dependency -> dependency.modId().equals("echoworldcore")
                                && dependency.reason().contains("world regions")),
                "NeoForge metadata candidate should preserve optional ECHO dependency reason");

        EchoRecipeDefinition dataRecipe = new EchoRecipeDefinition(
                "echoashfallprotocol:patch_filter_from_datapack",
                "minecraft:crafting_shaped",
                List.of(EchoItemRuntime.SCRAP_METAL_ITEM_ID, "ashfall:filter_canister"),
                Map.of(EchoItemRuntime.SCRAP_METAL_ITEM_ID, 2, "ashfall:filter_canister", 1),
                "ashfall:patched_filter",
                1,
                List.of("MM", " F"),
                "ashfall_survival",
                "equipment",
                "data/echoashfallprotocol/recipe/patch_filter_from_datapack.json"
        );
        EchoItemRecipe executableRecipe = new EchoCompatRecipeItemBridge()
                .toItemRecipe(dataRecipe, items.registry())
                .orElseThrow(() -> new AssertionError("concrete data recipe should bridge to item crafting"));
        require(executableRecipe.ingredients().get(new EchoItemId(EchoItemRuntime.SCRAP_METAL_ITEM_ID)) == 2,
                "bridged item recipe should preserve data ingredient counts");
        EchoInventoryContainer playerPack = items.inventoryStore().require(new EchoInventoryId("inventory:player-001"));
        EchoItemCraftResult dataCrafted = items.craftingSystem().craft(playerPack, executableRecipe);
        require(dataCrafted.crafted(), "bridged Minecraft data recipe should craft through item runtime");
        require(items.operations().count(playerPack, new EchoItemId("ashfall:patched_filter")) == 1,
                "bridged data recipe should add its output item to the inventory");

        require(EchoRuntimeCompatibilityAdapterBoundary.adapterRules().size() == 6,
                "adapter boundary rules should remain stable");
        require(EchoRuntimeCompatibilityAdapterBoundary.adapterRules()
                        .contains("migration tooling plans before it mutates player data"),
                "adapter boundary should require planning before mutation");
        require(EchoRuntimeCompatibilityAdapterBoundary.adapterRules()
                        .contains("AdapterCore content ids remain canonical across NeoForge, ECHO Native Loader, and Standalone"),
                "adapter boundary should keep AdapterCore ids canonical across runtimes");
        require(EchoRuntimeCompatibilityAdapterBoundary.adapterRules()
                        .contains("standalone live client targets OpenGL through the active renderer path"),
                "adapter boundary should require the standalone live client target to be OpenGL");

        EchoAdapterCoreStandaloneContentBridge bridge = compat.adapterCoreBridge();
        EchoAdapterCoreStandaloneRegistry adapterCoreRegistry = compat.adapterCoreRegistry();
        require(adapterCoreRegistry == bridge.registry(),
                "compat result should expose the bridge-owned AdapterCore registry");
        require(bridge.bindingCount() >= 30,
                "AdapterCore bridge should expose the expanded live Ashfall beta bindings");
        require(bridge.readyBindingCount() == bridge.bindingCount(),
                "all live AdapterCore bindings should be standalone-ready");
        require(bridge.supportsAllAdapterCoreRuntimes(),
                "AdapterCore bindings should name NeoForge, Native Loader, and Standalone ids");
        require(adapterCoreRegistry.supportsAllAdapterCoreRuntimes(),
                "AdapterCore registry entries should name NeoForge, Native Loader, and Standalone ids");
        require(adapterCoreRegistry.size() == bridge.bindingCount(),
                "AdapterCore registry should contain every bridge entry");
        require(adapterCoreRegistry.count(EchoAdapterCoreDomain.BLOCKS) >= 8,
                "AdapterCore registry should expose live block entries");
        require(adapterCoreRegistry.count(EchoAdapterCoreDomain.ITEMS) >= 4,
                "AdapterCore registry should expose survival items");
        require(adapterCoreRegistry.count(EchoAdapterCoreDomain.WORLDGEN) >= 5,
                "AdapterCore registry should expose region and hazard worldgen entries");
        require(adapterCoreRegistry.count(EchoAdapterCoreDomain.SOUNDS) >= 1,
                "AdapterCore registry should expose terminal sound entries");
        require(bridge.renderTarget() == EchoAdapterCoreRenderTarget.OPENGL,
                "standalone renderer target should be OpenGL");
        require(bridge.runtimeMarkerBlock().id().equals("echoadaptercore:runtime_marker_block"),
                "runtime marker block should come from AdapterCore bridge");
        require(bridge.waterRationItem().id().equals("echoashfallprotocol:clean_water_bottle"),
                "water ration live voxel id should come from AdapterCore bridge");
        require(adapterCoreRegistry.requireLiveVoxelBlock("echoashfallprotocol:fallout_dust")
                        .displayName()
                        .equals("Toxic Ash"),
                "Ashfall terrain blocks should come from AdapterCore registry");
        require(adapterCoreRegistry.requireContentId("echoashfallprotocol:sound/ui.echo_message")
                        .standaloneRuntimeId()
                        .equals("ashfall:radio_static"),
                "terminal sound should map through AdapterCore to the standalone audio cue id");
        require(adapterCoreRegistry.findRuntimeId(
                        EchoAdapterCoreRuntimeKind.ECHO_RUNTIME_STANDALONE,
                        EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID
                ).isPresent(),
                "water ration should be findable by standalone runtime id");
        require(bridge.bindings().stream()
                        .anyMatch(binding -> binding.idFor(EchoAdapterCoreRuntimeKind.NEOFORGE)
                                .equals("echoashfallprotocol:clean_water_bottle")),
                "water ration should retain its NeoForge id in AdapterCore bindings");
        require(bridge.bindings().stream()
                        .anyMatch(binding -> binding.idFor(EchoAdapterCoreRuntimeKind.ECHO_NATIVE_LOADER)
                                .equals("echoashfallprotocol:clean_water_bottle")),
                "water ration should retain its Native Loader id in AdapterCore bindings");
        require(bridge.bindings().stream()
                        .anyMatch(binding -> binding.idFor(EchoAdapterCoreRuntimeKind.ECHO_RUNTIME_STANDALONE)
                                .equals(EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID)),
                "water ration should keep the canonical standalone runtime item id");
        requireNativeContentRegistrationBlocks(bridge, adapterCoreRegistry);

        require(compat.mappingRegistry().count() == 7,
                "debug compatibility registry should contain seven mappings");
        require(compat.mappingRegistry().supportedCount() == 6,
                "six mappings should be directly supported");
        require(compat.mappingRegistry().manualReviewCount() == 1,
                "one mapping should require manual review");
        require(compat.mappingRegistry().blockedCount() == 0,
                "debug compatibility mappings should not be blocked");
        require(compat.sourceRecords().size() == 7,
                "debug source record set should contain seven records");

        require(compat.targetValidation().valid(),
                "target validation should pass");
        require(compat.targetValidation().warningCount() == 1,
                "target validation should surface one manual review warning");
        require(compat.targetValidation().errorCount() == 0,
                "target validation should not contain errors");
        require(compat.mappingRegistry().requireSource("echoashfallprotocol:item/clean_water_bottle")
                        .targetId()
                        .equals(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID),
                "clean water bottle should map to the canonical standalone item definition");
        require(compat.mappingRegistry().requireSource("echoashfallprotocol:mission/secure_crash_site")
                        .targetId()
                        .equals("ashfall:secure_crash_site"),
                "mission should map to standalone mission definition");

        require(compat.migrationPolicy().policyId().equals("echo:manual_migration_plan_only"),
                "migration policy id should be stable");
        require(compat.migrationPolicy().manualOnly(),
                "compat migration policy should be manual-only");
        require(!compat.migrationPolicy().executeAutomatically(),
                "compat migration policy should not execute automatically");
        require(!compat.migrationPolicy().mutateSourceAllowed(),
                "compat migration policy should not allow source mutation");
        require(compat.migrationPolicy().backupRequired(),
                "compat migration policy should require backup");

        EchoCompatMigrationPlan plan = compat.migrationPlan();
        require(!plan.blocked(), "debug compatibility migration plan should not be blocked");
        require(plan.steps().size() == 8,
                "migration plan should include backup plus seven mapping steps");
        require(plan.steps().getFirst().actionKind() == EchoCompatMigrationActionKind.REQUIRE_BACKUP,
                "first migration step should require backup");
        require(plan.manualReviewStepCount() == 1,
                "migration plan should contain one manual review step");
        require(plan.mutationStepCount() == 0,
                "migration plan should not contain mutation steps");
        require(plan.steps().stream().anyMatch(step -> step.targetId().equals("ashfall:toxic_ash")),
                "migration plan should include toxic ash hazard mapping");
        require(plan.steps().stream().anyMatch(step -> step.targetId().equals("echo:manual_save_review/player_progress")),
                "migration plan should include manual save review mapping");

        require(compat.diagnostics().count() == 5,
                "compat diagnostics should include initialization, AdapterCore, registry, review, and planning events");
        require(compat.diagnostics().warningCount() == 1,
                "compat diagnostics should include one manual review warning");
        require(compat.diagnostics().errorCount() == 0,
                "compat diagnostics should not contain errors");

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                compat,
                metadataScan,
                ashfallCandidate
        );

        System.out.println("phase14.17 compat runtime smoke PASS mappings="
                + compat.mappingRegistry().count()
                + " supported="
                + compat.mappingRegistry().supportedCount()
                + " manualReview="
                + compat.mappingRegistry().manualReviewCount()
                + " adapterCoreBindings="
                + bridge.bindingCount()
                + " adapterCoreRegistry="
                + adapterCoreRegistry.size()
                + " rendererTarget="
                + bridge.renderTarget().adapterId()
                + " steps="
                + plan.steps().size()
                + " mutations="
                + plan.mutationStepCount()
                + " diagnostics="
                + compat.diagnostics().count()
                + " blocked="
                + plan.blocked());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void writeText(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void requireNativeContentRegistrationBlocks(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoAdapterCoreStandaloneRegistry adapterCoreRegistry
    ) {
        EchoAdapterCoreRuntimeContentRegistry runtimeRegistry = new EchoAdapterCoreRuntimeContentRegistry();
        Map<String, Object> blockRow = Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:block/native_runtime_glass",
                "contentKind", "BLOCK",
                "domain", "blocks",
                "displayName", "Native Runtime Glass",
                "adapterKey", "registry.blocks.native_runtime_glass",
                "neoForgeId", "echoruntimehost:native_runtime_glass",
                "nativeLoaderId", "echoruntimehost:block/native_runtime_glass",
                "standaloneRuntimeId", "echoruntimehost:native_runtime_glass",
                "metadata", Map.of(
                        "liveVoxelId", "echoruntimehost:native_runtime_glass",
                        "argb", "#7FA6D9",
                        "detailArgb", "0xFFB9D4FF",
                        "atlasKey", "echoruntimehost/block/native_runtime_glass",
                        "materialPattern", "CACHE_PANEL",
                        "solid", false,
                        "opaque", false,
                        "hardness", "0.25"
                )
        );
        require(runtimeRegistry.registerAll(List.of(blockRow)) == 1,
                "native content registry should accept a new block registration row");
        require(runtimeRegistry.registerAll(List.of(blockRow)) == 0,
                "native content registry should treat identical block registration rows as idempotent");
        require(runtimeRegistry.registrations("blocks").size() == 1,
                "native content registry should filter block rows by domain");
        List<EchoAdapterCoreRegistryEntry> blockEntries = runtimeRegistry.entries("blocks");
        require(blockEntries.size() == 1,
                "native content registry should convert block rows into AdapterCore registry entries");
        EchoAdapterCoreRegistryEntry blockEntry = blockEntries.getFirst();
        require(blockEntry.voxelBlock().isPresent(),
                "native block registration should expose a live voxel block");
        EchoVoxelBlock nativeBlock = blockEntry.requireVoxelBlock();
        require(nativeBlock.id().equals("echoruntimehost:native_runtime_glass"),
                "native block voxel id should come from registration metadata");
        require(nativeBlock.displayName().equals("Native Runtime Glass"),
                "native block display name should come from registration metadata");
        require(nativeBlock.argb() == 0xFF7FA6D9,
                "native block color should parse from registration metadata");
        require(nativeBlock.detailArgb() == 0xFFB9D4FF,
                "native block detail color should parse from registration metadata");
        require(nativeBlock.atlasKey().equals("echoruntimehost/block/native_runtime_glass"),
                "native block atlas key should come from registration metadata");
        require(nativeBlock.materialPattern() == EchoVoxelMaterialPattern.CACHE_PANEL,
                "native block material pattern should come from registration metadata");
        require(!nativeBlock.solid() && !nativeBlock.opaque(),
                "native block collision and opacity flags should come from registration metadata");
        require(nativeBlock.hardness() == 0.25D,
                "native block hardness should parse from registration metadata");

        EchoAdapterCoreStandaloneContentBridge importedBridge =
                bridge.withRuntimeEntriesReplacingContentIds(runtimeRegistry.entries());
        require(importedBridge.registry().size() == adapterCoreRegistry.size() + 1,
                "native block registration should merge into the AdapterCore runtime registry");
        require(importedBridge.registry()
                        .requireLiveVoxelBlock("echoruntimehost:native_runtime_glass")
                        .materialPattern() == EchoVoxelMaterialPattern.CACHE_PANEL,
                "native block registration should be retrievable as a live voxel block after bridge import");
    }

    private static void writeReports(
            Path workspaceRoot,
            EchoCompatRuntimeResult compat,
            EchoNeoForgeMetadataScanResult metadataScan,
            EchoNeoForgeModCandidate ashfallCandidate
    ) throws IOException {
        Path reportDir = standaloneRoot(workspaceRoot).resolve("reports/echo/standalone");
        Files.createDirectories(reportDir);

        EchoAdapterCoreStandaloneContentBridge bridge = compat.adapterCoreBridge();
        EchoAdapterCoreStandaloneRegistry adapterCoreRegistry = compat.adapterCoreRegistry();
        EchoCompatMigrationPlan plan = compat.migrationPlan();
        EchoCompatMigrationPolicy policy = compat.migrationPolicy();
        EchoCompatValidationResult validation = compat.targetValidation();
        EchoCompatDiagnostics diagnostics = compat.diagnostics();

        Files.writeString(reportDir.resolve("runtime-compatibility.json"), """
                {
                  "schema": "echo.standalone.runtime_compatibility.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "phase": "14.17",
                  "summary": "Compatibility runtime created service-bound AdapterCore, mapping, validation, migration, and diagnostics services while keeping NeoForge candidates diagnostics-only.",
                  "runtimeResultServiceBound": true,
                  "boundaryServiceBound": true,
                  "adapterCoreBridgeServiceBound": true,
                  "adapterCoreRegistryServiceBound": true,
                  "mappingRegistryServiceBound": true,
                  "migrationPolicyServiceBound": true,
                  "targetValidatorServiceBound": true,
                  "targetValidationServiceBound": true,
                  "migrationPlannerServiceBound": true,
                  "migrationPlanServiceBound": true,
                  "diagnosticsServiceBound": true,
                  "mappingCount": %d,
                  "supportedMappingCount": %d,
                  "manualReviewMappingCount": %d,
                  "blockedMappingCount": %d,
                  "sourceRecordCount": %d,
                  "targetValidationValid": %s,
                  "targetValidationWarnings": %d,
                  "targetValidationErrors": %d,
                  "migrationPlanBlocked": %s,
                  "migrationStepCount": %d,
                  "migrationMutationStepCount": %d,
                  "migrationManualReviewStepCount": %d,
                  "diagnosticCount": %d,
                  "diagnosticWarnings": %d,
                  "diagnosticErrors": %d,
                  "adapterCoreBindingCount": %d,
                  "adapterCoreReadyBindingCount": %d,
                  "adapterCoreRegistryCount": %d,
                  "adapterCoreSupportsAllRuntimes": %s,
                  "rendererTarget": "%s",
                  "neoforgeFixtureCandidates": %d,
                  "neoforgeCandidateRuntimeStatus": "%s",
                  "neoforgeClassloaderCreated": false,
                  "neoforgeModuleCodeExecuted": false
                }
                """.formatted(
                compat.mappingRegistry().count(),
                compat.mappingRegistry().supportedCount(),
                compat.mappingRegistry().manualReviewCount(),
                compat.mappingRegistry().blockedCount(),
                compat.sourceRecords().size(),
                validation.valid(),
                validation.warningCount(),
                validation.errorCount(),
                plan.blocked(),
                plan.steps().size(),
                plan.mutationStepCount(),
                plan.manualReviewStepCount(),
                diagnostics.count(),
                diagnostics.warningCount(),
                diagnostics.errorCount(),
                bridge.bindingCount(),
                bridge.readyBindingCount(),
                adapterCoreRegistry.size(),
                adapterCoreRegistry.supportsAllAdapterCoreRuntimes() && bridge.supportsAllAdapterCoreRuntimes(),
                escape(bridge.renderTarget().adapterId()),
                metadataScan.candidateCount(),
                escape(ashfallCandidate.runtimeStatus())
        ));

        Files.writeString(reportDir.resolve("compatibility-boundary.json"), """
                {
                  "schema": "echo.standalone.compatibility_boundary.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "adapterRuleCount": %d,
                  "adapterRules": %s,
                  "contractsStayAdapterFree": %s,
                  "minecraftNeoForgeBridgesStayOutOfContracts": %s,
                  "adapterCoreIdsCanonicalAcrossRuntimes": %s,
                  "liveClientRendererTarget": "%s",
                  "migrationPlansBeforeMutation": %s
                }
                """.formatted(
                EchoRuntimeCompatibilityAdapterBoundary.adapterRules().size(),
                jsonArray(EchoRuntimeCompatibilityAdapterBoundary.adapterRules()),
                EchoRuntimeCompatibilityAdapterBoundary.adapterRules().contains("standalone contracts do not depend on adapters"),
                EchoRuntimeCompatibilityAdapterBoundary.adapterRules().contains("Minecraft and NeoForge bridges stay out of runtime contracts"),
                EchoRuntimeCompatibilityAdapterBoundary.adapterRules()
                        .contains("AdapterCore content ids remain canonical across NeoForge, ECHO Native Loader, and Standalone"),
                escape(bridge.renderTarget().adapterId()),
                EchoRuntimeCompatibilityAdapterBoundary.adapterRules()
                        .contains("migration tooling plans before it mutates player data")
        ));

        Files.writeString(reportDir.resolve("compatibility-mappings.json"), """
                {
                  "schema": "echo.standalone.compatibility_mappings.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "mappingCount": %d,
                  "supportedCount": %d,
                  "manualReviewCount": %d,
                  "blockedCount": %d,
                  "cleanWaterBottleTarget": "%s",
                  "secureCrashSiteTarget": "%s",
                  "mappings": %s
                }
                """.formatted(
                compat.mappingRegistry().count(),
                compat.mappingRegistry().supportedCount(),
                compat.mappingRegistry().manualReviewCount(),
                compat.mappingRegistry().blockedCount(),
                escape(compat.mappingRegistry()
                        .requireSource("echoashfallprotocol:item/clean_water_bottle")
                        .targetId()),
                escape(compat.mappingRegistry()
                        .requireSource("echoashfallprotocol:mission/secure_crash_site")
                        .targetId()),
                mappingArray(compat.mappingRegistry().all())
        ));

        Files.writeString(reportDir.resolve("compatibility-source-records.json"), """
                {
                  "schema": "echo.standalone.compatibility_source_records.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "sourceRecordCount": %d,
                  "sourceRecords": %s
                }
                """.formatted(
                compat.sourceRecords().size(),
                sourceRecordArray(compat.sourceRecords())
        ));

        Files.writeString(reportDir.resolve("compatibility-target-validation.json"), """
                {
                  "schema": "echo.standalone.compatibility_target_validation.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "valid": %s,
                  "warningCount": %d,
                  "errorCount": %d,
                  "issues": %s
                }
                """.formatted(
                validation.valid(),
                validation.warningCount(),
                validation.errorCount(),
                validationIssueArray(validation.issues())
        ));

        Files.writeString(reportDir.resolve("compatibility-migration-policy.json"), """
                {
                  "schema": "echo.standalone.compatibility_migration_policy.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "policyId": "%s",
                  "manualOnly": %s,
                  "executeAutomatically": %s,
                  "mutateSourceAllowed": %s,
                  "backupRequired": %s
                }
                """.formatted(
                escape(policy.policyId()),
                policy.manualOnly(),
                policy.executeAutomatically(),
                policy.mutateSourceAllowed(),
                policy.backupRequired()
        ));

        Files.writeString(reportDir.resolve("compatibility-migration-plan.json"), """
                {
                  "schema": "echo.standalone.compatibility_migration_plan.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "planId": "%s",
                  "sourceProfileId": "%s",
                  "targetProfileId": "%s",
                  "blocked": %s,
                  "stepCount": %d,
                  "firstAction": "%s",
                  "manualReviewStepCount": %d,
                  "mutationStepCount": %d,
                  "requiresBackup": %s,
                  "steps": %s
                }
                """.formatted(
                escape(plan.planId()),
                escape(plan.sourceProfileId()),
                escape(plan.targetProfileId()),
                plan.blocked(),
                plan.steps().size(),
                escape(plan.steps().getFirst().actionKind().name()),
                plan.manualReviewStepCount(),
                plan.mutationStepCount(),
                policy.backupRequired(),
                migrationStepArray(plan.steps())
        ));

        Files.writeString(reportDir.resolve("compatibility-manual-review.json"), """
                {
                  "schema": "echo.standalone.compatibility_manual_review.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "manualReviewMappingCount": %d,
                  "manualReviewStepCount": %d,
                  "automaticExecutionAllowed": %s,
                  "sourceMutationAllowed": %s,
                  "backupRequired": %s,
                  "manualReviewTargetId": "%s"
                }
                """.formatted(
                compat.mappingRegistry().manualReviewCount(),
                plan.manualReviewStepCount(),
                policy.executeAutomatically(),
                policy.mutateSourceAllowed(),
                policy.backupRequired(),
                escape(compat.mappingRegistry()
                        .requireSource("echoashfallprotocol:save/player_progress_v1")
                        .targetId())
        ));

        Files.writeString(reportDir.resolve("compatibility-diagnostics.json"), """
                {
                  "schema": "echo.standalone.compatibility_diagnostics.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeCompatSmokeHarness",
                  "status": "PASS",
                  "diagnosticCount": %d,
                  "warningCount": %d,
                  "errorCount": %d,
                  "metadataFixtureWarningCount": %d,
                  "metadataFixtureErrorCount": %d,
                  "diagnostics": %s
                }
                """.formatted(
                diagnostics.count(),
                diagnostics.warningCount(),
                diagnostics.errorCount(),
                metadataScan.warningCount(),
                metadataScan.errorCount(),
                diagnosticArray(diagnostics.all())
        ));
    }

    private static Path standaloneRoot(Path workspaceRoot) {
        if (workspaceRoot.getFileName() != null
                && workspaceRoot.getFileName().toString().equalsIgnoreCase("echo-standalone-runtime")) {
            return workspaceRoot;
        }
        if (Files.isDirectory(workspaceRoot.resolve("echo-runtime-app"))
                && Files.isRegularFile(workspaceRoot.resolve("settings.gradle"))) {
            return workspaceRoot;
        }
        Path nested = workspaceRoot.resolve("echo-standalone-runtime");
        if (Files.isDirectory(nested)) {
            return nested;
        }
        return workspaceRoot;
    }

    private static String mappingArray(List<EchoCompatContentMapping> mappings) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < mappings.size(); i++) {
            EchoCompatContentMapping mapping = mappings.get(i);
            json.append("{\"mappingId\":\"").append(escape(mapping.mappingId()))
                    .append("\",\"sourceId\":\"").append(escape(mapping.sourceId()))
                    .append("\",\"sourceKind\":\"").append(mapping.sourceKind().name())
                    .append("\",\"targetId\":\"").append(escape(mapping.targetId()))
                    .append("\",\"targetKind\":\"").append(mapping.targetKind().name())
                    .append("\",\"status\":\"").append(mapping.status().name())
                    .append("\"}");
            if (i + 1 < mappings.size()) {
                json.append(", ");
            }
        }
        return json.append(']').toString();
    }

    private static String sourceRecordArray(List<EchoCompatSourceRecord> records) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < records.size(); i++) {
            EchoCompatSourceRecord record = records.get(i);
            json.append("{\"recordId\":\"").append(escape(record.recordId()))
                    .append("\",\"sourceId\":\"").append(escape(record.sourceId()))
                    .append("\",\"sourceKind\":\"").append(record.sourceKind().name())
                    .append("\",\"recordType\":\"").append(escape(record.recordType()))
                    .append("\",\"fingerprint\":\"").append(escape(record.fingerprint()))
                    .append("\"}");
            if (i + 1 < records.size()) {
                json.append(", ");
            }
        }
        return json.append(']').toString();
    }

    private static String validationIssueArray(List<EchoCompatValidationIssue> issues) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < issues.size(); i++) {
            EchoCompatValidationIssue issue = issues.get(i);
            json.append("{\"severity\":\"").append(issue.severity().name())
                    .append("\",\"mappingId\":\"").append(escape(issue.mappingId()))
                    .append("\",\"message\":\"").append(escape(issue.message()))
                    .append("\"}");
            if (i + 1 < issues.size()) {
                json.append(", ");
            }
        }
        return json.append(']').toString();
    }

    private static String migrationStepArray(List<EchoCompatMigrationStep> steps) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < steps.size(); i++) {
            EchoCompatMigrationStep step = steps.get(i);
            json.append("{\"stepId\":\"").append(escape(step.stepId()))
                    .append("\",\"actionKind\":\"").append(step.actionKind().name())
                    .append("\",\"sourceId\":\"").append(escape(step.sourceId()))
                    .append("\",\"targetId\":\"").append(escape(step.targetId()))
                    .append("\",\"requiresBackup\":").append(step.requiresBackup())
                    .append(",\"mutatesSource\":").append(step.mutatesSource())
                    .append("}");
            if (i + 1 < steps.size()) {
                json.append(", ");
            }
        }
        return json.append(']').toString();
    }

    private static String diagnosticArray(List<EchoCompatDiagnostic> diagnostics) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < diagnostics.size(); i++) {
            EchoCompatDiagnostic diagnostic = diagnostics.get(i);
            json.append("{\"severity\":\"").append(diagnostic.severity().name())
                    .append("\",\"subject\":\"").append(escape(diagnostic.subject()))
                    .append("\",\"message\":\"").append(escape(diagnostic.message()))
                    .append("\"}");
            if (i + 1 < diagnostics.size()) {
                json.append(", ");
            }
        }
        return json.append(']').toString();
    }

    private static String jsonArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append('"').append(escape(values.get(i))).append('"');
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        return json.append(']').toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
