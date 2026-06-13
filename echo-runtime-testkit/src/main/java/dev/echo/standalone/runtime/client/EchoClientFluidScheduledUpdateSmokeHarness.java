package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidTickResult;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoClientFluidScheduledUpdateSmokeHarness {
    private static final Path REPORT_PATH =
            Path.of("reports", "echo", "standalone", "world-fluid-scheduled-updates.json");

    private EchoClientFluidScheduledUpdateSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        ScheduledFluidResult result = requireScheduledRuntimeAndClientTicks();
        writeReport(result);
        System.out.println("client fluid scheduled update smoke PASS interval="
                + result.interval()
                + " runtimeTick1Writes=" + result.runtimeTick1().totalWrites()
                + " runtimeTick2Writes=" + result.runtimeTick2().totalWrites()
                + " clientDirty=" + result.clientDirtyChunks());
    }

    private static ScheduledFluidResult requireScheduledRuntimeAndClientTicks() {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoVoxelBlock support = bridge.registry().requireLiveVoxelBlock(
                EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID
        );
        EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();

        EchoVoxelWorld runtimeWorld = scheduledWorld(support, "test:fluid_scheduled_runtime", 2, 1, 4);
        require(fluids.placeSource(runtimeWorld, EchoVoxelFluidType.WATER, 2, 1, 4).placed(),
                "Scheduled runtime source should place");
        runtimeWorld.setBlockStateAt(2, 1, 4,
                runtimeWorld.blockStateAt(2, 1, 4).withProperty("fluidTickInterval", "2"));
        int interval = EchoVoxelFluidRuntime.fluidTickInterval(runtimeWorld.blockStateAt(2, 1, 4));
        require(interval == 2,
                "Scheduled runtime source should expose data-driven fluidTickInterval=2");

        EchoVoxelFluidTickResult runtimeTick1 = fluids.tickScheduled(runtimeWorld, 1L);
        require(runtimeTick1.totalWrites() == 0,
                "Scheduled runtime tick 1 should not propagate an interval-2 source");
        require(!EchoVoxelFluidRuntime.isFluid(runtimeWorld.blockStateAt(3, 1, 4)),
                "Scheduled runtime tick 1 should leave adjacent air dry");

        EchoVoxelFluidTickResult runtimeTick2 = fluids.tickScheduled(runtimeWorld, 2L);
        require(runtimeTick2.horizontalWrites() >= 1,
                "Scheduled runtime tick 2 should propagate an interval-2 source");
        require(runtimeWorld.blockStateAt(3, 1, 4).block().id().equals(EchoVoxelFluidRuntime.WATER.id()),
                "Scheduled runtime tick 2 should create adjacent flowing water");

        EchoVoxelWorld clientWorld = scheduledWorld(support, "test:fluid_scheduled_client", 2, 1, 4);
        require(fluids.placeSource(clientWorld, EchoVoxelFluidType.WATER, 2, 1, 4).placed(),
                "Scheduled client source should place");
        clientWorld.setBlockStateAt(2, 1, 4,
                clientWorld.blockStateAt(2, 1, 4).withProperty("fluidTickInterval", "2"));

        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().restoreGameplaySnapshot(
                "client-fluid-scheduled-smoke",
                "Client Fluid Scheduled Smoke",
                new EchoClientGameplay.GameplaySnapshot(
                        clientWorld,
                        playerState(),
                        new EchoVoxelPlayerHotbar(List.of(), 0)
                ),
                List.of()
        );
        EchoClientGameSession session = worldSession.gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());

        gameplay.tick(EchoVoxelPlayerInput.idle(), new NoopGameplayInput(), 0.05D, session);
        worldSession.updateFromGameplay(gameplay);
        require(!EchoVoxelFluidRuntime.isFluid(session.world().blockStateAt(3, 1, 4)),
                "Client gameplay tick 1 should not propagate an interval-2 source");
        require(!gameplay.isWorldDirty(),
                "Client gameplay tick 1 should not mark chunks dirty when the schedule is not due");

        gameplay.tick(EchoVoxelPlayerInput.idle(), new NoopGameplayInput(), 0.05D, session);
        worldSession.updateFromGameplay(gameplay);
        EchoVoxelBlockState clientFlow = session.world().blockStateAt(3, 1, 4);
        require(clientFlow.block().id().equals(EchoVoxelFluidRuntime.WATER.id()),
                "Client gameplay tick 2 should propagate scheduled flowing water");
        require(clientFlow.property("fluidTick").orElse("").equals("2"),
                "Client scheduled flow should record the gameplay fluid tick");
        require(gameplay.isWorldDirty() && gameplay.dirtyChunkCount() > 0,
                "Client scheduled flow should mark dirty chunks for renderer refresh");

        return new ScheduledFluidResult(
                interval,
                runtimeTick1,
                runtimeTick2,
                clientFlow.block().id(),
                clientFlow.property("fluidTick").orElse(""),
                gameplay.dirtyChunkCount()
        );
    }

    private static EchoVoxelWorld scheduledWorld(EchoVoxelBlock support, String worldId, int sourceX, int sourceY, int sourceZ) {
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        for (int x = 0; x < 10; x++) {
            origin.setBlockLocal(x, 0, sourceZ, support);
        }
        origin.setBlockLocal(8, 0, 8, support);
        return new EchoVoxelWorld(
                worldId,
                20260610L,
                16,
                List.of(origin),
                8.5D,
                1.0D,
                8.5D,
                0.0D
        );
    }

    private static EchoVoxelPlayerState playerState() {
        return new EchoVoxelPlayerState(
                8.5D,
                1.0D,
                8.5D,
                0.0D,
                0.0D,
                0.0D,
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        );
    }

    private static void writeReport(ScheduledFluidResult result) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_fluid_scheduled_update_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientFluidScheduledUpdateSmokeHarness",
                  "status": "PASS",
                  "summary": "Scheduled fluid updates honor data-driven fluidTickInterval metadata and propagate through live client gameplay ticks with dirty chunk evidence.",
                  "runtime": {
                    "interval": %d,
                    "tick1Writes": %d,
                    "tick2Writes": %d,
                    "tick2HorizontalWrites": %d
                  },
                  "client": {
                    "flowBlock": "%s",
                    "flowTick": "%s",
                    "dirtyChunks": %d
                  },
                  "evidence": {
                    "nativeModLoaderCommandUsed": false,
                    "dataDrivenFluidTickInterval": true,
                    "runtimeScheduler": true,
                    "clientGameplayScheduler": true,
                    "rendererDirtyChunkSignal": true
                  }
                }
                """.formatted(
                result.interval(),
                result.runtimeTick1().totalWrites(),
                result.runtimeTick2().totalWrites(),
                result.runtimeTick2().horizontalWrites(),
                escape(result.clientFlowBlockId()),
                escape(result.clientFlowTick()),
                result.clientDirtyChunks()
        );
        Files.writeString(REPORT_PATH, json);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record ScheduledFluidResult(
            int interval,
            EchoVoxelFluidTickResult runtimeTick1,
            EchoVoxelFluidTickResult runtimeTick2,
            String clientFlowBlockId,
            String clientFlowTick,
            int clientDirtyChunks
    ) {
    }

    private static final class NoopGameplayInput implements EchoClientGameplayInput {
        @Override
        public int selectedHotbarSlot(int current) {
            return current;
        }

        @Override
        public boolean consumeBreak() {
            return false;
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
