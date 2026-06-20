package dev.echo.standalone.runtime.client;

import java.util.List;

record EchoClientEntitySpawnSummary(
        String biomeId,
        String definitionId,
        String reason,
        String threatProfile,
        int threatLevel,
        List<String> spawnBiomeTags,
        int livingEntities,
        int hostileEntities,
        long attempts,
        long spawned
) {
    static final EchoClientEntitySpawnSummary EMPTY =
            new EchoClientEntitySpawnSummary("none", "none", "idle", "", 0, List.of(), 0, 0, 0L, 0L);

    EchoClientEntitySpawnSummary {
        biomeId = clean(biomeId, "none");
        definitionId = clean(definitionId, "none");
        reason = clean(reason, "idle");
        threatProfile = clean(threatProfile, "");
        threatLevel = Math.max(0, threatLevel);
        spawnBiomeTags = List.copyOf(spawnBiomeTags == null ? List.of() : spawnBiomeTags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .toList());
    }

    private static String clean(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
