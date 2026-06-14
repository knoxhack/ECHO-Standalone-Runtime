package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerStep;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelBlockBreakResult;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Handles break/place, raycast, hotbar, and save/load interactions.
 */
final class EchoClientGameplay {
    private static final double WALK_FOOTSTEP_DISTANCE_BLOCKS = 0.85D;
    private static final double SPRINT_FOOTSTEP_DISTANCE_BLOCKS = 1.05D;
    private static final double CROUCH_FOOTSTEP_DISTANCE_BLOCKS = 0.70D;
    private static final double MIN_FOOTSTEP_INTERVAL_SECONDS = 0.18D;
    private static final EchoClientWorldInteractionCatalog DEFAULT_WORLD_INTERACTION_CATALOG =
            EchoClientContentProfiles.ashfallCrashSite().interactionCatalog();

    private EchoVoxelWorld world;
    private EchoVoxelPlayerController player;
    private EchoVoxelPlayerHotbar hotbar;
    private EchoClientAudio audio;
    private final EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();
    private long fluidGameTick;

    // Break state
    private EchoVoxelHit currentTarget;
    private double breakAccumulatedSeconds = 0.0;
    private double lastBreakToolSpeed = 1.0;
    private boolean wasBreaking = false;
    private boolean worldDirty = false;
    private final LinkedHashSet<EchoVoxelChunkId> dirtyChunkIds = new LinkedHashSet<>();
    private EchoClientScreenRouteRequest pendingScreenRoute = EchoClientScreenRouteRequest.NONE;
    private EchoClientSelectedItemUse selectedItemUse = EchoClientSelectedItemUse.none();
    private final ArrayList<EchoClientWorldFeedbackEvent> feedbackEvents = new ArrayList<>();
    private double footstepDistanceBlocks;
    private double footstepCooldownSeconds;

    GameplaySnapshot snapshot() {
        return new GameplaySnapshot(world, player.state(), hotbar);
    }

    void restore(GameplaySnapshot snap) {
        this.world = snap.world;
        this.player = new EchoVoxelPlayerController(snap.player);
        this.hotbar = snap.hotbar;
        currentTarget = null;
        breakAccumulatedSeconds = 0.0;
        wasBreaking = false;
        fluidGameTick = 0L;
        worldDirty = false;
        dirtyChunkIds.clear();
        resetFootstepAudio();
        pendingScreenRoute = EchoClientScreenRouteRequest.NONE;
        selectedItemUse = EchoClientSelectedItemUse.none();
        feedbackEvents.clear();
    }

    void init(EchoVoxelWorld world, EchoVoxelPlayerController player, EchoVoxelPlayerHotbar hotbar) {
        this.world = Objects.requireNonNull(world);
        this.player = Objects.requireNonNull(player);
        this.hotbar = Objects.requireNonNull(hotbar);
        worldDirty = false;
        dirtyChunkIds.clear();
        resetFootstepAudio();
        feedbackEvents.clear();
    }

    void setAudio(EchoClientAudio audio) {
        this.audio = audio;
    }

    EchoVoxelWorld world() {
        return world;
    }

    EchoVoxelPlayerController player() {
        return player;
    }

    EchoVoxelPlayerHotbar hotbar() {
        return hotbar;
    }

    EchoVoxelHit target() {
        return currentTarget;
    }

    double breakProgress() {
        if (currentTarget == null || breakAccumulatedSeconds <= 0.0) {
            return 0.0;
        }
        double speed = lastBreakToolSpeed;
        double required = Math.max(0.12, (0.22 + currentTarget.block().hardness() * 0.58) / speed);
        return Math.min(1.0, breakAccumulatedSeconds / required);
    }

    EchoClientScreenRouteRequest consumePendingScreenRoute() {
        EchoClientScreenRouteRequest route = pendingScreenRoute;
        pendingScreenRoute = EchoClientScreenRouteRequest.NONE;
        return route;
    }

    EchoClientSelectedItemUse consumeSelectedItemUse() {
        EchoClientSelectedItemUse use = selectedItemUse;
        selectedItemUse = EchoClientSelectedItemUse.none();
        return use;
    }

    List<EchoClientWorldFeedbackEvent> consumeFeedbackEvents() {
        if (feedbackEvents.isEmpty()) {
            return List.of();
        }
        List<EchoClientWorldFeedbackEvent> events = List.copyOf(feedbackEvents);
        feedbackEvents.clear();
        return events;
    }

