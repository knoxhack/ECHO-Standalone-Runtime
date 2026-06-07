package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public final class EchoSaveModSetCompatibilityChecker {
    public static final String ALGORITHM = "sha256:echo.save.mod_set.v1";
    public static final String MODULE_IDS_METADATA_KEY = "saveEnvironmentModuleIds";
    private static final String FALLBACK_MODULE_IDS_METADATA_KEY = "moduleIds";

    public EchoSaveModSetCompatibilityReport check(
            EchoSaveManifest manifest,
            List<String> currentModuleIds,
            EchoSaveRecoveryJournal journal
    ) throws IOException {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(journal, "journal");
        List<String> expected = normalizedModules(manifest.metadata()
                .getOrDefault(MODULE_IDS_METADATA_KEY,
                        manifest.metadata().getOrDefault(FALLBACK_MODULE_IDS_METADATA_KEY, "")));
        List<String> current = normalizedModules(currentModuleIds);
        List<String> missing = difference(expected, current);
        List<String> added = difference(current, expected);
        boolean compatible = !expected.isEmpty() && missing.isEmpty() && added.isEmpty();
        boolean loadBlocked = !compatible;
        boolean backupAvailable = !manifest.backupIds().isEmpty();
        String recoveryAction = compatible
                ? "continue"
                : backupAvailable ? "restore_backup_or_open_migration_prompt" : "open_migration_prompt";
        String message = compatible
                ? "Saved module set matches the current runtime module set."
                : blockedMessage(missing, added, backupAvailable);
        EchoSaveModSetCompatibilityReport report = new EchoSaveModSetCompatibilityReport(
                manifest.profileId(),
                manifest.slotId(),
                ALGORITHM,
                fingerprint(expected),
                fingerprint(current),
                expected,
                current,
                missing,
                added,
                compatible,
                loadBlocked,
                backupAvailable,
                loadBlocked,
                loadBlocked,
                loadBlocked,
                recoveryAction,
                message
        );
        journal.append(
                EchoSaveJournalEvent.MOD_SET_CHECKED,
                manifest.slotId(),
                "compatible=" + compatible
                        + " missing=" + String.join(",", missing)
                        + " added=" + String.join(",", added)
        );
        return report;
    }

    private static String blockedMessage(List<String> missing, List<String> added, boolean backupAvailable) {
        ArrayList<String> details = new ArrayList<>();
        if (!missing.isEmpty()) {
            details.add("missing mod(s) " + String.join(",", missing));
        }
        if (!added.isEmpty()) {
            details.add("added mod(s) " + String.join(",", added));
        }
        String suffix = backupAvailable
                ? "; restore a backup or approve a migration plan before loading."
                : "; approve a migration plan before loading.";
        return "Saved module set changed: " + String.join("; ", details) + suffix;
    }

    private static List<String> normalizedModules(String joinedModuleIds) {
        if (joinedModuleIds == null || joinedModuleIds.isBlank()) {
            return List.of();
        }
        ArrayList<String> modules = new ArrayList<>();
        for (String token : joinedModuleIds.split(",")) {
            String normalized = token.trim();
            if (!normalized.isBlank()) {
                modules.add(normalized);
            }
        }
        return normalizedModules(modules);
    }

    private static List<String> normalizedModules(List<String> moduleIds) {
        if (moduleIds == null || moduleIds.isEmpty()) {
            return List.of();
        }
        TreeSet<String> sorted = new TreeSet<>();
        for (String moduleId : moduleIds) {
            if (moduleId != null && !moduleId.isBlank()) {
                sorted.add(moduleId.trim());
            }
        }
        return List.copyOf(sorted);
    }

    private static List<String> difference(List<String> left, List<String> right) {
        TreeSet<String> result = new TreeSet<>(left);
        result.removeAll(new TreeSet<>(right));
        return List.copyOf(result);
    }

    private static String fingerprint(List<String> moduleIds) {
        StringBuilder builder = new StringBuilder(ALGORITHM).append('\n');
        for (String moduleId : moduleIds) {
            builder.append(moduleId).append('\n');
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(builder.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                output.append(Character.forDigit((b >>> 4) & 0x0F, 16));
                output.append(Character.forDigit(b & 0x0F, 16));
            }
            return output.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }
}
