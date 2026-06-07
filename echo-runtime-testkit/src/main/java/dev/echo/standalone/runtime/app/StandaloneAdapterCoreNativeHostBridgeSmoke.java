package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StandaloneAdapterCoreNativeHostBridgeSmoke {
    private static final String HOST_ID = StandaloneRuntimeHostContext.DEFAULT_RUNTIME_HOST_ID;
    private static final String SCANNER_ID = EchoAdapterCoreStandaloneContentBridge.EMERGENCY_SCANNER_ITEM_ID;

    private StandaloneAdapterCoreNativeHostBridgeSmoke() {
    }

    public static void main(String[] args) throws Exception {
        Path saveRoot = Files.createTempDirectory("echo-standalone-adaptercore-native-host-");
        StandaloneEchoRuntimeHost standaloneHost = StandaloneRuntimeHostFactory.ashfallLive().create(saveRoot);
        Object adapterCoreHost = registeredAdapterCoreHost();
        Object capabilities = registeredAdapterCoreCapabilities();
        requireNativeUiHotkeyActions(capabilities);
        Object inventory = adapterCoreHost.getClass().getMethod("playerInventory").invoke(adapterCoreHost);
        Object playerState = adapterCoreHost.getClass().getMethod("playerState").invoke(adapterCoreHost);
        Object worldBlocks = adapterCoreHost.getClass().getMethod("worldBlocks").invoke(adapterCoreHost);
        Object worldState = adapterCoreHost.getClass().getMethod("worldState").invoke(adapterCoreHost);
        Object structures = adapterCoreHost.getClass().getMethod("structures").invoke(adapterCoreHost);
        Object blockEntities = adapterCoreHost.getClass().getMethod("blockEntities").invoke(adapterCoreHost);
        Object runtimeCapabilities = adapterCoreHost.getClass().getMethod("capabilities").invoke(adapterCoreHost);
        Object events = adapterCoreHost.getClass().getMethod("events").invoke(adapterCoreHost);
        Object packets = adapterCoreHost.getClass().getMethod("packets").invoke(adapterCoreHost);
        Object contentRegistries = adapterCoreHost.getClass().getMethod("contentRegistries").invoke(adapterCoreHost);
        int before = count(standaloneHost.context().inventorySnapshot().get(SCANNER_ID));
        Object result = grant(inventory, standaloneHost);
        int after = count(standaloneHost.context().inventorySnapshot().get(SCANNER_ID));
        Map<String, Object> snapshot = snapshot(result);
        require(mutated(result), "standalone AdapterCore host grant must return a mutating NativeResult");
        require(after == before + 1, "standalone AdapterCore host grant must mutate runtime inventory");
        require(standaloneHost.ledger().hasSavedMutation(),
                "standalone AdapterCore host grant must write standalone save data");
        require(standaloneHost.ledger().hasVisibleMutation(),
                "standalone AdapterCore host grant must emit visible feedback");
        require(Boolean.TRUE.equals(snapshot.get("saveTouched")),
                "standalone AdapterCore NativeResult must report saveTouched");
        require(Boolean.TRUE.equals(snapshot.get("feedbackEmitted")),
                "standalone AdapterCore NativeResult must report feedbackEmitted");
        require("player.inventory.grant".equals(snapshot.get("eventName")),
                "standalone AdapterCore host grant must use the canonical inventory grant event name");
        require(SCANNER_ID.equals(snapshot.get("canonicalId")),
                "standalone AdapterCore host grant must preserve the canonical scanner item id");
        Object scannerResult = publishScannerUse(events, standaloneHost);
        Map<String, Object> scannerSnapshot = snapshot(scannerResult);
        require(mutated(scannerResult), "standalone AdapterCore host scanner use must publish a mutating scanner event");
        require(Boolean.TRUE.equals(scannerSnapshot.get("saveTouched")),
                "standalone AdapterCore scanner use NativeResult must report saveTouched");
        require(Boolean.TRUE.equals(scannerSnapshot.get("feedbackEmitted")),
                "standalone AdapterCore scanner use NativeResult must report feedbackEmitted");
        require("player.scanner_used".equals(scannerSnapshot.get("eventName")),
                "standalone AdapterCore scanner use must use the canonical player.scanner_used event name");
        require(SCANNER_ID.equals(scannerSnapshot.get("canonicalId")),
                "standalone AdapterCore scanner use must preserve the canonical scanner item id");
        require(details(scannerSnapshot).containsKey("mission"),
                "standalone AdapterCore scanner use must return mission evidence");
        Object terminalResult = publishTerminalCommand(events, standaloneHost);
        Map<String, Object> terminalSnapshot = snapshot(terminalResult);
        require(mutated(terminalResult), "standalone AdapterCore host terminal command must publish a mutating command event");
        require(Boolean.TRUE.equals(terminalSnapshot.get("saveTouched")),
                "standalone AdapterCore terminal command NativeResult must report saveTouched");
        require(Boolean.TRUE.equals(terminalSnapshot.get("feedbackEmitted")),
                "standalone AdapterCore terminal command NativeResult must report feedbackEmitted");
        require("command_execution".equals(terminalSnapshot.get("eventName")),
                "standalone AdapterCore terminal command must use the canonical command_execution event name");
        require("echoterminal:terminal".equals(terminalSnapshot.get("canonicalId")),
                "standalone AdapterCore terminal command must preserve the terminal screen canonical id");
        Object indexResult = publishIndexSearch(events, standaloneHost);
        Map<String, Object> indexSnapshot = snapshot(indexResult);
        require(mutated(indexResult), "standalone AdapterCore host index search must publish a mutating terminal event");
        require(Boolean.TRUE.equals(indexSnapshot.get("saveTouched")),
                "standalone AdapterCore index search NativeResult must report saveTouched");
        require(Boolean.TRUE.equals(indexSnapshot.get("feedbackEmitted")),
                "standalone AdapterCore index search NativeResult must report feedbackEmitted");
        require("player.terminal_opened".equals(indexSnapshot.get("eventName")),
                "standalone AdapterCore index search must use the canonical terminal opened event name");
        require("echoindex:index".equals(indexSnapshot.get("canonicalId")),
                "standalone AdapterCore index search must preserve the index screen canonical id");
        require(details(indexSnapshot).containsValue(EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID),
                "standalone AdapterCore index search must target the concrete field terminal block");
        Object hudResult = publishHudRefresh(events, standaloneHost);
        Map<String, Object> hudSnapshot = snapshot(hudResult);
        require(mutated(hudResult), "standalone AdapterCore host HUD refresh must publish a mutating tick event");
        require(Boolean.TRUE.equals(hudSnapshot.get("saveTouched")),
                "standalone AdapterCore HUD refresh NativeResult must report saveTouched");
        require(Boolean.TRUE.equals(hudSnapshot.get("feedbackEmitted")),
                "standalone AdapterCore HUD refresh NativeResult must report feedbackEmitted");
        require("client_tick".equals(hudSnapshot.get("eventName")),
                "standalone AdapterCore HUD refresh must use the canonical client tick event name");
        require("echoashfallprotocol:runtime_hud_notification".equals(hudSnapshot.get("canonicalId")),
                "standalone AdapterCore HUD refresh must preserve the runtime HUD notification canonical id");
        Object missionResult = publishMissionLogUpdate(events, standaloneHost);
        Map<String, Object> missionSnapshot = snapshot(missionResult);
        require(mutated(missionResult), "standalone AdapterCore host mission log update must publish a mutating mission event");
        require(Boolean.TRUE.equals(missionSnapshot.get("saveTouched")),
                "standalone AdapterCore mission log update NativeResult must report saveTouched");
        require(Boolean.TRUE.equals(missionSnapshot.get("feedbackEmitted")),
                "standalone AdapterCore mission log update NativeResult must report feedbackEmitted");
        require("mission.objective_completed".equals(missionSnapshot.get("eventName")),
                "standalone AdapterCore mission log update must use the canonical mission objective event name");
        require("echoashfallprotocol:secure_crash_outpost".equals(missionSnapshot.get("canonicalId")),
                "standalone AdapterCore mission log update must preserve the Ashfall mission canonical id");
        int nativeUiEventCount = publishNativeUiHotkeyEvents(events, standaloneHost);
        Object contentRegistrationResult = registerNativeScreen(contentRegistries, standaloneHost);
        Map<String, Object> contentRegistrationSnapshot = snapshot(contentRegistrationResult);
        require(mutated(contentRegistrationResult),
                "standalone AdapterCore content registry must publish a mutating registration result");
        require(Boolean.TRUE.equals(contentRegistrationSnapshot.get("saveTouched")),
                "standalone AdapterCore content registration NativeResult must report saveTouched");
        require("adaptercore.content.register".equals(contentRegistrationSnapshot.get("eventName")),
                "standalone AdapterCore content registration must use the canonical register event name");
        require("echoruntimehost:ui/native_registered_screen".equals(contentRegistrationSnapshot.get("canonicalId")),
                "standalone AdapterCore content registration must preserve the registered content id");
        require(count(map(contentRegistrationSnapshot.get("hostSnapshot")).get("contentRegistrations")) >= 1,
                "standalone AdapterCore content registration should update host content registry state");
        require(count(contentRegistrationSnapshot.get("ledgerSequence")) > 0,
                "standalone AdapterCore content registration should write through the mutation ledger");
        require(registeredContentCount(contentRegistries, standaloneHost, "ui_screens") >= 1,
                "standalone AdapterCore content registry should list registered UI screens");
        Map<String, Object> runtimeServiceEvents = exerciseRuntimeServiceSurfaces(
                inventory,
                playerState,
                worldBlocks,
                worldState,
                structures,
                blockEntities,
                runtimeCapabilities,
                packets,
                standaloneHost
        );
        System.out.println("standalone adaptercore native host bridge PASS runtimeHost=" + HOST_ID
                + " item=" + SCANNER_ID
                + " before=" + before
                + " after=" + after
                + " scannerEvent=" + scannerSnapshot.get("eventName")
                + " terminalEvent=" + terminalSnapshot.get("eventName")
                + " indexEvent=" + indexSnapshot.get("eventName")
                + " hudEvent=" + hudSnapshot.get("eventName")
                + " missionEvent=" + missionSnapshot.get("eventName")
                + " uiHotkeyActions=" + nativeUiEventCount
                + " contentEvent=" + contentRegistrationSnapshot.get("eventName")
                + " runtimeServices=" + runtimeServiceEvents.size());
    }

    private static Object registeredAdapterCoreHost() throws Exception {
        Class<?> registryClass = Class.forName("com.knoxhack.echo.adaptercore.EchoRuntimeHostRegistry");
        Object registry = registryClass.getMethod("global").invoke(null);
        Object optional = registryClass.getMethod("host", String.class).invoke(registry, HOST_ID);
        require(Boolean.TRUE.equals(optional.getClass().getMethod("isPresent").invoke(optional)),
                "standalone host factory must register an AdapterCore host when AdapterCore is present");
        return optional.getClass().getMethod("get").invoke(optional);
    }

    private static Object registeredAdapterCoreCapabilities() throws Exception {
        Class<?> registryClass = Class.forName("com.knoxhack.echo.adaptercore.EchoRuntimeHostRegistry");
        Object registry = registryClass.getMethod("global").invoke(null);
        Object optional = registryClass.getMethod("capabilities", String.class).invoke(registry, HOST_ID);
        require(Boolean.TRUE.equals(optional.getClass().getMethod("isPresent").invoke(optional)),
                "standalone host factory must register AdapterCore host capabilities");
        return optional.getClass().getMethod("get").invoke(optional);
    }

    private static void requireNativeUiHotkeyActions(Object capabilities) throws Exception {
        for (String actionId : new String[]{
                "player.scanner_used",
                "native.ui.use_scanner",
                "native.ui.surface_open",
                "native.ui.index_bookmark",
                "native.ui.holomap_state",
                "native.ui.signalos_terminal",
                "native.ui.ashfall_drone_command",
                "player.inventory.remove",
                "player.state.teleport",
                "player.state.bind_respawn",
                "player.advancement.grant",
                "player.state.write",
                "world.block.set",
                "world.block.clear",
                "world.state.marker.write",
                "world.state.weather.write",
                "world.state.route.write",
                "world.structure.place",
                "block_entity.tick",
                "block_entity.snapshot.apply",
                "capability.item.insert",
                "capability.item.extract",
                "capability.energy.receive",
                "capability.energy.extract",
                "packet.send_to_player",
                "packet.broadcast"
        }) {
            require(Boolean.TRUE.equals(capabilities.getClass()
                            .getMethod("supportsAction", String.class)
                            .invoke(capabilities, actionId)),
                    "standalone AdapterCore native host must advertise " + actionId);
        }
        require(Boolean.TRUE.equals(capabilities.getClass()
                        .getMethod("supportsNativeInterface", String.class)
                        .invoke(capabilities, "EchoNativeRuntimeHost.ContentRegistries")),
                "standalone AdapterCore native host must advertise content registry support");
        for (String nativeInterface : new String[]{
                "EchoNativeRuntimeHost.PlayerState",
                "EchoNativeRuntimeHost.WorldBlocks",
                "EchoNativeRuntimeHost.WorldState",
                "EchoNativeRuntimeHost.Structures",
                "EchoNativeRuntimeHost.BlockEntities",
                "EchoNativeRuntimeHost.Capabilities",
                "EchoNativeRuntimeHost.Packets"
        }) {
            require(Boolean.TRUE.equals(capabilities.getClass()
                            .getMethod("supportsNativeInterface", String.class)
                            .invoke(capabilities, nativeInterface)),
                    "standalone AdapterCore native host must advertise " + nativeInterface);
        }
        require(Boolean.TRUE.equals(capabilities.getClass()
                        .getMethod("supportsAction", String.class)
                        .invoke(capabilities, "adaptercore.content.register")),
                "standalone AdapterCore native host must advertise adaptercore.content.register");
    }

    private static Object grant(Object inventory, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Class<?> playerRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativePlayerRef");
        Class<?> stackClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeItemStack");
        Class<?> contextClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeMutationContext");
        Object playerRef = playerRefClass.getConstructor(String.class).newInstance(standaloneHost.context().playerId());
        Object stack = stackClass.getConstructor(String.class, int.class, Map.class)
                .newInstance(SCANNER_ID, 1, Map.of());
        Object context = contextClass
                .getConstructor(String.class, String.class, String.class, String.class, long.class, Map.class)
                .newInstance(
                        "native_client",
                        standaloneHost.context().dimensionId(),
                        "native_client.grant_item.echoashfallprotocol_portable_signal_scanner",
                        "SERVER",
                        standaloneHost.context().gameTime(),
                        Map.of(
                                "nativeInterface", "EchoNativeRuntimeHost.PlayerInventory",
                                "nativeMethod", "grant",
                                "hostRuntime", "echo_runtime_standalone"
                        )
                );
        Method grant = inventory.getClass().getMethod("grant", playerRefClass, stackClass, contextClass);
        grant.trySetAccessible();
        return grant.invoke(inventory, playerRef, stack, context);
    }

    private static Object publishScannerUse(Object events, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("screenId", "echolens:lens");
        payload.put("canonicalId", SCANNER_ID);
        payload.put("target", "echoashfallprotocol:scan_first_poi");
        payload.put("scanTarget", "echoashfallprotocol:scan_first_poi");
        payload.put("itemId", SCANNER_ID);
        payload.put("source", "native_ui_scanner");
        payload.put("deepScan", false);
        payload.put("signalFound", true);
        payload.put("runtimeFeedback", true);
        payload.put("runtimePoiDiscovery", true);
        payload.put("eventName", "player.scanner_used");
        return publishEvent(
                events,
                standaloneHost,
                "player.scanner_used",
                payload,
                "native_client.scanner_used.portable_signal_scanner"
        );
    }

    private static Object publishTerminalCommand(Object events, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("screenId", "echoterminal:terminal");
        payload.put("command", "status");
        payload.put("output", "Ashfall Terminal");
        payload.put("eventName", "command_execution");
        payload.put("canonicalId", "echoterminal:terminal");
        payload.put("terminalId", "echoterminal:ashfall_first_steps");
        payload.put("target", "echoterminal:ashfall_first_steps");
        payload.put("source", "native_ui_terminal");
        return publishEvent(
                events,
                standaloneHost,
                "command_execution",
                payload,
                "native_client.terminal_command.status"
        );
    }

    private static Object publishIndexSearch(Object events, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("screenId", "echoindex:index");
        payload.put("canonicalId", "echoindex:index");
        payload.put("terminalId", "echoterminal:ashfall_first_steps");
        payload.put("target", "echoindex:index/ashfall");
        payload.put("blockId", EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID);
        payload.put("x", 3);
        payload.put("y", 4);
        payload.put("z", 3);
        payload.put("query", "ashfall");
        payload.put("output", "Ashfall Index");
        payload.put("source", "native_ui_index");
        return publishEvent(
                events,
                standaloneHost,
                "player.terminal_opened",
                payload,
                "native_client.index_search.ashfall"
        );
    }

    private static Object publishHudRefresh(Object events, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("screenId", "echohudcore:hud");
        payload.put("canonicalId", "echoashfallprotocol:runtime_hud_notification");
        payload.put("target", "echoashfallprotocol:runtime_hud_notification");
        payload.put("ticks", 1);
        payload.put("seconds", 1.0D);
        payload.put("moved", true);
        payload.put("health", 85);
        payload.put("hazard", "ashfall");
        payload.put("mission", "secure crash outpost");
        payload.put("source", "native_ui_hud");
        return publishEvent(
                events,
                standaloneHost,
                "client_tick",
                payload,
                "native_client.hud_refresh.ashfall"
        );
    }

    private static Object publishMissionLogUpdate(Object events, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("screenId", "echoscreencore:mission_log");
        payload.put("canonicalId", "echoashfallprotocol:secure_crash_outpost");
        payload.put("target", "echoashfallprotocol:secure_crash_outpost");
        payload.put("missionId", "echoashfallprotocol:secure_crash_outpost");
        payload.put("missionTitle", "Secure Crash Outpost");
        payload.put("missionObjective", "Drop pod signal confirmed");
        payload.put("missionProgress", 0.5D);
        payload.put("missionStatus", "UPDATED");
        payload.put("missionUpdateLine", "Drop pod signal confirmed");
        payload.put("itemId", EchoAdapterCoreStandaloneContentBridge.FIELD_MANUAL_ITEM_ID);
        payload.put("source", "native_ui_mission_log");
        return publishEvent(
                events,
                standaloneHost,
                "mission.objective_completed",
                payload,
                "native_client.mission_log_update.secure_crash_outpost"
        );
    }

    private static int publishNativeUiHotkeyEvents(Object events, StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        int published = 0;
        for (String actionId : new String[]{
                "native.ui.surface_open",
                "native.ui.index_bookmark",
                "native.ui.holomap_state",
                "native.ui.signalos_terminal",
                "native.ui.ashfall_drone_command"
        }) {
            Map<String, Object> payload = nativeUiPayload(actionId);
            Object result = publishEvent(
                    events,
                    standaloneHost,
                    actionId,
                    payload,
                    "native_client." + actionId.replace('.', '_')
            );
            Map<String, Object> snapshot = snapshot(result);
            require(mutated(result), "standalone AdapterCore native UI hotkey must publish a mutating event: " + actionId);
            require(Boolean.TRUE.equals(snapshot.get("saveTouched")),
                    "standalone AdapterCore native UI hotkey NativeResult must report saveTouched: " + actionId);
            require(Boolean.TRUE.equals(snapshot.get("feedbackEmitted")),
                    "standalone AdapterCore native UI hotkey NativeResult must report feedbackEmitted: " + actionId);
            require(actionId.equals(snapshot.get("eventName")),
                    "standalone AdapterCore native UI hotkey must preserve event name: " + actionId);
            require(payload.get("canonicalId").equals(snapshot.get("canonicalId")),
                    "standalone AdapterCore native UI hotkey must preserve canonical id: " + actionId);
            require(details(snapshot).containsKey("mission"),
                    "standalone AdapterCore native UI hotkey must return mission evidence: " + actionId);
            published++;
        }
        return published;
    }

    private static Map<String, Object> nativeUiPayload(String actionId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("runtimeActionId", actionId);
        payload.put("eventName", actionId);
        payload.put("source", "native_ui_hotkey_smoke");
        switch (actionId) {
            case "native.ui.surface_open" -> {
                payload.put("screenId", "echoholomap:map");
                payload.put("canonicalId", "echoholomap:ashfall_map");
                payload.put("target", "echoholomap:ashfall_map");
                payload.put("surface", "HOLOMAP");
                payload.put("effect", "echoholomap:native_screen.open");
            }
            case "native.ui.index_bookmark" -> {
                payload.put("screenId", "echoindex:index");
                payload.put("canonicalId", "echoindex:entry/drop_pod");
                payload.put("target", "echoindex:entry/drop_pod");
                payload.put("entryId", "drop_pod");
                payload.put("title", "Drop Pod");
            }
            case "native.ui.holomap_state" -> {
                payload.put("screenId", "echoholomap:map");
                payload.put("canonicalId", "echoholomap:ashfall_map");
                payload.put("target", "echoholomap:ashfall_map");
                payload.put("action", "holomap.zoom_in");
                payload.put("zoom", 1.25D);
            }
            case "native.ui.signalos_terminal" -> {
                payload.put("screenId", "echosignalos:terminal");
                payload.put("canonicalId", "echosignalos:terminal");
                payload.put("target", "echosignalos:terminal");
                payload.put("action", "open");
                payload.put("serverboundPacketSent", true);
            }
            case "native.ui.ashfall_drone_command" -> {
                payload.put("screenId", "echoashfallprotocol:drone");
                payload.put("canonicalId", "echoashfallprotocol:companion_drone");
                payload.put("target", "echoashfallprotocol:companion_drone");
                payload.put("action", "ashfall.drone_status");
                payload.put("command", "status");
            }
            default -> payload.put("target", actionId);
        }
        return Map.copyOf(payload);
    }

    private static Object registerNativeScreen(
            Object contentRegistries,
            StandaloneEchoRuntimeHost standaloneHost
    ) throws Exception {
        Class<?> registrationClass =
                Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeContentRegistration");
        Class<?> contextClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeMutationContext");
        Object registration = registrationClass
                .getConstructor(
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        String.class,
                        Map.class
                )
                .newInstance(
                        "echoruntimehost",
                        "echoruntimehost:ui/native_registered_screen",
                        "UI_SCREEN",
                        "ui_screens",
                        "Native Registered Screen",
                        "screencore.native.registered_screen",
                        "echoruntimehost:native_registered_screen",
                        "echoruntimehost:screen/native_registered_screen",
                        "echoruntimehost:standalone/native_registered_screen",
                        Map.of("route", "screencore.native.registered_screen")
                );
        Object context = contextClass
                .getConstructor(String.class, String.class, String.class, String.class, long.class, Map.class)
                .newInstance(
                        "native_client",
                        standaloneHost.context().dimensionId(),
                        "native_client.content.register.native_registered_screen",
                        "SERVER",
                        standaloneHost.context().gameTime(),
                        Map.of(
                                "nativeInterface", "EchoNativeRuntimeHost.ContentRegistries",
                                "nativeMethod", "register",
                                "hostRuntime", "echo_runtime_standalone"
                        )
                );
        Method register = contentRegistries.getClass().getMethod("register", registrationClass, contextClass);
        register.trySetAccessible();
        return register.invoke(contentRegistries, registration, context);
    }

    private static int registeredContentCount(
            Object contentRegistries,
            StandaloneEchoRuntimeHost standaloneHost,
            String domain
    ) throws Exception {
        Class<?> contextClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeMutationContext");
        Object context = contextClass
                .getConstructor(String.class, String.class, String.class, String.class, long.class, Map.class)
                .newInstance(
                        "native_client",
                        standaloneHost.context().dimensionId(),
                        "native_client.content.list." + domain,
                        "SERVER",
                        standaloneHost.context().gameTime(),
                        Map.of(
                                "nativeInterface", "EchoNativeRuntimeHost.ContentRegistries",
                                "nativeMethod", "registrations",
                                "hostRuntime", "echo_runtime_standalone"
                        )
                );
        Method registrations = contentRegistries.getClass().getMethod("registrations", String.class, contextClass);
        registrations.trySetAccessible();
        Object result = registrations.invoke(contentRegistries, domain, context);
        return result instanceof List<?> list ? list.size() : 0;
    }

    private static Map<String, Object> exerciseRuntimeServiceSurfaces(
            Object inventory,
            Object playerState,
            Object worldBlocks,
            Object worldState,
            Object structures,
            Object blockEntities,
            Object runtimeCapabilities,
            Object packets,
            StandaloneEchoRuntimeHost standaloneHost
    ) throws Exception {
        LinkedHashMap<String, Object> events = new LinkedHashMap<>();
        Object playerRef = nativePlayerRef(standaloneHost);
        Object blockRef = nativeBlockRef(standaloneHost, 8, 4, 8);
        Object machineRef = nativeBlockRef(standaloneHost, 9, 4, 8);
        Object structureRef = nativeStructurePlacement(standaloneHost, "echoashfallprotocol:drop_pod_cache", 10, 4, 8);
        Object capabilityRequest = nativeCapabilityRequest(
                machineRef,
                "forge:item_handler",
                "UP",
                Map.of("capabilityKey", "smoke:field_terminal")
        );

        Object removeResult = invokeNamed(
                inventory,
                "remove",
                playerRef,
                SCANNER_ID,
                1,
                mutationContext(
                        standaloneHost,
                        "EchoNativeRuntimeHost.PlayerInventory",
                        "remove",
                        "native_client.inventory.remove.scanner"
                )
        );
        events.put("inventoryRemove", requireMutatingEvent(
                removeResult,
                "player.inventory.remove",
                "standalone AdapterCore inventory remove must mutate runtime inventory"
        ).get("eventName"));

        events.put("teleport", requireMutatingEvent(
                invokeNamed(
                        playerState,
                        "teleport",
                        playerRef,
                        nativePosition(standaloneHost, 4.5D, 6.0D, 4.5D),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.PlayerState",
                                "teleport",
                                "native_client.player.teleport.smoke"
                        )
                ),
                "player.state.teleport",
                "standalone AdapterCore player teleport must mutate player state"
        ).get("eventName"));

        events.put("respawn", requireMutatingEvent(
                invokeNamed(
                        playerState,
                        "bindRespawn",
                        playerRef,
                        nativePosition(standaloneHost, 4.0D, 6.0D, 4.0D),
                        true,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.PlayerState",
                                "bindRespawn",
                                "native_client.player.respawn.smoke"
                        )
                ),
                "player.state.bind_respawn",
                "standalone AdapterCore respawn bind must mutate player persistent state"
        ).get("eventName"));

        events.put("advancement", requireMutatingEvent(
                invokeNamed(
                        playerState,
                        "grantAdvancement",
                        playerRef,
                        "echoashfallprotocol:secure_crash_outpost",
                        "drop_pod_signal_confirmed",
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.PlayerState",
                                "grantAdvancement",
                                "native_client.player.advancement.smoke"
                        )
                ),
                "player.advancement.grant",
                "standalone AdapterCore advancement grant must mutate player persistent state"
        ).get("eventName"));

        events.put("playerState", requireMutatingEvent(
                invokeNamed(
                        playerState,
                        "writePersistentState",
                        playerRef,
                        "nativeSmoke",
                        "ready",
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.PlayerState",
                                "writePersistentState",
                                "native_client.player.state.write.smoke"
                        )
                ),
                "player.state.write",
                "standalone AdapterCore player state write must mutate player persistent state"
        ).get("eventName"));

        events.put("worldBlock", requireMutatingEvent(
                invokeNamed(
                        worldBlocks,
                        "setBlock",
                        blockRef,
                        nativeBlockState(EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.WorldBlocks",
                                "setBlock",
                                "native_client.world.block.set.terminal"
                        )
                ),
                "world.block.set",
                "standalone AdapterCore world block set must mutate voxel world"
        ).get("eventName"));
        Object readState = invokeNamed(
                worldBlocks,
                "blockState",
                blockRef,
                mutationContext(
                        standaloneHost,
                        "EchoNativeRuntimeHost.WorldBlocks",
                        "blockState",
                        "native_client.world.block.read.terminal"
                )
        );
        require(EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID.equals(invoke(readState, "blockId")),
                "standalone AdapterCore world block read must return the block written through the native host");
        require(Boolean.TRUE.equals(invokeNamed(
                        worldBlocks,
                        "isLoaded",
                        blockRef,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.WorldBlocks",
                                "isLoaded",
                                "native_client.world.block.loaded.terminal"
                        )
                )),
                "standalone AdapterCore world block loaded check must read runtime chunk state");
        events.put("worldBlockClear", requireMutatingEvent(
                invokeNamed(
                        worldBlocks,
                        "clearBlock",
                        blockRef,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.WorldBlocks",
                                "clearBlock",
                                "native_client.world.block.clear.terminal"
                        )
                ),
                "world.block.clear",
                "standalone AdapterCore world block clear must mutate voxel world"
        ).get("eventName"));

        events.put("worldMarker", requireMutatingEvent(
                invokeNamed(
                        worldState,
                        "writeMarker",
                        "native_smoke_marker",
                        Map.of("source", "adaptercore_native_host_smoke"),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.WorldState",
                                "writeMarker",
                                "native_client.world.marker.smoke"
                        )
                ),
                "world.state.marker.write",
                "standalone AdapterCore world marker write must mutate world state"
        ).get("eventName"));
        events.put("weather", requireMutatingEvent(
                invokeNamed(
                        worldState,
                        "writeWeatherState",
                        "ashfall_smoke",
                        Map.of("ashDensity", 0.65D),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.WorldState",
                                "writeWeatherState",
                                "native_client.world.weather.smoke"
                        )
                ),
                "world.state.weather.write",
                "standalone AdapterCore weather write must mutate world state"
        ).get("eventName"));
        events.put("route", requireMutatingEvent(
                invokeNamed(
                        worldState,
                        "writeRouteState",
                        "drop_pod_route",
                        Map.of("waypoints", 3),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.WorldState",
                                "writeRouteState",
                                "native_client.world.route.smoke"
                        )
                ),
                "world.state.route.write",
                "standalone AdapterCore route write must mutate world state"
        ).get("eventName"));

        events.put("structure", requireMutatingEvent(
                invokeNamed(
                        structures,
                        "placeStructure",
                        structureRef,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Structures",
                                "placeStructure",
                                "native_client.structure.place.smoke"
                        )
                ),
                "world.structure.place",
                "standalone AdapterCore structure placement must mutate voxel world"
        ).get("eventName"));

        events.put("blockEntityTick", requireMutatingEvent(
                invokeNamed(
                        blockEntities,
                        "tick",
                        machineRef,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.BlockEntities",
                                "tick",
                                "native_client.block_entity.tick.smoke"
                        )
                ),
                "block_entity.tick",
                "standalone AdapterCore block entity tick must mutate block entity state"
        ).get("eventName"));
        Object entitySnapshot = invokeNamed(
                blockEntities,
                "snapshot",
                machineRef,
                mutationContext(
                        standaloneHost,
                        "EchoNativeRuntimeHost.BlockEntities",
                        "snapshot",
                        "native_client.block_entity.snapshot.smoke"
                )
        );
        require(count(map(invoke(entitySnapshot, "state")).get("energyStored")) >= 1,
                "standalone AdapterCore block entity snapshot must read ticked energy state");
        events.put("blockEntityApply", requireMutatingEvent(
                invokeNamed(
                        blockEntities,
                        "applySnapshot",
                        nativeBlockEntitySnapshot(machineRef, "echoashfallprotocol:field_machine",
                                Map.of("mode", "native_apply", "energyStored", 17)),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.BlockEntities",
                                "applySnapshot",
                                "native_client.block_entity.apply.smoke"
                        )
                ),
                "block_entity.snapshot.apply",
                "standalone AdapterCore block entity apply must mutate block entity state"
        ).get("eventName"));
        Object appliedEntitySnapshot = invokeNamed(
                blockEntities,
                "snapshot",
                machineRef,
                mutationContext(
                        standaloneHost,
                        "EchoNativeRuntimeHost.BlockEntities",
                        "snapshot",
                        "native_client.block_entity.snapshot_after_apply.smoke"
                )
        );
        require("native_apply".equals(map(invoke(appliedEntitySnapshot, "state")).get("mode")),
                "standalone AdapterCore block entity snapshot must read applied state");

        events.put("capabilityInsert", requireMutatingEvent(
                invokeNamed(
                        runtimeCapabilities,
                        "insertItem",
                        capabilityRequest,
                        nativeItemStack(SCANNER_ID, 2),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Capabilities",
                                "insertItem",
                                "native_client.capability.item.insert.scanner"
                        )
                ),
                "capability.item.insert",
                "standalone AdapterCore capability item insert must mutate capability storage"
        ).get("eventName"));
        events.put("capabilityEnergy", requireMutatingEvent(
                invokeNamed(
                        runtimeCapabilities,
                        "receiveEnergy",
                        capabilityRequest,
                        7,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Capabilities",
                                "receiveEnergy",
                                "native_client.capability.energy.receive.smoke"
                        )
                ),
                "capability.energy.receive",
                "standalone AdapterCore capability energy receive must mutate capability storage"
        ).get("eventName"));
        events.put("capabilityExtract", requireMutatingEvent(
                invokeNamed(
                        runtimeCapabilities,
                        "extractItem",
                        capabilityRequest,
                        SCANNER_ID,
                        1,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Capabilities",
                                "extractItem",
                                "native_client.capability.item.extract.scanner"
                        )
                ),
                "capability.item.extract",
                "standalone AdapterCore capability item extract must mutate capability storage"
        ).get("eventName"));
        events.put("capabilityDrain", requireMutatingEvent(
                invokeNamed(
                        runtimeCapabilities,
                        "extractEnergy",
                        capabilityRequest,
                        3,
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Capabilities",
                                "extractEnergy",
                                "native_client.capability.energy.extract.smoke"
                        )
                ),
                "capability.energy.extract",
                "standalone AdapterCore capability energy extract must mutate capability storage"
        ).get("eventName"));
        Map<String, Object> capabilitySnapshot = map(invokeNamed(
                runtimeCapabilities,
                "readCapability",
                capabilityRequest,
                mutationContext(
                        standaloneHost,
                        "EchoNativeRuntimeHost.Capabilities",
                        "readCapability",
                        "native_client.capability.read.smoke"
                )
        ));
        require(count(map(capabilitySnapshot.get("items")).get(SCANNER_ID)) == 1,
                "standalone AdapterCore capability read must reflect inserted and extracted items");
        require(count(capabilitySnapshot.get("energy")) == 4,
                "standalone AdapterCore capability read must reflect received and extracted energy");

        int packetsBefore = standaloneHost.context().packetLog().size();
        events.put("packetSend", requireMutatingEvent(
                invokeNamed(
                        packets,
                        "sendToPlayer",
                        nativePacket(standaloneHost, "echoashfallprotocol:sync_hud", "echo:runtime",
                                Map.of("source", "native_host_smoke")),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Packets",
                                "sendToPlayer",
                                "native_client.packet.send.sync_hud"
                        )
                ),
                "packet.send_to_player",
                "standalone AdapterCore packet send must mutate packet log"
        ).get("eventName"));
        events.put("packetBroadcast", requireMutatingEvent(
                invokeNamed(
                        packets,
                        "broadcast",
                        nativePacket(standaloneHost, "echoashfallprotocol:broadcast_hint", "echo:runtime",
                                Map.of("source", "native_host_smoke")),
                        mutationContext(
                                standaloneHost,
                                "EchoNativeRuntimeHost.Packets",
                                "broadcast",
                                "native_client.packet.broadcast.hint"
                        )
                ),
                "packet.broadcast",
                "standalone AdapterCore packet broadcast must mutate packet log"
        ).get("eventName"));
        require(standaloneHost.context().packetLog().size() >= packetsBefore + 2,
                "standalone AdapterCore packet operations must append runtime packets");
        return Map.copyOf(events);
    }

    private static Map<String, Object> requireMutatingEvent(
            Object result,
            String eventName,
            String message
    ) {
        Map<String, Object> nativeSnapshot = snapshot(result);
        require(mutated(result), message);
        require(Boolean.TRUE.equals(nativeSnapshot.get("saveTouched")), message + " and write save data");
        require(eventName.equals(nativeSnapshot.get("eventName")), message + " with event " + eventName);
        return nativeSnapshot;
    }

    private static Object nativePlayerRef(StandaloneEchoRuntimeHost standaloneHost) throws Exception {
        Class<?> playerRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativePlayerRef");
        return playerRefClass.getConstructor(String.class).newInstance(standaloneHost.context().playerId());
    }

    private static Object nativePosition(
            StandaloneEchoRuntimeHost standaloneHost,
            double x,
            double y,
            double z
    ) throws Exception {
        Class<?> positionClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativePosition");
        return positionClass
                .getConstructor(String.class, double.class, double.class, double.class, float.class, float.class)
                .newInstance(standaloneHost.context().dimensionId(), x, y, z, 0.0F, 0.0F);
    }

    private static Object nativeBlockRef(
            StandaloneEchoRuntimeHost standaloneHost,
            int x,
            int y,
            int z
    ) throws Exception {
        Class<?> blockRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockRef");
        return blockRefClass
                .getConstructor(String.class, int.class, int.class, int.class)
                .newInstance(standaloneHost.context().dimensionId(), x, y, z);
    }

    private static Object nativeBlockState(String blockId) throws Exception {
        Class<?> blockStateClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockState");
        return blockStateClass.getConstructor(String.class, Map.class).newInstance(blockId, Map.of());
    }

    private static Object nativeStructurePlacement(
            StandaloneEchoRuntimeHost standaloneHost,
            String structureId,
            int x,
            int y,
            int z
    ) throws Exception {
        Class<?> placementClass =
                Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeStructurePlacement");
        return placementClass
                .getConstructor(String.class, String.class, int.class, int.class, int.class, String.class, Map.class)
                .newInstance(structureId, standaloneHost.context().dimensionId(), x, y, z, "origin", Map.of());
    }

    private static Object nativeBlockEntitySnapshot(
            Object blockRef,
            String blockEntityId,
            Map<String, Object> state
    ) throws Exception {
        Class<?> snapshotClass =
                Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockEntitySnapshot");
        Class<?> blockRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockRef");
        return snapshotClass
                .getConstructor(String.class, blockRefClass, Map.class)
                .newInstance(blockEntityId, blockRef, Map.copyOf(state));
    }

    private static Object nativeCapabilityRequest(
            Object blockRef,
            String capabilityId,
            String side,
            Map<String, Object> query
    ) throws Exception {
        Class<?> requestClass =
                Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeCapabilityRequest");
        Class<?> blockRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockRef");
        return requestClass
                .getConstructor(String.class, blockRefClass, String.class, Map.class)
                .newInstance(capabilityId, blockRef, side, Map.copyOf(query));
    }

    private static Object nativeItemStack(String itemId, int count) throws Exception {
        Class<?> stackClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeItemStack");
        return stackClass.getConstructor(String.class, int.class, Map.class).newInstance(itemId, count, Map.of());
    }

    private static Object nativePacket(
            StandaloneEchoRuntimeHost standaloneHost,
            String packetId,
            String channel,
            Map<String, Object> payload
    ) throws Exception {
        Class<?> playerRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativePlayerRef");
        Class<?> packetClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativePacket");
        return packetClass
                .getConstructor(String.class, playerRefClass, String.class, Map.class)
                .newInstance(packetId, nativePlayerRef(standaloneHost), channel, Map.copyOf(payload));
    }

    private static Object mutationContext(
            StandaloneEchoRuntimeHost standaloneHost,
            String nativeInterface,
            String nativeMethod,
            String idempotencyKey
    ) throws Exception {
        Class<?> contextClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeMutationContext");
        return contextClass
                .getConstructor(String.class, String.class, String.class, String.class, long.class, Map.class)
                .newInstance(
                        "native_client",
                        standaloneHost.context().dimensionId(),
                        idempotencyKey,
                        "SERVER",
                        standaloneHost.context().gameTime(),
                        Map.of(
                                "nativeInterface", nativeInterface,
                                "nativeMethod", nativeMethod,
                                "hostRuntime", "echo_runtime_standalone"
                        )
                );
    }

    private static Object invokeNamed(Object target, String methodName, Object... args) throws Exception {
        for (Method method : target.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                method.trySetAccessible();
                return method.invoke(target, args);
            }
        }
        throw new NoSuchMethodException(methodName + " with " + args.length + " arguments");
    }

    private static Object publishEvent(
            Object events,
            StandaloneEchoRuntimeHost standaloneHost,
            String eventName,
            Map<String, Object> payload,
            String idempotencyKey
    ) throws Exception {
        Class<?> playerRefClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativePlayerRef");
        Class<?> eventClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeEvent");
        Class<?> contextClass = Class.forName("com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeMutationContext");
        Object playerRef = playerRefClass.getConstructor(String.class).newInstance(standaloneHost.context().playerId());
        Object event = eventClass.getConstructor(String.class, playerRefClass, Map.class)
                .newInstance(
                        eventName,
                        playerRef,
                        Map.copyOf(payload)
                );
        Object context = contextClass
                .getConstructor(String.class, String.class, String.class, String.class, long.class, Map.class)
                .newInstance(
                        "native_client",
                        standaloneHost.context().dimensionId(),
                        idempotencyKey,
                        "SERVER",
                        standaloneHost.context().gameTime(),
                        Map.of(
                                "nativeInterface", "EchoNativeRuntimeHost.Events",
                                "nativeMethod", "publish",
                                "hostRuntime", "echo_runtime_standalone"
                        )
                );
        Method publish = events.getClass().getMethod("publish", eventClass, contextClass);
        publish.trySetAccessible();
        return publish.invoke(events, event, context);
    }

    private static boolean mutated(Object result) {
        Object completed = invoke(result, "completedWithMutation");
        if (completed instanceof Boolean value) {
            return value;
        }
        Object raw = invoke(result, "mutated");
        return raw instanceof Boolean value && value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> snapshot(Object result) {
        Object value = invoke(result, "snapshot");
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> details(Map<String, Object> snapshot) {
        Object value = snapshot.get("details");
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            method.trySetAccessible();
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static int count(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
