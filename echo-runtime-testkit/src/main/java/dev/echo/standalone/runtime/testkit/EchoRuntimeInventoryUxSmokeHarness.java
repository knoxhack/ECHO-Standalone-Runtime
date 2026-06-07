package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallInventoryUxResult;
import dev.echo.standalone.runtime.app.EchoAshfallInventoryUxRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

public final class EchoRuntimeInventoryUxSmokeHarness {
    private EchoRuntimeInventoryUxSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAshfallInventoryUxResult result = new EchoAshfallInventoryUxRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive()
        );

        require(result.ready(), "inventory UX smoke should pass: " + result.summary());
        require(result.adapterCoreBacked(), "inventory state should use AdapterCore-backed Ashfall content");
        require(result.dragMovementReady(), "inventory drag movement should move a stack between hotbar slots");
        require(result.stackSplitReady(), "inventory stack splitting should split a stack into an empty slot");
        require(result.hotbarAssignmentReady(), "inventory hotbar assignment should select an assigned slot");
        require(result.tooltipReady(), "inventory tooltips should include name, id, stack, usage, and AdapterCore source");
        require(result.disabledStatesReady(), "inventory disabled states should cover too-small split, empty source, and full inventory");
        require(result.consumeUseFeedbackReady(), "inventory use flow should surface consume/use feedback in player UX");
        require(result.keyboardMouseFlowReady(), "inventory keyboard/mouse shell flow should release mouse and return to play");

        System.out.println("phase15.inventory ux smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
