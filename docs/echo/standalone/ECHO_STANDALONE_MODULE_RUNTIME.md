# ECHO Standalone Module Runtime

The standalone module runtime now has two supported operating modes:

- Descriptor-only mode remains the safe catalog baseline. It discovers module descriptors, validates them, resolves dependencies, builds feature graphs, advances metadata lifecycle state, and binds module graph services without executing addon code.
- Native Loader ABI v1 is the standalone executable addon path. It creates isolated module classloaders, executes raw runtime entrypoints, AdapterCore entrypoints, or Native Platform entrypoints, enforces module capability permissions, publishes lifecycle bus events, runs data reload hooks, contains load/reload failures, revokes module-scoped runtime state, and unloads without launching Minecraft, NeoForge, or Fabric.

Addon support is built into the standalone runtime. The standalone loader scans folder, `.jar`, `.zip`, and `.echo-addon` addon roots for ECHO descriptors and runs compatible modules through its own ABI. It does not use Minecraft's native mod loader as its boot path; native mod-loader integrations are compatibility targets that can be bridged through standalone-safe ECHO entrypoints.

The current executable evidence is:

- `reports/echo/standalone/runtime-modules.json`
- `reports/echo/standalone/runtime-module-graph.json`
- `reports/echo/standalone/runtime-feature-graph.json`
- `reports/echo/standalone/runtime-services.json`
- `reports/echo/standalone/runtime-module-status.json`
- `reports/echo/standalone/native-loader-abi-v1-smoke.json`
- `reports/echo/standalone/real-module-execution-smoke.json`
- `reports/echo/standalone/addon-archive-compatibility-smoke.json`
- `reports/echo/standalone/beta-native-loader-execution.json`
- `reports/echo/standalone/beta-readiness-gate.json`

`verifyStandaloneModuleRuntime` regenerates the descriptor-only, Native Loader ABI, real module execution, and folder/jar/zip/`.echo-addon` addon evidence before verification. `runStandaloneBetaReadinessGate` additionally emits `beta-native-loader-execution.json`, a beta-blocking aggregate that proves the standalone boot path is `echo-native-loader-abi-v1`, official native entrypoints execute, folder/jar/zip/`.echo-addon` addons execute, unsafe `.echo-addon` archive classpaths are rejected, and no Minecraft/NeoForge/Fabric mod-loader command is used. These gates reject bootstrap placeholder schemas and require concrete `PASS` reports that prove descriptor graph safety, runtime service binding, executable ABI lifecycle behavior, real module load/reload/unload, unsafe addon rejection, and corrupt addon archive recovery.

## Descriptor Sources

The scanner discovers descriptors in `META-INF` folders with these names:

```text
META-INF/echo.mod.json
META-INF/echo.native.json
META-INF/echo.runtime.json
```

`EchoRuntimeModuleDescriptorSchema` is the executable descriptor schema contract for this standalone runtime slice. It locks schema id `echo.runtime.module.v1`, the scanner descriptor source names, required authoring fields, supported field names, field types, and the executable ABI v1 field set.

Descriptors parse into `EchoRuntimeModuleDescriptor`. Required authoring fields are:

- `id`
- `name`

Supported fields are:

- `schema`
- `id`
- `name`
- `version`
- `kind`
- `role`
- `side`
- `trust`
- `trustLevel`
- `official`
- `standalone`
- `requires`
- `optional`
- `provides`
- `consumes`
- `gameModes`
- `permissions`
- `classPath`
- `classpath`
- `entrypoint`
- `adapterCoreEntrypoint`
- `nativeEntrypoint`
- `nativeClasspath`
- `requiresVersions`
- `optionalVersions`
- `access`

Executable ABI v1 fields are:

- `permissions`
- `classPath`
- `classpath`
- `entrypoint`
- `adapterCoreEntrypoint`
- `nativeEntrypoint`
- `nativeClasspath`
- `requiresVersions`
- `optionalVersions`

Required and optional dependency version maps use semver-style ranges such as `[1.0.0,2.0.0)` and `[2.0.0,)`. Required dependency version mismatches fail the dependent module. Present optional dependency version mismatches emit warnings and do not block load; missing optional dependencies are allowed.

