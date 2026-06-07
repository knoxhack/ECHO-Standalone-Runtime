package dev.echo.standalone.runtime.client;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

record EchoClientMachineStateSnapshot(
        boolean graphConnected,
        boolean machineUiOpened,
        int scrapPressInputCount,
        int scrapPressOutputCount,
        int oreGrinderInputCount,
        int scrapPressProgressTicks,
        int recipeProgressTicks,
        int powerConsumed,
        boolean outputAppeared,
        int outputCountBeforeLogistics,
        boolean stateReloaded,
        boolean missionDependency,
        List<PowerNode> powerGraph,
        List<InventoryPort> inventoryPorts,
        List<BlockEntity> blockEntities,
        List<String> diagnostics
) {
    EchoClientMachineStateSnapshot(
            boolean graphConnected,
            boolean machineUiOpened,
            int scrapPressInputCount,
            int scrapPressOutputCount,
            int oreGrinderInputCount,
            int scrapPressProgressTicks,
            int recipeProgressTicks,
            int powerConsumed,
            boolean outputAppeared,
            int outputCountBeforeLogistics,
            boolean stateReloaded,
            boolean missionDependency,
            List<PowerNode> powerGraph,
            List<InventoryPort> inventoryPorts
    ) {
        this(
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
                powerGraph,
                inventoryPorts,
                List.of()
        );
    }

    EchoClientMachineStateSnapshot(
            boolean graphConnected,
            boolean machineUiOpened,
            int scrapPressInputCount,
            int scrapPressOutputCount,
            int oreGrinderInputCount,
            int scrapPressProgressTicks,
            int recipeProgressTicks,
            int powerConsumed,
            boolean outputAppeared,
            int outputCountBeforeLogistics,
            boolean stateReloaded,
            boolean missionDependency,
            List<PowerNode> powerGraph,
            List<InventoryPort> inventoryPorts,
            List<BlockEntity> blockEntities
    ) {
        this(
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
                powerGraph,
                inventoryPorts,
                blockEntities,
                List.of()
        );
    }

    EchoClientMachineStateSnapshot {
        if (scrapPressInputCount < 0) {
            scrapPressInputCount = 0;
        }
        if (scrapPressOutputCount < 0) {
            scrapPressOutputCount = 0;
        }
        if (oreGrinderInputCount < 0) {
            oreGrinderInputCount = 0;
        }
        if (scrapPressProgressTicks < 0) {
            scrapPressProgressTicks = 0;
        }
        if (recipeProgressTicks < 0) {
            recipeProgressTicks = 0;
        }
        if (powerConsumed < 0) {
            powerConsumed = 0;
        }
        if (outputCountBeforeLogistics < 0) {
            outputCountBeforeLogistics = 0;
        }
        powerGraph = powerGraph == null ? List.of() : List.copyOf(powerGraph);
        inventoryPorts = inventoryPorts == null ? List.of() : List.copyOf(inventoryPorts);
        blockEntities = blockEntities == null ? List.of() : List.copyOf(blockEntities);
        diagnostics = cleanDiagnostics(diagnostics);
    }

    static EchoClientMachineStateSnapshot reference() {
        return EchoClientMachineRuntime.reference().snapshot();
    }

    static EchoClientMachineStateSnapshot empty() {
        return new EchoClientMachineStateSnapshot(
                false,
                false,
                0,
                0,
                0,
                0,
                0,
                0,
                false,
                0,
                false,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    EchoClientMachineStateSnapshot withStateReloaded(boolean value) {
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
                value,
                missionDependency,
                powerGraph,
                inventoryPorts,
                blockEntities,
                diagnostics
        );
    }

    record PowerNode(
            String id,
            String kind,
            int energy,
            int capacity,
            int transferPerTick,
            int generationPerTick,
            List<String> neighbors
    ) {
        PowerNode {
            id = requireText(id, "id");
            kind = requireText(kind, "kind");
            if (energy < 0) {
                energy = 0;
            }
            if (capacity < 0) {
                capacity = 0;
            }
            if (transferPerTick < 0) {
                transferPerTick = 0;
            }
            if (generationPerTick < 0) {
                generationPerTick = 0;
            }
            neighbors = neighbors == null ? List.of() : List.copyOf(neighbors);
        }
    }

    record InventoryPort(
            String machineId,
            String port,
            List<String> accepts
    ) {
        InventoryPort {
            machineId = requireText(machineId, "machineId");
            port = requireText(port, "port");
            accepts = accepts == null ? List.of() : List.copyOf(accepts);
        }

        String label() {
            return machineId + "/" + port + ": " + String.join(", ", accepts);
        }
    }

    record BlockEntity(
            String entityId,
            String blockId,
            String kind,
            int x,
            int y,
            int z,
            int chunkX,
            int chunkY,
            int chunkZ,
            int localX,
            int localY,
            int localZ,
            Map<String, String> state
    ) {
        private static final int CHUNK_SIZE = 16;

        BlockEntity(
                String entityId,
                String blockId,
                String kind,
                int x,
                int y,
                int z,
                Map<String, String> state
        ) {
            this(
                    entityId,
                    blockId,
                    kind,
                    x,
                    y,
                    z,
                    Math.floorDiv(x, CHUNK_SIZE),
                    Math.floorDiv(y, CHUNK_SIZE),
                    Math.floorDiv(z, CHUNK_SIZE),
                    Math.floorMod(x, CHUNK_SIZE),
                    Math.floorMod(y, CHUNK_SIZE),
                    Math.floorMod(z, CHUNK_SIZE),
                    state
            );
        }

        BlockEntity {
            entityId = requireText(entityId, "entityId");
            blockId = requireText(blockId, "blockId");
            kind = requireText(kind, "kind");
            chunkX = Math.floorDiv(x, CHUNK_SIZE);
            chunkY = Math.floorDiv(y, CHUNK_SIZE);
            chunkZ = Math.floorDiv(z, CHUNK_SIZE);
            localX = Math.floorMod(x, CHUNK_SIZE);
            localY = Math.floorMod(y, CHUNK_SIZE);
            localZ = Math.floorMod(z, CHUNK_SIZE);
            state = state == null ? Map.of() : Map.copyOf(new TreeMap<>(state));
        }

        String label() {
            return entityId + " @ " + x + "," + y + "," + z
                    + " chunk " + chunkX + "," + chunkY + "," + chunkZ;
        }
    }

    private static String requireText(String value, String name) {
        String text = Objects.requireNonNull(value, name).trim();
        if (text.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return text;
    }

    private static List<String> cleanDiagnostics(List<String> diagnostics) {
        if (diagnostics == null || diagnostics.isEmpty()) {
            return List.of();
        }
        return diagnostics.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
