package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetEntry;
import dev.echo.standalone.runtime.assets.EchoAnimatedTexture;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelElement;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelFaceUv;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelBounds;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockTextureResolution;
import dev.echo.standalone.runtime.assets.EchoMissingTexture;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.render.EchoVoxelMeshDirection;
import dev.echo.standalone.runtime.render.EchoVoxelMeshFace;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

/**
 * Builds a single OpenGL texture atlas from block material atlasKeys.
 * Attempts to load PNG from classpath; falls back to procedural color textures.
 */
final class EchoClientTextureAtlas {
    private static final int TEX_SIZE = 64; // size of each tile
    private int textureId;
    private final Map<String, AtlasEntry> entries = new HashMap<>();
    private int atlasWidth;
    private int atlasHeight;
    private EchoMinecraftAssetResolver minecraftAssets;
    private EchoBlockTextureResolver blockTextureResolver;
    private final Map<String, String> faceAtlasKeys = new HashMap<>();
    private final Map<String, EchoBlockModelFaceUv> faceUvRects = new HashMap<>();
    private final Map<String, Integer> faceUvRotations = new HashMap<>();
    private final Map<String, Integer> faceTintIndices = new HashMap<>();
    private final Map<String, EchoBlockModelBounds> modelBoundsByBlockId = new HashMap<>();
    private final Map<String, List<EchoBlockModelBounds>> modelElementsByBlockId = new HashMap<>();
    private final Map<String, List<EchoBlockModelElement>> modelElementDefinitionsByBlockId = new HashMap<>();
    private final Map<String, String> modelTemplatesByBlockId = new HashMap<>();
    private final Map<String, Integer> modelXRotationsByBlockId = new HashMap<>();
    private final Map<String, Integer> modelYRotationsByBlockId = new HashMap<>();
    private final Map<String, EchoBlockTextureResolution> blockTextureResolutionsByLookupKey = new HashMap<>();
    private final Map<String, ByteBuffer> resourcePackTilesByLogicalId = new HashMap<>();
    private final Set<String> loggedModelTextureBlocks = new HashSet<>();
    private int blockTextureResolutionCacheHitCount;
    private int resourcePackTileDecodeCount;
    private int resourcePackTileCacheHitCount;
    private int lastRemovedBaseAtlasRequestCount;

    record AtlasEntry(float u1, float v1, float u2, float v2) {}

    void setMinecraftAssets(EchoMinecraftAssetResolver minecraftAssets) {
        this.minecraftAssets = minecraftAssets;
        this.blockTextureResolver = minecraftAssets == null ? null : new EchoBlockTextureResolver(minecraftAssets);
        blockTextureResolutionsByLookupKey.clear();
        resourcePackTilesByLogicalId.clear();
        loggedModelTextureBlocks.clear();
        blockTextureResolutionCacheHitCount = 0;
        resourcePackTileDecodeCount = 0;
        resourcePackTileCacheHitCount = 0;
    }

    public void build(Map<String, Integer> materialColors, Map<String, String> atlasKeys) {
        build(materialColors, atlasKeys, Map.of());
    }

    public void build(
            Map<String, Integer> materialColors,
            Map<String, String> atlasKeys,
            Map<String, String> blockIdsByAtlasKey
    ) {
        build(materialColors, atlasKeys, blockIdsByAtlasKey, blockIdsByAtlasKey.entrySet().stream()
                .map(entry -> new BlockModelRequest(entry.getValue(), Map.of(), entry.getKey()))
                .toList());
    }

    public void build(
            Map<String, Integer> materialColors,
            Map<String, String> atlasKeys,
            Map<String, String> blockIdsByAtlasKey,
            java.util.List<BlockModelRequest> blockModelRequests
    ) {
        LinkedHashMap<String, TileRequest> requests =
                planTileRequests(materialColors, atlasKeys, blockIdsByAtlasKey, blockModelRequests);

        int count = requests.size();
        if (count == 0) count = 1;
        int cols = (int) Math.ceil(Math.sqrt(count));
        int rows = (int) Math.ceil((double) count / cols);
        atlasWidth = cols * TEX_SIZE;
        atlasHeight = rows * TEX_SIZE;

        ByteBuffer atlasPixels = ByteBuffer.allocateDirect(atlasWidth * atlasHeight * 4);

        int index = 0;
        for (TileRequest request : requests.values()) {
            int col = index % cols;
            int row = index / cols;
            ByteBuffer tile = loadOrGenerateTile(
                    request.atlasKey(),
                    request.blockId(),
                    request.argb(),
                    request.textureId()
            );
            blitTile(atlasPixels, atlasWidth, atlasHeight, col * TEX_SIZE, row * TEX_SIZE, tile);
            float u1 = (col * TEX_SIZE) / (float) atlasWidth;
            float v1 = (row * TEX_SIZE) / (float) atlasHeight;
            float u2 = ((col + 1) * TEX_SIZE) / (float) atlasWidth;
            float v2 = ((row + 1) * TEX_SIZE) / (float) atlasHeight;
            entries.put(request.atlasKey(), new AtlasEntry(u1, v1, u2, v2));
            index++;
        }

        textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, atlasWidth, atlasHeight,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, atlasPixels);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public AtlasEntry get(String atlasKey) {
        return entries.getOrDefault(atlasKey,
                new AtlasEntry(0.0f, 0.0f, 1.0f, 1.0f));
    }

    public AtlasEntry get(EchoVoxelMeshFace face) {
        return face == null ? get("") : get(face, face.direction());
    }

    AtlasEntry get(EchoVoxelMeshFace face, EchoVoxelMeshDirection direction) {
        if (face == null) {
            return get("");
        }
        String blockId = blockId(face);
        EchoVoxelMeshDirection safeDirection = direction == null ? face.direction() : direction;
        if (!blockId.isBlank()) {
            String faceAtlasKey = faceAtlasKeys.get(faceLookupKey(
                    blockId,
                    face.material().stateProperties(),
                    safeDirection
            ));
            if (faceAtlasKey == null) {
                faceAtlasKey = faceAtlasKeys.get(faceLookupKey(blockId, Map.of(), safeDirection));
            }
            if (faceAtlasKey != null) {
                return applyFaceUv(get(faceAtlasKey), faceUvRect(blockId, face.material().stateProperties(), safeDirection));
            }
        }
        return applyFaceUv(get(face.material().atlasKey()), faceUvRect(blockId, face.material().stateProperties(), safeDirection));
    }

    AtlasEntry get(EchoVoxelMeshFace face, EchoVoxelMeshDirection direction, EchoBlockModelElement element) {
        if (face == null || element == null) {
            return get(face, direction);
        }
        EchoVoxelMeshDirection safeDirection = direction == null ? face.direction() : direction;
        EchoVoxelMeshDirection modelDirection = modelFaceForElementMetadata(face, safeDirection, element);
        String faceName = faceName(modelDirection);
        EchoBlockModelFaceUv uv = element.uvRectForFace(faceName).orElse(null);
        Optional<String> textureId = element.textureIdForFace(faceName);
        if (textureId.isPresent()) {
            return applyFaceUv(get(textureAtlasKey(textureId.get())), uv);
        }
        if (uv != null) {
            return applyFaceUv(get(face.material().atlasKey()), uv);
        }
        return get(face, safeDirection);
    }

