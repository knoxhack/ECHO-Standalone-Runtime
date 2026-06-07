package dev.echo.standalone.runtime.client;

import java.util.List;

record EchoClientModSummary(
        String id,
        String name,
        String version,
        String kind,
        String side,
        boolean standalone,
        boolean official,
        boolean nativeEntrypointDeclared,
        boolean adapterCoreDeclared,
        String runtimeStatus,
        String descriptorPath,
        int requiredCount,
        int optionalCount,
        List<String> adapterCoreDomains,
        List<String> adapterCoreRuntimes,
        String statusReason
) {
    EchoClientModSummary {
        id = text(id, "unknown-module");
        name = text(name, id);
        version = text(version, "0.0.0");
        kind = text(kind, "runtime_module");
        side = text(side, "both");
        runtimeStatus = text(runtimeStatus, "runtime-disabled-with-reason");
        descriptorPath = text(descriptorPath, "");
        adapterCoreDomains = adapterCoreDomains == null ? List.of() : List.copyOf(adapterCoreDomains);
        adapterCoreRuntimes = adapterCoreRuntimes == null ? List.of() : List.copyOf(adapterCoreRuntimes);
        statusReason = text(statusReason, runtimeStatus);
    }

    String menuLabel() {
        String flags = (standalone ? "standalone" : "external")
                + (nativeEntrypointDeclared ? ", native" : "")
                + (adapterCoreDeclared ? ", adaptercore" : "");
        return "Mod " + id + " - " + runtimeStatus + " (" + flags + ")";
    }

    String detailLabel() {
        return name + " " + version
                + " | " + kind + "/" + side
                + " | requires " + requiredCount
                + ", optional " + optionalCount
                + (adapterCoreDomains.isEmpty() ? "" : " | domains " + String.join(",", adapterCoreDomains));
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
