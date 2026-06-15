package dev.echo.standalone.runtime.world.openlands;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Raw Openlands biome definition loaded from module JSON.
 */
public record EchoOpenlandsBiomeDefinition(
        String id,
        String displayName,
        String temperature,
        String humidity,
        String terrainProfile,
        EchoOpenlandsBlockPalette blockPalette,
        List<String> resourceSet,
        Map<String, Object> extras
) {
    public EchoOpenlandsBiomeDefinition {
        id = requireText(id, "id");
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        temperature = temperature == null ? "mild" : temperature.trim();
        humidity = humidity == null ? "normal" : humidity.trim();
        terrainProfile = terrainProfile == null ? "rolling_lowland" : terrainProfile.trim();
        blockPalette = blockPalette == null ? new EchoOpenlandsBlockPalette("", "", "", List.of()) : blockPalette;
        resourceSet = resourceSet == null ? List.of() : List.copyOf(resourceSet);
        extras = extras == null ? Map.of() : Map.copyOf(extras);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    public record EchoOpenlandsBlockPalette(
            Object surface,
            Object subsurface,
            Object stone,
            List<String> treeFamilies
    ) {
        public EchoOpenlandsBlockPalette {
            treeFamilies = treeFamilies == null ? List.of() : List.copyOf(treeFamilies);
        }
    }
}
