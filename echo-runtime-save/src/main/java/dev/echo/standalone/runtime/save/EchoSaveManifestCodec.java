package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class EchoSaveManifestCodec {
    public EchoSaveManifest read(Path path) throws IOException {
        return parse(Files.readString(path));
    }

    public void write(Path path, EchoSaveManifest manifest) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, writeToString(manifest));
    }

    @SuppressWarnings("unchecked")
    public EchoSaveManifest parse(String text) {
        Object value = EchoSaveJson.parse(text);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Save manifest must be a JSON object");
        }
        Map<String, Object> object = (Map<String, Object>) map;
        List<EchoSaveFileState> files = new ArrayList<>();
        Object fileValue = object.get("files");
        if (fileValue instanceof List<?> fileList) {
            for (Object item : fileList) {
                Map<String, Object> file = (Map<String, Object>) item;
                files.add(new EchoSaveFileState(
                        string(file, "relativePath"),
                        string(file, "checksumSha256"),
                        number(file, "bytes").longValue()
                ));
            }
        }
        List<String> backupIds = new ArrayList<>();
        Object backupValue = object.get("backupIds");
        if (backupValue instanceof List<?> backupList) {
            for (Object item : backupList) {
                backupIds.add(String.valueOf(item));
            }
        }
        Map<String, String> metadata = new LinkedHashMap<>();
        Object metadataValue = object.get("metadata");
        if (metadataValue instanceof Map<?, ?> metadataMap) {
            for (Map.Entry<?, ?> entry : metadataMap.entrySet()) {
                metadata.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        return new EchoSaveManifest(
                string(object, "schema"),
                string(object, "profileId"),
                string(object, "slotId"),
                string(object, "packId"),
                number(object, "formatVersion").intValue(),
                string(object, "createdAt"),
                string(object, "updatedAt"),
                files,
                backupIds,
                metadata
        );
    }

    public String writeToString(EchoSaveManifest manifest) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\n");
        field(builder, "schema", manifest.schema(), true);
        field(builder, "profileId", manifest.profileId(), true);
        field(builder, "slotId", manifest.slotId(), true);
        field(builder, "packId", manifest.packId(), true);
        numberField(builder, "formatVersion", manifest.formatVersion(), true);
        field(builder, "createdAt", manifest.createdAt(), true);
        field(builder, "updatedAt", manifest.updatedAt(), true);
        builder.append("  \"files\": [\n");
        for (int index = 0; index < manifest.files().size(); index++) {
            EchoSaveFileState file = manifest.files().get(index);
            builder.append("    {\n");
            field(builder, "relativePath", file.relativePath(), true, 6);
            field(builder, "checksumSha256", file.checksumSha256(), true, 6);
            numberField(builder, "bytes", file.bytes(), false, 6);
            builder.append("    }");
            builder.append(index + 1 < manifest.files().size() ? ",\n" : "\n");
        }
        builder.append("  ],\n");
        builder.append("  \"backupIds\": [");
        for (int index = 0; index < manifest.backupIds().size(); index++) {
            builder.append("\"").append(escape(manifest.backupIds().get(index))).append("\"");
            if (index + 1 < manifest.backupIds().size()) {
                builder.append(", ");
            }
        }
        builder.append("],\n");
        builder.append("  \"metadata\": ");
        appendStringMap(builder, manifest.metadata());
        builder.append("\n}\n");
        return builder.toString();
    }

    private static String string(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing manifest field: " + key);
        }
        return String.valueOf(value);
    }

    private static Number number(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Missing numeric manifest field: " + key);
        }
        return number;
    }

    private static void field(StringBuilder builder, String key, String value, boolean comma) {
        field(builder, key, value, comma, 2);
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

    private static void numberField(StringBuilder builder, String key, long value, boolean comma) {
        numberField(builder, key, value, comma, 2);
    }

    private static void numberField(StringBuilder builder, String key, long value, boolean comma, int indent) {
        builder.append(" ".repeat(indent))
                .append("\"")
                .append(escape(key))
                .append("\": ")
                .append(value);
        builder.append(comma ? ",\n" : "\n");
    }

    private static void appendStringMap(StringBuilder builder, Map<String, String> map) {
        TreeMap<String, String> sorted = new TreeMap<>(map);
        builder.append("{");
        int index = 0;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            builder.append("\"")
                    .append(escape(entry.getKey()))
                    .append("\": \"")
                    .append(escape(entry.getValue()))
                    .append("\"");
            if (index + 1 < sorted.size()) {
                builder.append(", ");
            }
            index++;
        }
        builder.append("}");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
