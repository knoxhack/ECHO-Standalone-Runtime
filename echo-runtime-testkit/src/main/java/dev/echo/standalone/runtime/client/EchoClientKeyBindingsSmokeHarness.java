package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoClientKeyBindingsSmokeHarness {
    private EchoClientKeyBindingsSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireDefaultBindings();
        requirePersistenceAndControlsScreen();
        requireSettingsRuntimeApplication();
        System.out.println("client key bindings smoke PASS forward=Up drop=R reset=W");
    }

    private static void requireDefaultBindings() {
        EchoClientKeyBindings defaults = EchoClientKeyBindings.defaults();
        require(defaults.label(EchoClientKeyAction.MOVE_FORWARD).equals("W"),
                "Default forward binding should be W");
        require(defaults.label(EchoClientKeyAction.OPEN_INVENTORY).equals("E"),
                "Default inventory binding should be E");
        require(defaults.label(EchoClientKeyAction.SCREENSHOT).equals("F2"),
                "Default screenshot binding should be F2");
        require(defaults.label(EchoClientKeyAction.TOGGLE_FULLSCREEN).equals("F11"),
                "Default fullscreen toggle binding should be F11");
        require(defaults.hotbarSummary().equals("1 2 3 4 5 6 7 8 9"),
                "Default hotbar bindings should be number keys");

        EchoClientKeyBindings decoded = EchoClientKeyBindings.decode(
                "move_forward=UP;drop_item=R;toggle_fullscreen=F10;hotbar_1=Z;unknown_action=F12"
        );
        require(decoded.label(EchoClientKeyAction.MOVE_FORWARD).equals("Up"),
                "Decoded forward binding should accept named keys");
        require(decoded.label(EchoClientKeyAction.DROP_ITEM).equals("R"),
                "Decoded drop binding should accept letter keys");
        require(decoded.label(EchoClientKeyAction.TOGGLE_FULLSCREEN).equals("F10"),
                "Decoded fullscreen toggle binding should accept function keys");
        require(decoded.label(EchoClientKeyAction.HOTBAR_1).equals("Z"),
                "Decoded hotbar binding should accept letter overrides");
        require(decoded.label(EchoClientKeyAction.MOVE_BACKWARD).equals("S"),
                "Missing decoded actions should keep defaults");
        require(EchoClientKeyBindings.decode(decoded.encode()).label(EchoClientKeyAction.DROP_ITEM).equals("R"),
                "Encoded key bindings should round-trip through the options format");
    }

    private static void requirePersistenceAndControlsScreen() throws IOException {
        Path root = Files.createTempDirectory("echo-client-keybind-smoke");
        EchoClientSettingsStore store = new EchoClientSettingsStore(root.resolve("options.properties"));
        EchoClientKeyBindings custom = EchoClientKeyBindings.decode(
                "move_forward=UP;drop_item=R;toggle_fullscreen=F10;hotbar_1=Z"
        );
        EchoClientSettings settings = new EchoClientSettings(
                50,
                true,
                EchoClientSettings.DEFAULT_FOV_DEGREES,
                50,
                false,
                true,
                EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE,
                80,
                55,
                70,
                "en_us",
                true,
                false,
                false,
                custom
        );
        store.save(settings);
        require(store.lastError().isBlank(), "Saving keybinding settings should succeed");
        EchoClientSettings loaded = store.load();
        require(loaded.keyBindings().label(EchoClientKeyAction.MOVE_FORWARD).equals("Up"),
                "Options reload should preserve the custom forward binding");
        require(loaded.keyBindings().label(EchoClientKeyAction.DROP_ITEM).equals("R"),
                "Options reload should preserve the custom drop binding");
        require(loaded.keyBindings().label(EchoClientKeyAction.TOGGLE_FULLSCREEN).equals("F10"),
                "Options reload should preserve the custom fullscreen toggle binding");
        require(loaded.keyBindings().label(EchoClientKeyAction.HOTBAR_1).equals("Z"),
                "Options reload should preserve the custom hotbar binding");

        EchoClientScreenController screens = new EchoClientScreenController(loaded);
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTROLS, false),
                "Controls should open for keybinding smoke");
        EchoClientScreenSnapshot controls = screens.snapshot(false);
        require(controls.options().stream().anyMatch(option -> option.label().equals("Move Forward: Up")),
                "Controls should render the custom forward binding");
        require(controls.options().stream().anyMatch(option -> option.label().equals("Drop Item: R")),
                "Controls should render the custom drop binding");
        require(controls.options().stream().anyMatch(option -> option.label().equals("Toggle Fullscreen: F10")),
                "Controls should render the custom fullscreen toggle binding");
        require(controls.options().stream().anyMatch(option -> option.label().startsWith("Hotbar: Z 2 3")),
                "Controls should render the custom hotbar binding summary");
        require(controls.options().stream().anyMatch(option ->
                        option.command() == EchoClientScreenCommand.START_KEY_REBIND
                                && option.targetId().equals(EchoClientKeyAction.MOVE_FORWARD.id())
                                && option.enabled()),
                "Controls should expose Move Forward as a selectable rebind row");

        selectTarget(screens, EchoClientScreenCommand.START_KEY_REBIND, EchoClientKeyAction.MOVE_FORWARD.id());
        require(screens.activateSelection(false) == EchoClientScreenCommand.NONE,
                "Starting a key rebind should be handled inside the controls screen");
        require(screens.keyRebindActive(),
                "Controls should enter a pending key rebind state");
        require(screens.snapshot(false).options().stream()
                        .anyMatch(option -> option.label().equals("Move Forward: Press a key")),
                "Controls should render a player-facing pending rebind row");
        int keyT = EchoClientKeyBindings.decode("move_forward=T").key(EchoClientKeyAction.MOVE_FORWARD);
        require(screens.finishKeyRebind(keyT, false),
                "Controls should accept a supported key for rebinding");
        require(screens.clientSettings().keyBindings().label(EchoClientKeyAction.MOVE_FORWARD).equals("T"),
                "Controls rebind should update the selected action");
        require(screens.consumeClientSettingsDirty(),
                "Controls rebind should mark client settings dirty for persistence");
        require(screens.snapshot(false).toast().message().equals("Move Forward set to T"),
                "Controls rebind should publish a player-facing toast");

        selectCommand(screens, EchoClientScreenCommand.RESET_KEY_BINDINGS);
        require(screens.activateSelection(false) == EchoClientScreenCommand.NONE,
                "Keybinding reset should be handled inside the controls screen");
        require(screens.clientSettings().keyBindings().label(EchoClientKeyAction.MOVE_FORWARD).equals("W"),
                "Reset should restore the default forward binding");
        require(screens.clientSettings().keyBindings().label(EchoClientKeyAction.DROP_ITEM).equals("Q"),
                "Reset should restore the default drop binding");
        require(screens.clientSettings().keyBindings().label(EchoClientKeyAction.TOGGLE_FULLSCREEN).equals("F11"),
                "Reset should restore the default fullscreen toggle binding");
        require(screens.consumeClientSettingsDirty(),
                "Resetting key bindings should mark client settings dirty");
        require(screens.snapshot(false).toast().message().equals("Key bindings reset"),
                "Resetting key bindings should publish a ScreenCore toast");
    }

    private static void requireSettingsRuntimeApplication() {
        EchoClientScreenController screens = new EchoClientScreenController();
        RecordingInputTarget input = new RecordingInputTarget();
        EchoClientSettingsRuntimeController settingsRuntime = new EchoClientSettingsRuntimeController(
                screens,
                new NoopRenderTarget(),
                new EchoClientSettingsRuntimeController.WindowTarget() {
                    @Override
                    public void setFullscreen(boolean fullscreen) {
                    }

                    @Override
                    public void setVSync(boolean vSync) {
                    }
                }
        );
        settingsRuntime.attachInputTarget(input);
        EchoClientKeyBindings custom = EchoClientKeyBindings.decode("open_inventory=I");
        settingsRuntime.applyInputSettings(new EchoClientSettings(
                64,
                false,
                EchoClientSettings.DEFAULT_FOV_DEGREES,
                50,
                false,
                true,
                EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE,
                80,
                55,
                70,
                "en_us",
                true,
                false,
                false,
                custom
        ));
        require(input.mouseSensitivityPercent == 64,
                "Runtime input target should receive mouse sensitivity");
        require(!input.rawMouseInput,
                "Runtime input target should receive raw mouse preference");
        require(input.keyBindings.label(EchoClientKeyAction.OPEN_INVENTORY).equals("I"),
                "Runtime input target should receive custom key bindings");
    }

    private static void selectCommand(EchoClientScreenController screens, EchoClientScreenCommand command) {
        EchoClientScreenSnapshot snapshot = screens.snapshot(false);
        for (int index = 0; index < snapshot.options().size(); index++) {
            if (snapshot.options().get(index).command() == command) {
                while (screens.snapshot(false).selectedIndex() != index) {
                    screens.moveSelection(1, false, 720);
                }
                return;
            }
        }
        throw new AssertionError("Command not found: " + command);
    }

    private static void selectTarget(
            EchoClientScreenController screens,
            EchoClientScreenCommand command,
            String targetId
    ) {
        EchoClientScreenSnapshot snapshot = screens.snapshot(false);
        for (int index = 0; index < snapshot.options().size(); index++) {
            EchoClientScreenOption option = snapshot.options().get(index);
            if (option.command() == command && option.targetId().equals(targetId)) {
                while (screens.snapshot(false).selectedIndex() != index) {
                    screens.moveSelection(1, false, 720);
                }
                return;
            }
        }
        throw new AssertionError("Command target not found: " + command + " " + targetId);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingInputTarget implements EchoClientSettingsRuntimeController.InputTarget {
        private int mouseSensitivityPercent;
        private boolean rawMouseInput = true;
        private EchoClientKeyBindings keyBindings = EchoClientKeyBindings.defaults();

        @Override
        public void setMouseSensitivityPercent(int mouseSensitivityPercent) {
            this.mouseSensitivityPercent = mouseSensitivityPercent;
        }

        @Override
        public void setRawMouseInput(boolean rawMouseInput) {
            this.rawMouseInput = rawMouseInput;
        }

        @Override
        public void setKeyBindings(EchoClientKeyBindings keyBindings) {
            this.keyBindings = keyBindings;
        }
    }

    private static final class NoopRenderTarget implements EchoClientSettingsRuntimeController.RenderTarget {
        @Override
        public void setChunkViewDistance(int chunkViewDistance) {
        }

        @Override
        public void refreshWorldStreamingAndMeshes() {
        }
    }
}
