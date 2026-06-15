package dev.echo.standalone.runtime.nbt;

import java.util.Arrays;

/**
 * NBT long array tag.
 */
public record EchoNbtLongArray(long[] value) implements EchoNbtTag {

    public EchoNbtLongArray {
        value = value.clone();
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.LONG_ARRAY;
    }

    public long[] copyValue() {
        return value.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoNbtLongArray other)) {
            return false;
        }
        return Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
