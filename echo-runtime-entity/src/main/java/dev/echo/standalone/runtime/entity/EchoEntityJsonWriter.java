package dev.echo.standalone.runtime.entity;

final class EchoEntityJsonWriter {
    private EchoEntityJsonWriter() {
    }

    static String summary(EchoEntityStore store) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.entity_summary.v1", true, 2);
        numberField(builder, "entityCount", store.count(), true, 2);
        numberField(builder, "livingEntities", store.living().size(), true, 2);
        numberField(builder, "hostileEntities", store.hostile().size(), false, 2);
        builder.append("}\n");
        return builder.toString();
    }

    static String entity(EchoEntityState entity) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.entity_state.v1", true, 2);
        field(builder, "entityId", entity.id().value(), true, 2);
        field(builder, "definitionId", entity.definition().definitionId(), true, 2);
        field(builder, "displayName", entity.definition().displayName(), true, 2);
        field(builder, "kind", entity.definition().kind().name(), true, 2);
        numberField(builder, "x", entity.worldPosition().x(), true, 2);
        numberField(builder, "y", entity.worldPosition().y(), true, 2);
        numberField(builder, "z", entity.worldPosition().z(), true, 2);
        numberField(builder, "currentHealth", entity.health().currentHealth(), true, 2);
        numberField(builder, "maxHealth", entity.health().maxHealth(), true, 2);
        numberField(builder, "movementSpeed", entity.movement().movementSpeed(), true, 2);
        booleanField(builder, "blockedByWorld", entity.movement().blockedByWorld(), true, 2);
        field(builder, "aiProfile", entity.ai().profile(), true, 2);
        field(builder, "aiState", entity.ai().state().name(), false, 2);
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

    private static void booleanField(StringBuilder builder, String key, boolean value, boolean comma, int indent) {
        builder.append(" ".repeat(indent))
                .append("\"")
                .append(escape(key))
                .append("\": ")
                .append(value);
        builder.append(comma ? ",\n" : "\n");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
