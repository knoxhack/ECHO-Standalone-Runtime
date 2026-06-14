package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class EchoRuntimeModuleDescriptorSchema {
    public static final String SCHEMA_ID = "echo.runtime.module.v1";
    public static final List<String> DESCRIPTOR_SOURCES = List.of(
            "META-INF/echo.mod.json",
            "META-INF/echo.native.json",
            "META-INF/echo.runtime.json"
    );
    public static final List<String> REQUIRED_FIELDS = List.of(
            "id",
            "name"
    );
    public static final List<String> BASELINE_FIELDS = List.of(
            "schema",
            "id",
            "name",
            "version",
            "kind",
            "role",
            "side",
            "trust",
            "trustLevel",
            "official",
            "standalone",
            "requires",
            "optional",
            "provides",
            "consumes",
            "gameModes",
            "aliases",
            "access",
            "replacements"
    );
    public static final List<String> EXECUTABLE_ABI_V1_FIELDS = List.of(
            "permissions",
            "classPath",
            "classpath",
            "entrypoint",
            "adapterCoreEntrypoint",
            "nativeEntrypoint",
            "nativeClasspath",
            "requiresVersions",
            "optionalVersions"
    );
    public static final Map<String, String> FIELD_TYPES = Map.ofEntries(
            Map.entry("schema", "string"),
            Map.entry("id", "string"),
            Map.entry("name", "string"),
            Map.entry("version", "string"),
            Map.entry("kind", "string"),
            Map.entry("role", "string"),
            Map.entry("side", "string"),
            Map.entry("trust", "string"),
            Map.entry("trustLevel", "string"),
            Map.entry("official", "boolean"),
            Map.entry("standalone", "boolean"),
            Map.entry("requires", "string[]"),
            Map.entry("optional", "string[]"),
            Map.entry("provides", "string[]"),
            Map.entry("consumes", "string[]"),
            Map.entry("gameModes", "string[]"),
            Map.entry("permissions", "string[]"),
            Map.entry("aliases", "string[]"),
            Map.entry("classPath", "string[]"),
            Map.entry("classpath", "string[]"),
            Map.entry("entrypoint", "string"),
            Map.entry("adapterCoreEntrypoint", "string"),
            Map.entry("nativeEntrypoint", "string"),
            Map.entry("nativeClasspath", "string[]"),
            Map.entry("requiresVersions", "object<string,string>"),
            Map.entry("optionalVersions", "object<string,string>"),
            Map.entry("access", "object"),
            Map.entry("replacements", "replacement[]")
    );

    private static final Set<String> EXECUTABLE_REQUIRED_FIELDS = Set.of(
            "classPath",
            "entrypoint",
            "adapterCoreEntrypoint",
            "nativeEntrypoint",
            "nativeClasspath",
            "requiresVersions",
            "optionalVersions",
            "permissions"
    );

    private EchoRuntimeModuleDescriptorSchema() {
    }

    public static List<String> allFields() {
        java.util.LinkedHashSet<String> fields = new java.util.LinkedHashSet<>();
        fields.addAll(BASELINE_FIELDS);
        fields.addAll(EXECUTABLE_ABI_V1_FIELDS);
        return List.copyOf(fields);
    }

    public static boolean coversExecutableAbiV1() {
        return FIELD_TYPES.keySet().containsAll(EXECUTABLE_REQUIRED_FIELDS);
    }

    public static List<String> missingRequiredFields(Map<String, Object> json) {
        Objects.requireNonNull(json, "json");
        return REQUIRED_FIELDS.stream()
                .filter(field -> !json.containsKey(field))
                .toList();
    }

    public static List<String> unsupportedFields(Map<String, Object> json) {
        Objects.requireNonNull(json, "json");
        Set<String> known = FIELD_TYPES.keySet();
        return json.keySet().stream()
                .filter(field -> !known.contains(field))
                .sorted()
                .toList();
    }
}