    void tick(EchoVoxelPlayerInput input, EchoClientGameplayInput clientInput, double dt, EchoClientGameSession session) {
        worldDirty = false;
        dirtyChunkIds.clear();
        pendingScreenRoute = EchoClientScreenRouteRequest.NONE;
        selectedItemUse = EchoClientSelectedItemUse.none();

        // Update player physics
        EchoVoxelPlayerStep playerStep = player.tick(world, input, dt);
        updateFootstepAudio(playerStep, dt);
        tickScheduledFluids();

        // Raycast for target block
        EchoVoxelPlayerState state = player.state();
        Optional<EchoVoxelHit> hit = world.raycast(
                state.x(), state.eyeY(), state.z(),
                state.yawDegrees(), state.pitchDegrees(),
                state.reach()
        );
        currentTarget = hit.orElse(null);

        // Hotbar selection
        int slot = clientInput.selectedHotbarSlot(state.selectedSlot());
        if (slot != state.selectedSlot()) {
            hotbar.select(slot);
            player.selectSlot(slot);
        }

        // Break (hold left mouse)
        boolean canBreak = session == null || session.gameMode().allowsBlockBreaking();
        boolean breakPressed = clientInput.consumeBreak();
        boolean entityAttackHit = false;
        if (breakPressed && clientInput.isCursorLocked() && session != null) {
            EchoClientEntityAttackResult attack = session.attackLookedAtEntity(currentTarget);
            if (attack.hit()) {
                entityAttackHit = true;
                breakAccumulatedSeconds = 0.0;
                lastBreakToolSpeed = 1.0;
                wasBreaking = false;
                if (audio != null) audio.playBreak();
            }
        }
        if (entityAttackHit) {
            breakAccumulatedSeconds = 0.0;
            lastBreakToolSpeed = 1.0;
            wasBreaking = false;
        } else if (breakPressed || (wasBreaking && clientInput.isCursorLocked())) {
            if (canBreak && currentTarget != null && !currentTarget.block().air()) {
                breakAccumulatedSeconds += dt;
                double speed = session == null ? 1.0 : session.selectedMiningSpeed(currentTarget.block());
                lastBreakToolSpeed = speed;
                EchoVoxelBlockBreakResult result = world.attemptBreakBlock(
                        currentTarget.x(), currentTarget.y(), currentTarget.z(),
                        breakAccumulatedSeconds, speed
                );
                if (result.broken()) {
                    if (session != null) {
                        session.recordBlockBroken(currentTarget.block());
                    } else {
                        hotbar.add(currentTarget.block(), 1);
                    }
                    breakAccumulatedSeconds = 0.0;
                    markWorldDirtyAt(currentTarget.x(), currentTarget.y(), currentTarget.z());
                    emitFeedback(EchoClientWorldFeedbackEvent.blockBreak(currentTarget));
                    if (audio != null) audio.playBreak();
                } else if (breakPressed && audio != null) {
                    audio.playBlockHit();
                }
                wasBreaking = true;
            } else {
                breakAccumulatedSeconds = 0.0;
                lastBreakToolSpeed = 1.0;
                wasBreaking = false;
            }
        } else {
            breakAccumulatedSeconds = 0.0;
            lastBreakToolSpeed = 1.0;
            wasBreaking = false;
        }

        // Place (right mouse)
        if (clientInput.consumePlace()) {
            EchoVoxelBlock placed = hotbar.selected().block();
            boolean usedSelectedItem = false;
            if (placed.air() && session != null) {
                String selectedLabel = selectedItemLabel(session);
                if (session.spawnSelectedEntity(currentTarget)) {
                    selectedItemUse = EchoClientSelectedItemUse.spawned(selectedLabel);
                    usedSelectedItem = true;
                    if (audio != null) audio.playPlace();
                } else if (session.equipSelectedArmor()) {
                    selectedItemUse = EchoClientSelectedItemUse.equipped(selectedLabel);
                    usedSelectedItem = true;
                    if (audio != null) audio.playPlace();
                } else if (session.consumeSelectedConsumable()) {
                    selectedItemUse = EchoClientSelectedItemUse.consumed(selectedLabel);
                    usedSelectedItem = true;
                    if (audio != null) audio.playEat();
                } else if (session.activateSelectedCreativeItem()) {
                    selectedItemUse = EchoClientSelectedItemUse.interacted(selectedLabel);
                    usedSelectedItem = true;
                    if (audio != null) audio.playPlace();
                }
            }
            if (!usedSelectedItem && !input.crouch() && session != null) {
                EchoClientEntityInteractionResult interaction = session.interactLookedAtEntity(currentTarget);
                if (interaction.hit() && interaction.route().active()) {
                    pendingScreenRoute = interaction.route();
                    selectedItemUse = EchoClientSelectedItemUse.interacted(interaction.displayName());
                    usedSelectedItem = true;
                    if (audio != null) audio.playPlace();
                }
            }
            if (!usedSelectedItem && currentTarget != null) {
                EchoClientScreenRouteRequest interactionRoute = input.crouch()
                        ? EchoClientScreenRouteRequest.NONE
                        : worldInteractionRouteFor(currentTarget.block(), session);
                if (interactionRoute.active()) {
                    pendingScreenRoute = interactionRoute;
                    if (audio != null) audio.playPlace();
                } else {
                    EchoClientFluidBucketUse bucketUse = session == null
                            ? EchoClientFluidBucketUse.none("missing_session")
                            : session.useSelectedFluidBucket(currentTarget);
                    if (bucketUse.used()) {
                        selectedItemUse = EchoClientSelectedItemUse.bucketed(bucketUse.label());
                        usedSelectedItem = true;
                        markWorldDirtyAt(bucketUse.x(), bucketUse.y(), bucketUse.z());
                        if (bucketUse.action().equals("collect")) {
                            emitFeedback(EchoClientWorldFeedbackEvent.blockBreak(currentTarget));
                        } else {
                            emitFeedback(EchoClientWorldFeedbackEvent.blockPlace(
                                    bucketUse.state(),
                                    bucketUse.x(),
                                    bucketUse.y(),
                                    bucketUse.z(),
                                    currentTarget.normalX(),
                                    currentTarget.normalY(),
                                    currentTarget.normalZ()
                            ));
                        }
                        if (audio != null) audio.playPlace();
                    } else {
                        int px = currentTarget.x() + currentTarget.normalX();
                        int py = currentTarget.y() + currentTarget.normalY();
                        int pz = currentTarget.z() + currentTarget.normalZ();
                        boolean canPlace = session == null || session.gameMode().allowsBlockPlacing();
                        if (canPlace
                                && !placed.air()
                                && world.blockAt(px, py, pz).air()
                                && !state.intersectsBlock(px, py, pz)) {
                            EchoVoxelBlockState placedState = session == null
                                    ? EchoVoxelBlockState.of(placed)
                                    : session.defaultBlockStateFor(placed);
                            if (world.setBlockStateAt(px, py, pz, placedState)) {
                                if (session != null) {
                                    session.recordBlockPlaced(placedState);
                                }
                                if (session == null || session.gameMode().consumesPlacedItems()) {
                                    hotbar.consumeSelected();
                                }
                                markWorldDirtyAt(px, py, pz);
                                emitFeedback(EchoClientWorldFeedbackEvent.blockPlace(
                                        placedState,
                                        px,
                                        py,
                                        pz,
                                        currentTarget.normalX(),
                                        currentTarget.normalY(),
                                        currentTarget.normalZ()
                                ));
                                if (audio != null) audio.playPlace();
                            }
                        }
                    }
                }
            }
        }
    }

