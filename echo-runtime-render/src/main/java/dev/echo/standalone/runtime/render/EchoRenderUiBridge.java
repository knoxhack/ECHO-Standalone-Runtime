package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.ui.EchoUiFrame;
import dev.echo.standalone.runtime.ui.EchoUiSurface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRenderUiBridge {
    public List<EchoRenderCommand> commands(EchoUiFrame frame) {
        Objects.requireNonNull(frame, "frame");
        ArrayList<EchoRenderCommand> commands = new ArrayList<>();
        appendSurface(commands, "screen", frame.screen(), 0.0D, frame.theme().id());
        int modalIndex = 0;
        for (EchoUiSurface modal : frame.modals()) {
            appendSurface(commands, "modal-" + modalIndex, modal, 20.0D + modalIndex, frame.theme().id());
            modalIndex++;
        }
        return List.copyOf(commands);
    }

    private static void appendSurface(
            ArrayList<EchoRenderCommand> commands,
            String prefix,
            EchoUiSurface surface,
            double yOffset,
            String themeId
    ) {
        commands.add(new EchoRenderCommand(
                "ui:" + prefix + ":surface:" + surface.id(),
                EchoRenderLayer.UI,
                EchoRenderCommandType.UI_SURFACE,
                0.0D,
                yOffset,
                10.0D,
                42.0D,
                surface.lines().size() + 2.0D,
                "ui-theme:" + themeId,
                surface.title()
        ));
        commands.add(new EchoRenderCommand(
                "ui:" + prefix + ":title:" + surface.id(),
                EchoRenderLayer.UI,
                EchoRenderCommandType.TEXT,
                1.0D,
                yOffset + 1.0D,
                11.0D,
                0.0D,
                0.0D,
                "ui-text:headline",
                surface.title()
        ));
        for (int index = 0; index < surface.lines().size(); index++) {
            commands.add(new EchoRenderCommand(
                    "ui:" + prefix + ":line:" + index + ":" + surface.id(),
                    EchoRenderLayer.UI,
                    EchoRenderCommandType.TEXT,
                    1.0D,
                    yOffset + 2.0D + index,
                    11.0D,
                    0.0D,
                    0.0D,
                    "ui-text:body",
                    surface.lines().get(index)
            ));
        }
    }
}
