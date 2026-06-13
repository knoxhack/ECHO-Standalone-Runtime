package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lightweight audio manager for the standalone client.
 * Hooks into the existing {@link EchoAudioMixer} and plays simple feedback sounds.
 */
final class EchoClientAudio {
    private static final long SUBTITLE_DURATION_NANOS = 2_500_000_000L;

    private final EchoClientAudioProfile audioProfile;
    private EchoAudioMixer mixer;
    private EchoAudioBackend backend;
    private final EchoAudioClipRegistry registry = new EchoAudioClipRegistry();
    private final ArrayList<TrackedSubtitle> subtitles = new ArrayList<>();
    private EchoClientSettings settings = EchoClientSettings.defaults();
    private String currentAmbienceClipId = "";
    private String currentMusicClipId = "";

    EchoClientAudio() {
        this(EchoClientWorldTemplates.defaultTemplate().audioProfile());
    }

    EchoClientAudio(EchoClientAudioProfile audioProfile) {
        this.audioProfile = audioProfile == null ? EchoClientAudioProfile.genericDefault() : audioProfile;
    }

    void init() {
        try {
            init(new EchoJavaSoundAudioBackend(audioProfile.deviceSettings()));
        } catch (Exception e) {
            System.err.println("[echo-client] audio init failed: " + e.getMessage());
            mixer = null;
        }
    }

    void init(EchoAudioBackend backend) {
        closeBackend();
        this.backend = backend;
        mixer = new EchoAudioMixer(backend, volumeProfile(this.settings, audioProfile));
        registerClientClips();
    }

    void applySettings(EchoClientSettings settings) {
        this.settings = settings == null ? EchoClientSettings.defaults() : settings;
        if (!this.settings.subtitles()) {
            subtitles.clear();
        }
        if (mixer != null) {
            mixer.setProfile(volumeProfile(this.settings, audioProfile));
        }
    }

    boolean applyBiomeEnvironment(EchoClientBiomeEnvironment environment, long tick) {
        if (mixer == null || environment == null) {
            return false;
        }
        String nextClipId = environment.ambienceClipId();
        if (nextClipId.equals(currentAmbienceClipId)) {
            return false;
        }
        if (!currentAmbienceClipId.isBlank()) {
            EchoAudioClip previous = registry.find(currentAmbienceClipId).orElse(null);
            if (previous != null) {
                mixer.submit(new EchoAudioPlaybackRequest(
                        "echo:ambience-stop:" + tick,
                        EchoAudioPlaybackAction.STOP,
                        previous,
                        "biome=" + environment.biomeId(),
                        tick
                ));
            }
        }
        EchoAudioClip next = registry.find(nextClipId).orElse(null);
        if (next == null) {
            return false;
        }
        currentAmbienceClipId = nextClipId;
        mixer.submit(new EchoAudioPlaybackRequest(
                "echo:ambience-loop:" + nextClipId + ":" + tick,
                EchoAudioPlaybackAction.LOOP,
                next,
                "biome=" + environment.biomeId(),
                tick
        ));
        return true;
    }

    boolean applyMusicMode(EchoClientMusicMode mode, long tick) {
        if (mixer == null) {
            return false;
        }
        EchoClientMusicMode nextMode = mode == null ? EchoClientMusicMode.SILENT : mode;
        String nextClipId = nextMode.clipId();
        if (nextClipId.equals(currentMusicClipId)) {
            return false;
        }
        boolean changed = stopCurrentMusic(tick, "mode=" + nextMode.name().toLowerCase(java.util.Locale.ROOT));
        if (nextClipId.isBlank()) {
            return changed;
        }
        EchoAudioClip next = registry.find(nextClipId).orElse(null);
        if (next == null) {
            return changed;
        }
        currentMusicClipId = nextClipId;
        mixer.submit(new EchoAudioPlaybackRequest(
                "echo:music-loop:" + nextClipId + ":" + tick,
                EchoAudioPlaybackAction.LOOP,
                next,
                "mode=" + nextMode.name().toLowerCase(java.util.Locale.ROOT),
                tick
        ));
        return true;
    }

    void playBreak() {
        play("echo:client_break");
    }

    void playBlockHit() {
        play("echo:client_block_hit");
    }

    void playPlace() {
        play("echo:client_place");
    }

    void playEat() {
        play("echo:client_eat");
    }

    void playPickup() {
        play("echo:client_pickup");
    }

    void playUiClick() {
        play("echo:client_ui_click");
    }

    void playStep() {
        play("echo:client_step");
    }

