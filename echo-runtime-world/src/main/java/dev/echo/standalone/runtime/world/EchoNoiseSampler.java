package dev.echo.standalone.runtime.world;

/**
 * Simple value and gradient noise for terrain generation.
 * Deterministic for a given seed.
 */
public final class EchoNoiseSampler {
    private final long seed;

    public EchoNoiseSampler(long seed) {
        this.seed = seed;
    }

    /**
     * Sample noise in 2D at the given world coordinates.
     * Returns a value in approximately [-1, 1].
     */
    public double sample(double x, double z) {
        return sample(x, 0.0D, z);
    }

    /**
     * Sample noise in 3D at the given world coordinates.
     * Returns a value in approximately [-1, 1].
     */
    public double sample(double x, double y, double z) {
        double floorX = Math.floor(x);
        double floorY = Math.floor(y);
        double floorZ = Math.floor(z);
        int ix = (int) floorX;
        int iy = (int) floorY;
        int iz = (int) floorZ;
        double fracX = x - floorX;
        double fracY = y - floorY;
        double fracZ = z - floorZ;

        double n000 = gradient(seed, ix, iy, iz, fracX, fracY, fracZ);
        double n100 = gradient(seed, ix + 1, iy, iz, fracX - 1.0D, fracY, fracZ);
        double n010 = gradient(seed, ix, iy + 1, iz, fracX, fracY - 1.0D, fracZ);
        double n110 = gradient(seed, ix + 1, iy + 1, iz, fracX - 1.0D, fracY - 1.0D, fracZ);
        double n001 = gradient(seed, ix, iy, iz + 1, fracX, fracY, fracZ - 1.0D);
        double n101 = gradient(seed, ix + 1, iy, iz + 1, fracX - 1.0D, fracY, fracZ - 1.0D);
        double n011 = gradient(seed, ix, iy + 1, iz + 1, fracX, fracY - 1.0D, fracZ - 1.0D);
        double n111 = gradient(seed, ix + 1, iy + 1, iz + 1, fracX - 1.0D, fracY - 1.0D, fracZ - 1.0D);

        double u = smooth(fracX);
        double v = smooth(fracY);
        double w = smooth(fracZ);

        double nx00 = lerp(n000, n100, u);
        double nx10 = lerp(n010, n110, u);
        double nx01 = lerp(n001, n101, u);
        double nx11 = lerp(n011, n111, u);

        double nxy0 = lerp(nx00, nx10, v);
        double nxy1 = lerp(nx01, nx11, v);

        return lerp(nxy0, nxy1, w);
    }

    /**
     * Sample octave noise (fractal Brownian motion).
     */
    public double sampleOctave(double x, double y, double z, int octaves, double persistence, double lacunarity) {
        double total = 0.0D;
        double amplitude = 1.0D;
        double frequency = 1.0D;
        double maxValue = 0.0D;
        for (int i = 0; i < octaves; i++) {
            total += sample(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxValue;
    }

    private static double gradient(long seed, int x, int y, int z, double dx, double dy, double dz) {
        int hash = mix(mix(mix(seed, x), y), z);
        int h = hash & 15;
        double u = h < 8 ? dx : dy;
        double v = h < 4 ? dy : (h == 12 || h == 14 ? dx : dz);
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    private static int mix(long seed, int value) {
        long k = value;
        k *= 0x5bd1e995L;
        k ^= k >>> 24;
        k *= 0x5bd1e995L;
        long s = seed ^ k;
        s *= 0x5bd1e995L;
        s ^= s >>> 13;
        s *= 0x5bd1e995L;
        s ^= s >>> 15;
        return (int) s;
    }

    private static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double smooth(double t) {
        return t * t * t * (t * (t * 6.0D - 15.0D) + 10.0D);
    }
}