    void tickPassive(double dt) {
        worldDirty = false;
        dirtyChunkIds.clear();
        pendingScreenRoute = EchoClientScreenRouteRequest.NONE;
        EchoVoxelPlayerStep playerStep = player.tick(world, EchoVoxelPlayerInput.idle(), dt);
        updateFootstepAudio(playerStep, dt);
        tickScheduledFluids();
        EchoVoxelPlayerState state = player.state();
        currentTarget = world.raycast(
                state.x(), state.eyeY(), state.z(),
                state.yawDegrees(), state.pitchDegrees(),
                state.reach()
        ).orElse(null);
        breakAccumulatedSeconds = 0.0;
        lastBreakToolSpeed = 1.0;
        wasBreaking = false;
    }

    private void updateFootstepAudio(EchoVoxelPlayerStep step, double dt) {
        if (step == null) {
            return;
        }
        footstepCooldownSeconds = Math.max(0.0D, footstepCooldownSeconds - Math.max(0.0D, dt));
        EchoVoxelPlayerState previous = step.previous();
        EchoVoxelPlayerState current = step.current();
        if (audio == null || !current.grounded() || step.jumped()) {
            if (!current.grounded()) {
                footstepDistanceBlocks = 0.0D;
            }
            return;
        }
        double horizontalDistance = Math.hypot(current.x() - previous.x(), current.z() - previous.z());
        if (horizontalDistance <= 0.0001D) {
            return;
        }
        footstepDistanceBlocks += horizontalDistance;
        if (footstepDistanceBlocks < footstepDistanceThreshold(current) || footstepCooldownSeconds > 0.0D) {
            return;
        }
        audio.playStep();
        footstepDistanceBlocks = 0.0D;
        footstepCooldownSeconds = MIN_FOOTSTEP_INTERVAL_SECONDS;
    }

