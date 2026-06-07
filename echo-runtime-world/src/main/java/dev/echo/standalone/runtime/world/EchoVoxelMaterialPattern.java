package dev.echo.standalone.runtime.world;

import java.util.Locale;

public enum EchoVoxelMaterialPattern {
    FLAT,
    ASH_GRAIN,
    BASALT_CRACKS,
    RUST_PATCHES,
    TERMINAL_GRID,
    CACHE_PANEL,
    POWER_NODE,
    HAZARD_STRIPES,
    MARKER_GRID,
    WATER_RATION,
    ORE_VEIN,
    TOXIC_PUDDLE,
    WASTELAND_GRASS,
    RUBBLE_PILE,
    TWISTED_METAL,
    BERRY_BUSH;

    public static EchoVoxelMaterialPattern infer(String id) {
        if (id == null || id.isBlank()) {
            return FLAT;
        }
        String normalized = id.toLowerCase(Locale.ROOT);
        if (normalized.contains("toxic_ash") || normalized.contains("ash_block")) {
            return ASH_GRAIN;
        }
        if (normalized.contains("basalt")) {
            return BASALT_CRACKS;
        }
        if (normalized.contains("rust") || normalized.contains("debris")) {
            return RUST_PATCHES;
        }
        if (normalized.contains("terminal")) {
            return TERMINAL_GRID;
        }
        if (normalized.contains("cache") || normalized.contains("crate")) {
            return CACHE_PANEL;
        }
        if (normalized.contains("power")) {
            return POWER_NODE;
        }
        if (normalized.contains("hazard")) {
            return HAZARD_STRIPES;
        }
        if (normalized.contains("marker")) {
            return MARKER_GRID;
        }
        if (normalized.contains("water") || normalized.contains("puddle")) {
            return WATER_RATION;
        }
        if (normalized.contains("ore") || normalized.contains("scrap_ore")) {
            return ORE_VEIN;
        }
        if (normalized.contains("grass") || normalized.contains("berry")) {
            return WASTELAND_GRASS;
        }
        if (normalized.contains("rubble")) {
            return RUBBLE_PILE;
        }
        if (normalized.contains("twisted") || normalized.contains("metal")) {
            return TWISTED_METAL;
        }
        return FLAT;
    }
}
