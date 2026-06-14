package dev.echo.standalone.runtime.contracts;

/**
 * Well-known capability flag names used by {@link EchoRuntimeCapabilities}.
 */
public final class EchoRuntimeCapabilityFlags {

    private EchoRuntimeCapabilityFlags() {
    }

    /**
     * Enables the Minecraft-emulation code path: blockstates, NBT, Anvil saves, and vanilla
     * datapack loading. When absent, the runtime stays on the ECHO-native Ashfall alpha path.
     */
    public static final String MINECRAFT_EMULATION = "minecraft_emulation";

    /**
     * Enables the Anvil region read/write path. Implies {@link #MINECRAFT_EMULATION}.
     */
    public static final String ANVIL_IO = "anvil_io";

    /**
     * Enables the 3D noise world generator with vertical chunk sections.
     */
    public static final String VANILLA_WORLD_GENERATION = "vanilla_world_generation";

    /**
     * Enables redstone block-update propagation.
     */
    public static final String REDSTONE = "redstone";

    /**
     * Enables multiplayer socket networking.
     */
    public static final String MULTIPLAYER = "multiplayer";
}
