package dev.echo.standalone.runtime.contracts.voxel;

/**
 * Contract for a 16×16×16 chunk section storing blockstates and optional biome data.
 *
 * <p>Implementations are free to use palette compression, dense arrays, or sparse storage as
 * long as coordinates are resolved deterministically.
 */
public interface EchoChunkSectionContract {

    int SECTION_SIZE = 16;
    int SECTION_VOLUME = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;

    /**
     * Returns the section's Y index (block Y divided by 16).
     */
    int sectionY();

    /**
     * Returns the blockstate at local coordinates [0,15].
     */
    EchoBlockStateContract stateAt(int localX, int localY, int localZ);

    /**
     * Sets the blockstate at local coordinates [0,15].
     */
    void setState(int localX, int localY, int localZ, EchoBlockStateContract state);

    /**
     * Returns {@code true} if every state in this section is air.
     */
    boolean empty();

    /**
     * Returns the section data version, incremented on every mutation.
     */
    long version();
}
