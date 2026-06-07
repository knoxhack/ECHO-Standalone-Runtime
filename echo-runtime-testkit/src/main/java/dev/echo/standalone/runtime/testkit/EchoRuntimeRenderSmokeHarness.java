package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderBackend;
import dev.echo.standalone.runtime.render.EchoRenderCommandType;
import dev.echo.standalone.runtime.render.EchoRenderFrame;
import dev.echo.standalone.runtime.render.EchoRenderLayer;
import dev.echo.standalone.runtime.render.EchoRenderRuntime;
import dev.echo.standalone.runtime.render.EchoRenderRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderScene;
import dev.echo.standalone.runtime.render.EchoRenderWindowMode;
import dev.echo.standalone.runtime.render.EchoRenderWindowSettings;
import dev.echo.standalone.runtime.render.EchoRenderWindowState;
import dev.echo.standalone.runtime.render.EchoSoftwareFramebuffer;
import dev.echo.standalone.runtime.render.EchoSoftwareRenderBackend;
import dev.echo.standalone.runtime.render.EchoSoftwareRenderPass;
import dev.echo.standalone.runtime.render.EchoSoftwareRenderStats;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;

public final class EchoRuntimeRenderSmokeHarness {
    private EchoRuntimeRenderSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoStaticScreen(
                        "ashfall-hud",
                        "Ashfall HUD",
                        List.of("Mission: Secure The Crash Site", "Hydration: 55"),
                        "hud:mission"
                ),
                EchoUiTheme.defaultTerminal()
        );
        EchoRenderRuntimeResult render = new EchoRenderRuntime().createDebugRenderer(
                services,
                world,
                entities,
                gameplay,
                ui,
                EchoRenderWindowSettings.headlessDebug()
        );
        EchoRenderScene scene = render.initialScene();
        EchoRenderFrame frame = render.initialFrame();
        EchoRenderWindowState window = render.window();

        require(services.require(EchoRenderRuntimeResult.class) == render,
                "render runtime result should be service-bound");
        require(services.require(EchoRenderBackend.class) == render.backend(),
                "render backend should be service-bound");
        require(services.require(EchoRenderWindowState.class) == window,
                "render window state should be service-bound");
        require(services.require(EchoRenderScene.class) == scene,
                "render scene should be service-bound");
        require(services.require(EchoRenderFrame.class) == frame,
                "render frame should be service-bound");
        require(render.backend().backendId().equals("echo:software_renderer"),
                "software backend should be used by default");
        require(render.backend() instanceof EchoSoftwareRenderBackend,
                "default backend should expose software framebuffer output");
        require(window.open(), "headless render window should be open");
        require(window.mode() == EchoRenderWindowMode.HEADLESS, "window mode should be headless");
        require(window.viewport().width() == 1280 && window.viewport().height() == 720,
                "debug viewport should be 1280x720");

        require(scene.camera().cameraId().equals("ashfall-debug-camera"), "debug camera should be used");
        require(scene.commands().size() == 29, "debug scene should contain 29 render commands");
        require(scene.commandCount(EchoRenderLayer.BACKGROUND) == 1, "scene should contain one background command");
        require(scene.commandCount(EchoRenderLayer.WORLD) == 16, "scene should contain 16 world tiles");
        require(scene.commandCount(EchoRenderLayer.ENTITY) == 2, "scene should contain two entity commands");
        require(scene.commandCount(EchoRenderLayer.PARTICLE) == 5, "scene should contain five particles");
        require(scene.commandCount(EchoRenderLayer.UI) == 4, "scene should contain four UI bridge commands");
        require(scene.commandCount(EchoRenderLayer.DIAGNOSTIC) == 1, "scene should contain one diagnostic command");
        require(scene.commands().stream().anyMatch(command -> command.material().equals("world:blocked")),
                "blocked world tile should be represented");
        require(scene.commands().stream().anyMatch(command -> command.material().equals("entity:player")),
                "player entity should be represented");
        require(scene.commands().stream().anyMatch(command -> command.material().equals("entity:hostile")),
                "hostile entity should be represented");
        require(scene.commands().stream().anyMatch(command -> command.type() == EchoRenderCommandType.PARTICLE
                        && command.material().equals("particle:ash:ashfall:ash_storm")),
                "ash storm particles should be represented");
        require(scene.commands().stream().anyMatch(command -> command.label().equals("Ashfall HUD")),
                "UI bridge should include HUD surface");
        require(scene.commands().stream().anyMatch(command -> command.label().equals("mission=ACTIVE")),
                "diagnostic layer should include mission status");

        require(frame.frameIndex() == 0, "first frame index should be zero");
        require(frame.submittedCommandCount() == scene.commands().size(),
                "frame should submit all scene commands");
        require(frame.diagnostics().size() == 2, "frame should record software backend diagnostics");
        require(render.backend().frames().size() == 1, "software backend should retain one frame");

        EchoSoftwareRenderBackend software = (EchoSoftwareRenderBackend) render.backend();
        EchoSoftwareFramebuffer framebuffer = software.lastFramebuffer().orElseThrow();
        EchoSoftwareRenderStats stats = framebuffer.stats();
        require(framebuffer.width() == 1280 && framebuffer.height() == 720,
                "software framebuffer should match the viewport");
        require(stats.passes().equals(List.of(
                        EchoSoftwareRenderPass.CLEAR,
                        EchoSoftwareRenderPass.TILES,
                        EchoSoftwareRenderPass.SPRITES,
                        EchoSoftwareRenderPass.UI,
                        EchoSoftwareRenderPass.LIGHTING,
                        EchoSoftwareRenderPass.PARTICLES,
                        EchoSoftwareRenderPass.DEBUG_OVERLAY
                )),
                "software renderer should run the Phase 15.5 pass order");
        require(stats.clearCommands() == 1, "software renderer should process the clear pass");
        require(stats.tileCommands() == 16, "software renderer should rasterize 16 tiles");
        require(stats.spriteCommands() == 2, "software renderer should rasterize two entity sprites");
        require(stats.uiCommands() == 4, "software renderer should rasterize four UI commands");
        require(stats.particleCommands() == 5, "software renderer should rasterize five particles");
        require(stats.debugCommands() == 1, "software renderer should rasterize diagnostic overlays");
        require(stats.litPixels() == framebuffer.width() * framebuffer.height(),
                "software renderer should apply lighting to the framebuffer");
        require(stats.nonBackgroundPixels() > 0, "software framebuffer should contain drawn pixels");
        require(stats.checksum() != 0L, "software framebuffer checksum should be non-zero");
        require(framebuffer.pixel(550, 220) != framebuffer.pixel(10, 10),
                "player sprite sample should differ from the background sample");
        require(framebuffer.pixel(780, 500) != framebuffer.pixel(10, 10),
                "blocked tile sample should differ from the background sample");
        require(framebuffer.pixel(20, 20) != framebuffer.pixel(10, 10),
                "UI sample should differ from the background sample");

        EchoRenderRuntimeResult recording = new EchoRenderRuntime().createRecordingDebugRenderer(
                new EchoDefaultRuntimeServiceRegistry(),
                world,
                entities,
                gameplay,
                ui,
                EchoRenderWindowSettings.headlessDebug()
        );
        require(recording.backend().backendId().equals("echo:recording_renderer"),
                "recording backend should remain available for deterministic command audits");
        require(recording.backend().frames().size() == 1,
                "recording backend compatibility path should still retain one frame");

        System.out.println("phase15.5 renderer upgrade smoke PASS backend="
                + render.backend().backendId()
                + " commands="
                + scene.commands().size()
                + " world="
                + scene.commandCount(EchoRenderLayer.WORLD)
                + " entities="
                + scene.commandCount(EchoRenderLayer.ENTITY)
                + " particles="
                + scene.commandCount(EchoRenderLayer.PARTICLE)
                + " ui="
                + scene.commandCount(EchoRenderLayer.UI)
                + " frames="
                + render.backend().frames().size()
                + " checksum="
                + Long.toUnsignedString(stats.checksum()));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
