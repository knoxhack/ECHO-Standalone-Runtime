package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneEntityCombatResult;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneEntityCombatRuntime;
import dev.echo.standalone.runtime.gameplay.EchoStandaloneEntityCombatSmokeMain;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Map;

public final class EchoRuntimeAgent8EntityCombatParitySmokeHarness {
    private EchoRuntimeAgent8EntityCombatParitySmokeHarness() {
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
        Map<String, Object> reference = EchoStandaloneEntityCombatSmokeMain.referenceVector();

        require(standalone.adapterCoreBridge(), "entity combat standalone runtime must be AdapterCore-backed");
        require(standalone.parityVector().equals(reference),
                "entity combat standalone parity vector must match the Agent 8 native/reference vector: "
                        + standalone.parityVector());

        System.out.println("agent8 testkit entity combat parity smoke PASS " + standalone.parityVector());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
