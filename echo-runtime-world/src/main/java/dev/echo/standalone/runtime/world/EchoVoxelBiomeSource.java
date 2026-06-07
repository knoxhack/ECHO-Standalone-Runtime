package dev.echo.standalone.runtime.world;

public interface EchoVoxelBiomeSource {
    String id();

    EchoVoxelBiome biomeAt(long seed, int x, int z);

    default EchoVoxelBiome biomeAt(long seed, double x, double z) {
        return biomeAt(seed, (int) Math.floor(x), (int) Math.floor(z));
    }
}
