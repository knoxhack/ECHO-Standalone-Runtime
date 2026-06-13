package dev.echo.standalone.runtime.world;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoVoxelLightRuntime {
    public static final int MAX_LIGHT = 15;

    private static final int[][] DIRECTIONS = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 1, 0},
            {0, -1, 0},
            {0, 0, 1},
            {0, 0, -1}
    };

    public EchoVoxelLightSnapshot bake(EchoVoxelWorld world) {
        Objects.requireNonNull(world, "world");
        if (world.chunks().isEmpty()) {
            return new EchoVoxelLightSnapshot(Map.of(), Map.of(), 0, 0, 0, 0, 0, 0, 0, 0);
        }

        int minChunkX = world.chunks().stream().mapToInt(chunk -> chunk.id().x()).min().orElse(0);
        int maxChunkX = world.chunks().stream().mapToInt(chunk -> chunk.id().x()).max().orElse(0);
        int minChunkY = world.chunks().stream().mapToInt(chunk -> chunk.id().y()).min().orElse(0);
        int maxChunkY = world.chunks().stream().mapToInt(chunk -> chunk.id().y()).max().orElse(0);
        int minChunkZ = world.chunks().stream().mapToInt(chunk -> chunk.id().z()).min().orElse(0);
        int maxChunkZ = world.chunks().stream().mapToInt(chunk -> chunk.id().z()).max().orElse(0);
        int size = world.chunkSize();
        int minX = minChunkX * size;
        int maxX = (maxChunkX + 1) * size - 1;
        int minY = minChunkY * size;
        int maxY = (maxChunkY + 1) * size - 1;
        int minZ = minChunkZ * size;
        int maxZ = (maxChunkZ + 1) * size - 1;

        LinkedHashMap<Cell, Integer> skyLight = new LinkedHashMap<>();
        LinkedHashMap<Cell, Integer> blockLight = new LinkedHashMap<>();
        int loadedCells = 0;
        int emissiveCells = 0;

        for (EchoVoxelChunk chunk : world.chunks()) {
            int baseX = chunk.id().x() * size;
            int baseY = chunk.id().y() * size;
            int baseZ = chunk.id().z() * size;
            for (int y = 0; y < size; y++) {
                for (int z = 0; z < size; z++) {
                    for (int x = 0; x < size; x++) {
                        Cell cell = new Cell(baseX + x, baseY + y, baseZ + z);
                        skyLight.put(cell, 0);
                        blockLight.put(cell, 0);
                        loadedCells++;
                        int emission = lightEmission(chunk.stateAtLocal(x, y, z));
                        if (emission > 0) {
                            blockLight.put(cell, emission);
                            emissiveCells++;
                        }
                    }
                }
            }
        }

        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                int currentSky = MAX_LIGHT;
                for (int y = maxY; y >= minY; y--) {
                    Cell cell = new Cell(x, y, z);
                    if (!skyLight.containsKey(cell)) {
                        continue;
                    }
                    EchoVoxelBlockState state = world.blockStateAt(x, y, z);
                    int opacity = lightOpacity(state);
                    if (opacity >= MAX_LIGHT) {
                        currentSky = 0;
                    } else {
                        currentSky = Math.max(0, currentSky - opacity);
                    }
                    skyLight.put(cell, currentSky);
                }
            }
        }

        PropagationStats propagation = propagateBlockLight(world, blockLight);
        int skyLitCells = (int) skyLight.values().stream().filter(value -> value > 0).count();
        int blockLitCells = (int) blockLight.values().stream().filter(value -> value > 0).count();
        int maxSkyLight = skyLight.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        int maxBlockLight = blockLight.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        return new EchoVoxelLightSnapshot(
                skyLight,
                blockLight,
                loadedCells,
                skyLitCells,
                blockLitCells,
                maxSkyLight,
                maxBlockLight,
                emissiveCells,
                propagation.opaqueBlockedSteps(),
                propagation.crossChunkBlockLightWrites()
        );
    }

    private static PropagationStats propagateBlockLight(EchoVoxelWorld world, Map<Cell, Integer> blockLight) {
        ArrayDeque<Cell> queue = new ArrayDeque<>();
        blockLight.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .forEach(queue::add);
        int opaqueBlockedSteps = 0;
        int crossChunkWrites = 0;
        while (!queue.isEmpty()) {
            Cell cell = queue.removeFirst();
            int current = blockLight.getOrDefault(cell, 0);
            if (current <= 1) {
                continue;
            }
            for (int[] direction : DIRECTIONS) {
                Cell neighbor = new Cell(
                        cell.x() + direction[0],
                        cell.y() + direction[1],
                        cell.z() + direction[2]
                );
                if (!blockLight.containsKey(neighbor)) {
                    continue;
                }
                EchoVoxelBlockState target = world.blockStateAt(neighbor.x(), neighbor.y(), neighbor.z());
                int opacity = lightOpacity(target);
                if (opacity >= MAX_LIGHT) {
                    opaqueBlockedSteps++;
                    continue;
                }
                int next = current - Math.max(1, opacity);
                if (next > blockLight.getOrDefault(neighbor, 0)) {
                    if (!sameChunk(cell, neighbor, world.chunkSize())) {
                        crossChunkWrites++;
                    }
                    blockLight.put(neighbor, next);
                    queue.addLast(neighbor);
                }
            }
        }
        return new PropagationStats(opaqueBlockedSteps, crossChunkWrites);
    }

    public static int lightEmission(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        return state.property("lightEmission")
                .or(() -> state.property("emission"))
                .map(EchoVoxelLightRuntime::parseLight)
                .orElseGet(() -> inferredLightEmission(state.block()));
    }

    public static int lightOpacity(EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        return state.property("lightOpacity")
                .map(EchoVoxelLightRuntime::parseLight)
                .orElseGet(() -> {
                    if (state.air()) {
                        return 0;
                    }
                    return state.block().opaque() ? MAX_LIGHT : 1;
                });
    }

    private static int inferredLightEmission(EchoVoxelBlock block) {
        String id = block.id();
        if (id.contains("lava") || id.contains("glow") || id.contains("lamp") || id.contains("torch")) {
            return MAX_LIGHT;
        }
        return 0;
    }

    private static int parseLight(String value) {
        try {
            return Math.max(0, Math.min(MAX_LIGHT, Integer.parseInt(value.trim())));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static boolean sameChunk(Cell left, Cell right, int chunkSize) {
        return EchoVoxelChunkId.fromBlock(left.x(), left.y(), left.z(), chunkSize)
                .equals(EchoVoxelChunkId.fromBlock(right.x(), right.y(), right.z(), chunkSize));
    }

    public record Cell(int x, int y, int z) {
    }

    public record EchoVoxelLightSnapshot(
            Map<Cell, Integer> skyLight,
            Map<Cell, Integer> blockLight,
            int loadedCellCount,
            int skyLitCellCount,
            int blockLitCellCount,
            int maxSkyLight,
            int maxBlockLight,
            int emissiveCellCount,
            int opaqueBlockedSteps,
            int crossChunkBlockLightWrites
    ) {
        public EchoVoxelLightSnapshot {
            skyLight = Map.copyOf(new LinkedHashMap<>(skyLight));
            blockLight = Map.copyOf(new LinkedHashMap<>(blockLight));
        }

        public int skyLightAt(int x, int y, int z) {
            return skyLight.getOrDefault(new Cell(x, y, z), 0);
        }

        public int blockLightAt(int x, int y, int z) {
            return blockLight.getOrDefault(new Cell(x, y, z), 0);
        }

        public int combinedLightAt(int x, int y, int z) {
            return Math.max(skyLightAt(x, y, z), blockLightAt(x, y, z));
        }

        public List<Integer> lightLevelsAt(List<Cell> cells) {
            Objects.requireNonNull(cells, "cells");
            return cells.stream().map(cell -> combinedLightAt(cell.x(), cell.y(), cell.z())).toList();
        }
    }

    private record PropagationStats(int opaqueBlockedSteps, int crossChunkBlockLightWrites) {
    }
}
