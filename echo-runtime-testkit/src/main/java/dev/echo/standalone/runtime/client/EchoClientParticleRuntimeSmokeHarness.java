package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelHit;

import java.util.List;

public final class EchoClientParticleRuntimeSmokeHarness {
    private EchoClientParticleRuntimeSmokeHarness() {
    }

    public static void main(String[] args) {
        requireBurstLifecycleAndBudget();
        requireRendererMesh();
        requireDynamicSectionUploadPlan();
        requireGameplayBreakFeedback();
        System.out.println("client particle runtime smoke PASS breakDust=visible placeDust=bounded");
    }

    private static void requireBurstLifecycleAndBudget() {
        EchoClientParticleRuntime runtime = new EchoClientParticleRuntime();
        EchoClientWorldFeedbackEvent breakEvent = breakEvent();
        runtime.emit(breakEvent);
        require(runtime.count() == EchoClientParticleRuntime.BLOCK_BREAK_PARTICLES,
                "Block break feedback should spawn a deterministic dust burst");
        List<EchoClientParticle> particleView = runtime.particles();
        EchoClientParticle firstParticle = particleView.getFirst();
        require(particleView == runtime.particles(),
                "Particle runtime should expose a stable non-empty render view without copy churn");
        try {
            particleView.clear();
            throw new AssertionError("Particle runtime render view should be read-only");
        } catch (UnsupportedOperationException expected) {
            // Expected read-only view.
        }

        EchoClientWorldFeedbackEvent placeEvent = placeEvent();
        runtime.emit(placeEvent);
        require(particleView == runtime.particles() && particleView.size() == runtime.count(),
                "Particle runtime render view should track emitted particles without rebuilding the view");
        require(runtime.count() == EchoClientParticleRuntime.BLOCK_BREAK_PARTICLES
                        + EchoClientParticleRuntime.BLOCK_PLACE_PARTICLES,
                "Block place feedback should spawn a smaller placement dust burst");
        double firstY = firstParticle.y();
        runtime.tick(0.05D);
        require(particleView.getFirst() == firstParticle && firstParticle.y() != firstY,
                "Particle runtime should move live particles in place instead of replacing records");

        for (int i = 0; i < 32; i++) {
            runtime.emit(breakEvent);
        }
        require(runtime.count() == EchoClientParticleRuntime.MAX_ACTIVE_PARTICLES,
                "Particle runtime should cap active feedback particles");

        runtime.tick(1.0D);
        require(runtime.count() < EchoClientParticleRuntime.MAX_ACTIVE_PARTICLES,
                "Expired particles should be removed after ticking past their lifetime");
        require(particleView.size() == runtime.count(),
                "Particle runtime render view should track in-place expiry compaction");
    }

    private static void requireRendererMesh() {
        EchoClientParticleRuntime runtime = new EchoClientParticleRuntime();
        runtime.emit(breakEvent());
        EchoClientEntityRenderer.MeshData mesh = EchoClientEntityRenderer.meshData(
                List.of(),
                EchoClientEntityCatalog.empty(),
                List.of(),
                runtime.particles()
        );
        require(mesh.vertexCount()
                        == EchoClientParticleRuntime.BLOCK_BREAK_PARTICLES
                        * EchoClientEntityRenderer.PARTICLE_VERTEX_COUNT,
                "Particle renderer mesh should emit one visible cuboid per particle");
        require(mesh.indexCount()
                        == EchoClientParticleRuntime.BLOCK_BREAK_PARTICLES
                        * EchoClientEntityRenderer.PARTICLE_INDEX_COUNT,
                "Particle renderer mesh should emit triangle indices for every particle cuboid");

        EchoClientEntityCatalog catalog = EchoClientEntityCatalog.empty();
        EchoClientDroppedItem stableDrop = stableDroppedItem();
        int firstDropSectionSignature = EchoClientEntityRenderer.droppedItemMeshSignature(List.of(stableDrop));
        int firstEntitySectionSignature = EchoClientEntityRenderer.entityMeshSignature(List.of(), catalog);
        int firstParticleSectionSignature = EchoClientEntityRenderer.particleMeshSignature(runtime.particles());
        int firstSignature = EchoClientEntityRenderer.meshSignature(
                List.of(),
                catalog,
                List.of(),
                runtime.particles()
        );
        runtime.tick(0.10D);
        int movedParticleSectionSignature = EchoClientEntityRenderer.particleMeshSignature(runtime.particles());
        int movedSignature = EchoClientEntityRenderer.meshSignature(
                List.of(),
                catalog,
                List.of(),
                runtime.particles()
        );
        require(firstSignature != movedSignature,
                "Live particle movement and fade should refresh the entity/particle mesh signature");
        require(firstParticleSectionSignature != movedParticleSectionSignature,
                "Live particle movement and fade should refresh only the particle section signature");
        require(firstDropSectionSignature == EchoClientEntityRenderer.droppedItemMeshSignature(List.of(stableDrop)),
                "Particle churn should not invalidate a stable dropped-item render section signature");
        require(firstEntitySectionSignature == EchoClientEntityRenderer.entityMeshSignature(List.of(), catalog),
                "Particle churn should not invalidate a stable entity render section signature");
    }

