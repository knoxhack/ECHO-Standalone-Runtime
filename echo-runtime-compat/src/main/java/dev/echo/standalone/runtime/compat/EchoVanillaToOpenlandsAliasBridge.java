package dev.echo.standalone.runtime.compat;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Canonical vanilla Minecraft → Openlands/Foundation alias bridge.
 *
 * <p>This bridge is used when loading NeoForge datapacks or other vanilla-flavored content so that
 * references to {@code minecraft:*} IDs resolve to the ECHO-native Openlands/Foundation equivalents.
 * It is intentionally one-directional: it remaps vanilla inputs to ECHO canonical IDs, never the
 * reverse.
 */
public final class EchoVanillaToOpenlandsAliasBridge {

    private final Map<String, String> aliases;

    public EchoVanillaToOpenlandsAliasBridge() {
        Map<String, String> map = new LinkedHashMap<>();
        // Terrain
        map.put("minecraft:stone", "echomaterialcore:fieldstone");
        map.put("minecraft:dirt", "echoopenlandsprotocol:dry_soil");
        map.put("minecraft:grass_block", "echoopenlandsprotocol:meadow_grass_block");
        map.put("minecraft:sand", "echomaterialcore:sand");
        map.put("minecraft:gravel", "echomaterialcore:gravel");
        map.put("minecraft:clay", "echomaterialcore:clay");
        map.put("minecraft:mud", "echoopenlandsprotocol:mud");

        // Wood
        map.put("minecraft:oak_log", "echoopenlandsprotocol:pine_log");
        map.put("minecraft:oak_planks", "echomaterialcore:branchwood_planks");
        map.put("minecraft:stick", "echomaterialcore:branchwood_stick");

        // Ores / metals
        map.put("minecraft:iron_ore", "echoopenlandsprotocol:glow_crystal_cluster");
        map.put("minecraft:copper_ore", "echomaterialcore:cupral_vein");
        map.put("minecraft:tin_ore", "echomaterialcore:tinveil_vein");
        map.put("minecraft:iron_ingot", "echomaterialcore:ferrite_bar");
        map.put("minecraft:copper_ingot", "echomaterialcore:cupral_bar");
        map.put("minecraft:gold_ingot", "echomaterialcore:bronze_cast");

        // Light / utility
        map.put("minecraft:torch", "echoworldstarter:pitchlight");
        map.put("minecraft:lantern", "echoopenlandsprotocol:lantern");
        map.put("minecraft:chest", "echostationcore:field_crate");
        map.put("minecraft:crafting_table", "echostationcore:field_bench");
        map.put("minecraft:furnace", "echostationcore:kiln");
        map.put("minecraft:smoker", "echostationcore:cookpot");

        // Food / items
        map.put("minecraft:apple", "echoopenlandsprotocol:berries");
        map.put("minecraft:bread", "echoopenlandsprotocol:travel_bread");
        map.put("minecraft:cooked_beef", "echoopenlandsprotocol:cooked_meat");
        map.put("minecraft:beef", "echoopenlandsprotocol:raw_meat");

        this.aliases = Map.copyOf(map);
    }

    /**
     * Resolves a vanilla ID to its Openlands/Foundation canonical ID. Returns the input unchanged if
     * no mapping exists.
     */
    public String resolve(String id) {
        if (id == null || id.isBlank()) {
            return id;
        }
        boolean tag = id.startsWith("#");
        String key = tag ? id.substring(1) : id;
        String resolved = aliases.getOrDefault(key, key);
        return tag ? "#" + resolved : resolved;
    }

    public List<String> resolveAll(Collection<String> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(this::resolve).toList();
    }

    public Map<String, String> aliases() {
        return aliases;
    }
}
