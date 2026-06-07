package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeApplication;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCapabilities;
import dev.echo.standalone.runtime.contracts.EchoRuntimeMode;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoRuntimeBootContext(
        String runtimeId,
        EchoRuntimeMode mode,
        Path workspaceRoot,
        Path reportsRoot,
        Instant bootInstant,
        Duration tickBudget,
        int maxTicks,
        EchoRuntimeApplication application,
        EchoRuntimeCapabilities capabilities,
        Map<String, String> properties
) {
    public EchoRuntimeBootContext {
        runtimeId = requireText(runtimeId, "runtimeId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        Objects.requireNonNull(reportsRoot, "reportsRoot");
        Objects.requireNonNull(bootInstant, "bootInstant");
        Objects.requireNonNull(tickBudget, "tickBudget");
        if (tickBudget.isNegative() || tickBudget.isZero()) {
            throw new IllegalArgumentException("tickBudget must be positive");
        }
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must not be negative");
        }
        if (application == null) {
            application = new EchoHeadlessRuntimeApplication(maxTicks);
        }
        if (capabilities == null) {
            capabilities = EchoRuntimeCapabilities.empty();
        }
        Objects.requireNonNull(properties, "properties");
        properties = Map.copyOf(properties);
    }

    public static EchoRuntimeBootContext headless(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-headless",
                EchoRuntimeMode.HEADLESS_TEST,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(50),
                3,
                null,
                EchoRuntimeCapabilities.empty(),
                Map.of()
        );
    }

    public static EchoRuntimeBootContext windowed(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-windowed",
                EchoRuntimeMode.WINDOWED_DEV,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoWindowedRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "window.lifecycle",
                        "window.resize",
                        "window.fullscreen",
                        "window.close",
                        "window.crash_safe_shutdown",
                        "ashfall.playable_loop",
                        "ashfall.windowed_traversal"
                )),
                Map.of("echo.window.playableLoop", "true")
        );
    }

    public static EchoRuntimeBootContext live(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-live",
                EchoRuntimeMode.PLAYABLE_BETA,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoWindowedRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "window.visible",
                        "window.lifecycle",
                        "ashfall.playable_loop",
                        "ashfall.playable_mission",
                        "ashfall.vertical_slice"
                )),
                Map.of(
                        "echo.window.live", "true",
                        "echo.window.playableLoop", "true"
                )
        );
    }

    public static EchoRuntimeBootContext packagedTester(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-packaged-tester",
                EchoRuntimeMode.PACKAGED_TESTER,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoWindowedRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "packaged.tester",
                        "window.visible",
                        "window.lifecycle",
                        "ashfall.playable_loop",
                        "ashfall.playable_mission",
                        "ashfall.vertical_slice",
                        "live.deterministic_close"
                )),
                Map.of(
                        "echo.window.live", "true",
                        "echo.window.deterministicClose", "true",
                        "echo.window.playableLoop", "true",
                        "echo.packagedTester", "true"
                )
        );
    }

    public static EchoRuntimeBootContext packagedHumanSession(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-packaged-human-session",
                EchoRuntimeMode.PLAYABLE_BETA,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoWindowedRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "packaged.human_session",
                        "window.visible",
                        "window.lifecycle",
                        "ashfall.playable_loop",
                        "ashfall.playable_mission",
                        "ashfall.vertical_slice"
                )),
                Map.of(
                        "echo.window.live", "true",
                        "echo.window.playableLoop", "true",
                        "echo.packagedHumanSession", "true"
                )
        );
    }

    public static EchoRuntimeBootContext playableBeta(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-playable-beta",
                EchoRuntimeMode.PLAYABLE_BETA,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoPlayableBetaRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "adaptercore.system_modules",
                        "ashfall.playable_loop",
                        "ashfall.new_game",
                        "ashfall.save_load_continue"
                )),
                Map.of("echo.playableBeta.loop", "true")
        );
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
