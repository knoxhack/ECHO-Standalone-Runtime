package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockInstance;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

final class EchoClientMachineRuntime {
    private static final String MICRO_GENERATOR = "micro_generator";
    private static final String POWER_CABLE = "power_cable";
    private static final String LOAD_DISTRIBUTOR = "load_distributor";
    private static final String BATTERY_BANK = "battery_bank";
    private static final String SCRAP_PRESS = "scrap_press";
    private static final String ITEM_PIPE = "item_pipe";
    private static final String ORE_GRINDER = "ore_grinder";
    private static final String SCRAP_METAL_ITEM_ID = "echoashfallprotocol:scrap_metal";
    private static final String COMPRESSED_SCRAP_ITEM_ID = "echoashfallprotocol:compressed_scrap";
    private static final String SCRAP_PRESS_COMPRESSED_RECIPE_ID =
            "echoashfallprotocol:scrap_press/compressed_scrap";
    private static final String SCRAP_PRESS_DENSE_RECIPE_ID =
            "echoashfallprotocol:scrap_press/dense_compressed_scrap";
    private static final int SCRAP_PRESS_RECIPE_TICKS = 40;
    private static final List<MachineRecipe> SCRAP_PRESS_RECIPES = List.of(
            new MachineRecipe(
                    SCRAP_PRESS_COMPRESSED_RECIPE_ID,
                    "Compressed Scrap",
                    SCRAP_METAL_ITEM_ID,
                    1,
                    COMPRESSED_SCRAP_ITEM_ID,
                    1,
                    SCRAP_PRESS_RECIPE_TICKS
            ),
            new MachineRecipe(
                    SCRAP_PRESS_DENSE_RECIPE_ID,
                    "Dense Scrap Batch",
                    SCRAP_METAL_ITEM_ID,
                    2,
                    COMPRESSED_SCRAP_ITEM_ID,
                    2,
                    SCRAP_PRESS_RECIPE_TICKS * 2
            )
    );
    private static final List<String> POWER_GRAPH_IDS = List.of(
            MICRO_GENERATOR,
            POWER_CABLE,
            LOAD_DISTRIBUTOR,
            BATTERY_BANK,
            SCRAP_PRESS
    );

    private final LinkedHashMap<String, Node> nodes = new LinkedHashMap<>();
    private boolean graphConnected;
    private boolean machineUiOpened;
    private int scrapPressInputCount;
    private int scrapPressOutputCount;
    private int oreGrinderInputCount;
    private int scrapPressProgressTicks;
    private int recipeProgressTicks;
    private int powerConsumed;
    private boolean outputAppeared;
    private int outputCountBeforeLogistics;
    private boolean stateReloaded;
    private boolean missionDependency;
    private List<String> diagnostics = List.of();

    private EchoClientMachineRuntime() {
    }

    static EchoClientMachineRuntime reference() {
        EchoClientMachineRuntime runtime = emptyGraph();
        runtime.machineUiOpened = true;
        runtime.graphConnected = true;
        runtime.link(MICRO_GENERATOR, POWER_CABLE);
        runtime.link(POWER_CABLE, LOAD_DISTRIBUTOR);
        runtime.link(LOAD_DISTRIBUTOR, BATTERY_BANK);
        runtime.link(LOAD_DISTRIBUTOR, SCRAP_PRESS);
        runtime.link(SCRAP_PRESS, ITEM_PIPE);
        runtime.link(ITEM_PIPE, ORE_GRINDER);
        runtime.tick(40);
        return runtime;
    }

