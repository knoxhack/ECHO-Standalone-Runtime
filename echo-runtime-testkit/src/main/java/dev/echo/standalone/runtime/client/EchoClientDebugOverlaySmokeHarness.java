package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelHit;

public final class EchoClientDebugOverlaySmokeHarness {
    private EchoClientDebugOverlaySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        EchoClientFramePacingMonitor framePacing = new EchoClientFramePacingMonitor();
        framePacing.record(1.0D / 60.0D, 0.018D, 1, 0.001D, 0.0D);

        String text = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay,
                null,
                framePacing.snapshot()
        );
        require(text.contains("ECHO ASHFALL DEBUG"), "Debug overlay should identify the runtime");
        require(text.contains("FPS 60 STATE IN_GAME"), "Debug overlay should include FPS and state");
        require(text.contains("XYZ "), "Debug overlay should include player coordinates");
        require(text.contains("CHUNK "), "Debug overlay should include chunk coordinates");
        require(text.contains("LOADED " + session.world().loadedChunkCount()),
                "Debug overlay should include loaded chunk count");
        require(text.contains("CACHED " + session.cachedChunkCount()),
                "Debug overlay should include cached chunk count");
        require(text.contains("BIOME echoashfallprotocol:crash_zone_wasteland"),
                "Debug overlay should include current voxel biome");
        require(text.contains("ENV echo:ambience_ash_wasteland FOG"),
                "Debug overlay should include biome ambience and fog environment");
        require(text.contains("TARGET NONE"), "Debug overlay should handle missing target");
        require(text.contains("BREAK 0%"), "Debug overlay should include break progress");
        require(text.contains("MACHINE BE 7 GRAPH CONNECTED"),
                "Debug overlay should include machine block entity diagnostics");
        require(text.contains("FRAME MS 18.0 AVG 18.0 MAX 18.0 UPD 1 SLEEP 1.0 SLOW 0 STREAK 0"),
                "Debug overlay should include frame pacing counters");
        require(text.contains("RENDER CHUNK FULL 0 DIRTY 0 UP 0/0 PEND 0 MESH H 0 B 0 E 0 PROJ 0"),
                "Debug overlay should include renderer chunk upload diagnostics");
        require(text.contains("ATLAS REBUILD 0 REUSE 0 RES 0 TILE 0 DEC 0 DUP 0"),
                "Debug overlay should include atlas cache diagnostics");
        require(!text.contains(","), "Debug overlay text should avoid punctuation unsupported by the HUD font");

        EchoVoxelBlock block = new EchoVoxelBlock(
                "echoashfallprotocol:very_long_debug_target_block_identifier",
                "Debug Target",
                0xFFFFFFFF,
                true,
                true,
                1.0D
        );
        String target = EchoClientDebugOverlay.targetText(new EchoVoxelHit(1, 2, 3, 0, 1, 0, block, 2.75D));
        require(target.contains("TARGET echoashfallprotocol"),
                "Target debug line should include compacted block id");
        require(target.contains("AT 1 2 3"), "Target debug line should include block position");
        require(target.contains("D 2.8"), "Target debug line should include rounded distance");
        require(EchoClientDebugOverlay.breakText(0.424D).equals("BREAK 42%"),
                "Break progress should render as percent");
        require(EchoClientDebugOverlay.breakText(2.0D).equals("BREAK 100%"),
                "Break progress should clamp high values");

        System.out.println("client debug overlay smoke PASS lines=" + text.split("\\R").length);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
