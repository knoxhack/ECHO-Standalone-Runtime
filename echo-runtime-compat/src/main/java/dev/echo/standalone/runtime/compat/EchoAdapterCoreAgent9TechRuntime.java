package dev.echo.standalone.runtime.compat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class EchoAdapterCoreAgent9TechRuntime {
    public static final String CONTRACT_ID = "adaptercore.agent9.tech.machine_power_logistics.v1";

    public EchoAdapterCoreAgent9TechRuntime() {
    }

    public Map<String, Object> run(EchoAdapterCoreStandaloneContentBridge bridge) {
        EchoAdapterCoreStandaloneRegistry registry = bridge.registry();
        requireContent(registry, EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID);
        requireContent(registry, EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID);
        requireContent(registry, EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID);
        requireContent(registry, EchoAdapterCoreStandaloneContentBridge.LOAD_DISTRIBUTOR_BLOCK_ID);
        requireContent(registry, EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID);
        requireContent(registry, EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_PRESS_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.INSTALL_ITEM_PIPE_MISSION_ID);

        TechWorld world = TechWorld.reference();
        boolean machinePlaced = world.placeMachine("scrap_press");
        boolean uiOpened = world.openMachineUi("scrap_press");
        int inserted = world.insertInput("scrap_press", "scrap_metal", 9);
        boolean graphConnected = world.connectPowerGraph();
        int ticks = world.tickUntilOutput(80);
        int moved = world.transfer("scrap_press", "ore_grinder", "compressed_scrap", 1);
        boolean multiblockValid = world.validateMultiblock("factory_controller");
        VehicleAction vehicle = world.moveVehicle("wasteland_rover", 4);
        EconomyCharge economy = world.charge("faction_trade_depot", "scrap_credit", 25);
        List<String> loot = world.openLoot("supply_crate");
        Map<String, Object> saved = world.save();
        TechWorld restored = TechWorld.restore(saved);
        boolean saveLoadRoundTrip = restored.outputCount("scrap_press", "compressed_scrap") == 0
                && restored.inputCount("ore_grinder", "compressed_scrap") == 1
                && restored.energy("battery_bank") == world.energy("battery_bank")
                && restored.missionComplete("echoashfallprotocol:mission/build_scrap_press");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schema", CONTRACT_ID);
        result.put("status", allTrue(machinePlaced, uiOpened, inserted == 9, graphConnected,
                world.outputProduced, moved == 1, multiblockValid, vehicle.completed(),
                economy.paid(), !loot.isEmpty(), saveLoadRoundTrip) ? "PASS" : "FAIL");
        result.put("adapterCoreContract", CONTRACT_ID);
        result.put("runtime", "echo_standalone_runtime");
        result.put("referenceBehavior", "ashfall_machine_power_logistics");
        result.put("standaloneDuplicateGameplaySystem", false);
        result.put("minecraftRuntimeAccessed", false);
        result.put("machinePlaced", machinePlaced);
        result.put("machineUiOpened", uiOpened);
        result.put("machineAcceptedInput", inserted == 9);
        result.put("insertedInputCount", inserted);
        result.put("powerGraphConnected", graphConnected);
        result.put("recipeProgressed", world.recipeProgressed);
        result.put("recipeProgressTicks", ticks);
        result.put("powerConsumed", world.powerConsumed);
        result.put("outputAppeared", world.outputProduced);
        result.put("outputItem", "compressed_scrap");
        result.put("outputCountBeforeLogistics", world.outputCountBeforeLogistics);
        result.put("logisticsTransferCompleted", moved == 1);
        result.put("oreGrinderInputCount", restored.inputCount("ore_grinder", "compressed_scrap"));
        result.put("stateSaved", !saved.isEmpty());
        result.put("stateReloaded", saveLoadRoundTrip);
        result.put("missionCanDependOnMachineCompletion",
                restored.missionComplete("echoashfallprotocol:mission/build_scrap_press"));
        result.put("multiblockValidationPassed", multiblockValid);
        result.put("vehicleMovementActionCompleted", vehicle.completed());
        result.put("vehicleAction", vehicle.asMap());
        result.put("economyCostCharged", economy.paid());
        result.put("economyCharge", economy.asMap());
        result.put("lootOutputs", loot);
        result.put("powerGraph", world.powerGraph());
        result.put("inventoryPorts", world.inventoryPorts());
        result.put("adapterRuntimeIds", adapterRuntimeIds(registry));
        result.put("standaloneImplemented", List.of(
                "place_machine",
                "open_machine_ui",
                "insert_input",
                "consume_power",
                "process_recipe",
                "output_result",
                "save_machine_state",
                "reload_machine_state",
                "machine_ticking",
                "power_graph",
                "inventory_ports",
                "recipe_progress",
                "multiblock_validation",
                "logistics_transfer",
                "vehicle_movement_action",
                "economy_costs",
                "loot_outputs",
                "mission_dependency_on_machine_completion"));
        Map<String, Object> moduleEntrypoints = EchoAdapterCoreAgent9TechModuleEntrypoints.fromRuntime(result);
        result.put("moduleEntrypointsStatus", moduleEntrypoints.get("status"));
        result.put("moduleEntrypointCount", moduleEntrypoints.get("moduleEntrypointCount"));
        result.put("moduleEntrypoints", moduleEntrypoints.get("moduleEntrypoints"));
        return Map.copyOf(result);
    }

    private static void requireContent(EchoAdapterCoreStandaloneRegistry registry, String liveVoxelId) {
        registry.findLiveVoxelId(liveVoxelId).orElseThrow(() ->
                new IllegalArgumentException("Missing AdapterCore live voxel content for " + liveVoxelId));
    }

    private static List<Map<String, Object>> adapterRuntimeIds(EchoAdapterCoreStandaloneRegistry registry) {
        List<Map<String, Object>> ids = new ArrayList<>();
        for (String contentId : List.of(
                "echoashfallprotocol:block/scrap_press",
                "echoashfallprotocol:block/micro_generator",
                "echoashfallprotocol:block/power_cable",
                "echoashfallprotocol:block/load_distributor",
                "echoashfallprotocol:block/battery_bank",
                "echoashfallprotocol:block/item_pipe",
                "echoashfallprotocol:block/ore_grinder")) {
            EchoAdapterCoreRegistryEntry entry = registry.requireContentId(contentId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("contentId", contentId);
            row.put("neoForge", entry.idFor(EchoAdapterCoreRuntimeKind.NEOFORGE));
            row.put("nativeLoader", entry.idFor(EchoAdapterCoreRuntimeKind.ECHO_NATIVE_LOADER));
            row.put("standalone", entry.idFor(EchoAdapterCoreRuntimeKind.ECHO_RUNTIME_STANDALONE));
            ids.add(row);
        }
        return List.copyOf(ids);
    }

    private static boolean allTrue(boolean... values) {
        for (boolean value : values) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    private record VehicleAction(String vehicleId, int requestedSteps, int movedSteps, int fuelAfter) {
        boolean completed() {
            return movedSteps == requestedSteps && fuelAfter >= 0;
        }

        Map<String, Object> asMap() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("vehicleId", vehicleId);
            result.put("requestedSteps", requestedSteps);
            result.put("movedSteps", movedSteps);
            result.put("fuelAfter", fuelAfter);
            result.put("completed", completed());
            return result;
        }
    }

    private record EconomyCharge(String tradeRuleId, String currencyId, int cost, int balanceAfter) {
        boolean paid() {
            return balanceAfter >= 0;
        }

        Map<String, Object> asMap() {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tradeRuleId", tradeRuleId);
            result.put("currencyId", currencyId);
            result.put("cost", cost);
            result.put("balanceAfter", balanceAfter);
            result.put("paid", paid());
            return result;
        }
    }

    private static final class TechWorld {
        private final Map<String, Node> nodes = new LinkedHashMap<>();
        private final Map<String, Integer> balances = new LinkedHashMap<>();
        private final Set<String> completedMissions = new LinkedHashSet<>();
        private boolean graphConnected;
        private boolean recipeProgressed;
        private boolean outputProduced;
        private int powerConsumed;
        private int outputCountBeforeLogistics;

        static TechWorld reference() {
            TechWorld world = new TechWorld();
            world.nodes.put("micro_generator", Node.power("micro_generator", "GENERATOR", 3_000, 64, 8));
            world.nodes.put("power_cable", Node.power("power_cable", "CABLE", 1_000, 50, 0));
            world.nodes.put("load_distributor", Node.power("load_distributor", "ROUTER", 2_000, 512, 0));
            world.nodes.put("battery_bank", Node.power("battery_bank", "BATTERY", 10_000, 100, 0));
            world.nodes.put("scrap_press", Node.machine("scrap_press", 1_500, 128, 40, 1));
            world.nodes.put("item_pipe", Node.pipe("item_pipe"));
            world.nodes.put("ore_grinder", Node.machine("ore_grinder", 2_000, 128, 80, 2));
            world.nodes.put("factory_controller", Node.machine("factory_controller", 0, 0, 0, 0));
            world.nodes.put("wasteland_rover", Node.vehicle("wasteland_rover", 12));
            world.balances.put("scrap_credit", 100);
            return world;
        }

        static TechWorld restore(Map<String, Object> snapshot) {
            TechWorld world = reference();
            world.graphConnected = Boolean.TRUE.equals(snapshot.get("graphConnected"));
            world.recipeProgressed = Boolean.TRUE.equals(snapshot.get("recipeProgressed"));
            world.outputProduced = Boolean.TRUE.equals(snapshot.get("outputProduced"));
            world.powerConsumed = ((Number) snapshot.getOrDefault("powerConsumed", 0)).intValue();
            world.outputCountBeforeLogistics = ((Number) snapshot.getOrDefault("outputCountBeforeLogistics", 0)).intValue();
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> savedNodes = (Map<String, Map<String, Object>>) snapshot.get("nodes");
            if (savedNodes != null) {
                for (Map.Entry<String, Map<String, Object>> entry : savedNodes.entrySet()) {
                    Node node = world.nodes.get(entry.getKey());
                    if (node != null) {
                        node.restore(entry.getValue());
                    }
                }
            }
            @SuppressWarnings("unchecked")
            List<String> missions = (List<String>) snapshot.getOrDefault("completedMissions", List.of());
            world.completedMissions.addAll(missions);
            return world;
        }

        boolean placeMachine(String id) {
            Node node = nodes.get(id);
            if (node == null) {
                return false;
            }
            node.placed = true;
            return true;
        }

        boolean openMachineUi(String id) {
            Node node = nodes.get(id);
            if (node == null || !node.placed) {
                return false;
            }
            node.uiOpen = true;
            return true;
        }

        int insertInput(String id, String itemId, int count) {
            Node node = nodes.get(id);
            if (node == null || !node.uiOpen || count <= 0) {
                return 0;
            }
            node.inputs.merge(itemId, count, Integer::sum);
            return count;
        }

        boolean connectPowerGraph() {
            link("micro_generator", "power_cable");
            link("power_cable", "load_distributor");
            link("load_distributor", "battery_bank");
            link("load_distributor", "scrap_press");
            link("scrap_press", "item_pipe");
            link("item_pipe", "ore_grinder");
            graphConnected = true;
            return true;
        }

        int tickUntilOutput(int maxTicks) {
            int ticks = 0;
            while (ticks < maxTicks && outputCount("scrap_press", "compressed_scrap") == 0) {
                ticks++;
                tickPower();
                tickMachine(nodes.get("scrap_press"), "scrap_metal", "compressed_scrap");
            }
            outputCountBeforeLogistics = outputCount("scrap_press", "compressed_scrap");
            if (outputCountBeforeLogistics > 0) {
                completedMissions.add("echoashfallprotocol:mission/build_scrap_press");
                outputProduced = true;
            }
            return ticks;
        }

        int transfer(String from, String to, String itemId, int count) {
            if (!hasRoute(from, to)) {
                return 0;
            }
            Node source = nodes.get(from);
            Node target = nodes.get(to);
            int available = source.outputs.getOrDefault(itemId, 0);
            int moved = Math.min(available, count);
            if (moved <= 0) {
                return 0;
            }
            source.outputs.put(itemId, available - moved);
            target.inputs.merge(itemId, moved, Integer::sum);
            completedMissions.add("echoashfallprotocol:mission/install_item_pipe");
            return moved;
        }

        boolean validateMultiblock(String id) {
            return nodes.containsKey(id)
                    && nodes.containsKey("scrap_press")
                    && nodes.containsKey("power_cable")
                    && nodes.containsKey("item_pipe")
                    && graphConnected;
        }

        VehicleAction moveVehicle(String id, int steps) {
            Node vehicle = nodes.get(id);
            int moved = Math.min(steps, vehicle.fuel);
            vehicle.fuel -= moved;
            vehicle.position += moved;
            return new VehicleAction(id, steps, moved, vehicle.fuel);
        }

        EconomyCharge charge(String tradeRuleId, String currencyId, int cost) {
            int balance = balances.getOrDefault(currencyId, 0);
            int after = balance - cost;
            balances.put(currencyId, after);
            return new EconomyCharge(tradeRuleId, currencyId, cost, after);
        }

        List<String> openLoot(String sourceId) {
            if (!"supply_crate".equals(sourceId)) {
                return List.of();
            }
            return List.of("echoashfallprotocol:scrap_metal", "echoashfallprotocol:scrap_wire");
        }

        Map<String, Object> save() {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            Map<String, Object> savedNodes = new LinkedHashMap<>();
            for (Map.Entry<String, Node> entry : nodes.entrySet()) {
                savedNodes.put(entry.getKey(), entry.getValue().save());
            }
            snapshot.put("nodes", savedNodes);
            snapshot.put("completedMissions", List.copyOf(completedMissions));
            snapshot.put("graphConnected", graphConnected);
            snapshot.put("recipeProgressed", recipeProgressed);
            snapshot.put("outputProduced", outputProduced);
            snapshot.put("powerConsumed", powerConsumed);
            snapshot.put("outputCountBeforeLogistics", outputCountBeforeLogistics);
            return snapshot;
        }

        int outputCount(String id, String itemId) {
            return nodes.get(id).outputs.getOrDefault(itemId, 0);
        }

        int inputCount(String id, String itemId) {
            return nodes.get(id).inputs.getOrDefault(itemId, 0);
        }

        int energy(String id) {
            return nodes.get(id).energy;
        }

        int machineProgress(String id) {
            return nodes.get(id).progress;
        }

        boolean missionComplete(String id) {
            return completedMissions.contains(id);
        }

        List<Map<String, Object>> powerGraph() {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (String id : List.of("micro_generator", "power_cable", "load_distributor", "battery_bank", "scrap_press")) {
                Node node = nodes.get(id);
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", id);
                row.put("kind", node.kind);
                row.put("energy", node.energy);
                row.put("capacity", node.capacity);
                row.put("neighbors", List.copyOf(node.neighbors));
                rows.add(row);
            }
            return List.copyOf(rows);
        }

        List<Map<String, Object>> inventoryPorts() {
            return List.of(
                    inventoryPort("scrap_press", "input", List.of("scrap_metal")),
                    inventoryPort("scrap_press", "output", List.of("compressed_scrap")),
                    inventoryPort("ore_grinder", "input", List.of("compressed_scrap"))
            );
        }

        private static Map<String, Object> inventoryPort(String machineId, String port, List<String> accepts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("machineId", machineId);
            row.put("port", port);
            row.put("accepts", accepts);
            return row;
        }

        private void tickPower() {
            Node generator = nodes.get("micro_generator");
            generator.energy = Math.min(generator.capacity, generator.energy + generator.generationPerTick);
            moveEnergy("micro_generator", "power_cable", 8);
            moveEnergy("power_cable", "load_distributor", 8);
            moveEnergy("load_distributor", "scrap_press", 1);
            moveEnergy("load_distributor", "battery_bank", 7);
        }

        private void tickMachine(Node machine, String inputItem, String outputItem) {
            if (machine == null || machine.inputs.getOrDefault(inputItem, 0) <= 0 || machine.energy < machine.powerPerTick) {
                return;
            }
            machine.energy -= machine.powerPerTick;
            powerConsumed += machine.powerPerTick;
            machine.progress++;
            recipeProgressed = true;
            if (machine.progress >= machine.recipeTicks) {
                machine.inputs.put(inputItem, machine.inputs.get(inputItem) - 1);
                machine.outputs.merge(outputItem, 1, Integer::sum);
                machine.progress = 0;
            }
        }

        private void moveEnergy(String from, String to, int requested) {
            Node source = nodes.get(from);
            Node target = nodes.get(to);
            int moved = Math.min(requested, Math.min(source.energy, target.capacity - target.energy));
            if (moved <= 0) {
                return;
            }
            source.energy -= moved;
            target.energy += moved;
        }

        private void link(String left, String right) {
            nodes.get(left).neighbors.add(right);
            nodes.get(right).neighbors.add(left);
        }

        private boolean hasRoute(String from, String to) {
            Queue<String> queue = new ArrayDeque<>();
            Set<String> visited = new LinkedHashSet<>();
            queue.add(from);
            visited.add(from);
            while (!queue.isEmpty()) {
                String current = queue.remove();
                if (current.equals(to)) {
                    return true;
                }
                for (String next : nodes.get(current).neighbors) {
                    if (visited.add(next)) {
                        queue.add(next);
                    }
                }
            }
            return false;
        }
    }

    private static final class Node {
        private final String id;
        private final String kind;
        private final int capacity;
        private final int transferPerTick;
        private final int generationPerTick;
        private final int recipeTicks;
        private final int powerPerTick;
        private final List<String> neighbors = new ArrayList<>();
        private final Map<String, Integer> inputs = new LinkedHashMap<>();
        private final Map<String, Integer> outputs = new LinkedHashMap<>();
        private boolean placed;
        private boolean uiOpen;
        private int energy;
        private int progress;
        private int fuel;
        private int position;

        private Node(String id, String kind, int capacity, int transferPerTick, int generationPerTick,
                     int recipeTicks, int powerPerTick) {
            this.id = id;
            this.kind = kind;
            this.capacity = capacity;
            this.transferPerTick = transferPerTick;
            this.generationPerTick = generationPerTick;
            this.recipeTicks = recipeTicks;
            this.powerPerTick = powerPerTick;
        }

        static Node power(String id, String kind, int capacity, int transferPerTick, int generationPerTick) {
            return new Node(id, kind, capacity, transferPerTick, generationPerTick, 0, 0);
        }

        static Node machine(String id, int capacity, int transferPerTick, int recipeTicks, int powerPerTick) {
            Node node = new Node(id, "MACHINE", capacity, transferPerTick, 0, recipeTicks, powerPerTick);
            node.placed = true;
            return node;
        }

        static Node pipe(String id) {
            return new Node(id, "INVENTORY_PIPE", 0, 0, 0, 0, 0);
        }

        static Node vehicle(String id, int fuel) {
            Node node = new Node(id, "VEHICLE", 0, 0, 0, 0, 0);
            node.fuel = fuel;
            return node;
        }

        Map<String, Object> save() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", id);
            data.put("energy", energy);
            data.put("progress", progress);
            data.put("placed", placed);
            data.put("uiOpen", uiOpen);
            data.put("inputs", Map.copyOf(inputs));
            data.put("outputs", Map.copyOf(outputs));
            data.put("fuel", fuel);
            data.put("position", position);
            return data;
        }

        @SuppressWarnings("unchecked")
        void restore(Map<String, Object> data) {
            energy = ((Number) data.getOrDefault("energy", 0)).intValue();
            progress = ((Number) data.getOrDefault("progress", 0)).intValue();
            placed = Boolean.TRUE.equals(data.get("placed"));
            uiOpen = Boolean.TRUE.equals(data.get("uiOpen"));
            inputs.clear();
            inputs.putAll((Map<String, Integer>) data.getOrDefault("inputs", Map.of()));
            outputs.clear();
            outputs.putAll((Map<String, Integer>) data.getOrDefault("outputs", Map.of()));
            fuel = ((Number) data.getOrDefault("fuel", 0)).intValue();
            position = ((Number) data.getOrDefault("position", 0)).intValue();
        }
    }
}
