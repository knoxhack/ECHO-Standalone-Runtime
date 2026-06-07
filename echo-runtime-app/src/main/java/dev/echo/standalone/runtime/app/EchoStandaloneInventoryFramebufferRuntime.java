package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoIndexStandaloneAdapter;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoStandaloneInventoryFramebufferRuntime {
    private static final int WIDTH = 640;
    private static final int HEIGHT = 360;

    public EchoStandaloneInventoryFramebufferResult run(EchoAdapterCoreStandaloneContentBridge bridge) {
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
        EchoAshfallLiveMissionState mission = new EchoAshfallLiveMissionState();
        EchoIndexStandaloneAdapter index = new EchoIndexStandaloneAdapter();
        Map<String, Object> indexOverlay = index.inventoryOverlay(index.executeQuery(EchoIndexStandaloneAdapter.REFERENCE_QUERY));
        List<String> indexActions = strings(indexOverlay.get("actions"));
        List<String> indexRecipeRows = strings(indexOverlay.get("recipeRows"));
        EchoVoxelHudOverlay overlay = new EchoVoxelHudOverlay(
                hotbar,
                mission,
                bridge.runtimeSummary(),
                "echo:opengl_client_framebuffer_presenter",
                bridge.registrySummary(),
                "inventory target blocked",
                "inventory framebuffer smoke",
                "spawn yaw=" + Math.round(world.spawnYawDegrees()),
                "inventory-open",
                bridge.bindingCoverageSummary(),
                "opengl game presenter",
                "framebuffer upload ready",
                false,
                true,
                true,
                world.loadedChunkCount(),
                true,
                true,
                false,
                false,
                "Inventory",
                List.of(
                        "Index: " + indexOverlay.get("surfaceId"),
                        "Search focus: " + indexOverlay.get("focusedControl"),
                        "Rows: " + String.join(", ", indexRecipeRows),
                        "Actions: R recipes / U uses / B bookmark",
                        "E / Esc: Back to game",
                        "1-9: Select hotbar slot",
                        "F5: Manual Save",
                        "Mouse remains released"
                )
        );
        EchoVoxelFramebuffer base = new EchoVoxelSoftwareRenderer().render(world, camera, WIDTH, HEIGHT);
        EchoVoxelFramebuffer inventory = new EchoVoxelHudFramebufferCompositor().composite(base, overlay);
        return new EchoStandaloneInventoryFramebufferResult(
                "echo:opengl-visible-inventory-framebuffer",
                inventory.width(),
                inventory.height(),
                bridge.supportsAllAdapterCoreRuntimes(),
                base.width() == inventory.width()
                        && base.height() == inventory.height()
                        && base.argb().length == inventory.argb().length,
                base.checksum() != inventory.checksum(),
                Boolean.TRUE.equals(indexOverlay.get("visible")),
                String.valueOf(indexOverlay.get("surfaceId")),
                String.valueOf(indexOverlay.get("focusedControl")),
                indexActions.size(),
                indexRecipeRows.size(),
                changedPixels(
                        base,
                        inventory,
                        Math.max(0, WIDTH / 2 - 260),
                        Math.max(0, HEIGHT / 2 - 150),
                        Math.min(520, WIDTH),
                        Math.min(300, HEIGHT)
                ),
                (int) hotbar.slots().stream().filter(slot -> !slot.empty()).count(),
                base.checksum(),
                inventory.checksum()
        );
    }

    private static int changedPixels(
            EchoVoxelFramebuffer before,
            EchoVoxelFramebuffer after,
            int x,
            int y,
            int width,
            int height
    ) {
        int startX = clamp(x, 0, before.width());
        int startY = clamp(y, 0, before.height());
        int endX = clamp(x + width, startX, before.width());
        int endY = clamp(y + height, startY, before.height());
        int changed = 0;
        int[] beforePixels = before.argb();
        int[] afterPixels = after.argb();
        for (int row = startY; row < endY; row++) {
            int offset = row * before.width();
            for (int column = startX; column < endX; column++) {
                if (beforePixels[offset + column] != afterPixels[offset + column]) {
                    changed++;
                }
            }
        }
        return changed;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
