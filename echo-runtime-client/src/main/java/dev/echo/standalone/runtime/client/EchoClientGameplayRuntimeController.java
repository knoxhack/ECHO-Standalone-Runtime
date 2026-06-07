package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
final class EchoClientGameplayRuntimeController {
    private static final double AUTO_PICKUP_MIN_AGE_SECONDS = 0.55D;
    private static final double AUTO_PICKUP_CHECK_SECONDS = 0.10D;

    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientWorldSessionController worldSessions;
    private EchoClientParticleRuntimeController particleRuntime;
    private boolean debugOverlayEnabled;
    private double autoPickupCheckSeconds;

    EchoClientGameplayRuntimeController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientWorldSessionController worldSessions
    ) {
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.worldSessions = worldSessions;
    }

    boolean debugOverlayEnabled() {
        return debugOverlayEnabled;
    }

    void attachParticles(EchoClientParticleRuntimeController particleRuntime) {
        this.particleRuntime = particleRuntime;
    }

    void updateActiveGameplay(EchoClientInput input, double dt, Host host) {
        if (!runtimeServices.hasActiveWorld()) {
            autoPickupCheckSeconds = 0.0D;
            updateParticles(dt);
            screens.showMainMenu(runtimeServices.hasContinuableSession());
            input.setCursorLocked(false);
            return;
        }

        EchoClientGameSession session = runtimeServices.session();
        EchoClientGameplay gameplay = runtimeServices.gameplay();
        input.setCursorLocked(true);
        if (input.consumePause()) {
            screens.showPauseMenu();
            input.setCursorLocked(false);
            input.clearGameplayTriggers();
            return;
        }
        if (input.consumeOpenInventory()) {
            host.clearInventoryDrag();
            screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, runtimeServices.hasContinuableSession());
            input.setCursorLocked(false);
            input.clearGameplayTriggers();
            return;
        }
        if (input.consumeDebugOverlay()) {
            debugOverlayEnabled = !debugOverlayEnabled;
            screens.showToast(debugOverlayEnabled ? "Debug overlay on" : "Debug overlay off");
        }
        if (input.consumeSwapOffhand()) {
            if (runtimeServices.swapSelectedWithOffhand()) {
                screens.showToast("Swapped offhand");
            }
        }
        if (input.consumeDropItem()) {
            EchoClientDroppedItem drop = runtimeServices.dropSelectedItem();
            if (drop != null) {
                screens.showToast("Dropped " + drop.definition().displayName());
            }
        }

        EchoVoxelPlayerInput playerInput = input.poll(dt);
        gameplay.tick(playerInput, input, dt, session);
        runtimeServices.updateWorldSessionFromGameplay();
        EchoClientSelectedItemUse selectedItemUse = gameplay.consumeSelectedItemUse();
        if (selectedItemUse.active()) {
            screens.showToast(selectedItemUse.toastText());
        }
        EchoClientScreenRouteRequest interactionRoute = gameplay.consumePendingScreenRoute();
        updateParticles(dt);
        if (interactionRoute.active()) {
            host.clearInventoryDrag();
            screens.updateWorkbenchRecipes(
                    runtimeServices.workbenchRecipeSummaries(),
                    runtimeServices.workbenchRecipeError()
            );
            executeScreenRoute(interactionRoute);
            input.setCursorLocked(false);
            input.clearGameplayTriggers();
            return;
        }

        session = runtimeServices.session();
        if (session == null) {
            return;
        }
        session.tickPlayerSurvival(dt, playerInput);
        session.tickBiomeHazards(dt);

        host.refreshWorldStreamingAndMeshes();
        session = runtimeServices.session();
        if (session == null) {
            return;
        }
        session.tickEntities(dt);
        pickupNearbyDroppedItems(dt);
        if (showDeathScreenIfNeeded(session)) {
            input.setCursorLocked(false);
            input.clearGameplayTriggers();
            return;
        }

        if (input.consumeSave()) {
            captureMemorySave();
        }
        if (input.consumeLoad()) {
            restoreMemorySave(host);
        }
    }

    void tickPassiveWorld(double dt, Host host) {
        if (!runtimeServices.hasActiveWorld()) {
            autoPickupCheckSeconds = 0.0D;
            updateParticles(dt);
            return;
        }
        EchoClientGameSession session = runtimeServices.session();
        EchoClientGameplay gameplay = runtimeServices.gameplay();
        gameplay.tickPassive(dt);
        runtimeServices.updateWorldSessionFromGameplay();
        updateParticles(dt);
        session = runtimeServices.session();
        if (session != null) {
            session.tickPlayerSurvival(dt, EchoVoxelPlayerInput.idle());
        }

        host.refreshWorldStreamingAndMeshes();
        session = runtimeServices.session();
        if (session == null) {
            return;
        }
        session.tickEntities(dt);
        pickupNearbyDroppedItems(dt);
        if (showDeathScreenIfNeeded(session)) {
            host.clearInventoryDrag();
        }
    }

    void captureMemorySave() {
        runtimeServices.captureMemorySave();
        screens.showToast("Session saved");
        System.out.println("[echo-client] in-memory ScreenCore session save captured");
    }

    private void restoreMemorySave(Host host) {
        if (!runtimeServices.restoreMemorySave()) {
            System.out.println("[echo-client] load requested but no in-memory save exists");
            return;
        }
        host.attachSession();
        worldSessions.showInGameOrDeathScreen();
        screens.showToast("Session restored");
        System.out.println("[echo-client] in-memory ScreenCore session save restored");
    }

    private void executeScreenRoute(EchoClientScreenRouteRequest route) {
        if (route == null || !route.active()) {
            return;
        }
        if (route.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN
                && !route.targetId().isBlank()
                && screens.openRegisteredScreen(route.targetId(), runtimeServices.hasContinuableSession())) {
            return;
        }
        screens.executeNavigationCommand(route.command(), runtimeServices.hasContinuableSession());
    }

    private void pickupNearbyDroppedItems(double dt) {
        autoPickupCheckSeconds += Math.max(0.0D, dt);
        if (autoPickupCheckSeconds < AUTO_PICKUP_CHECK_SECONDS) {
            return;
        }
        autoPickupCheckSeconds = 0.0D;
        runtimeServices.pickupNearbyDroppedItems(AUTO_PICKUP_MIN_AGE_SECONDS);
    }

    private void updateParticles(double dt) {
        if (particleRuntime != null) {
            particleRuntime.updateFromGameplay(dt);
        }
    }

    private boolean showDeathScreenIfNeeded(EchoClientGameSession session) {
        if (session == null || session.playerVitals().alive()) {
            return false;
        }
        screens.showDeathScreen();
        screens.showToast("You died");
        return true;
    }

    interface Host {
        void clearInventoryDrag();

        void refreshWorldStreamingAndMeshes();

        void attachSession();
    }
}
