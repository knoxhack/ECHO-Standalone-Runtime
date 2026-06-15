package dev.echo.standalone.runtime.save;

/**
 * Result of comparing a saved registry fingerprint with the current runtime registry fingerprint.
 */
public record EchoSaveRegistryCompatibilityReport(
        boolean compatible,
        boolean loadBlocked,
        String savedAlgorithm,
        String savedFingerprint,
        String currentFingerprint,
        String message
) {
}
