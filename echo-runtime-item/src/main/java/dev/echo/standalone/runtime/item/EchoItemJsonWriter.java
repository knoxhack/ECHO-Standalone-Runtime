package dev.echo.standalone.runtime.item;

final class EchoItemJsonWriter {
    private EchoItemJsonWriter() {
    }

    static String summary(EchoItemRegistry registry, EchoInventoryStore store) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.item_inventory_summary.v1", true, 2);
        numberField(builder, "itemDefinitions", registry.count(), true, 2);
        numberField(builder, "inventories", store.count(), true, 2);
        numberField(builder, "occupiedSlots", store.occupiedSlots(), false, 2);
        builder.append("}\n");
        return builder.toString();
    }

    static String container(EchoInventoryContainer container) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", "echo.standalone.inventory_container.v1", true, 2);
        field(builder, "inventoryId", container.id().value(), true, 2);
        field(builder, "label", container.label(), true, 2);
        field(builder, "ownerEntityId", container.ownerEntityId().map(value -> value.value()).orElse(""), true, 2);
        numberField(builder, "capacity", container.capacity(), true, 2);
        numberField(builder, "occupiedSlots", container.occupiedSlots(), true, 2);
        builder.append("  \"slots\": [\n");
        int written = 0;
        for (EchoInventorySlot slot : container.slots()) {
            if (slot.stack().isEmpty()) {
                continue;
            }
            if (written > 0) {
                builder.append(",\n");
            }
            EchoItemStack stack = slot.stack().orElseThrow();
            builder.append("    {");
            inlineNumberField(builder, "slot", slot.index(), true);
            inlineField(builder, "itemId", stack.itemId().value(), true);
            inlineNumberField(builder, "quantity", stack.quantity(), false);
            builder.append("}");
            written++;
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

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
