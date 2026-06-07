package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelAshfallBiomes;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoClientBiomeHazardSmokeHarness {
    private EchoClientBiomeHazardSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireHazardModelAndHud();
        requireLiveHazardDamageAndDebug();
        requireNativeHazardRegistrationReachesLiveSession();
        requireDataWorldCoreHazardRegistrationReachesLiveSession();
        requireDiskRestore();
        System.out.println("client biome hazard smoke PASS hazard=restored exposure=live");
    }

    private static void requireHazardModelAndHud() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("hazard-model").gameSession();
        EchoClientHazardCatalog hazardCatalog = session.hazardCatalog();
        EchoClientHazardState.EchoClientHazardTick toxic =
                EchoClientHazardState.empty().tick(EchoVoxelAshfallBiomes.TOXIC_SWAMP, 15.0D, hazardCatalog);
        require(toxic.state().hazardId().equals("echoashfallprotocol:toxic_air"),
                "Toxic biome should map to toxic-air hazard state");
        require(toxic.state().exposurePercent() == 100,
                "Long toxic exposure should clamp to full hazard exposure");
        require(toxic.damage() > 0 && toxic.source().id().contains("toxic_air"),
                "Severe toxic exposure should produce typed hazard damage");
        require(EchoClientHud.hazardFillPixels(toxic.state(), 100) == 100,
                "HUD hazard bar should fill by exposure percent");

        EchoClientHazardState.EchoClientHazardTick safe =
                toxic.state().tick(EchoVoxelAshfallBiomes.CRASH_ZONE_WASTELAND, 10.0D, hazardCatalog);
        require(!safe.state().active(),
                "Safe biomes should decay hazard exposure back to inactive");
        require(EchoClientHud.hazardFillPixels(null, 100) == 0,
                "HUD hazard bar should tolerate missing hazard state");
    }

    private static void requireLiveHazardDamageAndDebug() {
        EchoClientWorldSession worldSession = worldSessionAtBiome("hazard-live", "radiation");
        EchoClientGameSession session = worldSession.gameSession();
        int beforeHealth = session.playerVitals().currentHealth();
        session.tickBiomeHazards(15.0D);
        require(session.hazardState().hazardId().equals("echoashfallprotocol:radiation"),
                "Live session should derive hazard state from the player's biome");
        require(session.hazardState().lastDamage() > 0 && session.playerVitals().currentHealth() < beforeHealth,
                "Live biome hazards should damage the player through the session damage pipeline");
        require(session.playerCombatState().lastDamageSource().id().contains("echo:hazard/echoashfallprotocol:radiation"),
                "Live hazard damage should preserve a typed damage source");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("HAZ echoashfallprotocol:radiation EXP 100 DMG"),
                "Debug overlay should expose current hazard id exposure and damage");
        require(!debug.contains(","),
                "Hazard debug line should preserve the HUD font punctuation contract");

        EchoClientWorldSession creativeWorld = worldSessionAtBiome("hazard-creative", "toxic");
        EchoClientGameSession creative = creativeWorld.gameSession();
        creative.setGameMode(EchoClientGameMode.CREATIVE);
        creative.tickBiomeHazards(15.0D);
        require(!creative.hazardState().active()
                        && creative.playerVitals().currentHealth() == EchoClientPlayerVitals.DEFAULT_MAX_HEALTH,
                "Creative mode should not build or apply biome hazard exposure");
    }

    private static void requireNativeHazardRegistrationReachesLiveSession() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        String nativeHazardId = importNativeRegisteredHazard(services);
        services.startNewWorld("native-hazard-smoke");
        EchoClientGameSession session = services.session();
        require(session != null,
                "Native hazard smoke should create an active runtime-services session");
        int beforeHealth = session.playerVitals().currentHealth();

        session.tickBiomeHazards(15.0D);
        require(session.hazardState().hazardId().equals(nativeHazardId),
                "Live session should derive crash-zone hazard state from native content registrations");
        require(session.hazardState().label().equals("Native Volatile Ash"),
                "Live native hazard state should preserve the imported display name");
        require(session.hazardState().exposurePercent() == 100,
                "Live native hazard exposure should clamp to full exposure");
        require(session.hazardState().lastDamage() > 0 && session.playerVitals().currentHealth() < beforeHealth,
                "Live native hazard should damage through the session damage pipeline");
        require(session.playerCombatState().lastDamageSource().id().contains(nativeHazardId),
                "Live native hazard damage should preserve a typed native damage source");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("HAZ " + nativeHazardId + " EXP 100 DMG"),
                "Debug overlay should expose the native hazard id exposure and damage");
    }

    private static String importNativeRegisteredHazard(EchoClientRuntimeServices services) {
        int imported = services.importAdapterCoreContentRegistrations(List.of(Map.of(
                "moduleId", "echoruntimehost",
                "contentId", "echoruntimehost:hazard/native_volatile_ash",
                "contentKind", "WORLD_HAZARD",
                "domain", "hazards",
                "displayName", "Native Volatile Ash",
                "adapterKey", "registry.hazards.native_volatile_ash",
                "neoForgeId", "echoruntimehost:native_volatile_ash",
                "nativeLoaderId", "echoruntimehost:hazard/native_volatile_ash",
                "standaloneRuntimeId", "echoruntimehost:native_volatile_ash",
                "metadata", Map.of(
                        "hazardId", "echoruntimehost:native_volatile_ash",
                        "biomeTags", List.of("crash_zone"),
                        "exposurePerSecond", "12.0",
                        "damage", 3
                )
        )));
        require(imported == 1,
                "Client runtime services should import one native hazard registration row");
        return "echoruntimehost:native_volatile_ash";
    }

    private static void requireDataWorldCoreHazardRegistrationReachesLiveSession() throws IOException {
        Path root = Path.of("build", "tmp", "client-data-worldcore-hazard-smoke").toAbsolutePath().normalize();
        deleteRecursively(root);
        Path workspaceRoot = root.resolve("Echo");
        Path standaloneRoot = workspaceRoot.resolve("echo-standalone-runtime");
        Path clientRoot = standaloneRoot.resolve("echo-runtime-client");
        Path packRoot = standaloneRoot.resolve("resourcepacks/data-worldcore-hazard-smoke");
        String hazardId = "smokehzd:hazard/toxic_spores";
        write(standaloneRoot.resolve("settings.gradle"), "rootProject.name = 'data-worldcore-hazard-smoke'\n");
        Files.createDirectories(clientRoot);
        Files.createDirectories(workspaceRoot.resolve("core"));
        write(packRoot.resolve("pack.mcmeta"), """
                {
                  "pack": {
                    "pack_format": 34,
                    "description": "Data WorldCore hazard smoke"
                  }
                }
                """);
        write(packRoot.resolve("data/smokehzd/worldgen/biome/toxic_data_basin.json"), """
                {
                  "temperature": 1.0,
                  "downfall": 0.7,
                  "centerX": 80,
                  "centerZ": 80,
                  "radius": 6,
                  "tags": ["runtime_smoke", "toxic"],
                  "effects": {
                    "fog_color": 2174020,
                    "grass_color": 4259960,
                    "ambient_particle": {
                      "options": {
                        "type": "minecraft:spore_blossom_air"
                      }
                    }
                  }
                }
                """);
        write(packRoot.resolve("data/smokehzd/echoworldcore/world_regions/data_hazard_basin.json"), """
                {
                  "id": "smokehzd:data_hazard_basin",
                  "type": "toxic_surface",
                  "displayName": "Data Hazard Basin",
                  "summary": "Region that links a standalone WorldCore hazard to a data biome.",
                  "biomeIds": ["smokehzd:toxic_data_basin"],
                  "hazardIds": ["%s"],
                  "radius": 6
                }
                """.formatted(hazardId));
        write(packRoot.resolve("data/smokehzd/echoworldcore/world_hazards/hazard/toxic_spores.json"), """
                {
                  "id": "%s",
                  "type": "toxic_air",
                  "displayName": "Toxic Spores",
                  "summary": "Data-driven spores that should reach the live client hazard model.",
                  "defaultSeverity": 64,
                  "ticking": true,
                  "exposurePerSecond": 16.0,
                  "damage": 3
                }
                """.formatted(hazardId));

        EchoClientResourcePackService resourcePacks = new EchoClientResourcePackService(List.of(clientRoot));
        EchoClientRuntimeServices services = new EchoClientRuntimeServices(
                EchoClientSaveSlotService.open(root.resolve("saves")),
                resourcePacks
        );
        require(resourcePacks.resourcePacks().stream().anyMatch(pack -> pack.id().equals("data-worldcore-hazard-smoke")),
                "Data WorldCore hazard smoke pack should be mounted");
        require(services.loadedDataWorldgenBiomeRowCount() >= 1,
                "Data WorldCore hazard smoke should bridge its data biome; rows="
                        + services.loadedDataWorldgenBiomeRowCount()
                        + " error=" + services.dataWorldgenBiomeError());
        require(services.loadedDataWorldCoreHazardRowCount() >= 1,
                "Data WorldCore hazard should bridge into live hazard rows; rows="
                        + services.loadedDataWorldCoreHazardRowCount()
                        + " error=" + services.dataWorldCoreHazardError());
        require(services.dataWorldgenBiomeError().isBlank(),
                "Data WorldCore hazard smoke biome bridge should not report an error");
        require(services.dataWorldCoreHazardError().isBlank(),
                "Data WorldCore hazard bridge should not report an error");

        services.startNewWorld("data-worldcore-hazard");
        EchoClientGameSession session = services.session();
        require(session != null, "Data WorldCore hazard smoke requires a live session");
        movePlayer(session, 80.5D, session.player().state().y(), 80.5D);
        services.streamAroundPlayer(EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE);
        int beforeHealth = session.playerVitals().currentHealth();
        session.tickBiomeHazards(10.0D);
        require(session.hazardState().hazardId().equals(hazardId),
                "Live session should derive hazard state from mounted WorldCore hazard data");
        require(session.hazardState().label().equals("Toxic Spores"),
                "Live WorldCore hazard should preserve its data display name");
        require(session.hazardState().exposurePercent() == 100,
                "Live WorldCore hazard exposure should use data severity/profile hints");
        require(session.hazardState().lastDamage() > 0 && session.playerVitals().currentHealth() < beforeHealth,
                "Live WorldCore hazard should damage through the session damage pipeline");
        require(session.playerCombatState().lastDamageSource().id().contains(hazardId),
                "Live WorldCore hazard damage should preserve a typed data hazard source");
    }

    private static void requireDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-biome-hazard-save-smoke").toAbsolutePath();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_hazard_profile.v1",
                "client-hazard-smoke",
                "Client Hazard Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-hazard"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = worldSessionAtBiome("hazard-save", "nexus");
        worldSession.gameSession().tickBiomeHazards(15.0D);

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-hazard-save", "hazard-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.HAZARDS_PATH).isPresent(),
                "Client save manifest should include hazard state");
        require(manifest.metadata().getOrDefault("clientHazardsCodec", "").equals("echo.client.hazards.v1"),
                "Client save manifest should advertise the hazard codec");
        require(Integer.parseInt(manifest.metadata().getOrDefault("hazardExposure", "0")) > 0,
                "Client save manifest should expose hazard exposure");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                saves,
                manifest
        );
        EchoClientWorldSession restored = EchoClientWorldSession.fromSavedSession(
                manifest.slotId(),
                manifest.metadata().getOrDefault("displayName", manifest.slotId()),
                restoredSnapshot
        );
        require(restored.gameSession().hazardState().hazardId().equals(worldSession.gameSession().hazardState().hazardId()),
                "Disk restore should preserve the active hazard id");
        require(restored.gameSession().hazardState().exposurePercent()
                        == worldSession.gameSession().hazardState().exposurePercent(),
                "Disk restore should preserve hazard exposure");
        require(restored.gameSession().hazardState().lastDamage()
                        == worldSession.gameSession().hazardState().lastDamage(),
                "Disk restore should preserve last hazard damage");
    }

    private static EchoClientWorldSession worldSessionAtBiome(String seed, String requiredTag) {
        EchoClientWorldSession base = EchoClientWorldSessionFactory.defaultFactory().newWorld(seed);
        EchoClientGameSession session = base.gameSession();
        int[] position = findBiomePosition(session.world(), requiredTag);
        EchoVoxelPlayerState current = session.player().state();
        EchoVoxelPlayerState relocated = new EchoVoxelPlayerState(
                position[0] + 0.5D,
                current.y(),
                position[1] + 0.5D,
                0.0D,
                current.yawDegrees(),
                current.pitchDegrees(),
                current.grounded(),
                current.crouching(),
                current.sprinting(),
                current.selectedSlot(),
                current.reach()
        );
        EchoClientSavedSessionSnapshot snapshot = new EchoClientSavedSessionSnapshot(
                new EchoClientGameplay.GameplaySnapshot(session.world(), relocated, session.hotbar()),
                session.inventorySnapshots(),
                session.containerSnapshots(),
                session.playerVitals(),
                session.playerCombatState(),
                session.progressionState(),
                session.hazardState(),
                session.toolState()
        );
        return EchoClientWorldSession.fromSavedSession(base.slotId(), base.displayName(), snapshot);
    }

    private static int[] findBiomePosition(EchoVoxelWorld world, String requiredTag) {
        for (int z = -384; z <= 384; z += 16) {
            for (int x = -384; x <= 384; x += 16) {
                if (world.biomeAt(x, z).hasTag(requiredTag)) {
                    return new int[]{x, z};
                }
            }
        }
        throw new AssertionError("Could not find biome tag " + requiredTag + " in deterministic Ashfall search area");
    }

    private static void movePlayer(
            EchoClientGameSession session,
            double x,
            double y,
            double z
    ) {
        EchoVoxelPlayerState current = session.player().state();
        EchoVoxelPlayerController moved = new EchoVoxelPlayerController(new EchoVoxelPlayerState(
                x,
                y,
                z,
                current.velocityY(),
                current.yawDegrees(),
                current.pitchDegrees(),
                current.grounded(),
                current.crouching(),
                current.sprinting(),
                current.selectedSlot(),
                current.reach()
        ));
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), moved, session.hotbar());
        session.updateFromGameplay(gameplay);
    }

    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
