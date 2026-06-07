package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;

public final class EchoClientFootstepAudioSmokeHarness {
    private EchoClientFootstepAudioSmokeHarness() {
    }

    public static void main(String[] args) {
        requireGroundedMovementFootsteps();
        System.out.println("client footstep audio smoke PASS cue=echo:client_step throttle=stride");
    }

    private static void requireGroundedMovementFootsteps() {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(backend);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        services.setAudio(audio);
        services.startNewWorld("footstep-audio-smoke");
        EchoClientGameSession session = services.session();
        require(session != null, "Footstep audio smoke should start an active client game session");

        EchoClientGameplay gameplay = services.gameplay();
        EchoVoxelPlayerState start = gameplay.player().state();
        EchoClientGameplayInput noTriggers = new NoTriggerGameplayInput();
        for (int i = 0; i < 5; i++) {
            gameplay.tick(EchoVoxelPlayerInput.idle(), noTriggers, 0.05D, session);
        }
        require(backend.events().isEmpty(),
                "Idle grounded gameplay ticks should not emit footstep audio");

        EchoVoxelPlayerInput forward = new EchoVoxelPlayerInput(
                true,
                false,
                false,
                false,
                false,
                false,
                false,
                0.0D,
                0.0D
        );
        for (int i = 0; i < 24; i++) {
            gameplay.tick(forward, noTriggers, 0.05D, session);
            services.updateWorldSessionFromGameplay();
        }

        EchoVoxelPlayerState end = gameplay.player().state();
        require(Math.hypot(end.x() - start.x(), end.z() - start.z()) > 1.5D,
                "Footstep audio smoke should move the player far enough to cross at least one stride");
        long stepEvents = backend.events().stream()
                .filter(event -> event.clip().clipId().equals("echo:client_step"))
                .count();
        require(stepEvents >= 1,
                "Grounded player movement should emit client footstep audio");
        require(stepEvents <= 6,
                "Footstep audio should be throttled by stride distance instead of playing every tick");
        require(backend.events().stream().allMatch(event ->
                        event.action() == EchoAudioPlaybackAction.PLAY
                                && event.bus() == EchoAudioBus.SFX
                                && event.effectiveGain() > 0.0D
                                && event.clip().clipId().equals("echo:client_step")),
                "Footstep audio events should be audible SFX one-shots");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class NoTriggerGameplayInput implements EchoClientGameplayInput {
        @Override
        public int selectedHotbarSlot(int current) {
            return current;
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
            return false;
        }
    }
}