This standalone checkout does not contain the full `echocore` datapack-example resource tree. Descriptor examples are maintained as executable smoke fixtures in `EchoRuntimeNativeLoaderAbiSmokeHarness`; the generated smoke report exports `descriptorSchemaId`, `descriptorSchemaSources`, `descriptorSchemaFields`, `descriptorExecutableAbiV1Fields`, `descriptorSchemaCoversExecutableAbiV1`, and `descriptorFieldTypes`.

## Runtime Pieces

- `EchoRuntimeModuleManager` orchestrates scanning, dependency resolution, trust validation, lifecycle publication, executable loading, and service binding.
- `EchoRuntimeModuleDescriptorScanner` discovers descriptor files from one or more folder roots and from `.jar` / `.zip` / `.echo-addon` addon archives.
- `EchoRuntimeModuleDescriptorParser` parses descriptor JSON through the structured runtime parser.
- `EchoRuntimeModuleDescriptorSchema` names the descriptor schema id, source files, field list, field types, required fields, and executable ABI v1 field set.
- `EchoRuntimeModuleDependencyResolver` resolves required and optional edges, validates dependency version ranges, fails required dependency cycles, and leaves optional dependency mismatches as warnings.
- `EchoRuntimeFeatureGraph` maps provided and consumed features.
- `EchoRuntimeModuleTrustPolicy` validates accepted trust levels.
- `EchoRuntimeModuleSandboxPolicy` keeps descriptor-only mode safe and exposes the opt-in ABI v1 execution policy.
- `EchoRuntimeModuleLoader` advances module lifecycle state and, in ABI v1 mode, creates a confined classloader from descriptor `classPath` / `access.nativeClasspath` entries inside the inferred addon module root. Loaded required dependencies and present optional dependencies are exposed as dependency classloaders, not as raw sibling paths.
- `EchoRuntimeModuleRegistry` stores descriptors, lifecycle state, traces, notes, and runtime status.
- `EchoRuntimeModuleLifecycleBus` publishes ordered lifecycle events to observers without letting observer exceptions break module activation.
- `EchoRuntimeModuleServiceBinder` registers module graph services into `EchoRuntimeServiceRegistry`.
- `EchoRuntimeModuleEntrypoint` is the raw ABI v1 contract for load, data reload, and unload.
- `EchoRuntimeAdapterCoreEntrypoint` is the AdapterCore ABI v1 contract for activate, data reload, and deactivate.
- `dev.echo.nativeplatform.contracts.EchoNativeModuleEntrypoint` is mirrored as the Native Platform ABI contract for real ECHO modules that declare `access.nativeEntrypoint`.
- `dev.echo.nativeplatform.contracts.EchoNativeSurfaceModuleEntrypoint` and `EchoNativeActivationSurfaceRegistrar` bridge surface-descriptor style Native Platform modules that expose `describeNativeSurfaces`.
- `dev.echo.nativeplatform.contracts.EchoNativeServiceRegistry` records and revokes Native Platform services registered by real module entrypoints.
- `EchoRuntimeModuleContext` exposes controlled module APIs: `requireService`, `registerContent`, `exportService`, `importService`, `publishConfig`, `registerAsset`, `writeSaveData`, and `readSaveData`.
- `EchoRuntimeModuleContentActivationRegistry` records and deactivates permission-checked content activation by module.
- `EchoRuntimeModuleServiceExportRegistry` records and revokes permission-checked module-scoped services by module.
- `EchoRuntimeModuleDataRegistry` records permission-checked module config, module-root-confined assets, and module save data. Runtime config and assets are revoked on unload; save data persists across unload.

## Lifecycle

The runtime supports these module lifecycle states:

```text
DISCOVERED
DESCRIPTOR_VALIDATED
DEPENDENCIES_RESOLVED
FEATURES_RESOLVED
TRUST_VALIDATED
LOADED
SERVICES_BOUND
COMMON_INIT
CLIENT_INIT
SERVER_INIT
DATA_RELOADED
READY
UNLOADED
DISABLED
FAILED
```

