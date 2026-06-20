package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class EchoStandaloneLauncherRuntime {
    private static final String FIXED_TIME = "1970-01-01T00:00:00Z";
    private static final List<String> REQUIRED_ARTIFACTS = List.of(
            "docs/echo/standalone/ECHO_STANDALONE_VERTICAL_SLICE.md",
            "reports/echo/standalone/runtime-vertical-slice.json",
            "reports/echo/standalone/vertical-slice-clean-exit.json",
            "docs/echo/standalone/ECHO_STANDALONE_LAUNCHER_MVP.md",
            "reports/echo/standalone/runtime-launcher.json"
    );
    private static final List<String> SUPPORT_BUNDLE_PATHS = List.of(
            "build.gradle",
            "settings.gradle",
            "docs/echo/standalone/ECHO_STANDALONE_VERTICAL_SLICE.md",
            "docs/echo/standalone/ECHO_STANDALONE_LAUNCHER_MVP.md",
            "docs/echo/standalone/FINAL_TESTER_SCRIPT.md",
            "scripts/sign-release-artifacts.ps1",
            "scripts/capture-clean-install-uninstall-evidence.ps1",
            "scripts/capture-manual-wallclock-playtest.ps1",
            "docs/echo/standalone/BETA_RELEASE_READINESS.md",
            "docs/echo/standalone/RELEASE_ACCEPTANCE_REPORT.md",
            "reports/echo/standalone/runtime-vertical-slice.json",
            "reports/echo/standalone/vertical-slice-clean-exit.json",
            "reports/echo/standalone/runtime-launcher.json",
            "reports/echo/standalone/launcher-detection.json",
            "reports/echo/standalone/launcher-verification.json",
            "reports/echo/standalone/launcher-repair-plan.json",
            "reports/echo/standalone/launcher-support-bundle.json",
            "reports/echo/standalone/launcher-handoff.json",
            "reports/echo/standalone/launcher-smoke.json",
            "reports/echo/standalone/beta-readiness-gate.json",
            "reports/echo/standalone/beta-readiness-checks.json",
            "reports/echo/standalone/beta-readiness-playable-qa.json",
            "reports/echo/standalone/save-incompatible-mod-recovery.json",
            "reports/echo/standalone/client-machine-terminal-surfaces.json",
            "reports/echo/standalone/client-block-model-chunk-render.json",
            "reports/echo/standalone/client-voxel-biome-rendering.json",
            "reports/echo/standalone/beta-readiness-support-bundle.json",
            "reports/echo/standalone/beta-readiness-ci-policy.json",
            "reports/echo/standalone/release-acceptance-report.json",
            "reports/echo/standalone/public-release-gate.json",
            "reports/echo/standalone/full-mechanics-parity-audit.json",
            "reports/echo/standalone/full-block-behavior-matrix.json",
            "reports/echo/standalone/full-command-keybind-debug-chat.json",
            "reports/echo/standalone/full-data-pack-runtime-coverage.json",
            "reports/echo/standalone/full-addon-extension-surface.json",
            "reports/echo/standalone/full-progression-statistics-objectives.json",
            "reports/echo/standalone/full-worldgen-dimensions-structures.json",
            "reports/echo/standalone/full-entity-ai-spawn-loop.json",
            "reports/echo/standalone/full-survival-player-loop.json",
            "reports/echo/standalone/full-audio-particles-weather-ambience.json",
            "reports/echo/standalone/public-release-external-evidence-handoff.json",
            "reports/echo/standalone/manual-playtest-report.json",
            "reports/echo/standalone/performance-stability-soak.json",
            "reports/echo/standalone/runtime-distribution.json",
            "reports/echo/standalone/distribution-artifacts.json",
            "reports/echo/standalone/distribution-bundled-runtime.json",
            "reports/echo/standalone/distribution-first-run-checks.json",
            "reports/echo/standalone/distribution-icons.json",
            "reports/echo/standalone/distribution-installer.json",
            "reports/echo/standalone/distribution-install-uninstall-evidence.json",
            "reports/echo/standalone/distribution-signing-setup.json",
            "reports/echo/standalone/distribution-package-reproducibility.json",
            "reports/echo/standalone/distribution-package-reproducibility-strict.json",
            "reports/echo/standalone/distribution-portable-image.json",
            "reports/echo/standalone/distribution-version-metadata.json",
            "reports/echo/standalone/beta-distribution-release-contract.json",
            "reports/echo/standalone/packaged-opengl-client-image.json",
            "reports/echo/standalone/packaged-exe-wallclock-smoke.json",
            "reports/echo/standalone/packaged-exe-wallclock-strict-30m.json",
            "reports/echo/standalone/packaged-exe-wallclock-strict-60m.json",
            "reports/echo/standalone/packaged-exe-wallclock-strict-rehearsal.json",
            "reports/echo/standalone/manual-wallclock-capture-rehearsal.json",
            "reports/echo/standalone/ashfall-audio-cue-coverage.json",
            "reports/echo/standalone/ashfall-no-duplicate-gameplay-audit.json",
            "reports/echo/standalone/full-workspace-build-smoke.json"
    );

    public EchoStandaloneLauncherResult run(
            EchoRuntimeServiceRegistry services,
            EchoStandaloneLauncherRequest request
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(request, "request");

        EchoStandaloneLauncherDetection detection = detect(request.workspaceRoot());
        EchoStandaloneLauncherVerification verification = verify(request, detection);
        EchoStandaloneLauncherRepairPlan repairPlan = repairPlan(verification);
        EchoStandaloneLauncherHandoffPlan handoffPlan =
                EchoStandaloneLauncherHandoffPlan.forMode(request.mode());
        Optional<EchoStandaloneSupportBundle> supportBundle = request.includeSupportBundle()
                ? Optional.of(supportBundle(request.workspaceRoot(), verification, repairPlan, handoffPlan))
                : Optional.empty();
        Optional<EchoRuntimeBootResult> launchResult = Optional.empty();
        if (request.mode().launchesStandalone() && request.allowLaunch() && verification.ready()) {
            launchResult = Optional.of(new EchoRuntimeLauncher().launch(
                    EchoRuntimeBootContext.headless(request.workspaceRoot())
            ));
        }
        boolean handoffPreserved = !request.mode().launchesStandalone() && launchResult.isEmpty();
        EchoStandaloneLauncherResult result = new EchoStandaloneLauncherResult(
                request,
                detection,
                verification,
                repairPlan,
                handoffPlan,
                supportBundle,
                launchResult,
                handoffPreserved
        );
        services.register(EchoStandaloneLauncherDetection.class, detection);
        services.register(EchoStandaloneLauncherVerification.class, verification);
        services.register(EchoStandaloneLauncherRepairPlan.class, repairPlan);
        services.register(EchoStandaloneLauncherHandoffPlan.class, handoffPlan);
        supportBundle.ifPresent(bundle -> services.register(EchoStandaloneSupportBundle.class, bundle));
        services.register(EchoStandaloneLauncherResult.class, result);
        return result;
    }

    private static EchoStandaloneLauncherDetection detect(Path workspaceRoot) throws IOException {
        Path root = workspaceRoot.toAbsolutePath().normalize();
        boolean settingsFilePresent = Files.isRegularFile(root.resolve("settings.gradle"));
        boolean buildFilePresent = Files.isRegularFile(root.resolve("build.gradle"));
        boolean docsRootPresent = Files.isDirectory(root.resolve("docs/echo/standalone"));
        boolean reportsRootPresent = Files.isDirectory(root.resolve("reports/echo/standalone"));
        return new EchoStandaloneLauncherDetection(
                root,
                settingsFilePresent,
                buildFilePresent,
                docsRootPresent,
                reportsRootPresent,
                buildFilePresent ? readVersion(root.resolve("build.gradle")) : "unknown"
        );
    }

    private static EchoStandaloneLauncherVerification verify(
            EchoStandaloneLauncherRequest request,
            EchoStandaloneLauncherDetection detection
    ) {
        ArrayList<EchoStandaloneLauncherCheck> checks = new ArrayList<>();
        checks.add(new EchoStandaloneLauncherCheck(
                "workspace.detected",
                detection.standaloneWorkspace(),
                "settings, build, docs, and reports roots are present"
        ));
        checks.add(new EchoStandaloneLauncherCheck(
                "runtime.version",
                supportedRuntimeVersion(detection.runtimeVersion()),
                "runtime version is " + detection.runtimeVersion()
        ));
        for (String artifact : REQUIRED_ARTIFACTS) {
            checks.add(new EchoStandaloneLauncherCheck(
                    "artifact." + artifact,
                    Files.isRegularFile(request.workspaceRoot().resolve(artifact)),
                    artifact
            ));
        }
        checks.add(new EchoStandaloneLauncherCheck(
                "mode." + request.mode().id(),
                true,
                request.mode().launchesStandalone()
                        ? "standalone runtime launch path selected"
                        : "external handoff preserved"
        ));
        return new EchoStandaloneLauncherVerification(checks);
    }

    private static boolean supportedRuntimeVersion(String version) {
        if (version == null || version.isBlank() || "unknown".equals(version)) {
            return false;
        }
        if (version.startsWith("0.1.0-phase14.")) {
            return true;
        }
        return version.matches("0\\.1\\.[0-9]+-(alpha|beta|rc)(\\..+)?");
    }

    private static EchoStandaloneLauncherRepairPlan repairPlan(EchoStandaloneLauncherVerification verification) {
        List<String> actions = verification.checks().stream()
                .filter(check -> !check.passed())
                .<String>map(check -> "plan repair for " + check.checkId() + ": " + check.detail())
                .toList();
        return new EchoStandaloneLauncherRepairPlan(
                "echo:standalone-launcher-repair-plan",
                true,
                actions
        );
    }

    private static EchoStandaloneSupportBundle supportBundle(
            Path workspaceRoot,
            EchoStandaloneLauncherVerification verification,
            EchoStandaloneLauncherRepairPlan repairPlan,
            EchoStandaloneLauncherHandoffPlan handoffPlan
    ) throws IOException {
        Path root = workspaceRoot.toAbsolutePath().normalize();
        Path supportRoot = root.resolve("build/support");
        Files.createDirectories(supportRoot);
        EchoStandaloneSupportBundleDiagnostics.Generated generatedDiagnostics =
                EchoStandaloneSupportBundleDiagnostics.generate(
                        root,
                        supportRoot,
                        verification,
                        repairPlan,
                        handoffPlan
                );
        ArrayList<EchoStandaloneSupportBundleEntry> entries = new ArrayList<>();
        ArrayList<String> supportPaths = new ArrayList<>(SUPPORT_BUNDLE_PATHS);
        supportPaths.addAll(generatedDiagnostics.entries());
        for (String relativePath : supportPaths) {
            Path path = root.resolve(relativePath);
            boolean present = Files.isRegularFile(path);
            entries.add(new EchoStandaloneSupportBundleEntry(
                    relativePath,
                    present,
                    present ? Files.size(path) : 0L
            ));
        }
        Path manifestPath = supportRoot.resolve("EchoStandaloneSupportBundle.manifest");
        Path archivePath = supportRoot.resolve("EchoStandaloneSupportBundle.zip");
        ArrayList<String> diagnostics = new ArrayList<>(List.of(
                "verificationReady=" + verification.ready(),
                "repairActions=" + repairPlan.actionCount(),
                "handoffMode=" + handoffPlan.modeId(),
                "runtimeFamily=" + handoffPlan.runtimeFamily(),
                "rendererTarget=" + handoffPlan.rendererTarget(),
                "standaloneLaunchTask=" + handoffPlan.standaloneLaunchTask(),
                "externalCommandPreserved=" + handoffPlan.externalCommandPreserved()
        ));
        diagnostics.addAll(generatedDiagnostics.diagnostics());
        writeManifest(manifestPath, entries, diagnostics);
        writeArchive(root, archivePath, manifestPath, entries, diagnostics);
        boolean manifestPresent = Files.isRegularFile(manifestPath);
        boolean archivePresent = Files.isRegularFile(archivePath);
        return new EchoStandaloneSupportBundle(
                "echo:standalone-launcher-support-bundle",
                FIXED_TIME,
                entries,
                diagnostics,
                root.relativize(manifestPath).toString(),
                manifestPresent,
                manifestPresent ? Files.readAllLines(manifestPath).size() : 0,
                root.relativize(archivePath).toString(),
                archivePresent,
                archivePresent ? Files.size(archivePath) : 0L
        );
    }

    private static void writeManifest(
            Path manifestPath,
            List<EchoStandaloneSupportBundleEntry> entries,
            List<String> diagnostics
    ) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        lines.add("bundleId=echo:standalone-launcher-support-bundle");
        lines.add("generatedAt=" + FIXED_TIME);
        for (String diagnostic : diagnostics) {
            lines.add("diagnostic." + lines.size() + "=" + diagnostic);
        }
        for (EchoStandaloneSupportBundleEntry entry : entries) {
            lines.add("entry=" + entry.relativePath()
                    + "|present=" + entry.present()
                    + "|bytes=" + entry.byteSize());
        }
        Files.write(manifestPath, lines);
    }

    private static void writeArchive(
            Path workspaceRoot,
            Path archivePath,
            Path manifestPath,
            List<EchoStandaloneSupportBundleEntry> entries,
            List<String> diagnostics
    ) throws IOException {
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archivePath))) {
            addZipEntry(zip, "support/manifest.txt", Files.readAllBytes(manifestPath));
            addZipEntry(zip, "support/diagnostics.txt",
                    String.join(System.lineSeparator(), diagnostics).getBytes(StandardCharsets.UTF_8));
            for (EchoStandaloneSupportBundleEntry entry : entries) {
                if (entry.present()) {
                    Path source = workspaceRoot.resolve(entry.relativePath()).normalize();
                    if (source.startsWith(workspaceRoot) && Files.isRegularFile(source)) {
                        addZipEntry(zip, entry.relativePath(), Files.readAllBytes(source));
                    }
                }
            }
        }
    }

    private static void addZipEntry(ZipOutputStream zip, String name, byte[] bytes) throws IOException {
        ZipEntry entry = new ZipEntry(name.replace('\\', '/'));
        entry.setTime(0L);
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
    }

    private static String readVersion(Path buildFile) throws IOException {
        for (String line : Files.readAllLines(buildFile)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("version =")) {
                int firstQuote = trimmed.indexOf('\'');
                int lastQuote = trimmed.lastIndexOf('\'');
                if (firstQuote >= 0 && lastQuote > firstQuote) {
                    return trimmed.substring(firstQuote + 1, lastQuote);
                }
            }
        }
        return "unknown";
    }
}