    private void resetFootstepAudio() {
        footstepDistanceBlocks = 0.0D;
        footstepCooldownSeconds = 0.0D;
    }

    private static double footstepDistanceThreshold(EchoVoxelPlayerState state) {
        if (state == null) {
            return WALK_FOOTSTEP_DISTANCE_BLOCKS;
        }
        if (state.crouching()) {
            return CROUCH_FOOTSTEP_DISTANCE_BLOCKS;
        }
        if (state.sprinting()) {
            return SPRINT_FOOTSTEP_DISTANCE_BLOCKS;
        }
        return WALK_FOOTSTEP_DISTANCE_BLOCKS;
    }

    static EchoClientScreenCommand worldInteractionCommandFor(EchoVoxelBlock block) {
        return worldInteractionRouteFor(block).command();
    }

    static EchoClientScreenRouteRequest worldInteractionRouteFor(EchoVoxelBlock block) {
        return DEFAULT_WORLD_INTERACTION_CATALOG.routeFor(block);
    }

    private static EchoClientScreenRouteRequest worldInteractionRouteFor(
            EchoVoxelBlock block,
            EchoClientGameSession session
    ) {
        return session == null ? worldInteractionRouteFor(block) : session.worldInteractionRouteFor(block);
    }

    private void emitFeedback(EchoClientWorldFeedbackEvent event) {
        if (event != null) {
            feedbackEvents.add(event);
        }
    }

    private void markWorldDirtyAt(int x, int y, int z) {
        worldDirty = true;
        if (world == null) {
            return;
        }
        int chunkSize = world.chunkSize();
        dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));

        int localX = Math.floorMod(x, chunkSize);
        int localY = Math.floorMod(y, chunkSize);
        int localZ = Math.floorMod(z, chunkSize);
        if (localX == 0) {
            dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x - 1, y, z, chunkSize));
        } else if (localX == chunkSize - 1) {
            dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x + 1, y, z, chunkSize));
        }
        if (localY == 0) {
            dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x, y - 1, z, chunkSize));
        } else if (localY == chunkSize - 1) {
            dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x, y + 1, z, chunkSize));
        }
        if (localZ == 0) {
            dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x, y, z - 1, chunkSize));
        } else if (localZ == chunkSize - 1) {
            dirtyChunkIds.add(EchoVoxelChunkId.fromBlock(x, y, z + 1, chunkSize));
        }
    }

    private void tickScheduledFluids() {
        if (world == null) {
            return;
        }
        EchoVoxelFluidRuntime.EchoVoxelFluidTickResult result = fluids.tickScheduled(world, ++fluidGameTick);
        if (result.totalWrites() <= 0) {
            return;
        }
        for (String changedCell : result.changedCells()) {
            markWorldDirtyCell(changedCell);
        }
    }

    private void markWorldDirtyCell(String changedCell) {
        if (changedCell == null || changedCell.isBlank()) {
            return;
        }
        String[] parts = changedCell.split(",", -1);
        if (parts.length != 3) {
            return;
        }
        try {
            markWorldDirtyAt(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        } catch (NumberFormatException ignored) {
        }
    }

    private static String selectedItemLabel(EchoClientGameSession session) {
        if (session == null) {
            return "Item";
        }
        int selectedSlot = session.hotbar().selectedSlot();
        EchoClientSlotStack selectedStack = session.inventoryScreenModel().slot(selectedSlot);
        return selectedStack.empty() ? "Item" : selectedStack.label();
    }

    boolean isWorldDirty() {
        return worldDirty;
    }

    Set<EchoVoxelChunkId> dirtyChunkIds() {
        return Set.copyOf(dirtyChunkIds);
    }

    int dirtyChunkCount() {
        return dirtyChunkIds.size();
    }

    record GameplaySnapshot(EchoVoxelWorld world, EchoVoxelPlayerState player, EchoVoxelPlayerHotbar hotbar) {}
}
