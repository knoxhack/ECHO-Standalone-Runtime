package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackEvent;
import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

public final class EchoClientPickupAudioSmokeHarness {
    private EchoClientPickupAudioSmokeHarness() {
    }

    public static void main(String[] args) {
        requireDelayedPickupAudio();
        System.out.println("client pickup audio smoke PASS pickup=delayed cue=echo:client_pickup");
    }

    private static void requireDelayedPickupAudio() {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(backend);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        services.setAudio(audio);
        services.startNewWorld("pickup-audio-smoke");
        EchoClientGameSession session = services.session();
        require(session != null, "Pickup audio smoke should start an active session");

        EchoVoxelBlock block = session.bridge().runtimeMarkerBlock();
        int beforeCount = countItem(session, block.id());
        EchoClientDroppedItem drop = session.dropBlockItem(block);
        require(drop != null, "Pickup audio smoke should create a nearby dropped block item");

        EchoClientDroppedItemRuntime.PickupResult delayed = services.pickupNearbyDroppedItems(1.0D);
        require(delayed.pickedQuantity() == 0 && delayed.remainingDrops() == 1,
                "Live pickup should honor the configured minimum item age");
        require(backend.events().isEmpty(),
                "Delayed pickup should not submit an item pickup sound event");

        session.tickEntities(1.0D);
        EchoClientDroppedItemRuntime.PickupResult pickup = services.pickupNearbyDroppedItems(0.55D);
        require(pickup.pickedQuantity() == 1 && pickup.remainingDrops() == 0,
                "Aged nearby dropped item should be collected by the live pickup path");
        require(countItem(session, block.id()) == beforeCount + 1,
                "Aged pickup should move the dropped item into inventory");
        require(backend.events().size() == 1,
                "Successful pickup should submit exactly one pickup sound event");

        EchoAudioPlaybackEvent event = backend.events().getFirst();
        require(event.action() == EchoAudioPlaybackAction.PLAY,
                "Pickup audio should be a one-shot play event");
        require(event.clip().clipId().equals("echo:client_pickup"),
                "Pickup audio should use the client pickup clip");
        require(event.bus() == EchoAudioBus.SFX && event.effectiveGain() > 0.0D,
                "Pickup audio should use the SFX bus with audible gain");
    }

    private static int countItem(EchoClientGameSession session, String itemId) {
        return session.inventoryScreenModel().slots().stream()
                .filter(slot -> slot.runtimeId().equals(itemId))
                .mapToInt(EchoClientSlotStack::count)
                .sum();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
