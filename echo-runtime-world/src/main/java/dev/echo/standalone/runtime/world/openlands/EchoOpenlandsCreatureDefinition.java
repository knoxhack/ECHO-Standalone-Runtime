package dev.echo.standalone.runtime.world.openlands;

import java.util.List;
import java.util.Map;

/**
 * Raw Openlands creature definition loaded from module JSON.
 */
public record EchoOpenlandsCreatureDefinition(
        String id,
        String displayName,
        String category,
        List<String> biomes,
        int health,
        int damage,
        String notes,
        Map<String, Object> raw
) {
}
