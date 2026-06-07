package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class StandaloneAdapterCoreNativeHostBridge {
    private static final String RUNTIME_HOST_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost";
    private static final String RUNTIME_HOST_REGISTRY =
            "com.knoxhack.echo.adaptercore.EchoRuntimeHostRegistry";
    private static final String RUNTIME_HOST_CAPABILITIES =
            "com.knoxhack.echo.adaptercore.EchoRuntimeHostCapabilities";
    private static final String PLAYER_INVENTORY_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$PlayerInventory";
    private static final String PLAYER_STATE_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$PlayerState";
    private static final String WORLD_BLOCKS_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$WorldBlocks";
    private static final String WORLD_STATE_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$WorldState";
    private static final String STRUCTURES_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$Structures";
    private static final String BLOCK_ENTITIES_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$BlockEntities";
    private static final String CAPABILITIES_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$Capabilities";
    private static final String EVENTS_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$Events";
    private static final String PACKETS_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$Packets";
    private static final String HUD_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$Hud";
    private static final String SAVE_DATA_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$SaveData";
    private static final String CONTENT_REGISTRIES_INTERFACE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$ContentRegistries";
    private static final String NATIVE_ITEM_STACK =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeItemStack";
    private static final String NATIVE_BLOCK_REF =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockRef";
    private static final String NATIVE_BLOCK_STATE =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockState";
    private static final String NATIVE_BLOCK_ENTITY_SNAPSHOT =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeBlockEntitySnapshot";
    private static final String NATIVE_EVENT =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeEvent";
    private static final String NATIVE_SAVE_DATA =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeSaveData";
    private static final String NATIVE_CONTENT_REGISTRATION =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeContentRegistration";
    private static final String NATIVE_RESULT =
            "com.knoxhack.echo.adaptercore.EchoNativeRuntimeHost$NativeResult";

    private StandaloneAdapterCoreNativeHostBridge() {
    }

    public static boolean registerIfAdapterCorePresent(StandaloneEchoRuntimeHost host) {
        Objects.requireNonNull(host, "host");
        try {
            Class<?> runtimeHostInterface = Class.forName(RUNTIME_HOST_INTERFACE);
            Class<?> registryClass = Class.forName(RUNTIME_HOST_REGISTRY);
            Class<?> capabilitiesClass = Class.forName(RUNTIME_HOST_CAPABILITIES);
            Class<?> playerInventoryInterface = Class.forName(PLAYER_INVENTORY_INTERFACE);
            Class<?> playerStateInterface = Class.forName(PLAYER_STATE_INTERFACE);
            Class<?> worldBlocksInterface = Class.forName(WORLD_BLOCKS_INTERFACE);
            Class<?> worldStateInterface = Class.forName(WORLD_STATE_INTERFACE);
            Class<?> structuresInterface = Class.forName(STRUCTURES_INTERFACE);
            Class<?> blockEntitiesInterface = Class.forName(BLOCK_ENTITIES_INTERFACE);
            Class<?> hostCapabilitiesInterface = Class.forName(CAPABILITIES_INTERFACE);
            Class<?> eventsInterface = Class.forName(EVENTS_INTERFACE);
            Class<?> packetsInterface = Class.forName(PACKETS_INTERFACE);
            Class<?> hudInterface = Class.forName(HUD_INTERFACE);
            Class<?> saveDataInterface = Class.forName(SAVE_DATA_INTERFACE);
            Class<?> contentRegistriesInterface = Class.forName(CONTENT_REGISTRIES_INTERFACE);
            Class<?> nativeItemStackClass = Class.forName(NATIVE_ITEM_STACK);
            Class<?> nativeBlockStateClass = Class.forName(NATIVE_BLOCK_STATE);
            Class<?> nativeBlockEntitySnapshotClass = Class.forName(NATIVE_BLOCK_ENTITY_SNAPSHOT);
            Class<?> nativeEventClass = Class.forName(NATIVE_EVENT);
            Class<?> nativeSaveDataClass = Class.forName(NATIVE_SAVE_DATA);
            Class<?> nativeContentRegistrationClass = Class.forName(NATIVE_CONTENT_REGISTRATION);
            Class<?> nativeResultClass = Class.forName(NATIVE_RESULT);
            Object inventoryProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{playerInventoryInterface},
                    new InventoryHandler(host, nativeItemStackClass, nativeResultClass)
            );
            Object playerStateProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{playerStateInterface},
                    new PlayerStateHandler(host, nativeResultClass)
            );
            Object worldBlocksProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{worldBlocksInterface},
                    new WorldBlocksHandler(host, nativeBlockStateClass, nativeResultClass)
            );
            Object worldStateProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{worldStateInterface},
                    new WorldStateHandler(host, nativeResultClass)
            );
            Object structuresProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{structuresInterface},
                    new StructuresHandler(host, nativeResultClass)
            );
            Object blockEntitiesProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{blockEntitiesInterface},
                    new BlockEntitiesHandler(host, nativeBlockEntitySnapshotClass, nativeResultClass)
            );
            Object hostCapabilitiesProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{hostCapabilitiesInterface},
                    new CapabilitiesHandler(host, nativeResultClass)
            );
            Object eventsProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{eventsInterface},
                    new EventsHandler(host, nativeEventClass, nativeResultClass)
            );
            Object packetsProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{packetsInterface},
                    new PacketsHandler(host, nativeResultClass)
            );
            Object hudProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{hudInterface},
                    new HudHandler(host, nativeResultClass)
            );
            Object saveDataProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{saveDataInterface},
                    new SaveDataHandler(host, nativeSaveDataClass, nativeResultClass)
            );
            Object contentRegistriesProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{contentRegistriesInterface},
                    new ContentRegistriesHandler(host, nativeContentRegistrationClass, nativeResultClass)
            );
            Object hostProxy = Proxy.newProxyInstance(
                    runtimeHostInterface.getClassLoader(),
                    new Class<?>[]{runtimeHostInterface},
                    new RuntimeHostHandler(
                            host,
                            inventoryProxy,
                            playerStateProxy,
                            worldBlocksProxy,
                            worldStateProxy,
                            structuresProxy,
                            blockEntitiesProxy,
                            hostCapabilitiesProxy,
                            eventsProxy,
                            packetsProxy,
                            hudProxy,
                            saveDataProxy,
                            contentRegistriesProxy
                    )
            );
            Object capabilities = capabilitiesClass
                    .getConstructor(String.class, Set.class, Set.class, Set.class,
                            boolean.class, boolean.class, boolean.class)
                    .newInstance(
                            host.context().runtimeHostId(),
                            Set.of(
                                    "EchoNativeRuntimeHost.PlayerInventory",
                                    "EchoNativeRuntimeHost.PlayerState",
                                    "EchoNativeRuntimeHost.WorldBlocks",
                                    "EchoNativeRuntimeHost.WorldState",
                                    "EchoNativeRuntimeHost.Structures",
                                    "EchoNativeRuntimeHost.BlockEntities",
                                    "EchoNativeRuntimeHost.Capabilities",
                                     "EchoNativeRuntimeHost.Events",
                                    "EchoNativeRuntimeHost.Packets",
                                     "EchoNativeRuntimeHost.Hud",
                                     "EchoNativeRuntimeHost.SaveData",
                                     "EchoNativeRuntimeHost.ContentRegistries"
                             ),
                             Set.of(
                                    "player.scanner_used",
                                    "native.ui.use_scanner",
                                    "player.inventory.grant",
                                    "native.ui.terminal_command",
                                    "command_execution",
                                    "native.ui.index_search",
                                    "player.terminal_opened",
                                    "native.ui.hud_refresh",
                                    "client_tick",
                                    "native.ui.mission_log_update",
                                    "mission.objective_completed",
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
                                     "packet.broadcast",
                                     "adaptercore.content.register"
                             ),
                            Set.of(EchoAdapterCoreStandaloneContentBridge.EMERGENCY_SCANNER_ITEM_ID),
                            true,
                            true,
                            true
                    );
            Object registry = registryClass.getMethod("global").invoke(null);
            registryClass
                    .getMethod("register", String.class, runtimeHostInterface, capabilitiesClass)
                    .invoke(registry, host.context().runtimeHostId(), hostProxy, capabilities);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static final class RuntimeHostHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Object inventoryProxy;
        private final Object playerStateProxy;
        private final Object worldBlocksProxy;
        private final Object worldStateProxy;
        private final Object structuresProxy;
        private final Object blockEntitiesProxy;
        private final Object hostCapabilitiesProxy;
        private final Object eventsProxy;
        private final Object packetsProxy;
        private final Object hudProxy;
        private final Object saveDataProxy;
        private final Object contentRegistriesProxy;

        private RuntimeHostHandler(
                StandaloneEchoRuntimeHost host,
                Object inventoryProxy,
                Object playerStateProxy,
                Object worldBlocksProxy,
                Object worldStateProxy,
                Object structuresProxy,
                Object blockEntitiesProxy,
                Object hostCapabilitiesProxy,
                Object eventsProxy,
                Object packetsProxy,
                Object hudProxy,
                Object saveDataProxy,
                Object contentRegistriesProxy
        ) {
            this.host = host;
            this.inventoryProxy = inventoryProxy;
            this.playerStateProxy = playerStateProxy;
            this.worldBlocksProxy = worldBlocksProxy;
            this.worldStateProxy = worldStateProxy;
            this.structuresProxy = structuresProxy;
            this.blockEntitiesProxy = blockEntitiesProxy;
            this.hostCapabilitiesProxy = hostCapabilitiesProxy;
            this.eventsProxy = eventsProxy;
            this.packetsProxy = packetsProxy;
            this.hudProxy = hudProxy;
            this.saveDataProxy = saveDataProxy;
            this.contentRegistriesProxy = contentRegistriesProxy;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            return switch (method.getName()) {
                case "playerInventory" -> inventoryProxy;
                case "playerState" -> playerStateProxy;
                case "worldBlocks" -> worldBlocksProxy;
                case "worldState" -> worldStateProxy;
                case "structures" -> structuresProxy;
                case "blockEntities" -> blockEntitiesProxy;
                case "capabilities" -> hostCapabilitiesProxy;
                case "events" -> eventsProxy;
                case "packets" -> packetsProxy;
                case "hud" -> hudProxy;
                case "saveData" -> saveDataProxy;
                case "contentRegistries" -> contentRegistriesProxy;
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }
    }

    private static final class InventoryHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeItemStackClass;
        private final Class<?> nativeResultClass;

        private InventoryHandler(
                StandaloneEchoRuntimeHost host,
                Class<?> nativeItemStackClass,
                Class<?> nativeResultClass
        ) {
            this.host = host;
            this.nativeItemStackClass = nativeItemStackClass;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "grant" -> grant(args == null ? new Object[0] : args);
                case "remove" -> remove(args == null ? new Object[0] : args);
                case "snapshot" -> snapshot();
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.PlayerInventory["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object grant(Object[] args) throws Exception {
            Object stack = args.length > 1 ? args[1] : null;
            Object context = args.length > 2 ? args[2] : null;
            String canonicalId = text(invokeAccessor(stack, "itemId"));
            int amount = number(invokeAccessor(stack, "count"), 1);
            String actionId = text(invokeAccessor(context, "idempotencyKey"));
            if (actionId.isBlank()) {
                actionId = "native_client.grant_item." + compactActionKey(canonicalId);
            }
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.playerInventory().grant(
                    actionId,
                    "player.inventory.grant",
                    canonicalId,
                    amount
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime inventory grant applied.");
        }

        private Object remove(Object[] args) throws Exception {
            String canonicalId = args.length > 1 ? text(args[1]) : "";
            int amount = args.length > 2 ? number(args[2], 1) : 1;
            Object context = args.length > 3 ? args[3] : null;
            String actionId = actionId(context, "native_client.remove_item." + compactActionKey(canonicalId));
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.playerInventory().remove(
                    actionId,
                    "player.inventory.remove",
                    canonicalId,
                    amount
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime inventory remove applied.");
        }

        private List<Object> snapshot() throws Exception {
            ArrayList<Object> stacks = new ArrayList<>();
            for (Map.Entry<String, Object> entry : host.context().inventorySnapshot().entrySet()) {
                stacks.add(nativeItemStackClass
                        .getConstructor(String.class, int.class, Map.class)
                        .newInstance(entry.getKey(), number(entry.getValue(), 1), Map.of()));
            }
            return List.copyOf(stacks);
        }

    }

    private static final class PlayerStateHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeResultClass;

        private PlayerStateHandler(StandaloneEchoRuntimeHost host, Class<?> nativeResultClass) {
            this.host = host;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "teleport" -> teleport(args == null ? new Object[0] : args);
                case "bindRespawn" -> bindRespawn(args == null ? new Object[0] : args);
                case "grantAdvancement" -> grantAdvancement(args == null ? new Object[0] : args);
                case "writePersistentState" -> writePersistentState(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.PlayerState["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object teleport(Object[] args) throws Exception {
            Object position = args.length > 1 ? args[1] : null;
            Object context = args.length > 2 ? args[2] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.playerState().teleport(
                    actionId(context, "native_client.player.teleport"),
                    "player.state.teleport",
                    decimal(invokeAccessor(position, "x"), 0.0D),
                    decimal(invokeAccessor(position, "y"), 0.0D),
                    decimal(invokeAccessor(position, "z"), 0.0D)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime player teleport applied.");
        }

        private Object bindRespawn(Object[] args) throws Exception {
            Object position = args.length > 1 ? args[1] : null;
            Object context = args.length > 3 ? args[3] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.playerState().bindRespawn(
                    actionId(context, "native_client.player.bind_respawn"),
                    "player.state.bind_respawn",
                    number(invokeAccessor(position, "x"), 0),
                    number(invokeAccessor(position, "y"), 0),
                    number(invokeAccessor(position, "z"), 0)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime respawn binding applied.");
        }

        private Object grantAdvancement(Object[] args) throws Exception {
            String advancementId = args.length > 1 ? text(args[1]) : "";
            String criterion = args.length > 2 ? text(args[2]) : "";
            Object context = args.length > 3 ? args[3] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.playerState().writePersistentState(
                    actionId(context, "native_client.player.advancement." + compactActionKey(advancementId)),
                    "player.advancement.grant",
                    "advancement/" + advancementId,
                    criterion.isBlank() ? "granted" : criterion,
                    Map.of(
                            "advancementId", advancementId,
                            "criterion", criterion,
                            "granted", true
                    )
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime advancement grant applied.");
        }

        private Object writePersistentState(Object[] args) throws Exception {
            String key = args.length > 1 ? text(args[1]) : "";
            Object value = args.length > 2 ? args[2] : "";
            Object context = args.length > 3 ? args[3] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.playerState().writePersistentState(
                    actionId(context, "native_client.player.state." + compactActionKey(key)),
                    "player.state.write",
                    key,
                    value,
                    Map.of("key", key, "value", Objects.toString(value, ""))
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime player state written.");
        }
    }

    private static final class WorldBlocksHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeBlockStateClass;
        private final Class<?> nativeResultClass;

        private WorldBlocksHandler(
                StandaloneEchoRuntimeHost host,
                Class<?> nativeBlockStateClass,
                Class<?> nativeResultClass
        ) {
            this.host = host;
            this.nativeBlockStateClass = nativeBlockStateClass;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "setBlock" -> setBlock(args == null ? new Object[0] : args);
                case "clearBlock" -> clearBlock(args == null ? new Object[0] : args);
                case "blockState" -> blockState(args == null ? new Object[0] : args);
                case "isLoaded" -> isLoaded(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.WorldBlocks["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object setBlock(Object[] args) throws Exception {
            Object blockRef = args.length > 0 ? args[0] : null;
            Object state = args.length > 1 ? args[1] : null;
            Object context = args.length > 2 ? args[2] : null;
            String blockId = text(invokeAccessor(state, "blockId"));
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.worldBlocks().setBlock(
                    actionId(context, "native_client.world.block.set." + compactActionKey(blockPositionKey(blockRef))),
                    "world.block.set",
                    blockId,
                    blockX(blockRef),
                    blockY(blockRef),
                    blockZ(blockRef)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime world block set.");
        }

        private Object clearBlock(Object[] args) throws Exception {
            Object blockRef = args.length > 0 ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.worldBlocks().clearBlock(
                    actionId(context, "native_client.world.block.clear." + compactActionKey(blockPositionKey(blockRef))),
                    "world.block.clear",
                    blockX(blockRef),
                    blockY(blockRef),
                    blockZ(blockRef)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime world block cleared.");
        }

        private Object blockState(Object[] args) throws Exception {
            Object blockRef = args.length > 0 ? args[0] : null;
            EchoVoxelBlock block = host.worldBlocks().blockState(blockX(blockRef), blockY(blockRef), blockZ(blockRef));
            return nativeBlockStateClass
                    .getConstructor(String.class, Map.class)
                    .newInstance(block.id(), Map.of(
                            "displayName", block.displayName(),
                            "atlasKey", block.atlasKey(),
                            "argb", block.argb(),
                            "detailArgb", block.detailArgb(),
                            "solid", block.solid(),
                            "opaque", block.opaque()
                    ));
        }

        private boolean isLoaded(Object[] args) {
            Object blockRef = args.length > 0 ? args[0] : null;
            return host.worldBlocks().isLoaded(blockX(blockRef), blockY(blockRef), blockZ(blockRef));
        }
    }

    private static final class WorldStateHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeResultClass;

        private WorldStateHandler(StandaloneEchoRuntimeHost host, Class<?> nativeResultClass) {
            this.host = host;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "writeMarker" -> writeMarker(args == null ? new Object[0] : args);
                case "writeWeatherState" -> writeWeatherState(args == null ? new Object[0] : args);
                case "writeRouteState" -> writeRouteState(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.WorldState["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object writeMarker(Object[] args) throws Exception {
            String markerId = args.length > 0 ? text(args[0]) : "";
            Map<String, Object> payload = args.length > 1 ? map(args[1]) : Map.of();
            Object context = args.length > 2 ? args[2] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.worldState().writeMarker(
                    actionId(context, "native_client.world.marker." + compactActionKey(markerId)),
                    "world.state.marker.write",
                    markerId,
                    payload
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime world marker written.");
        }

        private Object writeWeatherState(Object[] args) throws Exception {
            String stateId = args.length > 0 ? text(args[0]) : "";
            LinkedHashMap<String, Object> payload = new LinkedHashMap<>(args.length > 1 ? map(args[1]) : Map.of());
            payload.putIfAbsent("stateId", stateId);
            Object context = args.length > 2 ? args[2] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.worldState().writeWeatherState(
                    actionId(context, "native_client.world.weather." + compactActionKey(stateId)),
                    "world.state.weather.write",
                    Map.copyOf(payload)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime weather state written.");
        }

        private Object writeRouteState(Object[] args) throws Exception {
            String routeId = args.length > 0 ? text(args[0]) : "";
            LinkedHashMap<String, Object> payload = new LinkedHashMap<>(args.length > 1 ? map(args[1]) : Map.of());
            payload.putIfAbsent("routeId", routeId);
            Object context = args.length > 2 ? args[2] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.worldState().writeRouteState(
                    actionId(context, "native_client.world.route." + compactActionKey(routeId)),
                    "world.state.route.write",
                    Map.copyOf(payload)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime route state written.");
        }
    }

    private static final class StructuresHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeResultClass;

        private StructuresHandler(StandaloneEchoRuntimeHost host, Class<?> nativeResultClass) {
            this.host = host;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "placeStructure" -> placeStructure(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.Structures["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object placeStructure(Object[] args) throws Exception {
            Object placement = args.length > 0 ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            String structureId = text(invokeAccessor(placement, "structureId"));
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.structures().placeStructure(
                    actionId(context, "native_client.structure.place." + compactActionKey(structureId)),
                    "world.structure.place",
                    structureId,
                    number(invokeAccessor(placement, "originX"), 0),
                    number(invokeAccessor(placement, "originY"), 0),
                    number(invokeAccessor(placement, "originZ"), 0)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime structure placed.");
        }
    }

    private static final class BlockEntitiesHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeBlockEntitySnapshotClass;
        private final Class<?> nativeResultClass;

        private BlockEntitiesHandler(
                StandaloneEchoRuntimeHost host,
                Class<?> nativeBlockEntitySnapshotClass,
                Class<?> nativeResultClass
        ) {
            this.host = host;
            this.nativeBlockEntitySnapshotClass = nativeBlockEntitySnapshotClass;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "tick" -> tick(args == null ? new Object[0] : args);
                case "snapshot" -> snapshot(args == null ? new Object[0] : args);
                case "applySnapshot" -> applySnapshot(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.BlockEntities["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object tick(Object[] args) throws Exception {
            Object blockRef = args.length > 0 ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            String key = blockPositionKey(blockRef);
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.blockEntities().tick(
                    actionId(context, "native_client.block_entity.tick." + compactActionKey(key)),
                    "block_entity.tick",
                    key
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime block entity ticked.");
        }

        private Object snapshot(Object[] args) throws Exception {
            Object blockRef = args.length > 0 ? args[0] : null;
            String key = blockPositionKey(blockRef);
            Map<String, Object> state = host.blockEntities().snapshot(key);
            return nativeBlockEntitySnapshotClass
                    .getConstructor(String.class, Class.forName(NATIVE_BLOCK_REF), Map.class)
                    .newInstance("standalone:" + compactActionKey(key), blockRef, state);
        }

        private Object applySnapshot(Object[] args) throws Exception {
            Object snapshot = args.length > 0 ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            Object blockRef = invokeAccessor(snapshot, "block");
            String key = blockPositionKey(blockRef);
            LinkedHashMap<String, Object> state = new LinkedHashMap<>(map(invokeAccessor(snapshot, "state")));
            state.putIfAbsent("blockEntityId", text(invokeAccessor(snapshot, "blockEntityId")));
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.blockEntities().applySnapshot(
                    actionId(context, "native_client.block_entity.apply." + compactActionKey(key)),
                    "block_entity.snapshot.apply",
                    key,
                    Map.copyOf(state)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime block entity snapshot applied.");
        }
    }

    private static final class CapabilitiesHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeResultClass;

        private CapabilitiesHandler(StandaloneEchoRuntimeHost host, Class<?> nativeResultClass) {
            this.host = host;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "insertItem" -> insertItem(args == null ? new Object[0] : args);
                case "extractItem" -> extractItem(args == null ? new Object[0] : args);
                case "receiveEnergy" -> receiveEnergy(args == null ? new Object[0] : args);
                case "extractEnergy" -> extractEnergy(args == null ? new Object[0] : args);
                case "readCapability" -> readCapability(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.Capabilities["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object insertItem(Object[] args) throws Exception {
            Object request = args.length > 0 ? args[0] : null;
            Object stack = args.length > 1 ? args[1] : null;
            Object context = args.length > 2 ? args[2] : null;
            String canonicalId = text(invokeAccessor(stack, "itemId"));
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.capabilities().insertItem(
                    actionId(context, "native_client.capability.item.insert." + compactActionKey(canonicalId)),
                    "capability.item.insert",
                    capabilityKey(request),
                    canonicalId,
                    number(invokeAccessor(stack, "count"), 1)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime capability item inserted.");
        }

        private Object extractItem(Object[] args) throws Exception {
            Object request = args.length > 0 ? args[0] : null;
            String canonicalId = args.length > 1 ? text(args[1]) : "";
            int amount = args.length > 2 ? number(args[2], 1) : 1;
            Object context = args.length > 3 ? args[3] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.capabilities().extractItem(
                    actionId(context, "native_client.capability.item.extract." + compactActionKey(canonicalId)),
                    "capability.item.extract",
                    capabilityKey(request),
                    canonicalId,
                    amount
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime capability item extracted.");
        }

        private Object receiveEnergy(Object[] args) throws Exception {
            Object request = args.length > 0 ? args[0] : null;
            int amount = args.length > 1 ? number(args[1], 1) : 1;
            Object context = args.length > 2 ? args[2] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.capabilities().receiveEnergy(
                    actionId(context, "native_client.capability.energy.receive." + compactActionKey(capabilityKey(request))),
                    "capability.energy.receive",
                    capabilityKey(request),
                    amount
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime capability energy received.");
        }

        private Object extractEnergy(Object[] args) throws Exception {
            Object request = args.length > 0 ? args[0] : null;
            int amount = args.length > 1 ? number(args[1], 1) : 1;
            Object context = args.length > 2 ? args[2] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.capabilities().extractEnergy(
                    actionId(context, "native_client.capability.energy.extract." + compactActionKey(capabilityKey(request))),
                    "capability.energy.extract",
                    capabilityKey(request),
                    amount
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime capability energy extracted.");
        }

        private Map<String, Object> readCapability(Object[] args) {
            Object request = args.length > 0 ? args[0] : null;
            return host.capabilities().readCapability(capabilityKey(request));
        }
    }

    private static final class EventsHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeEventClass;
        private final Class<?> nativeResultClass;

        private EventsHandler(
                StandaloneEchoRuntimeHost host,
                Class<?> nativeEventClass,
                Class<?> nativeResultClass
        ) {
            this.host = host;
            this.nativeEventClass = nativeEventClass;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "publish" -> publish(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.Events["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object publish(Object[] args) throws Exception {
            Object event = args.length > 0 && nativeEventClass.isInstance(args[0]) ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            String eventName = text(invokeAccessor(event, "eventId"));
            Map<String, Object> payload = map(invokeAccessor(event, "payload"));
            String canonicalId = text(payload.get("canonicalId"));
            if (canonicalId.isBlank()) {
                canonicalId = text(payload.get("screenId"));
            }
            if (canonicalId.isBlank()) {
                canonicalId = eventName;
            }
            String actionId = text(invokeAccessor(context, "idempotencyKey"));
            if (actionId.isBlank()) {
                actionId = eventName;
            }
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.events().publish(
                    actionId,
                    eventName,
                    canonicalId,
                    payload
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime event consumed.");
        }
    }

    private static final class PacketsHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeResultClass;

        private PacketsHandler(StandaloneEchoRuntimeHost host, Class<?> nativeResultClass) {
            this.host = host;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "sendToPlayer" -> sendToPlayer(args == null ? new Object[0] : args);
                case "broadcast" -> broadcast(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.Packets["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object sendToPlayer(Object[] args) throws Exception {
            Object packet = args.length > 0 ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.packets().sendToPlayer(
                    actionId(context, "native_client.packet.send." + compactActionKey(packetId(packet))),
                    "packet.send_to_player",
                    packetId(packet),
                    packetPayload(packet)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime packet sent to player.");
        }

        private Object broadcast(Object[] args) throws Exception {
            Object packet = args.length > 0 ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.packets().broadcast(
                    actionId(context, "native_client.packet.broadcast." + compactActionKey(packetId(packet))),
                    "packet.broadcast",
                    packetId(packet),
                    packetPayload(packet)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime packet broadcast.");
        }

        private static String packetId(Object packet) {
            return text(invokeAccessor(packet, "packetId"));
        }

        private static Map<String, Object> packetPayload(Object packet) {
            LinkedHashMap<String, Object> payload = new LinkedHashMap<>(map(invokeAccessor(packet, "payload")));
            payload.putIfAbsent("channel", text(invokeAccessor(packet, "channel")));
            Object player = invokeAccessor(packet, "player");
            String playerId = text(invokeAccessor(player, "playerId"));
            if (!playerId.isBlank()) {
                payload.putIfAbsent("playerId", playerId);
            }
            return Map.copyOf(payload);
        }
    }

    private static final class HudHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeResultClass;

        private HudHandler(StandaloneEchoRuntimeHost host, Class<?> nativeResultClass) {
            this.host = host;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "publishNotification" -> publishNotification(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.Hud["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object publishNotification(Object[] args) throws Exception {
            Map<String, Object> payload = args.length > 1 ? map(args[1]) : Map.of();
            Object context = args.length > 2 ? args[2] : null;
            String actionId = text(invokeAccessor(context, "idempotencyKey"));
            if (actionId.isBlank()) {
                actionId = "native_client.hud_notification";
            }
            String message = text(payload.get("message"));
            if (message.isBlank()) {
                message = text(payload.get("output"));
            }
            if (message.isBlank()) {
                message = "Native HUD notification";
            }
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.hud().publishNotification(
                    actionId,
                    "hud.notification",
                    message,
                    payload
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime HUD notification emitted.");
        }
    }

    private static final class SaveDataHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeSaveDataClass;
        private final Class<?> nativeResultClass;

        private SaveDataHandler(
                StandaloneEchoRuntimeHost host,
                Class<?> nativeSaveDataClass,
                Class<?> nativeResultClass
        ) {
            this.host = host;
            this.nativeSaveDataClass = nativeSaveDataClass;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "write" -> write(args == null ? new Object[0] : args);
                case "read" -> read(args == null ? new Object[0] : args);
                case "delete" -> delete(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.SaveData["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object write(Object[] args) throws Exception {
            Object data = args.length > 0 && nativeSaveDataClass.isInstance(args[0]) ? args[0] : null;
            Object context = args.length > 1 ? args[1] : null;
            String scope = text(invokeAccessor(data, "scope"));
            String key = text(invokeAccessor(data, "key"));
            Map<String, Object> payload = new LinkedHashMap<>(map(invokeAccessor(data, "payload")));
            payload.putIfAbsent("scope", scope);
            payload.putIfAbsent("key", key);
            String eventName = text(payload.get("eventName"));
            if (eventName.isBlank()) {
                eventName = "save_data.write";
            }
            String actionId = text(invokeAccessor(context, "idempotencyKey"));
            if (actionId.isBlank()) {
                actionId = "native_client.save_data." + compactActionKey(scope + "." + key);
            }
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.saveData().write(
                    actionId,
                    eventName,
                    scope + "/" + key,
                    Map.copyOf(payload)
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime save data written.");
        }

        private Map<String, Object> read(Object[] args) {
            String scope = args.length > 0 ? text(args[0]) : "";
            String key = args.length > 1 ? text(args[1]) : "";
            return host.saveData().read(scope + "/" + key);
        }

        private Object delete(Object[] args) throws Exception {
            String scope = args.length > 0 ? text(args[0]) : "";
            String key = args.length > 1 ? text(args[1]) : "";
            Object context = args.length > 2 ? args[2] : null;
            String actionId = text(invokeAccessor(context, "idempotencyKey"));
            if (actionId.isBlank()) {
                actionId = "native_client.delete_save_data." + compactActionKey(scope + "." + key);
            }
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.saveData().delete(
                    actionId,
                    "save_data.delete",
                    scope + "/" + key
            );
            return adapterCoreResult(host, nativeResultClass, result, "Standalone runtime save data deleted.");
        }
    }

    private static final class ContentRegistriesHandler implements InvocationHandler {
        private final StandaloneEchoRuntimeHost host;
        private final Class<?> nativeContentRegistrationClass;
        private final Class<?> nativeResultClass;

        private ContentRegistriesHandler(
                StandaloneEchoRuntimeHost host,
                Class<?> nativeContentRegistrationClass,
                Class<?> nativeResultClass
        ) {
            this.host = host;
            this.nativeContentRegistrationClass = nativeContentRegistrationClass;
            this.nativeResultClass = nativeResultClass;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            return switch (method.getName()) {
                case "register" -> register(args == null ? new Object[0] : args);
                case "registrations" -> registrations(args == null ? new Object[0] : args);
                case "toString" -> "StandaloneAdapterCoreNativeHostBridge.ContentRegistries["
                        + host.context().runtimeHostId() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null || args.length == 0 ? null : args[0]);
                default -> defaultValue(method.getReturnType());
            };
        }

        private Object register(Object[] args) throws Exception {
            Object registration = args.length > 0 && nativeContentRegistrationClass.isInstance(args[0])
                    ? args[0]
                    : null;
            Object context = args.length > 1 ? args[1] : null;
            Map<String, Object> payload = registrationSnapshot(registration);
            String contentId = text(payload.get("contentId"));
            String actionId = text(invokeAccessor(context, "idempotencyKey"));
            if (actionId.isBlank()) {
                actionId = "native_client.content.register." + compactActionKey(contentId);
            }
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result = host.contentRegistries().register(
                    actionId,
                    "adaptercore.content.register",
                    contentId,
                    payload
            );
            return adapterCoreResult(host, nativeResultClass, result,
                    "Standalone runtime content registration applied.");
        }

        private List<Object> registrations(Object[] args) throws Exception {
            String domain = args.length > 0 ? text(args[0]) : "";
            ArrayList<Object> registrations = new ArrayList<>();
            for (Map<String, Object> row : host.contentRegistries().registrations(domain)) {
                registrations.add(nativeContentRegistrationClass
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
                                text(row.get("moduleId")),
                                text(row.get("contentId")),
                                text(row.get("contentKind")),
                                text(row.get("domain")),
                                text(row.get("displayName")),
                                text(row.get("adapterKey")),
                                text(row.get("neoForgeId")),
                                text(row.get("nativeLoaderId")),
                                text(row.get("standaloneRuntimeId")),
                                map(row.get("metadata"))
                        ));
            }
            return List.copyOf(registrations);
        }

        private static Map<String, Object> registrationSnapshot(Object registration) {
            if (registration == null) {
                return Map.of();
            }
            LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("moduleId", text(invokeAccessor(registration, "moduleId")));
            snapshot.put("contentId", text(invokeAccessor(registration, "contentId")));
            snapshot.put("contentKind", text(invokeAccessor(registration, "contentKind")));
            snapshot.put("domain", text(invokeAccessor(registration, "domain")));
            snapshot.put("displayName", text(invokeAccessor(registration, "displayName")));
            snapshot.put("adapterKey", text(invokeAccessor(registration, "adapterKey")));
            snapshot.put("neoForgeId", text(invokeAccessor(registration, "neoForgeId")));
            snapshot.put("nativeLoaderId", text(invokeAccessor(registration, "nativeLoaderId")));
            snapshot.put("standaloneRuntimeId", text(invokeAccessor(registration, "standaloneRuntimeId")));
            snapshot.put("metadata", map(invokeAccessor(registration, "metadata")));
            return Map.copyOf(snapshot);
        }
    }

    private static Object adapterCoreResult(
            StandaloneEchoRuntimeHost host,
            Class<?> nativeResultClass,
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result,
            String successMessage
    ) throws ReflectiveOperationException {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("runtimeHostId", host.context().runtimeHostId());
        snapshot.put("actionId", result.actionId());
        snapshot.put("eventName", result.eventName());
        snapshot.put("canonicalId", result.canonicalId());
        snapshot.put("saveTouched", result.saveTouched());
        snapshot.put("feedbackEmitted", result.feedbackEmitted());
        snapshot.put("ledgerSequence", result.ledgerSequence());
        snapshot.put("details", result.details());
        snapshot.put("hostSnapshot", result.hostSnapshot());
        String message = result.failureReason().isBlank() ? successMessage : result.failureReason();
        return nativeResultClass
                .getConstructor(boolean.class, String.class, String.class, Map.class)
                .newInstance(result.mutated(), result.status(), message, Map.copyOf(snapshot));
    }

    private static Object invokeAccessor(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            method.trySetAccessible();
            return method.invoke(target);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static String actionId(Object context, String fallback) {
        String idempotencyKey = text(invokeAccessor(context, "idempotencyKey"));
        return idempotencyKey.isBlank() ? fallback : idempotencyKey;
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static int number(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(text(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double decimal(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(text(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int blockX(Object blockRef) {
        return number(invokeAccessor(blockRef, "x"), 0);
    }

    private static int blockY(Object blockRef) {
        return number(invokeAccessor(blockRef, "y"), 0);
    }

    private static int blockZ(Object blockRef) {
        return number(invokeAccessor(blockRef, "z"), 0);
    }

    private static String blockPositionKey(Object blockRef) {
        String dimensionId = text(invokeAccessor(blockRef, "dimensionId"));
        if (dimensionId.isBlank()) {
            dimensionId = "unknown";
        }
        return dimensionId + ":" + blockX(blockRef) + "," + blockY(blockRef) + "," + blockZ(blockRef);
    }

    private static String capabilityKey(Object request) {
        Map<String, Object> query = map(invokeAccessor(request, "query"));
        String explicitKey = text(query.get("capabilityKey"));
        if (explicitKey.isBlank()) {
            explicitKey = text(query.get("key"));
        }
        if (!explicitKey.isBlank()) {
            return explicitKey;
        }
        String capabilityId = text(invokeAccessor(request, "capabilityId"));
        String side = text(invokeAccessor(request, "side"));
        String key = capabilityId.isBlank() ? "adaptercore:capability" : capabilityId;
        Object blockRef = invokeAccessor(request, "block");
        key += "@" + blockPositionKey(blockRef);
        return side.isBlank() ? key : key + "#" + side;
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                mapped.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(mapped);
    }

    private static String compactActionKey(String value) {
        String safe = text(value).toLowerCase(java.util.Locale.ROOT);
        StringBuilder builder = new StringBuilder(safe.length());
        for (int index = 0; index < safe.length(); index++) {
            char ch = safe.charAt(index);
            builder.append(Character.isLetterOrDigit(ch) ? ch : '_');
        }
        return builder.isEmpty() ? "unknown" : builder.toString();
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == char.class) {
            return '\0';
        }
        if (returnType == byte.class || returnType == short.class || returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0.0F;
        }
        if (returnType == double.class) {
            return 0.0D;
        }
        return null;
    }
}
