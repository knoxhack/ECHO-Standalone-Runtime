package dev.echo.standalone.runtime.world.block.state;

/**
 * Initializes a minimal set of vanilla Minecraft blocks in an {@link EchoBlockRegistry}.
 *
 * <p>This is a bootstrap set for testing Anvil import. A production runtime should generate the
 * full vanilla block table from Minecraft data or load it from a datapack.
 */
public final class EchoVanillaBlocks {

    private EchoVanillaBlocks() {
    }

    public static void registerAll(EchoBlockRegistry registry) {
        registry.register("minecraft:stone", "Stone");
        registry.register("minecraft:dirt", "Dirt");
        registry.register("minecraft:grass_block", "Grass Block")
                .property(new EchoBlockPropertyBoolean("snowy"));
        registry.register("minecraft:bedrock", "Bedrock");
        registry.register("minecraft:sand", "Sand")
                .property(new EchoBlockPropertyBoolean("falling"));
        registry.register("minecraft:gravel", "Gravel")
                .property(new EchoBlockPropertyBoolean("falling"));
        registry.register("minecraft:water", "Water")
                .property(new EchoBlockPropertyInteger("level", 0, 15));
        registry.register("minecraft:lava", "Lava")
                .property(new EchoBlockPropertyInteger("level", 0, 15));
        registry.register("minecraft:oak_log", "Oak Log")
                .property(new EchoBlockPropertyEnum("axis", "y", "x", "z"));
        registry.register("minecraft:oak_planks", "Oak Planks");
        registry.register("minecraft:oak_leaves", "Oak Leaves")
                .property(new EchoBlockPropertyInteger("distance", 1, 7))
                .property(new EchoBlockPropertyBoolean("persistent"))
                .property(new EchoBlockPropertyBoolean("waterlogged"));
        registry.register("minecraft:torch", "Torch");
    }
}
