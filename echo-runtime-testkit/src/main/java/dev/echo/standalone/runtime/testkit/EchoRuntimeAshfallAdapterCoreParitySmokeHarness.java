package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelSaveResult;
import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelSaveRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentBinding;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreContractLock;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayContracts;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayFeatureContract;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayFeatureKind;
import dev.echo.standalone.runtime.compat.EchoAshfallGameplayFeatureStatus;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class EchoRuntimeAshfallAdapterCoreParitySmokeHarness {
    private EchoRuntimeAshfallAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && isEchoRepoRoot(repoRoot),
                "Ashfall parity smoke requires the ECHO repo root");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        List<EchoAshfallGameplayFeatureContract> contracts = EchoAshfallGameplayContracts.ashfall(bridge);
        require(contracts.size() >= 32, "Ashfall parity matrix should cover beta gameplay feature rows");
        require(EchoAshfallGameplayContracts.parityReady(contracts),
                "Ashfall contracts should have no NeoForge-only, standalone-only, missing-runtime, or weak-evidence rows: "
                        + EchoAshfallGameplayContracts.blockingContracts(contracts));
        require(EchoAshfallGameplayContracts.missingRequiredAdapterCoreDomains(contracts).isEmpty(),
                "Ashfall contracts must cover every required AdapterCore beta domain: "
                        + EchoAshfallGameplayContracts.missingRequiredAdapterCoreDomains(contracts));
        require(EchoAshfallGameplayContracts.coveredRequiredAdapterCoreDomains(contracts).size()
                        == EchoAdapterCoreContractLock.requiredBetaDomains().size(),
                "Ashfall contracts should report full AdapterCore beta domain coverage");
        requireKinds(contracts);
        requireAdapterCoreRuntimeIdsRegistered(bridge, contracts);
        NeoForgeFeatureInventory inventory = scanNeoForgeFeatureInventory(repoRoot);
        require(inventory.totalScannedFeatures() >= 800,
                "Ashfall NeoForge feature inventory should scan the full source/data surface");
        requireNeoForgeBindingsExist(inventory, contracts);
        requireNoStandaloneOnlyGameplaySources(standaloneRoot);
        requireDataParity(repoRoot);
        requireAshfallRecipesLoadAsSharedData(repoRoot, inventory);
        requireAshfallLootLoadAsSharedData(repoRoot, inventory);
        requireAshfallMissionsLoadAsSharedData(repoRoot, inventory);
        requireAshfallStructuresLoadAsSharedData(repoRoot, inventory);
        requireAshfallSoundsLoadAsSharedData(repoRoot, inventory);
        requireArtifacts(standaloneRoot, inventory);

        EchoStandalonePlayableVoxelSaveResult save = new EchoStandalonePlayableVoxelSaveRuntime().run(
                bridge,
                Files.createTempDirectory("echo-ashfall-adaptercore-parity-save")
        );
        require(save.ready(), "AdapterCore-backed playable save should round-trip");
        require(save.contractBacked() && save.contractVersioned() && save.restoredContractState(),
                "Ashfall save/load state should be contract-backed, versioned, and restored through the contract");

        System.out.println("ashfall adaptercore parity smoke PASS rows="
                + contracts.size()
                + " saveVersion="
                + EchoAshfallGameplayContracts.CURRENT_SAVE_VERSION
                + " saveContract="
                + EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID
                + " scannedNeoForgeFeatures="
                + inventory.totalScannedFeatures()
                + " aliasesRegistered=true");
    }

    private static void requireKinds(List<EchoAshfallGameplayFeatureContract> contracts) {
        EnumSet<EchoAshfallGameplayFeatureKind> kinds = EnumSet.noneOf(EchoAshfallGameplayFeatureKind.class);
        for (EchoAshfallGameplayFeatureContract contract : contracts) {
            kinds.add(contract.kind());
        }
        for (EchoAshfallGameplayFeatureKind kind : EchoAshfallGameplayFeatureKind.values()) {
            require(kinds.contains(kind), "Ashfall parity matrix is missing feature kind " + kind.id());
        }
    }

    private static void requireAdapterCoreRuntimeIdsRegistered(
            EchoAdapterCoreStandaloneContentBridge bridge,
            List<EchoAshfallGameplayFeatureContract> contracts
    ) {
        for (EchoAshfallGameplayFeatureContract contract : contracts) {
            if (contract.status() != EchoAshfallGameplayFeatureStatus.ADAPTERCORE_BACKED) {
                continue;
            }
            EchoAdapterCoreContentBinding binding = bridge.registry()
                    .requireContentId(contract.contentId())
                    .binding();
            for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                require(bridge.registry().findRuntimeId(runtimeKind, binding.idFor(runtimeKind)).isPresent(),
                        contract.featureId() + " has unregistered " + runtimeKind.adapterId() + " id");
            }
            require(binding.contentId().equals(contract.contentId()),
                    contract.featureId() + " content id should be stable through AdapterCore");
            require(contract.standaloneAliasRegisteredThroughAdapterCore(),
                    contract.featureId() + " exposes a standalone runtime alias outside AdapterCore");
        }
    }

    private static void requireNoStandaloneOnlyGameplaySources(Path standaloneRoot) throws IOException {
        requireNoPlatformImplementationClassDependencies(standaloneRoot);

        Path missionState = standaloneRoot.resolve(
                "echo-runtime-app/src/main/java/dev/echo/standalone/runtime/app/EchoAshfallLiveMissionState.java");
        String missionText = Files.readString(missionState);
        require(missionText.contains("EchoAshfallGameplayContracts.LIVE_MISSION_STATE_CONTRACT_ID"),
                "live mission state must identify its AdapterCore contract");
        require(missionText.contains("adapterCoreContractId()"),
                "live mission state must expose its AdapterCore contract id");

        Path saveCodec = standaloneRoot.resolve(
                "echo-runtime-app/src/main/java/dev/echo/standalone/runtime/app/EchoStandalonePlayableVoxelSaveCodec.java");
        String saveText = Files.readString(saveCodec);
        require(saveText.contains("contractId=")
                        && saveText.contains("contractSchema=")
                        && saveText.contains("contractVersion="),
                "playable save codec must write contract id, schema, and version");
        require(saveText.contains("validateMissionContract"),
                "playable save codec must validate the AdapterCore mission save contract on restore");
    }

    private static void requireNoPlatformImplementationClassDependencies(Path standaloneRoot) throws IOException {
        Pattern forbiddenImport = Pattern.compile(
                "^\\s*import\\s+(net\\.minecraft|net\\.neoforged"
                        + "|com\\.knoxhack\\.echoashfallprotocol"
                        + "|com\\.knoxhack\\.echoworldcore"
                        + "|com\\.knoxhack\\.echomissioncore)\\.");
        Pattern forbiddenQualifiedType = Pattern.compile(
                "(net\\.minecraft|net\\.neoforged"
                        + "|com\\.knoxhack\\.echoashfallprotocol"
                        + "|com\\.knoxhack\\.echoworldcore"
                        + "|com\\.knoxhack\\.echomissioncore)\\.[A-Z_a-z]");
        StringBuilder violations = new StringBuilder();
        try (Stream<Path> stream = Files.walk(standaloneRoot)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".java")).toList()) {
                Path relative = standaloneRoot.relativize(path);
                String normalized = relative.toString().replace('\\', '/');
                if (!normalized.startsWith("echo-runtime-")
                        || !normalized.contains("/src/main/java/")
                        || normalized.startsWith("echo-runtime-testkit/")) {
                    continue;
                }
                int lineNumber = 0;
                for (String line : Files.readAllLines(path)) {
                    lineNumber++;
                    String code = stripStringLiterals(line);
                    if (forbiddenImport.matcher(code).find() || forbiddenQualifiedType.matcher(code).find()) {
                        violations.append(normalized)
                                .append(':')
                                .append(lineNumber)
                                .append(" -> ")
                                .append(line.strip())
                                .append('\n');
                    }
                }
            }
        }
        require(violations.isEmpty(),
                "standalone runtime production code must bridge through AdapterCore, not platform implementation classes:\n"
                        + violations);
    }

    private static String stripStringLiterals(String line) {
        StringBuilder result = new StringBuilder(line.length());
        boolean inString = false;
        boolean inCharacter = false;
        boolean escaping = false;
        for (int index = 0; index < line.length(); index++) {
            char ch = line.charAt(index);
            if (escaping) {
                escaping = false;
                result.append(' ');
                continue;
            }
            if ((inString || inCharacter) && ch == '\\') {
                escaping = true;
                result.append(' ');
                continue;
            }
            if (!inCharacter && ch == '"') {
                inString = !inString;
                result.append(' ');
                continue;
            }
            if (!inString && ch == '\'') {
                inCharacter = !inCharacter;
                result.append(' ');
                continue;
            }
            result.append(inString || inCharacter ? ' ' : ch);
        }
        return result.toString();
    }

    private static NeoForgeFeatureInventory scanNeoForgeFeatureInventory(Path repoRoot) throws IOException {
        Path ashfallJavaRoot = ashfallJavaRoot(repoRoot);
        Set<String> blocks = new HashSet<>(namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModBlocks.java"),
                        Pattern.compile("(?:registerCustomBlock|registerSimpleBlock|registerBlock|BLOCKS\\.register)\\(\"([a-z0-9_./-]+)\"")
                )
        ));
        blocks.addAll(namespaced(
                "echoterminal",
                scanJavaIds(
                        repoRoot.resolve("addons/echoterminal/src/main/java/com/knoxhack/echoterminal/registry/ModBlocks.java"),
                        Pattern.compile("(?:registerCustomBlock|registerSimpleBlock|registerBlock|BLOCKS\\.register)\\(\"([a-z0-9_./-]+)\"")
                )
        ));
        Set<String> blockItems = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModBlocks.java"),
                        Pattern.compile("BLOCK_ITEMS\\.registerSimpleBlockItem\\(\"([a-z0-9_./-]+)\"")
                )
        );

        Set<String> items = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModItems.java"),
                        Pattern.compile("(?:registerSimpleItem|registerSpawnEgg|register|ITEMS\\.registerSimpleItem|ITEMS\\.register)\\(\"([a-z0-9_./-]+)\"")
                )
        );
        Set<String> entities = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("entity/ModEntities.java"),
                        Pattern.compile("registerEntityType\\(\"([a-z0-9_./-]+)\"")
                )
        );
        Set<String> sounds = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModSounds.java"),
                        Pattern.compile("registerSound\\(\"([a-z0-9_./-]+)\"")
                )
        );
        Set<String> menus = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModMenuTypes.java"),
                        Pattern.compile("MENU_TYPES\\.register\\(\"([a-z0-9_./-]+)\"")
                )
        );
        Set<String> blockEntities = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModBlockEntities.java"),
                        Pattern.compile("BLOCK_ENTITIES\\.register\\(\"([a-z0-9_./-]+)\"")
                )
        );
        Set<String> effects = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModEffects.java"),
                        Pattern.compile("EFFECTS\\.register\\(\"([a-z0-9_./-]+)\"")
                )
        );
        Set<String> components = namespaced(
                "echoashfallprotocol",
                scanJavaIds(
                        ashfallJavaRoot.resolve("registry/ModDataComponents.java"),
                        Pattern.compile("DATA_COMPONENT_TYPES\\.register\\(\"([a-z0-9_./-]+)\"")
                )
        );

        Path dataRoot = ashfallDataRoot(repoRoot);
        Set<String> recipes = jsonResourceIds(dataRoot.resolve("recipe"), "echoashfallprotocol", "");
        Set<String> lootTables = jsonResourceIds(dataRoot.resolve("loot_table"), "echoashfallprotocol", "");
        Set<String> lootModifiers = jsonResourceIds(dataRoot.resolve("loot_modifiers"), "echoashfallprotocol", "");
        Set<String> missions = jsonResourceIds(dataRoot.resolve("missioncore/missions"), "echoashfallprotocol", "");
        Set<String> structures = jsonResourceIds(dataRoot.resolve("worldgen/structure"), "echoashfallprotocol", "");
        Set<String> worldRegions = jsonDeclaredIds(dataRoot.resolve("echoworldcore/world_regions"));
        Set<String> worldHazards = jsonDeclaredIds(dataRoot.resolve("echoworldcore/world_hazards"));

        return new NeoForgeFeatureInventory(
                blocks,
                blockItems,
                items,
                entities,
                sounds,
                menus,
                blockEntities,
                effects,
                components,
                recipes,
                lootTables,
                lootModifiers,
                missions,
                structures,
                worldRegions,
                worldHazards
        );
    }

    private static void requireNeoForgeBindingsExist(
            NeoForgeFeatureInventory inventory,
            List<EchoAshfallGameplayFeatureContract> contracts
    ) {
        for (EchoAshfallGameplayFeatureContract contract : contracts) {
            if (contract.status() != EchoAshfallGameplayFeatureStatus.ADAPTERCORE_BACKED
                    || !contract.neoForgeId().startsWith("echoashfallprotocol:")
                    && !contract.neoForgeId().startsWith("echoterminal:")) {
                continue;
            }
            String contentId = contract.contentId();
            String neoForgeId = contract.neoForgeId();
            if (contentId.contains(":block/")) {
                require(inventory.blocks().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge block " + neoForgeId);
            } else if (contentId.contains(":item/")) {
                require(inventory.items().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge item " + neoForgeId);
            } else if (contentId.contains(":entity/")) {
                require(inventory.entities().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge entity " + neoForgeId);
            } else if (contentId.contains(":recipe/")) {
                require(inventory.recipes().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge recipe " + neoForgeId);
            } else if (contentId.contains(":loot/")) {
                require(inventory.lootTables().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge loot table " + neoForgeId);
            } else if (contentId.contains(":sound/")) {
                require(inventory.sounds().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge sound " + neoForgeId);
            } else if (contentId.startsWith("echoashfallprotocol:ui/")) {
                require(inventory.menus().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge menu " + neoForgeId);
            } else if (contentId.contains(":mission/")) {
                require(inventory.missions().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge mission " + neoForgeId);
            } else if (contentId.contains(":component/")) {
                require(inventory.components().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge data component " + neoForgeId);
            } else if (contentId.contains(":effect/")) {
                require(inventory.effects().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge effect " + neoForgeId);
            } else if (contentId.contains(":structure/")) {
                require(inventory.structures().contains(neoForgeId),
                        contract.featureId() + " points at missing NeoForge structure " + neoForgeId);
            } else if (contentId.contains(":world_region/")) {
                require(inventory.worldRegions().contains(neoForgeId),
                        contract.featureId() + " points at missing WorldCore region " + neoForgeId);
            } else if (contentId.contains(":world_hazard/")) {
                require(inventory.worldHazards().contains(neoForgeId),
                        contract.featureId() + " points at missing WorldCore hazard " + neoForgeId);
            }
        }
    }

    private static Set<String> scanJavaIds(Path file, Pattern pattern) throws IOException {
        String text = Files.readString(file);
        Matcher matcher = pattern.matcher(text);
        Set<String> ids = new HashSet<>();
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        return Set.copyOf(ids);
    }

    private static Set<String> namespaced(String namespace, Set<String> paths) {
        Set<String> ids = new HashSet<>();
        for (String path : paths) {
            ids.add(namespace + ":" + path);
        }
        return Set.copyOf(ids);
    }

    private static Set<String> jsonResourceIds(Path root, String namespace, String prefix) throws IOException {
        if (!Files.isDirectory(root)) {
            return Set.of();
        }
        Set<String> ids = new HashSet<>();
        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> {
                        Path relative = root.relativize(path);
                        String resourcePath = relative.toString()
                                .replace('\\', '/')
                                .replaceFirst("\\.json$", "");
                        ids.add(namespace + ":" + prefix + resourcePath);
                    });
        }
        return Set.copyOf(ids);
    }

    private static Set<String> jsonDeclaredIds(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return Set.of();
        }
        Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*\"([a-z0-9_.-]+:[a-z0-9_./-]+)\"");
        Set<String> ids = new HashSet<>();
        try (Stream<Path> stream = Files.walk(root)) {
            for (Path path : stream.filter(file -> file.getFileName().toString().endsWith(".json")).toList()) {
                Matcher matcher = idPattern.matcher(Files.readString(path));
                if (matcher.find()) {
                    ids.add(matcher.group(1));
                }
            }
        }
        return Set.copyOf(ids);
    }

    private static boolean isEchoRepoRoot(Path repoRoot) {
        return Files.isDirectory(ashfallResourcesRoot(repoRoot))
                && Files.isDirectory(ashfallJavaRoot(repoRoot))
                && Files.isRegularFile(repoRoot.resolve("addons/echoworldcore/src/main/java/com/knoxhack/echoworldcore/registry/WorldCoreBuiltins.java"))
                && Files.isDirectory(repoRoot.resolve("echo-standalone-runtime"));
    }

    private static Path ashfallJavaRoot(Path repoRoot) {
        return repoRoot.resolve("addons/echoashfallprotocol/src/main/java/com/knoxhack/echoashfallprotocol");
    }

    private static Path ashfallResourcesRoot(Path repoRoot) {
        return repoRoot.resolve("addons/echoashfallprotocol/src/main/resources");
    }

    private static Path ashfallDataRoot(Path repoRoot) {
        return ashfallResourcesRoot(repoRoot).resolve("data/echoashfallprotocol");
    }

    private static void requireDataParity(Path repoRoot) throws IOException {
        Path dataRoot = ashfallDataRoot(repoRoot);
        Path hazard = dataRoot.resolve("echoworldcore/world_hazards/hazard/toxic_ash.json");
        require(Files.isRegularFile(hazard), "Ashfall toxic ash hazard data must live under echoashfallprotocol");
        String hazardText = Files.readString(hazard);
        require(hazardText.contains("\"id\": \"echoashfallprotocol:hazard/toxic_ash\""),
                "Ashfall toxic ash hazard should use the stable AdapterCore/WorldCore id");

        Path region = dataRoot.resolve("echoworldcore/world_regions/ashfall_toxic_swamp.json");
        require(Files.readString(region).contains("echoashfallprotocol:hazard/toxic_ash"),
                "Ashfall toxic swamp region should reference the Ashfall-owned toxic ash hazard");

        Path worldCoreBuiltins = repoRoot.resolve(
                "addons/echoworldcore/src/main/java/com/knoxhack/echoworldcore/registry/WorldCoreBuiltins.java");
        require(!Files.readString(worldCoreBuiltins).contains("echoashfallprotocol"),
                "Ashfall-specific hazards should not be hardcoded in WorldCore builtins");
    }

    private static void requireAshfallRecipesLoadAsSharedData(
            Path repoRoot,
            NeoForgeFeatureInventory inventory
    ) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult assets = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "data", ashfallResourcesRoot(repoRoot), "ashfall-neoforge-resources")
        )).load(services, List.of());
        EchoDataRuntimeResult data = new EchoDataRuntime().load(services, assets);
        for (String recipeId : inventory.recipes()) {
            require(data.recipes().find(recipeId).isPresent(),
                    "Ashfall NeoForge recipe should load through shared AdapterCore data runtime: " + recipeId);
        }
        require(data.recipes().find("echoashfallprotocol:power_cell").orElseThrow()
                        .result().equals("echoashfallprotocol:power_cell"),
                "Ashfall shaped power-cell recipe should expose its NeoForge result through shared data");
        require(data.recipes().find("echoashfallprotocol:power_cell").orElseThrow()
                        .ingredients().contains("echoashfallprotocol:energy_cell"),
                "Ashfall shaped power-cell recipe should expose key ingredients through shared data");
    }

    private static void requireAshfallLootLoadAsSharedData(
            Path repoRoot,
            NeoForgeFeatureInventory inventory
    ) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult assets = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "data", ashfallResourcesRoot(repoRoot), "ashfall-neoforge-resources")
        )).load(services, List.of());
        EchoDataRuntimeResult data = new EchoDataRuntime().load(services, assets);
        for (String lootTableId : inventory.lootTables()) {
            require(data.loot().find(lootTableId).isPresent(),
                    "Ashfall NeoForge loot table should load through shared AdapterCore data runtime: " + lootTableId);
        }
        for (String lootModifierId : inventory.lootModifiers()) {
            require(data.loot().find(lootModifierId).isPresent(),
                    "Ashfall NeoForge loot modifier should load through shared AdapterCore data runtime: " + lootModifierId);
        }
        require(data.loot().find("echoashfallprotocol:blocks/echo_cache").orElseThrow()
                        .entries().contains("echoashfallprotocol:echo_cache"),
                "Ashfall block loot should expose pooled item entries through shared data");
        require(data.loot().find("echoashfallprotocol:wiki_manual_radio_tower_cache").orElseThrow()
                        .entries().contains("echoashfallprotocol:chests/radio_tower_cache"),
                "Ashfall loot modifier should expose its target loot table through shared data");
    }

    private static void requireAshfallMissionsLoadAsSharedData(
            Path repoRoot,
            NeoForgeFeatureInventory inventory
    ) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult assets = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "data", ashfallResourcesRoot(repoRoot), "ashfall-neoforge-resources")
        )).load(services, List.of());
        EchoDataRuntimeResult data = new EchoDataRuntime().load(services, assets);
        for (String missionId : inventory.missions()) {
            require(data.missions().find(missionId).isPresent(),
                    "Ashfall MissionCore mission should load through shared AdapterCore data runtime: " + missionId);
        }
        require(data.missions().find("echoashfallprotocol:build_battery_bank").orElseThrow()
                        .objectives().contains("echoashfallprotocol:build_battery_bank/place_battery_bank"),
                "Ashfall MissionCore mission should expose objective ids through shared data");
        require(data.missions().find("echoashfallprotocol:build_battery_bank").orElseThrow()
                        .references().contains("echoashfallprotocol:battery_bank"),
                "Ashfall MissionCore mission should expose block/item targets through shared data");
        require(data.missions().find("echoashfallprotocol:build_battery_bank").orElseThrow()
                        .references().contains("echoashfallprotocol:energy_cell"),
                "Ashfall MissionCore mission should expose item requirements through shared data");
    }

    private static void requireAshfallStructuresLoadAsSharedData(
            Path repoRoot,
            NeoForgeFeatureInventory inventory
    ) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult assets = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "data", ashfallResourcesRoot(repoRoot), "ashfall-neoforge-resources")
        )).load(services, List.of());
        EchoDataRuntimeResult data = new EchoDataRuntime().load(services, assets);
        for (String structureId : inventory.structures()) {
            require(data.worldgenStructures().find(structureId).isPresent(),
                    "Ashfall worldgen structure should load through shared AdapterCore data runtime: " + structureId);
        }
        require(data.worldgenStructures().find("echoashfallprotocol:bio_lab").orElseThrow()
                        .references().contains("#echoashfallprotocol:has_structure/bio_lab"),
                "Ashfall structure should expose biome tag references through shared data");
        require(data.worldgenStructures().find("echoashfallprotocol:bio_lab").orElseThrow()
                        .references().contains("echoashfallprotocol:bio_lab"),
                "Ashfall structure should expose template pool references through shared data");
    }

    private static void requireAshfallSoundsLoadAsSharedData(
            Path repoRoot,
            NeoForgeFeatureInventory inventory
    ) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult assets = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "data", ashfallResourcesRoot(repoRoot), "ashfall-neoforge-resources")
        )).load(services, List.of());
        EchoDataRuntimeResult data = new EchoDataRuntime().load(services, assets);
        for (String soundId : inventory.sounds()) {
            require(data.sounds().find(soundId).isPresent(),
                    "Ashfall sound event should load through shared AdapterCore data runtime: " + soundId);
        }
        require(data.sounds().find("echoashfallprotocol:ui.echo_message").orElseThrow()
                        .subtitle().equals("subtitles.EchoAshfallProtocol.echo.message"),
                "Ashfall sound catalog should expose subtitles through shared data");
        require(data.sounds().find("echoashfallprotocol:event.ash_storm").orElseThrow()
                        .sourceLogicalId().equals("echoashfallprotocol:sounds.json"),
                "Ashfall sound catalog should be loaded from assets/echoashfallprotocol/sounds.json");
    }

    private static void requireArtifacts(
            Path standaloneRoot,
            NeoForgeFeatureInventory inventory
    ) throws IOException {
        Path matrix = standaloneRoot.resolve("docs/echo/standalone/ASHFALL_PARITY_MATRIX.md");
        require(Files.isRegularFile(matrix),
                "ASHFALL_PARITY_MATRIX.md is required");
        String matrixText = Files.readString(matrix);
        require(matrixText.contains("<!-- ASHFALL_FULL_INVENTORY:START -->")
                        && matrixText.contains("<!-- ASHFALL_FULL_INVENTORY:END -->")
                        && matrixText.contains("## Full NeoForge Inventory Rows"),
                "ASHFALL_PARITY_MATRIX.md must include the generated full NeoForge inventory appendix");
        Path noDuplicateAudit = standaloneRoot.resolve(
                "reports/echo/standalone/ashfall-no-duplicate-gameplay-audit.json");
        require(Files.isRegularFile(noDuplicateAudit),
                "Ashfall no-duplicate-gameplay audit report is required");
        String noDuplicateAuditText = Files.readString(noDuplicateAudit);
        require(noDuplicateAuditText.contains("\"status\": \"PASS\"")
                        && noDuplicateAuditText.contains("\"forbiddenDependencyViolations\": 0")
                        && noDuplicateAuditText.contains("\"standaloneOnlyGameplaySystems\": 0")
                        && noDuplicateAuditText.contains("requireNoPlatformImplementationClassDependencies"),
                "Ashfall no-duplicate-gameplay audit must pass and cite the source dependency audit");
        Path report = standaloneRoot.resolve("reports/echo/standalone/ashfall-parity-matrix.json");
        require(Files.isRegularFile(report), "ashfall parity matrix report is required");
        require(Files.readString(report).contains("\"status\": \"PASS\""),
                "ashfall parity matrix report must pass");
        Path inventoryReport = standaloneRoot.resolve("reports/echo/standalone/ashfall-feature-inventory.json");
        require(Files.isRegularFile(inventoryReport), "ashfall feature inventory report is required");
        String inventoryText = Files.readString(inventoryReport);
        require(inventoryText.contains("\"rowCoverage\"")
                        && inventoryText.contains("\"complete\": true")
                        && inventoryText.contains("\"status\": \"INVENTORY_GAPS\""),
                "ashfall feature inventory should record row-complete scanned NeoForge surface and remaining full-game gaps");
        int scannedFeatureCount = inventory.totalScannedFeatures();
        requireJsonInt(inventoryText, "totalRows", scannedFeatureCount);
        requireJsonInt(inventoryText, "scannedFeatures", scannedFeatureCount);
        int adapterBackedRows = extractJsonInt(inventoryText, "ADAPTERCORE_BACKED");
        int neoForgeOnlyRows = extractJsonInt(inventoryText, "NEOFORGE_ONLY");
        int standaloneOnlyRows = extractJsonInt(inventoryText, "STANDALONE_ONLY");
        int dataDrivenRows = extractJsonInt(inventoryText, "DATA_DRIVEN_SHARED");
        int missingRuntimeRows = extractJsonInt(inventoryText, "MISSING_RUNTIME");
        require(countOccurrences(inventoryText, "\"status\": \"ADAPTERCORE_BACKED\"") == adapterBackedRows,
                "ashfall feature inventory AdapterCore-backed count must match feature rows");
        require(countOccurrences(inventoryText, "\"status\": \"NEOFORGE_ONLY\"") == neoForgeOnlyRows,
                "ashfall feature inventory NeoForge-only count must match feature rows");
        require(countOccurrences(inventoryText, "\"status\": \"STANDALONE_ONLY\"") == standaloneOnlyRows,
                "ashfall feature inventory standalone-only count must match feature rows");
        require(countOccurrences(inventoryText, "\"status\": \"DATA_DRIVEN_SHARED\"") == dataDrivenRows,
                "ashfall feature inventory shared-data count must match feature rows");
        require(countOccurrences(inventoryText, "\"status\": \"MISSING_RUNTIME\"") == missingRuntimeRows,
                "ashfall feature inventory missing-runtime count must match feature rows");
        require(dataDrivenRows > 0,
                "ashfall feature inventory should classify verified shared datapack rows as DATA_DRIVEN_SHARED");
        require(matrixText.contains("Current scan records " + scannedFeatureCount + " row-level NeoForge features")
                        && matrixText.contains("`" + neoForgeOnlyRows + "` rows remain `NEOFORGE_ONLY`")
                        && matrixText.contains("`" + dataDrivenRows + "` rows are documented shared-data definitions"),
                "ASHFALL_PARITY_MATRIX.md summary must mirror ashfall feature inventory counts");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:cryogenic_ruins", "DATA_DRIVEN_SHARED");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:nexus_scar", "DATA_DRIVEN_SHARED");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:radiation_zone", "DATA_DRIVEN_SHARED");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:radwarden_outpost", "DATA_DRIVEN_SHARED");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:ruined_cityscape", "DATA_DRIVEN_SHARED");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:showcase_corridor", "DATA_DRIVEN_SHARED");
        requireInventoryRowStatus(inventoryText, "worldRegions",
                "echoashfallprotocol:toxic_swamp", "DATA_DRIVEN_SHARED");
        requireInventoryRows(inventoryText, "blocks", inventory.blocks());
        requireInventoryRows(inventoryText, "blockItems", inventory.blockItems());
        requireInventoryRows(inventoryText, "items", inventory.items());
        requireInventoryRows(inventoryText, "entities", inventory.entities());
        requireInventoryRows(inventoryText, "sounds", inventory.sounds());
        requireInventoryRows(inventoryText, "menus", inventory.menus());
        requireInventoryRows(inventoryText, "blockEntities", inventory.blockEntities());
        requireInventoryRows(inventoryText, "effects", inventory.effects());
        requireInventoryRows(inventoryText, "components", inventory.components());
        requireInventoryRows(inventoryText, "recipes", inventory.recipes());
        requireInventoryRows(inventoryText, "lootTables", inventory.lootTables());
        requireInventoryRows(inventoryText, "lootModifiers", inventory.lootModifiers());
        requireInventoryRows(inventoryText, "missions", inventory.missions());
        requireInventoryRows(inventoryText, "structures", inventory.structures());
        requireInventoryRows(inventoryText, "worldRegions", inventory.worldRegions());
        requireInventoryRows(inventoryText, "worldHazards", inventory.worldHazards());
        requireMatrixRows(matrixText, "blocks", inventory.blocks());
        requireMatrixRows(matrixText, "blockItems", inventory.blockItems());
        requireMatrixRows(matrixText, "items", inventory.items());
        requireMatrixRows(matrixText, "entities", inventory.entities());
        requireMatrixRows(matrixText, "sounds", inventory.sounds());
        requireMatrixRows(matrixText, "menus", inventory.menus());
        requireMatrixRows(matrixText, "blockEntities", inventory.blockEntities());
        requireMatrixRows(matrixText, "effects", inventory.effects());
        requireMatrixRows(matrixText, "components", inventory.components());
        requireMatrixRows(matrixText, "recipes", inventory.recipes());
        requireMatrixRows(matrixText, "lootTables", inventory.lootTables());
        requireMatrixRows(matrixText, "lootModifiers", inventory.lootModifiers());
        requireMatrixRows(matrixText, "missions", inventory.missions());
        requireMatrixRows(matrixText, "structures", inventory.structures());
        requireMatrixRows(matrixText, "worldRegions", inventory.worldRegions());
        requireMatrixRows(matrixText, "worldHazards", inventory.worldHazards());
    }

    private static int extractJsonInt(String text, String key) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)").matcher(text);
        require(matcher.find(), "ashfall feature inventory is missing numeric field " + key);
        return Integer.parseInt(matcher.group(1));
    }

    private static void requireJsonInt(String text, String key, int expected) {
        int actual = extractJsonInt(text, key);
        require(actual == expected,
                "ashfall feature inventory " + key + " expected " + expected + " but was " + actual);
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int index = text.indexOf(needle);
        while (index >= 0) {
            count++;
            index = text.indexOf(needle, index + needle.length());
        }
        return count;
    }

    private static void requireInventoryRowStatus(
            String inventoryText,
            String category,
            String id,
            String status
    ) {
        String rowKey = "\"featureKey\": \"" + category + ":" + id + "\"";
        int rowStart = inventoryText.indexOf(rowKey);
        require(rowStart >= 0, "ashfall feature inventory is missing row " + category + ":" + id);
        int nextRow = inventoryText.indexOf("\n    {", rowStart + rowKey.length());
        String rowText = nextRow >= 0
                ? inventoryText.substring(rowStart, nextRow)
                : inventoryText.substring(rowStart);
        require(rowText.contains("\"status\": \"" + status + "\""),
                "ashfall feature inventory row " + category + ":" + id + " should be " + status);
    }

    private static void requireInventoryRows(
            String inventoryText,
            String category,
            Set<String> ids
    ) {
        for (String id : ids) {
            String featureKey = "\"featureKey\": \"" + category + ":" + id + "\"";
            require(inventoryText.contains(featureKey),
                    "ashfall feature inventory is missing row " + category + ":" + id);
        }
    }

    private static void requireMatrixRows(
            String matrixText,
            String category,
            Set<String> ids
    ) {
        for (String id : ids) {
            String featureKey = "`" + category + ":" + id + "`";
            require(matrixText.contains(featureKey),
                    "ASHFALL_PARITY_MATRIX.md is missing row " + category + ":" + id);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record NeoForgeFeatureInventory(
            Set<String> blocks,
            Set<String> blockItems,
            Set<String> items,
            Set<String> entities,
            Set<String> sounds,
            Set<String> menus,
            Set<String> blockEntities,
            Set<String> effects,
            Set<String> components,
            Set<String> recipes,
            Set<String> lootTables,
            Set<String> lootModifiers,
            Set<String> missions,
            Set<String> structures,
            Set<String> worldRegions,
            Set<String> worldHazards
    ) {
        private int totalScannedFeatures() {
            return blocks.size()
                    + blockItems.size()
                    + items.size()
                    + entities.size()
                    + sounds.size()
                    + menus.size()
                    + blockEntities.size()
                    + effects.size()
                    + components.size()
                    + recipes.size()
                    + lootTables.size()
                    + lootModifiers.size()
                    + missions.size()
                    + structures.size()
                    + worldRegions.size()
                    + worldHazards.size();
        }
    }
}
