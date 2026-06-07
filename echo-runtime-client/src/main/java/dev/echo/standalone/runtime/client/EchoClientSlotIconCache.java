package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetEntry;
import dev.echo.standalone.runtime.assets.EchoAnimatedTexture;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockTextureResolution;
import dev.echo.standalone.runtime.assets.EchoItemTextureResolver;
import dev.echo.standalone.runtime.assets.EchoMissingTexture;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

final class EchoClientSlotIconCache {
    private static final boolean LOG_SLOT_ICON_UPLOADS = Boolean.getBoolean("echo.client.slotIconLog");

    private final Map<String, Integer> textures = new HashMap<>();
    private final ArrayDeque<QueuedIcon> queuedIcons = new ArrayDeque<>();
    private final Set<String> queuedIconKeys = new HashSet<>();
    private EchoMinecraftAssetResolver minecraftAssets;
    private EchoBlockTextureResolver blockTextureResolver;
    private EchoItemTextureResolver itemTextureResolver;
    private int lastPrewarmRequestCount;
    private int lastPrewarmLoadedCount;

    void setMinecraftAssets(EchoMinecraftAssetResolver minecraftAssets) {
        clear();
        this.minecraftAssets = minecraftAssets;
        this.blockTextureResolver = minecraftAssets == null ? null : new EchoBlockTextureResolver(minecraftAssets);
        this.itemTextureResolver = minecraftAssets == null ? null : new EchoItemTextureResolver(minecraftAssets);
    }

