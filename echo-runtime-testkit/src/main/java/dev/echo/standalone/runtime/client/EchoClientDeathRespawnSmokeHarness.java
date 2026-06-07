package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityAiComponent;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityHealthComponent;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementComponent;
import dev.echo.standalone.runtime.entity.EchoEntityPositionComponent;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

public final class EchoClientDeathRespawnSmokeHarness {
    private EchoClientDeathRespawnSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        session.entityStore().register(entity("test:death_guard", new EchoWorldPosition(4, 5, -4)));
        session.damagePlayer(EchoClientPlayerVitals.DEFAULT_MAX_HEALTH);
        require(!session.playerVitals().alive(),
                "Fatal damage should put player vitals into dead state");

        EchoClientScreenController screens = new EchoClientScreenController();
        screens.showDeathScreen();
        EchoClientScreenSnapshot death = screens.snapshot(true);
        require(death.state() == EchoClientGameState.DEAD,
                "Death screen should enter the DEAD game state");
        require(death.kind() == EchoClientScreenKind.DEATH_SCREEN,
                "Death screen should use the ScreenCore death route kind");
        require(death.title().equals("YOU DIED"),
                "Death screen title should match Minecraft-like death flow");
        require(death.footer().contains("echoscreencore:death_screen"),
                "Death screen footer should expose the ScreenCore route id");
        require(death.options().stream().anyMatch(option ->
                        option.label().equals("Respawn") && option.command() == EchoClientScreenCommand.RESPAWN),
                "Death screen should expose a respawn command");
        require(screens.activateSelection(true) == EchoClientScreenCommand.RESPAWN,
                "Default death-screen selection should activate Respawn");
        require(screens.escapeCommand() == EchoClientScreenCommand.NONE,
                "Esc should not dismiss the death screen into dead gameplay");

        EchoClientSavedSessionSnapshot deadSnapshot = session.savedSessionSnapshot();
        EchoClientWorldSession deadRestored = EchoClientWorldSessionFactory.defaultFactory()
                .restoreSavedSession(deadSnapshot);
        require(!deadRestored.gameSession().playerVitals().alive(),
                "Saved dead sessions should restore as dead until respawned");

        session.respawnPlayer();
        require(session.playerVitals().alive()
                        && session.playerVitals().currentHealth() == session.playerVitals().maxHealth(),
                "Respawn should restore full health");
        require(session.entityStore().count() == 0,
                "Respawn should clear active hostile entities around the old death point");
        require(close(session.player().state().x(), session.world().spawnX())
                        && close(session.player().state().z(), session.world().spawnZ()),
                "Respawn should return the player to the world spawn position");

        EchoClientScreenCatalog catalog = EchoClientScreenCatalog.loadDefault();
        require(catalog.findScreen("echoscreencore:death_screen").isPresent(),
                "Screen catalog should register the death-screen route");

        System.out.println("client death respawn smoke PASS route=echoscreencore:death_screen");
    }

    private static EchoEntityState entity(String entityId, EchoWorldPosition position) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                "echoashfallprotocol:rad_zombie",
                "Rad Zombie",
                EchoEntityKind.HOSTILE,
                20,
                1,
                "hostile_scavenger"
        );
        return new EchoEntityState(
                new EchoEntityId(entityId),
                definition,
                new EchoEntityPositionComponent(position),
                new EchoEntityHealthComponent(definition.maxHealth(), definition.maxHealth()),
                new EchoEntityMovementComponent(definition.movementSpeed(), true),
                new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.IDLE)
        );
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) <= 0.0001D;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
