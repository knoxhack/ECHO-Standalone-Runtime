package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.List;

record EchoClientAudioDiagnosticsSnapshot(
        boolean initialized,
        String backendId,
        boolean deviceOpen,
        boolean fallbackActive,
        int eventCount,
        int diagnosticCount,
        int warningCount,
        int errorCount,
        int masterVolumePercent,
        int musicVolumePercent,
        int ambienceVolumePercent,
        boolean subtitlesEnabled,
        int activeSubtitleCount,
        String currentAmbienceClipId,
        String currentMusicClipId,
        String latestDiagnostic
) {
    static final EchoClientAudioDiagnosticsSnapshot EMPTY = new EchoClientAudioDiagnosticsSnapshot(
            false,
            "",
            false,
            false,
            0,
            0,
            0,
            0,
            EchoClientSettings.defaults().masterVolumePercent(),
            EchoClientSettings.defaults().musicVolumePercent(),
            EchoClientSettings.defaults().ambienceVolumePercent(),
            EchoClientSettings.defaults().subtitles(),
            0,
            "",
            "",
            ""
    );

    EchoClientAudioDiagnosticsSnapshot {
        backendId = clean(backendId);
        eventCount = Math.max(0, eventCount);
        diagnosticCount = Math.max(0, diagnosticCount);
        warningCount = Math.max(0, warningCount);
        errorCount = Math.max(0, errorCount);
        masterVolumePercent = clampPercent(masterVolumePercent);
        musicVolumePercent = clampPercent(musicVolumePercent);
        ambienceVolumePercent = clampPercent(ambienceVolumePercent);
        activeSubtitleCount = Math.max(0, activeSubtitleCount);
        currentAmbienceClipId = clean(currentAmbienceClipId);
        currentMusicClipId = clean(currentMusicClipId);
        latestDiagnostic = clean(latestDiagnostic);
        initialized = initialized && !backendId.isBlank();
    }

    List<String> lines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Audio: Backend " + (initialized ? backendId : "not initialized")
                + " Device " + deviceLabel()
                + " Events " + eventCount
                + " Diagnostics " + diagnosticCount
                + " Warnings " + warningCount
                + " Errors " + errorCount);
        lines.add("Audio Mix: Master " + masterVolumePercent + "%"
                + " Music " + musicVolumePercent + "%"
                + " Ambience " + ambienceVolumePercent + "%"
                + " Subtitles " + (subtitlesEnabled ? "ON" : "OFF")
                + " Active " + activeSubtitleCount);
        lines.add("Audio Cue: Music " + clipLabel(currentMusicClipId)
                + " Ambience " + clipLabel(currentAmbienceClipId));
        if (!latestDiagnostic.isBlank()) {
            lines.add("Audio Diagnostic: " + compact(latestDiagnostic, 84));
        }
        return List.copyOf(lines);
    }

    String deviceLabel() {
        if (!initialized) {
            return "UNINITIALIZED";
        }
        if (deviceOpen) {
            return "OPEN";
        }
        return fallbackActive ? "FALLBACK" : "CLOSED";
    }

    private static String clipLabel(String clipId) {
        return clipId == null || clipId.isBlank() ? "none" : compact(clipId, 42);
    }

    private static String compact(String value, int maxLength) {
        String safe = clean(value);
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, Math.max(0, maxLength));
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replace('\r', ' ').replace('\n', ' ');
    }
}
