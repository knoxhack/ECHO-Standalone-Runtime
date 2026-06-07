package dev.echo.standalone.runtime.client;

import java.util.List;
import java.util.Map;

record EchoClientModScanSummary(
        List<EchoClientModSummary> modules,
        List<String> roots,
        Map<String, Integer> statusCounts,
        int graphIssueCount,
        int errorCount,
        int warningCount,
        String lastError
) {
    EchoClientModScanSummary {
        modules = modules == null ? List.of() : List.copyOf(modules);
        roots = roots == null ? List.of() : List.copyOf(roots);
        statusCounts = statusCounts == null ? Map.of() : Map.copyOf(statusCounts);
        lastError = lastError == null ? "" : lastError.trim();
    }

    static EchoClientModScanSummary empty() {
        return new EchoClientModScanSummary(List.of(), List.of(), Map.of(), 0, 0, 0, "");
    }

    int descriptorCount() {
        return modules.size();
    }

    int activeCount() {
        return statusCounts.getOrDefault("runtime-active", 0);
    }

    int nativeEntrypointCount() {
        return (int) modules.stream().filter(EchoClientModSummary::nativeEntrypointDeclared).count();
    }

    int adapterCoreDeclaredCount() {
        return (int) modules.stream().filter(EchoClientModSummary::adapterCoreDeclared).count();
    }

    boolean healthy() {
        return lastError.isBlank() && errorCount == 0;
    }

    String summaryLabel() {
        return descriptorCount() + " descriptor(s), "
                + activeCount() + " active, "
                + nativeEntrypointCount() + " native entrypoint(s)";
    }

    String issueLabel() {
        if (healthy() && warningCount == 0) {
            return "No module scan issues";
        }
        return errorCount + " error(s), " + warningCount + " warning(s)"
                + (lastError.isBlank() ? "" : " | " + lastError);
    }
}
