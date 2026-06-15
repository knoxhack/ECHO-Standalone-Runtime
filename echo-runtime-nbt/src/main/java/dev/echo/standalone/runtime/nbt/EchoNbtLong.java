package dev.echo.standalone.runtime.nbt;

/**
 * NBT long tag.
 */
public record EchoNbtLong(long value) implements EchoNbtTag {

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.LONG;
    }
}
