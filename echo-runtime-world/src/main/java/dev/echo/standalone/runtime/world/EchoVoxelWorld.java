package dev.echo.standalone.runtime.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record EchoVoxelWorld(
        String worldId,
        long seed,
        int chunkSize,
        List<EchoVoxelChunk> chunks,
        double spawnX,
        double spawnY,
        double spawnZ,
        double spawnYawDegrees,
        EchoVoxelBiomeSource biomeSource
) {
    public EchoVoxelWorld {
        if (worldId == null || worldId.isBlank()) {
            throw new IllegalArgumentException("worldId must not be blank");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        Objects.requireNonNull(chunks, "chunks");
        chunks = List.copyOf(chunks);
        biomeSource = biomeSource == null ? EchoVoxelBiomeSources.byWorldId(worldId) : biomeSource;
    }

    public EchoVoxelWorld(
            String worldId,
            long seed,
            int chunkSize,
            List<EchoVoxelChunk> chunks,
            double spawnX,
            double spawnY,
            double spawnZ,
            double spawnYawDegrees
    ) {
        this(
                worldId,
                seed,
                chunkSize,
                chunks,
                spawnX,
                spawnY,
                spawnZ,
                spawnYawDegrees,
                EchoVoxelBiomeSources.byWorldId(worldId)
        );
    }

    public EchoVoxelBlock blockAt(int x, int y, int z) {
        return blockStateAt(x, y, z).block();
    }

    public EchoVoxelBiome biomeAt(double x, double z) {
        return biomeAt((int) Math.floor(x), (int) Math.floor(z));
    }

    public EchoVoxelBiome biomeAt(int x, int z) {
        return biomeSource.biomeAt(seed, x, z);
    }

    public Optional<String> biomeIdAtBlock(int x, int y, int z) {
        if (y < 0) {
            return Optional.empty();
        }
        Optional<EchoVoxelChunk> chunk = chunkAt(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));
        if (chunk.isEmpty()) {
            return Optional.empty();
        }
        return chunk.orElseThrow().biomeIdAtLocal(
                Math.floorMod(x, chunkSize),
                Math.floorMod(y, chunkSize),
                Math.floorMod(z, chunkSize)
        );
    }

    public EchoVoxelBlockState blockStateAt(int x, int y, int z) {
        if (y < 0) {
            return EchoVoxelBlockState.AIR;
        }
        Optional<EchoVoxelChunk> chunk = chunkAt(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));
        if (chunk.isEmpty()) {
            return EchoVoxelBlockState.AIR;
        }
        return chunk.orElseThrow().stateAtLocal(
                Math.floorMod(x, chunkSize),
                Math.floorMod(y, chunkSize),
                Math.floorMod(z, chunkSize)
        );
    }

    public boolean setBlockAt(int x, int y, int z, EchoVoxelBlock block) {
        return setBlockStateAt(x, y, z, EchoVoxelBlockState.of(block));
    }

    public boolean setBlockStateAt(int x, int y, int z, EchoVoxelBlockState state) {
        Objects.requireNonNull(state, "state");
        if (y < 0) {
            return false;
        }
        Optional<EchoVoxelChunk> chunk = chunkAt(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));
        if (chunk.isEmpty()) {
            return false;
        }
        chunk.orElseThrow().setStateLocal(
                Math.floorMod(x, chunkSize),
                Math.floorMod(y, chunkSize),
                Math.floorMod(z, chunkSize),
                state
        );
        return true;
    }

    public EchoVoxelBlockBreakResult attemptBreakBlock(
            int x,
            int y,
            int z,
            double accumulatedSeconds,
            double toolSpeed
    ) {
        if (!Double.isFinite(accumulatedSeconds) || accumulatedSeconds < 0.0D) {
            throw new IllegalArgumentException("accumulatedSeconds must be finite and non-negative");
        }
        if (!Double.isFinite(toolSpeed) || toolSpeed <= 0.0D) {
            throw new IllegalArgumentException("toolSpeed must be finite and positive");
        }
        EchoVoxelBlock block = blockAt(x, y, z);
        if (block.air()) {
            return new EchoVoxelBlockBreakResult(x, y, z, block, accumulatedSeconds, 0.0D, 1.0D, false, "air");
        }
        double requiredSeconds = breakDurationSeconds(block, toolSpeed);
        double progress = clamp(accumulatedSeconds / requiredSeconds, 0.0D, 1.0D);
        if (progress < 1.0D) {
            return new EchoVoxelBlockBreakResult(
                    x,
                    y,
                    z,
                    block,
                    accumulatedSeconds,
                    requiredSeconds,
                    progress,
                    false,
                    "in_progress"
            );
        }
        boolean changed = setBlockStateAt(x, y, z, EchoVoxelBlockState.AIR);
        return new EchoVoxelBlockBreakResult(
                x,
                y,
                z,
                block,
                accumulatedSeconds,
                requiredSeconds,
                progress,
                changed,
                changed ? "broken" : "outside_loaded_chunk"
        );
    }

    public Optional<EchoVoxelHit> raycast(
            double originX,
            double originY,
            double originZ,
            double yawDegrees,
            double pitchDegrees,
            double maxDistance
    ) {
        Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById = chunkIndex();
        double yawRadians = Math.toRadians(yawDegrees);
        double pitchRadians = Math.toRadians(pitchDegrees);
        double horizontal = Math.cos(pitchRadians);
        double directionX = Math.sin(yawRadians) * horizontal;
        double directionY = Math.sin(pitchRadians);
        double directionZ = Math.cos(yawRadians) * horizontal;
        int previousX = floor(originX);
        int previousY = floor(originY);
        int previousZ = floor(originZ);
        for (double distance = 0.0D; distance <= maxDistance; distance += 0.05D) {
            int blockX = floor(originX + directionX * distance);
            int blockY = floor(originY + directionY * distance);
            int blockZ = floor(originZ + directionZ * distance);
            EchoVoxelBlock block = blockAt(chunksById, blockX, blockY, blockZ);
            if (!block.air()) {
                return Optional.of(new EchoVoxelHit(
                        blockX,
                        blockY,
                        blockZ,
                        Integer.compare(previousX, blockX),
                        Integer.compare(previousY, blockY),
                        Integer.compare(previousZ, blockZ),
                        block,
                        distance
                ));
            }
            previousX = blockX;
            previousY = blockY;
            previousZ = blockZ;
        }
        return Optional.empty();
    }

    public boolean collidesWithBox(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        if (!validBox(minX, minY, minZ, maxX, maxY, maxZ)) {
            throw new IllegalArgumentException("collision query box must be finite and ordered");
        }
        int blockMinX = floor(minX);
        int blockMinY = floor(minY);
        int blockMinZ = floor(minZ);
        int blockMaxX = floor(Math.nextDown(maxX));
        int blockMaxY = floor(Math.nextDown(maxY));
        int blockMaxZ = floor(Math.nextDown(maxZ));
        Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById = chunkIndex();
        for (int blockY = blockMinY; blockY <= blockMaxY; blockY++) {
            for (int blockZ = blockMinZ; blockZ <= blockMaxZ; blockZ++) {
                for (int blockX = blockMinX; blockX <= blockMaxX; blockX++) {
                    EchoVoxelBlock block = blockAt(chunksById, blockX, blockY, blockZ);
                    if (block.collisionBox().intersectsWorldBox(
                            blockX,
                            blockY,
                            blockZ,
                            minX,
                            minY,
                            minZ,
                            maxX,
                            maxY,
                            maxZ
                    )) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean hasChunk(EchoVoxelChunkId chunkId) {
        return chunkAt(chunkId).isPresent();
    }

    public int loadedChunkCount() {
        return chunks.size();
    }

    public EchoVoxelWorldTickResult tickLoadedBlocks(long gameTick) {
        int tickedBlocks = 0;
        int hazardBlocks = 0;
        int metadataWrites = 0;
        for (EchoVoxelChunk chunk : chunks) {
            EchoVoxelChunk.TickSummary summary = chunk.tickLoadedBlocks(gameTick);
            tickedBlocks += summary.tickedBlocks();
            hazardBlocks += summary.hazardBlocks();
            metadataWrites += summary.metadataWrites();
        }
        return new EchoVoxelWorldTickResult(
                gameTick,
                chunks.size(),
                tickedBlocks,
                hazardBlocks,
                metadataWrites
        );
    }

    public EchoVoxelWorldTickResult randomTickLoadedBlocks(long gameTick, long randomSeed, int samplesPerChunk) {
        if (gameTick < 0L) {
            throw new IllegalArgumentException("gameTick must not be negative");
        }
        if (samplesPerChunk < 0) {
            throw new IllegalArgumentException("samplesPerChunk must not be negative");
        }
        int tickedBlocks = 0;
        int hazardBlocks = 0;
        int metadataWrites = 0;
        int chunkIndex = 0;
        for (EchoVoxelChunk chunk : chunks) {
            EchoVoxelChunk.TickSummary summary = chunk.randomTickLoadedBlocks(
                    gameTick,
                    randomSeed + chunkIndex,
                    samplesPerChunk
            );
            tickedBlocks += summary.tickedBlocks();
            hazardBlocks += summary.hazardBlocks();
            metadataWrites += summary.metadataWrites();
            chunkIndex++;
        }
        return new EchoVoxelWorldTickResult(
                gameTick,
                chunks.size(),
                tickedBlocks,
                hazardBlocks,
                metadataWrites
        );
    }

    public Set<EchoVoxelChunkId> loadedChunkIds() {
        return Map.copyOf(chunkIndex()).keySet();
    }

    public EchoVoxelWorld withChunk(EchoVoxelChunk chunk) {
        return withAdditionalChunks(List.of(chunk));
    }

    public EchoVoxelWorld withAdditionalChunks(List<EchoVoxelChunk> additionalChunks) {
        Objects.requireNonNull(additionalChunks, "additionalChunks");
        if (additionalChunks.isEmpty()) {
            return this;
        }
        LinkedHashMap<EchoVoxelChunkId, EchoVoxelChunk> merged = new LinkedHashMap<>(chunkIndex());
        for (EchoVoxelChunk chunk : additionalChunks) {
            Objects.requireNonNull(chunk, "chunk");
            if (chunk.size() != chunkSize) {
                throw new IllegalArgumentException("additional chunk size must match world chunk size");
            }
            merged.putIfAbsent(chunk.id(), chunk);
        }
        if (merged.size() == chunks.size()) {
            return this;
        }
        return new EchoVoxelWorld(
                worldId,
                seed,
                chunkSize,
                List.copyOf(merged.values()),
                spawnX,
                spawnY,
                spawnZ,
                spawnYawDegrees,
                biomeSource
        );
    }

    public EchoVoxelWorld withLoadedChunks(List<EchoVoxelChunk> loadedChunks) {
        Objects.requireNonNull(loadedChunks, "loadedChunks");
        ArrayList<EchoVoxelChunk> next = new ArrayList<>();
        for (EchoVoxelChunk chunk : loadedChunks) {
            Objects.requireNonNull(chunk, "chunk");
            if (chunk.size() != chunkSize) {
                throw new IllegalArgumentException("loaded chunk size must match world chunk size");
            }
            next.add(chunk);
        }
        return new EchoVoxelWorld(
                worldId,
                seed,
                chunkSize,
                List.copyOf(next),
                spawnX,
                spawnY,
                spawnZ,
                spawnYawDegrees,
                biomeSource
        );
    }

    public EchoVoxelWorld withBiomeSource(EchoVoxelBiomeSource nextBiomeSource) {
        EchoVoxelBiomeSource replacement =
                nextBiomeSource == null ? EchoVoxelBiomeSources.byWorldId(worldId) : nextBiomeSource;
        if (replacement == biomeSource) {
            return this;
        }
        return new EchoVoxelWorld(
                worldId,
                seed,
                chunkSize,
                chunks,
                spawnX,
                spawnY,
                spawnZ,
                spawnYawDegrees,
                replacement
        );
    }

    public List<EchoVoxelBlockInstance> nonAirBlocks() {
        ArrayList<EchoVoxelBlockInstance> result = new ArrayList<>();
        for (EchoVoxelChunk chunk : chunks) {
            result.addAll(chunk.nonAirBlocks());
        }
        return List.copyOf(result);
    }

    private Optional<EchoVoxelChunk> chunkAt(EchoVoxelChunkId chunkId) {
        EchoVoxelChunkId safeChunkId = Objects.requireNonNull(chunkId, "chunkId");
        for (EchoVoxelChunk chunk : chunks) {
            if (chunk.id().equals(safeChunkId)) {
                return Optional.of(chunk);
            }
        }
        return Optional.empty();
    }

    private EchoVoxelBlock blockAt(
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            int x,
            int y,
            int z
    ) {
        return blockStateAt(chunksById, x, y, z).block();
    }

    private EchoVoxelBlockState blockStateAt(
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            int x,
            int y,
            int z
    ) {
        if (y < 0) {
            return EchoVoxelBlockState.AIR;
        }
        EchoVoxelChunk chunk = chunksById.get(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));
        if (chunk == null) {
            return EchoVoxelBlockState.AIR;
        }
        return chunk.stateAtLocal(
                Math.floorMod(x, chunkSize),
                Math.floorMod(y, chunkSize),
                Math.floorMod(z, chunkSize)
        );
    }

    private Map<EchoVoxelChunkId, EchoVoxelChunk> chunkIndex() {
        LinkedHashMap<EchoVoxelChunkId, EchoVoxelChunk> result = new LinkedHashMap<>();
        for (EchoVoxelChunk chunk : chunks) {
            result.putIfAbsent(chunk.id(), chunk);
        }
        return result;
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static double breakDurationSeconds(EchoVoxelBlock block, double toolSpeed) {
        return Math.max(0.12D, (0.22D + block.hardness() * 0.58D) / toolSpeed);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static boolean validBox(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        return Double.isFinite(minX)
                && Double.isFinite(minY)
                && Double.isFinite(minZ)
                && Double.isFinite(maxX)
                && Double.isFinite(maxY)
                && Double.isFinite(maxZ)
                && minX <= maxX
                && minY <= maxY
                && minZ <= maxZ;
    }
}
