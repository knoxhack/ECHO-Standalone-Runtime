package dev.echo.standalone.runtime.app;

public record EchoStandaloneLiveWindowWalkthroughResult(
        String runtimePath,
        boolean liveWindowPath,
        boolean deterministicClose,
        boolean headlessFallback,
        boolean adapterCoreRuntimeBridgeActive,
        boolean newGame,
        boolean spawn,
        boolean move,
        boolean openTerminal,
        boolean completeObjective,
        boolean interactWithHazard,
        boolean useItem,
        boolean save,
        boolean load,
        boolean continueGame,
        boolean exitCleanly,
        String playableSummary
) {
    public EchoStandaloneLiveWindowWalkthroughResult {
        runtimePath = EchoAppText.requireText(runtimePath, "runtimePath");
        playableSummary = EchoAppText.requireText(playableSummary, "playableSummary");
    }

    public static EchoStandaloneLiveWindowWalkthroughResult from(
            String runtimePath,
            boolean liveWindowPath,
            boolean deterministicClose,
            boolean headlessFallback,
            boolean adapterCoreRuntimeBridgeActive,
            EchoStandalonePlayableLoopResult playableLoop
    ) {
        return new EchoStandaloneLiveWindowWalkthroughResult(
                runtimePath,
                liveWindowPath,
                deterministicClose,
                headlessFallback,
                adapterCoreRuntimeBridgeActive,
                playableLoop.newGame(),
                playableLoop.spawn(),
                playableLoop.move(),
                playableLoop.openTerminal(),
                playableLoop.completeObjective(),
                playableLoop.interactWithHazard(),
                playableLoop.useItem(),
                playableLoop.save(),
                playableLoop.load(),
                playableLoop.continueGame(),
                playableLoop.exitCleanly(),
                playableLoop.summary()
        );
    }

    public boolean ready() {
        return adapterCoreRuntimeBridgeActive
                && newGame
                && spawn
                && move
                && openTerminal
                && completeObjective
                && interactWithHazard
                && useItem
                && save
                && load
                && continueGame
                && exitCleanly;
    }

    public int actionCount() {
        int actions = 0;
        actions += newGame ? 1 : 0;
        actions += spawn ? 1 : 0;
        actions += move ? 1 : 0;
        actions += openTerminal ? 1 : 0;
        actions += completeObjective ? 1 : 0;
        actions += interactWithHazard ? 1 : 0;
        actions += useItem ? 1 : 0;
        actions += save ? 1 : 0;
        actions += load ? 1 : 0;
        actions += continueGame ? 1 : 0;
        actions += exitCleanly ? 1 : 0;
        return actions;
    }

    public String summary() {
        return "runtimePath=" + runtimePath
                + " liveWindowPath=" + liveWindowPath
                + " deterministicClose=" + deterministicClose
                + " headlessFallback=" + headlessFallback
                + " adapterCore=" + adapterCoreRuntimeBridgeActive
                + " actions=" + actionCount() + "/11"
                + " " + playableSummary;
    }
}
