package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.ui.EchoAgent5UiParityResult;
import dev.echo.standalone.runtime.ui.EchoAgent5UiParityRuntime;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimeAgent5UiParitySmokeHarness {
    private EchoRuntimeAgent5UiParitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAgent5UiParityResult result = new EchoAgent5UiParityRuntime().run(services);
        require(services.require(EchoAgent5UiParityResult.class) == result,
                "Agent 5 UI parity result should be service-bound.");
        require(result.passed(), "Agent 5 UI parity failed: " + result.diagnostics());
        writeSmokeReport(result);
        System.out.println("agent5 ui parity smoke PASS runtime="
                + result.runtimeId()
                + " checks="
                + result.passedChecks().size()
                + " screens="
                + result.visitedScreenIds().size()
                + " terminalLines="
                + result.terminalOutput().size());
    }

    private static void writeSmokeReport(EchoAgent5UiParityResult result) throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "agent5-ui-parity-smoke.json").toAbsolutePath();
        Files.createDirectories(report.getParent());
        String json = """
                {
                  "schema": "echo.standalone.agent5.ui_parity_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "runtime": "%s",
                  "moduleIds": ["echoterminal", "echoindex", "echolens", "echohudcore", "echoscreencore", "echoholomap", "echowiki", "echonotificationcore", "echothemecore"],
                  "featureBuckets": ["gui", "hud", "screen", "inventory_overlay", "terminal", "index", "holomap", "lens", "audio"],
                  "trustedMutations": [
                    "terminal command executed",
                    "index search executed",
                    "lens scan executed",
                    "HUD update executed",
                    "notification queue updated",
                    "mission log update executed",
                    "settings profile applied",
                    "pause/death recovery actions executed"
                  ],
                  "visibleRoutes": ["echoterminal:terminal", "echoindex:index", "echolens:lens", "echohudcore:hud", "echoholomap:holomap", "echowiki:wiki", "echoscreencore:mission_log", "echoscreencore:settings", "echoscreencore:pause_flow", "echoscreencore:death_recovery"],
                  "saveEvidence": ["settings profile applied", "screen stack resumed previous screen"],
                  "networkEvidence": ["route-bound text command, lens scan, HUD update, and HoloMap/Wiki actions executed"],
                  "passedCheckCount": %d,
                  "visitedScreenCount": %d,
                  "terminalOutputLineCount": %d,
                  "blockers": []
                }
                """.formatted(
                result.runtimeId(),
                result.passedChecks().size(),
                result.visitedScreenIds().size(),
                result.terminalOutput().size()
        );
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
