package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.world.EchoVoxelOpenlandsBiomes;
import dev.echo.standalone.runtime.world.EchoVoxelOpenlandsWorldGeneration;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoClientOpenlandsSessionSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports/echo/standalone/client-openlands-session.json");

    private EchoClientOpenlandsSessionSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Files.createTempDirectory("echo-openlands-session-smoke");
        EchoClientRuntimeServices services = EchoClientRuntimeServices.openlandsStandard(saveRoot);
        services.startNewWorld("42", "Openlands Session Smoke");
        EchoClientWorldSession worldSession = services.worldSession();
        EchoClientGameSession session = services.session();
        EchoVoxelWorld world = session.world();

        require(worldSession != null, "Openlands runtime services should create a world session");
        require(session != null, "Openlands runtime services should expose an active game session");
        require(worldSession.slotId().startsWith("openlands-standard-"),
                "Openlands world session should use the Openlands slot id prefix");
        require(world.worldId().equals(EchoVoxelOpenlandsWorldGeneration.WORLD_ID),
                "Openlands client session should use the Openlands standalone first-hour world id");
        require(world.biomeAt(world.spawnX(), world.spawnZ()).id().equals(EchoVoxelOpenlandsBiomes.MEADOWS.id()),
                "Openlands client session should spawn in Meadows");
        require(session.hotbar().slot(0).block().id().equals("echoopenlandsprotocol:branchwood_planks"),
                "Openlands starter hotbar should expose branchwood planks");
        require(session.hotbar().slot(1).block().id().equals("echoopenlandsprotocol:campfire"),
                "Openlands starter hotbar should expose a campfire");
        require(session.containerScreenModel().slots().stream()
                        .anyMatch(slot -> slot.runtimeId().equals("echoopenlandsprotocol:berries") && slot.count() == 6),
                "Openlands starter satchel should include gentle starter food");
        require(session.workbenchRecipeSummaries().stream()
                        .anyMatch(recipe -> recipe.recipeId().equals("echoopenlandsprotocol:starter_fiber_binding")),
                "Openlands starter recipes should include fiber binding");
        require(session.hazardCatalog().rules().isEmpty(),
                "Openlands Standard client profile should not enable harsh environmental hazards");
        require(session.bridge().registry().requireLiveVoxelBlock("echoopenlandsprotocol:broken_waystone")
                        .id().equals("echoopenlandsprotocol:broken_waystone"),
                "Openlands bridge should resolve waystone blocks in the live registry");
        require(session.bridge().registry().requireLiveVoxelBlock("echoopenlandsprotocol:crude_pick")
                        .id().equals("echoopenlandsprotocol:crude_pick"),
                "Openlands bridge should resolve tool items in the live registry");

        OpenlandsRegistryCounts counts = countOpenlandsEntries(session);
        require(counts.blocks >= 53, "Openlands bridge should expose all MVP block ids");
        require(counts.items >= 50, "Openlands bridge should expose all MVP item ids");
        require(counts.biomes >= 4, "Openlands bridge should expose all MVP biome ids");

        EchoClientWorldStreamResult stream = session.streamAroundPlayer(2);
        require(stream.activeChunksChanged() || session.world().loadedChunkCount() >= 25,
                "Openlands client session should stream chunks through the live session path");
        require(session.world().loadedChunkCount() >= 25,
                "Openlands client session should hold a streamed 5x5 chunk region at view distance 2");

        EchoClientSavedSessionSnapshot snapshot = session.savedSessionSnapshot();
        EchoClientGameSession restored = EchoClientWorldTemplates.openlandsFirstHour()
                .restoreSession(snapshot, java.util.List.of());
        require(restored.world().worldId().equals(EchoVoxelOpenlandsWorldGeneration.WORLD_ID),
                "Openlands restored session should keep the Openlands world id");
        require(restored.hotbar().slot(0).block().id().equals("echoopenlandsprotocol:branchwood_planks"),
                "Openlands restored session should keep the starter hotbar");

        EchoClientGameSession assemblySession = createAssemblyOpenlandsSession();
        require(assemblySession.world().worldId().equals(EchoVoxelOpenlandsWorldGeneration.WORLD_ID),
                "Openlands runtime assembly should create an Openlands world session");
        require(assemblySession.hazardCatalog().rules().isEmpty(),
                "Openlands runtime assembly should preserve relaxed Standard hazards");

        writeReport(worldSession, session, counts, restored, assemblySession);
        System.out.println("client openlands session smoke PASS world="
                + world.worldId()
                + " chunks=" + session.world().loadedChunkCount()
                + " blocks=" + counts.blocks
                + " items=" + counts.items
                + " biomes=" + counts.biomes);
    }

    private static OpenlandsRegistryCounts countOpenlandsEntries(EchoClientGameSession session) {
        int blocks = 0;
        int items = 0;
        int biomes = 0;
        for (EchoAdapterCoreRegistryEntry entry : session.bridge().registry().entries()) {
            if (!entry.binding().moduleId().equals("echoopenlandsprotocol")) {
                continue;
            }
            if (entry.domain() == EchoAdapterCoreDomain.BLOCKS) {
                blocks++;
            } else if (entry.domain() == EchoAdapterCoreDomain.ITEMS) {
                items++;
            } else if (entry.domain() == EchoAdapterCoreDomain.BIOMES) {
                biomes++;
            }
        }
        return new OpenlandsRegistryCounts(blocks, items, biomes);
    }

    private static EchoClientGameSession createAssemblyOpenlandsSession() {
        EchoClientRuntimeAssembly assembly =
                EchoClientRuntimeAssembly.create(800, 480, EchoClientWorldTemplates.openlandsFirstHour());
        assembly.runtimeServices().startNewWorld("42", "Openlands Assembly Smoke");
        EchoClientGameSession session = assembly.runtimeServices().session();
        require(session != null, "Openlands runtime assembly should expose an active session");
        return session;
    }

    private static void writeReport(
            EchoClientWorldSession worldSession,
            EchoClientGameSession session,
            OpenlandsRegistryCounts counts,
            EchoClientGameSession restored,
            EchoClientGameSession assemblySession
    ) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_openlands_session.v1",
                  "status": "PASS",
                  "worldId": "%s",
                  "slotId": "%s",
                  "displayName": "%s",
                  "spawnBiome": "%s",
                  "loadedChunkCount": %d,
                  "starterHotbar": ["%s", "%s"],
                  "starterSatchelFood": true,
                  "starterRecipeFiberBinding": true,
                  "standardHazardsDisabled": %s,
                  "bridge": {
                    "openlandsBlocks": %d,
                    "openlandsItems": %d,
                    "openlandsBiomes": %d,
                    "brokenWaystoneResolvable": true,
                    "crudePickResolvable": true
                  },
                  "saveReload": {
                    "worldIdPreserved": %s,
                    "hotbarPreserved": %s
                  },
                  "runtimeAssembly": {
                    "worldId": "%s",
                    "standardHazardsDisabled": %s
                  }
                }
                """.formatted(
                escape(session.world().worldId()),
                escape(worldSession.slotId()),
                escape(worldSession.displayName()),
                escape(session.world().biomeAt(session.world().spawnX(), session.world().spawnZ()).id()),
                session.world().loadedChunkCount(),
                escape(session.hotbar().slot(0).block().id()),
                escape(session.hotbar().slot(1).block().id()),
                session.hazardCatalog().rules().isEmpty(),
                counts.blocks,
                counts.items,
                counts.biomes,
                restored.world().worldId().equals(session.world().worldId()),
                restored.hotbar().slot(0).block().id().equals(session.hotbar().slot(0).block().id()),
                escape(assemblySession.world().worldId()),
                assemblySession.hazardCatalog().rules().isEmpty()
        );
        Files.writeString(REPORT_PATH, json, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record OpenlandsRegistryCounts(int blocks, int items, int biomes) {
    }
}
