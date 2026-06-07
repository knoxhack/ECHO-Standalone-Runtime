package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneLiveGraphicsResult(
        String auditId,
        String adapterRenderTarget,
        String openGlRuntime,
        String livePresenterId,
        boolean adapterCoreTargetsOpenGl,
        boolean nativeOpenGlContextVerified,
        boolean gameWindowOpenGlPresenterVerified,
        boolean persistentGameWindowPresenter,
        boolean voxelMeshUploadVerified,
        boolean voxelFramebufferUploadVerified,
        long voxelFramebufferUploadBytes,
        boolean softwareVoxelPresenterActive,
        List<String> blockers
) {
    public EchoStandaloneLiveGraphicsResult {
        auditId = EchoAppText.requireText(auditId, "auditId");
        adapterRenderTarget = EchoAppText.requireText(adapterRenderTarget, "adapterRenderTarget");
        openGlRuntime = openGlRuntime == null || openGlRuntime.isBlank()
                ? "opengl-runtime"
                : openGlRuntime.trim();
        livePresenterId = EchoAppText.requireText(livePresenterId, "livePresenterId");
        if (voxelFramebufferUploadBytes < 0L) {
            throw new IllegalArgumentException("voxelFramebufferUploadBytes must not be negative");
        }
        Objects.requireNonNull(blockers, "blockers");
        blockers = List.copyOf(blockers);
    }

    public boolean clientReady() {
        return adapterCoreTargetsOpenGl
                && nativeOpenGlContextVerified
                && persistentGameWindowPresenter
                && voxelMeshUploadVerified
                && voxelFramebufferUploadVerified
                && blockers.isEmpty();
    }

    public boolean visibleSoftwarePresenterReady() {
        return softwareVoxelPresenterActive
                && livePresenterId.equals(EchoStandaloneLiveGraphicsAudit.SOFTWARE_VOXEL_PRESENTER_ID);
    }

    public String hudSummary() {
        if (persistentGameWindowPresenter) {
            if (livePresenterId.equals(EchoStandaloneLiveGraphicsAudit.OPENGL_CLIENT_PRESENTER_ID)) {
                return "opengl client presenter";
            }
            if (softwareVoxelPresenterActive) {
                return "software presenter / opengl target";
            }
            return "opengl game presenter";
        }
        if (nativeOpenGlContextVerified) {
            return "opengl target ready / presenter pending";
        }
        return livePresenterId;
    }

    public String blockerSummary() {
        return blockers.isEmpty() ? "none" : String.join("; ", blockers);
    }
}
