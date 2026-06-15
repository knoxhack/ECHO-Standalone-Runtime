package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.save.anvil.EchoLevelDatCodec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Smoke harness for Minecraft {@code level.dat} read/write (Phase A.5).
 *
 * <p>Creates a minimal level.dat, writes it to disk, reads it back, and asserts that seed,
 * spawn, and level name round-trip. Produces a deterministic report under
 * {@code reports/echo/standalone}.
 */
public final class EchoLevelDatRoundTripSmokeHarness {

    private EchoLevelDatRoundTripSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        Path tempDir = Files.createTempDirectory("echo-leveldat-roundtrip");
        Path levelDat = tempDir.resolve("level.dat");

        long seed = 12345L;
        EchoNbtCompound original = EchoLevelDatCodec.createMinimal(
                3465,
                "echo-roundtrip-test",
                seed,
                8, 70, 12
        );

        EchoLevelDatCodec.write(levelDat, original);
        EchoNbtCompound loadedRoot = EchoLevelDatCodec.read(levelDat);
        EchoNbtCompound loaded = EchoLevelDatCodec.readData(levelDat);

        boolean rootHasData = loadedRoot.contains("Data");
        boolean nameMatch = "echo-roundtrip-test".equals(loaded.getString("LevelName"));
        boolean seedMatch = loaded.getLong("RandomSeed") == seed;
        boolean spawnMatch = loaded.getInt("SpawnX") == 8
                && loaded.getInt("SpawnY") == 70
                && loaded.getInt("SpawnZ") == 12;
        boolean dataVersionMatch = loaded.getInt("DataVersion") == 3465;

        boolean pass = rootHasData && nameMatch && seedMatch && spawnMatch && dataVersionMatch;
        writeReport(reportRoot, pass, rootHasData, nameMatch, seedMatch, spawnMatch, dataVersionMatch);

        // Cleanup.
        Files.deleteIfExists(levelDat);
        Files.deleteIfExists(tempDir);

        if (!pass) {
            throw new AssertionError("level.dat round-trip failed: rootHasData=" + rootHasData
                    + " nameMatch=" + nameMatch + " seedMatch=" + seedMatch
                    + " spawnMatch=" + spawnMatch + " dataVersionMatch=" + dataVersionMatch);
        }

        System.out.println("level.dat round-trip smoke PASS seed=" + seed);
    }

    private static void writeReport(Path root, boolean pass, boolean rootHasData, boolean nameMatch,
                                    boolean seedMatch, boolean spawnMatch, boolean dataVersionMatch) throws IOException {
        Path path = root.resolve("level-dat-round-trip.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.level_dat_round_trip.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"rootHasData\": ").append(rootHasData).append(",\n");
        sb.append("  \"nameMatch\": ").append(nameMatch).append(",\n");
        sb.append("  \"seedMatch\": ").append(seedMatch).append(",\n");
        sb.append("  \"spawnMatch\": ").append(spawnMatch).append(",\n");
        sb.append("  \"dataVersionMatch\": ").append(dataVersionMatch).append("\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }
}
