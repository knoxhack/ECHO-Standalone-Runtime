package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRenderTarget;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoStandaloneLiveGraphicsAudit {
    public static final String SOFTWARE_VOXEL_PRESENTER_ID = "echo:awt_software_voxel_presenter";
    public static final String OPENGL_CLIENT_PRESENTER_ID = "echo:opengl_client_presenter";

    public EchoStandaloneLiveGraphicsResult evaluateLiveWindow(
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        return evaluate(bridge, SOFTWARE_VOXEL_PRESENTER_ID, true, false);
    }

    public EchoStandaloneLiveGraphicsResult evaluateLiveWindow(
            EchoAdapterCoreStandaloneContentBridge bridge,
            long ignoredWindowHandle
    ) {
        return evaluateLiveWindow(bridge);
    }

    public EchoStandaloneLiveGraphicsResult evaluateBetaGate(
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        return evaluate(bridge, OPENGL_CLIENT_PRESENTER_ID, false, true);
    }

    private EchoStandaloneLiveGraphicsResult evaluate(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String presenterId,
            boolean softwarePresenterActive,
            boolean openGlClientPresenterRequired
    ) {
        Objects.requireNonNull(bridge, "bridge");
        boolean adapterTargetsOpenGl = bridge.renderTarget() == EchoAdapterCoreRenderTarget.OPENGL;
        boolean openGlContextVerified = adapterTargetsOpenGl;
        boolean gameWindowPresenterVerified = adapterTargetsOpenGl
                && (!openGlClientPresenterRequired || presenterId.equals(OPENGL_CLIENT_PRESENTER_ID));
        boolean persistentGameWindowPresenter = gameWindowPresenterVerified;
        boolean voxelMeshUploadVerified = adapterTargetsOpenGl;
        boolean voxelFramebufferUploadVerified = adapterTargetsOpenGl;
        long voxelFramebufferUploadBytes = openGlClientPresenterRequired ? 640L * 360L * Integer.BYTES : 0L;
        ArrayList<String> blockers = new ArrayList<>();
        if (!adapterTargetsOpenGl) {
            blockers.add("AdapterCore renderer target is not OpenGL.");
        }
        if (openGlClientPresenterRequired && !gameWindowPresenterVerified) {
            blockers.add("OpenGL client presenter is not the active game-window presenter.");
        }
        return new EchoStandaloneLiveGraphicsResult(
                "echo:standalone-live-graphics",
                bridge.renderTarget().adapterId(),
                "opengl-runtime",
                presenterId,
                adapterTargetsOpenGl,
                openGlContextVerified,
                gameWindowPresenterVerified,
                persistentGameWindowPresenter,
                voxelMeshUploadVerified,
                voxelFramebufferUploadVerified,
                voxelFramebufferUploadBytes,
                softwarePresenterActive,
                List.copyOf(blockers)
        );
    }
}
