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
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

public final class EchoClientEntityInteractionSmokeHarness {
    private static final String NPC_ENTITY_ID = "test:crash_survivor";
    private static final String NPC_DEFINITION_ID = "echoashfallprotocol:crash_survivor";
    private static final String HOSTILE_ENTITY_ID = "test:rad_zombie";
    private static final String ENTITY_INTERACTION_SCREEN = "echoscreencore:entity_interaction";

    private EchoClientEntityInteractionSmokeHarness() {
    }

    public static void main(String[] args) {
        requireDirectNpcInteraction();
        requireGameplayRightClickInteraction();
        requireHostileNotInteractable();
        requireRegisteredEntityScreenRoute();
        System.out.println("client entity interaction smoke PASS npc=screen_route hostile=ignored");
    }

    private static void requireDirectNpcInteraction() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-interaction-direct").gameSession();
        EchoWorldPosition position = interactionTargetPosition(session);
        orientForInteraction(session, position);
        session.entityStore().register(entity(NPC_ENTITY_ID, NPC_DEFINITION_ID, "Crash Survivor", EchoEntityKind.NPC, position));

        EchoClientEntityInteractionResult interaction = session.interactLookedAtEntity(null);

        require(interaction.hit(), "Direct session interaction should hit the looked-at NPC");
        require(interaction.kind() == EchoEntityKind.NPC,
                "Direct session interaction should report the NPC kind");
        require(interaction.definitionId().equals(NPC_DEFINITION_ID),
                "Direct session interaction should preserve the canonical entity definition id");
        require(interaction.route().command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN
                        && interaction.route().targetId().equals(ENTITY_INTERACTION_SCREEN),
                "Direct session interaction should route through the registered entity interaction screen");
        EchoEntityState after = session.entityStore().require(new EchoEntityId(NPC_ENTITY_ID));
        require(after.health().currentHealth() == after.health().maxHealth(),
                "Interacting with an NPC should not damage or remove it");
    }

    private static void requireGameplayRightClickInteraction() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-interaction-gameplay").gameSession();
        selectEmptyHand(session, 7);
        EchoWorldPosition position = interactionTargetPosition(session);
        orientForInteraction(session, position);
        session.entityStore().register(entity(
                NPC_ENTITY_ID,
                NPC_DEFINITION_ID,
                "Crash Survivor",
                EchoEntityKind.NPC,
                position
        ));
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());

        gameplay.tick(EchoVoxelPlayerInput.idle(), new PlaceOnceInput(7), 1.0D / 60.0D, session);

        EchoClientScreenRouteRequest route = gameplay.consumePendingScreenRoute();
        require(route.command() == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN
                        && route.targetId().equals(ENTITY_INTERACTION_SCREEN),
                "Gameplay right-click should route NPC interaction before block interaction or placement");
        EchoClientSelectedItemUse selectedItemUse = gameplay.consumeSelectedItemUse();
        require(selectedItemUse.active()
                        && selectedItemUse.action().equals("interact")
                        && selectedItemUse.toastText().equals("Interacted with Crash Survivor"),
                "Gameplay right-click NPC interaction should emit a friendly interaction toast");
        require(!gameplay.isWorldDirty(),
                "NPC interaction should not mark the voxel block world dirty");
        require(session.entityStore().find(new EchoEntityId(NPC_ENTITY_ID)).isPresent(),
                "Gameplay NPC interaction should leave the entity in the live store");
    }

    private static void requireHostileNotInteractable() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-interaction-hostile").gameSession();
        EchoWorldPosition position = interactionTargetPosition(session);
        orientForInteraction(session, position);
        session.entityStore().register(entity(
                HOSTILE_ENTITY_ID,
                "echoashfallprotocol:rad_zombie",
                "Rad Zombie",
                EchoEntityKind.HOSTILE,
                position
        ));

        EchoClientEntityInteractionResult interaction = session.interactLookedAtEntity(null);

        require(!interaction.hit() && !interaction.route().active(),
                "Hostile entities should stay on combat input instead of friendly interaction routing");
        require(session.entityStore().find(new EchoEntityId(HOSTILE_ENTITY_ID)).isPresent(),
                "Failed hostile interaction should not damage or remove the hostile");
    }

    private static void requireRegisteredEntityScreenRoute() {
        EchoClientScreenController screens = new EchoClientScreenController();
        screens.updateScreenCatalog(EchoClientScreenCatalog.loadDefault());
        require(screens.openRegisteredScreen(ENTITY_INTERACTION_SCREEN, true),
                "ScreenCore controller should open the built-in entity interaction route");
        EchoClientScreenSnapshot snapshot = screens.snapshot(true);
        require(snapshot.kind() == EchoClientScreenKind.REGISTERED_SCREEN,
                "Entity interaction route should land on the registered screen kind");
        require(snapshot.title().equals("Entity Interaction"),
                "Registered entity interaction screen should expose a dedicated title");
        require(snapshot.options().stream()
                        .anyMatch(option -> option.label().equals("Route: screencore.entity_interaction")),
                "Registered entity interaction screen should expose the ScreenCore route row");
    }

    private static void selectEmptyHand(EchoClientGameSession session, int slot) {
        session.hotbar().assignSlot(slot, EchoVoxelBlock.AIR, 0);
        session.hotbar().select(slot);
        session.player().selectSlot(slot);
    }

    private static void orientForInteraction(EchoClientGameSession session, EchoWorldPosition targetPosition) {
        EchoVoxelPlayerState state = session.player().state();
        double dx = targetPosition.x() + 0.5D - state.x();
        double dy = targetPosition.y() + 0.6D - state.eyeY();
        double dz = targetPosition.z() + 0.5D - state.z();
        double horizontalDistance = Math.hypot(dx, dz);
        double targetYaw = Math.toDegrees(Math.atan2(dx, dz));
        double targetPitch = Math.toDegrees(Math.atan2(dy, horizontalDistance));
        session.player().tick(
                session.world(),
                EchoVoxelPlayerInput.look(targetYaw - state.yawDegrees(), targetPitch - state.pitchDegrees()),
                0.0D
        );
    }

    private static EchoWorldPosition interactionTargetPosition(EchoClientGameSession session) {
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        int standingY = (int) Math.floor(player.y());
        for (int distance = 2; distance <= 4; distance++) {
            int z = (int) Math.floor(player.z() + distance);
            if (session.world().blockStateAt(x, standingY, z).air()
                    && session.world().blockStateAt(x, standingY + 1, z).air()) {
                return new EchoWorldPosition(x, standingY, z);
            }
            int y = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
            if (y >= 0 && session.world().blockStateAt(x, y, z).air()) {
                return new EchoWorldPosition(x, y, z);
            }
        }
        throw new AssertionError("Could not find deterministic entity interaction target position");
    }

    private static EchoEntityState entity(
            String entityId,
            String definitionId,
            String displayName,
            EchoEntityKind kind,
            EchoWorldPosition position
    ) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                definitionId,
                displayName,
                kind,
                20,
                kind == EchoEntityKind.HOSTILE ? 1 : 0,
                kind == EchoEntityKind.HOSTILE ? "hostile_scavenger" : "idle"
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

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class PlaceOnceInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean placePressed = true;

        private PlaceOnceInput(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        public int selectedHotbarSlot(int current) {
            return selectedSlot;
        }

        @Override
        public boolean consumeBreak() {
            return false;
        }

        @Override
        public boolean isCursorLocked() {
            return true;
        }

        @Override
        public boolean consumePlace() {
            boolean value = placePressed;
            placePressed = false;
            return value;
        }
    }
}
