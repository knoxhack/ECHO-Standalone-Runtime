package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoClientWorldInteractionSmokeHarness {
    private EchoClientWorldInteractionSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoVoxelBlock cache = bridge.registry().requireLiveVoxelBlock("echoashfallprotocol:echo_cache");
        EchoVoxelBlock terminalBlock = bridge.registry().requireLiveVoxelBlock("echoterminal:echo_terminal");
        EchoVoxelBlock hazard = bridge.registry().requireLiveVoxelBlock("echoashfallprotocol:toxic_waste_barrel");
        EchoVoxelBlock power = bridge.registry().requireLiveVoxelBlock("echoashfallprotocol:power_node");
        EchoClientScreenRouteRequest terminalRoute = EchoClientGameplay.worldInteractionRouteFor(terminalBlock);

        require(EchoClientGameplay.worldInteractionCommandFor(cache) == EchoClientScreenCommand.OPEN_CONTAINER,
                "Crash cache should right-click into the ScreenCore container route");
        require(terminalRoute.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                "Field terminal should right-click into a registered AdapterCore ScreenCore route");
        require(terminalRoute.targetId().equals("echoterminal:ui/field_terminal"),
                "Field terminal should target the registered terminal AdapterCore content id");
        require(EchoClientGameplay.worldInteractionCommandFor(power) == EchoClientScreenCommand.OPEN_MACHINE,
                "Power node should right-click into the ScreenCore machine route");
        require(EchoClientGameplay.worldInteractionCommandFor(bridge.runtimeMarkerBlock()) == EchoClientScreenCommand.OPEN_WORKBENCH,
                "AdapterCore runtime marker should right-click into the ScreenCore workbench route");
        require(EchoClientGameplay.worldInteractionCommandFor(hazard) == EchoClientScreenCommand.NONE,
                "Toxic hazard blocks should not be misclassified as containers");
        require(EchoClientGameplay.worldInteractionCommandFor(EchoVoxelBlock.AIR) == EchoClientScreenCommand.NONE,
                "Air should not request a ScreenCore route");

        EchoClientScreenController screens = new EchoClientScreenController();
        screens.updateScreenCatalog(EchoClientScreenCatalog.loadDefault());
        screens.updateTechSurfaceModel(EchoClientTechSurfaceModel.from(bridge));
        screens.showPauseMenu();
        require(screens.executeNavigationCommand(
                        EchoClientGameplay.worldInteractionCommandFor(cache),
                        true),
                "Container interaction command should open through the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.CONTAINER,
                "Container interaction should land on the container screen kind");

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(
                        EchoClientGameplay.worldInteractionCommandFor(power),
                        true),
                "Machine interaction command should open through the ScreenCore controller");
        EchoClientScreenSnapshot machine = screens.snapshot(true);
        require(machine.kind() == EchoClientScreenKind.MACHINE,
                "Machine interaction should land on the machine screen kind");
        require(machine.footer().contains("echoscreencore:machine"),
                "Machine interaction should expose the ScreenCore machine route");
        require(machine.options().stream()
                        .anyMatch(option -> option.label().startsWith("AdapterCore Power: ")),
                "Machine interaction should expose AdapterCore power contract counts");
        require(machine.options().stream()
                        .anyMatch(option -> option.label().startsWith("Machine State: ")),
                "Machine interaction should expose AdapterCore machine state payloads");
        require(machine.options().stream()
                        .anyMatch(option -> option.label().startsWith("Power Graph: ")),
                "Machine interaction should expose AdapterCore power graph telemetry");
        require(machine.options().stream()
                        .anyMatch(option -> option.label().startsWith("Block Entities: ")),
                "Machine interaction should expose coordinate-backed block entity telemetry");
        require(machine.options().stream()
                        .anyMatch(option -> option.label().startsWith("Block Entity scrap_press @ ")),
                "Machine interaction should expose placed scrap_press block entity coordinates");

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(
                        EchoClientGameplay.worldInteractionCommandFor(bridge.runtimeMarkerBlock()),
                        true),
                "Workbench interaction command should open through the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.WORKBENCH,
                "Workbench interaction should land on the workbench screen kind");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Workbench Back should be handled by the ScreenCore controller");
        require(screens.snapshot(true).state() == EchoClientGameState.PAUSED,
                "Workbench Back should return to pause when opened from pause");

        screens.showPauseMenu();
        require(screens.openRegisteredScreen(
                        terminalRoute.targetId(),
                        true),
                "Terminal interaction command should open through the ScreenCore controller");
        EchoClientScreenSnapshot terminal = screens.snapshot(true);
        require(terminal.kind() == EchoClientScreenKind.REGISTERED_SCREEN,
                "Terminal interaction should land on a registered ScreenCore screen kind");
        require(terminal.title().equals("Field Terminal UI"),
                "Terminal interaction should expose the registered terminal screen title");
        require(terminal.options().stream()
                        .anyMatch(option -> option.label().contains("echoterminal:ui/field_terminal")),
                "Terminal interaction should expose the AdapterCore terminal content id");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Terminal Back should be handled by the ScreenCore controller");
        require(screens.snapshot(true).state() == EchoClientGameState.PAUSED,
                "Terminal Back should return to pause when opened from pause");

        screens.showInGame();
        require(screens.executeNavigationCommand(
                        EchoClientGameplay.worldInteractionCommandFor(power),
                        true),
                "Gameplay machine interaction should open through the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.MACHINE,
                "Gameplay machine interaction should land on the machine screen kind");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Gameplay machine Back should be handled by the ScreenCore controller");
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Gameplay machine Back should return directly to gameplay");

        screens.showInGame();
        require(screens.executeNavigationCommand(
                        EchoClientGameplay.worldInteractionCommandFor(bridge.runtimeMarkerBlock()),
                        true),
                "Gameplay workbench interaction should open through the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.WORKBENCH,
                "Gameplay workbench interaction should land on the workbench screen kind");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Gameplay workbench Back should be handled by the ScreenCore controller");
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Gameplay workbench Back should return directly to gameplay");

        screens.showInGame();
        require(screens.openRegisteredScreen(
                        terminalRoute.targetId(),
                        true),
                "Gameplay terminal interaction should open the registered ScreenCore controller route");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.REGISTERED_SCREEN,
                "Gameplay terminal interaction should land on a registered ScreenCore screen kind");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Gameplay terminal Back should be handled by the ScreenCore controller");
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Gameplay terminal Back should return directly to gameplay");

        writeSmokeReport();
        System.out.println("client world interaction smoke PASS cache=container terminal=registered machine=power workbench=runtime_marker");
    }

    private static void writeSmokeReport() throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "client-world-interaction-smoke.json").toAbsolutePath();
        Files.createDirectories(report.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_smoke.client-world-interaction-smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "runtime": "standalone",
                  "moduleIds": ["echoashfallprotocol", "echoterminal", "echoscreencore", "echoruntimehost"],
                  "featureBuckets": ["gui", "screen", "terminal", "blocks", "block_actions", "machines"],
                  "trustedMutations": [
                    "worldInteractionCommandFor:container",
                    "worldInteractionCommandFor:machine",
                    "worldInteractionCommandFor:workbench",
                    "openRegisteredScreen:echoterminal:ui/field_terminal"
                  ],
                  "visibleRoutes": [
                    "echoscreencore:container",
                    "echoscreencore:machine",
                    "echoscreencore:workbench",
                    "echoterminal:ui/field_terminal"
                  ],
                  "saveEvidence": [],
                  "networkEvidence": [],
                  "blockers": []
                }
                """;
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
