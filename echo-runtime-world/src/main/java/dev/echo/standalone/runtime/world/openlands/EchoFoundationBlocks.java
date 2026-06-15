package dev.echo.standalone.runtime.world.openlands;

import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;

/**
 * Bootstraps Foundation module blocks required by Openlands terrain and recipes.
 *
 * <p>These blocks are referenced by Openlands biome palettes and recipes but are not defined in
 * the Openlands block JSON. They are registered here with simple defaults.
 */
public final class EchoFoundationBlocks {

    private EchoFoundationBlocks() {
    }

    public static void registerAll(EchoBlockRegistry registry) {
        register(registry, "echomaterialcore:fieldstone", "Fieldstone");
        register(registry, "echomaterialcore:sand", "Sand");
        register(registry, "echomaterialcore:gravel", "Gravel");
        register(registry, "echomaterialcore:clay", "Clay");
        register(registry, "echomaterialcore:branchwood_log", "Branchwood Log");
        register(registry, "echoworldstarter:bedrock", "Bedrock");
    }

    private static void register(EchoBlockRegistry registry, String id, String displayName) {
        if (!registry.find(id).isPresent()) {
            registry.register(id, displayName);
        }
    }
}
