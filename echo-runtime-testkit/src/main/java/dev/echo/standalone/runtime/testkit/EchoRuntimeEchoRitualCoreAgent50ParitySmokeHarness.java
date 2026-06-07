package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoRitualCoreAgent50StandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoRitualCoreAgent50ParitySmokeHarness {
    private EchoRuntimeEchoRitualCoreAgent50ParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoRitualCoreAgent50StandaloneAdapter standaloneAdapter = new EchoRitualCoreAgent50StandaloneAdapter();
        Map<String, Object> nativeActivation = standaloneAdapter.executeActivation("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.executeActivation("echo-native-m17");
        Map<String, Object> standaloneReport = standaloneAdapter.activate();

        require(standaloneAdapter.referenceActivationPassed(nativeActivation),
                "native RitualCore reference activation should pass");
        require(standaloneAdapter.referenceActivationPassed(standaloneActivation),
                "standalone RitualCore activation should pass");
        require(Boolean.TRUE.equals(standaloneReport.get("ritualActivationExecuted")),
                "standalone activation should execute ritual service");
        require(nativeActivation.get("adapterCoreContract").equals(standaloneActivation.get("adapterCoreContract")),
                "native and standalone ritual contracts should match");
        require(nativeActivation.get("structure").equals(standaloneActivation.get("structure")),
                "native and standalone structure validation should match");
        require(nativeActivation.get("costs").equals(standaloneActivation.get("costs")),
                "native and standalone ritual costs should match");
        require(nativeActivation.get("outputs").equals(standaloneActivation.get("outputs")),
                "native and standalone ritual outputs should match");
        require(nativeActivation.get("altarStatus").equals(standaloneActivation.get("altarStatus")),
                "native and standalone altar status should match");
        require(nativeActivation.get("sideEffects").equals(standaloneActivation.get("sideEffects")),
                "native and standalone completion side effects should match");
        require(nativeActivation.get("diagnostics").equals(standaloneActivation.get("diagnostics")),
                "native and standalone diagnostics should match");

        System.out.println("echoritualcore parity smoke PASS contract="
                + nativeActivation.get("adapterCoreContract")
                + " ritual="
                + EchoRitualCoreAgent50StandaloneAdapter.RITUAL_ID
                + " outputs="
                + ((List<?>) nativeActivation.get("outputs")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
