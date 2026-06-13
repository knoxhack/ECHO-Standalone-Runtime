package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;

import java.util.Set;

final class EchoClientRenderRuntimeController {
    private final EchoGlfwWindow window;
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientGameplayRuntimeController gameplayRuntime;
    private final EchoClientSlotGridController slotGrid;

    private EchoClientRenderer renderer;
    private EchoClientHud hud;
    private EchoClientAudio audio;
    private EchoClientParticleRuntimeController particleRuntime;
    private EchoClientFramePacingSnapshot framePacing = EchoClientFramePacingSnapshot.EMPTY;
    private EchoVoxelBiome cachedEnvironmentBiome;
    private EchoClientBiomeEnvironment cachedEnvironment = EchoClientBiomeEnvironment.DEFAULT;
    private int biomeEnvironmentBuildCount;
    private int biomeEnvironmentCacheHitCount;

    EchoClientRenderRuntimeController(
            EchoGlfwWindow window,
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientGameplayRuntimeController gameplayRuntime,
            EchoClientSlotGridController slotGrid
    ) {
        this.window = window;
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.gameplayRuntime = gameplayRuntime;
        this.slotGrid = slotGrid;
    }

    void attach(EchoClientRenderer renderer, EchoClientHud hud, EchoClientAudio audio) {
        this.renderer = renderer;
        this.hud = hud;
        this.audio = audio;
        clearBiomeEnvironmentCache();
    }

    EchoClientRenderer renderer() {
        return renderer;
    }

    void attachParticles(EchoClientParticleRuntimeController particleRuntime) {
        this.particleRuntime = particleRuntime;
    }

    void reloadMinecraftAssets(boolean rebuildAtlas) {
        clearBiomeEnvironmentCache();
        if (renderer != null) {
            renderer.setMinecraftAssets(runtimeServices.minecraftAssets());
        }
        if (hud != null) {
            hud.setMinecraftAssets(runtimeServices.minecraftAssets());
            hud.setLanguage(runtimeServices.language());
        }
        EchoClientGameSession session = runtimeServices.session();
        if (rebuildAtlas && session != null && renderer != null) {
            renderer.rebuildAtlas(session.world());
        }
    }

    void setChunkViewDistance(int chunkViewDistance) {
        if (renderer != null) {
            renderer.setChunkViewDistance(chunkViewDistance);
        }
    }

    void attachActiveSession() {
        EchoClientGameSession session = runtimeServices.session();
        if (session == null || renderer == null) {
            return;
        }
        EchoClientGameplay gameplay = runtimeServices.gameplay();
        clearBiomeEnvironmentCache();
        gameplay.init(session.world(), session.player(), session.hotbar());
        renderer.rebuildAtlas(session.world());
        renderer.updateChunks(session.world(), gameplayCamera(session.player().state()));
    }

    void refreshWorldStreamingAndMeshes() {
        EchoClientGameSession session = runtimeServices.session();
        if (session == null || renderer == null) {
            return;
        }
        EchoClientGameplay gameplay = runtimeServices.gameplay();
        EchoClientWorldStreamResult streamResult =
                runtimeServices.streamAroundPlayer(screens.clientSettings().chunkViewDistance());
        session = runtimeServices.session();
        if (session == null) {
            return;
        }
        if (streamResult.loadedChunksChanged()) {
            gameplay.init(session.world(), session.player(), session.hotbar());
            renderer.rebuildAtlasIfSourceChanged(session.world());
            renderer.updateChunks(session.world(), gameplayCamera(session.player().state()));
        } else if (gameplay.isWorldDirty()) {
            Set<EchoVoxelChunkId> dirtyChunkIds = gameplay.dirtyChunkIds();
            if (dirtyChunkIds.isEmpty()) {
                renderer.updateChunks(session.world(), gameplayCamera(session.player().state()));
            } else {
                renderer.updateDirtyChunks(session.world(), dirtyChunkIds);
            }
            gameplay.init(session.world(), session.player(), session.hotbar());
        } else if (streamResult.renderRegionChanged()) {
            renderer.updateChunks(session.world(), gameplayCamera(session.player().state()));
        }
    }

    EchoClientUiViewport viewport() {
        return EchoClientUiScale.viewport(
                screens.clientSettings().uiScalePercent(),
                window.width(),
                window.height()
        );
    }

    EchoVoxelCamera gameplayCamera(EchoVoxelPlayerState state) {
        return state.camera(screens.clientSettings().fovDegrees());
    }

    EchoClientFramePacingSnapshot framePacingSnapshot() {
        return framePacing;
    }

