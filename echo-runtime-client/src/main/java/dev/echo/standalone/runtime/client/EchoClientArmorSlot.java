package dev.echo.standalone.runtime.client;

enum EchoClientArmorSlot {
    HEAD("head"),
    CHEST("chest"),
    LEGS("legs"),
    FEET("feet");

    private final String id;

    EchoClientArmorSlot(String id) {
        this.id = id;
    }

    String id() {
        return id;
    }

    static EchoClientArmorSlot parse(String value) {
        if (value == null || value.isBlank()) {
            return CHEST;
        }
        String normalized = value.trim().toLowerCase(java.util.Locale.ROOT);
        for (EchoClientArmorSlot slot : values()) {
            if (slot.id.equals(normalized) || slot.name().equalsIgnoreCase(normalized)) {
                return slot;
            }
        }
        return CHEST;
    }
}
