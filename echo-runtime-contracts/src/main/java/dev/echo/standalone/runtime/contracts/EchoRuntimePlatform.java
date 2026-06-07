package dev.echo.standalone.runtime.contracts;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record EchoRuntimePlatform(
        String id,
        String displayName,
        boolean standalone,
        boolean minecraftCompatible,
        Set<String> adapterKinds
) {
    public EchoRuntimePlatform {
        id = requireText(id, "id");
        displayName = requireText(displayName, "displayName");
        Objects.requireNonNull(adapterKinds, "adapterKinds");
        adapterKinds = Set.copyOf(new TreeSet<>(adapterKinds));
    }

    public static EchoRuntimePlatform standaloneEcho() {
        return new EchoRuntimePlatform("echo_standalone", "ECHO Standalone Runtime", true, false, Set.of());
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
