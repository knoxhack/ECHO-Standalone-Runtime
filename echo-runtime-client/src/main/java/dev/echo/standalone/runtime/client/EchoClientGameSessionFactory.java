package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.util.List;

interface EchoClientGameSessionFactory {
    long defaultSeed();

    EchoAdapterCoreStandaloneContentBridge contentBridge();

    EchoClientStarterLoadout starterLoadout();

    EchoClientEntityCatalog entityCatalog();

    EchoClientHazardCatalog hazardCatalog();

    EchoClientWorldInteractionCatalog interactionCatalog();

    EchoVoxelWorldStreamer streamer();

    EchoClientGameSession newSession(String seedText, List<EchoRecipeDefinition> dataRecipes);

    default EchoClientGameSession newSession(
            String seedText,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return newSession(seedText, dataRecipes);
    }

    EchoClientGameSession restoreSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes
    );

    default EchoClientGameSession restoreSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return restoreSession(snapshot, dataRecipes);
    }

    EchoClientGameSession restoreGameplaySnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes
    );

    default EchoClientGameSession restoreGameplaySnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return restoreGameplaySnapshot(snapshot, dataRecipes);
    }
}
