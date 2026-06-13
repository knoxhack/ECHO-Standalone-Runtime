package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class EchoClientSupportBundleService {
    private static final String FIXED_TIME = "1970-01-01T00:00:00Z";
    private static final String BUNDLE_ID = "echo:standalone-client-support-bundle";
    private final Path saveRoot;

    EchoClientSupportBundleService(Path saveRoot) {
        this.saveRoot = (saveRoot == null ? Path.of("saves").resolve("client") : saveRoot)
                .toAbsolutePath()
                .normalize();
    }

    EchoClientSupportBundleResult export(
            EchoClientScreenSnapshot screen,
            EchoClientRuntimeDiagnosticsSnapshot diagnostics,
            EchoClientSettings settings,
            List<EchoClientSaveSlotSummary> saveSlots,
            EchoClientModScanSummary modScan,
            EchoClientRuntimeContentSummary runtimeContent,
            List<EchoClientResourcePackSummary> resourcePacks,
            EchoClientScreenCatalog screenCatalog,
            List<EchoClientWorkbenchRecipeSummary> workbenchRecipes
    ) {
        try {
            Path supportRoot = saveRoot.resolve("support");
            Files.createDirectories(supportRoot);
            ArrayList<BundleFile> files = new ArrayList<>();
            files.add(new BundleFile("support-summary.json", supportSummaryJson()));
            files.add(new BundleFile("screen-snapshot.json", screenJson(screen)));
            files.add(new BundleFile("runtime-diagnostics.json", diagnosticsJson(diagnostics)));
            files.add(new BundleFile("client-settings.json", settingsJson(settings)));
            files.add(new BundleFile("save-slots.json", saveSlotsJson(saveSlots)));
            files.add(new BundleFile("module-content-summary.json", moduleContentJson(modScan, runtimeContent, screenCatalog)));
            files.add(new BundleFile("resource-packs.json", resourcePacksJson(resourcePacks)));
            files.add(new BundleFile("workbench-recipes.json", workbenchRecipesJson(workbenchRecipes)));

            Path manifestPath = supportRoot.resolve("EchoClientSupportBundle.manifest");
            Path archivePath = supportRoot.resolve("EchoClientSupportBundle.zip");
            writeManifest(manifestPath, files);
            writeFiles(supportRoot, files);
            writeArchive(supportRoot, archivePath, manifestPath, files);
            long archiveBytes = Files.isRegularFile(archivePath) ? Files.size(archivePath) : 0L;
            return new EchoClientSupportBundleResult(
                    archiveBytes > 0L,
                    archivePath.toString(),
                    manifestPath.toString(),
                    files.size() + 1,
                    archiveBytes,
                    archiveBytes > 0L
                            ? "Support bundle exported"
                            : "Support bundle archive was empty"
            );
        } catch (IOException | IllegalArgumentException exception) {
            return new EchoClientSupportBundleResult(
                    false,
                    "",
                    "",
                    0,
                    0L,
                    "Support bundle export failed: " + clean(exception.getMessage())
            );
        }
    }

    private static String supportSummaryJson() {
        return """
                {
                  "schema": "echo.standalone.client_support_bundle.v1",
                  "status": "PASS",
                  "bundleId": "%s",
                  "generatedAt": "%s",
                  "source": "OpenGL client UI",
                  "replacesPublicReleaseEvidence": false
                }
                """.formatted(BUNDLE_ID, FIXED_TIME);
    }

    private static String screenJson(EchoClientScreenSnapshot screen) {
        EchoClientScreenSnapshot safe = screen == null
                ? new EchoClientScreenSnapshot(
                        EchoClientGameState.BOOT,
                        EchoClientScreenKind.MAIN_MENU,
                        "",
                        "",
                        List.of(),
                        -1,
                        0,
                        false,
                        0.0D,
                        "",
                        EchoClientModalSnapshot.EMPTY,
                        EchoClientToastSnapshot.EMPTY,
                        "",
                        EchoClientSaveSlotThumbnailSnapshot.EMPTY
                )
                : screen;
        ArrayList<String> optionRows = new ArrayList<>();
        for (EchoClientScreenOption option : safe.options()) {
            optionRows.add("""
                    {
                      "label": "%s",
                      "command": "%s",
                      "enabled": %s,
                      "kind": "%s",
                      "tooltip": "%s",
                      "targetId": "%s"
                    }
                    """.formatted(
                    json(option.label()),
                    option.command().name(),
                    option.enabled(),
                    option.kind().name(),
                    json(option.tooltip()),
                    json(option.targetId())
            ));
        }
        return """
                {
                  "schema": "echo.standalone.client_screen_snapshot.v1",
                  "state": "%s",
                  "screen": "%s",
                  "title": "%s",
                  "subtitle": "%s",
                  "selectedIndex": %d,
                  "scrollOffset": %d,
                  "tooltip": "%s",
                  "footer": "%s",
                  "optionCount": %d,
                  "options": [%s]
                }
                """.formatted(
                safe.state().name(),
                safe.kind().name(),
                json(safe.title()),
                json(safe.subtitle()),
                safe.selectedIndex(),
                safe.scrollOffset(),
                json(safe.tooltip()),
                json(safe.footer()),
                safe.options().size(),
                String.join(",", optionRows)
        );
    }

    private static String diagnosticsJson(EchoClientRuntimeDiagnosticsSnapshot diagnostics) {
        EchoClientRuntimeDiagnosticsSnapshot safe = diagnostics == null
                ? EchoClientRuntimeDiagnosticsSnapshot.EMPTY
                : diagnostics;
        return """
                {
                  "schema": "echo.standalone.client_runtime_diagnostics.v1",
                  "activeWorld": %s,
                  "slotId": "%s",
                  "displayName": "%s",
                  "loadedChunks": %d,
                  "cachedChunks": %d,
                  "biomeId": "%s",
                  "hazardId": "%s",
                  "hazardExposurePercent": %d,
                  "currentHealth": %d,
                  "maxHealth": %d,
                  "livingEntities": %d,
                  "hostileEntities": %d,
                  "droppedItems": %d,
                  "droppedItemQuantity": %d,
                  "machineBlockEntities": %d,
                  "framePacing": {
                    "sampleCount": %d,
                    "line": "%s"
                  },
                  "audio": {
                    "initialized": %s,
                    "backendId": "%s",
                    "deviceOpen": %s,
                    "fallbackActive": %s,
                    "deviceLabel": "%s",
                    "eventCount": %d,
                    "diagnosticCount": %d,
                    "warningCount": %d,
                    "errorCount": %d,
                    "masterVolumePercent": %d,
                    "musicVolumePercent": %d,
                    "ambienceVolumePercent": %d,
                    "subtitlesEnabled": %s,
                    "activeSubtitleCount": %d,
                    "currentMusicClipId": "%s",
                    "currentAmbienceClipId": "%s",
                    "latestDiagnostic": "%s"
                  },
                  "lines": [%s]
                }
                """.formatted(
                safe.activeWorld(),
                json(safe.slotId()),
                json(safe.displayName()),
                safe.loadedChunks(),
                safe.cachedChunks(),
                json(safe.biomeId()),
                json(safe.hazardId()),
                safe.hazardExposurePercent(),
                safe.currentHealth(),
                safe.maxHealth(),
                safe.livingEntities(),
                safe.hostileEntities(),
                safe.droppedItems(),
                safe.droppedItemQuantity(),
                safe.machineBlockEntities(),
                safe.framePacing().sampleCount(),
                json(safe.framePacing().diagnosticsLine()),
                safe.audioDiagnostics().initialized(),
                json(safe.audioDiagnostics().backendId()),
                safe.audioDiagnostics().deviceOpen(),
                safe.audioDiagnostics().fallbackActive(),
                json(safe.audioDiagnostics().deviceLabel()),
                safe.audioDiagnostics().eventCount(),
                safe.audioDiagnostics().diagnosticCount(),
                safe.audioDiagnostics().warningCount(),
                safe.audioDiagnostics().errorCount(),
                safe.audioDiagnostics().masterVolumePercent(),
                safe.audioDiagnostics().musicVolumePercent(),
                safe.audioDiagnostics().ambienceVolumePercent(),
                safe.audioDiagnostics().subtitlesEnabled(),
                safe.audioDiagnostics().activeSubtitleCount(),
                json(safe.audioDiagnostics().currentMusicClipId()),
                json(safe.audioDiagnostics().currentAmbienceClipId()),
                json(safe.audioDiagnostics().latestDiagnostic()),
                stringArray(safe.lines())
        );
    }

    private static String settingsJson(EchoClientSettings settings) {
        EchoClientSettings safe = settings == null ? EchoClientSettings.defaults() : settings;
        return """
                {
                  "schema": "echo.standalone.client_settings.v1",
                  "mouseSensitivityPercent": %d,
                  "rawMouseInput": %s,
                  "fovDegrees": %d,
                  "uiScalePercent": %d,
                  "fullscreen": %s,
                  "vSync": %s,
                  "chunkViewDistance": %d,
                  "masterVolumePercent": %d,
                  "musicVolumePercent": %d,
                  "ambienceVolumePercent": %d,
                  "languageCode": "%s",
                  "subtitles": %s,
                  "highContrastUi": %s,
                  "reducedMotion": %s,
                  "hotbar": "%s"
                }
                """.formatted(
                safe.mouseSensitivityPercent(),
                safe.rawMouseInput(),
                safe.fovDegrees(),
                safe.uiScalePercent(),
                safe.fullscreen(),
                safe.vSync(),
                safe.chunkViewDistance(),
                safe.masterVolumePercent(),
                safe.musicVolumePercent(),
                safe.ambienceVolumePercent(),
                json(safe.languageCode()),
                safe.subtitles(),
                safe.highContrastUi(),
                safe.reducedMotion(),
                json(safe.keyBindings().hotbarSummary())
        );
    }

    private static String saveSlotsJson(List<EchoClientSaveSlotSummary> saveSlots) {
        List<EchoClientSaveSlotSummary> safe = saveSlots == null ? List.of() : saveSlots;
        ArrayList<String> rows = new ArrayList<>();
        for (EchoClientSaveSlotSummary slot : safe) {
            rows.add("""
                    {
                      "slotId": "%s",
                      "displayName": "%s",
                      "packId": "%s",
                      "loadable": %s,
                      "recoveryRequired": %s,
                      "thumbnailCaptured": %s,
                      "thumbnailSource": "%s",
                      "thumbnailPath": "%s",
                      "thumbnailWidth": %d,
                      "thumbnailHeight": %d
                    }
                    """.formatted(
                    json(slot.slotId()),
                    json(slot.displayName()),
                    json(slot.packId()),
                    slot.loadableInMemory(),
                    slot.recoveryRequired(),
                    slot.thumbnailCaptured(),
                    json(slot.thumbnailSource()),
                    json(slot.thumbnailPath()),
                    slot.thumbnailWidth(),
                    slot.thumbnailHeight()
            ));
        }
        return """
                {
                  "schema": "echo.standalone.client_save_slots.v1",
                  "slotCount": %d,
                  "slots": [%s]
                }
                """.formatted(safe.size(), String.join(",", rows));
    }

    private static String moduleContentJson(
            EchoClientModScanSummary modScan,
            EchoClientRuntimeContentSummary runtimeContent,
            EchoClientScreenCatalog screenCatalog
    ) {
        EchoClientModScanSummary safeModScan = modScan == null ? EchoClientModScanSummary.empty() : modScan;
        EchoClientRuntimeContentSummary safeContent = runtimeContent == null
                ? EchoClientRuntimeContentSummary.empty()
                : runtimeContent;
        EchoClientScreenCatalog safeCatalog = screenCatalog == null ? EchoClientScreenCatalog.empty() : screenCatalog;
        return """
                {
                  "schema": "echo.standalone.client_module_content_summary.v1",
                  "moduleSummary": "%s",
                  "descriptorCount": %d,
                  "adapterCoreDeclarations": %d,
                  "nativeEntrypoints": %d,
                  "graphIssues": %d,
                  "runtimeContent": "%s",
                  "runtimeRows": %d,
                  "screenCoreRoutes": %d,
                  "adapterCoreScreens": %d,
                  "topDomains": [%s]
                }
                """.formatted(
                json(safeModScan.summaryLabel()),
                safeModScan.descriptorCount(),
                safeModScan.adapterCoreDeclaredCount(),
                safeModScan.nativeEntrypointCount(),
                safeModScan.graphIssueCount(),
                json(safeContent.summaryLabel()),
                safeContent.rowCount(),
                safeCatalog.screenCount(),
                safeCatalog.adapterCoreScreenCount(),
                stringArray(safeContent.topDomainSummaries(8))
        );
    }

    private static String resourcePacksJson(List<EchoClientResourcePackSummary> resourcePacks) {
        List<EchoClientResourcePackSummary> safe = resourcePacks == null ? List.of() : resourcePacks;
        ArrayList<String> rows = new ArrayList<>();
        for (EchoClientResourcePackSummary pack : safe) {
            rows.add("""
                    {
                      "id": "%s",
                      "root": "%s",
                      "textures": %d,
                      "models": %d,
                      "blockstates": %d,
                      "langFiles": %d,
                      "soundEvents": %d
                    }
                    """.formatted(
                    json(pack.id()),
                    json(String.valueOf(pack.root())),
                    pack.textureCount(),
                    pack.modelCount(),
                    pack.blockstateCount(),
                    pack.langCount(),
                    pack.soundEventCount()
            ));
        }
        return """
                {
                  "schema": "echo.standalone.client_resource_packs.v1",
                  "packCount": %d,
                  "packs": [%s]
                }
                """.formatted(safe.size(), String.join(",", rows));
    }

    private static String workbenchRecipesJson(List<EchoClientWorkbenchRecipeSummary> workbenchRecipes) {
        List<EchoClientWorkbenchRecipeSummary> safe = workbenchRecipes == null ? List.of() : workbenchRecipes;
        ArrayList<String> rows = new ArrayList<>();
        for (EchoClientWorkbenchRecipeSummary recipe : safe) {
            rows.add("""
                    {
                      "recipeId": "%s",
                      "label": "%s",
                      "craftable": %s
                    }
                    """.formatted(
                    json(recipe.recipeId()),
                    json(recipe.label()),
                    recipe.craftable()
            ));
        }
        return """
                {
                  "schema": "echo.standalone.client_workbench_recipes.v1",
                  "recipeCount": %d,
                  "recipes": [%s]
                }
                """.formatted(safe.size(), String.join(",", rows));
    }

    private static void writeManifest(Path manifestPath, List<BundleFile> files) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("bundleId=" + BUNDLE_ID);
        lines.add("generatedAt=" + FIXED_TIME);
        lines.add("source=OpenGL client UI");
        for (BundleFile file : files) {
            lines.add("entry=" + file.relativePath());
        }
        Files.write(manifestPath, lines, StandardCharsets.UTF_8);
    }

    private static void writeFiles(Path supportRoot, List<BundleFile> files) throws IOException {
        for (BundleFile file : files) {
            Files.writeString(supportRoot.resolve(file.relativePath()), file.content(), StandardCharsets.UTF_8);
        }
    }

    private static void writeArchive(
            Path supportRoot,
            Path archivePath,
            Path manifestPath,
            List<BundleFile> files
    ) throws IOException {
        if (Files.exists(archivePath)) {
            Files.delete(archivePath);
        }
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archivePath))) {
            zipEntry(zip, "EchoClientSupportBundle.manifest", Files.readString(manifestPath, StandardCharsets.UTF_8));
            for (BundleFile file : files) {
                zipEntry(zip, file.relativePath(), Files.readString(supportRoot.resolve(file.relativePath()), StandardCharsets.UTF_8));
            }
        }
    }

    private static void zipEntry(ZipOutputStream zip, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(0L);
        zip.putNextEntry(entry);
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String stringArray(List<String> values) {
        List<String> safe = values == null ? List.of() : values;
        ArrayList<String> quoted = new ArrayList<>();
        for (String value : safe) {
            quoted.add("\"" + json(value) + "\"");
        }
        return String.join(",", quoted);
    }

    private static String json(String value) {
        String safe = value == null ? "" : value;
        return safe
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replace('\r', ' ').replace('\n', ' ');
    }

    private record BundleFile(String relativePath, String content) {
        private BundleFile {
            if (relativePath == null || relativePath.isBlank() || relativePath.contains("..")) {
                throw new IllegalArgumentException("relativePath must be a simple bundle path");
            }
            content = content == null ? "" : content;
        }
    }
}
