package dev.echo.standalone.runtime.nbt;

/**
 * NBT int tag.
 */
public record EchoNbtInt(int value) implements EchoNbtTag {

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.INT;
    }
}
