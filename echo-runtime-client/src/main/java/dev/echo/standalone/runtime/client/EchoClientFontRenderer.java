package dev.echo.standalone.runtime.client;

final class EchoClientFontRenderer {
    void drawCentered(
            EchoClientHud2D hud2d,
            String text,
            float centerX,
            float y,
            float scale,
            float r,
            float g,
            float b,
            float a
    ) {
        if (text == null || text.isBlank()) {
            return;
        }
        float width = estimateWidth(text, scale);
        hud2d.text(text, centerX - width / 2.0f + 1.0f, y + 1.0f, scale, 0.0f, 0.0f, 0.0f, a * 0.55f);
        hud2d.text(text, centerX - width / 2.0f, y, scale, r, g, b, a);
    }

    private static float estimateWidth(String text, float scale) {
        int visible = 0;
        for (int i = 0; i < text.length(); i++) {
            visible += text.charAt(i) == ' ' ? 3 : 6;
        }
        return visible * scale;
    }
}
