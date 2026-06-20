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
        Path packRoot,
        Path modulesRoot,
        String devAccount,
        boolean quickPlayNewWorld,
        boolean safeMode
) {
    static final String PACK_ROOT_PROPERTY = "echo.pack.root";
    static final String MODULES_ROOT_PROPERTY = "echo.modules.root";
    static final String SAFE_MODE_PROPERTY = "echo.safe.mode";
    private static final String PACK_ROOT_ENV = "ECHO_PACK_ROOT";
    private static final String MODULES_ROOT_ENV = "ECHO_MODULES_ROOT";

    private static final EchoClientLaunchContext EMPTY =
            new EchoClientLaunchContext(false, null, "", null, null, null, null, "", false, false);

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
        Path packRoot = configuredPath(PACK_ROOT_PROPERTY, PACK_ROOT_ENV);
        Path modulesRoot = configuredPath(MODULES_ROOT_PROPERTY, MODULES_ROOT_ENV);
        String devAccount = "";
        boolean quickPlayNewWorld = false;
        boolean safeMode = Boolean.parseBoolean(System.getProperty(SAFE_MODE_PROPERTY, "false"));

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
                case "--pack-root" -> packRoot = path(value);
                case "--modules-root" -> modulesRoot = path(value);
                case "--safe-mode" -> safeMode = true;
                case "--profileId" -> profileId = value;
                case "--profile" -> profileId = value;
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
                resolvePackRoot(packRoot, installPath, packManifest),
                resolveModulesRoot(modulesRoot, resolvePackRoot(packRoot, installPath, packManifest)),
                devAccount,
                quickPlayNewWorld,
                safeMode
        );
    }

    boolean hasPackContext() {
        return !profileId.isBlank()
                || installPath != null
                || packManifest != null
                || packRoot != null
                || modulesRoot != null;
    }

    boolean strictPackMode() {
        return packRoot != null || modulesRoot != null || installPath != null || packManifest != null;
    }

    void applySystemProperties() {
        if (packRoot != null) {
            System.setProperty(PACK_ROOT_PROPERTY, packRoot.toString());
        }
        if (modulesRoot != null) {
            System.setProperty(MODULES_ROOT_PROPERTY, modulesRoot.toString());
        }
        System.setProperty(SAFE_MODE_PROPERTY, Boolean.toString(safeMode));
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
        if (packRoot != null) {
            parts.add("packRoot=" + packRoot);
        }
        if (modulesRoot != null) {
            parts.add("modulesRoot=" + modulesRoot);
        }
        if (!devAccount.isBlank()) {
            parts.add("devAccount=" + devAccount);
        }
        if (quickPlayNewWorld) {
            parts.add("quickPlayNewWorld=true");
        }
        if (safeMode) {
            parts.add("safeMode=true");
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
        if (packRoot != null) {
            parts.add("pack " + packRoot.getFileName());
        }
        if (safeMode) {
            parts.add("safe mode");
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
        if (packRoot != null) {
            lines.add("Pack Root: " + packRoot);
        }
        if (modulesRoot != null) {
            lines.add("Modules Root: " + modulesRoot);
        }
        if (quickPlayNewWorld) {
            lines.add("Quick Play: New World");
        }
        if (safeMode) {
            lines.add("Safe Mode: true");
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

    private static Path configuredPath(String property, String environment) {
        String configured = clean(System.getProperty(property, ""));
        if (configured.isBlank()) {
            configured = clean(System.getenv(environment));
        }
        return configured.isBlank() ? null : Path.of(configured).toAbsolutePath().normalize();
    }

    private static Path resolvePackRoot(Path packRoot, Path installPath, Path packManifest) {
        if (packRoot != null) {
            return packRoot;
        }
        if (installPath != null) {
            return installPath;
        }
        if (packManifest != null) {
            Path parent = packManifest.getParent();
            if (parent != null && ".echo".equals(parent.getFileName() == null ? "" : parent.getFileName().toString())) {
                return parent.getParent();
            }
            return parent;
        }
        return null;
    }

    private static Path resolveModulesRoot(Path modulesRoot, Path packRoot) {
        if (modulesRoot != null) {
            return modulesRoot;
        }
        return packRoot == null ? null : packRoot.resolve("mods").toAbsolutePath().normalize();
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
