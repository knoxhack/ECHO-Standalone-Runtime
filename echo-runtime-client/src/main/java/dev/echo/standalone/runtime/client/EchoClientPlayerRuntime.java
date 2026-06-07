package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

final class EchoClientPlayerRuntime {
    private EchoClientPlayerVitals vitals;
    private EchoClientPlayerCombatState combatState;
    private EchoClientProgressionState progressionState;
    private EchoClientHazardState hazardState;
    private EchoClientToolState toolState;

    EchoClientPlayerRuntime() {
        this(
                EchoClientPlayerVitals.full(),
                EchoClientPlayerCombatState.defaults(),
                EchoClientProgressionState.empty(),
                EchoClientHazardState.empty(),
                EchoClientToolState.empty()
        );
    }

    EchoClientPlayerRuntime(
            EchoClientPlayerVitals vitals,
            EchoClientPlayerCombatState combatState,
            EchoClientProgressionState progressionState,
            EchoClientHazardState hazardState,
            EchoClientToolState toolState
    ) {
        this.vitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        this.combatState = combatState == null ? EchoClientPlayerCombatState.defaults() : combatState;
        this.progressionState = progressionState == null ? EchoClientProgressionState.empty() : progressionState;
        this.hazardState = hazardState == null ? EchoClientHazardState.empty() : hazardState;
        this.toolState = toolState == null ? EchoClientToolState.empty() : toolState;
    }

    EchoClientPlayerVitals vitals() {
        return vitals;
    }

    EchoClientPlayerCombatState combatState() {
        return combatState;
    }

    EchoClientProgressionState progressionState() {
        return progressionState;
    }

    EchoClientHazardState hazardState() {
        return hazardState;
    }

    EchoClientToolState toolState() {
        return toolState;
    }

    EchoClientToolStatus selectedToolStatus(EchoItemDefinition definition, EchoVoxelBlock targetBlock) {
        return EchoClientGameSimulationRules.selectedToolStatus(toolState, definition, targetBlock);
    }

    EchoClientGameMode gameMode() {
        return combatState.gameMode();
    }

    void setGameMode(EchoClientGameMode gameMode) {
        combatState = combatState.withGameMode(gameMode);
    }

    EchoClientPlayerVitals damagePlayer(int damage) {
        return damagePlayer(EchoClientDamageSource.generic(), damage);
    }

    EchoClientPlayerVitals damagePlayer(EchoClientDamageSource source, int damage) {
        EchoClientGameSimulationRules.DamageResult result =
                EchoClientGameSimulationRules.damagePlayer(vitals, combatState, source, damage);
        vitals = result.vitals();
        combatState = result.combatState();
        return vitals;
    }

    EchoClientPlayerVitals healPlayer(int amount) {
        vitals = EchoClientGameSimulationRules.healPlayer(vitals, amount);
        return vitals;
    }

    EchoClientProgressionState awardExperience(int amount, String milestone) {
        progressionState = EchoClientGameSimulationRules.awardExperience(progressionState, amount, milestone);
        return progressionState;
    }

    EchoClientProgressionState awardBlockBreakExperience(EchoVoxelBlock block) {
        progressionState = EchoClientGameSimulationRules.awardBlockBreakExperience(progressionState, block);
        return progressionState;
    }

    EchoClientPlayerVitals tickSurvival(double deltaSeconds, EchoVoxelPlayerInput input) {
        EchoClientGameSimulationRules.SurvivalTickResult result =
                EchoClientGameSimulationRules.tickPlayerSurvival(vitals, combatState, deltaSeconds, input);
        vitals = result.vitals();
        combatState = result.combatState();
        return vitals;
    }

    EchoClientHazardState applyHazardTick(EchoClientWorldRuntime.EchoClientBiomeHazardResult tick) {
        if (tick == null) {
            return hazardState;
        }
        hazardState = tick.state();
        if (tick.damage() > 0) {
            damagePlayer(tick.source(), tick.damage());
        }
        return hazardState;
    }

    void applyConsumableUse(EchoClientInventoryRuntime.ConsumableUseResult result) {
        if (result != null) {
            vitals = result.vitals();
        }
    }

    void applyArmorEquip(EchoClientInventoryRuntime.ArmorEquipResult result) {
        if (result != null && result.equipped()) {
            combatState = combatState.withEquipment(result.equipment());
        }
    }

    void setEquipment(EchoClientEquipmentState equipment) {
        combatState = combatState.withEquipment(equipment == null ? EchoClientEquipmentState.empty() : equipment);
    }

    void setToolState(EchoClientToolState nextToolState) {
        toolState = nextToolState == null ? EchoClientToolState.empty() : nextToolState;
    }

    void resetForRespawn() {
        vitals = EchoClientPlayerVitals.full();
        combatState = combatState.withLastDamageSource(EchoClientDamageSource.none());
        hazardState = EchoClientHazardState.empty();
        toolState = EchoClientToolState.empty();
    }
}
