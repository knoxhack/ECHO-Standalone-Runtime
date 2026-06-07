package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientScreenRuntimeControllerSmokeHarness {
    private EchoClientScreenRuntimeControllerSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-screen-runtime-controller-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSettingsController settings = new EchoClientSettingsController(
                screens,
                new EchoClientSettingsStore(saveRoot.resolve("client-options.properties")),
                new RecordingSettingsHost()
        );
        EchoClientScreenRuntimeController screenRuntime =
                new EchoClientScreenRuntimeController(services, screens, settings);

        screenRuntime.showInitialMainMenu();
        EchoClientScreenSnapshot title = screens.snapshot(services.hasContinuableSession());
        require(title.state() == EchoClientGameState.MAIN_MENU,
                "Screen runtime controller should publish the initial title route");
        require(!optionEnabled(title, EchoClientScreenCommand.CONTINUE_GAME),
                "Initial title should reflect that no save slot is continuable yet");
        require(services.modScanSummary().descriptorCount() > 0,
                "Screen runtime controller should have source-backed module scan data available");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MODS, false),
                "Mods route should open after screen runtime refresh");
        EchoClientScreenSnapshot mods = screens.snapshot(false);
        require(optionLabelPrefix(mods, "Module Scan: "),
                "Mods route should expose the scanner summary published by the runtime controller");
        require(optionLabelPrefix(mods, "Mod echoashfallprotocol"),
                "Mods route should expose source-backed module descriptor rows");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_DIAGNOSTICS, false),
                "Diagnostics route should open before an active world exists");
        EchoClientScreenSnapshot coldDiagnostics = screens.snapshot(false);
        require(optionLabel(coldDiagnostics, "World: No active world"),
                "Diagnostics route should expose an explicit no-active-world row before gameplay starts");

        services.startNewWorld("screen-runtime-controller");
        String slotId = services.worldSession().slotId();
        services.session().quickMoveContainerSlotToPlayer(1);
        placeSecondMachineNetwork(services.session());
        require(services.session().tickMachines(40) >= 1,
                "Screen runtime smoke should produce second-network compressed scrap for extraction controls");
        screenRuntime.refreshRuntimeSurfaces();

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORKBENCH, true),
                "Screen runtime controller should publish workbench recipes before the Workbench route opens");
        require(!screens.selectedWorkbenchRecipeId().isBlank(),
                "Workbench route should select a runtime recipe from refreshed screen data");

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MACHINE, true),
                "Screen runtime controller should publish the Machine ScreenCore route");
        EchoClientScreenSnapshot machine = screens.snapshot(true);
        require(machine.kind() == EchoClientScreenKind.MACHINE,
                "Machine route should open as a ScreenCore machine surface");
        require(optionLabelPrefix(machine, "AdapterCore Machines: "),
                "Machine route should expose AdapterCore machine contract counts");
        require(optionLabelPrefix(machine, "Machine State: "),
                "Machine route should expose AdapterCore machine state payloads");
        require(optionLabelPrefix(machine, "Recipe Progress: "),
                "Machine route should expose recipe progress telemetry");
        require(optionLabelPrefix(machine, "Power Graph: "),
                "Machine route should expose AdapterCore power graph telemetry");
        require(optionLabelPrefix(machine, "Block Entities: "),
                "Machine route should expose coordinate-backed block entity telemetry");
        require(optionLabelPrefix(machine, "Block Entity scrap_press @ "),
                "Machine route should expose placed scrap_press block entity coordinates");
        require(optionLabelPrefix(machine, "Machine Diagnostics: 1"),
                "Machine route should expose machine reconciliation diagnostic counts");
        require(optionLabelPrefix(machine, "Machine Diagnostic: multi-network machine graphs connected=2"),
                "Machine route should surface multi-network machine diagnostics through ScreenCore");
        require(optionLabelPrefix(machine, "Block Entity scrap_press@24,5,9 @ 24,5,9"),
                "Machine route should expose placed second scrap_press block entity coordinates");
        require(optionLabelPrefix(machine, "Port scrap_press/input: "),
                "Machine route should expose AdapterCore inventory port payloads");
        require(optionLabelPrefix(machine, "Port scrap_press@24,5,9/input: "),
                "Machine route should expose second machine inventory port payloads");
        require(optionTarget(machine, EchoClientScreenCommand.INSERT_MACHINE_INPUT, "scrap_press@24,5,9"),
                "Machine route should expose per-instance player input insertion controls");
        require(optionTarget(machine, EchoClientScreenCommand.EXTRACT_MACHINE_OUTPUT, "ore_grinder@26,5,9"),
                "Machine route should expose per-instance compressed-scrap extraction controls");
        require(optionLabelPrefix(machine, "Selected Recipe scrap_press@24,5,9: Compressed Scrap"),
                "Machine route should expose selected recipe rows for machine instances");
        require(optionTarget(
                        machine,
                        EchoClientScreenCommand.SELECT_MACHINE_RECIPE,
                        "scrap_press@24,5,9|echoashfallprotocol:scrap_press/dense_compressed_scrap"
                ),
                "Machine route should expose alternate recipe selection controls for machine instances");
        require(optionLabelPrefix(machine, "Machine Containers: "),
                "Machine route should expose container-backed machine slot counts");
        require(optionLabelPrefix(machine, "Slot scrap_press@24,5,9/input: echoashfallprotocol:scrap_metal"),
                "Machine route should expose second machine input slot payloads");
        require(optionLabelPrefix(machine, "Runtime Machine Rows: "),
                "Machine route should expose runtime machine content counts");
        require(optionCommand(machine, EchoClientScreenCommand.OPEN_WORKBENCH),
                "Machine route should bridge back into the refreshed workbench route");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Machine Back should be handled by the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.PAUSE_MENU,
                "Machine Back should return to the pause menu when opened from pause");

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_TERMINAL, true),
                "Screen runtime controller should publish the Terminal ScreenCore route");
        EchoClientScreenSnapshot terminal = screens.snapshot(true);
        require(terminal.kind() == EchoClientScreenKind.TERMINAL,
                "Terminal route should open as a ScreenCore terminal surface");
        require(optionLabelPrefix(terminal, "AdapterCore UI Screens: "),
                "Terminal route should expose AdapterCore UI screen counts");
        require(optionLabelPrefix(terminal, "AdapterCore Commands: "),
                "Terminal route should expose AdapterCore command counts");
        require(optionLabelPrefix(terminal, "Terminal Payload: "),
                "Terminal route should expose AdapterCore terminal payload state");
        require(optionLabelPrefix(terminal, "Terminal Commands: "),
                "Terminal route should expose mounted terminal command payload counts");
        require(optionLabelPrefix(terminal, "Command mission: "),
                "Terminal route should expose Ashfall mission command payloads");
        require(optionLabelPrefix(terminal, "Terminal Action: scan_target:"),
                "Terminal route should expose AdapterCore dashboard action payloads");
        require(optionTargetContains(terminal, EchoClientScreenCommand.OPEN_REGISTERED_SCREEN, "terminal"),
                "Terminal route should bridge into a registered AdapterCore terminal screen");
        EchoClientScreenCommand terminalRoute = screens.activateSelection(true);
        require(terminalRoute == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                "Terminal primary action should request the registered AdapterCore route");
        require(screens.executeNavigationCommand(terminalRoute, true),
                "Terminal registered route command should be handled by the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.REGISTERED_SCREEN,
                "Terminal primary action should open a registered ScreenCore screen");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Registered terminal Back should be handled by the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.TERMINAL,
                "Registered terminal Back should return to the terminal surface");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Terminal Back should be handled by the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.PAUSE_MENU,
                "Terminal Back should return to the pause menu when opened from pause");

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_RESOURCE_PACKS, true),
                "Resource Packs route should open from refreshed screen data");
        EchoClientScreenSnapshot packs = screens.snapshot(true);
        if (services.resourcePackSummaries().isEmpty()) {
            require(optionLabel(packs, "No Resource Packs Found"),
                    "Resource Pack route should reflect an empty runtime pack scan");
        } else {
            require(optionCommand(packs, EchoClientScreenCommand.OPEN_RESOURCE_PACK_DETAIL),
                    "Resource Pack route should expose mounted runtime pack details");
        }

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_DIAGNOSTICS, true),
                "Diagnostics route should open from an active world");
        screenRuntime.refreshRuntimeSurfaces();
        EchoClientScreenSnapshot liveDiagnostics = screens.snapshot(true);
        require(optionLabel(liveDiagnostics, "World Slot: " + slotId),
                "Diagnostics route should expose the active world slot id");
        require(optionLabelPrefix(liveDiagnostics, "World Name: "),
                "Diagnostics route should expose the active world display name");
        require(optionLabelPrefix(liveDiagnostics, "Chunks Loaded: "),
                "Diagnostics route should expose loaded and cached chunk counts");
        require(optionLabelPrefix(liveDiagnostics, "Biome: "),
                "Diagnostics route should expose current biome information");
        require(optionLabelPrefix(liveDiagnostics, "Hazard: "),
                "Diagnostics route should expose current hazard information");
        require(optionLabelPrefix(liveDiagnostics, "Vitals: "),
                "Diagnostics route should expose player vitals");
        require(optionLabelPrefix(liveDiagnostics, "Entities: "),
                "Diagnostics route should expose entity counts");
        require(optionLabelPrefix(liveDiagnostics, "Items: Drops "),
                "Diagnostics route should expose dropped item counts");
        require(optionLabelPrefix(liveDiagnostics, "Item Physics: Steps "),
                "Diagnostics route should expose dropped item physics counters");
        require(optionLabelPrefix(liveDiagnostics, "Renderer: Full Chunks "),
                "Diagnostics route should expose renderer mesh and upload counters");
        require(optionLabelPrefix(liveDiagnostics, "Atlas: Rebuilds "),
                "Diagnostics route should expose atlas cache counters");
        require(optionLabelPrefix(liveDiagnostics, "Machines: Block Entities "),
                "Diagnostics route should expose machine block entity counts");

        services.unloadWorld();
        screenRuntime.refreshRuntimeSurfaces();
        screens.showMainMenu(services.hasContinuableSession());
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORLD_SELECT, true),
                "World Select route should open after screen runtime refresh");
        screenRuntime.refreshRuntimeSurfaces();
        require(screens.selectedSaveSlotId().equals(slotId),
                "World Select should select the saved slot published by the screen runtime controller");

        System.out.println("client screen runtime controller smoke PASS slot=" + slotId
                + " resourcePacks=" + services.resourcePackSummaries().size()
                + " recipe=" + !screens.selectedWorkbenchRecipeId().isBlank());
    }

    private static boolean optionEnabled(EchoClientScreenSnapshot snapshot, EchoClientScreenCommand command) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command && option.enabled());
    }

    private static boolean optionCommand(EchoClientScreenSnapshot snapshot, EchoClientScreenCommand command) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command);
    }

    private static boolean optionLabel(EchoClientScreenSnapshot snapshot, String label) {
        return snapshot.options().stream()
                .anyMatch(option -> option.label().equals(label));
    }

    private static boolean optionLabelPrefix(EchoClientScreenSnapshot snapshot, String labelPrefix) {
        return snapshot.options().stream()
                .anyMatch(option -> option.label().startsWith(labelPrefix));
    }

    private static boolean optionTargetContains(
            EchoClientScreenSnapshot snapshot,
            EchoClientScreenCommand command,
            String targetFragment
    ) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command && option.targetId().contains(targetFragment));
    }

    private static boolean optionTarget(
            EchoClientScreenSnapshot snapshot,
            EchoClientScreenCommand command,
            String targetId
    ) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command && option.targetId().equals(targetId));
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
                "Screen runtime smoke should reconcile two complete machine networks");
        require(session.world().blockStateAt(24, 5, 9).property("blockEntityId").orElse("").equals("scrap_press"),
                "Second machine placement should carry canonical block entity identity before reconciliation");
    }

    private static void placeMachineBlock(EchoClientGameSession session, String blockId, int x) {
        EchoVoxelBlock block = session.bridge().registry().requireLiveVoxelBlock(blockId);
        EchoVoxelBlockState state = session.defaultBlockStateFor(block);
        require(session.world().setBlockStateAt(x, 5, 9, state),
                "Screen runtime smoke should place machine block " + blockId);
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

    private static final class RecordingSettingsHost implements EchoClientSettingsController.Host {
        @Override
        public void applyInputSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyAudioSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged) {
        }

        @Override
        public void applyWindowSettings(boolean fullscreen, boolean vSync) {
        }

        @Override
        public void settingsSaveFailed(Path path, String error) {
            throw new AssertionError("Settings save should not fail in screen runtime smoke: " + error);
        }
    }
}
