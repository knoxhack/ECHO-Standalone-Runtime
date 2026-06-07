package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoRenderCommand(
        String commandId,
        EchoRenderLayer layer,
        EchoRenderCommandType type,
        double x,
        double y,
        double z,
        double width,
        double height,
        String material,
        String label
) {
    public EchoRenderCommand {
        commandId = EchoRenderText.requireText(commandId, "commandId");
        Objects.requireNonNull(layer, "layer");
        Objects.requireNonNull(type, "type");
        if (width < 0.0D) {
            throw new IllegalArgumentException("width must not be negative");
        }
        if (height < 0.0D) {
            throw new IllegalArgumentException("height must not be negative");
        }
        material = EchoRenderText.requireText(material, "material");
        label = EchoRenderText.requireText(label, "label");
    }
}
