package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoAshfallExperienceQaResult;
import dev.echo.standalone.runtime.app.EchoAshfallExperienceQaRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;

public final class EchoRuntimeAshfallExperienceQaSmokeHarness {
    private EchoRuntimeAshfallExperienceQaSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoAshfallExperienceQaResult result = new EchoAshfallExperienceQaRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                Files.createTempDirectory("echo-ashfall-experience-qa")
        );

        require(result.ready(), "Ashfall experience QA smoke should pass: " + result.summary());
        require(result.fullChapterComplete(), "full chapter should complete cleanly");
        require(result.midRunSaveLoadReady(), "mid-run save/load should restore mission, hotbar, terminal notes, and frame");
        require(result.deathRecoveryReady(), "death/recovery path should fail safely and restore a healthy checkpoint");
        require(result.inventoryManipulationReady(), "inventory framebuffer should remain visible and nonblank");
        require(result.inventoryUxReady(), "inventory UX should cover drag movement, stack splitting, hotbar assignment, tooltips, disabled states, and keyboard/mouse flow");
        require(result.visibleHudFeedbackReady(), "visible HUD should draw held item and block-break feedback in the framebuffer path");
        require(result.visibleActionParticlesReady(), "visible HUD should draw deterministic action particles for mining/break/place feedback");
        require(result.terminalBranchingReady(), "terminal branching should cover offline, damaged, low power, online, and extraction authorization");
        require(result.scavengeDepletionReady(), "scavenge depletion should prevent repeat loot and persist through save/restore");
        require(result.powerRepairFlowReady(), "power repair should discover, consume repair kit, reboot, and require terminal confirmation");
        require(result.extractionEventReady(), "extraction should arm, apply countdown pressure, and complete after the hold window");
        require(result.hazardVarietyReady(), "hazards should cover toxic ash, hot ash, unstable ground, electrical discharge, and extraction storm counters");
        require(result.shelterSystemReady(), "shelter should provide AdapterCore-profile rest recovery, integrity, storm damage, and save-backed state");
        require(result.survivalNeedsReady(), "survival needs should use AdapterCore-profile ration recovery, drain thresholds, damage pulses, and save-backed counters");
        require(result.waterLoopReady(), "water loop should build rain collector/purifier, collect dirty water, forage food, and stockpile clean water through AdapterCore");
        require(result.fieldPowerReady(), "field power should build a micro generator, route cable, and verify output through AdapterCore");
        require(result.machinePowerReady(), "machine power should build scrap dynamo, charge an energy cell, buffer battery, and stabilize burner through AdapterCore");
        require(result.midgameProgressionReady(), "midgame progression should equip gas mask, decode schematic, build factory/research route, and overclock through AdapterCore");
        require(result.expeditionSafetyReady(), "expedition safety should repair filters, build scrubber/cleanser/thermal/med bay blocks, and persist treatment state through AdapterCore");
        require(result.advancedExpeditionReady(), "advanced expedition should build grinder/refiner/relay/drone route and persist alloy state through AdapterCore");
        require(result.fieldRecoveryReady(), "field recovery should use rad away, stim pack, warmth gear, return beacon, and keystone through AdapterCore");
        require(result.canonicalRouteReady(), "canonical beta route should run from new game to extraction on the real playable path");
        require(result.failureRecoveryReady(), "failure and recovery coverage should include death/retry and mid-route save/load");
        require(result.hudObjectiveStateReady(), "Agent 3 should have route, objective, terminal, extraction, shelter, and hint state");
        require(result.routeWideGuidanceReady(), "Agent 3 should expose player-facing guidance across the full beta route");
        require(result.audioCueCoverageReady(), "audio cue coverage should include ambience, mining, break, pickup, consumables, terminal, power, extraction, and danger");
        require(result.corruptedSaveDetected(), "corrupted save should produce a checksum mismatch diagnostic");
        require(result.adapterCoreParityReady(), "QA should keep AdapterCore parity ready");

        System.out.println("phase15.experience qa smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
