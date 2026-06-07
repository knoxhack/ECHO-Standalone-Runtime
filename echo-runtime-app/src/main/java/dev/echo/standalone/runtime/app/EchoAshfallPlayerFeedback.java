package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record EchoAshfallPlayerFeedback(
        String currentObjective,
        String currentHint,
        String shelterStatus,
        String selectedHotbarItem,
        String toolDurability,
        String actionFeedback,
        List<String> warningStates
) {
    public EchoAshfallPlayerFeedback {
        currentObjective = EchoAppText.requireText(currentObjective, "currentObjective");
        currentHint = EchoAppText.requireText(currentHint, "currentHint");
        shelterStatus = EchoAppText.requireText(shelterStatus, "shelterStatus");
        selectedHotbarItem = EchoAppText.requireText(selectedHotbarItem, "selectedHotbarItem");
        toolDurability = EchoAppText.requireText(toolDurability, "toolDurability");
        actionFeedback = EchoAppText.requireText(actionFeedback, "actionFeedback");
        warningStates = List.copyOf(Objects.requireNonNull(warningStates, "warningStates"));
    }

    public static EchoAshfallPlayerFeedback from(
            EchoAshfallLiveMissionState mission,
            EchoVoxelPlayerHotbar hotbar,
            boolean targetAvailable,
            String actionFeedback
    ) {
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(hotbar, "hotbar");
        EchoVoxelHotbarSlot selected = hotbar.selected();
        ArrayList<String> warnings = new ArrayList<>();
        if (mission.playerHealth() <= 25) {
            warnings.add("health critical");
        }
        if (mission.hydration() <= 25.0D) {
            warnings.add("hydration low");
            warnings.add("low water");
        }
        if (mission.hunger() <= 20.0D) {
            warnings.add("hunger low");
            warnings.add("low food");
        }
        if (mission.ashExposure() >= 70.0D) {
            warnings.add("ash exposure high");
        }
        if (mission.crossedAsh() && mission.ashExposure() >= 40.0D) {
            warnings.add("ash exposure rising");
        }
        if (!mission.shelterBuilt()) {
            warnings.add("shelter missing");
            warnings.add("shelter unsafe");
        } else if (mission.shelterIntegrity() < 45.0D) {
            warnings.add("shelter unsafe");
        }
        if (!targetAvailable) {
            warnings.add("no target");
        }
        if (!mission.terminalOnline()) {
            warnings.add("terminal offline");
        }
        if (mission.extractionStatus().contains("POWER REQUIRED") || mission.terminalState().equals("LOW POWER")) {
            warnings.add("power required");
        }
        String normalizedAction = actionFeedback == null ? "" : actionFeedback.toLowerCase();
        if (normalizedAction.contains("blocked") || normalizedAction.contains("cannot")) {
            warnings.add("cannot place");
        }
        if (normalizedAction.contains("usable, not placeable")) {
            warnings.add("wrong tool");
        }
        if (normalizedAction.contains("inventory_full")) {
            warnings.add("inventory full");
        }
        if (normalizedAction.contains("consumed_one") || normalizedAction.contains("ration used")) {
            warnings.add("item consumed");
        }
        if (normalizedAction.contains("no rations left") || normalizedAction.contains("cache recovered")) {
            warnings.add("cache depleted");
        }
        if (mission.powerRepaired()) {
            warnings.add("power restored");
        }
        if (mission.powerRepaired() && !mission.extracted()) {
            warnings.add("extraction started");
        }
        return new EchoAshfallPlayerFeedback(
                mission.nextObjective(),
                mission.currentHint(),
                mission.shelterStatus(),
                selected.label() + " x" + selected.count(),
                durabilityLabel(selected),
                actionFeedback == null || actionFeedback.isBlank() ? mission.lastMessage() : actionFeedback.trim(),
                warnings
        );
    }

    public boolean coversPlayerHud() {
        return !currentObjective.isBlank()
                && !currentHint.isBlank()
                && !shelterStatus.isBlank()
                && !selectedHotbarItem.isBlank()
                && !toolDurability.isBlank()
                && !actionFeedback.isBlank();
    }

    private static String durabilityLabel(EchoVoxelHotbarSlot selected) {
        if (selected.empty()) {
            return "empty slot";
        }
        String id = selected.block().id();
        if (id.contains("scanner")) {
            return "battery stable";
        }
        if (id.contains("field_manual")) {
            return "single-use checklist";
        }
        if (id.contains("repair_kit") || id.contains("power_cell")) {
            return selected.count() + " repair charge" + (selected.count() == 1 ? "" : "s");
        }
        if (id.contains("water_ration")
                || id.contains("field_ration")
                || id.contains("clean_water_bottle")
                || id.contains("emergency_ration")) {
            return selected.count() + " single-use ration" + (selected.count() == 1 ? "" : "s");
        }
        if (id.contains("shelter_anchor") || id.contains("ash_campfire")) {
            return selected.count() + " anchor deploy" + (selected.count() == 1 ? "" : "s");
        }
        return selected.block().hardness() <= 0.0D
                ? "no durability"
                : "hardness " + String.format("%.1f", selected.block().hardness());
    }
}
