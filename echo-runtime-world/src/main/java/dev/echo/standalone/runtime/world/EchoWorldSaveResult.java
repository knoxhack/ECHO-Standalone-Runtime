package dev.echo.standalone.runtime.world;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;

import java.util.List;
import java.util.Objects;

public record EchoWorldSaveResult(
        EchoSaveCommitResult commit,
        List<String> writtenPaths
) {
    public EchoWorldSaveResult {
        Objects.requireNonNull(commit, "commit");
        Objects.requireNonNull(writtenPaths, "writtenPaths");
        writtenPaths = writtenPaths.stream().sorted().toList();
    }
}
