package dev.echo.standalone.runtime.contracts.voxel;

/**
 * Contract for a block entity (tile entity): extra data attached to a block position.
 *
 * <p>The runtime stores block entity data as an opaque payload so that implementation modules can
 * later attach NBT or component storage without changing the contract.
 */
public interface EchoBlockEntityContract {

    /**
     * Returns the block entity's type ID, e.g. {@code "minecraft:furnace"}.
     */
    String id();

    /**
     * Returns the world position of the block entity.
     */
    EchoWorldPosition position();

    /**
     * Returns the blockstate at the block entity's position at the time the entity was created.
     */
    EchoBlockStateContract blockState();

    /**
     * Returns {@code true} if the block entity is still valid for its current blockstate.
     */
    boolean valid();
}
