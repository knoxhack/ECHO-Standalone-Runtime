package dev.echo.standalone.runtime.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Unified facade over the runtime's typed content registries.
 *
 * <p>Provides a single registration surface for Openlands and Foundation content and builds an
 * ordered registry ID list used for save fingerprints.
 */
public final class EchoUnifiedRegistry {

    private final EchoRecipeRegistry recipes;
    private final EchoLootRegistry loot;
    private final EchoWorldgenStructureRegistry structures;
    private final EchoDataTagRegistry tags;
    private final EchoSoundRegistry sounds;
    private final EchoWorldgenBiomeRegistry biomes;
    private final List<String> orderedIds = new ArrayList<>();

    public EchoUnifiedRegistry() {
        this.recipes = new EchoRecipeRegistry();
        this.loot = new EchoLootRegistry();
        this.structures = new EchoWorldgenStructureRegistry();
        this.tags = new EchoDataTagRegistry();
        this.sounds = new EchoSoundRegistry();
        this.biomes = new EchoWorldgenBiomeRegistry();
    }

    public void registerRecipe(EchoRecipeDefinition recipe) {
        recipes.register(recipe);
        orderedIds.add(recipe.id());
    }

    public void registerLoot(EchoLootDefinition lootTable) {
        loot.register(lootTable);
        orderedIds.add(lootTable.id());
    }

    public void registerStructure(EchoWorldgenStructureDefinition structure) {
        structures.register(structure);
        orderedIds.add(structure.id());
    }

    public void registerTag(EchoDataTag tag) {
        tags.register(tag);
        orderedIds.add(tag.id());
    }

    public void registerSound(EchoSoundDefinition sound) {
        sounds.register(sound);
        orderedIds.add(sound.id());
    }

    public void registerBiome(EchoWorldgenBiomeDefinition biome) {
        biomes.register(biome);
        orderedIds.add(biome.id());
    }

    public EchoRecipeRegistry recipes() {
        return recipes;
    }

    public EchoLootRegistry loot() {
        return loot;
    }

    public EchoWorldgenStructureRegistry structures() {
        return structures;
    }

    public EchoDataTagRegistry tags() {
        return tags;
    }

    public EchoSoundRegistry sounds() {
        return sounds;
    }

    public EchoWorldgenBiomeRegistry biomes() {
        return biomes;
    }

    public List<String> orderedIds() {
        return List.copyOf(orderedIds);
    }
}
