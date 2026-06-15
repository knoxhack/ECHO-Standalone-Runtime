package dev.echo.standalone.runtime.nbt;

/**
 * NBT float tag.
 */
public record EchoNbtFloat(float value) implements EchoNbtTag {

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.FLOAT;
    }
}
