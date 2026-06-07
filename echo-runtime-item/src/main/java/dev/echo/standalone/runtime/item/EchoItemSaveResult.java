package dev.echo.standalone.runtime.item;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;

import java.util.List;
import java.util.Objects;

public record EchoItemSaveResult(EchoSaveCommitResult commit, List<String> writtenPaths) {
    public EchoItemSaveResult {
        Objects.requireNonNull(commit, "commit");
        Objects.requireNonNull(writtenPaths, "writtenPaths");
        writtenPaths = List.copyOf(writtenPaths);
    }
}
