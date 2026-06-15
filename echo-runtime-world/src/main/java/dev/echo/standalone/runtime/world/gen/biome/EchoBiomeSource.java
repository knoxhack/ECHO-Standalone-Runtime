package dev.echo.standalone.runtime.world.gen.biome;

import dev.echo.standalone.runtime.world.EchoNoiseSampler;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsBiomeDefinition;

import java.util.List;
import java.util.Objects;

/**
 * Selects an Openlands biome for each column based on temperature/humidity categories.
 *
 * <p>Openlands biomes use categorical temperature ({@code frozen, cool, mild, warm}) and humidity
 * ({@code dry, normal, damp, wet}). This source samples continuous noise and maps it to the
 * nearest categorical biome.
 */
public final class EchoBiomeSource {

    private final List<EchoOpenlandsBiomeDefinition> biomes;
    private final EchoNoiseSampler temperatureNoise;
    private final EchoNoiseSampler humidityNoise;

    public EchoBiomeSource(long seed, List<EchoOpenlandsBiomeDefinition> biomes) {
        this.biomes = List.copyOf(Objects.requireNonNull(biomes, "biomes"));
        if (this.biomes.isEmpty()) {
            throw new IllegalArgumentException("Biome list is empty");
        }
        this.temperatureNoise = new EchoNoiseSampler(seed + 1);
        this.humidityNoise = new EchoNoiseSampler(seed + 2);
    }

    public String biomeAt(int worldX, int worldZ) {
        double temperature = temperatureNoise.sampleOctave(worldX * 0.0015D, 0.0D, worldZ * 0.0015D, 3, 0.5D, 2.0D);
        double humidity = humidityNoise.sampleOctave(worldX * 0.0015D, 0.0D, worldZ * 0.0015D, 3, 0.5D, 2.0D);
        // Push noise toward the [-1, 1] extremes so categorical biomes are well represented.
        temperature = Math.max(-1.0D, Math.min(1.0D, temperature * 1.4D));
        humidity = Math.max(-1.0D, Math.min(1.0D, humidity * 1.4D));

        EchoOpenlandsBiomeDefinition best = biomes.get(0);
        double bestDistance = Double.MAX_VALUE;
        for (EchoOpenlandsBiomeDefinition biome : biomes) {
            double dt = temperature - temperatureValue(biome.temperature());
            double dh = humidity - humidityValue(biome.humidity());
            double distance = dt * dt + dh * dh;
            if (distance < bestDistance) {
                bestDistance = distance;
                best = biome;
            }
        }
        return best.id();
    }

    private static double temperatureValue(String temperature) {
        return switch (temperature.toLowerCase(java.util.Locale.ROOT)) {
            case "frozen" -> -0.8D;
            case "cool" -> -0.3D;
            case "mild" -> 0.0D;
            case "warm" -> 0.5D;
            case "hot" -> 0.9D;
            default -> 0.0D;
        };
    }

    private static double humidityValue(String humidity) {
        return switch (humidity.toLowerCase(java.util.Locale.ROOT)) {
            case "dry" -> -0.7D;
            case "normal" -> 0.0D;
            case "damp" -> 0.4D;
            case "wet" -> 0.8D;
            default -> 0.0D;
        };
    }
}
