package dev.echo.standalone.runtime.audio;

import java.util.List;
import java.util.Objects;

public record EchoAudioCuePlan(List<EchoAudioPlaybackRequest> requests) {
    public EchoAudioCuePlan {
        Objects.requireNonNull(requests, "requests");
        requests = List.copyOf(requests);
    }

    public int size() {
        return requests.size();
    }
}
