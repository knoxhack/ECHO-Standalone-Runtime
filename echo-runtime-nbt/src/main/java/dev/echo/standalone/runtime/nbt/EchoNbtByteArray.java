package dev.echo.standalone.runtime.nbt;

import java.util.Arrays;
import java.util.Objects;

/**
 * NBT byte array tag.
 */
public record EchoNbtByteArray(byte[] value) implements EchoNbtTag {

    public EchoNbtByteArray {
        value = value.clone();
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.BYTE_ARRAY;
    }

    public byte[] copyValue() {
        return value.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoNbtByteArray other)) {
            return false;
        }
        return Arrays.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
