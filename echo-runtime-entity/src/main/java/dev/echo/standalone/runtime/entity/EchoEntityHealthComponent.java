package dev.echo.standalone.runtime.entity;

public record EchoEntityHealthComponent(int currentHealth, int maxHealth) {
    public EchoEntityHealthComponent {
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        if (currentHealth < 0 || currentHealth > maxHealth) {
            throw new IllegalArgumentException("currentHealth must be between zero and maxHealth");
        }
    }

    public boolean alive() {
        return currentHealth > 0;
    }

    public EchoEntityHealthComponent damage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        return new EchoEntityHealthComponent(Math.max(0, currentHealth - amount), maxHealth);
    }

    public EchoEntityHealthComponent heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        return new EchoEntityHealthComponent(Math.min(maxHealth, currentHealth + amount), maxHealth);
    }
}
