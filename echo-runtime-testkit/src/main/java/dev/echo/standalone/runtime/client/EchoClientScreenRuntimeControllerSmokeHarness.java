package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.audio.EchoAudioDeviceSettings;
import dev.echo.standalone.runtime.audio.EchoJavaSoundAudioBackend;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientScreenRuntimeControllerSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports/echo/standalone/client-machine-terminal-surfaces.json");

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
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(new EchoJavaSoundAudioBackend(EchoAudioDeviceSettings.forcedFallback()));
        audio.playUiClick();
        screenRuntime.attachAudio(audio);

        screenRuntime.showInitialMainMenu();
        EchoClientScreenSnapshot title = screens.snapshot(services.hasContinuableSession());
        require(title.state() == EchoClientGameState.MAIN_MENU,
                "Screen runtime controller should publish the initial title route");
        require(title.options().size() == 5,
                "Title menu should stay limited to player-facing primary actions");
        require(optionCommand(title, EchoClientScreenCommand.OPEN_OPTIONS),
                "Title menu should expose Options as the settings gateway");
        require(!optionCommand(title, EchoClientScreenCommand.OPEN_MODS),
                "Title menu should keep Mods off the idle hot path");
        require(!optionCommand(title, EchoClientScreenCommand.OPEN_RESOURCE_PACKS),
                "Title menu should keep Resource Packs off the idle hot path");
        EchoClientUiVisualPlan titleVisuals = new EchoClientUiRenderer().planVisuals(1280, 720, title);
        require(titleVisuals.mainMenuPanorama(),
                "Title menu should still use the Ashfall main-menu panorama plan");
        require(titleVisuals.panoramaLineBudget() <= 20,
                "Title menu panorama should keep idle line primitives within the lightweight frame budget");
        int initialRefreshCount = screenRuntime.surfaceRefreshCount();
        require(initialRefreshCount == 1,
                "Initial title should refresh runtime surfaces once before entering the idle menu loop");
        long titleSnapshotBuilds = screens.snapshotBuildCount();
        long titleSnapshotCacheHits = screens.snapshotCacheHitCount();
        for (int tick = 0; tick < 30; tick++) {
            require(!screenRuntime.refreshRuntimeSurfacesIfNeeded(),
                    "Idle title menu should not rescan runtime surfaces every client tick");
        }
        EchoClientScreenSnapshot cachedTitle = screens.snapshot(services.hasContinuableSession());
        require(cachedTitle == title,
                "Idle title menu should reuse a stable ScreenCore snapshot between input or state changes");
        require(screens.snapshotBuildCount() == titleSnapshotBuilds,
                "Idle title menu should not rebuild ScreenCore snapshots between input or state changes");
        require(screens.snapshotCacheHitCount() == titleSnapshotCacheHits + 1,
                "Idle title menu should record a ScreenCore snapshot cache hit");
        require(screenRuntime.surfaceRefreshCount() == initialRefreshCount,
                "Idle title menu should keep the surface refresh count stable between passive refresh intervals");
        int lightweightTitleRefreshCount = screenRuntime.lightweightTitleRefreshCount();
        int remainingPassiveTicks = EchoClientScreenRuntimeController.PASSIVE_SURFACE_REFRESH_INTERVAL_TICKS - 30;
        for (int tick = 0; tick < remainingPassiveTicks; tick++) {
            require(!screenRuntime.refreshRuntimeSurfacesIfNeeded(),
                    "Idle title menu should keep full runtime scans parked until the passive refresh boundary");
        }
        require(screenRuntime.refreshRuntimeSurfacesIfNeeded(),
                "Idle title menu should still record a passive title heartbeat at the refresh boundary");
        require(screenRuntime.surfaceRefreshCount() == initialRefreshCount,
                "Passive title heartbeat should not rescan save slots, modules, resource packs, or workbench surfaces");
        require(screenRuntime.lightweightTitleRefreshCount() == lightweightTitleRefreshCount + 1,
                "Passive title heartbeat should be tracked separately from full runtime surface refreshes");
        long idlePointerOptionBuilds = screens.optionListBuildCount();
        for (int tick = 0; tick < 60; tick++) {
            require(screens.handlePointer(-100.0D, -100.0D, false, false, 1280, 720, false)
                            == EchoClientScreenCommand.NONE,
                    "Idle title pointer frames outside the menu should not trigger commands");
        }
        require(screens.optionListBuildCount() == idlePointerOptionBuilds,
                "Idle title pointer frames should reuse published menu options instead of rebuilding rows");
        long titleOptionBuilds = screens.optionListBuildCount();
        screens.moveSelection(1, services.hasContinuableSession(), 720);
        require(screens.optionListBuildCount() == titleOptionBuilds + 1,
                "Title menu keyboard navigation should build option rows once per key press");
        EchoClientScreenSnapshot movedTitle = screens.snapshot(services.hasContinuableSession());
        require(movedTitle.selectedIndex() == 2,
                "Title menu keyboard navigation should still move selection to Load Game after the idle cache pass");
        require(!optionEnabled(title, EchoClientScreenCommand.CONTINUE_GAME),
                "Initial title should reflect that no save slot is continuable yet");
        require(services.modScanSummary().descriptorCount() > 0,
                "Screen runtime controller should have source-backed module scan data available");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_OPTIONS, false),
                "Options route should open from the slim title menu");
        EchoClientScreenSnapshot options = screens.snapshot(false);
        require(optionCommand(options, EchoClientScreenCommand.OPEN_MODS),
                "Options route should retain Mods access outside the idle title path");
        require(optionCommand(options, EchoClientScreenCommand.OPEN_RESOURCE_PACKS),
                "Options route should retain Resource Packs access outside the idle title path");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, false),
                "Options Back should return to the title menu before opening diagnostics surfaces");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MODS, false),
                "Mods route should open after screen runtime refresh");
        require(screenRuntime.refreshRuntimeSurfacesIfNeeded(),
                "Screen runtime controller should refresh surfaces immediately when the ScreenCore route changes");
        int modsRefreshCount = screenRuntime.surfaceRefreshCount();
        require(!screenRuntime.refreshRuntimeSurfacesIfNeeded(),
                "Unchanged Mods route should also stay idle between passive refresh intervals");
        require(screenRuntime.surfaceRefreshCount() == modsRefreshCount,
                "Unchanged Mods route should not rescan module and resource surfaces on consecutive ticks");
        EchoClientScreenSnapshot mods = screens.snapshot(false);
        require(optionLabelPrefix(mods, "Module Scan: "),
                "Mods route should expose the scanner summary published by the runtime controller");
        require(optionLabelPrefix(mods, "Mod echoashfallprotocol"),
                "Mods route should expose source-backed module descriptor rows");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_DIAGNOSTICS, false),
                "Diagnostics route should open before an active world exists");
        EchoClientScreenSnapshot coldDiagnostics = screens.snapshot(false);
        require(optionCommand(coldDiagnostics, EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE),
                "Diagnostics route should expose a player-facing support bundle export action");
        require(optionLabel(coldDiagnostics, "Support Bundle: Not exported"),
                "Diagnostics route should expose support bundle export status before a bundle is written");
        require(optionLabel(coldDiagnostics, "World: No active world"),
                "Diagnostics route should expose an explicit no-active-world row before gameplay starts");
        require(optionLabelPrefix(coldDiagnostics, "Audio: Backend echo:java_sound_audio Device FALLBACK"),
                "Cold Diagnostics route should expose forced audio fallback status");

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
        require(optionCommand(liveDiagnostics, EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE),
                "Live Diagnostics route should keep the support bundle export action available");
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
        require(optionLabelPrefix(liveDiagnostics, "Frame: Last "),
                "Diagnostics route should expose frame pacing counters");
        require(optionLabelPrefix(liveDiagnostics, "Audio: Backend echo:java_sound_audio Device FALLBACK"),
                "Diagnostics route should expose OpenGL client audio backend and fallback status");
        require(optionLabelPrefix(liveDiagnostics, "Audio Mix: Master "),
                "Diagnostics route should expose player-facing audio mix and subtitle state");
        require(optionLabelPrefix(liveDiagnostics, "Renderer: Full Chunks "),
                "Diagnostics route should expose renderer mesh and upload counters");
        require(optionLabelPrefix(liveDiagnostics, "Atlas: Rebuilds "),
                "Diagnostics route should expose atlas cache counters");
        require(optionLabelPrefix(liveDiagnostics, "Machines: Block Entities "),
                "Diagnostics route should expose machine block entity counts");

        screens.showFatalError(new IllegalStateException("Simulated OpenGL render failure"));
        EchoClientScreenSnapshot fatal = screens.snapshot(true);
        require(fatal.state() == EchoClientGameState.FATAL_ERROR,
                "Fatal runtime failures should publish a dedicated fatal error state");
        require(fatal.kind() == EchoClientScreenKind.FATAL_ERROR,
                "Fatal runtime failures should publish a dedicated ScreenCore fatal error route");
        require(fatal.title().equals("RUNTIME ERROR"),
                "Fatal runtime failures should use a clear player-facing title");
        require(fatal.footer().contains("echoscreencore:fatal_error"),
                "Fatal runtime failures should preserve the ScreenCore route id in the snapshot footer");
        require(optionCommand(fatal, EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE),
                "Fatal runtime failures should expose support bundle export directly");
        require(optionCommand(fatal, EchoClientScreenCommand.OPEN_DIAGNOSTICS),
                "Fatal runtime failures should expose Diagnostics directly");
        require(optionEnabled(fatal, EchoClientScreenCommand.QUIT_TO_TITLE),
                "Fatal runtime failures with an active world should allow return to title");
        require(optionCommand(fatal, EchoClientScreenCommand.QUIT_CLIENT),
                "Fatal runtime failures should allow the client to close");
        require(optionLabelPrefix(fatal, "Error: IllegalStateException"),
                "Fatal runtime failures should summarize the thrown exception");
        require(optionTooltipContains(fatal, "Simulated OpenGL render failure"),
                "Fatal runtime failures should preserve failure detail in option tooltips");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_DIAGNOSTICS, true),
                "Fatal runtime failures should allow Diagnostics inspection");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Diagnostics Back should return to the fatal error route");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.FATAL_ERROR,
                "Fatal error route should remain available after visiting Diagnostics");

        writeReport(slotId, titleVisuals, machine, terminal, liveDiagnostics, fatal, services, screenRuntime, screens);
        audio.close();

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

    private static boolean optionTooltipContains(EchoClientScreenSnapshot snapshot, String fragment) {
        return snapshot.options().stream()
                .anyMatch(option -> option.tooltip().contains(fragment));
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

    private static void writeReport(
            String slotId,
            EchoClientUiVisualPlan titleVisuals,
            EchoClientScreenSnapshot machine,
            EchoClientScreenSnapshot terminal,
            EchoClientScreenSnapshot diagnostics,
            EchoClientScreenSnapshot fatal,
            EchoClientRuntimeServices services,
            EchoClientScreenRuntimeController screenRuntime,
            EchoClientScreenController screens
    ) throws IOException {
        String json = """
                {
                  "schema": "echo.standalone.client_machine_terminal_surfaces.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientScreenRuntimeControllerSmokeHarness",
                  "status": "PASS",
                  "summary": "ScreenCore machine and terminal surfaces expose AdapterCore catalog counts, coordinate-backed machine state, power graph telemetry, inventory ports, per-instance recipe/input/output controls, registered terminal routes, terminal command payloads, live diagnostics, frame pacing counters, audio device/fallback diagnostics, fatal error routing, lightweight title-menu refreshes, and the player-facing OpenGL client support-bundle export action.",
                  "slotId": "%s",
                  "coverage": {
                    "titleMenuLightweightRefresh": true,
                    "titleMenuSnapshotCache": true,
                    "titleMenuLightweightPanoramaBudget": true,
                    "titleMenuSingleOptionBuildPerKeypress": true,
                    "titleMenuPointerIdleNoOptionRebuild": true,
                    "normalTitleMenuActions": true,
                    "machineScreenCoreRoute": true,
                    "machineAdapterCoreCounts": true,
                    "machineStatePayloadRows": true,
                    "machinePowerGraphTelemetry": true,
                    "machineBlockEntityCoordinates": true,
                    "multiNetworkMachineDiagnostics": true,
                    "machineInventoryPorts": true,
                    "perInstanceMachineInputControls": true,
                    "perInstanceMachineOutputControls": true,
                    "perInstanceRecipeSelection": true,
                    "containerBackedMachineSlots": true,
                    "terminalScreenCoreRoute": true,
                    "terminalAdapterCoreCounts": true,
                    "terminalCommandPayloads": true,
                    "registeredTerminalRoute": true,
                    "terminalBackStack": true,
                    "liveDiagnostics": true,
                    "framePacingCounters": true,
                    "audioDiagnostics": true,
                    "fatalErrorScreen": true,
                    "clientSupportBundleExport": true
                  },
                  "machine": {
                    "kind": "%s",
                    "optionCount": %d,
                    "adapterCoreMachineRows": %d,
                    "statePayloadRows": %d,
                    "powerGraphRows": %d,
                    "blockEntityRows": %d,
                    "portRows": %d,
                    "inputControlTargets": %d,
                    "outputControlTargets": %d,
                    "recipeSelectionTargets": %d,
                    "slotRows": %d,
                    "machineDiagnosticRows": %d
                  },
                  "terminal": {
                    "kind": "%s",
                    "optionCount": %d,
                    "adapterCoreUiRows": %d,
                    "adapterCoreCommandRows": %d,
                    "terminalPayloadRows": %d,
                    "terminalCommandRows": %d,
                    "terminalActionRows": %d,
                    "registeredRouteTargets": %d
                  },
                  "diagnostics": {
                    "kind": "%s",
                    "optionCount": %d,
                    "worldRows": %d,
                    "chunkRows": %d,
                    "biomeRows": %d,
                    "hazardRows": %d,
                    "machineRows": %d,
                    "framePacingRows": %d,
                    "audioRows": %d,
                    "supportBundleExportActions": %d,
                    "supportBundleStatusRows": %d
                  },
                  "fatalError": {
                    "kind": "%s",
                    "state": "%s",
                    "optionCount": %d,
                    "supportBundleExportActions": %d,
                    "diagnosticsActions": %d,
                    "quitToTitleActions": %d,
                    "quitClientActions": %d,
                    "errorRows": %d
                  },
                  "runtime": {
                    "modDescriptorCount": %d,
                    "resourcePackCount": %d,
                    "saveSlotCount": %d,
                    "fullSurfaceRefreshes": %d,
                    "lightweightTitleRefreshes": %d,
                    "titleMenuPanoramaLineBudget": %d,
                    "titleMenuPanoramaTerrainLayers": %d,
                    "titleMenuPanoramaAtmosphericStreaks": %d,
                    "screenSnapshotBuilds": %d,
                    "screenSnapshotCacheHits": %d,
                    "screenOptionListBuilds": %d
                  },
                  "nativeModLoaderCommandUsed": false
                }
                """.formatted(
                escape(slotId),
                machine.kind().name(),
                machine.options().size(),
                countLabelPrefix(machine, "AdapterCore Machines: "),
                countLabelPrefix(machine, "Machine State: "),
                countLabelPrefix(machine, "Power Graph: "),
                countLabelPrefix(machine, "Block Entity "),
                countLabelPrefix(machine, "Port "),
                countCommand(machine, EchoClientScreenCommand.INSERT_MACHINE_INPUT),
                countCommand(machine, EchoClientScreenCommand.EXTRACT_MACHINE_OUTPUT),
                countCommand(machine, EchoClientScreenCommand.SELECT_MACHINE_RECIPE),
                countLabelPrefix(machine, "Slot "),
                countLabelPrefix(machine, "Machine Diagnostic: "),
                terminal.kind().name(),
                terminal.options().size(),
                countLabelPrefix(terminal, "AdapterCore UI Screens: "),
                countLabelPrefix(terminal, "AdapterCore Commands: "),
                countLabelPrefix(terminal, "Terminal Payload: "),
                countLabelPrefix(terminal, "Terminal Commands: "),
                countLabelPrefix(terminal, "Terminal Action: "),
                countCommand(terminal, EchoClientScreenCommand.OPEN_REGISTERED_SCREEN),
                diagnostics.kind().name(),
                diagnostics.options().size(),
                countLabelPrefix(diagnostics, "World "),
                countLabelPrefix(diagnostics, "Chunks Loaded: "),
                countLabelPrefix(diagnostics, "Biome: "),
                countLabelPrefix(diagnostics, "Hazard: "),
                countLabelPrefix(diagnostics, "Machines: "),
                countLabelPrefix(diagnostics, "Frame: Last "),
                countLabelPrefix(diagnostics, "Audio"),
                countCommand(diagnostics, EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE),
                countLabelPrefix(diagnostics, "Support Bundle: "),
                fatal.kind().name(),
                fatal.state().name(),
                fatal.options().size(),
                countCommand(fatal, EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE),
                countCommand(fatal, EchoClientScreenCommand.OPEN_DIAGNOSTICS),
                countCommand(fatal, EchoClientScreenCommand.QUIT_TO_TITLE),
                countCommand(fatal, EchoClientScreenCommand.QUIT_CLIENT),
                countLabelPrefix(fatal, "Error: "),
                services.modScanSummary().descriptorCount(),
                services.resourcePackSummaries().size(),
                services.saveSlotSummaries().size(),
                screenRuntime.surfaceRefreshCount(),
                screenRuntime.lightweightTitleRefreshCount(),
                titleVisuals.panoramaLineBudget(),
                titleVisuals.panoramaTerrainLayers(),
                titleVisuals.panoramaAtmosphericStreaks(),
                screens.snapshotBuildCount(),
                screens.snapshotCacheHitCount(),
                screens.optionListBuildCount()
        );
        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(REPORT_PATH, json);
    }

    private static int countCommand(EchoClientScreenSnapshot snapshot, EchoClientScreenCommand command) {
        return (int) snapshot.options().stream()
                .filter(option -> option.command() == command)
                .count();
    }

    private static int countLabelPrefix(EchoClientScreenSnapshot snapshot, String labelPrefix) {
        return (int) snapshot.options().stream()
                .filter(option -> option.label().startsWith(labelPrefix))
                .count();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
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
