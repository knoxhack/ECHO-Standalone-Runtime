package dev.echo.standalone.runtime.save.anvil;

import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.nbt.EchoNbtIo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads and writes Minecraft {@code level.dat} files.
 */
public final class EchoLevelDatCodec {

    private EchoLevelDatCodec() {
    }

    public static EchoNbtCompound read(Path levelDat) throws IOException {
        try (InputStream in = Files.newInputStream(levelDat)) {
            return EchoNbtIo.readCompressed(in);
        }
    }

    public static void write(Path levelDat, EchoNbtCompound data) throws IOException {
        EchoNbtCompound root = new EchoNbtCompound()
                .put("Data", data);
        Files.createDirectories(levelDat.getParent());
        try (OutputStream out = Files.newOutputStream(levelDat)) {
            EchoNbtIo.writeCompressed(out, root);
        }
    }

    public static EchoNbtCompound readData(Path levelDat) throws IOException {
        EchoNbtCompound root = read(levelDat);
        return root.getCompound("Data");
    }

    public static EchoNbtCompound createMinimal(int dataVersion, String levelName, long seed,
                                                 int spawnX, int spawnY, int spawnZ) {
        return new EchoNbtCompound()
                .put("DataVersion", dataVersion)
                .put("LevelName", levelName)
                .put("RandomSeed", seed)
                .put("SpawnX", spawnX)
                .put("SpawnY", spawnY)
                .put("SpawnZ", spawnZ)
                .put("GameType", 0)
                .put("generatorName", "default")
                .put("generatorVersion", 1)
                .put("DayTime", 0L)
                .put("Time", 0L)
                .put("raining", (byte) 0)
                .put("thundering", (byte) 0)
                .put("hardcore", (byte) 0)
                .put("initialized", (byte) 1)
                .put("allowCommands", (byte) 0)
                .put("MapFeatures", (byte) 1);
    }
}
