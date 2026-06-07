package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelBounds;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelElement;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelElementRotation;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesh;
import dev.echo.standalone.runtime.render.EchoVoxelMeshDirection;
import dev.echo.standalone.runtime.render.EchoVoxelMeshFace;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * GPU mesh for one chunk: interleaved VBO + IBO.
 * Vertex layout: position(3) + texcoord(2) + normal(3) + color(4) = 12 floats.
 */
final class EchoClientChunkMesh {
    private int vao;
    private int vbo;
    private int ibo;
    private int indexCount;
    private boolean dirty = true;
    private int vertexBufferCapacityBytes;
    private int indexBufferCapacityBytes;
    private ByteBuffer vertexUploadBytes;
    private ByteBuffer indexUploadBytes;
    private FloatBuffer vertexUploadBuffer;
    private IntBuffer indexUploadBuffer;

    private static final int FLOATS_PER_VERTEX = 12;
    private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;
    private static final int POS_OFFSET = 0;
    private static final int UV_OFFSET = 3 * Float.BYTES;
    private static final int NORM_OFFSET = 5 * Float.BYTES;
    private static final int COLOR_OFFSET = 8 * Float.BYTES;
    private static final int MIN_DYNAMIC_BUFFER_BYTES = 1024;

    private final dev.echo.standalone.runtime.world.EchoVoxelChunkId chunkId;
    private EchoVoxelChunkMesh source;
    private int sourceSignature;

    EchoClientChunkMesh(dev.echo.standalone.runtime.world.EchoVoxelChunkId chunkId) {
        this.chunkId = Objects.requireNonNull(chunkId);
    }

    boolean setSource(EchoVoxelChunkMesh mesh) {
        int nextSignature = sourceSignature(mesh);
        if (source != null && sourceSignature == nextSignature) {
            source = mesh;
            return false;
        }
        this.source = mesh;
        this.sourceSignature = nextSignature;
        this.dirty = true;
        return true;
    }

    void markDirty() {
        this.dirty = true;
    }

    dev.echo.standalone.runtime.world.EchoVoxelChunkId chunkId() {
        return chunkId;
    }

    boolean isEmpty() {
        return source == null || source.faceCount() == 0;
    }

    boolean dirty() {
        return dirty;
    }

    static int sourceSignature(EchoVoxelChunkMesh mesh) {
        if (mesh == null) {
            return 0;
        }
        int hash = 17;
        hash = 31 * hash + mesh.chunkId().hashCode();
        hash = 31 * hash + mesh.target().hashCode();
        hash = 31 * hash + mesh.sourceBlockCount();
        hash = 31 * hash + mesh.faces().hashCode();
        return hash;
    }

