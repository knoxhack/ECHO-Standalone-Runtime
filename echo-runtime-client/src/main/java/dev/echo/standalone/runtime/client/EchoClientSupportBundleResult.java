package dev.echo.standalone.runtime.client;

record EchoClientSupportBundleResult(
        boolean exported,
        String archivePath,
        String manifestPath,
        int entryCount,
        long archiveBytes,
        String message
) {
    static final EchoClientSupportBundleResult EMPTY =
            new EchoClientSupportBundleResult(false, "", "", 0, 0L, "No support bundle exported");

    EchoClientSupportBundleResult {
        archivePath = clean(archivePath);
        manifestPath = clean(manifestPath);
        entryCount = Math.max(0, entryCount);
        archiveBytes = Math.max(0L, archiveBytes);
        message = clean(message);
        exported = exported && !archivePath.isBlank() && archiveBytes > 0L;
        if (message.isBlank()) {
            message = exported ? "Support bundle exported" : "Support bundle not exported";
        }
    }

    String menuLabel() {
        if (!exported) {
            return "Support Bundle: Not exported";
        }
        return "Support Bundle: " + entryCount + " files, " + archiveBytes + " bytes";
    }

    String toastLabel() {
        if (!exported) {
            return message;
        }
        return "Support bundle exported: " + archivePath;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replace('\\', '/');
    }
}