ABI v1 load order is dependency-first. Required dependencies always order before dependents. Present optional dependencies also order before optional consumers so soft integrations can import optional services, while missing optionals remain non-blocking. If a required dependency fails during executable load, dependent modules fail before classloader creation or entrypoint execution and emit a required-dependency diagnostic. If an entrypoint registers content or exports services and then fails during load, those module-scoped registrations are revoked before the module is left in `FAILED`. ABI v1 unload order is reverse activation order so consumers deactivate before providers, including optional consumers before optional providers, and unload revokes the module's service exports and content activations even when an unload hook throws. Data reload runs in activation order, and reload failures detach the failed module before the later unload phase.

Native Platform entrypoints run through the standalone loader without NeoForge. The loader maps `discover`, `resolve`, `loadClasses`, `construct`, `registerServices`, `registerContent`, `commonSetup`, `clientSetup`, `serverSetup`, and `ready` into the standalone load phase, maps data reload through the safe content/setup/ready subset, and maps unload to `shutdown`. Native services are revoked with the module on unload or failure.

Legacy Native Platform declarations that predate `EchoNativeModuleEntrypoint` can execute through the reflective compatibility bridge when they expose a supported `bootstrap()` or `describeNativeSurfaces(Map<String, String>)` method. Unsupported legacy declarations still fail with a clear module execution diagnostic.

## ABI v1 Smoke Gate

`EchoRuntimeNativeLoaderAbiSmokeHarness` proves the executable gate without NeoForge:

```text
echoabi-field-generator -> load, content activation, service export, config publish, asset registration, save data write, data reload, unload
echoabi-adaptercore-addon -> AdapterCore activate, content activation, service export, data reload, deactivate
echoabi-service-provider -> export service before dependent consumer load
echoabi-service-consumer -> import provider service, unload before provider
echoabi-optional-provider -> export optional service before optional consumer load
echoabi-optional-consumer -> import optional provider service, missing optional allowed, unload before optional provider
echoabi-optional-version-warning -> READY with non-blocking optional dependency version warning
echoabi-dependency-provider -> owns SharedDependency inside its confined module classloader
echoabi-dependency-consumer -> resolves SharedDependency only through the declared required dependency classloader
echoabi-surface-native -> Native Platform surface entrypoint, activation service, data reload, unload, native service revocation
echoabi-legacy-native -> reflective legacy bootstrap, data reload, unload, native service revocation
echoabi-unload-crash -> READY after load, FAILED during unload, state revoked
echoabi-crash-fixture -> FAILED during load
echoabi-dependent-on-crash -> FAILED without entrypoint execution after required dependency load failure
echoabi-partial-state-crash -> FAILED after content/service registration, then state revoked
echoabi-reload-crash -> READY after load, FAILED during data reload, detached before unload
echoabi-denied-content -> FAILED without content activation
echoabi-denied-registry-access -> FAILED without raw registry bypass
echoabi-denied-service-export -> FAILED without service export
echoabi-unknown-permission -> FAILED before entrypoint execution because descriptor declares an unknown permission
echoabi-classpath-escape -> FAILED before classloader activation
echoabi-incompatible-core -> FAILED during dependency version resolution
echoabi-cycle-a / echoabi-cycle-b -> FAILED during dependency cycle resolution
```

The generated report records these guard fields:

- `classloaderIsolated`
- `dependencyVersionMismatchIssues`
- `dependencyCycleIssues`
- `dependencyOrderedServiceLoad`
- `dependencyClassloaderDelegation`
- `reverseDependencyClassloaderUnload`
- `optionalDependencyOrderedLoad`
- `missingOptionalDependencyAllowed`
- `optionalVersionWarningNonBlocking`
- `unknownPermissionIssues`
- `unknownPermissionExecuted`
- `dependentOnFailedDependencyExecuted`
- `requiredDependencyFailureDiagnostic`
- `reverseDependencyUnload`
- `reverseOptionalDependencyUnload`
- `permissionCheckedActivationCount`
- `permissionCheckedServiceExportCount`
- `permissionCheckedServiceImportCount`
- `permissionCheckedConfigCount`
- `permissionCheckedAssetCount`
- `permissionCheckedSaveDataCount`
- `runtimeConfigRevokedOnUnload`
- `runtimeAssetsRevokedOnUnload`
- `moduleSaveDataPersistedAfterUnload`
- `partialStateCrashContentRevoked`
- `partialStateCrashServiceRevoked`
- `unloadCrashFinalLifecycle`
- `unloadCrashContentRevoked`
- `unloadCrashServiceRevoked`
- `unloadCrashDiagnostic`
- `preUnloadServiceExportCount`
- `preUnloadContentActivationCount`
- `postUnloadServiceExportCount`
- `postUnloadContentActivationCount`
- `deniedActivationCount`
- `deniedRegistryAccessActivationCount`
- `deniedServiceExportCount`
- `classpathEscapeExecuted`
- `reloadCrashFinalLifecycle`
- `reloadCrashUnloaded`
- `lifecycleBusEventCount`
- `lifecycleBusObserverEventCount`
- `surfaceNativeServiceRegisteredBeforeUnload`
- `legacyNativeServiceRegisteredBeforeUnload`
- `surfaceNativeServiceRevokedAfterUnload`
- `legacyNativeServiceRevokedAfterUnload`

