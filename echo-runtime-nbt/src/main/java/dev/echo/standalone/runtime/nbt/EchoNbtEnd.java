package dev.echo.standalone.runtime.nbt;

/**
 * NBT end tag marker.
 */
public record EchoNbtEnd() implements EchoNbtTag {

    public static final EchoNbtEnd INSTANCE = new EchoNbtEnd();

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.END;
    }
}
