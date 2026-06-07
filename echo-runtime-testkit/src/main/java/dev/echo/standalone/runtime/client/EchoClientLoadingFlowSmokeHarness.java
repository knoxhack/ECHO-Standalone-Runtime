package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientLoadingFlowSmokeHarness {
    private EchoClientLoadingFlowSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-loading-flow-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientScreenController createScreens = new EchoClientScreenController();
        createScreens.showMainMenu(false);
        require(createScreens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CREATE_WORLD, false),
                "Create World route should open before loading");
        createScreens.startLoadingNewGame();
        EchoClientScreenSnapshot creating = createScreens.snapshot(false);
        require(creating.loading(), "New Game should publish a ScreenCore loading snapshot");
        require(creating.state() == EchoClientGameState.MOD_SCAN,
                "New Game loading should begin with AdapterCore mod scanning");
        require(!creating.tooltip().isBlank(),
                "New Game loading snapshot should publish a deterministic ScreenCore tip");
        EchoClientUiVisualPlan creatingVisuals = new EchoClientUiRenderer().planVisuals(1280, 720, creating);
        require(creatingVisuals.loadingTipVisible(),
                "New Game loading renderer should plan the tip for the current stage");
        require(creatingVisuals.loadingTipKey().equals("loading.mod_scan"),
                "New Game loading renderer should expose the mod scan tip key");
        require(creating.subtitle().contains("Generating Ashfall crash site")
                        || creating.subtitle().contains("AdapterCore Pack Ashfall Live"),
                "New Game loading detail should identify the Ashfall generator path");
        advanceLoading(createScreens);
        require(createScreens.state() == EchoClientGameState.IN_GAME,
                "New Game loading should finish into gameplay state");

        EchoClientRuntimeServices savedClient = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        savedClient.startNewWorld("loading-flow");
        String slotId = savedClient.worldSession().slotId();
        String slotName = savedClient.worldSession().displayName();
        savedClient.unloadWorld();

        EchoClientScreenController loadScreens = new EchoClientScreenController();
        loadScreens.showMainMenu(true);
        require(loadScreens.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORLD_SELECT, true),
                "Load Game should open the World Select route");
        loadScreens.updateSaveSlots(savedClient.saveSlotSummaries(), savedClient.saveSlotError());
        require(loadScreens.selectedSaveSlotId().equals(slotId),
                "World Select should default to the saved slot before loading");
        require(loadScreens.selectedSaveSlotLabel().equals(slotName),
                "World Select should expose the selected save label for loading detail");
        loadScreens.startLoadingSavedWorld(loadScreens.selectedSaveSlotLabel());
        EchoClientScreenSnapshot restoring = loadScreens.snapshot(true);
        require(restoring.loading(), "Continue should publish a ScreenCore loading snapshot");
        require(!restoring.tooltip().isBlank(),
                "Continue loading snapshot should publish a deterministic ScreenCore tip");
        EchoClientUiVisualPlan restoringVisuals = new EchoClientUiRenderer().planVisuals(1280, 720, restoring);
        require(restoringVisuals.loadingTipVisible(),
                "Continue loading renderer should plan the tip for the current stage");
        require(restoringVisuals.screenCoreRouteId().equals("echoscreencore:loading"),
                "Continue loading renderer should keep the ScreenCore loading route");
        require(restoring.subtitle().contains("Restoring chunks and player state")
                        || restoring.subtitle().contains(slotName),
                "Continue loading detail should identify the saved-world restore path");
        advanceUntil(loadScreens, EchoClientGameState.LOADING_WORLD);
        EchoClientScreenSnapshot restoringWorld = loadScreens.snapshot(true);
        require(restoringWorld.tooltip().equals("TIP CHUNKS RESTORE CAMP STATE"),
                "Continue loading should switch to the world restore tip on the world stage");
        require(new EchoClientUiRenderer().planVisuals(1280, 720, restoringWorld)
                        .loadingTipKey().equals("loading.world"),
                "Continue loading renderer should expose the world restore tip key");
        advanceLoading(loadScreens);
        require(savedClient.continueFromSlot(slotId),
                "Saved-world restore should load successfully after loading completes");
        require(savedClient.hasActiveWorld(), "Saved-world restore should attach an active world session");
        require(savedClient.worldSession().slotId().equals(slotId),
                "Saved-world restore should preserve the selected slot id");

        System.out.println("client loading flow smoke PASS slot=" + slotId);
    }

    private static void advanceLoading(EchoClientScreenController screens) {
        for (int tick = 0; tick < 240 && screens.state() != EchoClientGameState.IN_GAME; tick++) {
            screens.updateLoading(1.0D / 20.0D);
        }
    }

    private static void advanceUntil(EchoClientScreenController screens, EchoClientGameState state) {
        for (int tick = 0; tick < 240 && screens.state() != state; tick++) {
            screens.updateLoading(1.0D / 20.0D);
        }
        require(screens.state() == state, "Loading flow should reach " + state);
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
}
