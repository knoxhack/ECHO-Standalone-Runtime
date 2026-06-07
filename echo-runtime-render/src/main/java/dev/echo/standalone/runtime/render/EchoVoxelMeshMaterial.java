package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoVoxelMeshMaterial(
        String materialId,
        String atlasKey,
        int argb,
        int detailArgb,
        EchoVoxelMaterialPattern pattern,
        boolean opaque,
        Map<String, String> stateProperties,
        String biomeId,
        int biomeTintArgb,
        boolean biomeTinted
) {
    public EchoVoxelMeshMaterial {
        materialId = requireText(materialId, "materialId");
        atlasKey = requireText(atlasKey, "atlasKey");
        pattern = Objects.requireNonNull(pattern, "pattern");
        stateProperties = normalizedProperties(stateProperties);
        biomeId = biomeId == null ? "" : biomeId.trim();
    }

    public EchoVoxelMeshMaterial(
            String materialId,
            String atlasKey,
            int argb,
            int detailArgb,
            EchoVoxelMaterialPattern pattern,
            boolean opaque
    ) {
        this(materialId, atlasKey, argb, detailArgb, pattern, opaque, Map.of(), "", 0xFFFFFFFF, false);
    }

    public static EchoVoxelMeshMaterial fromBlock(EchoVoxelBlock block) {
        return fromBlock(block, null);
    }

    public static EchoVoxelMeshMaterial fromBlock(EchoVoxelBlock block, EchoVoxelBiome biome) {
        return fromBlock(block, Map.of(), biome);
    }

    public static EchoVoxelMeshMaterial fromBlockState(EchoVoxelBlockState state, EchoVoxelBiome biome) {
        Objects.requireNonNull(state, "state");
        return fromBlock(state.block(), renderStateProperties(state.properties()), biome);
    }

    private static EchoVoxelMeshMaterial fromBlock(
            EchoVoxelBlock block,
            Map<String, String> stateProperties,
            EchoVoxelBiome biome
    ) {
        Objects.requireNonNull(block, "block");
        String normalizedBlockId = requireText(block.id(), "blockId");
        String biomeId = biome == null ? "" : biome.id();
        int biomeTintArgb = biome == null ? 0xFFFFFFFF : opaqueArgb(biome.grassColor());
        boolean biomeTinted = biome != null && receivesBiomeTint(block);
        return new EchoVoxelMeshMaterial(
                "voxel:block/" + normalizedBlockId,
                block.atlasKey(),
                biomeTinted ? blendRgb(block.argb(), biomeTintArgb, 0.62D) : block.argb(),
                biomeTinted ? blendRgb(block.detailArgb(), biomeTintArgb, 0.48D) : block.detailArgb(),
                block.materialPattern(),
                block.opaque(),
                stateProperties,
                biomeId,
                biomeTintArgb,
                biomeTinted
        );
    }

    public static boolean receivesBiomeTint(EchoVoxelBlock block) {
        Objects.requireNonNull(block, "block");
        String key = (block.id() + " " + block.atlasKey() + " " + block.displayName()).toLowerCase(Locale.ROOT);
        return key.contains("grass")
                || key.contains("foliage")
                || key.contains("leaves")
                || key.contains("leaf")
                || key.contains("berry")
                || key.contains("bush")
                || key.contains("vine")
                || key.contains("moss")
                || key.contains("sapling");
    }

    private static int blendRgb(int baseArgb, int tintArgb, double tintWeight) {
        double weight = Math.max(0.0D, Math.min(1.0D, tintWeight));
        int alpha = (baseArgb >>> 24) & 0xFF;
        int red = blendChannel((baseArgb >>> 16) & 0xFF, (tintArgb >>> 16) & 0xFF, weight);
        int green = blendChannel((baseArgb >>> 8) & 0xFF, (tintArgb >>> 8) & 0xFF, weight);
        int blue = blendChannel(baseArgb & 0xFF, tintArgb & 0xFF, weight);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int blendChannel(int base, int tint, double tintWeight) {
        return clamp((int) Math.round(base * (1.0D - tintWeight) + tint * tintWeight), 0, 255);
    }

    private static int opaqueArgb(int rgbOrArgb) {
        int alpha = (rgbOrArgb >>> 24) & 0xFF;
        return (alpha == 0 ? 0xFF000000 : (alpha << 24)) | (rgbOrArgb & 0x00FFFFFF);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Map<String, String> normalizedProperties(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, String> result = new TreeMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            result.put(entry.getKey().trim(), entry.getValue().trim());
        }
        return Map.copyOf(result);
    }

    private static Map<String, String> renderStateProperties(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, String> result = new TreeMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            String key = entry.getKey().trim();
            if (runtimeOnlyStateProperty(key)) {
                continue;
            }
            result.put(key, entry.getValue().trim());
        }
        return result.isEmpty() ? Map.of() : Map.copyOf(result);
    }

    private static boolean runtimeOnlyStateProperty(String key) {
        return switch (key) {
            case "blockEntityId", "canonicalId", "machineKind", "recipeProgressTicks", "source" -> true;
            default -> false;
        };
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return Objects.requireNonNull(value, name).trim();
    }
}
