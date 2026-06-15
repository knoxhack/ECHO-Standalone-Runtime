package dev.echo.standalone.runtime.world.chunk;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Palette for compressing a small set of repeated values in a chunk section.
 *
 * <p>Minecraft-style palettes support three internal modes:
 * <ul>
 *   <li><b>Single-value:</b> every entry is the same value.</li>
 *   <li><b>Linear:</b> values stored in a list; indices fit in few bits.</li>
 *   <li><b>Hash:</b> values stored in a map for O(1) lookup.</li>
 * </ul>
 *
 * @param <T> the value type
 */
public final class EchoPalette<T> {

    private final Mode mode;
    private final List<T> values;
    private final Map<T, Integer> index;
    private final T singleValue;

    private EchoPalette(Mode mode, List<T> values, Map<T, Integer> index, T singleValue) {
        this.mode = mode;
        this.values = values;
        this.index = index;
        this.singleValue = singleValue;
    }

    public static <T> EchoPalette<T> empty() {
        return new EchoPalette<>(Mode.SINGLE, List.of(), Map.of(), null);
    }

    public static <T> EchoPalette<T> of(T singleValue) {
        Objects.requireNonNull(singleValue, "singleValue");
        return new EchoPalette<>(Mode.SINGLE, List.of(), Map.of(), singleValue);
    }

    public int idFor(T value) {
        Objects.requireNonNull(value, "value");
        return switch (mode) {
            case SINGLE -> {
                if (!value.equals(singleValue)) {
                    throw new IllegalArgumentException("Value " + value + " is not the single palette value");
                }
                yield 0;
            }
            case LINEAR -> {
                int id = values.indexOf(value);
                if (id < 0) {
                    throw new IllegalArgumentException("Value " + value + " is not in palette");
                }
                yield id;
            }
            case HASH -> {
                Integer id = index.get(value);
                if (id == null) {
                    throw new IllegalArgumentException("Value " + value + " is not in palette");
                }
                yield id;
            }
        };
    }

    public T valueFor(int id) {
        return switch (mode) {
            case SINGLE -> {
                if (id != 0) {
                    throw new IndexOutOfBoundsException("Palette index " + id + " out of bounds for single-value palette");
                }
                yield singleValue;
            }
            case LINEAR, HASH -> {
                if (id < 0 || id >= values.size()) {
                    throw new IndexOutOfBoundsException("Palette index " + id + " out of bounds [0, " + values.size() + ")");
                }
                yield values.get(id);
            }
        };
    }

    public int size() {
        return switch (mode) {
            case SINGLE -> singleValue == null ? 0 : 1;
            case LINEAR, HASH -> values.size();
        };
    }

    public boolean contains(T value) {
        return switch (mode) {
            case SINGLE -> value.equals(singleValue);
            case LINEAR -> values.contains(value);
            case HASH -> index.containsKey(value);
        };
    }

    /**
     * Returns a new palette that also contains {@code value}. The current palette is unchanged.
     */
    public EchoPalette<T> with(T value) {
        Objects.requireNonNull(value, "value");
        if (contains(value)) {
            return this;
        }
        return switch (mode) {
            case SINGLE -> {
                if (singleValue == null) {
                    yield of(value);
                }
                ArrayList<T> list = new ArrayList<>(2);
                list.add(singleValue);
                list.add(value);
                yield new EchoPalette<>(Mode.LINEAR, List.copyOf(list), Map.of(singleValue, 0, value, 1), null);
            }
            case LINEAR -> {
                if (values.size() < 16) {
                    ArrayList<T> list = new ArrayList<>(values.size() + 1);
                    list.addAll(values);
                    list.add(value);
                    yield new EchoPalette<>(Mode.LINEAR, List.copyOf(list), buildIndex(list), null);
                }
                ArrayList<T> list = new ArrayList<>(values.size() + 1);
                list.addAll(values);
                list.add(value);
                yield new EchoPalette<>(Mode.HASH, List.copyOf(list), buildIndex(list), null);
            }
            case HASH -> {
                ArrayList<T> list = new ArrayList<>(values.size() + 1);
                list.addAll(values);
                list.add(value);
                yield new EchoPalette<>(Mode.HASH, List.copyOf(list), buildIndex(list), null);
            }
        };
    }

    public List<T> values() {
        return switch (mode) {
            case SINGLE -> singleValue == null ? List.of() : List.of(singleValue);
            case LINEAR, HASH -> List.copyOf(values);
        };
    }

    private static <T> Map<T, Integer> buildIndex(List<T> list) {
        LinkedHashMap<T, Integer> map = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i);
        }
        return Map.copyOf(map);
    }

    public int minimumBits() {
        int size = size();
        if (size <= 1) {
            return 1;
        }
        return 32 - Integer.numberOfLeadingZeros(size - 1);
    }

    private enum Mode {
        SINGLE,
        LINEAR,
        HASH
    }
}
