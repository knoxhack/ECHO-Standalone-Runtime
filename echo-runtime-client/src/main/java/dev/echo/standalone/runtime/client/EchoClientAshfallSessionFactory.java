package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.item.EchoInventoryOperations;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles;
import dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.util.List;

final class EchoClientAshfallSessionFactory implements EchoClientGameSessionFactory {
    private static final long DEFAULT_SEED = 42L;
    private static final int STREAM_RADIUS = 3;
    private static final EchoAdapterCoreStandaloneContentBridge DEFAULT_BRIDGE =
            EchoAdapterCoreStandaloneContentBridge.ashfallLive();
    private static final EchoClientContentProfiles.Profile CONTENT_PROFILE =
            EchoClientContentProfiles.ashfallCrashSite();
    private static final EchoClientAshfallSessionFactory INSTANCE =
            new EchoClientAshfallSessionFactory(DEFAULT_BRIDGE);
    private final EchoAdapterCoreStandaloneContentBridge contentBridge;
    private final List<EchoItemDefinition> runtimeItemDefinitions;
    private final EchoClientEntityCatalog entityCatalog;
    private final EchoClientHazardCatalog hazardCatalog;
    private final EchoClientWorldInteractionCatalog interactionCatalog;
    private final EchoClientRuntimeWorldgenCatalog worldgenCatalog;

    private EchoClientAshfallSessionFactory(EchoAdapterCoreStandaloneContentBridge contentBridge) {
        this(
                contentBridge,
                List.of(),
                CONTENT_PROFILE.entityCatalog(),
                CONTENT_PROFILE.hazardCatalog(),
                CONTENT_PROFILE.interactionCatalog(),
                EchoClientRuntimeWorldgenCatalog.empty()
        );
    }

    private EchoClientAshfallSessionFactory(
            EchoAdapterCoreStandaloneContentBridge contentBridge,
            List<EchoItemDefinition> runtimeItemDefinitions,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog,
            EchoClientWorldInteractionCatalog interactionCatalog,
            EchoClientRuntimeWorldgenCatalog worldgenCatalog
    ) {
        this.contentBridge = contentBridge == null ? DEFAULT_BRIDGE : contentBridge;
        this.runtimeItemDefinitions = runtimeItemDefinitions == null ? List.of() : List.copyOf(runtimeItemDefinitions);
        this.entityCatalog = entityCatalog == null ? CONTENT_PROFILE.entityCatalog() : entityCatalog;
        this.hazardCatalog = hazardCatalog == null ? CONTENT_PROFILE.hazardCatalog() : hazardCatalog;
        this.interactionCatalog = interactionCatalog == null
                ? CONTENT_PROFILE.interactionCatalog()
                : interactionCatalog;
        this.worldgenCatalog = worldgenCatalog == null ? EchoClientRuntimeWorldgenCatalog.empty() : worldgenCatalog;
    }

    static EchoClientAshfallSessionFactory instance() {
        return INSTANCE;
    }

    static EchoClientAshfallSessionFactory forContentBridge(EchoAdapterCoreStandaloneContentBridge contentBridge) {
        return contentBridge == null || contentBridge == DEFAULT_BRIDGE
                ? INSTANCE
                : new EchoClientAshfallSessionFactory(contentBridge);
    }

