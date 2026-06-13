package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelRenderPacket;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesher;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        EchoVoxelWorld showcaseWorld = materialShowcaseWorld(bridge);
        EchoVoxelCamera showcaseCamera = new EchoVoxelCamera(8.0D, 5.5D, -8.0D, 0.0D, -18.0D, 70.0D);
        EchoVoxelFramebuffer framebuffer = new EchoVoxelSoftwareRenderer().render(showcaseWorld, showcaseCamera, WIDTH, HEIGHT);
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

    private static EchoVoxelWorld materialShowcaseWorld(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        List<EchoVoxelBlock> blocks = showcaseBlocks(bridge);
        for (int index = 0; index < blocks.size(); index++) {
            int x = 2 + (index % 6) * 2;
            int z = 4 + (index / 6) * 4;
            chunk.setBlockLocal(x, 1, z, blocks.get(index));
        }
        return new EchoVoxelWorld(
                "echo:adaptercore_material_showcase",
                42L,
                16,
                List.of(chunk),
                8.0D,
                2.0D,
                8.0D,
                0.0D
        );
    }

    private static List<EchoVoxelBlock> showcaseBlocks(EchoAdapterCoreStandaloneContentBridge bridge) {
        LinkedHashMap<EchoVoxelMaterialPattern, EchoVoxelBlock> byPattern = new LinkedHashMap<>();
        ArrayList<EchoVoxelBlock> byAtlas = new ArrayList<>();
        for (EchoAdapterCoreRegistryEntry entry : bridge.registry().blocks()) {
            EchoVoxelBlock block = entry.voxelBlock().orElse(null);
            if (block == null || block.air()) {
                continue;
            }
            byPattern.putIfAbsent(block.materialPattern(), block);
            if (byAtlas.stream().noneMatch(existing -> existing.atlasKey().equals(block.atlasKey()))) {
                byAtlas.add(block);
            }
        }
        ArrayList<EchoVoxelBlock> result = new ArrayList<>(byPattern.values());
        for (EchoVoxelBlock block : byAtlas) {
            if (result.size() >= 18) {
                break;
            }
            if (result.stream().noneMatch(existing -> existing.id().equals(block.id()))) {
                result.add(block);
            }
        }
        return List.copyOf(result);
    }
}
