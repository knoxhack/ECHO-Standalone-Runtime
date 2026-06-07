package dev.echo.standalone.runtime.audio;

import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoAudioCuePlanner {
    public static final String CUE_MINING_HIT = "mining.hit";
    public static final String CUE_BLOCK_BREAK = "block.break";
    public static final String CUE_ITEM_PICKUP = "item.pickup";
    public static final String CUE_CONSUME_WATER = "consume.water";
    public static final String CUE_CONSUME_FOOD = "consume.food";
    public static final String CUE_TERMINAL_BEEP = "terminal.beep";
    public static final String CUE_POWER_REPAIR = "power.repair";
    public static final String CUE_EXTRACTION_BEACON = "extraction.beacon";
    public static final String CUE_DANGER_ALERT = "danger.alert";

    private final EchoAudioClipRegistry clips;

    public EchoAudioCuePlanner(EchoAudioClipRegistry clips) {
        this.clips = Objects.requireNonNull(clips, "clips");
    }

    public EchoAudioCuePlan initialAshfallLoop(
            EchoWorldRuntimeResult world,
            EchoGameplayRuntimeResult gameplay,
            String screenId
    ) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(gameplay, "gameplay");
        String resolvedScreenId = EchoAudioText.requireText(screenId, "screenId");
        ArrayList<EchoAudioPlaybackRequest> requests = new ArrayList<>();
        requests.add(new EchoAudioPlaybackRequest(
                "audio-request-ambience",
                EchoAudioPlaybackAction.LOOP,
                clips.require("ashfall:ambience_ash_storm"),
                "weather=" + world.world().chunks().getFirst().weather().profileId(),
                world.world().tick()
        ));
        requests.add(new EchoAudioPlaybackRequest(
                "audio-request-music",
                EchoAudioPlaybackAction.LOOP,
                clips.require("ashfall:music_survival_pulse"),
                "mission=" + gameplay.mission().missionId(),
                world.world().tick()
        ));
        requests.add(new EchoAudioPlaybackRequest(
                "audio-request-ui",
                EchoAudioPlaybackAction.PLAY,
                clips.require("echo:ui_terminal_blip"),
                "screen=" + resolvedScreenId,
                world.world().tick()
        ));
        if (gameplay.mission().status() == EchoGameplayMissionStatus.ACTIVE) {
            requests.add(new EchoAudioPlaybackRequest(
                    "audio-request-stinger",
                    EchoAudioPlaybackAction.PLAY,
                    clips.require("ashfall:mission_secure_stinger"),
                    "mission-status=" + gameplay.mission().status().name(),
                    world.world().tick()
            ));
        }
        return new EchoAudioCuePlan(requests);
    }

    public EchoAudioCuePlan ashfallGameplayCues(List<String> cueKeys, long tick) {
        Objects.requireNonNull(cueKeys, "cueKeys");
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative");
        }
        ArrayList<EchoAudioPlaybackRequest> requests = new ArrayList<>();
        for (String cueKey : cueKeys) {
            requests.add(requestFor(cueKey, requests.size() + 1, tick));
        }
        return new EchoAudioCuePlan(requests);
    }

    public static List<String> requiredAshfallGameplayCueKeys() {
        return List.of(
                CUE_MINING_HIT,
                CUE_BLOCK_BREAK,
                CUE_ITEM_PICKUP,
                CUE_CONSUME_WATER,
                CUE_CONSUME_FOOD,
                CUE_TERMINAL_BEEP,
                CUE_POWER_REPAIR,
                CUE_EXTRACTION_BEACON,
                CUE_DANGER_ALERT
        );
    }

    private EchoAudioPlaybackRequest requestFor(String cueKey, int sequence, long tick) {
        String key = EchoAudioText.requireText(cueKey, "cueKey");
        return switch (key) {
            case CUE_MINING_HIT -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:block_mining_hit", key, "block=" + "echoashfallprotocol:scorched_ash", tick);
            case CUE_BLOCK_BREAK -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:block_break", key, "block=" + "echoashfallprotocol:rusted_metal_debris", tick);
            case CUE_ITEM_PICKUP -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:item_pickup", key, "item=" + "echoashfallprotocol:clean_water_bottle", tick);
            case CUE_CONSUME_WATER -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:consume_water", key, "item=" + "echoashfallprotocol:clean_water_bottle", tick);
            case CUE_CONSUME_FOOD -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:consume_food", key, "item=" + "echoashfallprotocol:emergency_ration", tick);
            case CUE_TERMINAL_BEEP -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:radio_static", key, "adaptercore-sound=" + "echoashfallprotocol:sound/ui.echo_message", tick);
            case CUE_POWER_REPAIR -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:power_repair", key, "mission=power-repaired", tick);
            case CUE_EXTRACTION_BEACON -> gameplayRequest(sequence, EchoAudioPlaybackAction.LOOP,
                    "ashfall:extraction_beacon", key, "mission=extraction-armed", tick);
            case CUE_DANGER_ALERT -> gameplayRequest(sequence, EchoAudioPlaybackAction.PLAY,
                    "ashfall:danger_alert", key, "hazard=ash-exposure", tick);
            default -> throw new IllegalArgumentException("Unknown Ashfall gameplay cue key: " + key);
        };
    }

    private EchoAudioPlaybackRequest gameplayRequest(
            int sequence,
            EchoAudioPlaybackAction action,
            String clipId,
            String cueKey,
            String state,
            long tick
    ) {
        return new EchoAudioPlaybackRequest(
                String.format(java.util.Locale.ROOT, "ashfall-gameplay-cue-%02d", sequence),
                action,
                clips.require(clipId),
                "cue=" + cueKey + " " + state,
                tick
        );
    }
}
