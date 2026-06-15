package dev.echo.standalone.runtime.nbt;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Reads and writes Minecraft NBT format, including gzipped payloads.
 */
public final class EchoNbtIo {

    private EchoNbtIo() {
    }

    public static EchoNbtCompound readCompressed(InputStream in) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(new BufferedInputStream(in));
             DataInputStream data = new DataInputStream(gzip)) {
            return readRoot(data);
        }
    }

    public static EchoNbtCompound readUncompressed(InputStream in) throws IOException {
        try (DataInputStream data = new DataInputStream(new BufferedInputStream(in))) {
            return readRoot(data);
        }
    }

    public static EchoNbtCompound read(byte[] bytes) throws IOException {
        return readUncompressed(new ByteArrayInputStream(bytes));
    }

    public static EchoNbtCompound readGzipped(byte[] bytes) throws IOException {
        return readCompressed(new ByteArrayInputStream(bytes));
    }

    public static void writeCompressed(OutputStream out, EchoNbtCompound compound) throws IOException {
        try (GZIPOutputStream gzip = new GZIPOutputStream(new BufferedOutputStream(out));
             DataOutputStream data = new DataOutputStream(gzip)) {
            writeRoot(data, compound);
        }
    }

    public static void writeUncompressed(OutputStream out, EchoNbtCompound compound) throws IOException {
        try (DataOutputStream data = new DataOutputStream(new BufferedOutputStream(out))) {
            writeRoot(data, compound);
        }
    }

    public static byte[] writeBytes(EchoNbtCompound compound) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeUncompressed(out, compound);
        return out.toByteArray();
    }

    public static byte[] writeGzippedBytes(EchoNbtCompound compound) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeCompressed(out, compound);
        return out.toByteArray();
    }

    private static EchoNbtCompound readRoot(DataInputStream in) throws IOException {
        byte typeId = in.readByte();
        if (typeId != EchoNbtTagType.COMPOUND.id()) {
            throw new IOException("Root NBT tag must be a compound, got " + typeId);
        }
        readString(in); // root name, usually empty
        return readCompoundPayload(in);
    }

    private static void writeRoot(DataOutputStream out, EchoNbtCompound compound) throws IOException {
        out.writeByte(EchoNbtTagType.COMPOUND.id());
        writeString(out, "");
        writeCompoundPayload(out, compound);
    }

    private static EchoNbtTag readTag(DataInputStream in, EchoNbtTagType type) throws IOException {
        return switch (type) {
            case END -> EchoNbtEnd.INSTANCE;
            case BYTE -> new EchoNbtByte(in.readByte());
            case SHORT -> new EchoNbtShort(in.readShort());
            case INT -> new EchoNbtInt(in.readInt());
            case LONG -> new EchoNbtLong(in.readLong());
            case FLOAT -> new EchoNbtFloat(in.readFloat());
            case DOUBLE -> new EchoNbtDouble(in.readDouble());
            case BYTE_ARRAY -> {
                int length = in.readInt();
                byte[] value = new byte[length];
                in.readFully(value);
                yield new EchoNbtByteArray(value);
            }
            case STRING -> new EchoNbtString(readString(in));
            case LIST -> readListPayload(in);
            case COMPOUND -> readCompoundPayload(in);
            case INT_ARRAY -> {
                int length = in.readInt();
                int[] value = new int[length];
                for (int i = 0; i < length; i++) {
                    value[i] = in.readInt();
                }
                yield new EchoNbtIntArray(value);
            }
            case LONG_ARRAY -> {
                int length = in.readInt();
                long[] value = new long[length];
                for (int i = 0; i < length; i++) {
                    value[i] = in.readLong();
                }
                yield new EchoNbtLongArray(value);
            }
        };
    }

    private static void writeTag(DataOutputStream out, EchoNbtTag tag) throws IOException {
        switch (tag) {
            case EchoNbtEnd ignored -> {
            }
            case EchoNbtByte t -> out.writeByte(t.value());
            case EchoNbtShort t -> out.writeShort(t.value());
            case EchoNbtInt t -> out.writeInt(t.value());
            case EchoNbtLong t -> out.writeLong(t.value());
            case EchoNbtFloat t -> out.writeFloat(t.value());
            case EchoNbtDouble t -> out.writeDouble(t.value());
            case EchoNbtByteArray t -> {
                byte[] value = t.value();
                out.writeInt(value.length);
                out.write(value);
            }
            case EchoNbtString t -> writeString(out, t.value());
            case EchoNbtList t -> writeListPayload(out, t);
            case EchoNbtCompound t -> writeCompoundPayload(out, t);
            case EchoNbtIntArray t -> {
                int[] value = t.value();
                out.writeInt(value.length);
                for (int v : value) {
                    out.writeInt(v);
                }
            }
            case EchoNbtLongArray t -> {
                long[] value = t.value();
                out.writeInt(value.length);
                for (long v : value) {
                    out.writeLong(v);
                }
            }
        }
    }

    private static EchoNbtList readListPayload(DataInputStream in) throws IOException {
        byte elementTypeId = in.readByte();
        EchoNbtTagType elementType = EchoNbtTagType.byId(elementTypeId);
        int length = in.readInt();
        if (length < 0) {
            throw new IOException("Negative list length: " + length);
        }
        List<EchoNbtTag> elements = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            elements.add(readTag(in, elementType));
        }
        return new EchoNbtList(elementType, elements);
    }

    private static void writeListPayload(DataOutputStream out, EchoNbtList list) throws IOException {
        out.writeByte(list.elementType().id());
        out.writeInt(list.size());
        for (EchoNbtTag element : list.elements()) {
            writeTag(out, element);
        }
    }

    private static EchoNbtCompound readCompoundPayload(DataInputStream in) throws IOException {
        Map<String, EchoNbtTag> tags = new LinkedHashMap<>();
        while (true) {
            byte typeId = in.readByte();
            EchoNbtTagType type = EchoNbtTagType.byId(typeId);
            if (type == EchoNbtTagType.END) {
                break;
            }
            String name = readString(in);
            EchoNbtTag tag = readTag(in, type);
            tags.put(name, tag);
        }
        return new EchoNbtCompound(tags);
    }

    private static void writeCompoundPayload(DataOutputStream out, EchoNbtCompound compound) throws IOException {
        for (Map.Entry<String, EchoNbtTag> entry : compound.tags().entrySet()) {
            out.writeByte(entry.getValue().type().id());
            writeString(out, entry.getKey());
            writeTag(out, entry.getValue());
        }
        out.writeByte(EchoNbtTagType.END.id());
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readUnsignedShort();
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > 65535) {
            throw new IOException("NBT string exceeds 65535 bytes: " + value.length());
        }
        out.writeShort(bytes.length);
        out.write(bytes);
    }
}
