package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.item.EchoItemTooltipRenderer;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarMutation;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarTransferResult;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Objects;

public final class EchoAshfallInventoryUxRuntime {
    public EchoAshfallInventoryUxResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        EchoVoxelPlayerHotbar hotbar =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        0
                ).newStarterHotbar();
        hotbar.add(bridge.waterRationItem(), 4);
        hotbar.add(bridge.fieldRationItem(), 3);
        hotbar.add(bridge.emergencyScannerItem(), 1);

        EchoVoxelHotbarTransferResult drag = hotbar.moveOrMergeSlot(1, 6);
        EchoVoxelHotbarMutation shelterStack = hotbar.assignSlot(2, bridge.shelterAnchorBlock(), 8);
        EchoVoxelHotbarTransferResult split = hotbar.splitSlotTo(2, 5);
        EchoVoxelHotbarMutation assignment = hotbar.assignSlot(8, bridge.powerRepairKitItem(), 1);
        hotbar.select(8);

        List<String> tooltip = tooltipLines(hotbar.selected());
        EchoVoxelHotbarTransferResult blockedSplit = hotbar.splitSlotTo(8, 7);
        EchoVoxelHotbarTransferResult blockedDrag = hotbar.moveOrMergeSlot(7, 8);
        EchoVoxelHotbarMutation fullFill = fillInventoryWithWater(bridge);
        EchoAshfallLiveMissionState useMission = new EchoAshfallLiveMissionState();
        useMission.useWaterRation(bridge.survivalProfile());
        String waterFeedback = useMission.lastMessage();
        useMission.useFoodRation(bridge.survivalProfile());
        String foodFeedback = useMission.lastMessage();

        EchoStandaloneGameShellState title = EchoStandaloneGameShellState.titleNoSave();
        EchoStandaloneGameShellState playing = title.startNewGame();
        EchoStandaloneGameShellState inventory = playing.openInventory();
        EchoStandaloneGameShellState closedInventory = inventory.closeInventory();
        EchoStandaloneGameShellState paused = closedInventory.pause();
        EchoStandaloneGameShellState resumed = paused.resume();

        boolean adapterCoreBacked = allNonEmptySlotsUseAdapterCoreIds(bridge, hotbar);
        boolean dragMovementReady = drag.changed()
                && drag.reason().equals("moved_stack")
                && drag.source().empty()
                && drag.target().block().id().equals(bridge.waterRationItem().id())
                && drag.target().count() == 4;
        boolean stackSplitReady = shelterStack.changed()
                && split.changed()
                && split.reason().equals("split_stack")
                && hotbar.slot(2).count() == 4
                && hotbar.slot(5).count() == 4;
        boolean hotbarAssignmentReady = assignment.changed()
                && hotbar.selectedSlot() == 8
                && hotbar.selected().block().id().equals(bridge.powerRepairKitItem().id());
        boolean tooltipReady = tooltip.size() >= 4
                && tooltip.get(0).equals(bridge.powerRepairKitItem().displayName())
                && tooltip.stream().anyMatch(line -> line.contains(bridge.powerRepairKitItem().id()))
                && tooltip.stream().anyMatch(line -> line.startsWith("Use: "))
                && tooltip.stream().anyMatch(line -> line.contains("AdapterCore"))
                && tooltip.stream().anyMatch(line -> line.equals("State: Ready"));
        boolean disabledStatesReady = !blockedSplit.changed()
                && blockedSplit.reason().equals("source_stack_too_small")
                && !blockedDrag.changed()
                && blockedDrag.reason().equals("empty_source")
                && !fullFill.changed()
                && fullFill.reason().equals("inventory_full");
        boolean consumeUseFeedbackReady = waterFeedback.equals("water ration used: hydration restored")
                && foodFeedback.equals("field ration used: hunger restored")
                && EchoAshfallPlayerFeedback.from(
                useMission,
                hotbar,
                true,
                waterFeedback + " / consumed_one"
        ).warningStates().contains("item consumed");
        boolean keyboardMouseFlowReady = title.mode() == EchoStandaloneGameShellMode.TITLE
                && playing.gameplayActive()
                && inventory.mode() == EchoStandaloneGameShellMode.INVENTORY
                && !inventory.gameplayActive()
                && inventory.lines().stream().anyMatch(line -> line.contains("E / Esc"))
                && inventory.lines().stream().anyMatch(line -> line.contains("1-9"))
                && inventory.lines().stream().anyMatch(line -> line.contains("Mouse remains released"))
                && closedInventory.gameplayActive()
                && paused.mode() == EchoStandaloneGameShellMode.PAUSED
                && resumed.gameplayActive();

        int diagnostics = 0;
        diagnostics += adapterCoreBacked ? 1 : 0;
        diagnostics += dragMovementReady ? 1 : 0;
        diagnostics += stackSplitReady ? 1 : 0;
        diagnostics += hotbarAssignmentReady ? 1 : 0;
        diagnostics += tooltipReady ? 1 : 0;
        diagnostics += disabledStatesReady ? 1 : 0;
        diagnostics += consumeUseFeedbackReady ? 1 : 0;
        diagnostics += keyboardMouseFlowReady ? 1 : 0;

        return new EchoAshfallInventoryUxResult(
                adapterCoreBacked,
                dragMovementReady,
                stackSplitReady,
                hotbarAssignmentReady,
                tooltipReady,
                disabledStatesReady,
                consumeUseFeedbackReady,
                keyboardMouseFlowReady,
                diagnostics,
                "adapterCore=" + adapterCoreBacked
                        + " drag=" + drag.reason()
                        + " split=" + split.reason() + ":" + split.moved()
                        + " assign=" + assignment.reason()
                        + " tooltip=" + tooltip.size()
                        + " disabled=" + blockedSplit.reason() + "/" + blockedDrag.reason() + "/" + fullFill.reason()
                        + " useFeedback=" + consumeUseFeedbackReady
                        + " shell=" + inventory.summary()
        );
    }

    private static EchoVoxelHotbarMutation fillInventoryWithWater(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelPlayerHotbar full = new EchoVoxelPlayerHotbar(List.of(), 0);
        for (int index = 0; index < EchoVoxelPlayerHotbar.SLOT_COUNT; index++) {
            full.assignSlot(index, bridge.waterRationItem(), EchoVoxelPlayerHotbar.MAX_STACK);
        }
        return full.add(bridge.waterRationItem(), 1);
    }

    private static boolean allNonEmptySlotsUseAdapterCoreIds(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoVoxelPlayerHotbar hotbar
    ) {
        for (var slot : hotbar.slots()) {
            if (!slot.empty() && bridge.registry().findLiveVoxelId(slot.block().id()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static List<String> tooltipLines(EchoVoxelHotbarSlot slot) {
        if (slot.empty()) {
            return List.of("Empty", "Disabled until an item is assigned");
        }
        EchoVoxelBlock block = slot.block();
        EchoItemDefinition definition = new EchoItemDefinition(
                new EchoItemId(block.id()),
                block.displayName(),
                block.solid() ? EchoItemCategory.TOOL : EchoItemCategory.CONSUMABLE,
                EchoVoxelPlayerHotbar.MAX_STACK,
                Math.max(0.0D, block.hardness()),
                block.solid() ? List.of("adaptercore", "placeable") : List.of("adaptercore", "usable"),
                List.of(
                        block.solid() ? "Placeable terrain/tool block" : "Usable inventory item",
                        "AdapterCore item routed through standalone hotbar"
                )
        );
        return new EchoItemTooltipRenderer().render(new EchoItemStack(definition, slot.count()));
    }
}
