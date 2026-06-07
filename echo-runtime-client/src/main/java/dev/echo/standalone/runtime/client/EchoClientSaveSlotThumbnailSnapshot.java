package dev.echo.standalone.runtime.client;

record EchoClientSaveSlotThumbnailSnapshot(
        boolean visible,
        String slotId,
        String displayName,
        String packId,
        String statusLabel,
        String source,
        String relativePath,
        String resolvedPath,
        int width,
        int height,
        boolean captured,
        int skyArgb,
        int terrainArgb,
        int accentArgb,
        int shadowArgb
) {
    static final EchoClientSaveSlotThumbnailSnapshot EMPTY = new EchoClientSaveSlotThumbnailSnapshot(
            false,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            0,
            0,
            false,
            0xFF0A1214,
            0xFF17211C,
            0xFF2AD8BC,
            0xFF050809
    );

    EchoClientSaveSlotThumbnailSnapshot {
        slotId = clean(slotId);
        displayName = clean(displayName);
        packId = clean(packId);
        statusLabel = clean(statusLabel);
        source = clean(source);
        relativePath = clean(relativePath);
        resolvedPath = clean(resolvedPath);
        width = Math.max(0, width);
        height = Math.max(0, height);
        captured = captured && !relativePath.isBlank() && !resolvedPath.isBlank() && width > 0 && height > 0;
        skyArgb = opaque(skyArgb);
        terrainArgb = opaque(terrainArgb);
        accentArgb = opaque(accentArgb);
        shadowArgb = opaque(shadowArgb);
        visible = visible && !slotId.isBlank();
    }

    static EchoClientSaveSlotThumbnailSnapshot from(EchoClientSaveSlotSummary slot) {
        if (slot == null) {
            return EMPTY;
        }
        String seedText = slot.slotId() + "|" + slot.displayName() + "|" + slot.packId();
        int seed = seedText.hashCode();
        int sky = color(seed, 36, 54, 64, 36);
        int terrain = color(seed >>> 4, 54, 46, 34, 46);
        int accent = slot.loadableInMemory()
                ? color(seed >>> 9, 58, 176, 148, 42)
                : color(seed >>> 9, 156, 96, 48, 40);
        int shadow = color(seed >>> 14, 16, 22, 18, 16);
        boolean captured = slot.thumbnailCaptured()
                && !slot.thumbnailResolvedPath().isBlank()
                && slot.thumbnailWidth() > 0
                && slot.thumbnailHeight() > 0;
        return new EchoClientSaveSlotThumbnailSnapshot(
                true,
                slot.slotId(),
                slot.displayName(),
                slot.packId(),
                slot.loadableInMemory() ? "READY" : "DISK",
                captured ? slot.thumbnailSource() : "deterministic",
                captured ? slot.thumbnailPath() : "",
                captured ? slot.thumbnailResolvedPath() : "",
                captured ? slot.thumbnailWidth() : 0,
                captured ? slot.thumbnailHeight() : 0,
                captured,
                captured && slot.thumbnailSkyArgb() != 0 ? slot.thumbnailSkyArgb() : sky,
                captured && slot.thumbnailTerrainArgb() != 0 ? slot.thumbnailTerrainArgb() : terrain,
                captured && slot.thumbnailAccentArgb() != 0 ? slot.thumbnailAccentArgb() : accent,
                captured && slot.thumbnailShadowArgb() != 0 ? slot.thumbnailShadowArgb() : shadow
        );
    }

    private static int color(int seed, int baseR, int baseG, int baseB, int spread) {
        int r = clampColor(baseR + Math.floorMod(seed, spread));
        int g = clampColor(baseG + Math.floorMod(seed >>> 7, spread));
        int b = clampColor(baseB + Math.floorMod(seed >>> 15, spread));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static int opaque(int argb) {
        return 0xFF000000 | (argb & 0x00FFFFFF);
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
