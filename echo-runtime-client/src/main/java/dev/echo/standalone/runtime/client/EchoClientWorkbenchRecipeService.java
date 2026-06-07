package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.io.IOException;
import java.util.List;

final class EchoClientWorkbenchRecipeService {
    private List<EchoRecipeDefinition> recipes = List.of();
    private List<EchoDataTag> tags = List.of();
    private List<EchoLootDefinition> loot = List.of();
    private String lastError = "";

    void refresh(EchoAssetRuntimeResult assets) {
        lastError = "";
        recipes = List.of();
        tags = List.of();
        loot = List.of();
        if (assets == null) {
            lastError = "No mounted data assets";
            return;
        }
        try {
            EchoDataRuntimeResult data = new EchoDataRuntime().load(new EchoDefaultRuntimeServiceRegistry(), assets);
            recipes = data.recipes().recipes();
            tags = data.tags().tags();
            loot = data.loot().lootTables();
        } catch (IOException | IllegalArgumentException exception) {
            lastError = exception.getMessage();
        }
    }

    List<EchoRecipeDefinition> recipes() {
        return recipes;
    }

    List<EchoDataTag> tags() {
        return tags;
    }

    List<EchoLootDefinition> loot() {
        return loot;
    }

    String lastError() {
        return lastError;
    }
}
