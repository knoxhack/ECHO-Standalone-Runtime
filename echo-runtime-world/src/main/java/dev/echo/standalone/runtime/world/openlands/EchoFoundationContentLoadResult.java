package dev.echo.standalone.runtime.world.openlands;

import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.util.List;

/**
 * Result of loading Foundation module payloads that moved canonical content from Openlands.
 */
public record EchoFoundationContentLoadResult(
        List<EchoOpenlandsBlockDefinition> blocks,
        List<EchoOpenlandsItemDefinition> items,
        List<EchoRecipeDefinition> recipes,
        List<EchoFoundationStationDefinition> stations,
        List<EchoLootDefinition> loot,
        List<EchoFoundationCreatureRoleMapping> creatureRoleMappings,
        List<EchoFoundationContentSource> sources
) {
    public EchoFoundationContentLoadResult {
        blocks = blocks == null ? List.of() : List.copyOf(blocks);
        items = items == null ? List.of() : List.copyOf(items);
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
        stations = stations == null ? List.of() : List.copyOf(stations);
        loot = loot == null ? List.of() : List.copyOf(loot);
        creatureRoleMappings = creatureRoleMappings == null ? List.of() : List.copyOf(creatureRoleMappings);
        sources = sources == null ? List.of() : List.copyOf(sources);
    }
}
