package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public final class EchoRenderRuntime {
    public EchoRenderRuntimeResult createDebugRenderer(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoUiRuntimeResult ui,
            EchoRenderWindowSettings settings
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(gameplay, "gameplay");
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(settings, "settings");

        return createRenderer(
                services,
                world,
                entities,
                gameplay,
                ui,
                settings,
                new EchoSoftwareRenderBackend()
        );
    }

    public EchoRenderRuntimeResult createRecordingDebugRenderer(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoUiRuntimeResult ui,
            EchoRenderWindowSettings settings
    ) {
        return createRenderer(
                services,
                world,
                entities,
                gameplay,
                ui,
                settings,
                new EchoRecordingRenderBackend()
        );
    }

    public EchoRenderRuntimeResult createRenderer(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoUiRuntimeResult ui,
            EchoRenderWindowSettings settings,
            EchoRenderBackend backend
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(gameplay, "gameplay");
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(backend, "backend");

        EchoRenderWindowState window = backend.openWindow(settings);
        EchoRenderUiBridge uiBridge = new EchoRenderUiBridge();
        EchoRenderSceneBuilder sceneBuilder = new EchoRenderSceneBuilder(uiBridge);
        EchoRenderScene scene = sceneBuilder.buildDebugScene(world, entities, gameplay, ui.frame());
        EchoRenderFrame frame = backend.render(scene);
        EchoRenderRuntimeResult result = new EchoRenderRuntimeResult(
                backend,
                window,
                uiBridge,
                sceneBuilder,
                scene,
                frame
        );
        services.register(EchoRenderRuntimeResult.class, result);
        services.register(EchoRenderBackend.class, backend);
        services.register(EchoRenderWindowState.class, window);
        services.register(EchoRenderUiBridge.class, uiBridge);
        services.register(EchoRenderSceneBuilder.class, sceneBuilder);
        services.register(EchoRenderScene.class, scene);
        services.register(EchoRenderFrame.class, frame);
        return result;
    }
}
