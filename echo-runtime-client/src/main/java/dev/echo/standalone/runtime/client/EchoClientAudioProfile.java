package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioDeviceSettings;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfile;

import java.util.Map;

record EchoClientAudioProfile(
        EchoAudioDeviceSettings deviceSettings,
        EchoAudioVolumeProfile volumeProfile
) {
    EchoClientAudioProfile {
        deviceSettings = deviceSettings == null ? defaultDeviceSettings() : deviceSettings;
        volumeProfile = volumeProfile == null ? defaultVolumeProfile() : volumeProfile;
    }

    static EchoClientAudioProfile genericDefault() {
        return new EchoClientAudioProfile(
                defaultDeviceSettings(),
                defaultVolumeProfile()
        );
    }

    private static EchoAudioDeviceSettings defaultDeviceSettings() {
        return new EchoAudioDeviceSettings(true, false, 22050.0F, 16, 1, 12, "");
    }

    private static EchoAudioVolumeProfile defaultVolumeProfile() {
        return new EchoAudioVolumeProfile(
                        "client-default-volume",
                        Map.of(
                                EchoAudioBus.MASTER, 0.80D,
                                EchoAudioBus.MUSIC, 0.55D,
                                EchoAudioBus.AMBIENCE, 0.70D,
                                EchoAudioBus.SFX, 0.82D,
                                EchoAudioBus.UI, 0.90D,
                                EchoAudioBus.STINGER, 0.85D,
                                EchoAudioBus.ALERT, 0.92D,
                                EchoAudioBus.DIAGNOSTIC, 1.00D
                        ),
                        false
        );
    }

}
