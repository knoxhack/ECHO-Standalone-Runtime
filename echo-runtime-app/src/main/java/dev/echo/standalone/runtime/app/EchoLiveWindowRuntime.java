package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageAuditor;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageReport;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarMutation;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerStep;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.audio.EchoAudioClip;
import dev.echo.standalone.runtime.audio.EchoAudioClipRegistry;
import dev.echo.standalone.runtime.audio.EchoAudioMixer;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackRequest;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfiles;
import dev.echo.standalone.runtime.audio.EchoJavaSoundAudioBackend;
import dev.echo.standalone.runtime.audio.EchoAudioDeviceProfiles;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockBreakResult;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class EchoLiveWindowRuntime implements EchoRuntime {
    private final EchoRuntimeContext context;
    private final EchoRuntimeLifecycleManager lifecycleManager;
    private final EchoRuntimeShutdownController shutdownController;
    private final AtomicReference<Frame> frame = new AtomicReference<>();
    private static final String LIVE_WINDOW_CLASS = "SunAwtFrame";
    private static final String LIVE_WINDOW_TITLE = "ECHO Ashfall Standalone";
    private static final int SW_SHOW = 5;
    private static final int SW_RESTORE = 9;

    public EchoLiveWindowRuntime(
            EchoRuntimeContext context,
            EchoRuntimeLifecycleManager lifecycleManager,
            EchoRuntimeShutdownController shutdownController
    ) {
        this.context = context;
        this.lifecycleManager = lifecycleManager;
        this.shutdownController = shutdownController;
    }

    @Override
    public EchoRuntimeContext context() {
        return context;
    }

    @Override
    public EchoRuntimeLifecycle lifecycle() {
        return lifecycleManager.current();
    }

    @Override
    public void start() {
        appendLiveWindowEvent("start");
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> appendLiveWindowEvent(
                "uncaught thread=" + thread.getName() + " type=" + throwable.getClass().getName()
                        + " message=" + String.valueOf(throwable.getMessage())
        ));
        lifecycleManager.transition(EchoRuntimeLifecycle.STARTING_RENDERER);
        EchoAdapterCoreStandaloneContentBridge bridge = ensureAdapterCoreBridge();
        EchoStandaloneLiveGraphicsResult graphics = new EchoStandaloneLiveGraphicsAudit().evaluateLiveWindow(bridge);
        context.services().register(EchoStandaloneLiveGraphicsResult.class, graphics);
        EchoAdapterCoreModuleCoverageReport moduleCoverage = context.services()
                .find(EchoAdapterCoreModuleCoverageReport.class)
                .orElseGet(() -> buildAdapterCoreModuleCoverage(bridge));
        EchoAshfallPlayableMissionResult mission = new EchoAshfallPlayableMissionRuntime().boot(context.services());
        lifecycleManager.transition(EchoRuntimeLifecycle.RUNNING);
        runLivePlayableLoopIfEnabled();

        if (GraphicsEnvironment.isHeadless()) {
            requestStop(EchoRuntimeShutdownHook.noop("live_window_headless_fallback"));
            return;
        }
        if (Boolean.parseBoolean(context.configuration().properties()
                .getOrDefault("echo.window.deterministicClose", "false"))) {
            requestStop(EchoRuntimeShutdownHook.noop("live_window_deterministic_close"));
            return;
        }

        EchoSaveRuntimeResult liveSave = openLiveSave();
        showMissionWindow(mission, moduleCoverage, bridge, graphics, liveSave);
        appendLiveWindowEvent("showMissionWindow returned");
        requestStop(EchoRuntimeShutdownHook.noop("live_window_closed"));
    }

    @Override
    public void requestStop(EchoRuntimeShutdownHook shutdownHook) {
        appendLiveWindowEvent("requestStop reason=" + shutdownHook.reason());
        shutdownController.requestStop(shutdownHook);
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPING);
        Frame openFrame = frame.getAndSet(null);
        if (openFrame != null && openFrame.isDisplayable()) {
            EventQueue.invokeLater(openFrame::dispose);
        }
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPED);
    }

    private void appendLiveWindowEvent(String event) {
        try {
            Path logPath = context.environment().workspaceRoot()
                    .resolve("echo-standalone-runtime/build/tmp/live-window-runtime-events.log");
            Files.createDirectories(logPath.getParent());
            Files.writeString(
                    logPath,
                    Instant.now() + " " + event + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // Diagnostics only; never make the live window depend on local report writes.
        }
    }

    private EchoAdapterCoreStandaloneContentBridge ensureAdapterCoreBridge() {
        EchoAdapterCoreStandaloneContentBridge bridge = context.services()
                .find(EchoAdapterCoreStandaloneContentBridge.class)
                .orElseGet(EchoAdapterCoreStandaloneContentBridge::ashfallLive);
        context.services().register(EchoAdapterCoreStandaloneContentBridge.class, bridge);
        context.services().register(EchoAdapterCoreStandaloneRegistry.class, bridge.registry());
        return bridge;
    }

    private EchoAdapterCoreModuleCoverageReport buildAdapterCoreModuleCoverage(
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        List<Path> roots = moduleRoots(context.environment().workspaceRoot());
        if (roots.isEmpty()) {
            EchoAdapterCoreModuleCoverageReport empty = EchoAdapterCoreModuleCoverageReport.empty();
            context.services().register(EchoAdapterCoreModuleCoverageReport.class, empty);
            return empty;
        }
        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.descriptorOnly()
                .run(roots, context.services());
        EchoAdapterCoreModuleCoverageReport coverage = new EchoAdapterCoreModuleCoverageAuditor()
                .audit(modules, bridge);
        context.services().register(EchoAdapterCoreModuleCoverageReport.class, coverage);
        return coverage;
    }

    private static List<Path> moduleRoots(Path workspaceRoot) {
        return EchoStandaloneModuleRoots.resolve(workspaceRoot);
    }

    private EchoSaveRuntimeResult openLiveSave() {
        try {
            return EchoStandaloneLiveSessionSaveRuntime.openSave(
                    context.services(),
                    context.environment().workspaceRoot().resolve("saves/ashfall-live-profile")
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to open Ashfall live save profile", exception);
        }
    }

    private void runLivePlayableLoopIfEnabled() {
        boolean playableLoop = Boolean.parseBoolean(context.configuration().properties()
                .getOrDefault("echo.window.playableLoop", "false"));
        if (!playableLoop) {
            return;
        }
        try {
            Path baseSaveRoot = context.environment().workspaceRoot()
                    .resolve("saves/ashfall-live-playable-loop")
                    .resolve(context.environment().runtimeId());
            Files.createDirectories(baseSaveRoot);
            Path saveRoot = Files.createTempDirectory(baseSaveRoot, "run-");
            EchoStandalonePlayableLoopResult result = new EchoStandalonePlayableLoopRuntime().run(
                    context.services(),
                    context.environment().workspaceRoot(),
                    saveRoot
            );
            if (!result.ready()) {
                throw new IllegalStateException("Live playable loop failed: " + result.summary());
            }
            EchoStandaloneSystemModuleBootResult systemModuleBoot = context.services()
                    .find(EchoStandaloneSystemModuleBootResult.class)
                    .orElse(EchoStandaloneSystemModuleBootResult.inactive());
            context.services().register(EchoStandaloneLiveWindowWalkthroughResult.class,
                    EchoStandaloneLiveWindowWalkthroughResult.from(
                            "live-window",
                            true,
                            Boolean.parseBoolean(context.configuration().properties()
                                    .getOrDefault("echo.window.deterministicClose", "false")),
                            GraphicsEnvironment.isHeadless(),
                            systemModuleBoot.adapterCoreRuntimeBridgeActive(),
                            result
                    ));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to run live playable loop", exception);
        }
    }

    private static void addModuleRoot(List<Path> roots, Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path);
        }
    }

    private void showMissionWindow(
            EchoAshfallPlayableMissionResult mission,
            EchoAdapterCoreModuleCoverageReport moduleCoverage,
            EchoAdapterCoreStandaloneContentBridge adapterCoreBridge,
            EchoStandaloneLiveGraphicsResult graphics,
            EchoSaveRuntimeResult liveSave
    ) {
        CountDownLatch closed = new CountDownLatch(1);
        try {
            EventQueue.invokeAndWait(() -> {
                Frame window = new Frame(LIVE_WINDOW_TITLE);
                EchoLiveMissionCanvas canvas = new EchoLiveMissionCanvas(
                        mission,
                        moduleCoverage,
                        adapterCoreBridge,
                        graphics,
                        liveSave,
                        context.configuration().properties()
                );
                window.setLayout(new BorderLayout());
                window.add(canvas, BorderLayout.CENTER);
                window.setSize(new Dimension(1280, 720));
                window.setMinimumSize(new Dimension(960, 540));
                window.setFocusableWindowState(true);
                window.setLocationRelativeTo(null);
                window.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent event) {
                        appendLiveWindowEvent("windowClosing");
                        window.dispose();
                    }

                    @Override
                    public void windowClosed(WindowEvent event) {
                        appendLiveWindowEvent("windowClosed");
                        closed.countDown();
                    }
                });
                frame.set(window);
                appendLiveWindowEvent("window before visible");
                window.setVisible(true);
                appendLiveWindowEvent("window visible");
                forceWindowVisible(window, canvas, canvas.nativeVisibilityAssistEnabled());
            });
            closed.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for live runtime window to close", exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause() == null ? exception : exception.getCause();
            cause.printStackTrace(System.err);
            throw new IllegalStateException("Unable to open live runtime window", cause);
        }
    }

    private static void forceWindowVisible(Frame window, EchoLiveMissionCanvas canvas, boolean nativeVisibilityAssist) {
        window.setBounds(80, 80, 1280, 720);
        window.setState(Frame.NORMAL);
        window.setAlwaysOnTop(true);
        window.toFront();
        window.validate();
        window.repaint();
        canvas.requestFocusInWindow();
        canvas.repaint();
        Toolkit.getDefaultToolkit().sync();
        if (nativeVisibilityAssist) {
            forceNativeWindowVisible();
        }
        Timer focusRelease = new Timer(600, event -> {
            window.setBounds(80, 80, Math.max(960, window.getWidth()), Math.max(540, window.getHeight()));
            window.setState(Frame.NORMAL);
            window.setAlwaysOnTop(false);
            window.toFront();
            canvas.requestFocusInWindow();
            canvas.setIgnoreRepaint(false);
            canvas.repaint();
            ((Timer) event.getSource()).stop();
        });
        focusRelease.setRepeats(false);
        focusRelease.start();
    }

    private static void forceNativeWindowVisible() {
        if (!System.getProperty("os.name", "").toLowerCase().contains("win")) {
            return;
        }
        try (Arena arena = Arena.ofConfined()) {
            SymbolLookup user32 = SymbolLookup.libraryLookup("user32", arena);
            Linker linker = Linker.nativeLinker();
            MemorySegment findWindowAddress = user32.find("FindWindowW").orElseThrow();
            MemorySegment showWindowAddress = user32.find("ShowWindow").orElseThrow();
            MemorySegment setForegroundWindowAddress = user32.find("SetForegroundWindow").orElseThrow();
            MethodHandle findWindow = linker.downcallHandle(
                    findWindowAddress,
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );
            MethodHandle showWindow = linker.downcallHandle(
                    showWindowAddress,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );
            MethodHandle setForegroundWindow = linker.downcallHandle(
                    setForegroundWindowAddress,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );
            MemorySegment className = wideString(arena, LIVE_WINDOW_CLASS);
            MemorySegment title = wideString(arena, LIVE_WINDOW_TITLE);
            MemorySegment windowHandle = (MemorySegment) findWindow.invoke(className, title);
            if (windowHandle.address() == 0L) {
                return;
            }
            showWindow.invoke(windowHandle, SW_RESTORE);
            showWindow.invoke(windowHandle, SW_SHOW);
            setForegroundWindow.invoke(windowHandle);
        } catch (Throwable ignored) {
            // Swing remains the portable path; the native show call only fixes Windows hidden-start launches.
        }
    }

    private static MemorySegment wideString(Arena arena, String value) {
        char[] chars = (value + "\0").toCharArray();
        MemorySegment segment = arena.allocate(ValueLayout.JAVA_CHAR, chars.length);
        for (int index = 0; index < chars.length; index++) {
            segment.setAtIndex(ValueLayout.JAVA_CHAR, index, chars[index]);
        }
        return segment;
    }

    private static final class EchoLiveMissionCanvas extends Canvas {
        private static final long serialVersionUID = 1L;
        private static final Color BACKGROUND_TOP = new Color(5, 14, 17);
        private static final Color BACKGROUND_BOTTOM = new Color(13, 24, 25);
        private static final Color PANEL = new Color(20, 31, 33, 224);
        private static final Color LINE = new Color(93, 194, 177, 120);
        private static final Color TEXT = new Color(230, 240, 232);
        private static final Color MUTED = new Color(151, 172, 167);
        private static final Color ACCENT = new Color(113, 211, 183);
        private static final Color WARNING = new Color(232, 184, 92);
        private static final Color DANGER = new Color(215, 91, 91);
        private static final int AUTOSAVE_TICKS = 150;

        private final EchoAshfallPlayableMissionResult mission;
        private final EchoAdapterCoreStandaloneContentBridge adapterCoreBridge;
        private final EchoAdapterCoreModuleCoverageReport moduleCoverage;
        private final EchoSaveRuntimeResult liveSave;
        private final Map<String, String> runtimeProperties;
        private final ArrayList<EchoStandalonePlayableVoxelEdit> liveEdits = new ArrayList<>();
        private EchoStandaloneLiveGraphicsResult graphicsAudit;
        private final EchoVoxelWorldStreamer worldStreamer;
        private EchoVoxelWorld voxelWorld;
        private EchoVoxelPlayerController playerController;
        private EchoVoxelPlayerHotbar hotbar;
        private EchoAshfallLiveMissionState liveMission;
        private final EchoVoxelHudFramebufferCompositor hudCompositor = new EchoVoxelHudFramebufferCompositor();
        private EchoStandaloneGameShellState shellState;
        private final Set<Integer> pressedKeys = new HashSet<>();
        private final Timer keepAliveTimer;
        private final Timer playerTimer;
        private final Timer titleAnimTimer;
        private final Timer loadingTimer;
        private double titleYawOffset;
        private EchoVoxelFramebuffer voxelFrame;
        private BufferedImage voxelImage;
        private BufferedImage backBuffer;
        private EchoVoxelHit target;
        private String lastAction = "click to capture mouse - left mine, right use/place, 1-9 hotbar";
        private String lastMovement = "spawned";
        private boolean mouseCaptured;
        private boolean recenteringMouse;
        private Robot mouseRobot;
        private Cursor normalCursor;
        private Cursor blankCursor;
        private boolean saveDirty = true;
        private int saveSequence;
        private int autosaveTicks;
        private static final int DEFAULT_RENDER_WIDTH = 1280;
        private static final int DEFAULT_RENDER_HEIGHT = 720;
        private static final int MIN_RENDER_WIDTH = 320;
        private static final int MIN_RENDER_HEIGHT = 180;
        private static final int MAX_RENDER_WIDTH = 1280;
        private static final int MAX_RENDER_HEIGHT = 720;
        private static final boolean DEFAULT_NATIVE_VISIBILITY_ASSIST = false;
        private final EchoVoxelSoftwareRenderer softwareRenderer = new EchoVoxelSoftwareRenderer();
        private final ExecutorService renderExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "echo-voxel-render");
            t.setDaemon(true);
            return t;
        });
        private final AtomicBoolean renderPending = new AtomicBoolean(false);
        private KeyEventDispatcher keyDispatcher;
        private boolean invertMouseY;
        private double mouseSensitivityX;
        private double mouseSensitivityY;
        private final int framebufferWidth;
        private final int framebufferHeight;
        private final boolean nativeVisibilityAssist;
        private long fpsWindowStartNanos;
        private int fpsWindowFrames;
        private int displayedFps;
        private int optionsSelectionIndex = 0;
        private EchoVoxelHotbarSlot draggedSlot;
        private int draggedSourceIndex = -1;
        private double loadingProgress = 0.0D;
        private final EchoAudioMixer audioMixer;
        private final EchoAudioClipRegistry audioClipRegistry;
        private long audioTick = 0;

        private EchoLiveMissionCanvas(
                EchoAshfallPlayableMissionResult mission,
                EchoAdapterCoreModuleCoverageReport moduleCoverage,
                EchoAdapterCoreStandaloneContentBridge adapterCoreBridge,
                EchoStandaloneLiveGraphicsResult graphicsAudit,
                EchoSaveRuntimeResult liveSave,
                Map<String, String> runtimeProperties
        ) {
            this.mission = mission;
            this.adapterCoreBridge = adapterCoreBridge;
            this.moduleCoverage = moduleCoverage;
            this.graphicsAudit = graphicsAudit;
            this.liveSave = liveSave;
            this.runtimeProperties = Map.copyOf(runtimeProperties);
            this.invertMouseY = liveBoolean("echo.window.invertY", false);
            this.mouseSensitivityX = liveDouble("echo.window.mouseSensitivityX", 0.16D, 0.02D, 1.0D);
            this.mouseSensitivityY = liveDouble("echo.window.mouseSensitivityY", 0.12D, 0.02D, 1.0D);
            this.framebufferWidth = liveInt(
                    "echo.window.renderWidth",
                    DEFAULT_RENDER_WIDTH,
                    MIN_RENDER_WIDTH,
                    MAX_RENDER_WIDTH
            );
            this.framebufferHeight = liveInt(
                    "echo.window.renderHeight",
                    DEFAULT_RENDER_HEIGHT,
                    MIN_RENDER_HEIGHT,
                    MAX_RENDER_HEIGHT
            );
            this.nativeVisibilityAssist = liveBoolean(
                    "echo.window.nativeVisibilityAssist",
                    DEFAULT_NATIVE_VISIBILITY_ASSIST
            );
            this.fpsWindowStartNanos = System.nanoTime();
            dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                    dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                            adapterCoreBridge.registry()::requireLiveVoxelBlock,
                            adapterCoreBridge.runtimeMarkerBlock(),
                            1
                    );
            this.worldStreamer = sessionProfile.streamer();
            this.voxelWorld = worldStreamer.streamAround(sessionProfile.generate(42L, 0), 7.5D, 1.5D);
            this.playerController = EchoVoxelPlayerController.spawnAt(
                    voxelWorld,
                    voxelWorld.spawnX(),
                    1.5D,
                    voxelWorld.spawnYawDegrees(),
                    -32.0D
            );
            this.hotbar = sessionProfile.newStarterHotbar();
            this.hotbar.add(adapterCoreBridge.fieldManualItem(), 1);
            this.hotbar.add(adapterCoreBridge.shelterAnchorBlock(), 2);
            this.hotbar.add(adapterCoreBridge.waterRationItem(), 2);
            this.hotbar.add(adapterCoreBridge.fieldRationItem(), 2);
            this.hotbar.add(adapterCoreBridge.emergencyScannerItem(), 1);
            this.hotbar.add(adapterCoreBridge.dirtyWaterItem(), 2);
            this.hotbar.add(adapterCoreBridge.waterPurifierBlock(), 1);
            this.hotbar.add(adapterCoreBridge.handRecyclerBlock(), 1);
            this.audioClipRegistry = new EchoAudioClipRegistry();
            registerDebugAudioClips(audioClipRegistry);
            this.audioMixer = new EchoAudioMixer(
                    new EchoJavaSoundAudioBackend(EchoAudioDeviceProfiles.resolve(
                            EchoAudioDeviceProfiles.STANDALONE_DEFAULT_PROFILE_ID
                    )),
                    EchoAudioVolumeProfiles.resolve(EchoAudioVolumeProfiles.ASHFALL_SURVIVAL_MIX_PROFILE_ID)
            );
            this.liveMission = new EchoAshfallLiveMissionState();
            this.shellState = liveTitleShellState();
            renderVoxelFrame();
            setPreferredSize(new Dimension(1280, 720));
            setBackground(BACKGROUND_TOP);
            setFocusable(true);
            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    renderVoxelFrame();
                    repaint();
                }
            });
            setupMinecraftMouse();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    requestFocusInWindow();
                    if (!shellState.gameplayActive()) {
                        handleShellClick(event);
                        return;
                    }
                    captureMouse();
                    if (event.getButton() == MouseEvent.BUTTON1) {
                        if (breakTarget()) {
                            renderVoxelFrame();
                            repaint();
                        }
                    } else if (event.getButton() == MouseEvent.BUTTON3) {
                        if (useOrPlaceTarget()) {
                            renderVoxelFrame();
                            repaint();
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    if (shellState.mode() == EchoStandaloneGameShellMode.INVENTORY && draggedSlot != null) {
                        if (draggedSourceIndex >= 0) {
                            hotbar.assignSlot(draggedSourceIndex, draggedSlot.block(), draggedSlot.count());
                            draggedSlot = null;
                            draggedSourceIndex = -1;
                            renderVoxelFrame();
                            repaint();
                        }
                    }
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent event) {
                    handleMouseLook(event);
                }

                @Override
                public void mouseDragged(MouseEvent event) {
                    handleMouseLook(event);
                }
            });
            addMouseWheelListener(this::handleMouseWheel);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent event) {
                    releaseMouse();
                    scheduleRender();
                }
            });
            keyDispatcher = event -> {
                java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
                if (owner == null || !owner.isActive()) {
                    return false;
                }
                if (event.getID() == KeyEvent.KEY_PRESSED) {
                    if (event.getKeyCode() == KeyEvent.VK_ESCAPE && shouldCloseOnEscape()) {
                        owner.dispose();
                    } else {
                        handleKeyPressed(event);
                    }
                } else if (event.getID() == KeyEvent.KEY_RELEASED) {
                    handleKeyReleased(event);
                }
                return false;
            };
            KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyDispatcher);
            keepAliveTimer = new Timer(1_000, event -> {
            });
            playerTimer = new Timer(16, event -> {
                try {
                    tickPlayer(inputFromKeys(), 1.0D / 60.0D);
                    scheduleRender();
                } catch (Throwable throwable) {
                    System.err.println("playerTimer error: " + throwable.getClass().getName() + " " + throwable.getMessage());
                }
            });
            titleAnimTimer = new Timer(40, event -> {
                if (shellState.mode() == EchoStandaloneGameShellMode.TITLE) {
                    titleYawOffset += 0.06;
                    if (titleYawOffset >= 360.0) {
                        titleYawOffset -= 360.0;
                    }
                    scheduleRender();
                }
            });
            loadingTimer = new Timer(80, event -> {
                if (shellState.loadingActive()) {
                    loadingProgress += 0.06;
                    if (loadingProgress >= 1.0) {
                        loadingProgress = 1.0;
                        shellState = shellState.loadingComplete();
                        captureMouse();
                        lastAction = "loading complete";
                        playAudio("echo:ui_terminal_blip", "loading_complete");
                    }
                    scheduleRender();
                }
            });
            keepAliveTimer.start();
            playerTimer.start();
            titleAnimTimer.start();
            loadingTimer.start();
        }

        private EchoStandaloneGameShellState liveTitleShellState() {
            boolean continueAvailable = Files.isRegularFile(
                    liveSave.profile().slot(EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID).manifestPath()
            );
            String saveKind = continueAvailable ? liveSaveKind() : "new_game";
            return EchoStandaloneGameShellState.title(new EchoSaveProfileContinueFlow(
                    EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID,
                    saveKind,
                    true,
                    continueAvailable,
                    true,
                    true
            ));
        }

        private String liveSaveKind() {
            try {
                EchoSaveManifest manifest = liveSave.readManifest(EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID);
                return manifest.metadata().getOrDefault("saveKind", "manual");
            } catch (IOException | RuntimeException exception) {
                return "manual";
            }
        }

        private static void registerDebugAudioClips(EchoAudioClipRegistry registry) {
            registry.register(new EchoAudioClip(
                    "ashfall:block_break", "Block Break", "ashfall:sounds/gameplay/block_break.ogg",
                    dev.echo.standalone.runtime.audio.EchoAudioClipType.GAMEPLAY_FX,
                    dev.echo.standalone.runtime.audio.EchoAudioBus.SFX, false, 0.56D));
            registry.register(new EchoAudioClip(
                    "ashfall:block_place", "Block Place", "ashfall:sounds/gameplay/block_place.ogg",
                    dev.echo.standalone.runtime.audio.EchoAudioClipType.GAMEPLAY_FX,
                    dev.echo.standalone.runtime.audio.EchoAudioBus.SFX, false, 0.50D));
            registry.register(new EchoAudioClip(
                    "ashfall:item_pickup", "Item Pickup", "ashfall:sounds/gameplay/item_pickup.ogg",
                    dev.echo.standalone.runtime.audio.EchoAudioClipType.GAMEPLAY_FX,
                    dev.echo.standalone.runtime.audio.EchoAudioBus.SFX, false, 0.46D));
            registry.register(new EchoAudioClip(
                    "ashfall:jump", "Jump", "ashfall:sounds/gameplay/jump.ogg",
                    dev.echo.standalone.runtime.audio.EchoAudioClipType.GAMEPLAY_FX,
                    dev.echo.standalone.runtime.audio.EchoAudioBus.SFX, false, 0.42D));
            registry.register(new EchoAudioClip(
                    "echo:ui_terminal_blip", "Terminal Blip", "echo:sounds/ui/terminal_blip.ogg",
                    dev.echo.standalone.runtime.audio.EchoAudioClipType.UI_SOUND,
                    dev.echo.standalone.runtime.audio.EchoAudioBus.UI, false, 0.50D));
        }

        private void playAudio(String clipId, String reason) {
            if (audioMixer == null) {
                return;
            }
            try {
                EchoAudioClip clip = audioClipRegistry.find(clipId).orElse(null);
                if (clip == null) {
                    return;
                }
                audioTick++;
                audioMixer.submit(new EchoAudioPlaybackRequest(
                        "live-audio-" + audioTick,
                        EchoAudioPlaybackAction.PLAY,
                        clip,
                        reason,
                        audioTick
                ));
            } catch (RuntimeException ignored) {
            }
        }

        private String liveSetting(String name, String fallback) {
            String systemValue = System.getProperty(name);
            if (systemValue != null && !systemValue.isBlank()) {
                return systemValue;
            }
            return runtimeProperties.getOrDefault(name, fallback);
        }

        private boolean liveBoolean(String name, boolean fallback) {
            return Boolean.parseBoolean(liveSetting(name, String.valueOf(fallback)));
        }

        private int liveInt(String name, int fallback, int min, int max) {
            try {
                return clamp(Integer.parseInt(liveSetting(name, String.valueOf(fallback))), min, max);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        private double liveDouble(String name, double fallback, double min, double max) {
            try {
                return clamp(Double.parseDouble(liveSetting(name, String.valueOf(fallback))), min, max);
            } catch (NumberFormatException exception) {
                return fallback;
            }
        }

        private static int clamp(int value, int min, int max) {
            return Math.max(min, Math.min(max, value));
        }

        private static double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }

        private boolean mouseCaptured() {
            return mouseCaptured;
        }

        private boolean shouldCloseOnEscape() {
            return false;
        }

        private boolean nativeVisibilityAssistEnabled() {
            return nativeVisibilityAssist;
        }

        private void setupMinecraftMouse() {
            normalCursor = getCursor();
            try {
                mouseRobot = new Robot();
                Image image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "echo-blank-cursor");
            } catch (RuntimeException | java.awt.AWTException exception) {
                mouseRobot = null;
                blankCursor = Cursor.getDefaultCursor();
            }
        }

        private void captureMouse() {
            if (mouseCaptured) {
                return;
            }
            mouseCaptured = true;
            setCursor(blankCursor);
            centerMouse();
            lastAction = "mouse captured";
        }

        private void releaseMouse() {
            mouseCaptured = false;
            setCursor(normalCursor == null ? Cursor.getDefaultCursor() : normalCursor);
        }

        private void handleMouseLook(MouseEvent event) {
            if (!shellState.gameplayActive() || !mouseCaptured || mouseRobot == null) {
                return;
            }
            if (recenteringMouse) {
                recenteringMouse = false;
                return;
            }
            Point center = viewportCenterOnScreen();
            int deltaX = event.getXOnScreen() - center.x;
            int deltaY = event.getYOnScreen() - center.y;
            if (deltaX == 0 && deltaY == 0) {
                return;
            }
            double pitchSign = invertMouseY ? -1.0D : 1.0D;
            tickPlayer(EchoVoxelPlayerInput.look(
                    deltaX * mouseSensitivityX,
                    -deltaY * pitchSign * mouseSensitivityY
            ), 0.0D);
            centerMouse();
        }

        private void centerMouse() {
            if (!mouseCaptured || mouseRobot == null || !isShowing()) {
                return;
            }
            Point center = viewportCenterOnScreen();
            recenteringMouse = true;
            mouseRobot.mouseMove(center.x, center.y);
        }

        private Point viewportCenterOnScreen() {
            Point location = getLocationOnScreen();
            return new Point(location.x + Math.max(1, getWidth()) / 2, location.y + Math.max(1, getHeight()) / 2);
        }

        private void handleMouseWheel(MouseWheelEvent event) {
            if (!shellState.gameplayActive()) {
                return;
            }
            int rotation = event.getWheelRotation();
            if (rotation == 0) {
                return;
            }
            int next = Math.floorMod(hotbar.selectedSlot() + Integer.signum(rotation), EchoVoxelPlayerHotbar.HOTBAR_COUNT);
            if (selectHotbarSlot(next)) {
                renderVoxelFrame();
                repaint();
            }
        }

        private void handleKeyPressed(KeyEvent event) {
            int keyCode = event.getKeyCode();
            boolean changed = false;
            if (!shellState.gameplayActive()) {
                changed = handleShellKey(event);
                if (changed) {
                    renderVoxelFrame();
                    repaint();
                }
                return;
            }
            switch (event.getKeyCode()) {
                case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 -> changed = selectHotbarSlot(0);
                case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 -> changed = selectHotbarSlot(1);
                case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 -> changed = selectHotbarSlot(2);
                case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 -> changed = selectHotbarSlot(3);
                case KeyEvent.VK_5, KeyEvent.VK_NUMPAD5 -> changed = selectHotbarSlot(4);
                case KeyEvent.VK_6, KeyEvent.VK_NUMPAD6 -> changed = selectHotbarSlot(5);
                case KeyEvent.VK_7, KeyEvent.VK_NUMPAD7 -> changed = selectHotbarSlot(6);
                case KeyEvent.VK_8, KeyEvent.VK_NUMPAD8 -> changed = selectHotbarSlot(7);
                case KeyEvent.VK_9, KeyEvent.VK_NUMPAD9 -> changed = selectHotbarSlot(8);
                case KeyEvent.VK_ESCAPE -> {
                    releaseMouse();
                    shellState = shellState.pause();
                    pressedKeys.clear();
                    lastAction = "pause menu";
                    changed = true;
                    playAudio("echo:ui_terminal_blip", "pause_opened");
                }
                case KeyEvent.VK_F5 -> changed = saveLiveSession("manual");
                case KeyEvent.VK_E -> {
                    releaseMouse();
                    pressedKeys.clear();
                    shellState = shellState.openInventory();
                    lastAction = "inventory opened";
                    changed = true;
                    playAudio("echo:ui_terminal_blip", "inventory_opened");
                }
                case KeyEvent.VK_L -> {
                    releaseMouse();
                    pressedKeys.clear();
                    shellState = shellState.openMissionLog();
                    lastAction = "mission log opened";
                    changed = true;
                    playAudio("echo:ui_terminal_blip", "mission_log_opened");
                }
                default -> {
                    pressedKeys.add(keyCode);
                    tickPlayer(inputFromKeys(), 1.0D / 60.0D);
                    return;
                }
            }
            if (changed) {
                renderVoxelFrame();
                repaint();
            }
        }

        private void handleKeyReleased(KeyEvent event) {
            pressedKeys.remove(event.getKeyCode());
            scheduleRender();
        }

        private void handleShellClick(MouseEvent event) {
            if (shellState.mode() == EchoStandaloneGameShellMode.TITLE) {
                loadingProgress = 0.0D;
                shellState = shellState.startLoading();
                releaseMouse();
                lastAction = "new game";
                markSaveDirty();
            } else if (shellState.mode() == EchoStandaloneGameShellMode.PAUSED) {
                shellState = shellState.resume();
                captureMouse();
                lastAction = "resume";
            } else if (shellState.mode() == EchoStandaloneGameShellMode.OPTIONS) {
                shellState = shellState.closeOptions();
                lastAction = "options closed";
            } else if (shellState.mode() == EchoStandaloneGameShellMode.INVENTORY) {
                if (handleInventoryClick(event)) {
                    return;
                }
                shellState = shellState.closeInventory();
                captureMouse();
                lastAction = "inventory closed";
            } else if (shellState.mode() == EchoStandaloneGameShellMode.TERMINAL) {
                shellState = shellState.closeTerminal();
                captureMouse();
                lastAction = "terminal closed";
            } else if (shellState.mode() == EchoStandaloneGameShellMode.MISSION_LOG) {
                shellState = shellState.closeMissionLog();
                captureMouse();
                lastAction = "mission log closed";
            }
            renderVoxelFrame();
            repaint();
        }

        private boolean handleInventoryClick(MouseEvent event) {
            int canvasW = Math.max(1, getWidth());
            int canvasH = Math.max(1, getHeight());
            int mx = event.getX() * framebufferWidth / canvasW;
            int my = event.getY() * framebufferHeight / canvasH;

            boolean compact = framebufferWidth < 760 || framebufferHeight < 430;
            int slotSize = compact ? 38 : 46;
            int gap = compact ? 5 : 7;
            int gridWidth = slotSize * EchoVoxelPlayerHotbar.HOTBAR_COUNT + gap * 8;
            int panelWidth = Math.min(framebufferWidth - 48, Math.max(gridWidth + 52, compact ? 520 : 620));
            int panelHeight = compact ? 292 : 338;
            int px = Math.max(24, (framebufferWidth - panelWidth) / 2);
            int py = Math.max(28, (framebufferHeight - panelHeight) / 2);
            int gridX = px + Math.max(24, (panelWidth - gridWidth) / 2);
            int gridY = py + (compact ? 84 : 96);
            int hotbarY = gridY + 3 * (slotSize + gap) + (compact ? 10 : 14);

            for (int index = 0; index < EchoVoxelPlayerHotbar.HOTBAR_COUNT; index++) {
                int slotX = gridX + index * (slotSize + gap);
                if (mx >= slotX && mx < slotX + slotSize && my >= hotbarY && my < hotbarY + slotSize) {
                    if (selectHotbarSlot(index)) {
                        playAudio("echo:ui_terminal_blip", "inventory_slot_" + (index + 1));
                        renderVoxelFrame();
                        repaint();
                    }
                    return true;
                }
            }
            // carry grid click (left = pick up/swap, right = split)
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < EchoVoxelPlayerHotbar.HOTBAR_COUNT; column++) {
                    int slotIndex = row * EchoVoxelPlayerHotbar.HOTBAR_COUNT + column;
                    int slotX = gridX + column * (slotSize + gap);
                    int slotY = gridY + row * (slotSize + gap);
                    if (mx >= slotX && mx < slotX + slotSize && my >= slotY && my < slotY + slotSize) {
                        boolean isRightClick = event.getButton() == MouseEvent.BUTTON3;
                        if (draggedSlot != null) {
                            if (isRightClick && draggedSlot.count() > 1 && hotbar.slot(slotIndex + EchoVoxelPlayerHotbar.CARRY_START).empty()) {
                                int place = Math.max(1, draggedSlot.count() / 2);
                                int keep = draggedSlot.count() - place;
                                hotbar.assignSlot(slotIndex + EchoVoxelPlayerHotbar.CARRY_START, draggedSlot.block(), place);
                                draggedSlot = keep > 0 ? new EchoVoxelHotbarSlot(-1, draggedSlot.block(), keep) : null;
                            } else {
                                hotbar.moveOrMergeSlot(draggedSourceIndex, slotIndex + EchoVoxelPlayerHotbar.CARRY_START);
                                draggedSlot = null;
                                draggedSourceIndex = -1;
                            }
                        } else if (!hotbar.slot(slotIndex + EchoVoxelPlayerHotbar.CARRY_START).empty()) {
                            if (isRightClick) {
                                hotbar.splitSlotTo(slotIndex + EchoVoxelPlayerHotbar.CARRY_START,
                                        findEmptyCarryOrHotbarSlot(hotbar, slotIndex + EchoVoxelPlayerHotbar.CARRY_START));
                            } else {
                                draggedSlot = hotbar.slot(slotIndex + EchoVoxelPlayerHotbar.CARRY_START);
                                draggedSourceIndex = slotIndex + EchoVoxelPlayerHotbar.CARRY_START;
                                hotbar.assignSlot(draggedSourceIndex, EchoVoxelBlock.AIR, 0);
                            }
                        }
                        if (isRightClick) {
                            playAudio("echo:ui_terminal_blip", "inventory_split");
                        } else {
                            playAudio("echo:ui_terminal_blip", "inventory_drag");
                        }
                        renderVoxelFrame();
                        repaint();
                        return true;
                    }
                }
            }
            return false;
        }

        private static int findEmptyCarryOrHotbarSlot(EchoVoxelPlayerHotbar hotbar, int excludeIndex) {
            for (int i = EchoVoxelPlayerHotbar.CARRY_START; i < EchoVoxelPlayerHotbar.SLOT_COUNT; i++) {
                if (i != excludeIndex && hotbar.slot(i).empty()) {
                    return i;
                }
            }
            for (int i = 0; i < EchoVoxelPlayerHotbar.HOTBAR_COUNT; i++) {
                if (i != excludeIndex && hotbar.slot(i).empty()) {
                    return i;
                }
            }
            return -1;
        }

        private boolean handleShellKey(KeyEvent event) {
            if (shellState.loadingActive()) {
                return false;
            }
            switch (event.getKeyCode()) {
                case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.TITLE) {
                        loadingProgress = 0.0D;
                        shellState = shellState.startLoading();
                        releaseMouse();
                        lastAction = "new game";
                        markSaveDirty();
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.PAUSED) {
                        shellState = shellState.resume();
                        captureMouse();
                        lastAction = "resume";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.OPTIONS) {
                        shellState = shellState.closeOptions();
                        lastAction = "options closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.INVENTORY) {
                        shellState = shellState.closeInventory();
                        captureMouse();
                        lastAction = "inventory closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.TERMINAL) {
                        shellState = shellState.closeTerminal();
                        captureMouse();
                        lastAction = "terminal closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.MISSION_LOG) {
                        shellState = shellState.closeMissionLog();
                        captureMouse();
                        lastAction = "mission log closed";
                    }
                    return true;
                }
                case KeyEvent.VK_L -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.MISSION_LOG) {
                        shellState = shellState.closeMissionLog();
                        captureMouse();
                        lastAction = "mission log closed";
                        return true;
                    }
                    if (shellState.mode() == EchoStandaloneGameShellMode.INVENTORY
                            || shellState.mode() == EchoStandaloneGameShellMode.TERMINAL
                            || shellState.mode() == EchoStandaloneGameShellMode.PAUSED) {
                        shellState = shellState.openMissionLog();
                        lastAction = "mission log opened";
                        return true;
                    }
                    return false;
                }
                case KeyEvent.VK_E -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.INVENTORY) {
                        shellState = shellState.closeInventory();
                        lastAction = "inventory closed";
                        return true;
                    }
                    if (shellState.mode() == EchoStandaloneGameShellMode.TERMINAL) {
                        shellState = shellState.closeTerminal();
                        lastAction = "terminal closed";
                        return true;
                    }
                    return false;
                }
                case KeyEvent.VK_1, KeyEvent.VK_NUMPAD1 -> {
                    return selectInventoryHotbarSlot(0);
                }
                case KeyEvent.VK_2, KeyEvent.VK_NUMPAD2 -> {
                    return selectInventoryHotbarSlot(1);
                }
                case KeyEvent.VK_3, KeyEvent.VK_NUMPAD3 -> {
                    return selectInventoryHotbarSlot(2);
                }
                case KeyEvent.VK_4, KeyEvent.VK_NUMPAD4 -> {
                    return selectInventoryHotbarSlot(3);
                }
                case KeyEvent.VK_5, KeyEvent.VK_NUMPAD5 -> {
                    return selectInventoryHotbarSlot(4);
                }
                case KeyEvent.VK_6, KeyEvent.VK_NUMPAD6 -> {
                    return selectInventoryHotbarSlot(5);
                }
                case KeyEvent.VK_7, KeyEvent.VK_NUMPAD7 -> {
                    return selectInventoryHotbarSlot(6);
                }
                case KeyEvent.VK_8, KeyEvent.VK_NUMPAD8 -> {
                    return selectInventoryHotbarSlot(7);
                }
                case KeyEvent.VK_9, KeyEvent.VK_NUMPAD9 -> {
                    return selectInventoryHotbarSlot(8);
                }
                case KeyEvent.VK_C -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.TITLE && shellState.continueAvailable()) {
                        if (!restoreLiveSession()) {
                            return true;
                        }
                        shellState = shellState.continueGame();
                        captureMouse();
                        lastAction = "continue " + EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID;
                        return true;
                    }
                    return false;
                }
                case KeyEvent.VK_F5 -> {
                    return saveLiveSession("manual");
                }
                case KeyEvent.VK_O -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.PAUSED) {
                        shellState = shellState.openOptions();
                        lastAction = "options opened";
                        return true;
                    }
                    if (shellState.mode() == EchoStandaloneGameShellMode.OPTIONS) {
                        shellState = shellState.closeOptions();
                        lastAction = "options closed";
                        return true;
                    }
                    return false;
                }
                case KeyEvent.VK_ESCAPE -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.OPTIONS) {
                        shellState = shellState.closeOptions();
                        lastAction = "options closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.INVENTORY) {
                        shellState = shellState.closeInventory();
                        captureMouse();
                        lastAction = "inventory closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.TERMINAL) {
                        shellState = shellState.closeTerminal();
                        captureMouse();
                        lastAction = "terminal closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.MISSION_LOG) {
                        shellState = shellState.closeMissionLog();
                        captureMouse();
                        lastAction = "mission log closed";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.PAUSED) {
                        shellState = shellState.resume();
                        captureMouse();
                        lastAction = "resume";
                    } else if (shellState.mode() == EchoStandaloneGameShellMode.TITLE) {
                        shellState = shellState.startNewGame();
                        captureMouse();
                        lastAction = "new game";
                        markSaveDirty();
                    }
                    return true;
                }
                case KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT -> {
                    if (shellState.mode() == EchoStandaloneGameShellMode.OPTIONS) {
                        return adjustOption(event.getKeyCode());
                    }
                    return false;
                }
                default -> {
                    return false;
                }
            }
        }

        private EchoVoxelPlayerInput inputFromKeys() {
            return new EchoVoxelPlayerInput(
                    pressedKeys.contains(KeyEvent.VK_W),
                    pressedKeys.contains(KeyEvent.VK_S),
                    pressedKeys.contains(KeyEvent.VK_A),
                    pressedKeys.contains(KeyEvent.VK_D),
                    pressedKeys.contains(KeyEvent.VK_SPACE),
                    pressedKeys.contains(KeyEvent.VK_SHIFT),
                    pressedKeys.contains(KeyEvent.VK_CONTROL),
                    lookDelta(KeyEvent.VK_RIGHT) - lookDelta(KeyEvent.VK_LEFT),
                    keyboardPitchDelta()
            );
        }

        private double keyboardPitchDelta() {
            double normalDelta = lookDelta(KeyEvent.VK_UP) - lookDelta(KeyEvent.VK_DOWN);
            return invertMouseY ? -normalDelta : normalDelta;
        }

        private double lookDelta(int positiveKey, int alternatePositiveKey) {
            return pressedKeys.contains(positiveKey) || pressedKeys.contains(alternatePositiveKey) ? 4.0D : 0.0D;
        }

        private double lookDelta(int positiveKey) {
            return pressedKeys.contains(positiveKey) ? 3.0D : 0.0D;
        }

        private boolean selectInventoryHotbarSlot(int index) {
            if (shellState.mode() != EchoStandaloneGameShellMode.INVENTORY) {
                return false;
            }
            selectHotbarSlot(index);
            lastAction = "inventory selected slot " + (index + 1) + ": " + hotbar.selected().label();
            return true;
        }

        private boolean tickPlayer(EchoVoxelPlayerInput input, double seconds) {
            if (!shellState.gameplayActive()) {
                return false;
            }
            if (!input.active() && playerController.state().grounded()) {
                return false;
            }
            EchoVoxelPlayerStep step = playerController.tick(voxelWorld, input, seconds);
            voxelWorld = worldStreamer.streamAround(voxelWorld, step.current().x(), step.current().z());
            boolean changed = step.moved()
                    || step.jumped()
                    || step.collidedHorizontal()
                    || step.collidedVertical()
                    || !step.reason().equals("idle");
            if (changed) {
                if (step.jumped()) {
                    playAudio("ashfall:jump", "player_jump");
                }
                lastMovement = step.reason();
                liveMission.tick(voxelWorld, playerController.state(), step.moved(), seconds,
                        adapterCoreBridge.hazardTable(), adapterCoreBridge.shelterProfile(),
                        adapterCoreBridge.survivalProfile());
                markSaveDirty();
                maybeAutosave();
            }
            return changed;
        }

        private void markSaveDirty() {
            saveDirty = true;
        }

        private void maybeAutosave() {
            if (!saveDirty) {
                autosaveTicks = 0;
                return;
            }
            autosaveTicks++;
            if (autosaveTicks >= AUTOSAVE_TICKS) {
                saveLiveSession("autosave");
            }
        }

        private boolean saveLiveSession(String saveKind) {
            try {
                if (voxelFrame == null) {
                    renderVoxelFrame();
                }
                EchoSaveCommitResult commit = EchoStandalonePlayableVoxelSaveCodec.writeLiveSnapshot(
                        liveSave,
                        EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID,
                        "tx-live-" + saveKind + "-" + String.format("%04d", ++saveSequence),
                        playerController.state(),
                        hotbar,
                        liveMission,
                        liveEdits,
                        voxelFrame,
                        Map.of(
                                "saveKind", saveKind,
                                "runtime", "standalone",
                                "scenario", "live_game_window",
                                "adaptercore", "multi_runtime"
                        )
                );
                saveDirty = false;
                autosaveTicks = 0;
                shellState = shellState.saveAvailable(saveKind, saveKind + " saved");
                lastAction = saveKind + " saved " + commit.filesWritten()
                        + " files to " + EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID;
                return true;
            } catch (IOException | RuntimeException exception) {
                lastAction = saveKind + " failed: " + trim(exception.getMessage(), 54);
                return true;
            }
        }

        private boolean restoreLiveSession() {
            try {
                EchoSaveManifest manifest = liveSave.readManifest(EchoStandaloneLiveSessionSaveRuntime.LIVE_SLOT_ID);
                EchoStandalonePlayableVoxelSaveSnapshot snapshot =
                        EchoStandalonePlayableVoxelSaveCodec.restoreSnapshot(adapterCoreBridge, liveSave, manifest);
                voxelWorld = snapshot.world();
                playerController = new EchoVoxelPlayerController(snapshot.player());
                hotbar = snapshot.hotbar();
                liveMission = snapshot.mission();
                liveEdits.clear();
                liveEdits.addAll(snapshot.edits());
                saveDirty = false;
                autosaveTicks = 0;
                shellState = shellState.saveAvailable(
                        manifest.metadata().getOrDefault("saveKind", "manual"),
                        "continued save"
                );
                renderVoxelFrame();
                return true;
            } catch (IOException | RuntimeException exception) {
                lastAction = "continue failed: " + trim(exception.getMessage(), 54);
                return false;
            }
        }

        private void scheduleRender() {
            if (!renderPending.compareAndSet(false, true)) {
                return;
            }
            EchoVoxelPlayerState player = playerController.state();
            final EchoVoxelPlayerState renderPlayer;
            if (shellState.mode() == EchoStandaloneGameShellMode.TITLE) {
                double yaw = player.yawDegrees() + titleYawOffset;
                renderPlayer = new EchoVoxelPlayerState(
                        player.x(), player.y(), player.z(),
                        player.velocityY(), yaw, player.pitchDegrees(),
                        player.grounded(), player.crouching(), player.sprinting(),
                        player.selectedSlot(), player.reach()
                );
            } else {
                renderPlayer = player;
            }
            final EchoVoxelWorld world = voxelWorld;
            final int fbW = framebufferWidth;
            final int fbH = framebufferHeight;
            final EchoVoxelHudOverlay overlay = hudOverlay();
            renderExecutor.execute(() -> {
                try {
                    EchoVoxelHit raycastTarget = world.raycast(
                            renderPlayer.x(), renderPlayer.eyeY(), renderPlayer.z(),
                            renderPlayer.yawDegrees(), renderPlayer.pitchDegrees(), renderPlayer.reach()
                    ).orElse(null);
                    EchoVoxelFramebuffer rendered = softwareRenderer.render(
                            world, renderPlayer.camera(), fbW, fbH
                    );
                    EchoVoxelFramebuffer frame = hudCompositor.composite(rendered, overlay);
                    BufferedImage image = voxelImage(frame);
                    EventQueue.invokeLater(() -> {
                        target = raycastTarget;
                        voxelFrame = frame;
                        voxelImage = image;
                        recordRenderedFrame();
                        repaint();
                    });
                } finally {
                    renderPending.set(false);
                }
            });
        }

        private void renderVoxelFrame() {
            EchoVoxelPlayerState player = playerController.state();
            EchoVoxelPlayerState renderPlayer = player;
            if (shellState.mode() == EchoStandaloneGameShellMode.TITLE) {
                double yaw = player.yawDegrees() + titleYawOffset;
                renderPlayer = new EchoVoxelPlayerState(
                        player.x(), player.y(), player.z(),
                        player.velocityY(), yaw, player.pitchDegrees(),
                        player.grounded(), player.crouching(), player.sprinting(),
                        player.selectedSlot(), player.reach()
                );
            }
            target = voxelWorld.raycast(
                    renderPlayer.x(),
                    renderPlayer.eyeY(),
                    renderPlayer.z(),
                    renderPlayer.yawDegrees(),
                    renderPlayer.pitchDegrees(),
                    renderPlayer.reach()
            ).orElse(null);
            EchoVoxelFramebuffer renderedFrame = softwareRenderer.render(
                    voxelWorld,
                    renderPlayer.camera(),
                    framebufferWidth,
                    framebufferHeight
            );
            voxelFrame = hudCompositor.composite(renderedFrame, hudOverlay());
            voxelImage = voxelImage(voxelFrame);
            recordRenderedFrame();
        }

        private EchoVoxelHudOverlay hudOverlay() {
            return new EchoVoxelHudOverlay(
                    hotbar,
                    liveMission,
                    adapterCoreBridge.runtimeSummary(),
                    graphicsAudit.livePresenterId(),
                    adapterCoreBridge.registrySummary(),
                    targetLabel(),
                    lastAction,
                    playerPositionLabel(),
                    playerModeLabel(),
                    moduleCoverageLabel(),
                    graphicsAudit.hudSummary(),
                    rendererStatusLabel(),
                    target != null,
                    playerController.state().grounded(),
                    graphicsAudit.adapterCoreTargetsOpenGl(),
                    voxelWorld.loadedChunkCount(),
                    shellState.overlayVisible(),
                    shellState.mode() == EchoStandaloneGameShellMode.INVENTORY,
                    shellState.mode() == EchoStandaloneGameShellMode.TERMINAL,
                    shellState.mode() == EchoStandaloneGameShellMode.MISSION_LOG,
                    shellState.title(),
                    shellLines()
            );
        }

        private List<String> shellLines() {
            if (shellState.mode() == EchoStandaloneGameShellMode.OPTIONS) {
                return List.of(
                        (optionsSelectionIndex == 0 ? "> " : "  ") + "Invert Y: " + (invertMouseY ? "ON" : "OFF") + "  (Left/Right to toggle)",
                        (optionsSelectionIndex == 1 ? "> " : "  ") + "Sensitivity X: " + String.format("%.2f", mouseSensitivityX) + "  (+/-)",
                        (optionsSelectionIndex == 2 ? "> " : "  ") + "Sensitivity Y: " + String.format("%.2f", mouseSensitivityY) + "  (Shift +/-)",
                        "Esc / Enter: Back"
                );
            }
            return shellState.lines();
        }

        private boolean adjustOption(int keyCode) {
            switch (keyCode) {
                case KeyEvent.VK_UP -> {
                    optionsSelectionIndex = Math.floorMod(optionsSelectionIndex - 1, 3);
                    return true;
                }
                case KeyEvent.VK_DOWN -> {
                    optionsSelectionIndex = Math.floorMod(optionsSelectionIndex + 1, 3);
                    return true;
                }
                case KeyEvent.VK_LEFT -> {
                    switch (optionsSelectionIndex) {
                        case 0 -> invertMouseY = !invertMouseY;
                        case 1 -> mouseSensitivityX = Math.max(0.02D, mouseSensitivityX - 0.02D);
                        case 2 -> mouseSensitivityY = Math.max(0.02D, mouseSensitivityY - 0.02D);
                    }
                    return true;
                }
                case KeyEvent.VK_RIGHT -> {
                    switch (optionsSelectionIndex) {
                        case 0 -> invertMouseY = !invertMouseY;
                        case 1 -> mouseSensitivityX = Math.min(1.0D, mouseSensitivityX + 0.02D);
                        case 2 -> mouseSensitivityY = Math.min(1.0D, mouseSensitivityY + 0.02D);
                    }
                    return true;
                }
            }
            return false;
        }

        private void recordRenderedFrame() {
            long now = System.nanoTime();
            fpsWindowFrames++;
            long elapsed = now - fpsWindowStartNanos;
            if (elapsed >= 1_000_000_000L) {
                displayedFps = (int) Math.round(fpsWindowFrames * 1_000_000_000.0D / elapsed);
                fpsWindowFrames = 0;
                fpsWindowStartNanos = now;
            }
        }

        private boolean breakTarget() {
            if (target == null) {
                lastAction = "break: no block targeted";
                return false;
            }
            EchoVoxelBlockBreakResult breakProbe = voxelWorld.attemptBreakBlock(
                    target.x(),
                    target.y(),
                    target.z(),
                    0.0D,
                    1.0D
            );
            EchoVoxelBlockBreakResult breakResult = voxelWorld.attemptBreakBlock(
                    target.x(),
                    target.y(),
                    target.z(),
                    breakProbe.requiredSeconds(),
                    1.0D
            );
            EchoVoxelBlock brokenBlock = breakResult.block();
            boolean changed = breakResult.broken();
            EchoVoxelHotbarMutation pickup = changed
                    ? hotbar.add(brokenBlock, 1)
                    : new EchoVoxelHotbarMutation(false, "break_failed", hotbar.selected());
            EchoAshfallLiveMissionState.ScavengeReward scavenge = changed
                    ? liveMission.scavenge(
                    brokenBlock,
                    adapterCoreBridge.scavengeTable(),
                    "block:" + target.x() + "," + target.y() + "," + target.z()
            )
                    : EchoAshfallLiveMissionState.ScavengeReward.none("scavenge skipped");
            if (scavenge.rewarded()) {
                if (scavenge.waterRation()) {
                    hotbar.add(adapterCoreBridge.waterRationItem(), 1);
                }
                if (scavenge.foodRation()) {
                    hotbar.add(adapterCoreBridge.fieldRationItem(), 1);
                }
                if (scavenge.repairKits() > 0) {
                    hotbar.add(adapterCoreBridge.powerRepairKitItem(), scavenge.repairKits());
                }
            }
            if (changed) {
                liveEdits.add(new EchoStandalonePlayableVoxelEdit(
                        target.x(),
                        target.y(),
                        target.z(),
                        brokenBlock.id(),
                        EchoVoxelBlock.AIR.id()
                ));
                liveMission.markHazardCleared(brokenBlock);
                markSaveDirty();
                playAudio("ashfall:block_break", "block_break");
            }
            lastAction = changed
                    ? "broke " + brokenBlock.displayName() + " @ " + target.x() + "," + target.y() + "," + target.z()
                    + " in " + String.format("%.2f", breakResult.requiredSeconds()) + "s / " + pickup.reason()
                    + (scavenge.rewarded() ? " / " + scavenge.message() : "")
                    : "break failed outside loaded chunk";
            return changed;
        }

        private boolean interactTarget() {
            boolean terminalTarget = target != null && isTerminalTarget(target.block().id());
            boolean cacheTarget = target != null && isCacheTarget(target.block().id());
            boolean rainCollectorTarget = target != null && isRainCollectorTarget(target.block().id());
            boolean waterPurifierTarget = target != null && isWaterPurifierTarget(target.block().id());
            if (rainCollectorTarget) {
                int dirtyBefore = liveMission.dirtyWaterBottles();
                boolean changed = liveMission.collectRainWater(adapterCoreBridge.waterLoopProfile());
                int collected = liveMission.dirtyWaterBottles() - dirtyBefore;
                EchoVoxelHotbarMutation pickup = collected > 0
                        ? hotbar.add(adapterCoreBridge.dirtyWaterItem(), collected)
                        : new EchoVoxelHotbarMutation(false, "no_dirty_water_collected", hotbar.selected());
                lastAction = liveMission.lastMessage() + " / " + pickup.reason();
                if (changed || pickup.changed()) {
                    markSaveDirty();
                }
                return changed || pickup.changed();
            }
            if (waterPurifierTarget) {
                if (!hotbar.selected().empty()
                        && hotbar.selected().block().id().equals(adapterCoreBridge.dirtyWaterItem().id())) {
                    return insertSelectedDirtyWater();
                }
                int cleanBefore = liveMission.cleanWaterStockpile();
                boolean changed = liveMission.purifyWater(adapterCoreBridge.waterLoopProfile());
                int produced = liveMission.cleanWaterStockpile() - cleanBefore;
                EchoVoxelHotbarMutation pickup = produced > 0
                        ? hotbar.add(adapterCoreBridge.waterRationItem(), produced)
                        : new EchoVoxelHotbarMutation(false, "no_clean_water_created", hotbar.selected());
                lastAction = liveMission.lastMessage() + " / " + pickup.reason();
                if (changed || pickup.changed()) {
                    markSaveDirty();
                }
                return changed || pickup.changed();
            }
            boolean changed = liveMission.interact(target, playerController.state());
            if (cacheTarget
                    && liveMission.cacheRecovered()
                    && liveMission.repairKits() > 0
                    && !hasHotbarItem(adapterCoreBridge.powerRepairKitItem().id())) {
                EchoVoxelHotbarMutation pickup = hotbar.add(adapterCoreBridge.powerRepairKitItem(), 1);
                lastAction = liveMission.lastMessage() + " / " + pickup.reason();
            } else {
                lastAction = liveMission.lastMessage();
            }
            if (changed) {
                markSaveDirty();
            }
            if (terminalTarget) {
                releaseMouse();
                pressedKeys.clear();
                shellState = shellState.openTerminal();
            }
            return changed;
        }

        private boolean useOrPlaceTarget() {
            if (isInteractableTarget()) {
                return interactTarget();
            }
            if (hotbar.selected().block().id().equals(adapterCoreBridge.fieldManualItem().id())) {
                return useSelectedFieldManual();
            }
            if (hotbar.selected().block().id().equals(adapterCoreBridge.waterRationItem().id())) {
                return useSelectedWaterRation();
            }
            if (hotbar.selected().block().id().equals(adapterCoreBridge.fieldRationItem().id())) {
                return useSelectedFoodRation();
            }
            if (hotbar.selected().block().id().equals(adapterCoreBridge.emergencyScannerItem().id())) {
                return useSelectedEmergencyScanner();
            }
            return placeTarget();
        }

        private boolean isInteractableTarget() {
            if (target == null) {
                return false;
            }
            String blockId = target.block().id();
            return isTerminalTarget(blockId)
                    || isCacheTarget(blockId)
                    || isPowerTarget(blockId)
                    || isRainCollectorTarget(blockId)
                    || isWaterPurifierTarget(blockId);
        }

        private static boolean isTerminalTarget(String blockId) {
            return blockId.contains("field_terminal") || blockId.contains("echo_terminal");
        }

        private static boolean isCacheTarget(String blockId) {
            return blockId.contains("crash_cache") || blockId.contains("echo_cache") || blockId.contains("structure_cache");
        }

        private static boolean isPowerTarget(String blockId) {
            return blockId.contains("damaged_power_node") || blockId.contains("power_node");
        }

        private static boolean isRainCollectorTarget(String blockId) {
            return blockId.contains("rain_collector");
        }

        private static boolean isWaterPurifierTarget(String blockId) {
            return blockId.contains("water_purifier");
        }

        private boolean useSelectedFieldManual() {
            if (hotbar.selected().empty()
                    || !hotbar.selected().block().id().equals(adapterCoreBridge.fieldManualItem().id())) {
                return false;
            }
            boolean alreadyRead = liveMission.fieldManualRead();
            boolean used = liveMission.readFieldManual(adapterCoreBridge.fieldManualItem());
            EchoVoxelHotbarMutation consume = used && !alreadyRead
                    ? hotbar.consumeSelected()
                    : new EchoVoxelHotbarMutation(false, "manual_already_read", hotbar.selected());
            lastAction = used ? liveMission.lastMessage() + " / " + consume.reason() : liveMission.lastMessage();
            if (consume.changed() || used) {
                markSaveDirty();
            }
            return consume.changed() || used;
        }

        private boolean insertSelectedDirtyWater() {
            if (hotbar.selected().empty()
                    || !hotbar.selected().block().id().equals(adapterCoreBridge.dirtyWaterItem().id())) {
                return false;
            }
            int dirtyBefore = liveMission.dirtyWaterBottles();
            boolean handled = liveMission.insertDirtyWater(
                    adapterCoreBridge.dirtyWaterItem(),
                    adapterCoreBridge.waterLoopProfile()
            );
            boolean inserted = handled && liveMission.dirtyWaterBottles() > dirtyBefore;
            EchoVoxelHotbarMutation consume = inserted
                    ? hotbar.consumeSelected()
                    : new EchoVoxelHotbarMutation(false, "dirty_water_not_inserted", hotbar.selected());
            lastAction = handled ? liveMission.lastMessage() + " / " + consume.reason() : liveMission.lastMessage();
            if (consume.changed() || inserted || handled) {
                markSaveDirty();
            }
            return consume.changed() || inserted || handled;
        }

        private boolean useSelectedWaterRation() {
            if (hotbar.selected().empty()
                    || !hotbar.selected().block().id().equals(adapterCoreBridge.waterRationItem().id())) {
                return false;
            }
            EchoVoxelHotbarMutation consume = hotbar.consumeSelected();
            boolean used = liveMission.useWaterRation(adapterCoreBridge.survivalProfile());
            lastAction = used ? liveMission.lastMessage() + " / " + consume.reason() : liveMission.lastMessage();
            if (consume.changed() || used) {
                markSaveDirty();
            }
            return consume.changed() || used;
        }

        private boolean useSelectedFoodRation() {
            if (hotbar.selected().empty()
                    || !hotbar.selected().block().id().equals(adapterCoreBridge.fieldRationItem().id())) {
                return false;
            }
            EchoVoxelHotbarMutation consume = hotbar.consumeSelected();
            boolean used = liveMission.useFoodRation(adapterCoreBridge.survivalProfile());
            lastAction = used ? liveMission.lastMessage() + " / " + consume.reason() : liveMission.lastMessage();
            if (consume.changed() || used) {
                markSaveDirty();
            }
            return consume.changed() || used;
        }

        private boolean useSelectedEmergencyScanner() {
            if (hotbar.selected().empty()
                    || !hotbar.selected().block().id().equals(adapterCoreBridge.emergencyScannerItem().id())) {
                return false;
            }
            boolean used = liveMission.useEmergencyScanner(voxelWorld, playerController.state());
            lastAction = liveMission.lastMessage();
            if (used) {
                markSaveDirty();
            }
            return used;
        }

        private boolean placeTarget() {
            if (target == null) {
                lastAction = "place: aim at a loaded block face";
                return false;
            }
            EchoVoxelHotbarSlot selected = hotbar.selected();
            if (selected.empty()) {
                lastAction = "place: selected slot " + (hotbar.selectedSlot() + 1) + " is empty";
                return false;
            }
            if (!selected.block().solid()) {
                lastAction = selected.block().displayName() + " is usable, not placeable";
                return false;
            }
            int placeX = target.x() + target.normalX();
            int placeY = target.y() + target.normalY();
            int placeZ = target.z() + target.normalZ();
            if (!voxelWorld.blockAt(placeX, placeY, placeZ).air()) {
                lastAction = "place blocked @ " + placeX + "," + placeY + "," + placeZ;
                return false;
            }
            if (playerController.state().intersectsBlock(placeX, placeY, placeZ)) {
                lastAction = "place blocked by player body @ " + placeX + "," + placeY + "," + placeZ;
                return false;
            }
            boolean changed = voxelWorld.setBlockAt(placeX, placeY, placeZ, selected.block());
            EchoVoxelHotbarMutation consume = changed
                    ? hotbar.consumeSelected()
                    : new EchoVoxelHotbarMutation(false, "place_failed", hotbar.selected());
            if (changed) {
                liveEdits.add(new EchoStandalonePlayableVoxelEdit(
                        placeX,
                        placeY,
                        placeZ,
                        EchoVoxelBlock.AIR.id(),
                        selected.block().id()
                ));
                liveMission.markShelterBuilt(selected.block(), placeX, placeY, placeZ, playerController.state());
                liveMission.markRainCollectorBuilt(selected.block(), adapterCoreBridge.waterLoopProfile());
                liveMission.markWaterPurifierBuilt(selected.block(), adapterCoreBridge.waterLoopProfile());
                liveMission.markHandRecyclerBuilt(selected.block(), adapterCoreBridge.fieldWorkshopProfile());
                markSaveDirty();
                playAudio("ashfall:block_place", "block_place");
            }
            lastAction = changed
                    ? "placed " + selected.block().displayName() + " @ " + placeX + "," + placeY + "," + placeZ
                    + " / " + consume.reason()
                    : "place failed outside loaded chunk";
            return changed;
        }

        private boolean selectHotbarSlot(int index) {
            EchoVoxelHotbarMutation mutation = hotbar.select(index);
            playerController.selectSlot(index);
            lastAction = "selected slot " + (index + 1) + ": " + mutation.slot().label()
                    + " x" + mutation.slot().count();
            if (mutation.changed()) {
                markSaveDirty();
            }
            return mutation.changed();
        }

        private boolean hasHotbarItem(String blockId) {
            for (EchoVoxelHotbarSlot slot : hotbar.slots()) {
                if (!slot.empty() && slot.block().id().equals(blockId)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void removeNotify() {
            if (saveDirty) {
                saveLiveSession("autosave");
            }
            playerTimer.stop();
            keepAliveTimer.stop();
            titleAnimTimer.stop();
            loadingTimer.stop();
            if (keyDispatcher != null) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyDispatcher);
                keyDispatcher = null;
            }
            renderExecutor.shutdownNow();
            super.removeNotify();
        }

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }

        @Override
        public void paint(Graphics graphics) {
            BufferedImage buffer = backBuffer();
            Graphics2D g = buffer.createGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawViewport(g);
            } catch (RuntimeException exception) {
                drawPaintFailure(g, exception);
            } finally {
                g.dispose();
            }
            graphics.drawImage(buffer, 0, 0, null);
            if (shellState.gameplayActive()) {
                graphics.setColor(java.awt.Color.WHITE);
                graphics.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
                graphics.drawString(displayedFps + " FPS", 10, 20);
            } else if (shellState.loadingActive()) {
                drawLoadingOverlay((Graphics2D) graphics);
            }
        }

        private BufferedImage backBuffer() {
            int width = Math.max(1, getWidth());
            int height = Math.max(1, getHeight());
            if (backBuffer == null || backBuffer.getWidth() != width || backBuffer.getHeight() != height) {
                backBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            }
            return backBuffer;
        }

        private void drawLoadingOverlay(Graphics2D g) {
            int width = Math.max(1, getWidth());
            int height = Math.max(1, getHeight());
            g.setColor(new java.awt.Color(4, 10, 11, 220));
            g.fillRect(0, 0, width, height);
            g.setFont(new java.awt.Font(java.awt.Font.SANS_SERIF, java.awt.Font.BOLD, 28));
            g.setColor(new java.awt.Color(113, 211, 183));
            String title = shellState.title();
            int titleWidth = g.getFontMetrics().stringWidth(title);
            g.drawString(title, (width - titleWidth) / 2, height / 2 - 20);
            g.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 14));
            g.setColor(new java.awt.Color(151, 172, 167));
            String sub = "AdapterCore world generation in progress";
            int subWidth = g.getFontMetrics().stringWidth(sub);
            g.drawString(sub, (width - subWidth) / 2, height / 2 + 14);
            int barWidth = Math.min(400, width - 80);
            int barX = (width - barWidth) / 2;
            int barY = height / 2 + 36;
            g.setColor(new java.awt.Color(4, 10, 11, 180));
            g.fillRoundRect(barX, barY, barWidth, 14, 7, 7);
            g.setColor(new java.awt.Color(113, 211, 183));
            int fill = Math.max(4, (int) (barWidth * loadingProgress));
            g.fillRoundRect(barX, barY, fill, 14, 7, 7);
            g.setColor(new java.awt.Color(93, 194, 177, 136));
            g.drawRoundRect(barX, barY, barWidth, 14, 7, 7);
        }

        private void drawPaintFailure(Graphics2D g, RuntimeException exception) {
            g.setColor(BACKGROUND_TOP);
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(DANGER);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
            g.drawString("ECHO runtime paint failure", 32, 48);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            g.setColor(TEXT);
            g.drawString(exception.getClass().getSimpleName() + ": " + trim(String.valueOf(exception.getMessage()), 80), 34, 78);
        }

        private void drawBackground(Graphics2D g) {
            g.setPaint(new GradientPaint(0, 0, BACKGROUND_TOP, 0, getHeight(), BACKGROUND_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(new Color(90, 160, 142, 24));
            for (int x = -80; x < getWidth(); x += 96) {
                g.drawLine(x, 0, x + 240, getHeight());
            }
        }

        private void drawHeader(Graphics2D g) {
            int x = 20;
            int y = 22;
            g.setColor(new Color(4, 10, 11, 168));
            g.fillRoundRect(x - 8, y - 16, 408, 54, 8, 8);
            g.setColor(TEXT);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
            g.drawString("ECHO Ashfall Standalone", x, y + 4);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            g.setColor(MUTED);
            g.drawString(trim("AdapterCore / " + graphicsAudit.hudSummary(), 50), x + 2, y + 26);
            drawPill(g, getWidth() - 238, 18, mouseCaptured ? "ESC frees" : "ESC closes", WARNING);
            drawPill(g, getWidth() - 126, 18, "LIVE", ACCENT);
        }

        private void drawViewport(Graphics2D g) {
            int width = Math.max(1, getWidth());
            int height = Math.max(1, getHeight());
            g.setColor(BACKGROUND_TOP);
            g.fillRect(0, 0, width, height);
            if (voxelImage == null) {
                renderVoxelFrame();
            }
            if (voxelImage != null) {
                g.drawImage(voxelImage, 0, 0, width, height, null);
            }
        }

        private void drawHudCrosshair(Graphics2D g, int x, int y) {
            g.setStroke(new BasicStroke(3f));
            g.setColor(new Color(0, 0, 0, 160));
            g.drawLine(x - 13, y, x - 4, y);
            g.drawLine(x + 4, y, x + 13, y);
            g.drawLine(x, y - 13, x, y - 4);
            g.drawLine(x, y + 4, x, y + 13);
            g.setStroke(new BasicStroke(1.4f));
            g.setColor(new Color(236, 248, 241, 224));
            g.drawLine(x - 12, y, x - 4, y);
            g.drawLine(x + 4, y, x + 12, y);
            g.drawLine(x, y - 12, x, y - 4);
            g.drawLine(x, y + 4, x, y + 12);
        }

        private void drawHotbar(Graphics2D g, int x, int y, int availableWidth) {
            int gap = 6;
            int slotSize = Math.max(42, Math.min(56, (availableWidth - gap * 8) / EchoVoxelPlayerHotbar.HOTBAR_COUNT));
            int totalWidth = slotSize * EchoVoxelPlayerHotbar.HOTBAR_COUNT + gap * 8;
            int startX = x + Math.max(0, (availableWidth - totalWidth) / 2);
            for (EchoVoxelHotbarSlot slot : hotbar.hotbarSlots()) {
                int slotX = startX + slot.index() * (slotSize + gap);
                boolean selected = slot.index() == hotbar.selectedSlot();
                g.setColor(selected ? new Color(113, 211, 183, 88) : new Color(4, 10, 11, 176));
                g.fillRoundRect(slotX, y, slotSize, slotSize, 8, 8);
                g.setColor(selected ? ACCENT : LINE);
                g.setStroke(new BasicStroke(selected ? 2.0f : 1.0f));
                g.drawRoundRect(slotX, y, slotSize, slotSize, 8, 8);
                g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
                g.setColor(selected ? ACCENT : MUTED);
                g.drawString(String.valueOf(slot.index() + 1), slotX + 6, y + 14);
                if (!slot.empty()) {
                    g.setColor(new Color(slot.block().argb(), true));
                    g.fillRect(slotX + slotSize / 2 - 8, y + 18, 16, 16);
                    g.setColor(TEXT);
                    g.drawString(String.valueOf(slot.count()), slotX + slotSize - 20, y + slotSize - 8);
                }
            }
        }

        private void drawStatus(Graphics2D g) {
            int width = Math.min(332, Math.max(280, getWidth() - 40));
            int x = Math.max(20, getWidth() - width - 20);
            int y = 70;
            panel(g, x, y, width, 216);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            g.setColor(TEXT);
            g.drawString(liveMission.status() + " / " + trim(liveMission.nextObjective(), 24), x + 16, y + 26);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            metric(g, x + 16, y + 54, "Health", String.valueOf(liveMission.playerHealth()), liveMission.playerHealth() > 0 ? ACCENT : DANGER);
            metric(g, x + 16, y + 76, "Hydration", String.format("%.0f", liveMission.hydration()), liveMission.hydration() > 25.0D ? ACCENT : WARNING);
            metric(g, x + 16, y + 98, "Ash", String.format("%.1f", liveMission.ashExposure()), liveMission.ashExposure() < 70.0D ? WARNING : DANGER);
            metric(g, x + 16, y + 120, "Player", trim(playerPositionLabel(), 22), ACCENT);
            metric(g, x + 16, y + 142, "Mode", trim(playerModeLabel(), 22), playerController.state().grounded() ? ACCENT : WARNING);
            metric(g, x + 16, y + 164, "Modules", moduleCoverageLabel(), moduleCoverageColor());
            metric(g, x + 16, y + 186, "Render", trim(renderStatsLabel(), 22),
                    graphicsAudit.persistentGameWindowPresenter() ? ACCENT : WARNING);
            metric(g, x + 16, y + 208, "OpenGL", trim(rendererStatusLabel(), 22), rendererStatusColor());
        }

        private String renderStatsLabel() {
            String frameSize = voxelFrame == null
                    ? framebufferWidth + "x" + framebufferHeight
                    : voxelFrame.width() + "x" + voxelFrame.height();
            return frameSize + " " + displayedFps + " fps";
        }

        private String moduleCoverageLabel() {
            if (moduleCoverage.totalCount() == 0) {
                return "not scanned";
            }
            return moduleCoverage.activeCount() + "/" + moduleCoverage.totalCount() + " active";
        }

        private String moduleGapLabel() {
            if (moduleCoverage.totalCount() == 0) {
                return "no catalog";
            }
            return moduleCoverage.adapterGapCount() + " gaps, " + moduleCoverage.unsupportedCount() + " off";
        }

        private Color moduleCoverageColor() {
            return moduleCoverage.totalCount() > 0 && moduleCoverage.unsupportedCount() == 0
                    ? ACCENT
                    : WARNING;
        }

        private String rendererStatusLabel() {
            return graphicsAudit.adapterCoreTargetsOpenGl()
                    ? "opengl target / software presenter"
                    : "opengl target pending";
        }

        private Color rendererStatusColor() {
            return graphicsAudit.adapterCoreTargetsOpenGl() ? ACCENT : WARNING;
        }

        private void drawMissionFeed(Graphics2D g) {
            int width = Math.min(520, Math.max(280, getWidth() - 40));
            int x = 20;
            int y = Math.max(252, getHeight() - 236);
            panel(g, x, y, width, 72);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            g.setColor(TEXT);
            g.drawString("Ashfall Feed", x + 14, y + 23);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            List<String> steps = liveMission.feed();
            for (int i = 0; i < Math.min(2, steps.size()); i++) {
                String step = steps.get(i);
                int columnX = x + 14 + i * Math.max(220, (width - 28) / 2);
                g.setColor(ACCENT);
                g.drawString("0" + i + " " + trim(step, 24), columnX, y + 48);
                g.setColor(MUTED);
                g.drawString(trim(liveMission.objectiveSummary(i), 24), columnX, y + 62);
            }
        }

        private void panel(Graphics2D g, int x, int y, int width, int height) {
            g.setColor(PANEL);
            g.fillRoundRect(x, y, width, height, 10, 10);
            g.setColor(LINE);
            g.drawRoundRect(x, y, width, height, 10, 10);
        }

        private void metric(Graphics2D g, int x, int y, String label, String value, Color valueColor) {
            g.setColor(MUTED);
            g.drawString(label, x, y);
            g.setColor(valueColor);
            g.drawString(value, x + 136, y);
        }

        private void drawPill(Graphics2D g, int x, int y, String label, Color color) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 48));
            g.fillRoundRect(x, y, 96, 28, 14, 14);
            g.setColor(color);
            g.drawRoundRect(x, y, 96, 28, 14, 14);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            g.drawString(label, x + 16, y + 18);
        }

        private static BufferedImage voxelImage(EchoVoxelFramebuffer framebuffer) {
            BufferedImage image = new BufferedImage(
                    framebuffer.width(),
                    framebuffer.height(),
                    BufferedImage.TYPE_INT_ARGB
            );
            image.setRGB(0, 0, framebuffer.width(), framebuffer.height(), framebuffer.argb(), 0, framebuffer.width());
            return image;
        }

        private String targetLabel() {
            if (target == null) {
                return "none";
            }
            return target.block().displayName() + "@" + target.x() + "," + target.y() + "," + target.z();
        }

        private String playerPositionLabel() {
            EchoVoxelPlayerState player = playerController.state();
            return player.blockPosition() + " yaw=" + Math.round(player.yawDegrees());
        }

        private String playerModeLabel() {
            EchoVoxelPlayerState player = playerController.state();
            String mode = player.grounded() ? "grounded" : "airborne";
            if (player.sprinting()) {
                mode += "/sprint";
            }
            if (player.crouching()) {
                mode += "/crouch";
            }
            return mode + "/" + lastMovement;
        }

        private static String trim(String value, int maxLength) {
            if (value == null || value.isBlank()) {
                return "unknown";
            }
            if (value.length() <= maxLength) {
                return value;
            }
            return value.substring(0, Math.max(0, maxLength - 3)) + "...";
        }
    }
}
