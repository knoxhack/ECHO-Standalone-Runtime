package dev.echo.standalone.runtime.client;

import java.nio.file.Path;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

record EchoClientLaunchContext(
        boolean live,
        Path runtimeRoot,
        String profileId,
        Path installPath,
        Path packManifest,
        String devAccount,
        boolean quickPlayNewWorld
) {
    private static final EchoClientLaunchContext EMPTY =
            new EchoClientLaunchContext(false, null, "", null, null, "", false);

    EchoClientLaunchContext {
        profileId = clean(profileId);
        devAccount = clean(devAccount);
    }

    static EchoClientLaunchContext empty() {
        return EMPTY;
    }

    static EchoClientLaunchContext parse(String[] args) {
        boolean live = false;
        Path runtimeRoot = null;
        String profileId = "";
        Path installPath = null;
        Path packManifest = null;
        String devAccount = "";
        boolean quickPlayNewWorld = false;

        String[] safeArgs = args == null ? new String[0] : args;
        for (int index = 0; index < safeArgs.length; index++) {
            String raw = safeArgs[index] == null ? "" : safeArgs[index].trim();
            if (raw.isBlank()) {
                continue;
            }
            String key = raw;
            String value = "";
            int equals = raw.indexOf('=');
            if (equals >= 0) {
                key = raw.substring(0, equals).trim();
                value = raw.substring(equals + 1).trim();
            } else if (index + 1 < safeArgs.length && !safeArgs[index + 1].startsWith("--")) {
                value = safeArgs[++index].trim();
            }

            switch (key) {
                case "--live" -> {
                    live = true;
                    runtimeRoot = path(value);
                }
                case "--runtimeRoot" -> runtimeRoot = path(value);
                case "--profileId" -> profileId = value;
                case "--installPath" -> installPath = path(value);
                case "--packManifest" -> packManifest = path(value);
                case "--devAccount" -> devAccount = value;
                case "--quickPlayNewWorld" -> quickPlayNewWorld = true;
                default -> {
                    // Unknown launcher args are ignored so older packaged launchers remain compatible.
                }
            }
        }

        return new EchoClientLaunchContext(
                live,
                runtimeRoot,
                profileId,
                installPath,
                packManifest,
                devAccount,
                quickPlayNewWorld
        );
    }

    boolean hasPackContext() {
        return !profileId.isBlank() || installPath != null || packManifest != null;
    }

    String compactProfileLabel() {
        return profileId.isBlank() ? "standalone-runtime" : profileId;
    }

    String summaryLine() {
        ArrayList<String> parts = new ArrayList<>();
        parts.add("live=" + live);
        if (!profileId.isBlank()) {
            parts.add("profileId=" + profileId);
        }
        if (runtimeRoot != null) {
            parts.add("runtimeRoot=" + runtimeRoot);
        }
        if (installPath != null) {
            parts.add("installPath=" + installPath);
        }
        if (packManifest != null) {
            parts.add("packManifest=" + packManifest);
        }
        if (!devAccount.isBlank()) {
            parts.add("devAccount=" + devAccount);
        }
        if (quickPlayNewWorld) {
            parts.add("quickPlayNewWorld=true");
        }
        return String.join(" ", parts);
    }

    String screenSummary() {
        if (!hasPackContext()) {
            return "Standalone Ashfall route shell";
        }
        ArrayList<String> parts = new ArrayList<>();
        parts.add("Launch profile " + compactProfileLabel());
        if (installPath != null) {
            parts.add("install " + installPath.getFileName());
        }
        if (packManifest != null) {
            parts.add("manifest " + packManifest.getFileName());
        }
        return String.join(" | ", parts);
    }

    List<String> diagnosticsLines() {
        if (!hasPackContext()) {
            return List.of();
        }
        ArrayList<String> lines = new ArrayList<>();
        lines.add("Launch Profile: " + compactProfileLabel());
        if (devAccount != null && !devAccount.isBlank()) {
            lines.add("Dev Account: " + devAccount);
        }
        if (installPath != null) {
            lines.add("Install Path: " + installPath);
        }
        if (packManifest != null) {
            lines.add("Pack Manifest: " + packManifest);
        }
        if (runtimeRoot != null) {
            lines.add("Runtime Root: " + runtimeRoot);
        }
        if (quickPlayNewWorld) {
            lines.add("Quick Play: New World");
        }
        return List.copyOf(lines);
    }

    void appendInstanceLog(String message) {
        if (installPath == null) {
            return;
        }
        String cleanMessage = clean(message);
        if (cleanMessage.isBlank()) {
            return;
        }
        Path logPath = installPath.resolve("logs").resolve("latest.log");
        try {
            Files.createDirectories(logPath.getParent());
            Files.writeString(
                    logPath,
                    "[" + Instant.now() + "] " + cleanMessage + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // Diagnostics only; the game client must not depend on evidence log writes.
        }
    }

    private static Path path(String value) {
        String clean = clean(value);
        return clean.isBlank() ? null : Path.of(clean).toAbsolutePath().normalize();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
