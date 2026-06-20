package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeContentRegistry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientRuntimeServices {
    private final EchoClientGameplay gameplay = new EchoClientGameplay();
    private final EchoClientSaveSlotService saveSlots;
    private final EchoClientModScanService modScan;
    private final EchoClientModuleBootstrapResult moduleBootstrap;
    private final EchoClientResourcePackService resourcePacks;
    private final EchoClientWorkbenchRecipeService workbenchRecipes = new EchoClientWorkbenchRecipeService();
    private final EchoClientSupportBundleService supportBundles;
    private final EchoClientDataWorldgenStructureService dataWorldgenStructures =
            new EchoClientDataWorldgenStructureService();
    private final EchoClientDataWorldgenFeatureService dataWorldgenFeatures =
            new EchoClientDataWorldgenFeatureService();
    private final EchoClientDataWorldgenBiomeService dataWorldgenBiomes =
            new EchoClientDataWorldgenBiomeService();
    private final EchoClientDataWorldCoreRegionService dataWorldCoreRegions =
            new EchoClientDataWorldCoreRegionService();
    private final EchoClientDataWorldCoreHazardService dataWorldCoreHazards =
            new EchoClientDataWorldCoreHazardService();
    private final EchoAdapterCoreRuntimeContentRegistry runtimeContentRegistrations =
            new EchoAdapterCoreRuntimeContentRegistry();
    private final EchoClientEntityCatalog baseEntityCatalog;
    private final EchoClientHazardCatalog baseHazardCatalog;
    private final EchoClientWorldInteractionCatalog baseInteractionCatalog;
    private EchoClientWorldSessionFactory worldSessions;
    private EchoAdapterCoreStandaloneContentBridge runtimeContentBridge;
    private EchoClientScreenCatalog screenCatalog;
    private EchoClientWorldSession worldSession;
    private EchoClientSavedSessionSnapshot memorySave;
    private EchoClientAudio audio;
    private EchoClientSupportBundleResult lastSupportBundle = EchoClientSupportBundleResult.EMPTY;
    private String memorySaveSlotId = "";
    private String memorySaveDisplayName = "";
    private Boolean diskContinueAvailable;

    EchoClientRuntimeServices() {
        this(EchoClientSaveSlotService.openDefault());
    }

    EchoClientRuntimeServices(EchoClientSaveSlotService saveSlots) {
        this(
                saveSlots,
                EchoClientWorldSessionFactory.defaultFactory(),
                new EchoClientResourcePackService(),
                EchoClientModuleBootstrapResult.inactive()
        );
    }

    EchoClientRuntimeServices(
            EchoClientSaveSlotService saveSlots,
            EchoClientResourcePackService resourcePacks
    ) {
        this(
                saveSlots,
                EchoClientWorldSessionFactory.defaultFactory(),
                resourcePacks,
                EchoClientModuleBootstrapResult.inactive()
        );
    }

    static EchoClientRuntimeServices forTemplate(EchoClientWorldTemplate template) {
        return forTemplate(template, null, EchoClientModuleBootstrapResult.inactive());
    }

    static EchoClientRuntimeServices forTemplate(
            EchoClientWorldTemplate template,
            java.nio.file.Path saveRoot
    ) {
        return forTemplate(template, saveRoot, EchoClientModuleBootstrapResult.inactive());
    }

    static EchoClientRuntimeServices forTemplate(
            EchoClientWorldTemplate template,
            EchoClientModuleBootstrapResult moduleBootstrap
    ) {
        return forTemplate(template, null, moduleBootstrap);
    }

    static EchoClientRuntimeServices forTemplate(
            EchoClientWorldTemplate template,
            java.nio.file.Path saveRoot,
            EchoClientModuleBootstrapResult moduleBootstrap
    ) {
        EchoClientWorldTemplate safeTemplate = template == null
                ? EchoClientWorldTemplates.defaultTemplate()
                : template;
        java.nio.file.Path root = saveRoot == null
                ? defaultSaveRoot(safeTemplate)
                : saveRoot;
        return new EchoClientRuntimeServices(
                EchoClientSaveSlotService.open(root, safeTemplate),
                EchoClientWorldSessionFactory.forTemplate(safeTemplate),
                new EchoClientResourcePackService(),
                moduleBootstrap
        );
    }

    static EchoClientRuntimeServices openlandsStandard(java.nio.file.Path saveRoot) {
        return forTemplate(EchoClientWorldTemplates.openlandsFirstHour(), saveRoot);
    }

    private EchoClientRuntimeServices(
            EchoClientSaveSlotService saveSlots,
            EchoClientWorldSessionFactory worldSessions,
            EchoClientResourcePackService resourcePacks,
            EchoClientModuleBootstrapResult moduleBootstrap
    ) {
        this.saveSlots = saveSlots == null ? EchoClientSaveSlotService.openDefault() : saveSlots;
        this.supportBundles = new EchoClientSupportBundleService(this.saveSlots.saveRoot());
        this.worldSessions = worldSessions == null ? EchoClientWorldSessionFactory.defaultFactory() : worldSessions;
        this.moduleBootstrap = moduleBootstrap == null
                ? EchoClientModuleBootstrapResult.inactive()
                : moduleBootstrap;
        this.modScan = this.moduleBootstrap.active() ? null : new EchoClientModScanService();
        this.resourcePacks = resourcePacks == null ? new EchoClientResourcePackService() : resourcePacks;
        this.baseEntityCatalog = this.worldSessions.template().entityCatalog();
        this.baseHazardCatalog = this.worldSessions.template().hazardCatalog();
        this.baseInteractionCatalog = this.worldSessions.template().interactionCatalog();
        this.runtimeContentBridge = this.worldSessions.template().contentBridge();
        this.screenCatalog = EchoClientScreenCatalog.loadDefault(
                runtimeContentBridge,
                this.worldSessions.template().presentation()
        );
        workbenchRecipes.refresh(this.resourcePacks.assets());
        dataWorldgenStructures.refresh(this.resourcePacks.assets());
        dataWorldgenBiomes.refresh(this.resourcePacks.assets());
        dataWorldgenFeatures.refresh(this.resourcePacks.assets());
        dataWorldCoreRegions.refresh(this.resourcePacks.assets());
        dataWorldCoreHazards.refresh(this.resourcePacks.assets());
        refreshWorldSessionFactory();
        importAdapterCoreContentRegistrations(this.moduleBootstrap.adapterCoreContentRows());
    }

    private static java.nio.file.Path defaultSaveRoot(EchoClientWorldTemplate template) {
        EchoClientWorldTemplate safeTemplate = template == null
                ? EchoClientWorldTemplates.defaultTemplate()
                : template;
        String profileId = safeTemplate.saveProfile().profileId();
        if ("echo-client".equals(profileId)) {
            return java.nio.file.Path.of("saves").resolve("client");
        }
        if ("echo-client-openlands".equals(profileId)) {
            return java.nio.file.Path.of("saves").resolve("openlands-client");
        }
        return java.nio.file.Path.of("saves").resolve(safePathSegment(profileId));
    }

    private static String safePathSegment(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
        if (normalized.isBlank()) {
            return "client";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < normalized.length(); i++) {
            char ch = normalized.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9') || ch == '-' || ch == '_') {
                builder.append(ch);
            } else {
                builder.append('-');
            }
        }
        return builder.isEmpty() ? "client" : builder.toString();
    }

    EchoClientGameplay gameplay() {
        return gameplay;
    }

    void setAudio(EchoClientAudio audio) {
        this.audio = audio;
        gameplay.setAudio(audio);
    }

    EchoClientWorldSession worldSession() {
        return worldSession;
    }

    EchoClientGameSession session() {
        return worldSession == null ? null : worldSession.gameSession();
    }

    boolean hasActiveWorld() {
        return worldSession != null;
    }

    boolean hasContinuableSession() {
        return worldSession != null || memorySave != null || hasDiskContinueSlot();
    }

    boolean hasMemorySave() {
        return memorySave != null;
    }

    java.util.List<EchoClientSaveSlotSummary> saveSlotSummaries() {
        java.util.List<EchoClientSaveSlotSummary> summaries =
                saveSlots.listSlots(
                        memorySave == null ? "" : memorySaveSlotId,
                        runtimeContentRegistrations.registrations(""),
                        currentSaveEnvironmentMetadata()
                );
        diskContinueAvailable = summaries.stream().anyMatch(EchoClientSaveSlotSummary::loadableInMemory);
        return summaries;
    }

    String saveSlotError() {
        return saveSlots.lastError();
    }

    EchoClientSupportBundleResult exportSupportBundle(
            EchoClientScreenSnapshot screen,
            EchoClientSettings settings,
            EchoClientRuntimeDiagnosticsSnapshot diagnostics
    ) {
        EchoClientRuntimeDiagnosticsSnapshot safeDiagnostics = diagnostics == null
                ? runtimeDiagnosticsSnapshot()
                : diagnostics;
        lastSupportBundle = supportBundles.export(
                screen,
                safeDiagnostics,
                settings,
                saveSlotSummaries(),
                modScanSummary(),
                runtimeContentSummary(),
                resourcePackSummaries(),
                screenCatalog(),
                workbenchRecipeSummaries()
        );
        return lastSupportBundle;
    }

    EchoClientSupportBundleResult lastSupportBundleResult() {
        return lastSupportBundle;
    }

    EchoClientModScanSummary modScanSummary() {
        return moduleBootstrap.active() ? moduleBootstrap.modScanSummary() : modScan.summary();
    }

    EchoClientRuntimeContentSummary runtimeContentSummary() {
        return EchoClientRuntimeContentSummary.fromRows(runtimeContentRegistrations.registrations(""));
    }

    EchoClientCreativeInventoryController.CreativeInventoryModel creativeInventoryModel() {
        return new EchoClientCreativeInventoryController()
                .modelFromRuntimeContentRows(runtimeContentRegistrations.registrations(""));
    }

    EchoClientTechSurfaceModel techSurfaceModel() {
        EchoClientGameSession session = session();
        return session == null ? EchoClientTechSurfaceModel.from(runtimeContentBridge) : session.techSurfaceModel();
    }

    void refreshModScan() {
        if (moduleBootstrap.active()) {
            return;
        }
        modScan.refresh();
    }

    java.util.List<EchoClientResourcePackSummary> resourcePackSummaries() {
        return resourcePacks.resourcePacks();
    }

    String resourcePackError() {
        return resourcePacks.lastError();
    }

    void refreshResourcePacks() {
        resourcePacks.refresh();
        workbenchRecipes.refresh(resourcePacks.assets());
        dataWorldgenStructures.refresh(resourcePacks.assets());
        dataWorldgenBiomes.refresh(resourcePacks.assets());
        dataWorldgenFeatures.refresh(resourcePacks.assets());
        dataWorldCoreRegions.refresh(resourcePacks.assets());
        dataWorldCoreHazards.refresh(resourcePacks.assets());
        refreshWorldSessionFactory();
        refreshActiveWorldSessionRuntimeContent();
    }

    EchoMinecraftAssetResolver minecraftAssets() {
        return resourcePacks.minecraftAssets();
    }

    EchoClientLanguageService language() {
        return resourcePacks.language();
    }

    EchoClientScreenCatalog screenCatalog() {
        return screenCatalog;
    }

    EchoClientRuntimeDiagnosticsSnapshot runtimeDiagnosticsSnapshot() {
        return EchoClientRuntimeDiagnosticsSnapshot.from(worldSession);
    }

    void registerAdapterCoreScreen(EchoAdapterCoreRegistryEntry entry) {
        runtimeContentBridge = runtimeContentBridge.withRuntimeEntry(entry);
        refreshWorldSessionFactory();
        refreshActiveWorldSessionRuntimeContent();
        rebuildScreenCatalog();
    }

    int importAdapterCoreContentRegistrations(List<Map<String, Object>> registrations) {
        int changed = runtimeContentRegistrations.registerAll(registrations);
        if (changed == 0) {
            return 0;
        }
        runtimeContentBridge = runtimeContentBridge.withRuntimeEntriesReplacingContentIds(
                runtimeContentRegistrations.entries()
        );
        refreshWorldSessionFactory();
        refreshActiveWorldSessionRuntimeContent();
        rebuildScreenCatalog();
        return changed;
    }

    List<EchoClientWorkbenchRecipeSummary> workbenchRecipeSummaries() {
        EchoClientGameSession session = session();
        return session == null ? List.of() : session.workbenchRecipeSummaries();
    }

    EchoClientWorkbenchScreenModel workbenchScreenModel(String selectedRecipeId) {
        EchoClientGameSession session = session();
        return session == null ? null : session.workbenchScreenModel(selectedRecipeId);
    }

    String workbenchRecipeError() {
        return workbenchRecipes.lastError();
    }

    int loadedWorkbenchRecipeCount() {
        return workbenchRecipes.recipes().size();
    }

    int loadedDataWorldgenFeatureRowCount() {
        return dataWorldgenFeatures.rows().size();
    }

    String dataWorldgenFeatureError() {
        return dataWorldgenFeatures.lastError();
    }

    int loadedDataWorldgenStructureRowCount() {
        return dataWorldgenStructures.rows().size();
    }

    String dataWorldgenStructureError() {
        return dataWorldgenStructures.lastError();
    }

    int loadedDataWorldgenBiomeRowCount() {
        return dataWorldgenBiomes.rows().size();
    }

    String dataWorldgenBiomeError() {
        return dataWorldgenBiomes.lastError();
    }

    int loadedDataWorldCoreRegionRowCount() {
        return dataWorldCoreRegions.rows().size();
    }

    String dataWorldCoreRegionError() {
        return dataWorldCoreRegions.lastError();
    }

    int loadedDataWorldCoreHazardRowCount() {
        return dataWorldCoreHazards.rows().size();
    }

    String dataWorldCoreHazardError() {
        return dataWorldCoreHazards.lastError();
    }

    EchoItemCraftResult craftWorkbenchRecipe(String recipeId) {
        EchoClientGameSession session = session();
        return session == null ? null : session.craftWorkbenchRecipe(recipeId);
    }

    EchoClientMachineInputResult insertScrapIntoMachine(String machineId) {
        EchoClientGameSession session = session();
        return session == null
                ? new EchoClientMachineInputResult(false, machineId, "echoashfallprotocol:scrap_metal", 0, 0, 0, "no_session")
                : session.insertScrapIntoMachine(machineId);
    }

    EchoClientMachineOutputResult extractCompressedScrapFromMachine(String machineId) {
        EchoClientGameSession session = session();
        return session == null
                ? new EchoClientMachineOutputResult(
                        false,
                        machineId,
                        "echoashfallprotocol:compressed_scrap",
                        0,
                        0,
                        0,
                        "no_session"
                )
                : session.extractCompressedScrapFromMachine(machineId);
    }

    EchoClientMachineRecipeSelectionResult selectMachineRecipe(String machineRecipeTargetId) {
        EchoClientGameSession session = session();
        return session == null
                ? new EchoClientMachineRecipeSelectionResult(
                        false,
                        "",
                        machineRecipeTargetId,
                        "",
                        false,
                        "no_session"
                )
                : session.selectMachineRecipe(machineRecipeTargetId);
    }

    EchoClientInventoryScreenModel inventoryScreenModel() {
        EchoClientGameSession session = session();
        return session == null ? null : session.inventoryScreenModel();
    }

    EchoClientInventoryScreenModel containerScreenModel() {
        EchoClientGameSession session = session();
        return session == null ? null : session.containerScreenModel();
    }

    EchoClientEquipmentScreenModel equipmentScreenModel() {
        EchoClientGameSession session = session();
        return session == null
                ? EchoClientEquipmentScreenModel.fromEquipment(EchoClientEquipmentState.empty())
                : session.equipmentScreenModel();
    }

    boolean inventorySlotEmpty(int slotIndex) {
        EchoClientGameSession session = session();
        return session == null || session.inventorySlotEmpty(slotIndex);
    }

    boolean containerSlotEmpty(int slotIndex) {
        EchoClientGameSession session = session();
        return session == null || session.containerSlotEmpty(slotIndex);
    }

    boolean cursorStackHeld() {
        EchoClientGameSession session = session();
        return session != null && session.cursorStackHeld();
    }

    EchoClientSlotStack cursorSlotStack() {
        EchoClientGameSession session = session();
        return session == null ? EchoClientSlotStack.empty(0) : session.cursorSlotStack();
    }

    boolean primaryClickInventorySlot(int slotIndex) {
        EchoClientGameSession session = session();
        return session != null && session.primaryClickInventorySlot(slotIndex);
    }

    boolean secondaryClickInventorySlot(int slotIndex) {
        EchoClientGameSession session = session();
        return session != null && session.secondaryClickInventorySlot(slotIndex);
    }

    boolean primaryClickContainerSlot(int slotIndex) {
        EchoClientGameSession session = session();
        return session != null && session.primaryClickContainerSlot(slotIndex);
    }

    boolean secondaryClickContainerSlot(int slotIndex) {
        EchoClientGameSession session = session();
        return session != null && session.secondaryClickContainerSlot(slotIndex);
    }

    boolean returnCursorStackToInventory() {
        EchoClientGameSession session = session();
        return session == null || session.returnCursorStackToInventory();
    }

    boolean clickEquipmentSlot(EchoClientArmorSlot armorSlot) {
        EchoClientGameSession session = session();
        return session != null && session.clickEquipmentSlot(armorSlot);
    }

    boolean primaryClickOffhandSlot() {
        EchoClientGameSession session = session();
        return session != null && session.primaryClickOffhandSlot();
    }

    boolean secondaryClickOffhandSlot() {
        EchoClientGameSession session = session();
        return session != null && session.secondaryClickOffhandSlot();
    }

    boolean swapSelectedWithOffhand() {
        EchoClientGameSession session = session();
        return session != null && session.swapSelectedWithOffhand();
    }

    boolean moveOrMergeInventorySlot(int sourceSlot, int targetSlot) {
        EchoClientGameSession session = session();
        return session != null && session.moveOrMergeInventorySlot(sourceSlot, targetSlot).success();
    }

    boolean splitInventorySlotTo(int sourceSlot, int targetSlot) {
        EchoClientGameSession session = session();
        return session != null && session.splitInventorySlotTo(sourceSlot, targetSlot).success();
    }

    boolean quickMoveInventorySlot(int sourceSlot) {
        EchoClientGameSession session = session();
        return session != null && session.quickMoveInventorySlot(sourceSlot).success();
    }

    boolean swapInventorySlots(int sourceSlot, int hotbarSlot) {
        EchoClientGameSession session = session();
        return session != null && session.swapInventorySlots(sourceSlot, hotbarSlot).success();
    }

    EchoClientDroppedItem dropSelectedItem() {
        EchoClientGameSession session = session();
        return session == null ? null : session.dropSelectedItem();
    }

    EchoClientDroppedItem dropCursorStack(int quantity) {
        EchoClientGameSession session = session();
        return session == null ? null : session.dropCursorStack(quantity);
    }

    EchoClientDroppedItem dropInventorySlotStack(int slotIndex, int quantity) {
        EchoClientGameSession session = session();
        return session == null ? null : session.dropInventorySlotStack(slotIndex, quantity);
    }

    EchoClientDroppedItem dropContainerSlotStack(int slotIndex, int quantity) {
        EchoClientGameSession session = session();
        return session == null ? null : session.dropContainerSlotStack(slotIndex, quantity);
    }

    EchoClientDroppedItem dropEquipmentSlot(EchoClientArmorSlot armorSlot) {
        EchoClientGameSession session = session();
        return session == null ? null : session.dropEquipmentSlot(armorSlot);
    }

    EchoClientDroppedItem dropOffhandStack(int quantity) {
        EchoClientGameSession session = session();
        return session == null ? null : session.dropOffhandStack(quantity);
    }

    EchoClientDroppedItemRuntime.PickupResult pickupNearbyDroppedItems(double minimumAgeSeconds) {
        EchoClientGameSession session = session();
        if (session == null) {
            return new EchoClientDroppedItemRuntime.PickupResult(0, 0, "no_session");
        }
        EchoClientDroppedItemRuntime.PickupResult result = session.pickupNearbyDroppedItems(minimumAgeSeconds);
        if (result.pickedQuantity() > 0 && audio != null) {
            audio.playPickup();
        }
        return result;
    }

    boolean moveOrMergeContainerSlot(int sourceSlot, int targetSlot) {
        EchoClientGameSession session = session();
        return session != null && session.moveOrMergeContainerSlot(sourceSlot, targetSlot).success();
    }

    boolean splitContainerSlotTo(int sourceSlot, int targetSlot) {
        EchoClientGameSession session = session();
        return session != null && session.splitContainerSlotTo(sourceSlot, targetSlot).success();
    }

    boolean quickMoveContainerSlotToPlayer(int sourceSlot) {
        EchoClientGameSession session = session();
        return session != null && session.quickMoveContainerSlotToPlayer(sourceSlot).success();
    }

    boolean quickMoveInventorySlotToContainer(int sourceSlot) {
        EchoClientGameSession session = session();
        return session != null && session.quickMoveInventorySlotToContainer(sourceSlot).success();
    }

    boolean swapContainerSlotWithHotbar(int sourceSlot, int hotbarSlot) {
        EchoClientGameSession session = session();
        return session != null && session.swapContainerSlotWithHotbar(sourceSlot, hotbarSlot).success();
    }

    void startNewWorld(String seedText) {
        startNewWorld(seedText, List.of());
    }

    void startNewWorld(String seedText, String worldName) {
        startNewWorld(seedText, worldName, List.of());
    }

    void startNewWorld(String seedText, List<EchoRecipeDefinition> additionalRecipes) {
        worldSession = worldSessions.newWorld(
                seedText,
                combinedWorkbenchRecipes(additionalRecipes),
                workbenchRecipes.tags()
        );
        attachGameplay();
        refreshActiveWorldSessionRuntimeContent();
        memorySave = worldSession.gameSession().savedSessionSnapshot();
        rememberMemorySaveIdentity();
        saveSlots.recordSessionSummary(
                worldSession,
                "new-game",
                runtimeContentRegistrations.registrations(""),
                currentSaveEnvironmentMetadata()
        );
        diskContinueAvailable = true;
    }

    void startNewWorld(String seedText, String worldName, List<EchoRecipeDefinition> additionalRecipes) {
        worldSession = worldSessions.newWorld(
                seedText,
                worldName,
                combinedWorkbenchRecipes(additionalRecipes),
                workbenchRecipes.tags()
        );
        attachGameplay();
        refreshActiveWorldSessionRuntimeContent();
        memorySave = worldSession.gameSession().savedSessionSnapshot();
        rememberMemorySaveIdentity();
        saveSlots.recordSessionSummary(
                worldSession,
                "new-game",
                runtimeContentRegistrations.registrations(""),
                currentSaveEnvironmentMetadata()
        );
        diskContinueAvailable = true;
    }

    boolean continueFromMemorySave() {
        if (memorySave == null) {
            return false;
        }
        worldSession = memorySaveSlotId.isBlank()
                ? worldSessions.restoreSavedSession(
                        memorySave,
                        combinedWorkbenchRecipes(List.of()),
                        workbenchRecipes.tags()
                )
                : worldSessions.restoreSavedSession(
                        memorySaveSlotId,
                        memorySaveDisplayName,
                        memorySave,
                        combinedWorkbenchRecipes(List.of()),
                        workbenchRecipes.tags()
        );
        attachGameplay();
        refreshActiveWorldSessionRuntimeContent();
        return true;
    }

    boolean continueFromSlot(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            if (continueFromMemorySave()) {
                return true;
            }
            slotId = saveSlots.defaultContinueSlotId(
                    runtimeContentRegistrations.registrations(""),
                    currentSaveEnvironmentMetadata()
            ).orElse("");
            if (slotId.isBlank()) {
                return false;
            }
        }
        EchoClientRuntimeContentCompatibility compatibility =
                saveSlots.runtimeContentCompatibility(slotId, runtimeContentRegistrations.registrations(""));
        if (!compatibility.compatible()) {
            return false;
        }
        EchoClientSaveEnvironmentCompatibility environmentCompatibility =
                saveSlots.saveEnvironmentCompatibility(slotId, currentSaveEnvironmentMetadata());
        if (!environmentCompatibility.compatible()) {
            return false;
        }
        restoreRuntimeContentRows(compatibility.savedRows());
        EchoClientWorldSession restored = saveSlots.restoreSlot(slotId, runtimeContentBridge);
        if (restored == null) {
            return continueFromMemorySave();
        }
        worldSession = worldSessions.restoreSavedSession(
                restored.slotId(),
                restored.displayName(),
                restored.gameSession().savedSessionSnapshot(),
                combinedWorkbenchRecipes(List.of()),
                workbenchRecipes.tags()
        );
        attachGameplay();
        refreshActiveWorldSessionRuntimeContent();
        memorySave = worldSession.gameSession().savedSessionSnapshot();
        rememberMemorySaveIdentity();
        diskContinueAvailable = true;
        return true;
    }

    String backupAndPlanMigration(String slotId) {
        String result = saveSlots.backupAndPlanMigration(slotId);
        if (!result.isBlank()) {
            diskContinueAvailable = true;
        }
        return result;
    }

    boolean renameSlot(String slotId, String displayName) {
        if (slotId == null || slotId.isBlank()) {
            return false;
        }
        boolean renamed = saveSlots.renameSlot(slotId, displayName);
        if (renamed) {
            String normalizedName = normalizeDisplayName(displayName);
            if (worldSession != null && worldSession.slotId().equals(slotId)) {
                worldSession = worldSession.withDisplayName(normalizedName);
                rememberMemorySaveIdentity();
            } else if (memorySave != null && memorySaveSlotId.equals(slotId)) {
                memorySaveDisplayName = normalizedName;
            }
            diskContinueAvailable = null;
        }
        return renamed;
    }

    boolean deleteSlot(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return false;
        }
        boolean deletingActiveWorld = worldSession != null && worldSession.slotId().equals(slotId);
        boolean deletingMemorySave = memorySave != null && memorySaveSlotId.equals(slotId);
        boolean deleted = saveSlots.deleteSlot(slotId);
        if (deleted) {
            if (deletingActiveWorld) {
                worldSession = null;
                clearMemorySave();
            } else if (deletingMemorySave) {
                clearMemorySave();
            }
            diskContinueAvailable = null;
        }
        return deleted;
    }

    void unloadWorld() {
        worldSession = null;
    }

    void updateWorldSessionFromGameplay() {
        if (worldSession != null) {
            worldSession.updateFromGameplay(gameplay);
        }
    }

    EchoClientWorldStreamResult streamAroundPlayer() {
        return worldSession == null ? EchoClientWorldStreamResult.NONE : worldSession.streamAroundPlayer();
    }

    EchoClientWorldStreamResult streamAroundPlayer(int chunkViewDistance) {
        return worldSession == null
                ? EchoClientWorldStreamResult.NONE
                : worldSession.streamAroundPlayer(chunkViewDistance);
    }

    void captureMemorySave() {
        captureMemorySave(EchoClientSaveSlotThumbnailCapture.EMPTY);
    }

    void captureMemorySave(EchoClientSaveSlotThumbnailCapture thumbnailCapture) {
        if (worldSession == null) {
            return;
        }
        memorySave = worldSession.gameSession().savedSessionSnapshot();
        rememberMemorySaveIdentity();
        saveSlots.recordSessionSummary(
                worldSession,
                "manual-save",
                runtimeContentRegistrations.registrations(""),
                currentSaveEnvironmentMetadata(),
                thumbnailCapture
        );
        diskContinueAvailable = true;
    }

    boolean restoreMemorySave() {
        return continueFromMemorySave();
    }

    private void restoreRuntimeContentRows(List<Map<String, Object>> restoredRows) {
        int changed = runtimeContentRegistrations.registerAll(restoredRows);
        if (changed == 0) {
            return;
        }
        runtimeContentBridge = runtimeContentBridge.withRuntimeEntriesReplacingContentIds(
                runtimeContentRegistrations.entries()
        );
        refreshWorldSessionFactory();
        rebuildScreenCatalog();
    }

    private boolean hasDiskContinueSlot() {
        if (diskContinueAvailable == null) {
            diskContinueAvailable = saveSlots.hasLoadableSlot(
                    runtimeContentRegistrations.registrations(""),
                    currentSaveEnvironmentMetadata()
            );
        }
        return diskContinueAvailable;
    }

    private Map<String, String> currentSaveEnvironmentMetadata() {
        return EchoClientSaveEnvironmentFingerprint.metadata(
                modScanSummary(),
                resourcePacks.resourcePacks()
        );
    }

    private void rememberMemorySaveIdentity() {
        if (worldSession == null) {
            memorySaveSlotId = "";
            memorySaveDisplayName = "";
            return;
        }
        memorySaveSlotId = worldSession.slotId();
        memorySaveDisplayName = worldSession.displayName();
    }

    private void clearMemorySave() {
        memorySave = null;
        memorySaveSlotId = "";
        memorySaveDisplayName = "";
    }

    private static String normalizeDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return "New World";
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.length() <= 48 ? normalized : normalized.substring(0, 48).stripTrailing();
    }

    private void attachGameplay() {
        EchoClientGameSession session = session();
        if (session == null) {
            return;
        }
        gameplay.init(session.world(), session.player(), session.hotbar());
    }

    private void rebuildScreenCatalog() {
        screenCatalog = EchoClientScreenCatalog.loadDefault(
                runtimeContentBridge,
                worldSessions.template().presentation()
        );
    }

    private void refreshWorldSessionFactory() {
        EchoClientWorldTemplate currentTemplate = worldSessions.template();
        EchoClientWorldTemplate template = currentTemplate.withSessionFactory(runtimeSessionFactory(currentTemplate));
        worldSessions = EchoClientWorldSessionFactory.forTemplate(template);
    }

    private EchoClientGameSessionFactory runtimeSessionFactory(EchoClientWorldTemplate template) {
        EchoClientGameSessionFactory current = template == null ? null : template.sessionFactory();
        if (current instanceof EchoClientOpenlandsSessionFactory) {
            return EchoClientOpenlandsSessionFactory.forRuntimeContent(
                    runtimeContentBridge,
                    runtimeContentRegistrations.itemDefinitions(),
                    runtimeEntityCatalog(),
                    runtimeHazardCatalog(),
                    runtimeInteractionCatalog(),
                    runtimeWorldgenCatalog()
            );
        }
        return EchoClientAshfallSessionFactory.forRuntimeContent(
                runtimeContentBridge,
                runtimeContentRegistrations.itemDefinitions(),
                runtimeEntityCatalog(),
                runtimeHazardCatalog(),
                runtimeInteractionCatalog(),
                runtimeWorldgenCatalog()
        );
    }

    private void refreshActiveWorldSessionRuntimeContent() {
        EchoClientGameSession session = session();
        if (session == null) {
            return;
        }
        updateWorldSessionFromGameplay();
        EchoClientWorkbenchLoadout workbench = EchoClientWorkbenchLoadoutFactory.fromStarterLoadout(
                worldSessions.template().starterLoadout(),
                combinedWorkbenchRecipes(List.of()),
                runtimeContentRegistrations.itemDefinitions(),
                workbenchRecipes.tags(),
                combinedLootDefinitions()
        );
        EchoClientGameSessionFactory sessionFactory = worldSessions.template().sessionFactory();
        session.updateRuntimeContent(
                runtimeContentBridge,
                workbench.registry(),
                workbench.recipes(),
                workbench.lootTables(),
                runtimeEntityCatalog(),
                runtimeHazardCatalog(),
                runtimeInteractionCatalog(),
                sessionFactory.streamer()
        );
        attachGameplay();
        memorySave = session.savedSessionSnapshot();
        rememberMemorySaveIdentity();
    }

    EchoClientEntityCatalog runtimeEntityCatalog() {
        return EchoClientRuntimeEntityCatalogBridge.merge(
                baseEntityCatalog,
                runtimeContentRegistrations.registrations("")
        );
    }

    private EchoClientHazardCatalog runtimeHazardCatalog() {
        ArrayList<Map<String, Object>> rows = new ArrayList<>(runtimeContentRegistrations.registrations(""));
        rows.addAll(activePackResourceRows(dataWorldCoreHazards.rows()));
        return EchoClientRuntimeHazardCatalogBridge.merge(
                baseHazardCatalog,
                rows
        );
    }

    private EchoClientWorldInteractionCatalog runtimeInteractionCatalog() {
        return EchoClientRuntimeInteractionCatalogBridge.merge(
                baseInteractionCatalog,
                runtimeContentRegistrations.registrations("")
        );
    }

    EchoClientRuntimeWorldgenCatalog runtimeWorldgenCatalog() {
        ArrayList<Map<String, Object>> rows = new ArrayList<>(runtimeContentRegistrations.registrations(""));
        rows.addAll(activePackResourceRows(dataWorldCoreRegions.rows()));
        rows.addAll(activePackResourceRows(dataWorldgenStructures.rows()));
        rows.addAll(activePackResourceRows(dataWorldgenBiomes.rows()));
        rows.addAll(activePackResourceRows(dataWorldgenFeatures.rows()));
        return EchoClientRuntimeWorldgenCatalog.fromRows(rows);
    }

    private List<Map<String, Object>> activePackResourceRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        String activePackId = worldSessions.template().saveProfile().packId();
        if (activePackId == null || activePackId.isBlank()) {
            return rows;
        }
        ArrayList<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String targetPackId = explicitTargetPackId(row);
            if (targetPackId.isBlank() || targetPackId.equals(activePackId)) {
                String rowProfilePackId = rowProfilePackId(row);
                if (rowProfilePackId.isBlank() || rowProfilePackId.equals(activePackId)) {
                    filtered.add(row);
                }
            }
        }
        return filtered;
    }

    private static String rowProfilePackId(Map<String, Object> row) {
        String moduleId = rowModuleId(row);
        return isStandaloneProfilePackId(moduleId) ? moduleId : "";
    }

    private static boolean isStandaloneProfilePackId(String value) {
        String normalized = text(value);
        return normalized.equals(EchoClientWorldTemplates.ashfallCrashSite().saveProfile().packId())
                || normalized.equals(EchoClientWorldTemplates.openlandsFirstHour().saveProfile().packId());
    }

    private static String rowModuleId(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return "";
        }
        String moduleId = text(row.get("moduleId"));
        if (!moduleId.isBlank()) {
            return moduleId;
        }
        Object metadataValue = row.get("metadata");
        if (metadataValue instanceof Map<?, ?> metadata) {
            String metadataModule = text(metadata.get("moduleId"));
            if (!metadataModule.isBlank()) {
                return metadataModule;
            }
        }
        String contentId = text(row.get("contentId"));
        int separator = contentId.indexOf(':');
        return separator > 0 ? contentId.substring(0, separator) : "";
    }

    private static String explicitTargetPackId(Map<String, Object> row) {
        if (row == null || row.isEmpty()) {
            return "";
        }
        String targetPackId = firstText(
                row.get("packId"),
                row.get("targetPackId"),
                row.get("savePackId"),
                row.get("profilePackId")
        );
        if (!targetPackId.isBlank()) {
            return targetPackId;
        }
        Object metadataValue = row.get("metadata");
        if (metadataValue instanceof Map<?, ?> metadata) {
            return firstText(
                    metadata.get("packId"),
                    metadata.get("targetPackId"),
                    metadata.get("savePackId"),
                    metadata.get("profilePackId")
            );
        }
        return "";
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private List<EchoRecipeDefinition> combinedWorkbenchRecipes(
            List<EchoRecipeDefinition> additionalRecipes
    ) {
        LinkedHashMap<String, EchoRecipeDefinition> recipes = new LinkedHashMap<>();
        addRecipeDefinitions(recipes, workbenchRecipes.recipes());
        addRecipeDefinitions(recipes, runtimeContentRegistrations.recipeDefinitions());
        addRecipeDefinitions(recipes, additionalRecipes);
        return List.copyOf(recipes.values());
    }

    private List<EchoLootDefinition> combinedLootDefinitions() {
        LinkedHashMap<String, EchoLootDefinition> loot = new LinkedHashMap<>();
        addLootDefinitions(loot, workbenchRecipes.loot());
        addLootDefinitions(loot, runtimeContentRegistrations.lootDefinitions());
        return List.copyOf(loot.values());
    }

    private static void addRecipeDefinitions(
            Map<String, EchoRecipeDefinition> target,
            List<EchoRecipeDefinition> recipes
    ) {
        if (recipes == null || recipes.isEmpty()) {
            return;
        }
        for (EchoRecipeDefinition recipe : recipes) {
            if (recipe != null) {
                target.put(recipe.id(), recipe);
            }
        }
    }

    private static void addLootDefinitions(
            Map<String, EchoLootDefinition> target,
            List<EchoLootDefinition> loot
    ) {
        if (loot == null || loot.isEmpty()) {
            return;
        }
        for (EchoLootDefinition definition : loot) {
            if (definition != null) {
                target.put(definition.id(), definition);
            }
        }
    }
}
