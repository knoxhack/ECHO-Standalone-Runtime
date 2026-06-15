package dev.echo.standalone.runtime.world.gen.biome;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoBlockStateParser;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Surface rules driven by Openlands biome block palettes.
 */
public final class EchoSurfaceRules {

    private final EchoBlockRegistry registry;
    private final EchoBlockStateParser parser;
    private final Map<String, EchoOpenlandsBiomeDefinition> biomes;

    public EchoSurfaceRules(EchoBlockRegistry registry, List<EchoOpenlandsBiomeDefinition> biomes) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.parser = new EchoBlockStateParser(registry);
        this.biomes = new java.util.LinkedHashMap<>();
        for (EchoOpenlandsBiomeDefinition biome : biomes) {
            this.biomes.put(biome.id(), biome);
        }
    }

    public EchoSurfaceRule defaultRule() {
        return (biomeId, depthBelowSurface, worldY, surfaceY, belowSeaLevel) -> {
            EchoOpenlandsBiomeDefinition biome = biomes.get(biomeId);
            if (biome == null) {
                return fieldstone();
            }
            EchoOpenlandsBiomeDefinition.EchoOpenlandsBlockPalette palette = biome.blockPalette();
            if (depthBelowSurface == 0) {
                if (belowSeaLevel && worldY < surfaceY) {
                    return resolve(palette.surface(), "echomaterialcore:sand");
                }
                return resolve(palette.surface(), "echomaterialcore:fieldstone");
            }
            if (depthBelowSurface <= 3) {
                return resolve(palette.subsurface(), firstBlockId(palette.surface(), "echomaterialcore:fieldstone"));
            }
            return resolve(palette.stone(), "echomaterialcore:fieldstone");
        };
    }

    private EchoBlockStateContract resolve(Object paletteValue, String fallback) {
        String id = firstBlockId(paletteValue, fallback);
        return parser.parse(defaultState(id));
    }

    private String firstBlockId(Object value, String fallback) {
        if (value instanceof String text && !text.isBlank()) {
            return namespaced(text);
        }
        if (value instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof String text && !text.isBlank()) {
                return namespaced(text);
            }
        }
        return fallback;
    }

    private String namespaced(String id) {
        if (id == null || id.isBlank()) {
            return "echomaterialcore:fieldstone";
        }
        if (id.contains(":")) {
            return id;
        }
        return "echoopenlandsprotocol:" + id;
    }

    private String defaultState(String blockId) {
        if (blockId.equals("echoopenlandsprotocol:meadow_grass_block")) {
            return blockId + "[snowy=false]";
        }
        return blockId;
    }

    private EchoBlockStateContract fieldstone() {
        return parser.parse("echomaterialcore:fieldstone");
    }
}