    int uvRotationDegrees(EchoVoxelMeshFace face, EchoVoxelMeshDirection direction) {
        if (face == null) {
            return 0;
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return 0;
        }
        EchoVoxelMeshDirection safeDirection = direction == null ? face.direction() : direction;
        String lookupKey = faceLookupKey(blockId, face.material().stateProperties(), safeDirection);
        Integer rotation = faceUvRotations.get(lookupKey);
        if (rotation == null) {
            rotation = faceUvRotations.get(faceLookupKey(blockId, Map.of(), safeDirection));
        }
        if (rotation == null) {
            rotation = resolveFaceUvRotation(blockId, face.material().stateProperties(), safeDirection);
            if (rotation != 0) {
                faceUvRotations.put(lookupKey, rotation);
            }
        }
        return rotation == null ? 0 : rotation;
    }

    int uvRotationDegrees(EchoVoxelMeshFace face, EchoVoxelMeshDirection direction, EchoBlockModelElement element) {
        if (element == null) {
            return uvRotationDegrees(face, direction);
        }
        EchoVoxelMeshDirection safeDirection = direction == null
                ? (face == null ? EchoVoxelMeshDirection.UP : face.direction())
                : direction;
        EchoVoxelMeshDirection modelDirection = modelFaceForElementMetadata(face, safeDirection, element);
        int rotation = element.uvRotationDegreesForFace(faceName(modelDirection));
        return rotation != 0 ? rotation : uvRotationDegrees(face, safeDirection);
    }

    int tintIndex(EchoVoxelMeshFace face, EchoVoxelMeshDirection direction) {
        if (face == null) {
            return -1;
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return -1;
        }
        EchoVoxelMeshDirection safeDirection = direction == null ? face.direction() : direction;
        String lookupKey = faceLookupKey(blockId, face.material().stateProperties(), safeDirection);
        Integer tintIndex = faceTintIndices.get(lookupKey);
        if (tintIndex == null) {
            tintIndex = faceTintIndices.get(faceLookupKey(blockId, Map.of(), safeDirection));
        }
        if (tintIndex == null) {
            Optional<Integer> resolved = resolveFaceTintIndex(blockId, face.material().stateProperties(), safeDirection);
            if (resolved.isPresent()) {
                tintIndex = resolved.get();
                faceTintIndices.put(lookupKey, tintIndex);
            }
        }
        return tintIndex == null ? -1 : tintIndex;
    }

    int tintIndex(EchoVoxelMeshFace face, EchoVoxelMeshDirection direction, EchoBlockModelElement element) {
        if (element == null) {
            return tintIndex(face, direction);
        }
        EchoVoxelMeshDirection safeDirection = direction == null
                ? (face == null ? EchoVoxelMeshDirection.UP : face.direction())
                : direction;
        EchoVoxelMeshDirection modelDirection = modelFaceForElementMetadata(face, safeDirection, element);
        Optional<Integer> tintIndex = element.tintIndexForFace(faceName(modelDirection));
        return tintIndex.orElseGet(() -> tintIndex(face, safeDirection));
    }

    EchoVoxelMeshDirection modelFaceForElementMetadata(
            EchoVoxelMeshFace face,
            EchoVoxelMeshDirection worldDirection,
            EchoBlockModelElement element
    ) {
        EchoVoxelMeshDirection safeDirection = worldDirection == null
                ? (face == null ? EchoVoxelMeshDirection.UP : face.direction())
                : worldDirection;
        if (element == null || element.rotation().isPresent()) {
            return safeDirection;
        }
        int yRotation = face == null ? 0 : modelYRotationDegrees(face);
        int xRotation = face == null ? 0 : modelXRotationDegrees(face);
        return modelFaceForBlockRotation(safeDirection, xRotation, yRotation);
    }

    private static EchoVoxelMeshDirection modelFaceForBlockRotation(
            EchoVoxelMeshDirection worldDirection,
            int xRotationDegrees,
            int yRotationDegrees
    ) {
        EchoVoxelMeshDirection safeDirection = worldDirection == null ? EchoVoxelMeshDirection.UP : worldDirection;
        return inverseRotateFaceX(inverseRotateFaceY(safeDirection, yRotationDegrees), xRotationDegrees);
    }

    EchoVoxelMeshDirection worldFaceForElementCullFace(
            EchoVoxelMeshFace face,
            EchoVoxelMeshDirection modelDirection,
            EchoBlockModelElement element
    ) {
        EchoVoxelMeshDirection safeDirection = modelDirection == null ? EchoVoxelMeshDirection.UP : modelDirection;
        if (element == null || element.rotation().isPresent()) {
            return safeDirection;
        }
        int xRotation = face == null ? 0 : modelXRotationDegrees(face);
        int yRotation = face == null ? 0 : modelYRotationDegrees(face);
        return rotateFaceY(rotateFaceX(safeDirection, xRotation), yRotation);
    }

    EchoBlockModelBounds modelBounds(EchoVoxelMeshFace face) {
        if (face == null) {
            return EchoBlockModelBounds.fullCube();
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return EchoBlockModelBounds.fullCube();
        }
        String lookupKey = blockLookupKey(blockId, face.material().stateProperties());
        return modelBoundsByBlockId.computeIfAbsent(
                lookupKey,
                ignored -> resolveModelBounds(blockId, face.material().stateProperties())
        );
    }

    List<EchoBlockModelBounds> modelElements(EchoVoxelMeshFace face) {
        if (face == null) {
            return List.of();
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return List.of();
        }
        String lookupKey = blockLookupKey(blockId, face.material().stateProperties());
        return modelElementsByBlockId.computeIfAbsent(
                lookupKey,
                ignored -> resolveModelElements(blockId, face.material().stateProperties())
        );
    }

    List<EchoBlockModelElement> modelElementDefinitions(EchoVoxelMeshFace face) {
        if (face == null) {
            return List.of();
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return List.of();
        }
        String lookupKey = blockLookupKey(blockId, face.material().stateProperties());
        return modelElementDefinitionsByBlockId.computeIfAbsent(
                lookupKey,
                ignored -> resolveModelElementDefinitions(blockId, face.material().stateProperties())
        );
    }

    String modelTemplateKind(EchoVoxelMeshFace face) {
        if (face == null) {
            return "";
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return "";
        }
        String lookupKey = blockLookupKey(blockId, face.material().stateProperties());
        return modelTemplatesByBlockId.computeIfAbsent(
                lookupKey,
                ignored -> resolveModelTemplateKind(blockId, face.material().stateProperties())
        );
    }

    int modelXRotationDegrees(EchoVoxelMeshFace face) {
        if (face == null) {
            return 0;
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return 0;
        }
        String lookupKey = blockLookupKey(blockId, face.material().stateProperties());
        return modelXRotationsByBlockId.computeIfAbsent(
                lookupKey,
                ignored -> resolveModelXRotation(blockId, face.material().stateProperties())
        );
    }

