package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoVoxelAshfallBiomes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class EchoClientEntitySpawnSmokeHarness {
    private EchoClientEntitySpawnSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());

        require(session.livingEntityCount() == 0,
                "New client sessions should start without spawned runtime mobs");
        EchoClientEntitySpawnSummary first = session.tickEntities(1.1D);
        require(first.reason().equals("spawned"),
                "First eligible entity tick should spawn near the player");
        require(session.livingEntityCount() > 0,
                "Entity store should contain a spawned mob");
        require(session.hostileEntityCount() == session.livingEntityCount(),
                "Biome spawns should currently create hostile mobs");
        require(first.definitionId().equals("echoashfallprotocol:rad_zombie"),
                "Crash-zone biome should spawn the ashfall rad zombie prototype");

        EchoEntityState spawned = session.entityStore().living().getFirst();
        require(!session.world().blockStateAt(
                spawned.worldPosition().x(),
                spawned.worldPosition().y() - 1,
                spawned.worldPosition().z()
        ).air(), "Spawned mob should stand on generated terrain");
        require(session.world().blockStateAt(
                spawned.worldPosition().x(),
                spawned.worldPosition().y(),
                spawned.worldPosition().z()
        ).air(), "Spawned mob position should be empty air");

        for (int i = 0; i < 20; i++) {
            session.tickEntities(1.1D);
        }
        require(session.hostileEntityCount() <= 10,
                "Spawner should cap hostile density in loaded chunks");

        Set<String> definitions = session.entityStore().living().stream()
                .map(EchoEntityState::definition)
                .map(EchoEntityDefinition::definitionId)
                .collect(Collectors.toSet());
        require(definitions.contains("echoashfallprotocol:rad_zombie"),
                "Spawn set should include the crash-zone hostile definition");

        EchoClientEntityCatalog entityCatalog = session.entityCatalog();
        require(entityCatalog.definitionForBiome(EchoVoxelAshfallBiomes.TOXIC_SWAMP)
                        .definitionId().equals("echoashfallprotocol:toxic_slime"),
                "Toxic biome should map to toxic slime spawns");
        require(entityCatalog.definitionForBiome(EchoVoxelAshfallBiomes.RADIATION_ZONE)
                        .definitionId().equals("echoashfallprotocol:glowing_ghoul"),
                "Radiation biome should map to glowing ghoul spawns");
        require(entityCatalog.definitionForBiome(EchoVoxelAshfallBiomes.INDUSTRIAL_RUINS)
                        .definitionId().equals("echoashfallprotocol:rust_walker"),
                "Industrial biome should map to rust walker spawns");
        require(entityCatalog.definitionForBiome(EchoVoxelAshfallBiomes.NEXUS_SCAR)
                        .definitionId().equals("echoashfallprotocol:echo_drone"),
                "Nexus biome should map to ECHO drone spawns");

        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("ENTITIES " + session.livingEntityCount()),
                "Debug overlay should show live entity counts");
        require(debug.contains("SPAWN echoashfallprotocol:"),
                "Debug overlay should show the last spawn prototype");
        require(!debug.contains(","),
                "Entity debug lines should preserve the HUD font punctuation contract");

        requireNativeEntityRegistrationReachesLiveSession();

        System.out.println("client entity spawn smoke PASS living="
                + session.livingEntityCount()
                + " definitions=" + definitions.size());
    }

    private static void requireNativeEntityRegistrationReachesLiveSession() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        String nativeEntityId = importNativeRegisteredEntity(services);
        services.startNewWorld("native-entity-spawn-smoke");
        EchoClientGameSession session = services.session();
        require(session != null,
                "Native entity smoke should create an active runtime-services session");

        EchoClientEntitySpawnSummary first = session.tickEntities(1.1D);
        require(first.reason().equals("spawned"),
                "Native entity smoke should spawn through the live entity runtime");
        require(first.definitionId().equals(nativeEntityId),
                "Native entity spawn rule should override the crash-zone prototype in the live session");
        require(first.threatProfile().equals("irradiated_drone") && first.threatLevel() == 4,
                "Native entity spawn summary should expose graph threat metadata");
        require(first.spawnBiomeTags().contains("crash_zone"),
                "Native entity spawn summary should expose graph spawn tags");

        EchoEntityState spawned = session.entityStore().living().getFirst();
        require(spawned.definition().displayName().equals("Native Watch Drone"),
                "Spawned native entity should preserve its native registration display name");
        require(spawned.definition().maxHealth() == 42,
                "Spawned native entity should preserve native registration health data");
        require(spawned.definition().aiProfile().equals("hostile_scavenger"),
                "Spawned native entity should preserve native registration AI profile data");
        require(EchoClientEntityRenderer.argbForDefinition(nativeEntityId, session.entityCatalog()) == 0xFF40C8FF,
                "Live entity renderer metadata should use the native entity render tint");
        EchoClientEntityCatalog.RenderProfile nativeProfile =
                session.entityCatalog().renderProfile(nativeEntityId);
        require(nativeProfile.graphBackedVisual(),
                "Live entity catalog should mark module entity rows with model/texture/animation as graph-backed");
        require(nativeProfile.modelId().equals("echoruntimehost:entity/native_watch_drone"),
                "Live entity catalog should preserve graph entity model ids");
        require(nativeProfile.textureId().equals("echoruntimehost:textures/entity/native_watch_drone.png"),
                "Live entity catalog should preserve graph entity texture ids");
        require(nativeProfile.animationId().equals("echoruntimehost:animations/entity/native_watch_drone.animation.json"),
                "Live entity catalog should preserve graph entity animation ids");
        require(nativeProfile.spawnRuleMetadataPresent()
                        && nativeProfile.spawnBiomeTags().contains("crash_zone"),
                "Live entity catalog should preserve graph spawn metadata");
        require(nativeProfile.threatMetadataPresent()
                        && nativeProfile.threatProfile().equals("irradiated_drone")
                        && nativeProfile.threatLevel() == 4,
                "Live entity catalog should preserve graph threat metadata");
        EchoClientEntityCatalog.EntityVisualProfile firstProfile =
                session.entityCatalog().firstGraphBackedSpawnProfile().orElseThrow();
        require(firstProfile.definition().definitionId().equals(nativeEntityId),
                "Live entity catalog should expose module-backed visual profiles for evidence gates");
        require(session.entityCatalog().graphBackedVisualProfileCount() >= 1
                        && session.entityCatalog().graphBackedSpawnRuleProfileCount() >= 1
                        && session.entityCatalog().graphBackedThreatProfileCount() >= 1,
                "Live entity catalog should report graph entity visual, spawn, and threat coverage");
        EchoClientEntityRenderer.MeshData mesh =
                EchoClientEntityRenderer.meshData(session.entityStore().living(), session.entityCatalog());
        require(mesh.vertexCount() == 72,
                "Live entity renderer metadata should use the native entity drone shape");
    }

    private static String importNativeRegisteredEntity(EchoClientRuntimeServices services) {
        int imported = services.importAdapterCoreContentRegistrations(List.of(Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:entity/native_watch_drone",
                "contentKind", "ENTITY",
                "domain", "entities",
                "displayName", "Native Watch Drone",
                "adapterKey", "registry.entities.native_watch_drone",
                "neoForgeId", "echoruntimehost:native_watch_drone",
                "nativeLoaderId", "echoruntimehost:entity/native_watch_drone",
                "standaloneRuntimeId", "echoruntimehost:native_watch_drone",
                "metadata", Map.ofEntries(
                        Map.entry("definitionId", "echoruntimehost:native_watch_drone"),
                        Map.entry("kind", "HOSTILE"),
                        Map.entry("maxHealth", 42),
                        Map.entry("movementSpeed", 1),
                        Map.entry("aiProfile", "hostile_scavenger"),
                        Map.entry("biomeTags", List.of("crash_zone")),
                        Map.entry("modelId", "echoruntimehost:entity/native_watch_drone"),
                        Map.entry("textureId", "echoruntimehost:textures/entity/native_watch_drone.png"),
                        Map.entry("animationId", "echoruntimehost:animations/entity/native_watch_drone.animation.json"),
                        Map.entry("renderArgb", "#40C8FF"),
                        Map.entry("renderShape", "DRONE"),
                        Map.entry("threatProfile", "irradiated_drone"),
                        Map.entry("threatLevel", 4)
                )
        )));
        require(imported == 1,
                "Client runtime services should import one native entity registration row");
        return "echoruntimehost:native_watch_drone";
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
