package dev.echo.standalone.runtime.nbt;

/**
 * Sealed interface for all NBT tag types.
 */
public sealed interface EchoNbtTag
        permits EchoNbtEnd,
        EchoNbtByte,
        EchoNbtShort,
        EchoNbtInt,
        EchoNbtLong,
        EchoNbtFloat,
        EchoNbtDouble,
        EchoNbtByteArray,
        EchoNbtString,
        EchoNbtList,
        EchoNbtCompound,
        EchoNbtIntArray,
        EchoNbtLongArray {

    EchoNbtTagType type();
}
