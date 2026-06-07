package dev.echo.standalone.runtime.world;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoStandaloneWorldEffectsRuntime {
    private final Map<String, Map<String, Object>> persistedStatusStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStructurePoiLookupResult> resolvedPoiMarkers = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStructurePoiMarkerStateResult> lastPoiMarkerStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStructurePoiMarkerStateResult> resolvedPoiMarkerStateResults =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStructurePoiState> resolvedPoiStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStructureDiscoveryStateResult> lastStructureDiscoveryStates =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStructureDiscoveryStateResult> resolvedStructureDiscoveryStates =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneSpawnRuleEventResult> lastSpawnRuleEvents = new LinkedHashMap<>();
    private final Map<String, Integer> activeSpawnPopulations = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneSpawnZoneStateResult> lastSpawnZoneStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneSpawnZoneStateResult> activeSpawnZoneStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneSpawnZoneState> activeSpawnZoneStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherScheduleResult> activeWeatherSchedules = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherScheduleTickResult> lastWeatherScheduleTicks =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherStateApplyResult> lastWeatherStateApplications = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherStateApplyResult> lastWeatherSurfaceStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneAtmosphereStateApplyResult> lastAtmosphereStateApplications =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneAtmosphereStateApplyResult> lastAtmosphereSurfaceStates =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneAtmosphereRuntimeProfileResult> lastAtmosphereRuntimeProfiles =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneAtmosphereRuntimeProfileResult> weatherAtmosphereRuntimeProfiles =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherExposureMitigationResult> lastWeatherExposureMitigations =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherRouteRiskResult> lastWeatherRouteRiskResults =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneRouteWarningPostUseResult> lastRouteWarningPostUses =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneRouteWarningPostUseResult> routeWarningPostUses =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherForecastResult> lastWeatherForecasts = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherWarningResult> lastWeatherWarnings = new LinkedHashMap<>();
    private final Map<String, Map<String, EchoStandaloneWeatherWarningResult>> playerWeatherWarnings =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult> lastWeatherRadioUses =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult> weatherRadioPlayers =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherStationUseResult> lastWeatherStationUses = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWeatherStationUseResult> weatherStationPositions = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneEmergencySirenUseResult> lastEmergencySirenUses = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneEmergencySirenUseResult> emergencySirenPosts = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneClimateSensorReadResult> lastClimateSensorReadings =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneClimateSensorReadResult> climateSensorPositions =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneShelterReport> shelterReports = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneRegionTransitionResult> lastRegionTransitions = new LinkedHashMap<>();
    private final Map<String, String> activeRegionIds = new LinkedHashMap<>();
    private final Map<String, List<String>> startedRegionMissions = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneHazardTransitionResult> lastHazardTransitions = new LinkedHashMap<>();
    private final Map<String, String> activeHazardIds = new LinkedHashMap<>();
    private final Map<String, Double> playerHealth = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneHazardTickDamageResult> lastHazardTickDamage = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStatusEffectApplyResult> lastStatusEffectApplications = new LinkedHashMap<>();
    private final Map<String, Map<String, EchoStandaloneStatusEffectStackingResult>> lastStatusEffectStackings =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStatusEffectSaveResult> lastStatusEffectSaves = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStatusEffectLoadResult> lastStatusEffectLoads = new LinkedHashMap<>();
    private final Map<String, Map<String, EchoStandaloneStatusEffectExpiryResult>> lastStatusEffectExpiries =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStatusExposureMitigationResult> lastStatusExposureMitigations =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneStatusExposureMitigationResult> activeStatusExposureStates =
            new LinkedHashMap<>();
    private final Map<String, List<String>> activeStatusEffects = new LinkedHashMap<>();
    private final Map<String, Map<String, EchoStandaloneActiveStatusEffectState>> activeStatusEffectStates = new LinkedHashMap<>();
    private final Map<String, Map<String, EchoStandaloneStatusProfileState>> activeStatusProfileStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldCellSampleResult> lastWorldCellSamples = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldCellSampleResult> sampledWorldCells = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldCellSampleResult> sampledHazardFields = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneBiomeHazardOverlayResult> lastBiomeHazardOverlays = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneBiomeHazardOverlayResult> sampledBiomeHazardOverlays = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneBiomeAmbientStateResult> lastBiomeAmbientStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneBiomeAmbientStateResult> biomeAmbientStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldChunkStateResult> lastWorldChunkStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldChunkStateResult> sampledWorldChunkStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneWorldChunkState> sampledWorldChunks = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneHazardFieldStateResult> lastHazardFieldStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneHazardFieldStateResult> sampledHazardFieldStateResults = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneHazardFieldState> sampledHazardFieldStates = new LinkedHashMap<>();
    private EchoStandaloneWorldDataCatalogResult lastWorldDataCatalogResult;
    private final Map<String, EchoStandaloneDifficulty> activeDifficultyProfiles = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneDifficulty> regionDifficultyProfiles = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneDifficultyProfileSelectionResult> regionDifficultyProfileSelections =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneDifficultyApplicationState> activeDifficultyApplicationStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneDifficultyApplicationState> regionDifficultyApplicationStates = new LinkedHashMap<>();
    private final Map<String, EchoStandaloneDifficultyApplicationResult> activeDifficultyApplicationResults =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneDifficultyApplicationResult> regionDifficultyApplicationResults =
            new LinkedHashMap<>();
    private final Map<String, EchoStandaloneAgent7LiveHookEvidence> agent7LiveHookEvidence = new LinkedHashMap<>();

    public EchoStandaloneHazardTickDamageResult applyHazardTickDamage(EchoStandaloneHazardTickDamageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("hazard tick damage request must not be null");
        }
        double baseDamage = Math.max(0.0D, request.hazard().damagePerTick());
        double damage = baseDamage * request.difficulty().hazardMultiplier();
        double healthAfter = Math.max(0.0D, request.healthBefore() - damage);
        EchoStandaloneHazardTickDamageResult result = new EchoStandaloneHazardTickDamageResult(
                request.playerId(),
                request.hazard().id(),
                request.hazard().statusEffectId(),
                request.difficulty().id(),
                request.healthBefore(),
                healthAfter,
                baseDamage,
                damage,
                request.difficulty().hazardMultiplier(),
                request.severity(),
                request.gameTick(),
                request.sourceReason(),
                damage > 0.0D && healthAfter < request.healthBefore()
        );
        playerHealth.put(request.playerId(), healthAfter);
        lastHazardTickDamage.put(request.playerId(), result);
        activeDifficultyProfiles.put(request.playerId(), request.difficulty());
        recordDifficultyApplication(
                request.playerId(),
                "",
                request.difficulty(),
                result.hazardId(),
                result.baseDamage(),
                result.damageApplied(),
                "",
                0,
                0,
                0,
                result.gameTick(),
                "EchoStandaloneWorldEffectsRuntime.applyHazardTickDamage");
        return result;
    }

    public EchoStandaloneAgent7LiveHookEvidence recordAgent7LiveHook(
            String moduleId,
            String event,
            long gameTick,
            String sourceReason
    ) {
        String safeModuleId = requireText(moduleId, "agent7 live hook module id");
        String safeEvent = requireText(event, "agent7 live hook event");
        String key = safeModuleId + ":" + safeEvent;
        EchoStandaloneAgent7LiveHookEvidence evidence = new EchoStandaloneAgent7LiveHookEvidence(
                safeModuleId,
                safeEvent,
                key,
                Math.max(0L, gameTick),
                sourceReason == null ? "" : sourceReason,
                true,
                "standalone_world_simulation_exact_hook"
        );
        agent7LiveHookEvidence.put(key, evidence);
        return evidence;
    }

    public Map<String, EchoStandaloneAgent7LiveHookEvidence> agent7LiveHookEvidence() {
        return Map.copyOf(agent7LiveHookEvidence);
    }

    public EchoStandaloneAgent7LiveHookEvidence agent7LiveHookEvidence(String moduleId, String event) {
        return agent7LiveHookEvidence.get(requireText(moduleId, "agent7 live hook module id")
                + ":"
                + requireText(event, "agent7 live hook event"));
    }

    public double playerHealth(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return playerHealth.getOrDefault(playerId, 0.0D);
    }

    public EchoStandaloneHazardTickDamageResult lastHazardTickDamage(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastHazardTickDamage.get(playerId);
    }

    public EchoStandaloneStatusEffectSaveResult persistStatusEffect(EchoStandaloneStatusEffectSaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("status effect save request must not be null");
        }
        Map<String, Object> statusPayload = new LinkedHashMap<>();
        statusPayload.put("effectId", request.statusEffect().id());
        statusPayload.put("durationTicks", request.statusEffect().durationTicks());
        statusPayload.put("amplifier", request.statusEffect().amplifier());
        statusPayload.put("hazardId", request.hazardId());
        statusPayload.put("damageApplied", request.damageApplied());
        statusPayload.put("gameTick", request.gameTick());

        Map<String, Object> savedStatus = new LinkedHashMap<>();
        savedStatus.put(request.statusEffect().saveKey(), Map.copyOf(statusPayload));
        savedStatus.put("adapterCoreModule", "echoworldcore");
        persistedStatusStates.put(statusStoreKey(request.playerId(), request.hazardId()), Map.copyOf(savedStatus));

        EchoStandaloneStatusEffectSaveResult result = new EchoStandaloneStatusEffectSaveResult(
                request.playerId(),
                request.hazardId(),
                request.statusEffect().id(),
                request.statusEffect().durationTicks(),
                request.statusEffect().amplifier(),
                request.statusEffect().saveKey(),
                request.damageApplied(),
                request.gameTick(),
                savedStatus,
                request.sourceReason(),
                true
        );
        lastStatusEffectSaves.put(request.playerId(), result);
        EchoStandaloneStatusEffectApplyResult application = applyStatusEffect(
                new EchoStandaloneStatusEffectApplyRequest(
                        result.playerId(),
                        result.hazardId(),
                        result.damageApplied(),
                        result.gameTick(),
                        "standalone-status-save-apply",
                        new EchoStandaloneStatusEffect(
                                result.effectId(),
                                result.durationTicks(),
                                result.amplifier(),
                                result.saveKey()),
                        false));
        recordStatusProfileApplication(request.playerId(), result.hazardId(), result.effectId(), result.saveKey(),
                application.durationTicks(), application.amplifier(), application.damageApplied(),
                application.appliedGameTick(), true, application.loaded());
        return result;
    }

    public EchoStandaloneStatusEffectApplyResult applyStatusEffect(EchoStandaloneStatusEffectApplyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("status effect apply request must not be null");
        }
        long appliedGameTick = Math.max(0L, request.gameTick());
        long expiresAtTick = appliedGameTick + Math.max(0, request.statusEffect().durationTicks());
        Map<String, Object> activeStatusState = new LinkedHashMap<>();
        activeStatusState.put("moduleId", "echoworldcore");
        activeStatusState.put("playerId", request.playerId());
        activeStatusState.put("hazardId", request.hazardId());
        activeStatusState.put("effectId", request.statusEffect().id());
        activeStatusState.put("saveKey", request.statusEffect().saveKey());
        activeStatusState.put("durationTicks", request.statusEffect().durationTicks());
        activeStatusState.put("amplifier", request.statusEffect().amplifier());
        activeStatusState.put("damageApplied", request.damageApplied());
        activeStatusState.put("appliedGameTick", appliedGameTick);
        activeStatusState.put("expiresAtTick", expiresAtTick);
        activeStatusState.put("loaded", request.loaded());
        EchoStandaloneStatusEffectApplyResult result = new EchoStandaloneStatusEffectApplyResult(
                request.playerId(),
                request.hazardId(),
                request.statusEffect().id(),
                request.statusEffect().durationTicks(),
                request.statusEffect().amplifier(),
                request.statusEffect().saveKey(),
                request.damageApplied(),
                appliedGameTick,
                expiresAtTick,
                activeStatusState,
                request.sourceReason(),
                request.loaded(),
                true
        );
        lastStatusEffectApplications.put(request.playerId(), result);
        recordActiveStatusEffect(request.playerId(), result.effectId(), result.hazardId(), result.saveKey(),
                result.durationTicks(), result.amplifier(), result.damageApplied(),
                result.appliedGameTick(), result.loaded());
        return result;
    }

    public EchoStandaloneStatusExposureMitigationResult mitigateStatusExposure(
            EchoStandaloneStatusExposureMitigationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("status exposure mitigation request must not be null");
        }
        double mitigationFactor = 1.0D - request.mitigationRatio();
        double effectiveIntensity = request.exposureIntensity() * mitigationFactor;
        boolean immune = effectiveIntensity <= request.immunityThreshold();
        int effectiveDurationTicks = immune ? 0 : (int) Math.round(request.durationTicks() * mitigationFactor);
        double effectiveAccumulation = immune ? 0.0D : request.accumulationPerSecond() * mitigationFactor;
        if (immune) {
            effectiveIntensity = 0.0D;
        }
        Map<String, Object> exposureState = new LinkedHashMap<>();
        exposureState.put("adapterCoreContract", "echostatuscore:status/exposure_mitigation");
        exposureState.put("adapterCoreBridge", true);
        exposureState.put("nativeLoaderBackend", true);
        exposureState.put("moduleId", "echostatuscore");
        exposureState.put("playerId", request.playerId());
        exposureState.put("exposureId", request.exposureId());
        exposureState.put("hazardId", request.hazardId());
        exposureState.put("effectId", request.statusEffect().id());
        exposureState.put("statusKind", request.statusKind());
        exposureState.put("resistanceId", request.resistanceId());
        exposureState.put("mitigationRatio", request.mitigationRatio());
        exposureState.put("immunityThreshold", request.immunityThreshold());
        exposureState.put("originalIntensity", request.exposureIntensity());
        exposureState.put("effectiveIntensity", effectiveIntensity);
        exposureState.put("originalDurationTicks", request.durationTicks());
        exposureState.put("effectiveDurationTicks", effectiveDurationTicks);
        exposureState.put("originalAccumulationPerSecond", request.accumulationPerSecond());
        exposureState.put("effectiveAccumulationPerSecond", effectiveAccumulation);
        exposureState.put("immune", immune);
        exposureState.put("gameTick", request.gameTick());
        exposureState.put("sourceReason", request.sourceReason());

        EchoStandaloneStatusExposureMitigationResult result = new EchoStandaloneStatusExposureMitigationResult(
                request.playerId(),
                request.exposureId(),
                request.hazardId(),
                request.statusEffect().id(),
                request.statusKind(),
                request.exposureIntensity(),
                effectiveIntensity,
                request.durationTicks(),
                effectiveDurationTicks,
                request.accumulationPerSecond(),
                effectiveAccumulation,
                request.resistanceId(),
                request.mitigationRatio(),
                request.immunityThreshold(),
                immune,
                exposureState,
                request.gameTick(),
                request.sourceReason(),
                true
        );
        lastStatusExposureMitigations.put(request.playerId(), result);
        activeStatusExposureStates.put(request.exposureId(), result);
        return result;
    }

    public EchoStandaloneStatusExposureMitigationResult lastStatusExposureMitigation(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastStatusExposureMitigations.get(playerId);
    }

    public EchoStandaloneStatusExposureMitigationResult activeStatusExposureState(String exposureId) {
        if (exposureId == null || exposureId.isBlank()) {
            throw new IllegalArgumentException("status exposure id must not be blank");
        }
        return activeStatusExposureStates.get(exposureId);
    }

    public EchoStandaloneStatusEffectStackingResult stackStatusEffect(EchoStandaloneStatusEffectStackingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("status effect stacking request must not be null");
        }
        boolean refreshDuration = "REFRESH_DURATION".equals(request.stackingPolicy());
        int durationTicks = refreshDuration && request.hadPrevious()
                ? Math.max(request.previousDurationTicks(), request.statusEffect().durationTicks())
                : request.statusEffect().durationTicks();
        int amplifier = refreshDuration && request.hadPrevious()
                ? Math.max(request.previousAmplifier(), request.statusEffect().amplifier())
                : request.statusEffect().amplifier();
        double damageApplied = Math.max(request.previousDamageApplied(), request.damageApplied());
        long appliedGameTick = Math.max(0L, request.gameTick());
        long expiresAtTick = appliedGameTick + Math.max(0, durationTicks);
        return new EchoStandaloneStatusEffectStackingResult(
                request.playerId(),
                request.hazardId(),
                request.statusEffect().id(),
                request.statusEffect().saveKey(),
                request.stackingPolicy(),
                durationTicks,
                amplifier,
                damageApplied,
                appliedGameTick,
                expiresAtTick,
                request.hadPrevious(),
                request.hadPrevious() && refreshDuration,
                request.hadPrevious() && amplifier > request.previousAmplifier(),
                request.hadPrevious() && !refreshDuration,
                true,
                request.loaded(),
                request.sourceReason());
    }

    public EchoStandaloneStatusEffectLoadResult loadPersistedStatusEffect(
            EchoStandalonePersistedStatusEffectLoadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("persisted status effect load request must not be null");
        }
        Map<String, Object> savedStatus = persistedStatusStates.getOrDefault(
                statusStoreKey(request.playerId(), request.hazardId()),
                Map.of());
        return loadStatusEffect(new EchoStandaloneStatusEffectLoadRequest(
                request.playerId(),
                request.hazardId(),
                request.saveKey(),
                savedStatus,
                request.gameTick(),
                request.sourceReason()
        ));
    }

    public EchoStandaloneStatusEffectLoadResult loadStatusEffect(EchoStandaloneStatusEffectLoadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("status effect load request must not be null");
        }
        Map<String, Object> payload = statusPayload(request.savedStatusState(), request.saveKey());
        boolean loaded = !payload.isEmpty();
        EchoStandaloneStatusEffectLoadResult result = new EchoStandaloneStatusEffectLoadResult(
                request.playerId(),
                request.hazardId(),
                loaded ? text(payload.get("effectId")) : "",
                loaded ? integer(payload.get("durationTicks")) : 0,
                loaded ? integer(payload.get("amplifier")) : 0,
                request.saveKey(),
                loaded ? floating(payload.get("damageApplied")) : 0.0F,
                loaded ? Math.max(0L, longs(payload.get("gameTick"))) : 0L,
                request.gameTick(),
                request.sourceReason(),
                loaded
        );
        lastStatusEffectLoads.put(request.playerId(), result);
        if (loaded) {
            EchoStandaloneStatusEffectApplyResult application = applyStatusEffect(
                    new EchoStandaloneStatusEffectApplyRequest(
                            result.playerId(),
                            result.hazardId(),
                            result.damageApplied(),
                            result.loadedGameTick(),
                            "standalone-status-load-apply",
                            new EchoStandaloneStatusEffect(
                                    result.effectId(),
                                    result.durationTicks(),
                                    result.amplifier(),
                                    result.saveKey()),
                            true));
            recordStatusProfileApplication(request.playerId(), result.hazardId(), result.effectId(), result.saveKey(),
                    application.durationTicks(), application.amplifier(), application.damageApplied(),
                    application.appliedGameTick(), true, application.loaded());
        }
        return result;
    }

    public EchoStandaloneStatusEffectApplyResult lastStatusEffectApplication(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastStatusEffectApplications.get(playerId);
    }

    public EchoStandaloneStatusEffectStackingResult lastStatusEffectStacking(String playerId, String effectId) {
        if (playerId == null || playerId.isBlank() || effectId == null || effectId.isBlank()) {
            throw new IllegalArgumentException("player and effect ids must not be blank");
        }
        return lastStatusEffectStackings.getOrDefault(playerId, Map.of()).get(effectId);
    }

    public Map<String, EchoStandaloneStatusEffectStackingResult> lastStatusEffectStackings(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return Map.copyOf(lastStatusEffectStackings.getOrDefault(playerId, Map.of()));
    }

    public EchoStandaloneStatusEffectSaveResult lastStatusEffectSave(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastStatusEffectSaves.get(playerId);
    }

    public EchoStandaloneStatusEffectLoadResult lastStatusEffectLoad(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastStatusEffectLoads.get(playerId);
    }

    public EchoStandaloneStatusEffectExpiryResult evaluateStatusEffectExpiry(
            EchoStandaloneStatusEffectExpiryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("status effect expiry request must not be null");
        }
        long tick = Math.max(0L, request.gameTick());
        boolean expired = tick >= request.expiresAtTick();
        EchoStandaloneStatusEffectExpiryResult result = new EchoStandaloneStatusEffectExpiryResult(
                request.playerId(),
                request.hazardId(),
                request.effectId(),
                request.saveKey(),
                request.appliedGameTick(),
                request.expiresAtTick(),
                tick,
                expired,
                !expired,
                request.sourceReason()
        );
        Map<String, EchoStandaloneStatusEffectExpiryResult> expiries =
                new LinkedHashMap<>(lastStatusEffectExpiries.getOrDefault(request.playerId(), Map.of()));
        expiries.put(result.effectId(), result);
        lastStatusEffectExpiries.put(request.playerId(), Map.copyOf(expiries));
        return result;
    }

    public EchoStandaloneStatusEffectExpiryResult lastStatusEffectExpiry(String playerId, String effectId) {
        if (playerId == null || playerId.isBlank() || effectId == null || effectId.isBlank()) {
            throw new IllegalArgumentException("player and effect ids must not be blank");
        }
        return lastStatusEffectExpiries.getOrDefault(playerId, Map.of()).get(effectId);
    }

    public Map<String, EchoStandaloneStatusEffectExpiryResult> lastStatusEffectExpiries(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return Map.copyOf(lastStatusEffectExpiries.getOrDefault(playerId, Map.of()));
    }

    public List<String> activeStatusEffects(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return activeStatusEffects.getOrDefault(playerId, List.of());
    }

    public EchoStandaloneActiveStatusEffectState activeStatusEffectState(String playerId, String effectId) {
        if (playerId == null || playerId.isBlank() || effectId == null || effectId.isBlank()) {
            throw new IllegalArgumentException("player and effect ids must not be blank");
        }
        return activeStatusEffectStates.getOrDefault(playerId, Map.of()).get(effectId);
    }

    public Map<String, EchoStandaloneActiveStatusEffectState> activeStatusEffectStates(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return Map.copyOf(activeStatusEffectStates.getOrDefault(playerId, Map.of()));
    }

    public EchoStandaloneStatusProfileState activeStatusProfileState(String playerId, String effectId) {
        if (playerId == null || playerId.isBlank() || effectId == null || effectId.isBlank()) {
            throw new IllegalArgumentException("player and effect ids must not be blank");
        }
        return activeStatusProfileStates.getOrDefault(playerId, Map.of()).get(effectId);
    }

    public Map<String, EchoStandaloneStatusProfileState> activeStatusProfileStates(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return Map.copyOf(activeStatusProfileStates.getOrDefault(playerId, Map.of()));
    }

    public int tickStatusEffects(String playerId, long gameTick) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        pruneActiveStatusEffects(playerId, Math.max(0L, gameTick));
        return activeStatusEffects(playerId).size();
    }

    public EchoStandaloneStructurePoiLookupResult lookupStructurePoi(EchoStandaloneStructurePoiLookupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("structure POI lookup request must not be null");
        }
        long dx = request.playerX() - request.structure().x();
        long dy = request.playerY() - request.structure().y();
        long dz = request.playerZ() - request.structure().z();
        long distanceSquared = dx * dx + dy * dy + dz * dz;
        long maxDistanceSquared = (long) request.maxDistance() * request.maxDistance();
        boolean inRange = distanceSquared <= maxDistanceSquared;
        String markerId = "echoworldcore:marker/" + idPath(request.structure().id()) + "/"
                + request.structure().x() + "_" + request.structure().y() + "_" + request.structure().z();
        EchoStandaloneStructurePoiLookupResult result = new EchoStandaloneStructurePoiLookupResult(
                request.playerId(),
                request.regionId(),
                request.structure().id(),
                request.structure().poiId(),
                request.structure().x(),
                request.structure().y(),
                request.structure().z(),
                distanceSquared,
                request.maxDistance(),
                inRange,
                markerId,
                inRange ? "POI_IN_RANGE" : "POI_OUT_OF_RANGE",
                request.gameTick(),
                request.sourceReason()
        );
        if (inRange) {
            resolvedPoiMarkers.put(markerId, result);
            EchoStandaloneStructurePoiMarkerStateResult markerState = persistStructurePoiMarkerState(
                    new EchoStandaloneStructurePoiMarkerStateRequest(
                            result.playerId(),
                            "standalone-structure-poi-marker-state",
                            result
                    ));
            lastPoiMarkerStateResults.put(result.playerId(), markerState);
            resolvedPoiMarkerStateResults.put(markerState.markerId(), markerState);
            resolvedPoiStates.put(markerState.markerId(), EchoStandaloneStructurePoiState.from(markerState));
            EchoStandaloneStructureDiscoveryStateResult discoveryState = discoverStructurePoi(
                    new EchoStandaloneStructureDiscoveryStateRequest(
                            result.playerId(),
                            "worldcore-structure-discovery-state",
                            markerState));
            lastStructureDiscoveryStates.put(result.playerId(), discoveryState);
            resolvedStructureDiscoveryStates.put(discoveryState.markerId(), discoveryState);
        }
        return result;
    }

    public EchoStandaloneStructurePoiMarkerStateResult persistStructurePoiMarkerState(
            EchoStandaloneStructurePoiMarkerStateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("structure POI marker state request must not be null");
        }
        if (request.lookup() == null) {
            throw new IllegalArgumentException("structure POI marker state lookup must not be null");
        }
        return new EchoStandaloneStructurePoiMarkerStateResult(
                request.playerId(),
                request.lookup().markerId(),
                request.lookup().regionId(),
                request.lookup().structureId(),
                request.lookup().poiId(),
                request.lookup().x(),
                request.lookup().y(),
                request.lookup().z(),
                request.lookup().distanceSquared(),
                request.lookup().maxDistance(),
                request.lookup().inRange(),
                request.lookup().inRange(),
                request.lookup().lookupType(),
                request.lookup().gameTick(),
                request.sourceReason()
        );
    }

    public EchoStandaloneStructurePoiLookupResult resolvedPoiMarker(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            throw new IllegalArgumentException("marker id must not be blank");
        }
        return resolvedPoiMarkers.get(markerId);
    }

    public Map<String, EchoStandaloneStructurePoiLookupResult> resolvedPoiMarkers() {
        return Map.copyOf(resolvedPoiMarkers);
    }

    public EchoStandaloneStructurePoiMarkerStateResult lastPoiMarkerState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastPoiMarkerStateResults.get(playerId);
    }

    public EchoStandaloneStructurePoiMarkerStateResult resolvedPoiMarkerState(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            throw new IllegalArgumentException("marker id must not be blank");
        }
        return resolvedPoiMarkerStateResults.get(markerId);
    }

    public Map<String, EchoStandaloneStructurePoiMarkerStateResult> resolvedPoiMarkerStates() {
        return Map.copyOf(resolvedPoiMarkerStateResults);
    }

    public EchoStandaloneStructurePoiState resolvedPoiState(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            throw new IllegalArgumentException("marker id must not be blank");
        }
        return resolvedPoiStates.get(markerId);
    }

    public Map<String, EchoStandaloneStructurePoiState> resolvedPoiStates() {
        return Map.copyOf(resolvedPoiStates);
    }

    public EchoStandaloneStructureDiscoveryStateResult discoverStructurePoi(
            EchoStandaloneStructureDiscoveryStateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("structure discovery state request must not be null");
        }
        if (request.markerState() == null) {
            throw new IllegalArgumentException("structure discovery marker state must not be null");
        }
        boolean discovered = request.markerState().inRange() && request.markerState().markerPersisted();
        return new EchoStandaloneStructureDiscoveryStateResult(
                request.playerId(),
                request.markerState().markerId(),
                request.markerState().regionId(),
                request.markerState().structureId(),
                request.markerState().poiId(),
                "UNKNOWN",
                discovered ? "DISCOVERED" : "UNKNOWN",
                discovered,
                discovered,
                discovered,
                request.markerState().lastGameTick(),
                request.sourceReason());
    }

    public EchoStandaloneStructureDiscoveryStateResult lastStructureDiscoveryState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastStructureDiscoveryStates.get(playerId);
    }

    public EchoStandaloneStructureDiscoveryStateResult resolvedStructureDiscoveryState(String markerId) {
        if (markerId == null || markerId.isBlank()) {
            throw new IllegalArgumentException("marker id must not be blank");
        }
        return resolvedStructureDiscoveryStates.get(markerId);
    }

    public Map<String, EchoStandaloneStructureDiscoveryStateResult> resolvedStructureDiscoveryStates() {
        return Map.copyOf(resolvedStructureDiscoveryStates);
    }

    public EchoStandaloneSpawnRuleEventResult planSpawnRuleEvent(EchoStandaloneSpawnRuleEventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("spawn rule event request must not be null");
        }
        int scaledBudget = Math.max(0, (int) Math.round(
                request.spawnRule().maxCount() * request.difficulty().spawnMultiplier()));
        int spawnCount = Math.max(0, scaledBudget - request.activeMobCount());
        String eventType = spawnCount > 0 ? "SPAWN_ALLOWED" : "SPAWN_CAPPED";
        EchoStandaloneSpawnRuleEventResult result = new EchoStandaloneSpawnRuleEventResult(
                request.playerId(),
                request.spawnRule().id(),
                request.spawnRule().entityId(),
                request.regionId(),
                request.difficulty().id(),
                request.spawnRule().maxCount(),
                request.activeMobCount(),
                scaledBudget,
                spawnCount,
                request.difficulty().spawnMultiplier(),
                request.spawnRule().difficultyWeight(),
                eventType,
                request.x(),
                request.y(),
                request.z(),
                request.gameTick(),
                request.sourceReason()
        );
        lastSpawnRuleEvents.put(request.playerId(), result);
        EchoStandaloneSpawnZoneStateResult zoneState = persistSpawnZoneState(
                new EchoStandaloneSpawnZoneStateRequest(
                        result.playerId(),
                        "standalone-spawn-zone-state",
                        result
                ));
        lastSpawnZoneStateResults.put(request.playerId(), zoneState);
        activeSpawnZoneStateResults.put(zoneState.zoneKey(), zoneState);
        activeSpawnPopulations.put(zoneState.zoneKey(), zoneState.activePopulation());
        activeSpawnZoneStates.put(zoneState.zoneKey(), EchoStandaloneSpawnZoneState.from(zoneState));
        activeDifficultyProfiles.put(request.playerId(), request.difficulty());
        regionDifficultyProfiles.put(request.regionId(), request.difficulty());
        recordDifficultyApplication(
                request.playerId(),
                request.regionId(),
                request.difficulty(),
                "",
                0.0D,
                0.0D,
                result.ruleId(),
                result.maxCount(),
                result.scaledBudget(),
                result.activeMobCount() + result.spawnCount(),
                result.gameTick(),
                "EchoStandaloneWorldEffectsRuntime.planSpawnRuleEvent");
        return result;
    }

    public EchoStandaloneSpawnZoneStateResult persistSpawnZoneState(EchoStandaloneSpawnZoneStateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("spawn zone state request must not be null");
        }
        if (request.event() == null) {
            throw new IllegalArgumentException("spawn zone state event must not be null");
        }
        int activePopulation = request.event().activeMobCount() + request.event().spawnCount();
        String zoneKey = spawnZoneKey(request.event().regionId(), request.event().ruleId());
        return new EchoStandaloneSpawnZoneStateResult(
                request.playerId(),
                request.event().regionId(),
                request.event().ruleId(),
                zoneKey,
                request.event().entityId(),
                request.event().difficultyId(),
                request.event().maxCount(),
                request.event().activeMobCount(),
                request.event().scaledBudget(),
                request.event().spawnCount(),
                activePopulation,
                request.event().spawnMultiplier(),
                request.event().difficultyWeight(),
                request.event().eventType(),
                request.event().x(),
                request.event().y(),
                request.event().z(),
                request.event().gameTick(),
                request.sourceReason()
        );
    }

    public EchoStandaloneDifficulty activeDifficultyProfile(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return activeDifficultyProfiles.get(playerId);
    }

    public EchoStandaloneDifficulty regionDifficultyProfile(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("region id must not be blank");
        }
        return regionDifficultyProfiles.get(regionId);
    }

    public EchoStandaloneDifficultyProfileSelectionResult selectDifficultyProfile(
            EchoStandaloneDifficultyProfileSelectionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("difficulty profile selection request must not be null");
        }
        String selectedDifficulty = normalizeDifficulty(request.requestedDifficulty());
        EchoStandaloneDifficulty difficulty = difficultyFor(selectedDifficulty);
        EchoStandaloneDifficultyProfileSelectionResult result = new EchoStandaloneDifficultyProfileSelectionResult(
                text(request.playerId()),
                text(request.regionId()),
                text(request.missionId()),
                request.requestedDifficulty(),
                selectedDifficulty,
                difficulty.id(),
                difficulty.hazardMultiplier(),
                difficulty.spawnMultiplier(),
                Math.max(0L, request.gameTick()),
                text(request.sourceReason()),
                true
        );
        if (!result.regionId().isBlank()) {
            regionDifficultyProfiles.put(result.regionId(),
                    new EchoStandaloneDifficulty(result.difficultyId(), result.hazardMultiplier(), result.spawnMultiplier()));
            regionDifficultyProfileSelections.put(result.regionId(), result);
        }
        return result;
    }

    public EchoStandaloneDifficultyProfileSelectionResult regionDifficultyProfileSelection(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("region id must not be blank");
        }
        return regionDifficultyProfileSelections.get(regionId);
    }

    public Map<String, EchoStandaloneDifficultyProfileSelectionResult> regionDifficultyProfileSelections() {
        return Map.copyOf(regionDifficultyProfileSelections);
    }

    public EchoStandaloneDifficultyApplicationState activeDifficultyApplicationState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return activeDifficultyApplicationStates.get(playerId);
    }

    public EchoStandaloneDifficultyApplicationState regionDifficultyApplicationState(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("region id must not be blank");
        }
        return regionDifficultyApplicationStates.get(regionId);
    }

    public Map<String, EchoStandaloneDifficultyApplicationState> difficultyApplicationStates() {
        return Map.copyOf(regionDifficultyApplicationStates);
    }

    public EchoStandaloneDifficultyApplicationResult activeDifficultyApplicationResult(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return activeDifficultyApplicationResults.get(playerId);
    }

    public EchoStandaloneDifficultyApplicationResult regionDifficultyApplicationResult(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("region id must not be blank");
        }
        return regionDifficultyApplicationResults.get(regionId);
    }

    public Map<String, EchoStandaloneDifficultyApplicationResult> difficultyApplicationResults() {
        return Map.copyOf(regionDifficultyApplicationResults);
    }

    public EchoStandaloneSpawnRuleEventResult lastSpawnRuleEvent(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastSpawnRuleEvents.get(playerId);
    }

    public int activeSpawnPopulation(String regionId, String ruleId) {
        return activeSpawnPopulations.getOrDefault(spawnZoneKey(regionId, ruleId), 0);
    }

    public Map<String, Integer> activeSpawnPopulations() {
        return Map.copyOf(activeSpawnPopulations);
    }

    public EchoStandaloneSpawnZoneState activeSpawnZoneState(String regionId, String ruleId) {
        if (regionId == null || regionId.isBlank() || ruleId == null || ruleId.isBlank()) {
            throw new IllegalArgumentException("region and rule ids must not be blank");
        }
        return activeSpawnZoneStates.get(spawnZoneKey(regionId, ruleId));
    }

    public EchoStandaloneSpawnZoneStateResult lastSpawnZoneState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastSpawnZoneStateResults.get(playerId);
    }

    public EchoStandaloneSpawnZoneStateResult activeSpawnZoneStateResult(String regionId, String ruleId) {
        if (regionId == null || regionId.isBlank() || ruleId == null || ruleId.isBlank()) {
            throw new IllegalArgumentException("region and rule ids must not be blank");
        }
        return activeSpawnZoneStateResults.get(spawnZoneKey(regionId, ruleId));
    }

    public Map<String, EchoStandaloneSpawnZoneStateResult> activeSpawnZoneStateResults() {
        return Map.copyOf(activeSpawnZoneStateResults);
    }

    public Map<String, EchoStandaloneSpawnZoneState> activeSpawnZoneStates() {
        return Map.copyOf(activeSpawnZoneStates);
    }

    public EchoStandaloneWorldCellSampleResult sampleWorldCell(EchoStandaloneWorldCellSampleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("world cell sample request must not be null");
        }
        boolean inRegion = request.region().contains(request.x(), request.z());
        boolean inHazard = request.hazard().affects(request.x(), request.z());
        EchoStandaloneWorldCellSampleResult result = new EchoStandaloneWorldCellSampleResult(
                request.playerId(),
                request.worldId(),
                inRegion ? request.region().id() : "",
                inHazard ? request.hazard().id() : "",
                inRegion ? request.biome().id() : "",
                inRegion ? request.structure().id() : "",
                inRegion ? request.structure().poiId() : "",
                cellKey(request.worldId(), request.x(), request.y(), request.z()),
                request.x(),
                request.y(),
                request.z(),
                inRegion,
                inHazard,
                request.gameTick(),
                request.sourceReason()
        );
        lastWorldCellSamples.put(request.playerId(), result);
        sampledWorldCells.put(result.cellKey(), result);
        EchoStandaloneHazardTransitionResult hazardTransition = transitionHazard(
                new EchoStandaloneHazardTransitionRequest(
                        result.playerId(),
                        activeHazardIds.getOrDefault(result.playerId(), ""),
                        result.inHazard() ? result.activeHazardId() : "",
                        request.hazard().statusEffectId(),
                        result.gameTick(),
                        "worldcore-hazard-field-transition"
                ));
        EchoStandaloneBiomeHazardOverlayResult overlay = resolveBiomeHazardOverlay(
                new EchoStandaloneBiomeHazardOverlayRequest(
                        result.playerId(),
                        result.worldId(),
                        result.x(),
                        result.y(),
                        result.z(),
                        result.gameTick(),
                        "standalone-biome-hazard-overlay",
                        request.biome(),
                        request.hazard(),
                        result.inRegion(),
                        result.inHazard()
                ));
        lastBiomeHazardOverlays.put(result.playerId(), overlay);
        sampledBiomeHazardOverlays.put(overlay.cellKey(), overlay);
        EchoStandaloneWorldChunkStateResult chunkState = resolveWorldChunkState(
                new EchoStandaloneWorldChunkStateRequest(
                        result.playerId(),
                        result.worldId(),
                        result.x(),
                        result.y(),
                        result.z(),
                        result.gameTick(),
                        "standalone-chunk-state",
                        result
                ));
        lastWorldChunkStateResults.put(result.playerId(), chunkState);
        sampledWorldChunkStateResults.put(chunkState.chunkKey(), chunkState);
        sampledWorldChunks.put(chunkState.chunkKey(), EchoStandaloneWorldChunkState.from(chunkState));
        if (result.inHazard()) {
            sampledHazardFields.put(result.activeHazardId(), result);
            EchoStandaloneHazardFieldStateResult hazardFieldState = resolveHazardFieldState(
                    new EchoStandaloneHazardFieldStateRequest(
                            result.playerId(),
                            result.worldId(),
                            result.gameTick(),
                            "standalone-hazard-field-state",
                            request.hazard(),
                            result
                    ));
            lastHazardFieldStateResults.put(result.playerId(), hazardFieldState);
            sampledHazardFieldStateResults.put(hazardFieldState.hazardId(), hazardFieldState);
            sampledHazardFieldStates.put(hazardFieldState.hazardId(),
                    EchoStandaloneHazardFieldState.from(hazardFieldState));
        }
        return result;
    }

    public EchoStandaloneWorldDataCatalogResult materializeWorldDataCatalog(
            EchoStandaloneWorldDataCatalogRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("world data catalog request must not be null");
        }
        EchoStandaloneWorldDataCatalogResult result = new EchoStandaloneWorldDataCatalogResult(
                request.regionIds().size(),
                request.hazardIds().size(),
                request.weatherProfileIds().size(),
                request.biomeIds().size(),
                request.structureIds().size(),
                request.statusEffectIds().size(),
                request.difficultyIds().size(),
                request.spawnRuleCount(),
                request.sourceFiles().size(),
                representative(request.regionIds()),
                representative(request.hazardIds()),
                representative(request.weatherProfileIds()),
                representative(request.biomeIds()),
                representative(request.structureIds()),
                representative(request.statusEffectIds()),
                representative(request.difficultyIds()),
                request.sourceReason(),
                true);
        lastWorldDataCatalogResult = result;
        return result;
    }

    public EchoStandaloneWorldDataCatalogResult lastWorldDataCatalogResult() {
        return lastWorldDataCatalogResult;
    }

    public EchoStandaloneBiomeHazardOverlayResult resolveBiomeHazardOverlay(
            EchoStandaloneBiomeHazardOverlayRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("biome hazard overlay request must not be null");
        }
        boolean active = request.inRegion() && request.inHazard() && !request.biome().hazardTag().isBlank();
        double intensity = active ? Math.max(1.0D, request.hazard().damagePerTick()) : 0.0D;
        String overlayId = request.biome().id() + "|" + (active ? request.hazard().id() : "no_hazard");
        return new EchoStandaloneBiomeHazardOverlayResult(
                request.playerId(),
                request.worldId(),
                request.biome().id(),
                request.biome().biomeTag(),
                request.biome().hazardTag(),
                active ? request.hazard().id() : "",
                overlayId,
                cellKey(request.worldId(), request.x(), request.y(), request.z()),
                intensity,
                active,
                active,
                request.gameTick(),
                request.sourceReason()
        );
    }

    public EchoStandaloneBiomeAmbientStateResult applyBiomeAmbientState(
            EchoStandaloneBiomeAmbientStateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("biome ambient state request must not be null");
        }
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("biomeProfile", request.biomeProfileId());
        hudState.put("biomeTag", request.biomeTag());
        hudState.put("ambience", request.ambienceId());
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", request.soundProfileId());
        audioState.put("loop", true);
        audioState.put("biomeProfile", request.biomeProfileId());
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("atmosphereProfile", request.atmosphereProfileId());
        renderState.put("particleProfile", request.particleProfileId());
        renderState.put("visibilityModifier", request.visibilityModifier());
        renderState.put("ambientAssets", request.ambientAssetIds());
        List<Map<String, String>> runtimeBindings = List.of(
                binding("hud.biome", "biomeProfile", "echohudcore:biome/status_line"),
                binding("sound.ambience", "soundProfile", request.soundProfileId()),
                binding("render.particles", "particleProfile", request.particleProfileId()),
                binding("render.atmosphere", "atmosphereProfile", request.atmosphereProfileId()));
        Map<String, Object> ambientState = new LinkedHashMap<>();
        ambientState.put("adapterCoreContract", "echobiomecore:biome/ambient_state");
        ambientState.put("adapterCoreBridge", true);
        ambientState.put("nativeLoaderBackend", true);
        ambientState.put("moduleId", "echobiomecore");
        ambientState.put("playerId", request.playerId());
        ambientState.put("biomeProfileId", request.biomeProfileId());
        ambientState.put("biomeTag", request.biomeTag());
        ambientState.put("ambienceId", request.ambienceId());
        ambientState.put("soundProfileId", request.soundProfileId());
        ambientState.put("particleProfileId", request.particleProfileId());
        ambientState.put("ambientAssetIds", request.ambientAssetIds());
        ambientState.put("atmosphereProfileId", request.atmosphereProfileId());
        ambientState.put("visibilityModifier", request.visibilityModifier());
        ambientState.put("hudState", hudState);
        ambientState.put("audioState", audioState);
        ambientState.put("renderState", renderState);
        ambientState.put("runtimeBindings", runtimeBindings);
        ambientState.put("gameTick", request.gameTick());
        ambientState.put("sourceReason", request.sourceReason());
        EchoStandaloneBiomeAmbientStateResult result = new EchoStandaloneBiomeAmbientStateResult(
                request.playerId(),
                request.biomeProfileId(),
                request.biomeTag(),
                request.ambienceId(),
                hudState,
                audioState,
                renderState,
                ambientState,
                runtimeBindings,
                request.gameTick(),
                request.sourceReason(),
                true);
        lastBiomeAmbientStates.put(request.playerId(), result);
        biomeAmbientStates.put(request.biomeProfileId(), result);
        return result;
    }

    public EchoStandaloneHazardFieldStateResult resolveHazardFieldState(
            EchoStandaloneHazardFieldStateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("hazard field state request must not be null");
        }
        if (request.hazard() == null || request.cellSample() == null) {
            throw new IllegalArgumentException("hazard field state hazard and cell sample must not be null");
        }
        return new EchoStandaloneHazardFieldStateResult(
                request.playerId(),
                request.worldId(),
                request.hazard().id(),
                request.hazard().type(),
                request.hazard().centerX(),
                request.hazard().centerZ(),
                request.hazard().radius(),
                request.hazard().damagePerTick(),
                request.hazard().statusEffectId(),
                request.cellSample().cellKey(),
                request.cellSample().inHazard(),
                request.gameTick(),
                request.sourceReason()
        );
    }

    public EchoStandaloneWorldChunkStateResult resolveWorldChunkState(
            EchoStandaloneWorldChunkStateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("world chunk state request must not be null");
        }
        if (request.cellSample() == null) {
            throw new IllegalArgumentException("world chunk state cell sample must not be null");
        }
        int chunkX = Math.floorDiv(request.x(), 16);
        int chunkZ = Math.floorDiv(request.z(), 16);
        return new EchoStandaloneWorldChunkStateResult(
                request.playerId(),
                request.worldId(),
                chunkKey(request.worldId(), request.x(), request.z()),
                chunkX,
                chunkZ,
                request.cellSample().cellKey(),
                request.cellSample().x(),
                request.cellSample().y(),
                request.cellSample().z(),
                request.cellSample().activeRegionId(),
                request.cellSample().activeHazardId(),
                request.cellSample().biomeProfileId(),
                request.cellSample().structureId(),
                request.cellSample().poiId(),
                request.cellSample().inRegion(),
                request.cellSample().inHazard(),
                request.gameTick(),
                request.sourceReason()
        );
    }

    public EchoStandaloneWorldCellSampleResult lastWorldCellSample(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastWorldCellSamples.get(playerId);
    }

    public EchoStandaloneWorldCellSampleResult sampledWorldCell(String cellKey) {
        if (cellKey == null || cellKey.isBlank()) {
            throw new IllegalArgumentException("cell key must not be blank");
        }
        return sampledWorldCells.get(cellKey);
    }

    public EchoStandaloneWorldCellSampleResult sampledHazardField(String hazardId) {
        if (hazardId == null || hazardId.isBlank()) {
            throw new IllegalArgumentException("hazard id must not be blank");
        }
        return sampledHazardFields.get(hazardId);
    }

    public Map<String, EchoStandaloneWorldCellSampleResult> sampledWorldCells() {
        return Map.copyOf(sampledWorldCells);
    }

    public Map<String, EchoStandaloneWorldCellSampleResult> sampledHazardFields() {
        return Map.copyOf(sampledHazardFields);
    }

    public EchoStandaloneBiomeHazardOverlayResult lastBiomeHazardOverlay(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastBiomeHazardOverlays.get(playerId);
    }

    public EchoStandaloneBiomeHazardOverlayResult sampledBiomeHazardOverlay(String cellKey) {
        if (cellKey == null || cellKey.isBlank()) {
            throw new IllegalArgumentException("cell key must not be blank");
        }
        return sampledBiomeHazardOverlays.get(cellKey);
    }

    public Map<String, EchoStandaloneBiomeHazardOverlayResult> sampledBiomeHazardOverlays() {
        return Map.copyOf(sampledBiomeHazardOverlays);
    }

    public EchoStandaloneBiomeAmbientStateResult lastBiomeAmbientState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastBiomeAmbientStates.get(playerId);
    }

    public EchoStandaloneBiomeAmbientStateResult biomeAmbientState(String biomeProfileId) {
        if (biomeProfileId == null || biomeProfileId.isBlank()) {
            throw new IllegalArgumentException("biome profile id must not be blank");
        }
        return biomeAmbientStates.get(biomeProfileId);
    }

    public EchoStandaloneWorldChunkStateResult lastWorldChunkState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastWorldChunkStateResults.get(playerId);
    }

    public EchoStandaloneWorldChunkStateResult sampledWorldChunkState(String chunkKey) {
        if (chunkKey == null || chunkKey.isBlank()) {
            throw new IllegalArgumentException("chunk key must not be blank");
        }
        return sampledWorldChunkStateResults.get(chunkKey);
    }

    public Map<String, EchoStandaloneWorldChunkStateResult> sampledWorldChunkStates() {
        return Map.copyOf(sampledWorldChunkStateResults);
    }

    public EchoStandaloneWorldChunkState sampledWorldChunk(String chunkKey) {
        if (chunkKey == null || chunkKey.isBlank()) {
            throw new IllegalArgumentException("chunk key must not be blank");
        }
        return sampledWorldChunks.get(chunkKey);
    }

    public Map<String, EchoStandaloneWorldChunkState> sampledWorldChunks() {
        return Map.copyOf(sampledWorldChunks);
    }

    public EchoStandaloneHazardFieldState sampledHazardFieldState(String hazardId) {
        if (hazardId == null || hazardId.isBlank()) {
            throw new IllegalArgumentException("hazard id must not be blank");
        }
        return sampledHazardFieldStates.get(hazardId);
    }

    public EchoStandaloneHazardFieldStateResult lastHazardFieldState(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastHazardFieldStateResults.get(playerId);
    }

    public EchoStandaloneHazardFieldStateResult sampledHazardFieldStateResult(String hazardId) {
        if (hazardId == null || hazardId.isBlank()) {
            throw new IllegalArgumentException("hazard id must not be blank");
        }
        return sampledHazardFieldStateResults.get(hazardId);
    }

    public Map<String, EchoStandaloneHazardFieldStateResult> sampledHazardFieldStateResults() {
        return Map.copyOf(sampledHazardFieldStateResults);
    }

    public Map<String, EchoStandaloneHazardFieldState> sampledHazardFieldStates() {
        return Map.copyOf(sampledHazardFieldStates);
    }

    public EchoStandaloneRegionTransitionResult transitionRegion(EchoStandaloneRegionTransitionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("region transition request must not be null");
        }
        boolean hadPrevious = !request.previousRegionId().isBlank();
        boolean hasCurrent = !request.currentRegionId().isBlank();
        boolean sameRegion = hadPrevious && hasCurrent && request.previousRegionId().equals(request.currentRegionId());
        boolean entered = hasCurrent && !sameRegion;
        boolean exited = hadPrevious && !sameRegion;
        String eventType;
        if (entered && exited) {
            eventType = "SWITCH";
        } else if (entered) {
            eventType = "ENTER";
        } else if (exited) {
            eventType = "EXIT";
        } else {
            eventType = "STAY";
        }
        List<String> missionEvents = entered && !request.currentMissionId().isBlank()
                ? List.of(request.currentMissionId())
                : List.of();
        EchoStandaloneRegionTransitionResult result = new EchoStandaloneRegionTransitionResult(
                request.playerId(),
                request.previousRegionId(),
                request.currentRegionId(),
                eventType,
                entered,
                exited,
                missionEvents,
                request.gameTick(),
                request.sourceReason()
        );
        lastRegionTransitions.put(request.playerId(), result);
        if (hasCurrent) {
            activeRegionIds.put(request.playerId(), request.currentRegionId());
        } else {
            activeRegionIds.remove(request.playerId());
        }
        if (!missionEvents.isEmpty()) {
            ArrayList<String> missions = new ArrayList<>(
                    startedRegionMissions.getOrDefault(request.playerId(), List.of()));
            missions.addAll(missionEvents);
            startedRegionMissions.put(request.playerId(), List.copyOf(missions));
        }
        return result;
    }

    public EchoStandaloneRegionTransitionResult lastRegionTransition(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastRegionTransitions.get(playerId);
    }

    public EchoStandaloneHazardTransitionResult transitionHazard(EchoStandaloneHazardTransitionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("hazard transition request must not be null");
        }
        boolean hadPrevious = !request.previousHazardId().isBlank();
        boolean hasCurrent = !request.currentHazardId().isBlank();
        boolean sameHazard = hadPrevious && hasCurrent && request.previousHazardId().equals(request.currentHazardId());
        boolean entered = hasCurrent && !sameHazard;
        boolean exited = hadPrevious && !sameHazard;
        String eventType;
        if (entered && exited) {
            eventType = "SWITCH";
        } else if (entered) {
            eventType = "ENTER";
        } else if (exited) {
            eventType = "EXIT";
        } else {
            eventType = "STAY";
        }
        List<String> statusEffects = entered && !request.statusEffectId().isBlank()
                ? List.of(request.statusEffectId())
                : List.of();
        EchoStandaloneHazardTransitionResult result = new EchoStandaloneHazardTransitionResult(
                request.playerId(),
                request.previousHazardId(),
                request.currentHazardId(),
                eventType,
                entered,
                exited,
                statusEffects,
                request.gameTick(),
                request.sourceReason()
        );
        lastHazardTransitions.put(request.playerId(), result);
        if (hasCurrent) {
            activeHazardIds.put(request.playerId(), request.currentHazardId());
        } else {
            activeHazardIds.remove(request.playerId());
        }
        return result;
    }

    public EchoStandaloneHazardTransitionResult lastHazardTransition(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return lastHazardTransitions.get(playerId);
    }

    public String activeRegionId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return activeRegionIds.getOrDefault(playerId, "");
    }

    public String activeHazardId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return activeHazardIds.getOrDefault(playerId, "");
    }

    public List<String> startedRegionMissions(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("player id must not be blank");
        }
        return startedRegionMissions.getOrDefault(playerId, List.of());
    }

    public Map<String, String> activeRegionIds() {
        return Map.copyOf(activeRegionIds);
    }

    public Map<String, String> activeHazardIds() {
        return Map.copyOf(activeHazardIds);
    }

    public EchoStandaloneWeatherScheduleResult scheduleWeather(EchoStandaloneWeatherScheduleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather schedule request must not be null");
        }
        long warningTicks = Math.max(request.profile().warningTicks(), request.minimumWarningTicks());
        long warningStart = request.currentTick();
        long start = warningStart + warningTicks;
        long end = start + request.profile().durationTicks();
        boolean scheduled = request.profile().enabled()
                && request.profile().durationTicks() > 0
                && request.profile().weight() > 0;
        EchoStandaloneWeatherScheduleResult result = new EchoStandaloneWeatherScheduleResult(
                request.profile().id(),
                request.profile().type(),
                request.profile().severity(),
                request.profile().scope(),
                scheduled ? "FORECAST" : "SKIPPED",
                warningStart,
                start,
                end,
                request.centerX(),
                request.centerY(),
                request.centerZ(),
                request.radius(),
                request.sourceReason(),
                scheduled
        );
        if (scheduled) {
            activeWeatherSchedules.put(request.profile().id(), result);
        }
        return result;
    }

    public EchoStandaloneWeatherScheduleResult activeWeatherSchedule(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("weather profile id must not be blank");
        }
        return activeWeatherSchedules.get(profileId);
    }

    public Map<String, EchoStandaloneWeatherScheduleResult> activeWeatherSchedules() {
        return Map.copyOf(activeWeatherSchedules);
    }

    public EchoStandaloneWeatherStateApplyResult restoreWeatherSchedule(
            EchoStandaloneWeatherScheduleRestoreRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather schedule restore request must not be null");
        }
        activeWeatherSchedules.put(request.schedule().profileId(), request.schedule());
        return applyWeatherState(new EchoStandaloneWeatherStateApplyRequest(
                request.eventId(),
                request.regionId(),
                request.schedule().phase(),
                request.gameTick(),
                request.sourceReason(),
                request.weather(),
                request.atmosphere()));
    }

    public EchoStandaloneWeatherExposureMitigationResult mitigateWeatherExposure(
            EchoStandaloneWeatherExposureMitigationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather exposure mitigation request must not be null");
        }
        double filterDrain = request.weatherModifier().filterDrainMultiplier();
        double radiationExposure = request.weatherModifier().radiationExposureMultiplier();
        double toxicExposure = request.weatherModifier().toxicExposureMultiplier();
        double coldExposure = request.weatherModifier().coldExposureMultiplier();
        double heatExposure = request.weatherModifier().heatExposureMultiplier();
        double routeRisk = request.weatherModifier().routeRiskModifier();
        if (request.sheltered()) {
            filterDrain *= request.countermeasureModifier().filterDrainMultiplier();
            radiationExposure *= request.countermeasureModifier().radiationExposureMultiplier();
            toxicExposure *= request.countermeasureModifier().toxicExposureMultiplier();
            coldExposure *= request.countermeasureModifier().coldExposureMultiplier();
            heatExposure *= request.countermeasureModifier().heatExposureMultiplier();
            routeRisk *= request.countermeasureModifier().routeRiskModifier();
        }
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("moduleId", "echoweathercore");
        state.put("filterDrainMultiplier", filterDrain);
        state.put("radiationExposureMultiplier", radiationExposure);
        state.put("toxicExposureMultiplier", toxicExposure);
        state.put("coldExposureMultiplier", coldExposure);
        state.put("heatExposureMultiplier", heatExposure);
        state.put("routeRiskModifier", routeRisk);
        EchoStandaloneWeatherExposureMitigationResult result = new EchoStandaloneWeatherExposureMitigationResult(
                request.playerId(),
                request.weatherId(),
                request.weatherType(),
                request.sheltered(),
                state,
                request.gameTick(),
                request.sourceReason(),
                request.sheltered());
        lastWeatherExposureMitigations.put(weatherExposureKey(result.playerId(), result.weatherId()), result);
        return result;
    }

    public EchoStandaloneWeatherExposureMitigationResult lastWeatherExposureMitigation(
            String playerId,
            String weatherId) {
        if (playerId == null || playerId.isBlank() || weatherId == null || weatherId.isBlank()) {
            throw new IllegalArgumentException("weather exposure player and weather ids must not be blank");
        }
        return lastWeatherExposureMitigations.get(weatherExposureKey(playerId, weatherId));
    }

    public EchoStandaloneShelterReport reportShelter(EchoStandaloneShelterReportRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("shelter report request must not be null");
        }
        EchoStandaloneShelterReport report = new EchoStandaloneShelterReport(
                request.playerId(),
                request.x(),
                request.y(),
                request.z(),
                request.gameTick(),
                request.sourceReason(),
                true);
        shelterReports.put(request.playerId(), report);
        return report;
    }

    public EchoStandaloneShelterReport lastShelterReport(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("shelter report player id must not be blank");
        }
        return shelterReports.get(playerId);
    }

    public String routeWeatherRisk(String severity, double routeRiskModifier) {
        return evaluateWeatherRouteRisk(new EchoStandaloneWeatherRouteRiskRequest(
                "",
                "",
                severity,
                routeRiskModifier,
                0L,
                "standalone-weather-route-risk")).risk();
    }

    public String worldCoreHazardIdForWeatherType(String weatherType) {
        return switch (text(weatherType).toUpperCase(java.util.Locale.ROOT)) {
            case "ASH_STORM", "TOXIC_RAIN", "SPORE_BLOOM", "HEAT_SURGE" -> "echoworldcore:hazard/toxic_air";
            case "RADIATION_STORM" -> "echoworldcore:hazard/radiation";
            case "CRYO_FRONT" -> "echoworldcore:hazard/cryo_cold";
            case "NEXUS_SIGNAL_STORM", "ELECTROMAGNETIC_BLACKOUT", "STATIC_FOG", "MEMORY_RAIN" ->
                    "echoworldcore:hazard/nexus_anomaly";
            case "ORBITAL_DEBRIS_SHOWER" -> "echoworldcore:hazard/orbital_exposure";
            default -> "echoworldcore:hazard/secure_zone";
        };
    }

    public EchoStandaloneHazardTickDamageResult applyWorldCoreWeatherHazardTick(
            String playerId,
            String weatherType,
            double healthBefore,
            int severity,
            long gameTick,
            EchoStandaloneDifficulty difficulty) {
        String hazardId = worldCoreHazardIdForWeatherType(weatherType);
        int defaultSeverity = worldCoreWeatherHazardDefaultSeverity(hazardId);
        int effectiveSeverity = Math.max(defaultSeverity, Math.max(0, severity));
        double baseDamage = effectiveSeverity <= 0 ? 0.0D : Math.max(0.5D, effectiveSeverity / 50.0D);
        String hazardPath = hazardId.substring(hazardId.indexOf(':') + 1);
        String hazardType = hazardId.substring(hazardId.lastIndexOf('/') + 1);
        return applyHazardTickDamage(new EchoStandaloneHazardTickDamageRequest(
                playerId,
                healthBefore,
                effectiveSeverity,
                gameTick,
                "standalone-weather-worldcore-hazard-tick",
                new EchoStandaloneHazard(
                        hazardId,
                        hazardType,
                        0,
                        0,
                        0,
                        baseDamage,
                        "echostatuscore:status/" + hazardPath),
                difficulty == null ? new EchoStandaloneDifficulty("echodifficultycore:normal", 1.0D, 1.0D) : difficulty));
    }

    public int worldCoreWeatherHazardDefaultSeverity(String hazardId) {
        return switch (text(hazardId)) {
            case "echoworldcore:hazard/toxic_air" -> 55;
            case "echoworldcore:hazard/radiation" -> 70;
            case "echoworldcore:hazard/cryo_cold" -> 60;
            case "echoworldcore:hazard/nexus_anomaly" -> 85;
            case "echoworldcore:hazard/orbital_exposure" -> 75;
            default -> 0;
        };
    }

    public EchoStandaloneWeatherRouteRiskResult evaluateWeatherRouteRisk(
            EchoStandaloneWeatherRouteRiskRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather route risk request must not be null");
        }
        double baseScore = switch (text(request.severity()).toUpperCase(java.util.Locale.ROOT)) {
            case "LOW" -> 0.0D;
            case "MODERATE" -> 1.0D;
            case "SEVERE" -> 2.0D;
            case "EXTREME" -> 3.0D;
            default -> 0.0D;
        };
        double score = baseScore * Math.max(0.0D, request.routeRiskModifier());
        EchoStandaloneWeatherRouteRiskResult result = new EchoStandaloneWeatherRouteRiskResult(
                request.playerId(),
                request.weatherId(),
                text(request.severity()).toUpperCase(java.util.Locale.ROOT),
                request.routeRiskModifier(),
                score,
                riskForScore(score),
                request.gameTick(),
                request.sourceReason());
        lastWeatherRouteRiskResults.put(weatherRouteRiskKey(result.playerId(), result.weatherId()), result);
        return result;
    }

    public EchoStandaloneWeatherRouteRiskResult lastWeatherRouteRisk(String playerId, String weatherId) {
        return lastWeatherRouteRiskResults.get(weatherRouteRiskKey(playerId, weatherId));
    }

    public EchoStandaloneRouteWarningPostUseResult useRouteWarningPost(EchoStandaloneRouteWarningPostUseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("route warning post request must not be null");
        }
        String risk = text(request.risk()).toUpperCase(java.util.Locale.ROOT);
        String severity = text(request.severity()).toUpperCase(java.util.Locale.ROOT);
        String message = "Route Warning Post: Risk is " + risk;
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("routeWarning", message);
        hudState.put("risk", risk);
        hudState.put("severity", severity);
        hudState.put("weatherId", request.weatherId());
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", "echoweathercore:route_warning/" + risk.toLowerCase(java.util.Locale.ROOT));
        audioState.put("risk", risk);
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("warningPostOverlay", "echoweathercore:route_warning_post/"
                + risk.toLowerCase(java.util.Locale.ROOT));
        renderState.put("severity", severity);
        renderState.put("risk", risk);
        renderState.put("routeRiskModifier", request.routeRiskModifier());
        EchoStandaloneRouteWarningPostUseResult result = new EchoStandaloneRouteWarningPostUseResult(
                request.playerId(),
                request.weatherId(),
                severity,
                risk,
                request.routeRiskModifier(),
                request.x(),
                request.y(),
                request.z(),
                message,
                hudState,
                audioState,
                renderState,
                Math.max(0L, request.gameTick()),
                request.sourceReason(),
                !text(request.playerId()).isBlank());
        lastRouteWarningPostUses.put(result.playerId(), result);
        routeWarningPostUses.put(routeWarningPostKey(result.playerId(), result.x(), result.y(), result.z()), result);
        return result;
    }

    public EchoStandaloneRouteWarningPostUseResult lastRouteWarningPostUse(String playerId) {
        return lastRouteWarningPostUses.get(text(playerId));
    }

    public Map<String, EchoStandaloneRouteWarningPostUseResult> routeWarningPostUses() {
        return Map.copyOf(routeWarningPostUses);
    }

    public EchoStandaloneWeatherForecastResult forecastWeather(EchoStandaloneWeatherForecastRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather forecast request must not be null");
        }
        double riskScore = switch (text(request.severity()).toUpperCase(java.util.Locale.ROOT)) {
            case "LOW" -> 0.0D;
            case "MODERATE" -> 1.0D;
            case "SEVERE" -> 2.0D;
            case "EXTREME" -> 3.0D;
            default -> 0.0D;
        } * Math.max(0.0D, request.routeRiskModifier());
        EchoStandaloneWeatherForecastResult result = new EchoStandaloneWeatherForecastResult(
                request.playerId(),
                request.eventId(),
                request.weatherId(),
                text(request.weatherType()).toUpperCase(java.util.Locale.ROOT),
                request.displayName(),
                text(request.phase()).toUpperCase(java.util.Locale.ROOT),
                text(request.severity()).toUpperCase(java.util.Locale.ROOT),
                request.etaTicks(),
                text(request.regionId()).isBlank() ? "Unknown" : request.regionId(),
                (int) Math.max(0L, request.endTick() - request.startTick()),
                request.recommendedGear(),
                request.shelterRecommendation(),
                riskForScore(riskScore),
                request.routeRiskModifier(),
                Math.round(request.scannerReliabilityMultiplier() * 100.0D) + "%",
                request.echoLines(),
                request.gameTick(),
                request.sourceReason(),
                true);
        lastWeatherForecasts.put(weatherRouteRiskKey(result.playerId(), result.weatherId()), result);
        return result;
    }

    public EchoStandaloneWeatherForecastResult lastWeatherForecast(String playerId, String weatherId) {
        return lastWeatherForecasts.get(weatherRouteRiskKey(playerId, weatherId));
    }

    public Map<String, EchoStandaloneWeatherForecastResult> lastWeatherForecasts() {
        return Map.copyOf(lastWeatherForecasts);
    }

    public EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult useWeatherRadio(
            EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseRequest request
    ) {
        if (request == null) {
            throw new IllegalArgumentException("weather radio request must not be null");
        }
        String severity = text(request.strongestSeverity()).toUpperCase(java.util.Locale.ROOT);
        String routeRisk = text(request.routeRisk()).toUpperCase(java.util.Locale.ROOT);
        List<String> messageLines = new ArrayList<>();
        if (request.forecastsAvailable()) {
            messageLines.add("Weather Radio - Regional Forecast:");
            messageLines.addAll(request.forecastLines());
        } else {
            messageLines.add("Weather Radio: No regional weather events.");
        }
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("weatherRadio", messageLines.get(0));
        hudState.put("forecastCount", request.forecastLines().size());
        hudState.put("routeRisk", routeRisk);
        hudState.put("strongestSeverity", severity);
        hudState.put("weatherIds", request.weatherIds());
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", request.forecastsAvailable()
                ? "echoweathercore:weather_radio/forecast"
                : "echoweathercore:weather_radio/clear");
        audioState.put("forecastCount", request.forecastLines().size());
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("readout", request.forecastsAvailable()
                ? "echoweathercore:weather_radio/forecast"
                : "echoweathercore:weather_radio/clear");
        renderState.put("routeRisk", routeRisk);
        renderState.put("strongestSeverity", severity);
        EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult result =
                new EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult(
                request.playerId(),
                request.weatherIds(),
                request.forecastLines(),
                request.forecastsAvailable(),
                severity,
                routeRisk,
                Math.max(0, request.cooldownTicks()),
                messageLines,
                hudState,
                audioState,
                renderState,
                Math.max(0L, request.gameTick()),
                request.sourceReason(),
                !text(request.playerId()).isBlank());
        lastWeatherRadioUses.put(result.playerId(), result);
        weatherRadioPlayers.put(result.playerId(), result);
        return result;
    }

    public EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult lastWeatherRadioUse(String playerId) {
        return lastWeatherRadioUses.get(text(playerId));
    }

    public Map<String, EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult> weatherRadioPlayers() {
        return Map.copyOf(weatherRadioPlayers);
    }

    public EchoStandaloneWeatherStationUseResult useWeatherStation(EchoStandaloneWeatherStationUseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather station request must not be null");
        }
        String severity = text(request.strongestSeverity()).toUpperCase(java.util.Locale.ROOT);
        String routeRisk = text(request.routeRisk()).toUpperCase(java.util.Locale.ROOT);
        List<String> messageLines = new ArrayList<>();
        if (request.forecastsAvailable()) {
            messageLines.add("=== Weather Station Forecast ===");
            messageLines.addAll(request.forecastLines());
        } else {
            messageLines.add("Weather Station: No active or forecasted weather.");
        }
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("weatherStation", messageLines.get(0));
        hudState.put("forecastCount", request.forecastLines().size());
        hudState.put("routeRisk", routeRisk);
        hudState.put("strongestSeverity", severity);
        hudState.put("weatherIds", request.weatherIds());
        hudState.put("stationPosition", List.of(request.x(), request.y(), request.z()));
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", request.forecastsAvailable()
                ? "echoweathercore:weather_station/forecast"
                : "echoweathercore:weather_station/clear");
        audioState.put("forecastCount", request.forecastLines().size());
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("readout", request.forecastsAvailable()
                ? "echoweathercore:weather_station/forecast"
                : "echoweathercore:weather_station/clear");
        renderState.put("routeRisk", routeRisk);
        renderState.put("strongestSeverity", severity);
        renderState.put("stationPosition", List.of(request.x(), request.y(), request.z()));
        EchoStandaloneWeatherStationUseResult result = new EchoStandaloneWeatherStationUseResult(
                request.playerId(),
                request.weatherIds(),
                request.forecastLines(),
                request.forecastsAvailable(),
                severity,
                routeRisk,
                request.x(),
                request.y(),
                request.z(),
                messageLines,
                hudState,
                audioState,
                renderState,
                Math.max(0L, request.gameTick()),
                request.sourceReason(),
                !text(request.playerId()).isBlank());
        lastWeatherStationUses.put(result.playerId(), result);
        weatherStationPositions.put(weatherStationKey(result.playerId(), result.x(), result.y(), result.z()), result);
        return result;
    }

    public EchoStandaloneWeatherStationUseResult lastWeatherStationUse(String playerId) {
        return lastWeatherStationUses.get(text(playerId));
    }

    public Map<String, EchoStandaloneWeatherStationUseResult> weatherStationPositions() {
        return Map.copyOf(weatherStationPositions);
    }

    public EchoStandaloneWeatherWarningResult issueWeatherWarning(EchoStandaloneWeatherWarningRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather warning request must not be null");
        }
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("weatherWarning", request.message());
        hudState.put("weatherId", request.weatherId());
        hudState.put("phase", text(request.phase()).toUpperCase(java.util.Locale.ROOT));
        hudState.put("channel", request.channel());
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", "echoweathercore:warning/" + idPath(request.weatherId()));
        audioState.put("phase", text(request.phase()).toUpperCase(java.util.Locale.ROOT));
        audioState.put("channel", request.channel());
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("warningOverlay", "echoweathercore:overlay/" + idPath(request.weatherId()));
        renderState.put("severityPulse", text(request.phase()).equals("ACTIVE") || text(request.phase()).equals("CRITICAL"));
        renderState.put("recipientCount", request.recipientPlayerIds().size());
        EchoStandaloneWeatherWarningResult result = new EchoStandaloneWeatherWarningResult(
                request.eventId(),
                request.weatherId(),
                text(request.regionId()),
                text(request.phase()).toUpperCase(java.util.Locale.ROOT),
                request.channel(),
                request.message(),
                request.recipientPlayerIds(),
                request.recipientPlayerIds().size(),
                hudState,
                audioState,
                renderState,
                Math.max(0L, request.gameTick()),
                request.sourceReason(),
                !request.recipientPlayerIds().isEmpty());
        lastWeatherWarnings.put(result.eventId(), result);
        for (String playerId : result.recipientPlayerIds()) {
            Map<String, EchoStandaloneWeatherWarningResult> warnings = new LinkedHashMap<>(
                    playerWeatherWarnings.getOrDefault(playerId, Map.of()));
            warnings.put(result.eventId(), result);
            playerWeatherWarnings.put(playerId, Map.copyOf(warnings));
        }
        return result;
    }

    public EchoStandaloneWeatherWarningResult lastWeatherWarning(String eventId) {
        return lastWeatherWarnings.get(text(eventId));
    }

    public EchoStandaloneWeatherWarningResult lastWeatherWarning(String playerId, String eventId) {
        return playerWeatherWarnings.getOrDefault(text(playerId), Map.of()).get(text(eventId));
    }

    public Map<String, EchoStandaloneWeatherWarningResult> lastWeatherWarnings() {
        return Map.copyOf(lastWeatherWarnings);
    }

    public EchoStandaloneEmergencySirenUseResult useEmergencySiren(EchoStandaloneEmergencySirenUseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("emergency siren request must not be null");
        }
        String phase = text(request.phase()).toUpperCase(java.util.Locale.ROOT);
        String severity = text(request.severity()).toUpperCase(java.util.Locale.ROOT);
        String message = "Emergency Siren: "
                + (request.activeWeatherDetected() ? "ACTIVE WEATHER DETECTED" : "All clear.");
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("emergencySiren", message);
        hudState.put("activeWeatherDetected", request.activeWeatherDetected());
        hudState.put("phase", phase);
        hudState.put("severity", severity);
        hudState.put("weatherIds", request.weatherIds());
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", request.activeWeatherDetected()
                ? "echoweathercore:siren/active_weather"
                : "echoweathercore:siren/all_clear");
        audioState.put("activeWeatherDetected", request.activeWeatherDetected());
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("warningLight", request.activeWeatherDetected() ? "RED_PULSE" : "GREEN_STEADY");
        renderState.put("overlay", request.activeWeatherDetected()
                ? "echoweathercore:emergency_siren/active"
                : "echoweathercore:emergency_siren/clear");
        renderState.put("severity", severity);
        EchoStandaloneEmergencySirenUseResult result = new EchoStandaloneEmergencySirenUseResult(
                request.playerId(),
                request.weatherIds(),
                request.activeWeatherDetected(),
                phase,
                severity,
                request.x(),
                request.y(),
                request.z(),
                message,
                hudState,
                audioState,
                renderState,
                Math.max(0L, request.gameTick()),
                request.sourceReason(),
                !text(request.playerId()).isBlank());
        lastEmergencySirenUses.put(result.playerId(), result);
        emergencySirenPosts.put(emergencySirenKey(result.playerId(), result.x(), result.y(), result.z()), result);
        return result;
    }

    public EchoStandaloneEmergencySirenUseResult lastEmergencySirenUse(String playerId) {
        return lastEmergencySirenUses.get(text(playerId));
    }

    public Map<String, EchoStandaloneEmergencySirenUseResult> emergencySirenPosts() {
        return Map.copyOf(emergencySirenPosts);
    }

    public EchoStandaloneClimateSensorReadResult readClimateSensor(EchoStandaloneClimateSensorReadRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("climate sensor request must not be null");
        }
        int visibilityPercent = percent(request.visibilityMultiplier());
        int scannerReliabilityPercent = percent(request.scannerReliabilityMultiplier());
        List<String> messageLines = List.of(
                "Climate Sensor Reading:",
                "Sheltered: " + request.sheltered(),
                "Visibility: " + visibilityPercent + "%",
                "Scanner Reliability: " + scannerReliabilityPercent + "%");
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("climateSensor", "Visibility " + visibilityPercent + "% / Scanner "
                + scannerReliabilityPercent + "%");
        hudState.put("sheltered", request.sheltered());
        hudState.put("visibilityPercent", visibilityPercent);
        hudState.put("scannerReliabilityPercent", scannerReliabilityPercent);
        hudState.put("weatherIds", request.weatherIds());
        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", request.sheltered()
                ? "echoweathercore:climate_sensor/sheltered"
                : "echoweathercore:climate_sensor/exposed");
        audioState.put("sheltered", request.sheltered());
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("readout", "echoweathercore:climate_sensor/readout");
        renderState.put("visibilityPercent", visibilityPercent);
        renderState.put("scannerReliabilityPercent", scannerReliabilityPercent);
        renderState.put("routeRiskModifier", request.routeRiskModifier());
        EchoStandaloneClimateSensorReadResult result = new EchoStandaloneClimateSensorReadResult(
                request.playerId(),
                request.weatherIds(),
                request.sheltered(),
                visibilityPercent,
                scannerReliabilityPercent,
                request.filterDrainMultiplier(),
                request.toxicExposureMultiplier(),
                request.routeRiskModifier(),
                request.x(),
                request.y(),
                request.z(),
                messageLines,
                hudState,
                audioState,
                renderState,
                Math.max(0L, request.gameTick()),
                request.sourceReason(),
                !text(request.playerId()).isBlank());
        lastClimateSensorReadings.put(result.playerId(), result);
        climateSensorPositions.put(climateSensorKey(result.playerId(), result.x(), result.y(), result.z()), result);
        return result;
    }

    public EchoStandaloneClimateSensorReadResult lastClimateSensorReading(String playerId) {
        return lastClimateSensorReadings.get(text(playerId));
    }

    public Map<String, EchoStandaloneClimateSensorReadResult> climateSensorPositions() {
        return Map.copyOf(climateSensorPositions);
    }

    public EchoStandaloneWeatherStateApplyResult tickScheduledWeather(EchoStandaloneWeatherScheduleTickRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather schedule tick request must not be null");
        }
        EchoStandaloneWeatherScheduleTickResult tick = tickWeatherScheduleState(request);
        EchoStandaloneWeather weather = request.weather();
        EchoStandaloneAtmosphere atmosphere = request.atmosphere();
        if (tick.ended()) {
            weather = new EchoStandaloneWeather(
                    request.weather().id(),
                    weatherDisplayName(request.weather().hudLine()) + ": CLEAR",
                    "echoweathercore:event.clear",
                    "echorendercore:weather/clear");
            atmosphere = new EchoStandaloneAtmosphere(
                    "echoatmospherecore:clear_field",
                    1.0D,
                    "minecraft:empty",
                    "weather_phase:ENDED");
        } else {
            atmosphere = new EchoStandaloneAtmosphere(
                    request.atmosphere().id(),
                    request.atmosphere().visibility(),
                    request.atmosphere().particleProfile(),
                    "weather_phase:" + tick.phase());
        }
        return applyWeatherState(new EchoStandaloneWeatherStateApplyRequest(
                request.eventId(),
                request.regionId(),
                tick.phase(),
                request.gameTick(),
                request.sourceReason(),
                weather,
                atmosphere));
    }

    public EchoStandaloneWeatherScheduleTickResult tickWeatherScheduleState(
            EchoStandaloneWeatherScheduleTickRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather schedule tick request must not be null");
        }
        EchoStandaloneWeatherScheduleResult schedule = activeWeatherSchedules.get(request.weather().id());
        if (schedule == null) {
            throw new IllegalStateException("no active weather schedule for " + request.weather().id());
        }
        String phase = weatherPhase(schedule, request.gameTick());
        boolean ended = "ENDED".equals(phase);
        EchoStandaloneWeatherScheduleTickResult result = new EchoStandaloneWeatherScheduleTickResult(
                request.eventId(),
                schedule.profileId(),
                schedule.type(),
                schedule.severity(),
                schedule.scope(),
                schedule.phase(),
                phase,
                request.gameTick(),
                schedule.warningStartTick(),
                schedule.startTick(),
                schedule.endTick(),
                schedule.centerX(),
                schedule.centerY(),
                schedule.centerZ(),
                schedule.radius(),
                schedule.sourceReason(),
                schedule.scheduled() && !ended,
                ended,
                !schedule.phase().equals(phase));
        lastWeatherScheduleTicks.put(request.eventId(), result);
        if (ended) {
            activeWeatherSchedules.remove(request.weather().id());
        } else {
            activeWeatherSchedules.put(request.weather().id(), scheduleWithPhase(schedule, phase));
        }
        return result;
    }

    public EchoStandaloneWeatherStateApplyResult applyWeatherState(EchoStandaloneWeatherStateApplyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("weather state apply request must not be null");
        }
        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("weather", request.weather().hudLine());
        hudState.put("phase", request.phase());
        hudState.put("eventId", request.eventId());

        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", request.weather().audioCue());
        audioState.put("region", request.regionId());
        audioState.put("phase", request.phase());

        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("weatherProfile", request.weather().renderProfile());
        renderState.put("atmosphere", request.atmosphere().id());
        renderState.put("visibility", request.atmosphere().visibility());
        renderState.put("particles", request.atmosphere().particleProfile());
        renderState.put("skyFog", request.atmosphere().skyFog());

        EchoStandaloneWeatherStateApplyResult result = new EchoStandaloneWeatherStateApplyResult(
                request.eventId(),
                request.weather().id(),
                request.regionId(),
                request.phase(),
                hudState,
                audioState,
                renderState,
                request.gameTick(),
                request.sourceReason(),
                true
        );
        if ("ENDED".equals(request.phase())) {
            activeWeatherSchedules.remove(request.weather().id());
        }
        EchoStandaloneAtmosphereStateApplyResult atmosphereResult = applyAtmosphereState(
                new EchoStandaloneAtmosphereStateApplyRequest(
                        request.eventId(),
                        request.weather().id(),
                        request.regionId(),
                        request.phase(),
                        request.gameTick(),
                        request.sourceReason(),
                        request.atmosphere()));
        lastWeatherStateApplications.put(request.eventId(), result);
        lastWeatherSurfaceStates.put(request.regionId(), result);
        lastAtmosphereStateApplications.put(request.eventId(), atmosphereResult);
        lastAtmosphereSurfaceStates.put(request.regionId(), atmosphereResult);
        return result;
    }

    public EchoStandaloneAtmosphereStateApplyResult applyAtmosphereState(
            EchoStandaloneAtmosphereStateApplyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("atmosphere state apply request must not be null");
        }
        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("atmosphere", request.atmosphere().id());
        renderState.put("visibility", request.atmosphere().visibility());
        renderState.put("particles", request.atmosphere().particleProfile());
        renderState.put("skyFog", request.atmosphere().skyFog());
        renderState.put("phase", request.phase());

        Map<String, Object> runtimeBindings = new LinkedHashMap<>();
        runtimeBindings.put("moduleId", "echoatmospherecore");
        runtimeBindings.put("render.visibility", request.atmosphere().visibility());
        runtimeBindings.put("render.particles", request.atmosphere().particleProfile());
        runtimeBindings.put("render.skyFog", request.atmosphere().skyFog());
        runtimeBindings.put("weatherId", request.weatherId());

        return new EchoStandaloneAtmosphereStateApplyResult(
                request.eventId(),
                request.weatherId(),
                request.regionId(),
                request.phase(),
                renderState,
                runtimeBindings,
                request.gameTick(),
                request.sourceReason(),
                true);
    }

    public EchoStandaloneAtmosphereRuntimeProfileResult materializeAtmosphereRuntimeProfile(
            EchoStandaloneAtmosphereRuntimeProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("atmosphere runtime profile request must not be null");
        }
        Map<String, Object> stormVisibility = new LinkedHashMap<>();
        stormVisibility.put("visibilityId", "echoatmospherecore:storm_visibility/ashfall_active");
        stormVisibility.put("clearVisibility", request.clearVisibility());
        stormVisibility.put("stormVisibility", request.stormVisibility());
        stormVisibility.put("screenHazeIntensity", request.screenHazeIntensity());
        stormVisibility.put("reducesDistantLights", request.reducesDistantLights());

        Map<String, Object> fogProfile = new LinkedHashMap<>();
        fogProfile.put("fogId", request.fogId());
        fogProfile.put("colorArgb", request.fogColorArgb());
        fogProfile.put("density", request.fogDensity());
        fogProfile.put("startDistance", request.fogStartDistance());
        fogProfile.put("endDistance", request.fogEndDistance());
        fogProfile.put("stormAffected", request.stormAffected());

        Map<String, Object> skyTint = new LinkedHashMap<>();
        skyTint.put("skyTintId", request.skyTintId());
        skyTint.put("dayColorArgb", request.dayColorArgb());
        skyTint.put("nightColorArgb", request.nightColorArgb());
        skyTint.put("stormColorArgb", request.stormColorArgb());
        skyTint.put("celestialVisibility", request.celestialVisibility());

        Map<String, Object> ambientParticles = new LinkedHashMap<>();
        ambientParticles.put("particleProfileId", request.particleProfileId());
        ambientParticles.put("particleReferences", request.particleReferences());
        ambientParticles.put("density", request.particleDensity());
        ambientParticles.put("affectedByStormVisibility", request.affectedByStormVisibility());

        Map<String, Object> hookRefs = new LinkedHashMap<>();
        hookRefs.put("renderCoreHookReference", request.renderCoreHookReference());
        hookRefs.put("soundCoreHookReference", request.soundCoreHookReference());
        hookRefs.put("weatherProfileReference", request.weatherProfileReference());
        hookRefs.put("runtimePacketConsumer", request.runtimePacketConsumer());

        List<Map<String, String>> runtimeBindings = List.of(
                binding("render.visibility", "stormVisibility", "echorendercore:visibility/fog_distance"),
                binding("render.sky", "skyTint", "echorendercore:sky/tint"),
                binding("render.particles", "ambientParticles", "echorendercore:particles/ashfall"),
                binding("sound.ambience", "hookRefs", "echosoundcore:ambience/ash_storm"));
        List<String> diagnostics = List.of(
                "atmosphere.profile.loaded",
                "atmosphere.visibility.resolved",
                "atmosphere.fog_sky.bound",
                "atmosphere.particles.bound");

        Map<String, Object> runtimeProfileState = new LinkedHashMap<>();
        runtimeProfileState.put("adapterCoreContract", "echoatmospherecore:atmosphere/runtime_profile_tick");
        runtimeProfileState.put("service", "echoatmospherecore:atmosphere_service");
        runtimeProfileState.put("atmosphereProfileTickExecuted", true);
        runtimeProfileState.put("adapterCoreBridge", true);
        runtimeProfileState.put("nativeLoaderBackend", true);
        runtimeProfileState.put("moduleId", "echoatmospherecore");
        runtimeProfileState.put("packId", request.packId().isBlank() ? "unknown" : request.packId());
        runtimeProfileState.put("profileId", request.profileId());
        runtimeProfileState.put("weatherStateId", request.weatherStateId());
        runtimeProfileState.put("biomeAmbienceId", request.biomeAmbienceId());
        runtimeProfileState.put("stormVisibility", stormVisibility);
        runtimeProfileState.put("fogProfile", fogProfile);
        runtimeProfileState.put("skyTint", skyTint);
        runtimeProfileState.put("ambientParticles", ambientParticles);
        runtimeProfileState.put("hookRefs", hookRefs);
        runtimeProfileState.put("runtimeBindings", runtimeBindings);
        runtimeProfileState.put("diagnostics", diagnostics);
        runtimeProfileState.put("referenceBehavior", "atmospherecore_resolves_runtime_profile_tick");

        EchoStandaloneAtmosphereRuntimeProfileResult result = new EchoStandaloneAtmosphereRuntimeProfileResult(
                request.packId().isBlank() ? "unknown" : request.packId(),
                request.profileId(),
                request.weatherStateId(),
                request.biomeAmbienceId(),
                stormVisibility,
                fogProfile,
                skyTint,
                ambientParticles,
                hookRefs,
                runtimeBindings,
                diagnostics,
                runtimeProfileState,
                request.gameTick(),
                request.sourceReason(),
                true);
        lastAtmosphereRuntimeProfiles.put(result.profileId(), result);
        weatherAtmosphereRuntimeProfiles.put(result.weatherStateId(), result);
        return result;
    }

    public EchoStandaloneWeatherStateApplyResult lastWeatherStateApplication(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("weather event id must not be blank");
        }
        return lastWeatherStateApplications.get(eventId);
    }

    public EchoStandaloneWeatherStateApplyResult lastWeatherSurfaceState(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("weather region id must not be blank");
        }
        return lastWeatherSurfaceStates.get(regionId);
    }

    public Map<String, EchoStandaloneWeatherStateApplyResult> lastWeatherSurfaceStates() {
        return Map.copyOf(lastWeatherSurfaceStates);
    }

    public EchoStandaloneWeatherScheduleTickResult lastWeatherScheduleTick(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("weather schedule tick event id must not be blank");
        }
        return lastWeatherScheduleTicks.get(eventId);
    }

    public EchoStandaloneAtmosphereStateApplyResult lastAtmosphereStateApplication(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("atmosphere event id must not be blank");
        }
        return lastAtmosphereStateApplications.get(eventId);
    }

    public EchoStandaloneAtmosphereStateApplyResult lastAtmosphereSurfaceState(String regionId) {
        if (regionId == null || regionId.isBlank()) {
            throw new IllegalArgumentException("atmosphere region id must not be blank");
        }
        return lastAtmosphereSurfaceStates.get(regionId);
    }

    public Map<String, EchoStandaloneAtmosphereStateApplyResult> lastAtmosphereSurfaceStates() {
        return Map.copyOf(lastAtmosphereSurfaceStates);
    }

    public EchoStandaloneAtmosphereRuntimeProfileResult lastAtmosphereRuntimeProfile(String profileId) {
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("atmosphere runtime profile id must not be blank");
        }
        return lastAtmosphereRuntimeProfiles.get(profileId);
    }

    public EchoStandaloneAtmosphereRuntimeProfileResult weatherAtmosphereRuntimeProfile(String weatherStateId) {
        if (weatherStateId == null || weatherStateId.isBlank()) {
            throw new IllegalArgumentException("atmosphere runtime weather state id must not be blank");
        }
        return weatherAtmosphereRuntimeProfiles.get(weatherStateId);
    }

    public EchoStandaloneWorldEffectResult apply(EchoStandaloneWorldEffectTick tick) {
        if (tick == null) {
            throw new IllegalArgumentException("world effect tick must not be null");
        }
        boolean inRegion = tick.region().contains(tick.x(), tick.z());
        boolean inHazard = tick.hazard().affects(tick.x(), tick.z());
        String activeRegion = inRegion ? tick.region().id() : "";
        String activeHazard = inHazard ? tick.hazard().id() : "";
        EchoStandaloneHazardTickDamageResult damageResult = applyHazardTickDamage(new EchoStandaloneHazardTickDamageRequest(
                tick.playerId(),
                tick.health(),
                Math.max(0, (int) Math.round(tick.hazard().damagePerTick() * 50.0D)),
                6000L,
                "standalone-world-effect",
                tick.hazard(),
                tick.difficulty()
        ));
        double damage = inHazard ? damageResult.damageApplied() : 0.0D;
        double healthAfter = inHazard ? damageResult.healthAfter() : tick.health();

        Map<String, Object> hudState = new LinkedHashMap<>();
        hudState.put("weather", tick.weather().hudLine());
        hudState.put("hazard", activeHazard);
        hudState.put("damageApplied", damage);

        Map<String, Object> audioState = new LinkedHashMap<>();
        audioState.put("cue", tick.weather().audioCue());
        audioState.put("region", activeRegion);

        Map<String, Object> renderState = new LinkedHashMap<>();
        renderState.put("weatherProfile", tick.weather().renderProfile());
        renderState.put("atmosphere", tick.atmosphere().id());
        renderState.put("visibility", tick.atmosphere().visibility());
        renderState.put("particles", tick.atmosphere().particleProfile());
        renderState.put("skyFog", tick.atmosphere().skyFog());

        Map<String, Object> worldLookup = new LinkedHashMap<>();
        worldLookup.put("biomeProfile", tick.biome().id());
        worldLookup.put("biomeTag", tick.biome().biomeTag());
        worldLookup.put("structureId", tick.structure().id());
        worldLookup.put("poiId", tick.structure().poiId());
        worldLookup.put("poiPosition", List.of(tick.structure().x(), tick.structure().y(), tick.structure().z()));

        Map<String, Object> spawnEvent = new LinkedHashMap<>();
        spawnEvent.put("ruleId", tick.spawnRule().id());
        spawnEvent.put("entityId", tick.spawnRule().entityId());
        spawnEvent.put("regionId", tick.spawnRule().regionId());
        spawnEvent.put("budget", Math.round(tick.spawnRule().maxCount() * tick.difficulty().spawnMultiplier()));

        Map<String, Object> savedStatus = new LinkedHashMap<>();
        savedStatus.put(tick.statusEffect().saveKey(), Map.of(
                "effectId", tick.statusEffect().id(),
                "durationTicks", tick.statusEffect().durationTicks(),
                "amplifier", tick.statusEffect().amplifier()
        ));
        savedStatus.put("adapterCoreModule", "echoworldcore");

        return new EchoStandaloneWorldEffectResult(
                tick.playerId(),
                activeRegion,
                activeHazard,
                tick.health(),
                healthAfter,
                inRegion && !activeRegion.equals(tick.previousRegionId()) ? List.of(tick.region().missionId()) : List.of(),
                inHazard && !tick.hazard().statusEffectId().isBlank() ? List.of(tick.hazard().statusEffectId()) : List.of(),
                hudState,
                audioState,
                renderState,
                worldLookup,
                spawnEvent,
                savedStatus
        );
    }

    public record EchoStandaloneRegion(
            String id,
            String displayName,
            int minX,
            int maxX,
            int minZ,
            int maxZ,
            String missionId) {
        public boolean contains(int x, int z) {
            return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
        }
    }

    public record EchoStandaloneRegionTransitionRequest(
            String playerId,
            String previousRegionId,
            String currentRegionId,
            String currentMissionId,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneRegionTransitionResult(
            String playerId,
            String previousRegionId,
            String currentRegionId,
            String eventType,
            boolean regionEntered,
            boolean regionExited,
            List<String> missionEvents,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneHazardTransitionRequest(
            String playerId,
            String previousHazardId,
            String currentHazardId,
            String statusEffectId,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneHazardTransitionResult(
            String playerId,
            String previousHazardId,
            String currentHazardId,
            String eventType,
            boolean hazardEntered,
            boolean hazardExited,
            List<String> statusEffects,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneHazardTransitionResult {
            statusEffects = List.copyOf(statusEffects == null ? List.of() : statusEffects);
        }
    }

    public record EchoStandaloneHazard(
            String id,
            String type,
            int centerX,
            int centerZ,
            int radius,
            double damagePerTick,
            String statusEffectId) {
        public boolean affects(int x, int z) {
            int dx = x - centerX;
            int dz = z - centerZ;
            return dx * dx + dz * dz <= radius * radius;
        }
    }

    public record EchoStandaloneHazardTickDamageRequest(
            String playerId,
            double healthBefore,
            int severity,
            long gameTick,
            String sourceReason,
            EchoStandaloneHazard hazard,
            EchoStandaloneDifficulty difficulty) {
    }

    public record EchoStandaloneHazardTickDamageResult(
            String playerId,
            String hazardId,
            String statusEffectId,
            String difficultyId,
            double healthBefore,
            double healthAfter,
            double baseDamage,
            double damageApplied,
            double hazardMultiplier,
            int severity,
            long gameTick,
            String sourceReason,
            boolean damaged) {
    }

    public record EchoStandaloneWeather(String id, String hudLine, String audioCue, String renderProfile) {
    }

    public record EchoStandaloneWeatherStateApplyRequest(
            String eventId,
            String regionId,
            String phase,
            long gameTick,
            String sourceReason,
            EchoStandaloneWeather weather,
            EchoStandaloneAtmosphere atmosphere) {
    }

    public record EchoStandaloneWeatherStateApplyResult(
            String eventId,
            String weatherId,
            String regionId,
            String phase,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean applied) {
    }

    public record EchoStandaloneAtmosphereStateApplyRequest(
            String eventId,
            String weatherId,
            String regionId,
            String phase,
            long gameTick,
            String sourceReason,
            EchoStandaloneAtmosphere atmosphere) {
    }

    public record EchoStandaloneAtmosphereStateApplyResult(
            String eventId,
            String weatherId,
            String regionId,
            String phase,
            Map<String, Object> renderState,
            Map<String, Object> runtimeBindings,
            long gameTick,
            String sourceReason,
            boolean applied) {
    }

    public record EchoStandaloneAtmosphereRuntimeProfileRequest(
            String packId,
            String profileId,
            String weatherStateId,
            String biomeAmbienceId,
            double clearVisibility,
            double stormVisibility,
            double screenHazeIntensity,
            boolean reducesDistantLights,
            String fogId,
            int fogColorArgb,
            double fogDensity,
            double fogStartDistance,
            double fogEndDistance,
            boolean stormAffected,
            String skyTintId,
            int dayColorArgb,
            int nightColorArgb,
            int stormColorArgb,
            double celestialVisibility,
            String particleProfileId,
            List<String> particleReferences,
            double particleDensity,
            boolean affectedByStormVisibility,
            String renderCoreHookReference,
            String soundCoreHookReference,
            String weatherProfileReference,
            String runtimePacketConsumer,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneAtmosphereRuntimeProfileResult(
            String packId,
            String profileId,
            String weatherStateId,
            String biomeAmbienceId,
            Map<String, Object> stormVisibilityState,
            Map<String, Object> fogProfileState,
            Map<String, Object> skyTintState,
            Map<String, Object> ambientParticlesState,
            Map<String, Object> hookRefs,
            List<Map<String, String>> runtimeBindings,
            List<String> diagnostics,
            Map<String, Object> runtimeProfileState,
            long gameTick,
            String sourceReason,
            boolean applied) {
    }

    public record EchoStandaloneWeatherScheduleProfile(
            String id,
            String type,
            String severity,
            String scope,
            int durationTicks,
            int warningTicks,
            int weight,
            boolean enabled) {
    }

    public record EchoStandaloneWeatherScheduleRequest(
            long currentTick,
            int minimumWarningTicks,
            int centerX,
            int centerY,
            int centerZ,
            int radius,
            String sourceReason,
            EchoStandaloneWeatherScheduleProfile profile) {
    }

    public record EchoStandaloneWeatherScheduleTickRequest(
            String eventId,
            String regionId,
            long gameTick,
            String sourceReason,
            EchoStandaloneWeather weather,
            EchoStandaloneAtmosphere atmosphere) {
    }

    public record EchoStandaloneWeatherScheduleTickResult(
            String eventId,
            String profileId,
            String type,
            String severity,
            String scope,
            String previousPhase,
            String phase,
            long gameTick,
            long warningStartTick,
            long startTick,
            long endTick,
            int centerX,
            int centerY,
            int centerZ,
            int radius,
            String sourceReason,
            boolean active,
            boolean ended,
            boolean phaseChanged) {
    }

    public record EchoStandaloneWeatherScheduleRestoreRequest(
            EchoStandaloneWeatherScheduleResult schedule,
            String eventId,
            String regionId,
            long gameTick,
            String sourceReason,
            EchoStandaloneWeather weather,
            EchoStandaloneAtmosphere atmosphere) {
    }

    public record EchoStandaloneWeatherExposureModifier(
            String weatherType,
            double filterDrainMultiplier,
            double radiationExposureMultiplier,
            double toxicExposureMultiplier,
            double coldExposureMultiplier,
            double heatExposureMultiplier,
            double routeRiskModifier) {
    }

    public record EchoStandaloneWeatherExposureMitigationRequest(
            String playerId,
            String weatherId,
            String weatherType,
            boolean sheltered,
            long gameTick,
            String sourceReason,
            EchoStandaloneWeatherExposureModifier weatherModifier,
            EchoStandaloneWeatherExposureModifier countermeasureModifier) {
    }

    public record EchoStandaloneWeatherExposureMitigationResult(
            String playerId,
            String weatherId,
            String weatherType,
            boolean sheltered,
            Map<String, Object> modifierState,
            long gameTick,
            String sourceReason,
            boolean mitigated) {
        public EchoStandaloneWeatherExposureMitigationResult {
            modifierState = Map.copyOf(modifierState == null ? Map.of() : modifierState);
        }
    }

    public record EchoStandaloneWeatherRouteRiskRequest(
            String playerId,
            String weatherId,
            String severity,
            double routeRiskModifier,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneWeatherRouteRiskResult(
            String playerId,
            String weatherId,
            String severity,
            double routeRiskModifier,
            double riskScore,
            String risk,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneRouteWarningPostUseRequest(
            String playerId,
            String weatherId,
            String severity,
            String risk,
            double routeRiskModifier,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneRouteWarningPostUseResult(
            String playerId,
            String weatherId,
            String severity,
            String risk,
            double routeRiskModifier,
            int x,
            int y,
            int z,
            String message,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean delivered) {
        public EchoStandaloneRouteWarningPostUseResult {
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
        }
    }

    public record EchoStandaloneWeatherForecastRequest(
            String playerId,
            String eventId,
            String weatherId,
            String weatherType,
            String displayName,
            String phase,
            String severity,
            String regionId,
            long gameTick,
            long startTick,
            long endTick,
            long etaTicks,
            double routeRiskModifier,
            double scannerReliabilityMultiplier,
            List<String> recommendedGear,
            String shelterRecommendation,
            List<String> echoLines,
            String sourceReason) {
        public EchoStandaloneWeatherForecastRequest {
            recommendedGear = List.copyOf(recommendedGear == null ? List.of() : recommendedGear);
            echoLines = List.copyOf(echoLines == null ? List.of() : echoLines);
        }
    }

    public record EchoStandaloneWeatherForecastResult(
            String playerId,
            String eventId,
            String weatherId,
            String weatherType,
            String displayName,
            String phase,
            String severity,
            long etaTicks,
            String regionName,
            int durationEstimateTicks,
            List<String> recommendedGear,
            String shelterRecommendation,
            String routeRisk,
            double routeRiskModifier,
            String scannerReliability,
            List<String> echoLines,
            long gameTick,
            String sourceReason,
            boolean forecasted) {
        public EchoStandaloneWeatherForecastResult {
            recommendedGear = List.copyOf(recommendedGear == null ? List.of() : recommendedGear);
            echoLines = List.copyOf(echoLines == null ? List.of() : echoLines);
        }
    }

    public record EchoStandaloneWeatherRadioUseRequest(
            String playerId,
            List<String> weatherIds,
            List<String> forecastLines,
            boolean forecastsAvailable,
            String strongestSeverity,
            String routeRisk,
            int cooldownTicks,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneWeatherRadioUseRequest {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
            forecastLines = List.copyOf(forecastLines == null ? List.of() : forecastLines);
        }
    }

    public record EchoStandaloneWeatherRadioUseResult(
            String playerId,
            List<String> weatherIds,
            List<String> forecastLines,
            boolean forecastsAvailable,
            String strongestSeverity,
            String routeRisk,
            int cooldownTicks,
            List<String> messageLines,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean delivered) {
        public EchoStandaloneWeatherRadioUseResult {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
            forecastLines = List.copyOf(forecastLines == null ? List.of() : forecastLines);
            messageLines = List.copyOf(messageLines == null ? List.of() : messageLines);
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
        }
    }

    public record EchoStandaloneWeatherStationUseRequest(
            String playerId,
            List<String> weatherIds,
            List<String> forecastLines,
            boolean forecastsAvailable,
            String strongestSeverity,
            String routeRisk,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneWeatherStationUseRequest {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
            forecastLines = List.copyOf(forecastLines == null ? List.of() : forecastLines);
        }
    }

    public record EchoStandaloneWeatherStationUseResult(
            String playerId,
            List<String> weatherIds,
            List<String> forecastLines,
            boolean forecastsAvailable,
            String strongestSeverity,
            String routeRisk,
            int x,
            int y,
            int z,
            List<String> messageLines,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean delivered) {
        public EchoStandaloneWeatherStationUseResult {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
            forecastLines = List.copyOf(forecastLines == null ? List.of() : forecastLines);
            messageLines = List.copyOf(messageLines == null ? List.of() : messageLines);
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
        }
    }

    public record EchoStandaloneWeatherWarningRequest(
            String eventId,
            String weatherId,
            String regionId,
            String phase,
            String channel,
            String message,
            List<String> recipientPlayerIds,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneWeatherWarningRequest {
            recipientPlayerIds = List.copyOf(recipientPlayerIds == null ? List.of() : recipientPlayerIds);
        }
    }

    public record EchoStandaloneWeatherWarningResult(
            String eventId,
            String weatherId,
            String regionId,
            String phase,
            String channel,
            String message,
            List<String> recipientPlayerIds,
            int recipientCount,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean delivered) {
        public EchoStandaloneWeatherWarningResult {
            recipientPlayerIds = List.copyOf(recipientPlayerIds == null ? List.of() : recipientPlayerIds);
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
        }
    }

    public record EchoStandaloneEmergencySirenUseRequest(
            String playerId,
            List<String> weatherIds,
            boolean activeWeatherDetected,
            String phase,
            String severity,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneEmergencySirenUseRequest {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
        }
    }

    public record EchoStandaloneEmergencySirenUseResult(
            String playerId,
            List<String> weatherIds,
            boolean activeWeatherDetected,
            String phase,
            String severity,
            int x,
            int y,
            int z,
            String message,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean delivered) {
        public EchoStandaloneEmergencySirenUseResult {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
        }
    }

    public record EchoStandaloneClimateSensorReadRequest(
            String playerId,
            List<String> weatherIds,
            boolean sheltered,
            double visibilityMultiplier,
            double scannerReliabilityMultiplier,
            double filterDrainMultiplier,
            double toxicExposureMultiplier,
            double routeRiskModifier,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneClimateSensorReadRequest {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
        }
    }

    public record EchoStandaloneClimateSensorReadResult(
            String playerId,
            List<String> weatherIds,
            boolean sheltered,
            int visibilityPercent,
            int scannerReliabilityPercent,
            double filterDrainMultiplier,
            double toxicExposureMultiplier,
            double routeRiskModifier,
            int x,
            int y,
            int z,
            List<String> messageLines,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            long gameTick,
            String sourceReason,
            boolean delivered) {
        public EchoStandaloneClimateSensorReadResult {
            weatherIds = List.copyOf(weatherIds == null ? List.of() : weatherIds);
            messageLines = List.copyOf(messageLines == null ? List.of() : messageLines);
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
        }
    }

    public record EchoStandaloneShelterReportRequest(
            String playerId,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneShelterReport(
            String playerId,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason,
            boolean recorded) {
    }

    public record EchoStandaloneWeatherScheduleResult(
            String profileId,
            String type,
            String severity,
            String scope,
            String phase,
            long warningStartTick,
            long startTick,
            long endTick,
            int centerX,
            int centerY,
            int centerZ,
            int radius,
            String sourceReason,
            boolean scheduled) {
    }

    public record EchoStandaloneAtmosphere(String id, double visibility, String particleProfile, String skyFog) {
    }

    public record EchoStandaloneBiomeProfile(String id, String biomeTag, String hazardTag) {
    }

    public record EchoStandaloneBiomeAmbientStateRequest(
            String playerId,
            String biomeProfileId,
            String biomeTag,
            String ambienceId,
            String soundProfileId,
            String particleProfileId,
            List<String> ambientAssetIds,
            String atmosphereProfileId,
            double visibilityModifier,
            long gameTick,
            String sourceReason) {
        public EchoStandaloneBiomeAmbientStateRequest {
            ambientAssetIds = List.copyOf(ambientAssetIds == null ? List.of() : ambientAssetIds);
        }
    }

    public record EchoStandaloneBiomeAmbientStateResult(
            String playerId,
            String biomeProfileId,
            String biomeTag,
            String ambienceId,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            Map<String, Object> ambientState,
            List<Map<String, String>> runtimeBindings,
            long gameTick,
            String sourceReason,
            boolean applied) {
        public EchoStandaloneBiomeAmbientStateResult {
            hudState = Map.copyOf(hudState == null ? Map.of() : hudState);
            audioState = Map.copyOf(audioState == null ? Map.of() : audioState);
            renderState = Map.copyOf(renderState == null ? Map.of() : renderState);
            ambientState = Map.copyOf(ambientState == null ? Map.of() : ambientState);
            runtimeBindings = List.copyOf(runtimeBindings == null ? List.of() : runtimeBindings);
        }
    }

    public record EchoStandaloneBiomeHazardOverlayRequest(
            String playerId,
            String worldId,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason,
            EchoStandaloneBiomeProfile biome,
            EchoStandaloneHazard hazard,
            boolean inRegion,
            boolean inHazard) {
    }

    public record EchoStandaloneBiomeHazardOverlayResult(
            String playerId,
            String worldId,
            String biomeProfileId,
            String biomeTag,
            String hazardTag,
            String hazardId,
            String overlayId,
            String cellKey,
            double intensity,
            boolean active,
            boolean visibleOnHud,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStructurePlacement(String id, String poiId, int x, int y, int z) {
    }

    public record EchoStandaloneWorldDataCatalogRequest(
            List<String> regionIds,
            List<String> hazardIds,
            List<String> weatherProfileIds,
            List<String> biomeIds,
            List<String> structureIds,
            List<String> statusEffectIds,
            List<String> difficultyIds,
            int spawnRuleCount,
            List<String> sourceFiles,
            String sourceReason) {
        public EchoStandaloneWorldDataCatalogRequest {
            regionIds = List.copyOf(regionIds);
            hazardIds = List.copyOf(hazardIds);
            weatherProfileIds = List.copyOf(weatherProfileIds);
            biomeIds = List.copyOf(biomeIds);
            structureIds = List.copyOf(structureIds);
            statusEffectIds = List.copyOf(statusEffectIds);
            difficultyIds = List.copyOf(difficultyIds);
            if (spawnRuleCount < 0) {
                throw new IllegalArgumentException("spawnRuleCount must not be negative");
            }
            sourceFiles = List.copyOf(sourceFiles);
            sourceReason = sourceReason == null ? "" : sourceReason.trim();
        }
    }

    public record EchoStandaloneWorldDataCatalogResult(
            int regionCount,
            int hazardCount,
            int weatherProfileCount,
            int biomeCount,
            int structureCount,
            int statusEffectCount,
            int difficultyRuleCount,
            int spawnRuleCount,
            int sourceFileCount,
            List<String> representativeRegionIds,
            List<String> representativeHazardIds,
            List<String> representativeWeatherProfileIds,
            List<String> representativeBiomeIds,
            List<String> representativeStructureIds,
            List<String> representativeStatusEffectIds,
            List<String> representativeDifficultyIds,
            String sourceReason,
            boolean loaded) {
        public EchoStandaloneWorldDataCatalogResult {
            representativeRegionIds = List.copyOf(representativeRegionIds);
            representativeHazardIds = List.copyOf(representativeHazardIds);
            representativeWeatherProfileIds = List.copyOf(representativeWeatherProfileIds);
            representativeBiomeIds = List.copyOf(representativeBiomeIds);
            representativeStructureIds = List.copyOf(representativeStructureIds);
            representativeStatusEffectIds = List.copyOf(representativeStatusEffectIds);
            representativeDifficultyIds = List.copyOf(representativeDifficultyIds);
            sourceReason = sourceReason == null ? "" : sourceReason.trim();
        }
    }

    public record EchoStandaloneWorldCellSampleRequest(
            String playerId,
            String worldId,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason,
            EchoStandaloneRegion region,
            EchoStandaloneHazard hazard,
            EchoStandaloneBiomeProfile biome,
            EchoStandaloneStructurePlacement structure) {
    }

    public record EchoStandaloneWorldCellSampleResult(
            String playerId,
            String worldId,
            String activeRegionId,
            String activeHazardId,
            String biomeProfileId,
            String structureId,
            String poiId,
            String cellKey,
            int x,
            int y,
            int z,
            boolean inRegion,
            boolean inHazard,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneWorldChunkStateRequest(
            String playerId,
            String worldId,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason,
            EchoStandaloneWorldCellSampleResult cellSample) {
    }

    public record EchoStandaloneWorldChunkStateResult(
            String playerId,
            String worldId,
            String chunkKey,
            int chunkX,
            int chunkZ,
            String lastCellKey,
            int lastSampleX,
            int lastSampleY,
            int lastSampleZ,
            String activeRegionId,
            String activeHazardId,
            String biomeProfileId,
            String structureId,
            String poiId,
            boolean inRegion,
            boolean inHazard,
            long lastGameTick,
            String sourceReason) {
    }

    public record EchoStandaloneWorldChunkState(
            String worldId,
            String chunkKey,
            int chunkX,
            int chunkZ,
            String lastCellKey,
            int lastSampleX,
            int lastSampleY,
            int lastSampleZ,
            String activeRegionId,
            String activeHazardId,
            String biomeProfileId,
            String structureId,
            String poiId,
            boolean inRegion,
            boolean inHazard,
            long lastGameTick) {
        private static EchoStandaloneWorldChunkState from(EchoStandaloneWorldCellSampleResult result) {
            return new EchoStandaloneWorldChunkState(
                    result.worldId(),
                    EchoStandaloneWorldEffectsRuntime.chunkKey(result.worldId(), result.x(), result.z()),
                    Math.floorDiv(result.x(), 16),
                    Math.floorDiv(result.z(), 16),
                    result.cellKey(),
                    result.x(),
                    result.y(),
                    result.z(),
                    result.activeRegionId(),
                    result.activeHazardId(),
                    result.biomeProfileId(),
                    result.structureId(),
                    result.poiId(),
                    result.inRegion(),
                    result.inHazard(),
                    result.gameTick());
        }

        private static EchoStandaloneWorldChunkState from(EchoStandaloneWorldChunkStateResult result) {
            return new EchoStandaloneWorldChunkState(
                    result.worldId(),
                    result.chunkKey(),
                    result.chunkX(),
                    result.chunkZ(),
                    result.lastCellKey(),
                    result.lastSampleX(),
                    result.lastSampleY(),
                    result.lastSampleZ(),
                    result.activeRegionId(),
                    result.activeHazardId(),
                    result.biomeProfileId(),
                    result.structureId(),
                    result.poiId(),
                    result.inRegion(),
                    result.inHazard(),
                    result.lastGameTick());
        }
    }

    public record EchoStandaloneHazardFieldState(
            String hazardId,
            String type,
            int centerX,
            int centerZ,
            int radius,
            double damagePerTick,
            String statusEffectId,
            String lastCellKey,
            String worldId,
            boolean sampledInside,
            long lastGameTick) {
        private static EchoStandaloneHazardFieldState from(EchoStandaloneHazard hazard,
                EchoStandaloneWorldCellSampleResult result) {
            return new EchoStandaloneHazardFieldState(
                    hazard.id(),
                    hazard.type(),
                    hazard.centerX(),
                    hazard.centerZ(),
                    hazard.radius(),
                    hazard.damagePerTick(),
                    hazard.statusEffectId(),
                    result.cellKey(),
                    result.worldId(),
                    result.inHazard(),
                    result.gameTick());
        }

        private static EchoStandaloneHazardFieldState from(EchoStandaloneHazardFieldStateResult result) {
            return new EchoStandaloneHazardFieldState(
                    result.hazardId(),
                    result.type(),
                    result.centerX(),
                    result.centerZ(),
                    result.radius(),
                    result.damagePerTick(),
                    result.statusEffectId(),
                    result.lastCellKey(),
                    result.worldId(),
                    result.sampledInside(),
                    result.lastGameTick());
        }
    }

    public record EchoStandaloneHazardFieldStateRequest(
            String playerId,
            String worldId,
            long gameTick,
            String sourceReason,
            EchoStandaloneHazard hazard,
            EchoStandaloneWorldCellSampleResult cellSample) {
    }

    public record EchoStandaloneHazardFieldStateResult(
            String playerId,
            String worldId,
            String hazardId,
            String type,
            int centerX,
            int centerZ,
            int radius,
            double damagePerTick,
            String statusEffectId,
            String lastCellKey,
            boolean sampledInside,
            long lastGameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStructurePoiLookupRequest(
            String playerId,
            String regionId,
            int playerX,
            int playerY,
            int playerZ,
            int maxDistance,
            long gameTick,
            String sourceReason,
            EchoStandaloneStructurePlacement structure) {
    }

    public record EchoStandaloneStructurePoiLookupResult(
            String playerId,
            String regionId,
            String structureId,
            String poiId,
            int x,
            int y,
            int z,
            long distanceSquared,
            int maxDistance,
            boolean inRange,
            String markerId,
            String lookupType,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStructurePoiMarkerStateRequest(
            String playerId,
            String sourceReason,
            EchoStandaloneStructurePoiLookupResult lookup) {
    }

    public record EchoStandaloneStructurePoiMarkerStateResult(
            String playerId,
            String markerId,
            String regionId,
            String structureId,
            String poiId,
            int x,
            int y,
            int z,
            long distanceSquared,
            int maxDistance,
            boolean inRange,
            boolean markerPersisted,
            String lookupType,
            long lastGameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStructureDiscoveryStateRequest(
            String playerId,
            String sourceReason,
            EchoStandaloneStructurePoiMarkerStateResult markerState) {
    }

    public record EchoStandaloneStructureDiscoveryStateResult(
            String playerId,
            String markerId,
            String regionId,
            String structureId,
            String poiId,
            String previousDiscoveryState,
            String discoveryState,
            boolean discovered,
            boolean firstDiscovery,
            boolean holomapMarkerActive,
            long lastGameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStructurePoiState(
            String markerId,
            String regionId,
            String structureId,
            String poiId,
            int x,
            int y,
            int z,
            long distanceSquared,
            int maxDistance,
            boolean inRange,
            String lookupType,
            long lastGameTick) {
        private static EchoStandaloneStructurePoiState from(EchoStandaloneStructurePoiLookupResult result) {
            return new EchoStandaloneStructurePoiState(
                    result.markerId(),
                    result.regionId(),
                    result.structureId(),
                    result.poiId(),
                    result.x(),
                    result.y(),
                    result.z(),
                    result.distanceSquared(),
                    result.maxDistance(),
                    result.inRange(),
                    result.lookupType(),
                    result.gameTick());
        }

        private static EchoStandaloneStructurePoiState from(EchoStandaloneStructurePoiMarkerStateResult result) {
            return new EchoStandaloneStructurePoiState(
                    result.markerId(),
                    result.regionId(),
                    result.structureId(),
                    result.poiId(),
                    result.x(),
                    result.y(),
                    result.z(),
                    result.distanceSquared(),
                    result.maxDistance(),
                    result.inRange(),
                    result.lookupType(),
                    result.lastGameTick());
        }
    }

    public record EchoStandaloneSpawnRule(String id, String entityId, String regionId, int maxCount, double difficultyWeight) {
    }

    public record EchoStandaloneStatusEffect(String id, int durationTicks, int amplifier, String saveKey) {
    }

    public record EchoStandaloneStatusEffectApplyRequest(
            String playerId,
            String hazardId,
            float damageApplied,
            long gameTick,
            String sourceReason,
            EchoStandaloneStatusEffect statusEffect,
            boolean loaded) {
    }

    public record EchoStandaloneStatusEffectApplyResult(
            String playerId,
            String hazardId,
            String effectId,
            int durationTicks,
            int amplifier,
            String saveKey,
            float damageApplied,
            long appliedGameTick,
            long expiresAtTick,
            Map<String, Object> activeStatusState,
            String sourceReason,
            boolean loaded,
            boolean applied) {
    }

    public record EchoStandaloneStatusExposureMitigationRequest(
            String playerId,
            String exposureId,
            String hazardId,
            EchoStandaloneStatusEffect statusEffect,
            String statusKind,
            double exposureIntensity,
            int durationTicks,
            double accumulationPerSecond,
            String resistanceId,
            double mitigationRatio,
            double immunityThreshold,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStatusExposureMitigationResult(
            String playerId,
            String exposureId,
            String hazardId,
            String effectId,
            String statusKind,
            double originalIntensity,
            double effectiveIntensity,
            int originalDurationTicks,
            int effectiveDurationTicks,
            double originalAccumulationPerSecond,
            double effectiveAccumulationPerSecond,
            String resistanceId,
            double mitigationRatio,
            double immunityThreshold,
            boolean immune,
            Map<String, Object> exposureState,
            long gameTick,
            String sourceReason,
            boolean applied) {
        public EchoStandaloneStatusExposureMitigationResult {
            exposureState = Map.copyOf(exposureState == null ? Map.of() : exposureState);
        }
    }

    public record EchoStandaloneStatusEffectStackingRequest(
            String playerId,
            String hazardId,
            String stackingPolicy,
            int previousDurationTicks,
            int previousAmplifier,
            double previousDamageApplied,
            long previousAppliedGameTick,
            long previousExpiresAtTick,
            float damageApplied,
            long gameTick,
            String sourceReason,
            EchoStandaloneStatusEffect statusEffect,
            boolean hadPrevious,
            boolean loaded) {
    }

    public record EchoStandaloneStatusEffectStackingResult(
            String playerId,
            String hazardId,
            String effectId,
            String saveKey,
            String stackingPolicy,
            int durationTicks,
            int amplifier,
            double damageApplied,
            long appliedGameTick,
            long expiresAtTick,
            boolean hadPrevious,
            boolean refreshed,
            boolean amplifierUpgraded,
            boolean stacked,
            boolean retained,
            boolean loaded,
            String sourceReason) {
    }

    public record EchoStandaloneStatusEffectSaveRequest(
            String playerId,
            String hazardId,
            float damageApplied,
            long gameTick,
            String sourceReason,
            EchoStandaloneStatusEffect statusEffect) {
    }

    public record EchoStandaloneStatusEffectSaveResult(
            String playerId,
            String hazardId,
            String effectId,
            int durationTicks,
            int amplifier,
            String saveKey,
            float damageApplied,
            long gameTick,
            Map<String, Object> savedStatusState,
            String sourceReason,
            boolean saved) {
    }

    public record EchoStandaloneStatusEffectLoadRequest(
            String playerId,
            String hazardId,
            String saveKey,
            Map<String, Object> savedStatusState,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandalonePersistedStatusEffectLoadRequest(
            String playerId,
            String hazardId,
            String saveKey,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStatusEffectLoadResult(
            String playerId,
            String hazardId,
            String effectId,
            int durationTicks,
            int amplifier,
            String saveKey,
            float damageApplied,
            long savedGameTick,
            long loadedGameTick,
            String sourceReason,
            boolean loaded) {
    }

    public record EchoStandaloneStatusEffectExpiryRequest(
            String playerId,
            String hazardId,
            String effectId,
            String saveKey,
            long appliedGameTick,
            long expiresAtTick,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneStatusEffectExpiryResult(
            String playerId,
            String hazardId,
            String effectId,
            String saveKey,
            long appliedGameTick,
            long expiresAtTick,
            long gameTick,
            boolean expired,
            boolean retained,
            String sourceReason) {
    }

    public record EchoStandaloneActiveStatusEffectState(
            String effectId,
            String hazardId,
            String saveKey,
            int durationTicks,
            int amplifier,
            double damageApplied,
            long appliedGameTick,
            long expiresAtTick,
            boolean loaded) {
        public boolean activeAt(long gameTick) {
            return Math.max(0L, gameTick) < expiresAtTick;
        }
    }

    public record EchoStandaloneStatusProfileState(
            String playerId,
            String hazardId,
            String effectId,
            String saveKey,
            String statusKind,
            String severity,
            String stackingPolicy,
            int durationTicks,
            int amplifier,
            double damageApplied,
            double exposureIntensity,
            double accumulationPerSecond,
            boolean persisted,
            boolean loaded,
            long lastGameTick,
            String sourceReason) {
    }

    public record EchoStandaloneDifficulty(String id, double hazardMultiplier, double spawnMultiplier) {
    }

    public record EchoStandaloneDifficultyProfileSelectionRequest(
            String playerId,
            String regionId,
            String missionId,
            String requestedDifficulty,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneDifficultyProfileSelectionResult(
            String playerId,
            String regionId,
            String missionId,
            String requestedDifficulty,
            String selectedDifficulty,
            String difficultyId,
            double hazardMultiplier,
            double spawnMultiplier,
            long gameTick,
            String sourceReason,
            boolean selected) {
    }

    public record EchoStandaloneDifficultyApplicationResult(
            String playerId,
            String regionId,
            String difficultyId,
            double hazardMultiplier,
            double spawnMultiplier,
            String appliedHazardId,
            double baseHazardDamage,
            double scaledHazardDamage,
            String appliedSpawnRuleId,
            int maxSpawnCount,
            int scaledSpawnBudget,
            int activeSpawnPopulation,
            long lastGameTick,
            String sourceReason,
            boolean applied) {
    }

    public record EchoStandaloneDifficultyApplicationState(
            String playerId,
            String regionId,
            String difficultyId,
            double hazardMultiplier,
            double spawnMultiplier,
            String appliedHazardId,
            double baseHazardDamage,
            double scaledHazardDamage,
            String appliedSpawnRuleId,
            int maxSpawnCount,
            int scaledSpawnBudget,
            int activeSpawnPopulation,
            long lastGameTick,
            String sourceReason) {
        private static EchoStandaloneDifficultyApplicationState merge(
                EchoStandaloneDifficultyApplicationState previous,
                String playerId,
                String regionId,
                EchoStandaloneDifficulty difficulty,
                String hazardId,
                double baseDamage,
                double scaledDamage,
                String spawnRuleId,
                int maxSpawnCount,
                int scaledSpawnBudget,
                int activeSpawnPopulation,
                long gameTick,
                String sourceReason) {
            boolean hasHazard = hazardId != null && !hazardId.isBlank();
            boolean hasSpawn = spawnRuleId != null && !spawnRuleId.isBlank();
            return new EchoStandaloneDifficultyApplicationState(
                    text(playerId),
                    text(regionId),
                    difficulty.id(),
                    difficulty.hazardMultiplier(),
                    difficulty.spawnMultiplier(),
                    hasHazard || previous == null ? text(hazardId) : previous.appliedHazardId(),
                    hasHazard || previous == null ? Math.max(0.0D, baseDamage) : previous.baseHazardDamage(),
                    hasHazard || previous == null ? Math.max(0.0D, scaledDamage) : previous.scaledHazardDamage(),
                    hasSpawn || previous == null ? text(spawnRuleId) : previous.appliedSpawnRuleId(),
                    hasSpawn || previous == null ? Math.max(0, maxSpawnCount) : previous.maxSpawnCount(),
                    hasSpawn || previous == null ? Math.max(0, scaledSpawnBudget) : previous.scaledSpawnBudget(),
                    hasSpawn || previous == null ? Math.max(0, activeSpawnPopulation) : previous.activeSpawnPopulation(),
                    Math.max(0L, gameTick),
                    text(sourceReason));
        }
    }

    public record EchoStandaloneSpawnRuleEventRequest(
            String playerId,
            String regionId,
            int x,
            int y,
            int z,
            int activeMobCount,
            long gameTick,
            String sourceReason,
            EchoStandaloneSpawnRule spawnRule,
            EchoStandaloneDifficulty difficulty) {
    }

    public record EchoStandaloneSpawnRuleEventResult(
            String playerId,
            String ruleId,
            String entityId,
            String regionId,
            String difficultyId,
            int maxCount,
            int activeMobCount,
            int scaledBudget,
            int spawnCount,
            double spawnMultiplier,
            double difficultyWeight,
            String eventType,
            int x,
            int y,
            int z,
            long gameTick,
            String sourceReason) {
    }

    public record EchoStandaloneSpawnZoneStateRequest(
            String playerId,
            String sourceReason,
            EchoStandaloneSpawnRuleEventResult event) {
    }

    public record EchoStandaloneSpawnZoneStateResult(
            String playerId,
            String regionId,
            String ruleId,
            String zoneKey,
            String entityId,
            String difficultyId,
            int maxCount,
            int activeMobCount,
            int scaledBudget,
            int spawnCount,
            int activePopulation,
            double spawnMultiplier,
            double difficultyWeight,
            String eventType,
            int x,
            int y,
            int z,
            long lastGameTick,
            String sourceReason) {
    }

    public record EchoStandaloneSpawnZoneState(
            String regionId,
            String ruleId,
            String entityId,
            String difficultyId,
            int maxCount,
            int activeMobCount,
            int scaledBudget,
            int spawnCount,
            int activePopulation,
            double spawnMultiplier,
            double difficultyWeight,
            String eventType,
            int x,
            int y,
            int z,
            long lastGameTick) {
        private static EchoStandaloneSpawnZoneState from(EchoStandaloneSpawnZoneStateResult result) {
            return new EchoStandaloneSpawnZoneState(
                    result.regionId(),
                    result.ruleId(),
                    result.entityId(),
                    result.difficultyId(),
                    result.maxCount(),
                    result.activeMobCount(),
                    result.scaledBudget(),
                    result.spawnCount(),
                    result.activePopulation(),
                    result.spawnMultiplier(),
                    result.difficultyWeight(),
                    result.eventType(),
                    result.x(),
                    result.y(),
                    result.z(),
                    result.lastGameTick());
        }
    }

    public record EchoStandaloneWorldEffectTick(
            String playerId,
            int x,
            int y,
            int z,
            double health,
            String previousRegionId,
            EchoStandaloneRegion region,
            EchoStandaloneHazard hazard,
            EchoStandaloneWeather weather,
            EchoStandaloneAtmosphere atmosphere,
            EchoStandaloneBiomeProfile biome,
            EchoStandaloneStructurePlacement structure,
            EchoStandaloneSpawnRule spawnRule,
            EchoStandaloneStatusEffect statusEffect,
            EchoStandaloneDifficulty difficulty) {
    }

    public record EchoStandaloneWorldEffectResult(
            String playerId,
            String activeRegionId,
            String activeHazardId,
            double healthBefore,
            double healthAfter,
            List<String> missionEvents,
            List<String> statusEffects,
            Map<String, Object> hudState,
            Map<String, Object> audioState,
            Map<String, Object> renderState,
            Map<String, Object> worldLookup,
            Map<String, Object> spawnEvent,
            Map<String, Object> savedStatusState) {
    }

    private static String idPath(String id) {
        int colon = id == null ? -1 : id.indexOf(':');
        return colon >= 0 && colon < id.length() - 1 ? id.substring(colon + 1) : id;
    }

    private static String cellKey(String worldId, int x, int y, int z) {
        return worldId + ":" + x + ":" + y + ":" + z;
    }

    private static String chunkKey(String worldId, int x, int z) {
        return text(worldId) + ":chunk:" + Math.floorDiv(x, 16) + ":" + Math.floorDiv(z, 16);
    }

    private static String statusStoreKey(String playerId, String hazardId) {
        return text(playerId) + "|" + text(hazardId);
    }

    private static String weatherExposureKey(String playerId, String weatherId) {
        return text(playerId) + "|" + text(weatherId);
    }

    private static String weatherRouteRiskKey(String playerId, String weatherId) {
        return text(playerId) + "|" + text(weatherId);
    }

    private static String routeWarningPostKey(String playerId, int x, int y, int z) {
        return text(playerId) + "|" + x + "," + y + "," + z;
    }

    private static String emergencySirenKey(String playerId, int x, int y, int z) {
        return text(playerId) + "|" + x + "," + y + "," + z;
    }

    private static String climateSensorKey(String playerId, int x, int y, int z) {
        return text(playerId) + "|" + x + "," + y + "," + z;
    }

    private static String weatherStationKey(String playerId, int x, int y, int z) {
        return text(playerId) + "|" + x + "," + y + "," + z;
    }

    private static int percent(double multiplier) {
        return (int) Math.round(Math.max(0.0D, multiplier) * 100.0D);
    }

    private static String riskForScore(double score) {
        if (score < 1.0D) {
            return "SAFE";
        }
        if (score < 2.0D) {
            return "WATCH";
        }
        if (score < 3.0D) {
            return "HAZARDOUS";
        }
        if (score < 4.0D) {
            return "DELAY_RECOMMENDED";
        }
        return "ROUTE_LOCKDOWN";
    }

    private static String spawnZoneKey(String regionId, String ruleId) {
        return text(regionId) + "|" + text(ruleId);
    }

    private static String weatherPhase(EchoStandaloneWeatherScheduleResult schedule, long tick) {
        long safeTick = Math.max(0L, tick);
        if (safeTick >= schedule.endTick()) {
            return "ENDED";
        }
        long activeWindow = Math.max(1L, schedule.endTick() - schedule.startTick());
        if (safeTick >= schedule.startTick() + Math.round(activeWindow * 0.85D)) {
            return "CLEARING";
        }
        if (safeTick >= schedule.startTick() + Math.round(activeWindow * 0.6D)) {
            return "CRITICAL";
        }
        if (safeTick >= schedule.startTick()) {
            return "ACTIVE";
        }
        long warningWindow = Math.max(1L, schedule.startTick() - schedule.warningStartTick());
        if (safeTick >= schedule.warningStartTick() + Math.round(warningWindow * 0.5D)) {
            return "INCOMING";
        }
        return "FORECAST";
    }

    private static EchoStandaloneWeatherScheduleResult scheduleWithPhase(
            EchoStandaloneWeatherScheduleResult schedule,
            String phase) {
        return new EchoStandaloneWeatherScheduleResult(
                schedule.profileId(),
                schedule.type(),
                schedule.severity(),
                schedule.scope(),
                phase,
                schedule.warningStartTick(),
                schedule.startTick(),
                schedule.endTick(),
                schedule.centerX(),
                schedule.centerY(),
                schedule.centerZ(),
                schedule.radius(),
                schedule.sourceReason(),
                schedule.scheduled());
    }

    private static String weatherDisplayName(String hudLine) {
        String text = text(hudLine);
        int separator = text.indexOf(':');
        return separator > 0 ? text.substring(0, separator).strip() : text.strip();
    }

    private void recordDifficultyApplication(
            String playerId,
            String regionId,
            EchoStandaloneDifficulty difficulty,
            String hazardId,
            double baseDamage,
            double scaledDamage,
            String spawnRuleId,
            int maxSpawnCount,
            int scaledSpawnBudget,
            int activeSpawnPopulation,
            long gameTick,
            String sourceReason) {
        if (playerId == null || playerId.isBlank() || difficulty == null) {
            return;
        }
        EchoStandaloneDifficultyApplicationState previous = activeDifficultyApplicationStates.get(playerId);
        String safeRegionId = text(regionId);
        if (safeRegionId.isBlank() && previous != null) {
            safeRegionId = previous.regionId();
        }
        EchoStandaloneDifficultyApplicationState state = EchoStandaloneDifficultyApplicationState.merge(
                previous,
                playerId,
                safeRegionId,
                difficulty,
                hazardId,
                baseDamage,
                scaledDamage,
                spawnRuleId,
                maxSpawnCount,
                scaledSpawnBudget,
                activeSpawnPopulation,
                gameTick,
                sourceReason);
        activeDifficultyApplicationStates.put(playerId, state);
        EchoStandaloneDifficultyApplicationResult result = new EchoStandaloneDifficultyApplicationResult(
                state.playerId(),
                state.regionId(),
                state.difficultyId(),
                state.hazardMultiplier(),
                state.spawnMultiplier(),
                state.appliedHazardId(),
                state.baseHazardDamage(),
                state.scaledHazardDamage(),
                state.appliedSpawnRuleId(),
                state.maxSpawnCount(),
                state.scaledSpawnBudget(),
                state.activeSpawnPopulation(),
                state.lastGameTick(),
                state.sourceReason(),
                !state.appliedHazardId().isBlank() || !state.appliedSpawnRuleId().isBlank());
        activeDifficultyApplicationResults.put(playerId, result);
        if (!state.regionId().isBlank()) {
            regionDifficultyApplicationStates.put(state.regionId(), state);
            regionDifficultyApplicationResults.put(state.regionId(), result);
        }
    }

    public record EchoStandaloneAgent7LiveHookEvidence(
            String moduleId,
            String event,
            String key,
            long gameTick,
            String sourceReason,
            boolean liveGameplayHookVerified,
            String evidenceMode
    ) {
    }

    private void recordActiveStatusEffect(String playerId,
            String effectId,
            String hazardId,
            String saveKey,
            int durationTicks,
            int amplifier,
            double damageApplied,
            long appliedGameTick,
            boolean loaded) {
        if (effectId == null || effectId.isBlank()) {
            return;
        }
        EchoStandaloneActiveStatusEffectState previous =
                activeStatusEffectStates.getOrDefault(playerId, Map.of()).get(effectId);
        EchoStandaloneStatusEffectStackingResult stacking = stackStatusEffect(
                new EchoStandaloneStatusEffectStackingRequest(
                        playerId,
                        text(hazardId),
                        "REFRESH_DURATION",
                        previous == null ? 0 : previous.durationTicks(),
                        previous == null ? 0 : previous.amplifier(),
                        previous == null ? 0.0D : previous.damageApplied(),
                        previous == null ? 0L : previous.appliedGameTick(),
                        previous == null ? 0L : previous.expiresAtTick(),
                        (float) Math.max(0.0D, damageApplied),
                        Math.max(0L, appliedGameTick),
                        loaded ? "standalone-status-load-stacking" : "standalone-status-apply-stacking",
                        new EchoStandaloneStatusEffect(
                                effectId,
                                Math.max(0, durationTicks),
                                Math.max(0, amplifier),
                                text(saveKey)),
                        previous != null,
                        loaded));
        Map<String, EchoStandaloneStatusEffectStackingResult> stackings = new LinkedHashMap<>(
                lastStatusEffectStackings.getOrDefault(playerId, Map.of()));
        stackings.put(effectId, stacking);
        lastStatusEffectStackings.put(playerId, Map.copyOf(stackings));
        EchoStandaloneActiveStatusEffectState state = new EchoStandaloneActiveStatusEffectState(
                effectId,
                text(hazardId),
                text(saveKey),
                stacking.durationTicks(),
                stacking.amplifier(),
                stacking.damageApplied(),
                stacking.appliedGameTick(),
                stacking.expiresAtTick(),
                stacking.loaded());
        Map<String, EchoStandaloneActiveStatusEffectState> states = new LinkedHashMap<>(
                activeStatusEffectStates.getOrDefault(playerId, Map.of()));
        states.put(effectId, state);
        activeStatusEffectStates.put(playerId, Map.copyOf(states));
        ArrayList<String> effects = new ArrayList<>(activeStatusEffects.getOrDefault(playerId, List.of()));
        if (!effects.contains(effectId)) {
            effects.add(effectId);
        }
        activeStatusEffects.put(playerId, List.copyOf(effects));
    }

    private void recordStatusProfileApplication(String playerId,
            String hazardId,
            String effectId,
            String saveKey,
            int durationTicks,
            int amplifier,
            double damageApplied,
            long gameTick,
            boolean persisted,
            boolean loaded) {
        if (playerId == null || playerId.isBlank() || effectId == null || effectId.isBlank()) {
            return;
        }
        int severity = Math.max(0, (int) Math.round(Math.max(0.0D, damageApplied) * 12.5D));
        EchoStandaloneStatusProfileState state = new EchoStandaloneStatusProfileState(
                playerId,
                text(hazardId),
                effectId,
                text(saveKey),
                statusKind(hazardId),
                severityBand(severity),
                "REFRESH_DURATION",
                Math.max(0, durationTicks),
                Math.max(0, amplifier),
                Math.max(0.0D, damageApplied),
                Math.max(1.0D, severity / 25.0D),
                Math.max(0.2D, severity / 125.0D),
                persisted,
                loaded,
                Math.max(0L, gameTick),
                loaded ? "EchoStandaloneWorldEffectsRuntime.loadStatusEffect"
                        : "EchoStandaloneWorldEffectsRuntime.persistStatusEffect");
        Map<String, EchoStandaloneStatusProfileState> states = new LinkedHashMap<>(
                activeStatusProfileStates.getOrDefault(playerId, Map.of()));
        states.put(effectId, state);
        activeStatusProfileStates.put(playerId, Map.copyOf(states));
    }

    private void pruneActiveStatusEffects(String playerId, long gameTick) {
        Map<String, EchoStandaloneActiveStatusEffectState> states = activeStatusEffectStates.get(playerId);
        if (states == null || states.isEmpty()) {
            activeStatusEffects.remove(playerId);
            activeStatusProfileStates.remove(playerId);
            return;
        }
        Map<String, EchoStandaloneActiveStatusEffectState> retained = new LinkedHashMap<>();
        for (Map.Entry<String, EchoStandaloneActiveStatusEffectState> entry : states.entrySet()) {
            EchoStandaloneActiveStatusEffectState state = entry.getValue();
            EchoStandaloneStatusEffectExpiryResult expiry = evaluateStatusEffectExpiry(
                    new EchoStandaloneStatusEffectExpiryRequest(
                            playerId,
                            state.hazardId(),
                            state.effectId(),
                            state.saveKey(),
                            state.appliedGameTick(),
                            state.expiresAtTick(),
                            gameTick,
                            "standalone-status-expiry"));
            if (expiry.retained()) {
                retained.put(entry.getKey(), entry.getValue());
            }
        }
        if (retained.isEmpty()) {
            activeStatusEffectStates.remove(playerId);
            activeStatusEffects.remove(playerId);
            activeStatusProfileStates.remove(playerId);
            return;
        }
        activeStatusEffectStates.put(playerId, Map.copyOf(retained));
        activeStatusEffects.put(playerId, List.copyOf(retained.keySet()));
        Map<String, EchoStandaloneStatusProfileState> profileStates = activeStatusProfileStates.get(playerId);
        if (profileStates != null && !profileStates.isEmpty()) {
            Map<String, EchoStandaloneStatusProfileState> retainedProfiles = new LinkedHashMap<>();
            retained.keySet().forEach(effectId -> {
                EchoStandaloneStatusProfileState state = profileStates.get(effectId);
                if (state != null) {
                    retainedProfiles.put(effectId, state);
                }
            });
            if (retainedProfiles.isEmpty()) {
                activeStatusProfileStates.remove(playerId);
            } else {
                activeStatusProfileStates.put(playerId, Map.copyOf(retainedProfiles));
            }
        }
    }

    private static EchoStandaloneDifficulty difficultyFor(String difficulty) {
        return switch (difficulty) {
            case "easy" -> new EchoStandaloneDifficulty("echodifficultycore:easy", 1.0D, 0.85D);
            case "hard" -> new EchoStandaloneDifficulty("echodifficultycore:hard", 1.5D, 1.25D);
            case "extreme" -> new EchoStandaloneDifficulty("echodifficultycore:extreme", 2.0D, 1.5D);
            default -> new EchoStandaloneDifficulty("echodifficultycore:normal", 1.25D, 1.0D);
        };
    }

    private static String normalizeDifficulty(String difficulty) {
        String normalized = text(difficulty).toLowerCase(java.util.Locale.ROOT).strip();
        if (normalized.startsWith("echodifficultycore:")) {
            normalized = normalized.substring("echodifficultycore:".length());
        }
        return switch (normalized) {
            case "easy", "normal", "hard", "extreme" -> normalized;
            default -> "normal";
        };
    }

    private static String statusKind(String hazardId) {
        String path = text(hazardId).toLowerCase(java.util.Locale.ROOT);
        if (path.contains("radiation")) {
            return "RADIATION";
        }
        if (path.contains("toxic")) {
            return "TOXIN";
        }
        if (path.contains("cryo") || path.contains("cold")) {
            return "THERMAL";
        }
        if (path.contains("nexus") || path.contains("anomaly")) {
            return "ANOMALY";
        }
        return "ENVIRONMENTAL_HAZARD";
    }

    private static String severityBand(int severity) {
        if (severity >= 100) {
            return "CRITICAL";
        }
        if (severity >= 75) {
            return "HIGH";
        }
        if (severity >= 35) {
            return "MEDIUM";
        }
        if (severity > 0) {
            return "LOW";
        }
        return "UNKNOWN";
    }

    private static Map<String, Object> statusPayload(Map<String, Object> savedStatusState, String saveKey) {
        Object raw = savedStatusState.get(saveKey);
        if (!(raw instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                payload.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return payload;
    }

    private static List<String> representative(List<String> ids) {
        if (ids.size() <= 2) {
            return ids;
        }
        return List.of(ids.get(0), ids.get(ids.size() - 1));
    }

    private static Map<String, String> binding(String target, String source, String adapterHook) {
        Map<String, String> binding = new LinkedHashMap<>();
        binding.put("target", target);
        binding.put("source", source);
        binding.put("adapterHook", adapterHook);
        return Map.copyOf(binding);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String requireText(Object value, String name) {
        String text = text(value).strip();
        if (text.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return text;
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        if (value == null) {
            return 0;
        }
        return Math.max(0, Integer.parseInt(String.valueOf(value)));
    }

    private static long longs(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private static float floating(Object value) {
        if (value instanceof Number number) {
            return Math.max(0.0F, number.floatValue());
        }
        if (value == null) {
            return 0.0F;
        }
        return Math.max(0.0F, Float.parseFloat(String.valueOf(value)));
    }
}
