package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;

import java.util.List;
import java.util.Objects;

public record EchoVoxelHudOverlay(
        EchoVoxelPlayerHotbar hotbar,
        EchoAshfallLiveMissionState mission,
        String adapterSummary,
        String presenterSummary,
        String registrySummary,
        String targetLabel,
        String actionLabel,
        String playerPositionLabel,
        String playerModeLabel,
        String moduleCoverageLabel,
        String renderLabel,
        String rendererLabel,
        boolean targetAvailable,
        boolean playerGrounded,
        boolean rendererReady,
        int loadedChunkCount,
        boolean shellVisible,
        boolean inventoryVisible,
        boolean terminalVisible,
        boolean missionLogVisible,
        String shellTitle,
        List<String> shellLines
) {
    public EchoVoxelHudOverlay {
        Objects.requireNonNull(hotbar, "hotbar");
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(shellLines, "shellLines");
        adapterSummary = textOr(adapterSummary, "adapter pending");
        presenterSummary = textOr(presenterSummary, "presenter pending");
        registrySummary = textOr(registrySummary, "registry pending");
        targetLabel = textOr(targetLabel, "none");
        actionLabel = textOr(actionLabel, "ready");
        playerPositionLabel = textOr(playerPositionLabel, "spawn");
        playerModeLabel = textOr(playerModeLabel, "grounded");
        moduleCoverageLabel = textOr(moduleCoverageLabel, "modules pending");
        renderLabel = textOr(renderLabel, "render pending");
        rendererLabel = textOr(rendererLabel, "renderer pending");
        if (loadedChunkCount < 0) {
            throw new IllegalArgumentException("loadedChunkCount must not be negative");
        }
        shellTitle = textOr(shellTitle, "ECHO Ashfall");
        shellLines = List.copyOf(shellLines);
    }

    public String adapterLine() {
        return adapterSummary + " / " + presenterSummary + " / " + registrySummary;
    }

    private static String textOr(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
