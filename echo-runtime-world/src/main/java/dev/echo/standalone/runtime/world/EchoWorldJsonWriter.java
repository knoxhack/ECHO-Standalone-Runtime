package dev.echo.standalone.runtime.world;

final class EchoWorldJsonWriter {
    private EchoWorldJsonWriter() {
    }

    static String summary(EchoWorldState world) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "worldId", world.worldId(), true, 2);
        numberField(builder, "seed", world.seed(), true, 2);
        numberField(builder, "tick", world.tick(), true, 2);
        numberField(builder, "dimensions", world.dimensionCount(), true, 2);
        numberField(builder, "regions", world.regions().size(), true, 2);
        numberField(builder, "chunks", world.chunks().size(), true, 2);
        numberField(builder, "cells", world.cellCount(), true, 2);
        numberField(builder, "hazards", world.hazardCount(), true, 2);
        numberField(builder, "pointsOfInterest", world.poiCount(), false, 2);
        builder.append("}\n");
        return builder.toString();
    }

    static String chunk(EchoWorldChunk chunk) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "chunkId", chunk.id().key(), true, 2);
        field(builder, "regionId", chunk.regionId(), true, 2);
        field(builder, "weatherProfile", chunk.weather().profileId(), true, 2);
        numberField(builder, "temperatureCelsius", chunk.weather().temperatureCelsius(), true, 2);
        numberField(builder, "ashDensity", chunk.weather().ashDensity(), true, 2);
        numberField(builder, "cells", chunk.cells().size(), true, 2);
        numberField(builder, "hazards", chunk.hazards().size(), true, 2);
        numberField(builder, "pointsOfInterest", chunk.pointsOfInterest().size(), false, 2);
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

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