    private void play(String clipId) {
        if (mixer == null) return;
        try {
            EchoAudioClip clip = registry.find(clipId).orElse(null);
            if (clip != null) {
                long now = System.nanoTime();
                mixer.submit(new EchoAudioPlaybackRequest(
                        clipId + ":" + now,
                        EchoAudioPlaybackAction.PLAY,
                        clip,
                        "client",
                        0L
                ));
                recordSubtitle(clip, now);
            }
        } catch (Exception ignored) {}
    }

    void close() {
        stopCurrentMusic(0L, "client=close");
        closeBackend();
    }

    String currentAmbienceClipId() {
        return currentAmbienceClipId;
    }

    String currentMusicClipId() {
        return currentMusicClipId;
    }

    List<EchoClientSubtitleLine> subtitleLines() {
        return subtitleLines(System.nanoTime(), settings.subtitles());
    }

    List<EchoClientSubtitleLine> subtitleLines(long nowNanos, boolean enabled) {
        long now = Math.max(0L, nowNanos);
        pruneSubtitles(now);
        if (!enabled || subtitles.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientSubtitleLine> lines = new ArrayList<>(subtitles.size());
        for (TrackedSubtitle subtitle : subtitles) {
            lines.add(new EchoClientSubtitleLine(
                        subtitle.clipId(),
                        subtitle.text(),
                        Math.max(0.0D, (subtitle.expiresAtNanos() - now) / 1_000_000_000.0D)
            ));
        }
        return List.copyOf(lines);
    }

    EchoClientAudioDiagnosticsSnapshot diagnosticsSnapshot() {
        EchoAudioBackend currentBackend = backend;
        if (currentBackend == null || mixer == null) {
            return new EchoClientAudioDiagnosticsSnapshot(
                    false,
                    "",
                    false,
                    false,
                    0,
                    0,
                    0,
                    0,
                    settings.masterVolumePercent(),
                    settings.musicVolumePercent(),
                    settings.ambienceVolumePercent(),
                    settings.subtitles(),
                    subtitleLines().size(),
                    currentAmbienceClipId,
                    currentMusicClipId,
                    ""
            );
        }
        List<EchoAudioDiagnostic> diagnostics = currentBackend.diagnostics();
        String latestDiagnostic = diagnostics.isEmpty()
                ? ""
                : diagnostics.get(diagnostics.size() - 1).message();
        return new EchoClientAudioDiagnosticsSnapshot(
                true,
                currentBackend.backendId(),
                currentBackend.deviceOpen(),
                currentBackend instanceof EchoJavaSoundAudioBackend javaSound && javaSound.fallbackActive(),
                currentBackend.events().size(),
                diagnostics.size(),
                diagnosticCount(diagnostics, EchoAudioDiagnosticSeverity.WARNING),
                diagnosticCount(diagnostics, EchoAudioDiagnosticSeverity.ERROR),
                settings.masterVolumePercent(),
                settings.musicVolumePercent(),
                settings.ambienceVolumePercent(),
                settings.subtitles(),
                subtitleLines().size(),
                currentAmbienceClipId,
                currentMusicClipId,
                latestDiagnostic
        );
    }

    static EchoAudioVolumeProfile volumeProfile(EchoClientSettings settings) {
        return volumeProfile(settings, EchoClientWorldTemplates.defaultTemplate().audioProfile());
    }

    static EchoAudioVolumeProfile volumeProfile(EchoClientSettings settings, EchoClientAudioProfile audioProfile) {
        EchoClientSettings next = settings == null ? EchoClientSettings.defaults() : settings;
        EchoClientAudioProfile safeAudioProfile =
                audioProfile == null ? EchoClientAudioProfile.genericDefault() : audioProfile;
        return safeAudioProfile.volumeProfile()
                .withMasterVolume(percent(next.masterVolumePercent()))
                .withBusVolume(EchoAudioBus.MUSIC, percent(next.musicVolumePercent()))
                .withBusVolume(EchoAudioBus.AMBIENCE, percent(next.ambienceVolumePercent()));
    }

    private static double percent(int value) {
        return Math.max(0, Math.min(100, value)) / 100.0D;
    }

    private void registerClientClips() {
        registry.register(new EchoAudioClip(
                "echo:client_break", "Block Break", "echo:sfx/break",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.SFX, false, 0.6
        ));
        registry.register(new EchoAudioClip(
                "echo:client_block_hit", "Block Hit", "echo:sfx/block_hit",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.SFX, false, 0.38
        ));
        registry.register(new EchoAudioClip(
                "echo:client_place", "Block Place", "echo:sfx/place",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.SFX, false, 0.5
        ));
        registry.register(new EchoAudioClip(
                "echo:client_eat", "Consume Item", "echo:sfx/eat",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.SFX, false, 0.4
        ));
        registry.register(new EchoAudioClip(
                "echo:client_pickup", "Item Pickup", "echo:sfx/pickup",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.SFX, false, 0.35
        ));
        registry.register(new EchoAudioClip(
                "echo:client_ui_click", "UI Click", "echo:sfx/ui_click",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.UI, false, 0.32
        ));
        registry.register(new EchoAudioClip(
                "echo:client_step", "Footstep", "echo:sfx/step",
                EchoAudioClipType.GAMEPLAY_FX, EchoAudioBus.SFX, false, 0.25
        ));
        registerAmbienceClip("echo:ambience_ash_wasteland", "Ash Wasteland Ambience", "echo:ambience/ash_wasteland", 0.44);
        registerAmbienceClip("echo:ambience_toxic_swamp", "Toxic Swamp Ambience", "echo:ambience/toxic_swamp", 0.50);
        registerAmbienceClip("echo:ambience_radiation", "Radiation Ambience", "echo:ambience/radiation", 0.48);
        registerAmbienceClip("echo:ambience_ruins", "Ruins Ambience", "echo:ambience/ruins", 0.42);
        registerAmbienceClip("echo:ambience_cryogenic", "Cryogenic Ambience", "echo:ambience/cryogenic", 0.40);
        registerAmbienceClip("echo:ambience_nexus", "Nexus Ambience", "echo:ambience/nexus", 0.46);
        registerMusicClip("echo:music_menu", "Ashfall Menu Music", "echo:music/menu_theme", 0.38);
        registerMusicClip("echo:music_survival", "Ashfall Survival Pulse", "echo:music/survival_pulse", 0.34);
    }

    private void recordSubtitle(EchoAudioClip clip, long nowNanos) {
        if (!settings.subtitles() || clip == null || !subtitleEligible(clip)) {
            return;
        }
        pruneSubtitles(nowNanos);
        subtitles.removeIf(subtitle -> subtitle.clipId().equals(clip.clipId()));
        subtitles.add(0, new TrackedSubtitle(
                clip.clipId(),
                clip.displayName(),
                nowNanos + SUBTITLE_DURATION_NANOS
        ));
        while (subtitles.size() > EchoClientSubtitleOverlayPlan.MAX_LINES) {
            subtitles.remove(subtitles.size() - 1);
        }
    }

    private void pruneSubtitles(long nowNanos) {
        Iterator<TrackedSubtitle> iterator = subtitles.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().expiresAtNanos() <= nowNanos) {
                iterator.remove();
            }
        }
    }

    private static boolean subtitleEligible(EchoAudioClip clip) {
        return clip.type() == EchoAudioClipType.GAMEPLAY_FX && !clip.looping();
    }

    private static int diagnosticCount(
            List<EchoAudioDiagnostic> diagnostics,
            EchoAudioDiagnosticSeverity severity
    ) {
        int count = 0;
        for (EchoAudioDiagnostic diagnostic : diagnostics) {
            if (diagnostic != null && diagnostic.severity() == severity) {
                count++;
            }
        }
        return count;
    }

    private void closeBackend() {
        if (backend != null) {
            backend.close();
        }
        mixer = null;
        backend = null;
    }

    private void registerAmbienceClip(String clipId, String displayName, String assetKey, double gain) {
        registry.register(new EchoAudioClip(
                clipId,
                displayName,
                assetKey,
                EchoAudioClipType.AMBIENCE,
                EchoAudioBus.AMBIENCE,
                true,
                gain
        ));
    }

    private boolean stopCurrentMusic(long tick, String reason) {
        if (mixer == null || currentMusicClipId.isBlank()) {
            return false;
        }
        EchoAudioClip previous = registry.find(currentMusicClipId).orElse(null);
        currentMusicClipId = "";
        if (previous == null) {
            return false;
        }
        mixer.submit(new EchoAudioPlaybackRequest(
                "echo:music-stop:" + previous.clipId() + ":" + tick,
                EchoAudioPlaybackAction.STOP,
                previous,
                reason == null || reason.isBlank() ? "music=stop" : reason,
                tick
        ));
        return true;
    }

    private void registerMusicClip(String clipId, String displayName, String assetKey, double gain) {
        registry.register(new EchoAudioClip(
                clipId,
                displayName,
                assetKey,
                EchoAudioClipType.MUSIC,
                EchoAudioBus.MUSIC,
                true,
                gain
        ));
    }

    private record TrackedSubtitle(
            String clipId,
            String text,
            long expiresAtNanos
    ) {
    }
}
