package dev.echo.standalone.runtime.compat;

public enum EchoAshfallGameplayFeatureKind {
    BLOCKS("blocks"),
    ITEMS("items"),
    TOOLS("tools"),
    FLUIDS_CONSUMABLES("fluids_consumables"),
    HAZARDS("hazards"),
    WORLD_REGIONS("world_regions"),
    MISSIONS("missions"),
    TERMINALS("terminals"),
    POWER_REPAIR("power_repair"),
    LOOT_SCAVENGING("loot_scavenging"),
    SHELTER_LOGIC("shelter_logic"),
    EXTRACTION_LOGIC("extraction_logic"),
    SAVES_PROGRESSION("saves_progression"),
    BLOCK_INTERACTION("block_interaction"),
    SURVIVAL_STATS("survival_stats");

    private final String id;

    EchoAshfallGameplayFeatureKind(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
