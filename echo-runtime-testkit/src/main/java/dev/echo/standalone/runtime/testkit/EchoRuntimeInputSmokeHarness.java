package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputAction;
import dev.echo.standalone.runtime.input.EchoInputBindingMap;
import dev.echo.standalone.runtime.input.EchoInputContext;
import dev.echo.standalone.runtime.input.EchoInputControl;
import dev.echo.standalone.runtime.input.EchoInputEvent;
import dev.echo.standalone.runtime.input.EchoInputFocusState;
import dev.echo.standalone.runtime.input.EchoInputRouteResult;
import dev.echo.standalone.runtime.input.EchoInputRouteTarget;
import dev.echo.standalone.runtime.input.EchoInputRuntime;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoAgent5UiDataSources;
import dev.echo.standalone.runtime.ui.EchoTerminalScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

public final class EchoRuntimeInputSmokeHarness {
    private EchoRuntimeInputSmokeHarness() {
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
        EchoEntityId playerId = new EchoEntityId("player-001");
        EchoInputRuntimeResult input = new EchoInputRuntime().boot(services, ui, entities, gameplay, playerId);

        require(services.require(EchoInputRuntimeResult.class) == input,
                "input runtime result should be service-bound");
        require(services.require(EchoInputBindingMap.class) == input.bindings(),
                "input bindings should be service-bound");
        require(services.require(EchoInputFocusState.class) == input.focus(),
                "input focus state should be service-bound");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.keyboard("W"))
                                && binding.action() == EchoInputAction.MOVE_NORTH),
                "keyboard movement binding should exist");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.mouse("PRIMARY"))
                                && binding.action() == EchoInputAction.POINTER_PRIMARY),
                "mouse primary binding should exist");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.mouse("SECONDARY"))
                                && binding.action() == EchoInputAction.POINTER_SECONDARY),
                "mouse secondary binding should exist");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.mouse("MOVE"))
                                && binding.action() == EchoInputAction.MOUSE_LOOK),
                "mouse look binding should exist");
        for (int slot = 1; slot <= 9; slot++) {
            int expectedSlot = slot;
            require(input.bindings().bindings().stream().anyMatch(binding ->
                            binding.control().equals(EchoInputControl.keyboard("DIGIT" + expectedSlot))
                                    && binding.action().name().equals("QUICK_SLOT_" + expectedSlot)),
                    "keyboard hotbar binding " + slot + " should exist");
        }
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.keyboard("E"))
                                && binding.action() == EchoInputAction.INVENTORY_TOGGLE),
                "E should toggle inventory");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.context() == EchoInputContext.UI
                                && binding.control().equals(EchoInputControl.mouse("PRIMARY"))
                                && binding.action() == EchoInputAction.INVENTORY_DRAG_STACK),
                "inventory UI should bind primary mouse to click/drag move");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.context() == EchoInputContext.UI
                                && binding.control().equals(EchoInputControl.mouse("SECONDARY"))
                                && binding.action() == EchoInputAction.INVENTORY_SPLIT_STACK),
                "inventory UI should bind secondary mouse to stack split");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.context() == EchoInputContext.UI
                                && binding.control().equals(EchoInputControl.keyboard("DIGIT1"))
                                && binding.action() == EchoInputAction.INVENTORY_ASSIGN_HOTBAR),
                "inventory UI should bind digit keys to hotbar assignment");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.context() == EchoInputContext.UI
                                && binding.control().equals(EchoInputControl.mouse("MOVE"))
                                && binding.action() == EchoInputAction.INVENTORY_SHOW_TOOLTIP),
                "inventory UI should bind mouse move to item descriptions/tooltips");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.context() == EchoInputContext.UI
                                && binding.control().equals(EchoInputControl.keyboard("ENTER"))
                                && binding.action() == EchoInputAction.INVENTORY_USE_SELECTED),
                "inventory UI should bind Enter to consume/use feedback");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.keyboard("ESCAPE"))
                                && binding.action() == EchoInputAction.PAUSE_TOGGLE),
                "Escape should toggle pause from gameplay");
        require(input.bindings().bindings().stream().anyMatch(binding ->
                        binding.control().equals(EchoInputControl.gamepad("BUTTON_SOUTH"))
                                && binding.action() == EchoInputAction.INTERACT),
                "gamepad interact binding should exist");

        input.bindings().rebind(
                EchoInputContext.GAMEPLAY,
                EchoInputAction.MOVE_EAST,
                EchoInputControl.keyboard("ARROWRIGHT")
        );
        EchoInputRouteResult staleKey = input.dispatch(EchoInputEvent.press(0, EchoInputControl.keyboard("D")));
        require(!staleKey.handled(), "old keyboard move-east binding should be removed after rebind");

        EchoInputRouteResult quickSlot = input.dispatch(EchoInputEvent.press(1, EchoInputControl.keyboard("DIGIT1")));
        require(quickSlot.handled(), "quick slot should route to gameplay");
        require(quickSlot.interactionResult().orElseThrow().interactionId().equals("ashfall:drink_water"),
                "quick slot 1 should consume water");
        require(items.operations().count(
                items.inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID)
        ) == 1, "quick slot should consume one water ration");
        EchoInputRouteResult slotNine = input.dispatch(EchoInputEvent.press(10, EchoInputControl.keyboard("DIGIT9")));
        require(slotNine.handled() && slotNine.effects().contains("quick-slot:9"),
                "quick slot 9 should route to hotbar selection");
        EchoInputRouteResult mouseLook = input.dispatch(EchoInputEvent.press(11, EchoInputControl.mouse("MOVE")));
        require(mouseLook.handled() && mouseLook.effects().contains("mouse-look:captured"),
                "mouse movement should route to gameplay look capture");
        EchoInputRouteResult inventoryToggle = input.dispatch(EchoInputEvent.press(12, EchoInputControl.keyboard("E")));
        require(inventoryToggle.handled()
                        && inventoryToggle.target() == EchoInputRouteTarget.UI
                        && inventoryToggle.effects().contains("mouse:released"),
                "E inventory should route to UI and release mouse capture");
        EchoInputRouteResult pauseToggle = input.dispatch(EchoInputEvent.press(13, EchoInputControl.keyboard("ESCAPE")));
        require(pauseToggle.handled()
                        && pauseToggle.target() == EchoInputRouteTarget.UI
                        && pauseToggle.effects().contains("overlay:pause"),
                "Escape should route to pause overlay before terminal focus");
        input.focus().focusUi("inventory:grid");
        EchoInputRouteResult inventoryDrag = input.dispatch(EchoInputEvent.press(14, EchoInputControl.mouse("PRIMARY")));
        require(inventoryDrag.handled()
                        && inventoryDrag.effects().contains("inventory:drag_move")
                        && inventoryDrag.effects().contains("feedback:click/drag move"),
                "inventory primary click should route as click/drag move feedback");
        EchoInputRouteResult inventorySplit = input.dispatch(EchoInputEvent.press(15, EchoInputControl.mouse("SECONDARY")));
        require(inventorySplit.handled()
                        && inventorySplit.effects().contains("inventory:split_stack")
                        && inventorySplit.effects().contains("feedback:stack split"),
                "inventory secondary click should route as stack split feedback");
        EchoInputRouteResult inventoryAssign = input.dispatch(EchoInputEvent.press(16, EchoInputControl.keyboard("DIGIT4")));
        require(inventoryAssign.handled()
                        && inventoryAssign.effects().contains("inventory:assign_hotbar:4")
                        && inventoryAssign.effects().contains("feedback:hotbar assignment"),
                "inventory digit key should route as hotbar assignment feedback");
        EchoInputRouteResult inventoryTooltip = input.dispatch(EchoInputEvent.press(17, EchoInputControl.mouse("MOVE")));
        require(inventoryTooltip.handled()
                        && inventoryTooltip.effects().contains("inventory:show_tooltip")
                        && inventoryTooltip.effects().contains("feedback:item description tooltip"),
                "inventory mouse move should route as tooltip feedback");
        EchoInputRouteResult inventoryUse = input.dispatch(EchoInputEvent.press(18, EchoInputControl.keyboard("ENTER")));
        require(inventoryUse.handled()
                        && inventoryUse.effects().contains("inventory:use_selected")
                        && inventoryUse.effects().contains("feedback:consume/use feedback"),
                "inventory Enter should route as consume/use feedback");
        input.focus().focusGameplay();

        EchoInputRouteResult keyboardEast = input.dispatch(EchoInputEvent.press(2, EchoInputControl.keyboard("ARROWRIGHT")));
        require(keyboardEast.handled(), "rebound keyboard move east should be handled");
        require(entities.store().require(playerId).worldPosition().equals(new EchoWorldPosition(1, 0, 0)),
                "rebound keyboard input should move player east");

        EchoInputRouteResult gamepadEast = input.dispatch(EchoInputEvent.press(3, EchoInputControl.gamepad("DPAD_RIGHT")));
        EchoInputRouteResult gamepadSouth = input.dispatch(EchoInputEvent.press(4, EchoInputControl.gamepad("DPAD_DOWN")));
        require(gamepadEast.handled() && gamepadSouth.handled(), "gamepad d-pad movement should be handled");
        require(entities.store().require(playerId).worldPosition().equals(new EchoWorldPosition(2, 0, 1)),
                "gamepad movement should reach the crash cache");

        EchoInputRouteResult mouseInteract = input.dispatch(EchoInputEvent.press(5, EchoInputControl.mouse("PRIMARY")));
        require(mouseInteract.handled(), "mouse primary should route to interaction");
        require(mouseInteract.target() == EchoInputRouteTarget.GAMEPLAY,
                "mouse primary should target gameplay");
        require(mouseInteract.interactionResult().orElseThrow().interactionId().equals("ashfall:salvage_cache"),
                "mouse primary should salvage the crash cache at the target position");

        EchoInputRouteResult focusTerminal = input.dispatch(EchoInputEvent.press(6, EchoInputControl.keyboard("BACKQUOTE")));
        require(focusTerminal.handled(), "Terminal focus binding should be handled");
        require(input.focus().terminalFocused(), "Terminal should be focused");
        require(input.focus().focusPath().equals("terminal:input"), "Terminal focus path should come from UI frame");

        EchoInputRouteResult terminalText = input.dispatch(EchoInputEvent.text(7, "status"));
        require(terminalText.handled(), "Terminal text should route to UI");
        require(terminalText.target() == EchoInputRouteTarget.UI, "Terminal text target should be UI");
        require(shell.history().contains("status"), "Terminal shell should receive text command");
        String expectedTerminalStatus = EchoAgent5UiDataSources.reference().terminalReadyLine();
        require(shell.outputLines().stream().anyMatch(line -> line.contains(expectedTerminalStatus)),
                "Terminal status command should produce deterministic output");

        EchoInputRouteResult blockedMove = input.dispatch(EchoInputEvent.press(8, EchoInputControl.gamepad("DPAD_LEFT")));
        require(!blockedMove.handled(), "Terminal focus should block gameplay movement");
        require(entities.store().require(playerId).worldPosition().equals(new EchoWorldPosition(2, 0, 1)),
                "blocked movement should preserve player position");

        EchoInputRouteResult blurTerminal = input.dispatch(EchoInputEvent.press(9, EchoInputControl.keyboard("ESCAPE")));
        require(blurTerminal.handled(), "Escape should blur Terminal focus");
        require(!input.focus().terminalFocused(), "Terminal should no longer be focused");

        require(gameplay.mission().status() == EchoGameplayMissionStatus.ACTIVE,
                "input smoke should not claim full mission completion yet");

        System.out.println("phase15.3 input runtime smoke PASS bindings="
                + input.bindings().bindings().size()
                + " history=" + shell.history().size()
                + " position=" + entities.store().require(playerId).worldPosition().key()
                + " focus=" + input.focus().activeContext().name());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
