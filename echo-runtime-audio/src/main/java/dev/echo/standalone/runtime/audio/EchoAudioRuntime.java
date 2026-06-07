package dev.echo.standalone.runtime.audio;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;
import java.util.Objects;

public final class EchoAudioRuntime {
    public EchoAudioRuntimeResult createDebugAudio(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoGameplayRuntimeResult gameplay,
            String screenId,
            EchoAudioVolumeProfile volumeProfile
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(gameplay, "gameplay");
        String resolvedScreenId = EchoAudioText.requireText(screenId, "screenId");
        Objects.requireNonNull(volumeProfile, "volumeProfile");

        return createDeviceDebugAudio(
                services,
                world,
                gameplay,
                resolvedScreenId,
                volumeProfile,
                EchoAudioDeviceProfiles.resolve(EchoAudioDeviceProfiles.STANDALONE_DEFAULT_PROFILE_ID)
        );
    }

    public EchoAudioRuntimeResult createDeviceDebugAudio(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoGameplayRuntimeResult gameplay,
            String screenId,
            EchoAudioVolumeProfile volumeProfile,
            EchoAudioDeviceSettings deviceSettings
    ) {
        return createAudio(
                services,
                world,
                gameplay,
                screenId,
                volumeProfile,
                new EchoJavaSoundAudioBackend(deviceSettings)
        );
    }

    public EchoAudioRuntimeResult createRecordingDebugAudio(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoGameplayRuntimeResult gameplay,
            String screenId,
            EchoAudioVolumeProfile volumeProfile
    ) {
        return createAudio(
                services,
                world,
                gameplay,
                screenId,
                volumeProfile,
                new EchoRecordingAudioBackend()
        );
    }

    public EchoAudioRuntimeResult createAudio(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoGameplayRuntimeResult gameplay,
            String screenId,
            EchoAudioVolumeProfile volumeProfile,
            EchoAudioBackend backend
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(gameplay, "gameplay");
        String resolvedScreenId = EchoAudioText.requireText(screenId, "screenId");
        Objects.requireNonNull(volumeProfile, "volumeProfile");
        Objects.requireNonNull(backend, "backend");

        EchoAudioClipRegistry registry = new EchoAudioClipRegistry();
        registerDebugClips(registry);
        EchoAudioMixer mixer = new EchoAudioMixer(backend, volumeProfile);
        EchoAudioCuePlanner cuePlanner = new EchoAudioCuePlanner(registry);
        EchoAudioCuePlan cuePlan = cuePlanner.initialAshfallLoop(world, gameplay, resolvedScreenId);
        List<EchoAudioPlaybackEvent> events = cuePlan.requests().stream()
                .map(mixer::submit)
                .toList();

        EchoAudioRuntimeResult result = new EchoAudioRuntimeResult(
                backend,
                registry,
                volumeProfile,
                mixer,
                cuePlanner,
                cuePlan,
                events
        );
        services.register(EchoAudioRuntimeResult.class, result);
        services.register(EchoAudioBackend.class, backend);
        services.register(EchoAudioClipRegistry.class, registry);
        services.register(EchoAudioVolumeProfile.class, volumeProfile);
        services.register(EchoAudioMixer.class, mixer);
        services.register(EchoAudioCuePlanner.class, cuePlanner);
        services.register(EchoAudioCuePlan.class, cuePlan);
        return result;
    }

    private static void registerDebugClips(EchoAudioClipRegistry registry) {
        registry.register(new EchoAudioClip(
                "ashfall:ambience_ash_storm",
                "Ash Storm Ambience",
                "ashfall:sounds/ambience/ash_storm.ogg",
                EchoAudioClipType.AMBIENCE,
                EchoAudioBus.AMBIENCE,
                true,
                0.65D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:music_survival_pulse",
                "Survival Pulse",
                "ashfall:sounds/music/survival_pulse.ogg",
                EchoAudioClipType.MUSIC,
                EchoAudioBus.MUSIC,
                true,
                0.60D
        ));
        registry.register(new EchoAudioClip(
                "echo:ui_terminal_blip",
                "Terminal Blip",
                "echo:sounds/ui/terminal_blip.ogg",
                EchoAudioClipType.UI_SOUND,
                EchoAudioBus.UI,
                false,
                0.50D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:radio_static",
                "Radio Static",
                "echoashfallprotocol:sounds/ui/echo_message.ogg",
                EchoAudioClipType.UI_SOUND,
                EchoAudioBus.UI,
                false,
                0.52D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:block_mining_hit",
                "Mining Hit",
                "ashfall:sounds/gameplay/block_mining_hit.ogg",
                EchoAudioClipType.GAMEPLAY_FX,
                EchoAudioBus.SFX,
                false,
                0.48D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:block_break",
                "Block Break",
                "ashfall:sounds/gameplay/block_break.ogg",
                EchoAudioClipType.GAMEPLAY_FX,
                EchoAudioBus.SFX,
                false,
                0.56D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:item_pickup",
                "Item Pickup",
                "ashfall:sounds/gameplay/item_pickup.ogg",
                EchoAudioClipType.GAMEPLAY_FX,
                EchoAudioBus.SFX,
                false,
                0.46D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:consume_water",
                "Drink Water",
                "ashfall:sounds/gameplay/consume_water.ogg",
                EchoAudioClipType.GAMEPLAY_FX,
                EchoAudioBus.SFX,
                false,
                0.44D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:consume_food",
                "Eat Ration",
                "ashfall:sounds/gameplay/consume_food.ogg",
                EchoAudioClipType.GAMEPLAY_FX,
                EchoAudioBus.SFX,
                false,
                0.42D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:power_repair",
                "Power Repair",
                "ashfall:sounds/stingers/power_repair.ogg",
                EchoAudioClipType.MISSION_STINGER,
                EchoAudioBus.STINGER,
                false,
                0.74D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:extraction_beacon",
                "Extraction Beacon",
                "ashfall:sounds/stingers/extraction_beacon.ogg",
                EchoAudioClipType.MISSION_STINGER,
                EchoAudioBus.STINGER,
                true,
                0.68D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:danger_alert",
                "Danger Alert",
                "ashfall:sounds/alerts/danger_alert.ogg",
                EchoAudioClipType.ALERT,
                EchoAudioBus.ALERT,
                false,
                0.80D
        ));
        registry.register(new EchoAudioClip(
                "ashfall:mission_secure_stinger",
                "Secure Mission Stinger",
                "ashfall:sounds/stingers/secure_mission.ogg",
                EchoAudioClipType.MISSION_STINGER,
                EchoAudioBus.STINGER,
                false,
                0.75D
        ));
    }
}
