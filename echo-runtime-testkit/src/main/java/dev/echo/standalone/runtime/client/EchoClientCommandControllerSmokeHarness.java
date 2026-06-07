package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientCommandControllerSmokeHarness {
    private EchoClientCommandControllerSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-command-controller-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientGameplayRuntimeController gameplayRuntime =
                new EchoClientGameplayRuntimeController(services, screens, worldSessions);
        RecordingHost host = new RecordingHost();
        EchoClientCommandController commands =
                new EchoClientCommandController(services, screens, worldSessions, gameplayRuntime, host);

        services.startNewWorld("command-controller");
        String slotId = services.worldSession().slotId();
        screens.showPauseMenu();
        require(commands.execute(EchoClientScreenCommand.SAVE_GAME),
                "Command controller should handle Save Game when a world is active");
        require(host.savingTransitions == 1, "Save Game should request a saving host transition");
        require(screens.state() == EchoClientGameState.SAVING, "Save Game should publish the saving screen state");
        require(services.hasMemorySave(), "Save Game should capture an in-memory save");

        require(commands.execute(EchoClientScreenCommand.RELOAD_TEXTURE_ATLAS),
                "Command controller should handle Texture Atlas Reload");
        require(host.assetReloads == 1 && host.rebuildAtlasRequests == 1,
                "Texture Atlas Reload should refresh assets and request atlas rebuild for an active world");
        require(commands.execute(EchoClientScreenCommand.REFRESH_RESOURCE_PACKS),
                "Command controller should handle Resource Packs refresh");
        require(host.assetReloads == 2 && host.rebuildAtlasRequests == 2,
                "Resource Packs refresh should refresh assets and request atlas rebuild for an active world");

        services.session().quickMoveContainerSlotToPlayer(1);
        screens.showPauseMenu();
        screens.updateWorkbenchRecipes(services.workbenchRecipeSummaries(), services.workbenchRecipeError());
        require(commands.execute(EchoClientScreenCommand.OPEN_WORKBENCH),
                "Command controller should delegate Workbench navigation to the ScreenCore controller");
        String selectedRecipe = screens.selectedWorkbenchRecipeId();
        require(!selectedRecipe.isBlank(), "Workbench route should select a craftable recipe");
        require(commands.execute(EchoClientScreenCommand.CRAFT_WORKBENCH_RECIPE),
                "Command controller should craft the selected workbench recipe");
        require(services.workbenchScreenModel(selectedRecipe) != null,
                "Crafting should keep workbench recipe details available");

        placeSecondMachineNetwork(services.session());
        screens.showPauseMenu();
        screens.updateTechSurfaceModel(services.techSurfaceModel());
        require(commands.execute(EchoClientScreenCommand.OPEN_MACHINE),
                "Command controller should delegate Machine navigation to the ScreenCore controller");
        EchoClientScreenSnapshot machine = screens.snapshot(true);
        require(optionTarget(machine, EchoClientScreenCommand.INSERT_MACHINE_INPUT, "scrap_press@24,5,9"),
                "Machine route should expose per-instance input controls for the second scrap_press");
        selectTarget(screens, EchoClientScreenCommand.INSERT_MACHINE_INPUT, "scrap_press@24,5,9", true);
        int scrapBefore = itemCount(services.session(), "echoashfallprotocol:scrap_metal");
        EchoClientMachineStateSnapshot machineBeforeInput = services.session().machineStateSnapshot();
        int machineInputBefore = machineInputCount(machineBeforeInput, "scrap_press@24,5,9");
        int primaryMachineInputBefore = machineInputCount(machineBeforeInput, "scrap_press");
        require(commands.execute(EchoClientScreenCommand.INSERT_MACHINE_INPUT),
                "Command controller should insert player scrap into the selected machine instance");
        EchoClientMachineStateSnapshot machineAfterInput = services.session().machineStateSnapshot();
        require(itemCount(services.session(), "echoashfallprotocol:scrap_metal") == scrapBefore - 1,
                "Machine input command should consume one Scrap Metal from player inventory");
        require(machineInputCount(machineAfterInput, "scrap_press@24,5,9") == machineInputBefore + 1,
                "Machine input command should increase only the selected scrap_press instance input count");
        require(machineInputCount(machineAfterInput, "scrap_press") == primaryMachineInputBefore,
                "Machine input command should leave the primary scrap_press input untouched");
        require(screens.snapshot(true).toast().message().contains("scrap_press@24,5,9"),
                "Machine input command should report the selected instance in the ScreenCore toast");

        require(services.session().tickMachines(40) >= 1,
                "Command smoke should let the second machine network produce compressed scrap before extraction");
        screens.updateTechSurfaceModel(services.techSurfaceModel());
        machine = screens.snapshot(true);
        require(optionTarget(machine, EchoClientScreenCommand.EXTRACT_MACHINE_OUTPUT, "ore_grinder@26,5,9"),
                "Machine route should expose per-instance compressed-scrap extraction controls");
        selectTarget(screens, EchoClientScreenCommand.EXTRACT_MACHINE_OUTPUT, "ore_grinder@26,5,9", true);
        int compressedBefore = itemCount(services.session(), "echoashfallprotocol:compressed_scrap");
        EchoClientMachineStateSnapshot machineBeforeOutput = services.session().machineStateSnapshot();
        int machineOutputBefore = machineCompressedScrapCount(machineBeforeOutput, "ore_grinder@26,5,9");
        int primaryOutputBefore = machineCompressedScrapCount(machineBeforeOutput, "ore_grinder");
        require(machineOutputBefore > 0,
                "Second ore_grinder should hold compressed scrap before extraction");
        require(commands.execute(EchoClientScreenCommand.EXTRACT_MACHINE_OUTPUT),
                "Command controller should extract compressed scrap from the selected machine instance");
        EchoClientMachineStateSnapshot machineAfterOutput = services.session().machineStateSnapshot();
        require(itemCount(services.session(), "echoashfallprotocol:compressed_scrap") == compressedBefore + 1,
                "Machine output command should add one Compressed Scrap to player inventory");
        require(machineCompressedScrapCount(machineAfterOutput, "ore_grinder@26,5,9") == machineOutputBefore - 1,
                "Machine output command should decrement only the selected ore_grinder instance buffer");
        require(machineCompressedScrapCount(machineAfterOutput, "ore_grinder") == primaryOutputBefore,
                "Machine output command should leave the primary ore_grinder buffer untouched");
        require(screens.snapshot(true).toast().message().contains("ore_grinder@26,5,9"),
                "Machine output command should report the selected instance in the ScreenCore toast");

        String denseRecipeTarget =
                "scrap_press@24,5,9|echoashfallprotocol:scrap_press/dense_compressed_scrap";
        screens.updateTechSurfaceModel(services.techSurfaceModel());
        machine = screens.snapshot(true);
        require(optionTarget(machine, EchoClientScreenCommand.SELECT_MACHINE_RECIPE, denseRecipeTarget),
                "Machine route should expose per-instance recipe selection controls");
        selectTarget(screens, EchoClientScreenCommand.SELECT_MACHINE_RECIPE, denseRecipeTarget, true);
        EchoClientMachineStateSnapshot machineBeforeRecipe = services.session().machineStateSnapshot();
        int denseInputBefore = machineSlotCount(machineBeforeRecipe, "scrap_press@24,5,9", "input");
        require(commands.execute(EchoClientScreenCommand.SELECT_MACHINE_RECIPE),
                "Command controller should select the recipe for the targeted machine instance");
        EchoClientMachineStateSnapshot machineAfterRecipe = services.session().machineStateSnapshot();
        require(machineSelectedRecipe(machineAfterRecipe, "scrap_press@24,5,9")
                        .equals("echoashfallprotocol:scrap_press/dense_compressed_scrap"),
                "Machine recipe selection should be stored on the selected scrap_press instance");
        require(machineSelectedRecipe(machineAfterRecipe, "scrap_press")
                        .equals("echoashfallprotocol:scrap_press/compressed_scrap"),
                "Machine recipe selection should leave the primary scrap_press recipe untouched");
        require(screens.snapshot(true).toast().message().contains("scrap_press@24,5,9"),
                "Machine recipe command should report the selected instance in the ScreenCore toast");
        require(services.session().tickMachines(80) >= 1,
                "Command smoke should run the dense batch recipe after selection");
        EchoClientMachineStateSnapshot machineAfterDenseBatch = services.session().machineStateSnapshot();
        require(machineSlotCount(machineAfterDenseBatch, "scrap_press@24,5,9", "input") == denseInputBefore - 2,
                "Dense recipe should consume two scrap from the selected machine input slot");
        require(machineSlotCount(machineAfterDenseBatch, "scrap_press@24,5,9", "output") == 1,
                "Dense recipe should leave one compressed scrap in the selected machine output slot after logistics");
        require(machineSlotCount(machineAfterDenseBatch, "ore_grinder@26,5,9", "input") == 1,
                "Dense recipe should transfer one compressed scrap into the paired ore_grinder input slot");

        require(commands.execute(EchoClientScreenCommand.QUIT_TO_TITLE),
                "Command controller should handle Quit To Title");
        require(host.cursorUnlocks == 1, "Quit To Title should unlock the cursor through the host");
        require(!services.hasActiveWorld(), "Quit To Title should unload the active world");
        require(screens.state() == EchoClientGameState.MAIN_MENU,
                "Quit To Title should return to the main menu");

        require(commands.execute(EchoClientScreenCommand.OPEN_WORLD_SELECT),
                "Command controller should delegate World Select navigation to the ScreenCore controller");
        screens.updateSaveSlots(services.saveSlotSummaries(), services.saveSlotError());
        require(screens.selectedSaveSlotId().equals(slotId),
                "World Select should select the saved command-controller slot");
        EchoClientSaveSlotSummary selectedSlot = services.saveSlotSummaries().stream()
                .filter(slot -> slot.slotId().equals(slotId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Command smoke should expose the selected save summary"));
        EchoClientScreenSnapshot worldSelect = screens.snapshot(false);
        require(optionLabel(worldSelect, "Review Slot " + slotId),
                "World Select should expose the selected save slot id as a review row");
        EchoClientSaveSlotThumbnailSnapshot thumbnail = worldSelect.saveSlotThumbnail();
        require(thumbnail.visible() && thumbnail.slotId().equals(slotId),
                "World Select should expose a visible thumbnail for the selected save slot");
        require(thumbnail.displayName().equals(selectedSlot.displayName())
                        && thumbnail.packId().equals(selectedSlot.packId()),
                "World Select thumbnail should carry selected save summary metadata");
        require(thumbnail.statusLabel().equals("READY"),
                "World Select thumbnail should expose the selected save loadability status");
        require(thumbnail.captured()
                        && thumbnail.source().equals(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_SOURCE)
                        && thumbnail.relativePath().equals(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_PATH),
                "World Select thumbnail should use the captured saved-world icon metadata for fresh saves");
        require(thumbnail.width() == 160
                        && thumbnail.height() == 90
                        && Files.isRegularFile(Path.of(thumbnail.resolvedPath()))
                        && EchoClientUiRenderer.usesCapturedThumbnailTexture(thumbnail),
                "World Select thumbnail should be eligible for captured PNG texture rendering");
        require((thumbnail.skyArgb() & 0x00FFFFFF) != 0
                        && (thumbnail.terrainArgb() & 0x00FFFFFF) != 0
                        && (thumbnail.accentArgb() & 0x00FFFFFF) != 0,
                "World Select thumbnail should carry non-empty captured preview colors");
        require(optionLabelPrefix(worldSelect, "Review runtime "),
                "World Select should expose runtime compatibility review details for the selected save slot");
        require(optionLabelPrefix(worldSelect, "Review environment "),
                "World Select should expose mod/resource-pack environment review details for the selected save slot");
        require(worldSelect.options().stream()
                        .anyMatch(option -> option.label().equals("Rename To")
                                && option.kind() == EchoClientScreenOptionKind.TEXT),
                "World Select should expose a rename text field for the selected save slot");
        require(worldSelect.options().stream()
                        .anyMatch(option -> option.command() == EchoClientScreenCommand.RENAME_SELECTED_WORLD
                                && option.enabled()),
                "World Select should expose an enabled Rename World action for the selected save slot");
        selectLabel(screens, "Rename To", false);
        require(screens.activateSelection(false) == EchoClientScreenCommand.NONE,
                "Activating Rename To should enter text editing rather than dispatch a command");
        for (int i = 0; i < 64; i++) {
            screens.handleTextInput("", true, false);
        }
        screens.handleTextInput("Renamed Command Slot", false, false);
        screens.stopTextEditing(false);
        require(commands.execute(EchoClientScreenCommand.RENAME_SELECTED_WORLD),
                "Command controller should rename the selected world");
        require(services.saveSlotSummaries().stream()
                        .anyMatch(slot -> slot.slotId().equals(slotId)
                                && slot.displayName().equals("Renamed Command Slot")),
                "Renaming should persist the display name in the save manifest");
        require(screens.selectedSaveSlotId().equals(slotId),
                "Renaming should keep the selected slot id stable");
        require(screens.selectedSaveSlotLabel().equals("Renamed Command Slot"),
                "Renaming should refresh the World Select display name");
        require(screens.snapshot(false).saveSlotThumbnail().displayName().equals("Renamed Command Slot"),
                "Renaming should refresh the World Select thumbnail display name");
        require(commands.execute(EchoClientScreenCommand.BACKUP_SELECTED_WORLD),
                "Command controller should backup the selected world");
        require(Files.isDirectory(saveRoot.resolve("backups")),
                "Backup command should materialize a backups directory");
        require(commands.execute(EchoClientScreenCommand.DELETE_SELECTED_WORLD),
                "Command controller should delete the selected world");
        require(!Files.exists(saveRoot.resolve("slots").resolve(slotId)),
                "Delete command should remove the save slot directory");
        require(!services.hasContinuableSession(),
                "Deleting the selected save should clear memory/disk Continue state");

        require(commands.execute(EchoClientScreenCommand.QUIT_CLIENT),
                "Command controller should handle Quit Client");
        require(host.closeRequests == 1, "Quit Client should request host window close");

        System.out.println("client command controller smoke PASS slot=" + slotId
                + " assetReloads=" + host.assetReloads
                + " backups=true deleted=true");
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

    private static void placeSecondMachineNetwork(EchoClientGameSession session) {
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID, 20);
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID, 21);
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.LOAD_DISTRIBUTOR_BLOCK_ID, 22);
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID, 23);
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID, 24);
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID, 25);
        placeMachineBlock(session, EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID, 26);
        require(session.reconcileMachineBlockEntitiesFromWorld() == 14,
                "Command smoke should reconcile two complete machine networks");
    }

    private static void placeMachineBlock(EchoClientGameSession session, String blockId, int x) {
        EchoVoxelBlock block = session.bridge().registry().requireLiveVoxelBlock(blockId);
        EchoVoxelBlockState state = session.defaultBlockStateFor(block);
        require(session.world().setBlockStateAt(x, 5, 9, state),
                "Command smoke should place machine block " + blockId);
    }

    private static boolean optionTarget(
            EchoClientScreenSnapshot snapshot,
            EchoClientScreenCommand command,
            String targetId
    ) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command && option.targetId().equals(targetId));
    }

    private static boolean optionLabel(EchoClientScreenSnapshot snapshot, String label) {
        return snapshot.options().stream().anyMatch(option -> option.label().equals(label));
    }

    private static boolean optionLabelPrefix(EchoClientScreenSnapshot snapshot, String prefix) {
        return snapshot.options().stream().anyMatch(option -> option.label().startsWith(prefix));
    }

    private static void selectTarget(
            EchoClientScreenController screens,
            EchoClientScreenCommand command,
            String targetId,
            boolean hasSession
    ) {
        for (int attempt = 0; attempt < 80; attempt++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(hasSession);
            EchoClientScreenOption selected = snapshot.options().get(snapshot.selectedIndex());
            if (selected.command() == command && selected.targetId().equals(targetId)) {
                return;
            }
            screens.moveSelection(1, hasSession, 720);
        }
        throw new AssertionError("Could not select " + command + " target " + targetId);
    }

    private static void selectLabel(
            EchoClientScreenController screens,
            String label,
            boolean hasSession
    ) {
        for (int attempt = 0; attempt < 80; attempt++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(hasSession);
            EchoClientScreenOption selected = snapshot.options().get(snapshot.selectedIndex());
            if (selected.label().equals(label)) {
                return;
            }
            screens.moveSelection(1, hasSession, 720);
        }
        throw new AssertionError("Could not select label " + label);
    }

    private static int itemCount(EchoClientGameSession session, String itemId) {
        return session.inventorySnapshots().stream()
                .filter(slot -> slot.itemId().equals(itemId))
                .mapToInt(EchoClientInventorySlotSnapshot::count)
                .sum();
    }

    private static int machineInputCount(EchoClientMachineStateSnapshot snapshot, String machineId) {
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : snapshot.blockEntities()) {
            if (blockEntity.entityId().equals(machineId)) {
                return intValue(blockEntity.state().get("inputCount"));
            }
        }
        return -1;
    }

    private static int machineCompressedScrapCount(EchoClientMachineStateSnapshot snapshot, String machineId) {
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : snapshot.blockEntities()) {
            if (!blockEntity.entityId().equals(machineId)) {
                continue;
            }
            String canonicalId = blockEntity.state().getOrDefault("canonicalId", blockEntity.entityId());
            if (canonicalId.startsWith("ore_grinder")) {
                return intValue(blockEntity.state().get("inputCount"));
            }
            if (canonicalId.startsWith("scrap_press")) {
                return intValue(blockEntity.state().get("outputCount"));
            }
        }
        return -1;
    }

    private static String machineSelectedRecipe(EchoClientMachineStateSnapshot snapshot, String machineId) {
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : snapshot.blockEntities()) {
            if (blockEntity.entityId().equals(machineId)) {
                return blockEntity.state().getOrDefault("selectedRecipe", "");
            }
        }
        return "";
    }

    private static int machineSlotCount(
            EchoClientMachineStateSnapshot snapshot,
            String machineId,
            String slotName
    ) {
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : snapshot.blockEntities()) {
            if (blockEntity.entityId().equals(machineId)) {
                return intValue(blockEntity.state().get("slot." + slotName + ".count"));
            }
        }
        return -1;
    }

    private static int intValue(String value) {
        try {
            return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingHost implements EchoClientCommandController.Host {
        private int attachSessionRequests;
        private int savingTransitions;
        private int cursorUnlocks;
        private int closeRequests;
        private int assetReloads;
        private int rebuildAtlasRequests;

        @Override
        public void attachSession() {
            attachSessionRequests++;
        }

        @Override
        public void beginSaving() {
            savingTransitions++;
        }

        @Override
        public void unlockCursor() {
            cursorUnlocks++;
        }

        @Override
        public void requestClose() {
            closeRequests++;
        }

        @Override
        public void reloadMinecraftAssets(boolean rebuildAtlas) {
            assetReloads++;
            if (rebuildAtlas) {
                rebuildAtlasRequests++;
            }
        }
    }
}
