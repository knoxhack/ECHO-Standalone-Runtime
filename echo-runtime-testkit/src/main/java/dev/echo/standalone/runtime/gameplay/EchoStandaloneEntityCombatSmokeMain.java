package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoStandaloneEntityCombatSmokeMain {
    private EchoStandaloneEntityCombatSmokeMain() {
    }

    public static void main(String[] args) {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite());
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items);
        EchoStandaloneEntityCombatResult standalone = new EchoStandaloneEntityCombatRuntime().run(
                entities,
                items,
                gameplay);
        Map<String, Object> reference = referenceVector();
        require(standalone.adapterCoreBridge(), "standalone entity combat must be AdapterCore-backed");
        require(standalone.parityVector().equals(reference),
                "standalone entity combat vector must match Agent 8 reference/native vector: "
                        + standalone.parityVector());
        System.out.println("agent8 standalone entity combat parity smoke PASS " + standalone.parityVector());
    }

    public static Map<String, Object> referenceVector() {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("hostileSpawns", true);
        reference.put("playerCanAttack", true);
        reference.put("entityCanAttack", true);
        reference.put("playerHealthAfterAttack", 95);
        reference.put("hostileHealthAfterPlayerAttack", 0);
        reference.put("deathWorks", true);
        reference.put("recoveryWorks", true);
        reference.put("lootGranted", true);
        reference.put("missionObjectiveAdvanced", true);
        reference.put("npcInteractionOpened", true);
        reference.put("encounterStarted", true);
        reference.put("encounterEnded", true);
        reference.put("familiarBehaviorExecuted", true);
        reference.put("playerStatsUpdated", true);
        reference.put("playerExperienceAfterCombat", 30);
        return reference;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
