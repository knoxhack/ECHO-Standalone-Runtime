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

## Validation

The Phase 15.6 smoke harness runs both the live device-capable default backend and a forced fallback backend. It verifies cue routing, gains, mute behavior, per-bus volume, fallback activation, and clean close behavior.
