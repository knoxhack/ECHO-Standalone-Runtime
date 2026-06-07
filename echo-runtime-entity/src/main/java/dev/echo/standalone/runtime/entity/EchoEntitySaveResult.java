package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;

import java.util.List;
import java.util.Objects;

public record EchoEntitySaveResult(EchoSaveCommitResult commit, List<String> writtenPaths) {
    public EchoEntitySaveResult {
        Objects.requireNonNull(commit, "commit");
        Objects.requireNonNull(writtenPaths, "writtenPaths");
        writtenPaths = List.copyOf(writtenPaths);
    }
}
