package dev.echo.standalone.runtime.world;

public record EchoWorldGenerationSettings(
        String worldId,
        long seed,
        int chunkSize,
        String regionId,
        String dimensionId,
        EchoWorldDebugProfile profile
) {
    public EchoWorldGenerationSettings {
        worldId = EchoWorldText.requireText(worldId, "worldId");
        if (chunkSize < 1) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        regionId = EchoWorldText.requireText(regionId, "regionId");
        dimensionId = EchoWorldText.requireText(dimensionId, "dimensionId");
        profile = profile == null ? EchoWorldDebugProfile.generic() : profile;
    }

    public EchoWorldGenerationSettings(
            String worldId,
            long seed,
            int chunkSize,
            String regionId,
            String dimensionId
    ) {
        this(worldId, seed, chunkSize, regionId, dimensionId, EchoWorldDebugProfile.generic());
    }

}
