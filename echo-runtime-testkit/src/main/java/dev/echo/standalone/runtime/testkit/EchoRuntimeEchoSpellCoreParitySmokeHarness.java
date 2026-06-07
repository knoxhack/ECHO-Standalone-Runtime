package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoSpellCoreStandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoSpellCoreParitySmokeHarness {
    private EchoRuntimeEchoSpellCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoSpellCoreStandaloneAdapter standaloneAdapter = new EchoSpellCoreStandaloneAdapter();
        Map<String, Object> nativeState = standaloneAdapter.executeCastResolution("echo-native-m17");
        Map<String, Object> standaloneState = standaloneAdapter.executeCastResolution("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(standaloneAdapter.referenceCastPassed(nativeState),
                "native SpellCore reference cast should pass");
        require(standaloneAdapter.referenceCastPassed(standaloneState),
                "standalone SpellCore cast should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("spellCastResolved")),
                "standalone activation should execute spell cast resolution");
        require(nativeState.get("adapterCoreContract").equals(standaloneState.get("adapterCoreContract")),
                "native and standalone spell contracts should match");
        require(nativeState.get("playerId").equals(standaloneState.get("playerId")),
                "native and standalone player ids should match");
        require(nativeState.get("focusItem").equals(standaloneState.get("focusItem")),
                "native and standalone focus items should match");
        require(nativeState.get("deckState").equals(standaloneState.get("deckState")),
                "native and standalone deck state should match");
        require(nativeState.get("castProfile").equals(standaloneState.get("castProfile")),
                "native and standalone cast profiles should match");
        require(nativeState.get("aetherLedger").equals(standaloneState.get("aetherLedger")),
                "native and standalone aether ledgers should match");
        require(nativeState.get("cooldownState").equals(standaloneState.get("cooldownState")),
                "native and standalone cooldown states should match");
        require(nativeState.get("projectileIntent").equals(standaloneState.get("projectileIntent")),
                "native and standalone projectile intents should match");
        require(nativeState.get("events").equals(standaloneState.get("events")),
                "native and standalone events should match");
        require(nativeState.get("diagnostics").equals(standaloneState.get("diagnostics")),
                "native and standalone diagnostics should match");

        System.out.println("echospellcore parity smoke PASS contract="
                + nativeState.get("adapterCoreContract")
                + " spell="
                + EchoSpellCoreStandaloneAdapter.REFERENCE_SPELL_ID
                + " cost="
                + ((Map<?, ?>) nativeState.get("aetherLedger")).get("cost")
                + " cooldown="
                + ((Map<?, ?>) nativeState.get("cooldownState")).get("remainingTicks")
                + " projectileEffects="
                + ((List<?>) ((Map<?, ?>) nativeState.get("projectileIntent")).get("hitEffects")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
