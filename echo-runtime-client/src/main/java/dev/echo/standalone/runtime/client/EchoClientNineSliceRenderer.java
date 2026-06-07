package dev.echo.standalone.runtime.client;

final class EchoClientNineSliceRenderer {
    void panel(
            EchoClientHud2D hud2d,
            float x,
            float y,
            float w,
            float h,
            float r,
            float g,
            float b,
            float a,
            boolean selected
    ) {
        if (selected) {
            hud2d.rect(x - 5, y - 5, w + 10, h + 10, 0.18f, 0.82f, 0.72f, 0.78f);
        }
        hud2d.rect(x, y, w, h, r, g, b, a);
        hud2d.rect(x, y, w, 2, 0.45f, 0.70f, 0.67f, a);
        hud2d.rect(x, y + h - 2, w, 2, 0.10f, 0.32f, 0.34f, a);
        hud2d.rect(x, y, 2, h, 0.18f, 0.42f, 0.42f, a);
        hud2d.rect(x + w - 2, y, 2, h, 0.18f, 0.42f, 0.42f, a);
    }
}
