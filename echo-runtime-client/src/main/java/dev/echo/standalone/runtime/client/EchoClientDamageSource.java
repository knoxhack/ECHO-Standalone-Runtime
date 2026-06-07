package dev.echo.standalone.runtime.client;

record EchoClientDamageSource(
        String id,
        String label,
        boolean bypassesArmor,
        boolean bypassesGameMode,
        double exhaustion
) {
    EchoClientDamageSource {
        id = id == null || id.isBlank() ? "echo:generic" : id.trim();
        label = label == null || label.isBlank() ? "Generic" : label.trim();
        exhaustion = Math.max(0.0D, exhaustion);
    }

    static EchoClientDamageSource none() {
        return new EchoClientDamageSource("echo:none", "None", true, false, 0.0D);
    }

    static EchoClientDamageSource generic() {
        return new EchoClientDamageSource("echo:generic", "Generic", false, false, 0.1D);
    }

    static EchoClientDamageSource hostile(String entityId) {
        String normalized = entityId == null || entityId.isBlank() ? "hostile" : entityId.trim();
        return new EchoClientDamageSource("echo:hostile/" + normalized, "Hostile", false, false, 0.1D);
    }

    static EchoClientDamageSource starvation() {
        return new EchoClientDamageSource("minecraft:starve", "Starvation", true, false, 0.0D);
    }

    static EchoClientDamageSource hazard(String hazardId, String label) {
        String normalized = hazardId == null || hazardId.isBlank() ? "hazard" : hazardId.trim();
        String safeLabel = label == null || label.isBlank() ? "Hazard" : label.trim();
        return new EchoClientDamageSource("echo:hazard/" + normalized, safeLabel, true, false, 0.05D);
    }

    static EchoClientDamageSource parse(String id, String label, boolean bypassesArmor, boolean bypassesGameMode) {
        return new EchoClientDamageSource(id, label, bypassesArmor, bypassesGameMode, 0.0D);
    }
}