## Real Module Execution Gate

`EchoRuntimeRealModuleExecutionSmokeHarness` materializes real descriptors and standalone-safe native source from `C:\Development\Github\ECHO-Modules`, compiles them into temporary addon folders, and runs 94 real modules through a standalone `load -> reload -> unload` dependency chain:

- `echocore` through the reflective legacy native bootstrap bridge
- `echoplatformcore` through `EchoNativeSurfaceModuleEntrypoint`
- `echoschemacore` through its real schema registry native surface adapter
- `echovalidationcore` through its real validation and diagnostic surface adapter
- `echoadaptercore` through its real AdapterCore native module adapter
- `echocontentcore` through its real Native Platform content ownership/reference/gate surface adapter
- `echoassetcore` through its real asset registry, validation, and TextureForge prompt/report surface adapter
- `echorecipecore` through its real native recipe runtime adapter for machine recipe execution
- `echopackcore` through its real AdapterCore Ashfall pack load-plan adapter
- `echometadatacore` through its real manifest normalization, schema validation, conflict detection, and fallback scan surface adapter
- `echomodulegraph` through its real module graph, load-order, duplicate detection, and validation diagnostic surface adapter
- `echonetcore` through its real AdapterCore packet-service native adapter
- `echodatacore` through its real AdapterCore runtime profile persistence/sync adapter
- `echoworldcore` through its real AdapterCore world effects, hazard/status, spawn/difficulty, POI, region transition, and world data catalog adapter
- `echoatmospherecore` through its real atmosphere profile tick, state application, visibility, particle, sky fog, and live level tick adapter
- `echoweathercore` through its real weather schedule, live schedule tick, HUD/audio/render state, forecast, warning, shelter mitigation, route risk, Weather Radio, Weather Station, Emergency Siren, Climate Sensor, and Route Warning Post adapter
- `echoprogressioncore` through its real unlock graph, gates, objectives, recipe unlocks, and UI/world-event unlock surface adapter
- `echosocialcore` through its real faction, dialogue tree, NPC profile, and villager replacement surface adapter
- `echoeventcore` through its real world event, scheduler, and validation feature contract adapter
- `echoencountercore` through its real encounter definition, boss gate, and faction patrol contract adapter
- `echoeconomycore` through its real Agent9 economy runtime adapter
- `echoquestdirector` through its real mission selection, route pacing, campaign pressure, reminders, signals, recommendations, and event pacing adapter
- `echonpcore` through its real NPC profile, dialogue, screen, service, trade, and villager replacement contract adapter
- `echoguidecore` through its real guide page, search, and unlock visibility contract adapter
- `echolorecore` through its real lore entry, audio log, blackbox entry, and environmental story contract adapter
- `echostatuscore` through its real status effect application, stacking refresh, exposure mitigation, and live status registry adapter
- `echospawncore` through its real difficulty-scaled spawn rule event, spawn-zone state, and live finalize-spawn adapter
- `echostructurecore` through its real POI lookup, marker persistence, discovery state, and live structure level tick adapter
- `echolootcore` through its real Agent9 loot runtime adapter
- `echobiomecore` through its real biome ambient state, hazard overlay, HoloMap layer, and live biome tick adapter
- `echodifficultycore` through its real difficulty profile, pack policy, telemetry, profile selection, and difficulty application adapter
- `echocombatcore` through its real damage, weapon trait, enemy scaling, boss phase, armor, shield, and combat telemetry surface adapter
- `echocreaturecore` through its real creature archetype, AI profile, and scan metadata surface adapter
- `echoarmory` through its real gear/module state, station operation preview, and route-readiness surface adapter
- `echoagriculturereclamation` through its real greenhouse machine tuning, seed process, UI process card, and restoration worldgen surface adapter
- `echomachinecore` through its real Agent9 machine runtime adapter
- `echopowercore` through its real Agent9 power runtime adapter
- `echologisticscore` through its real Agent9 logistics runtime adapter
- `echosoundcore` through its real audio dispatch, adaptive music, ambience, stinger, UI cue, audio profile, and network action adapter
- `echohealthcore` through its real AdapterCore runtime health report adapter
- `echoagentcore` through its real safe command, task queue, prompt bundle, and run report surface adapter
- `echobridgecore` through its real session state, safe action, and local transport surface adapter
- `echoreportcore` through its real support bundle and release-readiness surface adapter
- `echocameracore` through its real camera profile, shake safety, and target anchor surface adapter
- `echocinematiccore` through its real cinematic sequence, pacing, and trigger UI surface adapter
- `echocodexcore` through its real AdapterCore Codex lookup service bridge
- `echocreatorcore` through its real permission gate, session project, pack authoring, and dashboard UI surface adapter
- `echorendercore` through its real AdapterCore preview frame render service bridge
- `echoscreencore` through its real AdapterCore screen composition and native screen projection bridge
- `echoscriptcore` through its real script definition, command, migration, UI bridge, and validation surface adapter
- `echoruntimeguard` through its real runtime budget, diagnostics, network, and command bridge adapter
- `echocommunitybridge` through its real server status, launcher chat, Discord sanitization, and player identity surface adapter
- `echoindex` through its real recipe query service and inventory overlay native adapter
- `echoholomap` through its real route snapshot, map service, and native map surface projection adapter
- `echolens` through its real field scan, inspection service, and native lens surface projection adapter
- `echoterminal` through its real dashboard surface and native terminal surface projection adapter
- `echotextureforge` through its real spec registry, prompt export, review state, texture audit, and dashboard surface adapter
- `echothemecore` through its real theme token, asset, render profile, surface, and application adapter
- `echotutorialcore` through its real tutorial card, flow, hint, onboarding, and tooltip surface adapter
- `echowiki` through its real guide, documentation, search, and ScreenCore surface lookup adapter
- `echobasegrid` through its real Agent9 base-grid runtime adapter
- `echoindustrialnexus` through its real Agent9 industrial runtime adapter
- `echologisticsnetwork` through its real Agent9 logistics-network runtime adapter
- `echomultiblockcore` through its real Agent9 multiblock runtime adapter
- `echopowergrid` through its real Agent9 power-grid runtime adapter
- `echorecovery` through its real grave/cache/compass/safe-mode field recovery plan adapter
- `echomissioncore` through its real AdapterCore objective progression and mission route adapter
- `echoplayercore` through its real Native Platform player travel, TPA, warp, and cooldown surface adapter
- `echoinputcore` through its real input feature contract and terminal focus route-priority adapter
- `echohudcore` through its real HUD snapshot and native HUD surface projection adapter
- `echonotificationcore` through its real toast, alert, mission update, and tutorial hint feature contract adapter
- `echovehiclecore` through its real Agent9 vehicle runtime adapter
- `echoblockworks` through its real Native Platform block catalog, pattern cutter, palette conversion, showcase, and worldgen surface adapter
- `echopresencelink` through its real AdapterCore story presence native adapter with missing optional integrations allowed
- `echoconvoyprotocol` through its real Agent9 convoy route, fuel, and cargo runtime adapter
- `echofamiliarcore` through its real companion registry, bond progression, command, and upgrade native adapter
- `signalos` through its real AdapterCore terminal, archive, data-drive, mission, chapter, and terminal-session native adapter
- `echospellcore` through its real AdapterCore Signal Focus spell cast resolution native adapter
- `echoritualcore` through its real AdapterCore altar activation, ritual output, and completion native adapter
- `echocursecore` through its real AdapterCore persistent curse state, cleansing, contract debt, and tick-effect native adapter
- `echoriftworlds` through its real AdapterCore pocket rift lifecycle, hazard, route, and return native adapter
- `echoblackboxprotocol` through its real AdapterCore Prime-route archive native adapter
- `echonexusprotocol` through its real AdapterCore Prime-route signal native adapter
- `echoorbitalremnants` through its real AdapterCore Prime-route data-drive native adapter
- `echoprimecore` through its real AdapterCore Prime route mission, flag, and save-state native adapter
- `echostationfall` through its real AdapterCore Stationfall chapter unlock native adapter
- `echogrimoire` through its real AdapterCore Arcane Codex archive native adapter
- `echoarcanacore` through its real AdapterCore Arcane Codex signal, flag, mission, and save-state native adapter
- `echorelictech` through its real AdapterCore relic containment, vault, instability, and story effect native adapter
- `echoarcaneindex` through its real AdapterCore Arcane Index chapter unlock native adapter
- `echoaetherworks` through its real AdapterCore AetherWorks presence-link native adapter
- `echoashfallprotocol` through its real Ashfall Protocol native module entrypoint, route contracts, machine runtime binding, Agent9 tech runtime, and AdapterCore service evidence
- `echogalacticcore` through its real GalacticCore native module entrypoint, runtime/gateway/host-execution/live-session services, and release smoke actions for routes, rockets, oxygen, dungeons, screens, and host-owned mutations
- `echoaddonapi` through the direct Native Platform entrypoint

