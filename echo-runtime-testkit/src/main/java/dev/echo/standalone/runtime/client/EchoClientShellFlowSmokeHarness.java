package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientShellFlowSmokeHarness {
    private EchoClientShellFlowSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-shell-flow-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        screens.updateScreenCatalog(services.screenCatalog());
        screens.updateSaveSlots(services.saveSlotSummaries(), services.saveSlotError());

        screens.showMainMenu(services.hasContinuableSession());
        EchoClientScreenSnapshot title = screens.snapshot(services.hasContinuableSession());
        require(title.state() == EchoClientGameState.MAIN_MENU, "Client should boot to the main menu shell");
        require(title.kind() == EchoClientScreenKind.MAIN_MENU, "Main menu should use the ScreenCore title route");
        EchoClientUiVisualPlan titleVisuals = new EchoClientUiRenderer().planVisuals(1280, 720, title);
        require(titleVisuals.mainMenuPanorama(),
                "Main menu should plan the Ashfall panorama treatment");
        require(titleVisuals.panoramaLayerCount() >= 6,
                "Main menu panorama should include sky, horizon, terrain, crash, beacon, and ash layers");
        require(titleVisuals.screenCoreRouteId().equals("echoscreencore:main_menu"),
                "Main menu panorama should be tied to the ScreenCore main menu route");
        require(optionEnabled(title, EchoClientScreenCommand.OPEN_CREATE_WORLD),
                "New Game route should be enabled on a fresh title");
        require(!optionEnabled(title, EchoClientScreenCommand.CONTINUE_GAME),
                "Continue should be disabled before a save exists");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CREATE_WORLD, false),
                "New Game should open the Create World shell");
        EchoClientScreenSnapshot createWorld = screens.snapshot(false);
        require(createWorld.kind() == EchoClientScreenKind.CREATE_WORLD,
                "Create World should be a ScreenCore route before loading starts");
        selectLabel(screens, "World Name", false);
        require(screens.activateSelection(false) == EchoClientScreenCommand.NONE,
                "World Name should edit through the ScreenCore text field");
        replaceActiveText(screens, "Cobalt Basin", false);
        require(screens.worldName().equals("Cobalt Basin"),
                "Create World should expose the edited world name before loading starts");
        selectCommand(screens, EchoClientScreenCommand.START_NEW_GAME, false);
        require(screens.activateSelection(false) == EchoClientScreenCommand.NONE,
                "Create World should ask for confirmation before starting");
        require(screens.snapshot(false).modal().visible(),
                "Create World confirmation modal should be visible");
        require(screens.snapshot(false).modal().message().contains("Cobalt Basin"),
                "Create World confirmation should name the pending save slot");
        require(screens.confirmModalSelection() == EchoClientScreenCommand.START_NEW_GAME,
                "Create World confirmation should emit START_NEW_GAME");

        require(worldSessions.beginNewWorldLoad(), "World session controller should begin New Game loading");
        EchoClientScreenSnapshot loading = screens.snapshot(false);
        require(loading.loading(), "New Game should publish a loading snapshot");
        require(loading.state() == EchoClientGameState.MOD_SCAN,
                "New Game should begin by scanning Native Loader/AdapterCore modules");
        EchoClientUiVisualPlan loadingVisuals = new EchoClientUiRenderer().planVisuals(1280, 720, loading);
        require(loadingVisuals.loadingTipVisible(),
                "New Game loading should plan a visible deterministic loading tip");
        require(loadingVisuals.loadingTipKey().equals("loading.mod_scan"),
                "New Game loading tip should be keyed to the current loading stage");
        advanceLoading(screens);
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Loading controller should complete into IN_GAME");
        require(worldSessions.finishPendingWorldLoad().sessionAttached(),
                "World session controller should attach the created world after loading");

        require(services.hasActiveWorld(), "Runtime services should attach an active world after load completion");
        String slotId = services.worldSession().slotId();
        String slotName = services.worldSession().displayName();
        require(!slotId.isBlank(), "Active world should have a save slot id");
        require(slotId.contains("cobalt-basin"),
                "Named Create World flow should produce a named save slot id");
        require(slotName.equals("Cobalt Basin"),
                "Named Create World flow should persist the edited save display name");
        requireColdStartNamedWorldSelect(saveRoot, slotId, slotName);

        screens.showPauseMenu();
        EchoClientScreenSnapshot pause = screens.snapshot(services.hasActiveWorld());
        require(pause.state() == EchoClientGameState.PAUSED, "Esc should route gameplay into the pause shell");
        require(optionEnabled(pause, EchoClientScreenCommand.RESUME_GAME),
                "Pause menu should expose Resume");
        require(screens.escapeCommand() == EchoClientScreenCommand.RESUME_GAME,
                "Esc on the pause menu should emit Resume");
        require(worldSessions.resumeOrTitle(), "World session controller should resume an active world");
        require(screens.state() == EchoClientGameState.IN_GAME, "Resume should return to the active world");
        require(services.hasActiveWorld(), "Resume should keep the runtime world attached");
        require(services.worldSession().slotId().equals(slotId), "Resume should preserve the exact save slot");

        screens.showPauseMenu();
        selectCommand(screens, EchoClientScreenCommand.QUIT_TO_TITLE, true);
        require(screens.activateSelection(true) == EchoClientScreenCommand.NONE,
                "Quit To Title should require confirmation");
        EchoClientScreenSnapshot quitModal = screens.snapshot(true);
        require(quitModal.modal().visible(), "Quit To Title confirmation modal should be visible");
        require(quitModal.modal().title().equals("QUIT TO TITLE"),
                "Quit To Title modal should identify the destructive shell transition");
        require(screens.confirmModalSelection() == EchoClientScreenCommand.QUIT_TO_TITLE,
                "Quit To Title confirmation should emit QUIT_TO_TITLE");

        worldSessions.quitToTitle();
        require(!services.hasActiveWorld(), "Quit To Title should unload the active world session");
        EchoClientScreenSnapshot returnedTitle = screens.snapshot(services.hasContinuableSession());
        require(returnedTitle.state() == EchoClientGameState.MAIN_MENU,
                "Quit To Title should return to the title shell");
        require(optionEnabled(returnedTitle, EchoClientScreenCommand.CONTINUE_GAME),
                "Returned title should keep Continue enabled from the saved session");

        require(worldSessions.beginContinueWorldLoad(),
                "World session controller should begin Continue loading from the title");
        require(screens.snapshot(true).loading(), "Continue should publish a saved-world loading snapshot");
        advanceLoading(screens);
        require(worldSessions.finishPendingWorldLoad().sessionAttached(),
                "World session controller should attach the continued world after loading");
        require(services.hasActiveWorld(), "Continue should attach an active world session");
        require(services.worldSession().slotId().equals(slotId),
                "Continue should restore the same save slot after Quit To Title");

        System.out.println("client shell flow smoke PASS slot=" + slotId + " titleContinue=true restored=true");
    }

    private static void requireColdStartNamedWorldSelect(Path saveRoot, String slotId, String slotName) {
        EchoClientRuntimeServices coldServices = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        require(!coldServices.hasMemorySave(),
                "Cold-start named slot check should prove it is not using an in-memory snapshot");
        require(coldServices.hasContinuableSession(),
                "Cold-start runtime services should discover the named save from disk");
        List<EchoClientSaveSlotSummary> coldSlots = coldServices.saveSlotSummaries();
        require(coldSlots.stream().anyMatch(slot -> slot.slotId().equals(slotId)
                        && slot.displayName().equals(slotName)
                        && slot.loadableInMemory()),
                "Cold-start World Select should list the named disk slot as loadable");

        EchoClientScreenController worldSelect = new EchoClientScreenController();
        worldSelect.showMainMenu(coldServices.hasContinuableSession());
        require(worldSelect.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORLD_SELECT, false),
                "Cold-start title should open World Select without an active gameplay session");
        worldSelect.updateSaveSlots(coldSlots, coldServices.saveSlotError());
        require(worldSelect.selectedSaveSlotId().equals(slotId),
                "Cold-start World Select should select the named disk slot");
        require(worldSelect.selectedSaveSlotLabel().equals(slotName),
                "Cold-start World Select should show the named disk slot label");
        require(coldServices.continueFromSlot(worldSelect.selectedSaveSlotId()),
                "Cold-start World Select should restore the named disk slot");
        require(coldServices.worldSession().slotId().equals(slotId)
                        && coldServices.worldSession().displayName().equals(slotName),
                "Cold-start restore should preserve the named save slot identity");
    }

    private static boolean optionEnabled(EchoClientScreenSnapshot snapshot, EchoClientScreenCommand command) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command && option.enabled());
    }

    private static void selectCommand(
            EchoClientScreenController screens,
            EchoClientScreenCommand command,
            boolean hasSession
    ) {
        for (int attempt = 0; attempt < 64; attempt++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(hasSession);
            if (!snapshot.options().isEmpty()
                    && snapshot.options().get(snapshot.selectedIndex()).command() == command) {
                return;
            }
            screens.moveSelection(1, hasSession, 720);
        }
        throw new AssertionError("Could not select command " + command);
    }

    private static void selectLabel(
            EchoClientScreenController screens,
            String label,
            boolean hasSession
    ) {
        for (int attempt = 0; attempt < 64; attempt++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(hasSession);
            if (!snapshot.options().isEmpty()
                    && snapshot.options().get(snapshot.selectedIndex()).label().equals(label)) {
                return;
            }
            screens.moveSelection(1, hasSession, 720);
        }
        throw new AssertionError("Could not select label " + label);
    }

    private static void replaceActiveText(
            EchoClientScreenController screens,
            String value,
            boolean hasSession
    ) {
        for (int index = 0; index < 64; index++) {
            screens.handleTextInput("", true, hasSession);
        }
        screens.handleTextInput(value, false, hasSession);
        screens.stopTextEditing(hasSession);
    }

    private static void advanceLoading(EchoClientScreenController screens) {
        for (int tick = 0; tick < 240 && screens.state() != EchoClientGameState.IN_GAME; tick++) {
            screens.updateLoading(1.0D / 20.0D);
        }
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
