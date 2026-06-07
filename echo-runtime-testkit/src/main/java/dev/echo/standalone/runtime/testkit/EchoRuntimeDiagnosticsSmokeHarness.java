package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoRuntimeDiagnosticCollector;

import java.util.Map;

public final class EchoRuntimeDiagnosticsSmokeHarness {
    private EchoRuntimeDiagnosticsSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeDiagnosticCollector diagnostics = new EchoRuntimeDiagnosticCollector();
        services.register(EchoRuntimeDiagnosticCollector.class, diagnostics);
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        EchoRuntimeDiagnosticSink sink = services.require(EchoRuntimeDiagnosticSink.class);
        sink.info("echo.runtime.boot.ready", "app_runtime", "boot ready");
        sink.warning("echo.runtime.asset.missing_optional", "asset_runtime", "optional asset missing");
        sink.emit(new EchoRuntimeDiagnostic(
                "echo.runtime.module.execution_failed",
                EchoRuntimeDiagnosticSeverity.FATAL,
                "module_runtime",
                "module execution failed",
                "fixture fatal diagnostic",
                Map.of("moduleId", "echo.diagnostics.fixture")
        ));

        require(services.require(EchoRuntimeDiagnosticCollector.class) == diagnostics,
                "diagnostic collector should be service-bound");
        require(diagnostics.count() == 3, "collector should contain three diagnostics");
        require(diagnostics.count(EchoRuntimeDiagnosticSeverity.INFO) == 1, "collector should count info diagnostics");
        require(diagnostics.count(EchoRuntimeDiagnosticSeverity.WARNING) == 1, "collector should count warning diagnostics");
        require(diagnostics.count(EchoRuntimeDiagnosticSeverity.FATAL) == 1, "collector should count fatal diagnostics");
        require(diagnostics.countsByLayer().get("module_runtime") == 1L,
                "collector should aggregate diagnostics by layer");
        require(diagnostics.countsByCode().get("echo.runtime.module.execution_failed") == 1L,
                "collector should aggregate diagnostics by code");
        require(diagnostics.byLayer("module_runtime").getFirst().attributes().get("moduleId").equals("echo.diagnostics.fixture"),
                "collector should preserve diagnostic attributes");
        require(diagnostics.hasFatal(), "collector should detect fatal diagnostics");

        System.out.println("runtime diagnostics smoke PASS diagnostics="
                + diagnostics.count()
                + " layers="
                + diagnostics.countsByLayer().size()
                + " fatal="
                + diagnostics.hasFatal());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
