package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Objects;

final class EchoClientEntityRenderer {
    static final int VERTEX_STRIDE_FLOATS = 10;
    static final int HUMANOID_VERTEX_COUNT = 48;
    static final int HUMANOID_INDEX_COUNT = 72;
    static final int DROPPED_ITEM_VERTEX_COUNT = 24;
    static final int DROPPED_ITEM_INDEX_COUNT = 36;
    static final int PARTICLE_VERTEX_COUNT = 24;
    static final int PARTICLE_INDEX_COUNT = 36;

    private static final int STRIDE = VERTEX_STRIDE_FLOATS * Float.BYTES;
    private static final int POS_OFFSET = 0;
    private static final int NORM_OFFSET = 3 * Float.BYTES;
    private static final int COLOR_OFFSET = 6 * Float.BYTES;
    private static final int MIN_DYNAMIC_BUFFER_BYTES = 256;

    private final EchoClientShader shader;
    private final int uProjection;
    private final int uView;
    private final int uLightDir;
    private final int uFogColor;
    private final int uFogDensity;

    private final RenderSection entitySection = new RenderSection();
    private final RenderSection droppedItemSection = new RenderSection();
    private final RenderSection particleSection = new RenderSection();

    EchoClientEntityRenderer() {
        shader = new EchoClientShader("/shaders/entity.vert", "/shaders/entity.frag");
        shader.use();
        uProjection = shader.uniform("uProjection");
        uView = shader.uniform("uView");
        uLightDir = shader.uniform("uLightDir");
        uFogColor = shader.uniform("uFogColor");
        uFogDensity = shader.uniform("uFogDensity");
        GL20.glUseProgram(0);
    }

    void render(
            EchoVoxelCamera camera,
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems,
            float[] projectionMatrix,
            float[] viewMatrix,
            EchoClientBiomeEnvironment environment,
            boolean fogEnabled
    ) {
        render(camera, entities, entityCatalog, droppedItems, List.of(), projectionMatrix, viewMatrix,
                environment, fogEnabled);
    }

    void render(
            EchoVoxelCamera camera,
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems,
            List<EchoClientParticle> particles,
            float[] projectionMatrix,
            float[] viewMatrix,
            EchoClientBiomeEnvironment environment,
            boolean fogEnabled
    ) {
        Objects.requireNonNull(camera, "camera");
        Objects.requireNonNull(projectionMatrix, "projectionMatrix");
        Objects.requireNonNull(viewMatrix, "viewMatrix");
        EchoClientBiomeEnvironment activeEnvironment =
                environment == null ? EchoClientBiomeEnvironment.DEFAULT : environment;
        int entitySignature = entityMeshSignature(entities, entityCatalog);
        if (sectionNeedsUpload(entitySection, entitySignature)) {
            entitySection.upload(entitySignature, entityMeshData(entities, entityCatalog));
        }
        int droppedItemSignature = droppedItemMeshSignature(droppedItems);
        if (sectionNeedsUpload(droppedItemSection, droppedItemSignature)) {
            droppedItemSection.upload(droppedItemSignature, droppedItemMeshData(droppedItems));
        }
        int particleSignature = particleMeshSignature(particles);
        if (sectionNeedsUpload(particleSection, particleSignature)) {
            particleSection.upload(particleSignature, particleMeshData(particles));
        }
        if (!entitySection.visible() && !droppedItemSection.visible() && !particleSection.visible()) {
            return;
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);

        shader.use();
        shader.setMat4(uProjection, projectionMatrix);
        shader.setMat4(uView, viewMatrix);
        shader.setVec3(uLightDir, 0.3f, 0.8f, 0.2f);
        shader.setVec3(
                uFogColor,
                activeEnvironment.fogRed(),
                activeEnvironment.fogGreen(),
                activeEnvironment.fogBlue()
        );
        shader.setFloat(uFogDensity, fogEnabled ? activeEnvironment.fogDensity() : 0.0f);

        entitySection.draw();
        droppedItemSection.draw();
        particleSection.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glUseProgram(0);
    }

