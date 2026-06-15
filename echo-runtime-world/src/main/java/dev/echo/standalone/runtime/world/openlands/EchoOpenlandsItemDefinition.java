package dev.echo.standalone.runtime.world.openlands;

import java.util.List;
import java.util.Map;

/**
 * Raw Openlands item definition loaded from module JSON.
 *
 * <p>This record is intentionally decoupled from {@code echo-runtime-item} so that
 * {@code echo-runtime-world} can load content without creating a module dependency cycle.
 * Callers in higher modules convert this to {@code EchoItemDefinition} for registration.
 */
public record EchoOpenlandsItemDefinition(
        String id,
        String displayName,
        String useType,
        int stackSize,
        List<String> tags,
        String model,
        String texture,
        List<String> recipeRefs,
        List<String> tooltipLines,
        Map<String, Object> raw
) {
}
