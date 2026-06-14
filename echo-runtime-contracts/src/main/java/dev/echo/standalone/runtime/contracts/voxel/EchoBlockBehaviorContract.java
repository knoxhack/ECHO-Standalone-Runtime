package dev.echo.standalone.runtime.contracts.voxel;

/**
 * Contract for data-driven block behavior metadata.
 *
 * <p>Behavior is intentionally separate from {@link EchoBlockContract} so that modules and
 * datapacks can attach behavior to blocks without changing the core block identity.
 */
public interface EchoBlockBehaviorContract {

    /**
     * Returns the block ID this behavior describes.
     */
    String blockId();

    /**
     * Returns how many seconds the block resists breaking with an unmodified hand.
     */
    double hardness();

    /**
     * Returns explosion resistance.
     */
    double blastResistance();

    /**
     * Returns required harvest tool category, e.g. {@code "pickaxe"}, {@code "axe"}, or empty.
     */
    String harvestTool();

    /**
     * Returns required harvest level (wood=0, stone=1, iron=2, diamond=3, netherite=4).
     */
    int harvestLevel();

    /**
     * Returns light emission level [0,15].
     */
    int lightEmission();

    /**
     * Returns light opacity [0,15]; 15 means full occlusion.
     */
    int lightOpacity();

    /**
     * Returns friction factor (default 0.6).
     */
    double friction();

    /**
     * Returns jump factor (default 1.0).
     */
    double jumpFactor();

    /**
     * Returns speed factor (default 1.0).
     */
    double speedFactor();

    /**
     * Returns {@code true} if the block randomly ticks.
     */
    boolean randomTick();

    /**
     * Returns {@code true} if the block is a solid full cube by default.
     */
    boolean solid();

    /**
     * Returns {@code true} if the block blocks light by default.
     */
    boolean opaque();
}