    int blockIcon(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return 0;
        }
        String cacheKey = blockCacheKey(block);
        Integer cached = textures.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        int textureId = loadResolvedBlockIcon(block);
        if (textureId == 0) {
            textureId = loadDirectBlockIcon(block);
        }
        textures.put(cacheKey, textureId);
        return textureId;
    }

    int itemIcon(String itemId) {
        return itemIcon(itemId, Map.of());
    }

    int itemIcon(String itemId, Map<String, Double> itemPredicates) {
        if (itemId == null || itemId.isBlank()) {
            return 0;
        }
        Map<String, Double> safePredicates = normalizePredicates(itemPredicates);
        String cacheKey = itemCacheKey(itemId, safePredicates);
        Integer cached = textures.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        int textureId = loadResolvedItemIcon(itemId, safePredicates);
        if (textureId == 0) {
            String[] parts = splitId(itemId.replace('\\', '/'));
            if (parts.length == 2) {
                textureId = loadTexture(parts[0], "item/" + parts[1]);
            }
        }
        textures.put(cacheKey, textureId);
        return textureId;
    }

    int cachedOrQueueBlockIcon(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return 0;
        }
        String cacheKey = blockCacheKey(block);
        Integer cached = textures.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        queueBlockIcon(block);
        return 0;
    }

    int cachedOrQueueItemIcon(String itemId, Map<String, Double> itemPredicates) {
        if (itemId == null || itemId.isBlank()) {
            return 0;
        }
        Map<String, Double> safePredicates = normalizePredicates(itemPredicates);
        String cacheKey = itemCacheKey(itemId, safePredicates);
        Integer cached = textures.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        queueItemIcon(itemId, safePredicates);
        return 0;
    }

    void queueSlotIcons(EchoClientInventoryScreenModel model) {
        if (model == null) {
            return;
        }
        for (EchoClientSlotStack slot : model.slots()) {
            queueSlotIcon(slot);
        }
    }

    void queueSlotIcons(EchoClientEquipmentScreenModel model) {
        if (model == null) {
            return;
        }
        for (EchoClientSlotStack slot : model.slots().values()) {
            queueSlotIcon(slot);
        }
        queueSlotIcon(model.offhandSlot());
    }

    void queueSlotIcons(EchoClientWorkbenchScreenModel model) {
        if (model == null || model.selectedRecipe() == null) {
            return;
        }
        for (EchoClientSlotStack slot : model.selectedRecipe().ingredients()) {
            queueSlotIcon(slot);
        }
        queueSlotIcon(model.selectedRecipe().output());
    }

    void queueSlotIcon(EchoClientSlotStack slot) {
        if (slot == null || slot.empty()) {
            return;
        }
        if (slot.blockSlot()) {
            queueBlockIcon(slot.block());
        } else if (slot.itemSlot()) {
            queueItemIcon(slot.runtimeId(), slot.itemModelPredicates());
        }
    }

    int prewarmQueuedIcons(int budget) {
        int safeBudget = Math.max(0, budget);
        int loaded = 0;
        int requests = 0;
        while (requests < safeBudget && !queuedIcons.isEmpty()) {
            QueuedIcon queued = queuedIcons.removeFirst();
            queuedIconKeys.remove(queued.cacheKey());
            if (textures.containsKey(queued.cacheKey())) {
                continue;
            }
            requests++;
            int before = textures.size();
            if (queued.block() != null) {
                blockIcon(queued.block());
            } else {
                itemIcon(queued.itemId(), queued.predicates());
            }
            if (textures.size() > before) {
                loaded++;
            }
        }
        lastPrewarmRequestCount = requests;
        lastPrewarmLoadedCount = loaded;
        return loaded;
    }

    int queuedIconCount() {
        return queuedIcons.size();
    }

    int cachedIconCount() {
        return textures.size();
    }

    int lastPrewarmRequestCount() {
        return lastPrewarmRequestCount;
    }

    int lastPrewarmLoadedCount() {
        return lastPrewarmLoadedCount;
    }

    EchoClientSlotIconPlan planItemIcon(String itemId) {
        return planItemIcon(itemId, Map.of());
    }

    EchoClientSlotIconPlan planItemIcon(String itemId, Map<String, Double> itemPredicates) {
        if (itemId == null || itemId.isBlank()) {
            return EchoClientSlotIconPlan.missing("item", "", "", "", "blank item id");
        }
        Map<String, Double> safePredicates = normalizePredicates(itemPredicates);
        if (itemTextureResolver != null) {
            try {
                EchoItemTextureResolver.EchoItemTextureLayerResolution resolution =
                        itemTextureResolver.resolveLayers(itemId, safePredicates);
                if (resolution.resolved()) {
                    EchoClientSlotIconPlan plan = planResolvedItemIcon(itemId, resolution.textureIds());
                    if (plan.resolved() || plan.missingTextureFallback()) {
                        return plan;
                    }
                } else if (resolution.missingReason().isPresent()) {
                    return EchoClientSlotIconPlan.missing(
                            "item",
                            itemId,
                            "",
                            "",
                            resolution.missingReason().orElseThrow()
                    );
                }
            } catch (IOException | IllegalArgumentException exception) {
                return EchoClientSlotIconPlan.missing("item", itemId, "", "", exception.getMessage());
            }
        }
        String[] parts = splitId(itemId.replace('\\', '/'));
        if (parts.length == 2) {
            return planTexture("item", itemId, parts[0], "item/" + parts[1]);
        }
        return EchoClientSlotIconPlan.missing("item", itemId, "", "", "invalid item id");
    }

    EchoClientSlotIconPlan planBlockIcon(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return EchoClientSlotIconPlan.missing("block", "", "", "", "blank block id");
        }
        if (blockTextureResolver != null) {
            try {
                EchoBlockTextureResolution resolution = blockTextureResolver.resolve(block.id());
                if (resolution.resolved()
                        && resolution.textureNamespace().isPresent()
                        && resolution.texturePath().isPresent()) {
                    EchoClientSlotIconPlan plan = planTexture(
                            "block",
                            block.id(),
                            resolution.textureNamespace().orElseThrow(),
                            resolution.texturePath().orElseThrow()
                    );
                    if (plan.resolved() || plan.missingTextureFallback()) {
                        return plan;
                    }
                }
            } catch (IOException | IllegalArgumentException exception) {
                return EchoClientSlotIconPlan.missing("block", block.id(), "", "", exception.getMessage());
            }
        }
        String atlasKey = block.atlasKey().replace('\\', '/');
        int slash = atlasKey.indexOf('/');
        String namespace = slash >= 0 ? atlasKey.substring(0, slash) : namespace(block.id());
        String path = slash >= 0 ? atlasKey.substring(slash + 1) : path(block.id());
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        if (!path.startsWith("block/")) {
            path = "block/" + path;
        }
        return planTexture("block", block.id(), namespace, path);
    }

    void clear() {
        for (int textureId : textures.values()) {
            if (textureId != 0) {
                GL11.glDeleteTextures(textureId);
            }
        }
        textures.clear();
        queuedIcons.clear();
        queuedIconKeys.clear();
        lastPrewarmRequestCount = 0;
        lastPrewarmLoadedCount = 0;
    }

    private void queueBlockIcon(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return;
        }
        String cacheKey = blockCacheKey(block);
        if (textures.containsKey(cacheKey) || !queuedIconKeys.add(cacheKey)) {
            return;
        }
        queuedIcons.addLast(new QueuedIcon(cacheKey, block, "", Map.of()));
    }

    private void queueItemIcon(String itemId, Map<String, Double> predicates) {
        if (itemId == null || itemId.isBlank()) {
            return;
        }
        Map<String, Double> safePredicates = normalizePredicates(predicates);
        String cacheKey = itemCacheKey(itemId, safePredicates);
        if (textures.containsKey(cacheKey) || !queuedIconKeys.add(cacheKey)) {
            return;
        }
        queuedIcons.addLast(new QueuedIcon(cacheKey, null, itemId, safePredicates));
    }

    private static String blockCacheKey(EchoVoxelBlock block) {
        return "block:" + block.id() + "|" + block.atlasKey();
    }

    private static String itemCacheKey(String itemId, Map<String, Double> itemPredicates) {
        return "item:" + itemId + predicateSignature(itemPredicates);
    }

    private int loadResolvedBlockIcon(EchoVoxelBlock block) {
        if (blockTextureResolver == null) {
            return 0;
        }
        try {
            EchoBlockTextureResolution resolution = blockTextureResolver.resolve(block.id());
            if (resolution.resolved()
                    && resolution.textureNamespace().isPresent()
                    && resolution.texturePath().isPresent()) {
                int textureId = loadTexture(resolution.textureNamespace().orElseThrow(), resolution.texturePath().orElseThrow());
                if (textureId != 0) {
                    return textureId;
                }
                System.out.println("[echo-client] slot block icon missing model texture for "
                        + block.id() + " -> " + resolution.textureId().orElse("<missing>"));
                return uploadMissingTexture("missing block icon " + block.id());
            }
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("[echo-client] slot block icon resolution failed for "
                    + block.id() + ": " + exception.getMessage());
        }
        return 0;
    }

    private int loadResolvedItemIcon(String itemId, Map<String, Double> itemPredicates) {
        if (itemTextureResolver == null) {
            return 0;
        }
        try {
            EchoItemTextureResolver.EchoItemTextureLayerResolution resolution =
                    itemTextureResolver.resolveLayers(itemId, itemPredicates);
            if (resolution.resolved()) {
                if (resolution.textureIds().size() > 1) {
                    int layeredTextureId = loadCompositeItemIcon(itemId, resolution.textureIds());
                    if (layeredTextureId != 0) {
                        return layeredTextureId;
                    }
                    return uploadMissingTexture("missing layered item icon " + itemId);
                }
                String textureIdValue = resolution.textureIds().get(0);
                String[] textureParts = splitId(textureIdValue);
                if (textureParts.length != 2) {
                    return 0;
                }
                int textureId = loadTexture(textureParts[0], textureParts[1]);
                if (textureId != 0) {
                    return textureId;
                }
                System.out.println("[echo-client] slot item icon missing model texture for "
                        + itemId + " -> " + textureIdValue);
                return uploadMissingTexture("missing item icon " + itemId);
            }
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("[echo-client] slot item icon resolution failed for "
                    + itemId + ": " + exception.getMessage());
        }
        return 0;
    }

    private int loadDirectBlockIcon(EchoVoxelBlock block) {
        String atlasKey = block.atlasKey().replace('\\', '/');
        int slash = atlasKey.indexOf('/');
        String namespace = slash >= 0 ? atlasKey.substring(0, slash) : namespace(block.id());
        String path = slash >= 0 ? atlasKey.substring(slash + 1) : path(block.id());
        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }
        if (!path.startsWith("block/")) {
            path = "block/" + path;
        }
        return loadTexture(namespace, path);
    }

    private EchoClientSlotIconPlan planResolvedItemIcon(String itemId, List<String> textureIds) {
        if (textureIds == null || textureIds.isEmpty()) {
            return EchoClientSlotIconPlan.missing("item", itemId, "", "", "item model has no resolved texture layers");
        }
        if (textureIds.size() == 1) {
            String[] textureParts = splitId(textureIds.get(0));
            if (textureParts.length == 2) {
                return planTexture("item", itemId, textureParts[0], textureParts[1]);
            }
            return EchoClientSlotIconPlan.missing("item", itemId, "", "", "invalid item texture id " + textureIds.get(0));
        }

        ArrayList<String> texturePaths = new ArrayList<>();
        ArrayList<String> sources = new ArrayList<>();
        String commonNamespace = "";
        boolean mixedNamespaces = false;
        boolean allResourcePack = true;
        for (String textureId : textureIds) {
            String[] textureParts = splitId(textureId);
            if (textureParts.length != 2) {
                return EchoClientSlotIconPlan.missing("item", itemId, "", "", "invalid item texture id " + textureId);
            }
            EchoClientSlotIconPlan layer = planTexture("item", itemId, textureParts[0], textureParts[1]);
            if (layer.missingTextureFallback()) {
                return EchoClientSlotIconPlan.missingTexture(
                        "item",
                        itemId,
                        layer.namespace(),
                        layer.texturePath(),
                        "missing layered item texture " + textureId + ": " + layer.detail()
                );
            }
            if (!layer.resolved()) {
                return EchoClientSlotIconPlan.missing(
                        "item",
                        itemId,
                        layer.namespace(),
                        layer.texturePath(),
                        "unresolved layered item texture " + textureId + ": " + layer.detail()
                );
            }
            if (commonNamespace.isBlank()) {
                commonNamespace = layer.namespace();
            } else if (!commonNamespace.equals(layer.namespace())) {
                mixedNamespaces = true;
            }
            allResourcePack = allResourcePack && "resource-pack".equals(layer.sourceKind());
            texturePaths.add(layer.texturePath());
            sources.add(layer.source());
        }

        return new EchoClientSlotIconPlan(
                "item",
                itemId,
                mixedNamespaces ? "mixed" : commonNamespace,
                String.join(",", texturePaths),
                String.join(",", sources),
                allResourcePack ? "resource-pack-layers" : "mixed-layers",
                true,
                false,
                "resolved " + textureIds.size() + " layered item textures"
        );
    }

    private int loadCompositeItemIcon(String itemId, List<String> textureIds) {
        ByteBuffer composited = ByteBuffer.allocateDirect(64 * 64 * 4);
        for (String textureId : textureIds) {
            String[] textureParts = splitId(textureId);
            if (textureParts.length != 2) {
                return 0;
            }
            TexturePixels layer = loadTexturePixels(textureParts[0], textureParts[1], 64, 64);
            if (layer == null) {
                System.out.println("[echo-client] slot layered item icon missing texture for "
                        + itemId + " -> " + textureId);
                return 0;
            }
            blendOver(composited, layer.pixels());
        }
        return uploadTexturePixels(composited, 64, 64,
                "composited item icon " + itemId + " layers=" + textureIds.size());
    }

    private int loadTexture(String namespace, String texturePath) {
        TexturePixels pixels = loadTexturePixels(namespace, texturePath, -1, -1);
        return pixels == null ? 0 : uploadTexturePixels(pixels.pixels(), pixels.width(), pixels.height(), pixels.source());
    }

    private TexturePixels loadTexturePixels(String namespace, String texturePath, int targetWidth, int targetHeight) {
        Optional<EchoAssetEntry> entry = resourcePackTexture(namespace, texturePath);
        if (entry.isPresent()) {
            try {
                return decodeTexturePath(
                        Files.readAllBytes(entry.get().file()),
                        entry.get().logicalId(),
                        namespace,
                        texturePath,
                        targetWidth,
                        targetHeight
                );
            } catch (IOException exception) {
                System.out.println("[echo-client] slot icon read failed for "
                        + entry.get().logicalId() + ": " + exception.getMessage());
            }
        }

        String resourcePath = "/assets/" + namespace + "/textures/" + texturePath + ".png";
        try (InputStream in = EchoClientSlotIconCache.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                return decodeTexturePath(in.readAllBytes(), resourcePath, namespace, texturePath, targetWidth, targetHeight);
            }
        } catch (IOException exception) {
            System.out.println("[echo-client] slot classpath icon read failed for "
                    + resourcePath + ": " + exception.getMessage());
        }
        return null;
    }

    private TexturePixels decodeTexturePath(
            byte[] bytes,
            String source,
            String namespace,
            String texturePath,
            int targetWidth,
            int targetHeight
    ) {
        TexturePixels decoded = decodeTexture(bytes, source, -1, -1);
        if (decoded == null) {
            return null;
        }
        TexturePixels frame = firstAnimationFrame(namespace, texturePath, decoded);
        if (targetWidth <= 0 || targetHeight <= 0
                || (frame.width() == targetWidth && frame.height() == targetHeight)) {
            return frame;
        }
        return new TexturePixels(
                scalePixels(frame.pixels(), frame.width(), frame.height(), targetWidth, targetHeight),
                targetWidth,
                targetHeight,
                frame.source()
        );
    }

    private TexturePixels firstAnimationFrame(String namespace, String texturePath, TexturePixels texture) {
        if (minecraftAssets == null || texture == null
                || texture.width() <= 0
                || texture.height() <= texture.width()
                || texture.height() % texture.width() != 0) {
            return texture;
        }
        try {
            EchoAnimatedTexture animation = EchoAnimatedTexture.load(minecraftAssets, namespace, texturePath);
            if (!animation.animated()) {
                return texture;
            }
            int frameHeight = texture.width();
            int frameCount = texture.height() / frameHeight;
            int requestedFrame = animation.frames().isEmpty()
                    ? 0
                    : animation.frames().get(0).index();
            int frameIndex = Math.max(0, Math.min(frameCount - 1, requestedFrame));
            ByteBuffer frame = ByteBuffer.allocateDirect(texture.width() * frameHeight * 4);
            int sourceOffsetY = frameIndex * frameHeight;
            for (int y = 0; y < frameHeight; y++) {
                int sourceRowStart = ((sourceOffsetY + y) * texture.width()) * 4;
                for (int x = 0; x < texture.width() * 4; x++) {
                    frame.put(texture.pixels().get(sourceRowStart + x));
                }
            }
            frame.flip();
            return new TexturePixels(
                    frame,
                    texture.width(),
                    frameHeight,
                    texture.source() + " frame=" + frameIndex
            );
        } catch (IOException | IllegalArgumentException exception) {
            return texture;
        }
    }

    private EchoClientSlotIconPlan planTexture(String kind, String targetId, String namespace, String texturePath) {
        Optional<EchoAssetEntry> entry = resourcePackTexture(namespace, texturePath);
        if (entry.isPresent()) {
            return EchoClientSlotIconPlan.resolved(
                    kind,
                    targetId,
                    namespace,
                    texturePath,
                    entry.get().logicalId(),
                    "resource-pack"
            );
        }
        String resourcePath = "/assets/" + namespace + "/textures/" + texturePath + ".png";
        try (InputStream in = EchoClientSlotIconCache.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                return EchoClientSlotIconPlan.resolved(
                        kind,
                        targetId,
                        namespace,
                        texturePath,
                        resourcePath,
                        "classpath"
                );
            }
        } catch (IOException exception) {
            return EchoClientSlotIconPlan.missing(kind, targetId, namespace, texturePath, exception.getMessage());
        }
        return EchoClientSlotIconPlan.missingTexture(
                kind,
                targetId,
                namespace,
                texturePath,
                "missing " + namespace + ":textures/" + texturePath + ".png"
        );
    }

    private Optional<EchoAssetEntry> resourcePackTexture(String namespace, String texturePath) {
        if (minecraftAssets == null) {
            return Optional.empty();
        }
        return minecraftAssets.texture(namespace, texturePath);
    }

    private static int uploadTexture(byte[] bytes, String source) {
        TexturePixels decoded = decodeTexture(bytes, source, -1, -1);
        if (decoded == null) {
            return 0;
        }
        return uploadTexturePixels(decoded.pixels(), decoded.width(), decoded.height(), source);
    }

    private static TexturePixels decodeTexture(byte[] bytes, String source, int targetWidth, int targetHeight) {
        ByteBuffer encoded = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(encoded, w, h, channels, 4);
            if (pixels == null) {
                return null;
            }
            int sourceWidth = w.get(0);
            int sourceHeight = h.get(0);
            int outWidth = targetWidth > 0 ? targetWidth : sourceWidth;
            int outHeight = targetHeight > 0 ? targetHeight : sourceHeight;
            ByteBuffer copied = sourceWidth == outWidth && sourceHeight == outHeight
                    ? copyPixels(pixels, sourceWidth, sourceHeight)
                    : scalePixels(pixels, sourceWidth, sourceHeight, outWidth, outHeight);
            STBImage.stbi_image_free(pixels);
            return new TexturePixels(copied, outWidth, outHeight, source);
        }
    }

    private static int uploadTexturePixels(ByteBuffer pixels, int width, int height, String source) {
        if (pixels == null || width <= 0 || height <= 0) {
            return 0;
        }
        pixels.rewind();
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        logSlotIconUpload(source + " (" + width + "x" + height + ")");
        return textureId;
    }

    private static ByteBuffer copyPixels(ByteBuffer source, int width, int height) {
        ByteBuffer copy = ByteBuffer.allocateDirect(width * height * 4);
        for (int index = 0; index < width * height * 4; index++) {
            copy.put(source.get(index));
        }
        copy.flip();
        return copy;
    }

    private static ByteBuffer scalePixels(ByteBuffer source, int sourceWidth, int sourceHeight, int width, int height) {
        ByteBuffer scaled = ByteBuffer.allocateDirect(width * height * 4);
        for (int y = 0; y < height; y++) {
            int sourceY = y * sourceHeight / height;
            for (int x = 0; x < width; x++) {
                int sourceX = x * sourceWidth / width;
                int sourceIndex = (sourceY * sourceWidth + sourceX) * 4;
                scaled.put(source.get(sourceIndex));
                scaled.put(source.get(sourceIndex + 1));
                scaled.put(source.get(sourceIndex + 2));
                scaled.put(source.get(sourceIndex + 3));
            }
        }
        scaled.flip();
        return scaled;
    }

    private static void blendOver(ByteBuffer target, ByteBuffer source) {
        int pixels = Math.min(target.capacity(), source.capacity()) / 4;
        for (int pixel = 0; pixel < pixels; pixel++) {
            int offset = pixel * 4;
            int sr = unsigned(source.get(offset));
            int sg = unsigned(source.get(offset + 1));
            int sb = unsigned(source.get(offset + 2));
            int sa = unsigned(source.get(offset + 3));
            if (sa == 0) {
                continue;
            }

            int dr = unsigned(target.get(offset));
            int dg = unsigned(target.get(offset + 1));
            int db = unsigned(target.get(offset + 2));
            int da = unsigned(target.get(offset + 3));
            float sourceAlpha = sa / 255.0f;
            float destAlpha = da / 255.0f;
            float outAlpha = sourceAlpha + destAlpha * (1.0f - sourceAlpha);
            if (outAlpha <= 0.0f) {
                target.put(offset, (byte) 0);
                target.put(offset + 1, (byte) 0);
                target.put(offset + 2, (byte) 0);
                target.put(offset + 3, (byte) 0);
                continue;
            }

            int outR = Math.round((sr * sourceAlpha + dr * destAlpha * (1.0f - sourceAlpha)) / outAlpha);
            int outG = Math.round((sg * sourceAlpha + dg * destAlpha * (1.0f - sourceAlpha)) / outAlpha);
            int outB = Math.round((sb * sourceAlpha + db * destAlpha * (1.0f - sourceAlpha)) / outAlpha);
            target.put(offset, (byte) clamp(outR));
            target.put(offset + 1, (byte) clamp(outG));
            target.put(offset + 2, (byte) clamp(outB));
            target.put(offset + 3, (byte) clamp(Math.round(outAlpha * 255.0f)));
        }
    }

    private static int uploadMissingTexture(String source) {
        byte[] pixels = EchoMissingTexture.rgbaChecker(64);
        ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length).put(pixels).flip();
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 64, 64,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        logSlotIconUpload(source + " (missing texture)");
        return textureId;
    }

    private static void logSlotIconUpload(String message) {
        if (LOG_SLOT_ICON_UPLOADS) {
            System.out.println("[echo-client] loaded slot icon: " + message);
        }
    }

    private static String namespace(String id) {
        String[] parts = splitId(id);
        return parts.length == 2 ? parts[0] : "minecraft";
    }

    private static String path(String id) {
        String[] parts = splitId(id);
        return parts.length == 2 ? parts[1] : id;
    }

    private static String[] splitId(String id) {
        int separator = id.indexOf(':');
        if (separator < 1 || separator == id.length() - 1) {
            return new String[0];
        }
        return new String[]{id.substring(0, separator), id.substring(separator + 1)};
    }

    private static Map<String, Double> normalizePredicates(Map<String, Double> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, Double> normalized = new TreeMap<>();
        for (Map.Entry<String, Double> entry : predicates.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || !Double.isFinite(entry.getValue())) {
                continue;
            }
            String key = entry.getKey().replace('\\', '/').trim().toLowerCase(java.util.Locale.ROOT);
            if (!key.isBlank()) {
                normalized.put(key, entry.getValue());
            }
        }
        return Map.copyOf(normalized);
    }

    private static String predicateSignature(Map<String, Double> predicates) {
        if (predicates == null || predicates.isEmpty()) {
            return "";
        }
        StringBuilder signature = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, Double> entry : new TreeMap<>(predicates).entrySet()) {
            if (!first) {
                signature.append(',');
            }
            signature.append(entry.getKey()).append('=')
                    .append(Math.round(entry.getValue() * 10000.0D) / 10000.0D);
            first = false;
        }
        return signature.append(']').toString();
    }

    private static int unsigned(byte value) {
        return value & 0xFF;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private record QueuedIcon(
            String cacheKey,
            EchoVoxelBlock block,
            String itemId,
            Map<String, Double> predicates
    ) {
        private QueuedIcon {
            cacheKey = cacheKey == null ? "" : cacheKey;
            itemId = itemId == null ? "" : itemId;
            predicates = predicates == null ? Map.of() : Map.copyOf(predicates);
        }
    }

    private record TexturePixels(ByteBuffer pixels, int width, int height, String source) {
        private TexturePixels {
            if (pixels == null) {
                throw new IllegalArgumentException("pixels must not be null");
            }
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("texture dimensions must be positive");
            }
            source = source == null ? "" : source;
        }
    }

    record EchoClientSlotIconPlan(
            String kind,
            String targetId,
            String namespace,
            String texturePath,
            String source,
            String sourceKind,
            boolean resolved,
            boolean missingTextureFallback,
            String detail
    ) {
        EchoClientSlotIconPlan {
            kind = kind == null ? "" : kind;
            targetId = targetId == null ? "" : targetId;
            namespace = namespace == null ? "" : namespace;
            texturePath = texturePath == null ? "" : texturePath;
            source = source == null ? "" : source;
            sourceKind = sourceKind == null ? "" : sourceKind;
            detail = detail == null ? "" : detail;
        }

        static EchoClientSlotIconPlan resolved(
                String kind,
                String targetId,
                String namespace,
                String texturePath,
                String source,
                String sourceKind
        ) {
            return new EchoClientSlotIconPlan(
                    kind,
                    targetId,
                    namespace,
                    texturePath,
                    source,
                    sourceKind,
                    true,
                    false,
                    "resolved"
            );
        }

        static EchoClientSlotIconPlan missingTexture(
                String kind,
                String targetId,
                String namespace,
                String texturePath,
                String detail
        ) {
            return new EchoClientSlotIconPlan(
                    kind,
                    targetId,
                    namespace,
                    texturePath,
                    EchoMissingTexture.LOGICAL_ID,
                    "missing-texture",
                    false,
                    true,
                    detail
            );
        }

        static EchoClientSlotIconPlan missing(
                String kind,
                String targetId,
                String namespace,
                String texturePath,
                String detail
        ) {
            return new EchoClientSlotIconPlan(
                    kind,
                    targetId,
                    namespace,
                    texturePath,
                    "",
                    "",
                    false,
                    false,
                    detail
            );
        }
    }
}
