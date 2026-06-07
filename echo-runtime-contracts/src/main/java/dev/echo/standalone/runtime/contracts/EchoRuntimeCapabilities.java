package dev.echo.standalone.runtime.contracts;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record EchoRuntimeCapabilities(Set<String> flags) {
    public EchoRuntimeCapabilities {
        Objects.requireNonNull(flags, "flags");
        flags = Set.copyOf(new TreeSet<>(flags));
    }

    public static EchoRuntimeCapabilities empty() {
        return new EchoRuntimeCapabilities(Set.of());
    }

    public static EchoRuntimeCapabilities of(Collection<String> flags) {
        return new EchoRuntimeCapabilities(Set.copyOf(flags));
    }

    public boolean supports(String flag) {
        return flags.contains(flag);
    }
}
