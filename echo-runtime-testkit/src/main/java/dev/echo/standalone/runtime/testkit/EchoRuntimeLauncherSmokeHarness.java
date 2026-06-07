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
import java.nio.file.Files;
import java.nio.file.Path;

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

        System.out.println("phase14.19 launcher smoke PASS detected="
                + standalone.detection().standaloneWorkspace()
                + " ready="
                + standalone.verification().ready()
                + " launched="
                + standalone.launched()
                + " ticks="
                + launch.ticksRun()
                + " bundle="
                + standalone.supportBundle().orElseThrow().presentEntryCount()
                + " archiveBytes="
                + standalone.supportBundle().orElseThrow().archiveByteSize()
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
}
