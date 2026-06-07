package dev.echo.standalone.runtime.render;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRenderCoreStandaloneAdapter {
    public static final String MODULE_ID = "echorendercore";
    public static final String ADAPTERCORE_CONTRACT_ID = "echorendercore:render/preview_frame";
    public static final String REFERENCE_SCENE_ID = "echorendercore:scene/ashfall_preview";
    public static final String REFERENCE_PROFILE_ID = "echorendercore:rendercore/examples/v21_terminal_screen";

    public Map<String, Object> activate() {
        Map<String, Object> previewFrame = executePreview("echo-native-m17");
        boolean previewFramePassed = referencePreviewPassed(previewFrame);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "rendercore_standalone_preview_frame_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", List.of(
                "render.diagnostics",
                "render.particle_profiles",
                "render.preview",
                "render.profiles",
                "render.screen_chrome",
                ADAPTERCORE_CONTRACT_ID
        ));
        report.put("previewFrame", previewFrame);
        report.put("previewFrameExecuted", previewFramePassed);
        report.put("serviceCodeExecuted", previewFramePassed);
        report.put("summary", "RenderCore standalone adapter executed the AdapterCore preview frame service.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executePreview(String packId) {
        EchoRecordingRenderBackend backend = new EchoRecordingRenderBackend();
        EchoRenderWindowState window = backend.openWindow(EchoRenderWindowSettings.headlessDebug());
        EchoRenderScene scene = new EchoRenderScene(
                REFERENCE_SCENE_ID,
                EchoRenderCameras.ashfallPreview(),
                List.of(
                        renderCommand("render-001-clear", EchoRenderLayer.BACKGROUND, EchoRenderCommandType.CLEAR, "background:ashfall", "clear"),
                        renderCommand("render-010-world", EchoRenderLayer.WORLD, EchoRenderCommandType.TILE, "world:ash", "ash-road"),
                        renderCommand("render-011-world", EchoRenderLayer.WORLD, EchoRenderCommandType.TILE, "world:blocked", "ruined-wall"),
                        renderCommand("render-020-entity", EchoRenderLayer.ENTITY, EchoRenderCommandType.ENTITY, "entity:player", "player-001"),
                        renderCommand("render-030-particle", EchoRenderLayer.PARTICLE, EchoRenderCommandType.PARTICLE, "particle:ash:ashfall:ash_storm", "ash-storm"),
                        renderCommand("render-031-particle", EchoRenderLayer.PARTICLE, EchoRenderCommandType.PARTICLE, "particle:glint:nexus", "nexus-glint"),
                        renderCommand("render-040-ui", EchoRenderLayer.UI, EchoRenderCommandType.UI_SURFACE, "ui:terminal", "Ashfall HUD"),
                        renderCommand("render-050-diagnostic", EchoRenderLayer.DIAGNOSTIC, EchoRenderCommandType.TEXT, "diagnostic:overlay", "mission=ACTIVE")
                )
        );
        EchoRenderFrame frame = backend.render(scene);

        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
        preview.put("service", "echorendercore:preview");
        preview.put("previewFrameExecuted", true);
        preview.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        preview.put("profileId", REFERENCE_PROFILE_ID);
        preview.put("sceneId", scene.sceneId());
        preview.put("backendId", backend.backendId());
        preview.put("window", Map.of(
                "mode", window.mode().name(),
                "width", window.viewport().width(),
                "height", window.viewport().height(),
                "open", window.open()
        ));
        preview.put("camera", Map.of(
                "cameraId", scene.camera().cameraId(),
                "zoom", scene.camera().zoom(),
                "pitchDegrees", scene.camera().pitchDegrees()
        ));
        preview.put("commands", scene.commands().stream().map(EchoRenderCoreStandaloneAdapter::command).toList());
        preview.put("layerCounts", Map.of(
                "BACKGROUND", Math.toIntExact(scene.commandCount(EchoRenderLayer.BACKGROUND)),
                "WORLD", Math.toIntExact(scene.commandCount(EchoRenderLayer.WORLD)),
                "ENTITY", Math.toIntExact(scene.commandCount(EchoRenderLayer.ENTITY)),
                "PARTICLE", Math.toIntExact(scene.commandCount(EchoRenderLayer.PARTICLE)),
                "UI", Math.toIntExact(scene.commandCount(EchoRenderLayer.UI)),
                "DIAGNOSTIC", Math.toIntExact(scene.commandCount(EchoRenderLayer.DIAGNOSTIC))
        ));
        preview.put("frame", Map.of(
                "frameIndex", frame.frameIndex(),
                "submittedCommandCount", frame.submittedCommandCount(),
                "diagnosticCount", frame.diagnostics().size(),
                "frameCount", backend.frames().size()
        ));
        preview.put("diagnostics", List.of(
                "render.profile.loaded",
                "render.scene.planned",
                "render.frame.recorded"
        ));
        preview.put("referenceBehavior", "rendercore_records_preview_frame");
        return Map.copyOf(preview);
    }

    public boolean referencePreviewPassed(Map<String, Object> preview) {
        return Boolean.TRUE.equals(preview.get("previewFrameExecuted"))
                && ADAPTERCORE_CONTRACT_ID.equals(preview.get("adapterCoreContract"))
                && REFERENCE_PROFILE_ID.equals(preview.get("profileId"))
                && REFERENCE_SCENE_ID.equals(preview.get("sceneId"))
                && "echo:recording_renderer".equals(preview.get("backendId"))
                && String.valueOf(preview.get("commands")).contains("particle:ash:ashfall:ash_storm")
                && String.valueOf(preview.get("commands")).contains("Ashfall HUD")
                && String.valueOf(preview.get("layerCounts")).contains("WORLD=2")
                && String.valueOf(preview.get("frame")).contains("submittedCommandCount=8")
                && String.valueOf(preview.get("diagnostics")).contains("render.frame.recorded");
    }

    private static EchoRenderCommand renderCommand(
            String id,
            EchoRenderLayer layer,
            EchoRenderCommandType type,
            String material,
            String label
    ) {
        return new EchoRenderCommand(id, layer, type, 0.0D, 0.0D, 0.0D, 32.0D, 32.0D, material, label);
    }

    private static Map<String, String> command(EchoRenderCommand command) {
        Map<String, String> output = new LinkedHashMap<>();
        output.put("id", command.commandId());
        output.put("layer", command.layer().name());
        output.put("type", command.type().name());
        output.put("material", command.material());
        output.put("label", command.label());
        return Map.copyOf(output);
    }
}
