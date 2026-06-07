package dev.echo.standalone.runtime.app;

import java.util.List;

public record EchoStandaloneGameShellState(
        EchoStandaloneGameShellMode mode,
        String selectedSlotId,
        String selectedSaveKind,
        boolean continueAvailable,
        boolean autosaveAvailable,
        boolean manualSaveAvailable,
        String lastAction
) {
    public EchoStandaloneGameShellState {
        if (mode == null) {
            throw new IllegalArgumentException("mode must not be null");
        }
        selectedSlotId = selectedSlotId == null || selectedSlotId.isBlank() ? "none" : selectedSlotId.trim();
        selectedSaveKind = selectedSaveKind == null || selectedSaveKind.isBlank() ? "new_game" : selectedSaveKind.trim();
        lastAction = lastAction == null || lastAction.isBlank() ? "shell ready" : lastAction.trim();
    }

    public static EchoStandaloneGameShellState title(EchoSaveProfileContinueFlow continueFlow) {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.TITLE,
                continueFlow.selectedSlotId(),
                continueFlow.selectedSaveKind(),
                continueFlow.continueAvailable(),
                continueFlow.autosaveAvailable(),
                continueFlow.manualSaveAvailable(),
                "title menu ready"
        );
    }

    public static EchoStandaloneGameShellState titleNoSave() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.TITLE,
                "none",
                "new_game",
                false,
                false,
                false,
                "title menu ready"
        );
    }

    public EchoStandaloneGameShellState startNewGame() {
        return playing("new game started");
    }

    public EchoStandaloneGameShellState startLoading() {
        return loading("new game started");
    }

    public EchoStandaloneGameShellState loadingComplete() {
        return playing("loading complete");
    }

    public EchoStandaloneGameShellState continueGame() {
        return continueAvailable
                ? playing("continued " + selectedSlotId + " from " + selectedSaveKind)
                : this;
    }

    public EchoStandaloneGameShellState pause() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.PAUSED,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                "game paused"
        );
    }

    public EchoStandaloneGameShellState resume() {
        return playing("game resumed");
    }

    public EchoStandaloneGameShellState openOptions() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.OPTIONS,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                "options opened"
        );
    }

    public EchoStandaloneGameShellState closeOptions() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.PAUSED,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                "options closed"
        );
    }

    public EchoStandaloneGameShellState openInventory() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.INVENTORY,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                "inventory opened"
        );
    }

    public EchoStandaloneGameShellState closeInventory() {
        return playing("inventory closed");
    }

    public EchoStandaloneGameShellState openTerminal() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.TERMINAL,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                "terminal opened"
        );
    }

    public EchoStandaloneGameShellState closeTerminal() {
        return playing("terminal closed");
    }

    public EchoStandaloneGameShellState openMissionLog() {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.MISSION_LOG,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                "mission log opened"
        );
    }

    public EchoStandaloneGameShellState closeMissionLog() {
        return playing("mission log closed");
    }

    public EchoStandaloneGameShellState saveAvailable(String saveKind, String action) {
        return new EchoStandaloneGameShellState(
                mode,
                selectedSlotId,
                saveKind,
                true,
                true,
                true,
                action
        );
    }

    public boolean gameplayActive() {
        return mode == EchoStandaloneGameShellMode.PLAYING;
    }

    public boolean loadingActive() {
        return mode == EchoStandaloneGameShellMode.LOADING;
    }

    public boolean overlayVisible() {
        return mode != EchoStandaloneGameShellMode.PLAYING && mode != EchoStandaloneGameShellMode.LOADING;
    }

    public String title() {
        return switch (mode) {
            case TITLE -> "ECHO Ashfall";
            case LOADING -> "Loading World";
            case PAUSED -> "Paused";
            case OPTIONS -> "Options";
            case INVENTORY -> "Inventory";
            case TERMINAL -> "Emergency Terminal";
            case MISSION_LOG -> "Mission Log";
            case PLAYING -> "Playing";
        };
    }

    public List<String> lines() {
        return switch (mode) {
            case TITLE -> List.of(
                    "Enter / click: New Game",
                    continueAvailable
                            ? "C: Continue " + selectedSlotId + " (" + selectedSaveKind + ")"
                            : "Continue: no save slot yet",
                    "WASD + mouse after launch",
                    "Esc: pause menu"
            );
            case PAUSED -> List.of(
                    "Enter / click: Resume",
                    "O: Options",
                    manualSaveAvailable ? "Manual Save: available" : "Manual Save: pending runtime save slot",
                    autosaveAvailable ? "Autosave: available" : "Autosave: pending runtime save slot"
            );
            case OPTIONS -> List.of(
                    "Mouse Look: Minecraft-style capture",
                    "Renderer: OpenGL client presenter",
                    "Java2D legacy pipeline: disabled",
                    "Esc / Enter: Back"
            );
            case INVENTORY -> List.of(
                    "E / Esc: Back to game",
                    "1-9: Select hotbar slot",
                    "F5: Manual Save",
                    "Mouse remains released"
            );
            case TERMINAL -> List.of(
                    "Enter / click / Esc: Back to game",
                    "Cache route: unlocked after terminal sync",
                    "L: Mission Log",
                    "F5: Manual Save"
            );
            case MISSION_LOG -> List.of(
                    "L / Enter / click / Esc: Back to game",
                    "Objective chain: shelter, scanner, terminal, water, food, ash, scavenge, cache, power, extract",
                    "Status values come from the live Ashfall mission state",
                    "F5: Manual Save"
            );
            case LOADING -> List.of(
                    "Generating terrain chunks",
                    "AdapterCore registry sync",
                    "Spawning player entity",
                    "Please wait..."
            );
            case PLAYING -> List.of();
        };
    }

    public String summary() {
        return "mode=" + mode.name().toLowerCase()
                + " slot=" + selectedSlotId
                + " saveKind=" + selectedSaveKind
                + " continue=" + continueAvailable
                + " autosave=" + autosaveAvailable
                + " manual=" + manualSaveAvailable;
    }

    private EchoStandaloneGameShellState loading(String action) {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.LOADING,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                action
        );
    }

    private EchoStandaloneGameShellState playing(String action) {
        return new EchoStandaloneGameShellState(
                EchoStandaloneGameShellMode.PLAYING,
                selectedSlotId,
                selectedSaveKind,
                continueAvailable,
                autosaveAvailable,
                manualSaveAvailable,
                action
        );
    }
}
