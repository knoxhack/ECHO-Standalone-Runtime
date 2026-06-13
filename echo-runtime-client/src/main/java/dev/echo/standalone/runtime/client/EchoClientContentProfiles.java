package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioDeviceProfiles;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfiles;
import dev.echo.standalone.runtime.data.EchoStandaloneClientContentProfile;
import dev.echo.standalone.runtime.data.EchoStandaloneClientContentProfileLoader;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.item.EchoItemCategory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientContentProfiles {
    private EchoClientContentProfiles() {
    }

    static Profile ashfallCrashSite() {
        return from(EchoStandaloneClientContentProfileLoader.loadAshfallCrashSite());
    }

    static Profile openlandsFirstHour() {
        return from(EchoStandaloneClientContentProfileLoader.loadOpenlandsFirstHour());
    }

    private static Profile from(EchoStandaloneClientContentProfile source) {
        return new Profile(
                worldTemplate(source.worldTemplate()),
                starterLoadout(source.starterLoadout()),
                entityCatalog(source.entityCatalog()),
                hazardCatalog(source.hazardCatalog()),
                interactionCatalog(source.interactionCatalog())
        );
    }

    private static WorldTemplateProfile worldTemplate(
            EchoStandaloneClientContentProfile.WorldTemplate source
    ) {
        return new WorldTemplateProfile(
                source.slotIdPrefix(),
                source.displayName(),
                saveProfile(source.saveProfile()),
                presentation(source.presentation()),
                audioProfile(source.audioProfile())
        );
    }

    private static EchoClientSaveProfileDefinition saveProfile(
            EchoStandaloneClientContentProfile.SaveProfile source
    ) {
        return new EchoClientSaveProfileDefinition(
                source.schema(),
                source.profileId(),
                source.displayName(),
                source.packId(),
                source.formatVersion(),
                source.metadata()
        );
    }

    private static EchoClientWorldPresentation presentation(
            EchoStandaloneClientContentProfile.Presentation source
    ) {
        return new EchoClientWorldPresentation(
                source.windowTitle(),
                source.settingsFileComment(),
                source.createWorldActionLabel(),
                source.worldTypeLabel(),
                source.packLabel(),
                source.newWorldModalMessage(),
                source.loadingInitialDetail(),
                source.newWorldGenerationLabel(),
                source.newWorldDetailSuffix(),
                source.newWorldLoadingFooter(),
                source.savedWorldLoadingFooter(),
                source.moduleSourceLabels(),
                source.hostileDamageSourceId()
        );
    }

    private static EchoClientAudioProfile audioProfile(
            EchoStandaloneClientContentProfile.AudioProfile source
    ) {
        return new EchoClientAudioProfile(
                EchoAudioDeviceProfiles.resolve(source.deviceProfileId()),
                EchoAudioVolumeProfiles.resolve(source.volumeProfileId())
        );
    }

    private static EchoClientStarterLoadout starterLoadout(
            EchoStandaloneClientContentProfile.StarterLoadout source
    ) {
        return new EchoClientStarterLoadout(
                source.playerInventoryId(),
                source.playerInventoryLabel(),
                source.openContainerId(),
                source.openContainerLabel(),
                source.items().stream()
                        .map(item -> new EchoClientStarterLoadout.Item(
                                item.id(),
                                item.displayName(),
                                EchoItemCategory.valueOf(item.category()),
                                item.maxStackSize(),
                                item.tags(),
                                item.tooltipLines()
                        ))
                        .toList(),
                source.openContainerStacks().stream()
                        .map(stack -> EchoClientStarterLoadout.stack(
                                stack.slotIndex(),
                                stack.itemId(),
                                stack.quantity()
                        ))
                        .toList(),
                source.workbenchRecipes().stream()
                        .map(recipe -> EchoClientStarterLoadout.recipe(
                                recipe.recipeId(),
                                recipe.ingredients(),
                                recipe.outputItemId(),
                                recipe.outputQuantity()
                        ))
                        .toList()
        );
    }

    private static EchoClientEntityCatalog entityCatalog(
            EchoStandaloneClientContentProfile.EntityCatalog source
    ) {
        LinkedHashMap<String, EchoEntityDefinition> definitions = new LinkedHashMap<>();
        EchoEntityDefinition fallback = entityDefinition(source.fallbackHostile());
        definitions.put(fallback.definitionId(), fallback);

        List<EchoClientEntityCatalog.SpawnRule> spawnRules = source.spawnRules().stream()
                .map(rule -> {
                    EchoEntityDefinition definition = entityDefinition(rule.definition());
                    definitions.put(definition.definitionId(), definition);
                    return new EchoClientEntityCatalog.SpawnRule(rule.biomeTags(), definition);
                })
                .toList();

        LinkedHashMap<String, EchoClientEntityCatalog.RenderProfile> renderProfiles = new LinkedHashMap<>();
        for (EchoStandaloneClientContentProfile.RenderProfile profile : source.renderProfiles()) {
            renderProfiles.put(
                    profile.definitionId(),
                    new EchoClientEntityCatalog.RenderProfile(
                            profile.argb(),
                            EchoClientEntityCatalog.RenderShape.valueOf(profile.shape())
                    )
            );
        }
        return new EchoClientEntityCatalog(fallback, spawnRules, renderProfiles, definitions);
    }

    private static EchoEntityDefinition entityDefinition(
            EchoStandaloneClientContentProfile.EntityDefinition source
    ) {
        return EchoClientEntityCatalog.hostile(
                source.id(),
                source.displayName(),
                source.maxHealth(),
                source.aiProfile()
        );
    }

    private static EchoClientHazardCatalog hazardCatalog(
            EchoStandaloneClientContentProfile.HazardCatalog source
    ) {
        return new EchoClientHazardCatalog(
                source.rules().stream()
                        .map(rule -> new EchoClientHazardCatalog.Rule(
                                rule.biomeTags(),
                                new EchoClientHazardCatalog.HazardProfile(
                                        rule.profile().id(),
                                        rule.profile().label(),
                                        rule.profile().exposurePerSecond(),
                                        rule.profile().damage()
                                )
                        ))
                        .toList()
        );
    }

    private static EchoClientWorldInteractionCatalog interactionCatalog(
            EchoStandaloneClientContentProfile.InteractionCatalog source
    ) {
        return new EchoClientWorldInteractionCatalog(
                source.rules().stream()
                        .map(rule -> new EchoClientWorldInteractionCatalog.Rule(
                                rule.matchTokens(),
                                EchoClientScreenCommand.valueOf(rule.command()),
                                rule.targetId()
                        ))
                        .toList()
        );
    }

    record Profile(
            WorldTemplateProfile worldTemplate,
            EchoClientStarterLoadout starterLoadout,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog,
            EchoClientWorldInteractionCatalog interactionCatalog
    ) {
        Profile {
            if (worldTemplate == null) {
                throw new IllegalArgumentException("worldTemplate must not be null");
            }
            starterLoadout = starterLoadout == null ? EchoClientStarterLoadout.empty() : starterLoadout;
            entityCatalog = entityCatalog == null ? EchoClientEntityCatalog.empty() : entityCatalog;
            hazardCatalog = hazardCatalog == null ? EchoClientHazardCatalog.empty() : hazardCatalog;
            interactionCatalog = interactionCatalog == null ? EchoClientWorldInteractionCatalog.empty() : interactionCatalog;
        }

        EchoClientWorldTemplate toWorldTemplate(EchoClientGameSessionFactory sessionFactory) {
            return new EchoClientWorldTemplate(
                    worldTemplate.slotIdPrefix(),
                    worldTemplate.displayName(),
                    worldTemplate.saveProfile(),
                    worldTemplate.presentation(),
                    worldTemplate.audioProfile(),
                    sessionFactory
            );
        }
    }

    record WorldTemplateProfile(
            String slotIdPrefix,
            String displayName,
            EchoClientSaveProfileDefinition saveProfile,
            EchoClientWorldPresentation presentation,
            EchoClientAudioProfile audioProfile
    ) {
        WorldTemplateProfile {
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
        }
    }
}
