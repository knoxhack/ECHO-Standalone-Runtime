package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record EchoOpenlandsWorldgenResult(
        String starterBiomeId,
        String starterRegionName,
        List<GeneratedCell> starterCells,
        List<GeneratedLandmark> landmarks,
        List<GeneratedCreatureSpawn> creatureSpawns,
        Map<String, String> starterGuaranteeEvidence,
        Set<String> normalizedPaletteMarkers,
        Set<String> evidenceIds,
        EchoSaveCommitResult saveCommit,
        Map<String, String> restoredState,
        boolean biomePalettesBound,
        boolean spawnTablesBound,
        boolean landmarkPoolsBound,
        boolean starterSpawnGuaranteesBound,
        boolean saveReloadPass,
        boolean worldgenComplete
) {
    public EchoOpenlandsWorldgenResult {
        starterBiomeId = requireText(starterBiomeId, "starterBiomeId");
        starterRegionName = requireText(starterRegionName, "starterRegionName");
        Objects.requireNonNull(starterCells, "starterCells");
        Objects.requireNonNull(landmarks, "landmarks");
        Objects.requireNonNull(creatureSpawns, "creatureSpawns");
        Objects.requireNonNull(starterGuaranteeEvidence, "starterGuaranteeEvidence");
        Objects.requireNonNull(normalizedPaletteMarkers, "normalizedPaletteMarkers");
        Objects.requireNonNull(evidenceIds, "evidenceIds");
        Objects.requireNonNull(saveCommit, "saveCommit");
        Objects.requireNonNull(restoredState, "restoredState");
        starterCells = List.copyOf(starterCells);
        landmarks = List.copyOf(landmarks);
        creatureSpawns = List.copyOf(creatureSpawns);
        starterGuaranteeEvidence = Map.copyOf(new TreeMap<>(starterGuaranteeEvidence));
        normalizedPaletteMarkers = Set.copyOf(new TreeSet<>(normalizedPaletteMarkers));
        evidenceIds = Set.copyOf(new TreeSet<>(evidenceIds));
        restoredState = Map.copyOf(new TreeMap<>(restoredState));
    }

    public String summary() {
        return "biome=" + starterBiomeId
                + " cells=" + starterCells.size()
                + " landmarks=" + landmarks.size()
                + " creatureSpawns=" + creatureSpawns.size()
                + " guarantees=" + starterGuaranteeEvidence.size()
                + " saveFiles=" + saveCommit.filesWritten()
                + " complete=" + worldgenComplete;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }

    public record GeneratedCell(
            int x,
            int z,
            String biomeId,
            String surfaceBlock,
            String markerId,
            String purpose
    ) {
        public GeneratedCell {
            biomeId = requireText(biomeId, "biomeId");
            surfaceBlock = requireText(surfaceBlock, "surfaceBlock");
            markerId = requireText(markerId, "markerId");
            purpose = requireText(purpose, "purpose");
        }
    }

    public record GeneratedLandmark(
            String id,
            String biomeId,
            int x,
            int z,
            String holoMapHint,
            String tutorialHook
    ) {
        public GeneratedLandmark {
            id = requireText(id, "landmark id");
            biomeId = requireText(biomeId, "landmark biome id");
            holoMapHint = requireText(holoMapHint, "holoMapHint");
            tutorialHook = requireText(tutorialHook, "tutorialHook");
        }
    }

    public record GeneratedCreatureSpawn(
            String creatureId,
            String biomeId,
            int weight,
            String group,
            List<String> conditions
    ) {
        public GeneratedCreatureSpawn {
            creatureId = requireText(creatureId, "creatureId");
            biomeId = requireText(biomeId, "creature biomeId");
            group = group == null ? "" : group;
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }
    }
}
