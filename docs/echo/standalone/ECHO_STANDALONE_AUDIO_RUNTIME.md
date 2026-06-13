# ECHO Standalone Audio Runtime

Phase 14.14 introduced the audio backend contract, buses, clip registry, cue planning, mixer routing, volume profile gain, recording backend, playback events, and diagnostics. Phase 15.6 upgrades the default backend to a Java Sound device adapter with deterministic recording fallback.

## Runtime Pieces

- `EchoAudioRuntime` creates and service-binds the debug audio runtime.
- `EchoAudioBackend` remains the backend contract used by device, recording, and future audio adapters.
- `EchoJavaSoundAudioBackend` is the default backend and attempts real PCM output through Java Sound.
- `EchoRecordingAudioBackend` remains the fallback and command-audit backend.
- `EchoAudioDeviceSettings` controls device enablement, forced-fallback testing, sample format, write duration, and requested device name.
- `EchoAudioPcmSynthesizer` creates short deterministic PCM buffers for ambience, music, SFX, UI, stinger, and alert cues until asset decoding arrives.
- `EchoAudioVolumeProfile` stores master/per-bus volume and mute state.
- `EchoAudioMixer` applies the current profile and can swap profiles at runtime.
- `EchoAudioCuePlanner` creates the initial Ashfall ambience, music, UI, and mission stinger cue plan, plus an Ashfall gameplay cue plan for state-change feedback.

## Debug Audio Plan

The Phase 15.6 debug audio plan contains:

```text
backend: echo:java_sound_audio
profile: ashfall-debug-volume
clips: 13
initial events: 4
device path: Java Sound SourceDataLine when available
fallback path: echo:recording_audio
```

The initial cue plan records:

```text
ambience: ashfall:ambience_ash_storm
music: ashfall:music_survival_pulse
UI sound: echo:ui_terminal_blip
mission stinger: ashfall:mission_secure_stinger
```

The gameplay cue plan records:

```text
mining hit: ashfall:block_mining_hit
block break: ashfall:block_break
item pickup: ashfall:item_pickup
water use: ashfall:consume_water
food use: ashfall:consume_food
terminal beep: ashfall:radio_static
power repair: ashfall:power_repair
extraction beacon: ashfall:extraction_beacon
danger alert: ashfall:danger_alert
```

## Volume Controls

The debug volume profile applies master and bus gains:

```text
master: 0.80
music: 0.55
ambience: 0.70
SFX: 0.82
UI: 0.90
stinger: 0.85
alert: 0.92
diagnostic: 1.00
```

The mixer supports replacing the active profile. `EchoAudioVolumeProfile.withMuted(true)` forces zero effective gain, and `withBusVolume(...)` adjusts individual buses.

## Device Fallback

The Java Sound backend attempts to open a mono 16-bit PCM `SourceDataLine`. If the device is disabled, unavailable, blocked, or forced to fail by test settings, the backend activates `EchoRecordingAudioBackend` and keeps the same event log semantics.

## Smoke Harness Coverage

`EchoRuntimeAudioSmokeHarness` proves:

- audio runtime result, backend, clip registry, volume profile, mixer, cue planner, and cue plan are service-bound.
- the default backend is `echo:java_sound_audio`.
- the backend either opens a Java Sound device or activates recording fallback.
- startup and gameplay clips are registered.
- ambience, music, SFX, UI, stinger, and alert buses contain their expected clips.
- the initial Ashfall cue plan submits four requests.
- ambience and music cues are looped.
- UI and stinger cues are one-shots.
- effective gain is calculated through the volume profile.
- mute and per-bus volume controls affect submitted gain.
- forced device failure activates fallback and closes cleanly.
- non-placeholder audio evidence is written to `runtime-audio.json`, `audio-backend.json`, `audio-buses.json`, `audio-clips.json`, `audio-ambience.json`, `audio-music.json`, `audio-ui-sounds.json`, `audio-mission-stingers.json`, `audio-volume-profiles.json`, and `audio-diagnostics.json`; `verifyStandaloneAudioRuntime` rejects bootstrap schemas for these reports.

`EchoRuntimeAshfallAudioCueSmokeHarness` proves AdapterCore-backed gameplay cue coverage and writes `ashfall-audio-cue-coverage.json`: ash ambience, mining hits, block break, item pickup, water/food use, terminal beep through `echoashfallprotocol:sound/ui.echo_message`, power repair, extraction beacon, and danger alerts.

## Out Of Scope

Phase 15.6 does not decode external OGG/WAV assets, stream long music tracks, spatialize audio, expose OS device selection UI, or sync audio across a network. The device backend currently emits short synthesized PCM buffers for runtime validation.