    void render(
            int fps,
            long environmentTick,
            EchoClientInput input,
            EchoClientFramePacingSnapshot framePacing
    ) {
        if (renderer == null || hud == null) {
            return;
        }
        this.framePacing = framePacing == null ? EchoClientFramePacingSnapshot.EMPTY : framePacing;
        EchoClientGameSession session = runtimeServices.session();
        EchoClientUiViewport viewport = viewport();
        if (session == null || screens.state() == EchoClientGameState.FATAL_ERROR) {
            clearBiomeEnvironmentCache();
            renderer.clearShell();
            hud.renderShell(
                    viewport.logicalWidth(),
                    viewport.logicalHeight(),
                    screens.snapshot(runtimeServices.hasContinuableSession())
            );
            return;
        }

        EchoVoxelPlayerState state = session.player().state();
        EchoVoxelCamera camera = gameplayCamera(state);
        var target = runtimeServices.gameplay().target();
        var targetBlock = target == null ? dev.echo.standalone.runtime.world.EchoVoxelBlock.AIR : target.block();
        applyBiomeEnvironment(session, environmentTick);
        renderer.render(
                camera,
                target,
                runtimeServices.gameplay().breakProgress(),
                session.renderEntities(camera, screens.clientSettings().chunkViewDistance()),
                session.entityCatalog(),
                session.renderDroppedItems(camera, screens.clientSettings().chunkViewDistance()),
                particleRuntime == null ? java.util.List.of() : particleRuntime.particles()
        );
        hud.render(
                viewport.logicalWidth(),
                viewport.logicalHeight(),
                state,
                session.inventoryScreenModel(),
                session.playerVitals(),
                session.playerCombatState(),
                session.progressionState(),
                session.hazardState(),
                session.selectedToolStatus(targetBlock),
                target,
                false,
                gameplayRuntime.debugOverlayEnabled()
                        ? EchoClientDebugOverlay.text(
                        fps,
                        screens.state(),
                        screens.screenKind(),
                        session,
                        runtimeServices.gameplay(),
                        renderer,
                        this.framePacing
                )
                        : "",
                audio == null ? java.util.List.of() : audio.subtitleLines()
        );

        if (screens.state() != EchoClientGameState.IN_GAME) {
            hud.renderOverlay(
                    viewport.logicalWidth(),
                    viewport.logicalHeight(),
                    screens.snapshot(runtimeServices.hasContinuableSession()),
                    activeInventoryModel(),
                    activeContainerModel(),
                    activeEquipmentModel(),
                    activeWorkbenchModel(),
                    slotGrid.dragSlot(),
                    runtimeServices.cursorSlotStack(),
                    viewport.logicalPointerX(input.pointerX()),
                    viewport.logicalPointerY(input.pointerY())
            );
        }
    }

    private EchoClientWorkbenchScreenModel activeWorkbenchModel() {
        return screens.screenKind() == EchoClientScreenKind.WORKBENCH
                ? runtimeServices.workbenchScreenModel(screens.selectedWorkbenchRecipeId())
                : null;
    }

    private EchoClientEquipmentScreenModel activeEquipmentModel() {
        return screens.screenKind() == EchoClientScreenKind.INVENTORY
                ? runtimeServices.equipmentScreenModel()
                : null;
    }

    private EchoClientInventoryScreenModel activeInventoryModel() {
        return screens.screenKind() == EchoClientScreenKind.INVENTORY
                || screens.screenKind() == EchoClientScreenKind.CONTAINER
                ? runtimeServices.inventoryScreenModel()
                : null;
    }

    private EchoClientInventoryScreenModel activeContainerModel() {
        return screens.screenKind() == EchoClientScreenKind.CONTAINER
                ? runtimeServices.containerScreenModel()
                : null;
    }

    private void applyBiomeEnvironment(EchoClientGameSession session, long environmentTick) {
        if (session == null || renderer == null) {
            return;
        }
        EchoVoxelPlayerState state = session.player().state();
        EchoClientBiomeEnvironment environment = resolveBiomeEnvironment(session.world().biomeAt(state.x(), state.z()));
        if (renderer.biomeEnvironment() != environment) {
            renderer.setBiomeEnvironment(environment);
        }
        if (audio != null) {
            audio.applyBiomeEnvironment(environment, environmentTick);
        }
    }

    EchoClientBiomeEnvironment resolveBiomeEnvironment(EchoVoxelBiome biome) {
        if (biome == null) {
            biome = dev.echo.standalone.runtime.world.EchoVoxelBiomeSources.defaultBiome();
        }
        if (cachedEnvironmentBiome == biome || (cachedEnvironmentBiome != null && cachedEnvironmentBiome.equals(biome))) {
            biomeEnvironmentCacheHitCount++;
            return cachedEnvironment;
        }
        cachedEnvironmentBiome = biome;
        cachedEnvironment = EchoClientBiomeEnvironment.fromBiome(biome);
        biomeEnvironmentBuildCount++;
        return cachedEnvironment;
    }

    int biomeEnvironmentBuildCount() {
        return biomeEnvironmentBuildCount;
    }

    int biomeEnvironmentCacheHitCount() {
        return biomeEnvironmentCacheHitCount;
    }

    void clearBiomeEnvironmentCache() {
        cachedEnvironmentBiome = null;
        cachedEnvironment = EchoClientBiomeEnvironment.DEFAULT;
    }
}
