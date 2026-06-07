package dev.echo.standalone.runtime.client;

record EchoClientEntitySpawnSummary(
        String biomeId,
        String definitionId,
        String reason,
        int livingEntities,
        int hostileEntities,
        long attempts,
        long spawned
) {
    static final EchoClientEntitySpawnSummary EMPTY =
            new EchoClientEntitySpawnSummary("none", "none", "idle", 0, 0, 0L, 0L);
}
