package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class EchoClientGameSimulationRules {
    private EchoClientGameSimulationRules() {
    }

    static EchoClientToolStatus selectedToolStatus(
            EchoClientToolState toolState,
            EchoItemDefinition definition,
            EchoVoxelBlock targetBlock
    ) {
        EchoClientToolState safeToolState = toolState == null ? EchoClientToolState.empty() : toolState;
        return definition == null ? EchoClientToolStatus.hand() : safeToolState.status(definition, targetBlock);
    }

    static DamageResult damagePlayer(
            EchoClientPlayerVitals vitals,
            EchoClientPlayerCombatState combatState,
            EchoClientDamageSource source,
            int damage
    ) {
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        EchoClientPlayerCombatState safeCombat = combatState == null
                ? EchoClientPlayerCombatState.defaults()
                : combatState;
        EchoClientDamageSource safeSource = source == null ? EchoClientDamageSource.generic() : source;
        int safeDamage = Math.max(0, damage);
        if (safeDamage == 0) {
            return new DamageResult(safeVitals, safeCombat.withLastDamageSource(safeSource));
        }
        if (!safeCombat.gameMode().takesDamage() && !safeSource.bypassesGameMode()) {
            return new DamageResult(safeVitals, safeCombat.withLastDamageSource(safeSource));
        }
        int effectiveDamage = safeSource.bypassesArmor()
                ? safeDamage
                : safeCombat.equipment().mitigateDamage(safeDamage);
        EchoClientPlayerVitals nextVitals = safeVitals.exhaust(safeSource.exhaustion()).damage(effectiveDamage);
        EchoClientEquipmentState nextEquipment = safeSource.bypassesArmor()
                ? safeCombat.equipment()
                : safeCombat.equipment().damageArmor(safeDamage);
        return new DamageResult(
                nextVitals,
                safeCombat.withEquipment(nextEquipment).withLastDamageSource(safeSource)
        );
    }

    static EchoClientPlayerVitals healPlayer(EchoClientPlayerVitals vitals, int amount) {
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        return safeVitals.heal(amount);
    }

    static EchoClientProgressionState awardExperience(
            EchoClientProgressionState progressionState,
            int amount,
            String milestone
    ) {
        EchoClientProgressionState safeProgression = progressionState == null
                ? EchoClientProgressionState.empty()
                : progressionState;
        return safeProgression.awardExperience(amount, milestone);
    }

    static EchoClientProgressionState awardBlockBreakExperience(
            EchoClientProgressionState progressionState,
            EchoVoxelBlock block
    ) {
        EchoVoxelBlock safeBlock = block == null ? EchoVoxelBlock.AIR : block;
        return awardExperience(progressionState, blockBreakExperience(safeBlock), "mine:" + safeBlock.id());
    }

    static SurvivalTickResult tickPlayerSurvival(
            EchoClientPlayerVitals vitals,
            EchoClientPlayerCombatState combatState,
            double deltaSeconds,
            EchoVoxelPlayerInput input
    ) {
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        EchoClientPlayerCombatState safeCombat = combatState == null
                ? EchoClientPlayerCombatState.defaults()
                : combatState;
        if (!safeCombat.gameMode().ticksSurvival()) {
            return new SurvivalTickResult(safeVitals, safeCombat);
        }
        EchoVoxelPlayerInput safeInput = input == null ? EchoVoxelPlayerInput.idle() : input;
        int beforeHealth = safeVitals.currentHealth();
        EchoClientPlayerVitals nextVitals = safeVitals.tickSurvival(
                deltaSeconds,
                safeInput.wantsHorizontalMovement(),
                safeInput.sprint(),
                safeInput.jump()
        );
        EchoClientPlayerCombatState nextCombat = nextVitals.currentHealth() < beforeHealth && nextVitals.starving()
                ? safeCombat.withLastDamageSource(EchoClientDamageSource.starvation())
                : safeCombat;
        return new SurvivalTickResult(nextVitals, nextCombat);
    }

    static boolean isConsumable(EchoItemDefinition definition) {
        return definition != null
                && (definition.category() == EchoItemCategory.CONSUMABLE
                || definition.tagged("food")
                || definition.tagged("ration")
                || definition.tagged("hydration"));
    }

    static boolean canConsume(EchoClientPlayerVitals vitals, EchoItemDefinition definition) {
        if (!isConsumable(definition)) {
            return false;
        }
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        return safeVitals.foodLevel() < EchoClientPlayerVitals.DEFAULT_MAX_FOOD
                || safeVitals.currentHealth() < safeVitals.maxHealth();
    }

    static EchoClientPlayerVitals consume(EchoClientPlayerVitals vitals, EchoItemDefinition definition) {
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        EchoClientPlayerVitals nextVitals = safeVitals.eat(consumableNutrition(definition), consumableSaturation(definition));
        if (definition != null && (definition.tagged("hydration") || normalizedItemId(definition).contains("water"))) {
            nextVitals = nextVitals.heal(1);
        }
        return nextVitals;
    }

    static int blockBreakExperience(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return 0;
        }
        int hardnessBonus = (int) Math.floor(block.hardness());
        return Math.max(1, Math.min(8, 1 + hardnessBonus));
    }

    static int entityAttackDamage(EchoItemDefinition definition, EchoClientToolStatus toolStatus) {
        EchoClientToolStatus safeTool = toolStatus == null ? EchoClientToolStatus.hand() : toolStatus;
        if (safeTool.activeTool() && !safeTool.broken()) {
            return 6;
        }
        if (definition != null && (definition.category() == EchoItemCategory.TOOL
                || definition.tagged("weapon")
                || definition.tagged("combat"))) {
            return 4;
        }
        return 2;
    }

    static int entityKillExperience(EchoClientEntityAttackResult attackResult) {
        if (attackResult == null || !attackResult.killed()) {
            return 0;
        }
        return "HOSTILE".equals(attackResult.entityKind()) ? 10 : 4;
    }

    static int entityDeathLootQuantity(EchoClientEntityAttackResult attackResult) {
        if (attackResult == null || !attackResult.killed()) {
            return 0;
        }
        return "HOSTILE".equals(attackResult.entityKind()) ? 2 : 1;
    }

    static Optional<String> spawnEggEntityId(EchoItemDefinition definition) {
        if (definition == null) {
            return Optional.empty();
        }
        String itemId = definition.id().value().trim();
        String namespace = namespace(itemId);
        for (String tag : definition.tags()) {
            String trimmed = tag == null ? "" : tag.trim();
            String normalized = trimmed.toLowerCase(Locale.ROOT);
            for (String prefix : List.of("entity:", "spawn_entity:", "spawns:")) {
                if (normalized.startsWith(prefix)) {
                    String candidate = trimmed.substring(prefix.length()).trim();
                    if (!candidate.isBlank()) {
                        return Optional.of(qualifiedEntityId(candidate, namespace));
                    }
                }
            }
        }
        if (itemId.toLowerCase(Locale.ROOT).endsWith("_spawn_egg")) {
            return Optional.of(itemId.substring(0, itemId.length() - "_spawn_egg".length()));
        }
        return Optional.empty();
    }

    static int craftExperience(EchoItemRecipe recipe) {
        int ingredientUnits = recipe.ingredients().values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        return Math.max(3, Math.min(18, 2 + ingredientUnits + recipe.outputQuantity()));
    }

    static ToolDamageResult damageTool(
            EchoClientToolState toolState,
            EchoItemDefinition definition,
            EchoVoxelBlock block
    ) {
        EchoClientToolState safeToolState = toolState == null ? EchoClientToolState.empty() : toolState;
        if (!EchoClientToolState.isTool(definition)) {
            return new ToolDamageResult(safeToolState, false);
        }
        int wear = EchoClientToolState.wearForBlock(block);
        if (wear <= 0) {
            return new ToolDamageResult(safeToolState, false);
        }
        int remaining = Math.max(0, safeToolState.status(definition, block).durability() - wear);
        EchoClientToolState nextToolState = safeToolState.damage(definition, wear);
        if (remaining <= 0) {
            nextToolState = nextToolState.remove(definition);
        }
        return new ToolDamageResult(nextToolState, remaining <= 0);
    }

    private static int consumableNutrition(EchoItemDefinition definition) {
        String id = normalizedItemId(definition);
        if (definition != null && (definition.tagged("ration") || id.contains("ration") || id.contains("food"))) {
            return 6;
        }
        if (definition != null && (definition.tagged("hydration") || id.contains("water"))) {
            return 4;
        }
        return 2;
    }

    private static double consumableSaturation(EchoItemDefinition definition) {
        String id = normalizedItemId(definition);
        if (definition != null && (definition.tagged("ration") || id.contains("ration") || id.contains("food"))) {
            return 0.6D;
        }
        if (definition != null && (definition.tagged("hydration") || id.contains("water"))) {
            return 0.3D;
        }
        return 0.2D;
    }

    private static String normalizedItemId(EchoItemDefinition definition) {
        return definition == null ? "" : definition.id().value().toLowerCase(Locale.ROOT);
    }

    private static String namespace(String itemId) {
        int separator = itemId.indexOf(':');
        return separator > 0 ? itemId.substring(0, separator) : "echo";
    }

    private static String qualifiedEntityId(String candidate, String namespace) {
        String trimmed = candidate.trim();
        return trimmed.indexOf(':') > 0 ? trimmed : namespace + ":" + trimmed;
    }

    record DamageResult(
            EchoClientPlayerVitals vitals,
            EchoClientPlayerCombatState combatState
    ) {
    }

    record SurvivalTickResult(
            EchoClientPlayerVitals vitals,
            EchoClientPlayerCombatState combatState
    ) {
    }

    record ToolDamageResult(
            EchoClientToolState toolState,
            boolean consumeStack
    ) {
    }
}
