package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiFrame;
import dev.echo.standalone.runtime.world.EchoWorldCell;
import dev.echo.standalone.runtime.world.EchoWorldChunk;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldWeatherField;

import java.util.ArrayList;
import java.util.Objects;

public final class EchoRenderSceneBuilder {
    private final EchoRenderUiBridge uiBridge;

    public EchoRenderSceneBuilder(EchoRenderUiBridge uiBridge) {
        this.uiBridge = Objects.requireNonNull(uiBridge, "uiBridge");
    }

    public EchoRenderScene buildDebugScene(
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoUiFrame uiFrame
    ) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(gameplay, "gameplay");
        Objects.requireNonNull(uiFrame, "uiFrame");
        ArrayList<EchoRenderCommand> commands = new ArrayList<>();
        commands.add(new EchoRenderCommand(
                "background:clear",
                EchoRenderLayer.BACKGROUND,
                EchoRenderCommandType.CLEAR,
                0.0D,
                0.0D,
                -1.0D,
                1.0D,
                1.0D,
                "color:#061014",
                "clear"
        ));
        for (EchoWorldChunk chunk : world.world().chunks()) {
            for (EchoWorldCell cell : chunk.cells()) {
                commands.add(worldTile(cell));
            }
            addParticles(commands, chunk.weather());
        }
        for (EchoEntityState entity : entities.store().all()) {
            commands.add(entitySprite(entity));
        }
        commands.add(new EchoRenderCommand(
                "diagnostic:mission",
                EchoRenderLayer.DIAGNOSTIC,
                EchoRenderCommandType.TEXT,
                0.0D,
                0.0D,
                20.0D,
                0.0D,
                0.0D,
                "diagnostic:mission",
                "mission=" + gameplay.mission().status().name()
        ));
        commands.addAll(uiBridge.commands(uiFrame));
        return new EchoRenderScene("ashfall-debug-scene", EchoRenderCameras.ashfallPreview(), commands);
    }

    private static EchoRenderCommand worldTile(EchoWorldCell cell) {
        String material = cell.blocked()
                ? "world:blocked"
                : cell.hazardIds().isEmpty()
                        ? "terrain:" + cell.terrain()
                        : "terrain:" + cell.terrain() + ":hazard";
        return new EchoRenderCommand(
                "world:tile:" + cell.position().key(),
                EchoRenderLayer.WORLD,
                EchoRenderCommandType.TILE,
                cell.position().x(),
                cell.position().z(),
                0.0D,
                1.0D,
                1.0D,
                material,
                cell.terrain()
        );
    }

    private static EchoRenderCommand entitySprite(EchoEntityState entity) {
        String material = entity.definition().kind() == EchoEntityKind.PLAYER
                ? "entity:player"
                : entity.hostile()
                        ? "entity:hostile"
                        : "entity:npc";
        return new EchoRenderCommand(
                "entity:" + entity.id().fileSafeKey(),
                EchoRenderLayer.ENTITY,
                EchoRenderCommandType.ENTITY,
                entity.worldPosition().x() + 0.5D,
                entity.worldPosition().z() + 0.5D,
                1.0D,
                0.7D,
                0.9D,
                material,
                entity.definition().displayName()
        );
    }

    private static void addParticles(ArrayList<EchoRenderCommand> commands, EchoWorldWeatherField weather) {
        int particleCount = 5;
        for (int index = 0; index < particleCount; index++) {
            commands.add(new EchoRenderCommand(
                    "particle:ash:" + index,
                    EchoRenderLayer.PARTICLE,
                    EchoRenderCommandType.PARTICLE,
                    0.25D + index * 0.65D,
                    0.35D + (index % 3) * 0.55D,
                    2.0D,
                    0.1D,
                    0.1D,
                    "particle:ash:" + weather.profileId(),
                    "ash-density=" + String.format(java.util.Locale.ROOT, "%.2f", weather.ashDensity())
            ));
        }
    }
}
