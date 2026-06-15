package dev.echo.standalone.runtime.nbt;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * NBT compound tag: an ordered map of named tags.
 */
public final class EchoNbtCompound implements EchoNbtTag {

    private final LinkedHashMap<String, EchoNbtTag> tags;

    public EchoNbtCompound(Map<String, ? extends EchoNbtTag> tags) {
        if (tags == null || tags.isEmpty()) {
            this.tags = new LinkedHashMap<>();
        } else {
            this.tags = new LinkedHashMap<>(tags.size());
            for (Map.Entry<String, ? extends EchoNbtTag> entry : tags.entrySet()) {
                String key = entry.getKey();
                EchoNbtTag value = entry.getValue();
                if (key == null || key.isEmpty()) {
                    throw new IllegalArgumentException("Compound tag key must not be blank");
                }
                if (value == null) {
                    throw new IllegalArgumentException("Compound tag value must not be null");
                }
                this.tags.put(key, value);
            }
        }
    }

    public EchoNbtCompound() {
        this.tags = new LinkedHashMap<>();
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.COMPOUND;
    }

    public Map<String, EchoNbtTag> tags() {
        return Collections.unmodifiableMap(tags);
    }

    public boolean contains(String key) {
        return tags.containsKey(key);
    }

    public Optional<EchoNbtTag> get(String key) {
        return Optional.ofNullable(tags.get(key));
    }

    public EchoNbtTag require(String key) {
        EchoNbtTag tag = tags.get(key);
        if (tag == null) {
            throw new IllegalArgumentException("Missing NBT tag: " + key);
        }
        return tag;
    }

    @SuppressWarnings("unchecked")
    public <T extends EchoNbtTag> Optional<T> get(String key, Class<T> expected) {
        EchoNbtTag tag = tags.get(key);
        if (tag == null || !expected.isInstance(tag)) {
            return Optional.empty();
        }
        return Optional.of((T) tag);
    }

    public boolean getBoolean(String key) {
        return get(key, EchoNbtByte.class).map(EchoNbtByte::asBoolean).orElse(false);
    }

    public byte getByte(String key) {
        return get(key, EchoNbtByte.class).map(EchoNbtByte::value).orElse((byte) 0);
    }

    public short getShort(String key) {
        return get(key, EchoNbtShort.class).map(EchoNbtShort::value).orElse((short) 0);
    }

    public int getInt(String key) {
        return get(key, EchoNbtInt.class).map(EchoNbtInt::value).orElse(0);
    }

    public long getLong(String key) {
        return get(key, EchoNbtLong.class).map(EchoNbtLong::value).orElse(0L);
    }

    public float getFloat(String key) {
        return get(key, EchoNbtFloat.class).map(EchoNbtFloat::value).orElse(0.0F);
    }

    public double getDouble(String key) {
        return get(key, EchoNbtDouble.class).map(EchoNbtDouble::value).orElse(0.0D);
    }

    public String getString(String key) {
        return get(key, EchoNbtString.class).map(EchoNbtString::value).orElse("");
    }

    public EchoNbtCompound getCompound(String key) {
        return get(key, EchoNbtCompound.class).orElseGet(EchoNbtCompound::new);
    }

    public EchoNbtList getList(String key) {
        return get(key, EchoNbtList.class).orElseGet(EchoNbtList::empty);
    }

    public EchoNbtCompound put(String key, EchoNbtTag tag) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Compound tag key must not be blank");
        }
        if (tag == null) {
            throw new IllegalArgumentException("Compound tag value must not be null");
        }
        tags.put(key, tag);
        return this;
    }

    public EchoNbtCompound put(String key, boolean value) {
        return put(key, new EchoNbtByte(value));
    }

    public EchoNbtCompound put(String key, byte value) {
        return put(key, new EchoNbtByte(value));
    }

    public EchoNbtCompound put(String key, short value) {
        return put(key, new EchoNbtShort(value));
    }

    public EchoNbtCompound put(String key, int value) {
        return put(key, new EchoNbtInt(value));
    }

    public EchoNbtCompound put(String key, long value) {
        return put(key, new EchoNbtLong(value));
    }

    public EchoNbtCompound put(String key, float value) {
        return put(key, new EchoNbtFloat(value));
    }

    public EchoNbtCompound put(String key, double value) {
        return put(key, new EchoNbtDouble(value));
    }

    public EchoNbtCompound put(String key, String value) {
        return put(key, new EchoNbtString(value));
    }

    public EchoNbtCompound put(String key, byte[] value) {
        return put(key, new EchoNbtByteArray(value));
    }

    public EchoNbtCompound put(String key, int[] value) {
        return put(key, new EchoNbtIntArray(value));
    }

    public EchoNbtCompound put(String key, long[] value) {
        return put(key, new EchoNbtLongArray(value));
    }

    public EchoNbtCompound remove(String key) {
        tags.remove(key);
        return this;
    }

    public boolean isEmpty() {
        return tags.isEmpty();
    }

    public int size() {
        return tags.size();
    }

    public List<String> keys() {
        return List.copyOf(tags.keySet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoNbtCompound other)) {
            return false;
        }
        return tags.equals(other.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tags);
    }

    @Override
    public String toString() {
        return "EchoNbtCompound{" + tags.size() + " tags}";
    }
}