    static EchoClientMachineRuntime restore(EchoClientMachineStateSnapshot snapshot) {
        EchoClientMachineStateSnapshot safeSnapshot =
                snapshot == null ? EchoClientMachineStateSnapshot.reference() : snapshot;
        EchoClientMachineRuntime runtime = safeSnapshot.blockEntities().isEmpty()
                ? emptyGraph()
                : new EchoClientMachineRuntime();
        runtime.graphConnected = safeSnapshot.graphConnected();
        runtime.machineUiOpened = safeSnapshot.machineUiOpened();
        runtime.stateReloaded = safeSnapshot.stateReloaded();
        runtime.diagnostics = safeSnapshot.diagnostics();

        if (safeSnapshot.blockEntities().isEmpty()) {
            runtime.applyAggregateStateToPrimary(
                    safeSnapshot.scrapPressInputCount(),
                    safeSnapshot.scrapPressOutputCount(),
                    safeSnapshot.oreGrinderInputCount(),
                    safeSnapshot.scrapPressProgressTicks(),
                    safeSnapshot.recipeProgressTicks(),
                    safeSnapshot.powerConsumed(),
                    safeSnapshot.outputAppeared(),
                    safeSnapshot.outputCountBeforeLogistics(),
                    safeSnapshot.missionDependency()
            );
        } else {
            for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : safeSnapshot.blockEntities()) {
                String canonicalId = canonicalIdForBlockEntity(blockEntity);
                Node base = defaultNode(canonicalId);
                if (base == null) {
                    continue;
                }
                Node node = base.withPlacement(
                        blockEntity.entityId(),
                        canonicalId,
                        blockEntity.blockId(),
                        blockEntity.kind(),
                        blockEntity.x(),
                        blockEntity.y(),
                        blockEntity.z()
                );
                node.applyBlockEntity(blockEntity);
                runtime.nodes.put(node.id, node);
            }
        }
        for (EchoClientMachineStateSnapshot.PowerNode savedNode : safeSnapshot.powerGraph()) {
            String canonicalId = canonicalIdFromInstanceId(savedNode.id()).orElse(savedNode.id());
            Node existing = runtime.nodes.get(savedNode.id());
            if (existing == null) {
                Node base = defaultNode(canonicalId);
                if (base == null) {
                    continue;
                }
                existing = base.withInstanceId(savedNode.id());
                runtime.nodes.put(existing.id, existing);
            }
            existing.applyPowerNode(savedNode);
        }
        runtime.syncAggregateState();
        return runtime;
    }

    EchoClientMachineStateSnapshot snapshot() {
        syncAggregateState();
        return new EchoClientMachineStateSnapshot(
                graphConnected,
                machineUiOpened,
                scrapPressInputCount,
                scrapPressOutputCount,
                oreGrinderInputCount,
                scrapPressProgressTicks,
                recipeProgressTicks,
                powerConsumed,
                outputAppeared,
                outputCountBeforeLogistics,
                stateReloaded,
                missionDependency,
                powerGraph(),
                inventoryPorts(),
                blockEntities(),
                diagnostics
        );
    }

    EchoClientTechSurfaceModel techSurfaceModel(dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge bridge) {
        return EchoClientTechSurfaceModel.from(snapshot(), bridge);
    }

    static Optional<MachineBlockDefinition> machineBlockDefinition(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return Optional.empty();
        }
        String entityId = entityIdForBlockId(block.id());
        Node node = entityId == null ? null : defaultNode(entityId);
        return node == null ? Optional.empty() : Optional.of(new MachineBlockDefinition(node.canonicalId, node.kind));
    }

    int reconcileFromWorld(EchoVoxelWorld world) {
        if (world == null) {
            graphConnected = false;
            diagnostics = List.of("machine world unavailable; graph disconnected");
            syncAggregateState();
            return 0;
        }
        LinkedHashMap<String, Node> placedNodes = new LinkedHashMap<>();
        LinkedHashMap<String, Integer> canonicalCounts = new LinkedHashMap<>();
        List<EchoVoxelBlockInstance> placedBlocks = world.nonAirBlocks().stream()
                .filter(block -> machineCanonicalId(block).isPresent())
                .sorted(Comparator.comparingInt(EchoVoxelBlockInstance::x)
                        .thenComparingInt(EchoVoxelBlockInstance::y)
                        .thenComparingInt(EchoVoxelBlockInstance::z))
                .toList();
        for (EchoVoxelBlockInstance block : placedBlocks) {
            String canonicalId = machineCanonicalId(block).orElseThrow();
            int seen = canonicalCounts.getOrDefault(canonicalId, 0);
            String instanceId = instanceIdForPlacement(canonicalId, block, seen, placedNodes);
            canonicalCounts.put(canonicalId, seen + 1);
            Node base = nodes.containsKey(instanceId) ? nodes.get(instanceId) : defaultNode(canonicalId);
            if (base == null) {
                continue;
            }
            placedNodes.put(instanceId, base.withPlacement(
                    instanceId,
                    canonicalId,
                    block.block().id(),
                    block.state().property("machineKind").orElse(base.kind),
                    block.x(),
                    block.y(),
                    block.z()
            ));
        }
        if (placedNodes.isEmpty()) {
            nodes.clear();
            graphConnected = false;
            machineUiOpened = false;
            diagnostics = List.of();
            syncAggregateState();
            return 0;
        }
        nodes.clear();
        nodes.putAll(placedNodes);
        rebuildPlacedLinks();
        graphConnected = connectedScrapPresses().size() > 0;
        machineUiOpened = machineUiOpened || nodes.values().stream().anyMatch(node -> SCRAP_PRESS.equals(node.canonicalId));
        diagnostics = machineDiagnostics(canonicalCounts);
        syncAggregateState();
        return nodes.size();
    }

    int tick(int ticks) {
        if (ticks <= 0 || !machineUiOpened) {
            return 0;
        }
        int processedTicks = 0;
        for (int tick = 0; tick < ticks; tick++) {
            tickPower();
            for (Node scrapPress : connectedScrapPresses()) {
                MachineRecipe recipe = selectedRecipe(scrapPress);
                if (scrapPress.inputCount < recipe.inputQuantity() || scrapPress.energy <= 0) {
                    continue;
                }
                scrapPress.energy--;
                scrapPress.powerConsumed++;
                scrapPress.recipeProgressTicks++;
                scrapPress.progressTicks++;
                processedTicks++;
                if (scrapPress.progressTicks >= recipe.ticks()) {
                    scrapPress.inputCount -= recipe.inputQuantity();
                    scrapPress.outputCount += recipe.outputQuantity();
                    scrapPress.outputCountBeforeLogistics = scrapPress.outputCount;
                    scrapPress.outputAppeared = true;
                    scrapPress.missionDependency = true;
                    scrapPress.progressTicks = 0;
                    transferOutputToOreGrinder(scrapPress);
                }
            }
        }
        graphConnected = connectedScrapPresses().size() > 0;
        diagnostics = machineDiagnostics(canonicalCounts());
        syncAggregateState();
        return processedTicks;
    }

    boolean acceptsScrapInput(String machineId) {
        Node target = inputTarget(machineId);
        return target != null && SCRAP_PRESS.equals(target.canonicalId);
    }

    MachineInputResult insertScrapInput(String machineId, int quantity) {
        int insertedQuantity = Math.max(0, quantity);
        if (insertedQuantity == 0) {
            return new MachineInputResult(false, normalizeMachineId(machineId), 0, 0, "missing_quantity");
        }
        Node target = inputTarget(machineId);
        if (target == null) {
            return new MachineInputResult(false, normalizeMachineId(machineId), 0, 0, "unknown_machine");
        }
        if (!SCRAP_PRESS.equals(target.canonicalId)) {
            return new MachineInputResult(false, target.id, 0, 0, "unsupported_machine_input");
        }
        target.inputCount += insertedQuantity;
        machineUiOpened = true;
        diagnostics = machineDiagnostics(canonicalCounts());
        syncAggregateState();
        return new MachineInputResult(true, target.id, insertedQuantity, target.inputCount, "inserted");
    }

    int compressedScrapAvailable(String machineId) {
        Node target = outputTarget(machineId);
        return target == null ? 0 : compressedScrapCount(target);
    }

    MachineOutputResult extractCompressedScrap(String machineId, int quantity) {
        int requestedQuantity = Math.max(0, quantity);
        if (requestedQuantity == 0) {
            return new MachineOutputResult(false, normalizeMachineId(machineId), 0, 0, "missing_quantity");
        }
        Node target = outputTarget(machineId);
        if (target == null) {
            return new MachineOutputResult(false, normalizeMachineId(machineId), 0, 0, "unknown_machine");
        }
        int available = compressedScrapCount(target);
        if (available <= 0) {
            return new MachineOutputResult(false, target.id, 0, 0, "no_compressed_scrap");
        }
        int extracted = Math.min(requestedQuantity, available);
        if (SCRAP_PRESS.equals(target.canonicalId)) {
            target.outputCount -= extracted;
        } else if (ORE_GRINDER.equals(target.canonicalId)) {
            target.oreGrinderInputCount -= extracted;
        } else {
            return new MachineOutputResult(false, target.id, 0, 0, "unsupported_machine_output");
        }
        machineUiOpened = true;
        diagnostics = machineDiagnostics(canonicalCounts());
        syncAggregateState();
        return new MachineOutputResult(true, target.id, extracted, compressedScrapCount(target), "extracted");
    }

    MachineRecipeSelectionResult selectRecipe(String machineRecipeTargetId) {
        MachineRecipeTarget target = machineRecipeTarget(machineRecipeTargetId);
        if (target.machineId().isBlank() || target.recipeId().isBlank()) {
            return new MachineRecipeSelectionResult(false, target.machineId(), target.recipeId(), "", false, "missing_target");
        }
        Node machine = inputTarget(target.machineId());
        if (machine == null) {
            return new MachineRecipeSelectionResult(
                    false,
                    target.machineId(),
                    target.recipeId(),
                    "",
                    false,
                    "unknown_machine"
            );
        }
        if (!SCRAP_PRESS.equals(machine.canonicalId)) {
            return new MachineRecipeSelectionResult(
                    false,
                    machine.id,
                    target.recipeId(),
                    machine.selectedRecipeId,
                    false,
                    "unsupported_machine_recipe"
            );
        }
        MachineRecipe recipe = recipeById(target.recipeId());
        if (recipe == null) {
            return new MachineRecipeSelectionResult(
                    false,
                    machine.id,
                    target.recipeId(),
                    machine.selectedRecipeId,
                    false,
                    "unknown_recipe"
            );
        }
        boolean changed = !recipe.id().equals(machine.selectedRecipeId);
        machine.selectedRecipeId = recipe.id();
        if (changed) {
            machine.progressTicks = 0;
        }
        machineUiOpened = true;
        diagnostics = machineDiagnostics(canonicalCounts());
        syncAggregateState();
        return new MachineRecipeSelectionResult(true, machine.id, recipe.id(), recipe.id(), changed, "selected");
    }

    private static EchoClientMachineRuntime emptyGraph() {
        EchoClientMachineRuntime runtime = new EchoClientMachineRuntime();
        for (String entityId : List.of(
                MICRO_GENERATOR,
                POWER_CABLE,
                LOAD_DISTRIBUTOR,
                BATTERY_BANK,
                SCRAP_PRESS,
                ITEM_PIPE,
                ORE_GRINDER
        )) {
            runtime.nodes.put(entityId, defaultNode(entityId));
        }
        return runtime;
    }

    private void applyAggregateStateToPrimary(
            int inputCount,
            int outputCount,
            int grinderInputCount,
            int progressTicks,
            int totalRecipeTicks,
            int consumedPower,
            boolean appearedOutput,
            int outputBeforeLogistics,
            boolean missionComplete
    ) {
        Node scrapPress = primaryNode(SCRAP_PRESS);
        if (scrapPress != null) {
            scrapPress.inputCount = inputCount;
            scrapPress.outputCount = outputCount;
            scrapPress.progressTicks = progressTicks;
            scrapPress.recipeProgressTicks = totalRecipeTicks;
            scrapPress.powerConsumed = consumedPower;
            scrapPress.outputAppeared = appearedOutput;
            scrapPress.outputCountBeforeLogistics = outputBeforeLogistics;
            scrapPress.missionDependency = missionComplete;
        }
        Node oreGrinder = primaryNode(ORE_GRINDER);
        if (oreGrinder != null) {
            oreGrinder.oreGrinderInputCount = grinderInputCount;
        }
    }

    private void transferOutputToOreGrinder(Node scrapPress) {
        if (scrapPress.outputCount <= 0) {
            return;
        }
        Node oreGrinder = firstReachableNode(scrapPress.id, ORE_GRINDER);
        if (oreGrinder == null) {
            return;
        }
        scrapPress.outputCount--;
        oreGrinder.oreGrinderInputCount++;
    }

    private void tickPower() {
        for (Node node : nodes.values()) {
            if (MICRO_GENERATOR.equals(node.canonicalId)) {
                node.energy = Math.min(node.capacity, node.energy + node.generationPerTick);
            }
        }
        for (Node node : nodes.values()) {
            if (MICRO_GENERATOR.equals(node.canonicalId)) {
                moveEnergyToNeighbors(node, POWER_CABLE, 8);
            }
        }
        for (Node node : nodes.values()) {
            if (POWER_CABLE.equals(node.canonicalId)) {
                moveEnergyToNeighbors(node, LOAD_DISTRIBUTOR, 8);
            }
        }
        for (Node node : nodes.values()) {
            if (LOAD_DISTRIBUTOR.equals(node.canonicalId)) {
                moveEnergyToReachable(node, SCRAP_PRESS, 1);
                moveEnergyToReachable(node, BATTERY_BANK, 7);
            }
        }
    }

    private void moveEnergyToNeighbors(Node source, String targetCanonicalId, int requested) {
        if (source == null || requested <= 0) {
            return;
        }
        for (String neighborId : source.neighbors) {
            Node target = nodes.get(neighborId);
            if (target != null && targetCanonicalId.equals(target.canonicalId)) {
                moveEnergy(source, target, requested);
            }
        }
    }

    private void moveEnergyToReachable(Node source, String targetCanonicalId, int requested) {
        if (source == null || requested <= 0) {
            return;
        }
        for (Node target : nodes.values()) {
            if (targetCanonicalId.equals(target.canonicalId) && hasRoute(source.id, target.id)) {
                moveEnergy(source, target, requested);
            }
        }
    }

    private static void moveEnergy(Node source, Node target, int requested) {
        int moved = Math.min(requested, Math.min(source.energy, target.capacity - target.energy));
        if (moved <= 0) {
            return;
        }
        source.energy -= moved;
        target.energy += moved;
    }

    private void link(String left, String right) {
        Node leftNode = nodes.get(left);
        Node rightNode = nodes.get(right);
        if (leftNode == null || rightNode == null) {
            return;
        }
        leftNode.neighbors.add(right);
        rightNode.neighbors.add(left);
    }

    private void rebuildPlacedLinks() {
        for (Node node : nodes.values()) {
            node.neighbors.clear();
        }
        ArrayList<Node> placed = new ArrayList<>(nodes.values());
        for (int leftIndex = 0; leftIndex < placed.size(); leftIndex++) {
            Node left = placed.get(leftIndex);
            for (int rightIndex = leftIndex + 1; rightIndex < placed.size(); rightIndex++) {
                Node right = placed.get(rightIndex);
                if (manhattanDistance(left, right) == 1 && canLink(left, right)) {
                    link(left.id, right.id);
                }
            }
        }
    }

    private static int manhattanDistance(Node left, Node right) {
        return Math.abs(left.x - right.x)
                + Math.abs(left.y - right.y)
                + Math.abs(left.z - right.z);
    }

    private static boolean canLink(Node left, Node right) {
        if (ITEM_PIPE.equals(left.canonicalId) || ITEM_PIPE.equals(right.canonicalId)) {
            return isMachineEndpoint(left.canonicalId) || isMachineEndpoint(right.canonicalId);
        }
        return !ORE_GRINDER.equals(left.canonicalId) && !ORE_GRINDER.equals(right.canonicalId);
    }

    private static boolean isMachineEndpoint(String entityId) {
        return SCRAP_PRESS.equals(entityId) || ORE_GRINDER.equals(entityId);
    }

    private List<Node> connectedScrapPresses() {
        ArrayList<Node> result = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (SCRAP_PRESS.equals(node.canonicalId)
                    && hasRouteToCanonical(node.id, MICRO_GENERATOR)
                    && hasRouteToCanonical(node.id, ORE_GRINDER)) {
                result.add(node);
            }
        }
        return result;
    }

    private boolean hasRouteToCanonical(String from, String canonicalId) {
        return firstReachableNode(from, canonicalId) != null;
    }

    private Node firstReachableNode(String from, String canonicalId) {
        ArrayList<String> queue = new ArrayList<>();
        LinkedHashSet<String> visited = new LinkedHashSet<>();
        queue.add(from);
        visited.add(from);
        for (int index = 0; index < queue.size(); index++) {
            String current = queue.get(index);
            Node node = nodes.get(current);
            if (node == null) {
                continue;
            }
            if (canonicalId.equals(node.canonicalId) && !current.equals(from)) {
                return node;
            }
            for (String next : node.neighbors) {
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return null;
    }

    private boolean hasRoute(String from, String to) {
        ArrayList<String> queue = new ArrayList<>();
        LinkedHashSet<String> visited = new LinkedHashSet<>();
        queue.add(from);
        visited.add(from);
        for (int index = 0; index < queue.size(); index++) {
            String current = queue.get(index);
            if (current.equals(to)) {
                return true;
            }
            Node node = nodes.get(current);
            if (node == null) {
                continue;
            }
            for (String next : node.neighbors) {
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return false;
    }

    private List<String> machineDiagnostics(Map<String, Integer> canonicalCounts) {
        ArrayList<String> result = new ArrayList<>();
        int connectedGraphs = connectedScrapPresses().size();
        if (connectedGraphs > 1) {
            result.add("multi-network machine graphs connected=" + connectedGraphs);
        }
        for (Node node : nodes.values()) {
            if (SCRAP_PRESS.equals(node.canonicalId)
                    && (!hasRouteToCanonical(node.id, MICRO_GENERATOR)
                    || !hasRouteToCanonical(node.id, ORE_GRINDER))) {
                result.add(node.id + " @ " + coordinate(node.x, node.y, node.z)
                        + " waiting for powered logistics route");
            }
        }
        return List.copyOf(result);
    }

    private Map<String, Integer> canonicalCounts() {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Node node : nodes.values()) {
            result.put(node.canonicalId, result.getOrDefault(node.canonicalId, 0) + 1);
        }
        return Map.copyOf(result);
    }

    private List<EchoClientMachineStateSnapshot.PowerNode> powerGraph() {
        ArrayList<EchoClientMachineStateSnapshot.PowerNode> result = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (POWER_GRAPH_IDS.contains(node.canonicalId)) {
                result.add(node.snapshot());
            }
        }
        return List.copyOf(result);
    }

    private List<EchoClientMachineStateSnapshot.InventoryPort> inventoryPorts() {
        ArrayList<EchoClientMachineStateSnapshot.InventoryPort> result = new ArrayList<>();
        for (Node node : nodes.values()) {
            if (SCRAP_PRESS.equals(node.canonicalId)) {
                result.add(new EchoClientMachineStateSnapshot.InventoryPort(node.id, "input", List.of("scrap_metal")));
                result.add(new EchoClientMachineStateSnapshot.InventoryPort(
                        node.id,
                        "output",
                        List.of("compressed_scrap")
                ));
            }
            if (ORE_GRINDER.equals(node.canonicalId)) {
                result.add(new EchoClientMachineStateSnapshot.InventoryPort(
                        node.id,
                        "input",
                        List.of("compressed_scrap")
                ));
            }
        }
        return List.copyOf(result);
    }

    private List<EchoClientMachineStateSnapshot.BlockEntity> blockEntities() {
        ArrayList<EchoClientMachineStateSnapshot.BlockEntity> result = new ArrayList<>();
        for (Node node : nodes.values()) {
            result.add(node.blockEntity(blockEntityState(node)));
        }
        return List.copyOf(result);
    }

    private Map<String, String> blockEntityState(Node node) {
        TreeMap<String, String> state = new TreeMap<>();
        state.put("canonicalId", node.canonicalId);
        state.put("instanceId", node.id);
        state.put("energy", Integer.toString(node.energy));
        state.put("capacity", Integer.toString(node.capacity));
        state.put("transferPerTick", Integer.toString(node.transferPerTick));
        state.put("generationPerTick", Integer.toString(node.generationPerTick));
        state.put("neighbors", pipeText(node.neighbors));
        if (LOAD_DISTRIBUTOR.equals(node.canonicalId)) {
            state.put("graphConnected", Boolean.toString(graphConnected));
            state.put("instanceGraphConnected", Boolean.toString(hasRouteToCanonical(node.id, SCRAP_PRESS)));
            state.put("machineUiOpened", Boolean.toString(machineUiOpened));
            state.put("stateReloaded", Boolean.toString(stateReloaded));
        }
        if (SCRAP_PRESS.equals(node.canonicalId)) {
            state.put("inputCount", Integer.toString(node.inputCount));
            state.put("outputCount", Integer.toString(node.outputCount));
            state.put("containerId", "machine:" + node.id);
            state.put("selectedRecipe", selectedRecipe(node).id());
            state.put("recipeOptions", recipeOptionsText(node.canonicalId));
            state.put("slot.input.item", SCRAP_METAL_ITEM_ID);
            state.put("slot.input.count", Integer.toString(node.inputCount));
            state.put("slot.output.item", COMPRESSED_SCRAP_ITEM_ID);
            state.put("slot.output.count", Integer.toString(node.outputCount));
            state.put("scrapPressProgressTicks", Integer.toString(node.progressTicks));
            state.put("recipeProgressTicks", Integer.toString(node.recipeProgressTicks));
            state.put("powerConsumed", Integer.toString(node.powerConsumed));
            state.put("outputAppeared", Boolean.toString(node.outputAppeared));
            state.put("outputCountBeforeLogistics", Integer.toString(node.outputCountBeforeLogistics));
            state.put("missionDependency", Boolean.toString(node.missionDependency));
            state.put("port.input", "scrap_metal");
            state.put("port.output", "compressed_scrap");
        }
        if (ORE_GRINDER.equals(node.canonicalId)) {
            state.put("inputCount", Integer.toString(node.oreGrinderInputCount));
            state.put("containerId", "machine:" + node.id);
            state.put("slot.input.item", COMPRESSED_SCRAP_ITEM_ID);
            state.put("slot.input.count", Integer.toString(node.oreGrinderInputCount));
            state.put("port.input", "compressed_scrap");
        }
        return Map.copyOf(state);
    }

    private void syncAggregateState() {
        Node scrapPress = primaryNode(SCRAP_PRESS);
        if (scrapPress == null) {
            scrapPressInputCount = 0;
            scrapPressOutputCount = 0;
            scrapPressProgressTicks = 0;
            recipeProgressTicks = 0;
            powerConsumed = 0;
            outputAppeared = false;
            outputCountBeforeLogistics = 0;
            missionDependency = false;
        } else {
            scrapPressInputCount = scrapPress.inputCount;
            scrapPressOutputCount = scrapPress.outputCount;
            scrapPressProgressTicks = scrapPress.progressTicks;
            recipeProgressTicks = scrapPress.recipeProgressTicks;
            powerConsumed = scrapPress.powerConsumed;
            outputAppeared = scrapPress.outputAppeared;
            outputCountBeforeLogistics = scrapPress.outputCountBeforeLogistics;
            missionDependency = scrapPress.missionDependency;
        }
        Node oreGrinder = primaryNode(ORE_GRINDER);
        oreGrinderInputCount = oreGrinder == null ? 0 : oreGrinder.oreGrinderInputCount;
    }

    private Node primaryNode(String canonicalId) {
        Node canonical = nodes.get(canonicalId);
        if (canonical != null && canonicalId.equals(canonical.canonicalId)) {
            return canonical;
        }
        for (Node node : nodes.values()) {
            if (canonicalId.equals(node.canonicalId)) {
                return node;
            }
        }
        return null;
    }

    private Node inputTarget(String machineId) {
        String normalized = normalizeMachineId(machineId);
        if (normalized.isBlank()) {
            return primaryNode(SCRAP_PRESS);
        }
        Node exact = nodes.get(normalized);
        if (exact != null) {
            return exact;
        }
        return null;
    }

    private Node outputTarget(String machineId) {
        String normalized = normalizeMachineId(machineId);
        if (normalized.isBlank()) {
            return primaryNode(ORE_GRINDER);
        }
        Node exact = nodes.get(normalized);
        if (exact != null) {
            return exact;
        }
        return null;
    }

    private static int compressedScrapCount(Node node) {
        if (node == null) {
            return 0;
        }
        if (SCRAP_PRESS.equals(node.canonicalId)) {
            return node.outputCount;
        }
        if (ORE_GRINDER.equals(node.canonicalId)) {
            return node.oreGrinderInputCount;
        }
        return 0;
    }

    private static MachineRecipe selectedRecipe(Node node) {
        MachineRecipe recipe = node == null ? null : recipeById(node.selectedRecipeId);
        return recipe == null ? SCRAP_PRESS_RECIPES.getFirst() : recipe;
    }

    private static MachineRecipe recipeById(String recipeId) {
        if (recipeId == null || recipeId.isBlank()) {
            return null;
        }
        String normalized = recipeId.trim();
        for (MachineRecipe recipe : SCRAP_PRESS_RECIPES) {
            if (recipe.id().equals(normalized)) {
                return recipe;
            }
        }
        return null;
    }

    private static String defaultRecipeId(String canonicalId) {
        return SCRAP_PRESS.equals(canonicalId) ? SCRAP_PRESS_COMPRESSED_RECIPE_ID : "";
    }

    private static String recipeOptionsText(String canonicalId) {
        if (!SCRAP_PRESS.equals(canonicalId)) {
            return "";
        }
        return pipeText(SCRAP_PRESS_RECIPES.stream().map(MachineRecipe::id).toList());
    }

    private static MachineRecipeTarget machineRecipeTarget(String value) {
        if (value == null || value.isBlank()) {
            return new MachineRecipeTarget("", "");
        }
        String trimmed = value.trim();
        int separator = trimmed.indexOf('|');
        if (separator < 0) {
            return new MachineRecipeTarget(normalizeMachineId(trimmed), "");
        }
        return new MachineRecipeTarget(
                normalizeMachineId(trimmed.substring(0, separator)),
                trimmed.substring(separator + 1).trim()
        );
    }

    private static String normalizeMachineId(String machineId) {
        if (machineId == null || machineId.isBlank()) {
            return "";
        }
        String normalized = machineId.trim();
        int portSeparator = normalized.indexOf('/');
        if (portSeparator >= 0) {
            normalized = normalized.substring(0, portSeparator);
        }
        return normalized.trim();
    }

    private static Optional<String> machineCanonicalId(EchoVoxelBlockInstance block) {
        String canonical = block.state().property("canonicalId").orElse("");
        if (defaultNode(canonical) != null) {
            return Optional.of(canonical);
        }
        String explicit = block.state().property("blockEntityId").orElse("");
        Optional<String> explicitCanonical = canonicalIdFromInstanceId(explicit);
        if (explicitCanonical.isPresent()) {
            return explicitCanonical;
        }
        return Optional.ofNullable(entityIdForBlockId(block.block().id()));
    }

    private static String instanceIdForPlacement(
            String canonicalId,
            EchoVoxelBlockInstance block,
            int seen,
            Map<String, Node> placedNodes
    ) {
        String explicit = block.state().property("blockEntityId").orElse("").trim();
        String candidate = "";
        if (canonicalIdFromInstanceId(explicit).filter(canonicalId::equals).isPresent()) {
            candidate = explicit;
        }
        if (candidate.isBlank() || (candidate.equals(canonicalId) && seen > 0)) {
            candidate = seen == 0 ? canonicalId : instanceId(canonicalId, block.x(), block.y(), block.z());
        }
        if (!placedNodes.containsKey(candidate)) {
            return candidate;
        }
        return instanceId(canonicalId, block.x(), block.y(), block.z());
    }

    private static String instanceId(String canonicalId, int x, int y, int z) {
        return canonicalId + "@" + coordinate(x, y, z);
    }

    private static String coordinate(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    private static String canonicalIdForBlockEntity(EchoClientMachineStateSnapshot.BlockEntity blockEntity) {
        String stateCanonical = blockEntity.state().get("canonicalId");
        if (defaultNode(stateCanonical) != null) {
            return stateCanonical;
        }
        return canonicalIdFromInstanceId(blockEntity.entityId())
                .orElseGet(() -> entityIdForBlockId(blockEntity.blockId()));
    }

    private static Optional<String> canonicalIdFromInstanceId(String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return Optional.empty();
        }
        String trimmed = entityId.trim();
        String canonical = trimmed.contains("@") ? trimmed.substring(0, trimmed.indexOf('@')) : trimmed;
        return defaultNode(canonical) == null ? Optional.empty() : Optional.of(canonical);
    }

    private static String entityIdForBlockId(String blockId) {
        return switch (blockId) {
            case EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID -> MICRO_GENERATOR;
            case EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID,
                 EchoAdapterCoreStandaloneContentBridge.REINFORCED_POWER_CABLE_BLOCK_ID,
                 EchoAdapterCoreStandaloneContentBridge.HIGH_VOLTAGE_POWER_CABLE_BLOCK_ID -> POWER_CABLE;
            case EchoAdapterCoreStandaloneContentBridge.LOAD_DISTRIBUTOR_BLOCK_ID -> LOAD_DISTRIBUTOR;
            case EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID -> BATTERY_BANK;
            case EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID -> SCRAP_PRESS;
            case EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID -> ITEM_PIPE;
            case EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID -> ORE_GRINDER;
            default -> null;
        };
    }

    private static Node defaultNode(String entityId) {
        if (entityId == null || entityId.isBlank()) {
            return null;
        }
        return switch (entityId) {
            case MICRO_GENERATOR -> new Node(
                    MICRO_GENERATOR,
                    MICRO_GENERATOR,
                    "GENERATOR",
                    3_000,
                    64,
                    8,
                    EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID,
                    4,
                    5,
                    9
            );
            case POWER_CABLE -> new Node(
                    POWER_CABLE,
                    POWER_CABLE,
                    "CABLE",
                    1_000,
                    50,
                    0,
                    EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID,
                    5,
                    5,
                    9
            );
            case LOAD_DISTRIBUTOR -> new Node(
                    LOAD_DISTRIBUTOR,
                    LOAD_DISTRIBUTOR,
                    "ROUTER",
                    2_000,
                    512,
                    0,
                    EchoAdapterCoreStandaloneContentBridge.LOAD_DISTRIBUTOR_BLOCK_ID,
                    6,
                    5,
                    9
            );
            case BATTERY_BANK -> new Node(
                    BATTERY_BANK,
                    BATTERY_BANK,
                    "BATTERY",
                    10_000,
                    100,
                    0,
                    EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID,
                    7,
                    5,
                    9
            );
            case SCRAP_PRESS -> {
                Node node = new Node(
                        SCRAP_PRESS,
                        SCRAP_PRESS,
                        "MACHINE",
                        1_500,
                        128,
                        0,
                        EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID,
                        8,
                        5,
                        9
                );
                node.inputCount = 9;
                yield node;
            }
            case ITEM_PIPE -> new Node(
                    ITEM_PIPE,
                    ITEM_PIPE,
                    "INVENTORY_PIPE",
                    0,
                    0,
                    0,
                    EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID,
                    9,
                    5,
                    9
            );
            case ORE_GRINDER -> new Node(
                    ORE_GRINDER,
                    ORE_GRINDER,
                    "MACHINE",
                    2_000,
                    128,
                    0,
                    EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID,
                    10,
                    5,
                    9
            );
            default -> null;
        };
    }

    private static String pipeText(Iterable<String> values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(value.trim().replace("|", "%7C"));
        }
        return builder.toString();
    }

    private static List<String> pipeValues(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (String value : text.split("\\|")) {
            if (!value.isBlank()) {
                result.add(value.replace("%7C", "|"));
            }
        }
        return List.copyOf(result);
    }

    private static int intValue(Map<String, String> state, String key, int fallback) {
        String value = state.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int slotCount(Map<String, String> state, String slot, int fallback) {
        String item = state.get("slot." + slot + ".item");
        if (item == null || item.isBlank()) {
            return fallback;
        }
        return intValue(state, "slot." + slot + ".count", fallback);
    }

    private static boolean booleanValue(Map<String, String> state, String key, boolean fallback) {
        String value = state.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(value);
    }

    record MachineBlockDefinition(String entityId, String kind) {}

    record MachineInputResult(
            boolean success,
            String machineId,
            int insertedQuantity,
            int inputCount,
            String reason
    ) {
        MachineInputResult {
            machineId = machineId == null ? "" : machineId.trim();
            if (insertedQuantity < 0) {
                insertedQuantity = 0;
            }
            if (inputCount < 0) {
                inputCount = 0;
            }
            reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
        }
    }

    record MachineOutputResult(
            boolean success,
            String machineId,
            int extractedQuantity,
            int outputCount,
            String reason
    ) {
        MachineOutputResult {
            machineId = machineId == null ? "" : machineId.trim();
            if (extractedQuantity < 0) {
                extractedQuantity = 0;
            }
            if (outputCount < 0) {
                outputCount = 0;
            }
            reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
        }
    }

    record MachineRecipeSelectionResult(
            boolean success,
            String machineId,
            String recipeId,
            String selectedRecipeId,
            boolean changed,
            String reason
    ) {
        MachineRecipeSelectionResult {
            machineId = machineId == null ? "" : machineId.trim();
            recipeId = recipeId == null ? "" : recipeId.trim();
            selectedRecipeId = selectedRecipeId == null ? "" : selectedRecipeId.trim();
            reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
        }
    }

    private record MachineRecipe(
            String id,
            String label,
            String inputItemId,
            int inputQuantity,
            String outputItemId,
            int outputQuantity,
            int ticks
    ) {
        private MachineRecipe {
            id = id == null ? "" : id.trim();
            label = label == null || label.isBlank() ? id : label.trim();
            inputItemId = inputItemId == null ? "" : inputItemId.trim();
            inputQuantity = Math.max(1, inputQuantity);
            outputItemId = outputItemId == null ? "" : outputItemId.trim();
            outputQuantity = Math.max(1, outputQuantity);
            ticks = Math.max(1, ticks);
        }
    }

    private record MachineRecipeTarget(String machineId, String recipeId) {
        private MachineRecipeTarget {
            machineId = machineId == null ? "" : machineId.trim();
            recipeId = recipeId == null ? "" : recipeId.trim();
        }
    }

    private static final class Node {
        private final String id;
        private final String canonicalId;
        private String kind;
        private int capacity;
        private int transferPerTick;
        private int generationPerTick;
        private String blockId;
        private int x;
        private int y;
        private int z;
        private final LinkedHashSet<String> neighbors = new LinkedHashSet<>();
        private int energy;
        private int inputCount;
        private int outputCount;
        private int progressTicks;
        private int recipeProgressTicks;
        private int powerConsumed;
        private boolean outputAppeared;
        private int outputCountBeforeLogistics;
        private boolean missionDependency;
        private int oreGrinderInputCount;
        private String selectedRecipeId;

        private Node(
                String id,
                String canonicalId,
                String kind,
                int capacity,
                int transferPerTick,
                int generationPerTick,
                String blockId,
                int x,
                int y,
                int z
        ) {
            this.id = id;
            this.canonicalId = canonicalId;
            this.kind = kind;
            this.capacity = Math.max(0, capacity);
            this.transferPerTick = Math.max(0, transferPerTick);
            this.generationPerTick = Math.max(0, generationPerTick);
            this.blockId = blockId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.selectedRecipeId = defaultRecipeId(canonicalId);
        }

        private Node withInstanceId(String nextId) {
            return withPlacement(nextId, canonicalId, blockId, kind, x, y, z);
        }

        private Node withPlacement(
                String nextId,
                String nextCanonicalId,
                String nextBlockId,
                String nextKind,
                int nextX,
                int nextY,
                int nextZ
        ) {
            Node node = new Node(
                    nextId,
                    nextCanonicalId,
                    nextKind,
                    capacity,
                    transferPerTick,
                    generationPerTick,
                    nextBlockId,
                    nextX,
                    nextY,
                    nextZ
            );
            node.energy = energy;
            node.inputCount = inputCount;
            node.outputCount = outputCount;
            node.progressTicks = progressTicks;
            node.recipeProgressTicks = recipeProgressTicks;
            node.powerConsumed = powerConsumed;
            node.outputAppeared = outputAppeared;
            node.outputCountBeforeLogistics = outputCountBeforeLogistics;
            node.missionDependency = missionDependency;
            node.oreGrinderInputCount = oreGrinderInputCount;
            node.selectedRecipeId = selectedRecipeId;
            return node;
        }

        private void applyBlockEntity(EchoClientMachineStateSnapshot.BlockEntity blockEntity) {
            kind = blockEntity.kind();
            blockId = blockEntity.blockId();
            x = blockEntity.x();
            y = blockEntity.y();
            z = blockEntity.z();
            Map<String, String> state = blockEntity.state();
            energy = intValue(state, "energy", energy);
            capacity = intValue(state, "capacity", capacity);
            transferPerTick = intValue(state, "transferPerTick", transferPerTick);
            generationPerTick = intValue(state, "generationPerTick", generationPerTick);
            inputCount = intValue(state, "inputCount", inputCount);
            outputCount = intValue(state, "outputCount", outputCount);
            inputCount = slotCount(state, "input", inputCount);
            outputCount = slotCount(state, "output", outputCount);
            progressTicks = intValue(state, "scrapPressProgressTicks", progressTicks);
            recipeProgressTicks = intValue(state, "recipeProgressTicks", recipeProgressTicks);
            powerConsumed = intValue(state, "powerConsumed", powerConsumed);
            outputAppeared = booleanValue(state, "outputAppeared", outputAppeared);
            outputCountBeforeLogistics = intValue(state, "outputCountBeforeLogistics", outputCountBeforeLogistics);
            missionDependency = booleanValue(state, "missionDependency", missionDependency);
            String savedRecipe = state.get("selectedRecipe");
            if (SCRAP_PRESS.equals(canonicalId) && recipeById(savedRecipe) != null) {
                selectedRecipeId = savedRecipe.trim();
            }
            if (ORE_GRINDER.equals(canonicalId)) {
                oreGrinderInputCount = intValue(state, "inputCount", oreGrinderInputCount);
                oreGrinderInputCount = slotCount(state, "input", oreGrinderInputCount);
            }
            List<String> savedNeighbors = pipeValues(state.get("neighbors"));
            if (!savedNeighbors.isEmpty()) {
                neighbors.clear();
                neighbors.addAll(savedNeighbors);
            }
        }

        private void applyPowerNode(EchoClientMachineStateSnapshot.PowerNode snapshot) {
            kind = snapshot.kind();
            energy = snapshot.energy();
            capacity = snapshot.capacity();
            transferPerTick = snapshot.transferPerTick();
            generationPerTick = snapshot.generationPerTick();
            neighbors.clear();
            neighbors.addAll(snapshot.neighbors());
        }

        private EchoClientMachineStateSnapshot.PowerNode snapshot() {
            return new EchoClientMachineStateSnapshot.PowerNode(
                    id,
                    kind,
                    energy,
                    capacity,
                    transferPerTick,
                    generationPerTick,
                    List.copyOf(neighbors)
            );
        }

        private EchoClientMachineStateSnapshot.BlockEntity blockEntity(Map<String, String> state) {
            return new EchoClientMachineStateSnapshot.BlockEntity(id, blockId, kind, x, y, z, state);
        }
    }
}
