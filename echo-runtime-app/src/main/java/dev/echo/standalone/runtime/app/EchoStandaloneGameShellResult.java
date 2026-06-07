package dev.echo.standalone.runtime.app;

public record EchoStandaloneGameShellResult(
        boolean titleVisible,
        boolean newGameStartsPlayableRuntime,
        boolean continueStartsPlayableRuntime,
        boolean pauseBlocksGameplay,
        boolean optionsVisible,
        boolean inventoryVisible,
        boolean inventoryBlocksGameplay,
        boolean terminalVisible,
        boolean terminalBlocksGameplay,
        boolean missionLogVisible,
        boolean missionLogBlocksGameplay,
        boolean resumeReturnsToGameplay,
        boolean saveProfileBound,
        boolean adapterCoreMultiRuntimeReady,
        String initialSummary,
        String finalSummary,
        String playableSummary
) {
    public EchoStandaloneGameShellResult {
        initialSummary = EchoAppText.requireText(initialSummary, "initialSummary");
        finalSummary = EchoAppText.requireText(finalSummary, "finalSummary");
        playableSummary = EchoAppText.requireText(playableSummary, "playableSummary");
    }

    public boolean ready() {
        return titleVisible
                && newGameStartsPlayableRuntime
                && continueStartsPlayableRuntime
                && pauseBlocksGameplay
                && optionsVisible
                && inventoryVisible
                && inventoryBlocksGameplay
                && terminalVisible
                && terminalBlocksGameplay
                && missionLogVisible
                && missionLogBlocksGameplay
                && resumeReturnsToGameplay
                && saveProfileBound
                && adapterCoreMultiRuntimeReady;
    }

    public String summary() {
        return "title=" + titleVisible
                + " newGame=" + newGameStartsPlayableRuntime
                + " continue=" + continueStartsPlayableRuntime
                + " pauseBlocks=" + pauseBlocksGameplay
                + " options=" + optionsVisible
                + " inventory=" + inventoryVisible
                + " terminal=" + terminalVisible
                + " missionLog=" + missionLogVisible
                + " resume=" + resumeReturnsToGameplay
                + " adapterCore=" + adapterCoreMultiRuntimeReady;
    }
}
