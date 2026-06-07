package dev.echo.standalone.runtime.save;

import java.util.List;
import java.util.Objects;

public record EchoSaveCorruptionReport(
        String profileId,
        String slotId,
        boolean healthy,
        int checkedFiles,
        int journalEntries,
        List<EchoSaveCorruptionIssue> issues
) {
    public EchoSaveCorruptionReport {
        profileId = EchoSavePaths.requireText(profileId, "profileId");
        slotId = EchoSavePaths.requireText(slotId, "slotId");
        if (checkedFiles < 0) {
            throw new IllegalArgumentException("checkedFiles must not be negative");
        }
        if (journalEntries < 0) {
            throw new IllegalArgumentException("journalEntries must not be negative");
        }
        Objects.requireNonNull(issues, "issues");
        issues = List.copyOf(issues);
        healthy = issues.stream().noneMatch(issue -> issue.severity() == EchoSaveCorruptionSeverity.ERROR);
    }
}
