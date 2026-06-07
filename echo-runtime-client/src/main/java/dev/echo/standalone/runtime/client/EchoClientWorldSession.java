package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.util.List;

final class EchoClientWorldSession {
    private final String slotId;
    private final String displayName;
    private final EchoClientGameSession gameSession;

    EchoClientWorldSession(String slotId, String displayName, EchoClientGameSession gameSession) {
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("slotId must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        if (gameSession == null) {
            throw new IllegalArgumentException("gameSession must not be null");
        }
        this.slotId = slotId;
        this.displayName = displayName;
        this.gameSession = gameSession;
    }

    static EchoClientWorldSession fromSavedSession(
            String slotId,
            String displayName,
            EchoClientSavedSessionSnapshot snapshot
    ) {
        return fromSavedSession(slotId, displayName, snapshot, List.of());
    }

    static EchoClientWorldSession fromSavedSession(
            String slotId,
            String displayName,
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes
    ) {
        return EchoClientWorldSessionFactory.defaultFactory()
                .restoreSavedSession(slotId, displayName, snapshot, recipes);
    }

    static EchoClientWorldSession fromSnapshot(
            String slotId,
            String displayName,
            EchoClientGameplay.GameplaySnapshot snapshot
    ) {
        return EchoClientWorldSessionFactory.defaultFactory()
                .restoreGameplaySnapshot(slotId, displayName, snapshot, List.of());
    }

    String slotId() {
        return slotId;
    }

    String displayName() {
        return displayName;
    }

    EchoClientGameSession gameSession() {
        return gameSession;
    }

    EchoClientWorldSession withDisplayName(String displayName) {
        return new EchoClientWorldSession(slotId, displayName, gameSession);
    }

    void updateFromGameplay(EchoClientGameplay gameplay) {
        gameSession.updateFromGameplay(gameplay);
    }

    EchoClientWorldStreamResult streamAroundPlayer() {
        return gameSession.streamAroundPlayer();
    }

    EchoClientWorldStreamResult streamAroundPlayer(int chunkViewDistance) {
        return gameSession.streamAroundPlayer(chunkViewDistance);
    }

}
