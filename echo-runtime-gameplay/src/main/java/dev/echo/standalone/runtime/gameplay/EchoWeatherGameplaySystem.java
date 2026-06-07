package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.world.EchoWorldWeatherField;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public final class EchoWeatherGameplaySystem {
    private final EchoWorldRuntimeResult world;
    private final EchoSurvivalState survival;
    private final EchoNotificationLog notifications;

    public EchoWeatherGameplaySystem(
            EchoWorldRuntimeResult world,
            EchoSurvivalState survival,
            EchoNotificationLog notifications
    ) {
        this.world = Objects.requireNonNull(world, "world");
        this.survival = Objects.requireNonNull(survival, "survival");
        this.notifications = Objects.requireNonNull(notifications, "notifications");
    }

    public EchoGameplayWeatherResult applyCurrentWeather() {
        EchoWorldWeatherField weather = world.world().chunks().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Weather requires at least one world chunk"))
                .weather();
        double heatStressDelta = EchoSurvivalState.round(weather.ashDensity() * 4.0D + weather.windSpeed() / 100.0D);
        survival.addHeatStress(heatStressDelta);
        notifications.add(
                EchoGameplayNotificationSeverity.WARNING,
                "Ash storm pressure is rising.",
                world.world().tick()
        );
        return new EchoGameplayWeatherResult(
                weather.profileId(),
                weather.ashDensity(),
                heatStressDelta,
                weather.visibility()
        );
    }
}
