package dev.echo.standalone.runtime.app;

public record EchoStandaloneMissionLogFramebufferResult(
        String target,
        int width,
        int height,
        boolean adapterCoreMultiRuntimeReady,
        boolean framebufferShapePreserved,
        boolean missionLogOverlayChangedFrame,
        int centralChangedPixels,
        int objectiveRows,
        int completedObjectives,
        int totalObjectives,
        boolean requiredObjectivesVisible,
        boolean optionalObjectivesVisible,
        boolean completedHistoryVisible,
        boolean currentHintVisible,
        boolean terminalNotesVisible,
        boolean extractionStatusVisible,
        long baseChecksum,
        long missionLogChecksum
) {
    public EchoStandaloneMissionLogFramebufferResult {
        target = EchoAppText.requireText(target, "target");
        if (width <= 0
                || height <= 0
                || centralChangedPixels < 0
                || objectiveRows < 0
                || completedObjectives < 0
                || totalObjectives <= 0) {
            throw new IllegalArgumentException("mission log framebuffer counts must be positive");
        }
    }

    public boolean ready() {
        return adapterCoreMultiRuntimeReady
                && framebufferShapePreserved
                && missionLogOverlayChangedFrame
                && centralChangedPixels > 15_000
                && objectiveRows >= 20
                && completedObjectives >= 15
                && totalObjectives >= 20
                && requiredObjectivesVisible
                && optionalObjectivesVisible
                && completedHistoryVisible
                && currentHintVisible
                && terminalNotesVisible
                && extractionStatusVisible
                && baseChecksum != 0L
                && missionLogChecksum != 0L
                && baseChecksum != missionLogChecksum;
    }

    public String summary() {
        return "target=" + target
                + " size=" + width + "x" + height
                + " centralPixels=" + centralChangedPixels
                + " objectives=" + completedObjectives + "/" + totalObjectives
                + " rows=" + objectiveRows
                + " required=" + requiredObjectivesVisible
                + " optional=" + optionalObjectivesVisible
                + " history=" + completedHistoryVisible
                + " notes=" + terminalNotesVisible
                + " checksum=" + Long.toUnsignedString(baseChecksum, 16)
                + "->" + Long.toUnsignedString(missionLogChecksum, 16);
    }
}