For WorldCore, the harness materializes the Ashfall world catalog resources needed by the native adapter and verifies the reference catalog counts from real ECHO-Modules data: 8 regions, 12 hazards, 9 weather profiles, 36 spawn rules, and 193 definition source files.

For executable coverage only, the harness materializes descriptors with `access.forceStandaloneExecution=true`. This lets tooling-class native modules such as MetadataCore and ModuleGraph execute inside the real-module smoke without changing their normal full-catalog `runtime-tooling-only` classification.

The generated report is `reports/echo/standalone/real-module-execution-smoke.json`. It records:

- `moduleIds`
- `sourceDescriptors`
- `sourceEntrypoints`
- `nativeEntrypoints`
- `loadReloadUnloadExecuted`
- `realDependencyChainExecuted`
- `legacyNativeBootstrapExecuted`
- `surfaceNativeEntrypointExecuted`
- `adapterCoreNativeEntrypointExecuted`
- `schemaCoreNativeEntrypointExecuted`
- `validationCoreNativeEntrypointExecuted`
- `contentCoreNativeEntrypointExecuted`
- `assetCoreNativeEntrypointExecuted`
- `recipeCoreNativeEntrypointExecuted`
- `packCoreNativeEntrypointExecuted`
- `netCoreNativeEntrypointExecuted`
- `dataCoreNativeEntrypointExecuted`
- `worldCoreNativeEntrypointExecuted`
- `healthCoreNativeEntrypointExecuted`
- `missionCoreNativeEntrypointExecuted`
- `playerCoreNativeEntrypointExecuted`
- `blockworksNativeEntrypointExecuted`
- `presenceLinkNativeEntrypointExecuted`
- `runtimeFoundationNativeEntrypointsExecuted`
- `runtimeFoundationNativeServiceIds`
- `uiNavigationNativeEntrypointsExecuted`
- `uiNavigationNativeServiceIds`
- `techGridNativeEntrypointsExecuted`
- `techGridNativeServiceIds`
- `storyArcanaNativeEntrypointsExecuted`
- `storyArcanaNativeServiceIds`
- `gameplayBridgeNativeEntrypointsExecuted`
- `gameplayBridgeNativeServiceIds`
- `ashfallProtocolNativeEntrypointExecuted`
- `ashfallProtocolServiceCodeExecuted`
- `ashfallProtocolGameplayHookEvidence`
- `ashfallProtocolMajorRouteStatus`
- `ashfallProtocolMidgameRouteStatus`
- `ashfallProtocolLateGameRouteStatus`
- `ashfallProtocolMachineRuntimeBindingStatus`
- `ashfallProtocolAgent9TechRuntimeStatus`
- `galacticCoreNativeEntrypointExecuted`
- `galacticCoreNativeServiceIds`
- `galacticCoreRuntimeGatewaySmokeActionCount`
- `galacticCoreHostExecutionSmokeActionCount`
- `galacticCoreLiveSessionMutationCount`
- `schemaCoreNativeServiceId`
- `schemaCoreNativeServiceSurfaces`
- `validationCoreNativeServiceId`
- `validationCoreNativeServiceSurfaces`
- `contentCoreNativeServiceId`
- `contentCoreReferenceRoundTripExecuted`
- `assetCoreNativeServiceId`
- `assetCoreAssetRegistryRoundTripExecuted`
- `assetCoreAssetValidationRoundTripExecuted`
- `assetCoreTextureForgePromptReady`
- `recipeCoreNativeServiceId`
- `recipeCoreNativeHostStatus`
- `recipeCoreHostLoadedEntrypoint`
- `packCoreNativeServiceId`
- `packCoreLoadPlanExecuted`
- `netCoreNativeServiceId`
- `netCoreNativeServiceSurfaces`
- `dataCoreNativeServiceId`
- `dataCoreRuntimeProfileExecuted`
- `worldCoreNativeServiceId`
- `worldCoreWorldEffectsRuntimeContract`
- `worldCoreDataCatalogRuntimeContract`
- `worldCoreRegionCellSampleExecuted`
- `worldCoreDataCatalogSourceFileCount`
- `worldSurvivalNativeEntrypointsExecuted`
- `worldSurvivalNativeServiceIds`
- `betaLoopNativeEntrypointsExecuted`
- `betaLoopNativeServiceIds`
- `techAndUiNativeEntrypointsExecuted`
- `techAndUiNativeServiceIds`
- `mechanicsCoreNativeEntrypointsExecuted`
- `mechanicsCoreNativeServiceIds`
- `mechanicsCoreActivationEvidence`
- `healthCoreNativeServiceId`
- `healthCoreRuntimeReportExecuted`
- `missionCoreNativeServiceId`
- `missionCoreObjectiveProgressionExecuted`
- `playerCoreNativeServiceId`
- `playerCoreFeatureContractRoundTripExecuted`
- `blockworksNativeServiceId`
- `blockworksBlockCatalogRoundTripExecuted`
- `blockworksPaletteConversionRoundTripExecuted`
- `blockworksWorldgenSiteRoundTripExecuted`
- `presenceLinkNativeServiceId`
- `presenceLinkNativeServiceSurfaces`
- `nativeServiceCountsBeforeUnload`
- `nativeServiceIdsBeforeUnload`
- `nativeServiceRegisteredBeforeUnload`
- `nativeServiceRevokedOnUnload`
- `nativeServiceSurfaces`

