package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeBootResult;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherDetection;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherHandoffPlan;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherRepairPlan;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherRequest;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherResult;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherRuntime;
import dev.echo.standalone.runtime.app.EchoStandaloneLauncherVerification;
import dev.echo.standalone.runtime.app.EchoStandaloneSupportBundle;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class EchoRuntimeLauncherSmokeHarness {
    private EchoRuntimeLauncherSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
        EchoStandaloneLauncherRuntime launcher = new EchoStandaloneLauncherRuntime();
        EchoDefaultRuntimeServiceRegistry standaloneServices = new EchoDefaultRuntimeServiceRegistry();
        EchoStandaloneLauncherResult standalone = launcher.run(
                standaloneServices,
                EchoStandaloneLauncherRequest.standalone(workspaceRoot)
        );

        require(standaloneServices.require(EchoStandaloneLauncherResult.class) == standalone,
                "launcher result should be service-bound");
        require(standaloneServices.require(EchoStandaloneLauncherDetection.class) == standalone.detection(),
                "launcher detection should be service-bound");
        require(standaloneServices.require(EchoStandaloneLauncherVerification.class) == standalone.verification(),
                "launcher verification should be service-bound");
        require(standaloneServices.require(EchoStandaloneLauncherRepairPlan.class) == standalone.repairPlan(),
                "launcher repair plan should be service-bound");
        require(standaloneServices.require(EchoStandaloneLauncherHandoffPlan.class) == standalone.handoffPlan(),
                "launcher handoff plan should be service-bound");
        require(standaloneServices.require(EchoStandaloneSupportBundle.class)
                        == standalone.supportBundle().orElseThrow(),
                "support bundle should be service-bound");
        require(standalone.detection().standaloneWorkspace(),
                "launcher should detect the standalone workspace");
        require(standalone.verification().ready(),
                "launcher verification should pass for the current workspace");
        require(standalone.repairPlan().planningOnly(),
                "launcher repair plan must be planning-only");
        require(standalone.repairPlan().actionCount() == 0,
                "ready workspace should not need launcher repair actions");
        require(standalone.handoffPlan().standaloneOpenGlClientTarget(),
                "standalone launcher handoff plan should target the OpenGL client task");
        EchoStandaloneSupportBundle bundle = standalone.supportBundle().orElseThrow();
        require(bundle.entries().size() >= 14,
                "support bundle should include tester docs plus release reports");
        require(bundle.presentEntryCount() == bundle.entries().size(),
                "all support bundle entries should be present for the current workspace");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("docs/echo/standalone/FINAL_TESTER_SCRIPT.md")),
                "support bundle should include the final tester script");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("scripts/sign-release-artifacts.ps1")),
                "support bundle should include the release signing helper script");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("scripts/capture-clean-install-uninstall-evidence.ps1")),
                "support bundle should include the clean install/uninstall capture script");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("scripts/capture-manual-wallclock-playtest.ps1")),
                "support bundle should include the manual wall-clock playtest capture script");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("docs/echo/standalone/BETA_RELEASE_READINESS.md")),
                "support bundle should include beta release readiness documentation");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/beta-readiness-gate.json")),
                "support bundle should include beta readiness gate evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/beta-readiness-checks.json")),
                "support bundle should include beta readiness check evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/beta-readiness-playable-qa.json")),
                "support bundle should include beta playable QA evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/save-incompatible-mod-recovery.json")),
                "support bundle should include save incompatible-mod recovery evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/client-machine-terminal-surfaces.json")),
                "support bundle should include machine and terminal ScreenCore surface evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/client-block-model-chunk-render.json")),
                "support bundle should include blockstate/model chunk-render evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/client-voxel-biome-rendering.json")),
                "support bundle should include voxel biome id and tint rendering evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/release-acceptance-report.json")),
                "support bundle should include release acceptance evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/manual-playtest-report.json")),
                "support bundle should include manual playtest evidence status");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/public-release-gate.json")),
                "support bundle should include public release gate evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-mechanics-parity-audit.json")),
                "support bundle should include full mechanics parity audit evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-block-behavior-matrix.json")),
                "support bundle should include full block behavior mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-command-keybind-debug-chat.json")),
                "support bundle should include full command/keybind/debug/chat mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-data-pack-runtime-coverage.json")),
                "support bundle should include full data pack/runtime mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-addon-extension-surface.json")),
                "support bundle should include full addon extension surface mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-progression-statistics-objectives.json")),
                "support bundle should include full progression/statistics/objectives mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-worldgen-dimensions-structures.json")),
                "support bundle should include full worldgen/dimensions/structures mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-entity-ai-spawn-loop.json")),
                "support bundle should include full entity AI/spawn mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-survival-player-loop.json")),
                "support bundle should include full survival/player-loop mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/full-audio-particles-weather-ambience.json")),
                "support bundle should include full presentation mechanics parity evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/public-release-external-evidence-handoff.json")),
                "support bundle should include public release external evidence handoff");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/performance-stability-soak.json")),
                "support bundle should include stability soak evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/distribution-artifacts.json")),
                "support bundle should include packaged artifact hash evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/distribution-package-reproducibility.json")),
                "support bundle should include package reproducibility evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/distribution-package-reproducibility-strict.json")),
                "support bundle should include strict two-refresh package reproducibility evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/distribution-installer.json")),
                "support bundle should include installer handoff evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/distribution-install-uninstall-evidence.json")),
                "support bundle should include clean install/uninstall evidence status");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/distribution-signing-setup.json")),
                "support bundle should include signing and setup readiness evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/packaged-opengl-client-image.json")),
                "support bundle should include packaged OpenGL client image evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/packaged-exe-wallclock-smoke.json")),
                "support bundle should include packaged EXE wall-clock smoke evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/packaged-exe-wallclock-strict-30m.json")),
                "support bundle should include strict 30-minute packaged EXE monitor evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/packaged-exe-wallclock-strict-60m.json")),
                "support bundle should include strict 60-minute packaged EXE monitor evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/packaged-exe-wallclock-strict-rehearsal.json")),
                "support bundle should include current OpenGL strict packaged EXE rehearsal evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/manual-wallclock-capture-rehearsal.json")),
                "support bundle should include manual wall-clock capture rehearsal evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/beta-readiness-support-bundle.json")),
                "support bundle should include beta support-bundle evidence");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("reports/echo/standalone/ashfall-no-duplicate-gameplay-audit.json")),
                "support bundle should include the Ashfall no-duplicate-gameplay audit");
        require(bundle.entries().stream().noneMatch(entry -> entry.relativePath()
                        .contains("native-loader")),
                "OpenGL support bundle should not require Native Loader ABI report entries");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("build/support/standalone-mod-diagnostics.json")),
                "support bundle should include generated mod diagnostics");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("build/support/standalone-runtime-registry-fingerprint.json")),
                "support bundle should include generated runtime registry fingerprints");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("build/support/standalone-adaptercore-target-diff.json")),
                "support bundle should include generated AdapterCore target diff diagnostics");
        require(bundle.entries().stream().anyMatch(entry -> entry.relativePath()
                        .equals("build/support/standalone-module-lifecycle-traces.json")),
                "support bundle should include generated module lifecycle trace diagnostics");
        require(bundle.diagnostics().stream().anyMatch(line -> line.equals("rendererTarget=opengl")),
                "standalone support bundle diagnostics should preserve the OpenGL renderer target");
        require(bundle.diagnostics().stream().anyMatch(line -> line.startsWith("runtimeRegistryFingerprint=")),
                "standalone support bundle diagnostics should include the runtime registry fingerprint");
        require(bundle.diagnostics().stream().anyMatch(line -> line.equals("adapterCoreTargetDiffStatus=PASS")),
                "standalone support bundle diagnostics should report a clean AdapterCore target diff");
        require(bundle.diagnostics().stream().anyMatch(line -> line.equals("moduleLifecycleStatus=PASS")),
                "standalone support bundle diagnostics should report passing descriptor-only module lifecycle evidence");
        require(bundle.diagnostics().stream().anyMatch(line -> line.startsWith("moduleLifecycleDescriptorCount=")),
                "standalone support bundle diagnostics should include descriptor-only module lifecycle counts");
        require(bundle.diagnostics().stream().noneMatch(line -> line.startsWith("nativeLoaderAbi")),
                "OpenGL support bundle diagnostics should not advertise Native Loader ABI evidence");
        require(Files.readString(workspaceRoot.resolve("build/support/standalone-mod-diagnostics.json"))
                        .contains("\"schema\": \"echo.standalone.support.mod_diagnostics.v1\""),
                "generated mod diagnostics should be a schema-tagged JSON artifact");
        require(Files.readString(workspaceRoot.resolve("build/support/standalone-mod-diagnostics.json"))
                        .contains("\"moduleLifecycle\""),
                "generated mod diagnostics should include the descriptor-only module lifecycle summary");
        require(!Files.readString(workspaceRoot.resolve("build/support/standalone-mod-diagnostics.json"))
                        .contains("\"nativeLoaderAbi\""),
                "generated OpenGL mod diagnostics should not include a Native Loader ABI summary");
        require(Files.readString(workspaceRoot.resolve("build/support/standalone-runtime-registry-fingerprint.json"))
                        .contains("\"runtimeRegistryFingerprint\""),
                "generated registry fingerprint artifact should expose the runtime registry fingerprint");
        require(Files.readString(workspaceRoot.resolve("build/support/standalone-adaptercore-target-diff.json"))
                        .contains("\"status\": \"PASS\""),
                "generated AdapterCore target diff artifact should pass for the current workspace");
        String moduleLifecycleTrace = Files.readString(
                workspaceRoot.resolve("build/support/standalone-module-lifecycle-traces.json"));
        require(moduleLifecycleTrace.contains("\"schema\": \"echo.standalone.support.module_lifecycle_traces.v1\""),
                "generated module lifecycle traces should be a schema-tagged JSON artifact");
        require(moduleLifecycleTrace.contains("\"status\": \"PASS\"")
                        && moduleLifecycleTrace.contains("\"descriptorCount\""),
                "generated module lifecycle traces should pass descriptor-only module graph diagnostics");
        require(!moduleLifecycleTrace.contains("nativeLoaderAbi"),
                "generated OpenGL module lifecycle traces should not reference Native Loader ABI evidence");
        require(bundle.complete(),
                "support bundle should export a manifest and zip archive for tester diagnostics");
        require(standalone.launched(),
                "standalone mode should launch the headless runtime");
        EchoRuntimeBootResult launch = standalone.launchResult().orElseThrow();
        require(launch.success() && launch.ticksRun() == 3,
                "headless launch should succeed and run three ticks");

        EchoStandaloneLauncherResult platformHandoff = launcher.run(
                new EchoDefaultRuntimeServiceRegistry(),
                EchoStandaloneLauncherRequest.platformHandoff(workspaceRoot)
        );
        require(!platformHandoff.launched() && platformHandoff.handoffPreserved(),
                "platform handoff should not launch standalone runtime");
        require(platformHandoff.supportBundle().isEmpty(),
                "handoff mode should not overwrite the standalone OpenGL support bundle");
        require(platformHandoff.handoffPlan().externalCommandPreserved()
                        && platformHandoff.handoffPlan().externalCommand().equals("external platform launcher"),
                "platform handoff should preserve an external launcher command");
        require(platformHandoff.verification().ready(),
                "handoff mode should still verify the standalone workspace");

        Path brokenRoot = Files.createTempDirectory("echo-runtime-launcher-missing");
        EchoStandaloneLauncherResult broken = launcher.run(
                new EchoDefaultRuntimeServiceRegistry(),
                EchoStandaloneLauncherRequest.standalone(brokenRoot)
        );
        require(!broken.verification().ready(),
                "missing workspace should fail launcher verification");
        require(!broken.launched(),
                "missing workspace must not launch");
        require(broken.repairPlan().planningOnly() && broken.repairPlan().actionCount() > 0,
                "missing workspace should produce planning-only repair actions");

        writeLauncherReports(workspaceRoot, standalone, platformHandoff, broken, launch, bundle);
        writeSmokeReport(workspaceRoot, standalone, platformHandoff, broken, launch, bundle);
        EchoStandaloneLauncherResult finalBundlePass = launcher.run(
                new EchoDefaultRuntimeServiceRegistry(),
                EchoStandaloneLauncherRequest.verifyOnly(workspaceRoot)
        );
        EchoStandaloneSupportBundle finalBundle = finalBundlePass.supportBundle().orElseThrow();
        require(finalBundle.complete(),
                "final support bundle pass should capture concrete launcher evidence reports");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/runtime-launcher.json",
                "echo.standalone.runtime_launcher.v2");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/launcher-detection.json",
                "echo.standalone.launcher_detection.v2");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/launcher-verification.json",
                "echo.standalone.launcher_verification.v2");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/launcher-repair-plan.json",
                "echo.standalone.launcher_repair_plan.v2");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/launcher-support-bundle.json",
                "echo.standalone.launcher_support_bundle.v2");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/launcher-handoff.json",
                "echo.standalone.launcher_handoff.v2");
        requireConcreteArchiveReport(workspaceRoot, finalBundle,
                "reports/echo/standalone/launcher-smoke.json",
                "echo.standalone.launcher_smoke.v2");
        bundle = finalBundle;
        writeLauncherReports(workspaceRoot, standalone, platformHandoff, broken, launch, bundle);
        writeSmokeReport(workspaceRoot, standalone, platformHandoff, broken, launch, bundle);

        System.out.println("phase14.19 launcher smoke PASS detected="
                + standalone.detection().standaloneWorkspace()
                + " ready="
                + standalone.verification().ready()
                + " launched="
                + standalone.launched()
                + " ticks="
                + launch.ticksRun()
                + " bundle="
                + bundle.presentEntryCount()
                + " archiveBytes="
                + bundle.archiveByteSize()
                + " repairActions="
                + standalone.repairPlan().actionCount()
                + " renderer="
                + standalone.handoffPlan().rendererTarget()
                + " handoffs=2");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireConcreteArchiveReport(
            Path workspaceRoot,
            EchoStandaloneSupportBundle bundle,
            String relativePath,
            String expectedSchema
    ) throws IOException {
        Path archive = workspaceRoot.resolve(bundle.archivePath()).normalize();
        require(archive.startsWith(workspaceRoot) && Files.isRegularFile(archive),
                "support bundle archive should exist inside the standalone workspace");
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            ZipEntry entry = zip.getEntry(relativePath.replace('\\', '/'));
            require(entry != null, "support bundle archive should contain " + relativePath);
            String text = new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
            require(text.contains("\"schema\": \"" + expectedSchema + "\""),
                    "support bundle archive should contain concrete schema " + expectedSchema);
            require(!text.contains("\"schema\": \"echo.standalone.evidence.bootstrap.v1\""),
                    "support bundle archive must not contain bootstrap launcher evidence");
        }
    }

    private static void writeLauncherReports(
            Path workspaceRoot,
            EchoStandaloneLauncherResult standalone,
            EchoStandaloneLauncherResult platformHandoff,
            EchoStandaloneLauncherResult broken,
            EchoRuntimeBootResult launch,
            EchoStandaloneSupportBundle bundle
    ) throws IOException {
        EchoStandaloneLauncherDetection detection = standalone.detection();
        EchoStandaloneLauncherVerification verification = standalone.verification();
        EchoStandaloneLauncherRepairPlan repairPlan = standalone.repairPlan();
        EchoStandaloneLauncherHandoffPlan handoffPlan = standalone.handoffPlan();

        writeReport(workspaceRoot, "reports/echo/standalone/runtime-launcher.json", "{\n"
                + "  \"schema\": \"echo.standalone.runtime_launcher.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"rendererTarget\": \"" + escape(handoffPlan.rendererTarget()) + "\",\n"
                + "  \"nativeModLoaderCommandUsed\": false,\n"
                + "  \"ready\": " + standalone.ready() + ",\n"
                + "  \"launched\": " + standalone.launched() + ",\n"
                + "  \"launchSuccess\": " + launch.success() + ",\n"
                + "  \"launchTicksRun\": " + launch.ticksRun() + ",\n"
                + "  \"finalLifecycle\": \"" + escape(launch.finalLifecycle().name()) + "\",\n"
                + "  \"diagnosticCount\": " + launch.diagnostics().size() + ",\n"
                + "  \"crashHandled\": " + launch.crashHandled() + ",\n"
                + "  \"supportBundleComplete\": " + bundle.complete() + ",\n"
                + "  \"platformHandoffPreserved\": " + platformHandoff.handoffPreserved() + ",\n"
                + "  \"brokenWorkspaceFailsClosed\": " + (!broken.verification().ready() && !broken.launched()) + "\n"
                + "}\n");

        writeReport(workspaceRoot, "reports/echo/standalone/launcher-detection.json", "{\n"
                + "  \"schema\": \"echo.standalone.launcher_detection.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"workspaceRoot\": \"" + escape(detection.workspaceRoot().toString()) + "\",\n"
                + "  \"standaloneWorkspace\": " + detection.standaloneWorkspace() + ",\n"
                + "  \"settingsFilePresent\": " + detection.settingsFilePresent() + ",\n"
                + "  \"buildFilePresent\": " + detection.buildFilePresent() + ",\n"
                + "  \"docsRootPresent\": " + detection.docsRootPresent() + ",\n"
                + "  \"reportsRootPresent\": " + detection.reportsRootPresent() + ",\n"
                + "  \"runtimeVersion\": \"" + escape(detection.runtimeVersion()) + "\",\n"
                + "  \"brokenWorkspaceDetected\": " + broken.detection().standaloneWorkspace() + "\n"
                + "}\n");

        writeReport(workspaceRoot, "reports/echo/standalone/launcher-verification.json", "{\n"
                + "  \"schema\": \"echo.standalone.launcher_verification.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"ready\": " + verification.ready() + ",\n"
                + "  \"passedCount\": " + verification.passedCount() + ",\n"
                + "  \"failedCount\": " + verification.failedCount() + ",\n"
                + "  \"checks\": " + verificationChecksArray(verification) + ",\n"
                + "  \"brokenWorkspaceReady\": " + broken.verification().ready() + ",\n"
                + "  \"brokenWorkspaceFailedCount\": " + broken.verification().failedCount() + "\n"
                + "}\n");

        writeReport(workspaceRoot, "reports/echo/standalone/launcher-repair-plan.json", "{\n"
                + "  \"schema\": \"echo.standalone.launcher_repair_plan.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"planId\": \"" + escape(repairPlan.planId()) + "\",\n"
                + "  \"planningOnly\": " + repairPlan.planningOnly() + ",\n"
                + "  \"actionCount\": " + repairPlan.actionCount() + ",\n"
                + "  \"actions\": " + stringArray(repairPlan.actions()) + ",\n"
                + "  \"brokenWorkspacePlanId\": \"" + escape(broken.repairPlan().planId()) + "\",\n"
                + "  \"brokenWorkspacePlanningOnly\": " + broken.repairPlan().planningOnly() + ",\n"
                + "  \"brokenWorkspaceActionCount\": " + broken.repairPlan().actionCount() + ",\n"
                + "  \"brokenWorkspaceActions\": " + stringArray(broken.repairPlan().actions()) + "\n"
                + "}\n");

        writeReport(workspaceRoot, "reports/echo/standalone/launcher-handoff.json", "{\n"
                + "  \"schema\": \"echo.standalone.launcher_handoff.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"modeId\": \"" + escape(handoffPlan.modeId()) + "\",\n"
                + "  \"runtimeFamily\": \"" + escape(handoffPlan.runtimeFamily()) + "\",\n"
                + "  \"rendererTarget\": \"" + escape(handoffPlan.rendererTarget()) + "\",\n"
                + "  \"standaloneLaunchTask\": \"" + escape(handoffPlan.standaloneLaunchTask()) + "\",\n"
                + "  \"externalCommand\": \"" + escape(handoffPlan.externalCommand()) + "\",\n"
                + "  \"launchesStandalone\": " + handoffPlan.launchesStandalone() + ",\n"
                + "  \"standaloneOpenGlClientTarget\": " + handoffPlan.standaloneOpenGlClientTarget() + ",\n"
                + "  \"platformHandoffModeId\": \"" + escape(platformHandoff.handoffPlan().modeId()) + "\",\n"
                + "  \"platformHandoffLaunched\": " + platformHandoff.launched() + ",\n"
                + "  \"platformHandoffPreserved\": " + platformHandoff.handoffPreserved() + ",\n"
                + "  \"platformExternalCommandPreserved\": " + platformHandoff.handoffPlan().externalCommandPreserved() + ",\n"
                + "  \"platformExternalCommand\": \"" + escape(platformHandoff.handoffPlan().externalCommand()) + "\"\n"
                + "}\n");

        writeReport(workspaceRoot, "reports/echo/standalone/launcher-support-bundle.json", "{\n"
                + "  \"schema\": \"echo.standalone.launcher_support_bundle.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"bundleId\": \"" + escape(bundle.bundleId()) + "\",\n"
                + "  \"complete\": " + bundle.complete() + ",\n"
                + "  \"entryCount\": " + bundle.entries().size() + ",\n"
                + "  \"presentEntryCount\": " + bundle.presentEntryCount() + ",\n"
                + "  \"manifestPath\": \"" + escape(bundle.manifestPath()) + "\",\n"
                + "  \"manifestPresent\": " + bundle.manifestPresent() + ",\n"
                + "  \"manifestEntryCount\": " + bundle.manifestEntryCount() + ",\n"
                + "  \"archivePath\": \"" + escape(bundle.archivePath()) + "\",\n"
                + "  \"archivePresent\": " + bundle.archivePresent() + ",\n"
                + "  \"archiveByteSize\": " + bundle.archiveByteSize() + ",\n"
                + "  \"entries\": " + bundleEntriesArray(bundle) + ",\n"
                + "  \"diagnostics\": " + stringArray(bundle.diagnostics()) + "\n"
                + "}\n");
    }

    private static void writeSmokeReport(
            Path workspaceRoot,
            EchoStandaloneLauncherResult standalone,
            EchoStandaloneLauncherResult platformHandoff,
            EchoStandaloneLauncherResult broken,
            EchoRuntimeBootResult launch,
            EchoStandaloneSupportBundle bundle
    ) throws IOException {
        Path report = workspaceRoot.resolve("reports/echo/standalone/launcher-smoke.json");
        Files.createDirectories(report.getParent());
        List<String> requiredEntries = List.of(
                "docs/echo/standalone/FINAL_TESTER_SCRIPT.md",
                "scripts/sign-release-artifacts.ps1",
                "scripts/capture-clean-install-uninstall-evidence.ps1",
                "scripts/capture-manual-wallclock-playtest.ps1",
                "docs/echo/standalone/BETA_RELEASE_READINESS.md",
                "reports/echo/standalone/beta-readiness-gate.json",
                "reports/echo/standalone/beta-readiness-checks.json",
                "reports/echo/standalone/beta-readiness-playable-qa.json",
                "reports/echo/standalone/manual-playtest-report.json",
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
                "reports/echo/standalone/distribution-artifacts.json",
                "reports/echo/standalone/distribution-install-uninstall-evidence.json",
                "reports/echo/standalone/distribution-signing-setup.json",
                "reports/echo/standalone/packaged-opengl-client-image.json",
                "reports/echo/standalone/packaged-exe-wallclock-smoke.json",
                "reports/echo/standalone/packaged-exe-wallclock-strict-30m.json",
                "reports/echo/standalone/packaged-exe-wallclock-strict-60m.json",
                "reports/echo/standalone/packaged-exe-wallclock-strict-rehearsal.json",
                "reports/echo/standalone/beta-readiness-support-bundle.json"
        );
        String json = "{\n"
                + "  \"schema\": \"echo.standalone.launcher_smoke.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeLauncherSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"rendererTarget\": \"" + escape(standalone.handoffPlan().rendererTarget()) + "\",\n"
                + "  \"nativeModLoaderCommandUsed\": false,\n"
                + "  \"detectedStandaloneWorkspace\": " + standalone.detection().standaloneWorkspace() + ",\n"
                + "  \"verificationReady\": " + standalone.verification().ready() + ",\n"
                + "  \"verificationPassedCount\": " + standalone.verification().passedCount() + ",\n"
                + "  \"verificationFailedCount\": " + standalone.verification().failedCount() + ",\n"
                + "  \"repairPlanningOnly\": " + standalone.repairPlan().planningOnly() + ",\n"
                + "  \"repairActionCount\": " + standalone.repairPlan().actionCount() + ",\n"
                + "  \"standaloneOpenGlClientTarget\": " + standalone.handoffPlan().standaloneOpenGlClientTarget() + ",\n"
                + "  \"launcherLaunched\": " + standalone.launched() + ",\n"
                + "  \"launchSuccess\": " + launch.success() + ",\n"
                + "  \"launchTicksRun\": " + launch.ticksRun() + ",\n"
                + "  \"supportBundleComplete\": " + bundle.complete() + ",\n"
                + "  \"supportBundleEntryCount\": " + bundle.entries().size() + ",\n"
                + "  \"supportBundlePresentEntryCount\": " + bundle.presentEntryCount() + ",\n"
                + "  \"supportBundleManifestPresent\": " + bundle.manifestPresent() + ",\n"
                + "  \"supportBundleManifestEntryCount\": " + bundle.manifestEntryCount() + ",\n"
                + "  \"supportBundleArchivePresent\": " + bundle.archivePresent() + ",\n"
                + "  \"supportBundleArchiveBytes\": " + bundle.archiveByteSize() + ",\n"
                + "  \"requiredSupportBundleEntries\": " + stringArray(requiredEntries) + ",\n"
                + "  \"platformHandoffLaunched\": " + platformHandoff.launched() + ",\n"
                + "  \"platformHandoffPreserved\": " + platformHandoff.handoffPreserved() + ",\n"
                + "  \"platformExternalCommandPreserved\": " + platformHandoff.handoffPlan().externalCommandPreserved() + ",\n"
                + "  \"platformExternalCommand\": \"" + escape(platformHandoff.handoffPlan().externalCommand()) + "\",\n"
                + "  \"brokenWorkspaceVerificationReady\": " + broken.verification().ready() + ",\n"
                + "  \"brokenWorkspaceLaunched\": " + broken.launched() + ",\n"
                + "  \"brokenWorkspaceRepairPlanningOnly\": " + broken.repairPlan().planningOnly() + ",\n"
                + "  \"brokenWorkspaceRepairActionCount\": " + broken.repairPlan().actionCount() + ",\n"
                + "  \"diagnostics\": " + stringArray(bundle.diagnostics()) + "\n"
                + "}\n";
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static void writeReport(Path workspaceRoot, String relativePath, String json) throws IOException {
        Path report = workspaceRoot.resolve(relativePath);
        Files.createDirectories(report.getParent());
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static String verificationChecksArray(EchoStandaloneLauncherVerification verification) {
        return verification.checks().stream()
                .map(check -> "{"
                        + "\"checkId\":\"" + escape(check.checkId()) + "\","
                        + "\"passed\":" + check.passed() + ","
                        + "\"detail\":\"" + escape(check.detail()) + "\""
                        + "}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String bundleEntriesArray(EchoStandaloneSupportBundle bundle) {
        return bundle.entries().stream()
                .map(entry -> "{"
                        + "\"relativePath\":\"" + escape(entry.relativePath()) + "\","
                        + "\"present\":" + entry.present() + ","
                        + "\"byteSize\":" + entry.byteSize()
                        + "}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String stringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
