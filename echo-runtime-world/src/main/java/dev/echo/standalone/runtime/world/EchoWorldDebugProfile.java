package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Objects;

public record EchoWorldDebugProfile(
        String regionDisplayName,
        int dangerLevel,
        String dimensionDisplayName,
        String environment,
        double gravity,
        EchoWorldHazard hazard,
        EchoWorldWeatherField weather,
        List<EchoWorldPoi> pois,
        String originTerrain,
        String primaryTerrain,
        String secondaryTerrain
) {
    public EchoWorldDebugProfile {
        regionDisplayName = EchoWorldText.requireText(regionDisplayName, "regionDisplayName");
        if (dangerLevel < 0) {
            throw new IllegalArgumentException("dangerLevel must not be negative");
        }
        dimensionDisplayName = EchoWorldText.requireText(dimensionDisplayName, "dimensionDisplayName");
        environment = EchoWorldText.requireText(environment, "environment");
        if (gravity <= 0.0D) {
            throw new IllegalArgumentException("gravity must be positive");
        }
        hazard = Objects.requireNonNull(hazard, "hazard");
        weather = Objects.requireNonNull(weather, "weather");
        pois = List.copyOf(pois == null ? List.of() : pois);
        originTerrain = EchoWorldText.requireText(originTerrain, "originTerrain");
        primaryTerrain = EchoWorldText.requireText(primaryTerrain, "primaryTerrain");
        secondaryTerrain = EchoWorldText.requireText(secondaryTerrain, "secondaryTerrain");
    }

    List<String> regionHazardIds() {
        return List.of(hazard.id());
    }

    public static EchoWorldDebugProfile generic() {
        return new EchoWorldDebugProfile(
                "Debug Region",
                1,
                "Debug Surface",
                "debug_surface",
                1.0D,
                new EchoWorldHazard(
                        "echo:debug_hazard",
                        "debug_pressure",
                        0.5D,
                        new EchoWorldPosition(1, 0, 1),
                        2
                ),
                new EchoWorldWeatherField(
                        "echo:debug_weather",
                        20.0D,
                        4.0D,
                        0.0D,
                        64.0D
                ),
                List.of(
                        new EchoWorldPoi(
                                "echo:debug_terminal",
                                "terminal",
                                "Debug Terminal",
                                new EchoWorldPosition(0, 0, 0)
                        )
                ),
                "debug_origin",
                "debug_ridge",
                "debug_flat"
        );
    }

    static EchoWorldDebugProfile ashfallCrashSite() {
        return new EchoWorldDebugProfile(
                "Crash Site",
                2,
                "Ashfall Surface",
                "toxic_wasteland",
                1.0D,
                new EchoWorldHazard(
                        "ashfall:toxic_ash",
                        "airborne_toxicity",
                        0.72D,
                        new EchoWorldPosition(1, 0, 1),
                        2
                ),
                new EchoWorldWeatherField(
                        "ashfall:ash_storm",
                        41.5D,
                        18.0D,
                        0.67D,
                        42.0D
                ),
                List.of(
                        new EchoWorldPoi(
                                "ashfall:terminal_pod",
                                "terminal",
                                "Emergency Terminal Pod",
                                new EchoWorldPosition(0, 0, 0)
                        ),
                        new EchoWorldPoi(
                                "ashfall:crash_cache",
                                "loot_cache",
                                "Crash Cache",
                                new EchoWorldPosition(2, 0, 1)
                        )
                ),
                "crash_debris",
                "ash_dune",
                "scorched_flat"
        );
    }
}
