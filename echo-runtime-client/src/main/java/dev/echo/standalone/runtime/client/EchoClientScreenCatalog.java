package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.ui.EchoAgent5ScreenCoreContract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class EchoClientScreenCatalog {
    private final List<EchoClientScreenCatalogEntry> entries;
    private final List<EchoClientScreenCatalogEntry> adapterCoreScreens;
    private final Map<EchoAdapterCoreDomain, Integer> domainCounts;
    private final EchoAgent5ScreenCoreContract screenCoreContract;
    private final String bindingCoverageSummary;
    private final boolean allRuntimeAliasesReady;

    private EchoClientScreenCatalog(
            List<EchoClientScreenCatalogEntry> entries,
            List<EchoClientScreenCatalogEntry> adapterCoreScreens,
            Map<EchoAdapterCoreDomain, Integer> domainCounts,
            EchoAgent5ScreenCoreContract screenCoreContract,
            String bindingCoverageSummary,
            boolean allRuntimeAliasesReady
    ) {
        this.entries = List.copyOf(entries);
        this.adapterCoreScreens = List.copyOf(adapterCoreScreens);
        this.domainCounts = Collections.unmodifiableMap(new LinkedHashMap<>(domainCounts));
        this.screenCoreContract = screenCoreContract;
        this.bindingCoverageSummary = bindingCoverageSummary == null ? "" : bindingCoverageSummary;
        this.allRuntimeAliasesReady = allRuntimeAliasesReady;
    }

    static EchoClientScreenCatalog empty() {
        EchoAgent5ScreenCoreContract contract = EchoAgent5ScreenCoreContract.runtime();
        List<EchoClientScreenCatalogEntry> builtIns = builtInScreenCoreEntries();
        return new EchoClientScreenCatalog(
                builtIns,
                List.of(),
                Map.of(),
                contract,
                "0/0 bindings ready",
                false
        );
    }

    static EchoClientScreenCatalog loadDefault() {
        return loadDefault(EchoClientWorldTemplates.defaultTemplate());
    }

    static EchoClientScreenCatalog loadDefault(EchoClientWorldTemplate template) {
        EchoClientWorldTemplate safeTemplate = template == null ? EchoClientWorldTemplates.defaultTemplate() : template;
        return loadDefault(safeTemplate.contentBridge(), safeTemplate.presentation());
    }

    static EchoClientScreenCatalog loadDefault(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoClientWorldPresentation presentation
    ) {
        EchoAdapterCoreStandaloneContentBridge safeBridge =
                bridge == null ? EchoClientWorldTemplates.defaultTemplate().contentBridge() : bridge;
        EchoClientWorldPresentation safePresentation =
                presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        EchoAgent5ScreenCoreContract contract = EchoAgent5ScreenCoreContract.runtime();
        ArrayList<EchoClientScreenCatalogEntry> entries = new ArrayList<>(builtInScreenCoreEntries());
        ArrayList<EchoClientScreenCatalogEntry> adapterScreens = new ArrayList<>();
        for (EchoAdapterCoreDomain uiDomain : adapterCoreUiDomains()) {
            for (EchoAdapterCoreRegistryEntry entry : safeBridge.registry().entriesForDomain(uiDomain)) {
                EchoClientScreenCatalogEntry catalogEntry =
                        EchoClientScreenCatalogEntry.adapterCore(entry, safePresentation);
                entries.add(catalogEntry);
                adapterScreens.add(catalogEntry);
            }
        }
        LinkedHashMap<EchoAdapterCoreDomain, Integer> domainCounts = new LinkedHashMap<>();
        for (EchoAdapterCoreDomain domain : EchoAdapterCoreDomain.values()) {
            int count = safeBridge.registry().count(domain);
            if (count > 0) {
                domainCounts.put(domain, count);
            }
        }
        return new EchoClientScreenCatalog(
                entries,
                adapterScreens,
                domainCounts,
                contract,
                safeBridge.bindingCoverageSummary(),
                safeBridge.supportsAllAdapterCoreRuntimes()
        );
    }

    List<EchoClientScreenCatalogEntry> entries() {
        return entries;
    }

    List<EchoClientScreenCatalogEntry> adapterCoreScreens() {
        return adapterCoreScreens;
    }

    Optional<EchoClientScreenCatalogEntry> findScreen(String screenId) {
        if (screenId == null || screenId.isBlank()) {
            return Optional.empty();
        }
        String requested = screenId.trim();
        return entries.stream()
                .filter(entry -> entry.screenId().equals(requested)
                        || entry.contentId().equals(requested)
                        || entry.standaloneRuntimeId().equals(requested))
                .findFirst();
    }

    List<String> topDomainSummaries(int limit) {
        ArrayList<String> summaries = new ArrayList<>();
        int added = 0;
        for (Map.Entry<EchoAdapterCoreDomain, Integer> entry : domainCounts.entrySet()) {
            if (added >= limit) {
                break;
            }
            summaries.add(domainLabel(entry.getKey()) + ": " + entry.getValue());
            added++;
        }
        return List.copyOf(summaries);
    }

    int adapterCoreScreenCount() {
        return adapterCoreScreens.size();
    }

    int screenCount() {
        return entries.size();
    }

    int domainCount(EchoAdapterCoreDomain domain) {
        if (domain == null) {
            return 0;
        }
        return domainCounts.getOrDefault(domain, 0);
    }

    String modSummary() {
        return adapterCoreScreenCount() + " AdapterCore UI screen(s), " + bindingCoverageSummary;
    }

    String screenCoreSummary() {
        String status = screenCoreContract.satisfied() ? "ready" : "incomplete";
        return screenCoreContract.primitives().size() + " ScreenCore primitive(s) " + status;
    }

    String diagnosticsSummary() {
        String aliasState = allRuntimeAliasesReady ? "all runtime aliases ready" : "runtime alias gaps";
        return screenCount() + " screen route(s), " + aliasState;
    }

    private static List<EchoClientScreenCatalogEntry> builtInScreenCoreEntries() {
        return List.of(
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:main_menu",
                        "Main Menu",
                        "screencore.main_menu",
                        "main_menu.primary",
                        "Title navigation and game launch"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:pause_flow",
                        "Pause Menu",
                        "screencore.pause_flow",
                        "pause.resume",
                        "In-world pause, save, options, and exit"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:death_screen",
                        "Death Screen",
                        "screencore.death_screen",
                        "death.respawn",
                        "Respawn and return to title flow"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:world_select",
                        "World Select",
                        "screencore.world_select",
                        "world_select.primary",
                        "Save slot browser"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:create_world",
                        "Create World",
                        "screencore.create_world",
                        "create_world.primary",
                        "Seed and pack profile setup"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:settings",
                        "Options",
                        "screencore.settings",
                        "settings.controls",
                        "Client settings root"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:controls",
                        "Controls",
                        "screencore.settings.controls",
                        "settings.controls.back",
                        "Input settings"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:video_settings",
                        "Video Settings",
                        "screencore.settings.video",
                        "settings.video.back",
                        "Renderer settings"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:audio_settings",
                        "Audio Settings",
                        "screencore.settings.audio",
                        "settings.audio.back",
                        "Sound mix settings"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:mods",
                        "Mods",
                        "screencore.mods",
                        "mods.back",
                        "AdapterCore and native loader catalog"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:resource_packs",
                        "Resource Packs",
                        "screencore.resource_packs",
                        "resource_packs.back",
                        "Minecraft asset pack browser"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:inventory",
                        "Inventory",
                        "screencore.inventory",
                        "inventory.slots",
                        "Player inventory slots"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:container",
                        "Container",
                        "screencore.container",
                        "container.slots",
                        "Container slot bridge"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:workbench",
                        "Workbench",
                        "screencore.workbench",
                        "workbench.recipes",
                        "Recipe data crafting bridge"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:machine",
                        "Machine",
                        "screencore.machine",
                        "machine.status",
                        "AdapterCore machine, power, IO, and recipe surface"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:terminal",
                        "Terminal",
                        "screencore.terminal",
                        "terminal.primary",
                        "Field terminal, command, mission, and registered UI surface"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:entity_interaction",
                        "Entity Interaction",
                        "screencore.entity_interaction",
                        "entity_interaction.primary",
                        "Live NPC and familiar interaction surface"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:diagnostics",
                        "Diagnostics",
                        "screencore.diagnostics",
                        "diagnostics.back",
                        "Runtime and ScreenCore telemetry"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:loading",
                        "Loading",
                        "screencore.loading",
                        "loading.progress",
                        "World loading progress"
                ),
                EchoClientScreenCatalogEntry.screenCore(
                        "echoscreencore:saving",
                        "Saving",
                        "screencore.saving",
                        "saving.status",
                        "Session save status"
                )
        );
    }

    private static List<EchoAdapterCoreDomain> adapterCoreUiDomains() {
        return List.of(
                EchoAdapterCoreDomain.UI_SCREENS,
                EchoAdapterCoreDomain.UI_OVERLAYS,
                EchoAdapterCoreDomain.TERMINAL,
                EchoAdapterCoreDomain.LENS,
                EchoAdapterCoreDomain.INDEX,
                EchoAdapterCoreDomain.HOLOMAP,
                EchoAdapterCoreDomain.WIKI
        );
    }

    private static String domainLabel(EchoAdapterCoreDomain domain) {
        String id = domain.id().replace('_', ' ');
        if (id.isBlank()) {
            return domain.name();
        }
        return Character.toUpperCase(id.charAt(0)) + id.substring(1);
    }
}
