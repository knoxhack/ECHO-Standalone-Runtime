package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;

import java.util.List;

public final class EchoClientSubtitleOverlaySmokeHarness {
    private EchoClientSubtitleOverlaySmokeHarness() {
    }

    public static void main(String[] args) {
        requireSubtitleLifecycle();
        requireOverlayCapAndLayout();
        System.out.println("client subtitle overlay smoke PASS lines=" + EchoClientSubtitleOverlayPlan.MAX_LINES);
    }

    private static void requireSubtitleLifecycle() {
        EchoClientAudio audio = audioWithBackend();
        audio.playBlockHit();
        audio.playBreak();

        List<EchoClientSubtitleLine> lines = audio.subtitleLines(System.nanoTime(), true);
        require(lines.size() == 2,
                "Block-hit and block-break playback should create two active subtitle lines");
        require("Block Break".equals(lines.get(0).text()),
                "Newest subtitle line should be ordered first");
        require(containsText(lines, "Block Hit"),
                "Subtitle lines should include the initial block-hit cue");
        try {
            lines.clear();
            throw new AssertionError("Subtitle lines should be returned as a read-only snapshot");
        } catch (UnsupportedOperationException expected) {
            // Expected read-only snapshot.
        }
        require(audio.subtitleLines(System.nanoTime(), false).isEmpty(),
                "Disabled subtitles should hide active subtitle lines");
        require(audio.subtitleLines(System.nanoTime() + 10_000_000_000L, true).isEmpty(),
                "Expired subtitle lines should be pruned");
        require(!EchoClientSubtitleOverlayPlan.from(320, 240, List.of()).visible(),
                "Subtitle overlay planner should fast-path empty subtitle input as hidden");
    }

    private static void requireOverlayCapAndLayout() {
        EchoClientAudio audio = audioWithBackend();
        audio.playBlockHit();
        audio.playBreak();
        audio.playPlace();
        audio.playPickup();
        audio.playStep();

        List<EchoClientSubtitleLine> lines = audio.subtitleLines(System.nanoTime(), true);
        require(lines.size() == EchoClientSubtitleOverlayPlan.MAX_LINES,
                "Subtitle overlay should retain only the newest capped line count");
        require(!containsText(lines, "Block Hit"),
                "Oldest subtitle line should be trimmed when the overlay is full");

        EchoClientSubtitleOverlayPlan plan = EchoClientSubtitleOverlayPlan.from(320, 240, lines);
        require(plan.visible(),
                "Subtitle overlay plan should be visible for active subtitle lines");
        require(plan.lines().size() == EchoClientSubtitleOverlayPlan.MAX_LINES,
                "Subtitle overlay plan should preserve the capped active subtitle lines");
        require(plan.x() >= 12 && plan.y() >= 12,
                "Subtitle overlay should stay inside the safe screen margin");
        require(plan.x() + plan.width() <= 320,
                "Subtitle overlay should fit the logical screen width");
        require(plan.y() + plan.height() <= 240,
                "Subtitle overlay should fit the logical screen height");
    }

    private static EchoClientAudio audioWithBackend() {
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(new EchoRecordingAudioBackend());
        return audio;
    }

    private static boolean containsText(List<EchoClientSubtitleLine> lines, String text) {
        return lines.stream().anyMatch(line -> line.text().equals(text));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
