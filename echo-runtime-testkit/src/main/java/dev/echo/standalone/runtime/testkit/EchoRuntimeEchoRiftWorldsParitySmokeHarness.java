package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoRiftWorldsStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoRiftWorldsParitySmokeHarness {
    private EchoRuntimeEchoRiftWorldsParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeLifecycle = executeNativeReferenceLifecycle("echo-native-m17");
        EchoRiftWorldsStandaloneAdapter standaloneAdapter = new EchoRiftWorldsStandaloneAdapter();
        Map<String, Object> standaloneLifecycle = standaloneAdapter.executeLifecycle("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceLifecyclePassed(nativeLifecycle),
                "native RiftWorlds reference lifecycle should pass");
        require(standaloneAdapter.referenceLifecyclePassed(standaloneLifecycle),
                "standalone RiftWorlds lifecycle should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("pocketLifecycleExecuted")),
                "standalone activation should execute pocket lifecycle");
        require(nativeLifecycle.get("adapterCoreContract").equals(standaloneLifecycle.get("adapterCoreContract")),
                "native and standalone RiftWorlds contracts should match");
        require(nativeLifecycle.get("riftSnapshot").equals(standaloneLifecycle.get("riftSnapshot")),
                "native and standalone rift snapshots should match");
        require(nativeLifecycle.get("chamberPlan").equals(standaloneLifecycle.get("chamberPlan")),
                "native and standalone chamber plans should match");
        require(nativeLifecycle.get("hazardProfile").equals(standaloneLifecycle.get("hazardProfile")),
                "native and standalone hazard profiles should match");
        require(nativeLifecycle.get("storyRoute").equals(standaloneLifecycle.get("storyRoute")),
                "native and standalone story routes should match");
        require(nativeLifecycle.get("exitPlan").equals(standaloneLifecycle.get("exitPlan")),
                "native and standalone exit plans should match");

        System.out.println("echoriftworlds parity smoke PASS contract="
                + nativeLifecycle.get("adapterCoreContract")
                + " rift="
                + EchoRiftWorldsStandaloneAdapter.REFERENCE_RIFT_ID
                + " chamberSteps="
                + ((List<?>) nativeLifecycle.get("chamberPlan")).size());
    }

    private static Map<String, Object> executeNativeReferenceLifecycle(String packId) {
        Map<String, Object> lifecycle = new LinkedHashMap<>();
        lifecycle.put("adapterCoreContract", EchoRiftWorldsStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        lifecycle.put("service", "echoriftworlds:pocket_rift_lifecycle_service");
        lifecycle.put("pocketLifecycleExecuted", true);
        lifecycle.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        lifecycle.put("riftSnapshot", Map.of(
                "riftId", EchoRiftWorldsStandaloneAdapter.REFERENCE_RIFT_ID,
                "riftEvent", "echoriftworlds:rift_event/cache_echo",
                "dimension", EchoRiftWorldsStandaloneAdapter.REFERENCE_DIMENSION_ID,
                "center", "144,72,-32",
                "returnDimension", "minecraft:overworld",
                "returnPosition", "144,64,-32",
                "expiresAfterTicks", 24000L,
                "state", "active"
        ));
        lifecycle.put("chamberPlan", List.of(
                chamberStep("clear_chamber_volume", "CLEAR_VOLUME", "7x6x7", false),
                chamberStep("build_bedrock_floor", "PLACE_BOUNDARY", "bedrock_floor", false),
                chamberStep("seal_tinted_glass_shell", "PLACE_BOUNDARY", "tinted_glass_shell", false),
                chamberStep("place_amethyst_anchor", "PLACE_ANCHOR", "amethyst_center", false),
                chamberStep("spawn_cache_shards", "SPAWN_REWARD", "amethyst_shard_x2", false)
        ));
        lifecycle.put("hazardProfile", Map.of(
                "hazardId", "echoriftworlds:hazard/rift_static",
                "pressure", 3,
                "effects", List.of("slow_falling", "absorption", "night_vision"),
                "hostileSignal", "endermite_echo",
                "destructiveActions", 0
        ));
        lifecycle.put("storyRoute", Map.of(
                "chapter", "signalos:chapter/cache_handoff",
                "storyFlag", "echoriftworlds:story_flag/cache_echo_seen",
                "missionSubject", "echoriftworlds:pocket_rift",
                "missionAction", "pocket_encounter"
        ));
        lifecycle.put("exitPlan", List.of(
                exitAction("return_anchor", "TELEPORT_RETURN", "minecraft:overworld@144,65,-32"),
                exitAction("expire_instance", "EXPIRE_AFTER_TTL", "24000"),
                exitAction("cleanup_chamber", "CLEANUP_VOLUME", "post_return")
        ));
        lifecycle.put("diagnostics", List.of(
                "riftworlds.rift.triggered",
                "riftworlds.chamber.planned",
                "riftworlds.hazard.profile_applied",
                "riftworlds.return_anchor.marked"
        ));
        lifecycle.put("referenceBehavior", "riftworlds_builds_pocket_rift_lifecycle");
        return Map.copyOf(lifecycle);
    }

    private static boolean nativeReferenceLifecyclePassed(Map<String, Object> lifecycle) {
        return Boolean.TRUE.equals(lifecycle.get("pocketLifecycleExecuted"))
                && EchoRiftWorldsStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(lifecycle.get("adapterCoreContract"))
                && String.valueOf(lifecycle.get("riftSnapshot")).contains(EchoRiftWorldsStandaloneAdapter.REFERENCE_RIFT_ID)
                && String.valueOf(lifecycle.get("riftSnapshot")).contains(EchoRiftWorldsStandaloneAdapter.REFERENCE_DIMENSION_ID)
                && String.valueOf(lifecycle.get("chamberPlan")).contains("PLACE_ANCHOR")
                && String.valueOf(lifecycle.get("hazardProfile")).contains("rift_static")
                && String.valueOf(lifecycle.get("storyRoute")).contains("cache_handoff")
                && String.valueOf(lifecycle.get("exitPlan")).contains("TELEPORT_RETURN")
                && String.valueOf(lifecycle.get("diagnostics")).contains("riftworlds.return_anchor.marked");
    }

    private static Map<String, Object> chamberStep(
            String id,
            String kind,
            String target,
            boolean destructive
    ) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("id", id);
        step.put("kind", kind);
        step.put("target", target);
        step.put("destructive", destructive);
        return Map.copyOf(step);
    }

    private static Map<String, Object> exitAction(String id, String kind, String target) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("id", id);
        action.put("kind", kind);
        action.put("target", target);
        action.put("requiresConfirmation", false);
        return Map.copyOf(action);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
