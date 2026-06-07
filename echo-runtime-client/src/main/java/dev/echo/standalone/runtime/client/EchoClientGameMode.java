package dev.echo.standalone.runtime.client;

enum EchoClientGameMode {
    SURVIVAL(true, true, true, true, true),
    CREATIVE(false, false, true, true, false),
    ADVENTURE(true, true, false, false, true);

    private final boolean takesDamage;
    private final boolean consumesPlacedItems;
    private final boolean allowsBlockBreaking;
    private final boolean allowsBlockPlacing;
    private final boolean ticksSurvival;

    EchoClientGameMode(
            boolean takesDamage,
            boolean consumesPlacedItems,
            boolean allowsBlockBreaking,
            boolean allowsBlockPlacing,
            boolean ticksSurvival
    ) {
        this.takesDamage = takesDamage;
        this.consumesPlacedItems = consumesPlacedItems;
        this.allowsBlockBreaking = allowsBlockBreaking;
        this.allowsBlockPlacing = allowsBlockPlacing;
        this.ticksSurvival = ticksSurvival;
    }

    boolean takesDamage() {
        return takesDamage;
    }

    boolean consumesPlacedItems() {
        return consumesPlacedItems;
    }

    boolean allowsBlockBreaking() {
        return allowsBlockBreaking;
    }

    boolean allowsBlockPlacing() {
        return allowsBlockPlacing;
    }

    boolean ticksSurvival() {
        return ticksSurvival;
    }

    static EchoClientGameMode parse(String value) {
        if (value == null || value.isBlank()) {
            return SURVIVAL;
        }
        try {
            return EchoClientGameMode.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return SURVIVAL;
        }
    }
}
