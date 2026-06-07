package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.List;

record EchoClientSaveSlotSummary(
        String slotId,
        String displayName,
        String packId,
        String updatedAt,
        boolean loadableInMemory,
        boolean recoveryRequired,
        String detail,
        String thumbnailPath,
        String thumbnailResolvedPath,
        String thumbnailSource,
        boolean thumbnailCaptured,
        int thumbnailWidth,
        int thumbnailHeight,
        int thumbnailSkyArgb,
        int thumbnailTerrainArgb,
        int thumbnailAccentArgb,
        int thumbnailShadowArgb
) {
    EchoClientSaveSlotSummary(
            String slotId,
            String displayName,
            String packId,
            String updatedAt,
            boolean loadableInMemory,
            String detail
    ) {
        this(
                slotId,
                displayName,
                packId,
                updatedAt,
                loadableInMemory,
                false,
                detail,
                "",
                "",
                "deterministic",
                false,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    EchoClientSaveSlotSummary {
        slotId = blankTo(slotId, "unknown-slot");
        displayName = blankTo(displayName, slotId);
        packId = blankTo(packId, "unknown-pack");
        updatedAt = blankTo(updatedAt, "unknown time");
        detail = blankTo(detail, "Save manifest discovered");
        thumbnailPath = thumbnailPath == null ? "" : thumbnailPath.trim();
        thumbnailResolvedPath = thumbnailResolvedPath == null ? "" : thumbnailResolvedPath.trim();
        thumbnailSource = blankTo(thumbnailSource, "deterministic");
        thumbnailCaptured = thumbnailCaptured && !thumbnailPath.isBlank() && !thumbnailResolvedPath.isBlank();
        thumbnailWidth = Math.max(0, thumbnailWidth);
        thumbnailHeight = Math.max(0, thumbnailHeight);
        thumbnailSkyArgb = opaque(thumbnailSkyArgb);
        thumbnailTerrainArgb = opaque(thumbnailTerrainArgb);
        thumbnailAccentArgb = opaque(thumbnailAccentArgb);
        thumbnailShadowArgb = opaque(thumbnailShadowArgb);
    }

    String menuLabel() {
        String suffix = recoveryRequired ? "Recovery required" : loadableInMemory ? "Ready" : "Disk restore pending";
        return displayName + " - " + suffix;
    }

    List<String> reviewLines() {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Slot " + slotId);
        lines.add("Pack " + packId);
        lines.add("Updated " + updatedAt);
        for (String part : detail.split("\\|")) {
            String normalized = part.trim();
            if (!normalized.isBlank()
                    && !normalized.startsWith("Pack ")
                    && !normalized.startsWith("updated ")) {
                lines.add(normalized);
            }
        }
        return List.copyOf(lines);
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static int opaque(int argb) {
        return argb == 0 ? 0 : 0xFF000000 | (argb & 0x00FFFFFF);
    }
}
