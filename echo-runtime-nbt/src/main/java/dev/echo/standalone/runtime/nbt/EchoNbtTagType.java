package dev.echo.standalone.runtime.nbt;

/**
 * NBT tag type IDs matching the Minecraft NBT specification.
 */
public enum EchoNbtTagType {
    END(0),
    BYTE(1),
    SHORT(2),
    INT(3),
    LONG(4),
    FLOAT(5),
    DOUBLE(6),
    BYTE_ARRAY(7),
    STRING(8),
    LIST(9),
    COMPOUND(10),
    INT_ARRAY(11),
    LONG_ARRAY(12);

    private final int id;

    EchoNbtTagType(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static EchoNbtTagType byId(int id) {
        for (EchoNbtTagType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown NBT tag type id: " + id);
    }
}
