package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoInventoryOperations;
import dev.echo.standalone.runtime.item.EchoInventorySlot;
import dev.echo.standalone.runtime.item.EchoInventoryTransferResult;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemCraftingSystem;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.item.EchoLootEntry;
import dev.echo.standalone.runtime.item.EchoLootTable;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class EchoClientInventoryRuntime {
    private EchoAdapterCoreStandaloneContentBridge bridge;
    private final EchoInventoryOperations operations;
    private final EchoInventoryContainer playerInventory;
    private final EchoInventoryContainer openContainer;
    private EchoItemRegistry itemRegistry;
    private EchoItemCraftingSystem craftingSystem;
    private List<EchoItemRecipe> workbenchRecipes;
    private Map<String, EchoLootTable> blockLootTables = Map.of();
    private EchoItemStack cursorStack;
    private long playerInventoryVersion = 1L;
    private long containerInventoryVersion = 1L;
    private EchoClientInventoryScreenModel cachedInventoryScreenModel;
    private EchoClientInventoryScreenModel cachedContainerScreenModel;
    private long cachedInventoryScreenModelVersion = -1L;
    private long cachedContainerScreenModelVersion = -1L;
    private int cachedInventorySelectedSlot = Integer.MIN_VALUE;
    private EchoClientToolState cachedInventoryToolState = EchoClientToolState.empty();
    private EchoClientToolState cachedContainerToolState = EchoClientToolState.empty();

    EchoClientInventoryRuntime(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoInventoryOperations operations,
            EchoInventoryContainer playerInventory,
            EchoInventoryContainer openContainer,
            EchoItemRegistry itemRegistry,
            List<EchoItemRecipe> workbenchRecipes,
            List<EchoLootTable> lootTables
    ) {
        this.bridge = bridge;
        this.operations = operations == null ? new EchoInventoryOperations() : operations;
        this.playerInventory = playerInventory == null ? newPlayerInventory() : playerInventory;
        this.openContainer = openContainer == null ? EchoClientStarterLoadout.empty().newOpenContainer() : openContainer;
        this.itemRegistry = itemRegistry == null ? new EchoItemRegistry() : itemRegistry;
        this.workbenchRecipes = workbenchRecipes == null ? List.of() : List.copyOf(workbenchRecipes);
        this.blockLootTables = blockLootTables(lootTables);
        this.craftingSystem = new EchoItemCraftingSystem(this.itemRegistry, this.operations);
    }

    void updateRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoItemRegistry itemRegistry,
            List<EchoItemRecipe> workbenchRecipes,
            List<EchoLootTable> lootTables
    ) {
        this.bridge = bridge == null ? this.bridge : bridge;
        this.itemRegistry = itemRegistry == null ? this.itemRegistry : itemRegistry;
        this.workbenchRecipes = workbenchRecipes == null ? this.workbenchRecipes : List.copyOf(workbenchRecipes);
        this.blockLootTables = lootTables == null ? this.blockLootTables : blockLootTables(lootTables);
        this.craftingSystem = new EchoItemCraftingSystem(this.itemRegistry, this.operations);
        invalidateInventoryModels();
    }

    EchoInventoryContainer playerInventory() {
        return playerInventory;
    }

    EchoInventoryContainer openContainer() {
        return openContainer;
    }

    List<EchoClientInventorySlotSnapshot> inventorySnapshots() {
        return snapshots(playerInventory);
    }

    List<EchoClientInventorySlotSnapshot> containerSnapshots() {
        return snapshots(openContainer);
    }

    EchoClientInventoryScreenModel inventoryScreenModel(EchoVoxelPlayerHotbar hotbar, EchoClientToolState toolState) {
        int selectedSlot = selectedSlot(hotbar);
        EchoClientToolState safeToolState = toolState == null ? EchoClientToolState.empty() : toolState;
        if (cachedInventoryScreenModel != null
                && cachedInventoryScreenModelVersion == playerInventoryVersion
                && cachedInventorySelectedSlot == selectedSlot
                && cachedInventoryToolState.equals(safeToolState)) {
            return cachedInventoryScreenModel;
        }
        cachedInventoryScreenModel =
                EchoClientInventoryScreenModel.fromItemContainer(playerInventory, selectedSlot, safeToolState);
        cachedInventoryScreenModelVersion = playerInventoryVersion;
        cachedInventorySelectedSlot = selectedSlot;
        cachedInventoryToolState = safeToolState;
        return cachedInventoryScreenModel;
    }

    EchoClientInventoryScreenModel containerScreenModel(EchoClientToolState toolState) {
        EchoClientToolState safeToolState = toolState == null ? EchoClientToolState.empty() : toolState;
        if (cachedContainerScreenModel != null
                && cachedContainerScreenModelVersion == containerInventoryVersion
                && cachedContainerToolState.equals(safeToolState)) {
            return cachedContainerScreenModel;
        }
        cachedContainerScreenModel = EchoClientInventoryScreenModel.fromItemContainer(openContainer, 0, safeToolState);
        cachedContainerScreenModelVersion = containerInventoryVersion;
        cachedContainerToolState = safeToolState;
        return cachedContainerScreenModel;
    }

    long playerInventoryVersion() {
        return playerInventoryVersion;
    }

    long containerInventoryVersion() {
        return containerInventoryVersion;
    }

    List<EchoClientWorkbenchRecipeSummary> workbenchRecipeSummaries() {
        return workbenchRecipes.stream()
                .map(this::workbenchRecipeSummary)
                .toList();
    }

    EchoClientWorkbenchScreenModel workbenchScreenModel(String selectedRecipeId) {
        return new EchoClientWorkbenchScreenModel(
                "echoscreencore:workbench",
                "Workbench",
                workbenchRecipeSummaries(),
                workbenchRecipeDetail(selectedRecipeId)
        );
    }

    WorkbenchCraftResult craftWorkbenchRecipe(String recipeId, EchoVoxelPlayerHotbar hotbar) {
        if (recipeId == null || recipeId.isBlank()) {
            return new WorkbenchCraftResult(
                    new EchoItemCraftResult("missing:recipe", false, 0, "missing_recipe"),
                    0,
                    ""
            );
        }
        EchoItemRecipe recipe = workbenchRecipes.stream()
                .filter(candidate -> candidate.recipeId().equals(recipeId))
                .findFirst()
                .orElse(null);
        if (recipe == null) {
            return new WorkbenchCraftResult(new EchoItemCraftResult(recipeId, false, 0, "unknown_recipe"), 0, "");
        }
        EchoItemCraftResult result = craftingSystem.craft(playerInventory, recipe);
        if (!result.crafted()) {
            return new WorkbenchCraftResult(result, 0, "");
        }
        markPlayerInventoryChanged();
        syncHotbarFromInventory(hotbar);
        return new WorkbenchCraftResult(
                result,
                EchoClientGameSimulationRules.craftExperience(recipe),
                "craft:" + recipe.recipeId()
        );
    }

    boolean inventorySlotEmpty(int slotIndex) {
        return playerInventory.slot(slotIndex).empty();
    }

    boolean containerSlotEmpty(int slotIndex) {
        return openContainer.slot(slotIndex).empty();
    }

    boolean cursorStackHeld() {
        return cursorStack != null;
    }

    EchoClientSlotStack cursorSlotStack(EchoClientToolState toolState) {
        return cursorStack == null
                ? EchoClientSlotStack.empty(0)
                : EchoClientSlotStack.fromItemStack(0, cursorStack, toolState);
    }

    boolean primaryClickInventorySlot(int slotIndex, EchoVoxelPlayerHotbar hotbar) {
        boolean changed = primaryClickSlot(playerInventory, slotIndex);
        if (changed) {
            markPlayerInventoryChanged();
            syncHotbarFromInventory(hotbar);
        }
        return changed;
    }

    boolean secondaryClickInventorySlot(int slotIndex, EchoVoxelPlayerHotbar hotbar) {
        boolean changed = secondaryClickSlot(playerInventory, slotIndex);
        if (changed) {
            markPlayerInventoryChanged();
            syncHotbarFromInventory(hotbar);
        }
        return changed;
    }

    boolean primaryClickContainerSlot(int slotIndex) {
        boolean changed = primaryClickSlot(openContainer, slotIndex);
        if (changed) {
            markContainerInventoryChanged();
        }
        return changed;
    }

    boolean secondaryClickContainerSlot(int slotIndex) {
        boolean changed = secondaryClickSlot(openContainer, slotIndex);
        if (changed) {
            markContainerInventoryChanged();
        }
        return changed;
    }

    boolean returnCursorStackToInventory(EchoVoxelPlayerHotbar hotbar) {
        if (cursorStack == null) {
            return true;
        }
        EchoInventoryOperationResult added = collectItemStack(cursorStack);
        if (added.quantity() <= 0) {
            return false;
        }
        cursorStack = added.quantity() >= cursorStack.quantity()
                ? null
                : cursorStack.withQuantity(cursorStack.quantity() - added.quantity());
        syncHotbarFromInventory(hotbar);
        return cursorStack == null;
    }

    EquipmentSlotClickResult clickEquipmentSlot(
            EchoClientArmorSlot armorSlot,
            EchoClientEquipmentState equipment,
            EchoVoxelPlayerHotbar hotbar
    ) {
        if (armorSlot == null) {
            return new EquipmentSlotClickResult(false, safeEquipment(equipment));
        }
        EchoClientEquipmentState safeEquipment = safeEquipment(equipment);
        Optional<EchoClientArmorPiece> currentPiece = safeEquipment.piece(armorSlot);
        if (cursorStack == null) {
            if (currentPiece.isEmpty()) {
                return new EquipmentSlotClickResult(false, safeEquipment);
            }
            cursorStack = currentPiece.orElseThrow().toStack();
            syncHotbarFromInventory(hotbar);
            return new EquipmentSlotClickResult(true, safeEquipment.unequip(armorSlot));
        }

        Optional<EchoClientArmorPiece> cursorArmor = EchoClientArmorPiece.fromItem(cursorStack.definition());
        if (cursorArmor.isEmpty() || cursorArmor.orElseThrow().slot() != armorSlot) {
            return new EquipmentSlotClickResult(false, safeEquipment);
        }
        if (currentPiece.isPresent() && cursorStack.quantity() > 1) {
            return new EquipmentSlotClickResult(false, safeEquipment);
        }

        EchoClientEquipmentState nextEquipment = safeEquipment.equip(cursorArmor.orElseThrow());
        EchoItemStack remainingCursor = cursorStack.remove(1).orElse(null);
        cursorStack = currentPiece.map(EchoClientArmorPiece::toStack).orElse(remainingCursor);
        syncHotbarFromInventory(hotbar);
        return new EquipmentSlotClickResult(true, nextEquipment);
    }

    EquipmentSlotClickResult primaryClickOffhandSlot(
            EchoClientEquipmentState equipment,
            EchoVoxelPlayerHotbar hotbar
    ) {
        EchoClientEquipmentState safeEquipment = safeEquipment(equipment);
        EchoItemStack offhandStack = safeEquipment.offhandStack();
        if (cursorStack == null) {
            if (offhandStack == null) {
                return new EquipmentSlotClickResult(false, safeEquipment);
            }
            cursorStack = offhandStack;
            return new EquipmentSlotClickResult(true, safeEquipment.withoutOffhand());
        }
        if (offhandStack == null) {
            EchoClientEquipmentState nextEquipment = safeEquipment.withOffhand(cursorStack);
            cursorStack = null;
            syncHotbarFromInventory(hotbar);
            return new EquipmentSlotClickResult(true, nextEquipment);
        }
        if (offhandStack.canMerge(cursorStack)) {
            int moved = Math.min(cursorStack.quantity(), offhandStack.spaceRemaining());
            if (moved <= 0) {
                return new EquipmentSlotClickResult(false, safeEquipment);
            }
            EchoClientEquipmentState nextEquipment = safeEquipment.withOffhand(offhandStack.add(moved));
            cursorStack = cursorStack.remove(moved).orElse(null);
            syncHotbarFromInventory(hotbar);
            return new EquipmentSlotClickResult(true, nextEquipment);
        }
        EchoClientEquipmentState nextEquipment = safeEquipment.withOffhand(cursorStack);
        cursorStack = offhandStack;
        syncHotbarFromInventory(hotbar);
        return new EquipmentSlotClickResult(true, nextEquipment);
    }

    EquipmentSlotClickResult secondaryClickOffhandSlot(
            EchoClientEquipmentState equipment,
            EchoVoxelPlayerHotbar hotbar
    ) {
        EchoClientEquipmentState safeEquipment = safeEquipment(equipment);
        EchoItemStack offhandStack = safeEquipment.offhandStack();
        if (cursorStack == null) {
            if (offhandStack == null) {
                return new EquipmentSlotClickResult(false, safeEquipment);
            }
            int moved = (offhandStack.quantity() + 1) / 2;
            cursorStack = offhandStack.withQuantity(moved);
            EchoClientEquipmentState nextEquipment = offhandStack.remove(moved)
                    .map(safeEquipment::withOffhand)
                    .orElseGet(safeEquipment::withoutOffhand);
            return new EquipmentSlotClickResult(true, nextEquipment);
        }
        if (offhandStack == null) {
            EchoClientEquipmentState nextEquipment = safeEquipment.withOffhand(cursorStack.withQuantity(1));
            cursorStack = cursorStack.remove(1).orElse(null);
            syncHotbarFromInventory(hotbar);
            return new EquipmentSlotClickResult(true, nextEquipment);
        }
        if (!offhandStack.canMerge(cursorStack) || offhandStack.spaceRemaining() <= 0) {
            return new EquipmentSlotClickResult(false, safeEquipment);
        }
        EchoClientEquipmentState nextEquipment = safeEquipment.withOffhand(offhandStack.add(1));
        cursorStack = cursorStack.remove(1).orElse(null);
        syncHotbarFromInventory(hotbar);
        return new EquipmentSlotClickResult(true, nextEquipment);
    }

    EquipmentSlotClickResult swapSelectedWithOffhand(
            EchoVoxelPlayerHotbar hotbar,
            EchoClientEquipmentState equipment
    ) {
        EchoClientEquipmentState safeEquipment = safeEquipment(equipment);
        EchoInventorySlot selectedSlot = selectedInventorySlot(hotbar).orElse(null);
        if (selectedSlot == null) {
            return new EquipmentSlotClickResult(false, safeEquipment);
        }
        EchoItemStack selectedStack = selectedSlot.stack().orElse(null);
        EchoItemStack offhandStack = safeEquipment.offhandStack();
        if (selectedStack == null && offhandStack == null) {
            return new EquipmentSlotClickResult(false, safeEquipment);
        }
        if (offhandStack == null) {
            selectedSlot.clear();
            markPlayerInventoryChanged();
            syncHotbarFromInventory(hotbar);
            return new EquipmentSlotClickResult(true, safeEquipment.withOffhand(selectedStack));
        }
        selectedSlot.setStack(offhandStack);
        markPlayerInventoryChanged();
        EchoClientEquipmentState nextEquipment = selectedStack == null
                ? safeEquipment.withoutOffhand()
                : safeEquipment.withOffhand(selectedStack);
        syncHotbarFromInventory(hotbar);
        return new EquipmentSlotClickResult(true, nextEquipment);
    }

    EchoInventoryTransferResult moveOrMergeInventorySlot(int sourceSlot, int targetSlot, EchoVoxelPlayerHotbar hotbar) {
        EchoInventoryTransferResult result = operations.moveOrMergeSlot(playerInventory, sourceSlot, targetSlot);
        markPlayerInventoryIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    EchoInventoryTransferResult splitInventorySlotTo(int sourceSlot, int targetSlot, EchoVoxelPlayerHotbar hotbar) {
        EchoInventoryTransferResult result = operations.splitSlotTo(playerInventory, sourceSlot, targetSlot);
        markPlayerInventoryIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    EchoInventoryTransferResult moveOrMergeContainerSlot(int sourceSlot, int targetSlot) {
        EchoInventoryTransferResult result = operations.moveOrMergeSlot(openContainer, sourceSlot, targetSlot);
        markContainerInventoryIfSuccessful(result);
        return result;
    }

    EchoInventoryTransferResult splitContainerSlotTo(int sourceSlot, int targetSlot) {
        EchoInventoryTransferResult result = operations.splitSlotTo(openContainer, sourceSlot, targetSlot);
        markContainerInventoryIfSuccessful(result);
        return result;
    }

    EchoInventoryTransferResult quickMoveContainerSlotToPlayer(int sourceSlot, EchoVoxelPlayerHotbar hotbar) {
        if (openContainer.slot(sourceSlot).empty()) {
            return new EchoInventoryTransferResult(openContainer.id(), sourceSlot, playerInventory.id(), false, 0, "empty_source");
        }
        int quantity = openContainer.slot(sourceSlot).stack().orElseThrow().quantity();
        EchoInventoryTransferResult result = operations.transfer(openContainer, sourceSlot, playerInventory, quantity);
        markPlayerAndContainerIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    EchoInventoryTransferResult quickMoveInventorySlotToContainer(int sourceSlot, EchoVoxelPlayerHotbar hotbar) {
        if (playerInventory.slot(sourceSlot).empty()) {
            return new EchoInventoryTransferResult(playerInventory.id(), sourceSlot, openContainer.id(), false, 0, "empty_source");
        }
        int quantity = playerInventory.slot(sourceSlot).stack().orElseThrow().quantity();
        EchoInventoryTransferResult result = operations.transfer(playerInventory, sourceSlot, openContainer, quantity);
        markPlayerAndContainerIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    EchoInventoryTransferResult swapContainerSlotWithHotbar(int sourceSlot, int hotbarSlot, EchoVoxelPlayerHotbar hotbar) {
        EchoInventoryTransferResult result = operations.swapSlots(openContainer, sourceSlot, playerInventory, hotbarSlot);
        markPlayerAndContainerIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    EchoInventoryTransferResult quickMoveInventorySlot(int sourceSlot, EchoVoxelPlayerHotbar hotbar) {
        int targetStart = sourceSlot < EchoVoxelPlayerHotbar.CARRY_START
                ? EchoVoxelPlayerHotbar.CARRY_START
                : 0;
        int targetEnd = sourceSlot < EchoVoxelPlayerHotbar.CARRY_START
                ? EchoVoxelPlayerHotbar.SLOT_COUNT
                : EchoVoxelPlayerHotbar.HOTBAR_COUNT;
        EchoInventoryTransferResult result = operations.quickMoveSlot(
                playerInventory,
                sourceSlot,
                targetStart,
                targetEnd
        );
        markPlayerInventoryIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    EchoInventoryTransferResult swapInventorySlots(int sourceSlot, int hotbarSlot, EchoVoxelPlayerHotbar hotbar) {
        EchoInventoryTransferResult result = operations.swapSlots(playerInventory, sourceSlot, hotbarSlot);
        markPlayerInventoryIfSuccessful(result);
        syncHotbarIfSuccessful(result, hotbar);
        return result;
    }

    void syncInventoryFromHotbar(EchoVoxelPlayerHotbar hotbar) {
        if (hotbar == null) {
            return;
        }
        boolean changed = false;
        for (EchoVoxelHotbarSlot hotbarSlot : hotbar.slots()) {
            var inventorySlot = playerInventory.slot(hotbarSlot.index());
            if (!hotbarSlot.empty()) {
                String blockItemId = hotbarSlot.block().id();
                EchoItemStack current = inventorySlot.stack().orElse(null);
                if (current != null
                        && current.itemId().value().equals(blockItemId)
                        && current.quantity() == hotbarSlot.count()) {
                    continue;
                }
                inventorySlot.clear();
                inventorySlot.setStack(new EchoItemStack(blockItemDefinition(hotbarSlot.block()), hotbarSlot.count()));
                changed = true;
            } else if (inventorySlot.stack().isPresent()
                    && isLiveBlockItem(inventorySlot.stack().orElseThrow())) {
                inventorySlot.clear();
                changed = true;
            }
        }
        if (changed) {
            markPlayerInventoryChanged();
        }
    }

    void syncHotbarFromInventory(EchoVoxelPlayerHotbar hotbar) {
        if (hotbar == null) {
            return;
        }
        for (int index = 0; index < EchoVoxelPlayerHotbar.SLOT_COUNT; index++) {
            var inventorySlot = playerInventory.slot(index);
            if (inventorySlot.empty()) {
                hotbar.assignSlot(index, EchoVoxelBlock.AIR, 0);
                continue;
            }
            EchoItemStack stack = inventorySlot.stack().orElseThrow();
            var block = bridge.registry()
                    .findLiveVoxelId(stack.itemId().value())
                    .map(entry -> entry.requireVoxelBlock())
                    .orElse(EchoVoxelBlock.AIR);
            hotbar.assignSlot(index, block, stack.quantity());
        }
    }

    void applyInventorySnapshot(List<EchoClientInventorySlotSnapshot> inventorySlots, EchoVoxelPlayerHotbar hotbar) {
        if (inventorySlots == null || inventorySlots.isEmpty()) {
            return;
        }
        for (var slot : playerInventory.slots()) {
            slot.clear();
        }
        for (EchoClientInventorySlotSnapshot snapshot : inventorySlots) {
            if (snapshot.index() >= playerInventory.capacity()) {
                continue;
            }
            registerDefinition(snapshot.definition());
            playerInventory.slot(snapshot.index()).setStack(new EchoItemStack(snapshot.definition(), snapshot.count()));
        }
        markPlayerInventoryChanged();
        syncHotbarFromInventory(hotbar);
    }

    void applyContainerSnapshot(List<EchoClientInventorySlotSnapshot> containerSlots) {
        if (containerSlots == null || containerSlots.isEmpty()) {
            return;
        }
        for (var slot : openContainer.slots()) {
            slot.clear();
        }
        for (EchoClientInventorySlotSnapshot snapshot : containerSlots) {
            if (snapshot.index() >= openContainer.capacity()) {
                continue;
            }
            registerDefinition(snapshot.definition());
            openContainer.slot(snapshot.index()).setStack(new EchoItemStack(snapshot.definition(), snapshot.count()));
        }
        markContainerInventoryChanged();
    }

    Optional<EchoItemDefinition> selectedItemDefinition(EchoVoxelPlayerHotbar hotbar) {
        int slotIndex = selectedSlot(hotbar);
        if (slotIndex < 0 || slotIndex >= playerInventory.capacity()) {
            return Optional.empty();
        }
        var slot = playerInventory.slot(slotIndex);
        if (slot.empty()) {
            return Optional.empty();
        }
        return slot.stack().map(EchoItemStack::definition);
    }

    void collectBlockDrop(EchoVoxelBlock block) {
        for (EchoItemStack stack : blockDropStacks(block)) {
            collectItemStack(stack);
        }
    }

    EchoItemStack blockDropStack(EchoVoxelBlock block) {
        List<EchoItemStack> stacks = blockDropStacks(block);
        return stacks.isEmpty() ? null : stacks.getFirst();
    }

    List<EchoItemStack> blockDropStacks(EchoVoxelBlock block) {
        EchoVoxelBlock safeBlock = block == null ? EchoVoxelBlock.AIR : block;
        if (safeBlock.air()) {
            return List.of();
        }
        Optional<EchoLootTable> table = blockLootTable(safeBlock);
        if (table.isPresent()) {
            ArrayList<EchoItemStack> stacks = new ArrayList<>();
            for (EchoLootEntry entry : table.orElseThrow().entries()) {
                EchoItemDefinition definition = itemRegistry.require(entry.itemId());
                addChunkedStacks(stacks, definition, entry.quantity());
            }
            if (!stacks.isEmpty()) {
                return List.copyOf(stacks);
            }
        }
        EchoItemDefinition definition = blockItemDefinition(safeBlock);
        return List.of(new EchoItemStack(definition, 1));
    }

    EchoInventoryOperationResult collectItemStack(EchoItemStack stack) {
        if (stack == null) {
            return new EchoInventoryOperationResult("add", false, 0, "missing_stack");
        }
        EchoItemDefinition definition = stack.definition();
        registerDefinition(definition);
        EchoInventoryOperationResult result = operations.add(playerInventory, stack);
        if (result.quantity() > 0) {
            markPlayerInventoryChanged();
        }
        return result;
    }

    EchoInventoryOperationResult collectItemStack(EchoItemStack stack, EchoVoxelPlayerHotbar hotbar) {
        EchoInventoryOperationResult result = collectItemStack(stack);
        if (result.quantity() > 0) {
            syncHotbarFromInventory(hotbar);
        }
        return result;
    }

    int availableSpace(EchoItemDefinition definition) {
        if (definition == null) {
            return 0;
        }
        registerDefinition(definition);
        return operations.availableSpace(playerInventory, definition);
    }

    int itemCount(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return 0;
        }
        return operations.count(playerInventory, new EchoItemId(itemId.trim()));
    }

    EchoInventoryOperationResult consumeItem(String itemId, int quantity, EchoVoxelPlayerHotbar hotbar) {
        if (itemId == null || itemId.isBlank()) {
            return new EchoInventoryOperationResult("consume", false, 0, "missing_item");
        }
        if (quantity <= 0) {
            return new EchoInventoryOperationResult("consume", false, 0, "missing_quantity");
        }
        EchoInventoryOperationResult result = operations.consume(playerInventory, new EchoItemId(itemId.trim()), quantity);
        if (result.success()) {
            markPlayerInventoryChanged();
            syncHotbarFromInventory(hotbar);
        }
        return result;
    }

    Optional<EchoItemStack> removeSelectedItemStack(EchoVoxelPlayerHotbar hotbar, int quantity) {
        if (quantity <= 0) {
            return Optional.empty();
        }
        Optional<SelectedStack> selected = selectedStack(hotbar);
        if (selected.isEmpty()) {
            return Optional.empty();
        }
        EchoItemStack stack = selected.orElseThrow().stack();
        int removed = Math.min(quantity, stack.quantity());
        EchoItemStack removedStack = new EchoItemStack(stack.definition(), removed);
        stack.remove(removed).ifPresentOrElse(selected.orElseThrow().slot()::setStack, selected.orElseThrow().slot()::clear);
        markPlayerInventoryChanged();
        syncHotbarFromInventory(hotbar);
        return Optional.of(removedStack);
    }

    Optional<EchoItemStack> removeCursorStack(int quantity) {
        if (cursorStack == null || quantity <= 0) {
            return Optional.empty();
        }
        int removed = Math.min(quantity, cursorStack.quantity());
        EchoItemStack removedStack = new EchoItemStack(cursorStack.definition(), removed);
        cursorStack = cursorStack.remove(removed).orElse(null);
        return Optional.of(removedStack);
    }

    Optional<EchoItemStack> removeInventorySlotStack(
            int slotIndex,
            int quantity,
            EchoVoxelPlayerHotbar hotbar
    ) {
        Optional<EchoItemStack> removed = removeSlotStack(playerInventory, slotIndex, quantity);
        if (removed.isPresent()) {
            markPlayerInventoryChanged();
            syncHotbarFromInventory(hotbar);
        }
        return removed;
    }

    Optional<EchoItemStack> removeContainerSlotStack(int slotIndex, int quantity) {
        Optional<EchoItemStack> removed = removeSlotStack(openContainer, slotIndex, quantity);
        if (removed.isPresent()) {
            markContainerInventoryChanged();
        }
        return removed;
    }

    ConsumableUseResult consumeSelectedConsumable(EchoVoxelPlayerHotbar hotbar, EchoClientPlayerVitals playerVitals) {
        Optional<SelectedStack> selected = selectedStack(hotbar);
        if (selected.isEmpty()) {
            return new ConsumableUseResult(false, playerVitals);
        }
        EchoItemStack stack = selected.orElseThrow().stack();
        EchoItemDefinition definition = stack.definition();
        if (!EchoClientGameSimulationRules.canConsume(playerVitals, definition)) {
            return new ConsumableUseResult(false, playerVitals);
        }

        EchoClientPlayerVitals nextVitals = EchoClientGameSimulationRules.consume(playerVitals, definition);
        stack.remove(1).ifPresentOrElse(selected.orElseThrow().slot()::setStack, selected.orElseThrow().slot()::clear);
        markPlayerInventoryChanged();
        syncHotbarFromInventory(hotbar);
        return new ConsumableUseResult(true, nextVitals);
    }

    ArmorEquipResult equipSelectedArmor(EchoVoxelPlayerHotbar hotbar, EchoClientEquipmentState equipment) {
        Optional<SelectedStack> selected = selectedStack(hotbar);
        if (selected.isEmpty()) {
            return new ArmorEquipResult(false, equipment);
        }
        EchoItemStack stack = selected.orElseThrow().stack();
        Optional<EchoClientArmorPiece> armor = EchoClientArmorPiece.fromItem(stack.definition());
        if (armor.isEmpty()) {
            return new ArmorEquipResult(false, equipment);
        }
        EchoClientArmorPiece nextPiece = armor.orElseThrow();
        EchoClientEquipmentState safeEquipment = equipment == null ? EchoClientEquipmentState.empty() : equipment;
        Optional<EchoClientArmorPiece> previousPiece = safeEquipment.piece(nextPiece.slot());
        stack.remove(1).ifPresentOrElse(selected.orElseThrow().slot()::setStack, selected.orElseThrow().slot()::clear);
        EchoClientEquipmentState nextEquipment = safeEquipment.equip(nextPiece);
        previousPiece.ifPresent(piece -> operations.add(playerInventory, piece.toStack()));
        markPlayerInventoryChanged();
        syncHotbarFromInventory(hotbar);
        return new ArmorEquipResult(true, nextEquipment);
    }

    EchoClientToolState damageSelectedTool(
            EchoVoxelPlayerHotbar hotbar,
            EchoClientToolState toolState,
            EchoVoxelBlock block
    ) {
        Optional<SelectedStack> selected = selectedStack(hotbar);
        if (selected.isEmpty()) {
            return toolState;
        }
        EchoClientGameSimulationRules.ToolDamageResult result =
                EchoClientGameSimulationRules.damageTool(toolState, selected.orElseThrow().stack().definition(), block);
        if (result.consumeStack()) {
            selected.orElseThrow().stack().remove(1).ifPresentOrElse(
                    selected.orElseThrow().slot()::setStack,
                    selected.orElseThrow().slot()::clear
            );
            markPlayerInventoryChanged();
        }
        return result.toolState();
    }

    EchoClientToolState damageSelectedTool(
            EchoVoxelPlayerHotbar hotbar,
            EchoClientToolState toolState,
            int wear
    ) {
        EchoClientToolState safeToolState = toolState == null ? EchoClientToolState.empty() : toolState;
        Optional<SelectedStack> selected = selectedStack(hotbar);
        if (selected.isEmpty() || wear <= 0) {
            return safeToolState;
        }
        EchoItemDefinition definition = selected.orElseThrow().stack().definition();
        if (!EchoClientToolState.isTool(definition)) {
            return safeToolState;
        }
        int remaining = Math.max(
                0,
                safeToolState.status(definition, EchoVoxelBlock.AIR).durability() - wear
        );
        EchoClientToolState nextToolState = safeToolState.damage(definition, wear);
        if (remaining <= 0) {
            nextToolState = nextToolState.remove(definition);
            selected.orElseThrow().stack().remove(1).ifPresentOrElse(
                    selected.orElseThrow().slot()::setStack,
                    selected.orElseThrow().slot()::clear
            );
            markPlayerInventoryChanged();
        }
        return nextToolState;
    }

    static EchoInventoryContainer newPlayerInventory() {
        return EchoClientStarterLoadout.empty().newPlayerInventory();
    }

    private List<EchoClientInventorySlotSnapshot> snapshots(EchoInventoryContainer container) {
        return container.slots().stream()
                .filter(slot -> !slot.empty())
                .map(EchoClientInventorySlotSnapshot::fromSlot)
                .toList();
    }

    private EchoClientWorkbenchRecipeSummary workbenchRecipeSummary(EchoItemRecipe recipe) {
        EchoItemDefinition output = itemRegistry.require(recipe.outputItemId());
        boolean craftable = canCraft(recipe);
        String label = output.displayName() + (recipe.outputQuantity() > 1 ? " x" + recipe.outputQuantity() : "");
        String tooltip = recipe.recipeId()
                + " | " + ingredientText(recipe)
                + " -> " + output.id().value();
        return new EchoClientWorkbenchRecipeSummary(recipe.recipeId(), label, tooltip, craftable);
    }

    private EchoClientWorkbenchRecipeDetail workbenchRecipeDetail(String recipeId) {
        EchoItemRecipe recipe = selectedWorkbenchRecipe(recipeId);
        if (recipe == null) {
            return EchoClientWorkbenchRecipeDetail.empty();
        }
        EchoClientWorkbenchRecipeSummary summary = workbenchRecipeSummary(recipe);
        ArrayList<EchoClientSlotStack> ingredients = new ArrayList<>();
        int index = 0;
        for (Map.Entry<EchoItemId, Integer> ingredient : recipe.ingredients().entrySet()) {
            ingredients.add(EchoClientSlotStack.fromItemDefinition(
                    index++,
                    itemRegistry.require(ingredient.getKey()),
                    ingredient.getValue()
            ));
        }
        EchoClientSlotStack output = EchoClientSlotStack.fromItemDefinition(
                98,
                itemRegistry.require(recipe.outputItemId()),
                recipe.outputQuantity()
        );
        return new EchoClientWorkbenchRecipeDetail(
                summary.recipeId(),
                summary.label(),
                summary.tooltip(),
                summary.craftable(),
                ingredients,
                output,
                summary.craftable() ? "Ready to craft" : missingIngredientStatus(recipe)
        );
    }

    private EchoItemRecipe selectedWorkbenchRecipe(String recipeId) {
        if (workbenchRecipes.isEmpty()) {
            return null;
        }
        if (recipeId != null && !recipeId.isBlank()) {
            for (EchoItemRecipe recipe : workbenchRecipes) {
                if (recipe.recipeId().equals(recipeId)) {
                    return recipe;
                }
            }
        }
        return workbenchRecipes.stream()
                .filter(this::canCraft)
                .findFirst()
                .orElse(workbenchRecipes.getFirst());
    }

    private boolean canCraft(EchoItemRecipe recipe) {
        for (Map.Entry<EchoItemId, Integer> ingredient : recipe.ingredients().entrySet()) {
            if (operations.count(playerInventory, ingredient.getKey()) < ingredient.getValue()) {
                return false;
            }
        }
        EchoItemDefinition output = itemRegistry.require(recipe.outputItemId());
        return operations.availableSpace(playerInventory, output) >= recipe.outputQuantity();
    }

    private String missingIngredientStatus(EchoItemRecipe recipe) {
        for (Map.Entry<EchoItemId, Integer> ingredient : recipe.ingredients().entrySet()) {
            int held = operations.count(playerInventory, ingredient.getKey());
            if (held < ingredient.getValue()) {
                return "Missing " + itemRegistry.require(ingredient.getKey()).displayName()
                        + " x" + (ingredient.getValue() - held);
            }
        }
        EchoItemDefinition output = itemRegistry.require(recipe.outputItemId());
        if (operations.availableSpace(playerInventory, output) < recipe.outputQuantity()) {
            return "Inventory full";
        }
        return "Unavailable";
    }

    private String ingredientText(EchoItemRecipe recipe) {
        return recipe.ingredients().entrySet().stream()
                .map(entry -> itemRegistry.require(entry.getKey()).displayName() + " x" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("No ingredients");
    }

    private Optional<SelectedStack> selectedStack(EchoVoxelPlayerHotbar hotbar) {
        Optional<EchoInventorySlot> selectedSlot = selectedInventorySlot(hotbar);
        if (selectedSlot.isEmpty()) {
            return Optional.empty();
        }
        var slot = selectedSlot.orElseThrow();
        if (slot.empty()) {
            return Optional.empty();
        }
        return slot.stack().map(stack -> new SelectedStack(slot, stack));
    }

    private Optional<EchoInventorySlot> selectedInventorySlot(EchoVoxelPlayerHotbar hotbar) {
        int slotIndex = selectedSlot(hotbar);
        if (slotIndex < 0 || slotIndex >= playerInventory.capacity()) {
            return Optional.empty();
        }
        return Optional.of(playerInventory.slot(slotIndex));
    }

    private static Optional<EchoItemStack> removeSlotStack(
            EchoInventoryContainer container,
            int slotIndex,
            int quantity
    ) {
        if (quantity <= 0) {
            return Optional.empty();
        }
        EchoInventorySlot slot = slotOrNull(container, slotIndex);
        if (slot == null || slot.empty()) {
            return Optional.empty();
        }
        EchoItemStack stack = slot.stack().orElseThrow();
        int removed = Math.min(quantity, stack.quantity());
        EchoItemStack removedStack = new EchoItemStack(stack.definition(), removed);
        stack.remove(removed).ifPresentOrElse(slot::setStack, slot::clear);
        return Optional.of(removedStack);
    }

    private int selectedSlot(EchoVoxelPlayerHotbar hotbar) {
        return hotbar == null ? -1 : hotbar.selectedSlot();
    }

    private void syncHotbarIfSuccessful(EchoInventoryTransferResult result, EchoVoxelPlayerHotbar hotbar) {
        if (result.success()) {
            syncHotbarFromInventory(hotbar);
        }
    }

    private void markPlayerInventoryIfSuccessful(EchoInventoryTransferResult result) {
        if (result != null && result.success()) {
            markPlayerInventoryChanged();
        }
    }

    private void markContainerInventoryIfSuccessful(EchoInventoryTransferResult result) {
        if (result != null && result.success()) {
            markContainerInventoryChanged();
        }
    }

    private void markPlayerAndContainerIfSuccessful(EchoInventoryTransferResult result) {
        if (result != null && result.success()) {
            markPlayerInventoryChanged();
            markContainerInventoryChanged();
        }
    }

    private void markPlayerInventoryChanged() {
        playerInventoryVersion++;
        cachedInventoryScreenModel = null;
        cachedInventoryScreenModelVersion = -1L;
    }

    private void markContainerInventoryChanged() {
        containerInventoryVersion++;
        cachedContainerScreenModel = null;
        cachedContainerScreenModelVersion = -1L;
    }

    private void invalidateInventoryModels() {
        markPlayerInventoryChanged();
        markContainerInventoryChanged();
    }

    private static EchoClientEquipmentState safeEquipment(EchoClientEquipmentState equipment) {
        return equipment == null ? EchoClientEquipmentState.empty() : equipment;
    }

    private boolean primaryClickSlot(EchoInventoryContainer container, int slotIndex) {
        EchoInventorySlot slot = slotOrNull(container, slotIndex);
        if (slot == null) {
            return false;
        }
        synchronized (container) {
            if (cursorStack == null) {
                if (slot.empty()) {
                    return false;
                }
                cursorStack = slot.stack().orElseThrow();
                slot.clear();
                return true;
            }
            if (slot.empty()) {
                slot.setStack(cursorStack);
                cursorStack = null;
                return true;
            }
            EchoItemStack slotStack = slot.stack().orElseThrow();
            if (slotStack.canMerge(cursorStack)) {
                int moved = Math.min(cursorStack.quantity(), slotStack.spaceRemaining());
                if (moved <= 0) {
                    return false;
                }
                slot.setStack(slotStack.add(moved));
                cursorStack = cursorStack.remove(moved).orElse(null);
                return true;
            }
            slot.setStack(cursorStack);
            cursorStack = slotStack;
            return true;
        }
    }

    private boolean secondaryClickSlot(EchoInventoryContainer container, int slotIndex) {
        EchoInventorySlot slot = slotOrNull(container, slotIndex);
        if (slot == null) {
            return false;
        }
        synchronized (container) {
            if (cursorStack == null) {
                if (slot.empty()) {
                    return false;
                }
                EchoItemStack slotStack = slot.stack().orElseThrow();
                int moved = (slotStack.quantity() + 1) / 2;
                cursorStack = slotStack.withQuantity(moved);
                slotStack.remove(moved).ifPresentOrElse(slot::setStack, slot::clear);
                return true;
            }
            if (slot.empty()) {
                slot.setStack(cursorStack.withQuantity(1));
                cursorStack = cursorStack.remove(1).orElse(null);
                return true;
            }
            EchoItemStack slotStack = slot.stack().orElseThrow();
            if (!slotStack.canMerge(cursorStack) || slotStack.spaceRemaining() <= 0) {
                return false;
            }
            slot.setStack(slotStack.add(1));
            cursorStack = cursorStack.remove(1).orElse(null);
            return true;
        }
    }

    private static EchoInventorySlot slotOrNull(EchoInventoryContainer container, int slotIndex) {
        if (container == null || slotIndex < 0 || slotIndex >= container.capacity()) {
            return null;
        }
        return container.slot(slotIndex);
    }

    private static EchoItemDefinition blockItemDefinition(EchoVoxelBlock block) {
        return new EchoItemDefinition(
                new EchoItemId(block.id()),
                block.displayName(),
                EchoItemCategory.MATERIAL,
                EchoVoxelPlayerHotbar.MAX_STACK,
                1.0D,
                List.of("block", "voxel"),
                List.of("Placeable voxel block")
        );
    }

    private boolean isLiveBlockItem(EchoItemStack stack) {
        return bridge.registry().findLiveVoxelId(stack.itemId().value()).isPresent();
    }

    private Optional<EchoLootTable> blockLootTable(EchoVoxelBlock block) {
        for (String id : blockLootTableIds(block)) {
            EchoLootTable table = blockLootTables.get(id);
            if (table != null) {
                return Optional.of(table);
            }
        }
        return Optional.empty();
    }

    private static Map<String, EchoLootTable> blockLootTables(List<EchoLootTable> lootTables) {
        if (lootTables == null || lootTables.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, EchoLootTable> tables = new LinkedHashMap<>();
        for (EchoLootTable table : lootTables) {
            if (table == null || !table.tableId().contains(":")) {
                continue;
            }
            tables.put(table.tableId(), table);
        }
        return Map.copyOf(tables);
    }

    private static List<String> blockLootTableIds(EchoVoxelBlock block) {
        if (block == null || block.air() || !block.id().contains(":")) {
            return List.of();
        }
        String namespace = block.id().substring(0, block.id().indexOf(':'));
        String path = block.id().substring(block.id().indexOf(':') + 1);
        return List.of(
                namespace + ":blocks/" + path,
                namespace + ":block/" + path
        );
    }

    private static void addChunkedStacks(
            List<EchoItemStack> stacks,
            EchoItemDefinition definition,
            int quantity
    ) {
        int remaining = Math.max(0, quantity);
        int maxStackSize = Math.max(1, definition.maxStackSize());
        while (remaining > 0) {
            int moved = Math.min(remaining, maxStackSize);
            stacks.add(new EchoItemStack(definition, moved));
            remaining -= moved;
        }
    }

    private void registerDefinition(EchoItemDefinition definition) {
        if (itemRegistry.find(definition.id()).isEmpty()) {
            itemRegistry.register(definition);
        }
    }

    record WorkbenchCraftResult(
            EchoItemCraftResult result,
            int experience,
            String milestone
    ) {
    }

    record ConsumableUseResult(
            boolean consumed,
            EchoClientPlayerVitals vitals
    ) {
    }

    record ArmorEquipResult(
            boolean equipped,
            EchoClientEquipmentState equipment
    ) {
    }

    record EquipmentSlotClickResult(
            boolean changed,
            EchoClientEquipmentState equipment
    ) {
    }

    private record SelectedStack(
            dev.echo.standalone.runtime.item.EchoInventorySlot slot,
            EchoItemStack stack
    ) {
    }
}
