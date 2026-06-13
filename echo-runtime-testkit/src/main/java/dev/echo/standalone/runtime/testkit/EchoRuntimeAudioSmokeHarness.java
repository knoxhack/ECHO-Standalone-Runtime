package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.audio.EchoAudioBackend;
import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioClipRegistry;
import dev.echo.standalone.runtime.audio.EchoAudioCuePlan;
import dev.echo.standalone.runtime.audio.EchoAudioCuePlanner;
import dev.echo.standalone.runtime.audio.EchoAudioDeviceSettings;
import dev.echo.standalone.runtime.audio.EchoAudioMixer;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackEvent;
import dev.echo.standalone.runtime.audio.EchoAudioRuntime;
import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfile;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfiles;
import dev.echo.standalone.runtime.audio.EchoJavaSoundAudioBackend;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class EchoRuntimeAudioSmokeHarness {
    private EchoRuntimeAudioSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoStaticScreen(
                        "ashfall-terminal",
                        "Ashfall Terminal",
                        List.of("Mission uplink active", "Storm pressure rising"),
                        "terminal:input"
                ),
                EchoUiTheme.defaultTerminal()
        );
        EchoAudioRuntimeResult audio = new EchoAudioRuntime().createDebugAudio(
                services,
                world,
                gameplay,
                ui.frame().screen().id(),
                EchoAudioVolumeProfiles.resolve(EchoAudioVolumeProfiles.ASHFALL_SURVIVAL_MIX_PROFILE_ID)
        );

        require(services.require(EchoAudioRuntimeResult.class) == audio,
                "audio runtime result should be service-bound");
        require(services.require(EchoAudioBackend.class) == audio.backend(),
                "audio backend should be service-bound");
        require(services.require(EchoAudioClipRegistry.class) == audio.clipRegistry(),
                "audio clip registry should be service-bound");
        require(services.require(EchoAudioVolumeProfile.class) == audio.volumeProfile(),
                "audio volume profile should be service-bound");
        require(services.require(EchoAudioMixer.class) == audio.mixer(),
                "audio mixer should be service-bound");
        require(services.require(EchoAudioCuePlanner.class) == audio.cuePlanner(),
                "audio cue planner should be service-bound");
        require(services.require(EchoAudioCuePlan.class) == audio.initialCuePlan(),
                "audio cue plan should be service-bound");

        require(audio.backend().backendId().equals("echo:java_sound_audio"),
                "Java Sound audio backend should be used by default");
        require(audio.backend() instanceof EchoJavaSoundAudioBackend,
                "default backend should expose device/fallback state");
        EchoJavaSoundAudioBackend deviceBackend = (EchoJavaSoundAudioBackend) audio.backend();
        require(audio.backend().deviceOpen() || deviceBackend.fallbackActive(),
                "audio backend should either open a device or activate recording fallback");
        require(audio.clipRegistry().count() == 13, "debug audio registry should contain startup and gameplay clips");
        require(audio.clipRegistry().byBus(EchoAudioBus.AMBIENCE).size() == 1,
                "ambience bus should contain one clip");
        require(audio.clipRegistry().byBus(EchoAudioBus.MUSIC).size() == 1,
                "music bus should contain one clip");
        require(audio.clipRegistry().byBus(EchoAudioBus.SFX).size() == 5,
                "SFX bus should contain five gameplay clips");
        require(audio.clipRegistry().byBus(EchoAudioBus.UI).size() == 2,
                "UI bus should contain terminal blip and AdapterCore radio static clips");
        require(audio.clipRegistry().byBus(EchoAudioBus.STINGER).size() == 3,
                "stinger bus should contain mission, power, and extraction clips");
        require(audio.clipRegistry().byBus(EchoAudioBus.ALERT).size() == 1,
                "alert bus should contain one danger clip");
        require(audio.initialCuePlan().size() == 4, "initial Ashfall cue plan should contain four requests");
        require(audio.initialEvents().size() == 4, "device backend should return four initial events");
        require(audio.backend().events().size() == 4, "backend event log should contain four events");
        require(audio.backend().diagnostics().size() >= 5,
                "backend diagnostics should include initialization/device/fallback and four submitted events");
        boolean defaultDeviceOpenDuringRun = audio.backend().deviceOpen();
        boolean defaultFallbackActiveDuringRun = deviceBackend.fallbackActive();

        EchoAudioPlaybackEvent ambience = audio.backend().events().get(0);
        EchoAudioPlaybackEvent music = audio.backend().events().get(1);
        EchoAudioPlaybackEvent uiEvent = audio.backend().events().get(2);
        EchoAudioPlaybackEvent stinger = audio.backend().events().get(3);

        require(ambience.action() == EchoAudioPlaybackAction.LOOP,
                "ambience should be submitted as a loop");
        require(ambience.clip().clipId().equals("ashfall:ambience_ash_storm"),
                "ash storm ambience should be first cue");
        requireDouble(ambience.effectiveGain(), 0.3640D, "ambience gain should apply volume profile");
        require(music.action() == EchoAudioPlaybackAction.LOOP,
                "music should be submitted as a loop");
        require(music.clip().clipId().equals("ashfall:music_survival_pulse"),
                "survival pulse music should be second cue");
        requireDouble(music.effectiveGain(), 0.2640D, "music gain should apply volume profile");
        require(uiEvent.action() == EchoAudioPlaybackAction.PLAY,
                "UI sound should be one-shot");
        require(uiEvent.clip().clipId().equals("echo:ui_terminal_blip"),
                "terminal blip should be third cue");
        requireDouble(uiEvent.effectiveGain(), 0.3600D, "UI gain should apply volume profile");
        require(stinger.action() == EchoAudioPlaybackAction.PLAY,
                "mission stinger should be one-shot");
        require(stinger.clip().clipId().equals("ashfall:mission_secure_stinger"),
                "mission stinger should be fourth cue");
        requireDouble(stinger.effectiveGain(), 0.5100D, "stinger gain should apply volume profile");
        require(stinger.reason().equals("mission-status=ACTIVE"),
                "stinger cue should reflect active mission state");

        EchoAudioVolumeProfile mutedProfile = audio.volumeProfile().withMuted(true);
        audio.mixer().setProfile(mutedProfile);
        EchoAudioPlaybackEvent mutedEvent = audio.mixer().submit(audio.initialCuePlan().requests().getFirst());
        requireDouble(mutedEvent.effectiveGain(), 0.0D, "muted profile should force zero gain");
        EchoAudioVolumeProfile quietUi = audio.volumeProfile().withBusVolume(EchoAudioBus.UI, 0.25D);
        requireDouble(
                quietUi.gainFor(EchoAudioBus.UI, audio.clipRegistry().require("echo:ui_terminal_blip").baseGain()),
                0.1000D,
                "UI bus volume control should affect effective gain"
        );

        EchoAudioRuntimeResult forcedFallback = new EchoAudioRuntime().createDeviceDebugAudio(
                new EchoDefaultRuntimeServiceRegistry(),
                world,
                gameplay,
                ui.frame().screen().id(),
                EchoAudioVolumeProfiles.resolve(EchoAudioVolumeProfiles.ASHFALL_SURVIVAL_MIX_PROFILE_ID),
                EchoAudioDeviceSettings.forcedFallback()
        );
        EchoJavaSoundAudioBackend fallbackBackend = (EchoJavaSoundAudioBackend) forcedFallback.backend();
        require(fallbackBackend.fallbackActive(), "forced device failure should activate recording fallback");
        require(!forcedFallback.backend().deviceOpen(), "forced fallback should not leave a device open");
        require(forcedFallback.backend().events().size() == 4, "forced fallback should preserve event logging");
        boolean forcedFallbackActiveDuringRun = fallbackBackend.fallbackActive();
        forcedFallback.backend().close();
        require(forcedFallback.backend().diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.message().endsWith("backend closed")),
                "forced fallback backend should close cleanly");

        audio.backend().close();
        require(audio.backend().diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.message().endsWith("backend closed")),
                "default device backend should close cleanly");

        writeReports(
                audio,
                forcedFallback,
                defaultDeviceOpenDuringRun,
                defaultFallbackActiveDuringRun,
                forcedFallbackActiveDuringRun,
                mutedEvent,
                quietUi
        );

        System.out.println("phase15.6 audio device runtime smoke PASS backend="
                + audio.backend().backendId()
                + " clips="
                + audio.clipRegistry().count()
                + " buses=6"
                + " events="
                + audio.backend().events().size()
                + " diagnostics="
                + audio.backend().diagnostics().size()
                + " deviceOpen="
                + audio.backend().deviceOpen()
                + " fallback="
                + deviceBackend.fallbackActive());
    }

    private static void writeReports(
            EchoAudioRuntimeResult audio,
            EchoAudioRuntimeResult forcedFallback,
            boolean defaultDeviceOpenDuringRun,
            boolean defaultFallbackActiveDuringRun,
            boolean forcedFallbackActiveDuringRun,
            EchoAudioPlaybackEvent mutedEvent,
            EchoAudioVolumeProfile quietUi
    ) throws IOException {
        Path root = Path.of("reports", "echo", "standalone");
        Files.createDirectories(root);
        String busCounts = busCounts(audio);
        String initialEvents = events(audio.initialEvents());
        String gameplayClips = clips(audio.clipRegistry().all());
        String diagnostics = diagnostics(audio.backend().diagnostics());
        String forcedDiagnostics = diagnostics(forcedFallback.backend().diagnostics());
        String volumeProfile = volumeProfile(audio.volumeProfile());

        write(root.resolve("runtime-audio.json"), """
                {
                  "schema": "echo.standalone.runtime_audio.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "summary": "Standalone audio runtime booted a service-bound Java Sound backend with deterministic fallback semantics, cue planning, clip registry, mixer, volume profile, and diagnostics.",
                  "backendId": "%s",
                  "serviceBound": true,
                  "clipCount": %d,
                  "busCounts": %s,
                  "initialCueRequests": %d,
                  "initialEvents": %s,
                  "volumeProfile": %s
                }
                """.formatted(
                quoteRaw(audio.backend().backendId()),
                audio.clipRegistry().count(),
                busCounts,
                audio.initialCuePlan().size(),
                initialEvents,
                volumeProfile
        ));
        write(root.resolve("audio-backend.json"), """
                {
                  "schema": "echo.standalone.audio_backend.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "backendId": "%s",
                  "deviceOpenDuringRun": %s,
                  "fallbackActiveDuringRun": %s,
                  "eventCount": %d,
                  "diagnosticCount": %d
                }
                """.formatted(
                quoteRaw(audio.backend().backendId()),
                defaultDeviceOpenDuringRun,
                defaultFallbackActiveDuringRun,
                audio.backend().events().size(),
                audio.backend().diagnostics().size()
        ));
        write(root.resolve("audio-buses.json"), """
                {
                  "schema": "echo.standalone.audio_buses.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "busCounts": %s,
                  "coveredBuses": ["AMBIENCE", "MUSIC", "SFX", "UI", "STINGER", "ALERT"]
                }
                """.formatted(busCounts));
        write(root.resolve("audio-clips.json"), """
                {
                  "schema": "echo.standalone.audio_clips.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "clipCount": %d,
                  "clips": %s
                }
                """.formatted(audio.clipRegistry().count(), gameplayClips));
        write(root.resolve("audio-ambience.json"), cueReport(
                "echo.standalone.audio_ambience.v1",
                "ashfall:ambience_ash_storm",
                audio.initialEvents().get(0)
        ));
        write(root.resolve("audio-music.json"), cueReport(
                "echo.standalone.audio_music.v1",
                "ashfall:music_survival_pulse",
                audio.initialEvents().get(1)
        ));
        write(root.resolve("audio-ui-sounds.json"), cueReport(
                "echo.standalone.audio_ui_sounds.v1",
                "echo:ui_terminal_blip",
                audio.initialEvents().get(2)
        ));
        write(root.resolve("audio-mission-stingers.json"), cueReport(
                "echo.standalone.audio_mission_stingers.v1",
                "ashfall:mission_secure_stinger",
                audio.initialEvents().get(3)
        ));
        write(root.resolve("audio-volume-profiles.json"), """
                {
                  "schema": "echo.standalone.audio_volume_profiles.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "profile": %s,
                  "mutedGain": %.4f,
                  "quietUiGain": %.4f
                }
                """.formatted(
                volumeProfile,
                mutedEvent.effectiveGain(),
                quietUi.gainFor(EchoAudioBus.UI, audio.clipRegistry().require("echo:ui_terminal_blip").baseGain())
        ));
        write(root.resolve("audio-diagnostics.json"), """
                {
                  "schema": "echo.standalone.audio_diagnostics.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "diagnosticCount": %d,
                  "diagnostics": %s
                }
                """.formatted(audio.backend().diagnostics().size(), diagnostics));

        write(root.resolve("runtime-audio-device.json"), """
                {
                  "schema": "echo.standalone.runtime_audio_device.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "summary": "Java Sound audio device path and forced recording fallback path both preserve submitted playback events and close cleanly.",
                  "backendId": "%s",
                  "deviceOpenDuringRun": %s,
                  "fallbackActiveDuringRun": %s,
                  "forcedFallbackActive": %s,
                  "forcedFallbackEvents": %d
                }
                """.formatted(
                quoteRaw(audio.backend().backendId()),
                defaultDeviceOpenDuringRun,
                defaultFallbackActiveDuringRun,
                forcedFallbackActiveDuringRun,
                forcedFallback.backend().events().size()
        ));
        write(root.resolve("audio-device-output.json"), """
                {
                  "schema": "echo.standalone.audio_device_output.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "backendId": "%s",
                  "deviceOpenDuringRun": %s,
                  "events": %s
                }
                """.formatted(quoteRaw(audio.backend().backendId()), defaultDeviceOpenDuringRun, events(audio.backend().events())));
        write(root.resolve("audio-device-fallback.json"), """
                {
                  "schema": "echo.standalone.audio_device_fallback.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "forcedFallbackActive": %s,
                  "fallbackBackendId": "%s",
                  "fallbackEvents": %d,
                  "fallbackDiagnostics": %s
                }
                """.formatted(
                forcedFallbackActiveDuringRun,
                quoteRaw(forcedFallback.backend().backendId()),
                forcedFallback.backend().events().size(),
                forcedDiagnostics
        ));
        write(root.resolve("audio-device-volume-controls.json"), """
                {
                  "schema": "echo.standalone.audio_device_volume_controls.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "mutedGain": %.4f,
                  "quietUiGain": %.4f,
                  "profile": %s
                }
                """.formatted(
                mutedEvent.effectiveGain(),
                quietUi.gainFor(EchoAudioBus.UI, audio.clipRegistry().require("echo:ui_terminal_blip").baseGain()),
                volumeProfile
        ));
        write(root.resolve("audio-device-smoke.json"), """
                {
                  "schema": "echo.standalone.audio_device_smoke.v1",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "defaultBackendEvents": %d,
                  "forcedFallbackEvents": %d,
                  "defaultDiagnostics": %d,
                  "forcedFallbackDiagnostics": %d,
                  "closeDiagnosticsObserved": true
                }
                """.formatted(
                audio.backend().events().size(),
                forcedFallback.backend().events().size(),
                audio.backend().diagnostics().size(),
                forcedFallback.backend().diagnostics().size()
        ));
    }

    private static String cueReport(String schema, String expectedClip, EchoAudioPlaybackEvent event) {
        return """
                {
                  "schema": "%s",
                  "status": "PASS",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAudioSmokeHarness",
                  "expectedClip": "%s",
                  "event": %s
                }
                """.formatted(schema, quoteRaw(expectedClip), event(event));
    }

    private static String busCounts(EchoAudioRuntimeResult audio) {
        StringBuilder json = new StringBuilder("{");
        EchoAudioBus[] buses = EchoAudioBus.values();
        for (int i = 0; i < buses.length; i++) {
            EchoAudioBus bus = buses[i];
            if (i > 0) {
                json.append(", ");
            }
            json.append("\"").append(bus.name()).append("\": ")
                    .append(audio.clipRegistry().byBus(bus).size());
        }
        return json.append("}").toString();
    }

    private static String clips(List<dev.echo.standalone.runtime.audio.EchoAudioClip> clips) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < clips.size(); i++) {
            dev.echo.standalone.runtime.audio.EchoAudioClip clip = clips.get(i);
            if (i > 0) {
                json.append(", ");
            }
            json.append("{\"clipId\": \"").append(quoteRaw(clip.clipId()))
                    .append("\", \"assetKey\": \"").append(quoteRaw(clip.assetKey()))
                    .append("\", \"type\": \"").append(clip.type().name())
                    .append("\", \"bus\": \"").append(clip.bus().name())
                    .append("\", \"looping\": ").append(clip.looping())
                    .append(", \"baseGain\": ").append(formatGain(clip.baseGain()))
                    .append("}");
        }
        return json.append("]").toString();
    }

    private static String events(List<EchoAudioPlaybackEvent> events) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < events.size(); i++) {
            if (i > 0) {
                json.append(", ");
            }
            json.append(event(events.get(i)));
        }
        return json.append("]").toString();
    }

    private static String event(EchoAudioPlaybackEvent event) {
        return "{\"eventId\": \"" + quoteRaw(event.eventId())
                + "\", \"action\": \"" + event.action().name()
                + "\", \"clipId\": \"" + quoteRaw(event.clip().clipId())
                + "\", \"bus\": \"" + event.bus().name()
                + "\", \"effectiveGain\": " + formatGain(event.effectiveGain())
                + ", \"reason\": \"" + quoteRaw(event.reason())
                + "\", \"tick\": " + event.tick()
                + "}";
    }

    private static String diagnostics(List<dev.echo.standalone.runtime.audio.EchoAudioDiagnostic> diagnostics) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < diagnostics.size(); i++) {
            dev.echo.standalone.runtime.audio.EchoAudioDiagnostic diagnostic = diagnostics.get(i);
            if (i > 0) {
                json.append(", ");
            }
            json.append("{\"severity\": \"").append(diagnostic.severity().name())
                    .append("\", \"message\": \"").append(quoteRaw(diagnostic.message()))
                    .append("\"}");
        }
        return json.append("]").toString();
    }

    private static String volumeProfile(EchoAudioVolumeProfile profile) {
        StringBuilder volumes = new StringBuilder("{");
        EchoAudioBus[] buses = EchoAudioBus.values();
        for (int i = 0; i < buses.length; i++) {
            EchoAudioBus bus = buses[i];
            if (i > 0) {
                volumes.append(", ");
            }
            volumes.append("\"").append(bus.name()).append("\": ")
                    .append(formatGain(profile.busVolumes().get(bus)));
        }
        return "{\"profileId\": \"" + quoteRaw(profile.profileId())
                + "\", \"muted\": " + profile.muted()
                + ", \"busVolumes\": " + volumes.append("}")
                + "}";
    }

    private static void write(Path path, String json) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, json);
    }

    private static String quoteRaw(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String formatGain(double value) {
        return String.format(Locale.ROOT, "%.4f", value);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireDouble(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > 0.0001D) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }
}