    int modelYRotationDegrees(EchoVoxelMeshFace face) {
        if (face == null) {
            return 0;
        }
        String blockId = blockId(face);
        if (blockId.isBlank()) {
            return 0;
        }
        String lookupKey = blockLookupKey(blockId, face.material().stateProperties());
        return modelYRotationsByBlockId.computeIfAbsent(
                lookupKey,
                ignored -> resolveModelYRotation(blockId, face.material().stateProperties())
        );
    }

    boolean crossModel(EchoVoxelMeshFace face) {
        String templateKind = modelTemplateKind(face);
        return "cross".equals(templateKind) || "tinted_cross".equals(templateKind);
    }

    boolean stairModel(EchoVoxelMeshFace face) {
        String templateKind = modelTemplateKind(face);
        return "stairs".equals(templateKind)
                || "inner_stairs".equals(templateKind)
                || "outer_stairs".equals(templateKind);
    }

    boolean wallModel(EchoVoxelMeshFace face) {
        String templateKind = modelTemplateKind(face);
        return "wall_post".equals(templateKind)
                || "wall_side".equals(templateKind)
                || "wall_side_tall".equals(templateKind)
                || "wall_inventory".equals(templateKind);
    }

    boolean fenceModel(EchoVoxelMeshFace face) {
        String templateKind = modelTemplateKind(face);
        return "fence_post".equals(templateKind)
                || "fence_side".equals(templateKind)
                || "fence_inventory".equals(templateKind);
    }

    boolean paneModel(EchoVoxelMeshFace face) {
        String templateKind = modelTemplateKind(face);
        return "pane_post".equals(templateKind)
                || "pane_side".equals(templateKind)
                || "pane_side_alt".equals(templateKind)
                || "pane_noside".equals(templateKind)
                || "pane_noside_alt".equals(templateKind);
    }

    boolean trapdoorModel(EchoVoxelMeshFace face) {
        String templateKind = modelTemplateKind(face);
        return "trapdoor_bottom".equals(templateKind)
                || "trapdoor_top".equals(templateKind)
                || "trapdoor_open".equals(templateKind);
    }

    boolean doorModel(EchoVoxelMeshFace face) {
        return modelTemplateKind(face).startsWith("door_");
    }

    public int textureId() {
        return textureId;
    }

    int cachedBlockTextureResolutionCount() {
        return blockTextureResolutionsByLookupKey.size();
    }

    int cachedResourcePackTileCount() {
        return resourcePackTilesByLogicalId.size();
    }

    int blockTextureResolutionCacheHitCount() {
        return blockTextureResolutionCacheHitCount;
    }

    int resourcePackTileDecodeCount() {
        return resourcePackTileDecodeCount;
    }

    int resourcePackTileCacheHitCount() {
        return resourcePackTileCacheHitCount;
    }

    int lastRemovedBaseAtlasRequestCount() {
        return lastRemovedBaseAtlasRequestCount;
    }

    int plannedAtlasTileCount(
            Map<String, Integer> materialColors,
            Map<String, String> atlasKeys,
            Map<String, String> blockIdsByAtlasKey,
            java.util.List<BlockModelRequest> blockModelRequests
    ) {
        return planTileRequests(materialColors, atlasKeys, blockIdsByAtlasKey, blockModelRequests).size();
    }

    public void delete() {
        if (textureId != 0) {
            GL11.glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    ByteBuffer loadOrGenerateTile(String atlasKey, String blockId, int argb, String textureId) {
        ByteBuffer explicitTile = loadTextureId(textureId);
        if (explicitTile != null) {
            return explicitTile;
        }
        if (textureId != null && !textureId.isBlank()) {
            System.out.println("[echo-client] missing model texture: " + textureId);
            return missingTextureTile();
        }

        ByteBuffer modelTile = loadModelResolvedTexture(blockId);
        if (modelTile != null) {
            return modelTile;
        }

        // atlasKey is like "modid/block_name"
        // Real mod asset paths are: /assets/{namespace}/textures/block/{name}.png
        int slash = atlasKey.indexOf('/');
        String namespace = slash >= 0 ? atlasKey.substring(0, slash) : atlasKey;
        String name = slash >= 0 ? atlasKey.substring(slash + 1) : atlasKey;
        // Some atlasKeys already include "block/" prefix (e.g. from registry-loaded blocks)
        if (name.startsWith("block/")) {
            name = name.substring("block/".length());
        }

        // Try primary path, then known fallback aliases
        String[] namesToTry = {name, resolveTextureAlias(name)};
        for (String tryName : namesToTry) {
            if (tryName == null) continue;
            ByteBuffer packTile = loadResourcePackTexture(namespace, tryName);
            if (packTile != null) {
                return packTile;
            }
            String blockPath = "/assets/" + namespace + "/textures/block/" + tryName + ".png";
            boolean found = false;
            try (InputStream in = EchoClientTextureAtlas.class.getResourceAsStream(blockPath)) {
                if (in != null) {
                    found = true;
                    byte[] bytes = in.readAllBytes();
                    ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        IntBuffer w = stack.mallocInt(1);
                        IntBuffer h = stack.mallocInt(1);
                        IntBuffer channels = stack.mallocInt(1);
                        ByteBuffer pixels = STBImage.stbi_load_from_memory(buf, w, h, channels, 4);
                        if (pixels != null) {
                            System.out.println("[echo-client] loaded texture: " + blockPath + " (" + w.get(0) + "x" + h.get(0) + ")");
                            ByteBuffer scaled = scaleTo64(pixels, w.get(0), h.get(0));
                            STBImage.stbi_image_free(pixels);
                            return scaled;
                        }
                    }
                }
            } catch (IOException ignored) {}
            if (!found) {
                System.out.println("[echo-client] texture not found: " + blockPath);
            }
        }

        System.out.println("[echo-client] procedural fallback for: " + atlasKey);
        return generateProceduralTile(argb, atlasKey);
    }

    private void addModelFaceRequests(
            LinkedHashMap<String, TileRequest> requests,
            Map<String, Integer> materialColors,
            Map<String, String> blockIdsByAtlasKey,
            java.util.List<BlockModelRequest> blockModelRequests
    ) {
        if (blockTextureResolver == null) {
            return;
        }
        java.util.List<BlockModelRequest> requestsToResolve = blockModelRequests == null || blockModelRequests.isEmpty()
                ? blockIdsByAtlasKey.entrySet().stream()
                        .map(entry -> new BlockModelRequest(entry.getValue(), Map.of(), entry.getKey()))
                        .toList()
                : blockModelRequests;
        java.util.HashSet<String> resolved = new java.util.HashSet<>();
        for (BlockModelRequest request : requestsToResolve) {
            String baseAtlasKey = request.baseAtlasKey();
            String blockId = request.blockId();
            if (blockId == null || blockId.isBlank()) {
                continue;
            }
            String blockLookupKey = blockLookupKey(blockId, request.stateProperties());
            if (!resolved.add(blockLookupKey)) {
                continue;
            }
            BlockRenderPlan plan = planBlockModel(request);
            if (!plan.resolved()) {
                continue;
            }
            removeCoveredBaseAtlasRequest(requests, baseAtlasKey, blockId, plan);
            modelBoundsByBlockId.put(blockLookupKey, plan.modelBounds());
            modelElementsByBlockId.put(blockLookupKey, plan.modelElements());
            modelElementDefinitionsByBlockId.put(blockLookupKey, plan.modelElementDefinitions());
            modelTemplatesByBlockId.put(blockLookupKey, plan.templateKind());
            modelXRotationsByBlockId.put(blockLookupKey, plan.xRotationDegrees());
            modelYRotationsByBlockId.put(blockLookupKey, plan.yRotationDegrees());
            for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
                int uvRotationDegrees = plan.uvRotationDegrees(direction);
                if (uvRotationDegrees != 0) {
                    faceUvRotations.put(faceLookupKey(blockId, request.stateProperties(), direction), uvRotationDegrees);
                }
                plan.uvRect(direction).ifPresent(uv ->
                        faceUvRects.put(faceLookupKey(blockId, request.stateProperties(), direction), uv));
                plan.tintIndex(direction).ifPresent(tintIndex ->
                        faceTintIndices.put(faceLookupKey(blockId, request.stateProperties(), direction), tintIndex));
                plan.textureId(direction).ifPresent(textureId -> {
                    String atlasKey = plan.atlasKey(direction).orElseGet(() -> textureAtlasKey(textureId));
                    requests.putIfAbsent(atlasKey, new TileRequest(
                            atlasKey,
                            blockId,
                            materialColors.getOrDefault(baseAtlasKey, 0xFFFFFFFF),
                            textureId
                    ));
                    faceAtlasKeys.put(faceLookupKey(blockId, request.stateProperties(), direction), atlasKey);
                });
            }
            for (EchoBlockModelElement element : plan.modelElementDefinitions()) {
                for (String textureId : element.textureIdsByFace().values()) {
                    String atlasKey = textureAtlasKey(textureId);
                    requests.putIfAbsent(atlasKey, new TileRequest(
                            atlasKey,
                            blockId,
                            materialColors.getOrDefault(baseAtlasKey, 0xFFFFFFFF),
                            textureId
                    ));
                }
            }
        }
    }

