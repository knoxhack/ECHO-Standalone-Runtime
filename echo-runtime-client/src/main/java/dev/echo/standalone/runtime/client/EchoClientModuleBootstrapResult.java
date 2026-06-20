package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoRuntimeDiagnosticCollector;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

final class EchoClientModuleBootstrapResult implements AutoCloseable {
    private static final EchoClientModuleBootstrapResult INACTIVE =
            new EchoClientModuleBootstrapResult(
                    false,
                    false,
                    false,
                    "",
                    null,
                    null,
                    null,
                    null,
                    EchoClientModScanSummary.empty(),
                    List.of(),
                    List.of()
            );

    private final boolean active;
    private final boolean strictPackMode;
    private final boolean safeMode;
    private final String failure;
    private final EchoRuntimeModuleManager manager;
    private final EchoDefaultRuntimeServiceRegistry services;
    private final EchoRuntimeModuleRuntimeResult moduleRuntimeResult;
    private final EchoRuntimeDiagnosticCollector diagnostics;
    private final EchoClientModScanSummary modScanSummary;
    private final List<Path> moduleRoots;
    private final List<Map<String, Object>> adapterCoreContentRows;
    private boolean closed;

    private EchoClientModuleBootstrapResult(
            boolean active,
            boolean strictPackMode,
            boolean safeMode,
            String failure,
            EchoRuntimeModuleManager manager,
            EchoDefaultRuntimeServiceRegistry services,
            EchoRuntimeModuleRuntimeResult moduleRuntimeResult,
            EchoRuntimeDiagnosticCollector diagnostics,
            EchoClientModScanSummary modScanSummary,
            List<Path> moduleRoots,
            List<Map<String, Object>> adapterCoreContentRows
    ) {
        this.active = active;
        this.strictPackMode = strictPackMode;
        this.safeMode = safeMode;
        this.failure = failure == null ? "" : failure.trim();
        this.manager = manager;
        this.services = services;
        this.moduleRuntimeResult = moduleRuntimeResult;
        this.diagnostics = diagnostics;
        this.modScanSummary = modScanSummary == null ? EchoClientModScanSummary.empty() : modScanSummary;
        this.moduleRoots = moduleRoots == null ? List.of() : List.copyOf(moduleRoots);
        this.adapterCoreContentRows = adapterCoreContentRows == null ? List.of() : List.copyOf(adapterCoreContentRows);
    }

    static EchoClientModuleBootstrapResult inactive() {
        return INACTIVE;
    }

    static EchoClientModuleBootstrapResult inactive(String failure, boolean strictPackMode, boolean safeMode) {
        String cleanFailure = failure == null ? "" : failure.trim();
        return new EchoClientModuleBootstrapResult(
                false,
                strictPackMode,
                safeMode,
                cleanFailure,
                null,
                null,
                null,
                null,
                new EchoClientModScanSummary(
                        List.of(),
                        List.of(),
                        Map.of(),
                        0,
                        cleanFailure.isBlank() ? 0 : 1,
                        0,
                        cleanFailure
                ),
                List.of(),
                List.of()
        );
    }

    static EchoClientModuleBootstrapResult active(
            boolean strictPackMode,
            boolean safeMode,
            EchoRuntimeModuleManager manager,
            EchoDefaultRuntimeServiceRegistry services,
            EchoRuntimeModuleRuntimeResult moduleRuntimeResult,
            EchoRuntimeDiagnosticCollector diagnostics,
            EchoClientModScanSummary modScanSummary,
            List<Path> moduleRoots,
            List<Map<String, Object>> adapterCoreContentRows
    ) {
        return new EchoClientModuleBootstrapResult(
                true,
                strictPackMode,
                safeMode,
                "",
                manager,
                services,
                moduleRuntimeResult,
                diagnostics,
                modScanSummary,
                moduleRoots,
                adapterCoreContentRows
        );
    }

    boolean active() {
        return active;
    }

    boolean strictPackMode() {
        return strictPackMode;
    }

    boolean safeMode() {
        return safeMode;
    }

    String failure() {
        return failure;
    }

    EchoRuntimeModuleRuntimeResult moduleRuntimeResult() {
        return moduleRuntimeResult;
    }

    EchoClientModScanSummary modScanSummary() {
        return modScanSummary;
    }

    List<Path> moduleRoots() {
        return moduleRoots;
    }

    List<Map<String, Object>> adapterCoreContentRows() {
        return adapterCoreContentRows;
    }

    EchoRuntimeDiagnosticCollector diagnostics() {
        return diagnostics;
    }

    @Override
    public void close() {
        if (closed || !active || manager == null || services == null || moduleRuntimeResult == null) {
            return;
        }
        closed = true;
        manager.unload(moduleRuntimeResult, services);
    }
}
