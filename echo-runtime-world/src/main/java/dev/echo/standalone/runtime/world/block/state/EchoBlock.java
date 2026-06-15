package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable block type implementing {@link EchoBlockContract}.
 *
 * <p>Each block has an ordered property list and a precomputed state table. The global state ID
 * of any state is {@code stateBaseId + localStateIndex}, where {@code stateBaseId} is assigned by
 * {@link EchoBlockRegistry} on freeze.
 */
public final class EchoBlock implements EchoBlockContract {

    private final String id;
    private final String displayName;
    private final boolean air;
    private final List<EchoBlockPropertyContract<?>> properties;
    private final int stateBaseId;
    private final int stateCount;
    private final EchoBlockState defaultState;

    EchoBlock(String id, String displayName, boolean air,
              List<EchoBlockPropertyContract<?>> properties, int stateBaseId) {
        this.id = id;
        this.displayName = displayName;
        this.air = air;
        this.properties = properties;
        this.stateBaseId = stateBaseId;
        this.stateCount = computeStateCount(properties);
        this.defaultState = new EchoBlockState(this, defaultValues(properties), stateBaseId + 0);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public boolean air() {
        return air;
    }

    @Override
    public List<EchoBlockPropertyContract<?>> properties() {
        return properties;
    }

    @Override
    public EchoBlockState defaultState() {
        return defaultState;
    }

    public int stateBaseId() {
        return stateBaseId;
    }

    @Override
    public int stateCount() {
        return stateCount;
    }

    /**
     * Returns the state matching the supplied property values.
     */
    public EchoBlockState state(Map<EchoBlockPropertyContract<?>, Object> values) {
        int localIndex = localIndex(values);
        return new EchoBlockState(this, copyValues(values), stateBaseId + localIndex);
    }

    /**
     * Returns the state matching the supplied property name/value map.
     */
    public EchoBlockState stateFromStrings(Map<String, String> values) {
        LinkedHashMap<EchoBlockPropertyContract<?>, Object> parsed = new LinkedHashMap<>();
        for (EchoBlockPropertyContract<?> property : properties) {
            String raw = values.get(property.name());
            Object value = raw == null ? property.defaultValue() : property.parse(raw);
            parsed.put(property, value);
        }
        return state(parsed);
    }

    int localIndex(Map<EchoBlockPropertyContract<?>, Object> values) {
        int index = 0;
        int multiplier = 1;
        for (int i = properties.size() - 1; i >= 0; i--) {
            EchoBlockPropertyContract<?> property = properties.get(i);
            Object value = values.get(property);
            if (value == null) {
                value = property.defaultValue();
            }
            int valueIndex = valueIndex(property, value);
            index += valueIndex * multiplier;
            multiplier *= property.values().size();
        }
        return index;
    }

    private int valueIndex(EchoBlockPropertyContract<?> property, Object value) {
        int index = 0;
        for (Object candidate : property.values()) {
            if (Objects.equals(candidate, value)) {
                return index;
            }
            index++;
        }
        throw new IllegalArgumentException(
                "Value " + property.serialize(unchecked(value)) + " is not valid for property " + property.name()
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T unchecked(Object value) {
        return (T) value;
    }

    private static int computeStateCount(List<EchoBlockPropertyContract<?>> properties) {
        int count = 1;
        for (EchoBlockPropertyContract<?> property : properties) {
            count *= property.values().size();
        }
        return count;
    }

    private static LinkedHashMap<EchoBlockPropertyContract<?>, Object> defaultValues(
            List<EchoBlockPropertyContract<?>> properties) {
        LinkedHashMap<EchoBlockPropertyContract<?>, Object> values = new LinkedHashMap<>();
        for (EchoBlockPropertyContract<?> property : properties) {
            values.put(property, property.defaultValue());
        }
        return values;
    }

    private static LinkedHashMap<EchoBlockPropertyContract<?>, Object> copyValues(
            Map<EchoBlockPropertyContract<?>, Object> values) {
        LinkedHashMap<EchoBlockPropertyContract<?>, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<EchoBlockPropertyContract<?>, Object> entry : values.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }
        return copy;
    }

    @Override
    public String toString() {
        return id;
    }
}