    private LinkedHashMap<String, TileRequest> planTileRequests(
            Map<String, Integer> materialColors,
            Map<String, String> atlasKeys,
            Map<String, String> blockIdsByAtlasKey,
            java.util.List<BlockModelRequest> blockModelRequests
    ) {
        entries.clear();
        faceAtlasKeys.clear();
        faceUvRects.clear();
        faceUvRotations.clear();
        faceTintIndices.clear();
        modelBoundsByBlockId.clear();
        modelElementsByBlockId.clear();
        modelElementDefinitionsByBlockId.clear();
        modelTemplatesByBlockId.clear();
        modelXRotationsByBlockId.clear();
        modelYRotationsByBlockId.clear();
        lastRemovedBaseAtlasRequestCount = 0;
        Map<String, Integer> safeMaterialColors = materialColors == null ? Map.of() : materialColors;
        Map<String, String> safeAtlasKeys = atlasKeys == null ? Map.of() : atlasKeys;
        Map<String, String> safeBlockIdsByAtlasKey = blockIdsByAtlasKey == null ? Map.of() : blockIdsByAtlasKey;
        LinkedHashMap<String, TileRequest> requests = new LinkedHashMap<>();
        for (String key : new java.util.LinkedHashSet<>(safeAtlasKeys.values())) {
            requests.putIfAbsent(key, new TileRequest(
                    key,
                    safeBlockIdsByAtlasKey.get(key),
                    safeMaterialColors.getOrDefault(key, 0xFFFFFFFF),
                    ""
            ));
        }
        addModelFaceRequests(requests, safeMaterialColors, safeBlockIdsByAtlasKey, blockModelRequests);
        return requests;
    }

    private void removeCoveredBaseAtlasRequest(
            LinkedHashMap<String, TileRequest> requests,
            String baseAtlasKey,
            String blockId,
            BlockRenderPlan plan
    ) {
        if (requests == null || baseAtlasKey == null || baseAtlasKey.isBlank()
                || blockId == null || blockId.isBlank() || !planCoversAllDirections(plan)) {
            return;
        }
        TileRequest baseRequest = requests.get(baseAtlasKey);
        if (baseRequest == null) {
            return;
        }
        if (baseRequest.textureId() != null && !baseRequest.textureId().isBlank()) {
            return;
        }
        if (baseRequest.blockId() != null && !baseRequest.blockId().isBlank()
                && !baseRequest.blockId().equals(blockId)) {
            return;
        }
        requests.remove(baseAtlasKey);
        lastRemovedBaseAtlasRequestCount++;
    }

