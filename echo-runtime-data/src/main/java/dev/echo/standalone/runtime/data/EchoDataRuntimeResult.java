package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoDataRuntimeResult(
        List<EchoDataDocument> documents,
        EchoDataSchemaRegistry schemas,
        EchoDataRegistryStore registries,
        EchoDataTagRegistry tags,
        EchoRecipeRegistry recipes,
        EchoLootRegistry loot,
        EchoMissionRegistry missions,
        EchoWorldgenStructureRegistry worldgenStructures,
        EchoWorldgenBiomeRegistry worldgenBiomes,
        EchoWorldgenFeatureRegistry worldgenFeatures,
        EchoWorldCoreRegionRegistry worldCoreRegions,
        EchoWorldCoreHazardRegistry worldCoreHazards,
        EchoSoundRegistry sounds,
        EchoDataValidationReport validationReport,
        EchoDataFreezeReport freezeReport
) {
    public EchoDataRuntimeResult {
        Objects.requireNonNull(documents, "documents");
        Objects.requireNonNull(schemas, "schemas");
        Objects.requireNonNull(registries, "registries");
        Objects.requireNonNull(tags, "tags");
        Objects.requireNonNull(recipes, "recipes");
        Objects.requireNonNull(loot, "loot");
        Objects.requireNonNull(missions, "missions");
        Objects.requireNonNull(worldgenStructures, "worldgenStructures");
        Objects.requireNonNull(worldgenBiomes, "worldgenBiomes");
        Objects.requireNonNull(worldgenFeatures, "worldgenFeatures");
        Objects.requireNonNull(worldCoreRegions, "worldCoreRegions");
        Objects.requireNonNull(worldCoreHazards, "worldCoreHazards");
        Objects.requireNonNull(sounds, "sounds");
        Objects.requireNonNull(validationReport, "validationReport");
        Objects.requireNonNull(freezeReport, "freezeReport");
        documents = List.copyOf(documents);
    }
}
