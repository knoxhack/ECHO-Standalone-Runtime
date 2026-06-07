package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.render.EchoRenderCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoRenderCoreParitySmokeHarness {
    private EchoRuntimeEchoRenderCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativePreview = executeNativeReferencePreview("echo-native-m17");
        EchoRenderCoreStandaloneAdapter standaloneAdapter = new EchoRenderCoreStandaloneAdapter();
        Map<String, Object> standalonePreview = standaloneAdapter.executePreview("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferencePreviewPassed(nativePreview), "native RenderCore preview should pass");
        require(standaloneAdapter.referencePreviewPassed(standalonePreview), "standalone RenderCore preview should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("previewFrameExecuted")),
                "standalone activation should execute preview frame");
        require(nativePreview.get("adapterCoreContract").equals(standalonePreview.get("adapterCoreContract")),
                "native and standalone render contracts should match");
        require(nativePreview.get("profileId").equals(standalonePreview.get("profileId")),
                "native and standalone profile ids should match");
        require(nativePreview.get("sceneId").equals(standalonePreview.get("sceneId")),
                "native and standalone scene ids should match");
        require(nativePreview.get("backendId").equals(standalonePreview.get("backendId")),
                "native and standalone backend ids should match");
        require(nativePreview.get("window").equals(standalonePreview.get("window")),
                "native and standalone windows should match");
        require(nativePreview.get("commands").equals(standalonePreview.get("commands")),
                "native and standalone render commands should match");
        require(nativePreview.get("layerCounts").equals(standalonePreview.get("layerCounts")),
                "native and standalone layer counts should match");
        require(nativePreview.get("frame").equals(standalonePreview.get("frame")),
                "native and standalone frame summaries should match");

        System.out.println("echorendercore parity smoke PASS contract="
                + nativePreview.get("adapterCoreContract")
                + " scene="
                + nativePreview.get("sceneId")
                + " commands="
                + ((List<?>) nativePreview.get("commands")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferencePreview(String packId) {
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("adapterCoreContract", EchoRenderCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        preview.put("service", "echorendercore:preview");
        preview.put("previewFrameExecuted", true);
        preview.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        preview.put("profileId", EchoRenderCoreStandaloneAdapter.REFERENCE_PROFILE_ID);
        preview.put("sceneId", EchoRenderCoreStandaloneAdapter.REFERENCE_SCENE_ID);
        preview.put("backendId", "echo:recording_renderer");
        preview.put("window", Map.of(
                "mode", "HEADLESS",
                "width", 1280,
                "height", 720,
                "open", true
        ));
        preview.put("camera", Map.of(
                "cameraId", "ashfall-debug-camera",
                "zoom", 1.0D,
                "pitchDegrees", 55.0D
        ));
        preview.put("commands", List.of(
                command("render-001-clear", "BACKGROUND", "CLEAR", "background:ashfall", "clear"),
                command("render-010-world", "WORLD", "TILE", "world:ash", "ash-road"),
                command("render-011-world", "WORLD", "TILE", "world:blocked", "ruined-wall"),
                command("render-020-entity", "ENTITY", "ENTITY", "entity:player", "player-001"),
                command("render-030-particle", "PARTICLE", "PARTICLE", "particle:ash:ashfall:ash_storm", "ash-storm"),
                command("render-031-particle", "PARTICLE", "PARTICLE", "particle:glint:nexus", "nexus-glint"),
                command("render-040-ui", "UI", "UI_SURFACE", "ui:terminal", "Ashfall HUD"),
                command("render-050-diagnostic", "DIAGNOSTIC", "TEXT", "diagnostic:overlay", "mission=ACTIVE")
        ));
        preview.put("layerCounts", Map.of(
                "BACKGROUND", 1,
                "WORLD", 2,
                "ENTITY", 1,
                "PARTICLE", 2,
                "UI", 1,
                "DIAGNOSTIC", 1
        ));
        preview.put("frame", Map.of(
                "frameIndex", 0L,
                "submittedCommandCount", 8,
                "diagnosticCount", 1,
                "frameCount", 1
        ));
        preview.put("diagnostics", List.of(
                "render.profile.loaded",
                "render.scene.planned",
                "render.frame.recorded"
        ));
        preview.put("referenceBehavior", "rendercore_records_preview_frame");
        return Map.copyOf(preview);
    }

    private static boolean nativeReferencePreviewPassed(Map<String, Object> preview) {
        return Boolean.TRUE.equals(preview.get("previewFrameExecuted"))
                && EchoRenderCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(preview.get("adapterCoreContract"))
                && EchoRenderCoreStandaloneAdapter.REFERENCE_PROFILE_ID.equals(preview.get("profileId"))
                && EchoRenderCoreStandaloneAdapter.REFERENCE_SCENE_ID.equals(preview.get("sceneId"))
                && "echo:recording_renderer".equals(preview.get("backendId"))
                && String.valueOf(preview.get("commands")).contains("particle:ash:ashfall:ash_storm")
                && String.valueOf(preview.get("commands")).contains("Ashfall HUD")
                && String.valueOf(preview.get("layerCounts")).contains("WORLD=2")
                && String.valueOf(preview.get("frame")).contains("submittedCommandCount=8")
                && String.valueOf(preview.get("diagnostics")).contains("render.frame.recorded");
    }

    private static Map<String, String> command(String id, String layer, String type, String material, String label) {
        Map<String, String> command = new LinkedHashMap<>();
        command.put("id", id);
        command.put("layer", layer);
        command.put("type", type);
        command.put("material", material);
        command.put("label", label);
        return Map.copyOf(command);
    }
}
