package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelRenderPacket;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesher;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.Objects;

public final class EchoStandaloneVoxelMaterialAtlasRuntime {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public EchoStandaloneVoxelMaterialAtlasResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorld world = sessionProfile.generate(42L, 0);
        world = sessionProfile.streamer().streamAround(world, world.spawnX(), world.spawnZ());
        EchoVoxelCamera camera = new EchoVoxelCamera(
                world.spawnX(),
                world.spawnY() + 1.2D,
                world.spawnZ(),
                world.spawnYawDegrees(),
                -22.0D,
                70.0D
        );
        EchoVoxelRenderPacket packet = new EchoVoxelChunkMesher().buildPacket(world, camera);
        EchoVoxelFramebuffer framebuffer = new EchoVoxelSoftwareRenderer().render(world, camera, WIDTH, HEIGHT);
        return new EchoStandaloneVoxelMaterialAtlasResult(
                "echo:adaptercore-voxel-material-atlas",
                bridge.supportsAllAdapterCoreRuntimes(),
                packet.materialAtlasKeys().size(),
                packet.materialPatternCount(),
                packet.patternedFaceCount(),
                framebuffer.uniqueColorCount(),
                framebuffer.checksum(),
                bridge.materialAtlasSummary()
        );
    }
}
