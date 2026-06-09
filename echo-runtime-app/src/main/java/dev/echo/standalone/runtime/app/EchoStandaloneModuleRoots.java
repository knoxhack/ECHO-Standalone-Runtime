package dev.echo.standalone.runtime.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class EchoStandaloneModuleRoots {
    private EchoStandaloneModuleRoots() {
    }

    static List<Path> resolve(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        ArrayList<Path> roots = new ArrayList<>();
        addIfDirectory(roots, modulesRoot(normalizedRoot));
        addIfDirectory(roots, normalizedRoot.resolve("src/main/resources"));
        return List.copyOf(roots);
    }

    static Path modulesRepoRoot(Path workspaceRoot) {
        Path modulesRoot = modulesRoot(workspaceRoot);
        Path parent = modulesRoot.getParent();
        return parent == null ? modulesRoot : parent;
    }

    private static Path modulesRoot(Path workspaceRoot) {
        String configured = System.getProperty("echo.modules.root");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("ECHO_MODULES_ROOT");
        }
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }

        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        ArrayList<Path> candidates = new ArrayList<>();
        candidates.add(normalizedRoot.resolve("ECHO-Modules/addons"));
        candidates.add(normalizedRoot.resolve("addons"));
        Path parent = normalizedRoot.getParent();
        if (parent != null) {
            candidates.add(parent.resolve("ECHO-Modules/addons"));
            candidates.add(parent.resolve("addons"));
        }
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                return candidate.toAbsolutePath().normalize();
            }
        }
        return candidates.get(0).toAbsolutePath().normalize();
    }

    private static void addIfDirectory(List<Path> roots, Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path.toAbsolutePath().normalize());
        }
    }
}
