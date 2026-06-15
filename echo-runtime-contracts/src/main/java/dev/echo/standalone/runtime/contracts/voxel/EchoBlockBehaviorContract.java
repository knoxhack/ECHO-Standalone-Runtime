package dev.echo.standalone.runtime.contracts.voxel;

/**
 * Physical and gameplay behavior of a block type.
 *
 * <p>This contract is intentionally platform-neutral: it mirrors Minecraft block properties
 * without importing Minecraft/NeoForge/LWJGL types.
 */
public interface EchoBlockBehaviorContract {

    String blockId();

    double destroyTime();

    double explosionResistance();

    double friction();

    double speedFactor();

    double jumpFactor();

    int lightEmission();

    int lightOpacity();

    boolean requiresTool();

    String harvestTool();

    int harvestLevel();

    boolean solid();

    boolean blocksMotion();

    boolean opaque();

    boolean randomTick();

    boolean flammable();

    int fireSpreadSpeed();
}
