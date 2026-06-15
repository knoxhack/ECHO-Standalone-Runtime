package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.util.List;

/**
 * Result of translating a NeoForge datapack into ECHO runtime content definitions.
 */
public record EchoNeoForgeDatapackScanResult(
        List<EchoRecipeDefinition> recipes,
        List<EchoLootDefinition> lootTables,
        List<EchoDataTag> tags,
        List<EchoCompatDiagnostic> diagnostics
) {
    public EchoNeoForgeDatapackScanResult {
        recipes = recipes == null ? List.of() : List.copyOf(recipes);
        lootTables = lootTables == null ? List.of() : List.copyOf(lootTables);
        tags = tags == null ? List.of() : List.copyOf(tags);
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
    }
}
