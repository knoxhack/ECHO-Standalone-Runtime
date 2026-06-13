package dev.echo.standalone.runtime.testkit;

import dev.echo.nativeplatform.contracts.EchoNativeServiceRegistry;
import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContentActivationRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycle;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleServiceExportRegistry;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class EchoRuntimeAddonArchiveCompatibilitySmokeHarness {
    private static final String FOLDER_MODULE_ID = "folder-runtime-addon";
    private static final String JAR_MODULE_ID = "archive-jar-addon";
    private static final String ZIP_MODULE_ID = "archive-zip-native-addon";
    private static final String ECHO_ADDON_MODULE_ID = "packaged-echo-addon";
    private static final String UNSAFE_ECHO_ADDON_MODULE_ID = "unsafe-echo-addon";

    private EchoRuntimeAddonArchiveCompatibilitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-addon-archive-compat");
        Path folderAddon = fixtureRoot.resolve(FOLDER_MODULE_ID);
        Path folderClasses = folderAddon.resolve("classes");
        Path jarClasses = fixtureRoot.resolve("jar-classes");
        Path zipClasses = fixtureRoot.resolve("zip-classes");
        Path echoAddonClasses = fixtureRoot.resolve("echo-addon-classes");
        Path unsafeEchoAddonClasses = fixtureRoot.resolve("unsafe-echo-addon-classes");
        compileSource(folderClasses, "fixture/folder/FolderAddonEntrypoint.java", """
                package fixture.folder;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;

                public final class FolderAddonEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.registerContent("folder.block", "folderaddon:block/runtime_marker");
                        context.exportService("folderaddon:service", "folder-loaded");
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.exportService("folderaddon:reload", "folder-reloaded");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                    }
                }
                """);
        compileSource(jarClasses, "fixture/archive/JarAddonEntrypoint.java", """
                package fixture.archive;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;

                public final class JarAddonEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.registerContent("archive.block", "archivejar:block/runtime_marker");
                        context.exportService("archivejar:service", "jar-loaded");
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                    }
                }
                """);
        compileSource(zipClasses, "fixture/archive/ZipNativeEntrypoint.java", """
                package fixture.archive;

                import dev.echo.nativeplatform.contracts.EchoNativeLoadStatus;
                import dev.echo.nativeplatform.contracts.EchoNativeModuleEntrypoint;
                import dev.echo.nativeplatform.contracts.EchoNativeModuleLoadContext;

                public final class ZipNativeEntrypoint implements EchoNativeModuleEntrypoint {
                    @Override
                    public void discover(EchoNativeModuleLoadContext context) {
                        context.attribute("zipNativeDiscovered", true);
                    }

                    @Override
                    public void registerServices(EchoNativeModuleLoadContext context) {
                        context.registerService("archivezip:native_service", this, "archive", "zip", "native");
                    }

                    @Override
                    public void ready(EchoNativeModuleLoadContext context) {
                        context.attribute("zipNativeReady", true);
                    }

                    @Override
                    public void shutdown(EchoNativeModuleLoadContext context) {
                        context.recordMutation("lifecycle", "shutdown", "archivezip", EchoNativeLoadStatus.MUTATED);
                    }
                }
                """);
        compileSource(echoAddonClasses, "fixture/archive/EchoAddonPackageEntrypoint.java", """
                package fixture.archive;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;

                public final class EchoAddonPackageEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.registerContent("echoaddon.block", "echoaddon:block/runtime_marker");
                        context.exportService("echoaddon:service", "echo-addon-loaded");
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                        context.exportService("echoaddon:reload", "echo-addon-reloaded");
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                    }
                }
                """);
        compileSource(unsafeEchoAddonClasses, "fixture/archive/UnsafeEchoAddonEntrypoint.java", """
                package fixture.archive;

                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleContext;
                import dev.echo.standalone.runtime.modules.EchoRuntimeModuleEntrypoint;

                public final class UnsafeEchoAddonEntrypoint implements EchoRuntimeModuleEntrypoint {
                    @Override
                    public void onLoad(EchoRuntimeModuleContext context) {
                        context.registerContent("unsafe.block", "unsafeechoaddon:block/should_not_load");
                        context.exportService("unsafeechoaddon:service", "unsafe-loaded");
                    }

                    @Override
                    public void onDataReload(EchoRuntimeModuleContext context) {
                    }

                    @Override
                    public void onUnload(EchoRuntimeModuleContext context) {
                    }
                }
                """);

        writeFolderDescriptor(folderAddon, """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "folder-runtime-addon",
                  "name": "Folder Runtime Addon",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["folder.runtime"],
                  "consumes": [],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.folder.FolderAddonEntrypoint",
                  "access": {}
                }
                """);

        Path jarAddon = fixtureRoot.resolve("archive-jar-addon.jar");
        Path zipAddon = fixtureRoot.resolve("archive-zip-native-addon.zip");
        Path echoAddon = fixtureRoot.resolve("packaged-echo-addon.echo-addon");
        Path unsafeEchoAddon = fixtureRoot.resolve("unsafe-echo-addon.echo-addon");
        Path corruptAddon = fixtureRoot.resolve("corrupt-addon.zip");
        writeArchive(jarAddon, jarClasses, """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "archive-jar-addon",
                  "name": "Archive Jar Addon",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["archive.jar"],
                  "consumes": [],
                  "permissions": ["content.register", "services.export"],
                  "entrypoint": "fixture.archive.JarAddonEntrypoint",
                  "access": {}
                }
                """);
        writeArchive(zipAddon, zipClasses, """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "archive-zip-native-addon",
                  "name": "Archive Zip Native Addon",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["archive.zip.native"],
                  "consumes": [],
                  "permissions": [],
                  "access": {
                    "nativeEntrypoint": "fixture.archive.ZipNativeEntrypoint",
                    "nativeClasspath": []
                  }
                }
                """);
        writeArchive(echoAddon, echoAddonClasses, "classes", """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "packaged-echo-addon",
                  "name": "Packaged Echo Addon",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["package.echo_addon"],
                  "consumes": [],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["classes"],
                  "entrypoint": "fixture.archive.EchoAddonPackageEntrypoint",
                  "access": {}
                }
                """);
        writeArchive(unsafeEchoAddon, unsafeEchoAddonClasses, "classes", """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "unsafe-echo-addon",
                  "name": "Unsafe Echo Addon",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["package.echo_addon.unsafe"],
                  "consumes": [],
                  "permissions": ["content.register", "services.export"],
                  "classPath": ["../classes"],
                  "entrypoint": "fixture.archive.UnsafeEchoAddonEntrypoint",
                  "access": {}
                }
                """);
        Files.writeString(corruptAddon, "not a zip", StandardCharsets.UTF_8);

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        EchoRuntimeModuleManager manager = EchoRuntimeModuleManager.executableAbiV1();
        EchoRuntimeModuleRuntimeResult result = manager.run(List.of(fixtureRoot), services);
        EchoRuntimeModuleRegistry registry = result.registry();
        EchoRuntimeModuleContentActivationRegistry content =
                services.require(EchoRuntimeModuleContentActivationRegistry.class);
        EchoRuntimeModuleServiceExportRegistry exports =
                services.require(EchoRuntimeModuleServiceExportRegistry.class);
        EchoNativeServiceRegistry nativeServices = services.require(EchoNativeServiceRegistry.class);

        require(registry.find(FOLDER_MODULE_ID).orElseThrow().moduleRoot().equals(folderAddon),
                "folder addon descriptor should preserve exploded module root");
        require(registry.find(JAR_MODULE_ID).orElseThrow().moduleRoot().equals(jarAddon),
                "jar addon descriptor should preserve archive module root");
        require(registry.find(ZIP_MODULE_ID).orElseThrow().moduleRoot().equals(zipAddon),
                "zip addon descriptor should preserve archive module root");
        require(registry.find(ECHO_ADDON_MODULE_ID).orElseThrow().moduleRoot().equals(echoAddon),
                "echo-addon package descriptor should preserve archive module root");
        require(registry.find(UNSAFE_ECHO_ADDON_MODULE_ID).orElseThrow().moduleRoot().equals(unsafeEchoAddon),
                "unsafe echo-addon package descriptor should be discovered before rejection");
        require(registry.lifecycle(FOLDER_MODULE_ID) == EchoRuntimeModuleLifecycle.READY,
                "folder addon should load from exploded directory and reach READY");
        require(registry.lifecycle(JAR_MODULE_ID) == EchoRuntimeModuleLifecycle.READY,
                "jar addon should load from archive and reach READY");
        require(registry.lifecycle(ZIP_MODULE_ID) == EchoRuntimeModuleLifecycle.READY,
                "zip native addon should load from archive and reach READY");
        require(registry.lifecycle(ECHO_ADDON_MODULE_ID) == EchoRuntimeModuleLifecycle.READY,
                "echo-addon package should load from archive and reach READY");
        require(registry.lifecycle(UNSAFE_ECHO_ADDON_MODULE_ID) == EchoRuntimeModuleLifecycle.FAILED,
                "unsafe echo-addon archive classPath escape should fail safely");
        require(content.activations(FOLDER_MODULE_ID).stream()
                        .anyMatch(activation -> activation.contentId().equals("folderaddon:block/runtime_marker")),
                "folder addon should register runtime content from folder-loaded class");
        require(exports.findService("folderaddon:service", String.class).orElse("").equals("folder-loaded"),
                "folder addon should export a runtime service from folder-loaded class");
        require(content.activations(JAR_MODULE_ID).stream()
                        .anyMatch(activation -> activation.contentId().equals("archivejar:block/runtime_marker")),
                "jar addon should register runtime content from archive-loaded class");
        require(exports.findService("archivejar:service", String.class).orElse("").equals("jar-loaded"),
                "jar addon should export a runtime service from archive-loaded class");
        require(nativeServices.hasService(ZIP_MODULE_ID, "archivezip:native_service"),
                "zip native addon should register a Native Platform service from archive-loaded class");
        require(content.activations(ECHO_ADDON_MODULE_ID).stream()
                        .anyMatch(activation -> activation.contentId().equals("echoaddon:block/runtime_marker")),
                "echo-addon package should register runtime content from package-loaded class");
        require(exports.findService("echoaddon:service", String.class).orElse("").equals("echo-addon-loaded"),
                "echo-addon package should export a runtime service from package-loaded class");
        require(content.activations(UNSAFE_ECHO_ADDON_MODULE_ID).isEmpty(),
                "unsafe echo-addon should not register runtime content");
        require(exports.findExport("unsafeechoaddon:service").isEmpty(),
                "unsafe echo-addon should not export runtime services");
        require(hasUnsafeArchiveEscapeDiagnostic(diagnostics),
                "unsafe echo-addon classPath escape should emit a module execution diagnostic");
        require(diagnostics.countsByCode().containsKey("ECHO-STANDALONE-MODULE-ARCHIVE-SCAN-FAILED"),
                "corrupt archive should emit a clear scan diagnostic without stopping valid addons");

        manager.reloadData(result, services);
        require(registry.lifecycle(FOLDER_MODULE_ID) == EchoRuntimeModuleLifecycle.DATA_RELOADED,
                "folder addon should survive data reload");
        require(registry.lifecycle(JAR_MODULE_ID) == EchoRuntimeModuleLifecycle.DATA_RELOADED,
                "jar addon should survive data reload");
        require(registry.lifecycle(ZIP_MODULE_ID) == EchoRuntimeModuleLifecycle.DATA_RELOADED,
                "zip native addon should survive data reload");
        require(registry.lifecycle(ECHO_ADDON_MODULE_ID) == EchoRuntimeModuleLifecycle.DATA_RELOADED,
                "echo-addon package should survive data reload");
        require(exports.findService("folderaddon:reload", String.class).orElse("").equals("folder-reloaded"),
                "folder addon should execute data reload hook from folder-loaded class");
        require(exports.findService("echoaddon:reload", String.class).orElse("").equals("echo-addon-reloaded"),
                "echo-addon package should execute data reload hook from package-loaded class");

        manager.unload(result, services);
        require(registry.lifecycle(FOLDER_MODULE_ID) == EchoRuntimeModuleLifecycle.UNLOADED,
                "folder addon should unload cleanly");
        require(registry.lifecycle(JAR_MODULE_ID) == EchoRuntimeModuleLifecycle.UNLOADED,
                "jar addon should unload cleanly");
        require(registry.lifecycle(ZIP_MODULE_ID) == EchoRuntimeModuleLifecycle.UNLOADED,
                "zip native addon should unload cleanly");
        require(registry.lifecycle(ECHO_ADDON_MODULE_ID) == EchoRuntimeModuleLifecycle.UNLOADED,
                "echo-addon package should unload cleanly");
        require(content.activations(FOLDER_MODULE_ID).isEmpty(),
                "folder addon content should be revoked on unload");
        require(exports.findExport("folderaddon:service").isEmpty(),
                "folder addon service should be revoked on unload");
        require(exports.findExport("folderaddon:reload").isEmpty(),
                "folder addon reload service should be revoked on unload");
        require(content.activations(JAR_MODULE_ID).isEmpty(),
                "jar addon content should be revoked on unload");
        require(exports.findExport("archivejar:service").isEmpty(),
                "jar addon service should be revoked on unload");
        require(!nativeServices.hasService(ZIP_MODULE_ID, "archivezip:native_service"),
                "zip native addon service should be revoked on unload");
        require(content.activations(ECHO_ADDON_MODULE_ID).isEmpty(),
                "echo-addon package content should be revoked on unload");
        require(exports.findExport("echoaddon:service").isEmpty(),
                "echo-addon package service should be revoked on unload");
        require(exports.findExport("echoaddon:reload").isEmpty(),
                "echo-addon package reload service should be revoked on unload");

        writeReport(Path.of(".").toAbsolutePath().normalize(), folderAddon, jarAddon, zipAddon, echoAddon,
                unsafeEchoAddon, corruptAddon, diagnostics);
        System.out.println("addon archive compatibility smoke PASS folder="
                + FOLDER_MODULE_ID
                + " jar=" + JAR_MODULE_ID
                + " zip=" + ZIP_MODULE_ID
                + " echoAddon=" + ECHO_ADDON_MODULE_ID
                + " unsafeEchoAddonRejected=true"
                + " corruptDiagnostic=true"
                + " diagnostics=" + diagnostics.diagnostics().size());
    }

    private static void compileSource(Path classesRoot, String relativeSourcePath, String source) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK compiler is required for addon archive compatibility smoke");
        }
        Path sourcePath = classesRoot.resolve(relativeSourcePath);
        Files.createDirectories(sourcePath.getParent());
        Files.writeString(sourcePath, source, StandardCharsets.UTF_8);
        int exitCode = compiler.run(
                null,
                null,
                null,
                "-classpath",
                System.getProperty("java.class.path"),
                "-d",
                classesRoot.toString(),
                sourcePath.toString()
        );
        if (exitCode != 0) {
            throw new IllegalStateException("Archive fixture compilation failed: " + sourcePath);
        }
    }

    private static void writeArchive(Path archive, Path classesRoot, String descriptor) throws IOException {
        writeArchive(archive, classesRoot, "", descriptor);
    }

    private static void writeArchive(Path archive, Path classesRoot, String classPrefix, String descriptor)
            throws IOException {
        String prefix = classPrefix == null || classPrefix.isBlank() ? "" : classPrefix.replace('\\', '/') + "/";
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archive))) {
            zipEntry(zip, "META-INF/echo.mod.json", descriptor);
            try (var stream = Files.walk(classesRoot)) {
                for (Path classFile : stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".class"))
                        .sorted()
                        .toList()) {
                    String entryName = prefix + classesRoot.relativize(classFile).toString().replace('\\', '/');
                    zip.putNextEntry(new ZipEntry(entryName));
                    zip.write(Files.readAllBytes(classFile));
                    zip.closeEntry();
                }
            }
        }
    }

    private static void writeFolderDescriptor(Path folderAddon, String descriptor) throws IOException {
        Path descriptorPath = folderAddon.resolve("META-INF/echo.mod.json");
        Files.createDirectories(descriptorPath.getParent());
        Files.writeString(descriptorPath, descriptor, StandardCharsets.UTF_8);
    }

    private static void zipEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static void writeReport(
            Path standaloneRoot,
            Path folderAddon,
            Path jarAddon,
            Path zipAddon,
            Path echoAddon,
            Path unsafeEchoAddon,
            Path corruptAddon,
            EchoRuntimeLogBridge diagnostics
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/addon-archive-compatibility-smoke.json");
        Files.createDirectories(report.getParent());
        String json = "{\n"
                + "  \"schema\": \"echo.standalone.addon_archive_compatibility_smoke.v5\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"folderScanRoot\": \"" + escape(folderAddon.getParent().toString()) + "\",\n"
                + "  \"folderAddon\": \"" + escape(folderAddon.toString()) + "\",\n"
                + "  \"jarAddon\": \"" + escape(jarAddon.toString()) + "\",\n"
                + "  \"zipAddon\": \"" + escape(zipAddon.toString()) + "\",\n"
                + "  \"echoAddon\": \"" + escape(echoAddon.toString()) + "\",\n"
                + "  \"echoAddonInternalClassPath\": \"classes\",\n"
                + "  \"unsafeEchoAddon\": \"" + escape(unsafeEchoAddon.toString()) + "\",\n"
                + "  \"unsafeEchoAddonRejected\": true,\n"
                + "  \"unsafeEchoAddonLifecycle\": \"FAILED\",\n"
                + "  \"unsafeEchoAddonEscapeDiagnostic\": " + hasUnsafeArchiveEscapeDiagnostic(diagnostics) + ",\n"
                + "  \"corruptAddon\": \"" + escape(corruptAddon.toString()) + "\",\n"
                + "  \"folderLoadReloadUnload\": true,\n"
                + "  \"jarLoadReloadUnload\": true,\n"
                + "  \"zipNativeLoadReloadUnload\": true,\n"
                + "  \"echoAddonLoadReloadUnload\": true,\n"
                + "  \"corruptArchiveRecovered\": true,\n"
                + "  \"diagnosticCodes\": " + stringArray(diagnostics.countsByCode().keySet().stream().sorted().toList()) + "\n"
                + "}\n";
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static boolean hasUnsafeArchiveEscapeDiagnostic(EchoRuntimeLogBridge diagnostics) {
        return diagnostics.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code().equals("echo.runtime.module.execution_failed")
                        && diagnostic.attributes().getOrDefault("moduleId", "").equals(UNSAFE_ECHO_ADDON_MODULE_ID)
                        && diagnostic.detail().contains("Archive module classPath entry escapes archive root"));
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        return json.append("]").toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
