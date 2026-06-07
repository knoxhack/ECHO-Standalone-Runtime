package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveBackup;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionIssue;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionSeverity;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveMigrationPlan;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class EchoClientSaveSlotService {
    private final EchoSaveRuntimeResult saves;
    private final EchoClientWorldTemplate template;
    private final Path slotsRoot;
    private String lastError = "";

    private EchoClientSaveSlotService(EchoSaveRuntimeResult saves, EchoClientWorldTemplate template) {
        this.saves = saves;
        this.template = template == null ? EchoClientWorldTemplates.defaultTemplate() : template;
        slotsRoot = saves.profile().root().resolve("slots");
    }

    static EchoClientSaveSlotService openDefault() {
        return open(Path.of("saves").resolve("client"));
    }

    static EchoClientSaveSlotService open(Path root) {
        return open(root, EchoClientWorldTemplates.defaultTemplate());
    }

    static EchoClientSaveSlotService open(Path root, EchoClientWorldTemplate template) {
        Path saveRoot = root == null ? Path.of("saves").resolve("client") : root;
        EchoClientWorldTemplate safeTemplate = template == null ? EchoClientWorldTemplates.defaultTemplate() : template;
        try {
            return new EchoClientSaveSlotService(
                    new EchoSaveRuntime().open(
                            new EchoDefaultRuntimeServiceRegistry(),
                            safeTemplate.saveProfile(saveRoot)
                    ),
                    safeTemplate
            );
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open client save runtime at " + saveRoot.toAbsolutePath(), e);
        }
    }

    boolean hasLoadableSlot() {
        return defaultContinueSlotId().isPresent();
    }

    boolean hasLoadableSlot(List<Map<String, Object>> currentRuntimeRows) {
        return hasLoadableSlot(currentRuntimeRows, Map.of());
    }

    boolean hasLoadableSlot(
            List<Map<String, Object>> currentRuntimeRows,
            Map<String, String> currentEnvironmentMetadata
    ) {
        return defaultContinueSlotId(currentRuntimeRows, currentEnvironmentMetadata).isPresent();
    }

    Optional<String> defaultContinueSlotId() {
        return defaultContinueSlotId(List.of());
    }

    Optional<String> defaultContinueSlotId(List<Map<String, Object>> currentRuntimeRows) {
        return defaultContinueSlotId(currentRuntimeRows, Map.of());
    }

    Optional<String> defaultContinueSlotId(
            List<Map<String, Object>> currentRuntimeRows,
            Map<String, String> currentEnvironmentMetadata
    ) {
        return listSlots("", currentRuntimeRows, currentEnvironmentMetadata).stream()
                .filter(EchoClientSaveSlotSummary::loadableInMemory)
                .max(Comparator
                        .comparing(EchoClientSaveSlotSummary::updatedAt)
                        .thenComparing(EchoClientSaveSlotSummary::slotId))
                .map(EchoClientSaveSlotSummary::slotId);
    }

    List<EchoClientSaveSlotSummary> listSlots(String memorySnapshotSlotId) {
        return listSlots(memorySnapshotSlotId, List.of());
    }

    List<EchoClientSaveSlotSummary> listSlots(
            String memorySnapshotSlotId,
            List<Map<String, Object>> currentRuntimeRows
    ) {
        return listSlots(memorySnapshotSlotId, currentRuntimeRows, Map.of());
    }

    List<EchoClientSaveSlotSummary> listSlots(
            String memorySnapshotSlotId,
            List<Map<String, Object>> currentRuntimeRows,
            Map<String, String> currentEnvironmentMetadata
    ) {
        ArrayList<EchoClientSaveSlotSummary> result = new ArrayList<>();
        lastError = "";
        if (!Files.isDirectory(slotsRoot)) {
            return List.of();
        }
        try (var stream = Files.list(slotsRoot)) {
            for (Path slotRoot : stream.filter(Files::isDirectory).sorted().toList()) {
                String slotId = slotRoot.getFileName().toString();
                Path manifestPath = slotRoot.resolve("manifest.json");
                if (!Files.isRegularFile(manifestPath)) {
                    result.add(new EchoClientSaveSlotSummary(
                            slotId,
                            slotId,
                            saves.profile().packId(),
                            "missing manifest",
                            false,
                            true,
                            "Recovery required | manifest missing | Slot directory has no manifest",
                            "",
                            "",
                            "deterministic",
                            false,
                            0,
                            0,
                            0,
                            0,
                            0,
                            0
                    ));
                    continue;
                }
                EchoSaveManifest manifest;
                try {
                    manifest = saves.readManifest(slotId);
                } catch (IOException | IllegalArgumentException exception) {
                    result.add(recoverySummary(slotId, "manifest unreadable", exception));
                    continue;
                }
                SaveHealth health = saveHealth(manifest.slotId());
                String displayName = manifest.metadata().getOrDefault("displayName", manifest.slotId());
                boolean canRestore = health.healthy() && EchoClientGameplaySaveCodec.canRestore(manifest);
                EchoClientRuntimeContentCompatibility compatibility =
                        runtimeContentCompatibility(manifest, currentRuntimeRows);
                EchoClientSaveEnvironmentCompatibility environmentCompatibility =
                        saveEnvironmentCompatibility(manifest, currentEnvironmentMetadata);
                boolean canUseMemorySnapshot = memorySnapshotSlotId != null
                        && !memorySnapshotSlotId.isBlank()
                        && manifest.slotId().equals(memorySnapshotSlotId);
                String thumbnailPath = manifest.metadata().getOrDefault("clientThumbnailPath", "");
                String thumbnailSource = manifest.metadata().getOrDefault("clientThumbnailSource", "");
                ThumbnailValidation thumbnail = validateThumbnail(manifest, thumbnailPath);
                String detail = "Pack " + manifest.packId() + " | files " + manifest.files().size()
                        + " | updated " + manifest.updatedAt()
                        + " | health " + health.detail()
                        + " | runtime " + compatibility.detail()
                        + " | " + environmentCompatibility.detail()
                        + " | thumbnail " + (thumbnail.captured() ? thumbnailSource : "deterministic");
                result.add(new EchoClientSaveSlotSummary(
                        manifest.slotId(),
                        displayName,
                        manifest.packId(),
                        manifest.updatedAt(),
                        health.healthy()
                                && ((canRestore && compatibility.compatible() && environmentCompatibility.compatible())
                                        || canUseMemorySnapshot),
                        !health.healthy(),
                        detail,
                        thumbnailPath,
                        thumbnail.resolvedPath(),
                        thumbnailSource,
                        thumbnail.captured(),
                        thumbnail.width(),
                        thumbnail.height(),
                        parseArgb(manifest.metadata().get("clientThumbnailSkyArgb")),
                        parseArgb(manifest.metadata().get("clientThumbnailTerrainArgb")),
                        parseArgb(manifest.metadata().get("clientThumbnailAccentArgb")),
                        parseArgb(manifest.metadata().get("clientThumbnailShadowArgb"))
                ));
            }
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
        }
        return List.copyOf(result);
    }

    private SaveHealth saveHealth(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return SaveHealth.recovery("INVALID_SLOT", "No save slot id");
        }
        try {
            return SaveHealth.from(saves.check(slotId));
        } catch (IOException | IllegalArgumentException exception) {
            String message = exception.getMessage() == null || exception.getMessage().isBlank()
                    ? "Save corruption check failed"
                    : exception.getMessage().trim();
            return SaveHealth.recovery("CORRUPTION_CHECK_FAILED", message);
        }
    }

    private EchoClientSaveSlotSummary recoverySummary(
            String slotId,
            String reason,
            Exception exception
    ) {
        String safeReason = reason == null || reason.isBlank() ? "manifest unavailable" : reason.trim();
        String message = exception == null || exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Save manifest could not be read"
                : exception.getMessage().trim().replace('\n', ' ').replace('\r', ' ');
        return new EchoClientSaveSlotSummary(
                slotId,
                slotId,
                saves.profile().packId(),
                "recovery required",
                false,
                true,
                "Recovery required | " + safeReason + " | " + message,
                "",
                "",
                "deterministic",
                false,
                0,
                0,
                0,
                0,
                0,
                0
        );
    }

    void recordSessionSummary(EchoClientWorldSession worldSession, String reason) {
        recordSessionSummary(worldSession, reason, List.of());
    }

    void recordSessionSummary(
            EchoClientWorldSession worldSession,
            String reason,
            List<Map<String, Object>> runtimeContentRows
    ) {
        recordSessionSummary(worldSession, reason, runtimeContentRows, Map.of());
    }

    void recordSessionSummary(
            EchoClientWorldSession worldSession,
            String reason,
            List<Map<String, Object>> runtimeContentRows,
            Map<String, String> environmentMetadata
    ) {
        if (worldSession == null) {
            return;
        }
        String transactionId = "tx-client-" + sanitize(reason);
        try {
            EchoClientGameplaySaveCodec.writeSession(
                    saves,
                    worldSession,
                    transactionId,
                    reason,
                    runtimeContentRows,
                    environmentMetadata
            );
            lastError = "";
        } catch (IOException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] save slot write failed: " + e.getMessage());
        }
    }

    EchoClientRuntimeContentCompatibility runtimeContentCompatibility(
            String slotId,
            List<Map<String, Object>> currentRuntimeRows
    ) {
        if (slotId == null || slotId.isBlank()) {
            lastError = "No save slot selected";
            return EchoClientRuntimeContentCompatibility.incompatible(lastError, List.of());
        }
        try {
            EchoSaveManifest manifest = saves.readManifest(slotId);
            EchoClientRuntimeContentCompatibility compatibility =
                    runtimeContentCompatibility(manifest, currentRuntimeRows);
            lastError = compatibility.compatible() ? "" : compatibility.detail();
            return compatibility;
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] runtime content compatibility check failed: " + e.getMessage());
            return EchoClientRuntimeContentCompatibility.incompatible(lastError, List.of());
        }
    }

    EchoClientSaveEnvironmentCompatibility saveEnvironmentCompatibility(
            String slotId,
            Map<String, String> currentEnvironmentMetadata
    ) {
        if (slotId == null || slotId.isBlank()) {
            lastError = "No save slot selected";
            return EchoClientSaveEnvironmentCompatibility.incompatible(lastError);
        }
        try {
            EchoSaveManifest manifest = saves.readManifest(slotId);
            EchoClientSaveEnvironmentCompatibility compatibility =
                    saveEnvironmentCompatibility(manifest, currentEnvironmentMetadata);
            lastError = compatibility.compatible() ? "" : compatibility.detail();
            return compatibility;
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] save environment compatibility check failed: " + e.getMessage());
            return EchoClientSaveEnvironmentCompatibility.incompatible(lastError);
        }
    }

    EchoClientWorldSession restoreSlot(String slotId) {
        return restoreSlot(slotId, template.contentBridge());
    }

    EchoClientWorldSession restoreSlot(String slotId, EchoAdapterCoreStandaloneContentBridge contentBridge) {
        try {
            EchoSaveManifest manifest = saves.readManifest(slotId);
            EchoClientSavedSessionSnapshot snapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                    contentBridge == null ? template.contentBridge() : contentBridge,
                    saves,
                    manifest
            );
            String displayName = manifest.metadata().getOrDefault("displayName", manifest.slotId());
            lastError = "";
            return EchoClientWorldSessionFactory.forTemplate(template)
                    .restoreSavedSession(manifest.slotId(), displayName, snapshot, List.of());
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] save slot restore failed: " + e.getMessage());
            return null;
        }
    }

    String backupAndPlanMigration(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            lastError = "No save slot selected";
            return "";
        }
        try {
            EchoSaveManifest manifest = saves.readManifest(slotId);
            String transactionId = "manual-backup-" + sanitize(slotId) + "-" + (manifest.backupIds().size() + 1);
            EchoSaveBackup backup = saves.backupService()
                    .createBackupIfPresent(saves.profile().slot(slotId), transactionId, saves.journal())
                    .orElseThrow(() -> new IOException("Save slot has no manifest to back up: " + slotId));
            EchoSaveMigrationPlan migration = saves.planMigration(slotId, saves.profile().formatVersion());
            recordBackupOnManifest(manifest, backup.backupId(), migration);
            lastError = "";
            return backup.backupId() + " | migration steps " + migration.steps().size();
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] save slot backup failed: " + e.getMessage());
            return "";
        }
    }

    boolean deleteSlot(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            lastError = "No save slot selected";
            return false;
        }
        try {
            Path slotRoot = saves.profile().slot(slotId).root();
            if (!Files.exists(slotRoot)) {
                lastError = "Save slot does not exist: " + slotId;
                return false;
            }
            deleteRecursively(slotRoot);
            lastError = "";
            return true;
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] save slot delete failed: " + e.getMessage());
            return false;
        }
    }

    boolean renameSlot(String slotId, String displayName) {
        String normalizedName = normalizeDisplayName(displayName);
        if (slotId == null || slotId.isBlank()) {
            lastError = "No save slot selected";
            return false;
        }
        if (normalizedName.isBlank()) {
            lastError = "Save name must not be blank";
            return false;
        }
        try {
            EchoSaveManifest manifest = saves.readManifest(slotId);
            java.util.TreeMap<String, String> metadata = new java.util.TreeMap<>(manifest.metadata());
            metadata.put("displayName", normalizedName);
            EchoSaveManifest updated = new EchoSaveManifest(
                    manifest.schema(),
                    manifest.profileId(),
                    manifest.slotId(),
                    manifest.packId(),
                    manifest.formatVersion(),
                    manifest.createdAt(),
                    manifest.updatedAt(),
                    manifest.files(),
                    manifest.backupIds(),
                    metadata
            );
            saves.manifestCodec().write(saves.profile().slot(manifest.slotId()).manifestPath(), updated);
            lastError = "";
            return true;
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            System.out.println("[echo-client] save slot rename failed: " + e.getMessage());
            return false;
        }
    }

    String lastError() {
        return lastError;
    }

    private EchoClientRuntimeContentCompatibility runtimeContentCompatibility(
            EchoSaveManifest manifest,
            List<Map<String, Object>> currentRuntimeRows
    ) throws IOException {
        List<Map<String, Object>> savedRows =
                EchoClientGameplaySaveCodec.restoreRuntimeContentRegistrations(saves, manifest);
        String savedFingerprint = EchoClientRuntimeContentFingerprint.fingerprint(savedRows);
        String manifestFingerprint = manifest.metadata()
                .getOrDefault(EchoClientRuntimeContentFingerprint.FINGERPRINT_METADATA_KEY, "");
        boolean hasRuntimeSidecar = manifest.file(EchoClientGameplaySaveCodec.RUNTIME_CONTENT_PATH).isPresent();
        boolean needsFingerprint = hasRuntimeSidecar || !savedRows.isEmpty() || !safeRuntimeRows(currentRuntimeRows).isEmpty();
        if (needsFingerprint && manifestFingerprint.isBlank()) {
            return EchoClientRuntimeContentCompatibility.incompatible(
                    "missing runtime content fingerprint",
                    savedRows
            );
        }
        if (!manifestFingerprint.isBlank() && !manifestFingerprint.equals(savedFingerprint)) {
            return EchoClientRuntimeContentCompatibility.incompatible(
                    "runtime content sidecar fingerprint mismatch saved="
                            + EchoClientRuntimeContentFingerprint.shortFingerprint(manifestFingerprint)
                            + " sidecar="
                            + EchoClientRuntimeContentFingerprint.shortFingerprint(savedFingerprint),
                    savedRows
            );
        }
        List<Map<String, Object>> currentRows = safeRuntimeRows(currentRuntimeRows);
        if (!currentRows.isEmpty()) {
            String currentFingerprint = EchoClientRuntimeContentFingerprint.fingerprint(currentRows);
            if (!currentFingerprint.equals(savedFingerprint)) {
                return EchoClientRuntimeContentCompatibility.incompatible(
                        "runtime content mismatch saved="
                                + EchoClientRuntimeContentFingerprint.shortFingerprint(savedFingerprint)
                                + " current="
                                + EchoClientRuntimeContentFingerprint.shortFingerprint(currentFingerprint),
                        savedRows
                );
            }
        }
        return EchoClientRuntimeContentCompatibility.compatible(
                "rows " + savedRows.size()
                        + " fp " + EchoClientRuntimeContentFingerprint.shortFingerprint(savedFingerprint),
                savedRows
        );
    }

    private EchoClientSaveEnvironmentCompatibility saveEnvironmentCompatibility(
            EchoSaveManifest manifest,
            Map<String, String> currentEnvironmentMetadata
    ) {
        Map<String, String> currentMetadata = safeStringMap(currentEnvironmentMetadata);
        String savedFingerprint = manifest.metadata()
                .getOrDefault(EchoClientSaveEnvironmentFingerprint.FINGERPRINT_METADATA_KEY, "");
        if (savedFingerprint.isBlank()) {
            return EchoClientSaveEnvironmentCompatibility.compatible("save environment unverified");
        }
        String currentFingerprint = currentMetadata
                .getOrDefault(EchoClientSaveEnvironmentFingerprint.FINGERPRINT_METADATA_KEY, "");
        if (currentFingerprint.isBlank()) {
            return EchoClientSaveEnvironmentCompatibility.incompatible("save environment unavailable");
        }
        if (!savedFingerprint.equals(currentFingerprint)) {
            String missingModules = missingIds(
                    manifest.metadata().get(EchoClientSaveEnvironmentFingerprint.MODULE_IDS_METADATA_KEY),
                    currentMetadata.get(EchoClientSaveEnvironmentFingerprint.MODULE_IDS_METADATA_KEY)
            );
            if (!missingModules.isBlank()) {
                return EchoClientSaveEnvironmentCompatibility.incompatible(
                        "missing mod(s) " + missingModules
                );
            }
            String missingPacks = missingIds(
                    manifest.metadata().get(EchoClientSaveEnvironmentFingerprint.RESOURCE_PACK_IDS_METADATA_KEY),
                    currentMetadata.get(EchoClientSaveEnvironmentFingerprint.RESOURCE_PACK_IDS_METADATA_KEY)
            );
            if (!missingPacks.isBlank()) {
                return EchoClientSaveEnvironmentCompatibility.incompatible(
                        "missing resource pack(s) " + missingPacks
                );
            }
            return EchoClientSaveEnvironmentCompatibility.incompatible(
                    "save environment mismatch saved="
                            + EchoClientSaveEnvironmentFingerprint.shortFingerprint(savedFingerprint)
                            + " current="
                            + EchoClientSaveEnvironmentFingerprint.shortFingerprint(currentFingerprint)
            );
        }
        return EchoClientSaveEnvironmentCompatibility.compatible(
                "environment mods "
                        + manifest.metadata()
                                .getOrDefault(EchoClientSaveEnvironmentFingerprint.MODULE_COUNT_METADATA_KEY, "?")
                        + " packs "
                        + manifest.metadata()
                                .getOrDefault(EchoClientSaveEnvironmentFingerprint.RESOURCE_PACK_COUNT_METADATA_KEY, "?")
                        + " fp "
                        + EchoClientSaveEnvironmentFingerprint.shortFingerprint(savedFingerprint)
        );
    }

    private static List<Map<String, Object>> safeRuntimeRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (row != null && !row.isEmpty()) {
                result.add(Map.copyOf(row));
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, String> safeStringMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        java.util.TreeMap<String, String> result = new java.util.TreeMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    private static String missingIds(String savedIds, String currentIds) {
        java.util.TreeSet<String> current = idSet(currentIds);
        java.util.TreeSet<String> missing = idSet(savedIds);
        missing.removeAll(current);
        return String.join(",", missing);
    }

    private static java.util.TreeSet<String> idSet(String text) {
        java.util.TreeSet<String> result = new java.util.TreeSet<>();
        if (text == null || text.isBlank()) {
            return result;
        }
        for (String token : text.split(",")) {
            String normalized = token.trim();
            if (!normalized.isBlank()) {
                result.add(normalized);
            }
        }
        return result;
    }

    private static boolean manifestHasFile(EchoSaveManifest manifest, String relativePath) {
        if (manifest == null || relativePath == null || relativePath.isBlank()) {
            return false;
        }
        try {
            return manifest.file(relativePath).isPresent();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private ThumbnailValidation validateThumbnail(EchoSaveManifest manifest, String relativePath) {
        if (manifest == null || relativePath == null || relativePath.isBlank()) {
            return ThumbnailValidation.missing();
        }
        if (!manifest.metadata()
                .getOrDefault("clientThumbnailCodec", "")
                .equals(EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_CODEC)) {
            return ThumbnailValidation.missing();
        }
        if (!manifestHasFile(manifest, relativePath)) {
            return ThumbnailValidation.missing();
        }
        Path dataRoot = saves.profile().slot(manifest.slotId()).dataRoot().toAbsolutePath().normalize();
        Path thumbnailPath = dataRoot.resolve(relativePath).toAbsolutePath().normalize();
        if (!thumbnailPath.startsWith(dataRoot) || !Files.isRegularFile(thumbnailPath)) {
            return ThumbnailValidation.missing();
        }
        try {
            BufferedImage image = ImageIO.read(thumbnailPath.toFile());
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                return ThumbnailValidation.missing();
            }
            int expectedWidth = parseInt(manifest.metadata().get("clientThumbnailWidth"));
            int expectedHeight = parseInt(manifest.metadata().get("clientThumbnailHeight"));
            if (expectedWidth > 0 && expectedWidth != image.getWidth()) {
                return ThumbnailValidation.missing();
            }
            if (expectedHeight > 0 && expectedHeight != image.getHeight()) {
                return ThumbnailValidation.missing();
            }
            return new ThumbnailValidation(true, thumbnailPath.toString(), image.getWidth(), image.getHeight());
        } catch (IOException | IllegalArgumentException exception) {
            return ThumbnailValidation.missing();
        }
    }

    private static int parseArgb(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        String trimmed = value.trim();
        try {
            long parsed = trimmed.startsWith("0x") || trimmed.startsWith("0X")
                    ? Long.parseLong(trimmed.substring(2), 16)
                    : Long.parseLong(trimmed);
            return (int) parsed;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(value.trim()));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private record ThumbnailValidation(boolean captured, String resolvedPath, int width, int height) {
        ThumbnailValidation {
            resolvedPath = resolvedPath == null ? "" : resolvedPath;
            captured = captured && !resolvedPath.isBlank() && width > 0 && height > 0;
            width = Math.max(0, width);
            height = Math.max(0, height);
        }

        static ThumbnailValidation missing() {
            return new ThumbnailValidation(false, "", 0, 0);
        }
    }

    private record SaveHealth(boolean healthy, String detail) {
        SaveHealth {
            detail = detail == null || detail.isBlank() ? "not checked" : detail.trim();
        }

        static SaveHealth from(EchoSaveCorruptionReport report) {
            if (report == null) {
                return recovery("CORRUPTION_CHECK_MISSING", "Save corruption check did not return a report");
            }
            if (report.issues().isEmpty()) {
                return new SaveHealth(true, "OK checkedFiles=" + report.checkedFiles());
            }
            List<EchoSaveCorruptionIssue> blockingIssues = report.issues().stream()
                    .filter(SaveHealth::blocksRestore)
                    .toList();
            List<EchoSaveCorruptionIssue> displayIssues = blockingIssues.isEmpty()
                    ? report.issues()
                    : blockingIssues;
            String joined = displayIssues.stream()
                    .map(SaveHealth::issueLabel)
                    .limit(3)
                    .collect(java.util.stream.Collectors.joining(","));
            String suffix = displayIssues.size() > 3 ? ",+" + (displayIssues.size() - 3) : "";
            return new SaveHealth(blockingIssues.isEmpty(),
                    (blockingIssues.isEmpty() ? "WARN " : "RECOVERY ") + joined + suffix);
        }

        static SaveHealth recovery(String code, String message) {
            String safeCode = code == null || code.isBlank() ? "UNKNOWN" : code.trim();
            String safeMessage = message == null || message.isBlank()
                    ? "Save health check failed"
                    : message.trim().replace('\n', ' ').replace('\r', ' ');
            return new SaveHealth(false, "RECOVERY " + safeCode + " " + safeMessage);
        }

        private static String issueLabel(EchoSaveCorruptionIssue issue) {
            if (issue == null) {
                return "UNKNOWN";
            }
            String path = issue.path() == null || issue.path().isBlank() ? "" : "@" + issue.path();
            return issue.code() + path;
        }

        private static boolean blocksRestore(EchoSaveCorruptionIssue issue) {
            return issue != null
                    && issue.severity() == EchoSaveCorruptionSeverity.ERROR
                    && !EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_PATH.equals(issue.path());
        }
    }

    private void recordBackupOnManifest(
            EchoSaveManifest manifest,
            String backupId,
            EchoSaveMigrationPlan migration
    ) throws IOException {
        LinkedHashSet<String> backupIds = new LinkedHashSet<>(manifest.backupIds());
        backupIds.add(backupId);
        java.util.TreeMap<String, String> metadata = new java.util.TreeMap<>(manifest.metadata());
        metadata.put("lastManualBackup", backupId);
        metadata.put("lastMigrationCheck", "from=" + migration.fromVersion()
                + " to=" + migration.toVersion()
                + " steps=" + migration.steps().size()
                + " blocked=" + migration.blocked());
        EchoSaveManifest updated = new EchoSaveManifest(
                manifest.schema(),
                manifest.profileId(),
                manifest.slotId(),
                manifest.packId(),
                manifest.formatVersion(),
                manifest.createdAt(),
                manifest.updatedAt(),
                manifest.files(),
                List.copyOf(backupIds),
                metadata
        );
        saves.manifestCodec().write(saves.profile().slot(manifest.slotId()).manifestPath(), updated);
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return "save";
        }
        return value.toLowerCase(java.util.Locale.ROOT).replaceAll("[^a-z0-9._-]+", "-");
    }

    private static String normalizeDisplayName(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.length() <= 48 ? normalized : normalized.substring(0, 48).stripTrailing();
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }
}
