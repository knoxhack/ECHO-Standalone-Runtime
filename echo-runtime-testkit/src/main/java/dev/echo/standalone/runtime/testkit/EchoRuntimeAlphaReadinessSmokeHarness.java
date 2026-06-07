package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandaloneAlphaReadinessGate;
import dev.echo.standalone.runtime.app.EchoStandaloneAlphaReadinessResult;
import dev.echo.standalone.runtime.app.EchoStandaloneAlphaReadinessStatus;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimeAlphaReadinessSmokeHarness {
    private EchoRuntimeAlphaReadinessSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
        EchoStandaloneAlphaReadinessGate gate = new EchoStandaloneAlphaReadinessGate();
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoStandaloneAlphaReadinessResult ready = gate.evaluate(services, workspaceRoot);

        require(services.require(EchoStandaloneAlphaReadinessResult.class) == ready,
                "alpha readiness result should be service-bound");
        require(ready.status() == EchoStandaloneAlphaReadinessStatus.READY,
                "current workspace should pass alpha readiness");
        require(ready.ready(),
                "ready helper should report true");
        require(ready.blockedCount() == 0,
                "ready workspace should have no blocking failures");
        require(ready.checkCount() == 48,
                "alpha readiness gate should evaluate forty-eight checks");
        require(ready.passedCount() == ready.checkCount(),
                "all readiness checks should pass");
        require(ready.launcherResult().verification().ready(),
                "launcher verification should pass inside readiness gate");
        require(!ready.launcherResult().launched(),
                "readiness gate should use launcher verify-only mode");
        require(ready.supportBundleReady(),
                "launcher support bundle should be ready");
        require(ready.checks().stream().anyMatch(check -> check.checkId()
                        .equals("docs.docs/echo/standalone/ECHO_STANDALONE_ALPHA_READINESS.md")),
                "alpha readiness documentation should be checked");
        require(ready.checks().stream().anyMatch(check -> check.checkId()
                        .equals("reports.reports/echo/standalone/runtime-alpha-readiness.json")),
                "alpha readiness report should be checked");

        Path brokenRoot = Files.createTempDirectory("echo-runtime-alpha-readiness-missing");
        EchoStandaloneAlphaReadinessResult blocked = gate.evaluate(
                new EchoDefaultRuntimeServiceRegistry(),
                brokenRoot
        );
        require(blocked.status() == EchoStandaloneAlphaReadinessStatus.BLOCKED,
                "missing workspace should block alpha readiness");
        require(blocked.blockedCount() > 0,
                "missing workspace should report blockers");
        require(!blocked.supportBundleReady(),
                "missing workspace should not have a ready support bundle");

        System.out.println("phase14.20 alpha readiness smoke PASS status="
                + ready.status().name()
                + " checks="
                + ready.checkCount()
                + " blocked="
                + ready.blockedCount()
                + " supportBundle="
                + ready.supportBundleReady());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
