package dev.echo.standalone.runtime.client;

record EchoClientPlayerCombatState(
        EchoClientGameMode gameMode,
        EchoClientEquipmentState equipment,
        EchoClientDamageSource lastDamageSource
) {
    EchoClientPlayerCombatState {
        gameMode = gameMode == null ? EchoClientGameMode.SURVIVAL : gameMode;
        equipment = equipment == null ? EchoClientEquipmentState.empty() : equipment;
        lastDamageSource = lastDamageSource == null ? EchoClientDamageSource.none() : lastDamageSource;
    }

    static EchoClientPlayerCombatState defaults() {
        return new EchoClientPlayerCombatState(
                EchoClientGameMode.SURVIVAL,
                EchoClientEquipmentState.empty(),
                EchoClientDamageSource.none()
        );
    }

    EchoClientPlayerCombatState withGameMode(EchoClientGameMode nextMode) {
        return new EchoClientPlayerCombatState(nextMode, equipment, lastDamageSource);
    }

    EchoClientPlayerCombatState withEquipment(EchoClientEquipmentState nextEquipment) {
        return new EchoClientPlayerCombatState(gameMode, nextEquipment, lastDamageSource);
    }

    EchoClientPlayerCombatState withLastDamageSource(EchoClientDamageSource source) {
        return new EchoClientPlayerCombatState(gameMode, equipment, source);
    }
}
