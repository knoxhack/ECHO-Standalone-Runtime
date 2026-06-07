package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.entity.EchoEntityId;

import java.util.Objects;

public final class EchoSurvivalState {
    private final EchoEntityId playerId;
    private double hydration;
    private double ashExposure;
    private double heatStress;

    public EchoSurvivalState(EchoEntityId playerId, double hydration, double ashExposure, double heatStress) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.hydration = clamp(hydration);
        this.ashExposure = clamp(ashExposure);
        this.heatStress = clamp(heatStress);
    }

    public synchronized EchoEntityId playerId() {
        return playerId;
    }

    public synchronized double hydration() {
        return hydration;
    }

    public synchronized double ashExposure() {
        return ashExposure;
    }

    public synchronized double heatStress() {
        return heatStress;
    }

    public synchronized void addHydration(double amount) {
        hydration = clamp(hydration + amount);
    }

    public synchronized void addAshExposure(double amount) {
        ashExposure = clamp(ashExposure + amount);
    }

    public synchronized void addHeatStress(double amount) {
        heatStress = clamp(heatStress + amount);
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(100.0D, round(value)));
    }

    static double round(double value) {
        return Math.round(value * 100.0D) / 100.0D;
    }
}
