package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class EchoClientResourcePackScreenSmokeHarness {
    private EchoClientResourcePackScreenSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        require(!services.resourcePackSummaries().isEmpty(),
                "Client should discover mounted Minecraft-style resource pack roots");
        requireClientModuleLaunchDiscoversAshfallResources();
        requireExternalResourcePacksOverrideBuiltInResources();

        EchoClientScreenController screens = new EchoClientScreenController();
        screens.showMainMenu(services.hasContinuableSession());
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_RESOURCE_PACKS, services.hasContinuableSession()),
                "Resource Packs navigation command should open the ScreenCore pack list");
        screens.updateResourcePacks(services.resourcePackSummaries(), services.resourcePackError());
        EchoClientScreenSnapshot packs = screens.snapshot(services.hasContinuableSession());
        require(packs.kind() == EchoClientScreenKind.RESOURCE_PACKS,
                "Resource Packs screen should be active");
        require(packs.options().stream()
                        .anyMatch(option -> option.command() == EchoClientScreenCommand.RELOAD_TEXTURE_ATLAS
                                && option.enabled()),
                "Resource Packs screen should expose an enabled Texture Atlas Reload command");

        EchoClientScreenOption firstPack = packs.options().stream()
                .filter(option -> option.command() == EchoClientScreenCommand.OPEN_RESOURCE_PACK_DETAIL)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Resource Packs screen should expose selectable pack detail rows"));
        require(!firstPack.targetId().isBlank(),
                "Resource pack detail row should carry the pack id as its target");

        EchoClientScreenCommand command = screens.activateSelection(services.hasContinuableSession());
        require(command == EchoClientScreenCommand.OPEN_RESOURCE_PACK_DETAIL,
                "Selecting a resource pack should request the detail route");
        require(screens.executeNavigationCommand(command, services.hasContinuableSession()),
                "Resource pack detail route should be handled by the ScreenCore controller");
        EchoClientScreenSnapshot detail = screens.snapshot(services.hasContinuableSession());
        require(detail.kind() == EchoClientScreenKind.RESOURCE_PACK_DETAIL,
                "Resource pack detail screen should be active");
        require(detail.title().equals(firstPack.targetId()),
                "Resource pack detail title should use the selected pack id");
        require(detail.options().stream().anyMatch(option -> option.label().startsWith("Root: ")),
                "Resource pack detail should expose the mounted root path");
        require(detail.options().stream().anyMatch(option -> option.label().startsWith("Namespaces: ")),
                "Resource pack detail should expose namespaces");
        require(detail.options().stream().anyMatch(option -> option.label().startsWith("Textures: ")),
                "Resource pack detail should expose texture count");
        require(detail.options().stream().anyMatch(option -> option.label().startsWith("Animated Textures: ")),
                "Resource pack detail should expose animated texture metadata count");
        require(detail.options().stream().anyMatch(option -> option.label().startsWith("Sound Events: ")),
                "Resource pack detail should expose sound event count");
        require(screens.executeNavigationCommand(EchoClientScreenCommand.BACK, services.hasContinuableSession()),
                "Back from resource pack detail should be handled");
        require(screens.snapshot(services.hasContinuableSession()).kind() == EchoClientScreenKind.RESOURCE_PACKS,
                "Back should return to the Resource Packs list");

        System.out.println("client resource pack screen smoke PASS packs="
                + services.resourcePackSummaries().size()
                + " opened=" + firstPack.targetId());
    }

    private static void requireClientModuleLaunchDiscoversAshfallResources() throws IOException {
        Path clientModuleRoot = Path.of("echo-runtime-client").toAbsolutePath().normalize();
        Path ashfallResources = Path.of(
                "..",
                "addons",
                "echoashfallprotocol",
                "src",
                "main",
                "resources"
        ).toAbsolutePath().normalize();
        List<Path> candidates = EchoClientResourcePackService.candidateRoots(List.of(clientModuleRoot));
        require(candidates.contains(ashfallResources),
                "Client module launch roots should still mount Ashfall addon resources");

        EchoClientResourcePackService service = new EchoClientResourcePackService(List.of(clientModuleRoot));
        require(service.resourcePacks().stream().anyMatch(pack -> pack.id().equals("echoashfallprotocol")),
                "Client module launch should expose echoashfallprotocol as a resource pack");
        EchoMinecraftAssetResolver minecraftAssets = service.minecraftAssets();
        require(minecraftAssets != null,
                "Client module launch should build a Minecraft asset resolver");
        for (String texture : List.of(
                "micro_generator",
                "ore_grinder",
                "scrap_press",
                "battery_bank",
                "toxic_puddle",
                "item_pipe",
                "scrap_ore",
                "power_cable",
                "load_distributor",
                "wasteland_grass"
        )) {
            require(minecraftAssets.texture("echoashfallprotocol", "block/" + texture).isPresent(),
                    "Ashfall texture should resolve from mounted addon resources: " + texture);
        }

        EchoBlockTextureResolver blockTextures = new EchoBlockTextureResolver(minecraftAssets);
        EchoBlockTextureResolver.EchoBlockTextureResolution microGenerator =
                blockTextures.resolve("echoashfallprotocol:micro_generator", Map.of("active", "false"));
        require(microGenerator.resolved(),
                "Ashfall micro generator blockstate/model should resolve through mounted addon resources");
        require(microGenerator.textureIdForFace("north").orElse("").equals(
                        "echoashfallprotocol:block/micro_generator_front"),
                "Ashfall micro generator north face should use the real front texture");
        require(microGenerator.textureIdForFace("up").orElse("").equals(
                        "echoashfallprotocol:block/micro_generator_top"),
                "Ashfall micro generator top face should use the real top texture");
        require(microGenerator.texturePathForFace("north")
                        .flatMap(path -> minecraftAssets.texture("echoashfallprotocol", path))
                        .isPresent(),
                "Ashfall model-resolved front texture PNG should be mounted");
    }

    private static void requireExternalResourcePacksOverrideBuiltInResources() throws IOException {
        Path root = Path.of("build/tmp/client-resource-pack-priority-smoke").toAbsolutePath().normalize();
        deleteRecursively(root);
        Path workspaceRoot = root.resolve("Echo");
        Path standaloneRoot = workspaceRoot.resolve("echo-standalone-runtime");
        Path clientRoot = standaloneRoot.resolve("echo-runtime-client");
        Path baseRoot = clientRoot.resolve("src/main/resources");
        Path overrideRoot = standaloneRoot.resolve("resourcepacks/override");
        Path zipPack = standaloneRoot.resolve("resourcepacks/archive-client-pack.zip");
        Path jarPack = standaloneRoot.resolve("resourcepacks/archive-client-addon.jar");
        Path baseTexture = baseRoot.resolve("assets/prioritytest/textures/block/panel.png");
        Path overrideTexture = overrideRoot.resolve("assets/prioritytest/textures/block/panel.png");
        write(standaloneRoot.resolve("settings.gradle"), "rootProject.name = 'priority-smoke'\n");
        Files.createDirectories(clientRoot);
        Files.createDirectories(workspaceRoot.resolve("core"));
        write(baseTexture, "base-texture");
        write(overrideTexture, "override-texture");
        writeArchivePack(zipPack, "archiveclient", "token", "archive-zip-texture");
        writeArchivePack(jarPack, "archivejar", "relic", "archive-jar-texture");

        List<Path> candidates = EchoClientResourcePackService.candidateRoots(List.of(clientRoot));
        require(candidates.indexOf(baseRoot) >= 0,
                "Priority smoke should include built-in client resources");
        require(candidates.indexOf(overrideRoot) >= 0,
                "Priority smoke should include external override pack");
        require(candidates.indexOf(zipPack) >= 0,
                "Client resource-pack discovery should include zip archives");
        require(candidates.indexOf(jarPack) >= 0,
                "Client resource-pack discovery should include jar archives");
        require(candidates.indexOf(baseRoot) < candidates.indexOf(overrideRoot),
                "External resource packs should mount after built-in resources");

        EchoClientResourcePackService service = new EchoClientResourcePackService(List.of(clientRoot));
        require(service.lastError().isBlank(),
                "Archive resource pack scan should not report an error: " + service.lastError());
        EchoMinecraftAssetResolver minecraftAssets = service.minecraftAssets();
        require(minecraftAssets != null,
                "Priority smoke should build a Minecraft asset resolver");
        require(minecraftAssets.texture("prioritytest", "block/panel")
                        .map(entry -> entry.file().equals(overrideTexture))
                        .orElse(false),
                "External resource pack texture should override built-in client texture");
        require(service.resourcePacks().stream().anyMatch(pack -> pack.root().equals(zipPack)
                        && pack.namespaces().contains("archiveclient")
                        && pack.textureCount() == 1L
                        && pack.modelCount() == 1L
                        && pack.langCount() == 1L),
                "Client resource-pack summaries should include parsed zip archive assets");
        require(service.resourcePacks().stream().anyMatch(pack -> pack.root().equals(jarPack)
                        && pack.namespaces().contains("archivejar")
                        && pack.textureCount() == 1L
                        && pack.modelCount() == 1L
                        && pack.langCount() == 1L),
                "Client resource-pack summaries should include parsed jar archive assets");
        require(service.assets().resolver()
                        .loadText("archiveclient:textures/item/token.png")
                        .orElseThrow()
                        .equals("archive-zip-texture"),
                "Client asset runtime should load texture bytes from a zip archive pack");
        require(service.assets().resolver()
                        .loadText("archivejar:textures/item/relic.png")
                        .orElseThrow()
                        .equals("archive-jar-texture"),
                "Client asset runtime should load texture bytes from a jar archive pack");
    }

    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }

    private static void writeArchivePack(Path archivePack, String namespace, String item, String texture) throws IOException {
        Files.createDirectories(archivePack.getParent());
        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(archivePack))) {
            zipEntry(zip, "pack.mcmeta", """
                    {
                      "pack": {
                        "pack_format": 34,
                        "description": "Client archive smoke pack"
                      }
                    }
                    """);
            zipEntry(zip, "assets/" + namespace + "/lang/en_us.json",
                    "{\"" + namespace + "." + item + "\":\"Archive Item\"}");
            zipEntry(zip, "assets/" + namespace + "/models/item/" + item + ".json",
                    "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\""
                            + namespace + ":item/" + item + "\"}}");
            zipEntry(zip, "assets/" + namespace + "/textures/item/" + item + ".png", texture);
        }
    }

    private static void zipEntry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
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