    void delete() {
        entitySection.delete();
        droppedItemSection.delete();
        particleSection.delete();
        shader.delete();
    }

    static MeshData meshData(List<EchoEntityState> entities, EchoClientEntityCatalog entityCatalog) {
        return meshData(entities, entityCatalog, List.of());
    }

    static MeshData meshData(
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems
    ) {
        return meshData(entities, entityCatalog, droppedItems, List.of());
    }

    static MeshData meshData(
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems,
            List<EchoClientParticle> particles
    ) {
        List<EchoEntityState> safeEntities = entities == null ? List.of() : entities;
        List<EchoClientDroppedItem> safeDrops = droppedItems == null ? List.of() : droppedItems;
        List<EchoClientParticle> safeParticles = particles == null ? List.of() : particles;
        if (safeEntities.isEmpty() && safeDrops.isEmpty() && safeParticles.isEmpty()) {
            return MeshData.EMPTY;
        }
        EchoClientEntityCatalog catalog = entityCatalog == null
                ? EchoClientEntityCatalog.empty()
                : entityCatalog;
        MeshBuilder builder = new MeshBuilder(
                safeEntities.size() * HUMANOID_VERTEX_COUNT
                        + safeDrops.size() * DROPPED_ITEM_VERTEX_COUNT
                        + safeParticles.size() * PARTICLE_VERTEX_COUNT
        );
        for (EchoEntityState entity : safeEntities) {
            if (entity != null && entity.alive()) {
                appendEntity(builder, entity, catalog);
            }
        }
        for (EchoClientDroppedItem drop : safeDrops) {
            if (drop != null && drop.quantity() > 0) {
                appendDroppedItem(builder, drop);
            }
        }
        for (EchoClientParticle particle : safeParticles) {
            if (particle != null && particle.alive()) {
                appendParticle(builder, particle);
            }
        }
        return builder.build();
    }

    static MeshData entityMeshData(List<EchoEntityState> entities, EchoClientEntityCatalog entityCatalog) {
        List<EchoEntityState> safeEntities = entities == null ? List.of() : entities;
        if (safeEntities.isEmpty()) {
            return MeshData.EMPTY;
        }
        EchoClientEntityCatalog catalog = entityCatalog == null
                ? EchoClientEntityCatalog.empty()
                : entityCatalog;
        MeshBuilder builder = new MeshBuilder(safeEntities.size() * HUMANOID_VERTEX_COUNT);
        for (EchoEntityState entity : safeEntities) {
            if (entity != null && entity.alive()) {
                appendEntity(builder, entity, catalog);
            }
        }
        return builder.build();
    }

    static MeshData droppedItemMeshData(List<EchoClientDroppedItem> droppedItems) {
        List<EchoClientDroppedItem> safeDrops = droppedItems == null ? List.of() : droppedItems;
        if (safeDrops.isEmpty()) {
            return MeshData.EMPTY;
        }
        MeshBuilder builder = new MeshBuilder(safeDrops.size() * DROPPED_ITEM_VERTEX_COUNT);
        for (EchoClientDroppedItem drop : safeDrops) {
            if (drop != null && drop.quantity() > 0) {
                appendDroppedItem(builder, drop);
            }
        }
        return builder.build();
    }

    static MeshData particleMeshData(List<EchoClientParticle> particles) {
        List<EchoClientParticle> safeParticles = particles == null ? List.of() : particles;
        if (safeParticles.isEmpty()) {
            return MeshData.EMPTY;
        }
        MeshBuilder builder = new MeshBuilder(safeParticles.size() * PARTICLE_VERTEX_COUNT);
        for (EchoClientParticle particle : safeParticles) {
            if (particle != null && particle.alive()) {
                appendParticle(builder, particle);
            }
        }
        return builder.build();
    }