    private static void requireDynamicSectionUploadPlan() {
        EchoClientEntityRenderer.DynamicBufferUploadPlan initial =
                EchoClientEntityRenderer.dynamicBufferUploadPlan(0, 128);
        require(initial.grow() && initial.capacityBytes() >= 128,
                "Dynamic entity sections should grow GPU buffers for the first upload");

        EchoClientEntityRenderer.DynamicBufferUploadPlan reuse =
                EchoClientEntityRenderer.dynamicBufferUploadPlan(initial.capacityBytes(), 96);
        require(!reuse.grow() && reuse.capacityBytes() == initial.capacityBytes(),
                "Moving drops and particles should update existing GPU buffers when capacity is sufficient");

        EchoClientEntityRenderer.DynamicBufferUploadPlan grow =
                EchoClientEntityRenderer.dynamicBufferUploadPlan(initial.capacityBytes(), initial.capacityBytes() + 1);
        require(grow.grow() && grow.capacityBytes() > initial.capacityBytes(),
                "Dynamic entity sections should only reallocate when a later mesh exceeds retained capacity");
    }

    private static void requireGameplayBreakFeedback() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("particle-feedback").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        EchoVoxelPlayerState state = session.player().state();
        EchoVoxelBlock targetBlock = session.bridge().runtimeMarkerBlock();
        require(placeBreakTargetOnLookRay(session, state, targetBlock),
                "Particle smoke should place a break target on the player's look ray inside the loaded world");

        FakeBreakInput input = new FakeBreakInput();
        List<EchoClientWorldFeedbackEvent> events = List.of();
        for (int i = 0; i < 60 && events.isEmpty(); i++) {
            gameplay.tick(EchoVoxelPlayerInput.idle(), input, 0.08D, session);
            events = gameplay.consumeFeedbackEvents();
        }
        require(events.stream().anyMatch(event -> event.kind() == EchoClientWorldFeedbackKind.BLOCK_BREAK
                        && event.sourceId().equals(targetBlock.id())),
                "Actual gameplay block breaking should queue a block-break particle feedback event");
    }

    private static EchoClientWorldFeedbackEvent breakEvent() {
        EchoVoxelBlock block = new EchoVoxelBlock(
                "echoashfallprotocol:particle_smoke_scrap",
                "Particle Smoke Scrap",
                0xFF8C7C68,
                true,
                true,
                1.0D
        );
        return EchoClientWorldFeedbackEvent.blockBreak(new EchoVoxelHit(4, 5, 6, 0, 1, 0, block, 2.0D));
    }

    private static EchoClientDroppedItem stableDroppedItem() {
        return new EchoClientDroppedItem(
                "drop-stable-particle-smoke",
                new EchoItemDefinition(
                        new EchoItemId("echoashfallprotocol:particle_stable_scrap"),
                        "Particle Stable Scrap",
                        EchoItemCategory.MATERIAL,
                        64,
                        1.0D,
                        List.of("particle_stable_signature"),
                        List.of("Stable dropped item for particle renderer section checks")
                ),
                1,
                4.0D,
                5.0D,
                6.0D,
                0.0D
        );
    }

    private static EchoClientWorldFeedbackEvent placeEvent() {
        EchoVoxelBlock block = new EchoVoxelBlock(
                "echoashfallprotocol:particle_smoke_plate",
                "Particle Smoke Plate",
                0xFF7B8C98,
                true,
                true,
                1.0D
        );
        return EchoClientWorldFeedbackEvent.blockPlace(EchoVoxelBlockState.of(block), 8, 5, 8, 0, 1, 0);
    }

    private static boolean placeBreakTargetOnLookRay(
            EchoClientGameSession session,
            EchoVoxelPlayerState state,
            EchoVoxelBlock targetBlock
    ) {
        double yawRadians = Math.toRadians(state.yawDegrees());
        double pitchRadians = Math.toRadians(state.pitchDegrees());
        double horizontal = Math.cos(pitchRadians);
        double directionX = Math.sin(yawRadians) * horizontal;
        double directionY = Math.sin(pitchRadians);
        double directionZ = Math.cos(yawRadians) * horizontal;
        for (double distance = 1.0D; distance <= state.reach() - 0.25D; distance += 0.25D) {
            int x = (int) Math.floor(state.x() + directionX * distance);
            int y = (int) Math.floor(state.eyeY() + directionY * distance);
            int z = (int) Math.floor(state.z() + directionZ * distance);
            if (state.intersectsBlock(x, y, z)) {
                continue;
            }
            if (session.world().setBlockStateAt(x, y, z, EchoVoxelBlockState.of(targetBlock))) {
                return true;
            }
        }
        return false;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class FakeBreakInput implements EchoClientGameplayInput {
        private boolean firstBreak = true;

        @Override
        public int selectedHotbarSlot(int current) {
            return current;
        }

        @Override
        public boolean consumeBreak() {
            boolean value = firstBreak;
            firstBreak = false;
            return value;
        }

        @Override
        public boolean isCursorLocked() {
            return true;
        }

        @Override
        public boolean consumePlace() {
            return false;
        }
    }
}
