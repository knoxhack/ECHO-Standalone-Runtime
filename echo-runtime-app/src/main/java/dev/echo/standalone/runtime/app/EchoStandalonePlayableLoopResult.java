package dev.echo.standalone.runtime.app;

public record EchoStandalonePlayableLoopResult(
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
        String shellSummary,
        String playableSummary,
        String saveSummary,
        String exitSummary
) {
    public EchoStandalonePlayableLoopResult {
        shellSummary = EchoAppText.requireText(shellSummary, "shellSummary");
        playableSummary = EchoAppText.requireText(playableSummary, "playableSummary");
        saveSummary = EchoAppText.requireText(saveSummary, "saveSummary");
        exitSummary = EchoAppText.requireText(exitSummary, "exitSummary");
    }

    public boolean ready() {
        return newGame
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

    public String summary() {
        return "newGame=" + newGame
                + " spawn=" + spawn
                + " move=" + move
                + " terminal=" + openTerminal
                + " objective=" + completeObjective
                + " hazard=" + interactWithHazard
                + " item=" + useItem
                + " save=" + save
                + " load=" + load
                + " continue=" + continueGame
                + " exit=" + exitCleanly;
    }
}
