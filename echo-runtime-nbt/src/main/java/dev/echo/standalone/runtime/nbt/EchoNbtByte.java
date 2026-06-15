package dev.echo.standalone.runtime.nbt;

/**
 * NBT byte tag.
 */
public record EchoNbtByte(byte value) implements EchoNbtTag {

    public EchoNbtByte {
        // byte is signed [-128,127]
    }

    public EchoNbtByte(int value) {
        this((byte) value);
    }

    public EchoNbtByte(boolean value) {
        this(value ? (byte) 1 : (byte) 0);
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.BYTE;
    }

    public boolean asBoolean() {
        return value != 0;
    }
}
