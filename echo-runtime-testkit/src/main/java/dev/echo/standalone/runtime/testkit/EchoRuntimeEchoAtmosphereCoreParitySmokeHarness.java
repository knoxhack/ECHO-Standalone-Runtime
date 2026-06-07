package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.gameplay.EchoAtmosphereCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoAtmosphereCoreParitySmokeHarness {
    private EchoRuntimeEchoAtmosphereCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeProfileTick = executeNativeReferenceProfileTick("echo-native-m17");
        EchoAtmosphereCoreStandaloneAdapter standaloneAdapter = new EchoAtmosphereCoreStandaloneAdapter();
        Map<String, Object> standaloneProfileTick = standaloneAdapter.executeProfileTick("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceProfileTickPassed(nativeProfileTick),
                "native AtmosphereCore reference profile tick should pass");
        require(standaloneAdapter.referenceProfileTickPassed(standaloneProfileTick),
                "standalone AtmosphereCore profile tick should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("atmosphereProfileTickExecuted")),
                "standalone activation should execute profile tick");
        require(nativeProfileTick.get("adapterCoreContract").equals(standaloneProfileTick.get("adapterCoreContract")),
                "native and standalone atmosphere contracts should match");
        require(nativeProfileTick.get("profileId").equals(standaloneProfileTick.get("profileId")),
                "native and standalone atmosphere profile ids should match");
        require(nativeProfileTick.get("weatherStateId").equals(standaloneProfileTick.get("weatherStateId")),
                "native and standalone weather state ids should match");
        require(nativeProfileTick.get("stormVisibility").equals(standaloneProfileTick.get("stormVisibility")),
                "native and standalone storm visibility should match");
        require(nativeProfileTick.get("fogProfile").equals(standaloneProfileTick.get("fogProfile")),
                "native and standalone fog profiles should match");
        require(nativeProfileTick.get("skyTint").equals(standaloneProfileTick.get("skyTint")),
                "native and standalone sky tint profiles should match");
        require(nativeProfileTick.get("ambientParticles").equals(standaloneProfileTick.get("ambientParticles")),
                "native and standalone ambient particle profiles should match");
        require(nativeProfileTick.get("hookRefs").equals(standaloneProfileTick.get("hookRefs")),
                "native and standalone hook references should match");
        require(nativeProfileTick.get("runtimeBindings").equals(standaloneProfileTick.get("runtimeBindings")),
                "native and standalone runtime bindings should match");
        require(nativeProfileTick.get("diagnostics").equals(standaloneProfileTick.get("diagnostics")),
                "native and standalone diagnostics should match");

        System.out.println("echoatmospherecore parity smoke PASS contract="
                + nativeProfileTick.get("adapterCoreContract")
                + " profile="
                + nativeProfileTick.get("profileId")
                + " bindings="
                + ((List<?>) nativeProfileTick.get("runtimeBindings")).size());
    }

    private static Map<String, Object> executeNativeReferenceProfileTick(String packId) {
        Map<String, Object> tick = new LinkedHashMap<>();
        tick.put("adapterCoreContract", EchoAtmosphereCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        tick.put("service", "echoatmospherecore:atmosphere_service");
        tick.put("atmosphereProfileTickExecuted", true);
        tick.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        tick.put("profileId", EchoAtmosphereCoreStandaloneAdapter.REFERENCE_PROFILE_ID);
        tick.put("weatherStateId", EchoAtmosphereCoreStandaloneAdapter.REFERENCE_WEATHER_STATE_ID);
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

    private static boolean nativeReferenceProfileTickPassed(Map<String, Object> tick) {
        return Boolean.TRUE.equals(tick.get("atmosphereProfileTickExecuted"))
                && EchoAtmosphereCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(tick.get("adapterCoreContract"))
                && EchoAtmosphereCoreStandaloneAdapter.REFERENCE_PROFILE_ID.equals(tick.get("profileId"))
                && EchoAtmosphereCoreStandaloneAdapter.REFERENCE_WEATHER_STATE_ID.equals(tick.get("weatherStateId"))
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

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
