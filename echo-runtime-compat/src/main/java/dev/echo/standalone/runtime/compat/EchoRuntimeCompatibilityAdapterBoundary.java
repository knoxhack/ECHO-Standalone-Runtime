package dev.echo.standalone.runtime.compat;

import java.util.List;

public final class EchoRuntimeCompatibilityAdapterBoundary {
    private static final List<String> ADAPTER_RULES = List.of(
            "adapters depend on standalone contracts",
            "standalone contracts do not depend on adapters",
            "Minecraft and NeoForge bridges stay out of runtime contracts",
            "AdapterCore content ids remain canonical across NeoForge, ECHO Native Loader, and Standalone",
            "standalone live client targets OpenGL through the active renderer path",
            "migration tooling plans before it mutates player data"
    );

    public EchoRuntimeCompatibilityAdapterBoundary() {
    }

    public static List<String> adapterRules() {
        return ADAPTER_RULES;
    }
}
