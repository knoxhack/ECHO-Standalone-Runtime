package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.gameplay.EchoWorldCoreStandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoWorldCoreParitySmokeHarness {
    private EchoRuntimeEchoWorldCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoWorldCoreStandaloneAdapter standaloneAdapter = new EchoWorldCoreStandaloneAdapter();
        Map<String, Object> nativeSample = standaloneAdapter.executeRegionCellSample("echo-native-m17");
        Map<String, Object> standaloneSample = standaloneAdapter.executeRegionCellSample("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(standaloneAdapter.referenceSamplePassed(nativeSample),
                "native WorldCore reference region cell sample should pass");
        require(standaloneAdapter.referenceSamplePassed(standaloneSample),
                "standalone WorldCore region cell sample should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("regionCellSampleExecuted")),
                "standalone activation should execute region cell sample");
        require(nativeSample.get("adapterCoreContract").equals(standaloneSample.get("adapterCoreContract")),
                "native and standalone AdapterCore contracts should match");
        require(nativeSample.get("worldId").equals(standaloneSample.get("worldId")),
                "native and standalone worlds should match");
        require(nativeSample.get("samplePoint").equals(standaloneSample.get("samplePoint")),
                "native and standalone sample points should match");
        require(nativeSample.get("regionDefinition").equals(standaloneSample.get("regionDefinition")),
                "native and standalone region definitions should match");
        require(nativeSample.get("hazardDefinition").equals(standaloneSample.get("hazardDefinition")),
                "native and standalone hazard definitions should match");
        require(nativeSample.get("activeRegionId").equals(standaloneSample.get("activeRegionId")),
                "native and standalone active regions should match");
        require(nativeSample.get("activeHazardId").equals(standaloneSample.get("activeHazardId")),
                "native and standalone active hazards should match");
        require(nativeSample.get("cellKey").equals(standaloneSample.get("cellKey")),
                "native and standalone cell keys should match");
        require(nativeSample.get("mapFeed").equals(standaloneSample.get("mapFeed")),
                "native and standalone map feed should match");
        require(nativeSample.get("integrationEvents").equals(standaloneSample.get("integrationEvents")),
                "native and standalone integration events should match");
        require(nativeSample.get("diagnostics").equals(standaloneSample.get("diagnostics")),
                "native and standalone diagnostics should match");

        System.out.println("echoworldcore parity smoke PASS contract="
                + nativeSample.get("adapterCoreContract")
                + " region="
                + nativeSample.get("activeRegionId")
                + " hazard="
                + nativeSample.get("activeHazardId")
                + " feeds="
                + ((List<?>) nativeSample.get("mapFeed")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
