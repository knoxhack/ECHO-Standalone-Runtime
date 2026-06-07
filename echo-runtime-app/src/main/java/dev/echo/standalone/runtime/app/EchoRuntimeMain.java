package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoRuntimeMain {
    private EchoRuntimeMain() {
    }

    public static void main(String[] args) {
        boolean live = args.length == 0 || ("--live".equals(args[0]) || "--windowed-live".equals(args[0]));
        boolean windowed = args.length > 0 && "--windowed".equals(args[0]);
        boolean headless = args.length > 0 && "--headless".equals(args[0]);
        boolean playableBeta = args.length > 0 && "--playable-beta".equals(args[0]);
        boolean packagedTester = args.length > 0 && "--packaged-tester".equals(args[0]);
        boolean packagedHumanSession = args.length > 0 && "--packaged-human-session".equals(args[0]);
        Path workspaceRoot = Path.of(".");
        if ((live || windowed || playableBeta || packagedTester || packagedHumanSession)
                && args.length > 1) {
            workspaceRoot = Path.of(args[1]);
        } else if (headless && args.length > 1) {
            workspaceRoot = Path.of(args[1]);
        } else if (!live && !windowed && !headless && !playableBeta && !packagedTester
                && !packagedHumanSession && args.length > 0) {
            workspaceRoot = Path.of(args[0]);
        }
        EchoRuntimeBootContext context = live
                ? EchoRuntimeBootContext.live(workspaceRoot)
                : packagedTester
                ? EchoRuntimeBootContext.packagedTester(workspaceRoot)
                : packagedHumanSession
                ? EchoRuntimeBootContext.packagedHumanSession(workspaceRoot)
                : playableBeta
                ? EchoRuntimeBootContext.playableBeta(workspaceRoot)
                : windowed
                ? EchoRuntimeBootContext.windowed(workspaceRoot)
                : EchoRuntimeBootContext.headless(workspaceRoot);
        EchoRuntimeBootResult result = new EchoRuntimeLauncher().launch(context);
        String summary = "echo-standalone-runtime exitCode=" + result.exitCode().code()
                + " mode=" + context.mode().id()
                + " lifecycle=" + result.finalLifecycle().id()
                + " ticksRun=" + result.ticksRun()
                + " adapterCoreRuntimeBridgeActive=" + result.systemModuleBoot().adapterCoreRuntimeBridgeActive()
                + " ashfallFirstPlayableLoopReady=" + result.ashfallFirstPlayableLoopReady()
                + " liveWindowWalkthroughReady=" + result.liveWindowWalkthroughReady()
                + " liveWindowWalkthroughSummary=" + result.liveWindowWalkthroughSummary();
        System.out.println(summary);
        writeResultFileIfRequested(summary);
        if (!result.success()) {
            for (EchoRuntimeDiagnostic diagnostic : result.diagnostics()) {
                System.err.println("echo-standalone-runtime diagnostic"
                        + " code=" + diagnostic.code()
                        + " severity=" + diagnostic.severity().name()
                        + " layer=" + diagnostic.runtimeLayer()
                        + " summary=" + diagnostic.summary()
                        + " detail=" + diagnostic.detail()
                        + " attributes=" + diagnostic.attributes());
            }
        }
    }

    private static void writeResultFileIfRequested(String summary) {
        String path = System.getenv("ECHO_RUNTIME_RESULT_PATH");
        if (path == null || path.isBlank()) {
            path = System.getProperty("echo.runtime.result.path", "");
        }
        if (path.isBlank()) {
            return;
        }
        try {
            Path resultPath = Path.of(path).toAbsolutePath().normalize();
            Path parent = resultPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(resultPath, summary + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            System.err.println("echo-standalone-runtime result file failed: " + exception.getMessage());
        }
    }
}
