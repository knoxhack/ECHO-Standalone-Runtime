package dev.echo.standalone.runtime.client;

public final class EchoClientGameplayRuntimeSmokeHarness {
    private EchoClientGameplayRuntimeSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientGameplayRuntimeController gameplayRuntime =
                new EchoClientGameplayRuntimeController(services, screens, worldSessions);
        CountingHost host = new CountingHost();

        services.startNewWorld("gameplay-runtime");
        String slotId = services.worldSession().slotId();
        screens.showInGame();
        gameplayRuntime.tickPassiveWorld(1.0D / 20.0D, host);

        require(services.hasActiveWorld(), "Passive gameplay tick should keep the active world attached");
        require(services.worldSession().slotId().equals(slotId),
                "Passive gameplay tick should preserve the active save slot");
        require(host.refreshCount == 1,
                "Passive gameplay tick should request exactly one render/world stream refresh");
        require(!gameplayRuntime.debugOverlayEnabled(),
                "Debug overlay should default to disabled before F3 input");

        gameplayRuntime.captureMemorySave();
        services.unloadWorld();
        require(!services.hasActiveWorld(), "Test setup should unload the active world before memory restore");
        require(services.restoreMemorySave(), "Runtime services should restore the controller-captured memory save");
        require(services.worldSession().slotId().equals(slotId),
                "Controller-captured memory save should preserve the active save slot id");

        System.out.println("client gameplay runtime smoke PASS slot=" + slotId + " refreshes=" + host.refreshCount);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class CountingHost implements EchoClientGameplayRuntimeController.Host {
        private int refreshCount;

        @Override
        public void clearInventoryDrag() {
        }

        @Override
        public void refreshWorldStreamingAndMeshes() {
            refreshCount++;
        }

        @Override
        public void attachSession() {
        }
    }
}
