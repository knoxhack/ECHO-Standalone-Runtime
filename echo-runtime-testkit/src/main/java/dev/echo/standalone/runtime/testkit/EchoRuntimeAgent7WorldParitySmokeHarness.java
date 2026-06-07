package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.world.EchoStandaloneWorldDefinitionLoader;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldDefinitionLoader.EchoStandaloneAgent7CatalogSnapshot;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldDefinitionLoader.EchoStandaloneWorldDefinitionSnapshot;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneBiomeAmbientStateRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneBiomeAmbientStateResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneBiomeHazardOverlayResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneHazardTickDamageResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneRegionTransitionResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneSpawnRuleEventResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandalonePersistedStatusEffectLoadRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectApplyResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffect;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusExposureMitigationRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusExposureMitigationResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectLoadResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectSaveRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectSaveResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectStackingResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStructurePoiLookupResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldEffectResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneAtmosphereStateApplyResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneAtmosphereRuntimeProfileRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneAtmosphereRuntimeProfileResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherScheduleResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherScheduleRestoreRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherScheduleTickRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherScheduleTickResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherStateApplyResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherExposureMitigationRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherExposureMitigationResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherExposureModifier;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherForecastRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherForecastResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRadioUseResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherStationUseRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherStationUseResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherWarningRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherWarningResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneEmergencySirenUseRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneEmergencySirenUseResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneClimateSensorReadRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneClimateSensorReadResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRouteRiskRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherRouteRiskResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneRouteWarningPostUseRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneRouteWarningPostUseResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneHazard;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneHazardTransitionResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneShelterReport;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneShelterReportRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneDifficulty;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneDifficultyProfileSelectionRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldCellSampleRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldCellSampleResult;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldDataCatalogRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldDataCatalogResult;

