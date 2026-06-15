package dev.echo.standalone.runtime.core;

import dev.echo.standalone.runtime.contracts.EchoRuntimeCapabilityFlags;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCapabilities;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Runtime feature flags controlling optional Minecraft-emulation and advanced engine paths.
 *
 * <p>Flags can be set via:
 * <ul>
 *   <li>System property: {@code -Decho.runtime.minecraftEmulation=true}</li>
 *   <li>Environment variable: {@code ECHO_RUNTIME_MINECRAFT_EMULATION=true}</li>
 *   <li>Programmatically through {@link #with(String, boolean)}</li>
 * </ul>
 */
public final class EchoRuntimeFeatureFlags {

    public static final String MINECRAFT_EMULATION_PROPERTY = "echo.runtime.minecraftEmulation";
    public static final String MINECRAFT_EMULATION_ENV = "ECHO_RUNTIME_MINECRAFT_EMULATION";

    private final Set<String> enabled;

    public EchoRuntimeFeatureFlags() {
        this(readDefaultFlags());
    }

    public EchoRuntimeFeatureFlags(Set<String> enabled) {
        this.enabled = new HashSet<>(enabled);
    }

    public boolean minecraftEmulation() {
        return enabled.contains(EchoRuntimeCapabilityFlags.MINECRAFT_EMULATION);
    }

    public boolean anvilIo() {
        return enabled.contains(EchoRuntimeCapabilityFlags.ANVIL_IO);
    }

    public boolean vanillaWorldGeneration() {
        return enabled.contains(EchoRuntimeCapabilityFlags.VANILLA_WORLD_GENERATION);
    }

    public boolean redstone() {
        return enabled.contains(EchoRuntimeCapabilityFlags.REDSTONE);
    }

    public boolean multiplayer() {
        return enabled.contains(EchoRuntimeCapabilityFlags.MULTIPLAYER);
    }

    public boolean enabled(String flag) {
        return flag != null && enabled.contains(flag);
    }

    public EchoRuntimeFeatureFlags with(String flag, boolean value) {
        if (flag == null || flag.isBlank()) {
            throw new IllegalArgumentException("flag must not be blank");
        }
        Set<String> next = new HashSet<>(enabled);
        if (value) {
            next.add(flag);
        } else {
            next.remove(flag);
        }
        return new EchoRuntimeFeatureFlags(next);
    }

    public EchoRuntimeCapabilities toCapabilities() {
        return new EchoRuntimeCapabilities(enabled);
    }

    public static Set<String> readDefaultFlags() {
        Set<String> flags = new HashSet<>();
        if (readBoolean(MINECRAFT_EMULATION_PROPERTY, MINECRAFT_EMULATION_ENV, false)) {
            flags.add(EchoRuntimeCapabilityFlags.MINECRAFT_EMULATION);
        }
        return flags;
    }

    private static boolean readBoolean(String property, String env, boolean defaultValue) {
        String value = System.getProperty(property);
        if (value == null) {
            value = System.getenv(env);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.trim().toLowerCase(Locale.ROOT));
    }
}
