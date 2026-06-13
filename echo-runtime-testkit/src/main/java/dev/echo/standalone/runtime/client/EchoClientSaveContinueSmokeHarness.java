package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientSaveContinueSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports/echo/standalone/client-save-continue.json");

    private EchoClientSaveContinueSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-save-continue-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices liveClient = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        require(!liveClient.hasContinuableSession(),
                "Fresh client should not report Continue before any memory or disk save exists");

        liveClient.startNewWorld("continue-flow");
        String savedSlotId = liveClient.worldSession().slotId();
        require(!savedSlotId.isBlank(), "New world should use a concrete save slot id");
        RuntimeSaveContentIds runtimeIds = importPersistentRuntimeContent(liveClient);
        EchoClientGameSession liveSession = liveClient.session();
        require(liveSession != null, "Native runtime save smoke requires an active live session");
        EchoVoxelBlock persistentBlock = liveSession.bridge().registry().requireLiveVoxelBlock(runtimeIds.blockId());
        var persistentEntry = liveSession.bridge().registry()
                .requireContentId("echoruntimehost:block/persistent_runtime_glass");
        var persistentItemEntry = liveSession.bridge().registry()
                .requireContentId("echoruntimehost:item/persistent_runtime_alloy");
        require(persistentBlock.atlasKey().equals("echoruntimehost/block/persistent_runtime_glass"),
                "Native texture metadata should normalize into the standalone atlas key");
        require(persistentEntry.assetReferences().blockstateId()
                        .equals("echoruntimehost:blockstates/persistent_runtime_glass"),
                "Native blockstate asset metadata should be retained by the runtime registry");
        require(persistentEntry.assetReferences().modelId()
                        .equals("echoruntimehost:models/block/persistent_runtime_glass"),
                "Native model asset metadata should be retained by the runtime registry");
        require(persistentEntry.assetReferences().textureId()
                        .equals("echoruntimehost:textures/block/persistent_runtime_glass"),
                "Native texture asset metadata should be retained by the runtime registry");
        require(persistentEntry.assetReferences().langKey()
                        .equals("block.echoruntimehost.persistent_runtime_glass"),
                "Native lang key metadata should be retained by the runtime registry");
        require(persistentItemEntry.assetReferences().modelId()
                        .equals("echoruntimehost:models/item/persistent_runtime_alloy")
                        && persistentItemEntry.assetReferences().textureId()
                        .equals("echoruntimehost:textures/item/persistent_runtime_alloy"),
                "Native item model and texture metadata should be retained by the runtime registry");
        require(persistentEntry.registryMetadata().tags().contains("transparent")
                        && "false".equals(persistentEntry.registryMetadata().defaultState().get("opaque"))
                        && persistentEntry.registryMetadata().behaviorHooks().contains("open_registered_screen")
                        && persistentEntry.registryMetadata().saveCodecVersion().equals("echo.block.v1")
                        && "adaptercore-native".equals(
                        persistentEntry.registryMetadata().compatibilityMetadata().get("source")),
                "Native block registry metadata should be retained by the runtime registry");
        require(persistentItemEntry.registryMetadata().tags().contains("crafting_component")
                        && persistentItemEntry.registryMetadata().saveCodecVersion().equals("echo.item.v1"),
                "Native item registry metadata should be retained by the runtime registry");
        int savedBlockX = (int) Math.floor(liveSession.world().spawnX()) + 2;
        int savedBlockY = 4;
        int savedBlockZ = (int) Math.floor(liveSession.world().spawnZ()) + 2;
        EchoVoxelBlockState persistentBlockState = liveSession.defaultBlockStateFor(persistentBlock).ticked();
        require(persistentBlockState.property("facing").orElse("").equals("north")
                        && persistentBlockState.property("powered").orElse("").equals("false")
                        && persistentBlockState.property("opaque").orElse("").equals("false")
                        && persistentBlockState.tickVersion() == 1L,
                "Native runtime block default state should materialize from registry metadata before saving");
        require(liveSession.world().setBlockStateAt(savedBlockX, savedBlockY, savedBlockZ, persistentBlockState),
                "Native runtime save smoke should place the imported block before saving");
        require(liveSession.world().blockStateAt(savedBlockX, savedBlockY, savedBlockZ)
                        .property("facing").orElse("").equals("north"),
                "Native runtime default block state should reach the live voxel world before saving");
        require(liveSession.worldInteractionRouteFor(persistentBlock).targetId().equals(runtimeIds.structureScreenId()),
                "Native runtime structure row should route before saving");
        requireMachinePlacementAndBreakLifecycle();
        EchoVoxelBlock microGeneratorBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID);
        EchoVoxelBlock powerCableBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID);
        EchoVoxelBlock loadDistributorBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.LOAD_DISTRIBUTOR_BLOCK_ID);
        EchoVoxelBlock batteryBankBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID);
        EchoVoxelBlock scrapPressBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID);
        EchoVoxelBlock itemPipeBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID);
        EchoVoxelBlock oreGrinderBlock = liveSession.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID);
        require(liveSession.world().setBlockStateAt(9, 5, 9, EchoVoxelBlockState.AIR)
                        && liveSession.world().setBlockStateAt(10, 5, 9, EchoVoxelBlockState.AIR)
                        && liveSession.world().setBlockStateAt(8, 5, 10, liveSession.defaultBlockStateFor(itemPipeBlock))
                        && liveSession.world().setBlockStateAt(8, 5, 11, liveSession.defaultBlockStateFor(oreGrinderBlock))
                        && liveSession.world().setBlockStateAt(20, 5, 9, liveSession.defaultBlockStateFor(microGeneratorBlock))
                        && liveSession.world().setBlockStateAt(21, 5, 9, liveSession.defaultBlockStateFor(powerCableBlock))
                        && liveSession.world().setBlockStateAt(22, 5, 9, liveSession.defaultBlockStateFor(loadDistributorBlock))
                        && liveSession.world().setBlockStateAt(23, 5, 9, liveSession.defaultBlockStateFor(batteryBankBlock))
                        && liveSession.world().setBlockStateAt(24, 5, 9, liveSession.defaultBlockStateFor(scrapPressBlock))
                        && liveSession.world().setBlockStateAt(25, 5, 9, liveSession.defaultBlockStateFor(itemPipeBlock))
                        && liveSession.world().setBlockStateAt(26, 5, 9, liveSession.defaultBlockStateFor(oreGrinderBlock)),
                "Machine smoke should move logistics blocks and add a second complete machine network before ticking");
        require(liveSession.reconcileMachineBlockEntitiesFromWorld() == 14,
                "Machine runtime should reconcile placed block entities from the edited voxel layout");
        int processedMachineTicks = liveSession.tickMachines(40);
        int materializedMachineBlocks = liveSession.materializeMachineBlockEntities();
        EchoClientMachineStateSnapshot savedMachineState = liveSession.machineStateSnapshot();
        int savedBatteryEnergy = powerNodeEnergy(savedMachineState, "battery_bank");
        require(processedMachineTicks == 80
                        && savedMachineState.recipeProgressTicks() == 80
                        && savedMachineState.powerConsumed() == 80
                        && savedMachineState.oreGrinderInputCount() == 2
                        && savedBatteryEnergy == 560
                        && materializedMachineBlocks == savedMachineState.blockEntities().size()
                        && machineBlockEntity(savedMachineState, "scrap_press").x() == 8
                        && machineBlockEntity(savedMachineState, "scrap_press").chunkX() == 0
                        && machineBlockEntity(savedMachineState, "item_pipe").x() == 8
                        && machineBlockEntity(savedMachineState, "item_pipe").z() == 10
                        && machineBlockEntity(savedMachineState, "ore_grinder").x() == 8
                        && machineBlockEntity(savedMachineState, "ore_grinder").z() == 11
                        && machineBlockEntity(savedMachineState, "scrap_press@24,5,9").state()
                        .getOrDefault("recipeProgressTicks", "").equals("40")
                        && machineBlockEntity(savedMachineState, "scrap_press@24,5,9").state()
                        .getOrDefault("powerConsumed", "").equals("40")
                        && machineBlockEntity(savedMachineState, "ore_grinder@26,5,9").state()
                        .getOrDefault("inputCount", "").equals("1")
                        && powerNodeEnergy(savedMachineState, "battery_bank@23,5,9") == 280
                        && savedMachineState.diagnostics().stream()
                        .anyMatch(row -> row.contains("multi-network machine graphs connected=2"))
                        && liveSession.world().blockAt(8, 5, 9).id()
                        .equals(EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID)
                        && liveSession.world().blockStateAt(24, 5, 9).property("blockEntityId").orElse("")
                        .equals("scrap_press@24,5,9")
                        && liveSession.world().blockAt(8, 5, 11).id()
                        .equals(EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID),
                "Machine runtime should advance independent power graphs and editable logistics layout before saving");
        liveClient.captureMemorySave(framebufferThumbnailCapture());
        Path thumbnailPath = saveRoot.resolve("slots")
                .resolve(savedSlotId)
                .resolve("data")
                .resolve(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_PATH);
        long capturedThumbnailBytes = Files.isRegularFile(thumbnailPath) ? Files.size(thumbnailPath) : 0L;
        require(capturedThumbnailBytes > 100,
                "Manual save should persist a captured save-slot thumbnail PNG");
        BufferedImage thumbnailImage = ImageIO.read(thumbnailPath.toFile());
        require(thumbnailImage != null && thumbnailImage.getWidth() == 160 && thumbnailImage.getHeight() == 90,
                "Manual save thumbnail should be a readable 160x90 PNG");
        String thumbnailManifestText = Files.readString(saveRoot.resolve("slots")
                .resolve(savedSlotId)
                .resolve("manifest.json"));
        require(thumbnailManifestText.contains("\"clientThumbnailCodec\"")
                        && thumbnailManifestText.contains(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_CODEC)
                        && thumbnailManifestText.contains(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_PATH)
                        && thumbnailManifestText.contains(
                                EchoClientSaveSlotThumbnailGenerator.FRAMEBUFFER_THUMBNAIL_SOURCE),
                "Manual save manifest should expose captured framebuffer thumbnail codec, path, and source metadata");
        EchoClientSaveSlotSummary savedSlotSummary = liveClient.saveSlotSummaries()
                .stream()
                .filter(slot -> slot.slotId().equals(savedSlotId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Manual save should be listed after thumbnail capture"));
        require(savedSlotSummary.thumbnailCaptured()
                        && savedSlotSummary.thumbnailPath().equals(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_PATH)
                        && Path.of(savedSlotSummary.thumbnailResolvedPath()).toAbsolutePath().normalize()
                        .equals(thumbnailPath.toAbsolutePath().normalize())
                        && savedSlotSummary.thumbnailSource()
                        .equals(EchoClientSaveSlotThumbnailGenerator.FRAMEBUFFER_THUMBNAIL_SOURCE)
                        && savedSlotSummary.thumbnailWidth() == 160
                        && savedSlotSummary.thumbnailHeight() == 90,
                "Save slot summary should mark fresh thumbnails as captured OpenGL framebuffer icons");
        EchoClientSaveSlotThumbnailSnapshot savedThumbnailSnapshot =
                EchoClientSaveSlotThumbnailSnapshot.from(savedSlotSummary);
        require(savedThumbnailSnapshot.captured()
                        && savedThumbnailSnapshot.width() == 160
                        && savedThumbnailSnapshot.height() == 90
                        && savedThumbnailSnapshot.resolvedPath().equals(savedSlotSummary.thumbnailResolvedPath())
                        && EchoClientUiRenderer.usesCapturedThumbnailTexture(savedThumbnailSnapshot),
                "Fresh save-slot thumbnail snapshot should be eligible for World Select PNG texture rendering");
        Files.writeString(thumbnailPath, "corrupt thumbnail");
        EchoClientSaveSlotSummary corruptThumbnailSummary = liveClient.saveSlotSummaries()
                .stream()
                .filter(slot -> slot.slotId().equals(savedSlotId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Corrupt thumbnail save should remain listed"));
        require(!corruptThumbnailSummary.thumbnailCaptured()
                        && corruptThumbnailSummary.thumbnailWidth() == 0
                        && corruptThumbnailSummary.thumbnailHeight() == 0
                        && corruptThumbnailSummary.detail().contains("thumbnail deterministic"),
                "Corrupt save-slot thumbnail PNG should fall back to deterministic World Select preview state");
        require(!EchoClientUiRenderer.usesCapturedThumbnailTexture(
                        EchoClientSaveSlotThumbnailSnapshot.from(corruptThumbnailSummary)),
                "Corrupt save-slot thumbnail snapshot should not be eligible for PNG texture rendering");
        Path blockEntitiesPath = saveRoot.resolve("slots")
                .resolve(savedSlotId)
                .resolve("data")
                .resolve(EchoClientGameplaySaveCodec.BLOCK_ENTITIES_PATH);
        Path legacyMachinesPath = saveRoot.resolve("slots")
                .resolve(savedSlotId)
                .resolve("data")
                .resolve(EchoClientGameplaySaveCodec.MACHINES_PATH);
        require(Files.isRegularFile(blockEntitiesPath),
                "Manual save should persist machine block entities and power graph state");
        require(!Files.exists(legacyMachinesPath),
                "Fresh machine saves should use coordinate-backed block entities instead of client machines sidecars");
        String blockEntitiesText = Files.readString(blockEntitiesPath);
        require(blockEntitiesText.contains("8\t5\t9\tscrap_press")
                        && blockEntitiesText.contains("8\t5\t10\titem_pipe")
                        && blockEntitiesText.contains("8\t5\t11\tore_grinder")
                        && blockEntitiesText.contains("24\t5\t9\tscrap_press@24,5,9")
                        && blockEntitiesText.contains("26\t5\t9\tore_grinder@26,5,9")
                        && blockEntitiesText.contains("recipeProgressTicks=80")
                        && blockEntitiesText.contains("recipeProgressTicks=40")
                        && blockEntitiesText.contains("powerConsumed=80")
                        && blockEntitiesText.contains("powerConsumed=40")
                        && blockEntitiesText.contains("battery_bank")
                        && blockEntitiesText.contains("port.input=scrap_metal")
                        && blockEntitiesText.contains("selectedRecipe=echoashfallprotocol:scrap_press/compressed_scrap")
                        && blockEntitiesText.contains("recipeOptions=echoashfallprotocol:scrap_press/compressed_scrap|echoashfallprotocol:scrap_press/dense_compressed_scrap")
                        && blockEntitiesText.contains("slot.input.item=echoashfallprotocol:scrap_metal")
                        && blockEntitiesText.contains("slot.output.item=echoashfallprotocol:compressed_scrap"),
                "Machine block entity sidecar should contain multi-instance coordinates, progress, power nodes, inventory ports, and container slots");
        Path runtimeContentPath = saveRoot.resolve("slots")
                .resolve(savedSlotId)
                .resolve("data")
                .resolve(EchoClientGameplaySaveCodec.RUNTIME_CONTENT_PATH);
        require(Files.isRegularFile(runtimeContentPath),
                "Manual save should persist native runtime content rows beside client save data");
        String runtimeContentText = Files.readString(runtimeContentPath);
        require(runtimeContentText.contains(runtimeIds.blockId()) && runtimeContentText.contains(runtimeIds.structureScreenId()),
                "Runtime content save sidecar should include block and structure row identifiers");
        List<Map<String, Object>> sidecarRows = EchoClientRuntimeContentSaveCodec.readRows(runtimeContentText);
        EchoClientRuntimeWorldgenCatalog sidecarWorldgen =
                EchoClientRuntimeWorldgenCatalog.fromRows(sidecarRows);
        require(!sidecarWorldgen.emptyCatalog()
                        && sidecarWorldgen.detailSummaryForSmoke().contains("@96,7,96")
                        && sidecarWorldgen.detailSummaryForSmoke().contains("@102,96 r4 y6"),
                "Runtime content save sidecar should restore native worldgen row metadata; "
                        + sidecarWorldgen.detailSummaryForSmoke());
        Path manifestPath = saveRoot.resolve("slots").resolve(savedSlotId).resolve("manifest.json");
        String manifestText = Files.readString(manifestPath);
        require(manifestText.contains("clientRuntimeContentCodec")
                        && manifestText.contains("clientBlockEntitiesCodec")
                        && !manifestText.contains("clientMachinesCodec")
                        && manifestText.contains("runtimeContentRows")
                        && manifestText.contains(EchoClientRuntimeContentFingerprint.ALGORITHM_METADATA_KEY)
                        && manifestText.contains(EchoClientRuntimeContentFingerprint.FINGERPRINT_METADATA_KEY)
                        && manifestText.contains(EchoClientSaveEnvironmentFingerprint.ALGORITHM_METADATA_KEY)
                        && manifestText.contains(EchoClientSaveEnvironmentFingerprint.FINGERPRINT_METADATA_KEY)
                        && manifestText.contains(EchoClientSaveEnvironmentFingerprint.MODULE_IDS_METADATA_KEY)
                        && manifestText.contains(EchoClientSaveEnvironmentFingerprint.RESOURCE_PACK_IDS_METADATA_KEY)
                        && manifestText.contains("\"runtimeContentRows\": \"7\""),
                "Save manifest should advertise native runtime content and save environment fingerprints");
        require(liveClient.hasContinuableSession(),
                "New world should make the client continuable through memory and disk save state");
        require(liveClient.saveSlotSummaries().stream()
                        .anyMatch(slot -> slot.slotId().equals(savedSlotId) && slot.loadableInMemory()),
                "New world save should appear as a loadable World Select slot");
        liveClient.unloadWorld();
        require(liveClient.hasContinuableSession(),
                "Quit to title should keep Continue enabled for the current client session");

        EchoClientRuntimeServices restartedClient = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        require(!restartedClient.hasMemorySave(),
                "Restarted client service should prove Continue is coming from disk, not memory");
        require(restartedClient.hasContinuableSession(),
                "Restarted client should enable Continue from a loadable disk save");

        EchoClientRuntimeServices incompatibleClient =
                new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        require(incompatibleClient.importAdapterCoreContentRegistrations(
                        List.of(conflictingRuntimeBlockRow(runtimeIds.blockId()))
                ) == 1,
                "Compatibility smoke should import a conflicting current runtime content catalog");
        require(incompatibleClient.saveSlotSummaries().stream()
                        .anyMatch(slot -> slot.slotId().equals(savedSlotId)
                                && !slot.loadableInMemory()
                                && slot.detail().contains("runtime content mismatch")),
                "World Select should disable a disk slot when current runtime content conflicts with the save");
        require(!incompatibleClient.hasContinuableSession(),
                "Conflicting current runtime content should disable disk Continue");
        require(!incompatibleClient.continueFromSlot(savedSlotId),
                "Disk Continue should reject a save whose runtime content fingerprint mismatches the current catalog");
        require(!incompatibleClient.hasActiveWorld(),
                "Runtime content mismatch should fail before a world session opens");
        require(incompatibleClient.saveSlotError().contains("runtime content mismatch"),
                "Runtime content mismatch should leave a readable save slot error");

        String missingModuleId = "echomissing:test_module";
        Files.writeString(manifestPath, tamperMissingModule(manifestText, missingModuleId));
        EchoClientRuntimeServices missingModClient =
                new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        require(missingModClient.saveSlotSummaries().stream()
                        .anyMatch(slot -> slot.slotId().equals(savedSlotId)
                                && !slot.loadableInMemory()
                                && slot.detail().contains("missing mod(s) " + missingModuleId)),
                "World Select should warn when a saved module is missing from the current scan");
        require(!missingModClient.hasContinuableSession(),
                "Missing saved module should disable disk Continue");
        require(!missingModClient.continueFromSlot(savedSlotId),
                "Disk Continue should reject a save whose saved module set is missing");
        require(missingModClient.saveSlotError().contains("missing mod(s) " + missingModuleId),
                "Missing module restore rejection should leave a readable save slot error");
        Files.writeString(manifestPath, manifestText);

        EchoClientScreenController title = new EchoClientScreenController();
        title.showMainMenu(restartedClient.hasContinuableSession());
        EchoClientScreenSnapshot titleSnapshot = title.snapshot(restartedClient.hasContinuableSession());
        EchoClientScreenOption continueOption = titleSnapshot.options().get(0);
        require(continueOption.command() == EchoClientScreenCommand.CONTINUE_GAME && continueOption.enabled(),
                "Title Continue option should be enabled when only a disk save exists");

        require(restartedClient.continueFromSlot(""),
                "Blank Continue request should load the default disk save when no memory snapshot exists");
        require(restartedClient.hasActiveWorld(), "Disk Continue should restore an active world session");
        require(restartedClient.worldSession().slotId().equals(savedSlotId),
                "Disk Continue should restore the saved slot id");
        require(restartedClient.hasMemorySave(),
                "Disk Continue should seed an in-memory snapshot for later quick resume");
        EchoClientGameSession restoredRuntimeSession = restartedClient.session();
        require(restoredRuntimeSession != null,
                "Disk Continue should restore a game session before native runtime content checks");
        EchoClientMachineStateSnapshot restoredMachineState = restoredRuntimeSession.machineStateSnapshot();
        require(restoredMachineState.stateReloaded()
                        && restoredMachineState.recipeProgressTicks() == savedMachineState.recipeProgressTicks()
                        && restoredMachineState.powerConsumed() == savedMachineState.powerConsumed()
                        && restoredMachineState.oreGrinderInputCount() == savedMachineState.oreGrinderInputCount()
                        && powerNodeEnergy(restoredMachineState, "battery_bank") == savedBatteryEnergy
                        && machineBlockEntity(restoredMachineState, "scrap_press").x() == 8
                        && machineBlockEntity(restoredMachineState, "scrap_press").localX() == 8
                        && machineBlockEntity(restoredMachineState, "item_pipe").z() == 10
                        && machineBlockEntity(restoredMachineState, "ore_grinder").z() == 11
                        && machineBlockEntity(restoredMachineState, "scrap_press@24,5,9").state()
                        .getOrDefault("recipeProgressTicks", "").equals("40")
                        && machineBlockEntity(restoredMachineState, "scrap_press@24,5,9").state()
                        .getOrDefault("selectedRecipe", "").equals("echoashfallprotocol:scrap_press/compressed_scrap")
                        && machineBlockEntity(restoredMachineState, "scrap_press@24,5,9").state()
                        .getOrDefault("slot.input.item", "").equals("echoashfallprotocol:scrap_metal")
                        && machineBlockEntity(restoredMachineState, "ore_grinder@26,5,9").state()
                        .getOrDefault("inputCount", "").equals("1"),
                "Disk Continue should restore saved multi-instance machine block-entity and power graph state");
        EchoClientTechSurfaceModel restoredTechSurface = restartedClient.techSurfaceModel();
        require(restoredTechSurface.recipeProgressTicks() == savedMachineState.recipeProgressTicks()
                        && restoredTechSurface.oreGrinderInputCount() == savedMachineState.oreGrinderInputCount()
                        && restoredTechSurface.stateReloaded()
                        && restoredTechSurface.blockEntities().stream()
                        .anyMatch(row -> row.contains("scrap_press @ 8,5,9"))
                        && restoredTechSurface.blockEntities().stream()
                        .anyMatch(row -> row.contains("ore_grinder @ 8,5,11"))
                        && restoredTechSurface.blockEntities().stream()
                        .anyMatch(row -> row.contains("scrap_press@24,5,9 @ 24,5,9"))
                        && restoredTechSurface.machineDiagnostics().stream()
                        .anyMatch(row -> row.contains("multi-network machine graphs connected=2")),
                "ScreenCore machine surface model should read restored machine state after disk Continue");
        EchoVoxelBlockState restoredScrapPressState = restoredRuntimeSession.world().blockStateAt(8, 5, 9);
        require(restoredScrapPressState.block().id()
                        .equals(EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID)
                        && restoredScrapPressState.property("blockEntityId").orElse("").equals("scrap_press")
                        && restoredScrapPressState.property("recipeProgressTicks").orElse("").equals("80"),
                "Disk Continue should restore the placed scrap_press block entity in the voxel chunk");
        EchoVoxelBlockState restoredSecondScrapPressState = restoredRuntimeSession.world().blockStateAt(24, 5, 9);
        require(restoredSecondScrapPressState.block().id()
                        .equals(EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID)
                        && restoredSecondScrapPressState.property("blockEntityId").orElse("")
                        .equals("scrap_press@24,5,9")
                        && restoredSecondScrapPressState.property("canonicalId").orElse("").equals("scrap_press")
                        && restoredSecondScrapPressState.property("recipeProgressTicks").orElse("").equals("40"),
                "Disk Continue should restore the second placed scrap_press block entity in the voxel chunk");
        require(restoredRuntimeSession.world().blockAt(10, 5, 9).air()
                        && restoredRuntimeSession.world().blockAt(8, 5, 11).id()
                        .equals(EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID),
                "Disk Continue should preserve the edited machine layout instead of the reference ore_grinder position");
        require(restoredRuntimeSession.world().blockAt(savedBlockX, savedBlockY, savedBlockZ).id().equals(runtimeIds.blockId()),
                "Disk Continue should restore saved chunks that contain native runtime block ids");
        EchoVoxelBlockState restoredPersistentState =
                restoredRuntimeSession.world().blockStateAt(savedBlockX, savedBlockY, savedBlockZ);
        require(restoredPersistentState.property("facing").orElse("").equals("north")
                        && restoredPersistentState.property("powered").orElse("").equals("false")
                        && restoredPersistentState.property("opaque").orElse("").equals("false")
                        && restoredPersistentState.tickVersion() == 1L,
                "Disk Continue should restore native runtime block state properties and tick version");
        EchoVoxelBlock restoredRuntimeBlock =
                restoredRuntimeSession.bridge().registry().requireLiveVoxelBlock(runtimeIds.blockId());
        var restoredRuntimeEntry = restoredRuntimeSession.bridge().registry()
                .requireContentId("echoruntimehost:block/persistent_runtime_glass");
        var restoredRuntimeItemEntry = restoredRuntimeSession.bridge().registry()
                .requireContentId("echoruntimehost:item/persistent_runtime_alloy");
        require(restoredRuntimeBlock.displayName().equals("Persistent Runtime Glass"),
                "Disk Continue should restore the native block registration before gameplay registry use");
        require(restoredRuntimeBlock.atlasKey().equals("echoruntimehost/block/persistent_runtime_glass"),
                "Disk Continue should restore normalized native texture metadata");
        require(restoredRuntimeEntry.assetReferences().blockstateId()
                        .equals("echoruntimehost:blockstates/persistent_runtime_glass")
                        && restoredRuntimeEntry.assetReferences().modelId()
                        .equals("echoruntimehost:models/block/persistent_runtime_glass")
                        && restoredRuntimeEntry.assetReferences().textureId()
                        .equals("echoruntimehost:textures/block/persistent_runtime_glass")
                        && restoredRuntimeEntry.assetReferences().langValue().equals("Persistent Runtime Glass"),
                "Disk Continue should restore native asset/model/lang references");
        require(restoredRuntimeItemEntry.assetReferences().modelId()
                        .equals("echoruntimehost:models/item/persistent_runtime_alloy")
                        && restoredRuntimeItemEntry.assetReferences().textureId()
                        .equals("echoruntimehost:textures/item/persistent_runtime_alloy"),
                "Disk Continue should restore native item model and texture references");
        require(restoredRuntimeEntry.registryMetadata().tags().contains("transparent")
                        && "false".equals(restoredRuntimeEntry.registryMetadata().defaultState().get("opaque"))
                        && restoredRuntimeEntry.registryMetadata().behaviorHooks().contains("open_registered_screen")
                        && restoredRuntimeEntry.registryMetadata().saveCodecVersion().equals("echo.block.v1")
                        && "adaptercore-native".equals(
                        restoredRuntimeEntry.registryMetadata().compatibilityMetadata().get("source")),
                "Disk Continue should restore native block registry metadata");
        require(restoredRuntimeItemEntry.registryMetadata().tags().contains("crafting_component")
                        && restoredRuntimeItemEntry.registryMetadata().saveCodecVersion().equals("echo.item.v1"),
                "Disk Continue should restore native item registry metadata");
        EchoClientScreenRouteRequest restoredRoute = restoredRuntimeSession.worldInteractionRouteFor(restoredRuntimeBlock);
        require(restoredRoute.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN
                        && restoredRoute.targetId().equals(runtimeIds.structureScreenId()),
                "Disk Continue should restore native structure interaction routing");
        require(restartedClient.workbenchRecipeSummaries().stream()
                        .anyMatch(recipe -> recipe.recipeId().equals(runtimeIds.recipeId())
                                && recipe.label().equals("Persistent Runtime Alloy")),
                "Disk Continue should restore native item and recipe rows into workbench runtime services");
        EchoItemCraftResult crafted = restartedClient.craftWorkbenchRecipe(runtimeIds.recipeId());
        require(crafted != null && crafted.crafted(),
                "Disk Continue should craft through restored native recipe rows");
        EchoClientEntitySpawnSummary spawned = restoredRuntimeSession.tickEntities(1.1D);
        require(spawned.reason().equals("spawned") && spawned.definitionId().equals(runtimeIds.entityId()),
                "Disk Continue should restore native entity spawn rows");
        int beforeHealth = restoredRuntimeSession.playerVitals().currentHealth();
        restoredRuntimeSession.tickBiomeHazards(15.0D);
        require(restoredRuntimeSession.hazardState().hazardId().equals(runtimeIds.hazardId())
                        && restoredRuntimeSession.playerVitals().currentHealth() < beforeHealth,
                "Disk Continue should restore native hazard rows into live biome hazard simulation");
        int cachedBeforeRuntimeWorldgen = restoredRuntimeSession.cachedChunkCount();
        EchoClientGameplay restoredGameplay = new EchoClientGameplay();
        restoredGameplay.init(
                restoredRuntimeSession.world(),
                restoredRuntimeSession.player(),
                restoredRuntimeSession.hotbar()
        );
        movePlayer(restoredRuntimeSession, restoredGameplay, 96.5D, restoredRuntimeSession.player().state().y(), 96.5D);
        boolean restoredTargetChunkLoadedBeforeWorldgen =
                restoredRuntimeSession.world().loadedChunkIds().contains(new EchoVoxelChunkId(6, 0, 6));
        EchoClientWorldStreamResult restoredWorldgenStream =
                restartedClient.streamAroundPlayer(EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE);
        require(restoredWorldgenStream.loadedChunksChanged()
                        && restoredRuntimeSession.cachedChunkCount() > cachedBeforeRuntimeWorldgen,
                "Disk Continue should stream new chunks through persisted native worldgen rows");
        EchoVoxelBlockState restoredStructureBase = restoredRuntimeSession.world().blockStateAt(96, 7, 96);
        EchoVoxelBlockState restoredStructureTop = restoredRuntimeSession.world().blockStateAt(96, 8, 96);
        require(restoredStructureBase.block().id().equals(runtimeIds.blockId())
                        && restoredStructureTop.block().id().equals(runtimeIds.blockId()),
                "Disk Continue should restore native structure placement rows for future streamed chunks; base="
                        + restoredStructureBase.block().id()
                        + " top=" + restoredStructureTop.block().id()
                        + " chunkLoadedBefore="
                        + restoredTargetChunkLoadedBeforeWorldgen
                        + " chunkLoaded="
                        + restoredRuntimeSession.world().loadedChunkIds().contains(new EchoVoxelChunkId(6, 0, 6)));
        require(restoredStructureBase.property("source").orElse("").equals("runtime_structure")
                        && restoredStructureBase.property("structure").orElse("").equals(runtimeIds.structureRuntimeId()),
                "Disk Continue should preserve native structure block metadata in streamed chunks");
        EchoVoxelBlockState restoredRegionMarker = restoredRuntimeSession.world().blockStateAt(102, 6, 96);
        require(restoredRegionMarker.block().id().equals(runtimeIds.blockId()),
                "Disk Continue should restore native world-region rows for future streamed chunks; marker="
                        + restoredRegionMarker.block().id());
        require(restoredRegionMarker.property("source").orElse("").equals("runtime_region")
                        && restoredRegionMarker.property("region").orElse("").equals(runtimeIds.regionRuntimeId()),
                "Disk Continue should preserve native world-region block metadata in streamed chunks");

        EchoClientRuntimeServices loadGameClient = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        EchoClientScreenController worldSelect = new EchoClientScreenController();
        worldSelect.showMainMenu(loadGameClient.hasContinuableSession());
        require(worldSelect.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORLD_SELECT, true),
                "Load Game should open the World Select screen");
        worldSelect.updateSaveSlots(loadGameClient.saveSlotSummaries(), loadGameClient.saveSlotError());
        EchoClientScreenSnapshot loadSnapshot = worldSelect.snapshot(loadGameClient.hasContinuableSession());
        require(loadSnapshot.kind() == EchoClientScreenKind.WORLD_SELECT,
                "World Select should be the active Load Game screen");
        require(loadSnapshot.options().stream()
                        .anyMatch(option -> option.command() == EchoClientScreenCommand.CONTINUE_GAME
                                && option.enabled()
                                && option.label().contains("Ready")),
                "World Select should expose the disk save as an enabled Ready slot");
        require(worldSelect.selectedSaveSlotId().equals(savedSlotId),
                "World Select should select the saved disk slot by default");
        require(loadGameClient.continueFromSlot(worldSelect.selectedSaveSlotId()),
                "Selecting the World Select save slot should restore it from disk");
        require(loadGameClient.worldSession().slotId().equals(savedSlotId),
                "World Select restore should preserve the selected slot id");

        String backupResult = loadGameClient.backupAndPlanMigration(savedSlotId);
        require(backupResult.contains("migration steps"),
                "Backup And Migration should create a backup and report migration readiness");
        String backupId = backupResult.substring(0, backupResult.indexOf(" | "));
        Path backupManifest = saveRoot.resolve("backups").resolve(backupId).resolve("manifest.json");
        require(Files.isRegularFile(backupManifest),
                "Backup And Migration should materialize a backup manifest on disk");
        String updatedManifest = Files.readString(saveRoot.resolve("slots").resolve(savedSlotId).resolve("manifest.json"));
        require(updatedManifest.contains(backupId),
                "Backup And Migration should record the new backup id in the slot manifest");
        require(updatedManifest.contains("lastMigrationCheck"),
                "Backup And Migration should record migration readiness metadata");

        require(loadGameClient.deleteSlot(savedSlotId),
                "Delete World should remove the selected disk save slot");
        require(!Files.exists(saveRoot.resolve("slots").resolve(savedSlotId)),
                "Delete World should remove the save slot directory from disk");
        require(!loadGameClient.hasContinuableSession(),
                "Deleting the only active save slot should disable Continue");
        EchoClientScreenController afterDeleteTitle = new EchoClientScreenController();
        afterDeleteTitle.showMainMenu(loadGameClient.hasContinuableSession());
        require(!afterDeleteTitle.snapshot(loadGameClient.hasContinuableSession()).options().get(0).enabled(),
                "Title Continue option should disable after deleting the only save");

        writeReport(savedSlotId, backupId, thumbnailPath, capturedThumbnailBytes, savedSlotSummary);
        System.out.println("client save continue smoke PASS slot=" + savedSlotId + " backup=" + backupId + " deleted=true");
    }

    private static void writeReport(
            String slotId,
            String backupId,
            Path thumbnailPath,
            long capturedThumbnailBytes,
            EchoClientSaveSlotSummary savedSlotSummary
    ) throws IOException {
        String json = """
                {
                  "schema": "echo.standalone.client_save_continue.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientSaveContinueSmokeHarness",
                  "status": "PASS",
                  "summary": "OpenGL client Save, World Select, Continue, captured save-slot thumbnails, corrupt-thumbnail fallback, backup, migration, and delete flows passed.",
                  "slotId": "%s",
                  "backupId": "%s",
                  "coverage": {
                    "capturedOpenGlFramebufferThumbnail": true,
                    "thumbnailPngPersisted": true,
                    "thumbnailManifestMetadata": true,
                    "worldSelectUsesCapturedTexture": true,
                    "corruptThumbnailFallsBackToDeterministic": true,
                    "diskContinueRestoresSlot": true,
                    "worldSelectRestore": true,
                    "runtimeContentMismatchBlocksContinue": true,
                    "missingModuleBlocksContinue": true,
                    "backupAndMigrationReady": true,
                    "deleteWorldDisablesContinue": true
                  },
                  "thumbnail": {
                    "source": "%s",
                    "relativePath": "%s",
                    "resolvedPath": "%s",
                    "width": %d,
                    "height": %d,
                    "bytesBeforeCorruptionProbe": %d,
                    "textureEligible": true,
                    "fallbackVerified": true
                  },
                  "nativeModLoaderCommandUsed": false
                }
                """.formatted(
                escape(slotId),
                escape(backupId),
                escape(savedSlotSummary.thumbnailSource()),
                escape(savedSlotSummary.thumbnailPath()),
                escape(thumbnailPath.toAbsolutePath().normalize().toString()),
                savedSlotSummary.thumbnailWidth(),
                savedSlotSummary.thumbnailHeight(),
                capturedThumbnailBytes
        );
        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(REPORT_PATH, json);
    }

    private static RuntimeSaveContentIds importPersistentRuntimeContent(EchoClientRuntimeServices services) {
        RuntimeSaveContentIds ids = new RuntimeSaveContentIds(
                "echoruntimehost:persistent_runtime_glass",
                "echoruntimehost:persistent_runtime_alloy",
                "echoruntimehost:craft_persistent_runtime_alloy",
                "echoruntimehost:persistent_watcher",
                "echoruntimehost:persistent_volatile_air",
                "echoruntimehost:standalone/persistent_runtime_cache",
                "echoruntimehost:persistent_runtime_cache",
                "echoruntimehost:persistent_runtime_region"
        );
        int imported = services.importAdapterCoreContentRegistrations(List.of(
                persistentBlockRow(ids.blockId()),
                persistentItemRow(ids.itemId()),
                persistentRecipeRow(ids.recipeId(), ids.itemId()),
                persistentEntityRow(ids.entityId()),
                persistentHazardRow(ids.hazardId()),
                persistentStructureRow(ids.structureScreenId(), ids.structureRuntimeId(), ids.blockId()),
                persistentRegionRow(ids.regionRuntimeId(), ids.blockId())
        ));
        require(imported == 7,
                "Native runtime save smoke should import seven persistent native content rows");
        return ids;
    }

    private static Map<String, Object> persistentBlockRow(String blockId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:block/persistent_runtime_glass",
                "contentKind", "BLOCK",
                "domain", "blocks",
                "displayName", "Persistent Runtime Glass",
                "adapterKey", "registry.blocks.persistent_runtime_glass",
                "neoForgeId", blockId,
                "nativeLoaderId", "echoruntimehost:block/persistent_runtime_glass",
                "standaloneRuntimeId", blockId,
                "metadata", Map.ofEntries(
                        Map.entry("liveVoxelId", blockId),
                        Map.entry("argb", "#77D6EA"),
                        Map.entry("detailArgb", "0xFFD8FBFF"),
                        Map.entry("blockstate", "assets/echoruntimehost/blockstates/persistent_runtime_glass.json"),
                        Map.entry("model", "echoruntimehost:block/persistent_runtime_glass"),
                        Map.entry("texture", "echoruntimehost:block/persistent_runtime_glass"),
                        Map.entry("langKey", "block.echoruntimehost.persistent_runtime_glass"),
                        Map.entry("langValue", "Persistent Runtime Glass"),
                        Map.entry("tags", List.of("adaptercore", "native-content", "persistent", "transparent")),
                        Map.entry("defaultState", Map.of("facing", "north", "powered", "false", "opaque", "false")),
                        Map.entry("behaviorHooks", List.of("open_registered_screen", "drop_self")),
                        Map.entry("saveCodecVersion", "echo.block.v1"),
                        Map.entry("compatibilityMetadata", Map.of("source", "adaptercore-native", "level", "2")),
                        Map.entry("materialPattern", "TERMINAL_GRID"),
                        Map.entry("solid", false),
                        Map.entry("opaque", false),
                        Map.entry("hardness", "0.3")
                )
        );
    }

    private static Map<String, Object> conflictingRuntimeBlockRow(String blockId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:block/persistent_runtime_glass",
                "contentKind", "BLOCK",
                "domain", "blocks",
                "displayName", "Conflicting Runtime Glass",
                "adapterKey", "registry.blocks.persistent_runtime_glass",
                "neoForgeId", blockId,
                "nativeLoaderId", "echoruntimehost:block/persistent_runtime_glass",
                "standaloneRuntimeId", blockId,
                "metadata", Map.of(
                        "liveVoxelId", blockId,
                        "argb", "#FF3366",
                        "texture", "echoruntimehost:block/conflicting_runtime_glass",
                        "saveCodecVersion", "echo.block.v2"
                )
        );
    }

    private static Map<String, Object> persistentItemRow(String itemId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:item/persistent_runtime_alloy",
                "contentKind", "ITEM",
                "domain", "items",
                "displayName", "Persistent Runtime Alloy",
                "adapterKey", "registry.items.persistent_runtime_alloy",
                "neoForgeId", itemId,
                "nativeLoaderId", "echoruntimehost:item/persistent_runtime_alloy",
                "standaloneRuntimeId", itemId,
                "metadata", Map.of(
                        "category", "MATERIAL",
                        "maxStackSize", 8,
                        "weight", "0.5",
                        "model", "echoruntimehost:item/persistent_runtime_alloy",
                        "texture", "echoruntimehost:item/persistent_runtime_alloy",
                        "tags", List.of("adaptercore", "native-content", "persistent", "crafting_component"),
                        "saveCodecVersion", "echo.item.v1",
                        "tooltipLines", List.of("Restored from a saved native runtime content catalog")
                )
        );
    }

    private static Map<String, Object> persistentRecipeRow(String recipeId, String itemId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:recipe/craft_persistent_runtime_alloy",
                "contentKind", "RECIPE",
                "domain", "recipes",
                "displayName", "Persistent Runtime Alloy",
                "adapterKey", "registry.recipes.craft_persistent_runtime_alloy",
                "neoForgeId", recipeId,
                "nativeLoaderId", "echoruntimehost:recipe/craft_persistent_runtime_alloy",
                "standaloneRuntimeId", recipeId,
                "metadata", Map.of(
                        "recipeId", recipeId,
                        "type", "minecraft:crafting_shapeless",
                        "ingredients", List.of("echoadaptercore:runtime_marker_block"),
                        "ingredientCounts", Map.of("echoadaptercore:runtime_marker_block", 1),
                        "result", itemId,
                        "resultCount", 1,
                        "pattern", List.of("S"),
                        "group", "runtime_native",
                        "category", "adaptercore",
                        "sourceLogicalId", "runtime/native/content/persistent_runtime_alloy.json"
                )
        );
    }

    private static Map<String, Object> persistentEntityRow(String entityId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:entity/persistent_watcher",
                "contentKind", "ENTITY",
                "domain", "entities",
                "displayName", "Persistent Watcher",
                "adapterKey", "registry.entities.persistent_watcher",
                "neoForgeId", entityId,
                "nativeLoaderId", "echoruntimehost:entity/persistent_watcher",
                "standaloneRuntimeId", entityId,
                "metadata", Map.of(
                        "definitionId", entityId,
                        "kind", "HOSTILE",
                        "maxHealth", 34,
                        "movementSpeed", 1,
                        "aiProfile", "hostile_scavenger",
                        "biomeTags", List.of("crash_zone"),
                        "renderArgb", "#D8C16A",
                        "renderShape", "DRONE"
                )
        );
    }

    private static Map<String, Object> persistentHazardRow(String hazardId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:hazard/persistent_volatile_air",
                "contentKind", "WORLD_HAZARD",
                "domain", "hazards",
                "displayName", "Persistent Volatile Air",
                "adapterKey", "registry.hazards.persistent_volatile_air",
                "neoForgeId", hazardId,
                "nativeLoaderId", "echoruntimehost:hazard/persistent_volatile_air",
                "standaloneRuntimeId", hazardId,
                "metadata", Map.of(
                        "hazardId", hazardId,
                        "biomeTags", List.of("crash_zone"),
                        "exposurePerSecond", "12.0",
                        "damage", 2
                )
        );
    }

    private static Map<String, Object> persistentStructureRow(
            String screenId,
            String structureRuntimeId,
            String blockId
    ) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:structure/persistent_runtime_cache",
                "contentKind", "STRUCTURE",
                "domain", "structures",
                "displayName", "Persistent Runtime Cache",
                "adapterKey", "registry.structures.persistent_runtime_cache",
                "neoForgeId", structureRuntimeId,
                "nativeLoaderId", "echoruntimehost:structure/persistent_runtime_cache",
                "standaloneRuntimeId", structureRuntimeId,
                "metadata", Map.of(
                        "matchTokens", List.of("persistent runtime glass"),
                        "command", "OPEN_REGISTERED_SCREEN",
                        "targetId", screenId,
                        "placementBlockId", blockId,
                        "shape", "PILLAR",
                        "x", 96,
                        "y", 7,
                        "z", 96,
                        "height", 2
                )
        );
    }

    private static Map<String, Object> persistentRegionRow(String regionRuntimeId, String blockId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:world_region/persistent_runtime_region",
                "contentKind", "WORLD_REGION",
                "domain", "world_regions",
                "displayName", "Persistent Runtime Region",
                "adapterKey", "registry.world_regions.persistent_runtime_region",
                "neoForgeId", regionRuntimeId,
                "nativeLoaderId", "echoruntimehost:world_region/persistent_runtime_region",
                "standaloneRuntimeId", regionRuntimeId,
                "metadata", Map.of(
                        "surfaceBlockId", blockId,
                        "centerX", 102,
                        "centerZ", 96,
                        "radius", 4,
                        "fixedY", 6
                )
        );
    }

    private record RuntimeSaveContentIds(
            String blockId,
            String itemId,
            String recipeId,
            String entityId,
            String hazardId,
            String structureScreenId,
            String structureRuntimeId,
            String regionRuntimeId
    ) {
    }

    private static String tamperMissingModule(String manifestText, String missingModuleId) {
        String existingModules = metadataValue(
                manifestText,
                EchoClientSaveEnvironmentFingerprint.MODULE_IDS_METADATA_KEY
        );
        String replacementModules = existingModules.isBlank()
                ? missingModuleId
                : existingModules + "," + missingModuleId;
        return replaceMetadataValue(
                replaceMetadataValue(
                        manifestText,
                        EchoClientSaveEnvironmentFingerprint.MODULE_IDS_METADATA_KEY,
                        replacementModules
                ),
                EchoClientSaveEnvironmentFingerprint.FINGERPRINT_METADATA_KEY,
                "0000000000000000000000000000000000000000000000000000000000000000"
        );
    }

    private static String metadataValue(String text, String key) {
        String marker = "\"" + key + "\": \"";
        int start = text.indexOf(marker);
        if (start < 0) {
            return "";
        }
        start += marker.length();
        int end = text.indexOf('"', start);
        return end < 0 ? "" : text.substring(start, end);
    }

    private static String replaceMetadataValue(String text, String key, String replacement) {
        String marker = "\"" + key + "\": \"";
        int start = text.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing manifest metadata key: " + key);
        }
        start += marker.length();
        int end = text.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Unterminated manifest metadata value: " + key);
        }
        return text.substring(0, start) + replacement + text.substring(end);
    }

    private static void movePlayer(
            EchoClientGameSession session,
            EchoClientGameplay gameplay,
            double x,
            double y,
            double z
    ) {
        EchoVoxelPlayerState current = session.player().state();
        EchoVoxelPlayerController moved = new EchoVoxelPlayerController(new EchoVoxelPlayerState(
                x,
                y,
                z,
                current.velocityY(),
                current.yawDegrees(),
                current.pitchDegrees(),
                current.grounded(),
                current.crouching(),
                current.sprinting(),
                current.selectedSlot(),
                current.reach()
        ));
        gameplay.init(session.world(), moved, session.hotbar());
        session.updateFromGameplay(gameplay);
    }

    private static void requireMachinePlacementAndBreakLifecycle() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory()
                .newWorld("machine-placement-lifecycle")
                .gameSession();
        EchoVoxelBlock itemPipeBlock = session.bridge().registry()
                .requireLiveVoxelBlock(EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID);
        EchoVoxelBlockState itemPipePlacementState = session.defaultBlockStateFor(itemPipeBlock);
        require(itemPipePlacementState.property("source").orElse("").equals("machine_block_entity")
                        && itemPipePlacementState.property("blockEntityId").orElse("").equals("item_pipe")
                        && itemPipePlacementState.property("machineKind").orElse("").equals("INVENTORY_PIPE"),
                "Hotbar machine placement state should immediately carry block entity identity metadata");

        EchoClientMachineStateSnapshot initialState = session.machineStateSnapshot();
        int initialPowerConsumed = initialState.powerConsumed();
        int initialOreGrinderInput = initialState.oreGrinderInputCount();
        require(session.world().setBlockStateAt(9, 5, 9, EchoVoxelBlockState.AIR),
                "Machine lifecycle smoke should clear the item pipe block before break reconciliation");
        session.recordBlockBroken(itemPipeBlock);
        EchoClientMachineStateSnapshot brokenState = session.machineStateSnapshot();
        require(!brokenState.graphConnected()
                        && !hasMachineBlockEntity(brokenState, "item_pipe")
                        && session.tickMachines(40) == 0
                        && session.machineStateSnapshot().powerConsumed() == initialPowerConsumed,
                "Breaking a placed item pipe should immediately disconnect machine ticks and remove its block entity");

        require(session.world().setBlockStateAt(9, 5, 9, itemPipePlacementState),
                "Machine lifecycle smoke should place the item pipe through the gameplay placement state factory");
        require(session.world().blockStateAt(9, 5, 9).property("blockEntityId").orElse("").equals("item_pipe"),
                "Placed machine block should expose block entity identity before a save/materialize pass");
        session.recordBlockPlaced(itemPipePlacementState);
        EchoClientMachineStateSnapshot replacedState = session.machineStateSnapshot();
        require(replacedState.graphConnected()
                        && machineBlockEntity(replacedState, "item_pipe").x() == 9
                        && machineBlockEntity(replacedState, "item_pipe").z() == 9
                        && session.world().blockStateAt(9, 5, 9)
                        .property("machineKind").orElse("").equals("INVENTORY_PIPE"),
                "Replacing the placed item pipe should immediately reconnect the machine graph");

        int resumedTicks = session.tickMachines(40);
        EchoClientMachineStateSnapshot resumedState = session.machineStateSnapshot();
        require(resumedTicks == 40
                        && resumedState.powerConsumed() == initialPowerConsumed + 40
                        && resumedState.oreGrinderInputCount() == initialOreGrinderInput + 1,
                "Machine ticks should resume after a broken logistics block is placed back into the world; ticks="
                        + resumedTicks
                        + " power=" + resumedState.powerConsumed()
                        + " oreInput=" + resumedState.oreGrinderInputCount()
                        + " replacedInput=" + replacedState.scrapPressInputCount()
                        + " replacedEnergy=" + machineBlockEntity(replacedState, "scrap_press").state()
                        .getOrDefault("energy", "")
                        + " replacedDiagnostics=" + replacedState.diagnostics());
    }

    private static EchoClientSaveSlotThumbnailCapture framebufferThumbnailCapture() throws IOException {
        BufferedImage image = new BufferedImage(320, 180, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = y < 70
                        ? 0xFF204D79
                        : y < 132
                        ? 0xFF3E6846
                        : 0xFF14201A;
                if (x > 132 && x < 188 && y > 42 && y < 138) {
                    color = 0xFFE0B74D;
                }
                image.setRGB(x, y, color);
            }
        }
        return EchoClientSaveSlotThumbnailCapture.fromImage(
                EchoClientSaveSlotThumbnailGenerator.FRAMEBUFFER_THUMBNAIL_SOURCE,
                image
        );
    }

    private static int powerNodeEnergy(EchoClientMachineStateSnapshot snapshot, String nodeId) {
        for (EchoClientMachineStateSnapshot.PowerNode node : snapshot.powerGraph()) {
            if (node.id().equals(nodeId)) {
                return node.energy();
            }
        }
        return -1;
    }

    private static EchoClientMachineStateSnapshot.BlockEntity machineBlockEntity(
            EchoClientMachineStateSnapshot snapshot,
            String entityId
    ) {
        return snapshot.blockEntities().stream()
                .filter(blockEntity -> blockEntity.entityId().equals(entityId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing machine block entity: " + entityId));
    }

    private static boolean hasMachineBlockEntity(EchoClientMachineStateSnapshot snapshot, String entityId) {
        return snapshot.blockEntities().stream()
                .anyMatch(blockEntity -> blockEntity.entityId().equals(entityId));
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
