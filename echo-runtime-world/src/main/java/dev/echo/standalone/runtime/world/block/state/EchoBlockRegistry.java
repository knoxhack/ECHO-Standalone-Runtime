package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Runtime registry for blocks. Assigns stable runtime IDs and global state IDs on freeze.
 *
 * <p>Registration order determines runtime IDs. Once frozen, the registry exposes immutable
 * blocks and supports stable global state IDs for placement, serialization, and rendering.
 */
public final class EchoBlockRegistry {

    public static final String AIR_ID = "echo:air";

    private final LinkedHashMap<String, Builder> builders = new LinkedHashMap<>();
    private final LinkedHashMap<String, EchoBlock> blocks = new LinkedHashMap<>();
    private boolean frozen;
    private int totalStateCount;

    public EchoBlockRegistry() {
        // Air is always registered first with no properties and state count 1.
        register(AIR_ID, "Air", List.of());
    }

    public Builder register(String id, String displayName) {
        return register(id, displayName, List.of());
    }

    public Builder register(String id, String displayName, Collection<? extends EchoBlockPropertyContract<?>> properties) {
        ensureMutable();
        String normalizedId = EchoBlockStateIds.normalizeId(id);
        if (builders.containsKey(normalizedId)) {
            throw new IllegalArgumentException("Block already registered: " + normalizedId);
        }
        Builder builder = new Builder(this, normalizedId, displayName, new ArrayList<>(properties));
        builders.put(normalizedId, builder);
        return builder;
    }

    public void freeze() {
        ensureMutable();
        int stateBaseId = 0;
        for (Builder builder : builders.values()) {
            EchoBlock block = builder.build(stateBaseId);
            blocks.put(block.id(), block);
            stateBaseId += block.stateCount();
        }
        this.totalStateCount = stateBaseId;
        this.frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    public Optional<EchoBlockContract> find(String id) {
        return Optional.ofNullable(blocks.get(EchoBlockStateIds.normalizeId(id)));
    }

    public EchoBlock require(String id) {
        return blocks.get(EchoBlockStateIds.normalizeId(id));
    }

    public List<EchoBlock> blocks() {
        ensureFrozen();
        return List.copyOf(blocks.values());
    }

    public int blockCount() {
        return blocks.size();
    }

    public int totalStateCount() {
        ensureFrozen();
        return totalStateCount;
    }

    public EchoBlockState air() {
        ensureFrozen();
        return blocks.get(AIR_ID).defaultState();
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Block registry is frozen");
        }
    }

    private void ensureFrozen() {
        if (!frozen) {
            throw new IllegalStateException("Block registry is not frozen");
        }
    }

    public static final class Builder {
        private final EchoBlockRegistry registry;
        private final String id;
        private final String displayName;
        private final List<EchoBlockPropertyContract<?>> properties;
        private boolean air;

        private Builder(EchoBlockRegistry registry, String id, String displayName,
                        List<EchoBlockPropertyContract<?>> properties) {
            this.registry = registry;
            this.id = id;
            this.displayName = displayName;
            this.properties = properties;
            this.air = AIR_ID.equals(id);
        }

        public Builder air(boolean air) {
            this.air = air;
            return this;
        }

        public Builder property(EchoBlockPropertyContract<?> property) {
            registry.ensureMutable();
            Objects.requireNonNull(property, "property");
            properties.add(property);
            return this;
        }

        public Builder property(EchoBlockPropertyContract<?>... properties) {
            registry.ensureMutable();
            for (EchoBlockPropertyContract<?> property : properties) {
                Objects.requireNonNull(property, "property");
                this.properties.add(property);
            }
            return this;
        }

        private EchoBlock build(int stateBaseId) {
            return new EchoBlock(id, displayName, air, List.copyOf(properties), stateBaseId);
        }
    }
}
