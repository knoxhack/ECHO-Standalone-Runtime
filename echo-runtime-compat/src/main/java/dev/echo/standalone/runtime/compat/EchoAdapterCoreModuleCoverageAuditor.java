package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleIssue;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class EchoAdapterCoreModuleCoverageAuditor {
    private static final Set<String> ADAPTER_CORE_BOOTSTRAP_MODULES = Set.of(
            "echocore",
            "echonetcore",
            "echoplatformcore",
            "echoadaptercore"
    );

    public EchoAdapterCoreModuleCoverageReport audit(
            EchoRuntimeModuleRuntimeResult modules,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        Objects.requireNonNull(modules, "modules");
        Objects.requireNonNull(bridge, "bridge");

        Set<String> liveBindingModules = new TreeSet<>();
        for (EchoAdapterCoreContentBinding binding : bridge.bindings()) {
            liveBindingModules.add(binding.moduleId());
        }

        ArrayList<EchoAdapterCoreModuleCoverageEntry> entries = new ArrayList<>();
        for (EchoRuntimeModuleDescriptor descriptor : modules.registry().descriptors()) {
            boolean graphFailed = modules.moduleGraph().failedModuleIds().contains(descriptor.id());
            boolean adapterProvider = ADAPTER_CORE_BOOTSTRAP_MODULES.contains(descriptor.id());
            boolean adapterDeclared = declaresAdapterCore(descriptor);
            boolean nativeEntrypoint = descriptor.access().containsKey("nativeEntrypoint");
            boolean liveBinding = liveBindingModules.contains(descriptor.id());
            List<EchoAdapterCoreDomain> declaredDomains = declaredDomains(descriptor);
            List<EchoAdapterCoreDomain> inferredDomains = inferredDomains(descriptor);
            List<EchoAdapterCoreDomain> adapterDomains = mergedDomains(declaredDomains, inferredDomains);
            List<EchoAdapterCoreRuntimeKind> declaredRuntimes = declaredRuntimes(descriptor);
            List<String> unknownDomains = unknownDeclaredDomains(descriptor);
            List<String> unknownRuntimes = unknownDeclaredRuntimes(descriptor);
            ArrayList<String> adapterKeys = new ArrayList<>();
            if (adapterProvider) {
                adapterKeys.add("adaptercore.bootstrap");
            }
            if (adapterDeclared) {
                adapterKeys.add("echoadaptercore.declared");
            }
            if (nativeEntrypoint) {
                adapterKeys.add("nativeEntrypoint");
            }
            if (liveBinding) {
                adapterKeys.add("liveVoxelBinding");
            }
            if (!declaredDomains.isEmpty()) {
                adapterKeys.add("adapterDomains");
            }
            if (!declaredRuntimes.isEmpty()) {
                adapterKeys.add("adapterRuntimes");
            }

            ArrayList<String> gaps = new ArrayList<>();
            EchoAdapterCoreModuleCoverageStatus status;
            boolean validAdapterMetadata = unknownDomains.isEmpty() && unknownRuntimes.isEmpty();
            boolean allRuntimeTargetsDeclared = EchoAdapterCoreContractLock.supportsEveryRuntime(declaredRuntimes);
            boolean moduleContractReady = adapterDeclared
                    && validAdapterMetadata
                    && !declaredDomains.isEmpty()
                    && allRuntimeTargetsDeclared;
            boolean providerContractReady = adapterProvider
                    && validAdapterMetadata
                    && allRuntimeTargetsDeclared
                    && (descriptor.id().equals("echoadaptercore") || !declaredDomains.isEmpty());
            if (graphFailed) {
                gaps.add("module graph failed dependency or trust validation");
                status = EchoAdapterCoreModuleCoverageStatus.UNSUPPORTED;
            } else if (!descriptor.standalone()) {
                gaps.add("metadata standalone=false");
                status = EchoAdapterCoreModuleCoverageStatus.UNSUPPORTED;
            } else if (providerContractReady || moduleContractReady) {
                status = EchoAdapterCoreModuleCoverageStatus.ACTIVE;
            } else {
                if (!nativeEntrypoint) {
                    gaps.add("missing nativeEntrypoint metadata");
                }
                if (!unknownDomains.isEmpty()) {
                    gaps.add("unknown AdapterCore domain declarations: " + unknownDomains);
                }
                if (!unknownRuntimes.isEmpty()) {
                    gaps.add("unknown AdapterCore runtime support declarations: " + unknownRuntimes);
                }
                if (!adapterDeclared) {
                    gaps.add("missing echoadaptercore bridge declaration");
                }
                if (declaredDomains.isEmpty()) {
                    gaps.add("missing AdapterCore domain declarations"
                            + (inferredDomains.isEmpty() ? "" : ": " + domainSummary(inferredDomains)));
                }
                if (!allRuntimeTargetsDeclared) {
                    gaps.add("missing AdapterCore runtime support declarations: "
                            + missingRuntimeSummary(declaredRuntimes));
                }
                if (liveBinding) {
                    gaps.add("live AdapterCore content binding cannot replace complete module metadata");
                }
                status = EchoAdapterCoreModuleCoverageStatus.ADAPTER_GAP;
            }

            entries.add(new EchoAdapterCoreModuleCoverageEntry(
                    descriptor.id(),
                    descriptor.name(),
                    descriptor.version(),
                    status,
                    descriptor.standalone(),
                    adapterDeclared,
                    adapterProvider,
                    nativeEntrypoint,
                    liveBinding,
                    adapterDomains,
                    declaredRuntimes,
                    adapterKeys,
                    gaps,
                    descriptor.descriptorPath()
            ));
        }

        return new EchoAdapterCoreModuleCoverageReport(
                entries,
                modules.moduleGraph().issues().stream()
                        .map(EchoAdapterCoreModuleCoverageAuditor::issueSummary)
                        .toList()
        );
    }

    private static boolean declaresAdapterCore(EchoRuntimeModuleDescriptor descriptor) {
        return descriptor.id().equals("echoadaptercore")
                || descriptor.access().containsKey("adapterCore")
                || descriptor.requires().contains("echoadaptercore")
                || descriptor.optional().contains("echoadaptercore")
                || hasAdapterCoreFeature(descriptor.provides())
                || hasAdapterCoreFeature(descriptor.consumes());
    }

    private static boolean hasAdapterCoreFeature(List<String> values) {
        return values.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.contains("adaptercore")
                        || value.equals("adapter.neoforge")
                        || value.equals("adapter.echo_native")
                        || value.equals("adapter.echo_runtime_standalone"));
    }

    private static List<EchoAdapterCoreDomain> declaredDomains(EchoRuntimeModuleDescriptor descriptor) {
        Object adapterCore = descriptor.access().get("adapterCore");
        if (!(adapterCore instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object domains = map.get("domains");
        if (!(domains instanceof List<?> list)) {
            return List.of();
        }
        LinkedHashSet<EchoAdapterCoreDomain> result = new LinkedHashSet<>();
        for (Object item : list) {
            if (item instanceof String text) {
                EchoAdapterCoreDomain.fromId(text).ifPresent(result::add);
            }
        }
        return result.stream().sorted().toList();
    }

    private static List<String> unknownDeclaredDomains(EchoRuntimeModuleDescriptor descriptor) {
        Object adapterCore = descriptor.access().get("adapterCore");
        if (!(adapterCore instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object domains = map.get("domains");
        if (!(domains instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(text -> EchoAdapterCoreDomain.fromId(text).isEmpty())
                .sorted()
                .toList();
    }

    private static List<EchoAdapterCoreRuntimeKind> declaredRuntimes(EchoRuntimeModuleDescriptor descriptor) {
        Object adapterCore = descriptor.access().get("adapterCore");
        if (!(adapterCore instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object runtimes = map.get("runtimes");
        if (!(runtimes instanceof List<?> list)) {
            return List.of();
        }
        LinkedHashSet<EchoAdapterCoreRuntimeKind> result = new LinkedHashSet<>();
        for (Object item : list) {
            if (item instanceof String text) {
                EchoAdapterCoreRuntimeKind.fromId(text).ifPresent(result::add);
            }
        }
        return result.stream().sorted().toList();
    }

    private static List<String> unknownDeclaredRuntimes(EchoRuntimeModuleDescriptor descriptor) {
        Object adapterCore = descriptor.access().get("adapterCore");
        if (!(adapterCore instanceof Map<?, ?> map)) {
            return List.of();
        }
        Object runtimes = map.get("runtimes");
        if (!(runtimes instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .filter(text -> EchoAdapterCoreRuntimeKind.fromId(text).isEmpty())
                .sorted()
                .toList();
    }

    private static List<EchoAdapterCoreDomain> inferredDomains(EchoRuntimeModuleDescriptor descriptor) {
        LinkedHashSet<EchoAdapterCoreDomain> domains = new LinkedHashSet<>();
        ArrayList<String> values = new ArrayList<>();
        values.add(descriptor.id());
        values.add(descriptor.kind());
        values.addAll(descriptor.provides());
        values.addAll(descriptor.consumes());
        values.addAll(descriptor.permissions());
        values.addAll(descriptor.gameModes());
        for (String value : values) {
            String normalized = value.toLowerCase(Locale.ROOT);
            addIfContains(domains, normalized, EchoAdapterCoreDomain.BLOCKS, "block", "multiblock");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.ITEMS, "item", "inventory", "content");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.INVENTORY, "inventory", "hotbar", "container");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.ENTITIES, "entity", "npc", "creature", "villager");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.RECIPES, "recipe", "crafting");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.LOOT, "loot");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.STRUCTURES, "structure", "ruin", "poi");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.UI_SCREENS,
                    "ui.", "ui_", "screen", "terminal", "hud", "holomap", "wiki", "guide", "notification");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.UI_OVERLAYS, "ui.overlays", "ui_overlays", "overlay", "hud");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.SOUNDS, "sound", "audio", "music");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.MISSIONS, "mission", "quest", "objective", "tutorial");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.SAVES, "save", "recovery", "profile", "player.data");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.WORLDGEN,
                    "world", "weather", "biome", "hazard", "region", "spawn", "teleport");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.NETWORKING, "network", "net", "payload");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.COMMANDS, "command");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.DIAGNOSTICS,
                    "diagnostic", "report", "health", "support_bundle", "readiness");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.WEATHER, "weather", "atmosphere", "storm");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.HAZARDS, "hazard", "radiation", "exposure");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.MACHINES,
                    "machine", "multiblock", "workshop", "industrial");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.POWER, "power", "energy", "grid", "battery");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.ECONOMY,
                    "economy", "currency", "market", "trade", "vendor");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.STORY,
                    "story", "lore", "narrative", "chapter", "codex");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.MAPS, "map", "holomap", "waypoint");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.PACKS, "pack.", "pack_", "pack.read", "pack.profile");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.THEMES, "theme", "palette", "skin");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.WIKI, "wiki", "codex", "guide_book");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.ASSETS, "asset", "texture", "resource");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.DATA, "data", "schema", "validation", "metadata");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.RENDERING,
                    "render", "camera", "cinematic", "lens", "particle");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.INPUT, "input", "keybind", "controller");
            addIfContains(domains, normalized, EchoAdapterCoreDomain.PLAYER, "player", "home", "warp", "tpa");
        }
        return domains.stream().sorted().toList();
    }

    private static List<EchoAdapterCoreDomain> mergedDomains(
            List<EchoAdapterCoreDomain> declaredDomains,
            List<EchoAdapterCoreDomain> inferredDomains
    ) {
        LinkedHashSet<EchoAdapterCoreDomain> domains = new LinkedHashSet<>(declaredDomains);
        domains.addAll(inferredDomains);
        return domains.stream().sorted().toList();
    }

    private static void addIfContains(
            Set<EchoAdapterCoreDomain> domains,
            String value,
            EchoAdapterCoreDomain domain,
            String... tokens
    ) {
        for (String token : tokens) {
            if (value.contains(token)) {
                domains.add(domain);
                return;
            }
        }
    }

    private static String domainSummary(List<EchoAdapterCoreDomain> domains) {
        return domains.stream()
                .map(EchoAdapterCoreDomain::id)
                .toList()
                .toString();
    }

    private static String missingRuntimeSummary(List<EchoAdapterCoreRuntimeKind> declaredRuntimes) {
        return EchoAdapterCoreContractLock.missingRuntimes(declaredRuntimes).stream()
                .map(EchoAdapterCoreRuntimeKind::adapterId)
                .toList()
                .toString();
    }

    private static String issueSummary(EchoRuntimeModuleIssue issue) {
        String moduleId = issue.moduleId() == null ? "workspace" : issue.moduleId();
        return issue.severity().name() + ":" + moduleId + ":" + issue.code() + ":" + issue.summary();
    }
}