    static int argbForDefinition(String definitionId, EchoClientEntityCatalog entityCatalog) {
        EchoClientEntityCatalog catalog = entityCatalog == null
                ? EchoClientEntityCatalog.empty()
                : entityCatalog;
        return catalog.renderProfile(definitionId).argb();
    }

    static int meshSignature(List<EchoEntityState> entities, EchoClientEntityCatalog entityCatalog) {
        return meshSignature(entities, entityCatalog, List.of());
    }

    static int meshSignature(
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems
    ) {
        return meshSignature(entities, entityCatalog, droppedItems, List.of());
    }

    static int meshSignature(
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems,
            List<EchoClientParticle> particles
    ) {
        int signature = 17;
        signature = 31 * signature + entityMeshSignature(entities, entityCatalog);
        signature = 31 * signature + droppedItemMeshSignature(droppedItems);
        signature = 31 * signature + particleMeshSignature(particles);
        return signature;
    }

    static int entityMeshSignature(List<EchoEntityState> entities, EchoClientEntityCatalog entityCatalog) {
        List<EchoEntityState> safeEntities = entities == null ? List.of() : entities;
        int signature = 19;
        signature = 31 * signature + System.identityHashCode(entityCatalog);
        for (EchoEntityState entity : safeEntities) {
            if (entity == null) {
                signature = 31 * signature;
                continue;
            }
            EchoWorldPosition position = entity.worldPosition();
            signature = 31 * signature + entity.id().value().hashCode();
            signature = 31 * signature + entity.definition().definitionId().hashCode();
            signature = 31 * signature + position.x();
            signature = 31 * signature + position.y();
            signature = 31 * signature + position.z();
            signature = 31 * signature + entity.health().currentHealth();
            signature = 31 * signature + (entity.alive() ? 1 : 0);
            signature = 31 * signature + entity.ai().state().ordinal();
        }
        return signature;
    }

    static int droppedItemMeshSignature(List<EchoClientDroppedItem> droppedItems) {
        List<EchoClientDroppedItem> safeDrops = droppedItems == null ? List.of() : droppedItems;
        int signature = 23;
        for (EchoClientDroppedItem drop : safeDrops) {
            if (drop == null) {
                signature = 31 * signature;
                continue;
            }
            signature = 31 * signature + drop.dropId().hashCode();
            signature = 31 * signature + drop.itemId().value().hashCode();
            signature = 31 * signature + drop.quantity();
            signature = 31 * signature + bucket(drop.x());
            signature = 31 * signature + bucket(drop.y());
            signature = 31 * signature + bucket(drop.z());
        }
        return signature;
    }

    static int particleMeshSignature(List<EchoClientParticle> particles) {
        List<EchoClientParticle> safeParticles = particles == null ? List.of() : particles;
        int signature = 29;
        for (EchoClientParticle particle : safeParticles) {
            if (particle == null) {
                signature = 31 * signature;
                continue;
            }
            signature = 31 * signature + particle.particleId().hashCode();
            signature = 31 * signature + particle.kind().ordinal();
            signature = 31 * signature + bucket(particle.x());
            signature = 31 * signature + bucket(particle.y());
            signature = 31 * signature + bucket(particle.z());
            signature = 31 * signature + bucket(particle.renderSize());
            signature = 31 * signature + particle.renderArgb();
        }
        return signature;
    }

