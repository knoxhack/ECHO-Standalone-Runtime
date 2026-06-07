package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputAction;
import dev.echo.standalone.runtime.input.EchoInputControl;
import dev.echo.standalone.runtime.input.EchoInputEvent;
import dev.echo.standalone.runtime.input.EchoInputRuntime;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.player.EchoPlayerCameraRig;
import dev.echo.standalone.runtime.player.EchoPlayerController;
import dev.echo.standalone.runtime.player.EchoPlayerControllerResult;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntime;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntimeResult;
import dev.echo.standalone.runtime.player.EchoPlayerControllerState;
import dev.echo.standalone.runtime.player.EchoPlayerFacing;
import dev.echo.standalone.runtime.player.EchoPlayerInteractionTargeter;
import dev.echo.standalone.runtime.player.EchoPlayerInventoryShortcuts;
import dev.echo.standalone.runtime.render.EchoRenderCamera;
import dev.echo.standalone.runtime.ui.EchoTerminalScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

public final class EchoRuntimePlayerControllerSmokeHarness {
    private static final EchoEntityId PLAYER_ID = new EchoEntityId("player-001");
    private static final EchoInventoryId PLAYER_PACK = new EchoInventoryId("inventory:player-001");
    private static final EchoItemId WATER_RATION = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);

    private EchoRuntimePlayerControllerSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoTerminalShell shell = new EchoTerminalShell();
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoTerminalScreen("terminal", "Ashfall Terminal", shell),
                EchoUiTheme.defaultTerminal()
        );
        EchoInputRuntimeResult input = new EchoInputRuntime().boot(services, ui, entities, gameplay, PLAYER_ID);
        EchoPlayerControllerRuntimeResult playerRuntime = new EchoPlayerControllerRuntime().boot(
                services,
                world,
                entities,
                gameplay,
                items,
                input,
                PLAYER_ID
        );
        EchoPlayerController controller = playerRuntime.controller();

        require(services.require(EchoPlayerControllerRuntimeResult.class) == playerRuntime,
                "player controller runtime result should be service-bound");
        require(services.require(EchoPlayerController.class) == controller,
                "player controller should be service-bound");
        require(services.require(EchoPlayerCameraRig.class) == playerRuntime.cameraRig(),
                "camera rig should be service-bound");
        require(services.require(EchoPlayerInteractionTargeter.class) == playerRuntime.targeter(),
                "targeter should be service-bound");
        require(services.require(EchoPlayerInventoryShortcuts.class) == playerRuntime.shortcuts(),
                "inventory shortcuts should be service-bound");

        EchoPlayerControllerState initial = controller.state();
        require(initial.player().worldPosition().equals(new EchoWorldPosition(0, 0, 0)),
                "player should start at the Ashfall terminal");
        require(initial.facing() == EchoPlayerFacing.EAST, "player should start facing east");
        requireCamera(initial.camera(), 0.5D, 3.0D, 0.5D, "initial camera");
        require(initial.target().orElseThrow().id().equals("ashfall:terminal_pod"),
                "initial target should be the terminal");
        require(initial.target().orElseThrow().exact(), "initial target should be exact");

        EchoPlayerControllerResult terminal = controller.handle(mouse(0, "PRIMARY"));
        require(terminal.handled(), "terminal interaction should be handled");
        require(terminal.action().orElseThrow() == EchoInputAction.POINTER_PRIMARY,
                "terminal input should resolve to interact");
        require(terminal.interaction().orElseThrow().interactionId().equals("ashfall:activate_terminal"),
                "controller should activate the exact terminal target");
        require(terminal.interaction().orElseThrow().objectiveCompleted(),
                "terminal interaction should complete terminal objective");

        EchoPlayerControllerResult water = controller.handle(key(1, "DIGIT1"));
        require(water.handled(), "quick slot should be handled");
        require(water.shortcut().orElseThrow().used(), "quick slot should consume water");
        require(water.shortcut().orElseThrow().itemId().equals(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID),
                "quick slot should report clean water bottle");
        require(items.operations().count(items.inventoryStore().require(PLAYER_PACK), WATER_RATION) == 1,
                "quick slot should leave one water ration");

        EchoPlayerControllerResult eastOne = controller.handle(key(2, "D"));
        requireMove(eastOne, "1,0,0", "first east move");
        require(eastOne.hazard().orElseThrow().intensity() == 0.72D,
                "first move should apply toxic ash feedback");
        require(eastOne.hazard().orElseThrow().healthDamage() == 4,
                "first move should damage suit seals");
        requireCamera(eastOne.state().camera(), 1.5D, 3.0D, 0.5D, "first east camera");

        EchoPlayerControllerResult eastTwo = controller.handle(key(3, "D"));
        requireMove(eastTwo, "2,0,0", "second east move");
        require(eastTwo.state().target().orElseThrow().id().equals("ashfall:crash_cache"),
                "nearby target should become the crash cache");
        require(!eastTwo.state().target().orElseThrow().exact(),
                "crash cache should not be exact until the player reaches it");

        EchoPlayerControllerResult southOne = controller.handle(key(4, "S"));
        requireMove(southOne, "2,0,1", "south move to cache");
        requireCamera(southOne.state().camera(), 2.5D, 3.0D, 1.5D, "cache camera");
        require(southOne.state().target().orElseThrow().id().equals("ashfall:crash_cache"),
                "cache should be targeted at the cache position");
        require(southOne.state().target().orElseThrow().exact(),
                "cache target should be exact at the cache position");
        require(southOne.state().hazard().orElseThrow().message().contains("Toxic ash"),
                "state should retain the latest hazard feedback");

        EchoPlayerControllerResult cache = controller.handle(mouse(5, "PRIMARY"));
        require(cache.handled(), "cache interaction should be handled");
        require(cache.interaction().orElseThrow().interactionId().equals("ashfall:salvage_cache"),
                "controller should salvage the exact cache target");
        require(gameplay.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "controller traversal should complete the Ashfall debug mission");
        require(gameplay.mission().completedObjectiveCount() == 3,
                "all three player-facing objectives should be complete");

        requireMove(controller.handle(key(6, "D")), "3,0,1", "east traversal");
        requireMove(controller.handle(key(7, "S")), "3,0,2", "south traversal");
        EchoPlayerControllerResult blocked = controller.handle(key(8, "S"));
        require(!blocked.handled(), "blocked collision should not be handled as movement");
        require(blocked.movement().orElseThrow().reason().equals("blocked_cell"),
                "blocked movement should report blocked_cell");
        require(blocked.state().player().worldPosition().key().equals("3,0,2"),
                "blocked collision should preserve player position");
        requireCamera(blocked.state().camera(), 3.5D, 3.0D, 2.5D, "blocked camera");

        EchoPlayerControllerResult focusTerminal = controller.handle(key(9, "BACKQUOTE"));
        require(focusTerminal.handled(), "controller should delegate Terminal focus");
        require(input.focus().terminalFocused(), "Terminal focus should be active");
        EchoPlayerControllerResult blockedByFocus = controller.handle(key(10, "A"));
        require(!blockedByFocus.handled(), "Terminal focus should block gameplay movement");
        require(blockedByFocus.effects().contains("terminal-focus-blocks-gameplay"),
                "focus-blocked movement should report the input focus reason");
        require(blockedByFocus.state().player().worldPosition().key().equals("3,0,2"),
                "focus-blocked movement should preserve player position");
        EchoPlayerControllerResult blurTerminal = controller.handle(key(11, "ESCAPE"));
        require(blurTerminal.handled(), "controller should delegate Terminal blur");
        require(!input.focus().terminalFocused(), "Terminal focus should be cleared");

        System.out.println("phase15.4 player controller smoke PASS position="
                + blocked.state().player().worldPosition().key()
                + " camera="
                + fixed(blocked.state().camera().x())
                + ","
                + fixed(blocked.state().camera().z())
                + " collision=" + blocked.movement().orElseThrow().reason()
                + " mission=" + gameplay.mission().status().name()
                + " health=" + blocked.state().player().health().currentHealth()
                + " target=" + southOne.state().target().orElseThrow().id());
    }

    private static EchoInputEvent key(long sequence, String key) {
        return EchoInputEvent.press(sequence, EchoInputControl.keyboard(key));
    }

    private static EchoInputEvent mouse(long sequence, String button) {
        return EchoInputEvent.press(sequence, EchoInputControl.mouse(button));
    }

    private static void requireMove(EchoPlayerControllerResult result, String expectedPosition, String label) {
        require(result.handled(), label + " should be handled");
        require(result.movement().orElseThrow().moved(), label + " should move");
        require(result.state().player().worldPosition().key().equals(expectedPosition),
                label + " should end at " + expectedPosition);
    }

    private static void requireCamera(EchoRenderCamera camera, double x, double y, double z, String label) {
        requireClose(camera.x(), x, label + " x");
        requireClose(camera.y(), y, label + " y");
        requireClose(camera.z(), z, label + " z");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireClose(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 0.001D) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static String fixed(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
