package dev.echo.standalone.runtime.modules;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoRuntimeModuleDescriptor(
        String schema,
        String id,
        String name,
        String version,
        String kind,
        String role,
        EchoRuntimeModuleSide side,
        String trust,
        boolean official,
        boolean standalone,
        List<String> requires,
        List<String> optional,
        List<String> provides,
        List<String> consumes,
        List<String> gameModes,
        List<String> permissions,
        List<String> aliases,
        List<String> classPath,
        String entrypoint,
        String adapterCoreEntrypoint,
        String nativeEntrypoint,
        Map<String, String> requiresVersions,
        Map<String, String> optionalVersions,
        Map<String, Object> access,
        Path descriptorPath,
        Path moduleRoot
) {
    public EchoRuntimeModuleDescriptor {
        schema = requireText(schema, "schema");
        id = requireText(id, "id");
        name = requireText(name, "name");
        version = requireText(version, "version");
        kind = requireText(kind, "kind");
        role = role == null ? "" : role;
        Objects.requireNonNull(side, "side");
        trust = requireText(trust, "trust");
        Objects.requireNonNull(requires, "requires");
        Objects.requireNonNull(optional, "optional");
        Objects.requireNonNull(provides, "provides");
        Objects.requireNonNull(consumes, "consumes");
        Objects.requireNonNull(gameModes, "gameModes");
        Objects.requireNonNull(permissions, "permissions");
        Objects.requireNonNull(aliases, "aliases");
        Objects.requireNonNull(classPath, "classPath");
        entrypoint = entrypoint == null ? "" : entrypoint;
        adapterCoreEntrypoint = adapterCoreEntrypoint == null ? "" : adapterCoreEntrypoint;
        nativeEntrypoint = nativeEntrypoint == null ? "" : nativeEntrypoint;
        Objects.requireNonNull(requiresVersions, "requiresVersions");
        Objects.requireNonNull(optionalVersions, "optionalVersions");
        Objects.requireNonNull(access, "access");
        Objects.requireNonNull(descriptorPath, "descriptorPath");
        Objects.requireNonNull(moduleRoot, "moduleRoot");
        requires = sortedCopy(requires);
        optional = sortedCopy(optional);
        provides = sortedCopy(provides);
        consumes = sortedCopy(consumes);
        gameModes = sortedCopy(gameModes);
        permissions = sortedCopy(permissions);
        String canonicalId = id;
        aliases = sortedCopy(aliases).stream()
                .filter(alias -> !alias.equals(canonicalId))
                .toList();
        classPath = sortedCopy(classPath);
        requiresVersions = sortedMapCopy(requiresVersions);
        optionalVersions = sortedMapCopy(optionalVersions);
        access = Map.copyOf(access);
    }

    public String executableEntrypoint() {
        if (!adapterCoreEntrypoint.isBlank()) {
            return adapterCoreEntrypoint;
        }
        return !nativeEntrypoint.isBlank() ? nativeEntrypoint : entrypoint;
    }

    private static List<String> sortedCopy(List<String> values) {
        return values.stream().sorted().toList();
    }

    private static Map<String, String> sortedMapCopy(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (left, right) -> left,
                                java.util.LinkedHashMap::new
                        ),
                        java.util.Collections::unmodifiableMap
                ));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
