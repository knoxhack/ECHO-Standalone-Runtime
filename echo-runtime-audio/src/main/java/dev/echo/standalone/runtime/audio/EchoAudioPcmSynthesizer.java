package dev.echo.standalone.runtime.audio;

final class EchoAudioPcmSynthesizer {
    private EchoAudioPcmSynthesizer() {
    }

    static byte[] synthesize(EchoAudioPlaybackEvent event, EchoAudioDeviceSettings settings) {
        int durationMillis = durationMillisFor(event);
        int sampleCount = Math.max(1, Math.round(settings.sampleRate() * durationMillis / 1000.0F));
        byte[] bytes = new byte[sampleCount * 2];
        double frequency = frequencyFor(event);
        double gain = Math.min(0.35D, event.effectiveGain() * 0.35D);
        Waveform waveform = waveformFor(event);
        double sampleRate = settings.sampleRate();
        long seed = event.tick() * 31L + event.eventId().hashCode();

        for (int index = 0; index < sampleCount; index++) {
            double t = index / sampleRate;
            double envelope = envelope(index, sampleCount, event.bus());
            double raw = waveform.sample(t, frequency, index, sampleRate, seed);
            short sample = (short) Math.round(raw * Short.MAX_VALUE * gain * envelope);
            bytes[index * 2] = (byte) (sample & 0xFF);
            bytes[index * 2 + 1] = (byte) ((sample >>> 8) & 0xFF);
        }
        return bytes;
    }

    private static int durationMillisFor(EchoAudioPlaybackEvent event) {
        return switch (event.bus()) {
            case SFX -> event.reason().contains("step") || event.reason().contains("footstep") ? 60 : 120;
            case UI -> 50;
            case ALERT -> 180;
            case STINGER -> 250;
            case DIAGNOSTIC -> 80;
            case MUSIC -> 400;
            case AMBIENCE -> 300;
            case MASTER -> 200;
        };
    }

    private static double frequencyFor(EchoAudioPlaybackEvent event) {
        return switch (event.bus()) {
            case AMBIENCE -> 55.0D;
            case MUSIC -> 261.63D;
            case SFX -> event.reason().contains("break") || event.reason().contains("mine") ? 180.0D
                    : event.reason().contains("place") ? 240.0D
                    : event.reason().contains("step") || event.reason().contains("footstep") ? 90.0D
                    : 320.0D;
            case UI -> event.reason().contains("blip") ? 1760.0D : 880.0D;
            case STINGER -> 440.0D;
            case ALERT -> 660.0D;
            case DIAGNOSTIC -> 330.0D;
            case MASTER -> 440.0D;
        };
    }

    private static Waveform waveformFor(EchoAudioPlaybackEvent event) {
        return switch (event.bus()) {
            case SFX -> event.reason().contains("step") || event.reason().contains("footstep")
                    ? Waveform.NOISE_BAND
                    : Waveform.SQUARE;
            case UI -> Waveform.SQUARE;
            case ALERT -> Waveform.SAWTOOTH;
            case STINGER -> Waveform.SQUARE;
            case DIAGNOSTIC -> Waveform.SINE;
            case MUSIC -> Waveform.SINE_VIBRATO;
            case AMBIENCE -> Waveform.NOISE_LOW;
            case MASTER -> Waveform.SINE;
        };
    }

    private static double envelope(int index, int sampleCount, EchoAudioBus bus) {
        int attackSamples = Math.max(1, sampleCount / 16);
        int releaseSamples = Math.max(1, sampleCount / 4);
        if (index < attackSamples) {
            return index / (double) attackSamples;
        }
        int samplesRemaining = sampleCount - index - 1;
        if (samplesRemaining < releaseSamples) {
            return Math.max(0.0D, samplesRemaining / (double) releaseSamples);
        }
        return switch (bus) {
            case SFX, UI, STINGER, ALERT -> 1.0D;
            case MUSIC -> 0.85D + 0.15D * Math.sin(index * 0.01D);
            case AMBIENCE -> 0.7D + 0.3D * Math.sin(index * 0.003D);
            case DIAGNOSTIC, MASTER -> 1.0D;
        };
    }

    @FunctionalInterface
    private interface Waveform {
        double sample(double t, double frequency, int index, double sampleRate, long seed);

        Waveform SINE = (t, f, i, sr, s) -> Math.sin(2.0D * Math.PI * f * t);

        Waveform SINE_VIBRATO = (t, f, i, sr, s) -> {
            double vibrato = 4.0D * Math.sin(2.0D * Math.PI * 5.0D * t);
            return Math.sin(2.0D * Math.PI * (f + vibrato) * t);
        };

        Waveform SQUARE = (t, f, i, sr, s) -> {
            double phase = (f * t) % 1.0D;
            return phase < 0.5D ? 1.0D : -1.0D;
        };

        Waveform SAWTOOTH = (t, f, i, sr, s) -> {
            double phase = (f * t) % 1.0D;
            return 2.0D * (phase - 0.5D);
        };

        Waveform NOISE_LOW = (t, f, i, sr, s) -> {
            double n = noise(i, s);
            // simple one-pole lowpass
            return n * 0.6D + (i > 0 ? noise(i - 1, s) : n) * 0.4D;
        };

        Waveform NOISE_BAND = (t, f, i, sr, s) -> {
            double n = noise(i, s);
            // bandpass-ish by combining two delayed samples
            double prev = i > 2 ? noise(i - 2, s) : n;
            return (n - prev) * 0.7D;
        };
    }

    private static double noise(int index, long seed) {
        long x = index * 374761393L + seed * 668265263L;
        x = (x ^ (x >>> 30)) * 1274126177L;
        x = x ^ (x >>> 28);
        long raw = x % 10001L;
        return (raw / 5000.5D) - 1.0D;
    }
}
