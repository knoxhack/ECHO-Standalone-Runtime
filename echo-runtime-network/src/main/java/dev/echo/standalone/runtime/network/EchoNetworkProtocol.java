package dev.echo.standalone.runtime.network;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record EchoNetworkProtocol(
        String protocolId,
        int version,
        int minCompatibleVersion,
        List<String> features
) {
    public EchoNetworkProtocol {
        protocolId = EchoNetworkText.requireText(protocolId, "protocolId");
        if (version <= 0) {
            throw new IllegalArgumentException("version must be positive");
        }
        if (minCompatibleVersion <= 0) {
            throw new IllegalArgumentException("minCompatibleVersion must be positive");
        }
        if (minCompatibleVersion > version) {
            throw new IllegalArgumentException("minCompatibleVersion must not exceed version");
        }
        Objects.requireNonNull(features, "features");
        ArrayList<String> normalized = new ArrayList<>();
        for (String feature : features) {
            String normalizedFeature = EchoNetworkText.requireText(feature, "feature");
            if (!normalized.contains(normalizedFeature)) {
                normalized.add(normalizedFeature);
            }
        }
        features = List.copyOf(normalized);
    }

    public boolean compatibleWith(EchoNetworkProtocol peer) {
        Objects.requireNonNull(peer, "peer");
        return protocolId.equals(peer.protocolId())
                && version >= peer.minCompatibleVersion()
                && peer.version() >= minCompatibleVersion;
    }

    public String featureCsv() {
        return String.join(",", features);
    }
}
