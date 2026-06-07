package dev.echo.standalone.runtime.client;

record EchoClientWorldStreamResult(
        boolean playerChunkChanged,
        boolean activeChunksChanged,
        boolean cachedChunksChanged,
        boolean viewDistanceChanged
) {
    static final EchoClientWorldStreamResult NONE = new EchoClientWorldStreamResult(false, false, false, false);

    boolean loadedChunksChanged() {
        return activeChunksChanged;
    }

    boolean renderRegionChanged() {
        return playerChunkChanged || activeChunksChanged || viewDistanceChanged;
    }
}
