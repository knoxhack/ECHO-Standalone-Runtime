package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBiome;

record EchoClientHazardState(
        String hazardId,
        String label,
        double exposure,
        double tickSeconds,
        int lastDamage
) {
    static final double MAX_EXPOSURE = 100.0D;
    static final double DAMAGE_THRESHOLD = 80.0D;
    private static final double DAMAGE_INTERVAL_SECONDS = 4.0D;
    private static final double SAFE_DECAY_PER_SECOND = 16.0D;

    EchoClientHazardState {
        hazardId = hazardId == null || hazardId.isBlank() ? "echo:none" : hazardId.trim();
        label = label == null || label.isBlank() ? "None" : label.trim();
        exposure = clamp(exposure, 0.0D, MAX_EXPOSURE);
        tickSeconds = Math.max(0.0D, tickSeconds);
        lastDamage = Math.max(0, lastDamage);
        if (exposure <= 0.0D) {
            hazardId = "echo:none";
            label = "None";
            tickSeconds = 0.0D;
            lastDamage = 0;
        }
    }

    static EchoClientHazardState empty() {
        return new EchoClientHazardState("echo:none", "None", 0.0D, 0.0D, 0);
    }

    EchoClientHazardTick tick(
            EchoVoxelBiome biome,
            double deltaSeconds,
            EchoClientHazardCatalog hazardCatalog
    ) {
        double dt = Math.max(0.0D, deltaSeconds);
        EchoClientHazardCatalog safeCatalog =
                hazardCatalog == null ? EchoClientHazardCatalog.empty() : hazardCatalog;
        EchoClientHazardCatalog.HazardProfile profile = safeCatalog.profileForBiome(biome);
        if (profile.inactive()) {
            double nextExposure = Math.max(0.0D, exposure - SAFE_DECAY_PER_SECOND * dt);
            return new EchoClientHazardTick(
                    new EchoClientHazardState(hazardId, label, nextExposure, 0.0D, 0),
                    0,
                    EchoClientDamageSource.none()
            );
        }

        boolean sameHazard = hazardId.equals(profile.hazardId());
        double carriedExposure = sameHazard ? exposure : exposure * 0.5D;
        double nextExposure = Math.min(MAX_EXPOSURE, carriedExposure + profile.exposurePerSecond() * dt);
        double nextTickSeconds = nextExposure >= DAMAGE_THRESHOLD
                ? (sameHazard ? tickSeconds : 0.0D) + dt
                : 0.0D;
        int damage = 0;
        while (nextTickSeconds >= DAMAGE_INTERVAL_SECONDS) {
            nextTickSeconds -= DAMAGE_INTERVAL_SECONDS;
            damage += profile.damage();
        }
        EchoClientDamageSource source = EchoClientDamageSource.hazard(profile.hazardId(), profile.label());
        return new EchoClientHazardTick(
                new EchoClientHazardState(profile.hazardId(), profile.label(), nextExposure, nextTickSeconds, damage),
                damage,
                source
        );
    }

    int exposurePercent() {
        return (int) Math.round(exposure);
    }

    boolean active() {
        return exposure > 0.0D;
    }

    record EchoClientHazardTick(
            EchoClientHazardState state,
            int damage,
            EchoClientDamageSource source
    ) {
    }

    private static double clamp(double value, double minimum, double maximum) {
        if (!Double.isFinite(value)) {
            return minimum;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }
}
