package dev.echo.standalone.runtime.nbt;

/**
 * NBT short tag.
 */
public record EchoNbtShort(short value) implements EchoNbtTag {

    public EchoNbtShort(int value) {
        this((short) value);
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.SHORT;
    }
}
