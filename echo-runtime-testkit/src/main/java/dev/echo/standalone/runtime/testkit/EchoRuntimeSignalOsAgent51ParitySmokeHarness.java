package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.SignalOsAgent51StandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoRuntimeSignalOsAgent51ParitySmokeHarness {
    private EchoRuntimeSignalOsAgent51ParitySmokeHarness() {
    }

    public static void main(String[] args) {
        SignalOsAgent51StandaloneAdapter standaloneAdapter = new SignalOsAgent51StandaloneAdapter();
        Map<String, Object> nativeSession = standaloneAdapter.executeTerminalSession("operator-ashfall-01");
        Map<String, Object> standaloneSession = standaloneAdapter.executeTerminalSession("operator-ashfall-01");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(standaloneAdapter.referenceSessionPassed(nativeSession), "native SignalOS terminal session should pass");
        require(standaloneAdapter.referenceSessionPassed(standaloneSession), "standalone SignalOS terminal session should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("terminalSessionExecuted")),
                "standalone activation should execute terminal session");
        require(nativeSession.get("adapterCoreContract").equals(standaloneSession.get("adapterCoreContract")),
                "native and standalone AdapterCore contracts should match");
        require(nativeSession.get("operatorId").equals(standaloneSession.get("operatorId")),
                "native and standalone operator ids should match");
        require(nativeSession.get("chapter").equals(standaloneSession.get("chapter")),
                "native and standalone chapters should match");
        require(nativeSession.get("mission").equals(standaloneSession.get("mission")),
                "native and standalone missions should match");
        require(nativeSession.get("desktopShell").equals(standaloneSession.get("desktopShell")),
                "native and standalone desktop shells should match");
        require(nativeSession.get("mountedDrive").equals(standaloneSession.get("mountedDrive")),
                "native and standalone mounted drives should match");
        require(nativeSession.get("archiveUnlock").equals(standaloneSession.get("archiveUnlock")),
                "native and standalone archive unlocks should match");
        require(nativeSession.get("saveState").equals(standaloneSession.get("saveState")),
                "native and standalone save states should match");
        require(nativeSession.get("diagnostics").equals(standaloneSession.get("diagnostics")),
                "native and standalone diagnostics should match");

        Map<?, ?> drive = (Map<?, ?>) nativeSession.get("mountedDrive");
        System.out.println("signalos parity smoke PASS contract="
                + nativeSession.get("adapterCoreContract")
                + " mission="
                + SignalOsAgent51StandaloneAdapter.REFERENCE_MISSION_ID
                + " driveFiles="
                + ((List<?>) drive.get("files")).size()
                + " diagnostics="
                + ((List<?>) nativeSession.get("diagnostics")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
