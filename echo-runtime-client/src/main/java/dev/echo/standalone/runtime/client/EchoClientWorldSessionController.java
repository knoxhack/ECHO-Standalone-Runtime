package dev.echo.standalone.runtime.client;

final class EchoClientWorldSessionController {
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientLaunchContext launchContext;

    private PendingWorldLoad pendingWorldLoad = PendingWorldLoad.NONE;
    private String pendingContinueSlotId = "";

    EchoClientWorldSessionController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens
    ) {
        this(runtimeServices, screens, EchoClientLaunchContext.empty());
    }

    EchoClientWorldSessionController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientLaunchContext launchContext
    ) {
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.launchContext = launchContext == null ? EchoClientLaunchContext.empty() : launchContext;
    }

    boolean beginNewWorldLoad() {
        pendingWorldLoad = PendingWorldLoad.NEW_WORLD;
        pendingContinueSlotId = "";
        screens.startLoadingNewGame();
        return true;
    }

    boolean beginContinueWorldLoad() {
        if (!runtimeServices.hasContinuableSession()) {
            return false;
        }
        pendingWorldLoad = PendingWorldLoad.CONTINUE_WORLD;
        pendingContinueSlotId = screens.selectedSaveSlotId();
        screens.startLoadingSavedWorld(screens.selectedSaveSlotLabel());
        return true;
    }

    LoadCompletion finishPendingWorldLoad() {
        PendingWorldLoad action = pendingWorldLoad;
        String slotId = pendingContinueSlotId;
        clearPendingWorldLoad();
        return switch (action) {
            case NEW_WORLD -> startNewGameSession();
            case CONTINUE_WORLD -> finishContinueWorldLoad(slotId);
            case NONE -> {
                screens.showMainMenu(runtimeServices.hasContinuableSession());
                screens.showToast("No world load queued");
                yield LoadCompletion.NONE;
            }
        };
    }

    boolean resumeOrTitle() {
        if (runtimeServices.hasActiveWorld()) {
            showInGameOrDeathScreen();
            return true;
        }
        screens.showMainMenu(runtimeServices.hasContinuableSession());
        return false;
    }

    boolean respawn() {
        if (!runtimeServices.hasActiveWorld()) {
            return false;
        }
        EchoClientGameSession session = runtimeServices.session();
        if (session == null) {
            return false;
        }
        session.respawnPlayer();
        screens.showInGame();
        screens.showToast("Respawned");
        return true;
    }

    void quitToTitle() {
        clearPendingWorldLoad();
        boolean canContinue = runtimeServices.hasContinuableSession();
        runtimeServices.unloadWorld();
        screens.showMainMenu(canContinue);
    }

    void showInGameOrDeathScreen() {
        EchoClientGameSession session = runtimeServices.session();
        if (session != null && !session.playerVitals().alive()) {
            screens.showDeathScreen();
        } else {
            screens.showInGame();
        }
    }

    private LoadCompletion startNewGameSession() {
        String worldName = screens.worldName();
        runtimeServices.startNewWorld(screens.worldSeed(), worldName);
        screens.showInGame();
        screens.showToast("World created");
        System.out.println("[echo-client] Joined world: " + worldName);
        launchContext.appendInstanceLog("[echo-client] Joined world: " + worldName);
        return LoadCompletion.SESSION_ATTACHED;
    }

    private LoadCompletion finishContinueWorldLoad(String slotId) {
        if (runtimeServices.continueFromSlot(slotId)) {
            showInGameOrDeathScreen();
            screens.showToast("World loaded");
            launchContext.appendInstanceLog("[echo-client] Joined world: " + slotId);
            return LoadCompletion.SESSION_ATTACHED;
        }
        screens.showMainMenu(runtimeServices.hasContinuableSession());
        screens.showToast("World load failed");
        return LoadCompletion.NONE;
    }

    private void clearPendingWorldLoad() {
        pendingWorldLoad = PendingWorldLoad.NONE;
        pendingContinueSlotId = "";
    }

    enum LoadCompletion {
        NONE,
        SESSION_ATTACHED;

        boolean sessionAttached() {
            return this == SESSION_ATTACHED;
        }
    }

    private enum PendingWorldLoad {
        NONE,
        NEW_WORLD,
        CONTINUE_WORLD
    }
}
