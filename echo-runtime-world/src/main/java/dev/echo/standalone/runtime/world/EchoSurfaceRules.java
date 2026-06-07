package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Rules that determine which block replaces another at a given world position
 * during surface generation. Rules are evaluated in order; the first matching
 * rule wins.
 */
public final class EchoSurfaceRules {
    private final List<Rule> rules;

    public EchoSurfaceRules(List<Rule> rules) {
        this.rules = rules == null ? List.of() : List.copyOf(rules);
    }

    /**
     * Apply rules to choose a surface block for the given column.
     *
     * @param worldX     world X
     * @param worldY     world Y (depth in column)
     * @param worldZ     world Z
     * @param noiseValue noise sample at this position
     * @param biome      resolved biome
     * @param defaultBlock block to use if no rule matches
     * @return the chosen block
     */
    public EchoVoxelBlock apply(
            int worldX,
            int worldY,
            int worldZ,
            double noiseValue,
            EchoVoxelBiome biome,
            EchoVoxelBlock defaultBlock
    ) {
        for (Rule rule : rules) {
            Optional<EchoVoxelBlock> result = rule.apply(worldX, worldY, worldZ, noiseValue, biome, defaultBlock);
            if (result.isPresent()) {
                return result.get();
            }
        }
        return defaultBlock;
    }

    public static EchoSurfaceRules empty() {
        return new EchoSurfaceRules(List.of());
    }

    /**
     * A single surface rule.
     */
    @FunctionalInterface
    public interface Rule {
        /**
         * @return the replacement block if this rule matches, otherwise empty
         */
        Optional<EchoVoxelBlock> apply(
                int worldX,
                int worldY,
                int worldZ,
                double noiseValue,
                EchoVoxelBiome biome,
                EchoVoxelBlock defaultBlock
        );
    }

    /**
     * Replaces the top block (y == height) with the given block if the biome matches.
     */
    public static Rule topBlockIfBiome(EchoVoxelBlock block, String biomeTag) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(biomeTag, "biomeTag");
        return (x, y, z, noise, biome, def) -> {
            if (y == 0 && biome != null && biome.hasTag(biomeTag)) {
                return Optional.of(block);
            }
            return Optional.empty();
        };
    }

    /**
     * Replaces blocks below a given depth with the given block.
     */
    public static Rule belowDepth(EchoVoxelBlock block, int depth) {
        Objects.requireNonNull(block, "block");
        return (x, y, z, noise, biome, def) -> {
            if (y < -depth) {
                return Optional.of(block);
            }
            return Optional.empty();
        };
    }

    /**
     * Replaces the top block with the given block if noise is below a threshold.
     */
    public static Rule topIfNoiseBelow(EchoVoxelBlock block, double threshold) {
        Objects.requireNonNull(block, "block");
        return (x, y, z, noise, biome, def) -> {
            if (y == 0 && noise < threshold) {
                return Optional.of(block);
            }
            return Optional.empty();
        };
    }
}
