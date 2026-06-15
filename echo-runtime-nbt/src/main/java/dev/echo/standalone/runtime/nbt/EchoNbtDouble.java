package dev.echo.standalone.runtime.nbt;

/**
 * NBT double tag.
 */
public record EchoNbtDouble(double value) implements EchoNbtTag {

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.DOUBLE;
    }
}
