package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandaloneVoxelMaterialAtlasResult;
import dev.echo.standalone.runtime.app.EchoStandaloneVoxelMaterialAtlasRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

public final class EchoRuntimeVoxelMaterialAtlasSmokeHarness {
    private EchoRuntimeVoxelMaterialAtlasSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoStandaloneVoxelMaterialAtlasResult result = new EchoStandaloneVoxelMaterialAtlasRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive()
        );

        require(result.adapterCoreMultiRuntimeReady(),
                "material atlas should keep NeoForge, ECHO Native Loader, and standalone AdapterCore bindings");
        require(result.atlasKeyCount() >= 7,
                "Ashfall block materials should expose distinct atlas keys: " + result.summary());
        require(result.materialPatternCount() >= 7,
                "Ashfall block materials should expose distinct render patterns: " + result.summary());
        require(result.patternedFaceCount() > 400,
                "voxel meshing should preserve patterned material faces: " + result.summary());
        require(result.uniqueFramebufferColors() > 48,
                "software framebuffer should show material pattern color variation before OpenGL upload: "
                        + result.summary());
        require(result.framebufferChecksum() != 0L,
                "software framebuffer checksum should be non-zero: " + result.summary());
        require(result.ready(), "AdapterCore voxel material atlas should be ready: " + result.summary());

        System.out.println("phase15.material atlas smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
