package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.assets.EchoMinecraftResourcePack;
import dev.echo.standalone.runtime.assets.EchoSoundsJsonLoader;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

final class EchoClientResourcePackService {
    private final List<Path> launchAnchors;
    private List<EchoClientResourcePackSummary> resourcePacks = List.of();
    private EchoAssetRuntimeResult assets;
    private EchoMinecraftAssetResolver minecraftAssets;
    private final EchoClientLanguageService language = new EchoClientLanguageService(null);
    private String lastError = "";

    EchoClientResourcePackService() {
        this(EchoClientWorkspaceRoots.launchAnchors());
    }

    EchoClientResourcePackService(List<Path> launchAnchors) {
        this.launchAnchors = launchAnchors == null || launchAnchors.isEmpty()
                ? EchoClientWorkspaceRoots.launchAnchors()
                : List.copyOf(launchAnchors);
        refresh();
    }

    void refresh() {
        ArrayList<EchoClientResourcePackSummary> result = new ArrayList<>();
        ArrayList<EchoAssetMount> mounts = new ArrayList<>();
        lastError = "";
        try {
            int order = 0;
            for (Path candidate : candidateRoots(launchAnchors)) {
                EchoMinecraftResourcePack pack = EchoMinecraftResourcePack.scan(candidate);
                if (pack.totalAssetCount() <= 0L && !pack.hasPackMetadata()) {
                    continue;
                }
                mounts.add(new EchoAssetMount(order++, "minecraft-resource-pack", pack.root(), pack.id()));
                result.add(new EchoClientResourcePackSummary(
                        pack.id(),
                        pack.root(),
                        pack.namespaces(),
                        pack.textureCount(),
                        pack.animatedTextureMetadataCount(),
                        pack.modelCount(),
                        pack.blockstateCount(),
                        pack.langCount(),
                        pack.soundsJsonCount(),
                        soundEventCount(pack),
                        pack.detail()
                ));
            }
            result.sort(java.util.Comparator.comparing(EchoClientResourcePackSummary::id));
            resourcePacks = List.copyOf(result);
            assets = new EchoAssetRuntime(mounts)
                    .load(new EchoDefaultRuntimeServiceRegistry(), List.of());
            minecraftAssets = new EchoMinecraftAssetResolver(assets.resolver());
            language.setMinecraftAssets(minecraftAssets);
        } catch (IOException | IllegalArgumentException e) {
            lastError = e.getMessage();
            resourcePacks = List.copyOf(result);
            assets = null;
            minecraftAssets = null;
            language.setMinecraftAssets(null);
        }
    }

    List<EchoClientResourcePackSummary> resourcePacks() {
        return resourcePacks;
    }

    String lastError() {
        return lastError;
    }

    EchoMinecraftAssetResolver minecraftAssets() {
        return minecraftAssets;
    }

    EchoAssetRuntimeResult assets() {
        return assets;
    }

    EchoClientLanguageService language() {
        return language;
    }

    static List<Path> candidateRoots(List<Path> launchAnchors) throws IOException {
        LinkedHashSet<Path> result = new LinkedHashSet<>();
        for (Path standaloneRoot : EchoClientWorkspaceRoots.standaloneRuntimeRoots(launchAnchors)) {
            addIfDirectory(result, standaloneRoot.resolve("echo-runtime-client/src/main/resources"));
        }
        for (Path echoRoot : EchoClientWorkspaceRoots.echoWorkspaceRoots(launchAnchors)) {
            addIfDirectory(result, echoRoot.resolve("src/main/resources"));
            addModuleResourceRoots(result, echoRoot.resolve("core"));
            addModuleResourceRoots(result, echoRoot.resolve("addons"));
            addModuleResourceRoots(result, echoRoot.resolve("ECHO-Modules/addons"));
        }
        for (Path addonsRoot : EchoClientWorkspaceRoots.echoModuleAddonRoots(launchAnchors)) {
            addModuleResourceRoots(result, addonsRoot);
            if (addonsRoot.getFileName() != null
                    && addonsRoot.getFileName().toString().equalsIgnoreCase("mods")) {
                addChildren(result, addonsRoot);
            }
        }
        for (Path standaloneRoot : EchoClientWorkspaceRoots.standaloneRuntimeRoots(launchAnchors)) {
            addChildren(result, standaloneRoot.resolve("resourcepacks"));
            addChildren(result, standaloneRoot.resolve("packs"));
        }
        addChildren(result, Path.of("resourcepacks"));
        addChildren(result, Path.of("packs"));
        return List.copyOf(result);
    }

    private static void addChildren(LinkedHashSet<Path> result, Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (var stream = Files.list(root)) {
            for (Path child : stream.sorted().toList()) {
                addIfPackRoot(result, child);
            }
        }
    }

    private static void addModuleResourceRoots(LinkedHashSet<Path> result, Path modulesRoot) throws IOException {
        if (!Files.isDirectory(modulesRoot)) {
            return;
        }
        try (var stream = Files.list(modulesRoot)) {
            for (Path module : stream.filter(Files::isDirectory).sorted().toList()) {
                addIfDirectory(result, module.resolve("src/main/resources"));
            }
        }
    }

    private static void addIfDirectory(LinkedHashSet<Path> result, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (Files.isDirectory(normalized)) {
            result.add(normalized);
        }
    }

    private static void addIfPackRoot(LinkedHashSet<Path> result, Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        if (Files.isDirectory(normalized) || Files.isRegularFile(normalized) && archivePack(normalized)) {
            result.add(normalized);
        }
    }

    private static boolean archivePack(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".zip") || name.endsWith(".jar");
    }

    private static long soundEventCount(EchoMinecraftResourcePack pack) {
        if (pack.soundsJsonCount() <= 0L || pack.namespaces().isEmpty()) {
            return 0L;
        }
        try {
            EchoAssetRuntimeResult assets = new EchoAssetRuntime(List.of(
                    new EchoAssetMount(0, "minecraft-resource-pack", pack.root(), pack.id())
            )).load(new EchoDefaultRuntimeServiceRegistry(), List.of());
            EchoSoundsJsonLoader loader = new EchoSoundsJsonLoader(new EchoMinecraftAssetResolver(assets.resolver()));
            long count = 0L;
            for (String namespace : pack.namespaces()) {
                count += loader.load(namespace).events().size();
            }
            return count;
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("[echo-client] sounds.json parse failed for "
                    + pack.id() + ": " + exception.getMessage());
            return 0L;
        }
    }
}
