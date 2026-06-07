package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoDataCoreAgent49StandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoDataCoreAgent49ParitySmokeHarness {
    private EchoRuntimeEchoDataCoreAgent49ParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoDataCoreAgent49StandaloneAdapter standaloneAdapter = new EchoDataCoreAgent49StandaloneAdapter();
        Map<String, Object> nativePlan = standaloneAdapter.executeRuntimeProfile(
                EchoDataCoreAgent49StandaloneAdapter.REFERENCE_PLAYER_ID
        );
        Map<String, Object> standalonePlan = standaloneAdapter.executeRuntimeProfile(
                EchoDataCoreAgent49StandaloneAdapter.REFERENCE_PLAYER_ID
        );
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(standaloneAdapter.referencePlanPassed(nativePlan),
                "native DataCore runtime profile sync reference should pass");
        require(standaloneAdapter.referencePlanPassed(standalonePlan),
                "standalone DataCore runtime profile sync should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("dataRuntimeProfileExecuted")),
                "standalone activation should execute the runtime profile sync service");
        require(nativePlan.get("adapterCoreContract").equals(standalonePlan.get("adapterCoreContract")),
                "native and standalone AdapterCore contracts should match");
        require(nativePlan.get("playerId").equals(standalonePlan.get("playerId")),
                "native and standalone profile owners should match");
        require(nativePlan.get("registeredKeys").equals(standalonePlan.get("registeredKeys")),
                "native and standalone registered keys should match");
        require(nativePlan.get("metadataReload").equals(standalonePlan.get("metadataReload")),
                "native and standalone metadata reload results should match");
        require(nativePlan.get("persistenceSnapshot").equals(standalonePlan.get("persistenceSnapshot")),
                "native and standalone persisted snapshots should match");
        require(nativePlan.get("syncPayload").equals(standalonePlan.get("syncPayload")),
                "native and standalone sync payloads should match");
        require(nativePlan.get("diagnostics").equals(standalonePlan.get("diagnostics")),
                "native and standalone diagnostics should match");
        require(nativePlan.get("writeSafety").equals(standalonePlan.get("writeSafety")),
                "native and standalone write safety gates should match");
        require(nativePlan.get("events").equals(standalonePlan.get("events")),
                "native and standalone emitted events should match");

        Map<?, ?> syncPayload = (Map<?, ?>) nativePlan.get("syncPayload");
        System.out.println("echodatacore parity smoke PASS contract="
                + nativePlan.get("adapterCoreContract")
                + " player="
                + nativePlan.get("playerId")
                + " keys="
                + ((List<?>) nativePlan.get("registeredKeys")).size()
                + " syncEntries="
                + syncPayload.get("entryCount"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
