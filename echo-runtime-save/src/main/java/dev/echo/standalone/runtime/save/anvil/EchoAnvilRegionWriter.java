package dev.echo.standalone.runtime.save.anvil;

import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.nbt.EchoNbtIo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Writes Anvil region files ({@code .mca}) from raw chunk NBT compounds.
 */
public final class EchoAnvilRegionWriter implements AutoCloseable {

    public static final int SECTOR_SIZE = 4096;
    public static final int CHUNKS_PER_REGION = 32;
    public static final int HEADER_SIZE = 2 * SECTOR_SIZE;

    private final Path regionFile;
    private final int[] locations = new int[CHUNKS_PER_REGION * CHUNKS_PER_REGION];
    private final int[] timestamps = new int[CHUNKS_PER_REGION * CHUNKS_PER_REGION];
    private final List<Sector> sectors = new ArrayList<>();

    public EchoAnvilRegionWriter(Path regionFile) {
        this.regionFile = regionFile;
        Arrays.fill(locations, 0);
        Arrays.fill(timestamps, 0);
        // Reserve header sectors 0 and 1.
        sectors.add(new Sector(0, new byte[SECTOR_SIZE], true));
        sectors.add(new Sector(1, new byte[SECTOR_SIZE], true));
    }

    public void writeChunk(int chunkX, int chunkZ, EchoNbtCompound chunkRoot, int timestamp) throws IOException {
        if (chunkX < 0 || chunkX >= CHUNKS_PER_REGION || chunkZ < 0 || chunkZ >= CHUNKS_PER_REGION) {
            throw new IndexOutOfBoundsException(
                    "Chunk coordinates [" + chunkX + ", " + chunkZ + "] outside region [0, 31]"
            );
        }

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        try (DeflaterOutputStream deflater = new DeflaterOutputStream(compressed, new Deflater(Deflater.DEFAULT_COMPRESSION));
             DataOutputStream data = new DataOutputStream(deflater)) {
            EchoNbtIo.writeUncompressed(data, chunkRoot);
        }

        byte[] chunkBytes = compressed.toByteArray();
        int dataLength = chunkBytes.length + 1; // +1 for compression type byte
        int totalLength = 4 + dataLength; // 4 bytes length + compression type + data
        int sectorCount = (totalLength + SECTOR_SIZE - 1) / SECTOR_SIZE;
        if (sectorCount > 255) {
            throw new IOException("Chunk data too large: " + totalLength + " bytes");
        }

        int sectorOffset = allocateSectors(sectorCount);
        byte[] sectorData = new byte[sectorCount * SECTOR_SIZE];
        sectorData[0] = (byte) ((dataLength >>> 24) & 0xFF);
        sectorData[1] = (byte) ((dataLength >>> 16) & 0xFF);
        sectorData[2] = (byte) ((dataLength >>> 8) & 0xFF);
        sectorData[3] = (byte) (dataLength & 0xFF);
        sectorData[4] = 2; // zlib compression
        System.arraycopy(chunkBytes, 0, sectorData, 5, chunkBytes.length);

        for (int i = 0; i < sectorCount; i++) {
            byte[] sector = new byte[SECTOR_SIZE];
            System.arraycopy(sectorData, i * SECTOR_SIZE, sector, 0, SECTOR_SIZE);
            sectors.add(new Sector(sectorOffset + i, sector, false));
        }

        int entryIndex = (chunkZ * CHUNKS_PER_REGION) + chunkX;
        locations[entryIndex] = (sectorOffset << 8) | (sectorCount & 0xFF);
        timestamps[entryIndex] = timestamp;
    }

    public void close() throws IOException {
        Files.createDirectories(regionFile.getParent());
        try (RandomAccessFile file = new RandomAccessFile(regionFile.toFile(), "rw")) {
            file.setLength(0);

            // Write location table
            byte[] locationTable = new byte[SECTOR_SIZE];
            for (int i = 0; i < locations.length; i++) {
                int value = locations[i];
                locationTable[i * 4] = (byte) ((value >>> 24) & 0xFF);
                locationTable[i * 4 + 1] = (byte) ((value >>> 16) & 0xFF);
                locationTable[i * 4 + 2] = (byte) ((value >>> 8) & 0xFF);
                locationTable[i * 4 + 3] = (byte) (value & 0xFF);
            }
            file.write(locationTable);

            // Write timestamp table
            byte[] timestampTable = new byte[SECTOR_SIZE];
            for (int i = 0; i < timestamps.length; i++) {
                int value = timestamps[i];
                timestampTable[i * 4] = (byte) ((value >>> 24) & 0xFF);
                timestampTable[i * 4 + 1] = (byte) ((value >>> 16) & 0xFF);
                timestampTable[i * 4 + 2] = (byte) ((value >>> 8) & 0xFF);
                timestampTable[i * 4 + 3] = (byte) (value & 0xFF);
            }
            file.write(timestampTable);

            // Sort sectors by offset and write.
            sectors.sort(java.util.Comparator.comparingInt(s -> s.offset));
            int expectedOffset = 2;
            for (Sector sector : sectors) {
                if (sector.offset < expectedOffset) {
                    // Header or duplicate; skip after header.
                    if (sector.offset == 0 || sector.offset == 1) {
                        continue;
                    }
                    throw new IOException("Sector offset collision at " + sector.offset);
                }
                while (expectedOffset < sector.offset) {
                    // Fill gap with zeros.
                    file.write(new byte[SECTOR_SIZE]);
                    expectedOffset++;
                }
                file.write(sector.data);
                expectedOffset++;
            }
        }
    }

    private int allocateSectors(int count) {
        if (sectors.isEmpty()) {
            return 2;
        }
        int maxOffset = 2;
        for (Sector sector : sectors) {
            maxOffset = Math.max(maxOffset, sector.offset + 1);
        }
        return maxOffset;
    }

    private record Sector(int offset, byte[] data, boolean reserved) {
    }
}
