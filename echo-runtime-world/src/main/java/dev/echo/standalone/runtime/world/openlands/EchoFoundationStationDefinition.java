package dev.echo.standalone.runtime.world.openlands;

import java.util.Map;

/**
 * A Foundation crafting station definition from a moved Openlands payload.
 */
public record EchoFoundationStationDefinition(
        String id,
        String displayName,
        String requiresBlock,
        String grid,
        String process,
        String notes,
        Map<String, Object> extras
) {
    public EchoFoundationStationDefinition {
        id = id == null || id.isBlank() ? "echo:foundation_station_unknown" : id.trim();
        displayName = displayName == null || displayName.isBlank() ? id : displayName.trim();
        requiresBlock = requiresBlock == null ? "" : requiresBlock.trim();
        grid = grid == null ? "" : grid.trim();
        process = process == null ? "" : process.trim();
        notes = notes == null ? "" : notes.trim();
        extras = extras == null ? Map.of() : extras.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        e -> e.getKey().trim(),
                        Map.Entry::getValue
                ));
    }
}
