package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityAiComponent;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityHealthComponent;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityMovementComponent;
import dev.echo.standalone.runtime.entity.EchoEntityPositionComponent;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlockInstance;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class EchoClientPackagedVisibleEvidenceRecorder {
    private static final String SCHEMA = "echo.standalone.packaged_visible_client_captures.v1";
    private static final int SETTLE_FRAMES = 3;
    private static final int WORLD_LOAD_TIMEOUT_FRAMES = 1_200;
    private static final List<String> REQUIRED_EVIDENCE_IDS = List.of(
            "main_menu_module_diagnostics",
            "creative_tab_list",
            "module_backed_inventory",
            "searchable_index",
            "terminal_screen",
            "lens_overlay",
            "hud_overlays",
            "screencore_page",
            "ashfall_biome_view",
            "module_block_placement",
            "module_item_usage",
            "module_entity_mob_view",
            "module_structure_feature_view",
            "mission_objective_progress_ui",
            "adaptercore_mutation_receipts",
            "save_load_visible_state",
            "performance_replay"
    );

    private final EchoClientLaunchContext launchContext;
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientModuleBootstrapResult moduleBootstrap;
    private final EchoClientScreenRuntimeController screenRuntime;
    private final EchoClientScreenController screens;
    private final EchoClientCommandController commands;
    private final EchoGlfwWindow window;
    private final ArrayDeque<CaptureTask> pendingTasks;
    private final ArrayList<Map<String, Object>> captures = new ArrayList<>();
    private final ArrayList<Map<String, Object>> blockers = new ArrayList<>();
    private final ArrayList<Map<String, Object>> adapterCoreReceipts = new ArrayList<>();

    private CaptureTask currentTask;
    private CaptureTask pendingWorldTask;
    private int settleFrames;
    private int worldWaitFrames;
    private boolean worldStartRequested;
    private boolean worldUnavailable;
    private boolean finished;
    private boolean manifestWritten;
    private EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget activeWorldgenEvidenceTarget;

    EchoClientPackagedVisibleEvidenceRecorder(
            EchoClientLaunchContext launchContext,
            EchoClientRuntimeServices runtimeServices,
            EchoClientModuleBootstrapResult moduleBootstrap,
            EchoClientScreenRuntimeController screenRuntime,
            EchoClientScreenController screens,
            EchoClientCommandController commands,
            EchoGlfwWindow window
    ) {
        this.launchContext = launchContext == null ? EchoClientLaunchContext.empty() : launchContext;
        this.runtimeServices = runtimeServices;
        this.moduleBootstrap = moduleBootstrap == null
                ? EchoClientModuleBootstrapResult.inactive()
                : moduleBootstrap;
        this.screenRuntime = screenRuntime;
        this.screens = screens;
        this.commands = commands;
        this.window = window;
        this.pendingTasks = new ArrayDeque<>(buildTasks());
    }

    void prepareBeforeRender(long frameSequence) {
        if (finished || currentTask != null) {
            if (settleFrames > 0) {
                settleFrames--;
            }
            return;
        }
        scheduleNextTask(frameSequence);
        if (settleFrames > 0) {
            settleFrames--;
        }
    }

    void captureAfterRender(long frameSequence) {
        if (finished || currentTask == null || settleFrames > 0) {
            return;
        }
        CaptureTask task = currentTask;
        currentTask = null;
        try {
            captures.add(capture(task, frameSequence));
        } catch (IOException | RuntimeException exception) {
            addBlocker(task.id(), "capture_failed", exception.getMessage());
        }
    }

    void writeIncompleteIfNeeded() {
        if (!finished) {
            finish("client_closed_before_capture_sequence_completed");
            return;
        }
        if (!manifestWritten) {
            writeManifest("client_closed_after_capture_sequence_completed");
        }
    }

    private List<CaptureTask> buildTasks() {
        return List.of(
                new CaptureTask(
                        "main_menu_module_diagnostics",
                        "Installed-module diagnostics",
                        false,
                        true,
                        "diagnostics",
                        this::openDiagnostics
                ),
                new CaptureTask(
                        "module_backed_inventory",
                        "Module-backed inventory",
                        true,
                        true,
                        "inventory",
                        this::openCreativeInventory
                ),
                new CaptureTask(
                        "creative_tab_list",
                        "Module-backed creative tab list",
                        true,
                        true,
                        "inventory",
                        this::openCreativeInventory
                ),
                new CaptureTask(
                        "searchable_index",
                        "AdapterCore Index screen",
                        true,
                        true,
                        "registered_screen",
                        () -> openAdapterCoreScreenContaining("index")
                ),
                new CaptureTask(
                        "terminal_screen",
                        "Module-backed Terminal screen",
                        true,
                        true,
                        "registered_screen",
                        () -> openAdapterCoreScreenContaining("terminal")
                ),
                new CaptureTask(
                        "lens_overlay",
                        "Module-backed Lens overlay route",
                        true,
                        true,
                        "registered_screen",
                        () -> openAdapterCoreScreenContaining("lens")
                ),
                new CaptureTask(
                        "hud_overlays",
                        "Module-backed HUD/overlay route",
                        true,
                        true,
                        "registered_screen",
                        this::openHudOverlayRoute
                ),
                new CaptureTask(
                        "screencore_page",
                        "Installed AdapterCore ScreenCore route",
                        true,
                        true,
                        "registered_screen",
                        this::openFirstAdapterCoreScreen
                ),
                new CaptureTask(
                        "ashfall_biome_view",
                        "Ashfall biome/worldgen view",
                        true,
                        true,
                        "in_game",
                        this::openWorldgenView
                ),
                new CaptureTask(
                        "module_block_placement",
                        "Module block placement",
                        true,
                        true,
                        "in_game",
                        this::playCreativeBlockPlacement
                ),
                new CaptureTask(
                        "module_item_usage",
                        "Module item usage",
                        true,
                        true,
                        "in_game",
                        this::playCreativeItemUsage
                ),
                new CaptureTask(
                        "module_entity_mob_view",
                        "Module entity/mob visual view",
                        true,
                        true,
                        "in_game",
                        this::openModuleEntityVisualCapture
                ),
                new CaptureTask(
                        "module_structure_feature_view",
                        "Module structure or feature view",
                        true,
                        false,
                        "in_game",
                        this::openModuleStructureFeatureView
                ),
                new CaptureTask(
                        "mission_objective_progress_ui",
                        "Mission/objective progress UI",
                        true,
                        true,
                        "registered_screen",
                        this::openMissionSurface
                ),
                new CaptureTask(
                        "adaptercore_mutation_receipts",
                        "AdapterCore mutation receipt evidence",
                        true,
                        true,
                        "receipts",
                        this::openReceiptEvidence
                ),
                new CaptureTask(
                        "save_load_visible_state",
                        "Save/load visible state",
                        true,
                        true,
                        "save_load",
                        this::captureSaveLoadVisibleState
                ),
                new CaptureTask(
                        "performance_replay",
                        "Render-thread loading performance replay",
                        true,
                        true,
                        "performance",
                        () -> unsupportedVisibleAction(
                                "performance_replay",
                                "No packaged-client performance replay proves render-thread jar/graph/texture work is absent yet."
                        )
                )
        );
    }

    private void scheduleNextTask(long frameSequence) {
        if (pendingWorldTask != null) {
            scheduleWorldTaskIfReady();
            return;
        }
        while (!pendingTasks.isEmpty()) {
            CaptureTask task = pendingTasks.removeFirst();
            if (task.requiresModuleContent() && !moduleContentReady()) {
                addBlocker(task.id(), "module_content_unavailable", moduleContentBlockerDetail());
                continue;
            }
            if (task.requiresWorld() && !runtimeServices.hasActiveWorld()) {
                if (worldUnavailable) {
                    addBlocker(task.id(), "world_unavailable", "World session did not become active for capture.");
                    continue;
                }
                pendingWorldTask = task;
                requestWorldStart(frameSequence);
                return;
            }
            prepareTask(task);
            return;
        }
        finish("capture_sequence_completed");
    }

    private void scheduleWorldTaskIfReady() {
        if (runtimeServices.hasActiveWorld()) {
            CaptureTask task = pendingWorldTask;
            pendingWorldTask = null;
            prepareTask(task);
            return;
        }
        if (!worldStartRequested) {
            requestWorldStart(0L);
            return;
        }
        worldWaitFrames++;
        if (worldWaitFrames > WORLD_LOAD_TIMEOUT_FRAMES) {
            worldUnavailable = true;
            addBlocker(
                    pendingWorldTask.id(),
                    "world_load_timeout",
                    "World session did not become active within " + WORLD_LOAD_TIMEOUT_FRAMES + " rendered frames."
            );
            pendingWorldTask = null;
        }
    }

    private void requestWorldStart(long frameSequence) {
        if (worldStartRequested) {
            return;
        }
        worldStartRequested = true;
        worldWaitFrames = 0;
        if (!commands.execute(EchoClientScreenCommand.START_NEW_GAME)) {
            worldUnavailable = true;
            addBlocker(
                    pendingWorldTask == null ? "world" : pendingWorldTask.id(),
                    "world_start_rejected",
                    "START_NEW_GAME command was rejected before visible evidence capture."
            );
        } else {
            System.out.println("[echo-client] visible evidence capture requested world start at frame " + frameSequence);
        }
    }

    private void prepareTask(CaptureTask task) {
        screenRuntime.refreshRuntimeSurfaces();
        if (!task.opener().get()) {
            addBlocker(task.id(), "screen_unavailable", "The client could not open " + task.description() + ".");
            return;
        }
        currentTask = task;
        settleFrames = SETTLE_FRAMES;
    }

    private boolean openDiagnostics() {
        return screens.executeNavigationCommand(
                EchoClientScreenCommand.OPEN_DIAGNOSTICS,
                runtimeServices.hasContinuableSession()
        );
    }

    private boolean openCreativeInventory() {
        EchoClientCreativeInventoryController.CreativeInventoryModel model =
                runtimeServices.creativeInventoryModel();
        if (model.tabs().isEmpty() || model.entries().isEmpty()) {
            return false;
        }
        return screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true);
    }

    private boolean openFirstAdapterCoreScreen() {
        List<EchoClientScreenCatalogEntry> adapterCoreScreens = runtimeServices.screenCatalog().adapterCoreScreens();
        if (adapterCoreScreens.isEmpty()) {
            return false;
        }
        return screens.openRegisteredScreen(adapterCoreScreens.get(0).screenId(), runtimeServices.hasContinuableSession());
    }

    private boolean openAdapterCoreScreenContaining(String token) {
        String needle = token == null ? "" : token.toLowerCase(java.util.Locale.ROOT);
        List<EchoClientScreenCatalogEntry> entries = runtimeServices.screenCatalog().adapterCoreScreens();
        for (int index = entries.size() - 1; index >= 0; index--) {
            EchoClientScreenCatalogEntry entry = entries.get(index);
            String haystack = String.join(" ",
                    entry.screenId(),
                    entry.title(),
                    entry.contentId(),
                    entry.adapterKey(),
                    entry.standaloneRuntimeId()
            ).toLowerCase(java.util.Locale.ROOT);
            if (haystack.contains(needle)) {
                return screens.openRegisteredScreen(entry.screenId(), runtimeServices.hasContinuableSession());
            }
        }
        return false;
    }

    private boolean openHudOverlayRoute() {
        return openAdapterCoreScreenContaining("hud") || openAdapterCoreScreenContaining("overlay");
    }

    private boolean openWorldgenView() {
        if (runtimeDomainCount("biomes") <= 0
                && runtimeDomainCount("worldgen") <= 0
                && runtimeDomainCount("structures") <= 0
                && runtimeDomainCount("features") <= 0
                && runtimeServices.loadedDataWorldgenBiomeRowCount() <= 0
                && runtimeServices.loadedDataWorldgenStructureRowCount() <= 0
                && runtimeServices.loadedDataWorldgenFeatureRowCount() <= 0) {
            return false;
        }
        screens.showInGame();
        return true;
    }

    private boolean openModuleStructureFeatureView() {
        EchoClientGameSession session = runtimeServices.session();
        if (session == null) {
            return false;
        }
        EchoClientRuntimeWorldgenCatalog catalog = runtimeServices.runtimeWorldgenCatalog();
        List<EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget> targets = catalog.evidenceTargets();
        if (targets.isEmpty()) {
            addBlocker(
                    "module_structure_feature_view",
                    "module_worldgen_target_missing",
                    "No installed Content Graph or data worldgen structure/feature row exposes standalone "
                            + "placement coordinates plus a live module block id."
            );
            return false;
        }
        for (EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget target : targets) {
            activeWorldgenEvidenceTarget = target;
            moveVisiblePlayerNearTarget(session, target);
            screens.showInGame();
            return true;
        }
        addBlocker(
                "module_structure_feature_view",
                "module_worldgen_not_visible",
                "Parsed " + targets.size()
                        + " module structure/feature worldgen target(s), but none produced a visible "
                        + "runtime_structure/runtime_feature block in the streamed world."
        );
        return false;
    }

    private WorldgenBlockEvidence findWorldgenEvidence(
            EchoClientGameSession activeSession,
            EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget target
    ) {
        if (activeSession == null || target == null) {
            return null;
        }
        int chunkSize = activeSession.world().chunkSize();
        for (EchoVoxelBlockInstance block : activeSession.world().nonAirBlocks()) {
            if (!insideTargetFootprint(block, target, chunkSize)) {
                continue;
            }
            EchoVoxelBlockState state = block.state();
            if (target.matches(state)) {
                return new WorldgenBlockEvidence(target, block, state);
            }
        }
        return null;
    }

    private void moveVisiblePlayerNearTarget(
            EchoClientGameSession session,
            EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget target
    ) {
        EchoVoxelPlayerState current = session.player().state();
        double evidenceX = target.cameraX() + 0.5D;
        double evidenceY = target.fixedY()
                ? target.y() + Math.max(1.0D, target.height() * 0.5D)
                : Math.max(2.0D, current.y() - 1.0D);
        double evidenceZ = target.cameraZ() + 0.5D;
        double cameraX = evidenceX - 3.5D;
        double cameraY = target.cameraY((int) Math.floor(current.y()), session.world().chunkSize());
        double cameraZ = evidenceZ - 3.5D;
        double dx = evidenceX - cameraX;
        double dy = evidenceY - (cameraY + current.eyeHeight());
        double dz = evidenceZ - cameraZ;
        double yaw = Math.toDegrees(Math.atan2(dx, dz));
        double horizontal = Math.max(0.0001D, Math.hypot(dx, dz));
        double pitch = Math.max(-75.0D, Math.min(55.0D, -Math.toDegrees(Math.atan2(dy, horizontal))));
        EchoVoxelPlayerController moved = new EchoVoxelPlayerController(new EchoVoxelPlayerState(
                cameraX,
                cameraY,
                cameraZ,
                current.velocityY(),
                yaw,
                pitch,
                current.grounded(),
                current.crouching(),
                current.sprinting(),
                current.selectedSlot(),
                current.reach()
        ));
        runtimeServices.gameplay().init(session.world(), moved, session.hotbar());
        runtimeServices.updateWorldSessionFromGameplay();
    }

    private static boolean insideTargetFootprint(
            EchoVoxelBlockInstance block,
            EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget target,
            int chunkSize
    ) {
        if (block.x() < target.x() || block.x() >= target.x() + target.width()) {
            return false;
        }
        if (block.z() < target.z() || block.z() >= target.z() + target.depth()) {
            return false;
        }
        if (!target.fixedY()) {
            return block.y() >= 0 && block.y() < chunkSize;
        }
        return block.y() >= target.y() && block.y() < target.y() + target.height();
    }

    private static String moduleId(String contentId) {
        String text = text(contentId);
        int separator = text.indexOf(':');
        return separator > 0 ? text.substring(0, separator) : "runtime";
    }

    private boolean openModuleEntityVisualCapture() {
        EchoClientGameSession session = runtimeServices.session();
        if (session == null) {
            return false;
        }
        EchoClientEntityCatalog catalog = session.entityCatalog();
        Optional<EchoClientEntityCatalog.EntityVisualProfile> profile =
                catalog.firstGraphBackedSpawnProfile();
        if (profile.isEmpty()) {
            addBlocker(
                    "module_entity_mob_view",
                    "module_entity_visual_profile_missing",
                    "No module entity row imported graph-backed model, texture, or animation metadata."
            );
            return false;
        }
        EchoClientEntityCatalog.EntityVisualProfile entityProfile = profile.get();
        if (!entityProfile.spawnRuleMetadataPresent()) {
            addBlocker(
                    "module_entity_mob_view",
                    "module_entity_spawn_rule_missing",
                    "Graph-backed entity " + entityProfile.definition().definitionId()
                            + " has no Content Graph spawn rule or biome-tag metadata."
            );
            return false;
        }
        if (!entityProfile.threatMetadataPresent()) {
            addBlocker(
                    "module_entity_mob_view",
                    "module_entity_threat_metadata_missing",
                    "Graph-backed entity " + entityProfile.definition().definitionId()
                            + " has no Content Graph threat profile or threat level metadata."
            );
            return false;
        }
        EchoClientEntityCatalog.RenderProfile renderProfile = entityProfile.renderProfile();
        if (!textureAvailable(renderProfile.textureId())) {
            addBlocker(
                    "module_entity_mob_view",
                    "module_entity_texture_asset_missing",
                    "Graph-backed entity " + entityProfile.definition().definitionId()
                            + " declares texture=" + renderProfile.textureId()
                            + ", but the installed module resource packs do not expose that texture."
            );
            return false;
        }
        spawnVisibleModuleEntity(session, entityProfile.definition());
        screens.showInGame();
        return true;
    }

    private void spawnVisibleModuleEntity(
            EchoClientGameSession session,
            EchoEntityDefinition definition
    ) {
        EchoVoxelPlayerState player = session.player().state();
        double yawRadians = Math.toRadians(player.yawDegrees());
        int x = (int) Math.floor(player.x() + Math.sin(yawRadians) * 4.0D);
        int y = Math.max(1, (int) Math.floor(player.y()));
        int z = (int) Math.floor(player.z() + Math.cos(yawRadians) * 4.0D);
        EchoEntityId id = new EchoEntityId("visible:module_entity_mob_view");
        session.entityStore().remove(id);
        session.entityStore().register(new EchoEntityState(
                id,
                definition,
                new EchoEntityPositionComponent(new EchoWorldPosition(x, y, z)),
                new EchoEntityHealthComponent(definition.maxHealth(), definition.maxHealth()),
                new EchoEntityMovementComponent(definition.movementSpeed(), true),
                new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.IDLE)
        ));
    }

    private boolean textureAvailable(String textureId) {
        String[] texture = splitTextureId(textureId);
        if (texture == null) {
            return false;
        }
        return runtimeServices.minecraftAssets().texture(texture[0], texture[1]).isPresent();
    }

    private boolean openMissionSurface() {
        return openAdapterCoreScreenContaining("mission") || openAdapterCoreScreenContaining("objective");
    }

    private boolean playCreativeBlockPlacement() {
        return playCreativeEntry(true, "module_block_placement", "visible_content_graph_block_place");
    }

    private boolean playCreativeItemUsage() {
        return playCreativeEntry(false, "module_item_usage", "visible_content_graph_item_use");
    }

    private boolean playCreativeEntry(boolean block, String captureId, String action) {
        EchoClientGameSession session = runtimeServices.session();
        if (session == null) {
            return false;
        }
        EchoClientCreativeInventoryController.CreativeEntry entry =
                firstCreativeEntry(block);
        if (entry == null) {
            addBlocker(
                    captureId,
                    "module_creative_entry_missing",
                    "No module-backed creative " + (block ? "block" : "item") + " entry is loaded."
            );
            return false;
        }
        session.setGameMode(EchoClientGameMode.CREATIVE);
        EchoClientCreativeInventoryController creative = new EchoClientCreativeInventoryController();
        EchoClientCreativeInventoryController.CreativeSelectionResult selection =
                creative.selectEntry(session, entry, 0);
        if (!selection.selected()) {
            addBlocker(captureId, "creative_selection_failed", selection.blocker());
            return false;
        }
        EchoClientCreativeInventoryController.CreativePlayResult play =
                creative.useSelectedEntry(session, runtimeServices.gameplay(), entry, 0);
        if (!play.played()) {
            addBlocker(captureId, "creative_action_failed", play.blocker());
            return false;
        }
        adapterCoreReceipts.add(receipt(
                captureId,
                action,
                block ? "blocks" : "items",
                entry.moduleId(),
                entry.itemId(),
                play.mutation(),
                Map.of(
                        "worldDirty", play.worldDirty(),
                        "dirtyChunkCount", play.dirtyChunkCount(),
                        "feedbackEvents", play.feedbackEvents(),
                        "source", "content_graph_creative_inventory"
                )
        ));
        screens.showInGame();
        return true;
    }

    private EchoClientCreativeInventoryController.CreativeEntry firstCreativeEntry(boolean block) {
        for (EchoClientCreativeInventoryController.CreativeEntry entry
                : runtimeServices.creativeInventoryModel().entries()) {
            if (entry.block() == block) {
                return entry;
            }
        }
        return null;
    }

    private boolean openReceiptEvidence() {
        if (adapterCoreReceipts.isEmpty()) {
            addBlocker(
                    "adaptercore_mutation_receipts",
                    "adaptercore_receipts_missing",
                    "No visible gameplay action produced a mutation receipt during capture."
            );
            return false;
        }
        screens.showInGame();
        return true;
    }

    private boolean captureSaveLoadVisibleState() {
        if (!runtimeServices.hasActiveWorld()) {
            return false;
        }
        runtimeServices.captureMemorySave();
        if (!runtimeServices.restoreMemorySave()) {
            addBlocker(
                    "save_load_visible_state",
                    "save_load_replay_failed",
                    "The visible capture script could not restore the in-memory module-backed save."
            );
            return false;
        }
        adapterCoreReceipts.add(receipt(
                "save_load_visible_state",
                "visible_save_load_replay",
                "saves",
                "runtime",
                "runtime:memory_save",
                "save_load",
                Map.of(
                        "source", "runtime_services_memory_save",
                        "hasContinuableSession", runtimeServices.hasContinuableSession()
                )
        ));
        screens.showInGame();
        return true;
    }

    private boolean unsupportedVisibleAction(String id, String detail) {
        addBlocker(id, "visible_gameplay_bridge_missing", detail);
        return false;
    }

    private int runtimeDomainCount(String domain) {
        if (domain == null || domain.isBlank()) {
            return 0;
        }
        return runtimeServices.runtimeContentSummary()
                .domainCounts()
                .getOrDefault(domain.trim().toLowerCase(java.util.Locale.ROOT), 0);
    }

    private Map<String, Object> entityVisualDiagnostics() {
        EchoClientEntityCatalog catalog = runtimeServices.runtimeEntityCatalog();
        Optional<EchoClientEntityCatalog.EntityVisualProfile> firstProfile =
                catalog.firstGraphBackedSpawnProfile();
        LinkedHashMap<String, Object> diagnostics = new LinkedHashMap<>();
        diagnostics.put("graphBackedVisualProfileCount", catalog.graphBackedVisualProfileCount());
        diagnostics.put("graphBackedSpawnRuleProfileCount", catalog.graphBackedSpawnRuleProfileCount());
        diagnostics.put("graphBackedThreatProfileCount", catalog.graphBackedThreatProfileCount());
        diagnostics.put("textureBackedEntityRenderer", true);
        diagnostics.put("fullModelBackedEntityRenderer", false);
        diagnostics.put("rendererMode", "content_graph_texture_crossed_planes");
        firstProfile.ifPresent(profile -> diagnostics.put("firstGraphBackedEntity", entityVisualProfile(profile)));
        return Map.copyOf(diagnostics);
    }

    private Map<String, Object> worldgenDiagnostics() {
        EchoClientRuntimeWorldgenCatalog catalog = runtimeServices.runtimeWorldgenCatalog();
        LinkedHashMap<String, Object> diagnostics = new LinkedHashMap<>();
        diagnostics.put("structurePlacementCount", catalog.structurePlacementCount());
        diagnostics.put("featurePlacementCount", catalog.featurePlacementCount());
        diagnostics.put("regionRuleCount", catalog.regionRuleCount());
        diagnostics.put("biomeRuleCount", catalog.biomeRuleCount());
        diagnostics.put("visibleEvidenceTargetCount", catalog.evidenceTargets().size());
        diagnostics.put("dataWorldgenStructureRows", runtimeServices.loadedDataWorldgenStructureRowCount());
        diagnostics.put("dataWorldgenFeatureRows", runtimeServices.loadedDataWorldgenFeatureRowCount());
        diagnostics.put("dataWorldgenBiomeRows", runtimeServices.loadedDataWorldgenBiomeRowCount());
        diagnostics.put("dataWorldgenStructureError", runtimeServices.dataWorldgenStructureError());
        diagnostics.put("dataWorldgenFeatureError", runtimeServices.dataWorldgenFeatureError());
        diagnostics.put("dataWorldgenBiomeError", runtimeServices.dataWorldgenBiomeError());
        if (!catalog.evidenceTargets().isEmpty()) {
            diagnostics.put("firstVisibleEvidenceTarget", catalog.evidenceTargets().get(0).metadata());
        }
        return Map.copyOf(diagnostics);
    }

    private static Map<String, Object> entityVisualProfile(
            EchoClientEntityCatalog.EntityVisualProfile profile
    ) {
        EchoClientEntityCatalog.RenderProfile renderProfile = profile.renderProfile();
        LinkedHashMap<String, Object> value = new LinkedHashMap<>();
        value.put("definitionId", profile.definition().definitionId());
        value.put("displayName", profile.definition().displayName());
        value.put("modelId", renderProfile.modelId());
        value.put("textureId", renderProfile.textureId());
        value.put("animationId", renderProfile.animationId());
        value.put("renderProfileId", renderProfile.renderProfileId());
        value.put("threatProfile", renderProfile.threatProfile());
        value.put("threatLevel", renderProfile.threatLevel());
        value.put("spawnBiomeTags", profile.spawnBiomeTags());
        value.put("graphBackedVisual", profile.graphBackedVisual());
        value.put("threatMetadataPresent", profile.threatMetadataPresent());
        value.put("spawnRuleMetadataPresent", profile.spawnRuleMetadataPresent());
        return Map.copyOf(value);
    }

    private Map<String, Object> capture(CaptureTask task, long frameSequence) throws IOException {
        validateVisibleWorldgenEvidence(task);
        Path outputRoot = requirePath(launchContext.evidenceOutputRoot(), "evidence output root");
        Files.createDirectories(outputRoot);
        Path imagePath = outputRoot.resolve(task.id() + ".png").toAbsolutePath().normalize();
        BufferedImage image =
                EchoClientScreenshotService.captureFramebufferImage(window.width(), window.height(), GL11.GL_BACK);
        if (!ImageIO.write(image, "png", imagePath.toFile())) {
            throw new IOException("No PNG writer available for visible evidence capture");
        }
        String capturedAt = Instant.now().toString();
        EchoClientScreenSnapshot snapshot = screens.snapshot(runtimeServices.hasContinuableSession());

        LinkedHashMap<String, Object> capture = new LinkedHashMap<>();
        capture.put("id", task.id());
        capture.put("status", "PASS");
        capture.put("label", task.description());
        capture.put("capturedAt", capturedAt);
        capture.put("frameSequence", frameSequence);
        capture.put("screenshotPath", displayPath(imagePath));
        capture.put("screenshotSha256", sha256(imagePath));
        capture.put("screenshotBytes", Files.size(imagePath));
        capture.put("packagedClient", launchContext.packagedClientEvidence());
        capture.put("strictPackMode", launchContext.strictPackMode());
        capture.put("safeMode", launchContext.safeMode());
        capture.put("devMode", false);
        capture.put("synthetic", false);
        capture.put("placeholder", false);
        capture.put("headlessOnly", false);
        capture.put("skipped", false);
        capture.put("playerVisible", true);
        capture.put("screenKind", snapshot.kind().name());
        capture.put("gameState", snapshot.state().name());
        capture.put("screenTitle", snapshot.title());
        capture.put("screenSubtitle", snapshot.subtitle());
        capture.put("captureRoute", task.screenKind());
        capture.put("moduleBootstrapActive", moduleBootstrap.active());
        capture.put("contentGraphLoaded", moduleBootstrap.contentGraphLoaded());
        capture.put("runtimeContentRows", runtimeServices.runtimeContentSummary().rowCount());
        capture.put("runtimeContentDomains", runtimeServices.runtimeContentSummary().domainCounts());
        capture.put("adapterCoreScreenCount", runtimeServices.screenCatalog().adapterCoreScreenCount());
        capture.put("contentGraphConsumption", moduleBootstrap.contentGraphConsumptionReport());
        capture.put("entityVisualDiagnostics", entityVisualDiagnostics());
        capture.put("worldgenDiagnostics", worldgenDiagnostics());
        capture.put("adapterCoreReceipts", receiptsForCapture(task.id()));
        EchoClientCreativeInventoryController.CreativeInventoryModel creativeInventory =
                runtimeServices.creativeInventoryModel();
        capture.put("creativeTabCount", creativeInventory.tabs().size());
        capture.put("creativeEntryCount", creativeInventory.entries().size());
        return capture;
    }

    private void validateVisibleWorldgenEvidence(CaptureTask task) {
        if (!"module_structure_feature_view".equals(task.id())) {
            return;
        }
        WorldgenBlockEvidence evidence = findWorldgenEvidence(
                runtimeServices.session(),
                activeWorldgenEvidenceTarget
        );
        if (evidence == null) {
            throw new IllegalStateException(
                    "Module structure/feature target was not visible in the active streamed world."
            );
        }
        adapterCoreReceipts.add(receipt(
                "module_structure_feature_view",
                "visible_content_graph_worldgen_stream",
                "worldgen",
                moduleId(evidence.target().contentId()),
                evidence.target().contentId(),
                "stream_worldgen_content",
                evidence.metadata()
        ));
    }

    private void finish(String reason) {
        finished = true;
        writeManifest(reason);
        window.requestClose();
    }

    private void writeManifest(String reason) {
        if (manifestWritten) {
            return;
        }
        manifestWritten = true;
        Path manifest = launchContext.evidenceManifest();
        if (manifest == null) {
            addBlocker("manifest", "manifest_path_missing", "Visible evidence capture has no manifest path.");
            return;
        }
        try {
            Path normalizedManifest = manifest.toAbsolutePath().normalize();
            Path parent = normalizedManifest.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(normalizedManifest, toPrettyJson(manifest(reason)) + System.lineSeparator());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write visible evidence capture manifest: " + manifest, exception);
        }
    }

    private Map<String, Object> manifest(String reason) {
        List<String> capturedIds = captures.stream()
                .map(capture -> String.valueOf(capture.get("id")))
                .toList();
        List<String> missingIds = REQUIRED_EVIDENCE_IDS.stream()
                .filter(id -> !capturedIds.contains(id))
                .toList();
        ArrayList<Map<String, Object>> allBlockers = new ArrayList<>(blockers);
        for (String missingId : missingIds) {
            allBlockers.add(blocker(missingId, "missing_required_capture", "Required visible evidence was not captured."));
        }

        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("moduleBootstrapActive", moduleBootstrap.active());
        metadata.put("moduleBootstrapFailure", moduleBootstrap.failure());
        metadata.put("contentGraphLoaded", moduleBootstrap.contentGraphLoaded());
        metadata.put("moduleRoots", moduleBootstrap.moduleRoots().stream().map(Path::toString).toList());
        metadata.put("runtimeContentRows", runtimeServices.runtimeContentSummary().rowCount());
        metadata.put("runtimeContentDomains", runtimeServices.runtimeContentSummary().domainCounts());
        metadata.put("adapterCoreScreenCount", runtimeServices.screenCatalog().adapterCoreScreenCount());
        metadata.put("contentGraphConsumption", moduleBootstrap.contentGraphConsumptionReport());
        metadata.put("entityVisualDiagnostics", entityVisualDiagnostics());
        metadata.put("worldgenDiagnostics", worldgenDiagnostics());
        metadata.put("modScanSummary", runtimeServices.modScanSummary().summaryLabel());
        metadata.put("modScanIssues", runtimeServices.modScanSummary().issueLabel());

        boolean pass = allBlockers.isEmpty()
                && launchContext.packagedClientEvidence()
                && launchContext.strictPackMode()
                && !launchContext.safeMode();

        LinkedHashMap<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("schema", SCHEMA);
        manifest.put("generatedAt", Instant.now().toString());
        manifest.put("status", pass ? "PASS" : "BLOCKED");
        manifest.put("reason", reason);
        manifest.put("packagedClient", launchContext.packagedClientEvidence());
        manifest.put("strictPackMode", launchContext.strictPackMode());
        manifest.put("safeMode", launchContext.safeMode());
        manifest.put("devMode", false);
        manifest.put("synthetic", false);
        manifest.put("headlessOnly", false);
        manifest.put("requiredEvidenceCount", REQUIRED_EVIDENCE_IDS.size());
        manifest.put("captureCount", captures.size());
        manifest.put("missingRequiredCaptureIds", missingIds);
        manifest.put("captures", captures);
        manifest.put("adapterCoreReceipts", List.copyOf(adapterCoreReceipts));
        manifest.put("blockers", allBlockers);
        manifest.put("metadata", metadata);
        return manifest;
    }

    private List<Map<String, Object>> receiptsForCapture(String captureId) {
        String requested = text(captureId);
        if (requested.isBlank() || adapterCoreReceipts.isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> matches = new ArrayList<>();
        for (Map<String, Object> receipt : adapterCoreReceipts) {
            String visibleCaptureId = text(String.valueOf(receipt.get("visibleCaptureId")));
            if (requested.equals(visibleCaptureId)) {
                matches.add(receipt);
                continue;
            }
            Object captureIds = receipt.get("captureIds");
            if (captureIds instanceof Collection<?> collection) {
                for (Object id : collection) {
                    if (requested.equals(text(String.valueOf(id)))) {
                        matches.add(receipt);
                        break;
                    }
                }
            }
        }
        return List.copyOf(matches);
    }

    private static Map<String, Object> receipt(
            String visibleCaptureId,
            String action,
            String domain,
            String moduleId,
            String contentId,
            String mutation,
            Map<String, Object> metadata
    ) {
        LinkedHashMap<String, Object> receipt = new LinkedHashMap<>();
        String cleanCaptureId = text(visibleCaptureId);
        receipt.put("id", "adaptercore:" + cleanCaptureId + ":" + Math.abs((contentId + action).hashCode()));
        receipt.put("status", "PASS");
        receipt.put("action", text(action));
        receipt.put("domain", text(domain));
        receipt.put("moduleId", text(moduleId));
        receipt.put("contentId", text(contentId));
        receipt.put("mutation", text(mutation));
        receipt.put("visibleCaptureId", cleanCaptureId);
        receipt.put("captureIds", List.of(cleanCaptureId, "adaptercore_mutation_receipts"));
        receipt.put("metadataOnly", false);
        receipt.put("synthetic", false);
        receipt.put("headlessOnly", false);
        receipt.put("playerVisible", true);
        receipt.put("metadata", metadata == null ? Map.of() : Map.copyOf(metadata));
        return receipt;
    }

    private boolean moduleContentReady() {
        return moduleBootstrap.active()
                && moduleBootstrap.contentGraphLoaded()
                && !runtimeServices.runtimeContentSummary().emptyContent();
    }

    private String moduleContentBlockerDetail() {
        if (!moduleBootstrap.active()) {
            String failure = moduleBootstrap.failure();
            return failure.isBlank()
                    ? "Strict installed-module bootstrap is inactive."
                    : "Strict installed-module bootstrap is inactive: " + failure;
        }
        if (!moduleBootstrap.contentGraphLoaded()) {
            return "No installed Content Graph artifacts were loaded.";
        }
        if (runtimeServices.runtimeContentSummary().emptyContent()) {
            return "No module-backed runtime content rows were imported into client services.";
        }
        return "Module-backed runtime content is unavailable.";
    }

    private void addBlocker(String id, String code, String detail) {
        blockers.add(blocker(id, code, detail));
    }

    private static Map<String, Object> blocker(String id, String code, String detail) {
        LinkedHashMap<String, Object> blocker = new LinkedHashMap<>();
        blocker.put("id", text(id));
        blocker.put("code", text(code));
        blocker.put("detail", text(detail));
        return blocker;
    }

    private static Path requirePath(Path path, String label) {
        if (path == null) {
            throw new IllegalStateException(label + " is not configured");
        }
        return path.toAbsolutePath().normalize();
    }

    private static String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(path));
            StringBuilder result = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                result.append(String.format("%02x", value & 0xFF));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }

    private static String displayPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        Path cwd = Path.of("").toAbsolutePath().normalize();
        try {
            return cwd.relativize(normalized).toString().replace('\\', '/');
        } catch (IllegalArgumentException ignored) {
            return normalized.toString().replace('\\', '/');
        }
    }

    private static String toPrettyJson(Object value) {
        StringBuilder builder = new StringBuilder();
        appendJson(builder, value, 0);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendJson(StringBuilder builder, Object value, int indent) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String string) {
            builder.append('"').append(escapeJson(string)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map<?, ?> map) {
            appendMap(builder, (Map<Object, Object>) map, indent);
        } else if (value instanceof Collection<?> collection) {
            appendCollection(builder, collection, indent);
        } else {
            builder.append('"').append(escapeJson(String.valueOf(value))).append('"');
        }
    }

    private static void appendMap(StringBuilder builder, Map<Object, Object> map, int indent) {
        if (map.isEmpty()) {
            builder.append("{}");
            return;
        }
        builder.append('{').append(System.lineSeparator());
        int index = 0;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            indent(builder, indent + 2);
            builder.append('"').append(escapeJson(String.valueOf(entry.getKey()))).append("\": ");
            appendJson(builder, entry.getValue(), indent + 2);
            if (++index < map.size()) {
                builder.append(',');
            }
            builder.append(System.lineSeparator());
        }
        indent(builder, indent);
        builder.append('}');
    }

    private static void appendCollection(StringBuilder builder, Collection<?> collection, int indent) {
        if (collection.isEmpty()) {
            builder.append("[]");
            return;
        }
        builder.append('[').append(System.lineSeparator());
        int index = 0;
        for (Object entry : collection) {
            indent(builder, indent + 2);
            appendJson(builder, entry, indent + 2);
            if (++index < collection.size()) {
                builder.append(',');
            }
            builder.append(System.lineSeparator());
        }
        indent(builder, indent);
        builder.append(']');
    }

    private static void indent(StringBuilder builder, int indent) {
        builder.append(" ".repeat(Math.max(0, indent)));
    }

    private static String escapeJson(String value) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\b' -> result.append("\\b");
                case '\f' -> result.append("\\f");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (ch < 0x20) {
                        result.append(String.format("\\u%04x", (int) ch));
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    private static String[] splitTextureId(String textureId) {
        String normalized = text(textureId).replace('\\', '/');
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator == normalized.length() - 1) {
            return null;
        }
        String path = normalized.substring(separator + 1);
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        if (path.isBlank()) {
            return null;
        }
        return new String[]{normalized.substring(0, separator), path};
    }

    private record WorldgenBlockEvidence(
            EchoClientRuntimeWorldgenCatalog.WorldgenEvidenceTarget target,
            EchoVoxelBlockInstance block,
            EchoVoxelBlockState state
    ) {
        private WorldgenBlockEvidence {
            if (target == null || block == null || state == null) {
                throw new IllegalArgumentException("worldgen evidence values must not be null");
            }
        }

        Map<String, Object> metadata() {
            LinkedHashMap<String, Object> value = new LinkedHashMap<>();
            value.put("source", "content_graph_runtime_worldgen");
            value.put("target", target.metadata());
            value.put("blockId", block.block().id());
            value.put("x", block.x());
            value.put("y", block.y());
            value.put("z", block.z());
            value.put("stateProperties", state.properties());
            return Map.copyOf(value);
        }
    }

    private record CaptureTask(
            String id,
            String description,
            boolean requiresWorld,
            boolean requiresModuleContent,
            String screenKind,
            Supplier<Boolean> opener
    ) {
        private CaptureTask {
            id = text(id);
            description = text(description);
            screenKind = text(screenKind);
        }
    }
}
