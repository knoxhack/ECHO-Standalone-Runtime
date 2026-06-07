package dev.echo.standalone.runtime.compat;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EchoAdapterCoreContractLock {
    private static final Set<EchoAdapterCoreDomain> REQUIRED_BETA_DOMAINS = EnumSet.of(
            EchoAdapterCoreDomain.BLOCKS,
            EchoAdapterCoreDomain.ITEMS,
            EchoAdapterCoreDomain.ENTITIES,
            EchoAdapterCoreDomain.RECIPES,
            EchoAdapterCoreDomain.LOOT,
            EchoAdapterCoreDomain.STRUCTURES,
            EchoAdapterCoreDomain.UI_SCREENS,
            EchoAdapterCoreDomain.SOUNDS,
            EchoAdapterCoreDomain.MISSIONS,
            EchoAdapterCoreDomain.SAVES,
            EchoAdapterCoreDomain.WORLDGEN,
            EchoAdapterCoreDomain.NETWORKING,
            EchoAdapterCoreDomain.COMMANDS
    );
    private static final Set<EchoAdapterCoreDomain> PHASE1_AUDIT_DOMAINS = EnumSet.of(
            EchoAdapterCoreDomain.BLOCKS,
            EchoAdapterCoreDomain.ITEMS,
            EchoAdapterCoreDomain.INVENTORY,
            EchoAdapterCoreDomain.ENTITIES,
            EchoAdapterCoreDomain.RECIPES,
            EchoAdapterCoreDomain.LOOT,
            EchoAdapterCoreDomain.STRUCTURES,
            EchoAdapterCoreDomain.UI_SCREENS,
            EchoAdapterCoreDomain.UI_OVERLAYS,
            EchoAdapterCoreDomain.SOUNDS,
            EchoAdapterCoreDomain.MISSIONS,
            EchoAdapterCoreDomain.SAVES,
            EchoAdapterCoreDomain.WORLDGEN,
            EchoAdapterCoreDomain.NETWORKING,
            EchoAdapterCoreDomain.COMMANDS,
            EchoAdapterCoreDomain.DIAGNOSTICS,
            EchoAdapterCoreDomain.DATA,
            EchoAdapterCoreDomain.INPUT,
            EchoAdapterCoreDomain.RENDERING,
            EchoAdapterCoreDomain.PLAYER,
            EchoAdapterCoreDomain.WEATHER,
            EchoAdapterCoreDomain.HAZARDS,
            EchoAdapterCoreDomain.MACHINES,
            EchoAdapterCoreDomain.POWER,
            EchoAdapterCoreDomain.ECONOMY,
            EchoAdapterCoreDomain.STORY
    );

    private EchoAdapterCoreContractLock() {
    }

    public static List<EchoAdapterCoreDomain> requiredBetaDomains() {
        return REQUIRED_BETA_DOMAINS.stream().sorted().toList();
    }

    public static List<EchoAdapterCoreDomain> phase1AuditDomains() {
        return PHASE1_AUDIT_DOMAINS.stream().sorted().toList();
    }

    public static boolean supportsEveryRuntime(List<EchoAdapterCoreRuntimeKind> runtimes) {
        Objects.requireNonNull(runtimes, "runtimes");
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            if (!runtimes.contains(runtimeKind)) {
                return false;
            }
        }
        return true;
    }

    public static List<EchoAdapterCoreRuntimeKind> missingRuntimes(List<EchoAdapterCoreRuntimeKind> runtimes) {
        Objects.requireNonNull(runtimes, "runtimes");
        ArrayList<EchoAdapterCoreRuntimeKind> missing = new ArrayList<>();
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            if (!runtimes.contains(runtimeKind)) {
                missing.add(runtimeKind);
            }
        }
        return List.copyOf(missing);
    }
}
