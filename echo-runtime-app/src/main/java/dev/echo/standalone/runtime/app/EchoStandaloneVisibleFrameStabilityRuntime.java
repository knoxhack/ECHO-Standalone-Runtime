package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.List;
import java.util.Objects;

public final class EchoStandaloneVisibleFrameStabilityRuntime {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;
    private static final int FRAME_COUNT = 5;
    private static final int SKY_TOP = 0xFF07151A;
    private static final int SKY_BOTTOM = 0xFF132423;

    public EchoStandaloneVisibleFrameStabilityResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
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
        EchoVoxelPlayerHotbar hotbar = sessionProfile.newStarterHotbar();
        hotbar.add(bridge.waterRationItem(), 2);
        EchoAshfallLiveMissionState mission = EchoAshfallLiveMissionState.restored(
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                false,
                false,
                false,
                1,
                1,
                1,
                100,
                86.0D,
                78.0D,
                12.0D,
                "visible frame stability: terminal route open"
        );
        EchoVoxelHudOverlay overlay = new EchoVoxelHudOverlay(
                hotbar,
                mission,
                bridge.runtimeSummary(),
                "echo:opengl_client_framebuffer_presenter",
                bridge.registrySummary(),
                "visible frame stability",
                "no white screen / no flicker smoke",
                "spawn yaw=" + Math.round(world.spawnYawDegrees()),
                "playing",
                bridge.bindingCoverageSummary(),
                "opengl game presenter",
                "framebuffer upload stable",
                true,
                true,
                true,
                world.loadedChunkCount(),
                true,
                false,
                false,
                false,
                "",
                List.of()
        );

        EchoVoxelSoftwareRenderer renderer = new EchoVoxelSoftwareRenderer();
        EchoVoxelHudFramebufferCompositor compositor = new EchoVoxelHudFramebufferCompositor();
        int minUniqueColors = Integer.MAX_VALUE;
        int minFacesDrawn = Integer.MAX_VALUE;
        int minBlockPixels = Integer.MAX_VALUE;
        int maxWhitePixels = 0;
        boolean dimensionsStable = true;
        boolean noWhiteFrames = true;
        boolean noBlankFrames = true;
        boolean stableChecksums = true;
        long stableChecksum = 0L;

        for (int index = 0; index < FRAME_COUNT; index++) {
            EchoVoxelFramebuffer base = renderer.render(world, camera, WIDTH, HEIGHT);
            EchoVoxelFramebuffer visible = compositor.composite(base, overlay);
            dimensionsStable &= visible.width() == WIDTH
                    && visible.height() == HEIGHT
                    && visible.argb().length == WIDTH * HEIGHT;
            int uniqueColors = visible.uniqueColorCount();
            int whitePixels = whitePixels(visible);
            int blockPixels = nonSkyPixels(base);
            minUniqueColors = Math.min(minUniqueColors, uniqueColors);
            minFacesDrawn = Math.min(minFacesDrawn, base.facesDrawn());
            minBlockPixels = Math.min(minBlockPixels, blockPixels);
            maxWhitePixels = Math.max(maxWhitePixels, whitePixels);
            noWhiteFrames &= whitePixels < (WIDTH * HEIGHT) / 100;
            noBlankFrames &= uniqueColors >= 40 && blockPixels >= 20_000 && base.facesDrawn() >= 400;
            if (index == 0) {
                stableChecksum = visible.checksum();
            } else {
                stableChecksums &= stableChecksum == visible.checksum();
            }
        }

        return new EchoStandaloneVisibleFrameStabilityResult(
                "echo:opengl-visible-frame-stability",
                FRAME_COUNT,
                WIDTH,
                HEIGHT,
                bridge.supportsAllAdapterCoreRuntimes(),
                dimensionsStable,
                noWhiteFrames,
                noBlankFrames,
                stableChecksums,
                minBlockPixels >= 20_000 && minFacesDrawn >= 400,
                minUniqueColors,
                minFacesDrawn,
                minBlockPixels,
                maxWhitePixels,
                stableChecksum
        );
    }

    private static int nonSkyPixels(EchoVoxelFramebuffer framebuffer) {
        int changed = 0;
        int[] pixels = framebuffer.argb();
        for (int y = 0; y < framebuffer.height(); y++) {
            int sky = skyColor(y, framebuffer.height());
            int offset = y * framebuffer.width();
            for (int x = 0; x < framebuffer.width(); x++) {
                int color = pixels[offset + x];
                if (!nearColor(color, sky, 3)) {
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int whitePixels(EchoVoxelFramebuffer framebuffer) {
        int white = 0;
        for (int color : framebuffer.argb()) {
            int red = (color >>> 16) & 0xFF;
            int green = (color >>> 8) & 0xFF;
            int blue = color & 0xFF;
            if (red >= 245 && green >= 245 && blue >= 245) {
                white++;
            }
        }
        return white;
    }

    private static int skyColor(int y, int height) {
        double t = height <= 1 ? 0.0D : (double) y / (double) (height - 1);
        return mix(SKY_TOP, SKY_BOTTOM, t);
    }

    private static int mix(int left, int right, double t) {
        int a = channel(left, 24);
        int r = (int) Math.round(channel(left, 16) + (channel(right, 16) - channel(left, 16)) * t);
        int g = (int) Math.round(channel(left, 8) + (channel(right, 8) - channel(left, 8)) * t);
        int b = (int) Math.round(channel(left, 0) + (channel(right, 0) - channel(left, 0)) * t);
        return (a << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
    }

    private static int channel(int color, int shift) {
        return (color >>> shift) & 0xFF;
    }

    private static boolean nearColor(int left, int right, int tolerance) {
        return Math.abs(channel(left, 16) - channel(right, 16)) <= tolerance
                && Math.abs(channel(left, 8) - channel(right, 8)) <= tolerance
                && Math.abs(channel(left, 0) - channel(right, 0)) <= tolerance;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
