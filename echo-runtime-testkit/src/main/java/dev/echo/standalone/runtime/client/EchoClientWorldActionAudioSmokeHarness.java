package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackEvent;
import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;

import java.util.List;

public final class EchoClientWorldActionAudioSmokeHarness {
    private EchoClientWorldActionAudioSmokeHarness() {
    }

    public static void main(String[] args) {
        requireBlockHitAndBreakAudio();
        requireBlockPlaceAudio();
        System.out.println("client world action audio smoke PASS hit=block break=block place=block");
    }

    private static void requireBlockHitAndBreakAudio() {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientRuntimeServices services = servicesWithAudio(backend);
        services.startNewWorld("world-action-audio-break");
        EchoClientGameSession session = services.session();
        EchoClientGameplay gameplay = services.gameplay();
        EchoVoxelBlock targetBlock = session.bridge().runtimeMarkerBlock();
        require(placeBreakTargetOnLookRay(session, targetBlock),
                "World action audio smoke should place a break target on the player's look ray");

        BreakOnceInput input = new BreakOnceInput(session.hotbar().selectedSlot());
        gameplay.tick(EchoVoxelPlayerInput.idle(), input, 0.02D, session);
        require(count(backend.events(), "echo:client_block_hit") == 1,
                "Initial mining click should emit one block-hit audio cue before the block breaks");
        require(count(backend.events(), "echo:client_break") == 0,
                "Initial mining click should not emit the final block-break cue");

        for (int i = 0; i < 80 && count(backend.events(), "echo:client_break") == 0; i++) {
            gameplay.tick(EchoVoxelPlayerInput.idle(), input, 0.05D, session);
            services.updateWorldSessionFromGameplay();
        }
        require(count(backend.events(), "echo:client_break") == 1,
                "Finishing a mined block should emit one block-break audio cue");
        require(backend.events().stream()
                        .filter(event -> event.clip().clipId().equals("echo:client_block_hit")
                                || event.clip().clipId().equals("echo:client_break"))
                        .allMatch(EchoClientWorldActionAudioSmokeHarness::audibleSfxPlay),
                "Block-hit and block-break audio should be audible SFX play events");
    }

    private static void requireBlockPlaceAudio() {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientRuntimeServices services = servicesWithAudio(backend);
        services.startNewWorld("world-action-audio-place");
        EchoClientGameSession session = services.session();
        EchoClientGameplay gameplay = services.gameplay();
        EchoVoxelBlock anchorBlock = session.bridge().runtimeMarkerBlock();
        require(placeBreakTargetOnLookRay(session, anchorBlock),
                "World action audio smoke should place an anchor target on the player's look ray");

        EchoVoxelBlock placeBlock = session.bridge().shelterAnchorBlock();
        int selectedSlot = session.hotbar().selectedSlot();
        session.hotbar().assignSlot(selectedSlot, placeBlock, 1);
        session.player().selectSlot(selectedSlot);
        gameplay.tick(EchoVoxelPlayerInput.idle(), new PlaceOnceInput(selectedSlot), 0.02D, session);

        require(count(backend.events(), "echo:client_place") == 1,
                "Right-click block placement should emit one block-place audio cue");
        require(backend.events().stream()
                        .filter(event -> event.clip().clipId().equals("echo:client_place"))
                        .allMatch(EchoClientWorldActionAudioSmokeHarness::audibleSfxPlay),
                "Block-place audio should be an audible SFX play event");
    }

    private static EchoClientRuntimeServices servicesWithAudio(EchoRecordingAudioBackend backend) {
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(backend);
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        services.setAudio(audio);
        return services;
    }

    private static boolean placeBreakTargetOnLookRay(
            EchoClientGameSession session,
            EchoVoxelBlock targetBlock
    ) {
        EchoVoxelPlayerState state = session.player().state();
        double yawRadians = Math.toRadians(state.yawDegrees());
        double pitchRadians = Math.toRadians(state.pitchDegrees());
        double horizontal = Math.cos(pitchRadians);
        double directionX = Math.sin(yawRadians) * horizontal;
        double directionY = Math.sin(pitchRadians);
        double directionZ = Math.cos(yawRadians) * horizontal;
        for (double distance = 1.0D; distance <= state.reach() - 0.25D; distance += 0.25D) {
            int x = (int) Math.floor(state.x() + directionX * distance);
            int y = (int) Math.floor(state.eyeY() + directionY * distance);
            int z = (int) Math.floor(state.z() + directionZ * distance);
            if (state.intersectsBlock(x, y, z)) {
                continue;
            }
            if (session.world().setBlockStateAt(x, y, z, EchoVoxelBlockState.of(targetBlock))) {
                return true;
            }
        }
        return false;
    }

    private static long count(List<EchoAudioPlaybackEvent> events, String clipId) {
        return events.stream()
                .filter(event -> event.clip().clipId().equals(clipId))
                .count();
    }

    private static boolean audibleSfxPlay(EchoAudioPlaybackEvent event) {
        return event.action() == EchoAudioPlaybackAction.PLAY
                && event.bus() == EchoAudioBus.SFX
                && event.effectiveGain() > 0.0D;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class BreakOnceInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean pressed = true;

        private BreakOnceInput(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        public int selectedHotbarSlot(int current) {
            return selectedSlot;
        }

        @Override
        public boolean consumeBreak() {
            boolean value = pressed;
            pressed = false;
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

    private static final class PlaceOnceInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean pressed = true;

        private PlaceOnceInput(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        public int selectedHotbarSlot(int current) {
            return selectedSlot;
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
            boolean value = pressed;
            pressed = false;
            return value;
        }
    }
}
