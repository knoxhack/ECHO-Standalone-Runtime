package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.assets.EchoAssetConflict;
import dev.echo.standalone.runtime.assets.EchoAssetHotReload;
import dev.echo.standalone.runtime.assets.EchoAssetIndex;
import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetResolver;
import dev.echo.standalone.runtime.assets.EchoAssetReloadReport;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.assets.EchoAnimatedTexture;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver;
import dev.echo.standalone.runtime.assets.EchoItemTextureResolver;
import dev.echo.standalone.runtime.assets.EchoLangRuntime;
import dev.echo.standalone.runtime.assets.EchoMaterialAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.assets.EchoMinecraftResourcePack;
import dev.echo.standalone.runtime.assets.EchoThemeAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoSoundsJsonLoader;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class EchoRuntimeAssetSmokeHarness {
    private EchoRuntimeAssetSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-assets-smoke");
        Path runtimeDefaults = fixtureRoot.resolve("runtime-defaults");
        Path ashfallBase = fixtureRoot.resolve("ashfall-base");
        Path ashfallOverride = fixtureRoot.resolve("ashfall-dev-override");
        Path archivePack = fixtureRoot.resolve("archive-pack.zip");
        Path jarPack = fixtureRoot.resolve("jar-pack.jar");

        write(runtimeDefaults.resolve("assets/echo/lang/en_us.json"), "{\"echo.boot\":\"Boot\"}");
        write(runtimeDefaults.resolve("assets/echo/materials/cyberglass.json"), "{\"alpha\":0.64}");
        write(ashfallBase.resolve("assets/ashfall/textures/gui/terminal.png"), "base-terminal");
        write(ashfallBase.resolve("assets/ashfall/blockstates/crash_panel.json"),
                "{\"variants\":{\"\":{\"model\":\"ashfall:block/crash_panel\"}}}");
        write(ashfallBase.resolve("assets/ashfall/models/block/crash_panel.json"),
                "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"ashfall:block/crash_panel\"}}");
        write(ashfallBase.resolve("assets/ashfall/textures/block/crash_panel.png"), "block-texture");
        write(ashfallBase.resolve("assets/ashfall/textures/block/crash_panel.png.mcmeta"), """
                {
                  "animation": {
                    "frametime": 3,
                    "interpolate": true,
                    "frames": [0, 1, { "index": 2, "time": 7 }]
                  }
                }
                """);
        write(ashfallBase.resolve("assets/ashfall/models/item/scrap_metal.json"),
                "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"ashfall:item/scrap_metal\"}}");
        write(ashfallBase.resolve("assets/ashfall/textures/item/scrap_metal.png"), "item-texture");
        write(ashfallBase.resolve("assets/ashfall/sounds.json"), """
                {
                  "ui.echo_message": {
                    "subtitle": "subtitles.ashfall.ui.echo_message",
                    "replace": true,
                    "sounds": [
                      "ui/echo_message",
                      { "name": "ashfall:ambient/ash_wind", "type": "sound", "stream": true }
                    ]
                  }
                }
                """);
        write(ashfallBase.resolve("assets/ashfall/materials/ash_steel.json"), "{\"roughness\":0.8}");
        write(ashfallBase.resolve("data/ashfall/themes/cyberglass.json"), "{\"accent\":\"cyan\"}");
        write(ashfallBase.resolve("data/ashfall/world_regions/crash_site.json"), "{\"danger\":1}");
        write(ashfallOverride.resolve("assets/ashfall/textures/gui/terminal.png"), "override-terminal");
        write(ashfallOverride.resolve("data/ashfall/world_hazards/toxic_ash.json"), "{\"toxicity\":2}");
        writeArchivePack(archivePack);
        writeJarPack(jarPack);

        EchoAssetRuntime runtime = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "asset", runtimeDefaults, "runtime-defaults"),
                new EchoAssetMount(1, "asset", ashfallBase, "pack-base"),
                new EchoAssetMount(2, "asset", ashfallOverride, "dev-override"),
                new EchoAssetMount(3, "asset", archivePack, "archive-pack"),
                new EchoAssetMount(4, "asset", jarPack, "jar-pack")
        ));
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult result = runtime.load(services, List.of(
                "ashfall:textures/gui/terminal.png",
                "ashfall:world_regions/crash_site.json",
                "ashfall:missing/not_found.json"
        ));

        EchoAssetIndex index = result.index();
        require(index.namespaces().contains("ashfall"), "ashfall namespace should be indexed");
        require(index.namespaces().contains("echo"), "echo namespace should be indexed");
        require(index.namespaces().contains("ziptest"), "archive pack namespace should be indexed");
        require(index.namespaces().contains("jartest"), "jar resource pack namespace should be indexed");
        require(index.entries().size() == 27, "directory, zip archive, and jar archive assets/data definitions should be indexed");
        require(result.resolver().loadText("ashfall:textures/gui/terminal.png")
                        .orElseThrow()
                        .equals("override-terminal"),
                "highest-order mount should override base texture");
        require(result.resolver().loadText("ziptest:lang/en_us.json")
                        .orElseThrow()
                        .contains("ziptest.badge"),
                "zip resource pack lang JSON should load from embedded archive bytes");
        require(result.resolver().loadText("ashfall:world_regions/crash_site.json").orElseThrow().contains("danger"),
                "data definition should load");
        require(result.missingReport().missingLogicalIds().equals(List.of("ashfall:missing/not_found.json")),
                "missing report should include required missing asset");
        require(result.conflicts().stream()
                        .map(EchoAssetConflict::logicalId)
                        .toList()
                        .contains("ashfall:textures/gui/terminal.png"),
                "conflict detector should report overridden texture");

        EchoAssetResolver resolver = services.require(EchoAssetResolver.class);
        require(new EchoLangRuntime(resolver).loadLanguage("echo", "en_us").orElseThrow().contains("echo.boot"),
                "language runtime should load lang JSON");
        require(new EchoThemeAssetRuntime(resolver).loadTheme("ashfall", "cyberglass").orElseThrow().contains("accent"),
                "theme runtime should load theme JSON");
        require(new EchoMaterialAssetRuntime(resolver).loadMaterial("ashfall", "ash_steel").orElseThrow().contains("roughness"),
                "material runtime should load material JSON");
        EchoMinecraftAssetResolver minecraft = new EchoMinecraftAssetResolver(resolver);
        require(new EchoBlockTextureResolver(minecraft)
                        .resolve("ashfall:crash_panel")
                        .textureId()
                        .orElseThrow()
                        .equals("ashfall:block/crash_panel"),
                "block texture resolver should follow blockstate -> block model -> cube_all texture");
        require(new EchoBlockTextureResolver(minecraft)
                        .resolve("ziptest:archive_block")
                        .textureId()
                        .orElseThrow()
                        .equals("ziptest:block/archive_block"),
                "block texture resolver should read blockstate/model JSON from a zip resource pack");
        EchoAnimatedTexture animatedTexture = EchoAnimatedTexture.load(minecraft, "ashfall", "block/crash_panel");
        require(animatedTexture.animated(), "animated texture metadata should mark texture animated");
        require(animatedTexture.frameTimeTicks() == 3, "animated texture should parse default frametime");
        require(animatedTexture.interpolate(), "animated texture should parse interpolate flag");
        require(animatedTexture.frames().size() == 3, "animated texture should parse frame list");
        require(animatedTexture.frames().get(2).index() == 2 && animatedTexture.frames().get(2).timeTicks() == 7,
                "animated texture should parse object frame time override");
        require(new EchoItemTextureResolver(minecraft)
                        .resolve("ashfall:scrap_metal")
                        .textureId()
                        .orElseThrow()
                        .equals("ashfall:item/scrap_metal"),
                "item texture resolver should follow item/generated layer0 texture");
        require(new EchoItemTextureResolver(minecraft)
                        .resolve("ziptest:archive_badge")
                        .textureId()
                        .orElseThrow()
                        .equals("ziptest:item/archive_badge"),
                "item texture resolver should read item model JSON from a zip resource pack");
        require(new EchoBlockTextureResolver(minecraft)
                        .resolve("jartest:jar_block")
                        .textureId()
                        .orElseThrow()
                        .equals("jartest:block/jar_block"),
                "block texture resolver should read blockstate/model JSON from a jar resource pack");
        require(new EchoItemTextureResolver(minecraft)
                        .resolve("jartest:jar_badge")
                        .textureId()
                        .orElseThrow()
                        .equals("jartest:item/jar_badge"),
                "item texture resolver should read item model JSON from a jar resource pack");
        EchoSoundsJsonLoader.EchoSoundsDefinition sounds = new EchoSoundsJsonLoader(minecraft).load("ashfall");
        EchoSoundsJsonLoader.EchoSoundEventDefinition echoMessage =
                sounds.findEvent("ashfall:ui.echo_message").orElseThrow();
        require(echoMessage.subtitle().equals("subtitles.ashfall.ui.echo_message"),
                "sounds.json loader should parse subtitles");
        require(echoMessage.replace(), "sounds.json loader should parse replace flag");
        require(echoMessage.sounds().size() == 2, "sounds.json loader should parse string and object variants");
        require(echoMessage.sounds().getFirst().name().equals("ashfall:ui/echo_message"),
                "unqualified sound names should resolve under the sounds namespace");
        require(echoMessage.sounds().get(1).stream(), "object sound variants should parse stream flag");
        EchoMinecraftResourcePack scannedArchive = EchoMinecraftResourcePack.scan(archivePack);
        require(scannedArchive.hasPackMetadata(), "zip resource pack scanner should read pack.mcmeta");
        require(scannedArchive.namespaces().contains("ziptest"), "zip resource pack scanner should find namespaces");
        require(scannedArchive.textureCount() == 2, "zip resource pack scanner should count textures");
        require(scannedArchive.modelCount() == 2, "zip resource pack scanner should count models");
        require(scannedArchive.blockstateCount() == 1, "zip resource pack scanner should count blockstates");
        require(scannedArchive.langCount() == 1, "zip resource pack scanner should count lang files");
        EchoMinecraftResourcePack scannedJar = EchoMinecraftResourcePack.scan(jarPack);
        require(scannedJar.hasPackMetadata(), "jar resource pack scanner should read pack.mcmeta");
        require(scannedJar.namespaces().contains("jartest"), "jar resource pack scanner should find namespaces");
        require(scannedJar.textureCount() == 2, "jar resource pack scanner should count textures");
        require(scannedJar.modelCount() == 2, "jar resource pack scanner should count models");
        require(scannedJar.blockstateCount() == 1, "jar resource pack scanner should count blockstates");
        require(scannedJar.langCount() == 1, "jar resource pack scanner should count lang files");

        write(ashfallOverride.resolve("assets/ashfall/icons/hazard.png"), "hazard-icon");
        EchoAssetReloadReport reload = new EchoAssetHotReload(runtime).reload(index);
        require(reload.added().equals(List.of("ashfall:icons/hazard.png")), "hot reload should report added hazard icon");
        require(reload.nextIndex().entries().size() == 28, "hot reload next index should include new asset");

        writeReports(result, scannedArchive, scannedJar, reload);

        System.out.println("phase14.5 asset runtime smoke PASS assets="
                + index.entries().size()
                + " conflicts="
                + result.conflicts().size()
                + " missing="
                + result.missingReport().missingLogicalIds().size()
                + " added="
                + reload.added().size());
    }

    private static void writeReports(
            EchoAssetRuntimeResult result,
            EchoMinecraftResourcePack scannedArchive,
            EchoMinecraftResourcePack scannedJar,
            EchoAssetReloadReport reload
    ) throws IOException {
        Path reportDir = Path.of("reports/echo/standalone");
        Files.createDirectories(reportDir);
        EchoAssetIndex index = result.index();
        List<String> mountKinds = result.mounts().stream()
                .map(mount -> mount.kind().toLowerCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
        List<String> mountSources = result.mounts().stream()
                .map(EchoAssetMount::source)
                .toList();
        List<String> indexedLogicalIds = index.entries().stream()
                .map(entry -> entry.logicalId())
                .distinct()
                .sorted()
                .toList();
        List<String> conflictIds = result.conflicts().stream()
                .map(EchoAssetConflict::logicalId)
                .sorted()
                .toList();

        write(reportDir.resolve("asset-index.json"), """
                {
                  "schema": "echo.standalone.asset_index.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAssetSmokeHarness",
                  "status": "PASS",
                  "entryCount": %d,
                  "namespaceCount": %d,
                  "namespaces": %s,
                  "logicalIds": %s,
                  "archiveBytesIndexed": %s,
                  "jarBytesIndexed": %s,
                  "directoryAssetsIndexed": %s,
                  "dataDefinitionsIndexed": %s
                }
                """.formatted(
                index.entries().size(),
                index.namespaces().size(),
                jsonArray(index.namespaces()),
                jsonArray(indexedLogicalIds),
                bool(indexedLogicalIds.contains("ziptest:textures/block/archive_block.png")),
                bool(indexedLogicalIds.contains("jartest:textures/block/jar_block.png")),
                bool(indexedLogicalIds.contains("ashfall:textures/gui/terminal.png")),
                bool(indexedLogicalIds.contains("ashfall:world_regions/crash_site.json"))
        ));

        write(reportDir.resolve("asset-mounts.json"), """
                {
                  "schema": "echo.standalone.asset_mounts.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAssetSmokeHarness",
                  "status": "PASS",
                  "mountCount": %d,
                  "mountSources": %s,
                  "mountKinds": %s,
                  "folderMounts": %d,
                  "archiveMounts": %d,
                  "jarMounts": %d,
                  "overrideWins": %s,
                  "serviceResolverBound": %s
                }
                """.formatted(
                result.mounts().size(),
                jsonArray(mountSources),
                jsonArray(mountKinds),
                result.mounts().stream().filter(mount -> Files.isDirectory(mount.root())).count(),
                result.mounts().stream().filter(mount -> Files.isRegularFile(mount.root())).count(),
                result.mounts().stream().filter(mount -> mount.root().getFileName().toString().endsWith(".jar")).count(),
                bool(result.resolver().resolve("ashfall:textures/gui/terminal.png")
                        .map(entry -> "dev-override".equals(entry.mount().source()))
                        .orElse(false)),
                bool(true)
        ));

        write(reportDir.resolve("asset-missing.json"), """
                {
                  "schema": "echo.standalone.asset_missing.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAssetSmokeHarness",
                  "status": "PASS",
                  "missingCount": %d,
                  "missingLogicalIds": %s,
                  "requiredMissingDetected": %s
                }
                """.formatted(
                result.missingReport().missingLogicalIds().size(),
                jsonArray(result.missingReport().missingLogicalIds()),
                bool(result.missingReport().missingLogicalIds().contains("ashfall:missing/not_found.json"))
        ));

        write(reportDir.resolve("asset-conflicts.json"), """
                {
                  "schema": "echo.standalone.asset_conflicts.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAssetSmokeHarness",
                  "status": "PASS",
                  "conflictCount": %d,
                  "conflictLogicalIds": %s,
                  "overrideConflictDetected": %s
                }
                """.formatted(
                result.conflicts().size(),
                jsonArray(conflictIds),
                bool(conflictIds.contains("ashfall:textures/gui/terminal.png"))
        ));

        write(reportDir.resolve("resource-pack-plan.json"), """
                {
                  "schema": "echo.standalone.resource_pack_plan.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeAssetSmokeHarness",
                  "status": "PASS",
                  "supportsFolderMounts": %s,
                  "supportsZipMounts": %s,
                  "supportsJarMounts": %s,
                  "packMetadataRead": %s,
                  "jarPackMetadataRead": %s,
                  "archiveNamespaceCount": %d,
                  "archiveNamespaces": %s,
                  "jarArchiveNamespaces": %s,
                  "archiveTextureCount": %d,
                  "archiveModelCount": %d,
                  "archiveBlockstateCount": %d,
                  "archiveLangCount": %d,
                  "jarArchiveTextureCount": %d,
                  "jarArchiveModelCount": %d,
                  "jarArchiveBlockstateCount": %d,
                  "jarArchiveLangCount": %d,
                  "minecraftStyleResolvers": %s,
                  "hotReloadAdded": %s
                }
                """.formatted(
                bool(true),
                bool(true),
                bool(true),
                bool(scannedArchive.hasPackMetadata()),
                bool(scannedJar.hasPackMetadata()),
                scannedArchive.namespaces().size(),
                jsonArray(scannedArchive.namespaces()),
                jsonArray(scannedJar.namespaces()),
                scannedArchive.textureCount(),
                scannedArchive.modelCount(),
                scannedArchive.blockstateCount(),
                scannedArchive.langCount(),
                scannedJar.textureCount(),
                scannedJar.modelCount(),
                scannedJar.blockstateCount(),
                scannedJar.langCount(),
                bool(true),
                jsonArray(reload.added())
        ));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void writeArchivePack(Path archivePack) throws IOException {
        Files.createDirectories(archivePack.getParent());
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archivePack))) {
            zipEntry(zip, "pack.mcmeta", """
                    {
                      "pack": {
                        "pack_format": 34,
                        "description": "Archive smoke pack"
                      }
                    }
                    """);
            zipEntry(zip, "assets/ziptest/lang/en_us.json", "{\"ziptest.badge\":\"Archive Badge\"}");
            zipEntry(zip, "assets/ziptest/blockstates/archive_block.json",
                    "{\"variants\":{\"\":{\"model\":\"ziptest:block/archive_block\"}}}");
            zipEntry(zip, "assets/ziptest/models/block/archive_block.json",
                    "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"ziptest:block/archive_block\"}}");
            zipEntry(zip, "assets/ziptest/textures/block/archive_block.png", "archive-block-texture");
            zipEntry(zip, "assets/ziptest/models/item/archive_badge.json",
                    "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"ziptest:item/archive_badge\"}}");
            zipEntry(zip, "assets/ziptest/textures/item/archive_badge.png", "archive-item-texture");
        }
    }

    private static void zipEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void writeJarPack(Path jarPack) throws IOException {
        Files.createDirectories(jarPack.getParent());
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(jarPack))) {
            zipEntry(zip, "pack.mcmeta", """
                    {
                      "pack": {
                        "pack_format": 34,
                        "description": "Jar smoke pack"
                      }
                    }
                    """);
            zipEntry(zip, "assets/jartest/lang/en_us.json", "{\"jartest.badge\":\"Jar Badge\"}");
            zipEntry(zip, "assets/jartest/blockstates/jar_block.json",
                    "{\"variants\":{\"\":{\"model\":\"jartest:block/jar_block\"}}}");
            zipEntry(zip, "assets/jartest/models/block/jar_block.json",
                    "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"jartest:block/jar_block\"}}");
            zipEntry(zip, "assets/jartest/textures/block/jar_block.png", "jar-block-texture");
            zipEntry(zip, "assets/jartest/models/item/jar_badge.json",
                    "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"jartest:item/jar_badge\"}}");
            zipEntry(zip, "assets/jartest/textures/item/jar_badge.png", "jar-item-texture");
        }
    }

    private static String jsonArray(Collection<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values.stream().sorted().toList()) {
            escaped.add("\"" + jsonEscape(value) + "\"");
        }
        return "[" + String.join(", ", escaped) + "]";
    }

    private static String jsonEscape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String bool(boolean value) {
        return value ? "true" : "false";
    }
}
