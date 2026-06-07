package dev.echo.standalone.runtime.client;

import java.nio.file.Path;
import java.util.Set;

record EchoClientResourcePackSummary(
        String id,
        Path root,
        Set<String> namespaces,
        long textureCount,
        long animatedTextureMetadataCount,
        long modelCount,
        long blockstateCount,
        long langCount,
        long soundsJsonCount,
        long soundEventCount,
        String detail
) {
    EchoClientResourcePackSummary {
        id = id == null || id.isBlank() ? "unknown-pack" : id;
        namespaces = namespaces == null ? Set.of() : Set.copyOf(namespaces);
        detail = detail == null || detail.isBlank() ? "Minecraft-style resource pack" : detail;
    }

    String menuLabel() {
        String label = id + " - " + namespaces.size() + " ns, " + textureCount + " textures";
        if (animatedTextureMetadataCount > 0L) {
            label += ", " + animatedTextureMetadataCount + " animated";
        }
        return soundEventCount > 0L ? label + ", " + soundEventCount + " sounds" : label;
    }

    String detailLabel() {
        String text = detail;
        if (animatedTextureMetadataCount > 0L) {
            text += " | animated textures " + animatedTextureMetadataCount;
        }
        if (soundEventCount > 0L) {
            text += " | sound events " + soundEventCount;
        }
        return text;
    }
}
