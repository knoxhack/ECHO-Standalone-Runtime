package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoPackCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoPackCoreParitySmokeHarness {
    private EchoRuntimeEchoPackCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativePlan = executeNativeReferenceLoadPlan("ashfall", "echo_native");
        EchoPackCoreStandaloneAdapter standaloneAdapter = new EchoPackCoreStandaloneAdapter();
        Map<String, Object> standalonePlan = standaloneAdapter.executeLoadPlan("ashfall");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferencePlanPassed(nativePlan), "native PackCore reference load plan should pass");
        require(standaloneAdapter.referencePlanPassed(standalonePlan), "standalone PackCore load plan should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("packLoadPlanExecuted")),
                "standalone activation should execute load plan");
        require(nativePlan.get("adapterCoreContract").equals(standalonePlan.get("adapterCoreContract")),
                "native and standalone AdapterCore contracts should match");
        require(nativePlan.get("packId").equals(standalonePlan.get("packId")),
                "native and standalone pack ids should match");
        require(nativePlan.get("profile").equals(standalonePlan.get("profile")),
                "native and standalone profiles should match");
        require(nativePlan.get("lockfile").equals(standalonePlan.get("lockfile")),
                "native and standalone lockfile summaries should match");
        require(nativePlan.get("validation").equals(standalonePlan.get("validation")),
                "native and standalone validation results should match");
        require(nativePlan.get("loadPlan").equals(standalonePlan.get("loadPlan")),
                "native and standalone load-plan steps should match");
        require(nativePlan.get("readinessGates").equals(standalonePlan.get("readinessGates")),
                "native and standalone readiness gates should match");

        System.out.println("echopackcore parity smoke PASS contract="
                + nativePlan.get("adapterCoreContract")
                + " pack="
                + EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID
                + " steps="
                + ((List<?>) nativePlan.get("loadPlan")).size()
                + " validations="
                + ((List<?>) nativePlan.get("validation")).size());
    }

    private static Map<String, Object> executeNativeReferenceLoadPlan(String packId, String runtime) {
        String normalizedPackId = normalizePackId(packId);
        Map<String, Object> plan = new LinkedHashMap<>();
        plan.put("adapterCoreContract", EchoPackCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        plan.put("service", "echopackcore:pack_load_plan_service");
        plan.put("packLoadPlanExecuted", true);
        plan.put("packId", normalizedPackId);
        plan.put("runtime", normalizeText(runtime, "echo_native"));
        plan.put("profileSource", "packs/ashfall/echo.pack.json");
        plan.put("lockfileSource", "packs/ashfall/echo.lock.json");
        plan.put("profile", profile());
        plan.put("lockfile", lockfile());
        plan.put("validation", validation());
        plan.put("loadPlan", loadPlan());
        plan.put("readinessGates", readinessGates());
        plan.put("diagnostics", List.of(
                "pack.profile.loaded",
                "pack.lockfile.verified",
                "pack.load_plan.ordered",
                "pack.repair.confirmation_gated"
        ));
        plan.put("referenceBehavior", "packcore_builds_ashfall_load_plan");
        return Map.copyOf(plan);
    }

    private static boolean nativeReferencePlanPassed(Map<String, Object> plan) {
        return Boolean.TRUE.equals(plan.get("packLoadPlanExecuted"))
                && EchoPackCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(plan.get("adapterCoreContract"))
                && EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID.equals(plan.get("packId"))
                && String.valueOf(plan.get("profile")).contains("rootModule=" + EchoPackCoreStandaloneAdapter.REFERENCE_ROOT_MODULE)
                && String.valueOf(plan.get("profile")).contains("requiredModules=18")
                && String.valueOf(plan.get("lockfile")).contains("lockedModules=39")
                && String.valueOf(plan.get("validation")).contains("LOCKFILE_MATCHES_PROFILE")
                && String.valueOf(plan.get("loadPlan")).contains("activate_root_gameplay_module")
                && String.valueOf(plan.get("readinessGates")).contains("requiresConfirmationForRepair=true")
                && String.valueOf(plan.get("diagnostics")).contains("pack.load_plan.ordered");
    }

    private static Map<String, Object> profile() {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID);
        profile.put("name", "Ashfall");
        profile.put("version", "1.7.6");
        profile.put("status", "active");
        profile.put("type", "official_pack");
        profile.put("rootModule", EchoPackCoreStandaloneAdapter.REFERENCE_ROOT_MODULE);
        profile.put("gameMode", "ashfall_survival");
        profile.put("worldProfile", "post_gridfall_wasteland");
        profile.put("startProfile", "drop_pod_start");
        profile.put("theme", "wasteland_cyberglass");
        profile.put("releaseChannel", "beta");
        profile.put("strictOfficialOnly", true);
        profile.put("minecraftVersion", "26.1.2");
        profile.put("loaderKind", "NeoForge");
        profile.put("loaderVersion", "26.1.2.29-beta");
        profile.put("requiredModules", 18);
        profile.put("recommendedModules", 13);
        profile.put("optionalModules", 8);
        profile.put("requiredFeatures", 19);
        profile.put("optionalFeatures", 6);
        profile.put("requiredModuleIds", List.of(
                "echocore",
                "echonetcore",
                "echodatacore",
                "echoplatformcore",
                "echoadaptercore",
                "echoschemacore",
                "echovalidationcore",
                EchoPackCoreStandaloneAdapter.MODULE_ID,
                "echohealthcore",
                "echoagentcore",
                "echoreportcore",
                "echoterminal",
                "echoindex",
                "echolens",
                "echoholomap",
                "echomissioncore",
                "echoworldcore",
                EchoPackCoreStandaloneAdapter.REFERENCE_ROOT_MODULE
        ));
        return Map.copyOf(profile);
    }

    private static Map<String, Object> lockfile() {
        Map<String, Object> lockfile = new LinkedHashMap<>();
        lockfile.put("schema", "echo.pack.lockfile.v1");
        lockfile.put("packId", EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID);
        lockfile.put("packVersion", "1.7.6");
        lockfile.put("variant", "standard");
        lockfile.put("channel", "beta");
        lockfile.put("rootModule", EchoPackCoreStandaloneAdapter.REFERENCE_ROOT_MODULE);
        lockfile.put("lockedModules", 39);
        lockfile.put("lockedFeatures", 25);
        lockfile.put("checksumAlgorithm", "sha256");
        lockfile.put("checksumModes", List.of("jar", "source_metadata", "resource_metadata", "synthetic", "missing", "unknown"));
        return Map.copyOf(lockfile);
    }

    private static List<Map<String, Object>> validation() {
        return List.of(
                validation("PROFILE_ID_MATCHES_REQUEST", "PASS", "ashfall profile id matches the requested PackOS load target"),
                validation("ROOT_MODULE_PRESENT", "PASS", "echoashfallprotocol is declared as the active official pack root module"),
                validation("LOCKFILE_MATCHES_PROFILE", "PASS", "lockfile pack id, version, channel, variant, and root module match the profile"),
                validation("REPAIR_IS_CONFIRMATION_GATED", "PASS", "PackCore only emits repair plans and does not execute destructive repair actions")
        );
    }

    private static List<Map<String, Object>> loadPlan() {
        return List.of(
                step("load_profile", "READ_PROFILE", "packs/ashfall/echo.pack.json", false),
                step("verify_lockfile", "VERIFY_LOCKFILE", "packs/ashfall/echo.lock.json", false),
                step("activate_platform_contracts", "ACTIVATE_REQUIRED_MODULES", "echocore,echoadaptercore,echoplatformcore", false),
                step("activate_player_surfaces", "ACTIVATE_REQUIRED_MODULES", "echoterminal,echoindex,echolens,echoholomap", false),
                step("activate_root_gameplay_module", "ACTIVATE_ROOT_MODULE", EchoPackCoreStandaloneAdapter.REFERENCE_ROOT_MODULE, false),
                step("emit_repair_preview", "PLAN_ONLY_REPAIR", "repair.plan", true)
        );
    }

    private static Map<String, Object> readinessGates() {
        Map<String, Object> gates = new LinkedHashMap<>();
        gates.put("publicReleaseAllowed", true);
        gates.put("requiresLockfile", true);
        gates.put("requiresReadiness", true);
        gates.put("requiresSupportBundle", true);
        gates.put("requiresConfirmationForRepair", true);
        gates.put("destructiveActions", 0);
        return Map.copyOf(gates);
    }

    private static Map<String, Object> validation(String id, String status, String summary) {
        Map<String, Object> validation = new LinkedHashMap<>();
        validation.put("id", id);
        validation.put("status", status);
        validation.put("summary", summary);
        return Map.copyOf(validation);
    }

    private static Map<String, Object> step(String id, String kind, String target, boolean requiresConfirmation) {
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("id", id);
        step.put("kind", kind);
        step.put("target", target);
        step.put("requiresConfirmation", requiresConfirmation);
        step.put("destructive", false);
        return Map.copyOf(step);
    }

    private static String normalizePackId(String packId) {
        String normalized = normalizeText(packId, EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID);
        return EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID.equals(normalized) || "echo-native-m17".equals(normalized)
                ? EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID
                : normalized;
    }

    private static String normalizeText(String text, String fallback) {
        return text == null || text.isBlank() ? fallback : text.trim();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
