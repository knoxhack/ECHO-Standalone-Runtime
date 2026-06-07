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

import java.util.List;

public final class EchoRuntimeAudioSmokeHarness {
    private EchoRuntimeAudioSmokeHarness() {
    }

    public static void main(String[] args) {
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
        forcedFallback.backend().close();
        require(forcedFallback.backend().diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.message().endsWith("backend closed")),
                "forced fallback backend should close cleanly");

        audio.backend().close();
        require(audio.backend().diagnostics().stream()
                        .anyMatch(diagnostic -> diagnostic.message().endsWith("backend closed")),
                "default device backend should close cleanly");

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
