package dev.echo.standalone.runtime.client;

import java.util.List;
import java.util.Objects;

record EchoClientSavedSessionSnapshot(
        EchoClientGameplay.GameplaySnapshot gameplay,
        List<EchoClientInventorySlotSnapshot> inventorySlots,
        List<EchoClientInventorySlotSnapshot> containerSlots,
        EchoClientPlayerVitals playerVitals,
        EchoClientPlayerCombatState playerCombatState,
        EchoClientProgressionState progressionState,
        EchoClientHazardState hazardState,
        EchoClientToolState toolState,
        List<EchoClientEntitySnapshot> entities,
        List<EchoClientDroppedItemSnapshot> droppedItems,
        EchoClientMachineStateSnapshot machineState
) {
    EchoClientSavedSessionSnapshot(
            EchoClientGameplay.GameplaySnapshot gameplay,
            List<EchoClientInventorySlotSnapshot> inventorySlots
    ) {
        this(
                gameplay,
                inventorySlots,
                List.of(),
                EchoClientPlayerVitals.full(),
                EchoClientPlayerCombatState.defaults(),
                EchoClientProgressionState.empty(),
                EchoClientHazardState.empty(),
                EchoClientToolState.empty(),
                List.of(),
                List.of(),
                EchoClientMachineStateSnapshot.reference()
        );
    }

    EchoClientSavedSessionSnapshot(
            EchoClientGameplay.GameplaySnapshot gameplay,
            List<EchoClientInventorySlotSnapshot> inventorySlots,
            List<EchoClientInventorySlotSnapshot> containerSlots
    ) {
        this(
                gameplay,
                inventorySlots,
                containerSlots,
                EchoClientPlayerVitals.full(),
                EchoClientPlayerCombatState.defaults(),
                EchoClientProgressionState.empty(),
                EchoClientHazardState.empty(),
                EchoClientToolState.empty(),
                List.of(),
                List.of(),
                EchoClientMachineStateSnapshot.reference()
        );
    }

    EchoClientSavedSessionSnapshot(
            EchoClientGameplay.GameplaySnapshot gameplay,
            List<EchoClientInventorySlotSnapshot> inventorySlots,
            List<EchoClientInventorySlotSnapshot> containerSlots,
            EchoClientPlayerVitals playerVitals
    ) {
        this(
                gameplay,
                inventorySlots,
                containerSlots,
                playerVitals,
                EchoClientPlayerCombatState.defaults(),
                EchoClientProgressionState.empty(),
                EchoClientHazardState.empty(),
                EchoClientToolState.empty(),
                List.of(),
                List.of(),
                EchoClientMachineStateSnapshot.reference()
        );
    }

    EchoClientSavedSessionSnapshot(
            EchoClientGameplay.GameplaySnapshot gameplay,
            List<EchoClientInventorySlotSnapshot> inventorySlots,
            List<EchoClientInventorySlotSnapshot> containerSlots,
            EchoClientPlayerVitals playerVitals,
            EchoClientPlayerCombatState playerCombatState,
            EchoClientProgressionState progressionState,
            EchoClientHazardState hazardState,
            EchoClientToolState toolState
    ) {
        this(
                gameplay,
                inventorySlots,
                containerSlots,
                playerVitals,
                playerCombatState,
                progressionState,
                hazardState,
                toolState,
                List.of(),
                List.of(),
                EchoClientMachineStateSnapshot.reference()
        );
    }

    EchoClientSavedSessionSnapshot {
        Objects.requireNonNull(gameplay, "gameplay");
        inventorySlots = inventorySlots == null ? List.of() : List.copyOf(inventorySlots);
        containerSlots = containerSlots == null ? List.of() : List.copyOf(containerSlots);
        playerVitals = playerVitals == null ? EchoClientPlayerVitals.full() : playerVitals;
        playerCombatState = playerCombatState == null ? EchoClientPlayerCombatState.defaults() : playerCombatState;
        progressionState = progressionState == null ? EchoClientProgressionState.empty() : progressionState;
        hazardState = hazardState == null ? EchoClientHazardState.empty() : hazardState;
        toolState = toolState == null ? EchoClientToolState.empty() : toolState;
        entities = entities == null ? List.of() : List.copyOf(entities);
        droppedItems = droppedItems == null ? List.of() : List.copyOf(droppedItems);
        machineState = machineState == null ? EchoClientMachineStateSnapshot.reference() : machineState;
    }
}
