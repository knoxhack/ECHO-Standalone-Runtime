package dev.echo.standalone.runtime.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoJavaSoundAudioBackend implements EchoAudioBackend {
    private final EchoAudioDeviceSettings settings;
    private final ArrayList<EchoAudioPlaybackEvent> events = new ArrayList<>();
    private final ArrayList<EchoAudioDiagnostic> diagnostics = new ArrayList<>();
    private EchoRecordingAudioBackend fallback;
    private SourceDataLine line;
    private boolean fallbackActive;
    private boolean closed;

    public EchoJavaSoundAudioBackend(EchoAudioDeviceSettings settings) {
        this.settings = Objects.requireNonNull(settings, "settings");
        openDevice();
    }

    @Override
    public String backendId() {
        return "echo:java_sound_audio";
    }

    @Override
    public synchronized boolean deviceOpen() {
        return line != null && line.isOpen() && !fallbackActive;
    }

    public synchronized boolean fallbackActive() {
        return fallbackActive;
    }

    public EchoAudioDeviceSettings settings() {
        return settings;
    }

    @Override
    public synchronized EchoAudioPlaybackEvent submit(
            EchoAudioPlaybackRequest request,
            EchoAudioVolumeProfile profile
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(profile, "profile");
        if (closed) {
            throw new IllegalStateException("Java Sound audio backend is closed");
        }
        EchoAudioPlaybackEvent event = new EchoAudioPlaybackEvent(
                String.format(java.util.Locale.ROOT, "device-audio-event-%03d", events.size() + 1),
                request.action(),
                request.clip(),
                request.clip().bus(),
                profile.gainFor(request.clip().bus(), request.clip().baseGain()),
                request.reason(),
                request.tick()
        );
        events.add(event);
        if (deviceOpen() && event.effectiveGain() > 0.0D && event.action() != EchoAudioPlaybackAction.STOP) {
            byte[] pcm = EchoAudioPcmSynthesizer.synthesize(event, settings);
            line.write(pcm, 0, pcm.length);
            diagnostics.add(new EchoAudioDiagnostic(
                    EchoAudioDiagnosticSeverity.INFO,
                    "device wrote " + pcm.length + " PCM bytes for " + event.clip().clipId()
            ));
        } else if (fallbackActive) {
            fallback().submit(request, profile);
        } else {
            diagnostics.add(new EchoAudioDiagnostic(
                    EchoAudioDiagnosticSeverity.INFO,
                    "device skipped " + event.clip().clipId() + " gain=" + event.effectiveGain()
            ));
        }
        return event;
    }

    @Override
    public synchronized List<EchoAudioPlaybackEvent> events() {
        return List.copyOf(events);
    }

    @Override
    public synchronized List<EchoAudioDiagnostic> diagnostics() {
        ArrayList<EchoAudioDiagnostic> snapshot = new ArrayList<>(diagnostics);
        if (fallback != null) {
            snapshot.addAll(fallback.diagnostics());
        }
        return List.copyOf(snapshot);
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        if (line != null) {
            line.flush();
            line.stop();
            line.close();
        }
        if (fallback != null) {
            fallback.close();
        }
        closed = true;
        diagnostics.add(new EchoAudioDiagnostic(EchoAudioDiagnosticSeverity.INFO, "java sound backend closed"));
    }

    private void openDevice() {
        if (!settings.deviceEnabled()) {
            activateFallback(EchoAudioDiagnosticSeverity.INFO, "audio device disabled by settings");
            return;
        }
        if (settings.forceDeviceFailure()) {
            activateFallback(EchoAudioDiagnosticSeverity.WARNING, "forced audio device failure");
            return;
        }
        AudioFormat format = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                settings.sampleRate(),
                settings.sampleSizeBits(),
                settings.channels(),
                settings.channels() * 2,
                settings.sampleRate(),
                false
        );
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
            SourceDataLine openedLine = (SourceDataLine) AudioSystem.getLine(info);
            int bufferSize = Math.max(256, Math.round(settings.sampleRate() * settings.channels() * 2 * 0.05F));
            openedLine.open(format, bufferSize);
            openedLine.start();
            line = openedLine;
            diagnostics.add(new EchoAudioDiagnostic(
                    EchoAudioDiagnosticSeverity.INFO,
                    "java sound device opened " + openedLine.getLineInfo()
            ));
        } catch (LineUnavailableException exception) {
            activateFallback(EchoAudioDiagnosticSeverity.WARNING, "audio device unavailable: " + exception.getClass().getSimpleName());
        } catch (RuntimeException exception) {
            activateFallback(EchoAudioDiagnosticSeverity.WARNING, "audio device failed: " + exception.getClass().getSimpleName());
        }
    }

    private void activateFallback(EchoAudioDiagnosticSeverity severity, String reason) {
        fallbackActive = true;
        line = null;
        fallback = new EchoRecordingAudioBackend();
        diagnostics.add(new EchoAudioDiagnostic(severity, reason + "; recording fallback active"));
    }

    private EchoRecordingAudioBackend fallback() {
        if (fallback == null) {
            fallback = new EchoRecordingAudioBackend();
        }
        return fallback;
    }
}
