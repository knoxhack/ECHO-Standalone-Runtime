package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.List;

record EchoClientSubtitleOverlayPlan(
        int x,
        int y,
        int width,
        int height,
        List<EchoClientSubtitleLine> lines
) {
    static final int MAX_LINES = 4;

    EchoClientSubtitleOverlayPlan {
        lines = lines == null ? List.of() : List.copyOf(lines);
        width = Math.max(0, width);
        height = Math.max(0, height);
    }

    boolean visible() {
        return !lines.isEmpty() && width > 0 && height > 0;
    }

    static EchoClientSubtitleOverlayPlan from(
            int screenWidth,
            int screenHeight,
            List<EchoClientSubtitleLine> subtitleLines
    ) {
        if (subtitleLines == null || subtitleLines.isEmpty()) {
            return new EchoClientSubtitleOverlayPlan(0, 0, 0, 0, List.of());
        }
        ArrayList<EchoClientSubtitleLine> safeLines = new ArrayList<>(Math.min(MAX_LINES, subtitleLines.size()));
        int maxChars = 0;
        for (EchoClientSubtitleLine line : subtitleLines) {
            if (line == null || line.text().isBlank()) {
                continue;
            }
            safeLines.add(line);
            maxChars = Math.max(maxChars, line.text().length());
            if (safeLines.size() >= MAX_LINES) {
                break;
            }
        }
        if (safeLines.isEmpty()) {
            return new EchoClientSubtitleOverlayPlan(0, 0, 0, 0, List.of());
        }
        int safeWidth = Math.max(1, screenWidth);
        int safeHeight = Math.max(1, screenHeight);
        int panelWidth = Math.min(
                Math.max(112, maxChars * 6 + 24),
                Math.max(80, safeWidth - 24)
        );
        int panelHeight = safeLines.size() * 14 + 10;
        int x = Math.max(12, safeWidth - panelWidth - 12);
        int y = Math.max(12, safeHeight - 170 - panelHeight);
        return new EchoClientSubtitleOverlayPlan(x, y, panelWidth, panelHeight, safeLines);
    }
}
