package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentBinding;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EchoClientScreenCatalogSmokeHarness {
    private EchoClientScreenCatalogSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        require(services.language()
                        .itemName("echoashfallprotocol:clean_water_bottle", "fallback")
                        .equals("Clean Water Bottle"),
                "Client language service should resolve Minecraft item lang keys from mounted resource packs");
        require(services.language()
                        .blockName("echoashfallprotocol:ash_campfire", "fallback")
                        .equals("Ash Campfire"),
                "Client language service should resolve Minecraft block lang keys from mounted resource packs");

        EchoClientScreenCatalog catalog = services.screenCatalog();
        EchoClientModScanSummary modScan = services.modScanSummary();
        require(catalog.adapterCoreScreenCount() > 0, "AdapterCore should provide registered UI screens");
        require(modScan.descriptorCount() > 0,
                "Client mod scanner should discover source-backed ECHO module descriptors");
        require(modScan.nativeEntrypointCount() > 0,
                "Client mod scanner should report native loader entrypoints");
        require(modScan.adapterCoreDeclaredCount() > 0,
                "Client mod scanner should report AdapterCore-capable modules");
        require(modScan.roots().stream().anyMatch(root -> root.replace('\\', '/').endsWith("/addons")),
                "Client mod scanner should scan source addon descriptors instead of fixture rows");
        require(modScan.modules().stream().anyMatch(module -> module.id().equals("echoashfallprotocol")),
                "Client mod scanner should expose the root Ashfall protocol module");
        int staticAdapterCoreScreens = catalog.adapterCoreScreenCount();
        EchoClientScreenCatalogEntry runtimeRegisteredScreen = registerRuntimeScreen(services);
        catalog = services.screenCatalog();
        require(catalog.adapterCoreScreenCount() == staticAdapterCoreScreens + 1,
                "Runtime services should merge AdapterCore UI screens registered after client startup");
        require(catalog.findScreen(runtimeRegisteredScreen.screenId()).isPresent(),
                "Runtime-registered AdapterCore screen should be discoverable by standalone screen id");
        require(catalog.findScreen(runtimeRegisteredScreen.contentId()).isPresent(),
                "Runtime-registered AdapterCore screen should be discoverable by canonical content id");
        EchoClientScreenCatalogEntry nativeImportedScreen = importNativeRegisteredScreen(services);
        catalog = services.screenCatalog();
        require(catalog.adapterCoreScreenCount() == staticAdapterCoreScreens + 2,
                "Runtime services should merge native content registrations into AdapterCore UI screens");
        require(catalog.findScreen(nativeImportedScreen.screenId()).isPresent(),
                "Native-imported AdapterCore screen should be discoverable by standalone screen id");
        require(catalog.findScreen(nativeImportedScreen.contentId()).isPresent(),
                "Native-imported AdapterCore screen should be discoverable by canonical content id");
        String nativeBlockId = importNativeRegisteredBlock(services);
        catalog = services.screenCatalog();
        require(catalog.adapterCoreScreenCount() == staticAdapterCoreScreens + 2,
                "Native block registration should not add UI screen routes");
        String nativeItemId = importNativeRegisteredItem(services);
        catalog = services.screenCatalog();
        require(catalog.adapterCoreScreenCount() == staticAdapterCoreScreens + 2,
                "Native item registration should not add UI screen routes");
        String nativeRecipeId = importNativeRegisteredRecipe(services, nativeItemId);
        catalog = services.screenCatalog();
        require(catalog.adapterCoreScreenCount() == staticAdapterCoreScreens + 2,
                "Native recipe registration should not add UI screen routes");

        EchoClientScreenController screens = new EchoClientScreenController();
        screens.updateModScan(modScan);
        screens.updateScreenCatalog(catalog);
        screens.updateTechSurfaceModel(services.techSurfaceModel());
        screens.showMainMenu(false);
        require(screens.snapshot(false).kind() == EchoClientScreenKind.MAIN_MENU,
                "Client should boot the screen controller at main menu");
        require(!screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, false),
                "Inventory navigation should require a live or continuable session");
        require(!screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTAINER, false),
                "Container navigation should require a live or continuable session");
        require(!screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MACHINE, false),
                "Machine navigation should require a live or continuable session");
        require(!screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_TERMINAL, false),
                "Terminal navigation should require a live or continuable session");

        screens.showPauseMenu();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory navigation command should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot inventory = screens.snapshot(true);
        require(inventory.state() == EchoClientGameState.SCREEN_OPEN
                        && inventory.kind() == EchoClientScreenKind.INVENTORY,
                "Inventory should open as a ScreenCore screen route");
        require(inventory.footer().contains("echoscreencore:inventory"),
                "Inventory snapshot should expose the ScreenCore inventory route");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Inventory Back command should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot paused = screens.snapshot(true);
        require(paused.state() == EchoClientGameState.PAUSED
                        && paused.kind() == EchoClientScreenKind.PAUSE_MENU,
                "Inventory Back should return to the pause menu when opened from pause");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTAINER, true),
                "Container navigation command should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot container = screens.snapshot(true);
        require(container.state() == EchoClientGameState.SCREEN_OPEN
                        && container.kind() == EchoClientScreenKind.CONTAINER,
                "Container should open as a ScreenCore screen route");
        require(container.footer().contains("echoscreencore:container"),
                "Container snapshot should expose the ScreenCore container route");
        screens.closeSlotGridScreen(true);
        require(screens.snapshot(true).kind() == EchoClientScreenKind.PAUSE_MENU,
                "Container close should return to the pause menu when opened from pause");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MACHINE, true),
                "Machine navigation command should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot machine = screens.snapshot(true);
        require(machine.state() == EchoClientGameState.SCREEN_OPEN
                        && machine.kind() == EchoClientScreenKind.MACHINE,
                "Machine should open as a ScreenCore screen route");
        require(machine.footer().contains("echoscreencore:machine"),
                "Machine snapshot should expose the ScreenCore machine route");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("AdapterCore Machines: ")),
                "Machine surface should expose AdapterCore machine contract counts");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("Machine State: ")),
                "Machine surface should expose live AdapterCore machine state");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("Recipe Progress: ")),
                "Machine surface should expose machine recipe progress telemetry");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("Power Graph: ")),
                "Machine surface should expose power graph telemetry");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("Block Entities: ")),
                "Machine surface should expose coordinate-backed block entity telemetry");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("Block Entity scrap_press @ ")),
                "Machine surface should expose placed scrap_press block entity coordinates");
        require(machine.options().stream().anyMatch(option -> option.label().startsWith("Port scrap_press/input: ")),
                "Machine surface should expose AdapterCore inventory port payloads");
        require(machine.options().stream().anyMatch(option -> option.command() == EchoClientScreenCommand.OPEN_WORKBENCH),
                "Machine surface should bridge into the workbench recipe route");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Machine Back command should be handled by the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.PAUSE_MENU,
                "Machine Back should return to the pause menu when opened from pause");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_TERMINAL, true),
                "Terminal navigation command should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot terminalSurface = screens.snapshot(true);
        require(terminalSurface.state() == EchoClientGameState.SCREEN_OPEN
                        && terminalSurface.kind() == EchoClientScreenKind.TERMINAL,
                "Terminal should open as a ScreenCore screen route");
        require(terminalSurface.footer().contains("echoscreencore:terminal"),
                "Terminal snapshot should expose the ScreenCore terminal route");
        require(terminalSurface.options().stream()
                        .anyMatch(option -> option.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN
                                && option.targetId().contains("terminal")),
                "Terminal surface should bridge into the registered AdapterCore terminal route");
        require(terminalSurface.options().stream().anyMatch(option -> option.label().startsWith("AdapterCore Commands: ")),
                "Terminal surface should expose AdapterCore command contract counts");
        require(terminalSurface.options().stream().anyMatch(option -> option.label().startsWith("Terminal Payload: ")),
                "Terminal surface should expose AdapterCore terminal payload state");
        require(terminalSurface.options().stream().anyMatch(option -> option.label().startsWith("Terminal Commands: ")),
                "Terminal surface should expose mounted terminal command payload counts");
        require(terminalSurface.options().stream().anyMatch(option -> option.label().startsWith("Command mission: ")),
                "Terminal surface should expose Ashfall mission command payloads");
        require(terminalSurface.options().stream().anyMatch(option -> option.label().startsWith("Terminal Action: scan_target:")),
                "Terminal surface should expose AdapterCore dashboard action payloads");
        EchoClientScreenCommand openTerminalRoute = screens.activateSelection(true);
        require(openTerminalRoute == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                "Selecting the terminal surface primary action should request the registered terminal route");
        require(screens.executeNavigationCommand(openTerminalRoute, true),
                "Terminal surface registered route command should be handled");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.REGISTERED_SCREEN,
                "Terminal primary action should land on the registered AdapterCore terminal screen");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Registered terminal Back should be handled");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.TERMINAL,
                "Registered terminal Back should return to the terminal surface");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, true),
                "Terminal Back command should be handled by the ScreenCore controller");
        require(screens.snapshot(true).kind() == EchoClientScreenKind.PAUSE_MENU,
                "Terminal Back should return to the pause menu when opened from pause");
        requireWorkbenchScreenUsesRecipeData(services, nativeItemId, nativeRecipeId);
        requireNativeBlockRegistrationReachesSession(services, nativeBlockId);
        requireActiveSessionHotImportsReachSession(services);
        requireItemContainerInventoryModel();

        screens.showMainMenu(false);

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_MODS, false),
                "Mods navigation command should be handled");
        EchoClientScreenSnapshot mods = screens.snapshot(false);
        require(mods.kind() == EchoClientScreenKind.MODS, "Mods screen should be active");
        require(mods.options().stream().anyMatch(option -> option.label().startsWith("Module Scan: ")),
                "Mods screen should expose the module scanner summary");
        require(mods.options().stream().anyMatch(option -> option.label().startsWith("Mod echoashfallprotocol")),
                "Mods screen should expose real source-backed module rows");
        require(mods.options().stream().anyMatch(option -> option.label().startsWith("AdapterCore UI: ")),
                "Mods screen should separate AdapterCore UI route rows from module scan rows");
        long registeredRows = mods.options().stream()
                .filter(option -> option.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN)
                .filter(option -> !option.targetId().isBlank())
                .count();
        require(registeredRows == catalog.adapterCoreScreenCount(),
                "Mods screen should expose every AdapterCore UI screen as a selectable row");

        EchoClientScreenOption selected = mods.options().get(mods.selectedIndex());
        EchoClientScreenCatalogEntry expected = catalog.findScreen(selected.targetId()).orElseThrow();
        EchoClientScreenCommand openRegistered = screens.activateSelection(false);
        require(openRegistered == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                "Selecting an AdapterCore UI row should request a registered ScreenCore route");
        require(screens.executeNavigationCommand(openRegistered, false),
                "Registered ScreenCore route command should be handled");

        EchoClientScreenSnapshot detail = screens.snapshot(false);
        require(detail.kind() == EchoClientScreenKind.REGISTERED_SCREEN,
                "Registered screen detail should be active");
        require(detail.title().equals(expected.title()),
                "Registered screen title should come from the AdapterCore catalog");
        require(detail.footer().contains(expected.screenId()),
                "ScreenCore footer should include the registered screen id");
        require(detail.options().stream().anyMatch(option -> option.label().contains(expected.nativeLoaderId())),
                "Registered screen detail should expose the ECHO Native Loader id");
        require(detail.options().stream().anyMatch(option -> option.label().contains(expected.standaloneRuntimeId())),
                "Registered screen detail should expose the standalone runtime id");

        EchoClientScreenCommand back = screens.activateSelection(false);
        require(back == EchoClientScreenCommand.BACK, "Registered screen should offer Back as its primary action");
        require(screens.executeNavigationCommand(back, false), "Back should return to the previous screen");
        require(screens.snapshot(false).kind() == EchoClientScreenKind.MODS,
                "Back should restore the Mods screen");

        System.out.println("client screen catalog smoke PASS registeredScreens="
                + catalog.adapterCoreScreenCount()
                + " totalRoutes="
                + catalog.screenCount()
                + " opened="
                + expected.screenId());
    }

    private static EchoClientScreenCatalogEntry registerRuntimeScreen(EchoClientRuntimeServices services) {
        EchoAdapterCoreRegistryEntry entry = new EchoAdapterCoreRegistryEntry(
                new EchoAdapterCoreContentBinding(
                        "echoruntimehost",
                        "echoruntimehost:ui/runtime_registered_diagnostics",
                        EchoAdapterCoreContentKind.UI_SCREEN,
                        "screencore.runtime.registered_diagnostics",
                        "echoruntimehost:runtime_registered_diagnostics",
                        "echoruntimehost:screen/runtime_registered_diagnostics",
                        "echoruntimehost:standalone/runtime_registered_diagnostics",
                        "",
                        true
                ),
                EchoAdapterCoreDomain.UI_SCREENS,
                "Runtime Registered Diagnostics",
                null
        );
        services.registerAdapterCoreScreen(entry);
        EchoClientScreenCatalogEntry registered = services.screenCatalog()
                .findScreen("echoruntimehost:standalone/runtime_registered_diagnostics")
                .orElseThrow(() -> new AssertionError("Runtime-registered AdapterCore screen was not cataloged"));
        require(registered.title().equals("Runtime Registered Diagnostics"),
                "Runtime-registered screen title should come from the AdapterCore registration");
        require(registered.nativeLoaderId().equals("echoruntimehost:screen/runtime_registered_diagnostics"),
                "Runtime-registered screen should preserve the native loader id");
        return registered;
    }

    private static EchoClientScreenCatalogEntry importNativeRegisteredScreen(EchoClientRuntimeServices services) {
        int before = services.screenCatalog().adapterCoreScreenCount();
        int imported = services.importAdapterCoreContentRegistrations(List.of(Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:ui/native_catalog_import",
                "contentKind", "UI_SCREEN",
                "domain", "ui_screens",
                "displayName", "Native Catalog Import",
                "adapterKey", "screencore.native.catalog_import",
                "neoForgeId", "echoruntimehost:native_catalog_import",
                "nativeLoaderId", "echoruntimehost:screen/native_catalog_import",
                "standaloneRuntimeId", "echoruntimehost:standalone/native_catalog_import",
                "metadata", Map.of("route", "screencore.native.catalog_import")
        )));
        require(imported == 1,
                "Client runtime services should import one native content registration row");
        require(services.screenCatalog().adapterCoreScreenCount() == before + 1,
                "Native content import should add a new AdapterCore screen route");

        Map<String, Object> updatedRow = Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:ui/native_catalog_import",
                "contentKind", "UI_SCREEN",
                "domain", "ui_screens",
                "displayName", "Native Catalog Import Updated",
                "adapterKey", "screencore.native.catalog_import.updated",
                "neoForgeId", "echoruntimehost:native_catalog_import",
                "nativeLoaderId", "echoruntimehost:screen/native_catalog_import",
                "standaloneRuntimeId", "echoruntimehost:standalone/native_catalog_import",
                "metadata", Map.of("route", "screencore.native.catalog_import.updated")
        );
        int updated = services.importAdapterCoreContentRegistrations(List.of(updatedRow));
        require(updated == 1,
                "Client runtime services should accept an updated native registration row");
        require(services.screenCatalog().adapterCoreScreenCount() == before + 1,
                "Updated native registration should replace the existing content id instead of duplicating it");
        require(services.importAdapterCoreContentRegistrations(List.of(updatedRow)) == 0,
                "Identical native registration import should be idempotent");
        require(services.screenCatalog().adapterCoreScreenCount() == before + 1,
                "Idempotent native registration import should not add duplicate screen routes");
        EchoClientScreenCatalogEntry registered = services.screenCatalog()
                .findScreen("echoruntimehost:standalone/native_catalog_import")
                .orElseThrow(() -> new AssertionError("Native-imported AdapterCore screen was not cataloged"));
        require(registered.title().equals("Native Catalog Import Updated"),
                "Native-imported screen title should update from the latest content registration");
        require(registered.adapterKey().equals("screencore.native.catalog_import.updated"),
                "Native-imported screen should preserve the latest adapter route key");
        require(registered.nativeLoaderId().equals("echoruntimehost:screen/native_catalog_import"),
                "Native-imported screen should preserve the native loader id");
        return registered;
    }

    private static String importNativeRegisteredBlock(EchoClientRuntimeServices services) {
        int imported = services.importAdapterCoreContentRegistrations(List.of(Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:block/client_runtime_glass",
                "contentKind", "BLOCK",
                "domain", "blocks",
                "displayName", "Client Runtime Glass",
                "adapterKey", "registry.blocks.client_runtime_glass",
                "neoForgeId", "echoruntimehost:client_runtime_glass",
                "nativeLoaderId", "echoruntimehost:block/client_runtime_glass",
                "standaloneRuntimeId", "echoruntimehost:client_runtime_glass",
                "metadata", Map.of(
                        "liveVoxelId", "echoruntimehost:client_runtime_glass",
                        "argb", "#6FAFE3",
                        "detailArgb", "0xFFBFE8FF",
                        "atlasKey", "echoruntimehost/block/client_runtime_glass",
                        "materialPattern", "TERMINAL_GRID",
                        "solid", false,
                        "opaque", false,
                        "hardness", "0.35"
                )
        )));
        require(imported == 1,
                "Client runtime services should import one native block registration row");
        return "echoruntimehost:client_runtime_glass";
    }

    private static String importNativeRegisteredItem(EchoClientRuntimeServices services) {
        int imported = services.importAdapterCoreContentRegistrations(List.of(Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:item/native_runtime_gel",
                "contentKind", "ITEM",
                "domain", "items",
                "displayName", "Native Runtime Gel",
                "adapterKey", "registry.items.native_runtime_gel",
                "neoForgeId", "echoruntimehost:native_runtime_gel",
                "nativeLoaderId", "echoruntimehost:item/native_runtime_gel",
                "standaloneRuntimeId", "echoruntimehost:native_runtime_gel",
                "metadata", Map.of(
                        "category", "MATERIAL",
                        "maxStackSize", 16,
                        "weight", "0.2",
                        "tags", List.of("adaptercore", "native-content", "gel"),
                        "tooltipLines", List.of("Synthesized by native content registration")
                )
        )));
        require(imported == 1,
                "Client runtime services should import one native item registration row");
        return "echoruntimehost:native_runtime_gel";
    }

    private static String importNativeRegisteredRecipe(EchoClientRuntimeServices services, String nativeItemId) {
        int imported = services.importAdapterCoreContentRegistrations(List.of(Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:recipe/craft_native_runtime_gel",
                "contentKind", "RECIPE",
                "domain", "recipes",
                "displayName", "Native Runtime Gel",
                "adapterKey", "registry.recipes.craft_native_runtime_gel",
                "neoForgeId", "echoruntimehost:craft_native_runtime_gel",
                "nativeLoaderId", "echoruntimehost:recipe/craft_native_runtime_gel",
                "standaloneRuntimeId", "echoruntimehost:craft_native_runtime_gel",
                "metadata", Map.of(
                        "recipeId", "echoruntimehost:craft_native_runtime_gel",
                        "type", "minecraft:crafting_shapeless",
                        "ingredients", List.of("echoashfallprotocol:scrap_metal"),
                        "ingredientCounts", Map.of("echoashfallprotocol:scrap_metal", 1),
                        "result", nativeItemId,
                        "resultCount", 1,
                        "pattern", List.of("S"),
                        "group", "runtime_native",
                        "category", "adaptercore",
                        "sourceLogicalId", "runtime/native/content/native_runtime_gel.json"
                )
        )));
        require(imported == 1,
                "Client runtime services should import one native recipe registration row");
        return "echoruntimehost:craft_native_runtime_gel";
    }

    private static void requireNativeBlockRegistrationReachesSession(
            EchoClientRuntimeServices services,
            String nativeBlockId
    ) {
        EchoClientGameSession session = services.session();
        require(session != null,
                "Native block session smoke requires an active client game session");
        EchoVoxelBlock block = session.bridge().registry().requireLiveVoxelBlock(nativeBlockId);
        require(block.displayName().equals("Client Runtime Glass"),
                "Imported native block should be visible through the active game session registry");
        require(block.materialPattern() == EchoVoxelMaterialPattern.TERMINAL_GRID,
                "Imported native block material should reach the active game session registry");
        require(!block.solid() && !block.opaque(),
                "Imported native block collision flags should reach the active game session registry");
        int x = (int) Math.floor(session.world().spawnX());
        int y = 4;
        int z = (int) Math.floor(session.world().spawnZ());
        require(session.world().setBlockAt(x, y, z, block),
                "Imported native block should be placeable in the active voxel world");
        require(session.world().blockAt(x, y, z).id().equals(nativeBlockId),
                "Imported native block should round-trip through active voxel world storage");
    }

    private static void requireActiveSessionHotImportsReachSession(EchoClientRuntimeServices services) {
        EchoClientGameSession session = services.session();
        require(session != null,
                "Active hot-import smoke requires an active client game session");
        ActiveHotImportIds ids = importActiveRuntimeContent(services);
        require(services.session() == session,
                "Runtime content imports should refresh the active session in place");

        EchoVoxelBlock block = session.bridge().registry().requireLiveVoxelBlock(ids.blockId());
        require(block.displayName().equals("Active Hot Glass"),
                "Active hot-imported block should reach the existing session registry");
        int x = (int) Math.floor(session.world().spawnX()) + 1;
        int y = 4;
        int z = (int) Math.floor(session.world().spawnZ()) + 1;
        require(session.world().setBlockAt(x, y, z, block),
                "Active hot-imported block should be placeable in the existing world");
        require(session.world().blockAt(x, y, z).id().equals(ids.blockId()),
                "Active hot-imported block should round-trip through existing world storage");
        EchoClientScreenRouteRequest route = session.worldInteractionRouteFor(block);
        require(route.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN
                        && route.targetId().equals(ids.screenId()),
                "Active hot-imported block behavior metadata should route through the existing interaction catalog");

        EchoClientWorkbenchRecipeSummary summary = services.workbenchRecipeSummaries().stream()
                .filter(recipe -> recipe.recipeId().equals(ids.recipeId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Active hot-imported recipe should reach live workbench recipes"));
        require(summary.label().equals("Active Hot Alloy"),
                "Active hot-imported recipe should use the imported item display name");
        require(summary.craftable(),
                "Active hot-imported recipe should be craftable from a runtime item tag ingredient");
        EchoClientWorkbenchScreenModel model = services.workbenchScreenModel(ids.recipeId());
        require(model != null && model.selectedRecipe().output().runtimeId().equals(ids.itemId()),
                "Active hot-imported recipe detail should expose the imported item output");
        EchoItemCraftResult crafted = services.craftWorkbenchRecipe(ids.recipeId());
        require(crafted != null && crafted.crafted(),
                "Active hot-imported tag recipe should craft through the existing session inventory runtime");
        require(services.inventoryScreenModel().slots().stream()
                        .anyMatch(slot -> slot.runtimeId().equals(ids.itemId())
                                && slot.label().equals("Active Hot Alloy")),
                "Active hot-imported crafted item should land in existing live inventory slots");

        EchoClientEntitySpawnSummary spawned = session.tickEntities(1.1D);
        require(spawned.reason().equals("spawned") && spawned.definitionId().equals(ids.entityId()),
                "Active hot-imported entity should reach the existing entity spawner");
        require(session.entityStore().living().stream()
                        .anyMatch(entity -> entity.definition().definitionId().equals(ids.entityId())
                                && entity.definition().displayName().equals("Active Watcher")),
                "Active hot-imported entity should be stored in the existing entity store");

        int beforeHealth = session.playerVitals().currentHealth();
        session.tickBiomeHazards(15.0D);
        require(session.hazardState().hazardId().equals(ids.hazardId()),
                "Active hot-imported hazard should reach the existing world hazard runtime");
        require(session.hazardState().lastDamage() > 0 && session.playerVitals().currentHealth() < beforeHealth,
                "Active hot-imported hazard should damage through the existing player runtime");
    }

    private static ActiveHotImportIds importActiveRuntimeContent(EchoClientRuntimeServices services) {
        ActiveHotImportIds ids = new ActiveHotImportIds(
                "echoruntimehost:active_hot_glass",
                "echoruntimehost:active_hot_alloy",
                "echoruntimehost:craft_active_hot_alloy",
                "echoruntimehost:active_watcher",
                "echoruntimehost:active_volatile_air",
                "echoruntimehost:standalone/native_catalog_import"
        );
        int imported = services.importAdapterCoreContentRegistrations(List.of(
                activeBlockRow(ids.blockId()),
                activeItemRow(ids.itemId()),
                activeRecipeRow(ids.recipeId(), ids.itemId()),
                activeEntityRow(ids.entityId()),
                activeHazardRow(ids.hazardId())
        ));
        require(imported == 5,
                "Client runtime services should hot-import five native runtime content rows");
        return ids;
    }

    private static Map<String, Object> activeBlockRow(String blockId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:block/active_hot_glass",
                "contentKind", "BLOCK",
                "domain", "blocks",
                "displayName", "Active Hot Glass",
                "adapterKey", "registry.blocks.active_hot_glass",
                "neoForgeId", blockId,
                "nativeLoaderId", "echoruntimehost:block/active_hot_glass",
                "standaloneRuntimeId", blockId,
                "metadata", Map.of(
                        "liveVoxelId", blockId,
                        "argb", "#8FE7C8",
                        "detailArgb", "0xFFC9FFF0",
                        "atlasKey", "echoruntimehost/block/active_hot_glass",
                        "materialPattern", "TERMINAL_GRID",
                        "solid", false,
                        "opaque", false,
                        "hardness", "0.4",
                        "behaviorHooks", List.of("open_registered_screen"),
                        "targetId", "echoruntimehost:standalone/native_catalog_import"
                )
        );
    }

    private static Map<String, Object> activeItemRow(String itemId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:item/active_hot_alloy",
                "contentKind", "ITEM",
                "domain", "items",
                "displayName", "Active Hot Alloy",
                "adapterKey", "registry.items.active_hot_alloy",
                "neoForgeId", itemId,
                "nativeLoaderId", "echoruntimehost:item/active_hot_alloy",
                "standaloneRuntimeId", itemId,
                "metadata", Map.of(
                        "category", "MATERIAL",
                        "maxStackSize", 8,
                        "weight", "0.45",
                        "tags", List.of("adaptercore", "native-content", "hot-import"),
                        "tooltipLines", List.of("Crafted after an active native content import")
                )
        );
    }

    private static Map<String, Object> activeRecipeRow(String recipeId, String itemId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:recipe/craft_active_hot_alloy",
                "contentKind", "RECIPE",
                "domain", "recipes",
                "displayName", "Active Hot Alloy",
                "adapterKey", "registry.recipes.craft_active_hot_alloy",
                "neoForgeId", recipeId,
                "nativeLoaderId", "echoruntimehost:recipe/craft_active_hot_alloy",
                "standaloneRuntimeId", recipeId,
                "metadata", Map.of(
                        "recipeId", recipeId,
                        "type", "minecraft:crafting_shapeless",
                        "ingredients", List.of("#gel"),
                        "ingredientCounts", Map.of("#gel", 1),
                        "result", itemId,
                        "resultCount", 1,
                        "pattern", List.of("G"),
                        "group", "runtime_native",
                        "category", "adaptercore",
                        "sourceLogicalId", "runtime/native/content/active_hot_alloy.json"
                )
        );
    }

    private static Map<String, Object> activeEntityRow(String entityId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:entity/active_watcher",
                "contentKind", "ENTITY",
                "domain", "entities",
                "displayName", "Active Watcher",
                "adapterKey", "registry.entities.active_watcher",
                "neoForgeId", entityId,
                "nativeLoaderId", "echoruntimehost:entity/active_watcher",
                "standaloneRuntimeId", entityId,
                "metadata", Map.of(
                        "definitionId", entityId,
                        "kind", "HOSTILE",
                        "maxHealth", 36,
                        "movementSpeed", 1,
                        "aiProfile", "hostile_scavenger",
                        "biomeTags", List.of("crash_zone"),
                        "renderArgb", "#F0B14A",
                        "renderShape", "DRONE"
                )
        );
    }

    private static Map<String, Object> activeHazardRow(String hazardId) {
        return Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:hazard/active_volatile_air",
                "contentKind", "WORLD_HAZARD",
                "domain", "hazards",
                "displayName", "Active Volatile Air",
                "adapterKey", "registry.hazards.active_volatile_air",
                "neoForgeId", hazardId,
                "nativeLoaderId", "echoruntimehost:hazard/active_volatile_air",
                "standaloneRuntimeId", hazardId,
                "metadata", Map.of(
                        "hazardId", hazardId,
                        "biomeTags", List.of("crash_zone"),
                        "exposurePerSecond", "14.0",
                        "damage", 2
                )
        );
    }

    private record ActiveHotImportIds(
            String blockId,
            String itemId,
            String recipeId,
            String entityId,
            String hazardId,
            String screenId
    ) {
    }

    private static void requireItemContainerInventoryModel() throws IOException {
        EchoInventoryContainer container = new EchoInventoryContainer(
                new EchoInventoryId("inventory:screen-model-smoke"),
                Optional.empty(),
                "Screen Model Pack",
                2
        );
        EchoItemDefinition water = new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:clean_water_bottle"),
                "Clean Water Bottle",
                EchoItemCategory.CONSUMABLE,
                4,
                0.5D,
                List.of("hydration"),
                List.of("Sealed emergency water.")
        );
        container.slot(0).setStack(new EchoItemStack(water, 2));

        EchoClientInventoryScreenModel model = EchoClientInventoryScreenModel.fromItemContainer(container);
        require(model.title().equals("Screen Model Pack"),
                "Item inventory model should keep the container label as its screen title");
        require(model.slots().size() == EchoClientInventoryScreenModel.SLOT_COUNT,
                "Item inventory model should pad into the ScreenCore slot grid size");
        require(model.slot(0).itemSlot(),
                "Item inventory model should expose item-runtime stacks as item slots");
        require(model.slot(0).runtimeId().equals("echoashfallprotocol:clean_water_bottle"),
                "Item inventory model should preserve the item runtime id");
        require(model.slot(0).count() == 2,
                "Item inventory model should preserve stack quantity");
        require(model.slot(1).empty() && model.slot(9).empty(),
                "Item inventory model should preserve empty and padded slots");
        requireLiveSessionInventoryUsesItemRuntimeModel();
    }

    private static void requireWorkbenchScreenUsesRecipeData(
            EchoClientRuntimeServices services,
            String nativeItemId,
            String nativeRecipeId
    ) {
        services.startNewWorld("workbench-screen-smoke");
        require(services.loadedWorkbenchRecipeCount() > 0,
                "Workbench service should load Minecraft-style data recipes from mounted packs");
        EchoClientGameSession session = services.session();
        require(session.quickMoveContainerSlotToPlayer(1).success(),
                "Workbench smoke should move scrap from container into player inventory");
        List<EchoClientWorkbenchRecipeSummary> summaries = services.workbenchRecipeSummaries();
        require(!summaries.isEmpty(), "Workbench should expose recipe summaries");
        EchoClientWorkbenchRecipeSummary craftable = summaries.stream()
                .filter(recipe -> recipe.recipeId().equals(nativeRecipeId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Workbench should expose the imported native item recipe"));
        require(craftable.label().equals("Native Runtime Gel"),
                "Workbench recipe summary should use the native item definition display name");
        require(craftable.craftable(),
                "Workbench should mark the imported native item recipe craftable after moving scrap into inventory");

        EchoClientScreenController screens = new EchoClientScreenController();
        screens.showPauseMenu();
        screens.updateWorkbenchRecipes(summaries, services.workbenchRecipeError());
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_WORKBENCH, true),
                "Workbench navigation command should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot workbench = screens.snapshot(true);
        require(workbench.kind() == EchoClientScreenKind.WORKBENCH,
                "Workbench should open as a ScreenCore screen route");
        require(workbench.footer().contains("echoscreencore:workbench"),
                "Workbench snapshot should expose the ScreenCore workbench route");
        require(workbench.options().stream()
                        .anyMatch(option -> option.targetId().equals(craftable.recipeId()) && option.enabled()),
                "Workbench ScreenCore options should include enabled craftable recipe rows");
        EchoClientWorkbenchScreenModel model = services.workbenchScreenModel(craftable.recipeId());
        require(model != null,
                "Workbench should expose a selected recipe detail model");
        require(model.screenId().equals("echoscreencore:workbench"),
                "Workbench detail model should keep the ScreenCore workbench route id");
        require(model.selectedRecipe().recipeId().equals(craftable.recipeId()),
                "Workbench detail model should select the requested recipe");
        require(!model.selectedRecipe().ingredients().isEmpty(),
                "Workbench detail model should expose ingredient preview slots");
        require(model.selectedRecipe().ingredients().stream().allMatch(EchoClientSlotStack::itemSlot),
                "Workbench detail ingredients should be item-runtime slots");
        require(model.selectedRecipe().output().itemSlot(),
                "Workbench detail model should expose an item-runtime output preview");
        require(model.selectedRecipe().output().runtimeId().equals(nativeItemId),
                "Workbench detail output should point at the imported native item definition");
        require(model.selectedRecipe().output().label().equals("Native Runtime Gel"),
                "Workbench detail output should preserve the imported native item display name");
        require(model.selectedRecipe().craftable(),
                "Workbench detail model should mirror live craftability");

        EchoItemCraftResult crafted = services.craftWorkbenchRecipe(craftable.recipeId());
        require(crafted != null && crafted.crafted(),
                "Workbench should craft the selected item-runtime recipe");
        require(services.inventoryScreenModel().slots().stream()
                        .anyMatch(slot -> slot.runtimeId().equals(nativeItemId)
                                && slot.label().equals("Native Runtime Gel")
                                && slot.count() >= 1),
                "Crafting the native item recipe should add the imported native item to live inventory slots");
        require(services.workbenchRecipeSummaries().stream().anyMatch(recipe -> recipe.recipeId().equals(craftable.recipeId())),
                "Workbench recipe list should remain available after crafting");
        EchoClientWorkbenchScreenModel afterCraft = services.workbenchScreenModel(craftable.recipeId());
        require(afterCraft != null && !afterCraft.selectedRecipe().status().isBlank(),
                "Workbench detail model should report live craft status after crafting");
    }

    private static void requireLiveSessionInventoryUsesItemRuntimeModel() throws IOException {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("screen-model-smoke").gameSession();
        EchoClientInventoryScreenModel model = session.inventoryScreenModel();
        require(session.inventoryScreenModel() == model,
                "Repeated live client inventory screen reads should reuse the cached item-runtime model");
        require(model.slot(0).itemSlot(),
                "Live client inventory screen model should be sourced from item-runtime slots");
        require(model.slot(0).runtimeId().equals(session.hotbar().slot(0).block().id()),
                "Live client inventory item id should mirror the gameplay hotbar block id");
        require(model.slot(0).count() == 12,
                "Live client inventory should preserve the starter hotbar stack count");
        EchoItemStack unchangedHotbarStack = session.playerInventory().slot(0).stack().orElseThrow();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        session.updateFromGameplay(gameplay);
        require(session.inventoryScreenModel() == model,
                "Unchanged gameplay sync should keep the cached live inventory screen model");
        require(session.playerInventory().slot(0).stack().orElseThrow() == unchangedHotbarStack,
                "Repeated gameplay sync should not recreate unchanged hotbar item stacks");

        EchoClientInventoryScreenModel beforeSplit = session.inventoryScreenModel();
        require(session.splitInventorySlotTo(0, 1).success(),
                "Live client inventory split should use item-runtime slot operations");
        EchoClientInventoryScreenModel afterSplit = session.inventoryScreenModel();
        require(afterSplit != beforeSplit && session.inventoryScreenModel() == afterSplit,
                "Live client inventory mutations should rebuild once then reuse the cached screen model");
        require(afterSplit.slot(0).count() == 6,
                "Live client inventory split should update the item screen model source slot");
        require(afterSplit.slot(1).count() == 6,
                "Live client inventory split should update the item screen model target slot");
        require(session.hotbar().slot(1).count() == 6,
                "Live client item inventory split should sync back to the voxel hotbar");

        require(session.moveOrMergeInventorySlot(1, 0).success(),
                "Live client inventory merge should use item-runtime slot operations");
        EchoClientInventoryScreenModel afterMerge = session.inventoryScreenModel();
        require(afterMerge.slot(0).count() == 12,
                "Live client inventory merge should update the item screen model");
        require(session.hotbar().slot(0).count() == 12 && session.hotbar().slot(1).empty(),
                "Live client item inventory merge should sync back to the voxel hotbar");
        require(session.quickMoveInventorySlot(0).success(),
                "Live client inventory quick move should use item-runtime range operations");
        EchoClientInventoryScreenModel afterQuickMove = session.inventoryScreenModel();
        require(afterQuickMove.slot(0).empty(),
                "Live client quick move should clear the hotbar source slot");
        require(afterQuickMove.slot(9).count() == 12,
                "Live client quick move should move hotbar stacks into the carry grid");
        require(session.hotbar().slot(0).empty() && session.hotbar().slot(9).count() == 12,
                "Live client quick move should sync carry slots back to the voxel hotbar");
        require(session.swapInventorySlots(9, 1).success(),
                "Live client inventory number-key swap should use item-runtime slot operations");
        EchoClientInventoryScreenModel afterSwap = session.inventoryScreenModel();
        require(afterSwap.slot(9).empty()
                        && afterSwap.slot(1).count() == 12,
                "Live client hotbar key swap should exchange carry and hotbar slots");
        require(session.hotbar().slot(1).count() == 12 && session.hotbar().slot(9).empty(),
                "Live client hotbar key swap should sync back to gameplay hotbar slots");
        requireLiveSessionContainerUsesItemRuntimeModel();
        requireClientInventorySaveRoundTrip();
    }

    private static void requireLiveSessionContainerUsesItemRuntimeModel() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("container-screen-smoke").gameSession();
        EchoClientInventoryScreenModel container = session.containerScreenModel();
        require(session.containerScreenModel() == container,
                "Repeated live client container screen reads should reuse the cached item-runtime model");
        require(container.title().equals("Crash Cache"),
                "Live client container screen model should expose the container label");
        require(container.slot(0).itemSlot()
                        && container.slot(0).runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && container.slot(0).count() == 2,
                "Live client container should expose item-runtime cache contents");
        require(session.splitContainerSlotTo(1, 2).success(),
                "Live client container split should use item-runtime slot operations");
        EchoClientInventoryScreenModel afterSplit = session.containerScreenModel();
        require(afterSplit != container && session.containerScreenModel() == afterSplit,
                "Live client container mutations should rebuild once then reuse the cached screen model");
        require(afterSplit.slot(1).count() == 3
                        && afterSplit.slot(2).count() == 2,
                "Live client container split should update the container model");
        require(session.moveOrMergeContainerSlot(2, 1).success(),
                "Live client container merge should use item-runtime slot operations");
        EchoClientInventoryScreenModel afterMerge = session.containerScreenModel();
        require(afterMerge.slot(1).count() == 5
                        && afterMerge.slot(2).empty(),
                "Live client container merge should update the container model");
        require(session.quickMoveContainerSlotToPlayer(0).success(),
                "Live client container quick move should transfer into the player inventory");
        EchoClientInventoryScreenModel afterQuickMove = session.containerScreenModel();
        require(afterQuickMove.slot(0).empty(),
                "Live client container quick move should clear the source cache slot");
        require(session.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && session.inventoryScreenModel().slot(1).count() == 2,
                "Live client container quick move should preserve item-only stacks in player inventory");
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        session.updateFromGameplay(gameplay);
        require(session.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && session.inventoryScreenModel().slot(1).count() == 2,
                "Live client gameplay sync should not erase non-block item stacks");
        require(session.swapContainerSlotWithHotbar(1, 2).success(),
                "Live client container hotbar swap should use cross-container item-runtime operations");
        require(session.containerScreenModel().slot(1).empty()
                        && session.inventoryScreenModel().slot(2).runtimeId().equals("echoashfallprotocol:scrap_metal"),
                "Live client container hotbar swap should move cache items into player inventory slots");
    }

    private static void requireClientInventorySaveRoundTrip() throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-client-inventory-save");
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_save_profile.v1",
                "screen-catalog-inventory",
                "Screen Catalog Inventory",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/screen-catalog-inventory"),
                Map.of("surface", "echoscreencore:inventory")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().newWorld("inventory-save-smoke");
        require(worldSession.gameSession().splitInventorySlotTo(0, 1).success(),
                "Inventory save smoke should create a split stack before saving");
        require(worldSession.gameSession().splitContainerSlotTo(1, 2).success(),
                "Inventory save smoke should create a split container stack before saving");

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-inventory-save", "inventory-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.INVENTORY_PATH).isPresent(),
                "Client save manifest should include the item-runtime inventory file");
        require(manifest.file(EchoClientGameplaySaveCodec.CONTAINER_PATH).isPresent(),
                "Client save manifest should include the item-runtime container file");
        require(manifest.metadata().getOrDefault("clientInventoryCodec", "").equals("echo.client.inventory.v1"),
                "Client save manifest should advertise the item inventory codec");
        require(manifest.metadata().getOrDefault("clientContainerCodec", "").equals("echo.client.container.v1"),
                "Client save manifest should advertise the item container codec");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                saves,
                manifest
        );
        EchoClientWorldSession restored = EchoClientWorldSession.fromSavedSession(
                manifest.slotId(),
                manifest.metadata().getOrDefault("displayName", manifest.slotId()),
                restoredSnapshot
        );
        require(restored.gameSession().inventoryScreenModel().slot(0).count() == 6,
                "Client inventory save restore should keep the source split stack count");
        require(restored.gameSession().inventoryScreenModel().slot(1).count() == 6,
                "Client inventory save restore should keep the target split stack count");
        require(restored.gameSession().hotbar().slot(1).count() == 6,
                "Client inventory save restore should sync restored inventory into the gameplay hotbar");
        require(restored.gameSession().containerScreenModel().slot(1).count() == 3,
                "Client container save restore should keep the source split stack count");
        require(restored.gameSession().containerScreenModel().slot(2).count() == 2,
                "Client container save restore should keep the target split stack count");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