    private static boolean planCoversAllDirections(BlockRenderPlan plan) {
        if (plan == null || !plan.resolved()) {
            return false;
        }
        for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
            if (plan.textureId(direction).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    BlockRenderPlan planBlockModel(BlockModelRequest request) {
        BlockModelRequest safeRequest = request == null ? new BlockModelRequest("", Map.of(), "missing") : request;
        if (blockTextureResolver == null) {
            return BlockRenderPlan.unresolved(safeRequest, "minecraft assets unavailable");
        }
        if (safeRequest.blockId().isBlank()) {
            return BlockRenderPlan.unresolved(safeRequest, "missing block id");
        }
        try {
            EchoBlockTextureResolution resolution =
                    resolveBlockTexture(safeRequest.blockId(), safeRequest.stateProperties());
            if (!resolution.resolved()) {
                return new BlockRenderPlan(
                        safeRequest.blockId(),
                        safeRequest.stateProperties(),
                        safeRequest.baseAtlasKey(),
                        false,
                        resolution.modelId().orElse(""),
                        resolution.templateKind().orElse(""),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        Map.of(),
                        resolution.xRotationDegrees(),
                        resolution.yRotationDegrees(),
                        resolution.uvLock(),
                        resolution.modelBoundsOrFullCube(),
                        resolution.modelElements(),
                        resolution.modelElementDefinitions(),
                        resolution.missingReason().orElse("missing block model texture")
                );
            }
            java.util.EnumMap<EchoVoxelMeshDirection, String> textureIds =
                    new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
            java.util.EnumMap<EchoVoxelMeshDirection, String> atlasKeys =
                    new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
            java.util.EnumMap<EchoVoxelMeshDirection, Integer> uvRotations =
                    new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
            java.util.EnumMap<EchoVoxelMeshDirection, EchoBlockModelFaceUv> uvRects =
                    new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
            java.util.EnumMap<EchoVoxelMeshDirection, Integer> tintIndices =
                    new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
            for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
                EchoVoxelMeshDirection modelDirection = modelFaceForBlockRotation(
                        direction,
                        resolution.xRotationDegrees(),
                        resolution.yRotationDegrees()
                );
                int uvRotationDegrees = resolution.uvRotationDegreesForFace(faceName(modelDirection));
                if (uvRotationDegrees != 0) {
                    uvRotations.put(direction, uvRotationDegrees);
                }
                resolution.uvRectForFace(faceName(modelDirection)).ifPresent(uv -> uvRects.put(direction, uv));
                resolution.tintIndexForFace(faceName(modelDirection)).ifPresent(tintIndex -> tintIndices.put(direction, tintIndex));
                resolution.textureIdForFace(faceName(modelDirection)).ifPresent(textureId -> {
                    textureIds.put(direction, textureId);
                    atlasKeys.put(direction, textureAtlasKey(textureId));
                });
            }
            return new BlockRenderPlan(
                    safeRequest.blockId(),
                    safeRequest.stateProperties(),
                    safeRequest.baseAtlasKey(),
                    !textureIds.isEmpty(),
                    resolution.modelId().orElse(""),
                    resolution.templateKind().orElse(""),
                    textureIds,
                    atlasKeys,
                    uvRotations,
                    uvRects,
                    tintIndices,
                    resolution.xRotationDegrees(),
                    resolution.yRotationDegrees(),
                    resolution.uvLock(),
                    resolution.modelBoundsOrFullCube(),
                    resolution.modelElements(),
                    resolution.modelElementDefinitions(),
                    textureIds.isEmpty() ? "model has no face textures" : ""
            );
        } catch (IOException | IllegalArgumentException exception) {
            return BlockRenderPlan.unresolved(safeRequest, exception.getMessage());
        }
    }

    private ByteBuffer loadTextureId(String textureId) {
        if (textureId == null || textureId.isBlank()) {
            return null;
        }
        String[] parts = splitTextureId(textureId);
        return loadResourcePackTexturePath(parts[0], parts[1]);
    }

    private ByteBuffer loadModelResolvedTexture(String blockId) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return null;
        }
        try {
            EchoBlockTextureResolution resolution = resolveBlockTexture(blockId, Map.of());
            if (!resolution.resolved()) {
                return null;
            }
            Optional<String> namespace = resolution.textureNamespace();
            Optional<String> texturePath = resolution.texturePath();
            if (namespace.isEmpty() || texturePath.isEmpty()) {
                return null;
            }
            ByteBuffer tile = loadResourcePackTexturePath(namespace.get(), texturePath.get());
            if (tile != null) {
                if (loggedModelTextureBlocks.add(blockLookupKey(blockId, Map.of()))) {
                    System.out.println("[echo-client] model texture for " + blockId
                            + " -> " + resolution.textureId().orElse("<missing>")
                            + resolution.templateKind().map(kind -> " via " + kind).orElse(""));
                }
            } else {
                System.out.println("[echo-client] missing model texture for " + blockId
                        + " -> " + resolution.textureId().orElse("<missing>"));
                return missingTextureTile();
            }
            return tile;
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("[echo-client] model texture resolution failed for " + blockId
                    + ": " + exception.getMessage());
            return null;
        }
    }

    private EchoBlockModelBounds resolveModelBounds(String blockId, Map<String, String> stateProperties) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return EchoBlockModelBounds.fullCube();
        }
        try {
            return resolveBlockTexture(blockId, stateProperties).modelBoundsOrFullCube();
        } catch (IOException | IllegalArgumentException exception) {
            return EchoBlockModelBounds.fullCube();
        }
    }

    private List<EchoBlockModelBounds> resolveModelElements(String blockId, Map<String, String> stateProperties) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return List.of();
        }
        try {
            return resolveBlockTexture(blockId, stateProperties).modelElements();
        } catch (IOException | IllegalArgumentException exception) {
            return List.of();
        }
    }

    private List<EchoBlockModelElement> resolveModelElementDefinitions(
            String blockId,
            Map<String, String> stateProperties
    ) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return List.of();
        }
        try {
            return resolveBlockTexture(blockId, stateProperties).modelElementDefinitions();
        } catch (IOException | IllegalArgumentException exception) {
            return List.of();
        }
    }

    private String resolveModelTemplateKind(String blockId, Map<String, String> stateProperties) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return "";
        }
        try {
            return resolveBlockTexture(blockId, stateProperties).templateKind().orElse("");
        } catch (IOException | IllegalArgumentException exception) {
            return "";
        }
    }

    private int resolveModelXRotation(String blockId, Map<String, String> stateProperties) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return 0;
        }
        try {
            return resolveBlockTexture(blockId, stateProperties).xRotationDegrees();
        } catch (IOException | IllegalArgumentException exception) {
            return 0;
        }
    }

    private int resolveModelYRotation(String blockId, Map<String, String> stateProperties) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank()) {
            return 0;
        }
        try {
            return resolveBlockTexture(blockId, stateProperties).yRotationDegrees();
        } catch (IOException | IllegalArgumentException exception) {
            return 0;
        }
    }

    private int resolveFaceUvRotation(
            String blockId,
            Map<String, String> stateProperties,
            EchoVoxelMeshDirection direction
    ) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank() || direction == null) {
            return 0;
        }
        try {
            return resolveBlockTexture(blockId, stateProperties)
                    .uvRotationDegreesForFace(faceName(direction));
        } catch (IOException | IllegalArgumentException exception) {
            return 0;
        }
    }

    private Optional<EchoBlockModelFaceUv> resolveFaceUvRect(
            String blockId,
            Map<String, String> stateProperties,
            EchoVoxelMeshDirection direction
    ) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank() || direction == null) {
            return Optional.empty();
        }
        try {
            return resolveBlockTexture(blockId, stateProperties)
                    .uvRectForFace(faceName(direction));
        } catch (IOException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Optional<Integer> resolveFaceTintIndex(
            String blockId,
            Map<String, String> stateProperties,
            EchoVoxelMeshDirection direction
    ) {
        if (blockTextureResolver == null || blockId == null || blockId.isBlank() || direction == null) {
            return Optional.empty();
        }
        try {
            return resolveBlockTexture(blockId, stateProperties)
                    .tintIndexForFace(faceName(direction));
        } catch (IOException | IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private ByteBuffer loadResourcePackTexture(String namespace, String blockTextureName) {
        return loadResourcePackTexturePath(namespace, "block/" + blockTextureName);
    }

    private EchoBlockTextureResolution resolveBlockTexture(
            String blockId,
            Map<String, String> stateProperties
    ) throws IOException {
        Map<String, String> safeStateProperties = normalizedStateProperties(stateProperties);
        String lookupKey = blockLookupKey(blockId, safeStateProperties);
        EchoBlockTextureResolution cached = blockTextureResolutionsByLookupKey.get(lookupKey);
        if (cached != null) {
            blockTextureResolutionCacheHitCount++;
            return cached;
        }
        EchoBlockTextureResolution resolution = blockTextureResolver.resolve(blockId, safeStateProperties);
        blockTextureResolutionsByLookupKey.put(lookupKey, resolution);
        return resolution;
    }

    private ByteBuffer loadResourcePackTexturePath(String namespace, String texturePath) {
        if (minecraftAssets == null) {
            return null;
        }
        Optional<EchoAssetEntry> entry = minecraftAssets.texture(namespace, texturePath);
        if (entry.isEmpty()) {
            return null;
        }
        ByteBuffer cached = resourcePackTilesByLogicalId.get(entry.get().logicalId());
        if (cached != null) {
            resourcePackTileCacheHitCount++;
            return cached;
        }
        try {
            byte[] bytes = Files.readAllBytes(entry.get().file());
            ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length).put(bytes).flip();
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);
                ByteBuffer pixels = STBImage.stbi_load_from_memory(buf, w, h, channels, 4);
                if (pixels != null) {
                    TextureFrame frame = firstAnimationFrame(namespace, texturePath, pixels, w.get(0), h.get(0));
                    System.out.println("[echo-client] loaded resource-pack texture: "
                            + entry.get().logicalId() + " (" + w.get(0) + "x" + h.get(0) + ")"
                            + (frame.animatedFrame() ? " frame=" + frame.frameIndex() : ""));
                    ByteBuffer scaled = scaleTo64(frame.pixels(), frame.width(), frame.height());
                    ByteBuffer readOnly = scaled.asReadOnlyBuffer();
                    resourcePackTilesByLogicalId.put(entry.get().logicalId(), readOnly);
                    resourcePackTileDecodeCount++;
                    STBImage.stbi_image_free(pixels);
                    return readOnly;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    private TextureFrame firstAnimationFrame(String namespace, String texturePath, ByteBuffer pixels, int width, int height) {
        if (minecraftAssets == null || pixels == null || width <= 0 || height <= width || height % width != 0) {
            return new TextureFrame(pixels, width, height, false, 0);
        }
        try {
            EchoAnimatedTexture animation = EchoAnimatedTexture.load(minecraftAssets, namespace, texturePath);
            if (!animation.animated()) {
                return new TextureFrame(pixels, width, height, false, 0);
            }
            int frameHeight = width;
            int frameCount = height / frameHeight;
            int requestedFrame = animation.frames().isEmpty()
                    ? 0
                    : animation.frames().get(0).index();
            int frameIndex = Math.max(0, Math.min(frameCount - 1, requestedFrame));
            ByteBuffer frame = ByteBuffer.allocateDirect(width * frameHeight * 4);
            int sourceOffsetY = frameIndex * frameHeight;
            for (int y = 0; y < frameHeight; y++) {
                int sourceRowStart = ((sourceOffsetY + y) * width) * 4;
                for (int x = 0; x < width * 4; x++) {
                    frame.put(pixels.get(sourceRowStart + x));
                }
            }
            frame.flip();
            return new TextureFrame(frame, width, frameHeight, true, frameIndex);
        } catch (IOException | IllegalArgumentException exception) {
            return new TextureFrame(pixels, width, height, false, 0);
        }
    }

    private static String resolveTextureAlias(String name) {
        return switch (name) {
            case "rubble" -> "concrete_rubble";
            default -> null;
        };
    }

    private static String textureAtlasKey(String textureId) {
        return "minecraft-texture/" + textureId.replace(':', '/');
    }

    private static String faceLookupKey(
            String blockId,
            Map<String, String> stateProperties,
            EchoVoxelMeshDirection direction
    ) {
        return blockLookupKey(blockId, stateProperties) + "#" + direction.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String blockLookupKey(String blockId, Map<String, String> stateProperties) {
        return blockId + stateSignature(stateProperties);
    }

    private static String stateSignature(Map<String, String> stateProperties) {
        if (stateProperties == null || stateProperties.isEmpty()) {
            return "";
        }
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : stateProperties.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            sorted.put(entry.getKey().trim(), entry.getValue().trim());
        }
        if (sorted.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            builder.append(entry.getKey()).append('=').append(entry.getValue());
            first = false;
        }
        return builder.append(']').toString();
    }

    private static Map<String, String> normalizedStateProperties(Map<String, String> stateProperties) {
        if (stateProperties == null || stateProperties.isEmpty()) {
            return Map.of();
        }
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String> entry : stateProperties.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()
                    || entry.getValue() == null || entry.getValue().isBlank()) {
                continue;
            }
            sorted.put(entry.getKey().trim(), entry.getValue().trim());
        }
        return sorted.isEmpty() ? Map.of() : Map.copyOf(sorted);
    }

    private static String faceName(EchoVoxelMeshDirection direction) {
        return direction.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static EchoVoxelMeshDirection inverseRotateFaceY(EchoVoxelMeshDirection direction, int degrees) {
        return rotateFaceY(direction, 360 - normalizeModelRotation(degrees));
    }

    private static EchoVoxelMeshDirection inverseRotateFaceX(EchoVoxelMeshDirection direction, int degrees) {
        return rotateFaceX(direction, 360 - normalizeModelRotation(degrees));
    }

    private static EchoVoxelMeshDirection rotateFaceY(EchoVoxelMeshDirection direction, int degrees) {
        EchoVoxelMeshDirection safeDirection = direction == null ? EchoVoxelMeshDirection.UP : direction;
        return switch (normalizeModelRotation(degrees)) {
            case 90 -> switch (safeDirection) {
                case NORTH -> EchoVoxelMeshDirection.EAST;
                case EAST -> EchoVoxelMeshDirection.SOUTH;
                case SOUTH -> EchoVoxelMeshDirection.WEST;
                case WEST -> EchoVoxelMeshDirection.NORTH;
                default -> safeDirection;
            };
            case 180 -> switch (safeDirection) {
                case NORTH -> EchoVoxelMeshDirection.SOUTH;
                case SOUTH -> EchoVoxelMeshDirection.NORTH;
                case EAST -> EchoVoxelMeshDirection.WEST;
                case WEST -> EchoVoxelMeshDirection.EAST;
                default -> safeDirection;
            };
            case 270 -> switch (safeDirection) {
                case NORTH -> EchoVoxelMeshDirection.WEST;
                case WEST -> EchoVoxelMeshDirection.SOUTH;
                case SOUTH -> EchoVoxelMeshDirection.EAST;
                case EAST -> EchoVoxelMeshDirection.NORTH;
                default -> safeDirection;
            };
            default -> safeDirection;
        };
    }

    private static EchoVoxelMeshDirection rotateFaceX(EchoVoxelMeshDirection direction, int degrees) {
        EchoVoxelMeshDirection safeDirection = direction == null ? EchoVoxelMeshDirection.UP : direction;
        return switch (normalizeModelRotation(degrees)) {
            case 90 -> switch (safeDirection) {
                case UP -> EchoVoxelMeshDirection.SOUTH;
                case SOUTH -> EchoVoxelMeshDirection.DOWN;
                case DOWN -> EchoVoxelMeshDirection.NORTH;
                case NORTH -> EchoVoxelMeshDirection.UP;
                default -> safeDirection;
            };
            case 180 -> switch (safeDirection) {
                case UP -> EchoVoxelMeshDirection.DOWN;
                case DOWN -> EchoVoxelMeshDirection.UP;
                case NORTH -> EchoVoxelMeshDirection.SOUTH;
                case SOUTH -> EchoVoxelMeshDirection.NORTH;
                default -> safeDirection;
            };
            case 270 -> switch (safeDirection) {
                case UP -> EchoVoxelMeshDirection.NORTH;
                case NORTH -> EchoVoxelMeshDirection.DOWN;
                case DOWN -> EchoVoxelMeshDirection.SOUTH;
                case SOUTH -> EchoVoxelMeshDirection.UP;
                default -> safeDirection;
            };
            default -> safeDirection;
        };
    }

    private static String blockId(EchoVoxelMeshFace face) {
        String materialId = face.material().materialId();
        String prefix = "voxel:block/";
        return materialId.startsWith(prefix) ? materialId.substring(prefix.length()) : "";
    }

    private static String[] splitTextureId(String textureId) {
        String normalized = textureId == null ? "" : textureId.replace('\\', '/').trim();
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator == normalized.length() - 1) {
            throw new IllegalArgumentException("Invalid texture id: " + textureId);
        }
        return new String[]{normalized.substring(0, separator), normalized.substring(separator + 1)};
    }

    private record TileRequest(String atlasKey, String blockId, int argb, String textureId) {
        private TileRequest {
            atlasKey = atlasKey == null || atlasKey.isBlank() ? "missing" : atlasKey;
            blockId = blockId == null ? "" : blockId;
            textureId = textureId == null ? "" : textureId;
        }
    }

    private record TextureFrame(ByteBuffer pixels, int width, int height, boolean animatedFrame, int frameIndex) {
        private TextureFrame {
            if (pixels == null) {
                throw new IllegalArgumentException("pixels must not be null");
            }
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("texture frame dimensions must be positive");
            }
            frameIndex = Math.max(0, frameIndex);
        }
    }

    record BlockModelRequest(String blockId, Map<String, String> stateProperties, String baseAtlasKey) {
        BlockModelRequest {
            blockId = blockId == null ? "" : blockId.trim();
            stateProperties = stateProperties == null ? Map.of() : Map.copyOf(stateProperties);
            baseAtlasKey = baseAtlasKey == null || baseAtlasKey.isBlank() ? "missing" : baseAtlasKey.trim();
        }
    }

    record BlockRenderPlan(
            String blockId,
            Map<String, String> stateProperties,
            String baseAtlasKey,
            boolean resolved,
            String modelId,
            String templateKind,
            Map<EchoVoxelMeshDirection, String> textureIdsByDirection,
            Map<EchoVoxelMeshDirection, String> atlasKeysByDirection,
            Map<EchoVoxelMeshDirection, Integer> uvRotationsByDirection,
            Map<EchoVoxelMeshDirection, EchoBlockModelFaceUv> uvRectsByDirection,
            Map<EchoVoxelMeshDirection, Integer> tintIndicesByDirection,
            int xRotationDegrees,
            int yRotationDegrees,
            boolean uvLock,
            EchoBlockModelBounds modelBounds,
            List<EchoBlockModelBounds> modelElements,
            List<EchoBlockModelElement> modelElementDefinitions,
            String missingReason
    ) {
        BlockRenderPlan {
            blockId = blockId == null ? "" : blockId.trim();
            stateProperties = stateProperties == null ? Map.of() : Map.copyOf(new TreeMap<>(stateProperties));
            baseAtlasKey = baseAtlasKey == null || baseAtlasKey.isBlank() ? "missing" : baseAtlasKey.trim();
            modelId = modelId == null ? "" : modelId.trim();
            templateKind = templateKind == null ? "" : templateKind.trim();
            textureIdsByDirection = textureIdsByDirection == null
                    ? Map.of()
                    : Map.copyOf(textureIdsByDirection);
            atlasKeysByDirection = atlasKeysByDirection == null
                    ? Map.of()
                    : Map.copyOf(atlasKeysByDirection);
            uvRotationsByDirection = uvRotationsByDirection == null
                    ? Map.of()
                    : normalizeUvRotations(uvRotationsByDirection);
            uvRectsByDirection = uvRectsByDirection == null
                    ? Map.of()
                    : Map.copyOf(uvRectsByDirection);
            tintIndicesByDirection = tintIndicesByDirection == null
                    ? Map.of()
                    : normalizeTintIndices(tintIndicesByDirection);
            xRotationDegrees = normalizeModelRotation(xRotationDegrees);
            yRotationDegrees = normalizeModelRotation(yRotationDegrees);
            modelBounds = modelBounds == null ? EchoBlockModelBounds.fullCube() : modelBounds;
            modelElements = modelElements == null ? List.of() : List.copyOf(modelElements);
            modelElementDefinitions = modelElementDefinitions == null
                    ? List.of()
                    : List.copyOf(modelElementDefinitions);
            missingReason = missingReason == null ? "" : missingReason.trim();
            if (textureIdsByDirection.isEmpty()) {
                resolved = false;
            }
        }

        static BlockRenderPlan unresolved(BlockModelRequest request, String reason) {
            BlockModelRequest safeRequest = request == null ? new BlockModelRequest("", Map.of(), "missing") : request;
            return new BlockRenderPlan(
                    safeRequest.blockId(),
                    safeRequest.stateProperties(),
                    safeRequest.baseAtlasKey(),
                    false,
                    "",
                    "",
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    Map.of(),
                    0,
                    0,
                    false,
                    EchoBlockModelBounds.fullCube(),
                    List.of(),
                    List.of(),
                    reason
            );
        }

        Optional<String> textureId(EchoVoxelMeshDirection direction) {
            return Optional.ofNullable(textureIdsByDirection.get(direction));
        }

        Optional<String> atlasKey(EchoVoxelMeshDirection direction) {
            return Optional.ofNullable(atlasKeysByDirection.get(direction));
        }

        int uvRotationDegrees(EchoVoxelMeshDirection direction) {
            return uvRotationsByDirection.getOrDefault(direction, 0);
        }

        Optional<EchoBlockModelFaceUv> uvRect(EchoVoxelMeshDirection direction) {
            return Optional.ofNullable(uvRectsByDirection.get(direction));
        }

        Optional<Integer> tintIndex(EchoVoxelMeshDirection direction) {
            return Optional.ofNullable(tintIndicesByDirection.get(direction));
        }
    }

    private EchoBlockModelFaceUv faceUvRect(
            String blockId,
            Map<String, String> stateProperties,
            EchoVoxelMeshDirection direction
    ) {
        if (blockId == null || blockId.isBlank() || direction == null) {
            return null;
        }
        String lookupKey = faceLookupKey(blockId, stateProperties, direction);
        EchoBlockModelFaceUv uv = faceUvRects.get(lookupKey);
        if (uv == null) {
            uv = faceUvRects.get(faceLookupKey(blockId, Map.of(), direction));
        }
        if (uv == null) {
            Optional<EchoBlockModelFaceUv> resolved = resolveFaceUvRect(blockId, stateProperties, direction);
            if (resolved.isPresent()) {
                uv = resolved.get();
                faceUvRects.put(lookupKey, uv);
            }
        }
        return uv;
    }

    private static AtlasEntry applyFaceUv(AtlasEntry base, EchoBlockModelFaceUv uv) {
        if (base == null || uv == null) {
            return base == null ? new AtlasEntry(0.0f, 0.0f, 1.0f, 1.0f) : base;
        }
        float width = base.u2() - base.u1();
        float height = base.v2() - base.v1();
        return new AtlasEntry(
                base.u1() + (float) (uv.u1() / 16.0D) * width,
                base.v1() + (float) (uv.v1() / 16.0D) * height,
                base.u1() + (float) (uv.u2() / 16.0D) * width,
                base.v1() + (float) (uv.v2() / 16.0D) * height
        );
    }

    private static Map<EchoVoxelMeshDirection, Integer> normalizeUvRotations(
            Map<EchoVoxelMeshDirection, Integer> rotations
    ) {
        if (rotations == null || rotations.isEmpty()) {
            return Map.of();
        }
        java.util.EnumMap<EchoVoxelMeshDirection, Integer> result =
                new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
        for (Map.Entry<EchoVoxelMeshDirection, Integer> entry : rotations.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            int rotation = normalizeModelRotation(entry.getValue());
            if (rotation != 0) {
                result.put(entry.getKey(), rotation);
            }
        }
        return Map.copyOf(result);
    }

    private static Map<EchoVoxelMeshDirection, Integer> normalizeTintIndices(
            Map<EchoVoxelMeshDirection, Integer> tintIndices
    ) {
        if (tintIndices == null || tintIndices.isEmpty()) {
            return Map.of();
        }
        java.util.EnumMap<EchoVoxelMeshDirection, Integer> result =
                new java.util.EnumMap<>(EchoVoxelMeshDirection.class);
        for (Map.Entry<EchoVoxelMeshDirection, Integer> entry : tintIndices.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() < 0) {
                continue;
            }
            result.put(entry.getKey(), entry.getValue());
        }
        return Map.copyOf(result);
    }

    private static ByteBuffer generateProceduralTile(int argb, String key) {
        ByteBuffer buf = ByteBuffer.allocateDirect(TEX_SIZE * TEX_SIZE * 4);
        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;
        boolean noise = key.contains("dust") || key.contains("ash") || key.contains("rubble");
        boolean metal = key.contains("metal") || key.contains("rust") || key.contains("hull");
        boolean stone = key.contains("basalt") || key.contains("stone") || key.contains("ore");
        boolean water = key.contains("water") || key.contains("puddle");
        boolean organic = key.contains("grass") || key.contains("berry") || key.contains("bush");
        for (int y = 0; y < TEX_SIZE; y++) {
            for (int x = 0; x < TEX_SIZE; x++) {
                int pr = r;
                int pg = g;
                int pb = b;

                // Base noise for all materials
                int n = (hash(x * 73 + y * 131 + key.hashCode()) & 0x3F) - 32;
                pr = clamp(pr + n, 0, 255);
                pg = clamp(pg + n, 0, 255);
                pb = clamp(pb + n, 0, 255);

                if (noise || key.contains("fallout")) {
                    // Heavy grain + cracks
                    int crack = hash(x * 193 + y * 277) & 0xFF;
                    if (crack > 220) {
                        int dark = -20;
                        pr = clamp(pr + dark, 0, 255);
                        pg = clamp(pg + dark, 0, 255);
                        pb = clamp(pb + dark, 0, 255);
                    }
                } else if (metal) {
                    // Scratches and panels
                    int panel = (x / 8) + (y / 8);
                    int shift = (panel % 2 == 0) ? 10 : -6;
                    pr = clamp(pr + shift, 0, 255);
                    pg = clamp(pg + shift, 0, 255);
                    pb = clamp(pb + shift, 0, 255);
                    int scratch = hash(x * 311 + y * 401) & 0xFF;
                    if (scratch > 240) {
                        pr = clamp(pr + 30, 0, 255);
                        pg = clamp(pg + 25, 0, 255);
                        pb = clamp(pb + 20, 0, 255);
                    }
                } else if (stone) {
                    // Crystalline flecks
                    int fleck = hash(x * 151 + y * 211) & 0xFF;
                    if (fleck > 230) {
                        pr = clamp(pr + 25, 0, 255);
                        pg = clamp(pg + 20, 0, 255);
                        pb = clamp(pb + 15, 0, 255);
                    }
                } else if (water) {
                    // Ripples
                    int ripple = (int) (Math.sin(x * 0.8 + y * 0.4) * 15);
                    pr = clamp(pr + ripple, 0, 255);
                    pg = clamp(pg + ripple, 0, 255);
                    pb = clamp(pb + ripple + 10, 0, 255);
                } else if (organic) {
                    // Mottling
                    int mottle = hash(x * 113 + y * 167) & 0x3F;
                    pr = clamp(pr + mottle - 16, 0, 255);
                    pg = clamp(pg + mottle - 8, 0, 255);
                    pb = clamp(pb + mottle - 24, 0, 255);
                }

                // Subtle border bevel for all blocks
                if (x < 2 || x >= TEX_SIZE - 2 || y < 2 || y >= TEX_SIZE - 2) {
                    pr = clamp(pr - 12, 0, 255);
                    pg = clamp(pg - 12, 0, 255);
                    pb = clamp(pb - 12, 0, 255);
                }

                buf.put((byte) pr);
                buf.put((byte) pg);
                buf.put((byte) pb);
                buf.put((byte) 255);
            }
        }
        buf.flip();
        return buf;
    }

    private static ByteBuffer missingTextureTile() {
        byte[] pixels = EchoMissingTexture.rgbaChecker(TEX_SIZE);
        return ByteBuffer.allocateDirect(pixels.length).put(pixels).flip();
    }

    private static ByteBuffer scaleTo64(ByteBuffer src, int srcW, int srcH) {
        ByteBuffer dst = ByteBuffer.allocateDirect(TEX_SIZE * TEX_SIZE * 4);
        for (int y = 0; y < TEX_SIZE; y++) {
            for (int x = 0; x < TEX_SIZE; x++) {
                int sx = x * srcW / TEX_SIZE;
                int sy = y * srcH / TEX_SIZE;
                int idx = (sy * srcW + sx) * 4;
                dst.put(src.get(idx));
                dst.put(src.get(idx + 1));
                dst.put(src.get(idx + 2));
                dst.put(src.get(idx + 3));
            }
        }
        dst.flip();
        return dst;
    }

    private static void blitTile(ByteBuffer atlas, int atlasW, int atlasH,
                                  int offsetX, int offsetY, ByteBuffer tile) {
        // Flip vertically: STBImage returns top-to-bottom, but OpenGL expects bottom-to-top
        for (int y = 0; y < TEX_SIZE; y++) {
            int atlasRowStart = ((offsetY + (TEX_SIZE - 1 - y)) * atlasW + offsetX) * 4;
            int tileRowStart = y * TEX_SIZE * 4;
            for (int x = 0; x < TEX_SIZE * 4; x++) {
                atlas.put(atlasRowStart + x, tile.get(tileRowStart + x));
            }
        }
    }

    private static int hash(int x) {
        x = ((x >>> 16) ^ x) * 0x45D9F3B;
        x = ((x >>> 16) ^ x) * 0x45D9F3B;
        x = (x >>> 16) ^ x;
        return x;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int normalizeModelRotation(int degrees) {
        int normalized = Math.floorMod(degrees, 360);
        return normalized % 90 == 0 ? normalized : 0;
    }
}
