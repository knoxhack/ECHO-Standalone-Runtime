package dev.echo.standalone.runtime.nbt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NBT list tag. All elements must share the same tag type.
 */
public final class EchoNbtList implements EchoNbtTag {

    private final EchoNbtTagType elementType;
    private final List<EchoNbtTag> elements;

    public EchoNbtList(EchoNbtTagType elementType, List<? extends EchoNbtTag> elements) {
        this.elementType = Objects.requireNonNull(elementType, "elementType");
        if (elementType == EchoNbtTagType.END && !elements.isEmpty()) {
            throw new IllegalArgumentException("End-typed list must be empty");
        }
        for (EchoNbtTag element : elements) {
            if (element.type() != elementType) {
                throw new IllegalArgumentException(
                        "List element type " + element.type() + " does not match list type " + elementType
                );
            }
        }
        this.elements = List.copyOf(elements);
    }

    public static EchoNbtList empty() {
        return new EchoNbtList(EchoNbtTagType.END, List.of());
    }

    public static EchoNbtList of(EchoNbtTag... elements) {
        if (elements.length == 0) {
            return empty();
        }
        EchoNbtTagType type = elements[0].type();
        return new EchoNbtList(type, List.of(elements));
    }

    @Override
    public EchoNbtTagType type() {
        return EchoNbtTagType.LIST;
    }

    public EchoNbtTagType elementType() {
        return elementType;
    }

    public List<EchoNbtTag> elements() {
        return elements;
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public EchoNbtTag get(int index) {
        return elements.get(index);
    }

    @SuppressWarnings("unchecked")
    public <T extends EchoNbtTag> T get(int index, Class<T> expected) {
        EchoNbtTag tag = elements.get(index);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(
                    "Expected " + expected.getSimpleName() + " but found " + tag.type()
            );
        }
        return (T) tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EchoNbtList other)) {
            return false;
        }
        return elementType == other.elementType && elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType, elements);
    }

    @Override
    public String toString() {
        return "EchoNbtList{" + elementType + ", " + elements.size() + " elements}";
    }
}
