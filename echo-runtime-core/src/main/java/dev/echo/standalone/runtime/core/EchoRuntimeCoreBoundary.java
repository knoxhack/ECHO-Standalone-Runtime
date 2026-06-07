package dev.echo.standalone.runtime.core;

import java.util.List;

public final class EchoRuntimeCoreBoundary {
    private static final List<String> RESPONSIBILITIES = List.of(
            "owns runtime lifecycle orchestration",
            "owns service registry composition",
            "owns diagnostics fan-out",
            "does not own Minecraft or NeoForge adapters"
    );

    private EchoRuntimeCoreBoundary() {
    }

    public static List<String> responsibilities() {
        return RESPONSIBILITIES;
    }
}
