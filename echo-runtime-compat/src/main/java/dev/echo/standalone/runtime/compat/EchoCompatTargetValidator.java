package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.ArrayList;
import java.util.Objects;

public final class EchoCompatTargetValidator {
    public EchoCompatValidationResult validate(
            EchoCompatMappingRegistry registry,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayRuntimeResult gameplay
    ) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");
        ArrayList<EchoCompatValidationIssue> issues = new ArrayList<>();
        for (EchoCompatContentMapping mapping : registry.all()) {
            validateMapping(mapping, world, entities, items, gameplay, issues);
        }
        return new EchoCompatValidationResult(issues);
    }

    private static void validateMapping(
            EchoCompatContentMapping mapping,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayRuntimeResult gameplay,
            ArrayList<EchoCompatValidationIssue> issues
    ) {
        boolean present = switch (mapping.targetKind()) {
            case STANDALONE_ITEM -> items.registry().find(new EchoItemId(mapping.targetId())).isPresent();
            case STANDALONE_WORLD_REGION -> world.world().regions().stream()
                    .anyMatch(region -> region.id().equals(mapping.targetId()));
            case STANDALONE_WORLD_HAZARD -> world.world().chunks().stream()
                    .flatMap(chunk -> chunk.hazards().stream())
                    .anyMatch(hazard -> hazard.id().equals(mapping.targetId()));
            case STANDALONE_ENTITY -> entities.store().all().stream()
                    .anyMatch(entity -> entity.definition().definitionId().equals(mapping.targetId()));
            case STANDALONE_GAMEPLAY_MISSION -> gameplay.mission().missionId().equals(mapping.targetId());
            case STANDALONE_SAVE_RECORD -> mapping.status() == EchoCompatMappingStatus.MANUAL_REVIEW;
        };
        if (!present) {
            issues.add(new EchoCompatValidationIssue(
                    EchoCompatDiagnosticSeverity.ERROR,
                    mapping.mappingId(),
                    "Target is not present in standalone debug runtime: " + mapping.targetId()
            ));
        }
        if (mapping.status() == EchoCompatMappingStatus.MANUAL_REVIEW) {
            issues.add(new EchoCompatValidationIssue(
                    EchoCompatDiagnosticSeverity.WARNING,
                    mapping.mappingId(),
                    "Manual review required before migration: " + mapping.sourceId()
            ));
        }
    }
}
