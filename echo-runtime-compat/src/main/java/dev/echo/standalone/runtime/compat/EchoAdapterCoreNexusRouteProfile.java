package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreNexusRouteProfile(
        String profileContentId,
        String nexusCapacitorLiveVoxelId,
        String nexusCoreLiveVoxelId,
        String signalScannerLiveVoxelId,
        String nexusScarAvatarEntityContentId,
        String buildNexusCapacitorMissionId,
        String findNexusCoreMissionId,
        String awakenNexusCoreMissionId,
        String scanPrimeRelaysMissionId,
        String resolvePrimeRelaysMissionId,
        String stabilizeNexusGridMissionId,
        String neutralizeNexusScarAvatarMissionId,
        String reachDecisionMissionId,
        int capacitorCharge,
        int nexusCoreSignal,
        int primeRelayCount,
        int nexusStability,
        int avatarThreat
) {
    public EchoAdapterCoreNexusRouteProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        nexusCapacitorLiveVoxelId = EchoCompatText.requireText(
                nexusCapacitorLiveVoxelId,
                "nexusCapacitorLiveVoxelId"
        );
        nexusCoreLiveVoxelId = EchoCompatText.requireText(nexusCoreLiveVoxelId, "nexusCoreLiveVoxelId");
        signalScannerLiveVoxelId = EchoCompatText.requireText(signalScannerLiveVoxelId, "signalScannerLiveVoxelId");
        nexusScarAvatarEntityContentId = EchoCompatText.requireText(
                nexusScarAvatarEntityContentId,
                "nexusScarAvatarEntityContentId"
        );
        buildNexusCapacitorMissionId = EchoCompatText.requireText(
                buildNexusCapacitorMissionId,
                "buildNexusCapacitorMissionId"
        );
        findNexusCoreMissionId = EchoCompatText.requireText(findNexusCoreMissionId, "findNexusCoreMissionId");
        awakenNexusCoreMissionId = EchoCompatText.requireText(awakenNexusCoreMissionId, "awakenNexusCoreMissionId");
        scanPrimeRelaysMissionId = EchoCompatText.requireText(scanPrimeRelaysMissionId, "scanPrimeRelaysMissionId");
        resolvePrimeRelaysMissionId = EchoCompatText.requireText(
                resolvePrimeRelaysMissionId,
                "resolvePrimeRelaysMissionId"
        );
        stabilizeNexusGridMissionId = EchoCompatText.requireText(
                stabilizeNexusGridMissionId,
                "stabilizeNexusGridMissionId"
        );
        neutralizeNexusScarAvatarMissionId = EchoCompatText.requireText(
                neutralizeNexusScarAvatarMissionId,
                "neutralizeNexusScarAvatarMissionId"
        );
        reachDecisionMissionId = EchoCompatText.requireText(reachDecisionMissionId, "reachDecisionMissionId");
        if (capacitorCharge <= 0
                || nexusCoreSignal <= 0
                || primeRelayCount <= 0
                || nexusStability <= 0
                || avatarThreat <= 0) {
            throw new IllegalArgumentException("nexus route profile values must be positive");
        }
    }

    public static EchoAdapterCoreNexusRouteProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.NEXUS_ROUTE_PROFILE_ID);
        EchoAdapterCoreRegistryEntry nexusCapacitor = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.NEXUS_CAPACITOR_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry nexusCore = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.NEXUS_CORE_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry signalScanner = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SIGNAL_SCANNER_BLOCK_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.NEXUS_SCAR_AVATAR_ENTITY_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_NEXUS_CAPACITOR_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FIND_NEXUS_CORE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.AWAKEN_NEXUS_CORE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.SCAN_PRIME_RELAYS_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.RESOLVE_PRIME_RELAYS_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.STABILIZE_NEXUS_GRID_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.NEUTRALIZE_NEXUS_SCAR_AVATAR_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.REACH_DECISION_MISSION_ID);
        return new EchoAdapterCoreNexusRouteProfile(
                EchoAdapterCoreStandaloneContentBridge.NEXUS_ROUTE_PROFILE_ID,
                nexusCapacitor.liveVoxelId(),
                nexusCore.liveVoxelId(),
                signalScanner.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.NEXUS_SCAR_AVATAR_ENTITY_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_NEXUS_CAPACITOR_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.FIND_NEXUS_CORE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.AWAKEN_NEXUS_CORE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.SCAN_PRIME_RELAYS_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.RESOLVE_PRIME_RELAYS_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.STABILIZE_NEXUS_GRID_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.NEUTRALIZE_NEXUS_SCAR_AVATAR_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.REACH_DECISION_MISSION_ID,
                400,
                100,
                3,
                100,
                275
        );
    }

    private static EchoAdapterCoreRegistryEntry requireLiveVoxel(
            EchoAdapterCoreStandaloneRegistry registry,
            String liveVoxelId
    ) {
        EchoAdapterCoreRegistryEntry entry = registry.findLiveVoxelId(liveVoxelId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No AdapterCore live voxel entry for " + liveVoxelId));
        if (entry.liveVoxelId().isBlank()) {
            throw new IllegalStateException(liveVoxelId + " must expose a live voxel id");
        }
        return entry;
    }
}
