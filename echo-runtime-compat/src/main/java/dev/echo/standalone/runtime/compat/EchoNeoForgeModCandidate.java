package dev.echo.standalone.runtime.compat;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record EchoNeoForgeModCandidate(
        String modId,
        String displayName,
        String version,
        String license,
        List<EchoNeoForgeDependency> dependencies,
        List<String> parseWarnings,
        Path metadataPath
) {
    public static final String COMPATIBILITY_KIND = "neoforge-metadata-candidate";
    public static final String RUNTIME_STATUS = "runtime-disabled-with-reason";
    public static final String RUNTIME_REASON =
            "NeoForge metadata discovered for compatibility diagnostics only; no classloader or module code execution";

    public EchoNeoForgeModCandidate {
        modId = require(modId, "modId");
        displayName = optional(displayName).isBlank() ? modId : optional(displayName);
        version = optional(version).isBlank() ? "unknown" : optional(version);
        license = optional(license).isBlank() ? "unspecified" : optional(license);
        Objects.requireNonNull(dependencies, "dependencies");
        Objects.requireNonNull(parseWarnings, "parseWarnings");
        Objects.requireNonNull(metadataPath, "metadataPath");
        dependencies = dependencies.stream()
                .sorted(Comparator.comparing(EchoNeoForgeDependency::modId)
                        .thenComparing(EchoNeoForgeDependency::type)
                        .thenComparing(EchoNeoForgeDependency::versionRange))
                .toList();
        parseWarnings = parseWarnings.stream().sorted().toList();
        metadataPath = metadataPath.toAbsolutePath().normalize();
    }

    public String compatibilityKind() {
        return COMPATIBILITY_KIND;
    }

    public String runtimeStatus() {
        return RUNTIME_STATUS;
    }

    public String runtimeReason() {
        return RUNTIME_REASON;
    }

    public List<EchoNeoForgeDependency> requiredDependencies() {
        return dependencies.stream()
                .filter(EchoNeoForgeDependency::required)
                .toList();
    }

    public List<EchoNeoForgeDependency> optionalDependencies() {
        return dependencies.stream()
                .filter(EchoNeoForgeDependency::optionalDependency)
                .toList();
    }

    public List<EchoNeoForgeDependency> platformDependencies() {
        return dependencies.stream()
                .filter(EchoNeoForgeDependency::platformDependency)
                .toList();
    }

    public List<EchoNeoForgeDependency> nonPlatformRequiredDependencies() {
        return dependencies.stream()
                .filter(EchoNeoForgeDependency::required)
                .filter(dependency -> !dependency.platformDependency())
                .toList();
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    private static String require(String value, String name) {
        String normalized = optional(value);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }
}