    static EchoClientAshfallSessionFactory forRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge contentBridge,
            List<EchoItemDefinition> runtimeItemDefinitions
    ) {
        return forRuntimeContent(
                contentBridge,
                runtimeItemDefinitions,
                CONTENT_PROFILE.entityCatalog(),
                CONTENT_PROFILE.hazardCatalog(),
                CONTENT_PROFILE.interactionCatalog()
        );
    }

    static EchoClientAshfallSessionFactory forRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge contentBridge,
            List<EchoItemDefinition> runtimeItemDefinitions,
            EchoClientEntityCatalog entityCatalog
    ) {
        return forRuntimeContent(
                contentBridge,
                runtimeItemDefinitions,
                entityCatalog,
                CONTENT_PROFILE.hazardCatalog(),
                CONTENT_PROFILE.interactionCatalog()
        );
    }

    static EchoClientAshfallSessionFactory forRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge contentBridge,
            List<EchoItemDefinition> runtimeItemDefinitions,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog
    ) {
        return forRuntimeContent(
                contentBridge,
                runtimeItemDefinitions,
                entityCatalog,
                hazardCatalog,
                CONTENT_PROFILE.interactionCatalog()
        );
    }

    static EchoClientAshfallSessionFactory forRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge contentBridge,
            List<EchoItemDefinition> runtimeItemDefinitions,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog,
            EchoClientWorldInteractionCatalog interactionCatalog
    ) {
        return forRuntimeContent(
                contentBridge,
                runtimeItemDefinitions,
                entityCatalog,
                hazardCatalog,
                interactionCatalog,
                EchoClientRuntimeWorldgenCatalog.empty()
        );
    }

    static EchoClientAshfallSessionFactory forRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge contentBridge,
            List<EchoItemDefinition> runtimeItemDefinitions,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog,
            EchoClientWorldInteractionCatalog interactionCatalog,
            EchoClientRuntimeWorldgenCatalog worldgenCatalog
    ) {
        List<EchoItemDefinition> definitions = runtimeItemDefinitions == null
                ? List.of()
                : List.copyOf(runtimeItemDefinitions);
        EchoClientEntityCatalog catalog = entityCatalog == null ? CONTENT_PROFILE.entityCatalog() : entityCatalog;
        EchoClientHazardCatalog hazards = hazardCatalog == null ? CONTENT_PROFILE.hazardCatalog() : hazardCatalog;
        EchoClientWorldInteractionCatalog interactions = interactionCatalog == null
                ? CONTENT_PROFILE.interactionCatalog()
                : interactionCatalog;
        EchoClientRuntimeWorldgenCatalog worldgen = worldgenCatalog == null
                ? EchoClientRuntimeWorldgenCatalog.empty()
                : worldgenCatalog;
        return (contentBridge == null || contentBridge == DEFAULT_BRIDGE)
                        && definitions.isEmpty()
                        && catalog == CONTENT_PROFILE.entityCatalog()
                        && hazards == CONTENT_PROFILE.hazardCatalog()
                        && interactions == CONTENT_PROFILE.interactionCatalog()
                        && worldgen.emptyCatalog()
                ? INSTANCE
                : new EchoClientAshfallSessionFactory(contentBridge, definitions, catalog, hazards, interactions, worldgen);
    }

    @Override
    public long defaultSeed() {
        return DEFAULT_SEED;
    }

    @Override
    public EchoAdapterCoreStandaloneContentBridge contentBridge() {
        return contentBridge;
    }

    @Override
    public EchoClientStarterLoadout starterLoadout() {
        return CONTENT_PROFILE.starterLoadout();
    }

    @Override
    public EchoClientEntityCatalog entityCatalog() {
        return entityCatalog;
    }

    @Override
    public EchoClientHazardCatalog hazardCatalog() {
        return hazardCatalog;
    }

    @Override
    public EchoClientWorldInteractionCatalog interactionCatalog() {
        return interactionCatalog;
    }

    @Override
    public EchoVoxelWorldStreamer streamer() {
        return voxelSessionProfile(contentBridge()).streamer();
    }

    @Override
    public EchoClientGameSession newSession(String seedText, List<EchoRecipeDefinition> dataRecipes) {
        return newCrashSite(seedText, dataRecipes, List.of());
    }

    @Override
    public EchoClientGameSession newSession(
            String seedText,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return newCrashSite(seedText, dataRecipes, dataTags);
    }

    @Override
    public EchoClientGameSession restoreSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes
    ) {
        return fromSavedSnapshot(snapshot, dataRecipes, List.of());
    }

    @Override
    public EchoClientGameSession restoreSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return fromSavedSnapshot(snapshot, dataRecipes, dataTags);
    }

    @Override
    public EchoClientGameSession restoreGameplaySnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes
    ) {
        return restoreGameplaySnapshot(snapshot, dataRecipes, List.of());
    }

    @Override
    public EchoClientGameSession restoreGameplaySnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return fromSnapshot(
                snapshot,
                List.of(),
                List.of(),
                dataRecipes,
                dataTags,
                EchoClientPlayerVitals.full(),
                EchoClientPlayerCombatState.defaults(),
                EchoClientProgressionState.empty(),
                EchoClientHazardState.empty(),
                EchoClientToolState.empty(),
                List.of(),
                List.of(),
                EchoClientMachineStateSnapshot.reference()
        );
    }

    private EchoClientGameSession newCrashSite(
            String seedText,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        long seed = seedFromText(seedText);
        EchoAdapterCoreStandaloneContentBridge bridge = contentBridge();
        EchoVoxelSessionRuntimeProfile sessionProfile = voxelSessionProfile(bridge);
        EchoVoxelWorld world = sessionProfile.generateAndStream(seed);
        EchoVoxelWorldStreamer streamer = sessionProfile.streamer();
        EchoVoxelPlayerController player =
                EchoVoxelPlayerController.spawnAt(world, world.spawnX(), world.spawnZ(), world.spawnYawDegrees(), -32.0D);
        EchoVoxelPlayerHotbar hotbar = sessionProfile.newStarterHotbar();
        EchoClientWorkbenchLoadout workbench =
                EchoClientWorkbenchLoadoutFactory.fromStarterLoadout(
                starterLoadout(),
                dataRecipes,
                runtimeItemDefinitions,
                dataTags
        );
        EchoClientGameSession session = new EchoClientGameSession(
                bridge,
                world,
                player,
                hotbar,
                new EchoInventoryOperations(),
                starterLoadout().newPlayerInventory(),
                starterLoadout().newOpenContainer(),
                workbench.registry(),
                workbench.recipes(),
                workbench.lootTables(),
                streamer,
                entityCatalog(),
                hazardCatalog(),
                interactionCatalog,
                CONTENT_PROFILE.worldTemplate().presentation()
        );
        session.materializeMachineBlockEntities();
        return session;
    }

    private EchoClientGameSession fromSavedSnapshot(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes
    ) {
        return fromSavedSnapshot(snapshot, dataRecipes, List.of());
    }

    private EchoClientGameSession fromSavedSnapshot(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags
    ) {
        return fromSnapshot(
                snapshot.gameplay(),
                snapshot.inventorySlots(),
                snapshot.containerSlots(),
                dataRecipes,
                dataTags,
                snapshot.playerVitals(),
                snapshot.playerCombatState(),
                snapshot.progressionState(),
                snapshot.hazardState(),
                snapshot.toolState(),
                snapshot.entities(),
                snapshot.droppedItems(),
                snapshot.machineState()
        );
    }

    private EchoClientGameSession fromSnapshot(
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoClientInventorySlotSnapshot> inventorySlots,
            List<EchoClientInventorySlotSnapshot> containerSlots,
            List<EchoRecipeDefinition> dataRecipes,
            List<EchoDataTag> dataTags,
            EchoClientPlayerVitals playerVitals,
            EchoClientPlayerCombatState playerCombatState,
            EchoClientProgressionState progressionState,
            EchoClientHazardState hazardState,
            EchoClientToolState toolState,
            List<EchoClientEntitySnapshot> entitySnapshots,
            List<EchoClientDroppedItemSnapshot> droppedItemSnapshots,
            EchoClientMachineStateSnapshot machineState
    ) {
        EchoAdapterCoreStandaloneContentBridge bridge = contentBridge();
        EchoVoxelSessionRuntimeProfile sessionProfile = voxelSessionProfile(bridge);
        EchoVoxelWorldStreamer streamer = sessionProfile.streamer();
        EchoVoxelPlayerController player = new EchoVoxelPlayerController(snapshot.player());
        EchoClientWorkbenchLoadout workbench =
                EchoClientWorkbenchLoadoutFactory.fromStarterLoadout(
                starterLoadout(),
                dataRecipes,
                runtimeItemDefinitions,
                dataTags
        );
        EchoClientGameSession session = new EchoClientGameSession(
                bridge,
                snapshot.world(),
                player,
                snapshot.hotbar(),
                new EchoInventoryOperations(),
                starterLoadout().newPlayerInventory(),
                starterLoadout().newOpenContainer(),
                workbench.registry(),
                workbench.recipes(),
                workbench.lootTables(),
                streamer,
                entityCatalog(),
                hazardCatalog(),
                interactionCatalog,
                CONTENT_PROFILE.worldTemplate().presentation()
        );
        session.applyInventorySnapshot(inventorySlots);
        session.applyContainerSnapshot(containerSlots);
        session.restorePlayerRuntime(
                playerVitals,
                playerCombatState,
                progressionState,
                hazardState,
                toolState
        );
        session.applyDroppedItemSnapshots(droppedItemSnapshots);
        session.applyEntitySnapshots(entitySnapshots);
        session.applyMachineStateSnapshot(machineState);
        session.materializeMachineBlockEntities();
        return session;
    }

    private static long seedFromText(String seedText) {
        String normalized = seedText == null || seedText.isBlank() ? Long.toString(DEFAULT_SEED) : seedText.trim();
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException ignored) {
            return normalized.hashCode();
        }
    }

    private EchoVoxelSessionRuntimeProfile voxelSessionProfile(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelSessionRuntimeProfile base = EchoVoxelSessionProfiles.ashfallCrashSite(
                bridge.registry()::requireLiveVoxelBlock,
                bridge.runtimeMarkerBlock(),
                STREAM_RADIUS
        );
        return worldgenCatalog.decorate(base, bridge);
    }
}