    private static boolean sectionNeedsUpload(RenderSection section, int signature) {
        return section.uploadedSignature() != signature;
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

    private static void appendEntity(
            MeshBuilder builder,
            EchoEntityState entity,
            EchoClientEntityCatalog entityCatalog
    ) {
        EchoWorldPosition position = entity.worldPosition();
        float x = position.x() + 0.5f;
        float y = position.y();
        float z = position.z() + 0.5f;
        EchoClientEntityCatalog.RenderProfile renderProfile =
                entityCatalog.renderProfile(entity.definition().definitionId());
        int color = renderProfile.argb();
        if (renderProfile.shape() == EchoClientEntityCatalog.RenderShape.SLIME) {
            addCuboid(builder, x, y, z, 0.82f, 0.62f, 0.82f, color);
        } else if (renderProfile.shape() == EchoClientEntityCatalog.RenderShape.DRONE) {
            addCuboid(builder, x, y + 0.8f, z, 0.86f, 0.52f, 0.86f, color);
            addCuboid(builder, x, y + 0.8f, z, 1.24f, 0.16f, 0.28f, darken(color, 0.72f));
            addCuboid(builder, x, y + 0.8f, z, 0.28f, 0.16f, 1.24f, darken(color, 0.72f));
        } else {
            addCuboid(builder, x, y + 0.64f, z, 0.62f, 1.28f, 0.62f, color);
            addCuboid(builder, x, y + 1.50f, z, 0.54f, 0.54f, 0.54f, lighten(color, 1.14f));
        }
    }

    private static void appendDroppedItem(MeshBuilder builder, EchoClientDroppedItem drop) {
        float restOffset = (float) (Math.sin(drop.dropId().hashCode() * 0.01D) * 0.018D);
        float scale = (float) Math.min(0.46D, 0.30D + Math.log1p(drop.quantity()) * 0.035D);
        addCuboid(
                builder,
                (float) drop.x(),
                (float) drop.y() + 0.28f + restOffset,
                (float) drop.z(),
                scale,
                scale * 0.62f,
                scale,
                argbForDroppedItem(drop)
        );
    }

    static int argbForDroppedItem(EchoClientDroppedItem drop) {
        String itemId = drop == null ? "" : drop.itemId().value();
        int hash = itemId.hashCode();
        int r = 84 + Math.floorMod(hash, 128);
        int g = 92 + Math.floorMod(hash >>> 8, 112);
        int b = 108 + Math.floorMod(hash >>> 16, 104);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static void appendParticle(MeshBuilder builder, EchoClientParticle particle) {
        float size = (float) particle.renderSize();
        addCuboid(
                builder,
                (float) particle.x(),
                (float) particle.y(),
                (float) particle.z(),
                size,
                size,
                size,
                particle.renderArgb()
        );
    }

    private static void addCuboid(
            MeshBuilder builder,
            float centerX,
            float centerY,
            float centerZ,
            float width,
            float height,
            float depth,
            int argb
    ) {
        float minX = centerX - width * 0.5f;
        float maxX = centerX + width * 0.5f;
        float minY = centerY - height * 0.5f;
        float maxY = centerY + height * 0.5f;
        float minZ = centerZ - depth * 0.5f;
        float maxZ = centerZ + depth * 0.5f;
        face(builder, argb, 0.0f, 1.0f, 0.0f,
                minX, maxY, minZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ);
        face(builder, argb, 1.0f, 0.0f, 0.0f,
                maxX, minY, maxZ, maxX, minY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ);
        face(builder, argb, -1.0f, 0.0f, 0.0f,
                minX, minY, minZ, minX, minY, maxZ, minX, maxY, maxZ, minX, maxY, minZ);
        face(builder, argb, 0.0f, 0.0f, 1.0f,
                minX, minY, maxZ, maxX, minY, maxZ, maxX, maxY, maxZ, minX, maxY, maxZ);
        face(builder, argb, 0.0f, 0.0f, -1.0f,
                maxX, minY, minZ, minX, minY, minZ, minX, maxY, minZ, maxX, maxY, minZ);
        face(builder, argb, 0.0f, -1.0f, 0.0f,
                minX, minY, minZ, maxX, minY, minZ, maxX, minY, maxZ, minX, minY, maxZ);
    }

    private static void face(
            MeshBuilder builder,
            int argb,
            float normalX,
            float normalY,
            float normalZ,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2,
            float x3,
            float y3,
            float z3
    ) {
        int base = builder.vertexCount();
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        float a = ((argb >>> 24) & 0xFF) / 255.0f;
        builder.vertex(x0, y0, z0, normalX, normalY, normalZ, r, g, b, a);
        builder.vertex(x1, y1, z1, normalX, normalY, normalZ, r, g, b, a);
        builder.vertex(x2, y2, z2, normalX, normalY, normalZ, r, g, b, a);
        builder.vertex(x3, y3, z3, normalX, normalY, normalZ, r, g, b, a);
        builder.index(base);
        builder.index(base + 1);
        builder.index(base + 2);
        builder.index(base);
        builder.index(base + 2);
        builder.index(base + 3);
    }

    private static int lighten(int argb, float factor) {
        return transformRgb(argb, factor);
    }

    private static int darken(int argb, float factor) {
        return transformRgb(argb, factor);
    }

    private static int transformRgb(int argb, float factor) {
        int a = (argb >>> 24) & 0xFF;
        int r = clampColor(Math.round(((argb >>> 16) & 0xFF) * factor));
        int g = clampColor(Math.round(((argb >>> 8) & 0xFF) * factor));
        int b = clampColor(Math.round((argb & 0xFF) * factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static int bucket(double value) {
        return (int) Math.floor(value * 8.0D);
    }

    record MeshData(float[] vertices, int[] indices, int vertexCount, int indexCount) {
        static final MeshData EMPTY = new MeshData(new float[0], new int[0], 0, 0);
    }

    record DynamicBufferUploadPlan(boolean grow, int requestedBytes, int capacityBytes) {
        DynamicBufferUploadPlan {
            requestedBytes = Math.max(1, requestedBytes);
            capacityBytes = Math.max(requestedBytes, capacityBytes);
        }
    }

    private static final class RenderSection {
        private int vao;
        private int vbo;
        private int ibo;
        private int indexCount;
        private int uploadedSignature = Integer.MIN_VALUE;
        private int vertexBufferCapacityBytes;
        private int indexBufferCapacityBytes;
        private ByteBuffer vertexUploadBytes;
        private ByteBuffer indexUploadBytes;
        private FloatBuffer vertexUploadBuffer;
        private IntBuffer indexUploadBuffer;

        private int uploadedSignature() {
            return uploadedSignature;
        }

        private boolean visible() {
            return indexCount > 0;
        }

        private void upload(int signature, MeshData mesh) {
            MeshData safeMesh = mesh == null ? MeshData.EMPTY : mesh;
            uploadedSignature = signature;
            if (safeMesh.indexCount() <= 0) {
                indexCount = 0;
                return;
            }
            if (vao == 0) {
                vao = GL30.glGenVertexArrays();
                vbo = GL15.glGenBuffers();
                ibo = GL15.glGenBuffers();
            }
            indexCount = safeMesh.indexCount();

            GL30.glBindVertexArray(vao);

            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            FloatBuffer vertexBuffer = vertexUploadBuffer(safeMesh.vertices().length);
            vertexBuffer.put(safeMesh.vertices()).flip();
            DynamicBufferUploadPlan vertexPlan =
                    dynamicBufferUploadPlan(vertexBufferCapacityBytes, safeMesh.vertices().length * Float.BYTES);
            if (vertexPlan.grow()) {
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) vertexPlan.capacityBytes(), GL15.GL_DYNAMIC_DRAW);
                vertexBufferCapacityBytes = vertexPlan.capacityBytes();
            }
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, vertexBuffer);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
            IntBuffer indexBuffer = indexUploadBuffer(safeMesh.indices().length);
            indexBuffer.put(safeMesh.indices()).flip();
            DynamicBufferUploadPlan indexPlan =
                    dynamicBufferUploadPlan(indexBufferCapacityBytes, safeMesh.indices().length * Integer.BYTES);
            if (indexPlan.grow()) {
                GL15.glBufferData(
                        GL15.GL_ELEMENT_ARRAY_BUFFER,
                        (long) indexPlan.capacityBytes(),
                        GL15.GL_DYNAMIC_DRAW
                );
                indexBufferCapacityBytes = indexPlan.capacityBytes();
            }
            GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, 0L, indexBuffer);

            GL20.glEnableVertexAttribArray(0);
            GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, STRIDE, POS_OFFSET);
            GL20.glEnableVertexAttribArray(1);
            GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, STRIDE, NORM_OFFSET);
            GL20.glEnableVertexAttribArray(2);
            GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, STRIDE, COLOR_OFFSET);

