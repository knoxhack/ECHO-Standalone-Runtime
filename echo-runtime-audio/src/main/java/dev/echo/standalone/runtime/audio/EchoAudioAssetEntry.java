package dev.echo.standalone.runtime.audio;

public record EchoAudioAssetEntry(
        String assetKey,
        String fileName,
        EchoAudioAssetFormat format
) {
}
