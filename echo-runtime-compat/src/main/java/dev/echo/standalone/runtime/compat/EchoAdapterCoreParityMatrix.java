package dev.echo.standalone.runtime.compat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoAdapterCoreParityMatrix {
    private static final Map<EchoAdapterCoreDomain, String> NEOFORGE_FEATURES = Map.ofEntries(
            Map.entry(EchoAdapterCoreDomain.BLOCKS, "DeferredRegister<Block> and blockstate/model assets"),
            Map.entry(EchoAdapterCoreDomain.ITEMS, "DeferredRegister<Item>, creative tabs, and item components"),
            Map.entry(EchoAdapterCoreDomain.INVENTORY, "Player inventory, containers, slots, transfer, and sync hooks"),
            Map.entry(EchoAdapterCoreDomain.ENTITIES, "EntityType registration, AI, spawn, and sync hooks"),
            Map.entry(EchoAdapterCoreDomain.RECIPES, "RecipeSerializer, RecipeType, and datapack recipe JSON"),
            Map.entry(EchoAdapterCoreDomain.LOOT, "Loot tables and NeoForge global loot modifiers"),
            Map.entry(EchoAdapterCoreDomain.STRUCTURES, "Structure templates, pools, and feature placement"),
            Map.entry(EchoAdapterCoreDomain.UI_SCREENS, "MenuType, Screen registration, HUD, and terminal screens"),
            Map.entry(EchoAdapterCoreDomain.UI_OVERLAYS, "HUD overlays, inventory overlays, tooltips, and render/input hooks"),
            Map.entry(EchoAdapterCoreDomain.SOUNDS, "SoundEvent registration and sound JSON"),
            Map.entry(EchoAdapterCoreDomain.MISSIONS, "MissionCore services, objectives, and progression hooks"),
            Map.entry(EchoAdapterCoreDomain.SAVES, "SavedData, attachments, and profile migration state"),
            Map.entry(EchoAdapterCoreDomain.WORLDGEN, "Biome, region, hazard, POI, and worldgen datapacks"),
            Map.entry(EchoAdapterCoreDomain.NETWORKING, "Custom payload channels and packet handlers"),
            Map.entry(EchoAdapterCoreDomain.COMMANDS, "Brigadier command registration"),
            Map.entry(EchoAdapterCoreDomain.DIAGNOSTICS, "GameTest, log markers, support bundles, and readiness reports"),
            Map.entry(EchoAdapterCoreDomain.DATA, "Datapack JSON, schema validation, tags, recipes, and generated registries"),
            Map.entry(EchoAdapterCoreDomain.INPUT, "Key mappings, click handlers, screen focus, and input event hooks"),
            Map.entry(EchoAdapterCoreDomain.RENDERING, "Block/entity renderers, HUD overlays, particles, and baked model hooks"),
            Map.entry(EchoAdapterCoreDomain.PLAYER, "Player capabilities, starting kit, progression state, and interaction hooks"),
            Map.entry(EchoAdapterCoreDomain.WEATHER, "Weather events, atmosphere state, storms, and environmental tick hooks"),
            Map.entry(EchoAdapterCoreDomain.HAZARDS, "Radiation, cold, toxicity, exposure, and world hazard event handlers"),
            Map.entry(EchoAdapterCoreDomain.MACHINES, "Block entities, menus, recipes, power IO, and machine tick handlers"),
            Map.entry(EchoAdapterCoreDomain.POWER, "Energy storage, cables, network balancing, and machine power contracts"),
            Map.entry(EchoAdapterCoreDomain.ECONOMY, "Vendor, trade, currency, reward, and market data hooks"),
            Map.entry(EchoAdapterCoreDomain.STORY, "Chapter route, lore, faction, dialogue, and story progression hooks")
    );
    private static final Map<EchoAdapterCoreDomain, String> STANDALONE_BEHAVIORS = Map.ofEntries(
            Map.entry(EchoAdapterCoreDomain.BLOCKS, "Voxel registry exposes live block/material entries through AdapterCore."),
            Map.entry(EchoAdapterCoreDomain.ITEMS, "Hotbar and inventory items resolve from AdapterCore item bindings."),
            Map.entry(EchoAdapterCoreDomain.INVENTORY, "Standalone inventory state, slots, transfer, and sync are service-bound runtime data."),
            Map.entry(EchoAdapterCoreDomain.ENTITIES, "Standalone entity runtime uses AdapterCore entity contracts instead of NeoForge classes."),
            Map.entry(EchoAdapterCoreDomain.RECIPES, "Crafting recipes resolve as data contracts for standalone item crafting."),
            Map.entry(EchoAdapterCoreDomain.LOOT, "Cache and reward loot resolve as AdapterCore data, not NeoForge loot APIs."),
            Map.entry(EchoAdapterCoreDomain.STRUCTURES, "Crash-site and POI layout contracts feed the voxel world generator."),
            Map.entry(EchoAdapterCoreDomain.UI_SCREENS, "Terminal, inventory, mission log, and HUD screens render from standalone UI targets."),
            Map.entry(EchoAdapterCoreDomain.UI_OVERLAYS, "Standalone overlays render and route input through explicit UI overlay contracts."),
            Map.entry(EchoAdapterCoreDomain.SOUNDS, "Audio cues bind to standalone buses and generated audio events."),
            Map.entry(EchoAdapterCoreDomain.MISSIONS, "Mission state is shared by playable voxel, UI, saves, and beta gate runtime."),
            Map.entry(EchoAdapterCoreDomain.SAVES, "Save profile writes world, player, hotbar, mission, and render snapshots."),
            Map.entry(EchoAdapterCoreDomain.WORLDGEN, "Ashfall regions, hazards, and materials drive standalone voxel chunks."),
            Map.entry(EchoAdapterCoreDomain.NETWORKING, "Local sync contracts cover entity and inventory packet behavior."),
            Map.entry(EchoAdapterCoreDomain.COMMANDS, "Runtime command hooks expose debug and terminal command targets."),
            Map.entry(EchoAdapterCoreDomain.DIAGNOSTICS, "Standalone diagnostics emit the same readiness, support, and parity evidence."),
            Map.entry(EchoAdapterCoreDomain.DATA, "Standalone data registries load the same schema-backed content definitions."),
            Map.entry(EchoAdapterCoreDomain.INPUT, "Standalone input routes keyboard, mouse, and focus actions through runtime contracts."),
            Map.entry(EchoAdapterCoreDomain.RENDERING, "Standalone render targets consume AdapterCore material, HUD, and scene bindings."),
            Map.entry(EchoAdapterCoreDomain.PLAYER, "Standalone player state drives movement, inventory, survival, and interaction contracts."),
            Map.entry(EchoAdapterCoreDomain.WEATHER, "Standalone world ticks apply AdapterCore weather and atmosphere contracts."),
            Map.entry(EchoAdapterCoreDomain.HAZARDS, "Standalone hazard zones apply AdapterCore exposure and mitigation rules."),
            Map.entry(EchoAdapterCoreDomain.MACHINES, "Standalone machine simulation consumes AdapterCore power, recipe, and IO profiles."),
            Map.entry(EchoAdapterCoreDomain.POWER, "Standalone power systems consume AdapterCore network and machine power profiles."),
            Map.entry(EchoAdapterCoreDomain.ECONOMY, "Standalone rewards and vendors resolve through AdapterCore economy data."),
            Map.entry(EchoAdapterCoreDomain.STORY, "Standalone chapters, factions, and dialogue resolve from AdapterCore story contracts.")
    );

    private final List<EchoAdapterCoreParityMatrixEntry> entries;

    private EchoAdapterCoreParityMatrix(List<EchoAdapterCoreParityMatrixEntry> entries) {
        Objects.requireNonNull(entries, "entries");
        this.entries = entries.stream()
                .sorted(Comparator.comparing(entry -> entry.domain().id()))
                .toList();
    }

    public static EchoAdapterCoreParityMatrix ashfall(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        EnumMap<EchoAdapterCoreDomain, EchoAdapterCoreRegistryEntry> byDomain =
                new EnumMap<>(EchoAdapterCoreDomain.class);
        for (EchoAdapterCoreRegistryEntry entry : bridge.registry().entries()) {
            if (entry.binding().moduleId().equals("echoashfallprotocol")
                    || entry.binding().moduleId().equals("echoterminal")
                    || entry.binding().moduleId().equals("echopowergrid")) {
                byDomain.putIfAbsent(entry.domain(), entry);
            }
        }

        ArrayList<EchoAdapterCoreParityMatrixEntry> result = new ArrayList<>();
        for (EchoAdapterCoreDomain domain : EchoAdapterCoreContractLock.requiredBetaDomains()) {
            EchoAdapterCoreRegistryEntry entry = byDomain.get(domain);
            if (entry != null) {
                result.add(new EchoAdapterCoreParityMatrixEntry(
                        domain,
                        NEOFORGE_FEATURES.get(domain),
                        entry.binding().adapterKey(),
                        STANDALONE_BEHAVIORS.get(domain)
                ));
            }
        }
        return new EchoAdapterCoreParityMatrix(result);
    }

    public List<EchoAdapterCoreParityMatrixEntry> entries() {
        return entries;
    }

    public Optional<EchoAdapterCoreParityMatrixEntry> find(EchoAdapterCoreDomain domain) {
        Objects.requireNonNull(domain, "domain");
        return entries.stream()
                .filter(entry -> entry.domain() == domain)
                .findFirst();
    }

    public List<EchoAdapterCoreDomain> missingRequiredBetaDomains() {
        return EchoAdapterCoreContractLock.requiredBetaDomains().stream()
                .filter(domain -> find(domain).isEmpty())
                .toList();
    }

    public boolean completeForBeta() {
        return missingRequiredBetaDomains().isEmpty()
                && entries.stream().allMatch(entry -> !entry.adapterBinding().isBlank());
    }

    public String summary() {
        return entries.size()
                + "/"
                + EchoAdapterCoreContractLock.requiredBetaDomains().size()
                + " required AdapterCore parity domains covered";
    }
}
