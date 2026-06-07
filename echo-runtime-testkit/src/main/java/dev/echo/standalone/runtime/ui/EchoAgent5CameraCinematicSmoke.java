package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5CameraCinematicSmoke {
    private EchoAgent5CameraCinematicSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> frame = EchoAgent5UiActionRouter.routeCameraCinematicFrame(
                Map.of("cinematicFrame", 0),
                source
        );
        Map<String, Object> surface = EchoAgent5UiModuleSurfaceRenderers.renderHud(frame, source);
        Map<String, Object> host = EchoAgent5UiScreenHostModel.render("HUD", frame, "ashfall", 12, 3, 2, 1, source);
        String cue = String.valueOf(source.cinematicValues().get("cue"));
        boolean passed = Boolean.TRUE.equals(frame.get("handled"))
                && "over_shoulder".equals(frame.get("cameraMode"))
                && Integer.valueOf(72).equals(frame.get("cameraFov"))
                && cue.equals(frame.get("cinematicCue"))
                && Integer.valueOf(1).equals(frame.get("cinematicFrame"))
                && Boolean.TRUE.equals(frame.get("cinematicLetterbox"))
                && ("camera_cinematic:frame:" + cue).equals(frame.get("effect"))
                && linesContain(surface, "Camera over_shoulder frame 1 cue " + cue)
                && linesContain(surface, "Letterbox: active")
                && linesContain(host, "Camera over_shoulder frame 1 cue " + cue);
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("cameraCinematicSmokeClass", EchoAgent5CameraCinematicSmoke.class.getSimpleName());
        smoke.put("cameraMode", frame.get("cameraMode"));
        smoke.put("cameraFov", frame.get("cameraFov"));
        smoke.put("cameraTarget", frame.get("cameraTarget"));
        smoke.put("cinematicCue", frame.get("cinematicCue"));
        smoke.put("cinematicFrame", frame.get("cinematicFrame"));
        smoke.put("cinematicLetterbox", frame.get("cinematicLetterbox"));
        smoke.put("cinematicSubtitle", frame.get("cinematicSubtitle"));
        smoke.put("cinematicOutput", frame.get("cinematicOutput"));
        smoke.put("effect", frame.get("effect"));
        smoke.put("surfaceLines", surface.get("lines"));
        smoke.put("hostSurfaceLines", host.get("surfaceLines"));
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    private static boolean linesContain(Map<String, Object> model, String token) {
        Object value = model.get("lines");
        if (value == null) {
            value = model.get("surfaceLines");
        }
        if (value instanceof Iterable<?> lines) {
            for (Object line : lines) {
                if (String.valueOf(line).contains(token)) {
                    return true;
                }
            }
        }
        return false;
    }
}
