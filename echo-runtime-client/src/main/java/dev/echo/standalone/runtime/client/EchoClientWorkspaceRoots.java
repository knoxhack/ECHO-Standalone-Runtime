package dev.echo.standalone.runtime.client;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.LinkedHashSet;
import java.util.List;

final class EchoClientWorkspaceRoots {
    private static final String STANDALONE_ROOT_NAME = "echo-standalone-runtime";
    private static final String STANDALONE_REPO_NAME = "ECHO-Standalone-Runtime";
    private static final String MODULES_REPO_NAME = "ECHO-Modules";
    private static final String MODULES_ROOT_PROPERTY = "echo.modules.root";
    private static final String MODULES_ROOT_ENV = "ECHO_MODULES_ROOT";

    private EchoClientWorkspaceRoots() {
    }

    static List<Path> launchAnchors() {
        LinkedHashSet<Path> anchors = new LinkedHashSet<>();
        addAnchor(anchors, Path.of(System.getProperty("user.dir", ".")));
        addAnchor(anchors, Path.of("."));
        CodeSource codeSource = EchoClientWorkspaceRoots.class.getProtectionDomain().getCodeSource();
        if (codeSource != null && codeSource.getLocation() != null) {
            try {
                addAnchor(anchors, Path.of(codeSource.getLocation().toURI()));
            } catch (URISyntaxException | IllegalArgumentException ignored) {
            }
        }
        return List.copyOf(anchors);
    }

    static List<Path> standaloneRuntimeRoots() {
        return standaloneRuntimeRoots(launchAnchors());
    }

    static List<Path> standaloneRuntimeRoots(List<Path> anchors) {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        for (Path anchor : safeAnchors(anchors)) {
            addStandaloneRoots(roots, anchor);
        }
        return List.copyOf(roots);
    }

    static List<Path> echoWorkspaceRoots() {
        return echoWorkspaceRoots(launchAnchors());
    }

    static List<Path> echoWorkspaceRoots(List<Path> anchors) {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        for (Path standaloneRoot : standaloneRuntimeRoots(anchors)) {
            Path parent = standaloneRoot.getParent();
            if (workspaceLike(parent)) {
                roots.add(parent);
            }
        }
        for (Path anchor : safeAnchors(anchors)) {
            Path current = directoryAnchor(anchor);
            while (current != null) {
                if (workspaceLike(current)) {
                    roots.add(current);
                }
                current = current.getParent();
            }
        }
        return List.copyOf(roots);
    }

    static List<Path> echoModuleAddonRoots() {
        return echoModuleAddonRoots(launchAnchors());
    }

    static List<Path> echoModuleAddonRoots(List<Path> anchors) {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        addIfDirectory(roots, configuredModulesRoot());
        for (Path echoRoot : echoWorkspaceRoots(anchors)) {
            addIfDirectory(roots, echoRoot.resolve("addons"));
            addIfDirectory(roots, echoRoot.resolve(MODULES_REPO_NAME).resolve("addons"));
        }
        for (Path standaloneRoot : standaloneRuntimeRoots(anchors)) {
            Path parent = standaloneRoot.getParent();
            if (parent != null) {
                addIfDirectory(roots, parent.resolve(MODULES_REPO_NAME).resolve("addons"));
                addIfDirectory(roots, parent.resolve("addons"));
            }
        }
        return List.copyOf(roots);
    }

    private static void addStandaloneRoots(LinkedHashSet<Path> roots, Path anchor) {
        Path current = directoryAnchor(anchor);
        while (current != null) {
            if (standaloneLike(current)) {
                roots.add(current);
            }
            Path nested = current.resolve(STANDALONE_ROOT_NAME);
            if (standaloneLike(nested)) {
                roots.add(nested.toAbsolutePath().normalize());
            }
            current = current.getParent();
        }
    }

    private static List<Path> safeAnchors(List<Path> anchors) {
        if (anchors == null || anchors.isEmpty()) {
            return launchAnchors();
        }
        return anchors.stream()
                .filter(anchor -> anchor != null)
                .map(Path::toAbsolutePath)
                .map(Path::normalize)
                .toList();
    }

    private static void addAnchor(LinkedHashSet<Path> anchors, Path anchor) {
        if (anchor != null) {
            anchors.add(anchor.toAbsolutePath().normalize());
        }
    }

    private static void addIfDirectory(LinkedHashSet<Path> roots, Path path) {
        if (path == null) {
            return;
        }
        Path normalized = path.toAbsolutePath().normalize();
        if (Files.isDirectory(normalized)) {
            roots.add(normalized);
        }
    }

    private static Path configuredModulesRoot() {
        String configured = System.getProperty(MODULES_ROOT_PROPERTY, "").trim();
        if (configured.isBlank()) {
            configured = System.getenv(MODULES_ROOT_ENV);
        }
        if (configured == null || configured.isBlank()) {
            return null;
        }
        return Path.of(configured);
    }

    private static Path directoryAnchor(Path anchor) {
        Path normalized = anchor.toAbsolutePath().normalize();
        return Files.isRegularFile(normalized) ? normalized.getParent() : normalized;
    }

    private static boolean standaloneLike(Path path) {
        return path != null
                && Files.isRegularFile(path.resolve("settings.gradle"))
                && Files.isDirectory(path.resolve("echo-runtime-client"));
    }

    private static boolean workspaceLike(Path path) {
        return path != null
                && (Files.isDirectory(path.resolve(STANDALONE_ROOT_NAME))
                        || Files.isDirectory(path.resolve(STANDALONE_REPO_NAME)))
                && (Files.isDirectory(path.resolve("addons"))
                        || Files.isDirectory(path.resolve("core"))
                        || Files.isDirectory(path.resolve(MODULES_REPO_NAME).resolve("addons"))
                        || Files.isDirectory(path.resolve(MODULES_REPO_NAME).resolve("core")));
    }
}
