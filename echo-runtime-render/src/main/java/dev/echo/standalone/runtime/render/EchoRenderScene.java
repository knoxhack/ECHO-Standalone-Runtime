package dev.echo.standalone.runtime.render;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record EchoRenderScene(
        String sceneId,
        EchoRenderCamera camera,
        List<EchoRenderCommand> commands
) {
    public EchoRenderScene {
        sceneId = EchoRenderText.requireText(sceneId, "sceneId");
        Objects.requireNonNull(camera, "camera");
        Objects.requireNonNull(commands, "commands");
        commands = commands.stream()
                .sorted(Comparator
                        .comparingInt((EchoRenderCommand command) -> command.layer().order())
                        .thenComparing(EchoRenderCommand::commandId))
                .toList();
    }

    public List<EchoRenderCommand> commandsForLayer(EchoRenderLayer layer) {
        Objects.requireNonNull(layer, "layer");
        return commands.stream()
                .filter(command -> command.layer() == layer)
                .toList();
    }

    public long commandCount(EchoRenderLayer layer) {
        return commandsForLayer(layer).size();
    }
}
