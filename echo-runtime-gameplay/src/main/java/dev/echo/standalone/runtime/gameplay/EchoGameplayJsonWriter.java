package dev.echo.standalone.runtime.gameplay;

final class EchoGameplayJsonWriter {
    private EchoGameplayJsonWriter() {
    }

    static String summary(
            EchoGameplayMissionState mission,
            EchoSurvivalState survival,
            EchoProgressionState progression,
            EchoFactionRuntime factions,
            EchoNotificationLog notifications
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.gameplay_summary.v1", true, 2);
        field(builder, "missionId", mission.missionId(), true, 2);
        field(builder, "missionStatus", mission.status().name(), true, 2);
        numberField(builder, "missionProgressPercent", mission.progressPercent(), true, 2);
        numberField(builder, "level", progression.level(), true, 2);
        numberField(builder, "hydration", survival.hydration(), true, 2);
        numberField(builder, "ashExposure", survival.ashExposure(), true, 2);
        numberField(builder, "factions", factions.count(), true, 2);
        numberField(builder, "notifications", notifications.count(), false, 2);
        builder.append("}\n");
        return builder.toString();
    }

    static String mission(EchoGameplayMissionState mission) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.gameplay_mission.v1", true, 2);
        field(builder, "missionId", mission.missionId(), true, 2);
        field(builder, "title", mission.title(), true, 2);
        field(builder, "status", mission.status().name(), true, 2);
        numberField(builder, "progressPercent", mission.progressPercent(), true, 2);
        builder.append("  \"objectives\": [\n");
        int index = 0;
        for (EchoGameplayMissionObjective objective : mission.objectives()) {
            if (index > 0) {
                builder.append(",\n");
            }
            builder.append("    {");
            inlineField(builder, "objectiveId", objective.objectiveId(), true);
            inlineField(builder, "status", objective.status().name(), true);
            inlineNumberField(builder, "progress", objective.progress(), true);
            inlineNumberField(builder, "targetProgress", objective.targetProgress(), false);
            builder.append("}");
            index++;
        }
        builder.append("\n  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    static String survival(EchoSurvivalState survival) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.gameplay_survival.v1", true, 2);
        field(builder, "playerId", survival.playerId().value(), true, 2);
        numberField(builder, "hydration", survival.hydration(), true, 2);
        numberField(builder, "ashExposure", survival.ashExposure(), true, 2);
        numberField(builder, "heatStress", survival.heatStress(), false, 2);
        builder.append("}\n");
        return builder.toString();
    }

    static String progression(EchoProgressionState progression) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.gameplay_progression.v1", true, 2);
        numberField(builder, "experience", progression.experience(), true, 2);
        numberField(builder, "level", progression.level(), true, 2);
        builder.append("  \"milestones\": [");
        for (int index = 0; index < progression.milestones().size(); index++) {
            if (index > 0) {
                builder.append(", ");
            }
            builder.append("\"").append(escape(progression.milestones().get(index))).append("\"");
        }
        builder.append("]\n");
        builder.append("}\n");
        return builder.toString();
    }

    static String factions(EchoFactionRuntime factions) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.gameplay_factions.v1", true, 2);
        builder.append("  \"factions\": [\n");
        int index = 0;
        for (EchoFactionStanding standing : factions.all()) {
            if (index > 0) {
                builder.append(",\n");
            }
            builder.append("    {");
            inlineField(builder, "factionId", standing.factionId(), true);
            inlineField(builder, "displayName", standing.displayName(), true);
            inlineNumberField(builder, "reputation", standing.reputation(), true);
            inlineBooleanField(builder, "hostile", standing.hostile(), false);
            builder.append("}");
            index++;
        }
        builder.append("\n  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    static String notifications(EchoNotificationLog notifications) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.gameplay_notifications.v1", true, 2);
        builder.append("  \"notifications\": [\n");
        int index = 0;
        for (EchoGameplayNotification notification : notifications.all()) {
            if (index > 0) {
                builder.append(",\n");
            }
            builder.append("    {");
            inlineField(builder, "notificationId", notification.notificationId(), true);
            inlineField(builder, "severity", notification.severity().name(), true);
            inlineField(builder, "message", notification.message(), true);
            inlineNumberField(builder, "tick", notification.tick(), false);
            builder.append("}");
            index++;
        }
        builder.append("\n  ]\n");
        builder.append("}\n");
        return builder.toString();
    }

    private static void field(StringBuilder builder, String key, String value, boolean comma, int indent) {
        builder.append(" ".repeat(indent))
                .append("\"")
                .append(escape(key))
                .append("\": \"")
                .append(escape(value))
                .append("\"");
        builder.append(comma ? ",\n" : "\n");
    }

    private static void numberField(StringBuilder builder, String key, long value, boolean comma, int indent) {
        builder.append(" ".repeat(indent))
                .append("\"")
                .append(escape(key))
                .append("\": ")
                .append(value);
        builder.append(comma ? ",\n" : "\n");
    }

    private static void numberField(StringBuilder builder, String key, double value, boolean comma, int indent) {
        builder.append(" ".repeat(indent))
                .append("\"")
                .append(escape(key))
                .append("\": ")
                .append(String.format(java.util.Locale.ROOT, "%.2f", value));
        builder.append(comma ? ",\n" : "\n");
    }

    private static void inlineField(StringBuilder builder, String key, String value, boolean comma) {
        builder.append("\"")
                .append(escape(key))
                .append("\": \"")
                .append(escape(value))
                .append("\"");
        if (comma) {
            builder.append(", ");
        }
    }

    private static void inlineNumberField(StringBuilder builder, String key, long value, boolean comma) {
        builder.append("\"")
                .append(escape(key))
                .append("\": ")
                .append(value);
        if (comma) {
            builder.append(", ");
        }
    }

    private static void inlineBooleanField(StringBuilder builder, String key, boolean value, boolean comma) {
        builder.append("\"")
                .append(escape(key))
                .append("\": ")
                .append(value);
        if (comma) {
            builder.append(", ");
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
