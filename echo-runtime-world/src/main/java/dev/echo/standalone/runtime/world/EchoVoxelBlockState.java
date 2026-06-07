package dev.echo.standalone.runtime.world;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public record EchoVoxelBlockState(
        EchoVoxelBlock block,
        Map<String, String> properties,
        long tickVersion
) {
    public static final EchoVoxelBlockState AIR = new EchoVoxelBlockState(EchoVoxelBlock.AIR, Map.of(), 0L);

    public EchoVoxelBlockState {
        block = Objects.requireNonNull(block, "block");
        Objects.requireNonNull(properties, "properties");
        properties = Map.copyOf(new TreeMap<>(properties));
        if (tickVersion < 0L) {
            throw new IllegalArgumentException("tickVersion must not be negative");
        }
    }

    public static EchoVoxelBlockState of(EchoVoxelBlock block) {
        Objects.requireNonNull(block, "block");
        return block.air() ? AIR : new EchoVoxelBlockState(block, Map.of(), 0L);
    }

    public boolean air() {
        return block.air();
    }

    public Optional<String> property(String key) {
        return Optional.ofNullable(properties.get(requireKey(key)));
    }

    public EchoVoxelBlockState withProperty(String key, String value) {
        String normalizedKey = requireKey(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
        TreeMap<String, String> next = new TreeMap<>(properties);
        next.put(normalizedKey, value.trim());
        return new EchoVoxelBlockState(block, next, tickVersion);
    }

    public EchoVoxelBlockState ticked() {
        return new EchoVoxelBlockState(block, properties, tickVersion + 1L);
    }

    private static String requireKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key must not be blank");
        }
        return key.trim();
    }
}