    boolean uploadIfDirty(EchoClientTextureAtlas atlas) {
        if (!dirty) {
            return false;
        }
        dirty = false;
        if (source == null) {
            indexCount = 0;
            return true;
        }

        MeshData meshData = meshData(source, atlas);
        indexCount = meshData.indexCount();
        if (indexCount == 0) {
            return true;
        }

        if (vao == 0) {
            vao = GL30.glGenVertexArrays();
            vbo = GL15.glGenBuffers();
            ibo = GL15.glGenBuffers();
        }

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer vBuf = vertexUploadBuffer(meshData.vertices().length);
        vBuf.put(meshData.vertices()).flip();
        DynamicBufferUploadPlan vertexPlan =
                dynamicBufferUploadPlan(vertexBufferCapacityBytes, meshData.vertices().length * Float.BYTES);
        if (vertexPlan.grow()) {
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) vertexPlan.capacityBytes(), GL15.GL_DYNAMIC_DRAW);
            vertexBufferCapacityBytes = vertexPlan.capacityBytes();
        }
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, vBuf);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        IntBuffer iBuf = indexUploadBuffer(meshData.indices().length);
        iBuf.put(meshData.indices()).flip();
        DynamicBufferUploadPlan indexPlan =
                dynamicBufferUploadPlan(indexBufferCapacityBytes, meshData.indices().length * Integer.BYTES);
        if (indexPlan.grow()) {
            GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, (long) indexPlan.capacityBytes(), GL15.GL_DYNAMIC_DRAW);
            indexBufferCapacityBytes = indexPlan.capacityBytes();
        }
        GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0L, iBuf);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, STRIDE, POS_OFFSET);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, STRIDE, UV_OFFSET);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, STRIDE, NORM_OFFSET);
        GL20.glEnableVertexAttribArray(3);
        GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, STRIDE, COLOR_OFFSET);

        GL30.glBindVertexArray(0);
        return true;
    }

    static DynamicBufferUploadPlan dynamicBufferUploadPlan(int currentCapacityBytes, int requestedBytes) {
        int safeCurrentCapacity = Math.max(0, currentCapacityBytes);
        int safeRequestedBytes = Math.max(1, requestedBytes);
        if (safeCurrentCapacity >= safeRequestedBytes) {
            return new DynamicBufferUploadPlan(false, safeRequestedBytes, safeCurrentCapacity);
        }
        int nextCapacity = Math.max(MIN_DYNAMIC_BUFFER_BYTES, safeCurrentCapacity);
        while (nextCapacity < safeRequestedBytes && nextCapacity < Integer.MAX_VALUE / 2) {
            nextCapacity *= 2;
        }
        if (nextCapacity < safeRequestedBytes) {
            nextCapacity = safeRequestedBytes;
        }
        return new DynamicBufferUploadPlan(true, safeRequestedBytes, nextCapacity);
    }

    static MeshData meshData(EchoVoxelChunkMesh source, EchoClientTextureAtlas atlas) {
        if (source == null || source.faceCount() == 0) {
            return MeshData.empty();
        }
        MeshBuilder builder = new MeshBuilder();
        HashSet<String> emittedCustomBlocks = new HashSet<>();
        Map<String, EnumSet<EchoVoxelMeshDirection>> visibleDirectionsByBlock = visibleDirectionsByBlock(source);
        for (EchoVoxelMeshFace face : source.faces()) {
            EnumSet<EchoVoxelMeshDirection> visibleDirections = visibleDirectionsByBlock.get(modelBlockKey(face));
            if (atlas != null && atlas.crossModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addCrossQuads(builder, face, atlas);
                }
                continue;
            }
            if (atlas != null && atlas.stairModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addStairQuads(builder, face, atlas, visibleDirections);
                }
                continue;
            }
            if (atlas != null && atlas.fenceModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addFenceQuads(builder, face, atlas, visibleDirections);
                }
                continue;
            }
            if (atlas != null && atlas.paneModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addPaneQuads(builder, face, atlas, visibleDirections);
                }
                continue;
            }
            if (atlas != null && atlas.trapdoorModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addTrapdoorQuads(builder, face, atlas, visibleDirections);
                }
                continue;
            }
            if (atlas != null && atlas.doorModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addDoorQuads(builder, face, atlas, visibleDirections);
                }
                continue;
            }
            if (atlas != null && atlas.wallModel(face)) {
                if (emittedCustomBlocks.add(modelBlockKey(face))) {
                    addWallQuads(builder, face, atlas, visibleDirections);
                }
                continue;
            }
            if (atlas != null) {
                List<EchoBlockModelElement> elementDefinitions = atlas.modelElementDefinitions(face);
                if (!elementDefinitions.isEmpty()) {
                    if (emittedCustomBlocks.add(modelBlockKey(face))) {
                        addElementQuads(builder, face, atlas, elementDefinitions, visibleDirections);
                    }
                    continue;
                }
                List<EchoBlockModelBounds> elements = atlas.modelElements(face);
                if (!elements.isEmpty()) {
                    if (emittedCustomBlocks.add(modelBlockKey(face))) {
                        addElementBoundsQuads(builder, face, atlas, elements, visibleDirections);
                    }
                    continue;
                }
            }
            EchoBlockModelBounds bounds = atlas == null ? EchoBlockModelBounds.fullCube() : atlas.modelBounds(face);
            EchoClientTextureAtlas.AtlasEntry uv = atlas == null
                    ? new EchoClientTextureAtlas.AtlasEntry(0.0f, 0.0f, 1.0f, 1.0f)
                    : atlas.get(face);
            builder.addQuad(
                    offsetCorners(face, cornerOffsets(face.direction(), bounds)),
                    face.direction().normalX(),
                    face.direction().normalY(),
                    face.direction().normalZ(),
                    uv,
                    faceArgb(face, face.direction(), atlas)
            );
        }
        return builder.build();
    }

    void draw() {
        if (indexCount <= 0 || vao == 0) {
            return;
        }
        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    void delete() {
        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            GL15.glDeleteBuffers(vbo);
            GL15.glDeleteBuffers(ibo);
            vao = 0;
            vbo = 0;
            ibo = 0;
        }
        indexCount = 0;
        vertexBufferCapacityBytes = 0;
        indexBufferCapacityBytes = 0;
        vertexUploadBytes = null;
        indexUploadBytes = null;
        vertexUploadBuffer = null;
        indexUploadBuffer = null;
    }

    private FloatBuffer vertexUploadBuffer(int floats) {
        int bytes = Math.max(1, floats) * Float.BYTES;
        if (vertexUploadBytes == null || vertexUploadBytes.capacity() < bytes) {
            vertexUploadBytes = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
            vertexUploadBuffer = vertexUploadBytes.asFloatBuffer();
        }
        vertexUploadBuffer.clear();
        return vertexUploadBuffer;
    }

    private IntBuffer indexUploadBuffer(int ints) {
        int bytes = Math.max(1, ints) * Integer.BYTES;
        if (indexUploadBytes == null || indexUploadBytes.capacity() < bytes) {
            indexUploadBytes = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
            indexUploadBuffer = indexUploadBytes.asIntBuffer();
        }
        indexUploadBuffer.clear();
        return indexUploadBuffer;
    }

    static float[] cornerOffsets(EchoVoxelMeshDirection direction) {
        return cornerOffsets(direction, EchoBlockModelBounds.fullCube());
    }

    static float[] cornerOffsets(EchoVoxelMeshDirection direction, EchoBlockModelBounds bounds) {
        EchoBlockModelBounds safeBounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
        float minX = safeBounds.minXUnit();
        float minY = safeBounds.minYUnit();
        float minZ = safeBounds.minZUnit();
        float maxX = safeBounds.maxXUnit();
        float maxY = safeBounds.maxYUnit();
        float maxZ = safeBounds.maxZUnit();
        return switch (direction) {
            case UP -> new float[]{minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ};
            case EAST -> new float[]{maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ};
            case WEST -> new float[]{minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ};
            case SOUTH -> new float[]{minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ};
            case NORTH -> new float[]{maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ};
            case DOWN -> new float[]{minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ};
        };
    }

    private static void addCrossQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas
    ) {
        EchoClientTextureAtlas.AtlasEntry uv = atlas == null
                ? new EchoClientTextureAtlas.AtlasEntry(0.0f, 0.0f, 1.0f, 1.0f)
                : atlas.get(face);
        float x0 = face.x();
        float y0 = face.y();
        float z0 = face.z();
        float x1 = x0 + 1.0f;
        float y1 = y0 + 1.0f;
        float z1 = z0 + 1.0f;
        int argb = crossArgb(face, atlas);
        float diagonal = 0.70710677f;

        float[] planeA = new float[]{x0, y0, z0, x1, y0, z1, x1, y1, z1, x0, y1, z0};
        float[] planeAReverse = new float[]{x0, y0, z0, x0, y1, z0, x1, y1, z1, x1, y0, z1};
        float[] planeB = new float[]{x1, y0, z0, x0, y0, z1, x0, y1, z1, x1, y1, z0};
        float[] planeBReverse = new float[]{x1, y0, z0, x1, y1, z0, x0, y1, z1, x0, y0, z1};

        builder.addQuad(planeA, diagonal, 0.0f, -diagonal, uv, argb);
        builder.addQuad(planeAReverse, -diagonal, 0.0f, diagonal, uv, argb);
        builder.addQuad(planeB, diagonal, 0.0f, diagonal, uv, argb);
        builder.addQuad(planeBReverse, -diagonal, 0.0f, -diagonal, uv, argb);
    }

    private static void addStairQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        Map<String, String> state = face.material().stateProperties();
        HorizontalFacing facing = HorizontalFacing.from(state.get("facing"));
        String shape = state.getOrDefault("shape", "straight");
        boolean top = "top".equalsIgnoreCase(state.getOrDefault("half", "bottom"));
        boolean inner = "inner_stairs".equals(atlas.modelTemplateKind(face)) || shape.startsWith("inner_");
        boolean outer = "outer_stairs".equals(atlas.modelTemplateKind(face)) || shape.startsWith("outer_");
        boolean rightTurn = shape.endsWith("_right");

        Footprint facingHalf = Footprint.half(facing);
        Footprint oppositeFacingHalf = Footprint.half(facing.opposite());
        HorizontalFacing turn = rightTurn ? facing.right() : facing.left();
        Footprint turnHalf = Footprint.half(turn);
        Footprint oppositeTurnHalf = Footprint.half(turn.opposite());
        float lowY0 = top ? 0.5f : 0.0f;
        float lowY1 = top ? 1.0f : 0.5f;

        ArrayList<UnitBox> boxes = new ArrayList<>();
        if (outer) {
            boxes.add(facingHalf.intersect(turnHalf).box(0.0f, 1.0f));
            boxes.add(oppositeFacingHalf.box(lowY0, lowY1));
            boxes.add(facingHalf.intersect(oppositeTurnHalf).box(lowY0, lowY1));
        } else if (inner) {
            boxes.add(facingHalf.box(0.0f, 1.0f));
            boxes.add(oppositeFacingHalf.intersect(turnHalf).box(0.0f, 1.0f));
            boxes.add(oppositeFacingHalf.intersect(oppositeTurnHalf).box(lowY0, lowY1));
        } else {
            boxes.add(facingHalf.box(0.0f, 1.0f));
            boxes.add(oppositeFacingHalf.box(lowY0, lowY1));
        }

        for (UnitBox box : boxes) {
            addBoxQuads(builder, face, box, atlas, visibleDirections);
        }
    }

    private static void addWallQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        Map<String, String> state = face.material().stateProperties();
        boolean up = !"false".equalsIgnoreCase(state.getOrDefault("up", "true"));
        ArrayList<UnitBox> boxes = new ArrayList<>();
        if (up) {
            boxes.add(new UnitBox(0.25f, 0.0f, 0.25f, 0.75f, 1.0f, 0.75f));
        }
        addWallArm(boxes, HorizontalFacing.NORTH, state.get("north"));
        addWallArm(boxes, HorizontalFacing.EAST, state.get("east"));
        addWallArm(boxes, HorizontalFacing.SOUTH, state.get("south"));
        addWallArm(boxes, HorizontalFacing.WEST, state.get("west"));
        if (boxes.isEmpty()) {
            boxes.add(new UnitBox(0.25f, 0.0f, 0.25f, 0.75f, 1.0f, 0.75f));
        }
        for (UnitBox box : boxes) {
            addBoxQuads(builder, face, box, atlas, visibleDirections);
        }
    }

    private static void addFenceQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        Map<String, String> state = face.material().stateProperties();
        ArrayList<UnitBox> boxes = new ArrayList<>();
        boxes.add(new UnitBox(0.375f, 0.0f, 0.375f, 0.625f, 1.0f, 0.625f));
        addFenceArm(boxes, HorizontalFacing.NORTH, state.get("north"));
        addFenceArm(boxes, HorizontalFacing.EAST, state.get("east"));
        addFenceArm(boxes, HorizontalFacing.SOUTH, state.get("south"));
        addFenceArm(boxes, HorizontalFacing.WEST, state.get("west"));
        for (UnitBox box : boxes) {
            addBoxQuads(builder, face, box, atlas, visibleDirections);
        }
    }

    private static void addPaneQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        Map<String, String> state = face.material().stateProperties();
        boolean north = trueState(state.get("north"));
        boolean east = trueState(state.get("east"));
        boolean south = trueState(state.get("south"));
        boolean west = trueState(state.get("west"));
        boolean northSouth = north || south;
        boolean eastWest = east || west;
        ArrayList<UnitBox> boxes = new ArrayList<>();
        if (!northSouth && !eastWest) {
            boxes.add(new UnitBox(0.4375f, 0.0f, 0.0f, 0.5625f, 1.0f, 1.0f));
            boxes.add(new UnitBox(0.0f, 0.0f, 0.4375f, 1.0f, 1.0f, 0.5625f));
        } else {
            if (northSouth) {
                float minZ = north ? 0.0f : 0.4375f;
                float maxZ = south ? 1.0f : 0.5625f;
                boxes.add(new UnitBox(0.4375f, 0.0f, minZ, 0.5625f, 1.0f, maxZ));
            }
            if (eastWest) {
                float minX = west ? 0.0f : 0.4375f;
                float maxX = east ? 1.0f : 0.5625f;
                boxes.add(new UnitBox(minX, 0.0f, 0.4375f, maxX, 1.0f, 0.5625f));
            }
        }
        for (UnitBox box : boxes) {
            addBoxQuads(builder, face, box, atlas, visibleDirections);
        }
    }

    private static void addTrapdoorQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        Map<String, String> state = face.material().stateProperties();
        boolean open = trueState(state.get("open")) || "trapdoor_open".equals(atlas.modelTemplateKind(face));
        UnitBox box;
        if (open) {
            box = switch (HorizontalFacing.from(state.get("facing"))) {
                case NORTH -> new UnitBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.1875f);
                case EAST -> new UnitBox(0.8125f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                case SOUTH -> new UnitBox(0.0f, 0.0f, 0.8125f, 1.0f, 1.0f, 1.0f);
                case WEST -> new UnitBox(0.0f, 0.0f, 0.0f, 0.1875f, 1.0f, 1.0f);
            };
        } else {
            boolean top = "top".equalsIgnoreCase(state.getOrDefault("half", "bottom"))
                    || "trapdoor_top".equals(atlas.modelTemplateKind(face));
            box = top
                    ? new UnitBox(0.0f, 0.8125f, 0.0f, 1.0f, 1.0f, 1.0f)
                    : new UnitBox(0.0f, 0.0f, 0.0f, 1.0f, 0.1875f, 1.0f);
        }
        addBoxQuads(builder, face, box, atlas, visibleDirections);
    }

    private static void addDoorQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        Map<String, String> state = face.material().stateProperties();
        HorizontalFacing facing = HorizontalFacing.from(state.get("facing"));
        boolean open = trueState(state.get("open")) || atlas.modelTemplateKind(face).endsWith("_open");
        HorizontalFacing panelEdge = open
                ? openDoorEdge(facing, "left".equalsIgnoreCase(state.getOrDefault("hinge", "left")))
                : facing;
        addBoxQuads(builder, face, doorPanelBox(panelEdge), atlas, visibleDirections);
    }

    private static void addElementQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            List<EchoBlockModelElement> elements,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        int xRotationDegrees = atlas == null ? 0 : atlas.modelXRotationDegrees(face);
        int yRotationDegrees = atlas == null ? 0 : atlas.modelYRotationDegrees(face);
        for (EchoBlockModelElement element : elements) {
            if (element == null) {
                continue;
            }
            if (element.rotation().isPresent()) {
                addRotatedElementQuads(
                        builder,
                        face,
                        atlas,
                        element,
                        visibleDirections,
                        xRotationDegrees,
                        yRotationDegrees
                );
                continue;
            }
            addBoundsQuads(
                    builder,
                    face,
                    rotateBoundsY(rotateBoundsX(element.bounds(), xRotationDegrees), yRotationDegrees),
                    atlas,
                    visibleDirections,
                    element
            );
        }
    }

    private static void addRotatedElementQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            EchoBlockModelElement element,
            Set<EchoVoxelMeshDirection> visibleDirections,
            int xRotationDegrees,
            int yRotationDegrees
    ) {
        EchoBlockModelElementRotation rotation = element.rotation().orElse(null);
        if (rotation == null || !rotation.active()) {
            addBoundsQuads(builder, face,
                    rotateBoundsY(rotateBoundsX(element.bounds(), xRotationDegrees), yRotationDegrees),
                    atlas,
                    visibleDirections,
                    element);
            return;
        }
        for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
            if (!modelFaceVisible(element.bounds(), direction, visibleDirections, element)) {
                continue;
            }
            EchoClientTextureAtlas.AtlasEntry uv = atlas == null
                    ? new EchoClientTextureAtlas.AtlasEntry(0.0f, 0.0f, 1.0f, 1.0f)
                    : atlas.get(face, direction, element);
            int uvRotationDegrees = atlas == null ? 0 : atlas.uvRotationDegrees(face, direction, element);
            float[] normal = rotatedNormal(direction, rotation, xRotationDegrees, yRotationDegrees);
            builder.addQuad(
                    rotatedElementCorners(face, cornerOffsets(direction, element.bounds()), rotation,
                            xRotationDegrees, yRotationDegrees),
                    normal[0],
                    normal[1],
                    normal[2],
                    uv,
                    faceArgb(face, direction, atlas, element),
                    uvRotationDegrees
            );
        }
    }

    private static void addElementBoundsQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoClientTextureAtlas atlas,
            List<EchoBlockModelBounds> elements,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        int xRotationDegrees = atlas == null ? 0 : atlas.modelXRotationDegrees(face);
        int yRotationDegrees = atlas == null ? 0 : atlas.modelYRotationDegrees(face);
        for (EchoBlockModelBounds element : elements) {
            addBoundsQuads(
                    builder,
                    face,
                    rotateBoundsY(rotateBoundsX(element, xRotationDegrees), yRotationDegrees),
                    atlas,
                    visibleDirections,
                    null
            );
        }
    }

    private static void addWallArm(
            ArrayList<UnitBox> boxes,
            HorizontalFacing direction,
            String connection
    ) {
        String normalized = connection == null ? "" : connection.trim().toLowerCase(java.util.Locale.ROOT);
        if (!"low".equals(normalized) && !"tall".equals(normalized)) {
            return;
        }
        float maxY = "tall".equals(normalized) ? 1.0f : 0.875f;
        switch (direction) {
            case NORTH -> boxes.add(new UnitBox(0.3125f, 0.0f, 0.0f, 0.6875f, maxY, 0.5f));
            case EAST -> boxes.add(new UnitBox(0.5f, 0.0f, 0.3125f, 1.0f, maxY, 0.6875f));
            case SOUTH -> boxes.add(new UnitBox(0.3125f, 0.0f, 0.5f, 0.6875f, maxY, 1.0f));
            case WEST -> boxes.add(new UnitBox(0.0f, 0.0f, 0.3125f, 0.5f, maxY, 0.6875f));
        }
    }

    private static void addFenceArm(
            ArrayList<UnitBox> boxes,
            HorizontalFacing direction,
            String connection
    ) {
        String normalized = connection == null ? "" : connection.trim().toLowerCase(java.util.Locale.ROOT);
        if (!"true".equals(normalized)) {
            return;
        }
        switch (direction) {
            case NORTH -> boxes.add(new UnitBox(0.4375f, 0.375f, 0.0f, 0.5625f, 0.9375f, 0.5625f));
            case EAST -> boxes.add(new UnitBox(0.4375f, 0.375f, 0.4375f, 1.0f, 0.9375f, 0.5625f));
            case SOUTH -> boxes.add(new UnitBox(0.4375f, 0.375f, 0.4375f, 0.5625f, 0.9375f, 1.0f));
            case WEST -> boxes.add(new UnitBox(0.0f, 0.375f, 0.4375f, 0.5625f, 0.9375f, 0.5625f));
        }
    }

    private static boolean trueState(String value) {
        return "true".equalsIgnoreCase(value == null ? "" : value.trim());
    }

    private static EchoBlockModelBounds rotateBoundsY(EchoBlockModelBounds bounds, int degrees) {
        EchoBlockModelBounds safeBounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
        int rotation = Math.floorMod(degrees, 360);
        if (rotation == 0 || rotation % 90 != 0) {
            return safeBounds;
        }
        double[][] corners = {
                rotateXZ(safeBounds.fromX(), safeBounds.fromZ(), rotation),
                rotateXZ(safeBounds.toX(), safeBounds.fromZ(), rotation),
                rotateXZ(safeBounds.fromX(), safeBounds.toZ(), rotation),
                rotateXZ(safeBounds.toX(), safeBounds.toZ(), rotation)
        };
        double minX = 16.0D;
        double minZ = 16.0D;
        double maxX = 0.0D;
        double maxZ = 0.0D;
        for (double[] corner : corners) {
            minX = Math.min(minX, corner[0]);
            minZ = Math.min(minZ, corner[1]);
            maxX = Math.max(maxX, corner[0]);
            maxZ = Math.max(maxZ, corner[1]);
        }
        return new EchoBlockModelBounds(minX, safeBounds.fromY(), minZ, maxX, safeBounds.toY(), maxZ);
    }

    private static EchoBlockModelBounds rotateBoundsX(EchoBlockModelBounds bounds, int degrees) {
        EchoBlockModelBounds safeBounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
        int rotation = Math.floorMod(degrees, 360);
        if (rotation == 0 || rotation % 90 != 0) {
            return safeBounds;
        }
        double[][] corners = {
                rotateYZ(safeBounds.fromY(), safeBounds.fromZ(), rotation),
                rotateYZ(safeBounds.toY(), safeBounds.fromZ(), rotation),
                rotateYZ(safeBounds.fromY(), safeBounds.toZ(), rotation),
                rotateYZ(safeBounds.toY(), safeBounds.toZ(), rotation)
        };
        double minY = 16.0D;
        double minZ = 16.0D;
        double maxY = 0.0D;
        double maxZ = 0.0D;
        for (double[] corner : corners) {
            minY = Math.min(minY, corner[0]);
            minZ = Math.min(minZ, corner[1]);
            maxY = Math.max(maxY, corner[0]);
            maxZ = Math.max(maxZ, corner[1]);
        }
        return new EchoBlockModelBounds(safeBounds.fromX(), minY, minZ, safeBounds.toX(), maxY, maxZ);
    }

    private static float[] rotatedElementCorners(
            EchoVoxelMeshFace face,
            float[] corners,
            EchoBlockModelElementRotation rotation,
            int xRotationDegrees,
            int yRotationDegrees
    ) {
        float[] result = new float[corners.length];
        for (int index = 0; index < corners.length; index += 3) {
            double[] point = new double[]{
                    corners[index] * 16.0D,
                    corners[index + 1] * 16.0D,
                    corners[index + 2] * 16.0D
            };
            point = rotateModelPoint(point, rotation.axis(), rotation.angleDegrees(),
                    rotation.originX(), rotation.originY(), rotation.originZ());
            point = rotateModelPoint(point, "x", xRotationDegrees, 8.0D, 8.0D, 8.0D);
            point = rotateModelPoint(point, "y", yRotationDegrees, 8.0D, 8.0D, 8.0D);
            result[index] = face.x() + (float) (point[0] / 16.0D);
            result[index + 1] = face.y() + (float) (point[1] / 16.0D);
            result[index + 2] = face.z() + (float) (point[2] / 16.0D);
        }
        return result;
    }

    private static float[] rotatedNormal(
            EchoVoxelMeshDirection direction,
            EchoBlockModelElementRotation rotation,
            int xRotationDegrees,
            int yRotationDegrees
    ) {
        double[] normal = new double[]{
                direction.normalX(),
                direction.normalY(),
                direction.normalZ()
        };
        normal = rotateVector(normal, rotation.axis(), rotation.angleDegrees());
        normal = rotateVector(normal, "x", xRotationDegrees);
        normal = rotateVector(normal, "y", yRotationDegrees);
        double length = Math.sqrt(normal[0] * normal[0] + normal[1] * normal[1] + normal[2] * normal[2]);
        if (length <= 0.0D) {
            return new float[]{direction.normalX(), direction.normalY(), direction.normalZ()};
        }
        return new float[]{
                (float) (normal[0] / length),
                (float) (normal[1] / length),
                (float) (normal[2] / length)
        };
    }

    private static double[] rotateModelPoint(
            double[] point,
            String axis,
            double angleDegrees,
            double originX,
            double originY,
            double originZ
    ) {
        if (point == null || Math.abs(angleDegrees) < 1.0E-6D) {
            return point;
        }
        double[] relative = new double[]{
                point[0] - originX,
                point[1] - originY,
                point[2] - originZ
        };
        double[] rotated = rotateVector(relative, axis, angleDegrees);
        return new double[]{
                originX + rotated[0],
                originY + rotated[1],
                originZ + rotated[2]
        };
    }

    private static double[] rotateVector(double[] vector, String axis, double angleDegrees) {
        if (vector == null || Math.abs(angleDegrees) < 1.0E-6D) {
            return vector;
        }
        double radians = Math.toRadians(angleDegrees);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vector[0];
        double y = vector[1];
        double z = vector[2];
        return switch (axis == null ? "" : axis) {
            case "x" -> new double[]{x, y * cos - z * sin, y * sin + z * cos};
            case "y" -> new double[]{x * cos - z * sin, y, x * sin + z * cos};
            case "z" -> new double[]{x * cos - y * sin, x * sin + y * cos, z};
            default -> vector;
        };
    }

    private static double[] rotateXZ(double x, double z, int degrees) {
        return switch (degrees) {
            case 90 -> new double[]{16.0D - z, x};
            case 180 -> new double[]{16.0D - x, 16.0D - z};
            case 270 -> new double[]{z, 16.0D - x};
            default -> new double[]{x, z};
        };
    }

    private static double[] rotateYZ(double y, double z, int degrees) {
        return switch (degrees) {
            case 90 -> new double[]{16.0D - z, y};
            case 180 -> new double[]{16.0D - y, 16.0D - z};
            case 270 -> new double[]{z, 16.0D - y};
            default -> new double[]{y, z};
        };
    }

    private static HorizontalFacing openDoorEdge(HorizontalFacing facing, boolean leftHinge) {
        return switch (facing) {
            case NORTH -> leftHinge ? HorizontalFacing.WEST : HorizontalFacing.EAST;
            case EAST -> leftHinge ? HorizontalFacing.NORTH : HorizontalFacing.SOUTH;
            case SOUTH -> leftHinge ? HorizontalFacing.EAST : HorizontalFacing.WEST;
            case WEST -> leftHinge ? HorizontalFacing.SOUTH : HorizontalFacing.NORTH;
        };
    }

    private static UnitBox doorPanelBox(HorizontalFacing edge) {
        return switch (edge) {
            case NORTH -> new UnitBox(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.1875f);
            case EAST -> new UnitBox(0.8125f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            case SOUTH -> new UnitBox(0.0f, 0.0f, 0.8125f, 1.0f, 1.0f, 1.0f);
            case WEST -> new UnitBox(0.0f, 0.0f, 0.0f, 0.1875f, 1.0f, 1.0f);
        };
    }

    private static void addBoxQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            UnitBox box,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        addBoundsQuads(builder, face, box.bounds(), atlas, visibleDirections);
    }

    private static void addBoundsQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoBlockModelBounds bounds,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections
    ) {
        addBoundsQuads(builder, face, bounds, atlas, visibleDirections, null);
    }

    private static void addBoundsQuads(
            MeshBuilder builder,
            EchoVoxelMeshFace face,
            EchoBlockModelBounds bounds,
            EchoClientTextureAtlas atlas,
            Set<EchoVoxelMeshDirection> visibleDirections,
            EchoBlockModelElement element
    ) {
        for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
            if (!modelFaceVisible(face, bounds, direction, visibleDirections, element, atlas)) {
                continue;
            }
            EchoClientTextureAtlas.AtlasEntry uv = atlas == null
                    ? new EchoClientTextureAtlas.AtlasEntry(0.0f, 0.0f, 1.0f, 1.0f)
                    : atlas.get(face, direction, element);
            int uvRotationDegrees = atlas == null ? 0 : atlas.uvRotationDegrees(face, direction, element);
            builder.addQuad(
                    offsetCorners(face, cornerOffsets(direction, bounds)),
                    direction.normalX(),
                    direction.normalY(),
                    direction.normalZ(),
                    uv,
                    faceArgb(face, direction, atlas, element),
                    uvRotationDegrees
            );
        }
    }

    private static Map<String, EnumSet<EchoVoxelMeshDirection>> visibleDirectionsByBlock(EchoVoxelChunkMesh source) {
        HashMap<String, EnumSet<EchoVoxelMeshDirection>> result = new HashMap<>();
        for (EchoVoxelMeshFace face : source.faces()) {
            result.computeIfAbsent(modelBlockKey(face), ignored -> EnumSet.noneOf(EchoVoxelMeshDirection.class))
                    .add(face.direction());
        }
        return result;
    }

    private static boolean modelFaceVisible(
            EchoBlockModelBounds bounds,
            EchoVoxelMeshDirection direction,
            Set<EchoVoxelMeshDirection> visibleDirections,
            EchoBlockModelElement element
    ) {
        if (visibleDirections == null || visibleDirections.isEmpty() || direction == null) {
            return true;
        }
        if (element != null) {
            EchoVoxelMeshDirection cullDirection = element.cullFaceForFace(faceName(direction))
                    .flatMap(EchoClientChunkMesh::directionFromFaceName)
                    .orElse(null);
            return cullDirection == null || visibleDirections.contains(cullDirection);
        }
        if (!outerBoundaryFace(bounds, direction)) {
            return true;
        }
        return visibleDirections.contains(direction);
    }

    private static boolean modelFaceVisible(
            EchoVoxelMeshFace face,
            EchoBlockModelBounds bounds,
            EchoVoxelMeshDirection direction,
            Set<EchoVoxelMeshDirection> visibleDirections,
            EchoBlockModelElement element,
            EchoClientTextureAtlas atlas
    ) {
        if (visibleDirections == null || visibleDirections.isEmpty() || direction == null) {
            return true;
        }
        if (element != null) {
            EchoVoxelMeshDirection modelDirection = atlas == null
                    ? direction
                    : atlas.modelFaceForElementMetadata(face, direction, element);
            EchoVoxelMeshDirection cullDirection = element.cullFaceForFace(faceName(modelDirection))
                    .flatMap(EchoClientChunkMesh::directionFromFaceName)
                    .map(cullFace -> atlas == null
                            ? cullFace
                            : atlas.worldFaceForElementCullFace(face, cullFace, element))
                    .orElse(null);
            return cullDirection == null || visibleDirections.contains(cullDirection);
        }
        if (!outerBoundaryFace(bounds, direction)) {
            return true;
        }
        return visibleDirections.contains(direction);
    }

    private static String faceName(EchoVoxelMeshDirection direction) {
        return direction.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static java.util.Optional<EchoVoxelMeshDirection> directionFromFaceName(String faceName) {
        if (faceName == null || faceName.isBlank()) {
            return java.util.Optional.empty();
        }
        return switch (faceName.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "up" -> java.util.Optional.of(EchoVoxelMeshDirection.UP);
            case "down" -> java.util.Optional.of(EchoVoxelMeshDirection.DOWN);
            case "north" -> java.util.Optional.of(EchoVoxelMeshDirection.NORTH);
            case "south" -> java.util.Optional.of(EchoVoxelMeshDirection.SOUTH);
            case "east" -> java.util.Optional.of(EchoVoxelMeshDirection.EAST);
            case "west" -> java.util.Optional.of(EchoVoxelMeshDirection.WEST);
            default -> java.util.Optional.empty();
        };
    }

    private static boolean outerBoundaryFace(EchoBlockModelBounds bounds, EchoVoxelMeshDirection direction) {
        EchoBlockModelBounds safeBounds = bounds == null ? EchoBlockModelBounds.fullCube() : bounds;
        float epsilon = 0.0001f;
        return switch (direction) {
            case UP -> safeBounds.maxYUnit() >= 1.0f - epsilon;
            case DOWN -> safeBounds.minYUnit() <= epsilon;
            case EAST -> safeBounds.maxXUnit() >= 1.0f - epsilon;
            case WEST -> safeBounds.minXUnit() <= epsilon;
            case SOUTH -> safeBounds.maxZUnit() >= 1.0f - epsilon;
            case NORTH -> safeBounds.minZUnit() <= epsilon;
        };
    }

    private static int faceArgb(
            EchoVoxelMeshFace face,
            EchoVoxelMeshDirection direction,
            EchoClientTextureAtlas atlas
    ) {
        return faceArgb(face, direction, atlas, null);
    }

    private static int faceArgb(
            EchoVoxelMeshFace face,
            EchoVoxelMeshDirection direction,
            EchoClientTextureAtlas atlas,
            EchoBlockModelElement element
    ) {
        if (face == null) {
            return 0xFFFFFFFF;
        }
        if (atlas != null && atlas.tintIndex(face, direction, element) >= 0) {
            return biomeTintArgb(face);
        }
        return face.material().argb();
    }

    private static int crossArgb(EchoVoxelMeshFace face, EchoClientTextureAtlas atlas) {
        if (face == null) {
            return 0xFFFFFFFF;
        }
        if (atlas != null && "tinted_cross".equals(atlas.modelTemplateKind(face))) {
            return biomeTintArgb(face);
        }
        return face.material().argb();
    }

    private static int biomeTintArgb(EchoVoxelMeshFace face) {
        if (face.material().biomeTinted()) {
            return 0xFF000000 | (face.material().biomeTintArgb() & 0x00FFFFFF);
        }
        return face.material().argb();
    }

    private static float[] offsetCorners(EchoVoxelMeshFace face, float[] corners) {
        float[] result = new float[corners.length];
        for (int index = 0; index < corners.length; index += 3) {
            result[index] = face.x() + corners[index];
            result[index + 1] = face.y() + corners[index + 1];
            result[index + 2] = face.z() + corners[index + 2];
        }
        return result;
    }

    private static String modelBlockKey(EchoVoxelMeshFace face) {
        return face.x() + "," + face.y() + "," + face.z()
                + "|" + face.material().materialId()
                + "|" + face.material().stateProperties();
    }

    private enum HorizontalFacing {
        NORTH,
        EAST,
        SOUTH,
        WEST;

        private static HorizontalFacing from(String value) {
            if (value == null) {
                return EAST;
            }
            return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
                case "north" -> NORTH;
                case "south" -> SOUTH;
                case "west" -> WEST;
                default -> EAST;
            };
        }

        private HorizontalFacing opposite() {
            return switch (this) {
                case NORTH -> SOUTH;
                case EAST -> WEST;
                case SOUTH -> NORTH;
                case WEST -> EAST;
            };
        }

        private HorizontalFacing left() {
            return switch (this) {
                case NORTH -> WEST;
                case EAST -> NORTH;
                case SOUTH -> EAST;
                case WEST -> SOUTH;
            };
        }

        private HorizontalFacing right() {
            return switch (this) {
                case NORTH -> EAST;
                case EAST -> SOUTH;
                case SOUTH -> WEST;
                case WEST -> NORTH;
            };
        }
    }

    private record Footprint(float minX, float minZ, float maxX, float maxZ) {
        private static Footprint half(HorizontalFacing facing) {
            return switch (facing) {
                case NORTH -> new Footprint(0.0f, 0.0f, 1.0f, 0.5f);
                case EAST -> new Footprint(0.5f, 0.0f, 1.0f, 1.0f);
                case SOUTH -> new Footprint(0.0f, 0.5f, 1.0f, 1.0f);
                case WEST -> new Footprint(0.0f, 0.0f, 0.5f, 1.0f);
            };
        }

        private Footprint intersect(Footprint other) {
            return new Footprint(
                    Math.max(minX, other.minX),
                    Math.max(minZ, other.minZ),
                    Math.min(maxX, other.maxX),
                    Math.min(maxZ, other.maxZ)
            );
        }

        private UnitBox box(float minY, float maxY) {
            return new UnitBox(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    private record UnitBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        private EchoBlockModelBounds bounds() {
            return new EchoBlockModelBounds(
                    minX * 16.0D,
                    minY * 16.0D,
                    minZ * 16.0D,
                    maxX * 16.0D,
                    maxY * 16.0D,
                    maxZ * 16.0D
            );
        }
    }

    record MeshData(float[] vertices, int[] indices) {
        MeshData {
            vertices = vertices == null ? new float[0] : vertices;
            indices = indices == null ? new int[0] : indices;
        }

        static MeshData empty() {
            return new MeshData(new float[0], new int[0]);
        }

        int vertexCount() {
            return vertices.length / FLOATS_PER_VERTEX;
        }

        int indexCount() {
            return indices.length;
        }
    }

    record DynamicBufferUploadPlan(boolean grow, int requestedBytes, int capacityBytes) {
        DynamicBufferUploadPlan {
            requestedBytes = Math.max(1, requestedBytes);
            capacityBytes = Math.max(requestedBytes, capacityBytes);
        }
    }

    private static final class MeshBuilder {
        private final ArrayList<Float> vertices = new ArrayList<>();
        private final ArrayList<Integer> indices = new ArrayList<>();

        private void addQuad(
                float[] corners,
                float nx,
                float ny,
                float nz,
                EchoClientTextureAtlas.AtlasEntry uv,
                int argb
        ) {
            addQuad(corners, nx, ny, nz, uv, argb, 0);
        }

        private void addQuad(
                float[] corners,
                float nx,
                float ny,
                float nz,
                EchoClientTextureAtlas.AtlasEntry uv,
                int argb,
                int uvRotationDegrees
        ) {
            int baseVertex = vertices.size() / FLOATS_PER_VERTEX;
            float r = ((argb >>> 16) & 0xFF) / 255.0f;
            float g = ((argb >>> 8) & 0xFF) / 255.0f;
            float b = (argb & 0xFF) / 255.0f;
            int rotationSteps = Math.floorMod(uvRotationDegrees, 360) / 90;
            for (int corner = 0; corner < 4; corner++) {
                float ao = 1.0f - (corner == 1 || corner == 2 ? 0.06f : 0.0f);
                int uvCorner = Math.floorMod(corner - rotationSteps, 4);
                float u = (uvCorner == 0 || uvCorner == 3) ? uv.u1() : uv.u2();
                float v = (uvCorner == 0 || uvCorner == 1) ? uv.v1() : uv.v2();
                vertices.add(corners[corner * 3]);
                vertices.add(corners[corner * 3 + 1]);
                vertices.add(corners[corner * 3 + 2]);
                vertices.add(u);
                vertices.add(v);
                vertices.add(nx);
                vertices.add(ny);
                vertices.add(nz);
                vertices.add(r * ao);
                vertices.add(g * ao);
                vertices.add(b * ao);
                vertices.add(1.0f);
            }
            indices.add(baseVertex);
            indices.add(baseVertex + 1);
            indices.add(baseVertex + 2);
            indices.add(baseVertex);
            indices.add(baseVertex + 2);
            indices.add(baseVertex + 3);
        }

        private MeshData build() {
            float[] vertexArray = new float[vertices.size()];
            for (int index = 0; index < vertices.size(); index++) {
                vertexArray[index] = vertices.get(index);
            }
            int[] indexArray = new int[indices.size()];
            for (int index = 0; index < indices.size(); index++) {
                indexArray[index] = indices.get(index);
            }
            return new MeshData(vertexArray, indexArray);
        }
    }
}
