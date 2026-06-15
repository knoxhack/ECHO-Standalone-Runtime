package dev.echo.standalone.runtime.nbt;

/**
 * NBT string tag.
 */
public record EchoNbtString(String value) implements EchoNbtTag {

    public EchoNbtString {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.STRING;
    }
}
