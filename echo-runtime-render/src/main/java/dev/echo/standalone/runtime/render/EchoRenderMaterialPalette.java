package dev.echo.standalone.runtime.render;

final class EchoRenderMaterialPalette {
    private EchoRenderMaterialPalette() {
    }

    static int colorFor(EchoRenderCommand command) {
        String material = command.material();
        if (material.startsWith("color:#")) {
            return parseHex(material.substring("color:#".length()));
        }
        if (material.equals("world:blocked")) {
            return argb(255, 71, 43, 39);
        }
        if (material.startsWith("terrain:") && material.endsWith(":hazard")) {
            return argb(255, 132, 93, 45);
        }
        if (material.equals("terrain:crash_debris")) {
            return argb(255, 92, 101, 96);
        }
        if (material.equals("terrain:ash_dune")) {
            return argb(255, 116, 118, 105);
        }
        if (material.startsWith("terrain:")) {
            return argb(255, 76, 89, 82);
        }
        if (material.equals("entity:player")) {
            return argb(255, 67, 179, 214);
        }
        if (material.equals("entity:hostile")) {
            return argb(255, 196, 72, 67);
        }
        if (material.startsWith("entity:")) {
            return argb(255, 182, 189, 154);
        }
        if (material.startsWith("particle:ash:")) {
            return argb(180, 188, 178, 154);
        }
        if (material.startsWith("ui-theme:")) {
            return argb(232, 18, 30, 42);
        }
        if (material.equals("ui-text:headline")) {
            return argb(255, 232, 224, 184);
        }
        if (material.equals("ui-text:body")) {
            return argb(255, 181, 200, 192);
        }
        if (material.startsWith("diagnostic:")) {
            return argb(255, 245, 196, 84);
        }
        return argb(255, 218, 214, 185);
    }

    static int argb(int alpha, int red, int green, int blue) {
        return (clamp(alpha) << 24) | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
    }

    private static int parseHex(String hex) {
        if (hex.length() != 6) {
            return argb(255, 6, 16, 20);
        }
        int red = Integer.parseInt(hex.substring(0, 2), 16);
        int green = Integer.parseInt(hex.substring(2, 4), 16);
        int blue = Integer.parseInt(hex.substring(4, 6), 16);
        return argb(255, red, green, blue);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
