# ECHO Standalone Audio Device Runtime

Phase 15.6 adds real audio output behind the existing audio backend contract. The runtime now tries Java Sound first and falls back to deterministic recording if the device path cannot open.

## Implemented

- `EchoJavaSoundAudioBackend` writes PCM to `SourceDataLine` when available.
- `EchoAudioDeviceSettings` controls device enablement and forced fallback.
- `EchoAudioPcmSynthesizer` creates short deterministic cue buffers for ambience, music, UI, and mission stingers.
- `EchoAudioMixer` supports runtime profile replacement.
- `EchoAudioVolumeProfile` supports mute, master volume, and bus-volume copies.
- `EchoRecordingAudioBackend` remains the fallback path.

## Fallback Contract

Device failure is not fatal. Submitted requests still produce `EchoAudioPlaybackEvent` entries with the same action, clip, bus, gain, reason, and tick data. Diagnostics explain whether the Java Sound device opened, wrote PCM bytes, skipped muted playback, or activated fallback.

The player-facing OpenGL client also surfaces audio backend/device/fallback status through the ScreenCore Diagnostics route and client support bundle. The deterministic client smoke forces the Java Sound fallback path so `reports/echo/standalone/client-machine-terminal-surfaces.json` and exported `runtime-diagnostics.json` prove the UI/support path preserves backend id, device label, event counts, warning/error counts, mix levels, subtitle state, current cues, and latest backend diagnostic. This does not replace a manual real-speaker or headset verification run.

Public release now tracks that manual check separately in `reports/echo/standalone/audio-hardware-verification.json`. The report must be generated with `scripts/capture-audio-hardware-evidence.ps1` and record the physical output device, music/UI/SFX/ambience cue confirmation, volume/mute behavior, ScreenCore Diagnostics audio row, support-bundle audio diagnostics, tester attestation, and any hardware audio issues.

## Validation

The Phase 15.6 smoke harness runs both the live device-capable default backend and a forced fallback backend. It verifies cue routing, gains, mute behavior, per-bus volume, fallback activation, and clean close behavior.

It also writes non-placeholder device evidence to `runtime-audio-device.json`, `audio-device-output.json`, `audio-device-fallback.json`, `audio-device-volume-controls.json`, and `audio-device-smoke.json`; `verifyStandaloneAudioDeviceRuntime` rejects bootstrap schemas for these reports.
