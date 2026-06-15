package dev.echo.standalone.runtime.world.gen.biome;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;

/**
 * Selects the blockstate for a column position based on biome and depth below surface.
 */
@FunctionalInterface
public interface EchoSurfaceRule {

    /**
     * Returns the blockstate for the given position.
     *
     * @param biomeId          biome identifier for the column
     * @param depthBelowSurface number of blocks below the surface; 0 is the surface block itself
     * @param worldY           absolute Y coordinate
     * @param surfaceY         computed surface Y coordinate for the column
     * @param belowSeaLevel    whether this position is at or below sea level
     * @return blockstate to place
     */
    EchoBlockStateContract apply(String biomeId, int depthBelowSurface, int worldY, int surfaceY, boolean belowSeaLevel);
}
