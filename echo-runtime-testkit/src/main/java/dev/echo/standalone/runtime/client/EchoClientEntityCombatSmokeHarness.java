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
import dev.echo.standalone.runtime.world.EchoWorldPosition;

public final class EchoClientEntityCombatSmokeHarness {
    private static final String TEST_ENTITY_ID = "test:combat_target";
    private static final String TEST_DEFINITION_ID = "echoashfallprotocol:rad_zombie";
    private static final String SCRAP_METAL_ID = "echoashfallprotocol:scrap_metal";

    private EchoClientEntityCombatSmokeHarness() {
    }

    public static void main(String[] args) {
        requireDirectSessionCombat();
        requireGameplayLeftClickCombat();
        System.out.println("client entity combat smoke PASS killed=2 loot=scrap xp=awarded");
    }

    private static void requireDirectSessionCombat() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-combat-direct").gameSession();
        equipStarterTool(session);
        orientForCombat(session);
        EchoWorldPosition targetPosition = combatTargetPosition(session);
        session.entityStore().register(hostile(TEST_ENTITY_ID, targetPosition));

        int beforeXp = session.progressionState().experience();
        int beforeDrops = session.droppedItemQuantity();
        int beforeDurability = session.selectedToolStatus(session.bridge().runtimeMarkerBlock()).durability();

        EchoClientEntityAttackResult attack = session.attackLookedAtEntity(null);

        require(attack.hit() && attack.killed(),
                "Direct session attack should hit and kill the looked-at hostile");
        require(attack.damage() >= 6 && attack.healthBefore() == 6 && attack.healthAfter() == 0,
                "Tool-backed entity attack should apply lethal combat damage");
        require(session.entityStore().find(new EchoEntityId(TEST_ENTITY_ID)).isEmpty(),
                "Killed hostile should be removed from the live entity store");
        require(session.droppedItemQuantity() == beforeDrops + 2
                        && session.droppedItemSnapshots().stream()
                                .anyMatch(drop -> drop.itemId().equals(SCRAP_METAL_ID) && drop.quantity() == 2),
                "Killed hostile should create save-backed dropped scrap metal");
        require(session.progressionState().experience() == beforeXp + 10
                        && session.progressionState().milestones().contains("kill:" + TEST_DEFINITION_ID),
                "Killed hostile should award combat XP and a kill milestone");
        require(session.selectedToolStatus(session.bridge().runtimeMarkerBlock()).durability() == beforeDurability - 1,
                "Entity attack should wear the selected tool without breaking block mining state");
    }

    private static void requireGameplayLeftClickCombat() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-combat-gameplay").gameSession();
        equipStarterTool(session);
        orientForCombat(session);
        EchoWorldPosition targetPosition = combatTargetPosition(session);
        session.entityStore().register(hostile(TEST_ENTITY_ID, targetPosition));
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());

        int beforeXp = session.progressionState().experience();
        int beforeDrops = session.droppedItemQuantity();
        gameplay.tick(EchoVoxelPlayerInput.idle(), new BreakOnceInput(1), 1.0D / 60.0D, session);

        require(session.entityStore().find(new EchoEntityId(TEST_ENTITY_ID)).isEmpty(),
                "Gameplay left-click should route through entity combat before block breaking");
        require(session.progressionState().experience() == beforeXp + 10,
                "Gameplay left-click combat should award hostile kill XP");
        require(session.droppedItemQuantity() == beforeDrops + 2,
                "Gameplay left-click combat should create hostile death loot");
        require(gameplay.breakProgress() == 0.0D && !gameplay.isWorldDirty(),
                "Entity combat click should not leave block breaking progress or mark the voxel world dirty");
    }

    private static void equipStarterTool(EchoClientGameSession session) {
        require(session.quickMoveContainerSlotToPlayer(4).success(),
                "Entity combat smoke should move starter salvage pick into player inventory");
        session.hotbar().select(1);
        session.player().selectSlot(1);
        require(session.selectedToolStatus(session.bridge().runtimeMarkerBlock()).activeTool(),
                "Entity combat smoke should select a live starter tool");
    }

    private static void orientForCombat(EchoClientGameSession session) {
        EchoVoxelPlayerState state = session.player().state();
        session.player().tick(
                session.world(),
                EchoVoxelPlayerInput.look(-state.yawDegrees(), -12.0D - state.pitchDegrees()),
                0.0D
        );
    }

    private static EchoWorldPosition combatTargetPosition(EchoClientGameSession session) {
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        for (int distance = 2; distance <= 4; distance++) {
            int z = (int) Math.floor(player.z() + distance);
            int y = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
            if (y >= 0 && session.world().blockStateAt(x, y, z).air()) {
                return new EchoWorldPosition(x, y, z);
            }
        }
        throw new AssertionError("Could not find deterministic combat target position");
    }

    private static EchoEntityState hostile(String entityId, EchoWorldPosition position) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                TEST_DEFINITION_ID,
                "Rad Zombie",
                EchoEntityKind.HOSTILE,
                6,
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

    private static final class BreakOnceInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean breakPressed = true;

        private BreakOnceInput(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        public int selectedHotbarSlot(int current) {
            return selectedSlot;
        }

        @Override
        public boolean consumeBreak() {
            boolean value = breakPressed;
            breakPressed = false;
            return value;
        }

        @Override
        public boolean isCursorLocked() {
            return true;
        }

        @Override
        public boolean consumePlace() {
            return false;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
