package dev.echo.standalone.runtime.gameplay;

import java.util.ArrayList;
import java.util.List;

public final class EchoProgressionState {
    private static final int EXPERIENCE_PER_LEVEL = 50;

    private int experience;
    private final ArrayList<String> milestones = new ArrayList<>();

    public synchronized void awardExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        experience += amount;
    }

    public synchronized void addMilestone(String milestoneId) {
        String normalized = EchoGameplayText.requireText(milestoneId, "milestoneId");
        if (!milestones.contains(normalized)) {
            milestones.add(normalized);
        }
    }

    public synchronized int experience() {
        return experience;
    }

    public synchronized int level() {
        return 1 + experience / EXPERIENCE_PER_LEVEL;
    }

    public synchronized List<String> milestones() {
        return List.copyOf(milestones);
    }
}
