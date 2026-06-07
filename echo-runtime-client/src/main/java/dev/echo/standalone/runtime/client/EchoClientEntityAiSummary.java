package dev.echo.standalone.runtime.client;

record EchoClientEntityAiSummary(
        int idle,
        int pursuing,
        int attacking,
        int movements,
        int attacks,
        int blocked,
        long totalMovements,
        long totalAttacks,
        String reason
) {
    static final EchoClientEntityAiSummary EMPTY =
            new EchoClientEntityAiSummary(0, 0, 0, 0, 0, 0, 0L, 0L, "idle");
}
