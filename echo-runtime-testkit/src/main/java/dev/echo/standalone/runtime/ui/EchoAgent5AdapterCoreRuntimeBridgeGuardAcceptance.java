package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5AdapterCoreRuntimeBridgeGuardAcceptance {
    private EchoAgent5AdapterCoreRuntimeBridgeGuardAcceptance() {
    }

    public static Map<String, Object> assess(
            boolean adapterCoreRuntimeBridgeActive,
            Map<String, Object> liveClientHostEvidence
    ) {
        boolean hostAccepted = Boolean.TRUE.equals(liveClientHostEvidence.get("accepted"));
        boolean accepted = adapterCoreRuntimeBridgeActive && hostAccepted;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("serviceCodeExecuted", true);
        result.put("adapterCoreRuntimeBridgeActive", adapterCoreRuntimeBridgeActive);
        result.put("liveClientHostEvidenceAccepted", hostAccepted);
        result.put("contract", "adaptercore:agent5_ui_runtime_bridge_guard");
        result.put("effect", accepted
                ? "adaptercore_runtime_bridge_guard:accepted:agent5_ui"
                : "adaptercore_runtime_bridge_guard:rejected");
        result.put("rejection", accepted ? "" : rejection(adapterCoreRuntimeBridgeActive, hostAccepted));
        return result;
    }

    public static Map<String, Object> smoke() {
        Map<String, Object> accepted = assess(true, Map.of("accepted", true));
        Map<String, Object> rejectedNoRuntime = assess(false, Map.of("accepted", true));
        Map<String, Object> rejectedNoHost = assess(true, Map.of("accepted", false));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("adapterCoreRuntimeBridgeGuardAcceptanceSmokeClass",
                EchoAgent5AdapterCoreRuntimeBridgeGuardAcceptance.class.getSimpleName());
        smoke.put("serviceCodeExecuted", true);
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoRuntimeBridge", rejectedNoRuntime);
        smoke.put("rejectedNoHostEvidence", rejectedNoHost);
        smoke.put("passed", Boolean.TRUE.equals(accepted.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRuntime.get("accepted"))
                && "adaptercore_runtime_bridge_inactive".equals(rejectedNoRuntime.get("rejection"))
                && Boolean.FALSE.equals(rejectedNoHost.get("accepted"))
                && "live_client_host_evidence_not_accepted".equals(rejectedNoHost.get("rejection")));
        return smoke;
    }

    private static String rejection(boolean adapterCoreRuntimeBridgeActive, boolean hostAccepted) {
        if (!adapterCoreRuntimeBridgeActive) {
            return "adaptercore_runtime_bridge_inactive";
        }
        if (!hostAccepted) {
            return "live_client_host_evidence_not_accepted";
        }
        return "unknown";
    }
}
