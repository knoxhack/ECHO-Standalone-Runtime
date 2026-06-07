package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.StandaloneEchoRuntimeHost;
import dev.echo.standalone.runtime.app.StandaloneRuntimeHostFactory;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class EchoStandaloneRuntimeHostActionSmokeHarness {
    private EchoStandaloneRuntimeHostActionSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path saveRoot = Files.createTempDirectory("echo-standalone-runtime-host-smoke-");
        StandaloneEchoRuntimeHost host = StandaloneRuntimeHostFactory.ashfallLive().create(saveRoot);

        StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult waterUse = host.applyAdapterCoreAction(
                "adaptercore.gameplay_handler.item_use",
                "item_use",
                Map.of(
                        "canonicalId", EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID,
                        "itemId", EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID,
                        "source", "player_action"
                )
        );
        requireMutation(waterUse, "water item_use");
        require(Boolean.TRUE.equals(waterUse.hostSnapshot().get("waterUsed")), "water item_use did not update mission");

        StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult shelterPlace = host.applyAdapterCoreAction(
                "adaptercore.gameplay_handler.block_place",
                "block_place",
                Map.of(
                        "canonicalId", EchoAdapterCoreStandaloneContentBridge.SHELTER_ANCHOR_BLOCK_ID,
                        "blockId", EchoAdapterCoreStandaloneContentBridge.SHELTER_ANCHOR_BLOCK_ID,
                        "x", 2,
                        "y", 5,
                        "z", 2,
                        "source", "player_action"
                )
        );
        requireMutation(shelterPlace, "shelter block_place");
        require(Boolean.TRUE.equals(shelterPlace.hostSnapshot().get("shelterBuilt")), "block_place did not update mission");
        require(
                host.context().world().blockAt(2, 5, 2).id()
                        .equals(EchoAdapterCoreStandaloneContentBridge.SHELTER_ANCHOR_BLOCK_ID),
                "block_place did not mutate the standalone world with the canonical block id"
        );
        require(host.ledger().hasSavedMutation(), "ledger has no saved mutation");
        require(host.ledger().hasVisibleMutation(), "ledger has no visible mutation");
        require(
                Files.walk(saveRoot).anyMatch(path -> path.toString().replace('\\', '/').endsWith("playable/mission.properties")),
                "standalone host did not write playable mission save data"
        );

        System.out.println("Standalone runtime host action smoke passed: AdapterCore item_use and block_place mutated state, mission, save, and HUD feedback.");
    }

    private static void requireMutation(
            StandaloneEchoRuntimeHost.StandaloneRuntimeHostResult result,
            String label
    ) {
        require(result.mutated(), label + " was not marked MUTATED: " + result.status() + " " + result.failureReason());
        require(result.saveTouched(), label + " did not write save data");
        require(result.feedbackEmitted(), label + " did not emit visible feedback");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
