package dev.echo.standalone.runtime.audio;

import java.util.List;

public interface EchoAudioBackend {
    String backendId();

    boolean deviceOpen();

    EchoAudioPlaybackEvent submit(EchoAudioPlaybackRequest request, EchoAudioVolumeProfile profile);

    List<EchoAudioPlaybackEvent> events();

    List<EchoAudioDiagnostic> diagnostics();

    void close();
}
