package dev.echo.standalone.runtime.client;

record EchoClientPlayerVitals(
        int currentHealth,
        int maxHealth,
        int lastDamage,
        int foodLevel,
        double saturation,
        double exhaustion,
        double survivalTickSeconds
) {
    static final int DEFAULT_MAX_HEALTH = 20;
    static final int DEFAULT_MAX_FOOD = 20;
    private static final double EXHAUSTION_THRESHOLD = 4.0D;
    private static final double SURVIVAL_TICK_INTERVAL_SECONDS = 4.0D;
    private static final double WALK_EXHAUSTION_PER_SECOND = 0.005D;
    private static final double SPRINT_EXHAUSTION_PER_SECOND = 0.05D;
    private static final double JUMP_EXHAUSTION = 0.05D;
    private static final double REGEN_EXHAUSTION = 1.5D;

    EchoClientPlayerVitals(int currentHealth, int maxHealth, int lastDamage) {
        this(currentHealth, maxHealth, lastDamage, DEFAULT_MAX_FOOD, 5.0D, 0.0D, 0.0D);
    }

    EchoClientPlayerVitals {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        currentHealth = Math.max(0, Math.min(maxHealth, currentHealth));
        lastDamage = Math.max(0, lastDamage);
        foodLevel = Math.max(0, Math.min(DEFAULT_MAX_FOOD, foodLevel));
        saturation = clamp(saturation, 0.0D, foodLevel);
        exhaustion = Math.max(0.0D, exhaustion);
        survivalTickSeconds = Math.max(0.0D, survivalTickSeconds);
    }

    static EchoClientPlayerVitals full() {
        return new EchoClientPlayerVitals(DEFAULT_MAX_HEALTH, DEFAULT_MAX_HEALTH, 0);
    }

    EchoClientPlayerVitals damage(int amount) {
        int damage = Math.max(0, amount);
        return withHealth(currentHealth - damage, damage);
    }

    EchoClientPlayerVitals heal(int amount) {
        int healing = Math.max(0, amount);
        return withHealth(currentHealth + healing, 0);
    }

    EchoClientPlayerVitals eat(int nutrition, double saturationModifier) {
        int safeNutrition = Math.max(0, nutrition);
        double safeModifier = Math.max(0.0D, saturationModifier);
        int nextFood = Math.min(DEFAULT_MAX_FOOD, foodLevel + safeNutrition);
        double gainedSaturation = safeNutrition * safeModifier * 2.0D;
        double nextSaturation = Math.min(nextFood, saturation + gainedSaturation);
        return new EchoClientPlayerVitals(
                currentHealth,
                maxHealth,
                0,
                nextFood,
                nextSaturation,
                exhaustion,
                survivalTickSeconds
        );
    }

    EchoClientPlayerVitals exhaust(double amount) {
        return applyExhaustion(Math.max(0.0D, amount), 0.0D);
    }

    EchoClientPlayerVitals tickSurvival(
            double deltaSeconds,
            boolean moving,
            boolean sprinting,
            boolean jumping
    ) {
        if (!alive()) {
            return this;
        }
        double movementExhaustion = 0.0D;
        double dt = Math.max(0.0D, deltaSeconds);
        if (moving) {
            movementExhaustion += (sprinting ? SPRINT_EXHAUSTION_PER_SECOND : WALK_EXHAUSTION_PER_SECOND) * dt;
        }
        if (jumping) {
            movementExhaustion += JUMP_EXHAUSTION;
        }
        return applyExhaustion(movementExhaustion, dt);
    }

    boolean alive() {
        return currentHealth > 0;
    }

    int heartSlots() {
        return (int) Math.ceil(maxHealth / 2.0D);
    }

    int filledHeartSlots() {
        return currentHealth / 2;
    }

    boolean halfHeartAt(int slot) {
        if (slot < 0) {
            return false;
        }
        return currentHealth % 2 == 1 && slot == filledHeartSlots();
    }

    int foodSlots() {
        return DEFAULT_MAX_FOOD / 2;
    }

    int filledFoodSlots() {
        return foodLevel / 2;
    }

    boolean halfFoodAt(int slot) {
        if (slot < 0) {
            return false;
        }
        return foodLevel % 2 == 1 && slot == filledFoodSlots();
    }

    boolean canRegenerate() {
        return foodLevel >= 18 && currentHealth > 0 && currentHealth < maxHealth;
    }

    boolean starving() {
        return foodLevel <= 0 && currentHealth > 0;
    }

    private EchoClientPlayerVitals applyExhaustion(double addedExhaustion, double deltaSeconds) {
        int nextHealth = currentHealth;
        int nextLastDamage = lastDamage;
        int nextFood = foodLevel;
        double nextSaturation = saturation;
        double nextExhaustion = exhaustion + addedExhaustion;
        while (nextExhaustion >= EXHAUSTION_THRESHOLD) {
            nextExhaustion -= EXHAUSTION_THRESHOLD;
            if (nextSaturation > 0.0D) {
                nextSaturation = Math.max(0.0D, nextSaturation - 1.0D);
            } else {
                nextFood = Math.max(0, nextFood - 1);
            }
        }

        double nextTimer = survivalTickSeconds + Math.max(0.0D, deltaSeconds);
        while (nextTimer >= SURVIVAL_TICK_INTERVAL_SECONDS) {
            nextTimer -= SURVIVAL_TICK_INTERVAL_SECONDS;
            if (nextFood >= 18 && nextHealth > 0 && nextHealth < maxHealth) {
                nextHealth = Math.min(maxHealth, nextHealth + 1);
                nextLastDamage = 0;
                nextExhaustion += REGEN_EXHAUSTION;
            } else if (nextFood <= 0 && nextHealth > 0) {
                nextHealth = Math.max(0, nextHealth - 1);
                nextLastDamage = 1;
            }
        }

        return new EchoClientPlayerVitals(
                nextHealth,
                maxHealth,
                nextLastDamage,
                nextFood,
                nextSaturation,
                nextExhaustion,
                nextTimer
        );
    }

    private EchoClientPlayerVitals withHealth(int health, int damage) {
        return new EchoClientPlayerVitals(
                health,
                maxHealth,
                damage,
                foodLevel,
                saturation,
                exhaustion,
                survivalTickSeconds
        );
    }

    private static double clamp(double value, double minimum, double maximum) {
        if (!Double.isFinite(value)) {
            return minimum;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }
}
