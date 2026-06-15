package dev.echo.standalone.runtime.nbt;

import java.util.Arrays;

/**
 * NBT int array tag.
 */
public record EchoNbtIntArray(int[] value) implements EchoNbtTag {

    public EchoNbtIntArray {
        value = value.clone();
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.INT_ARRAY;
    }

    public int[] copyValue() {
        return value.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoNbtIntArray other)) {
            return false;
        }
        return Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
