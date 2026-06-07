package dev.echo.standalone.runtime.compat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoNeoForgeMetadataParser {
    private static final String MOD_ID_PLACEHOLDER = "${mod_id}";
    private static final String MOD_NAME_PLACEHOLDER = "${mod_name}";

    public List<EchoNeoForgeModCandidate> parse(Path metadataPath) throws IOException {
        Objects.requireNonNull(metadataPath, "metadataPath");
        Path normalizedPath = metadataPath.toAbsolutePath().normalize();
        String inferredModuleId = inferModuleId(normalizedPath);
        Map<String, String> rootFields = new LinkedHashMap<>();
        ArrayList<Map<String, String>> modTables = new ArrayList<>();
        ArrayList<DependencyTable> dependencyTables = new ArrayList<>();
        Map<String, String> currentFields = rootFields;
        String currentDependencyOwner = "";
        boolean skippingTripleString = false;
        String tripleDelimiter = "";

        for (String rawLine : Files.readAllLines(normalizedPath)) {
            String line = rawLine.trim();
            if (skippingTripleString) {
                if (line.contains(tripleDelimiter)) {
                    skippingTripleString = false;
                    tripleDelimiter = "";
                }
                continue;
            }
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("[[") && line.endsWith("]]")) {
                String table = line.substring(2, line.length() - 2).trim();
                if (table.equals("mods")) {
                    currentFields = new LinkedHashMap<>();
                    modTables.add(currentFields);
                    currentDependencyOwner = "";
                } else if (table.startsWith("dependencies.")) {
                    currentFields = new LinkedHashMap<>();
                    currentDependencyOwner = table.substring("dependencies.".length()).trim();
                    dependencyTables.add(new DependencyTable(currentDependencyOwner, currentFields));
                } else {
                    currentFields = rootFields;
                    currentDependencyOwner = "";
                }
                continue;
            }
            int equalsIndex = line.indexOf('=');
            if (equalsIndex <= 0) {
                continue;
            }
            String key = line.substring(0, equalsIndex).trim();
            String valuePart = line.substring(equalsIndex + 1).trim();
            String triple = tripleDelimiter(valuePart);
            if (!triple.isBlank()) {
                currentFields.put(key, parseValue(valuePart));
                if (!closesTripleOnSameLine(valuePart, triple)) {
                    skippingTripleString = true;
                    tripleDelimiter = triple;
                }
                continue;
            }
            currentFields.put(key, parseValue(valuePart));
        }

        if (modTables.isEmpty()) {
            throw new IllegalArgumentException("NeoForge metadata has no [[mods]] table: " + normalizedPath);
        }

        ArrayList<EchoNeoForgeModCandidate> candidates = new ArrayList<>();
        for (Map<String, String> modTable : modTables) {
            String modId = templateAware(modTable.get("modId"), inferredModuleId, inferredModuleId);
            if (modId.isBlank()) {
                throw new IllegalArgumentException("NeoForge metadata mod table is missing modId: " + normalizedPath);
            }
            String displayName = templateAware(modTable.get("displayName"), inferredModuleId, modId);
            String version = templateAware(modTable.get("version"), inferredModuleId, modTable.get("version"));
            String license = templateAware(rootFields.get("license"), inferredModuleId, rootFields.get("license"));
            ArrayList<EchoNeoForgeDependency> dependencies = new ArrayList<>();
            ArrayList<String> warnings = new ArrayList<>();
            for (DependencyTable table : dependencyTables) {
                if (!appliesToMod(table.owner(), modId, modTables.size() == 1)) {
                    continue;
                }
                String dependencyModId = templateAware(table.fields().get("modId"), inferredModuleId, "");
                if (dependencyModId.isBlank()) {
                    warnings.add("dependency table missing modId for owner " + table.owner());
                    continue;
                }
                dependencies.add(new EchoNeoForgeDependency(
                        modId,
                        dependencyModId,
                        dependencyType(table.fields()),
                        table.fields().get("versionRange"),
                        table.fields().get("ordering"),
                        table.fields().get("side"),
                        table.fields().get("reason")
                ));
            }
            if (dependencies.isEmpty()) {
                warnings.add("no dependency metadata discovered");
            }
            candidates.add(new EchoNeoForgeModCandidate(
                    modId,
                    displayName,
                    version,
                    license,
                    dependencies,
                    warnings,
                    normalizedPath
            ));
        }
        return candidates;
    }

    private static String dependencyType(Map<String, String> fields) {
        String type = optional(fields.get("type"));
        if (!type.isBlank()) {
            return type;
        }
        String mandatory = optional(fields.get("mandatory"));
        if (mandatory.equalsIgnoreCase("true")) {
            return "required";
        }
        if (mandatory.equalsIgnoreCase("false")) {
            return "optional";
        }
        return "unspecified";
    }

    private static boolean appliesToMod(String owner, String modId, boolean singleMod) {
        String normalizedOwner = owner == null ? "" : owner.trim();
        return normalizedOwner.isBlank()
                || normalizedOwner.equals(MOD_ID_PLACEHOLDER)
                || normalizedOwner.equals(modId)
                || singleMod;
    }

    private static String parseValue(String valuePart) {
        String value = valuePart.trim();
        if (value.startsWith("\"\"\"") || value.startsWith("'''")) {
            String delimiter = value.substring(0, 3);
            int closing = value.indexOf(delimiter, 3);
            return closing >= 0 ? value.substring(3, closing).trim() : "";
        }
        if (value.startsWith("\"")) {
            return quotedValue(value, '"');
        }
        if (value.startsWith("'")) {
            return quotedValue(value, '\'');
        }
        int comment = value.indexOf('#');
        if (comment >= 0) {
            value = value.substring(0, comment);
        }
        return value.trim();
    }

    private static String quotedValue(String value, char quote) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = 1; i < value.length(); i++) {
            char character = value.charAt(i);
            if (quote == '"' && !escaped && character == '\\') {
                escaped = true;
                continue;
            }
            if (!escaped && character == quote) {
                return result.toString().trim();
            }
            result.append(character);
            escaped = false;
        }
        return result.toString().trim();
    }

    private static String tripleDelimiter(String valuePart) {
        if (valuePart.startsWith("\"\"\"")) {
            return "\"\"\"";
        }
        if (valuePart.startsWith("'''")) {
            return "'''";
        }
        return "";
    }

    private static boolean closesTripleOnSameLine(String valuePart, String delimiter) {
        return valuePart.indexOf(delimiter, 3) >= 0;
    }

    private static String templateAware(String value, String inferredModuleId, String fallback) {
        String normalized = optional(value);
        if (normalized.equals(MOD_ID_PLACEHOLDER) || normalized.equals(MOD_NAME_PLACEHOLDER)) {
            return optional(inferredModuleId).isBlank() ? optional(fallback) : inferredModuleId;
        }
        return normalized.isBlank() ? optional(fallback) : normalized;
    }

    private static String inferModuleId(Path metadataPath) {
        for (int i = 0; i < metadataPath.getNameCount(); i++) {
            if (metadataPath.getName(i).toString().equals("src") && i > 0) {
                return metadataPath.getName(i - 1).toString();
            }
        }
        Path parent = metadataPath.getParent();
        if (parent != null && parent.getParent() != null) {
            Path fallback = parent.getParent();
            if (fallback.getFileName() != null) {
                return fallback.getFileName().toString();
            }
        }
        return "";
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    private record DependencyTable(String owner, Map<String, String> fields) {
    }
}
