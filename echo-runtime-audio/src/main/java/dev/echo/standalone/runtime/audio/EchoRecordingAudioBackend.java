package dev.echo.standalone.runtime.audio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRecordingAudioBackend implements EchoAudioBackend {
    private final ArrayList<EchoAudioPlaybackEvent> events = new ArrayList<>();
    private final ArrayList<EchoAudioDiagnostic> diagnostics = new ArrayList<>();
    private boolean closed;

    public EchoRecordingAudioBackend() {
        diagnostics.add(new EchoAudioDiagnostic(
                EchoAudioDiagnosticSeverity.INFO,
                "recording backend initialized without audio device"
        ));
    }

    @Override
    public String backendId() {
        return "echo:recording_audio";
    }

    @Override
    public boolean deviceOpen() {
        return false;
    }

    @Override
    public synchronized EchoAudioPlaybackEvent submit(
            EchoAudioPlaybackRequest request,
            EchoAudioVolumeProfile profile
    ) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(profile, "profile");
        if (closed) {
            throw new IllegalStateException("Recording audio backend is closed");
        }
        EchoAudioPlaybackEvent event = new EchoAudioPlaybackEvent(
                String.format(java.util.Locale.ROOT, "audio-event-%03d", events.size() + 1),
                request.action(),
                request.clip(),
                request.clip().bus(),
                profile.gainFor(request.clip().bus(), request.clip().baseGain()),
                request.reason(),
                request.tick()
        );
        events.add(event);
        diagnostics.add(new EchoAudioDiagnostic(
                EchoAudioDiagnosticSeverity.INFO,
                "recorded " + event.action().name() + " " + event.clip().clipId()
        ));
        return event;
    }

    @Override
    public synchronized List<EchoAudioPlaybackEvent> events() {
        return List.copyOf(events);
    }

    @Override
    public synchronized List<EchoAudioDiagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }

    @Override
    public synchronized void close() {
        closed = true;
        diagnostics.add(new EchoAudioDiagnostic(EchoAudioDiagnosticSeverity.INFO, "recording backend closed"));
    }
}
