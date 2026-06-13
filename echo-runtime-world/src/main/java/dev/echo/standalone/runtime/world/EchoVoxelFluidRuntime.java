package dev.echo.standalone.runtime.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public final class EchoVoxelFluidRuntime {
    public static final EchoVoxelBlock WATER = new EchoVoxelBlock(
            "echo:water",
            "Water",
            0xAA2F7DDE,
            0xCC5FA8FF,
            "echo/block/water",
            EchoVoxelMaterialPattern.FLAT,
            false,
            false,
            100.0D
    );
    public static final EchoVoxelBlock LAVA = new EchoVoxelBlock(
            "echo:lava",
            "Lava",
            0xDDEE5C18,
            0xFFFFB347,
            "echo/block/lava",
            EchoVoxelMaterialPattern.TOXIC_PUDDLE,
            false,
            false,
            100.0D
    );
    public static final EchoVoxelBlock HARDENED_FLUID_STONE = new EchoVoxelBlock(
            "echo:hardened_fluid_stone",
            "Hardened Fluid Stone",
            0xFF3F4650,
            0xFF68717B,
            "echo/block/hardened_fluid_stone",
            EchoVoxelMaterialPattern.BASALT_CRACKS,
            true,
            true,
            2.8D
    );

    public EchoVoxelFluidPlacement placeSource(EchoVoxelWorld world, EchoVoxelFluidType fluid, int x, int y, int z) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(fluid, "fluid");
        EchoVoxelBlockState target = world.blockStateAt(x, y, z);
        Optional<EchoVoxelFluidType> targetFluid = fluidType(target);
        if (targetFluid.isPresent() && targetFluid.orElseThrow() != fluid) {
            boolean hardened = world.setBlockStateAt(x, y, z, hardenedState(0L, fluid, targetFluid.orElseThrow()));
            return new EchoVoxelFluidPlacement(x, y, z, fluid, hardened,
                    hardened ? "source_hardened_existing_fluid" : "outside_loaded_chunk");
        }
        if (canWaterlog(target)) {
            boolean placed = world.setBlockStateAt(x, y, z, waterloggedState(target, fluid, 0, 0L, false, true));
            return new EchoVoxelFluidPlacement(x, y, z, fluid, placed,
                    placed ? "source_waterlogged" : "outside_loaded_chunk");
        }
        if (!target.air() && targetFluid.isEmpty()) {
            return new EchoVoxelFluidPlacement(x, y, z, fluid, false, "blocked_by_solid");
        }
        boolean placed = world.setBlockStateAt(x, y, z, sourceState(fluid, 0L));
        return new EchoVoxelFluidPlacement(x, y, z, fluid, placed, placed ? "source_placed" : "outside_loaded_chunk");
    }

    public EchoVoxelFluidTickResult tick(EchoVoxelWorld world, long gameTick) {
        return tick(world, gameTick, false);
    }

    public EchoVoxelFluidTickResult tickScheduled(EchoVoxelWorld world, long gameTick) {
        return tick(world, gameTick, true);
    }

    private EchoVoxelFluidTickResult tick(EchoVoxelWorld world, long gameTick, boolean scheduledOnly) {
        Objects.requireNonNull(world, "world");
        if (gameTick < 0L) {
            throw new IllegalArgumentException("gameTick must not be negative");
        }
        List<EchoVoxelBlockInstance> fluids = world.nonAirBlocks().stream()
                .filter(instance -> isFluid(instance.state()))
                .sorted(Comparator.comparingInt(EchoVoxelBlockInstance::y)
                        .thenComparingInt(EchoVoxelBlockInstance::x)
                        .thenComparingInt(EchoVoxelBlockInstance::z))
                .toList();
        int sourceCells = 0;
        int downwardWrites = 0;
        int horizontalWrites = 0;
        int hardenedCells = 0;
        int blockedBySolid = 0;
        int outsideLoadedChunk = 0;
        int crossChunkWrites = 0;
        ArrayList<String> changedCells = new ArrayList<>();

        for (EchoVoxelBlockInstance instance : fluids) {
            EchoVoxelFluidType fluid = fluidType(instance.state()).orElse(null);
            if (fluid == null) {
                continue;
            }
            if (scheduledOnly && !scheduledTickDue(instance.state(), fluid, gameTick)) {
                continue;
            }
            int level = fluidLevel(instance.state());
            if (level == 0) {
                sourceCells++;
            }

            FlowWrite down = tryWriteFluid(
                    world,
                    fluid,
                    level == 0 ? 1 : level,
                    instance.x(),
                    instance.y() - 1,
                    instance.z(),
                    gameTick,
                    true,
                    instance
            );
            if (down.wrote()) {
                downwardWrites++;
                crossChunkWrites += down.crossChunk() ? 1 : 0;
                changedCells.add(down.changedCell());
                continue;
            }
            hardenedCells += down.hardened() ? 1 : 0;
            blockedBySolid += down.blockedBySolid() ? 1 : 0;
            outsideLoadedChunk += down.outsideLoadedChunk() ? 1 : 0;
            if (down.hardened()) {
                changedCells.add(down.changedCell());
                continue;
            }

            if (level >= fluid.maxLevel()) {
                continue;
            }
            int nextLevel = level + 1;
            for (int[] direction : HORIZONTAL_DIRECTIONS) {
                FlowWrite horizontal = tryWriteFluid(
                        world,
                        fluid,
                        nextLevel,
                        instance.x() + direction[0],
                        instance.y(),
                        instance.z() + direction[1],
                        gameTick,
                        false,
                        instance
                );
                if (horizontal.wrote()) {
                    horizontalWrites++;
                    crossChunkWrites += horizontal.crossChunk() ? 1 : 0;
                    changedCells.add(horizontal.changedCell());
                } else if (horizontal.hardened()) {
                    hardenedCells++;
                    changedCells.add(horizontal.changedCell());
                } else if (horizontal.blockedBySolid()) {
                    blockedBySolid++;
                } else if (horizontal.outsideLoadedChunk()) {
                    outsideLoadedChunk++;
                }
            }
        }

        return new EchoVoxelFluidTickResult(
                gameTick,
                fluids.size(),
                sourceCells,
                downwardWrites,
                horizontalWrites,
                hardenedCells,
                blockedBySolid,
                outsideLoadedChunk,
                crossChunkWrites,
                List.copyOf(changedCells)
        );
    }

    public static boolean isFluid(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        return fluidType(state).isPresent();
    }

    public static boolean isWaterloggable(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        return booleanProperty(state, "waterloggable") || booleanProperty(state, "waterlogged");
    }

    public static boolean isWaterlogged(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        return booleanProperty(state, "waterlogged") && fluidType(state).isPresent();
    }

    public static EchoVoxelBlockState drainedState(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        if (isCanonicalFluidBlock(state)) {
            return EchoVoxelBlockState.AIR;
        }
        TreeMap<String, String> properties = new TreeMap<>(state.properties());
        properties.remove("fluid");
        properties.remove("fluidFalling");
        properties.remove("fluidLevel");
        properties.remove("fluidSource");
        properties.remove("fluidTick");
        properties.remove("waterlogged");
        return new EchoVoxelBlockState(state.block(), properties, state.tickVersion());
    }

    public static Optional<EchoVoxelFluidType> fluidType(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        String id = state.property("fluid").orElse(state.block().id());
        return EchoVoxelFluidType.fromId(id);
    }

    public static int fluidLevel(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        String value = state.property("fluidLevel").orElse("0");
        try {
            return Math.max(0, Integer.parseInt(value));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public static int fluidTickInterval(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        EchoVoxelFluidType fluid = fluidType(state).orElse(null);
        if (fluid == null) {
            return 0;
        }
        return parsedTickInterval(state, fluid);
    }

    public static boolean isCanonicalFluidBlock(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        return EchoVoxelFluidType.fromId(state.block().id()).isPresent();
    }

    public static double fluidSurfaceHeight(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        EchoVoxelFluidType fluid = fluidType(state).orElse(null);
        if (fluid == null || !isCanonicalFluidBlock(state)) {
            return 1.0D;
        }
        if (booleanProperty(state, "fluidFalling")) {
            return 1.0D;
        }
        int level = Math.max(0, Math.min(fluid.maxLevel(), fluidLevel(state)));
        return Math.max(
                1.0D / (fluid.maxLevel() + 2.0D),
                (fluid.maxLevel() + 1.0D - level) / (fluid.maxLevel() + 2.0D)
        );
    }

    private FlowWrite tryWriteFluid(
            EchoVoxelWorld world,
            EchoVoxelFluidType fluid,
            int level,
            int x,
            int y,
            int z,
            long gameTick,
            boolean falling,
            EchoVoxelBlockInstance source
    ) {
        if (y < 0) {
            return FlowWrite.outside();
        }
        EchoVoxelBlockState target = world.blockStateAt(x, y, z);
        Optional<EchoVoxelFluidType> targetFluid = fluidType(target);
        if (targetFluid.isPresent() && targetFluid.orElseThrow() != fluid) {
            boolean hardened = world.setBlockStateAt(x, y, z, hardenedState(gameTick, fluid, targetFluid.orElseThrow()));
            return hardened
                    ? FlowWrite.hardened(cellId(x, y, z), crossesChunk(source, x, y, z, world.chunkSize()))
                    : FlowWrite.outside();
        }
        if (targetFluid.isPresent() && fluidLevel(target) <= level) {
            return FlowWrite.unchanged();
        }
        if (canWaterlog(target)) {
            boolean wrote = world.setBlockStateAt(
                    x,
                    y,
                    z,
                    waterloggedState(target, fluid, level, gameTick, falling, false)
            );
            return wrote
                    ? FlowWrite.wrote(cellId(x, y, z), crossesChunk(source, x, y, z, world.chunkSize()))
                    : FlowWrite.outside();
        }
        if (!target.air() && targetFluid.isEmpty()) {
            return FlowWrite.blocked();
        }
        boolean wrote = world.setBlockStateAt(x, y, z, flowState(fluid, level, gameTick, falling));
        return wrote
                ? FlowWrite.wrote(cellId(x, y, z), crossesChunk(source, x, y, z, world.chunkSize()))
                : FlowWrite.outside();
    }

    private static EchoVoxelBlockState sourceState(EchoVoxelFluidType fluid, long gameTick) {
        return flowState(fluid, 0, gameTick, false).withProperty("fluidSource", "true");
    }

    private static EchoVoxelBlockState flowState(EchoVoxelFluidType fluid, int level, long gameTick, boolean falling) {
        return EchoVoxelBlockState.of(fluid.block())
                .withProperty("fluid", fluid.id())
                .withProperty("fluidLevel", Integer.toString(Math.max(0, Math.min(fluid.maxLevel(), level))))
                .withProperty("fluidFalling", Boolean.toString(falling))
                .withProperty("fluidTick", Long.toString(gameTick));
    }

    private static EchoVoxelBlockState waterloggedState(
            EchoVoxelBlockState host,
            EchoVoxelFluidType fluid,
            int level,
            long gameTick,
            boolean falling,
            boolean source
    ) {
        EchoVoxelBlockState state = host
                .withProperty("waterloggable", "true")
                .withProperty("waterlogged", "true")
                .withProperty("fluid", fluid.id())
                .withProperty("fluidLevel", Integer.toString(Math.max(0, Math.min(fluid.maxLevel(), level))))
                .withProperty("fluidFalling", Boolean.toString(falling))
                .withProperty("fluidTick", Long.toString(gameTick));
        return source ? state.withProperty("fluidSource", "true") : state;
    }

    private static EchoVoxelBlockState hardenedState(
            long gameTick,
            EchoVoxelFluidType incoming,
            EchoVoxelFluidType existing
    ) {
        return EchoVoxelBlockState.of(HARDENED_FLUID_STONE)
                .withProperty("interaction", "fluid_hardening")
                .withProperty("incomingFluid", incoming.id())
                .withProperty("existingFluid", existing.id())
                .withProperty("fluidTick", Long.toString(gameTick));
    }

    private static boolean crossesChunk(EchoVoxelBlockInstance source, int x, int y, int z, int chunkSize) {
        return EchoVoxelChunkId.fromBlock(source.x(), source.y(), source.z(), chunkSize)
                .equals(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize)) == false;
    }

    private static boolean canWaterlog(EchoVoxelBlockState state) {
        return !isCanonicalFluidBlock(state) && isWaterloggable(state);
    }

    private static boolean scheduledTickDue(EchoVoxelBlockState state, EchoVoxelFluidType fluid, long gameTick) {
        return gameTick > 0L && gameTick % parsedTickInterval(state, fluid) == 0L;
    }

    private static int parsedTickInterval(EchoVoxelBlockState state, EchoVoxelFluidType fluid) {
        String value = state.property("fluidTickInterval").orElse(Integer.toString(fluid.defaultTickInterval()));
        try {
            return Math.max(1, Math.min(1200, Integer.parseInt(value)));
        } catch (NumberFormatException ignored) {
            return fluid.defaultTickInterval();
        }
    }

    private static boolean booleanProperty(EchoVoxelBlockState state, String key) {
        return state.property(key)
                .map(value -> value.equalsIgnoreCase("true"))
                .orElse(false);
    }

    private static String cellId(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private static final int[][] HORIZONTAL_DIRECTIONS = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
    };

    public enum EchoVoxelFluidType {
        WATER("water", 7, 5),
        LAVA("lava", 4, 30);

        private final String id;
        private final int maxLevel;
        private final int defaultTickInterval;

        EchoVoxelFluidType(String id, int maxLevel, int defaultTickInterval) {
            this.id = id;
            this.maxLevel = maxLevel;
            this.defaultTickInterval = defaultTickInterval;
        }

        public String id() {
            return id;
        }

        public EchoVoxelBlock block() {
            return switch (this) {
                case WATER -> EchoVoxelFluidRuntime.WATER;
                case LAVA -> EchoVoxelFluidRuntime.LAVA;
            };
        }

        public int maxLevel() {
            return maxLevel;
        }

        public int defaultTickInterval() {
            return defaultTickInterval;
        }

        static Optional<EchoVoxelFluidType> fromId(String id) {
            if (id == null || id.isBlank()) {
                return Optional.empty();
            }
            String normalized = id.toLowerCase(Locale.ROOT);
            for (EchoVoxelFluidType value : values()) {
                if (normalized.equals(value.id) || normalized.equals(value.block().id())) {
                    return Optional.of(value);
                }
            }
            return Optional.empty();
        }
    }

    public record EchoVoxelFluidPlacement(
            int x,
            int y,
            int z,
            EchoVoxelFluidType fluid,
            boolean placed,
            String reason
    ) {
        public EchoVoxelFluidPlacement {
            Objects.requireNonNull(fluid, "fluid");
            if (reason == null || reason.isBlank()) {
                throw new IllegalArgumentException("reason must not be blank");
            }
        }
    }

    public record EchoVoxelFluidTickResult(
            long gameTick,
            int fluidCellsBeforeTick,
            int sourceCells,
            int downwardWrites,
            int horizontalWrites,
            int hardenedCells,
            int blockedBySolid,
            int outsideLoadedChunk,
            int crossChunkWrites,
            List<String> changedCells
    ) {
        public EchoVoxelFluidTickResult {
            changedCells = List.copyOf(Objects.requireNonNull(changedCells, "changedCells"));
        }

        public int totalWrites() {
            return downwardWrites + horizontalWrites + hardenedCells;
        }

        public String summary() {
            return "tick=" + gameTick
                    + " fluids=" + fluidCellsBeforeTick
                    + " sources=" + sourceCells
                    + " down=" + downwardWrites
                    + " horizontal=" + horizontalWrites
                    + " hardened=" + hardenedCells
                    + " crossChunk=" + crossChunkWrites;
        }
    }

    private record FlowWrite(
            boolean wrote,
            boolean hardened,
            boolean blockedBySolid,
            boolean outsideLoadedChunk,
            boolean crossChunk,
            String changedCell
    ) {
        static FlowWrite wrote(String changedCell, boolean crossChunk) {
            return new FlowWrite(true, false, false, false, crossChunk, changedCell);
        }

        static FlowWrite hardened(String changedCell, boolean crossChunk) {
            return new FlowWrite(false, true, false, false, crossChunk, changedCell);
        }

        static FlowWrite blocked() {
            return new FlowWrite(false, false, true, false, false, "");
        }

        static FlowWrite outside() {
            return new FlowWrite(false, false, false, true, false, "");
        }

        static FlowWrite unchanged() {
            return new FlowWrite(false, false, false, false, false, "");
        }
    }
}
