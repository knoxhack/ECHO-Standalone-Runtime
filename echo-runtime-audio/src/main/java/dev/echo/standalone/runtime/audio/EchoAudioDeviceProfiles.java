package dev.echo.standalone.runtime.audio;

import java.util.Locale;

public final class EchoAudioDeviceProfiles {
    public static final String STANDALONE_DEFAULT_PROFILE_ID = "echoashfallprotocol:audio/device/standalone_default";

    private EchoAudioDeviceProfiles() {
    }

    public static EchoAudioDeviceSettings resolve(String profileId) {
        String normalized = profileId == null ? "" : profileId.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(STANDALONE_DEFAULT_PROFILE_ID)
                || normalized.equals("echoashfallprotocol:ashfall_default")
                || normalized.equals("ashfall_default")) {
            return ashfallDefault();
        }
        return new EchoAudioDeviceSettings(true, false, 22050.0F, 16, 1, 12, "");
    }

    public static EchoAudioDeviceSettings ashfallDefault() {
        return new EchoAudioDeviceSettings(true, false, 22050.0F, 16, 1, 12, "");
    }
}
