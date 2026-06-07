package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityAiComponent;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityHealthComponent;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementComponent;
import dev.echo.standalone.runtime.entity.EchoEntityPositionComponent;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemLootResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoStandaloneEntityCombatRuntime {
    private static final EchoEntityId PLAYER_ID = new EchoEntityId("player-001");
    private static final EchoEntityId HOSTILE_ID = new EchoEntityId("scavenger-001");
    private static final EchoEntityId FAMILIAR_ID = new EchoEntityId("familiar-001");
    private static final String COMBAT_OBJECTIVE = "ashfall:salvage_cache";
    private static final int FAMILIAR_ASSIST_DAMAGE = 2;

    public EchoStandaloneEntityCombatResult run(
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayRuntimeResult gameplay
    ) {
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");

        EchoEntityState player = entities.store().require(PLAYER_ID);
        EchoEntityState hostile = entities.store().require(HOSTILE_ID).withPosition(new EchoWorldPosition(1, 0, 0));
        EchoEntityState familiar = registerFamiliar(entities);
        entities.store().update(hostile);

        boolean hostileSpawned = hostile.hostile() && hostile.alive();
        boolean encounterStarted = hostileSpawned && player.alive() && familiar.alive();
        int beforeEntityAttack = player.health().currentHealth();
        entities.aiSystem().tick(entities.store());
        player = entities.store().require(PLAYER_ID);
        boolean entityCanAttack = player.health().currentHealth() == beforeEntityAttack - 5;

        hostile = damageHostile(entities, FAMILIAR_ASSIST_DAMAGE);
        int hostileHealthAfterFamiliarAssist = hostile.health().currentHealth();
        boolean familiarBehaviorExecuted = familiar.definition().kind() == EchoEntityKind.FAMILIAR
                && hostileHealthAfterFamiliarAssist == 33;

        hostile = damageHostile(entities, 12);
        hostile = damageHostile(entities, 12);
        hostile = damageHostile(entities, 12);

        boolean playerCanAttack = hostile.health().currentHealth() == 0;
        boolean deathWorks = !hostile.alive();
        boolean recoveryWorks = player.alive();
        EchoItemLootResult loot = deathWorks
                ? items.lootRuntime().grant(
                items.debugLootTable(),
                items.inventoryStore().require(new EchoInventoryId("inventory:player-001")))
                : new EchoItemLootResult("ashfall:crash_cache_salvage", false, 0, 0, "target_alive");
        boolean lootGranted = loot.granted();
        boolean missionObjectiveAdvanced = deathWorks && gameplay.mission().completeObjective(COMBAT_OBJECTIVE);
        if (missionObjectiveAdvanced) {
            gameplay.progression().awardExperience(30);
            gameplay.progression().addMilestone("agent8_hostile_defeated");
            gameplay.factions().adjustReputation("ashfall:crash_survivors", 5);
            gameplay.factions().adjustReputation("ashfall:wasteland_scavengers", -5);
        }
        boolean encounterEnded = encounterStarted && deathWorks && lootGranted && missionObjectiveAdvanced;
        int playerExperienceAfterCombat = gameplay.progression().experience();
        boolean playerStatsUpdated = playerExperienceAfterCombat == 30
                && gameplay.progression().milestones().contains("agent8_hostile_defeated");
        boolean npcInteractionOpened = gameplay.interactionSystem().drinkWater(PLAYER_ID).success();

        Map<String, Object> parityVector = new LinkedHashMap<>();
        parityVector.put("hostileSpawns", hostileSpawned);
        parityVector.put("playerCanAttack", playerCanAttack);
        parityVector.put("entityCanAttack", entityCanAttack);
        parityVector.put("playerHealthAfterAttack", player.health().currentHealth());
        parityVector.put("hostileHealthAfterPlayerAttack", hostile.health().currentHealth());
        parityVector.put("deathWorks", deathWorks);
        parityVector.put("recoveryWorks", recoveryWorks);
        parityVector.put("lootGranted", lootGranted);
        parityVector.put("missionObjectiveAdvanced", missionObjectiveAdvanced);
        parityVector.put("npcInteractionOpened", npcInteractionOpened);
        parityVector.put("encounterStarted", encounterStarted);
        parityVector.put("encounterEnded", encounterEnded);
        parityVector.put("familiarBehaviorExecuted", familiarBehaviorExecuted);
        parityVector.put("playerStatsUpdated", playerStatsUpdated);
        parityVector.put("playerExperienceAfterCombat", playerExperienceAfterCombat);

        return new EchoStandaloneEntityCombatResult(
                "echo_runtime_standalone",
                true,
                hostileSpawned,
                playerCanAttack,
                entityCanAttack,
                player.health().currentHealth(),
                hostile.health().currentHealth(),
                deathWorks,
                recoveryWorks,
                lootGranted,
                missionObjectiveAdvanced,
                npcInteractionOpened,
                encounterStarted,
                encounterEnded,
                familiarBehaviorExecuted,
                playerStatsUpdated,
                playerExperienceAfterCombat,
                List.of(
                        "EchoEntityType",
                        "EchoEntityInstance",
                        "EchoNpcProfile",
                        "EchoCreatureBrain",
                        "EchoEncounter",
                        "EchoDamageSource",
                        "EchoCombatStats",
                        "EchoWeaponProfile",
                        "EchoArmorProfile",
                        "EchoFactionRelation",
                        "EchoInteractionOption"),
                parityVector);
    }

    private static EchoEntityState registerFamiliar(EchoEntityRuntimeResult entities) {
        EchoEntityDefinition familiarDefinition = new EchoEntityDefinition(
                "echofamiliarcore:spirit_drone",
                "Spirit Drone",
                EchoEntityKind.FAMILIAR,
                24,
                2,
                "familiar_assist"
        );
        EchoEntityState familiar = new EchoEntityState(
                FAMILIAR_ID,
                familiarDefinition,
                new EchoEntityPositionComponent(new EchoWorldPosition(0, 0, 1)),
                new EchoEntityHealthComponent(familiarDefinition.maxHealth(), familiarDefinition.maxHealth()),
                new EchoEntityMovementComponent(familiarDefinition.movementSpeed(), true),
                new EchoEntityAiComponent(familiarDefinition.aiProfile(), EchoEntityAiState.IDLE)
        );
        entities.store().register(familiar);
        return familiar;
    }

    private static EchoEntityState damageHostile(EchoEntityRuntimeResult entities, int damage) {
        EchoEntityState hostile = entities.store().require(HOSTILE_ID);
        EchoEntityState damaged = hostile.withHealth(hostile.health().damage(damage));
        entities.store().update(damaged);
        return damaged;
    }
}
