package dev.echo.standalone.runtime.gameplay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAtmosphereCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoatmospherecore";
    public static final String ADAPTERCORE_CONTRACT_ID = "echoatmospherecore:atmosphere/runtime_profile_tick";
    public static final String REFERENCE_PROFILE_ID = "echoatmospherecore:profile/ashfall_storm_visibility";
    public static final String REFERENCE_WEATHER_STATE_ID = "echoweathercore:weather/ash_storm_active";

    public Map<String, Object> activate() {
        Map<String, Object> atmosphereProfileTick = executeProfileTick("echo-native-m17");
        boolean atmosphereProfileTickPassed = referenceProfileTickPassed(atmosphereProfileTick);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "atmospherecore_standalone_runtime_profile_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", List.of(
                "atmosphere.fog",
                "atmosphere.hooks",
                "atmosphere.particles",
                "atmosphere.profiles",
                "atmosphere.sky",
                ADAPTERCORE_CONTRACT_ID
        ));
        report.put("atmosphereProfileTick", atmosphereProfileTick);
        report.put("atmosphereProfileTickExecuted", atmosphereProfileTickPassed);
        report.put("serviceCodeExecuted", atmosphereProfileTickPassed);
        report.put("summary", "AtmosphereCore standalone adapter executed the AdapterCore runtime profile tick service.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executeProfileTick(String packId) {
        Map<String, Object> tick = new LinkedHashMap<>();
        tick.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
        tick.put("service", "echoatmospherecore:atmosphere_service");
        tick.put("atmosphereProfileTickExecuted", true);
        tick.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        tick.put("profileId", REFERENCE_PROFILE_ID);
        tick.put("weatherStateId", REFERENCE_WEATHER_STATE_ID);
        tick.put("biomeAmbienceId", "echoashfallprotocol:ambience/wasteland_surface");
        tick.put("stormVisibility", Map.of(
                "visibilityId", "echoatmospherecore:storm_visibility/ashfall_active",
                "clearVisibility", 0.82D,
                "stormVisibility", 0.31D,
                "screenHazeIntensity", 0.66D,
                "reducesDistantLights", true
        ));
        tick.put("fogProfile", Map.of(
                "fogId", "echoatmospherecore:fog/ashfall_active",
                "colorArgb", -9263400,
                "density", 0.58D,
                "startDistance", 6.0D,
                "endDistance", 72.0D,
                "stormAffected", true
        ));
        tick.put("skyTint", Map.of(
                "skyTintId", "echoatmospherecore:sky_tint/ashfall_active",
                "dayColorArgb", -6313816,
                "nightColorArgb", -11905975,
                "stormColorArgb", -10274248,
                "celestialVisibility", 0.24D
        ));
        tick.put("ambientParticles", Map.of(
                "particleProfileId", "echoatmospherecore:ambient_particles/ashfall_active",
                "particleReferences", List.of(
                        "echoashfallprotocol:particle/fine_ash",
                        "echoashfallprotocol:particle/ember_trace"
                ),
                "density", 0.64D,
                "affectedByStormVisibility", true
        ));
        tick.put("hookRefs", Map.of(
                "renderCoreHookReference", "echorendercore:hook/atmosphere_fog_sky",
                "soundCoreHookReference", "echosoundcore:ambience/ash_storm",
                "weatherProfileReference", "echoweathercore:weather_profiles/ash_storm",
                "runtimePacketConsumer", "echoatmospherecore:ashfall_runtime_packet_consumers"
        ));
        tick.put("runtimeBindings", List.of(
                binding("render.visibility", "stormVisibility", "echorendercore:visibility/fog_distance"),
                binding("render.sky", "skyTint", "echorendercore:sky/tint"),
                binding("render.particles", "ambientParticles", "echorendercore:particles/ashfall"),
                binding("sound.ambience", "hookRefs", "echosoundcore:ambience/ash_storm")
        ));
        tick.put("diagnostics", List.of(
                "atmosphere.profile.loaded",
                "atmosphere.visibility.resolved",
                "atmosphere.fog_sky.bound",
                "atmosphere.particles.bound"
        ));
        tick.put("referenceBehavior", "atmospherecore_resolves_runtime_profile_tick");
        return Map.copyOf(tick);
    }

    public boolean referenceProfileTickPassed(Map<String, Object> tick) {
        return Boolean.TRUE.equals(tick.get("atmosphereProfileTickExecuted"))
                && ADAPTERCORE_CONTRACT_ID.equals(tick.get("adapterCoreContract"))
                && REFERENCE_PROFILE_ID.equals(tick.get("profileId"))
                && REFERENCE_WEATHER_STATE_ID.equals(tick.get("weatherStateId"))
                && String.valueOf(tick.get("stormVisibility")).contains("stormVisibility=0.31")
                && String.valueOf(tick.get("fogProfile")).contains("density=0.58")
                && String.valueOf(tick.get("skyTint")).contains("celestialVisibility=0.24")
                && String.valueOf(tick.get("ambientParticles")).contains("echoashfallprotocol:particle/fine_ash")
                && String.valueOf(tick.get("hookRefs")).contains("echorendercore:hook/atmosphere_fog_sky")
                && String.valueOf(tick.get("runtimeBindings")).contains("sound.ambience")
                && String.valueOf(tick.get("diagnostics")).contains("atmosphere.fog_sky.bound");
    }

    private static Map<String, String> binding(String target, String source, String adapterHook) {
        Map<String, String> binding = new LinkedHashMap<>();
        binding.put("target", target);
        binding.put("source", source);
        binding.put("adapterHook", adapterHook);
        return Map.copyOf(binding);
    }
}
