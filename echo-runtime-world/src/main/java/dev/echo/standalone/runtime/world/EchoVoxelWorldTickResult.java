package dev.echo.standalone.runtime.world;

public record EchoVoxelWorldTickResult(
        long gameTick,
        int loadedChunks,
        int tickedBlocks,
        int hazardBlocks,
        int metadataWrites
) {
    public EchoVoxelWorldTickResult {
        if (gameTick < 0L
                || loadedChunks < 0
                || tickedBlocks < 0
                || hazardBlocks < 0
                || metadataWrites < 0) {
            throw new IllegalArgumentException("world tick counts must not be negative");
        }
    }

    public boolean deterministicTickApplied() {
        return loadedChunks > 0 && tickedBlocks > 0 && metadataWrites >= tickedBlocks;
    }

    public String summary() {
        return "tick=" + gameTick
                + " chunks=" + loadedChunks
                + " blocks=" + tickedBlocks
                + " hazards=" + hazardBlocks
                + " metadata=" + metadataWrites;
    }
}
