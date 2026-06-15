package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable blockstate implementing {@link EchoBlockStateContract}.
 *
 * <p>Property values are stored in a map keyed by property contract. The stable global state ID
 * is computed by the owning block's registry assignment.
 */
public final class EchoBlockState implements EchoBlockStateContract {

    private final EchoBlock block;
    private final LinkedHashMap<EchoBlockPropertyContract<?>, Object> values;
    private final int stableId;

    EchoBlockState(EchoBlock block, LinkedHashMap<EchoBlockPropertyContract<?>, Object> values, int stableId) {
        this.block = Objects.requireNonNull(block, "block");
        this.values = values;
        this.stableId = stableId;
    }

    @Override
    public EchoBlock block() {
        return block;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T value(EchoBlockPropertyContract<T> property) {
        Objects.requireNonNull(property, "property");
        if (!values.containsKey(property)) {
            throw new IllegalArgumentException("Property " + property.name() + " is not defined for block " + block.id());
        }
        return (T) values.get(property);
    }

    @Override
    public <T> EchoBlockState with(EchoBlockPropertyContract<T> property, T value) {
        Objects.requireNonNull(property, "property");
        if (!values.containsKey(property)) {
            throw new IllegalArgumentException("Property " + property.name() + " is not defined for block " + block.id());
        }
        property.requireValid(value);
        LinkedHashMap<EchoBlockPropertyContract<?>, Object> next = new LinkedHashMap<>(values);
        next.put(property, value);
        return block.state(next);
    }

    @Override
    public int stableId() {
        return stableId;
    }

    @Override
    public boolean air() {
        return block.air();
    }

    public Map<EchoBlockPropertyContract<?>, Object> values() {
        return Map.copyOf(values);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoBlockState other)) {
            return false;
        }
        return stableId == other.stableId && block.id().equals(other.block.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(block.id(), stableId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(block.id());
        if (!values.isEmpty()) {
            sb.append('[');
            boolean first = true;
            for (Map.Entry<EchoBlockPropertyContract<?>, Object> entry : values.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(entry.getKey().name()).append('=').append(entry.getKey().serialize(unchecked(entry.getValue())));
                first = false;
            }
            sb.append(']');
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T> T unchecked(Object value) {
        return (T) value;
    }
}
