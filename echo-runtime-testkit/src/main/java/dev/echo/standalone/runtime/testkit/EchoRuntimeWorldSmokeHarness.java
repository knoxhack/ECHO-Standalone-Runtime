package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldSaveResult;
import dev.echo.standalone.runtime.world.EchoWorldState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class EchoRuntimeWorldSmokeHarness {
    private EchoRuntimeWorldSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoWorldState state = world.world();

        require(services.require(EchoWorldRuntimeResult.class) == world, "world runtime result should be service-bound");
        require(services.require(EchoWorldState.class) == state, "world state should be service-bound");
        require(state.dimensionCount() == 1, "debug world should contain one dimension");
        require(state.dimension("ashfall:surface").orElseThrow().regionIds().contains("ashfall:crash_site"),
                "Ashfall surface dimension should own the crash site region");
        require(state.regions().size() == 1, "debug world should contain one region");
        require(state.chunks().size() == 1, "debug world should contain one chunk");
        require(state.cellCount() == 16, "debug world should contain a 4x4 cell grid");
        require(state.hazardCount() == 1, "debug world should contain one hazard");
        require(state.poiCount() == 2, "debug world should contain two POIs");
        require(world.query().cellAt(new EchoWorldPosition(0, 0, 0)).orElseThrow().terrain().equals("crash_debris"),
                "origin cell should be crash debris");
        require(world.query().hazardIntensityAt(new EchoWorldPosition(1, 0, 1)) == 0.72D,
                "hazard center should report toxic ash intensity");
        require(world.query().poi("ashfall:terminal_pod").orElseThrow().label().equals("Emergency Terminal Pod"),
                "terminal POI should be queryable");
        require(world.query().dimension("ashfall:surface").orElseThrow().environment().equals("toxic_wasteland"),
                "Ashfall surface dimension should be queryable");

        Path fixtureRoot = Files.createTempDirectory("echo-runtime-world-smoke");
        EchoSaveProfile saveProfile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-world",
                "Ashfall World",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/ashfall-world"),
                Map.of("phase", "14.9")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(services, saveProfile);
        EchoWorldSaveResult saved = world.saveHook().save(saves, "slot-world", "tx-world-001");
        require(saved.commit().filesWritten() == 2, "world save hook should write summary and one chunk");
        require(saved.writtenPaths().contains("world/summary.json"), "world summary should be written");
        require(saved.writtenPaths().contains("world/chunks/0_0.json"), "chunk summary should be written");

        EchoSaveManifest manifest = saves.readManifest("slot-world");
        require(manifest.file("world/summary.json").isPresent(), "manifest should track world summary");
        require(manifest.file("world/chunks/0_0.json").isPresent(), "manifest should track chunk summary");
        EchoSaveCorruptionReport saveCheck = saves.check("slot-world");
        require(saveCheck.healthy(), "world save should pass corruption check");

        System.out.println("phase14.9 world runtime smoke PASS regions="
                + state.regions().size()
                + " dimensions="
                + state.dimensionCount()
                + " chunks="
                + state.chunks().size()
                + " cells="
                + state.cellCount()
                + " hazards="
                + state.hazardCount()
                + " poi="
                + state.poiCount()
                + " savedFiles="
                + saved.writtenPaths().size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