## Addon Compatibility Gate

`EchoRuntimeAddonArchiveCompatibilitySmokeHarness` proves external addon discovery, execution, and recovery behavior:

- folder-scanned exploded addon directories with `META-INF/echo.mod.json`
- folder-scanned `.jar` addons with `META-INF/echo.mod.json`
- folder-scanned `.zip` addons with `META-INF/echo.mod.json`
- folder-scanned `.echo-addon` packages with `META-INF/echo.mod.json`
- raw standalone ABI entrypoints loaded directly from an exploded folder classpath
- raw standalone ABI entrypoints loaded directly from a jar archive
- raw standalone ABI entrypoints loaded directly from a `.echo-addon` archive package with descriptor `classPath: ["classes"]`
- Native Platform ABI entrypoints loaded directly from a zip archive
- unsafe `.echo-addon` archive packages with escaping descriptor classpaths such as `classPath: ["../classes"]` fail safely before entrypoint execution
- corrupt archive diagnostics that do not prevent valid archive addons from loading, reloading, and unloading

The generated report is `reports/echo/standalone/addon-archive-compatibility-smoke.json` with schema `echo.standalone.addon_archive_compatibility_smoke.v5`. The gate records `folderLoadReloadUnload`, `jarLoadReloadUnload`, `zipNativeLoadReloadUnload`, `echoAddonLoadReloadUnload`, `echoAddonInternalClassPath`, `unsafeEchoAddonRejected`, `unsafeEchoAddonLifecycle`, `unsafeEchoAddonEscapeDiagnostic`, and `corruptArchiveRecovered`.

