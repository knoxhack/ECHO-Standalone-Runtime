package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.List;

record EchoClientProgressionState(
        int experience,
        int lastAward,
        List<String> milestones
) {
    static final int EXPERIENCE_PER_LEVEL = 50;

    EchoClientProgressionState {
        experience = Math.max(0, experience);
        lastAward = Math.max(0, lastAward);
        ArrayList<String> cleanMilestones = new ArrayList<>();
        if (milestones != null) {
            for (String milestone : milestones) {
                if (milestone != null && !milestone.isBlank() && !cleanMilestones.contains(milestone)) {
                    cleanMilestones.add(milestone);
                }
            }
        }
        milestones = List.copyOf(cleanMilestones);
    }

    static EchoClientProgressionState empty() {
        return new EchoClientProgressionState(0, 0, List.of());
    }

    EchoClientProgressionState awardExperience(int amount, String milestone) {
        int safeAmount = Math.max(0, amount);
        if (safeAmount == 0) {
            return this;
        }
        ArrayList<String> nextMilestones = new ArrayList<>(milestones);
        if (milestone != null && !milestone.isBlank() && !nextMilestones.contains(milestone)) {
            nextMilestones.add(milestone);
        }
        return new EchoClientProgressionState(experience + safeAmount, safeAmount, nextMilestones);
    }

    int level() {
        return 1 + experience / EXPERIENCE_PER_LEVEL;
    }

    int experienceIntoLevel() {
        return experience % EXPERIENCE_PER_LEVEL;
    }

    int experienceForNextLevel() {
        return EXPERIENCE_PER_LEVEL;
    }

    double progressToNextLevel() {
        return experienceIntoLevel() / (double) EXPERIENCE_PER_LEVEL;
    }
}
