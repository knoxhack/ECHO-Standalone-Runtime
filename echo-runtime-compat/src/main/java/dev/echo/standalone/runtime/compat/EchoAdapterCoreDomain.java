package dev.echo.standalone.runtime.compat;

import java.util.Locale;
import java.util.Optional;

public enum EchoAdapterCoreDomain {
    BLOCKS("blocks"),
    ITEMS("items"),
    INVENTORY("inventory"),
    ENTITIES("entities"),
    CREATURES("creatures"),
    RECIPES("recipes"),
    LOOT("loot"),
    STRUCTURES("structures"),
    BIOMES("biomes"),
    UI_SCREENS("ui_screens"),
    UI_OVERLAYS("ui_overlays"),
    SOUNDS("sounds"),
    MISSIONS("missions"),
    PROGRESSION("progression"),
    PLAYTESTS("playtests"),
    TUTORIALS("tutorials"),
    SAVES("saves"),
    WORLDGEN("worldgen"),
    WAYSTONES("waystones"),
    ANCHORS("anchors"),
    FRAGMENTS("fragments"),
    ANOMALIES("anomalies"),
    FAMILIAR("familiar"),
    RIFTS("rifts"),
    RITUALS("rituals"),
    SPELLS("spells"),
    NETWORKING("networking"),
    COMMANDS("commands"),
    DIAGNOSTICS("diagnostics"),
    MAPS("maps"),
    HOLOMAP("holomap"),
    LENS("lens"),
    LOGISTICS("logistics"),
    CATALOG("catalog"),
    DEPOTS("depots"),
    PROBES("probes"),
    ROUTES("routes"),
    SALVAGE("salvage"),
    SECTORS("sectors"),
    PACKS("packs"),
    THEMES("themes"),
    WIKI("wiki"),
    INDEX("index"),
    ASSETS("assets"),
    DATA("data"),
    RENDERING("rendering"),
    INPUT("input"),
    PLAYER("player"),
    WEATHER("weather"),
    HAZARDS("hazards"),
    MACHINES("machines"),
    POWER("power"),
    RECOVERY("recovery"),
    TERMINAL("terminal"),
    ECONOMY("economy"),
    PERMISSIONS("permissions"),
    VEHICLES("vehicles"),
    STORY("story");

    private final String id;

    EchoAdapterCoreDomain(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoAdapterCoreDomain> fromId(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
        if (normalized.equals("audio") || normalized.equals("sound")) {
            return Optional.of(SOUNDS);
        }
        for (EchoAdapterCoreDomain domain : values()) {
            if (domain.id.equals(normalized) || domain.name().toLowerCase(Locale.ROOT).equals(normalized)) {
                return Optional.of(domain);
            }
        }
        return Optional.empty();
    }
}
