package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderBackend;
import dev.echo.standalone.runtime.render.EchoRenderCamera;
import dev.echo.standalone.runtime.render.EchoRenderCommand;
import dev.echo.standalone.runtime.render.EchoRenderCommandType;
import dev.echo.standalone.runtime.render.EchoRenderDiagnostic;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class EchoRuntimeRenderSmokeHarness {
    private EchoRuntimeRenderSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
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

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                render,
                recording,
                framebuffer
        );

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

    private static void writeReports(
            Path standaloneRoot,
            EchoRenderRuntimeResult render,
            EchoRenderRuntimeResult recording,
            EchoSoftwareFramebuffer framebuffer
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);

        EchoRenderScene scene = render.initialScene();
        EchoRenderFrame frame = render.initialFrame();
        EchoRenderWindowState window = render.window();
        EchoSoftwareRenderStats stats = framebuffer.stats();
        int backgroundSample = framebuffer.pixel(10, 10);
        int playerSample = framebuffer.pixel(550, 220);
        int blockedTileSample = framebuffer.pixel(780, 500);
        int uiSample = framebuffer.pixel(20, 20);

        write(root.resolve("runtime-render.json"), """
                {
                  "schema": "echo.standalone.runtime_render.v2",
                  "status": "PASS",
                  "phase": "15.5",
                  "summary": "Renderer runtime created service-bound headless software rendering, built the Ashfall debug scene, rasterized a real ARGB framebuffer, bridged UI surfaces, and retained recording backend compatibility.",
                  "runtimeResultServiceBound": true,
                  "backendServiceBound": true,
                  "windowServiceBound": true,
                  "sceneServiceBound": true,
                  "frameServiceBound": true,
                  "backendId": "%s",
                  "recordingBackendId": "%s",
                  "windowMode": "%s",
                  "viewportWidth": %d,
                  "viewportHeight": %d,
                  "sceneId": "%s",
                  "commandCount": %d,
                  "frameIndex": %d,
                  "submittedCommandCount": %d,
                  "frameCount": %d,
                  "framebufferWidth": %d,
                  "framebufferHeight": %d,
                  "nonBackgroundPixels": %d,
                  "checksum": "%s",
                  "recordingFrameCount": %d
                }
                """.formatted(
                escape(render.backend().backendId()),
                escape(recording.backend().backendId()),
                window.mode().name(),
                window.viewport().width(),
                window.viewport().height(),
                escape(scene.sceneId()),
                scene.commands().size(),
                frame.frameIndex(),
                frame.submittedCommandCount(),
                render.backend().frames().size(),
                framebuffer.width(),
                framebuffer.height(),
                stats.nonBackgroundPixels(),
                Long.toUnsignedString(stats.checksum()),
                recording.backend().frames().size()
        ));

        write(root.resolve("render-backend.json"), """
                {
                  "schema": "echo.standalone.render_backend.v2",
                  "status": "PASS",
                  "backendId": "%s",
                  "backendClass": "%s",
                  "softwareBackend": %s,
                  "recordingBackendAvailable": %s,
                  "recordingBackendId": "%s",
                  "framesRetained": %d,
                  "framebufferRetained": %s,
                  "displayFree": true,
                  "lwjglFree": true,
                  "gpuContextFree": true,
                  "awtFree": true,
                  "jfxFree": true
                }
                """.formatted(
                escape(render.backend().backendId()),
                escape(render.backend().getClass().getName()),
                render.backend() instanceof EchoSoftwareRenderBackend,
                "echo:recording_renderer".equals(recording.backend().backendId()),
                escape(recording.backend().backendId()),
                render.backend().frames().size(),
                ((EchoSoftwareRenderBackend) render.backend()).lastFramebuffer().isPresent()
        ));

        write(root.resolve("render-window.json"), """
                {
                  "schema": "echo.standalone.render_window.v2",
                  "status": "PASS",
                  "windowId": "%s",
                  "title": "%s",
                  "mode": "%s",
                  "open": %s,
                  "closeRequested": %s,
                  "viewportWidth": %d,
                  "viewportHeight": %d,
                  "headless": %s,
                  "logicalWindowOnly": true
                }
                """.formatted(
                escape(window.windowId()),
                escape(window.title()),
                window.mode().name(),
                window.open(),
                window.closeRequested(),
                window.viewport().width(),
                window.viewport().height(),
                window.mode() == EchoRenderWindowMode.HEADLESS
        ));

        write(root.resolve("render-scene.json"), """
                {
                  "schema": "echo.standalone.render_scene.v2",
                  "status": "PASS",
                  "sceneId": "%s",
                  "commandCount": %d,
                  "submittedCommandCount": %d,
                  "commandTypes": %s,
                  "materials": %s,
                  "commands": %s
                }
                """.formatted(
                escape(scene.sceneId()),
                scene.commands().size(),
                frame.submittedCommandCount(),
                stringArray(scene.commands().stream()
                        .map(command -> command.type().name())
                        .distinct()
                        .sorted()
                        .toList()),
                stringArray(scene.commands().stream()
                        .map(EchoRenderCommand::material)
                        .distinct()
                        .sorted()
                        .toList()),
                commandsJson(scene.commands())
        ));

        write(root.resolve("render-camera.json"), """
                {
                  "schema": "echo.standalone.render_camera.v2",
                  "status": "PASS",
                  "camera": %s,
                  "cameraId": "%s",
                  "zoom": %.2f,
                  "pitchDegrees": %.2f,
                  "sampledPlayerPixelDiffersFromBackground": %s,
                  "sampledBlockedTileDiffersFromBackground": %s,
                  "sampledUiPixelDiffersFromBackground": %s
                }
                """.formatted(
                cameraJson(scene.camera()),
                escape(scene.camera().cameraId()),
                scene.camera().zoom(),
                scene.camera().pitchDegrees(),
                playerSample != backgroundSample,
                blockedTileSample != backgroundSample,
                uiSample != backgroundSample
        ));

        write(root.resolve("render-layers.json"), """
                {
                  "schema": "echo.standalone.render_layers.v2",
                  "status": "PASS",
                  "background": %d,
                  "world": %d,
                  "entity": %d,
                  "particle": %d,
                  "ui": %d,
                  "diagnostic": %d,
                  "statsClearCommands": %d,
                  "statsTileCommands": %d,
                  "statsSpriteCommands": %d,
                  "statsParticleCommands": %d,
                  "statsUiCommands": %d,
                  "statsDebugCommands": %d,
                  "softwarePasses": %s,
                  "passOrderMatches": %s
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.BACKGROUND),
                scene.commandCount(EchoRenderLayer.WORLD),
                scene.commandCount(EchoRenderLayer.ENTITY),
                scene.commandCount(EchoRenderLayer.PARTICLE),
                scene.commandCount(EchoRenderLayer.UI),
                scene.commandCount(EchoRenderLayer.DIAGNOSTIC),
                stats.clearCommands(),
                stats.tileCommands(),
                stats.spriteCommands(),
                stats.particleCommands(),
                stats.uiCommands(),
                stats.debugCommands(),
                passArray(stats.passes()),
                stats.passes().equals(List.of(
                        EchoSoftwareRenderPass.CLEAR,
                        EchoSoftwareRenderPass.TILES,
                        EchoSoftwareRenderPass.SPRITES,
                        EchoSoftwareRenderPass.UI,
                        EchoSoftwareRenderPass.LIGHTING,
                        EchoSoftwareRenderPass.PARTICLES,
                        EchoSoftwareRenderPass.DEBUG_OVERLAY
                ))
        ));

        write(root.resolve("render-debug-world.json"), """
                {
                  "schema": "echo.standalone.render_debug_world.v2",
                  "status": "PASS",
                  "worldTileCount": %d,
                  "blockedTileRepresented": %s,
                  "worldMaterials": %s,
                  "tileCommands": %s,
                  "blockedTilePixelDiffersFromBackground": %s
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.WORLD),
                scene.commands().stream().anyMatch(command -> command.material().equals("world:blocked")),
                stringArray(scene.commandsForLayer(EchoRenderLayer.WORLD).stream()
                        .map(EchoRenderCommand::material)
                        .distinct()
                        .sorted()
                        .toList()),
                commandsJson(scene.commandsForLayer(EchoRenderLayer.WORLD)),
                blockedTileSample != backgroundSample
        ));

        write(root.resolve("render-entities.json"), """
                {
                  "schema": "echo.standalone.render_entities.v2",
                  "status": "PASS",
                  "entityCommandCount": %d,
                  "playerRepresented": %s,
                  "hostileRepresented": %s,
                  "entityMaterials": %s,
                  "entityCommands": %s,
                  "playerPixelDiffersFromBackground": %s
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.ENTITY),
                scene.commands().stream().anyMatch(command -> command.material().equals("entity:player")),
                scene.commands().stream().anyMatch(command -> command.material().equals("entity:hostile")),
                stringArray(scene.commandsForLayer(EchoRenderLayer.ENTITY).stream()
                        .map(EchoRenderCommand::material)
                        .distinct()
                        .sorted()
                        .toList()),
                commandsJson(scene.commandsForLayer(EchoRenderLayer.ENTITY)),
                playerSample != backgroundSample
        ));

        write(root.resolve("render-particles.json"), """
                {
                  "schema": "echo.standalone.render_particles.v2",
                  "status": "PASS",
                  "particleCommandCount": %d,
                  "ashStormParticlesRepresented": %s,
                  "particleMaterials": %s,
                  "particleCommands": %s,
                  "statsParticleCommands": %d
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.PARTICLE),
                scene.commands().stream().anyMatch(command -> command.type() == EchoRenderCommandType.PARTICLE
                        && command.material().equals("particle:ash:ashfall:ash_storm")),
                stringArray(scene.commandsForLayer(EchoRenderLayer.PARTICLE).stream()
                        .map(EchoRenderCommand::material)
                        .distinct()
                        .sorted()
                        .toList()),
                commandsJson(scene.commandsForLayer(EchoRenderLayer.PARTICLE)),
                stats.particleCommands()
        ));

        write(root.resolve("render-ui-bridge.json"), """
                {
                  "schema": "echo.standalone.render_ui_bridge.v2",
                  "status": "PASS",
                  "uiCommandCount": %d,
                  "uiStatsCommands": %d,
                  "hudSurfaceRepresented": %s,
                  "missionTextRepresented": %s,
                  "uiPixelDiffersFromBackground": %s,
                  "uiCommands": %s
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.UI),
                stats.uiCommands(),
                scene.commands().stream().anyMatch(command -> command.label().equals("Ashfall HUD")),
                scene.commands().stream().anyMatch(command -> command.label().contains("Mission: Secure The Crash Site")),
                uiSample != backgroundSample,
                commandsJson(scene.commandsForLayer(EchoRenderLayer.UI))
        ));

        write(root.resolve("render-diagnostics.json"), """
                {
                  "schema": "echo.standalone.render_diagnostics.v2",
                  "status": "PASS",
                  "diagnosticCommandCount": %d,
                  "frameDiagnosticCount": %d,
                  "frameDiagnostics": %s,
                  "litPixels": %d,
                  "litFullFramebuffer": %s,
                  "nonBackgroundPixels": %d,
                  "checksum": "%s",
                  "checksumNonZero": %s,
                  "diagnosticOverlayRepresented": %s
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.DIAGNOSTIC),
                frame.diagnostics().size(),
                diagnosticsJson(frame.diagnostics()),
                stats.litPixels(),
                stats.litPixels() == framebuffer.width() * framebuffer.height(),
                stats.nonBackgroundPixels(),
                Long.toUnsignedString(stats.checksum()),
                stats.checksum() != 0L,
                scene.commands().stream().anyMatch(command -> command.label().equals("mission=ACTIVE"))
        ));

        write(root.resolve("runtime-renderer-upgrade.json"), """
                {
                  "schema": "echo.standalone.runtime_renderer_upgrade.v2",
                  "status": "PASS",
                  "phase": "15.5",
                  "summary": "Default renderer path upgraded from command recording to a headless-safe software backend that produces deterministic ARGB framebuffer output while preserving the recording backend contract.",
                  "defaultBackendId": "%s",
                  "softwareBackendDefault": %s,
                  "recordingBackendPreserved": %s,
                  "recordingBackendId": "%s",
                  "sceneId": "%s",
                  "frameCount": %d,
                  "recordingFrameCount": %d,
                  "commandCount": %d,
                  "submittedCommandCount": %d,
                  "framebufferWidth": %d,
                  "framebufferHeight": %d,
                  "nonBackgroundPixels": %d,
                  "checksum": "%s",
                  "headlessSafe": %s
                }
                """.formatted(
                escape(render.backend().backendId()),
                render.backend() instanceof EchoSoftwareRenderBackend,
                "echo:recording_renderer".equals(recording.backend().backendId())
                        && recording.backend().frames().size() == 1,
                escape(recording.backend().backendId()),
                escape(scene.sceneId()),
                render.backend().frames().size(),
                recording.backend().frames().size(),
                scene.commands().size(),
                frame.submittedCommandCount(),
                framebuffer.width(),
                framebuffer.height(),
                stats.nonBackgroundPixels(),
                Long.toUnsignedString(stats.checksum()),
                window.mode() == EchoRenderWindowMode.HEADLESS
        ));

        write(root.resolve("renderer-upgrade-backend.json"), """
                {
                  "schema": "echo.standalone.renderer_upgrade_backend.v2",
                  "status": "PASS",
                  "backendId": "%s",
                  "backendClass": "%s",
                  "implementsBackendContract": true,
                  "softwareBackend": %s,
                  "recordingBackendId": "%s",
                  "recordingBackendAvailable": %s,
                  "logicalWindowOpen": %s,
                  "headlessMode": %s,
                  "framesRetained": %d,
                  "framebufferRetained": %s,
                  "displayFree": true,
                  "gpuContextFree": true,
                  "nativeWindowFree": true,
                  "jfxFree": true
                }
                """.formatted(
                escape(render.backend().backendId()),
                escape(render.backend().getClass().getName()),
                render.backend() instanceof EchoSoftwareRenderBackend,
                escape(recording.backend().backendId()),
                "echo:recording_renderer".equals(recording.backend().backendId()),
                window.open(),
                window.mode() == EchoRenderWindowMode.HEADLESS,
                render.backend().frames().size(),
                ((EchoSoftwareRenderBackend) render.backend()).lastFramebuffer().isPresent()
        ));

        write(root.resolve("renderer-upgrade-framebuffer.json"), """
                {
                  "schema": "echo.standalone.renderer_upgrade_framebuffer.v2",
                  "status": "PASS",
                  "width": %d,
                  "height": %d,
                  "pixelCount": %d,
                  "argbLength": %d,
                  "nonBackgroundPixels": %d,
                  "checksum": "%s",
                  "checksumNonZero": %s,
                  "playerSampleDiffersFromBackground": %s,
                  "blockedTileSampleDiffersFromBackground": %s,
                  "uiSampleDiffersFromBackground": %s
                }
                """.formatted(
                framebuffer.width(),
                framebuffer.height(),
                framebuffer.width() * framebuffer.height(),
                framebuffer.argb().length,
                stats.nonBackgroundPixels(),
                Long.toUnsignedString(stats.checksum()),
                stats.checksum() != 0L,
                playerSample != backgroundSample,
                blockedTileSample != backgroundSample,
                uiSample != backgroundSample
        ));

        write(root.resolve("renderer-upgrade-pipeline.json"), """
                {
                  "schema": "echo.standalone.renderer_upgrade_pipeline.v2",
                  "status": "PASS",
                  "passes": %s,
                  "passOrderMatches": %s,
                  "clearCommands": %d,
                  "tileCommands": %d,
                  "spriteCommands": %d,
                  "uiCommands": %d,
                  "lightingPassPresent": %s,
                  "particleCommands": %d,
                  "debugCommands": %d,
                  "sceneCommandCount": %d,
                  "submittedCommandCount": %d
                }
                """.formatted(
                passArray(stats.passes()),
                stats.passes().equals(List.of(
                        EchoSoftwareRenderPass.CLEAR,
                        EchoSoftwareRenderPass.TILES,
                        EchoSoftwareRenderPass.SPRITES,
                        EchoSoftwareRenderPass.UI,
                        EchoSoftwareRenderPass.LIGHTING,
                        EchoSoftwareRenderPass.PARTICLES,
                        EchoSoftwareRenderPass.DEBUG_OVERLAY
                )),
                stats.clearCommands(),
                stats.tileCommands(),
                stats.spriteCommands(),
                stats.uiCommands(),
                stats.passes().contains(EchoSoftwareRenderPass.LIGHTING),
                stats.particleCommands(),
                stats.debugCommands(),
                scene.commands().size(),
                frame.submittedCommandCount()
        ));

        write(root.resolve("renderer-upgrade-lighting.json"), """
                {
                  "schema": "echo.standalone.renderer_upgrade_lighting.v2",
                  "status": "PASS",
                  "lightingPassPresent": %s,
                  "litPixels": %d,
                  "expectedPixels": %d,
                  "litFullFramebuffer": %s,
                  "nonBackgroundPixels": %d,
                  "checksum": "%s",
                  "checksumNonZero": %s
                }
                """.formatted(
                stats.passes().contains(EchoSoftwareRenderPass.LIGHTING),
                stats.litPixels(),
                framebuffer.width() * framebuffer.height(),
                stats.litPixels() == framebuffer.width() * framebuffer.height(),
                stats.nonBackgroundPixels(),
                Long.toUnsignedString(stats.checksum()),
                stats.checksum() != 0L
        ));

        write(root.resolve("renderer-upgrade-debug-overlays.json"), """
                {
                  "schema": "echo.standalone.renderer_upgrade_debug_overlays.v2",
                  "status": "PASS",
                  "diagnosticCommandCount": %d,
                  "debugCommands": %d,
                  "debugOverlayPassPresent": %s,
                  "missionOverlayRepresented": %s,
                  "frameDiagnosticCount": %d,
                  "frameDiagnostics": %s,
                  "containsPixelDiagnostic": %s,
                  "containsPassDiagnostic": %s
                }
                """.formatted(
                scene.commandCount(EchoRenderLayer.DIAGNOSTIC),
                stats.debugCommands(),
                stats.passes().contains(EchoSoftwareRenderPass.DEBUG_OVERLAY),
                scene.commands().stream().anyMatch(command -> command.label().equals("mission=ACTIVE")),
                frame.diagnostics().size(),
                diagnosticsJson(frame.diagnostics()),
                frame.diagnostics().stream().anyMatch(diagnostic -> diagnostic.message().contains("software pixels=")),
                frame.diagnostics().stream().anyMatch(diagnostic -> diagnostic.message().equals("software passes=7"))
        ));
    }

    private static String cameraJson(EchoRenderCamera camera) {
        return """
                {
                    "cameraId": "%s",
                    "x": %.2f,
                    "y": %.2f,
                    "z": %.2f,
                    "zoom": %.2f,
                    "pitchDegrees": %.2f
                  }""".formatted(
                escape(camera.cameraId()),
                camera.x(),
                camera.y(),
                camera.z(),
                camera.zoom(),
                camera.pitchDegrees()
        );
    }

    private static String commandsJson(List<EchoRenderCommand> commands) {
        return commands.stream()
                .map(command -> """
                        {
                            "commandId": "%s",
                            "layer": "%s",
                            "type": "%s",
                            "material": "%s",
                            "label": "%s",
                            "x": %.2f,
                            "y": %.2f,
                            "z": %.2f,
                            "width": %.2f,
                            "height": %.2f
                          }""".formatted(
                        escape(command.commandId()),
                        command.layer().name(),
                        command.type().name(),
                        escape(command.material()),
                        escape(command.label()),
                        command.x(),
                        command.y(),
                        command.z(),
                        command.width(),
                        command.height()
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String diagnosticsJson(List<EchoRenderDiagnostic> diagnostics) {
        return diagnostics.stream()
                .map(diagnostic -> """
                        {
                            "severity": "%s",
                            "message": "%s"
                          }""".formatted(
                        diagnostic.severity().name(),
                        escape(diagnostic.message())
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String passArray(List<EchoSoftwareRenderPass> passes) {
        return passes.stream()
                .map(EchoSoftwareRenderPass::name)
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String stringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
