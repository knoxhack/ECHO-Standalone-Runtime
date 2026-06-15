package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Boolean blockstate property, e.g. {@code powered}, {@code lit}, {@code waterlogged}.
 */
public final class EchoBlockPropertyBoolean implements EchoBlockPropertyContract<Boolean> {

    private final String name;
    private final boolean defaultValue;

    public EchoBlockPropertyBoolean(String name) {
        this(name, false);
    }

    public EchoBlockPropertyBoolean(String name, boolean defaultValue) {
        this.name = EchoBlockStateIds.requireText(name, "name");
        this.defaultValue = defaultValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<Boolean> type() {
        return Boolean.class;
    }

    @Override
    public Collection<Boolean> values() {
        return List.of(Boolean.FALSE, Boolean.TRUE);
    }

    @Override
    public Boolean defaultValue() {
        return defaultValue;
    }

    @Override
    public Boolean parse(String value) {
        Objects.requireNonNull(value, "value");
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "true" -> Boolean.TRUE;
            case "false" -> Boolean.FALSE;
            default -> throw new IllegalArgumentException("Invalid boolean value '" + value + "' for property " + name);
        };
    }

    @Override
    public String serialize(Boolean value) {
        return requireValid(value).toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