## Permission Model

Module code does not receive the raw runtime service registry through `EchoRuntimeModuleContext`.

Known descriptor permissions are defined by `EchoRuntimeModulePermissionCatalog`. The core executable ABI permissions are:

- `content.register`
- `services.export`
- `services.import`
- `client.config`
- `assets.read`
- `data.persistence`

The descriptor-only catalog also recognizes pack, AdapterCore, UI, data, world, diagnostics, platform, and release-support capabilities used by the official module catalog, including foundation bootstrap permissions such as `registry:foundation`, `data:foundation`, and `launcher:dependency`, plus experience-specific state permissions such as `world.anomaly_state`.

Unknown descriptor permissions fail graph validation with `ECHO-STANDALONE-MODULE-PERMISSION-UNKNOWN` before classloader activation or entrypoint execution.

`runStandaloneAdapterCoreModuleCoverageSmoke` writes `reports/echo/standalone/runtime-adaptercore-permission-catalog.json` with the known/used/unknown permission counts from the scanned catalog; current evidence records 124 known permissions, 118 used permissions, and 0 unknown permissions.

Allowed module APIs:

- `requireService(type)` for ordinary runtime services
- `registerContent(kind, contentId)` with `content.register`
- `exportService(serviceId, service)` with `services.export`
- `importService(serviceId, type)` with `services.import`
- `publishConfig(key, value)` with `client.config`
- `registerAsset(assetId, relativePath)` with `assets.read`; asset paths must resolve inside the addon module root
- `writeSaveData(key, value)` and `readSaveData(key)` with `data.persistence`

