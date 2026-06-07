package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

public final class EchoAssetLoader {
    public byte[] loadBytes(EchoAssetEntry entry) throws IOException {
        Objects.requireNonNull(entry, "entry");
        byte[] embeddedBytes = entry.embeddedBytes();
        if (embeddedBytes != null) {
            return embeddedBytes;
        }
        return Files.readAllBytes(entry.file());
    }

    public String loadText(EchoAssetEntry entry) throws IOException {
        return new String(loadBytes(entry), StandardCharsets.UTF_8);
    }
}