            GL30.glBindVertexArray(0);
        }

        private void draw() {
            if (indexCount <= 0 || vao == 0) {
                return;
            }
            GL30.glBindVertexArray(vao);
            GL11.glDrawElements(GL11.GL_TRIANGLES, indexCount, GL11.GL_UNSIGNED_INT, 0);
            GL30.glBindVertexArray(0);
        }

        private void delete() {
            if (vao != 0) {
                GL30.glDeleteVertexArrays(vao);
                GL15.glDeleteBuffers(vbo);
                GL15.glDeleteBuffers(ibo);
                vao = 0;
                vbo = 0;
                ibo = 0;
            }
            indexCount = 0;
            uploadedSignature = Integer.MIN_VALUE;
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
    }

    private static final class MeshBuilder {
        private float[] vertices;
        private int[] indices;
        private int vertexOffset;
        private int indexOffset;

        private MeshBuilder(int expectedVertices) {
            vertices = new float[Math.max(1, expectedVertices) * VERTEX_STRIDE_FLOATS];
            indices = new int[Math.max(1, expectedVertices / 4 * 6)];
        }

        int vertexCount() {
            return vertexOffset / VERTEX_STRIDE_FLOATS;
        }

        void vertex(
                float x,
                float y,
                float z,
                float normalX,
                float normalY,
                float normalZ,
                float r,
                float g,
                float b,
                float a
        ) {
            ensureVertexCapacity(VERTEX_STRIDE_FLOATS);
            vertices[vertexOffset++] = x;
            vertices[vertexOffset++] = y;
            vertices[vertexOffset++] = z;
            vertices[vertexOffset++] = normalX;
            vertices[vertexOffset++] = normalY;
            vertices[vertexOffset++] = normalZ;
            vertices[vertexOffset++] = r;
            vertices[vertexOffset++] = g;
            vertices[vertexOffset++] = b;
            vertices[vertexOffset++] = a;
        }

        void index(int index) {
            ensureIndexCapacity(1);
            indices[indexOffset++] = index;
        }

        MeshData build() {
            if (vertexOffset == vertices.length && indexOffset == indices.length) {
                return new MeshData(vertices, indices, vertexCount(), indexOffset);
            }
            float[] exactVertices = new float[vertexOffset];
            System.arraycopy(vertices, 0, exactVertices, 0, vertexOffset);
            int[] exactIndices = new int[indexOffset];
            System.arraycopy(indices, 0, exactIndices, 0, indexOffset);
            return new MeshData(exactVertices, exactIndices, vertexCount(), indexOffset);
        }

        private void ensureVertexCapacity(int additionalFloats) {
            if (vertexOffset + additionalFloats <= vertices.length) {
                return;
            }
            float[] grown = new float[Math.max(vertices.length * 2, vertexOffset + additionalFloats)];
            System.arraycopy(vertices, 0, grown, 0, vertexOffset);
            vertices = grown;
        }

        private void ensureIndexCapacity(int additionalIndices) {
            if (indexOffset + additionalIndices <= indices.length) {
                return;
            }
            int[] grown = new int[Math.max(indices.length * 2, indexOffset + additionalIndices)];
            System.arraycopy(indices, 0, grown, 0, indexOffset);
            indices = grown;
        }
    }
}
