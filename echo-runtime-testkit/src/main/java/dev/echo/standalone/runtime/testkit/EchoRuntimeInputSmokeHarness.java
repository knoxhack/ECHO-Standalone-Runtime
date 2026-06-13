package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputAction;
import dev.echo.standalone.runtime.input.EchoInputBinding;
import dev.echo.standalone.runtime.input.EchoInputBindingMap;
import dev.echo.standalone.runtime.input.EchoInputContext;
import dev.echo.standalone.runtime.input.EchoInputControl;
import dev.echo.standalone.runtime.input.EchoInputDeviceType;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeInputSmokeHarness {
    private EchoRuntimeInputSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
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
        int waterRemainingAfterQuickSlot = items.operations().count(
                items.inventoryStore().require(new EchoInventoryId("inventory:player-001")),
                new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID)
        );
        require(waterRemainingAfterQuickSlot == 1, "quick slot should consume one water ration");
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

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                input,
                entities,
                gameplay,
                shell,
                staleKey,
                quickSlot,
                waterRemainingAfterQuickSlot,
                slotNine,
                mouseLook,
                inventoryToggle,
                pauseToggle,
                inventoryDrag,
                inventorySplit,
                inventoryAssign,
                inventoryTooltip,
                inventoryUse,
                keyboardEast,
                gamepadEast,
                gamepadSouth,
                mouseInteract,
                focusTerminal,
                terminalText,
                expectedTerminalStatus,
                blockedMove,
                blurTerminal,
                playerId
        );

        System.out.println("phase15.3 input runtime smoke PASS bindings="
                + input.bindings().bindings().size()
                + " history=" + shell.history().size()
                + " position=" + entities.store().require(playerId).worldPosition().key()
                + " focus=" + input.focus().activeContext().name());
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoInputRuntimeResult input,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoTerminalShell shell,
            EchoInputRouteResult staleKey,
            EchoInputRouteResult quickSlot,
            int waterRemainingAfterQuickSlot,
            EchoInputRouteResult slotNine,
            EchoInputRouteResult mouseLook,
            EchoInputRouteResult inventoryToggle,
            EchoInputRouteResult pauseToggle,
            EchoInputRouteResult inventoryDrag,
            EchoInputRouteResult inventorySplit,
            EchoInputRouteResult inventoryAssign,
            EchoInputRouteResult inventoryTooltip,
            EchoInputRouteResult inventoryUse,
            EchoInputRouteResult keyboardEast,
            EchoInputRouteResult gamepadEast,
            EchoInputRouteResult gamepadSouth,
            EchoInputRouteResult mouseInteract,
            EchoInputRouteResult focusTerminal,
            EchoInputRouteResult terminalText,
            String expectedTerminalStatus,
            EchoInputRouteResult blockedMove,
            EchoInputRouteResult blurTerminal,
            EchoEntityId playerId
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);

        List<EchoInputBinding> bindings = input.bindings().bindings();
        EchoWorldPosition finalPosition = entities.store().require(playerId).worldPosition();
        boolean terminalStatusOutput = shell.outputLines().stream()
                .anyMatch(line -> line.contains(expectedTerminalStatus));

        write(root.resolve("runtime-input.json"), """
                {
                  "schema": "echo.standalone.runtime_input.v2",
                  "status": "PASS",
                  "phase": "15.3",
                  "summary": "Input runtime booted service-bound device-neutral bindings, focus state, and router, then routed gameplay, UI inventory, terminal, and blocked-focus inputs deterministically.",
                  "serviceBound": true,
                  "bindingsBound": true,
                  "focusBound": true,
                  "routerBound": true,
                  "bindingCount": %d,
                  "deviceCount": %d,
                  "contexts": %s,
                  "finalFocusContext": "%s",
                  "finalFocusPath": "%s",
                  "finalPlayerPosition": %s,
                  "missionStatus": "%s",
                  "terminalHistoryCount": %d,
                  "terminalStatusOutput": %s
                }
                """.formatted(
                bindings.size(),
                deviceCount(bindings),
                jsonStringArray(List.of("GAMEPLAY", "TERMINAL", "UI")),
                input.focus().activeContext(),
                escape(input.focus().focusPath()),
                positionJson(finalPosition),
                gameplay.mission().status(),
                shell.history().size(),
                terminalStatusOutput
        ));

        write(root.resolve("input-devices.json"), """
                {
                  "schema": "echo.standalone.input_devices.v2",
                  "status": "PASS",
                  "deviceNeutral": true,
                  "nativePollingRequired": false,
                  "deviceTypes": %s,
                  "keyboardBindingCount": %d,
                  "mouseBindingCount": %d,
                  "gamepadBindingCount": %d,
                  "supportsTextInput": %s,
                  "supportsMouseLook": %s,
                  "supportsGamepadMovement": %s
                }
                """.formatted(
                jsonStringArray(List.of("KEYBOARD", "MOUSE", "GAMEPAD")),
                countDevice(bindings, EchoInputDeviceType.KEYBOARD),
                countDevice(bindings, EchoInputDeviceType.MOUSE),
                countDevice(bindings, EchoInputDeviceType.GAMEPAD),
                hasBinding(bindings, EchoInputContext.TERMINAL, EchoInputControl.keyboard("TEXT"), EchoInputAction.TERMINAL_SUBMIT_TEXT),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.mouse("MOVE"), EchoInputAction.MOUSE_LOOK),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("DPAD_RIGHT"), EchoInputAction.MOVE_EAST)
        ));

        write(root.resolve("input-bindings.json"), """
                {
                  "schema": "echo.standalone.input_bindings.v2",
                  "status": "PASS",
                  "bindingCount": %d,
                  "gameplayBindingCount": %d,
                  "uiBindingCount": %d,
                  "terminalBindingCount": %d,
                  "hasKeyboardMovement": %s,
                  "hasMousePrimary": %s,
                  "hasMouseSecondary": %s,
                  "hasMouseLook": %s,
                  "hasHotbarDigits": %s,
                  "hasInventoryDrag": %s,
                  "hasInventorySplit": %s,
                  "hasInventoryHotbarAssign": %s,
                  "hasInventoryTooltip": %s,
                  "hasInventoryUse": %s,
                  "hasPause": %s,
                  "hasGamepadInteract": %s,
                  "bindings": %s
                }
                """.formatted(
                bindings.size(),
                countContext(bindings, EchoInputContext.GAMEPLAY),
                countContext(bindings, EchoInputContext.UI),
                countContext(bindings, EchoInputContext.TERMINAL),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("W"), EchoInputAction.MOVE_NORTH),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.mouse("PRIMARY"), EchoInputAction.POINTER_PRIMARY),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.mouse("SECONDARY"), EchoInputAction.POINTER_SECONDARY),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.mouse("MOVE"), EchoInputAction.MOUSE_LOOK),
                hasHotbarDigits(bindings),
                hasBinding(bindings, EchoInputContext.UI, EchoInputControl.mouse("PRIMARY"), EchoInputAction.INVENTORY_DRAG_STACK),
                hasBinding(bindings, EchoInputContext.UI, EchoInputControl.mouse("SECONDARY"), EchoInputAction.INVENTORY_SPLIT_STACK),
                hasBinding(bindings, EchoInputContext.UI, EchoInputControl.keyboard("DIGIT1"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR),
                hasBinding(bindings, EchoInputContext.UI, EchoInputControl.mouse("MOVE"), EchoInputAction.INVENTORY_SHOW_TOOLTIP),
                hasBinding(bindings, EchoInputContext.UI, EchoInputControl.keyboard("ENTER"), EchoInputAction.INVENTORY_USE_SELECTED),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("ESCAPE"), EchoInputAction.PAUSE_TOGGLE),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("BUTTON_SOUTH"), EchoInputAction.INTERACT),
                bindingsJson(bindings)
        ));

        write(root.resolve("input-rebinding.json"), """
                {
                  "schema": "echo.standalone.input_rebinding.v2",
                  "status": "PASS",
                  "context": "GAMEPLAY",
                  "action": "MOVE_EAST",
                  "oldControl": "keyboard:d",
                  "newControl": "keyboard:arrowright",
                  "oldBindingRemoved": %s,
                  "newBindingActive": %s,
                  "staleKey": %s,
                  "reboundRoute": %s,
                  "playerPositionAfterRebound": %s
                }
                """.formatted(
                !hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("D"), EchoInputAction.MOVE_EAST),
                hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("ARROWRIGHT"), EchoInputAction.MOVE_EAST),
                routeResultJson(staleKey),
                routeResultJson(keyboardEast),
                positionJson(keyboardEast.movementResult().orElseThrow().to())
        ));

        write(root.resolve("input-focus.json"), """
                {
                  "schema": "echo.standalone.input_focus.v2",
                  "status": "PASS",
                  "focusTerminal": %s,
                  "terminalText": %s,
                  "blockedMove": %s,
                  "blurTerminal": %s,
                  "terminalHistory": %s,
                  "terminalStatusOutput": %s,
                  "blockedMovementPreservedPosition": %s,
                  "finalContext": "%s",
                  "finalFocusPath": "%s"
                }
                """.formatted(
                routeResultJson(focusTerminal),
                routeResultJson(terminalText),
                routeResultJson(blockedMove),
                routeResultJson(blurTerminal),
                jsonStringArray(shell.history()),
                terminalStatusOutput,
                finalPosition.equals(new EchoWorldPosition(2, 0, 1)),
                input.focus().activeContext(),
                escape(input.focus().focusPath())
        ));

        write(root.resolve("input-routing.json"), """
                {
                  "schema": "echo.standalone.input_routing.v2",
                  "status": "PASS",
                  "quickSlot": %s,
                  "waterRemainingAfterQuickSlot": %d,
                  "slotNine": %s,
                  "mouseLook": %s,
                  "inventoryToggle": %s,
                  "pauseToggle": %s,
                  "inventoryDrag": %s,
                  "inventorySplit": %s,
                  "inventoryAssign": %s,
                  "inventoryTooltip": %s,
                  "inventoryUse": %s,
                  "gamepadEast": %s,
                  "gamepadSouth": %s,
                  "mouseInteract": %s,
                  "finalPlayerPosition": %s,
                  "routingCoveredGameplayUiAndTerminal": %s
                }
                """.formatted(
                routeResultJson(quickSlot),
                waterRemainingAfterQuickSlot,
                routeResultJson(slotNine),
                routeResultJson(mouseLook),
                routeResultJson(inventoryToggle),
                routeResultJson(pauseToggle),
                routeResultJson(inventoryDrag),
                routeResultJson(inventorySplit),
                routeResultJson(inventoryAssign),
                routeResultJson(inventoryTooltip),
                routeResultJson(inventoryUse),
                routeResultJson(gamepadEast),
                routeResultJson(gamepadSouth),
                routeResultJson(mouseInteract),
                positionJson(finalPosition),
                quickSlot.handled()
                        && inventoryDrag.handled()
                        && terminalText.handled()
                        && !blockedMove.handled()
        ));
    }

    private static int deviceCount(List<EchoInputBinding> bindings) {
        return (int) bindings.stream()
                .map(binding -> binding.control().deviceType())
                .distinct()
                .count();
    }

    private static int countDevice(List<EchoInputBinding> bindings, EchoInputDeviceType deviceType) {
        return (int) bindings.stream()
                .filter(binding -> binding.control().deviceType() == deviceType)
                .count();
    }

    private static int countContext(List<EchoInputBinding> bindings, EchoInputContext context) {
        return (int) bindings.stream()
                .filter(binding -> binding.context() == context)
                .count();
    }

    private static boolean hasBinding(
            List<EchoInputBinding> bindings,
            EchoInputContext context,
            EchoInputControl control,
            EchoInputAction action
    ) {
        return bindings.stream().anyMatch(binding -> binding.context() == context
                && binding.control().equals(control)
                && binding.action() == action);
    }

    private static boolean hasHotbarDigits(List<EchoInputBinding> bindings) {
        for (int slot = 1; slot <= 9; slot++) {
            EchoInputAction action = EchoInputAction.valueOf("QUICK_SLOT_" + slot);
            if (!hasBinding(bindings, EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT" + slot), action)) {
                return false;
            }
        }
        return true;
    }

    private static String bindingsJson(List<EchoInputBinding> bindings) {
        return bindings.stream()
                .map(binding -> """
                        {
                          "context": "%s",
                          "device": "%s",
                          "control": "%s",
                          "action": "%s",
                          "label": "%s"
                        }""".formatted(
                        binding.context(),
                        binding.control().deviceType(),
                        escape(binding.control().stableId()),
                        binding.action(),
                        escape(binding.label())
                ).strip())
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String routeResultJson(EchoInputRouteResult result) {
        return """
                {
                  "handled": %s,
                  "target": "%s",
                  "action": %s,
                  "context": %s,
                  "control": %s,
                  "movement": %s,
                  "interaction": %s,
                  "uiHandled": %s,
                  "effects": %s
                }""".formatted(
                result.handled(),
                result.target(),
                result.action().map(action -> "\"" + action.action().name() + "\"").orElse("null"),
                result.action().map(action -> "\"" + action.context().name() + "\"").orElse("null"),
                result.action().map(action -> "\"" + escape(action.source().control().stableId()) + "\"").orElse("null"),
                result.movementResult().map(EchoRuntimeInputSmokeHarness::movementJson).orElse("null"),
                result.interactionResult().map(EchoRuntimeInputSmokeHarness::interactionJson).orElse("null"),
                result.uiResult().map(ui -> Boolean.toString(ui.handled())).orElse("null"),
                jsonStringArray(result.effects())
        ).strip();
    }

    private static String movementJson(dev.echo.standalone.runtime.entity.EchoEntityMovementResult movement) {
        return """
                {
                  "entityId": "%s",
                  "from": %s,
                  "to": %s,
                  "moved": %s,
                  "reason": "%s"
                }""".formatted(
                escape(movement.entityId().value()),
                positionJson(movement.from()),
                positionJson(movement.to()),
                movement.moved(),
                escape(movement.reason())
        ).strip();
    }

    private static String interactionJson(dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult interaction) {
        return """
                {
                  "interactionId": "%s",
                  "success": %s,
                  "objectiveCompleted": %s,
                  "experienceAwarded": %d,
                  "reason": "%s"
                }""".formatted(
                escape(interaction.interactionId()),
                interaction.success(),
                interaction.objectiveCompleted(),
                interaction.experienceAwarded(),
                escape(interaction.reason())
        ).strip();
    }

    private static String positionJson(EchoWorldPosition position) {
        return """
                {"x": %d, "y": %d, "z": %d, "key": "%s"}""".formatted(
                position.x(),
                position.y(),
                position.z(),
                escape(position.key())
        ).trim();
    }

    private static String jsonStringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
