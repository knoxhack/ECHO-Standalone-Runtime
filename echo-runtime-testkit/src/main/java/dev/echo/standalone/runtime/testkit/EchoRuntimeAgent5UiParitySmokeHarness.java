package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.ui.EchoAgent5UiParityResult;
import dev.echo.standalone.runtime.ui.EchoAgent5UiParityRuntime;

public final class EchoRuntimeAgent5UiParitySmokeHarness {
    private EchoRuntimeAgent5UiParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAgent5UiParityResult result = new EchoAgent5UiParityRuntime().run(services);
        require(services.require(EchoAgent5UiParityResult.class) == result,
                "Agent 5 UI parity result should be service-bound.");
        require(result.passed(), "Agent 5 UI parity failed: " + result.diagnostics());
        System.out.println("agent5 ui parity smoke PASS runtime="
                + result.runtimeId()
                + " checks="
                + result.passedChecks().size()
                + " screens="
                + result.visitedScreenIds().size()
                + " terminalLines="
                + result.terminalOutput().size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
