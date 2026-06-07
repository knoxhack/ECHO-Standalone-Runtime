package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityAiTickResult;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementIntent;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntitySaveResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.entity.EchoEntityStore;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEntitySmokeHarness {
    private EchoRuntimeEntitySmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoEntityStore store = entities.store();
        EchoEntityId playerId = new EchoEntityId("player-001");
        EchoEntityId scavengerId = new EchoEntityId("scavenger-001");

        require(services.require(EchoEntityRuntimeResult.class) == entities,
                "entity runtime result should be service-bound");
        require(services.require(EchoEntityStore.class) == store,
                "entity store should be service-bound");
        require(store.count() == 2, "debug entity set should contain player and hostile");
        require(store.hostile().size() == 1, "debug entity set should contain one hostile");
        require(store.require(playerId).definition().kind() == EchoEntityKind.PLAYER,
                "player should use player entity kind");
        require(store.require(scavengerId).definition().definitionId().equals("ashfall:hostile_scavenger"),
                "scavenger should use Ashfall hostile definition");
        List<EchoEntityState> allView = store.all();
        List<EchoEntityState> livingView = store.living();
        List<EchoEntityState> hostileView = store.hostile();
        require(allView == store.all() && livingView == store.living() && hostileView == store.hostile(),
                "entity store should reuse stable read views between mutations");
        try {
            livingView.clear();
            throw new AssertionError("entity store living view should be read-only");
        } catch (UnsupportedOperationException expected) {
            // Expected read-only view.
        }

        EchoEntityMovementResult moved = entities.movementSystem().move(
                store,
                new EchoEntityMovementIntent(playerId, 1, 0)
        );
        require(moved.moved(), "player should move into an open world cell");
        require(store.all() != allView && store.living() != livingView && store.hostile() != hostileView,
                "entity store should invalidate cached read views after entity updates");
        require(store.require(playerId).worldPosition().equals(new EchoWorldPosition(1, 0, 0)),
                "player position should update after movement");

        EchoEntityMovementResult blocked = entities.movementSystem().move(
                store,
                new EchoEntityMovementIntent(playerId, 2, 3)
        );
        require(!blocked.moved(), "blocked world cell should prevent player movement");
        require(blocked.reason().equals("blocked_cell"), "blocked move should report blocked_cell");
        require(store.require(playerId).worldPosition().equals(new EchoWorldPosition(1, 0, 0)),
                "failed movement should preserve player position");

        EchoEntityAiTickResult aiTick = entities.aiSystem().tick(store);
        EchoEntityState scavengerAfterAi = store.require(scavengerId);
        require(aiTick.movements() == 1, "hostile scavenger should pursue player");
        require(aiTick.attacks() == 0, "scavenger should not attack until adjacent before tick");
        require(scavengerAfterAi.worldPosition().equals(new EchoWorldPosition(2, 0, 0)),
                "scavenger should step toward player");
        require(scavengerAfterAi.ai().state() == EchoEntityAiState.PURSUING,
                "scavenger AI state should be pursuing");

        store.update(scavengerAfterAi.withHealth(scavengerAfterAi.health().damage(10)));
        require(store.require(scavengerId).health().currentHealth() == 25,
                "scavenger health should apply deterministic damage");
        require(store.require(scavengerId).alive(), "damaged scavenger should remain alive");

        Path fixtureRoot = Files.createTempDirectory("echo-runtime-entity-smoke");
        EchoSaveProfile saveProfile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-entities",
                "Ashfall Entities",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/ashfall-entities"),
                Map.of("phase", "14.10")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(services, saveProfile);
        EchoEntitySaveResult saved = entities.saveHook().save(saves, "slot-entities", "tx-entity-001");
        require(saved.commit().filesWritten() == 3, "entity save hook should write summary and two entities");
        require(saved.writtenPaths().contains("entities/summary.json"), "entity summary should be written");
        require(saved.writtenPaths().contains("entities/player-001.json"), "player entity should be written");
        require(saved.writtenPaths().contains("entities/scavenger-001.json"), "scavenger entity should be written");

        EchoSaveManifest manifest = saves.readManifest("slot-entities");
        require(manifest.file("entities/summary.json").isPresent(), "manifest should track entity summary");
        require(manifest.file("entities/player-001.json").isPresent(), "manifest should track player entity");
        require(manifest.file("entities/scavenger-001.json").isPresent(), "manifest should track scavenger entity");
        EchoSaveCorruptionReport saveCheck = saves.check("slot-entities");
        require(saveCheck.healthy(), "entity save should pass corruption check");

        System.out.println("phase14.10 entity runtime smoke PASS entities="
                + store.count()
                + " hostiles="
                + store.hostile().size()
                + " moved="
                + ((moved.moved() ? 1 : 0) + aiTick.movements())
                + " savedFiles="
                + saved.writtenPaths().size()
                + " damage=10");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
