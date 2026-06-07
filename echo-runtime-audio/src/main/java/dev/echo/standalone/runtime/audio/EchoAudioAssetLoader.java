package dev.echo.standalone.runtime.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class EchoAudioAssetLoader {
    private final List<String> searchPaths;
    private final ClassLoader classLoader;
    private final ArrayList<EchoAudioDiagnostic> diagnostics = new ArrayList<>();

    public EchoAudioAssetLoader(List<String> searchPaths, ClassLoader classLoader) {
        this.searchPaths = List.copyOf(Objects.requireNonNull(searchPaths, "searchPaths"));
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    public List<EchoAudioAssetEntry> scan() {
        ArrayList<EchoAudioAssetEntry> entries = new ArrayList<>();
        for (String searchPath : searchPaths) {
            scanPath(searchPath, entries);
        }
        diagnostics.add(new EchoAudioDiagnostic(
                EchoAudioDiagnosticSeverity.INFO,
                "asset scan complete: " + entries.size() + " audio files found"
        ));
        return List.copyOf(entries);
    }

    public Optional<EchoAudioAssetEntry> find(String assetKey) {
        String normalized = normalizeKey(assetKey);
        for (EchoAudioAssetEntry entry : scan()) {
            if (entry.assetKey().equals(normalized)) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public byte[] loadPcm16Mono(String assetKey, float targetSampleRate) {
        Optional<EchoAudioAssetEntry> entry = find(assetKey);
        if (entry.isEmpty()) {
            diagnostics.add(new EchoAudioDiagnostic(
                    EchoAudioDiagnosticSeverity.WARNING,
                    "asset not found: " + assetKey
            ));
            return new byte[0];
        }
        try (InputStream raw = openStream(entry.get())) {
            return decodeToPcm16Mono(raw, targetSampleRate);
        } catch (IOException exception) {
            diagnostics.add(new EchoAudioDiagnostic(
                    EchoAudioDiagnosticSeverity.ERROR,
                    "asset decode failed: " + assetKey + " " + exception.getClass().getSimpleName()
            ));
            return new byte[0];
        }
    }

    private void scanPath(String searchPath, ArrayList<EchoAudioAssetEntry> entries) {
        try (InputStream listing = classLoader.getResourceAsStream(searchPath)) {
            if (listing == null) {
                diagnostics.add(new EchoAudioDiagnostic(
                        EchoAudioDiagnosticSeverity.INFO,
                        "asset path not present: " + searchPath
                ));
                return;
            }
            String content = new String(listing.readAllBytes());
            for (String line : content.split("[\r\n]+")) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                if (isAudioFile(trimmed)) {
                    String key = normalizeKey(searchPath.endsWith("/") ? searchPath + trimmed : searchPath + "/" + trimmed);
                    entries.add(new EchoAudioAssetEntry(key, trimmed, guessFormat(trimmed)));
                }
            }
        } catch (IOException exception) {
            diagnostics.add(new EchoAudioDiagnostic(
                    EchoAudioDiagnosticSeverity.WARNING,
                    "asset path scan failed: " + searchPath + " " + exception.getClass().getSimpleName()
            ));
        }
    }

    private InputStream openStream(EchoAudioAssetEntry entry) {
        InputStream stream = classLoader.getResourceAsStream(entry.assetKey());
        if (stream == null) {
            throw new UncheckedIOException(new IOException("resource not found: " + entry.assetKey()));
        }
        return stream;
    }

    private static byte[] decodeToPcm16Mono(InputStream source, float targetSampleRate) throws IOException {
        try (AudioInputStream original = AudioSystem.getAudioInputStream(source)) {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    targetSampleRate,
                    16,
                    1,
                    2,
                    targetSampleRate,
                    false
            );
            try (AudioInputStream converted = AudioSystem.getAudioInputStream(targetFormat, original)) {
                return converted.readAllBytes();
            }
        } catch (UnsupportedAudioFileException exception) {
            throw new IOException("unsupported audio file format", exception);
        }
    }

    private static boolean isAudioFile(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".wav") || lower.endsWith(".ogg") || lower.endsWith(".mp3") || lower.endsWith(".aiff");
    }

    private static String normalizeKey(String key) {
        String normalized = key.replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    private static EchoAudioAssetFormat guessFormat(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".ogg")) {
            return EchoAudioAssetFormat.OGG_VORBIS;
        }
        if (lower.endsWith(".mp3")) {
            return EchoAudioAssetFormat.MP3;
        }
        if (lower.endsWith(".aiff") || lower.endsWith(".au")) {
            return EchoAudioAssetFormat.AIFF;
        }
        return EchoAudioAssetFormat.WAV;
    }

    public List<EchoAudioDiagnostic> diagnostics() {
        return List.copyOf(diagnostics);
    }
}
