package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Compares the registry fingerprint stored in a save manifest against the current runtime fingerprint.
 */
public final class EchoSaveRegistryCompatibilityChecker {

    public static final String FINGERPRINT_KEY = "registryFingerprint";
    public static final String ALGORITHM_KEY = "registryFingerprintAlgorithm";

    /**
     * Checks whether the current ordered registry IDs are compatible with the saved manifest.
     * Checks whether the current ordered registry IDs are compatible with the saved manifest.
     *
     * <p>If the manifest has no registry fingerprint, the save is treated as compatible (legacy tolerant)
     * but the report notes that the fingerprint is missing.
     */
    public EchoSaveRegistryCompatibilityReport check(EchoSaveManifest manifest, List<String> currentOrderedIds,
                                                      EchoSaveRecoveryJournal journal) throws IOException {
        String currentFingerprint = EchoSaveRegistryFingerprint.compute(currentOrderedIds);
        Map<String, String> metadata = manifest.metadata();
        String savedFingerprint = metadata.get(FINGERPRINT_KEY);
        String savedAlgorithm = metadata.get(ALGORITHM_KEY);

        if (savedFingerprint == null || savedFingerprint.isBlank()) {
            if (journal != null) {
                journal.append(EchoSaveJournalEvent.MOD_SET_CHECKED, manifest.slotId(), "registry_fingerprint_missing");
            }
            return new EchoSaveRegistryCompatibilityReport(true, false, savedAlgorithm, savedFingerprint,
                    currentFingerprint, "Save has no registry fingerprint; legacy tolerant load.");
        }

        boolean compatible = savedFingerprint.equals(currentFingerprint);
        if (journal != null) {
            journal.append(EchoSaveJournalEvent.MOD_SET_CHECKED, manifest.slotId(),
                    "registry_fingerprint_match=" + compatible);
        }
        return new EchoSaveRegistryCompatibilityReport(compatible, !compatible, savedAlgorithm, savedFingerprint,
                currentFingerprint, compatible ? "Registry fingerprint matches."
                        : "Registry fingerprint mismatch: save was written with a different content set.");
    }
}
