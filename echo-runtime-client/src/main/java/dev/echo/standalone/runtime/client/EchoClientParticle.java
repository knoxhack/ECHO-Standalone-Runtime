package dev.echo.standalone.runtime.client;

import java.util.Objects;

final class EchoClientParticle {
    private final String particleId;
    private final EchoClientWorldFeedbackKind kind;
    private double x;
    private double y;
    private double z;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private double ageSeconds;
    private final double lifetimeSeconds;
    private final double size;
    private final int argb;

    EchoClientParticle(
            String particleId,
            EchoClientWorldFeedbackKind kind,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            double ageSeconds,
            double lifetimeSeconds,
            double size,
            int argb
    ) {
        if (particleId == null || particleId.isBlank()) {
            throw new IllegalArgumentException("particleId must not be blank");
        }
        this.particleId = particleId;
        this.kind = kind == null ? EchoClientWorldFeedbackKind.BLOCK_BREAK : kind;
        this.x = finite(x);
        this.y = finite(y);
        this.z = finite(z);
        this.velocityX = finite(velocityX);
        this.velocityY = finite(velocityY);
        this.velocityZ = finite(velocityZ);
        this.ageSeconds = Math.max(0.0D, finite(ageSeconds));
        this.lifetimeSeconds = Math.max(0.05D, finite(lifetimeSeconds));
        this.size = Math.max(0.015D, finite(size));
        this.argb = ((argb >>> 24) & 0xFF) == 0 ? 0xFF9AA0A8 : argb;
    }

    String particleId() {
        return particleId;
    }

    EchoClientWorldFeedbackKind kind() {
        return kind;
    }

    double x() {
        return x;
    }

    double y() {
        return y;
    }

    double z() {
        return z;
    }

    double velocityX() {
        return velocityX;
    }

    double velocityY() {
        return velocityY;
    }

    double velocityZ() {
        return velocityZ;
    }

    double ageSeconds() {
        return ageSeconds;
    }

    double lifetimeSeconds() {
        return lifetimeSeconds;
    }

    double size() {
        return size;
    }

    int argb() {
        return argb;
    }

    boolean alive() {
        return ageSeconds < lifetimeSeconds;
    }

    double lifeProgress() {
        return Math.max(0.0D, Math.min(1.0D, ageSeconds / lifetimeSeconds));
    }

    double renderSize() {
        return size * (1.0D - lifeProgress() * 0.45D);
    }

    int renderArgb() {
        int alpha = Math.max(64, (int) Math.round(255.0D * (1.0D - lifeProgress() * 0.65D)));
        return (alpha << 24) | (argb & 0x00FFFFFF);
    }

    EchoClientParticle tick(double dt) {
        EchoClientParticle next = copy();
        next.tickInPlace(dt);
        return next;
    }

    boolean tickInPlace(double dt) {
        double elapsed = Math.max(0.0D, dt);
        double safeDt = Math.min(0.10D, elapsed);
        double nextVelocityY = velocityY - 3.5D * safeDt;
        x += velocityX * safeDt;
        y += nextVelocityY * safeDt;
        z += velocityZ * safeDt;
        velocityX *= 0.94D;
        velocityY = nextVelocityY * 0.94D;
        velocityZ *= 0.94D;
        ageSeconds += elapsed;
        return alive();
    }

    private EchoClientParticle copy() {
        return new EchoClientParticle(
                particleId,
                kind,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ,
                ageSeconds,
                lifetimeSeconds,
                size,
                argb
        );
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0.0D;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EchoClientParticle particle)) {
            return false;
        }
        return Double.compare(x, particle.x) == 0
                && Double.compare(y, particle.y) == 0
                && Double.compare(z, particle.z) == 0
                && Double.compare(velocityX, particle.velocityX) == 0
                && Double.compare(velocityY, particle.velocityY) == 0
                && Double.compare(velocityZ, particle.velocityZ) == 0
                && Double.compare(ageSeconds, particle.ageSeconds) == 0
                && Double.compare(lifetimeSeconds, particle.lifetimeSeconds) == 0
                && Double.compare(size, particle.size) == 0
                && argb == particle.argb
                && particleId.equals(particle.particleId)
                && kind == particle.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                particleId,
                kind,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ,
                ageSeconds,
                lifetimeSeconds,
                size,
                argb
        );
    }

    @Override
    public String toString() {
        return "EchoClientParticle[particleId=" + particleId
                + ", kind=" + kind
                + ", x=" + x
                + ", y=" + y
                + ", z=" + z
                + ", velocityX=" + velocityX
                + ", velocityY=" + velocityY
                + ", velocityZ=" + velocityZ
                + ", ageSeconds=" + ageSeconds
                + ", lifetimeSeconds=" + lifetimeSeconds
                + ", size=" + size
                + ", argb=" + argb
                + "]";
    }
}
