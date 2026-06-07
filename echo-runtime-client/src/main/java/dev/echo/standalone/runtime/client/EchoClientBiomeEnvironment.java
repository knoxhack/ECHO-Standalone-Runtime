package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSources;

import java.util.Locale;
import java.util.Objects;

record EchoClientBiomeEnvironment(
        String biomeId,
        float fogRed,
        float fogGreen,
        float fogBlue,
        float fogDensity,
        String ambienceClipId,
        String ambienceLabel
) {
    static final EchoClientBiomeEnvironment DEFAULT = fromBiome(
            EchoVoxelBiomeSources.defaultBiome()
    );

    EchoClientBiomeEnvironment {
        biomeId = requireText(biomeId, "biomeId");
        fogRed = clamp01(fogRed);
        fogGreen = clamp01(fogGreen);
        fogBlue = clamp01(fogBlue);
        fogDensity = Math.max(0.0F, Math.min(0.08F, fogDensity));
        ambienceClipId = requireText(ambienceClipId, "ambienceClipId");
        ambienceLabel = requireText(ambienceLabel, "ambienceLabel");
    }

    static EchoClientBiomeEnvironment fromBiome(EchoVoxelBiome biome) {
        EchoVoxelBiome safeBiome = Objects.requireNonNull(biome, "biome");
        String clipId = ambienceClipId(safeBiome);
        return new EchoClientBiomeEnvironment(
                safeBiome.id(),
                colorComponent(safeBiome.fogColor(), 16),
                colorComponent(safeBiome.fogColor(), 8),
                colorComponent(safeBiome.fogColor(), 0),
                fogDensity(safeBiome),
                clipId,
                ambienceLabel(clipId)
        );
    }

    String fogDebugText() {
        return String.format(
                Locale.ROOT,
                "FOG %.2f %.2f %.2f D %.3f",
                fogRed,
                fogGreen,
                fogBlue,
                fogDensity
        );
    }

    private static String ambienceClipId(EchoVoxelBiome biome) {
        if (biome.hasTag("toxic")) {
            return "echo:ambience_toxic_swamp";
        }
        if (biome.hasTag("radiation")) {
            return "echo:ambience_radiation";
        }
        if (biome.hasTag("industrial") || biome.hasTag("city")) {
            return "echo:ambience_ruins";
        }
        if (biome.hasTag("cold")) {
            return "echo:ambience_cryogenic";
        }
        if (biome.hasTag("nexus") || biome.hasTag("anomaly")) {
            return "echo:ambience_nexus";
        }
        return "echo:ambience_ash_wasteland";
    }

    private static String ambienceLabel(String clipId) {
        int separator = clipId.indexOf(':');
        String path = separator >= 0 ? clipId.substring(separator + 1) : clipId;
        String[] words = path.split("_+");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? clipId : builder.toString();
    }

    private static float fogDensity(EchoVoxelBiome biome) {
        float density = 0.010F + (float) Math.min(0.018D, biome.downfall() * 0.012D);
        if (biome.hasTag("toxic")) {
            density += 0.012F;
        }
        if (biome.hasTag("radiation") || biome.hasTag("nexus")) {
            density += 0.009F;
        }
        if (biome.hasTag("industrial") || biome.hasTag("city")) {
            density += 0.006F;
        }
        if (biome.hasTag("cold")) {
            density += 0.004F;
        }
        return density;
    }

    private static float colorComponent(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255.0F;
    }

    private static float clamp01(float value) {
        if (!Float.isFinite(value)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
