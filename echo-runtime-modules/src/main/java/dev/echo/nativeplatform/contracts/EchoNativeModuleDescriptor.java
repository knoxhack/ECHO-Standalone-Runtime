package dev.echo.nativeplatform.contracts;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record EchoNativeModuleDescriptor(
        String id,
        String name,
        String version,
        String kind,
        String role,
        String entrypoint,
        EchoNativeRuntimeSide side,
        List<String> requires,
        List<String> optional,
        List<String> provides,
        Path descriptorPath,
        List<Path> classpath
) {
    public EchoNativeModuleDescriptor {
        id = requireText(id, "id");
        name = requireText(name, "name");
        version = version == null || version.isBlank() ? "0.0.0" : version.trim();
        kind = kind == null || kind.isBlank() ? "runtime_module" : kind.trim();
        role = role == null ? "" : role.trim();
        entrypoint = entrypoint == null ? "" : entrypoint.trim();
        side = Objects.requireNonNullElse(side, EchoNativeRuntimeSide.UNKNOWN);
        requires = requires == null ? List.of() : List.copyOf(requires);
        optional = optional == null ? List.of() : List.copyOf(optional);
        provides = provides == null ? List.of() : List.copyOf(provides);
        classpath = classpath == null ? List.of() : List.copyOf(classpath);
    }

    public boolean hasEntrypoint() {
        return !entrypoint.isBlank();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
