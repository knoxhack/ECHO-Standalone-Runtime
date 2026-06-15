package dev.echo.standalone.runtime.save.anvil;

import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.nbt.EchoNbtIo;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Loads individual chunks from an Anvil region file ({@code .mca}).
 */
public final class EchoAnvilRegionLoader {

    public static final int SECTOR_SIZE = 4096;
    public static final int CHUNKS_PER_REGION = 32;
    public static final int HEADER_SIZE = 2 * SECTOR_SIZE;

    private final Path regionFile;
    private final byte[] header;

    public EchoAnvilRegionLoader(Path regionFile) throws IOException {
        this.regionFile = regionFile;
        this.header = new byte[HEADER_SIZE];
        try (InputStream in = Files.newInputStream(regionFile)) {
            int read = in.read(header);
            if (read != HEADER_SIZE) {
                throw new IOException("Region file header incomplete: " + read + " bytes");
            }
        }
    }

    public boolean hasChunk(int chunkX, int chunkZ) {
        int entry = locationEntry(chunkX, chunkZ);
        int offset = ((header[entry] & 0xFF) << 16)
                | ((header[entry + 1] & 0xFF) << 8)
                | (header[entry + 2] & 0xFF);
        return offset != 0;
    }

    public Optional<EchoAnvilChunkData> loadChunk(int chunkX, int chunkZ) throws IOException {
        if (!hasChunk(chunkX, chunkZ)) {
            return Optional.empty();
        }

        int entry = locationEntry(chunkX, chunkZ);
        int offset = ((header[entry] & 0xFF) << 16)
                | ((header[entry + 1] & 0xFF) << 8)
                | (header[entry + 2] & 0xFF);
        int sectors = header[entry + 3] & 0xFF;

        long byteOffset = (long) offset * SECTOR_SIZE;
        int byteLength = sectors * SECTOR_SIZE;

        byte[] chunkBytes = new byte[byteLength];
        try (InputStream in = Files.newInputStream(regionFile)) {
            in.skipNBytes(byteOffset);
            int read = in.read(chunkBytes);
            if (read != byteLength) {
                throw new IOException("Incomplete chunk data at offset " + byteOffset);
            }
        }

        try (DataInputStream data = new DataInputStream(new ByteArrayInputStream(chunkBytes))) {
            int length = data.readInt();
            if (length < 1 || length > byteLength - 4) {
                throw new IOException("Invalid chunk data length: " + length);
            }
            byte compressionType = data.readByte();
            byte[] compressed = new byte[length - 1];
            data.readFully(compressed);

            EchoNbtCompound root = switch (compressionType) {
                case 1 -> EchoNbtIo.readCompressed(new ByteArrayInputStream(compressed));
                case 2 -> EchoNbtIo.readUncompressed(new InflaterInputStream(new ByteArrayInputStream(compressed)));
                default -> throw new IOException("Unknown chunk compression type: " + compressionType);
            };

            EchoAnvilChunkDecoder decoder = new EchoAnvilChunkDecoder();
            return Optional.of(decoder.decode(root));
        }
    }

    private static int locationEntry(int chunkX, int chunkZ) {
        if (chunkX < 0 || chunkX >= CHUNKS_PER_REGION || chunkZ < 0 || chunkZ >= CHUNKS_PER_REGION) {
            throw new IndexOutOfBoundsException(
                    "Chunk coordinates [" + chunkX + ", " + chunkZ + "] outside region [0, 31]"
            );
        }
        return 4 * ((chunkZ & 31) * CHUNKS_PER_REGION + (chunkX & 31));
    }
}
