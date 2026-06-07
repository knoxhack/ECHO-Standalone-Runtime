package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreAgent9TechRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoTerminalStandaloneAdapter;
import dev.echo.standalone.runtime.gameplay.EchoAshfallStandaloneMissionRuntime;
import dev.echo.standalone.runtime.ui.EchoAshfallMissionUiBridge;
import dev.echo.standalone.runtime.ui.EchoTerminalCommand;
import dev.echo.standalone.runtime.ui.EchoTerminalCommandRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

record EchoClientTechSurfaceModel(
        boolean available,
        String status,
        String error,
        String adapterCoreContract,
        boolean machineUiOpened,
        int insertedInputCount,
        int recipeProgressTicks,
        int powerConsumed,
        boolean outputAppeared,
        int outputCountBeforeLogistics,
        int oreGrinderInputCount,
        boolean stateReloaded,
        boolean missionDependency,
        List<EchoClientTechPowerNode> powerGraph,
        List<String> inventoryPorts,
        List<String> blockEntities,
        List<String> machineDiagnostics,
        List<EchoClientMachineContainerSlot> machineContainerSlots,
        List<EchoClientMachineRecipeOption> machineRecipeOptions,
        String terminalPageId,
        String terminalService,
        String terminalCommand,
        List<String> terminalCards,
        List<String> terminalActions,
        List<String> terminalDiagnostics,
        List<String> terminalCommands,
        int terminalRuntimeCommandCount
) {
    EchoClientTechSurfaceModel {
        status = text(status);
        error = text(error);
        adapterCoreContract = text(adapterCoreContract);
        powerGraph = powerGraph == null ? List.of() : List.copyOf(powerGraph);
        inventoryPorts = inventoryPorts == null ? List.of() : List.copyOf(inventoryPorts);
        blockEntities = blockEntities == null ? List.of() : List.copyOf(blockEntities);
        machineDiagnostics = machineDiagnostics == null ? List.of() : List.copyOf(machineDiagnostics);
        machineContainerSlots = machineContainerSlots == null ? List.of() : List.copyOf(machineContainerSlots);
        machineRecipeOptions = machineRecipeOptions == null ? List.of() : List.copyOf(machineRecipeOptions);
        terminalPageId = text(terminalPageId);
        terminalService = text(terminalService);
        terminalCommand = text(terminalCommand);
        terminalCards = terminalCards == null ? List.of() : List.copyOf(terminalCards);
        terminalActions = terminalActions == null ? List.of() : List.copyOf(terminalActions);
        terminalDiagnostics = terminalDiagnostics == null ? List.of() : List.copyOf(terminalDiagnostics);
        terminalCommands = terminalCommands == null ? List.of() : List.copyOf(terminalCommands);
        if (terminalRuntimeCommandCount < 0) {
            terminalRuntimeCommandCount = 0;
        }
    }

    static EchoClientTechSurfaceModel empty() {
        return unavailable("No AdapterCore tech surface has been published yet");
    }

    static EchoClientTechSurfaceModel from(EchoAdapterCoreStandaloneContentBridge bridge) {
        if (bridge == null) {
            return unavailable("AdapterCore content bridge is not mounted");
        }
        try {
            Map<String, Object> tech = new EchoAdapterCoreAgent9TechRuntime().run(bridge);
            Map<String, Object> terminal = new EchoTerminalStandaloneAdapter().activate();
            Map<String, Object> dashboard = map(terminal.get("dashboardSurface"));
            EchoTerminalCommandRegistry commands = new EchoTerminalCommandRegistry();
            new EchoAshfallMissionUiBridge(new EchoAshfallStandaloneMissionRuntime())
                    .registerTerminalCommands(commands);
            boolean pass = "PASS".equals(text(tech.get("status")))
                    && Boolean.TRUE.equals(terminal.get("dashboardSurfaceExecuted"));
            return new EchoClientTechSurfaceModel(
                    pass,
                    firstText(tech.get("status"), pass ? "PASS" : "WARN"),
                    pass ? "" : "AdapterCore tech runtime or terminal dashboard did not pass",
                    firstText(tech.get("adapterCoreContract"), EchoAdapterCoreAgent9TechRuntime.CONTRACT_ID),
                    bool(tech.get("machineUiOpened")),
                    integer(tech.get("insertedInputCount")),
                    integer(tech.get("recipeProgressTicks")),
                    integer(tech.get("powerConsumed")),
                    bool(tech.get("outputAppeared")),
                    integer(tech.get("outputCountBeforeLogistics")),
                    integer(tech.get("oreGrinderInputCount")),
                    bool(tech.get("stateReloaded")),
                    bool(tech.get("missionCanDependOnMachineCompletion")),
                    powerGraph(listOfMaps(tech.get("powerGraph"))),
                    inventoryPorts(listOfMaps(tech.get("inventoryPorts"))),
                    blockEntitiesFromSnapshot(EchoClientMachineStateSnapshot.reference().blockEntities()),
                    EchoClientMachineStateSnapshot.reference().diagnostics(),
                    machineContainerSlotsFromSnapshot(EchoClientMachineStateSnapshot.reference().blockEntities()),
                    machineRecipeOptionsFromSnapshot(EchoClientMachineStateSnapshot.reference().blockEntities()),
                    text(dashboard.get("pageId")),
                    text(dashboard.get("service")),
                    text(dashboard.get("command")),
                    listOfStrings(dashboard.get("visibleCards")),
                    listOfStrings(dashboard.get("actions")),
                    listOfStrings(dashboard.get("diagnostics")),
                    terminalCommandRows(commands.commands()),
                    commands.runtimeCommands().size()
            );
        } catch (RuntimeException ex) {
            return unavailable(ex.getMessage());
        }
    }

    static EchoClientTechSurfaceModel from(
            EchoClientMachineStateSnapshot machineState,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        if (machineState == null) {
            return from(bridge);
        }
        try {
            Map<String, Object> terminal = new EchoTerminalStandaloneAdapter().activate();
            Map<String, Object> dashboard = map(terminal.get("dashboardSurface"));
            EchoTerminalCommandRegistry commands = new EchoTerminalCommandRegistry();
            new EchoAshfallMissionUiBridge(new EchoAshfallStandaloneMissionRuntime())
                    .registerTerminalCommands(commands);
            boolean terminalPassed = Boolean.TRUE.equals(terminal.get("dashboardSurfaceExecuted"));
            boolean machineAvailable = machineState.graphConnected() && machineState.machineUiOpened();
            return new EchoClientTechSurfaceModel(
                    machineAvailable && terminalPassed,
                    machineAvailable && terminalPassed ? "PASS" : "WARN",
                    machineAvailable ? "" : "Saved machine graph is not connected or UI is not open",
                    EchoAdapterCoreAgent9TechRuntime.CONTRACT_ID,
                    machineState.machineUiOpened(),
                    machineState.scrapPressInputCount(),
                    machineState.recipeProgressTicks(),
                    machineState.powerConsumed(),
                    machineState.outputAppeared(),
                    machineState.outputCountBeforeLogistics(),
                    machineState.oreGrinderInputCount(),
                    machineState.stateReloaded(),
                    machineState.missionDependency(),
                    powerGraphFromSnapshot(machineState.powerGraph()),
                    inventoryPortsFromSnapshot(machineState.inventoryPorts()),
                    blockEntitiesFromSnapshot(machineState.blockEntities()),
                    machineState.diagnostics(),
                    machineContainerSlotsFromSnapshot(machineState.blockEntities()),
                    machineRecipeOptionsFromSnapshot(machineState.blockEntities()),
                    text(dashboard.get("pageId")),
                    text(dashboard.get("service")),
                    text(dashboard.get("command")),
                    listOfStrings(dashboard.get("visibleCards")),
                    listOfStrings(dashboard.get("actions")),
                    listOfStrings(dashboard.get("diagnostics")),
                    terminalCommandRows(commands.commands()),
                    commands.runtimeCommands().size()
            );
        } catch (RuntimeException ex) {
            return unavailable(ex.getMessage());
        }
    }

    String machineSummary() {
        if (!available) {
            return "Machine telemetry unavailable: " + error;
        }
        return "scrap_press processed=" + outputAppeared
                + ", ticks=" + recipeProgressTicks
                + ", energy=" + powerConsumed
                + ", graphNodes=" + powerGraph.size()
                + ", blockEntities=" + blockEntities.size()
                + ", diagnostics=" + machineDiagnostics.size();
    }

    String terminalSummary() {
        if (!available) {
            return "Terminal payload unavailable: " + error;
        }
        return terminalPageId + ", commands=" + terminalCommands.size()
                + ", actions=" + terminalActions.size();
    }

    List<EchoClientScreenOption> machineOptions() {
        if (!available) {
            return List.of(new EchoClientScreenOption(
                    "Machine Telemetry Unavailable",
                    EchoClientScreenCommand.NONE,
                    false,
                    error
            ));
        }
        ArrayList<EchoClientScreenOption> options = new ArrayList<>();
        options.add(info("Machine State: scrap_press "
                + (outputAppeared ? "processed compressed_scrap" : "waiting for output"),
                "AdapterCore contract " + adapterCoreContract + ", status=" + status));
        options.add(info("Recipe Progress: " + recipeProgressTicks + " tick(s), energy " + powerConsumed,
                "Standalone machine tick model consumed AdapterCore power for the recipe"));
        options.add(info("Machine IO: input " + insertedInputCount
                        + " scrap_metal, output " + outputCountBeforeLogistics + " compressed_scrap",
                "Input and output slots are sourced from the AdapterCore Agent9 tech runtime"));
        for (EchoClientMachineRecipeOption recipe : selectedRecipeOptions()) {
            options.add(info("Selected Recipe " + recipe.machineId() + ": " + recipe.label(),
                    "Machine recipe is stored on the coordinate-backed block entity"));
        }
        for (EchoClientMachineRecipeOption recipe : machineRecipeOptions) {
            if (recipe.selected()) {
                continue;
            }
            options.add(EchoClientScreenOption.target(
                    "Select " + recipe.label() + " For " + recipe.machineId(),
                    EchoClientScreenCommand.SELECT_MACHINE_RECIPE,
                    recipe.targetId(),
                    "Switches this machine instance to recipe " + recipe.recipeId()
            ));
        }
        for (String machineId : scrapInputMachineIds()) {
            options.add(EchoClientScreenOption.target(
                    "Insert Scrap Into " + machineId,
                    EchoClientScreenCommand.INSERT_MACHINE_INPUT,
                    machineId,
                    "Consumes one Scrap Metal from player inventory and inserts it into this machine input port"
            ));
        }
        for (String machineId : compressedScrapMachineIds()) {
            options.add(EchoClientScreenOption.target(
                    "Extract Compressed Scrap From " + machineId,
                    EchoClientScreenCommand.EXTRACT_MACHINE_OUTPUT,
                    machineId,
                    "Moves one compressed_scrap item from this machine buffer into player inventory"
            ));
        }
        options.add(info("Logistics: ore_grinder input " + oreGrinderInputCount + " compressed_scrap",
                "Item pipe transfer moved the scrap_press output into the ore_grinder input"));
        options.add(info("Machine Containers: " + machineContainerSlots.size() + " slot(s)",
                "Machine inventory slots are stored on coordinate-backed block entities"));
        for (EchoClientMachineContainerSlot slot : limitedSlots(machineContainerSlots, 10)) {
            options.add(info("Slot " + slot.machineId() + "/" + slot.slotName()
                            + ": " + slot.itemId() + " x" + slot.count(),
                    "Container-backed machine slot exposed through ScreenCore"));
        }
        options.add(info("Machine Save: reloaded=" + stateReloaded + ", mission=" + missionDependency,
                "Machine state round-tripped and mission dependency checks observed the result"));
        options.add(info("Power Graph: " + powerGraph.size() + " node(s), connected=true",
                "Power graph published by AdapterCore machine/power/logistics runtime"));
        options.add(info("Block Entities: " + blockEntities.size() + " coordinate-backed",
                "Machine runtime state is attached to placed block coordinates and chunk-local positions"));
        options.add(info("Machine Diagnostics: " + machineDiagnostics.size(),
                machineDiagnostics.isEmpty()
                        ? "No machine reconciliation notices were detected"
                        : "Machine reconciliation notices surfaced through ScreenCore"));
        for (String diagnostic : limited(machineDiagnostics, 5)) {
            options.add(info("Machine Diagnostic: " + diagnostic,
                    "Standalone machine reconciliation notice surfaced through ScreenCore"));
        }
        for (EchoClientTechPowerNode node : firstPowerNodes(8)) {
            options.add(info("Power " + node.id() + ": " + node.energy() + "/" + node.capacity(),
                    node.detailLabel()));
        }
        for (String blockEntity : limited(blockEntities, 14)) {
            options.add(info("Block Entity " + blockEntity,
                    "Saved machine state row keyed by world and chunk coordinates"));
        }
        for (String port : limited(inventoryPorts, 8)) {
            options.add(info("Port " + port, "Machine inventory port exposed by AdapterCore"));
        }
        return List.copyOf(options);
    }

    private List<String> scrapInputMachineIds() {
        if (inventoryPorts.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (String port : inventoryPorts) {
            String value = text(port);
            if (!value.startsWith("scrap_press") || !value.contains("/input:") || !value.contains("scrap_metal")) {
                continue;
            }
            int portSeparator = value.indexOf("/input:");
            if (portSeparator <= 0) {
                continue;
            }
            String machineId = value.substring(0, portSeparator).trim();
            if (!machineId.isBlank() && !result.contains(machineId)) {
                result.add(machineId);
            }
        }
        return List.copyOf(result);
    }

    private List<String> compressedScrapMachineIds() {
        if (inventoryPorts.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (String port : inventoryPorts) {
            String value = text(port);
            if (!value.contains("compressed_scrap")) {
                continue;
            }
            int portSeparator = value.indexOf('/');
            if (portSeparator <= 0) {
                continue;
            }
            String machineId = value.substring(0, portSeparator).trim();
            if (!machineId.isBlank() && !result.contains(machineId)) {
                result.add(machineId);
            }
        }
        return List.copyOf(result);
    }

    private List<EchoClientMachineRecipeOption> selectedRecipeOptions() {
        if (machineRecipeOptions.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientMachineRecipeOption> result = new ArrayList<>();
        for (EchoClientMachineRecipeOption option : machineRecipeOptions) {
            if (option.selected()) {
                result.add(option);
            }
        }
        return List.copyOf(result);
    }

    List<EchoClientScreenOption> terminalOptions() {
        if (!available) {
            return List.of(new EchoClientScreenOption(
                    "Terminal Payload Unavailable",
                    EchoClientScreenCommand.NONE,
                    false,
                    error
            ));
        }
        ArrayList<EchoClientScreenOption> options = new ArrayList<>();
        options.add(info("Terminal Payload: " + terminalPageId,
                "Service " + terminalService + " executed command " + terminalCommand));
        options.add(info("Terminal Commands: " + terminalCommands.size()
                        + " shell, " + terminalRuntimeCommandCount + " runtime",
                "Shared terminal command registry is mounted behind the ScreenCore surface"));
        for (String command : limited(terminalCommands, 8)) {
            options.add(info("Command " + command, "Terminal command payload row"));
        }
        options.add(info("Terminal Cards: " + terminalCards.size(),
                terminalCards.isEmpty() ? "No visible cards" : String.join(", ", terminalCards)));
        options.add(info("Terminal Actions: " + terminalActions.size(),
                terminalActions.isEmpty() ? "No actions" : String.join(", ", terminalActions)));
        for (String action : limited(terminalActions, 4)) {
            options.add(info("Terminal Action: " + action, "AdapterCore dashboard action"));
        }
        options.add(info("Terminal Diagnostics: " + String.join(", ", terminalDiagnostics),
                "AdapterCore terminal diagnostics emitted by the dashboard service"));
        return List.copyOf(options);
    }

    private List<EchoClientTechPowerNode> firstPowerNodes(int limit) {
        if (limit <= 0 || powerGraph.isEmpty()) {
            return List.of();
        }
        return powerGraph.subList(0, Math.min(limit, powerGraph.size()));
    }

    private static EchoClientTechSurfaceModel unavailable(String error) {
        return new EchoClientTechSurfaceModel(
                false,
                "UNAVAILABLE",
                text(error).isBlank() ? "Unknown AdapterCore tech surface error" : error,
                "",
                false,
                0,
                0,
                0,
                false,
                0,
                0,
                false,
                false,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                "",
                "",
                "",
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0
        );
    }

    private static EchoClientScreenOption info(String label, String tooltip) {
        return new EchoClientScreenOption(label, EchoClientScreenCommand.NONE, false, tooltip);
    }

    private static List<EchoClientTechPowerNode> powerGraph(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientTechPowerNode> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String id = text(row.get("id"));
            if (id.isBlank()) {
                continue;
            }
            result.add(new EchoClientTechPowerNode(
                    id,
                    text(row.get("kind")),
                    integer(row.get("energy")),
                    integer(row.get("capacity")),
                    listOfStrings(row.get("neighbors"))
            ));
        }
        return List.copyOf(result);
    }

    private static List<EchoClientTechPowerNode> powerGraphFromSnapshot(
            List<EchoClientMachineStateSnapshot.PowerNode> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientTechPowerNode> result = new ArrayList<>();
        for (EchoClientMachineStateSnapshot.PowerNode row : rows) {
            if (row != null) {
                result.add(new EchoClientTechPowerNode(
                        row.id(),
                        row.kind(),
                        row.energy(),
                        row.capacity(),
                        row.neighbors()
                ));
            }
        }
        return List.copyOf(result);
    }

    private static List<String> inventoryPorts(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String machineId = text(row.get("machineId"));
            String port = text(row.get("port"));
            List<String> accepts = listOfStrings(row.get("accepts"));
            if (!machineId.isBlank() && !port.isBlank()) {
                result.add(machineId + "/" + port + ": " + String.join(", ", accepts));
            }
        }
        return List.copyOf(result);
    }

    private static List<String> inventoryPortsFromSnapshot(
            List<EchoClientMachineStateSnapshot.InventoryPort> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (EchoClientMachineStateSnapshot.InventoryPort row : rows) {
            if (row != null) {
                result.add(row.label());
            }
        }
        return List.copyOf(result);
    }

    private static List<String> blockEntitiesFromSnapshot(
            List<EchoClientMachineStateSnapshot.BlockEntity> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (EchoClientMachineStateSnapshot.BlockEntity row : rows) {
            if (row != null) {
                result.add(row.label());
            }
        }
        return List.copyOf(result);
    }

    private static List<EchoClientMachineContainerSlot> machineContainerSlotsFromSnapshot(
            List<EchoClientMachineStateSnapshot.BlockEntity> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientMachineContainerSlot> result = new ArrayList<>();
        for (EchoClientMachineStateSnapshot.BlockEntity row : rows) {
            if (row == null || row.state().isEmpty()) {
                continue;
            }
            for (Map.Entry<String, String> entry : row.state().entrySet()) {
                String key = entry.getKey();
                if (!key.startsWith("slot.") || !key.endsWith(".item")) {
                    continue;
                }
                String slotName = key.substring("slot.".length(), key.length() - ".item".length());
                String itemId = text(entry.getValue());
                if (slotName.isBlank() || itemId.isBlank()) {
                    continue;
                }
                result.add(new EchoClientMachineContainerSlot(
                        row.entityId(),
                        slotName,
                        itemId,
                        integer(row.state().get("slot." + slotName + ".count"))
                ));
            }
        }
        return List.copyOf(result);
    }

    private static List<EchoClientMachineRecipeOption> machineRecipeOptionsFromSnapshot(
            List<EchoClientMachineStateSnapshot.BlockEntity> rows
    ) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientMachineRecipeOption> result = new ArrayList<>();
        for (EchoClientMachineStateSnapshot.BlockEntity row : rows) {
            if (row == null || row.state().isEmpty()) {
                continue;
            }
            String selectedRecipe = text(row.state().get("selectedRecipe"));
            List<String> options = pipeValues(row.state().get("recipeOptions"));
            if (selectedRecipe.isBlank() && options.isEmpty()) {
                continue;
            }
            if (options.isEmpty()) {
                options = List.of(selectedRecipe);
            }
            for (String recipeId : options) {
                String normalized = text(recipeId);
                if (!normalized.isBlank()) {
                    result.add(new EchoClientMachineRecipeOption(
                            row.entityId(),
                            normalized,
                            machineRecipeLabel(normalized),
                            normalized.equals(selectedRecipe)
                    ));
                }
            }
        }
        return List.copyOf(result);
    }

    private static List<String> terminalCommandRows(List<EchoTerminalCommand> commands) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (EchoTerminalCommand command : commands) {
            result.add(command.name() + ": " + command.description());
        }
        return List.copyOf(result);
    }

    private static List<String> limited(List<String> values, int limit) {
        if (limit <= 0 || values == null || values.isEmpty()) {
            return List.of();
        }
        return values.subList(0, Math.min(limit, values.size()));
    }

    private static List<EchoClientMachineContainerSlot> limitedSlots(
            List<EchoClientMachineContainerSlot> values,
            int limit
    ) {
        if (limit <= 0 || values == null || values.isEmpty()) {
            return List.of();
        }
        return values.subList(0, Math.min(limit, values.size()));
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

    private static String machineRecipeLabel(String recipeId) {
        return switch (text(recipeId)) {
            case "echoashfallprotocol:scrap_press/compressed_scrap" -> "Compressed Scrap";
            case "echoashfallprotocol:scrap_press/dense_compressed_scrap" -> "Dense Scrap Batch";
            default -> recipeId;
        };
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return Map.of();
    }

    private static List<Map<String, Object>> listOfMaps(Object value) {
        if (!(value instanceof List<?> rows)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object row : rows) {
            Map<String, Object> map = map(row);
            if (!map.isEmpty()) {
                result.add(map);
            }
        }
        return List.copyOf(result);
    }

    private static List<String> listOfStrings(Object value) {
        if (!(value instanceof List<?> values)) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object item : values) {
            String text = text(item);
            if (!text.isBlank()) {
                result.add(text);
            }
        }
        return List.copyOf(result);
    }

    private static boolean bool(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.parseBoolean(text(value));
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = text(value);
        if (text.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    record EchoClientMachineContainerSlot(
            String machineId,
            String slotName,
            String itemId,
            int count
    ) {
        EchoClientMachineContainerSlot {
            machineId = text(machineId);
            slotName = text(slotName);
            itemId = text(itemId);
            if (count < 0) {
                count = 0;
            }
        }
    }

    record EchoClientMachineRecipeOption(
            String machineId,
            String recipeId,
            String label,
            boolean selected
    ) {
        EchoClientMachineRecipeOption {
            machineId = text(machineId);
            recipeId = text(recipeId);
            label = text(label).isBlank() ? recipeId : text(label);
        }

        String targetId() {
            return machineId + "|" + recipeId;
        }
    }

    record EchoClientTechPowerNode(
            String id,
            String kind,
            int energy,
            int capacity,
            List<String> neighbors
    ) {
        EchoClientTechPowerNode {
            id = text(id);
            kind = text(kind).toUpperCase(Locale.ROOT);
            if (energy < 0) {
                energy = 0;
            }
            if (capacity < 0) {
                capacity = 0;
            }
            neighbors = neighbors == null ? List.of() : List.copyOf(neighbors);
        }

        String detailLabel() {
            return kind + " neighbors=" + (neighbors.isEmpty() ? "none" : String.join(",", neighbors));
        }
    }
}
