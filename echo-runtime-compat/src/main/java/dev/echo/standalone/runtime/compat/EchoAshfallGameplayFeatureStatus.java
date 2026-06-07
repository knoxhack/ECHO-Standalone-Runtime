package dev.echo.standalone.runtime.compat;

public enum EchoAshfallGameplayFeatureStatus {
    ADAPTERCORE_BACKED("AdapterCore-backed"),
    DATA_DRIVEN_SHARED("data-driven shared"),
    NEOFORGE_ONLY("NeoForge-only"),
    STANDALONE_ONLY("standalone-only"),
    MISSING_RUNTIME("missing runtime");

    private final String label;

    EchoAshfallGameplayFeatureStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
