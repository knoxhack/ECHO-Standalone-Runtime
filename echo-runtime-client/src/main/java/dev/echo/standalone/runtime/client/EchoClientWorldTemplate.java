package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.save.EchoSaveProfile;

import java.nio.file.Path;
import java.util.List;

record EchoClientWorldTemplate(
        String slotIdPrefix,
        String displayName,
        EchoClientSaveProfileDefinition saveProfile,
        EchoClientWorldPresentation presentation,
        EchoClientAudioProfile audioProfile,
        EchoClientGameSessionFactory sessionFactory
) {
    EchoClientWorldTemplate {
        if (slotIdPrefix == null || slotIdPrefix.isBlank()) {
            throw new IllegalArgumentException("slotIdPrefix must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        if (saveProfile == null) {
            throw new IllegalArgumentException("saveProfile must not be null");
        }
        presentation = presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        audioProfile = audioProfile == null ? EchoClientAudioProfile.genericDefault() : audioProfile;
        if (sessionFactory == null) {
            throw new IllegalArgumentException("sessionFactory must not be null");
        }
    }

    long defaultSeed() {
        return sessionFactory.defaultSeed();
    }

    EchoAdapterCoreStandaloneContentBridge contentBridge() {
        return sessionFactory.contentBridge();
    }

    EchoSaveProfile saveProfile(Path root) {
        return saveProfile.toSaveProfile(root);
    }

    EchoClientStarterLoadout starterLoadout() {
        return sessionFactory.starterLoadout();
    }

    EchoClientEntityCatalog entityCatalog() {
        return sessionFactory.entityCatalog();
    }

    EchoClientHazardCatalog hazardCatalog() {
        return sessionFactory.hazardCatalog();
    }

    EchoClientWorldInteractionCatalog interactionCatalog() {
        return sessionFactory.interactionCatalog();
    }

    EchoClientGameSession newSession(String seedText, List<EchoRecipeDefinition> recipes) {
        return sessionFactory.newSession(seedText, recipes);
    }

    EchoClientGameSession newSession(
            String seedText,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        return sessionFactory.newSession(seedText, recipes, tags);
    }

    EchoClientGameSession restoreSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes
    ) {
        return sessionFactory.restoreSession(snapshot, recipes);
    }

    EchoClientGameSession restoreSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        return sessionFactory.restoreSession(snapshot, recipes, tags);
    }

    EchoClientGameSession restoreGameplaySnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> recipes
    ) {
        return sessionFactory.restoreGameplaySnapshot(snapshot, recipes);
    }

    EchoClientGameSession restoreGameplaySnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        return sessionFactory.restoreGameplaySnapshot(snapshot, recipes, tags);
    }

    EchoClientWorldTemplate withSessionFactory(EchoClientGameSessionFactory replacementFactory) {
        return new EchoClientWorldTemplate(
                slotIdPrefix,
                displayName,
                saveProfile,
                presentation,
                audioProfile,
                replacementFactory == null ? sessionFactory : replacementFactory
        );
    }
}
