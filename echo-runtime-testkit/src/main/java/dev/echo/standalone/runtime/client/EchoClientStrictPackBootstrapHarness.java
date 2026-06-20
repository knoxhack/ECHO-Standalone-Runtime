package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycle;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class EchoClientStrictPackBootstrapHarness {
    private static final String MODULE_ID = "echo-strictpack-fixture";
    private static final String MODULE_JAR = "echo-strictpack-fixture-1.0.0-standalone.jar";
    private static final String CONTENT_ID = MODULE_ID + ":strict_pack_ingot";

    public static void main(String[] args) throws Exception {
        Path root = Files.createTempDirectory("echo-client-strict-pack-bootstrap");

        FixturePack valid = createPack(root.resolve("valid"), true, null);
        EchoClientModuleBootstrapResult result = EchoClientModuleBootstrap.boot(context(valid, false));
        require(result.active(), "strict installed-pack bootstrap should be active");
        require(result.strictPackMode(), "bootstrap should retain strict-pack diagnostics");
        require(!result.safeMode(), "valid strict pack should not be safe mode");
        require(result.moduleRoots().size() == 1, "valid pack should discover one installed module jar");
        require(result.modScanSummary().descriptorCount() == 1, "valid pack should expose one executed descriptor");
        require(result.modScanSummary().activeCount() == 1, "valid pack should expose one active module");
        require(result.moduleRuntimeResult().registry().runtimeStatus(MODULE_ID) == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "valid module should be runtime-active");
        require(result.adapterCoreContentRows().size() == 1, "native activation bridge should produce one content row");
        require(CONTENT_ID.equals(result.adapterCoreContentRows().get(0).get("contentId")),
                "native content row should use the fixture content id");

        EchoClientRuntimeServices services = EchoClientRuntimeServices.forTemplate(
                EchoClientWorldTemplates.ashfallCrashSite(),
                root.resolve("saves"),
                result
        );
        require(services.runtimeContentSummary().rowCount() == 1,
                "client runtime services should import native AdapterCore content");
        services.refreshModScan();
        require(services.runtimeContentSummary().rowCount() == 1,
                "active module bootstrap refresh should not duplicate runtime content");
        require(services.importAdapterCoreContentRegistrations(result.adapterCoreContentRows()) == 0,
                "re-importing identical native content rows should be duplicate-free");

        EchoRuntimeModuleRuntimeResult runtimeResult = result.moduleRuntimeResult();
        result.close();
        require(runtimeResult.registry().lifecycle(MODULE_ID) == EchoRuntimeModuleLifecycle.UNLOADED,
                "bootstrap close should unload the executable module graph");

        FixturePack missing = createPack(root.resolve("missing"), false, null);
        expectFailure("missing artifact", () -> EchoClientModuleBootstrap.boot(context(missing, false)));

        FixturePack mismatch = createPack(root.resolve("mismatch"), true, "0".repeat(64));
        expectFailure("checksum mismatch", () -> EchoClientModuleBootstrap.boot(context(mismatch, false)));

        EchoClientModuleBootstrapResult safe = EchoClientModuleBootstrap.boot(context(missing, true));
        require(!safe.active(), "explicit safe mode should not activate a broken installed pack");
        require(safe.strictPackMode(), "safe-mode result should still report strict-pack context");
        require(safe.safeMode(), "safe-mode result should retain safe-mode diagnostics");
        require(!safe.failure().isBlank(), "safe-mode result should retain the bootstrap failure");

        System.out.println("STRICT PACK BOOTSTRAP TESTS PASS");
    }

    private static FixturePack createPack(Path packRoot, boolean writeJar, String shaOverride) throws IOException {
        Path modulesRoot = packRoot.resolve("mods");
        Files.createDirectories(modulesRoot);
        Path jar = modulesRoot.resolve(MODULE_JAR);
        String sha256 = "0";
        if (writeJar) {
            writeModuleJar(packRoot.resolve("fixture-build"), jar);
            sha256 = shaOverride == null ? sha256(jar) : shaOverride;
        }
        Path manifest = packRoot.resolve(".echo").resolve("pack-manifest.json");
        Files.createDirectories(manifest.getParent());
        Files.writeString(manifest, """
                {
                  "schemaVersion": "echo.pack_manifest.v1",
                  "pack": "strict-pack-bootstrap-fixture",
                  "files": [
                    {
                      "id": "%1$s",
                      "moduleId": "%1$s",
                      "artifactFamily": "standalone",
                      "path": "mods/%2$s",
                      "assetName": "%2$s",
                      "sha256": "%3$s",
                      "required": true
                    }
                  ]
                }
                """.formatted(MODULE_ID, MODULE_JAR, sha256), StandardCharsets.UTF_8);
        return new FixturePack(packRoot, modulesRoot);
    }

    private static void writeModuleJar(Path buildRoot, Path jar) throws IOException {
        Path source = buildRoot.resolve("src/fixture/strictpack/StrictPackNativeEntrypoint.java");
        Path classes = buildRoot.resolve("classes");
        Files.createDirectories(source.getParent());
        Files.createDirectories(classes);
        Files.writeString(source, """
                package fixture.strictpack;

                import java.util.LinkedHashMap;
                import java.util.List;
                import java.util.Map;

                public final class StrictPackNativeEntrypoint {
                    public Map<String, Object> describeNativeSurfaces(Map<String, String> context) {
                        Map<String, Object> registration = new LinkedHashMap<>();
                        registration.put("registry", "item");
                        registration.put("id", "strict_pack_ingot");
                        registration.put("displayName", "Strict Pack Ingot");
                        registration.put("moduleId", context.get("moduleId"));

                        Map<String, Object> bridge = new LinkedHashMap<>();
                        bridge.put("registrations", List.of(registration));

                        Map<String, Object> activation = new LinkedHashMap<>();
                        activation.put("nativeAdapterCodeExecuted", true);
                        activation.put("serviceCodeExecuted", true);
                        activation.put("registryMutated", true);
                        activation.put("logicalRegistrationCount", 1);
                        activation.put("registeredFeatureContracts", List.of("strict-pack-content"));
                        activation.put("adapterDomains", List.of("item"));
                        activation.put("runtimeTargets", List.of("echo_runtime_standalone"));
                        activation.put("registryBridge", bridge);
                        return activation;
                    }
                }
                """, StandardCharsets.UTF_8);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        require(compiler != null, "strict-pack bootstrap harness requires a JDK compiler");
        int exitCode = compiler.run(
                null,
                null,
                null,
                "-encoding",
                "UTF-8",
                "-d",
                classes.toString(),
                source.toString()
        );
        require(exitCode == 0, "fixture module source should compile");
        Files.createDirectories(jar.getParent());
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            addJarEntry(output, "META-INF/echo.mod.json", descriptor().getBytes(StandardCharsets.UTF_8));
            try (var stream = Files.walk(classes)) {
                for (Path file : stream
                        .filter(Files::isRegularFile)
                        .sorted()
                        .toList()) {
                    String entryName = classes.relativize(file).toString().replace('\\', '/');
                    addJarEntry(output, entryName, Files.readAllBytes(file));
                }
            }
        }
    }

    private static String descriptor() {
        return """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "%1$s",
                  "name": "Strict Pack Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": true,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["echo:test-content"],
                  "consumes": [],
                  "classPath": ["."],
                  "nativeEntrypoint": "fixture.strictpack.StrictPackNativeEntrypoint",
                  "access": {
                    "forceStandaloneExecution": true
                  }
                }
                """.formatted(MODULE_ID);
    }

    private static EchoClientLaunchContext context(FixturePack pack, boolean safeMode) {
        List<String> args = new java.util.ArrayList<>(List.of(
                "--pack-root",
                pack.packRoot().toString(),
                "--modules-root",
                pack.modulesRoot().toString()
        ));
        if (safeMode) {
            args.add("--safe-mode");
        }
        return EchoClientLaunchContext.parse(args.toArray(String[]::new));
    }

    private static void addJarEntry(JarOutputStream output, String name, byte[] bytes) throws IOException {
        JarEntry entry = new JarEntry(name);
        entry.setTime(0L);
        output.putNextEntry(entry);
        output.write(bytes);
        output.closeEntry();
    }

    private static String sha256(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Files.readAllBytes(file));
            return HexFormat.of().formatHex(digest.digest());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash " + file + ": " + exception.getMessage(), exception);
        }
    }

    private static void expectFailure(String expected, ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (RuntimeException exception) {
            require(exception.getMessage() != null && exception.getMessage().contains(expected),
                    "expected failure containing '" + expected + "' but got " + exception.getMessage());
            return;
        } catch (Exception exception) {
            throw new AssertionError("expected runtime failure containing '" + expected + "'", exception);
        }
        throw new AssertionError("expected failure containing '" + expected + "'");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record FixturePack(Path packRoot, Path modulesRoot) {
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private EchoClientStrictPackBootstrapHarness() {
    }
}
