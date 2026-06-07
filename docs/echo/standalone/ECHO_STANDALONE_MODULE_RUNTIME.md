# ECHO Standalone Module Runtime

The standalone module runtime now has two supported operating modes:

- Descriptor-only mode remains the safe catalog baseline. It discovers module descriptors, validates them, resolves dependencies, builds feature graphs, advances metadata lifecycle state, and binds module graph services without executing addon code.
- Native Loader ABI v1 is the opt-in executable path. It creates isolated module classloaders, executes raw runtime entrypoints or AdapterCore entrypoints, enforces module capability permissions, publishes lifecycle bus events, runs data reload hooks, contains load/reload failures, revokes module-scoped runtime state, and unloads without launching NeoForge.

The current executable evidence is:

- `reports/echo/standalone/native-loader-abi-v1-smoke.json`
- `reports/echo/standalone/runtime-native-loader-abi-v1.json`

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
- `requiresVersions`
- `optionalVersions`
- `access`

Executable ABI v1 fields are:

- `permissions`
- `classPath`
- `classpath`
- `entrypoint`
- `adapterCoreEntrypoint`
- `requiresVersions`
- `optionalVersions`

Required and optional dependency version maps use semver-style ranges such as `[1.0.0,2.0.0)` and `[2.0.0,)`. Required dependency version mismatches fail the dependent module. Present optional dependency version mismatches emit warnings and do not block load; missing optional dependencies are allowed.

This standalone checkout does not contain the full `echocore` datapack-example resource tree. Descriptor examples are maintained as executable smoke fixtures in `EchoRuntimeNativeLoaderAbiSmokeHarness`; the generated smoke report exports `descriptorSchemaId`, `descriptorSchemaSources`, `descriptorSchemaFields`, `descriptorExecutableAbiV1Fields`, `descriptorSchemaCoversExecutableAbiV1`, and `descriptorFieldTypes`.

## Runtime Pieces

- `EchoRuntimeModuleManager` orchestrates scanning, dependency resolution, trust validation, lifecycle publication, executable loading, and service binding.
- `EchoRuntimeModuleDescriptorScanner` discovers descriptor files from one or more roots.
- `EchoRuntimeModuleDescriptorParser` parses descriptor JSON through the structured runtime parser.
- `EchoRuntimeModuleDescriptorSchema` names the descriptor schema id, source files, field list, field types, required fields, and executable ABI v1 field set.
- `EchoRuntimeModuleDependencyResolver` resolves required and optional edges, validates dependency version ranges, fails required dependency cycles, and leaves optional dependency mismatches as warnings.
- `EchoRuntimeFeatureGraph` maps provided and consumed features.
- `EchoRuntimeModuleTrustPolicy` validates accepted trust levels.
- `EchoRuntimeModuleSandboxPolicy` keeps descriptor-only mode safe and exposes the opt-in ABI v1 execution policy.
- `EchoRuntimeModuleLoader` advances module lifecycle state and, in ABI v1 mode, creates a confined classloader from descriptor `classPath` entries inside the module root.
- `EchoRuntimeModuleRegistry` stores descriptors, lifecycle state, traces, notes, and runtime status.
- `EchoRuntimeModuleLifecycleBus` publishes ordered lifecycle events to observers without letting observer exceptions break module activation.
- `EchoRuntimeModuleServiceBinder` registers module graph services into `EchoRuntimeServiceRegistry`.
- `EchoRuntimeModuleEntrypoint` is the raw ABI v1 contract for load, data reload, and unload.
- `EchoRuntimeAdapterCoreEntrypoint` is the AdapterCore ABI v1 contract for activate, data reload, and deactivate.
- `EchoRuntimeModuleContext` exposes controlled module APIs: `requireService`, `registerContent`, `exportService`, and `importService`.
- `EchoRuntimeModuleContentActivationRegistry` records and deactivates permission-checked content activation by module.
- `EchoRuntimeModuleServiceExportRegistry` records and revokes permission-checked module-scoped services by module.

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

## ABI v1 Smoke Gate

`EchoRuntimeNativeLoaderAbiSmokeHarness` proves the executable gate without NeoForge:

```text
echoabi-field-generator -> load, content activation, service export, data reload, unload
echoabi-adaptercore-addon -> AdapterCore activate, content activation, service export, data reload, deactivate
echoabi-service-provider -> export service before dependent consumer load
echoabi-service-consumer -> import provider service, unload before provider
echoabi-optional-provider -> export optional service before optional consumer load
echoabi-optional-consumer -> import optional provider service, missing optional allowed, unload before optional provider
echoabi-optional-version-warning -> READY with non-blocking optional dependency version warning
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

## Permission Model

Module code does not receive the raw runtime service registry through `EchoRuntimeModuleContext`.

Known descriptor permissions are defined by `EchoRuntimeModulePermissionCatalog`:

- `content.register`
- `services.export`
- `services.import`

Unknown descriptor permissions fail graph validation with `ECHO-STANDALONE-MODULE-PERMISSION-UNKNOWN` before classloader activation or entrypoint execution.

Allowed module APIs:

- `requireService(type)` for ordinary runtime services
- `registerContent(kind, contentId)` with `content.register`
- `exportService(serviceId, service)` with `services.export`
- `importService(serviceId, type)` with `services.import`

Restricted internal services cannot be fetched through `requireService`:

- `EchoRuntimeServiceRegistry`
- `EchoRuntimeModuleContentActivationRegistry`
- `EchoRuntimeModuleServiceExportRegistry`

## Classloader Isolation

ABI v1 module classloaders are created from descriptor `classPath` entries only after each entry resolves inside the module root. Entries such as `..` fail before classloader activation. The smoke records `classpathEscapeLifecycle: FAILED` and `classpathEscapeExecuted: false`.

## Service Binding

The module runtime binds:

- `EchoRuntimeModuleRegistry`
- `EchoRuntimeModuleGraph`
- `EchoRuntimeFeatureGraph`
- `EchoRuntimeModuleSandboxPolicy`
- `EchoRuntimeModuleLifecycleBus`
- `EchoRuntimeModuleContentActivationRegistry`
- `EchoRuntimeModuleServiceExportRegistry`

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
- mutate arbitrary filesystem paths
- provide full addon compatibility
- replace the standalone game kernel, renderer, world simulation, save system, or packaged runtime smoke gates

ABI v1 is the locked contract for real standalone module execution. It is necessary for a playable Echo Native runtime, but it is not sufficient by itself to make the full game complete.
