package dev.echo.standalone.runtime.world.chunk;

import java.util.Arrays;
import java.util.Objects;

/**
 * Fixed-size bit-packed storage backed by a long array.
 *
 * <p>Each entry uses {@code bitsPerEntry} bits. The storage size is immutable; values can be
 * updated in place. This is the same primitive used by Minecraft chunk sections and Anvil
 * palettes.
 */
public final class EchoBitStorage {

    private final long[] data;
    private final int size;
    private final int bitsPerEntry;
    private final long mask;

    public EchoBitStorage(int bitsPerEntry, int size) {
        this(bitsPerEntry, size, null);
    }

    public EchoBitStorage(int bitsPerEntry, int size, long[] data) {
        if (bitsPerEntry < 1 || bitsPerEntry > 64) {
            throw new IllegalArgumentException("bitsPerEntry must be in [1, 64]");
        }
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative");
        }
        this.bitsPerEntry = bitsPerEntry;
        this.size = size;
        this.mask = bitsPerEntry == 64 ? ~0L : ((1L << bitsPerEntry) - 1L);
        long totalBits = (long) size * bitsPerEntry;
        int longCount = (int) ((totalBits + 63L) / 64L);
        if (data == null) {
            this.data = new long[longCount];
        } else if (data.length != longCount) {
            throw new IllegalArgumentException(
                    "Data array length " + data.length + " does not match expected " + longCount
            );
        } else {
            this.data = data.clone();
        }
    }

    public int size() {
        return size;
    }

    public int bitsPerEntry() {
        return bitsPerEntry;
    }

    public long get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [0, " + size + ")");
        }
        long bitIndex = (long) index * bitsPerEntry;
        int longIndex = (int) (bitIndex >>> 6);
        int bitOffset = (int) (bitIndex & 0x3F);
        if (bitOffset + bitsPerEntry <= 64) {
            return (data[longIndex] >>> bitOffset) & mask;
        }
        int remaining = 64 - bitOffset;
        long low = (data[longIndex] >>> bitOffset) & ((1L << remaining) - 1L);
        long high = (data[longIndex + 1] << remaining) & mask;
        return low | high;
    }

    public void set(int index, long value) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds [0, " + size + ")");
        }
        if (bitsPerEntry < 64 && (value >>> bitsPerEntry) != 0L) {
            throw new IllegalArgumentException(
                    "Value " + value + " does not fit in " + bitsPerEntry + " bits"
            );
        }
        long bitIndex = (long) index * bitsPerEntry;
        int longIndex = (int) (bitIndex >>> 6);
        int bitOffset = (int) (bitIndex & 0x3F);
        if (bitOffset + bitsPerEntry <= 64) {
            data[longIndex] = (data[longIndex] & ~(mask << bitOffset)) | (value << bitOffset);
        } else {
            int remaining = 64 - bitOffset;
            data[longIndex] = (data[longIndex] & ((1L << bitOffset) - 1L)) | (value << bitOffset);
            data[longIndex + 1] = (data[longIndex + 1] & ~((1L << (bitsPerEntry - remaining)) - 1L))
                    | (value >>> remaining);
        }
    }

    public long[] rawData() {
        return data.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoBitStorage other)) {
            return false;
        }
        return size == other.size && bitsPerEntry == other.bitsPerEntry && Arrays.equals(data, other.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, bitsPerEntry, Arrays.hashCode(data));
    }
}
