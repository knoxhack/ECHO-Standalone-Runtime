package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;

import java.util.List;
import java.util.Objects;

public record EchoGameplaySaveResult(EchoSaveCommitResult commit, List<String> writtenPaths) {
    public EchoGameplaySaveResult {
        Objects.requireNonNull(commit, "commit");
        Objects.requireNonNull(writtenPaths, "writtenPaths");
        writtenPaths = List.copyOf(writtenPaths);
    }
}
