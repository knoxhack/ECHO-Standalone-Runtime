package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRegistryCompatibilityChecker;
import dev.echo.standalone.runtime.save.EchoSaveRegistryCompatibilityReport;
import dev.echo.standalone.runtime.save.EchoSaveRegistryFingerprint;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Phase C smoke harness: proves save registry fingerprint storage and incompatible-mod-set rejection.
 */
public final class EchoSaveFingerprintSmokeHarness {

    private EchoSaveFingerprintSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        List<String> contentSetA = List.of("echoopenlandsprotocol:meadows", "echoopenlandsprotocol:woodlands");
        List<String> contentSetB = List.of("echoopenlandsprotocol:meadows", "echoopenlandsprotocol:marshlands");

        Path fixtureRoot = Files.createTempDirectory("echo-save-fingerprint-smoke");
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "fingerprint-smoke",
                "Fingerprint Smoke",
                "echoopenlandsprotocol",
                1,
                fixtureRoot.resolve("profiles/fingerprint-smoke"),
                Map.of()
        );

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoSaveRuntimeResult save = new EchoSaveRuntime().open(services, profile);

        String fingerprintA = EchoSaveRegistryFingerprint.compute(contentSetA);
        EchoSaveTransaction tx = save.beginTransaction("slot-a", "tx-001");
        tx.writeText("world/summary.json", "{}");
        EchoSaveCommitResult commit = tx.commit(Map.of(
                EchoSaveRegistryCompatibilityChecker.FINGERPRINT_KEY, fingerprintA,
                EchoSaveRegistryCompatibilityChecker.ALGORITHM_KEY, EchoSaveRegistryFingerprint.ALGORITHM
        ));

        EchoSaveRegistryCompatibilityReport compatible = save.checkRegistryFingerprint("slot-a", contentSetA);
        EchoSaveRegistryCompatibilityReport incompatible = save.checkRegistryFingerprint("slot-a", contentSetB);

        int checked = 0;
        int failures = 0;
        StringBuilder failureDetails = new StringBuilder();

        checked++;
        if (!compatible.compatible()) {
            failures++;
            failureDetails.append("same content set should be compatible; ");
        }
        checked++;
        if (!incompatible.loadBlocked()) {
            failures++;
            failureDetails.append("different content set should block load; ");
        }
        checked++;
        if (!fingerprintA.equals(compatible.savedFingerprint())) {
            failures++;
            failureDetails.append("saved fingerprint mismatch; ");
        }

        boolean pass = failures == 0;
        writeReport(reportRoot, pass, fingerprintA, compatible.savedFingerprint(),
                incompatible.currentFingerprint(), checked, failures,
                failureDetails.length() > 0 ? failureDetails.toString() : "");

        deleteRecursive(fixtureRoot);

        if (!pass) {
            throw new AssertionError("Save fingerprint smoke failed: " + failureDetails);
        }

        System.out.println("save fingerprint smoke PASS fingerprint=" + fingerprintA
                + " checked=" + checked);
    }

    private static void writeReport(Path root, boolean pass, String fingerprintA, String savedFingerprint,
                                    String mismatchedFingerprint, int checked, int failures,
                                    String failureDetails) throws IOException {
        Path path = root.resolve("save-fingerprint.json");
        String status = pass ? "PASS" : "FAIL";
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schema\": \"echo.standalone.save_fingerprint.v1\",\n");
        sb.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        sb.append("  \"status\": \"").append(status).append("\",\n");
        sb.append("  \"algorithm\": \"").append(EchoSaveRegistryFingerprint.ALGORITHM).append("\",\n");
        sb.append("  \"savedFingerprint\": \"").append(savedFingerprint).append("\",\n");
        sb.append("  \"compatibleFingerprint\": \"").append(fingerprintA).append("\",\n");
        sb.append("  \"mismatchedFingerprint\": \"").append(mismatchedFingerprint).append("\",\n");
        sb.append("  \"checks\": ").append(checked).append(",\n");
        sb.append("  \"failures\": ").append(failures).append(",\n");
        sb.append("  \"failureDetails\": \"").append(escape(failureDetails)).append("\"\n");
        sb.append("}\n");
        Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void deleteRecursive(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        if (Files.isDirectory(path)) {
            try (var stream = Files.list(path)) {
                for (Path child : stream.toList()) {
                    deleteRecursive(child);
                }
            }
        }
        Files.deleteIfExists(path);
    }
}