Restricted internal services cannot be fetched through `requireService`:

- `EchoRuntimeServiceRegistry`
- `EchoRuntimeModuleContentActivationRegistry`
- `EchoRuntimeModuleServiceExportRegistry`
- `EchoRuntimeModuleDataRegistry`

## Classloader Isolation

ABI v1 module classloaders are created from descriptor `classPath` or `access.nativeClasspath` entries only after each entry resolves inside the inferred addon module root. For source-tree modules, descriptors under `src/main/resources/META-INF` infer the addon project root; packaged/fixture descriptors infer the parent folder that owns `META-INF`; jar/zip modules infer the archive file itself and default the classpath to that archive. Entries such as `..`, sibling roots, or absolute paths outside the module root fail before classloader activation. The smoke records `classpathEscapeLifecycle: FAILED` and `classpathEscapeExecuted: false`.

Declared dependencies do not widen filesystem access. After a dependency module has loaded, its classloader can satisfy classes requested by dependent modules. Consumers unload before providers so dependency-owned classes stay available throughout the consumer unload hook.

## Service Binding

The module runtime binds:

- `EchoRuntimeModuleRegistry`
- `EchoRuntimeModuleGraph`
- `EchoRuntimeFeatureGraph`
- `EchoRuntimeModuleSandboxPolicy`
- `EchoRuntimeModuleLifecycleBus`
- `EchoRuntimeModuleContentActivationRegistry`
- `EchoRuntimeModuleServiceExportRegistry`
- `EchoRuntimeModuleDataRegistry`
- `EchoNativeServiceRegistry` when a Native Platform entrypoint executes

`META-INF/echo.ai.json` remains supported by the workspace metadata scanner for safe edit zones, task routing, and automation hints, but it is not a standalone runtime module descriptor and is not loaded into the runtime module graph.

## Out Of Scope

The descriptor-only baseline still does not:

- create classloaders
- execute module code
- transform bytecode
- resolve Minecraft classes
- launch Minecraft or NeoForge
- mount assets or datapacks
- start gameplay systems
- migrate saves

The ABI v1 executable path still does not:

- transform bytecode
- resolve Minecraft or NeoForge classes
- launch Minecraft or NeoForge
- delegate addon boot to Minecraft, NeoForge, or Fabric native mod loaders
- mutate arbitrary filesystem paths
- execute Minecraft/NeoForge-bound implementation classes outside the standalone-safe native entrypoint slices
- replace the standalone game kernel, renderer, world simulation, save system, or packaged runtime smoke gates

ABI v1 is the locked contract for real standalone module execution. It is necessary for a playable Echo Native runtime, but it is not sufficient by itself to make the full game complete.
