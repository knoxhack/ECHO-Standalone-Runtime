package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneSupportBundle(
        String bundleId,
        String generatedAt,
        List<EchoStandaloneSupportBundleEntry> entries,
        List<String> diagnostics,
        String manifestPath,
        boolean manifestPresent,
        int manifestEntryCount,
        String archivePath,
        boolean archivePresent,
        long archiveByteSize
) {
    public EchoStandaloneSupportBundle {
        bundleId = requireText(bundleId, "bundleId");
        generatedAt = requireText(generatedAt, "generatedAt");
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(diagnostics, "diagnostics");
        manifestPath = requireText(manifestPath, "manifestPath");
        archivePath = requireText(archivePath, "archivePath");
        if (manifestEntryCount < 0) {
            throw new IllegalArgumentException("manifestEntryCount must not be negative");
        }
        if (archiveByteSize < 0L) {
            throw new IllegalArgumentException("archiveByteSize must not be negative");
        }
        entries = List.copyOf(entries);
        diagnostics = diagnostics.stream()
                .map(diagnostic -> requireText(diagnostic, "diagnostic"))
                .toList();
    }

    public int presentEntryCount() {
        return (int) entries.stream().filter(EchoStandaloneSupportBundleEntry::present).count();
    }

    public boolean complete() {
        return presentEntryCount() == entries.size()
                && manifestPresent
                && manifestEntryCount >= entries.size()
                && archivePresent
                && archiveByteSize > 0L;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim().replace('\\', '/');
    }
}