import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeAgent7WorldParitySmokeHarness {
    private EchoRuntimeAgent7WorldParitySmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path repoRoot = args.length == 0 ? Path.of("..").toAbsolutePath().normalize()
                : Path.of(args[0]).toAbsolutePath().normalize();
        EchoStandaloneWorldDefinitionLoader loader = new EchoStandaloneWorldDefinitionLoader();
        EchoStandaloneAgent7CatalogSnapshot catalog = loader.loadAgent7Catalog(repoRoot);
        EchoStandaloneWorldDefinitionSnapshot snapshot = loader.loadDefinition(
                repoRoot,
                EchoStandaloneWorldDefinitionLoader.ashfallCrashZoneProfile()
        );
        EchoStandaloneWorldDefinitionSnapshot toxicSnapshot = loader.loadDefinition(
                repoRoot,
                EchoStandaloneWorldDefinitionLoader.ashfallToxicSwampProfile()
        );
        EchoStandaloneWorldEffectsRuntime runtime = new EchoStandaloneWorldEffectsRuntime();
        runtime.recordAgent7LiveHook(
                "echoworldcore",
                "player_tick.post",
                9001L,
                "standalone-agent7-world-player-tick");
        runtime.recordAgent7LiveHook(
                "echoweathercore",
                "level_tick.post",
                9001L,
                "standalone-agent7-weather-level-tick");
        runtime.recordAgent7LiveHook(
                "echoatmospherecore",
                "level_tick.post",
                9001L,
                "standalone-agent7-atmosphere-level-tick");
        runtime.recordAgent7LiveHook(
                "echobiomecore",
                "level_tick.post",
                9001L,
                "standalone-agent7-biome-level-tick");
        runtime.recordAgent7LiveHook(
                "echostructurecore",
                "level_tick.post",
                9001L,
                "standalone-agent7-structure-level-tick");
        runtime.recordAgent7LiveHook(
                "echospawncore",
                "finalize_spawn",
                9001L,
                "standalone-agent7-spawn-finalize");
        runtime.recordAgent7LiveHook(
                "echodifficultycore",
                "server_starting",
                0L,
                "standalone-agent7-difficulty-server-starting");
        runtime.recordAgent7LiveHook(
                "echostatuscore",
                "server_starting",
                0L,
                "standalone-agent7-status-server-starting");
        require(runtime.agent7LiveHookEvidence().size() == 8
                        && runtime.agent7LiveHookEvidence("echoworldcore", "player_tick.post").liveGameplayHookVerified()
                        && runtime.agent7LiveHookEvidence("echostatuscore", "server_starting").liveGameplayHookVerified(),
                "standalone runtime must emulate exact Agent 7 live-hook evidence for all owned modules");
        require(runtime.worldCoreHazardIdForWeatherType("ASH_STORM").equals("echoworldcore:hazard/toxic_air")
                        && runtime.worldCoreHazardIdForWeatherType("RADIATION_STORM").equals("echoworldcore:hazard/radiation")
                        && runtime.worldCoreHazardIdForWeatherType("CRYO_FRONT").equals("echoworldcore:hazard/cryo_cold")
                        && runtime.worldCoreHazardIdForWeatherType("NEXUS_SIGNAL_STORM").equals("echoworldcore:hazard/nexus_anomaly")
                        && runtime.worldCoreHazardIdForWeatherType("ORBITAL_DEBRIS_SHOWER").equals("echoworldcore:hazard/orbital_exposure")
                        && runtime.worldCoreHazardIdForWeatherType("NONE").equals("echoworldcore:hazard/secure_zone")
                        && runtime.worldCoreHazardIdForWeatherType(null).equals("echoworldcore:hazard/secure_zone"),
                "standalone runtime must emulate WeatherCore to authoritative WorldCore hazard id mapping");
        EchoStandaloneHazardTickDamageResult weatherHazardDamage =
                runtime.applyWorldCoreWeatherHazardTick(
                        "agent7-weather-player",
                        "ASH_STORM",
                        20.0D,
                        50,
                        9002L,
                        new EchoStandaloneDifficulty("echodifficultycore:easy", 1.0D, 0.85D));
        require(weatherHazardDamage.hazardId().equals("echoworldcore:hazard/toxic_air")
                        && weatherHazardDamage.statusEffectId().equals("echostatuscore:status/hazard/toxic_air")
                        && weatherHazardDamage.damaged()
                        && weatherHazardDamage.healthAfter() < 20.0D
                        && weatherHazardDamage.damageApplied() >= 1.1D,
                "standalone runtime must damage/apply status for WeatherCore-mapped WorldCore fallback hazards");
        EchoStandaloneWorldDataCatalogResult catalogState = runtime.materializeWorldDataCatalog(
                new EchoStandaloneWorldDataCatalogRequest(
                        catalog.regionIds(),
                        catalog.hazardIds(),
                        catalog.weatherProfileIds(),
                        catalog.biomeIds(),
                        catalog.structureIds(),
                        catalog.statusEffectIds(),
                        catalog.difficultyIds(),
                        catalog.spawnRuleCount(),
                        catalog.sourceFiles(),
                        "standalone-agent7-data-catalog-runtime-state"
                ));
        EchoStandaloneWorldEffectResult result = runtime.apply(snapshot.effectTick());
        EchoStandaloneRegionTransitionResult enterTransition = runtime.transitionRegion(snapshot.regionEnterRequest());
        EchoStandaloneRegionTransitionResult exitTransition = runtime.transitionRegion(snapshot.regionExitRequest());
        EchoStandaloneWeatherScheduleResult schedule = runtime.scheduleWeather(snapshot.weatherScheduleRequest());
        boolean scheduleRetainedBeforeSurface =
                schedule.equals(runtime.activeWeatherSchedule(schedule.profileId()))
                        && runtime.activeWeatherSchedules().size() == 1;
        EchoStandaloneWeatherStateApplyResult weatherState =
                runtime.applyWeatherState(snapshot.weatherStateApplyRequest());
        EchoStandaloneAtmosphereStateApplyResult atmosphereState =
                runtime.lastAtmosphereStateApplication(weatherState.eventId());
        EchoStandaloneAtmosphereRuntimeProfileResult atmosphereRuntimeProfile =
                runtime.materializeAtmosphereRuntimeProfile(new EchoStandaloneAtmosphereRuntimeProfileRequest(
                        "echoashfallprotocol",
                        "echoatmospherecore:profile/ashfall_storm_visibility",
                        "echoweathercore:weather/ash_storm_active",
                        "echoashfallprotocol:ambience/wasteland_surface",
                        0.82D,
                        0.31D,
                        0.66D,
                        true,
                        "echoatmospherecore:fog/ashfall_active",
                        -9263400,
                        0.58D,
                        6.0D,
                        72.0D,
                        true,
                        "echoatmospherecore:sky_tint/ashfall_active",
                        -6313816,
                        -11905975,
                        -10274248,
                        0.24D,
                        "echoatmospherecore:ambient_particles/ashfall_active",
                        List.of("echoashfallprotocol:particle/fine_ash", "echoashfallprotocol:particle/ember_trace"),
                        0.64D,
                        true,
                        "echorendercore:hook/atmosphere_fog_sky",
                        "echosoundcore:ambience/ash_storm",
                        "echoweathercore:weather_profiles/ash_storm",
                        "echoatmospherecore:ashfall_runtime_packet_consumers",
                        9002L,
                        "standalone-agent7-atmosphere-runtime-profile"));
        require(atmosphereRuntimeProfile.applied()
                        && atmosphereRuntimeProfile.profileId().equals("echoatmospherecore:profile/ashfall_storm_visibility")
                        && atmosphereRuntimeProfile.weatherStateId().equals("echoweathercore:weather/ash_storm_active")
                        && atmosphereRuntimeProfile.stormVisibilityState().get("stormVisibility").equals(0.31D)
                        && atmosphereRuntimeProfile.fogProfileState().get("density").equals(0.58D)
                        && atmosphereRuntimeProfile.skyTintState().get("celestialVisibility").equals(0.24D)
                        && atmosphereRuntimeProfile.ambientParticlesState().toString().contains("echoashfallprotocol:particle/fine_ash")
                        && atmosphereRuntimeProfile.hookRefs().toString().contains("echorendercore:hook/atmosphere_fog_sky")
                        && atmosphereRuntimeProfile.runtimeBindings().toString().contains("sound.ambience")
                        && atmosphereRuntimeProfile.diagnostics().contains("atmosphere.fog_sky.bound")
                        && atmosphereRuntimeProfile.equals(runtime.lastAtmosphereRuntimeProfile(atmosphereRuntimeProfile.profileId()))
                        && atmosphereRuntimeProfile.equals(runtime.weatherAtmosphereRuntimeProfile(atmosphereRuntimeProfile.weatherStateId())),
                "AtmosphereCore runtime profile should materialize visibility/fog/sky/particle bindings");
        EchoStandaloneWorldEffectsRuntime restoredWeatherRuntime = new EchoStandaloneWorldEffectsRuntime();
        EchoStandaloneWeatherStateApplyResult restoredWeatherSurface =
                restoredWeatherRuntime.restoreWeatherSchedule(new EchoStandaloneWeatherScheduleRestoreRequest(
                        schedule,
                        weatherState.eventId(),
                        weatherState.regionId(),
                        weatherState.gameTick(),
                        weatherState.sourceReason(),
                        snapshot.weatherStateApplyRequest().weather(),
                        snapshot.weatherStateApplyRequest().atmosphere()
                ));
        EchoStandaloneAtmosphereStateApplyResult restoredAtmosphereSurface =
                restoredWeatherRuntime.lastAtmosphereSurfaceState(weatherState.regionId());
        EchoStandaloneWeatherStateApplyResult weatherPhaseState =
                runtime.tickScheduledWeather(new EchoStandaloneWeatherScheduleTickRequest(
                        snapshot.weatherPhaseStateApplyRequest().eventId(),
                        snapshot.weatherPhaseStateApplyRequest().regionId(),
                        snapshot.weatherPhaseStateApplyRequest().gameTick(),
                        "standalone-weather-schedule-tick",
                        snapshot.weatherStateApplyRequest().weather(),
                        snapshot.weatherStateApplyRequest().atmosphere()
                ));
        EchoStandaloneWeatherScheduleTickResult weatherPhaseTick =
                runtime.lastWeatherScheduleTick(weatherPhaseState.eventId());
        boolean schedulePhaseRetainedAfterActive =
                runtime.activeWeatherSchedule(schedule.profileId()) != null
                        && runtime.activeWeatherSchedule(schedule.profileId()).phase().equals("ACTIVE");
        EchoStandaloneAtmosphereStateApplyResult atmospherePhaseState =
                runtime.lastAtmosphereStateApplication(weatherPhaseState.eventId());
        EchoStandaloneWeatherStateApplyResult weatherEndedState =
                runtime.tickScheduledWeather(new EchoStandaloneWeatherScheduleTickRequest(
                        snapshot.weatherEndedStateApplyRequest().eventId(),
                        snapshot.weatherEndedStateApplyRequest().regionId(),
                        snapshot.weatherEndedStateApplyRequest().gameTick(),
                        "standalone-weather-schedule-tick",
                        snapshot.weatherStateApplyRequest().weather(),
                        snapshot.weatherStateApplyRequest().atmosphere()
                ));
        EchoStandaloneWeatherScheduleTickResult weatherEndedTick =
                runtime.lastWeatherScheduleTick(weatherEndedState.eventId());
        EchoStandaloneAtmosphereStateApplyResult atmosphereEndedState =
                runtime.lastAtmosphereStateApplication(weatherEndedState.eventId());
        EchoStandaloneHazardTickDamageResult hazardDamage =
                runtime.applyHazardTickDamage(snapshot.hazardTickDamageRequest());
        EchoStandaloneWorldCellSampleResult cellSample =
                runtime.sampleWorldCell(snapshot.worldCellSampleRequest());
        EchoStandaloneHazardTransitionResult hazardEnterTransition =
                runtime.lastHazardTransition(cellSample.playerId());
        EchoStandaloneBiomeHazardOverlayResult biomeOverlay =
                runtime.lastBiomeHazardOverlay(cellSample.playerId());
        EchoStandaloneBiomeAmbientStateResult biomeAmbientState =
                runtime.applyBiomeAmbientState(new EchoStandaloneBiomeAmbientStateRequest(
                        "agent7-player",
                        snapshot.biomeId(),
                        "#echoashfallprotocol:common_wasteland_biomes",
                        "echoashfallprotocol:ambience/wasteland_surface",
                        "echosoundcore:ambience/wasteland_wind",
                        "echoatmospherecore:ambient_particles/ashfall_active",
                        List.of("echoassetcore:ambient/ashfall_dust_motes",
                                "echoassetcore:ambient/radioactive_grit"),
                        "echoatmospherecore:profile/ashfall_storm_visibility",
                        0.72D,
                        6004L,
                        "standalone-biome-ambient-state"));
        EchoStandaloneStructurePoiLookupResult structureLookup =
                runtime.lookupStructurePoi(snapshot.structurePoiLookupRequest());
        EchoStandaloneStatusEffectSaveResult statusSave =
                runtime.persistStatusEffect(snapshot.statusEffectSaveRequest());
        EchoStandaloneStatusEffectApplyResult savedStatusApplication =
                runtime.lastStatusEffectApplication(statusSave.playerId());
        var savedStatusStateBeforeLoad = runtime.activeStatusEffectState(statusSave.playerId(), statusSave.effectId());
        var savedStatusProfileBeforeLoad = runtime.activeStatusProfileState(statusSave.playerId(), statusSave.effectId());
        EchoStandaloneStatusExposureMitigationResult statusExposureMitigation =
                runtime.mitigateStatusExposure(new EchoStandaloneStatusExposureMitigationRequest(
                        statusSave.playerId(),
                        "echostatuscore:exposure/salvage_debris_resisted",
                        statusSave.hazardId(),
                        new EchoStandaloneStatusEffect(
                                statusSave.effectId(),
                                statusSave.durationTicks(),
                                statusSave.amplifier(),
                                statusSave.saveKey()),
                        "ENVIRONMENTAL_HAZARD",
                        1.0D,
                        statusSave.durationTicks(),
                        0.20D,
                        "echostatuscore:resistance/scraplined_boots",
                        0.55D,
                        0.15D,
                        statusSave.gameTick() + 1L,
                        "standalone-status-exposure-mitigation"));
        long refreshedStatusGameTick = statusSave.gameTick() + 2L;
        EchoStandaloneStatusEffectSaveResult refreshedStatusSave =
                runtime.persistStatusEffect(new EchoStandaloneStatusEffectSaveRequest(
                        statusSave.playerId(),
                        statusSave.hazardId(),
                        statusSave.damageApplied() + 1.0F,
                        refreshedStatusGameTick,
                        "standalone-status-refresh-save",
                        new EchoStandaloneStatusEffect(
                                statusSave.effectId(),
                                statusSave.durationTicks(),
                                statusSave.amplifier() + 1,
                                statusSave.saveKey())
                ));
        EchoStandaloneStatusEffectStackingResult statusStacking =
                runtime.lastStatusEffectStacking(statusSave.playerId(), statusSave.effectId());
        var stackedStatusStateBeforeLoad = runtime.activeStatusEffectState(statusSave.playerId(), statusSave.effectId());
        EchoStandaloneStatusEffectLoadResult statusLoad =
                runtime.loadPersistedStatusEffect(new EchoStandalonePersistedStatusEffectLoadRequest(
                        snapshot.statusEffectLoadRequest().playerId(),
                        snapshot.statusEffectLoadRequest().hazardId(),
                        snapshot.statusEffectLoadRequest().saveKey(),
                        snapshot.statusEffectLoadRequest().gameTick(),
                        snapshot.statusEffectLoadRequest().sourceReason()
                ));
        EchoStandaloneStatusEffectApplyResult loadedStatusApplication =
                runtime.lastStatusEffectApplication(statusLoad.playerId());
        EchoStandaloneSpawnRuleEventResult spawnEvent = runtime.planSpawnRuleEvent(snapshot.spawnRuleEventRequest());
        EchoStandaloneWorldEffectsRuntime toxicRuntime = new EchoStandaloneWorldEffectsRuntime();
        EchoStandaloneWorldEffectResult toxicResult = toxicRuntime.apply(toxicSnapshot.effectTick());
        EchoStandaloneRegionTransitionResult toxicEnterTransition =
                toxicRuntime.transitionRegion(toxicSnapshot.regionEnterRequest());
        EchoStandaloneWeatherScheduleResult toxicSchedule =
                toxicRuntime.scheduleWeather(toxicSnapshot.weatherScheduleRequest());
        EchoStandaloneWeatherStateApplyResult toxicWeatherState =
                toxicRuntime.applyWeatherState(toxicSnapshot.weatherStateApplyRequest());
        EchoStandaloneWeatherForecastResult toxicForecast =
                toxicRuntime.forecastWeather(new EchoStandaloneWeatherForecastRequest(
                        "agent7-player",
                        toxicWeatherState.eventId(),
                        toxicSchedule.profileId(),
                        toxicSchedule.type(),
                        "Ashfall Toxic Front",
                        toxicSchedule.phase(),
                        toxicSchedule.severity(),
                        toxicWeatherState.regionId(),
                        toxicSchedule.warningStartTick() + 1L,
                        toxicSchedule.startTick(),
                        toxicSchedule.endTick(),
                        toxicSchedule.startTick() - toxicSchedule.warningStartTick() - 1L,
                        1.45D,
                        1.0D,
                        List.of("echoweathercore:ash_filter_wrap", "echoweathercore:toxic_rain_collector"),
                        "Seek shelter if available.",
                        List.of(
                                "Toxic particulate rising across the swamp route.",
                                "Shelter marker recommended before the front peaks."),
                        "agent7-weather-forecast"));
        EchoStandaloneWeatherRadioUseResult toxicRadio =
                toxicRuntime.useWeatherRadio(new EchoStandaloneWeatherRadioUseRequest(
                        "agent7-player",
                        List.of(toxicSchedule.profileId()),
                        List.of(" - Ashfall Toxic Front [FORECAST, MODERATE]"),
                        true,
                        "MODERATE",
                        toxicForecast.routeRisk(),
                        40,
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-weather-radio"));
        EchoStandaloneWeatherStationUseResult toxicStation =
                toxicRuntime.useWeatherStation(new EchoStandaloneWeatherStationUseRequest(
                        "agent7-player",
                        List.of(toxicSchedule.profileId()),
                        List.of(" - Ashfall Toxic Front [FORECAST]"),
                        true,
                        "MODERATE",
                        toxicForecast.routeRisk(),
                        48,
                        68,
                        48,
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-weather-station"));
        EchoStandaloneWeatherWarningResult toxicWarning =
                toxicRuntime.issueWeatherWarning(new EchoStandaloneWeatherWarningRequest(
                        toxicWeatherState.eventId(),
                        toxicSchedule.profileId(),
                        toxicWeatherState.regionId(),
                        toxicSchedule.phase(),
                        "forecast_broadcast",
                        "Toxic particulate rising across the swamp route.",
                        List.of("agent7-player"),
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-weather-warning"));
        EchoStandaloneEmergencySirenUseResult toxicSiren =
                toxicRuntime.useEmergencySiren(new EchoStandaloneEmergencySirenUseRequest(
                        "agent7-player",
                        List.of(toxicSchedule.profileId()),
                        true,
                        toxicSchedule.phase(),
                        "MODERATE",
                        48,
                        68,
                        48,
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-emergency-siren"));
        EchoStandaloneWeatherExposureMitigationResult toxicWeatherMitigation =
                toxicRuntime.mitigateWeatherExposure(new EchoStandaloneWeatherExposureMitigationRequest(
                        "agent7-player",
                        toxicSchedule.profileId(),
                        "TOXIC_RAIN",
                        true,
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-weather-exposure-mitigation",
                        new EchoStandaloneWeatherExposureModifier(
                                "TOXIC_RAIN", 1.35D, 1.0D, 1.5D, 1.0D, 1.0D, 1.45D),
                        new EchoStandaloneWeatherExposureModifier(
                                "TOXIC_RAIN", 0.5D, 1.0D, 0.25D, 1.0D, 1.0D, 0.5D)
                ));
        EchoStandaloneClimateSensorReadResult toxicClimateSensor =
                toxicRuntime.readClimateSensor(new EchoStandaloneClimateSensorReadRequest(
                        "agent7-player",
                        List.of(toxicSchedule.profileId()),
                        true,
                        0.38D,
                        1.0D,
                        number(toxicWeatherMitigation, "filterDrainMultiplier"),
                        number(toxicWeatherMitigation, "toxicExposureMultiplier"),
                        number(toxicWeatherMitigation, "routeRiskModifier"),
                        48,
                        68,
                        48,
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-climate-sensor"));
        EchoStandaloneWeatherStateApplyResult toxicWeatherEndedState =
                toxicRuntime.tickScheduledWeather(new EchoStandaloneWeatherScheduleTickRequest(
                        toxicSnapshot.weatherEndedStateApplyRequest().eventId(),
                        toxicSnapshot.weatherEndedStateApplyRequest().regionId(),
                        toxicSnapshot.weatherEndedStateApplyRequest().gameTick(),
                        "standalone-weather-schedule-tick",
                        toxicSnapshot.weatherStateApplyRequest().weather(),
                        toxicSnapshot.weatherStateApplyRequest().atmosphere()
                ));
        EchoStandaloneShelterReport toxicShelterReport =
                toxicRuntime.reportShelter(new EchoStandaloneShelterReportRequest(
                        "agent7-player", 48, 68, 48, toxicSchedule.warningStartTick() + 1L,
                        "standalone-weather-shelter-report"));
        EchoStandaloneWeatherRouteRiskResult toxicRouteRiskResult =
                toxicRuntime.evaluateWeatherRouteRisk(new EchoStandaloneWeatherRouteRiskRequest(
                        "agent7-player",
                        toxicSchedule.profileId(),
                        "MODERATE",
                        number(toxicWeatherMitigation, "routeRiskModifier"),
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-weather-route-risk"
                ));
        String toxicRouteRisk = toxicRouteRiskResult.risk();
        EchoStandaloneRouteWarningPostUseResult toxicRouteWarningPost =
                toxicRuntime.useRouteWarningPost(new EchoStandaloneRouteWarningPostUseRequest(
                        "agent7-player",
                        toxicSchedule.profileId(),
                        "MODERATE",
                        toxicRouteRisk,
                        number(toxicWeatherMitigation, "routeRiskModifier"),
                        48,
                        68,
                        48,
                        toxicSchedule.warningStartTick() + 1L,
                        "agent7-route-warning-post"
                ));
        EchoStandaloneHazardTickDamageResult toxicHazardDamage =
                toxicRuntime.applyHazardTickDamage(toxicSnapshot.hazardTickDamageRequest());
        EchoStandaloneWorldCellSampleResult toxicCellSample =
                toxicRuntime.sampleWorldCell(toxicSnapshot.worldCellSampleRequest());
        EchoStandaloneStructurePoiLookupResult toxicStructureLookup =
                toxicRuntime.lookupStructurePoi(toxicSnapshot.structurePoiLookupRequest());
        EchoStandaloneStatusEffectSaveResult toxicStatusSave =
                toxicRuntime.persistStatusEffect(toxicSnapshot.statusEffectSaveRequest());
        EchoStandaloneStatusEffectLoadResult toxicStatusLoad =
                toxicRuntime.loadPersistedStatusEffect(new EchoStandalonePersistedStatusEffectLoadRequest(
                        toxicSnapshot.statusEffectLoadRequest().playerId(),
                        toxicSnapshot.statusEffectLoadRequest().hazardId(),
                        toxicSnapshot.statusEffectLoadRequest().saveKey(),
                        toxicSnapshot.statusEffectLoadRequest().gameTick(),
                        toxicSnapshot.statusEffectLoadRequest().sourceReason()
                ));
        EchoStandaloneSpawnRuleEventResult toxicSpawnEvent =
                toxicRuntime.planSpawnRuleEvent(toxicSnapshot.spawnRuleEventRequest());

        require(snapshot.worldState().cellCount() == 36,
                "standalone simulation must materialize cells from loaded world definitions");
        require(snapshot.worldState().hazardCount() == 1,
                "standalone simulation must materialize a hazard field from loaded world definitions");
        require(snapshot.worldState().poiCount() == 1,
                "standalone simulation must materialize POI lookup data from loaded world definitions");
        require(catalog.regionIds().size() >= 8
                        && catalog.regionIds().contains("echoashfallprotocol:crash_zone_wasteland")
                        && catalog.regionIds().contains("echoashfallprotocol:toxic_swamp"),
                "catalog audit must load Agent 7 world regions from data folders");
        require(catalog.hazardIds().size() >= 12
                        && catalog.hazardIds().contains(snapshot.hazardId())
                        && catalog.hazardIds().contains("echoashfallprotocol:hazard/toxic_ash"),
                "catalog audit must load Agent 7 hazard definitions from data folders");
        require(catalog.weatherProfileIds().size() >= 9
                        && catalog.weatherProfileIds().contains(snapshot.weatherId())
                        && catalog.weatherProfileIds().contains("echoashfallprotocol:ashfall_toxic_front"),
                "catalog audit must load Agent 7 weather profiles from data folders");
        require(catalog.biomeIds().size() >= 9
                        && catalog.biomeIds().contains(snapshot.biomeId()),
                "catalog audit must load biome definitions from data folders");
        require(catalog.structureIds().size() >= 25
                        && catalog.structureIds().contains(snapshot.structureId()),
                "catalog audit must load structure definitions from data folders");
        require(catalog.statusEffectIds().contains("echostatuscore:status/salvage_debris")
                        && catalog.difficultyIds().contains("echodifficultycore:easy")
                        && catalog.difficultyIds().contains("echodifficultycore:hard")
                        && catalog.spawnRuleCount() > 0,
                "catalog audit must derive status effects, difficulty rules, and spawn rules from loaded data");
        require(catalogState.loaded()
                        && catalogState.regionCount() == catalog.regionIds().size()
                        && catalogState.hazardCount() == catalog.hazardIds().size()
                        && catalogState.weatherProfileCount() == catalog.weatherProfileIds().size()
                        && catalogState.structureCount() == catalog.structureIds().size()
                        && catalogState.spawnRuleCount() == catalog.spawnRuleCount()
                        && runtime.lastWorldDataCatalogResult().equals(catalogState),
                "catalog audit must materialize standalone AdapterCore world data catalog state");
        require(snapshot.sourceFiles().contains(
                        "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_regions/ashfall_crash_zone_wasteland.json"),
                "region input must be loaded from Ashfall world data");
        require(snapshot.sourceFiles().contains(
                        "addons/echoworldcore/src/main/resources/data/echoworldcore/echoworldcore/world_hazards/hazard/salvage_debris.json"),
                "hazard input must be loaded from WorldCore hazard data");
        require(snapshot.sourceFiles().contains(
                        "addons/echoweathercore/src/main/resources/data/echoweathercore/weather_profiles/ash_storm.json"),
                "weather input must be loaded from WeatherCore data");
        require(toxicSnapshot.sourceFiles().contains(
                        "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_regions/ashfall_toxic_swamp.json")
                        && toxicSnapshot.sourceFiles().contains(
                                "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_hazards/hazard/toxic_ash.json")
                        && toxicSnapshot.sourceFiles().contains(
                                "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoweathercore/weather_profiles/ashfall_toxic_front.json")
                        && toxicSnapshot.sourceFiles().contains(
                                "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/worldgen/biome/toxic_swamp.json"),
                "second slice must be loaded from Toxic Swamp world, hazard, weather, and biome data");

        require(result.activeRegionId().equals(snapshot.regionId()),
                "region enter detection must match the Agent 7 reference tick");
        require(enterTransition.regionEntered() && enterTransition.eventType().equals("ENTER"),
                "region transition must emit an enter event from no previous region");
        require(runtime.lastRegionTransition(enterTransition.playerId()).equals(exitTransition)
                        && runtime.activeRegionId(enterTransition.playerId()).isBlank()
                        && runtime.activeRegionIds().isEmpty()
                        && runtime.startedRegionMissions(enterTransition.playerId()).contains(
                                "echoashfallprotocol:mission/secure_crash_outpost"),
                "region transitions must retain mission-start state and clear active region after exit");
        require(exitTransition.regionExited() && exitTransition.eventType().equals("EXIT"),
                "region transition must emit an exit event when the current region clears");
        require(exitTransition.previousRegionId().equals(snapshot.regionId()) && exitTransition.currentRegionId().isBlank(),
                "region exit transition must preserve previous region and clear current region");
        require(result.activeHazardId().equals(snapshot.hazardId()),
                "hazard detection must match the Agent 7 reference tick");
        require(result.healthBefore() == 20.0D && result.healthAfter() == 18.0D,
                "hazard damage must apply difficulty-scaled tick damage");
        require(hazardDamage.damaged()
                        && hazardDamage.damageApplied() == 2.0D
                        && hazardDamage.healthAfter() == result.healthAfter(),
                "hazard tick damage contract must match the world effect health mutation");
        require(hazardDamage.difficultyId().equals("echodifficultycore:easy")
                        && hazardDamage.hazardMultiplier() == 1.0D,
                "hazard tick damage contract must preserve the loaded difficulty profile");
        require(runtime.lastHazardTickDamage(hazardDamage.playerId()).equals(hazardDamage)
                        && runtime.playerHealth(hazardDamage.playerId()) == hazardDamage.healthAfter(),
                "hazard tick damage must persist standalone player health state");
        require(cellSample.inRegion() && cellSample.inHazard(),
                "world cell sample must detect the loaded region cell and hazard field");
        require(cellSample.activeRegionId().equals(snapshot.regionId())
                        && cellSample.activeHazardId().equals(snapshot.hazardId()),
                "world cell sample must preserve active region and hazard ids");
        require(cellSample.biomeProfileId().equals(snapshot.biomeId())
                        && cellSample.poiId().equals("echoashfallprotocol:poi/drop_pod"),
                "world cell sample must preserve biome and POI ids from loaded definitions");
        require(cellSample.cellKey().equals(snapshot.worldState().worldId() + ":32:68:32"),
                "world cell sample must derive a stable world cell key");
        require(runtime.lastWorldCellSample(cellSample.playerId()).equals(cellSample)
                        && runtime.sampledWorldCell(cellSample.cellKey()).equals(cellSample)
                        && runtime.sampledHazardField(cellSample.activeHazardId()).equals(cellSample)
                        && runtime.sampledWorldCells().size() == 1
                        && runtime.sampledHazardFields().size() == 1,
                "world cell sample must retain sampled cell and hazard-field state");
        require(hazardEnterTransition != null
                        && hazardEnterTransition.eventType().equals("ENTER")
                        && hazardEnterTransition.hazardEntered()
                        && hazardEnterTransition.currentHazardId().equals(cellSample.activeHazardId())
                        && hazardEnterTransition.statusEffects().contains(snapshot.statusEffectSaveRequest().statusEffect().id())
                        && runtime.activeHazardId(cellSample.playerId()).equals(cellSample.activeHazardId()),
                "world cell sample must materialize hazard-field enter state");
        String chunkKey = cellSample.worldId() + ":chunk:" + Math.floorDiv(cellSample.x(), 16)
                + ":" + Math.floorDiv(cellSample.z(), 16);
        var adapterChunkState = runtime.lastWorldChunkState(cellSample.playerId());
        require(adapterChunkState != null
                        && adapterChunkState.chunkKey().equals(chunkKey)
                        && adapterChunkState.lastCellKey().equals(cellSample.cellKey())
                        && adapterChunkState.activeHazardId().equals(cellSample.activeHazardId())
                        && runtime.sampledWorldChunkState(chunkKey).equals(adapterChunkState)
                        && runtime.sampledWorldChunkStates().size() == 1,
                "world cell sample must materialize retained AdapterCore chunk state");
        var chunkState = runtime.sampledWorldChunk(chunkKey);
        require(chunkState != null
                        && chunkState.lastCellKey().equals(cellSample.cellKey())
                        && chunkState.activeHazardId().equals(cellSample.activeHazardId())
                        && chunkState.chunkX() == Math.floorDiv(cellSample.x(), 16)
                        && runtime.sampledWorldChunks().size() == 1,
                "world cell sample must materialize retained standalone chunk state");
        var hazardFieldState = runtime.sampledHazardFieldState(cellSample.activeHazardId());
        var adapterHazardFieldState = runtime.lastHazardFieldState(cellSample.playerId());
        require(adapterHazardFieldState != null
                        && adapterHazardFieldState.hazardId().equals(cellSample.activeHazardId())
                        && adapterHazardFieldState.lastCellKey().equals(cellSample.cellKey())
                        && adapterHazardFieldState.sampledInside()
                        && runtime.sampledHazardFieldStateResult(cellSample.activeHazardId())
                                .equals(adapterHazardFieldState)
                        && runtime.sampledHazardFieldStateResults().size() == 1,
                "world cell sample must materialize retained AdapterCore hazard-field state");
        require(hazardFieldState != null
                        && hazardFieldState.lastCellKey().equals(cellSample.cellKey())
                        && hazardFieldState.sampledInside()
                        && runtime.sampledHazardFieldStates().size() == 1,
                "world cell sample must materialize retained standalone hazard-field state");
        require(biomeOverlay != null
                        && biomeOverlay.active()
                        && biomeOverlay.visibleOnHud()
                        && biomeOverlay.biomeProfileId().equals(cellSample.biomeProfileId())
                        && biomeOverlay.hazardId().equals(cellSample.activeHazardId())
                        && biomeOverlay.cellKey().equals(cellSample.cellKey())
                        && runtime.sampledBiomeHazardOverlay(cellSample.cellKey()).equals(biomeOverlay)
                        && runtime.sampledBiomeHazardOverlays().size() == 1,
                "world cell sample must resolve and retain BiomeCore hazard overlay state");
        require(biomeAmbientState.applied()
                        && biomeAmbientState.biomeProfileId().equals(cellSample.biomeProfileId())
                        && biomeAmbientState.hudState().get("ambience").equals(
                                "echoashfallprotocol:ambience/wasteland_surface")
                        && biomeAmbientState.audioState().get("cue").equals(
                                "echosoundcore:ambience/wasteland_wind")
                        && biomeAmbientState.renderState().get("particleProfile").equals(
                                "echoatmospherecore:ambient_particles/ashfall_active")
                        && biomeAmbientState.ambientState().get("adapterCoreContract").equals(
                                "echobiomecore:biome/ambient_state")
                        && biomeAmbientState.runtimeBindings().toString().contains("sound.ambience")
                        && runtime.lastBiomeAmbientState("agent7-player").equals(biomeAmbientState)
                        && runtime.biomeAmbientState(biomeAmbientState.biomeProfileId()).equals(biomeAmbientState),
                "BiomeCore ambient profile must materialize retained HUD/audio/render state");
        EchoStandaloneWorldCellSampleRequest sampleRequest = snapshot.worldCellSampleRequest();
        runtime.sampleWorldCell(new EchoStandaloneWorldCellSampleRequest(
                sampleRequest.playerId(),
                sampleRequest.worldId(),
                sampleRequest.x(),
                sampleRequest.y(),
                sampleRequest.z(),
                sampleRequest.gameTick() + 1L,
                "standalone-hazard-field-exit-sample",
                sampleRequest.region(),
                new EchoStandaloneHazard(
                        sampleRequest.hazard().id(),
                        sampleRequest.hazard().type(),
                        sampleRequest.x() + 96,
                        sampleRequest.z() + 96,
                        sampleRequest.hazard().radius(),
                        sampleRequest.hazard().damagePerTick(),
                        sampleRequest.hazard().statusEffectId()),
                sampleRequest.biome(),
                sampleRequest.structure()));
        EchoStandaloneHazardTransitionResult hazardExitTransition =
                runtime.lastHazardTransition(cellSample.playerId());
        require(hazardExitTransition.eventType().equals("EXIT")
                        && hazardExitTransition.hazardExited()
                        && hazardExitTransition.previousHazardId().equals(cellSample.activeHazardId())
                        && hazardExitTransition.currentHazardId().isBlank()
                        && runtime.activeHazardId(cellSample.playerId()).isBlank()
                        && runtime.activeHazardIds().isEmpty(),
                "world cell sample must materialize hazard-field exit state");
        require(result.missionEvents().contains("echoashfallprotocol:mission/secure_crash_outpost"),
                "region entry must trigger the mission event");
        require(result.statusEffects().contains("echostatuscore:status/salvage_debris"),
                "hazard tick must apply the status effect");
        require(result.hudState().get("weather").equals("ASH STORM: Ash front detected. Visibility loss expected."),
                "weather must feed HUD state");
        require(result.audioState().get("cue").equals("echoashfallprotocol:event.ash_storm"),
                "weather must feed audio state");
        require(result.renderState().get("weatherProfile").equals("echorendercore:hazard/ash_storm"),
                "weather must feed render state");
        require(weatherState.applied()
                        && weatherState.hudState().get("weather").equals(result.hudState().get("weather"))
                        && weatherState.audioState().get("cue").equals(result.audioState().get("cue"))
                        && weatherState.renderState().get("weatherProfile").equals(result.renderState().get("weatherProfile")),
                "weather state apply contract must match HUD/audio/render world effect state");
        require(atmosphereState != null
                        && atmosphereState.applied()
                        && atmosphereState.renderState().get("atmosphere").equals(weatherState.renderState().get("atmosphere"))
                        && atmosphereState.renderState().get("visibility").equals(weatherState.renderState().get("visibility"))
                        && atmosphereState.runtimeBindings().get("moduleId").equals("echoatmospherecore"),
                "atmosphere state apply contract must retain visibility/particle/sky-fog runtime state");
        require(restoredWeatherRuntime.activeWeatherSchedule(schedule.profileId()).equals(schedule)
                        && restoredWeatherRuntime.lastWeatherSurfaceState(weatherState.regionId()).equals(restoredWeatherSurface)
                        && restoredWeatherSurface.hudState().equals(weatherState.hudState())
                        && restoredWeatherSurface.audioState().equals(weatherState.audioState())
                        && restoredWeatherSurface.renderState().equals(weatherState.renderState()),
                "weather restore must rebuild the saved schedule and live HUD/audio/render surface");
        require(restoredAtmosphereSurface != null
                        && restoredAtmosphereSurface.renderState().equals(atmosphereState.renderState())
                        && restoredAtmosphereSurface.runtimeBindings().equals(atmosphereState.runtimeBindings()),
                "weather restore must rebuild the saved AtmosphereCore runtime surface");
        require(weatherPhaseState.applied()
                        && weatherPhaseState.phase().equals("ACTIVE")
                        && weatherPhaseState.hudState().get("phase").equals("ACTIVE")
                        && weatherPhaseState.audioState().get("phase").equals("ACTIVE")
                        && weatherPhaseState.sourceReason().equals("standalone-weather-schedule-tick"),
                "weather schedule tick must refresh active HUD/audio/render state");
        require(weatherPhaseTick != null
                        && weatherPhaseTick.previousPhase().equals("FORECAST")
                        && weatherPhaseTick.phase().equals("ACTIVE")
                        && weatherPhaseTick.phaseChanged()
                        && weatherPhaseTick.active()
                        && !weatherPhaseTick.ended()
                        && schedulePhaseRetainedAfterActive,
                "weather schedule tick must materialize and retain the ACTIVE schedule phase");
        require(weatherPhaseState.hudState().get("weather").equals(weatherState.hudState().get("weather"))
                        && weatherPhaseState.renderState().get("weatherProfile").equals(
                                weatherState.renderState().get("weatherProfile"))
                        && weatherPhaseState.renderState().get("atmosphere").equals(
                                weatherState.renderState().get("atmosphere")),
                "weather phase transition must preserve profile-driven HUD/render data");
        require(atmospherePhaseState != null
                        && atmospherePhaseState.phase().equals("ACTIVE")
                        && atmospherePhaseState.renderState().get("skyFog").equals("weather_phase:ACTIVE"),
                "weather phase transition must refresh AtmosphereCore runtime state");
        require(weatherEndedState.applied()
                        && weatherEndedState.phase().equals("ENDED")
                        && weatherEndedState.hudState().get("weather").equals("ASH STORM: CLEAR")
                        && weatherEndedState.sourceReason().equals("standalone-weather-schedule-tick"),
                "weather schedule tick must clear the HUD weather state when ended");
        require(weatherEndedTick != null
                        && weatherEndedTick.previousPhase().equals("ACTIVE")
                        && weatherEndedTick.phase().equals("ENDED")
                        && weatherEndedTick.phaseChanged()
                        && weatherEndedTick.ended()
                        && !weatherEndedTick.active(),
                "weather schedule tick must materialize the ENDED schedule phase");
        require(weatherEndedState.audioState().get("cue").equals("echoweathercore:event.clear")
                        && weatherEndedState.renderState().get("weatherProfile").equals("echorendercore:weather/clear")
                        && weatherEndedState.renderState().get("visibility").equals(1.0D),
                "weather ended transition must clear audio/render/atmosphere state");
        require(atmosphereEndedState != null
                        && atmosphereEndedState.phase().equals("ENDED")
                        && atmosphereEndedState.renderState().get("atmosphere").equals("echoatmospherecore:clear_field")
                        && atmosphereEndedState.renderState().get("particles").equals("minecraft:empty"),
                "weather ended transition must clear AtmosphereCore runtime state");
        require(scheduleRetainedBeforeSurface && runtime.activeWeatherSchedule(schedule.profileId()) == null,
                "weather schedule must be retained before surface application and retired after ENDED phase");
        require(runtime.lastWeatherStateApplication(weatherEndedState.eventId()).equals(weatherEndedState)
                        && runtime.lastWeatherSurfaceState(weatherEndedState.regionId()).equals(weatherEndedState)
                        && runtime.lastWeatherSurfaceStates().size() == 1,
                "weather state application must retain the live standalone HUD/audio/render surface");
        require(runtime.lastAtmosphereStateApplication(weatherEndedState.eventId()).equals(atmosphereEndedState)
                        && runtime.lastAtmosphereSurfaceState(weatherEndedState.regionId()).equals(atmosphereEndedState)
                        && runtime.lastAtmosphereSurfaceStates().size() == 1,
                "atmosphere state application must retain the live standalone AtmosphereCore surface");
        require(result.worldLookup().get("poiId").equals("echoashfallprotocol:poi/drop_pod"),
                "structure/POI lookup must survive the runtime translation");
        require(structureLookup.structureId().equals(snapshot.structureId())
                        && structureLookup.poiId().equals("echoashfallprotocol:poi/drop_pod"),
                "structure/POI lookup must preserve loaded structure and POI ids");
        require(structureLookup.inRange() && structureLookup.distanceSquared() == 4632L,
                "structure/POI lookup must resolve the drop pod within scan range");
        require(structureLookup.markerId().equals("echoworldcore:marker/drop_pod/30_0_30"),
                "structure/POI lookup must derive the same marker id as WorldCore native scans");
        require(runtime.resolvedPoiMarker(structureLookup.markerId()) != null
                        && runtime.resolvedPoiMarker(structureLookup.markerId()).equals(structureLookup)
                        && runtime.resolvedPoiMarkers().containsKey(structureLookup.markerId()),
                "structure/POI lookup must persist an in-range marker in standalone world state");
        var poiMarkerState = runtime.lastPoiMarkerState(structureLookup.playerId());
        require(poiMarkerState != null
                        && poiMarkerState.markerId().equals(structureLookup.markerId())
                        && poiMarkerState.structureId().equals(structureLookup.structureId())
                        && poiMarkerState.poiId().equals(structureLookup.poiId())
                        && poiMarkerState.markerPersisted()
                        && runtime.resolvedPoiMarkerState(structureLookup.markerId()).equals(poiMarkerState)
                        && runtime.resolvedPoiMarkerStates().size() == 1,
                "structure/POI lookup must persist AdapterCore marker state");
        var poiState = runtime.resolvedPoiState(structureLookup.markerId());
        require(poiState != null
                        && poiState.structureId().equals(structureLookup.structureId())
                        && poiState.poiId().equals(structureLookup.poiId())
                        && poiState.distanceSquared() == structureLookup.distanceSquared()
                        && poiState.lastGameTick() == structureLookup.gameTick()
                        && runtime.resolvedPoiStates().containsKey(structureLookup.markerId()),
                "structure/POI lookup must retain detailed standalone POI runtime state");
        var structureDiscovery = runtime.lastStructureDiscoveryState(structureLookup.playerId());
        require(structureDiscovery != null
                        && structureDiscovery.markerId().equals(structureLookup.markerId())
                        && structureDiscovery.discoveryState().equals("DISCOVERED")
                        && structureDiscovery.discovered()
                        && structureDiscovery.holomapMarkerActive()
                        && runtime.resolvedStructureDiscoveryState(structureLookup.markerId()).equals(structureDiscovery)
                        && runtime.resolvedStructureDiscoveryStates().size() == 1,
                "structure/POI lookup must materialize StructureCore discovery state");
        require(result.spawnEvent().get("entityId").equals("echoashfallprotocol:rad_zombie"),
                "spawn rule event must survive the runtime translation");
        require(spawnEvent.entityId().equals("echoashfallprotocol:rad_zombie"),
                "spawn rule event must preserve the loaded biome spawn entity");
        require(spawnEvent.scaledBudget() == 2 && spawnEvent.spawnCount() == 2,
                "spawn rule event must apply difficulty-scaled budget");
        require(spawnEvent.difficultyId().equals("echodifficultycore:easy"),
                "spawn rule event must preserve the loaded mission difficulty profile");
        require(runtime.lastSpawnRuleEvent(spawnEvent.playerId()).equals(spawnEvent)
                        && runtime.activeSpawnPopulation(spawnEvent.regionId(), spawnEvent.ruleId()) == spawnEvent.scaledBudget()
                        && runtime.activeSpawnPopulations().size() == 1,
                "spawn rule event must persist standalone spawn-zone runtime state");
        var adapterSpawnZoneState = runtime.lastSpawnZoneState(spawnEvent.playerId());
        require(adapterSpawnZoneState != null
                        && adapterSpawnZoneState.zoneKey().equals(spawnEvent.regionId() + "|" + spawnEvent.ruleId())
                        && adapterSpawnZoneState.entityId().equals(spawnEvent.entityId())
                        && adapterSpawnZoneState.scaledBudget() == spawnEvent.scaledBudget()
                        && adapterSpawnZoneState.activePopulation() == spawnEvent.activeMobCount() + spawnEvent.spawnCount()
                        && runtime.activeSpawnZoneStateResult(spawnEvent.regionId(), spawnEvent.ruleId())
                                .equals(adapterSpawnZoneState)
                        && runtime.activeSpawnZoneStateResults().size() == 1,
                "spawn rule event must retain AdapterCore spawn-zone state");
        var spawnZoneState = runtime.activeSpawnZoneState(spawnEvent.regionId(), spawnEvent.ruleId());
        require(spawnZoneState != null
                        && spawnZoneState.entityId().equals(spawnEvent.entityId())
                        && spawnZoneState.scaledBudget() == spawnEvent.scaledBudget()
                        && spawnZoneState.activePopulation() == spawnEvent.activeMobCount() + spawnEvent.spawnCount()
                        && spawnZoneState.difficultyId().equals(spawnEvent.difficultyId())
                        && spawnZoneState.lastGameTick() == spawnEvent.gameTick()
                        && runtime.activeSpawnZoneStates().containsKey(spawnEvent.regionId() + "|" + spawnEvent.ruleId()),
                "spawn rule event must retain detailed standalone spawn-zone simulation state");
        require(runtime.activeDifficultyProfile(spawnEvent.playerId()).id().equals(spawnEvent.difficultyId())
                        && runtime.regionDifficultyProfile(spawnEvent.regionId()).spawnMultiplier() == spawnEvent.spawnMultiplier()
                        && runtime.regionDifficultyProfile(spawnEvent.regionId()).hazardMultiplier() == hazardDamage.hazardMultiplier(),
                "difficulty modifiers must persist as standalone runtime state");
        var difficultySelection = runtime.selectDifficultyProfile(new EchoStandaloneDifficultyProfileSelectionRequest(
                spawnEvent.playerId(),
                spawnEvent.regionId(),
                "echoashfallprotocol:mission/secure_crash_outpost",
                "easy",
                spawnEvent.gameTick(),
                "standalone-difficulty-profile-selection"));
        require(difficultySelection.selected()
                        && difficultySelection.difficultyId().equals(spawnEvent.difficultyId())
                        && difficultySelection.selectedDifficulty().equals("easy")
                        && difficultySelection.hazardMultiplier() == hazardDamage.hazardMultiplier()
                        && difficultySelection.spawnMultiplier() == spawnEvent.spawnMultiplier()
                        && runtime.regionDifficultyProfileSelection(spawnEvent.regionId()).equals(difficultySelection),
                "DifficultyCore profile selection must materialize standalone hazard/spawn multiplier state");
        var difficultyApplicationState = runtime.regionDifficultyApplicationState(spawnEvent.regionId());
        require(difficultyApplicationState != null
                        && difficultyApplicationState.difficultyId().equals(spawnEvent.difficultyId())
                        && difficultyApplicationState.appliedHazardId().equals(hazardDamage.hazardId())
                        && difficultyApplicationState.scaledHazardDamage() == hazardDamage.damageApplied()
                        && difficultyApplicationState.appliedSpawnRuleId().equals(spawnEvent.ruleId())
                        && difficultyApplicationState.scaledSpawnBudget() == spawnEvent.scaledBudget()
                        && runtime.activeDifficultyApplicationState(spawnEvent.playerId()).equals(difficultyApplicationState)
                        && runtime.difficultyApplicationStates().containsKey(spawnEvent.regionId()),
                "difficulty modifier application must retain hazard and spawn effects as standalone runtime state");
        var adapterDifficultyApplication = runtime.regionDifficultyApplicationResult(spawnEvent.regionId());
        require(adapterDifficultyApplication != null
                        && adapterDifficultyApplication.difficultyId().equals(spawnEvent.difficultyId())
                        && adapterDifficultyApplication.appliedHazardId().equals(hazardDamage.hazardId())
                        && adapterDifficultyApplication.scaledHazardDamage() == hazardDamage.damageApplied()
                        && adapterDifficultyApplication.appliedSpawnRuleId().equals(spawnEvent.ruleId())
                        && adapterDifficultyApplication.scaledSpawnBudget() == spawnEvent.scaledBudget()
                        && runtime.activeDifficultyApplicationResult(spawnEvent.playerId()).equals(adapterDifficultyApplication)
                        && runtime.difficultyApplicationResults().containsKey(spawnEvent.regionId())
                        && adapterDifficultyApplication.applied(),
                "difficulty modifier application must materialize AdapterCore difficulty application state");
        require(result.savedStatusState().containsKey("echoworldcore.hazard.salvage_debris.status"),
                "status effects must be represented as save/load state");
        require(statusSave.saved()
                        && statusSave.effectId().equals("echostatuscore:status/salvage_debris")
                        && statusSave.saveKey().equals("echoworldcore.hazard.salvage_debris.status"),
                "status effect save must persist the hazard status effect");
        require(runtime.lastStatusEffectSave(statusSave.playerId()).equals(refreshedStatusSave)
                        && runtime.activeStatusEffects(statusSave.playerId()).contains(statusSave.effectId()),
                "status effect save must retain active standalone status state");
        require(savedStatusApplication != null
                        && savedStatusApplication.applied()
                        && !savedStatusApplication.loaded()
                        && savedStatusApplication.effectId().equals(statusSave.effectId())
                        && savedStatusApplication.activeStatusState().get("moduleId").equals("echoworldcore")
                        && savedStatusApplication.expiresAtTick() == statusSave.gameTick() + statusSave.durationTicks(),
                "status effect save must route through the standalone status apply runtime state");
        require(savedStatusStateBeforeLoad != null
                        && savedStatusStateBeforeLoad.durationTicks() == statusSave.durationTicks()
                        && savedStatusStateBeforeLoad.amplifier() == statusSave.amplifier()
                        && savedStatusStateBeforeLoad.expiresAtTick() == statusSave.gameTick() + statusSave.durationTicks(),
                "status effect save must retain timed standalone status duration/amplifier state");
        require(savedStatusProfileBeforeLoad != null
                        && savedStatusProfileBeforeLoad.statusKind().equals("ENVIRONMENTAL_HAZARD")
                        && savedStatusProfileBeforeLoad.severity().equals("LOW")
                        && savedStatusProfileBeforeLoad.stackingPolicy().equals("REFRESH_DURATION")
                        && savedStatusProfileBeforeLoad.exposureIntensity() == 1.0D
                        && savedStatusProfileBeforeLoad.persisted()
                        && !savedStatusProfileBeforeLoad.loaded(),
                "status effect save must retain StatusCore-style profile and exposure runtime state");
        require(statusExposureMitigation.applied()
                        && !statusExposureMitigation.immune()
                        && statusExposureMitigation.originalIntensity() == 1.0D
                        && Math.abs(statusExposureMitigation.effectiveIntensity() - 0.45D) < 0.0000001D
                        && statusExposureMitigation.effectiveDurationTicks() == 90
                        && Math.abs(statusExposureMitigation.effectiveAccumulationPerSecond() - 0.09D) < 0.0000001D
                        && statusExposureMitigation.exposureState().get("adapterCoreContract").equals(
                                "echostatuscore:status/exposure_mitigation")
                        && runtime.lastStatusExposureMitigation(statusSave.playerId()).equals(statusExposureMitigation)
                        && runtime.activeStatusExposureState(statusExposureMitigation.exposureId()).equals(
                                statusExposureMitigation),
                "status exposure mitigation must retain StatusCore resistance runtime state");
        require(statusStacking != null
                        && statusStacking.hadPrevious()
                        && statusStacking.refreshed()
                        && statusStacking.amplifierUpgraded()
                        && !statusStacking.stacked()
                        && statusStacking.durationTicks() == statusSave.durationTicks()
                        && statusStacking.amplifier() == statusSave.amplifier() + 1
                        && statusStacking.appliedGameTick() == refreshedStatusGameTick
                        && statusStacking.expiresAtTick() == refreshedStatusGameTick + statusSave.durationTicks()
                        && runtime.activeStatusEffects(statusSave.playerId()).size() == 1,
                "status effect stacking must refresh duration and upgrade amplifier in standalone runtime state");
        require(stackedStatusStateBeforeLoad != null
                        && stackedStatusStateBeforeLoad.durationTicks() == statusStacking.durationTicks()
                        && stackedStatusStateBeforeLoad.amplifier() == statusStacking.amplifier()
                        && stackedStatusStateBeforeLoad.expiresAtTick() == statusStacking.expiresAtTick(),
                "status effect stacking must update active standalone status state");
        require(statusSave.savedStatusState().containsKey("echoworldcore.hazard.salvage_debris.status"),
                "status effect save must expose the same saved payload key as the world effect result");
        require(statusLoad.loaded()
                        && statusLoad.effectId().equals(statusSave.effectId())
                        && statusLoad.durationTicks() == refreshedStatusSave.durationTicks()
                        && statusLoad.amplifier() == refreshedStatusSave.amplifier(),
                "status effect load must rehydrate the saved hazard status effect");
        require(statusLoad.damageApplied() == refreshedStatusSave.damageApplied()
                        && statusLoad.savedGameTick() == refreshedStatusSave.gameTick(),
                "status effect load must preserve saved damage and saved tick values");
        require(runtime.lastStatusEffectLoad(statusLoad.playerId()).equals(statusLoad)
                        && runtime.activeStatusEffects(statusLoad.playerId()).contains(statusLoad.effectId())
                        && runtime.activeStatusEffects(statusLoad.playerId()).size() == 1,
                "status effect load must retain rehydrated standalone status state");
        require(loadedStatusApplication != null
                        && loadedStatusApplication.applied()
                        && loadedStatusApplication.loaded()
                        && loadedStatusApplication.effectId().equals(statusLoad.effectId())
                        && loadedStatusApplication.appliedGameTick() == statusLoad.loadedGameTick(),
                "status effect load must route through the standalone status apply runtime state");
        var loadedStatusState = runtime.activeStatusEffectState(statusLoad.playerId(), statusLoad.effectId());
        var loadedStatusProfile = runtime.activeStatusProfileState(statusLoad.playerId(), statusLoad.effectId());
        require(loadedStatusProfile != null
                        && loadedStatusProfile.effectId().equals(statusLoad.effectId())
                        && loadedStatusProfile.hazardId().equals(statusLoad.hazardId())
                        && loadedStatusProfile.loaded()
                        && runtime.activeStatusProfileStates(statusLoad.playerId()).containsKey(statusLoad.effectId()),
                "status effect load must retain rehydrated StatusCore-style profile state");
        boolean statusExpired = runtime.tickStatusEffects(statusLoad.playerId(),
                statusLoad.loadedGameTick() + statusLoad.durationTicks() + 1L) == 0
                && runtime.activeStatusEffectStates(statusLoad.playerId()).isEmpty()
                && runtime.activeStatusProfileStates(statusLoad.playerId()).isEmpty();
        var statusExpiry = runtime.lastStatusEffectExpiry(statusLoad.playerId(), statusLoad.effectId());
        require(loadedStatusState != null
                        && loadedStatusState.loaded()
                        && loadedStatusState.appliedGameTick() == statusLoad.loadedGameTick()
                        && loadedStatusState.expiresAtTick() == statusLoad.loadedGameTick() + statusLoad.durationTicks(),
                "status effect load must retain timed standalone rehydrated status state");
        require(statusExpired,
                "status effect runtime state must expire by duration");
        require(statusExpiry != null
                        && statusExpiry.expired()
                        && !statusExpiry.retained()
                        && statusExpiry.effectId().equals(statusLoad.effectId())
                        && statusExpiry.expiresAtTick() == loadedStatusState.expiresAtTick(),
                "status effect expiry must materialize AdapterCore-style expiry state");
        require(schedule.scheduled(),
                "weather schedule must create a forecast from the loaded weather profile");
        require(schedule.profileId().equals(snapshot.weatherId()),
                "weather schedule must preserve the loaded profile id");
        require(schedule.warningStartTick() == 6000L && schedule.startTick() == 8400L && schedule.endTick() == 20400L,
                "weather schedule must preserve warning/start/end ticks from the data-backed profile");
        require(schedule.phase().equals("FORECAST"),
                "weather schedule must start as a forecast event");
        require(toxicResult.activeRegionId().equals("echoashfallprotocol:toxic_swamp")
                        && toxicResult.activeHazardId().equals("echoashfallprotocol:hazard/toxic_ash")
                        && toxicResult.hudState().get("weather").equals(
                                "ASHFALL TOXIC FRONT: Toxic front approaching. Return to the pod route or use a marked shelter."),
                "second slice must execute toxic swamp region, hazard, and weather HUD state");
        require(toxicEnterTransition.regionEntered()
                        && toxicRuntime.startedRegionMissions(toxicEnterTransition.playerId()).contains(
                                "echoashfallprotocol:mission/first_relay_station_route"),
                "second slice region transition must retain route mission state");
        require(toxicSchedule.profileId().equals("echoashfallprotocol:ashfall_toxic_front")
                        && toxicSchedule.warningStartTick() == 9000L
                        && toxicSchedule.endTick() == 23400L
                        && toxicWeatherEndedState.phase().equals("ENDED")
                        && toxicRuntime.activeWeatherSchedules().isEmpty(),
                "second slice must schedule and retire the toxic front weather event");
        require(toxicWeatherState.renderState().get("weatherProfile").equals(
                        "echorendercore:hazard/ashfall_toxic_front")
                        && toxicWeatherEndedState.renderState().get("weatherProfile").equals("echorendercore:weather/clear"),
                "second slice weather must update render state and clear after end");
        require(toxicWeatherMitigation.mitigated()
                        && Math.abs(number(toxicWeatherMitigation, "toxicExposureMultiplier") - 0.375D) < 0.0001D
                        && Math.abs(number(toxicWeatherMitigation, "filterDrainMultiplier") - 0.675D) < 0.0001D
                        && Math.abs(number(toxicWeatherMitigation, "routeRiskModifier") - 0.725D) < 0.0001D
                        && toxicRuntime.lastWeatherExposureMitigation("agent7-player", toxicSchedule.profileId())
                                .equals(toxicWeatherMitigation),
                "second slice weather exposure mitigation must retain sheltered countermeasure state");
        require(toxicShelterReport.recorded()
                        && toxicRuntime.lastShelterReport("agent7-player").equals(toxicShelterReport),
                "second slice weather shelter report must retain standalone shelter state");
        require(toxicRouteRisk.equals("SAFE")
                        && toxicRouteRiskResult.riskScore() == number(toxicWeatherMitigation, "routeRiskModifier")
                        && toxicRuntime.lastWeatherRouteRisk("agent7-player", toxicSchedule.profileId())
                                .equals(toxicRouteRiskResult),
                "second slice sheltered route risk must materialize AdapterCore route-risk state");
        require(toxicRouteWarningPost.delivered()
                        && toxicRouteWarningPost.risk().equals("SAFE")
                        && toxicRouteWarningPost.message().equals("Route Warning Post: Risk is SAFE")
                        && toxicRouteWarningPost.hudState().get("routeWarning").equals(
                                "Route Warning Post: Risk is SAFE")
                        && toxicRouteWarningPost.audioState().get("cue").equals("echoweathercore:route_warning/safe")
                        && toxicRouteWarningPost.renderState().get("warningPostOverlay").equals(
                                "echoweathercore:route_warning_post/safe")
                        && toxicRuntime.lastRouteWarningPostUse("agent7-player").equals(toxicRouteWarningPost)
                        && toxicRuntime.routeWarningPostUses().size() == 1,
                "second slice route warning post must retain player-facing HUD/audio/render warning state");
        require(toxicHazardDamage.difficultyId().equals("echodifficultycore:hard")
                        && toxicHazardDamage.damageApplied() > hazardDamage.damageApplied()
                        && Math.abs(toxicHazardDamage.damageApplied() - 7.44D) < 0.0001D
                        && Math.abs(toxicRuntime.playerHealth(toxicHazardDamage.playerId()) - 12.56D) < 0.0001D,
                "second slice toxic ash damage must apply hard difficulty scaling");
        require(toxicCellSample.inRegion()
                        && toxicCellSample.inHazard()
                        && toxicCellSample.biomeProfileId().equals("echoashfallprotocol:toxic_swamp")
                        && toxicCellSample.cellKey().equals(
                                "echoashfallprotocol:ashfall_toxic_swamp_definition_world:48:68:48"),
                "second slice must materialize a toxic swamp cell and hazard field");
        require(toxicStructureLookup.structureId().equals("echoashfallprotocol:toxic_swamp")
                        && toxicStructureLookup.poiId().equals("echoashfallprotocol:poi/toxic_swamp")
                        && toxicRuntime.resolvedPoiMarkers().containsKey(toxicStructureLookup.markerId()),
                "second slice must resolve and persist a toxic swamp POI marker");
        var toxicLoadedStatusProfile =
                toxicRuntime.activeStatusProfileState(toxicStatusLoad.playerId(), toxicStatusLoad.effectId());
        require(toxicStatusSave.effectId().equals("echostatuscore:status/toxic_ash")
                        && toxicStatusLoad.loaded()
                        && toxicLoadedStatusProfile != null
                        && toxicLoadedStatusProfile.statusKind().equals("TOXIN")
                        && toxicLoadedStatusProfile.severity().equals("HIGH"),
                "second slice must save/load toxic ash status and retain StatusCore profile state");
        require(toxicSpawnEvent.ruleId().equals("echospawncore:spawn/rad_zombie_toxic_swamp")
                        && toxicSpawnEvent.scaledBudget() == 3
                        && toxicRuntime.regionDifficultyApplicationState(toxicSpawnEvent.regionId())
                                .appliedSpawnRuleId().equals(toxicSpawnEvent.ruleId()),
                "second slice must apply hard difficulty spawn scaling to the toxic swamp spawn rule");
        require(toxicForecast.forecasted()
                        && toxicForecast.weatherId().equals("echoashfallprotocol:ashfall_toxic_front")
                        && toxicForecast.phase().equals("FORECAST")
                        && toxicForecast.routeRisk().equals("WATCH")
                        && toxicForecast.recommendedGear().size() == 2
                        && toxicRuntime.lastWeatherForecast("agent7-player", toxicSchedule.profileId()).equals(toxicForecast),
                "second slice must materialize and retain WeatherCore forecast state");
        require(toxicRadio.delivered()
                        && toxicRadio.forecastsAvailable()
                        && toxicRadio.weatherIds().contains(toxicSchedule.profileId())
                        && toxicRadio.routeRisk().equals("WATCH")
                        && toxicRadio.messageLines().get(0).equals("Weather Radio - Regional Forecast:")
                        && toxicRadio.messageLines().contains(" - Ashfall Toxic Front [FORECAST, MODERATE]")
                        && toxicRadio.hudState().get("weatherRadio").equals(
                                "Weather Radio - Regional Forecast:")
                        && toxicRadio.audioState().get("cue").equals(
                                "echoweathercore:weather_radio/forecast")
                        && toxicRadio.renderState().get("readout").equals(
                                "echoweathercore:weather_radio/forecast")
                        && toxicRuntime.lastWeatherRadioUse("agent7-player").equals(toxicRadio)
                        && toxicRuntime.weatherRadioPlayers().size() == 1,
                "second slice must materialize and retain Weather Radio broadcast state");
        require(toxicStation.delivered()
                        && toxicStation.forecastsAvailable()
                        && toxicStation.weatherIds().contains(toxicSchedule.profileId())
                        && toxicStation.routeRisk().equals("WATCH")
                        && toxicStation.messageLines().get(0).equals("=== Weather Station Forecast ===")
                        && toxicStation.messageLines().contains(" - Ashfall Toxic Front [FORECAST]")
                        && toxicStation.hudState().get("weatherStation").equals(
                                "=== Weather Station Forecast ===")
                        && toxicStation.audioState().get("cue").equals(
                                "echoweathercore:weather_station/forecast")
                        && toxicStation.renderState().get("readout").equals(
                                "echoweathercore:weather_station/forecast")
                        && toxicRuntime.lastWeatherStationUse("agent7-player").equals(toxicStation)
                        && toxicRuntime.weatherStationPositions().size() == 1,
                "second slice must materialize and retain Weather Station forecast state");
        require(toxicWarning.delivered()
                        && toxicWarning.weatherId().equals(toxicSchedule.profileId())
                        && toxicWarning.phase().equals("FORECAST")
                        && toxicWarning.channel().equals("forecast_broadcast")
                        && toxicWarning.recipientPlayerIds().contains("agent7-player")
                        && toxicWarning.hudState().get("weatherWarning").toString().contains("Toxic particulate")
                        && toxicWarning.audioState().get("cue").equals("echoweathercore:warning/ashfall_toxic_front")
                        && toxicRuntime.lastWeatherWarning(toxicWeatherState.eventId()).equals(toxicWarning)
                        && toxicRuntime.lastWeatherWarning("agent7-player", toxicWeatherState.eventId()).equals(toxicWarning),
                "second slice must materialize and retain WeatherCore warning delivery state");
        require(toxicSiren.delivered()
                        && toxicSiren.activeWeatherDetected()
                        && toxicSiren.weatherIds().contains(toxicSchedule.profileId())
                        && toxicSiren.message().equals("Emergency Siren: ACTIVE WEATHER DETECTED")
                        && toxicSiren.hudState().get("emergencySiren").equals(
                                "Emergency Siren: ACTIVE WEATHER DETECTED")
                        && toxicSiren.audioState().get("cue").equals("echoweathercore:siren/active_weather")
                        && toxicSiren.renderState().get("overlay").equals("echoweathercore:emergency_siren/active")
                        && toxicRuntime.lastEmergencySirenUse("agent7-player").equals(toxicSiren)
                        && toxicRuntime.emergencySirenPosts().size() == 1,
                "second slice must materialize and retain Emergency Siren warning state");
        require(toxicClimateSensor.delivered()
                        && toxicClimateSensor.sheltered()
                        && toxicClimateSensor.weatherIds().contains(toxicSchedule.profileId())
                        && toxicClimateSensor.visibilityPercent() == 38
                        && toxicClimateSensor.scannerReliabilityPercent() == 100
                        && toxicClimateSensor.hudState().get("climateSensor").equals(
                                "Visibility 38% / Scanner 100%")
                        && toxicClimateSensor.audioState().get("cue").equals(
                                "echoweathercore:climate_sensor/sheltered")
                        && toxicClimateSensor.renderState().get("readout").equals(
                                "echoweathercore:climate_sensor/readout")
                        && toxicRuntime.lastClimateSensorReading("agent7-player").equals(toxicClimateSensor)
                        && toxicRuntime.climateSensorPositions().size() == 1,
                "second slice must materialize and retain Climate Sensor weather modifier state");

        System.out.println("agent7 world parity smoke PASS region="
                + result.activeRegionId()
                + " hazard="
                + result.activeHazardId()
                + " healthAfter="
                + result.healthAfter()
                + " retainedHealth="
                + runtime.playerHealth(hazardDamage.playerId())
                + " hazardDamage="
                + hazardDamage.damageApplied()
                + " regionExit="
                + exitTransition.previousRegionId()
                + " regionStateCleared="
                + runtime.activeRegionId(enterTransition.playerId()).isBlank()
                + " missionStateStarted="
                + runtime.startedRegionMissions(enterTransition.playerId()).size()
                + " weatherEnd="
                + schedule.endTick()
                + " weatherState="
                + weatherState.phase()
                + " weatherPhase="
                + weatherPhaseState.phase()
                + " weatherEnded="
                + weatherEndedState.phase()
                + " weatherTicked="
                + weatherPhaseState.sourceReason().equals("standalone-weather-schedule-tick")
                + " weatherScheduleTick="
                + weatherPhaseTick.phase()
                + " weatherScheduleEndedTick="
                + weatherEndedTick.phase()
                + " weatherSurfaceRetained="
                + runtime.lastWeatherSurfaceState(weatherEndedState.regionId()).applied()
                + " activeWeatherSchedules="
                + runtime.activeWeatherSchedules().size()
                + " weatherReloadSurfaceRestored="
                + restoredWeatherSurface.equals(restoredWeatherRuntime.lastWeatherSurfaceState(weatherState.regionId()))
                + " atmosphereSurfaceRetained="
                + runtime.lastAtmosphereSurfaceState(weatherEndedState.regionId()).applied()
                + " atmosphereReloadSurfaceRestored="
                + restoredAtmosphereSurface.equals(restoredWeatherRuntime.lastAtmosphereSurfaceState(weatherState.regionId()))
                + " atmosphereProfile="
                + atmosphereRuntimeProfile.stormVisibilityState().get("stormVisibility")
                + " atmosphereProfileState="
                + (runtime.lastAtmosphereRuntimeProfile(atmosphereRuntimeProfile.profileId()) == null ? 0 : 1)
                + " cell="
                + cellSample.cellKey()
                + " sampledCells="
                + runtime.sampledWorldCells().size()
                + " sampledHazards="
                + runtime.sampledHazardFields().size()
                + " sampledChunks="
                + runtime.sampledWorldChunks().size()
                + " chunkAdapterState="
                + runtime.sampledWorldChunkStates().size()
                + " hazardFieldState="
                + runtime.sampledHazardFieldStates().size()
                + " hazardAdapterState="
                + runtime.sampledHazardFieldStateResults().size()
                + " hazardEnter="
                + hazardEnterTransition.eventType()
                + " hazardTransition="
                + runtime.lastHazardTransition(cellSample.playerId()).eventType()
                + " activeHazards="
                + runtime.activeHazardIds().size()
                + " biomeOverlayActive="
                + biomeOverlay.active()
                + " biomeOverlayState="
                + runtime.sampledBiomeHazardOverlays().size()
                + " biomeAmbientCue="
                + biomeAmbientState.audioState().get("cue")
                + " biomeAmbientState="
                + (runtime.lastBiomeAmbientState("agent7-player") == null ? 0 : 1)
                + " poi="
                + structureLookup.poiId()
                + " poiMarkerPersisted="
                + runtime.resolvedPoiMarkers().containsKey(structureLookup.markerId())
                + " poiMarkerState="
                + runtime.resolvedPoiMarkerStates().size()
                + " poiState="
                + runtime.resolvedPoiStates().size()
                + " structureDiscovery="
                + runtime.resolvedStructureDiscoveryState(structureLookup.markerId()).discoveryState()
                + " structureDiscoveryState="
                + runtime.resolvedStructureDiscoveryStates().size()
                + " status="
                + statusSave.effectId()
                + " statusLoad="
                + statusLoad.effectId()
                + " persistedStatusLoad="
                + statusLoad.loaded()
                + " statusApplyRuntime="
                + savedStatusApplication.applied()
                + " statusStacking="
                + statusStacking.stackingPolicy()
                + " statusStackDuration="
                + statusStacking.durationTicks()
                + " statusStackAmplifier="
                + statusStacking.amplifier()
                + " statusLoadApplyRuntime="
                + loadedStatusApplication.applied()
                + " activeStatusEffects="
                + (statusExpired ? 0 : runtime.activeStatusEffects(statusLoad.playerId()).size())
                + " statusExpired="
                + statusExpired
                + " statusExpiryAdapterState="
                + (statusExpiry != null && statusExpiry.expired())
                + " statusProfile="
                + loadedStatusProfile.statusKind()
                + " statusResistanceIntensity="
                + statusExposureMitigation.effectiveIntensity()
                + " statusResistanceDuration="
                + statusExposureMitigation.effectiveDurationTicks()
                + " statusResistanceState="
                + (runtime.activeStatusExposureState(statusExposureMitigation.exposureId()) == null ? 0 : 1)
                + " spawnBudget="
                + spawnEvent.scaledBudget()
                + " spawnStateActive="
                + runtime.activeSpawnPopulation(spawnEvent.regionId(), spawnEvent.ruleId())
                + " spawnZoneAdapterState="
                + runtime.activeSpawnZoneStateResults().size()
                + " spawnZoneState="
                + runtime.activeSpawnZoneStates().size()
                + " difficultyState="
                + runtime.activeDifficultyProfile(spawnEvent.playerId()).id()
                + " difficultySelection="
                + runtime.regionDifficultyProfileSelection(spawnEvent.regionId()).difficultyId()
                + " difficultyApplied="
                + runtime.regionDifficultyApplicationState(spawnEvent.regionId()).appliedSpawnRuleId()
                + " difficultyAdapterState="
                + runtime.regionDifficultyApplicationResult(spawnEvent.regionId()).appliedSpawnRuleId()
                + " secondRegion="
                + toxicResult.activeRegionId()
                + " secondHazard="
                + toxicResult.activeHazardId()
                + " secondWeather="
                + toxicSchedule.profileId()
                + " secondWeatherEnded="
                + toxicWeatherEndedState.phase()
                + " weatherCountermeasureMitigated="
                + toxicWeatherMitigation.mitigated()
                + " shelterReportRecorded="
                + toxicShelterReport.recorded()
                + " mitigatedRouteRisk="
                + toxicRouteRisk
                + " routeRiskAdapterState="
                + toxicRuntime.lastWeatherRouteRisk("agent7-player", toxicSchedule.profileId()).risk()
                + " routeWarningPost="
                + toxicRouteWarningPost.risk()
                + " routeWarningPostState="
                + toxicRuntime.routeWarningPostUses().size()
                + " agent7StandaloneLiveHooks="
                + runtime.agent7LiveHookEvidence().size()
                + " weatherHazardMap="
                + runtime.worldCoreHazardIdForWeatherType("ASH_STORM")
                + " weatherHazardDamage="
                + weatherHazardDamage.damageApplied()
                + " weatherHazardStatus="
                + weatherHazardDamage.statusEffectId()
                + " weatherForecast="
                + toxicForecast.routeRisk()
                + " weatherForecastState="
                + toxicRuntime.lastWeatherForecasts().size()
                + " weatherRadio="
                + toxicRadio.routeRisk()
                + " weatherRadioState="
                + toxicRuntime.weatherRadioPlayers().size()
                + " weatherStation="
                + toxicStation.routeRisk()
                + " weatherStationState="
                + toxicRuntime.weatherStationPositions().size()
                + " weatherWarningDelivered="
                + toxicWarning.delivered()
                + " weatherWarningState="
                + toxicRuntime.lastWeatherWarnings().size()
                + " emergencySiren="
                + toxicSiren.activeWeatherDetected()
                + " emergencySirenState="
                + toxicRuntime.emergencySirenPosts().size()
                + " climateSensorVisibility="
                + toxicClimateSensor.visibilityPercent()
                + " climateSensorState="
                + toxicRuntime.climateSensorPositions().size()
                + " secondDamage="
                + toxicHazardDamage.damageApplied()
                + " secondHealth="
                + toxicRuntime.playerHealth(toxicHazardDamage.playerId())
                + " secondCell="
                + toxicCellSample.cellKey()
                + " secondPoi="
                + toxicStructureLookup.poiId()
                + " secondStatus="
                + toxicStatusLoad.effectId()
                + " secondStatusProfile="
                + toxicLoadedStatusProfile.statusKind()
                + " secondSpawnBudget="
                + toxicSpawnEvent.scaledBudget()
                + " secondDifficultyApplied="
                + toxicRuntime.regionDifficultyApplicationState(toxicSpawnEvent.regionId()).appliedSpawnRuleId()
                + " catalogRegions="
                + catalog.regionIds().size()
                + " catalogHazards="
                + catalog.hazardIds().size()
                + " catalogWeather="
                + catalog.weatherProfileIds().size()
                + " catalogBiomes="
                + catalog.biomeIds().size()
                + " catalogStructures="
                + catalog.structureIds().size()
                + " catalogSpawnRules="
                + catalog.spawnRuleCount()
                + " catalogAdapterState="
                + catalogState.loaded()
                + " sources="
                + snapshot.sourceFiles().size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static double number(EchoStandaloneWeatherExposureMitigationResult result, String key) {
        Object value = result.modifierState().get(key);
        return value instanceof Number number ? number.doubleValue() : Double.NaN;
    }
}
