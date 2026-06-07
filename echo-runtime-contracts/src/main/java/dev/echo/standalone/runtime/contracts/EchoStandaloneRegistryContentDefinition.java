package dev.echo.standalone.runtime.contracts;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneRegistryContentDefinition(
        String registry,
        String id,
        String addon,
        String kind,
        String blockstate,
        String model,
        String texture,
        String lang,
        List<String> inputs,
        List<String> outputs,
        List<String> entries,
        String source,
        boolean searchVisible,
        int mergedSourceCount
) {
    public EchoStandaloneRegistryContentDefinition {
        registry = requireText(registry, "registry");
        id = requireText(id, "id");
        addon = requireText(addon, "addon");
        kind = optionalText(kind);
        blockstate = optionalText(blockstate);
        model = optionalText(model);
        texture = optionalText(texture);
        lang = optionalText(lang);
        inputs = List.copyOf(Objects.requireNonNull(inputs, "inputs"));
        outputs = List.copyOf(Objects.requireNonNull(outputs, "outputs"));
        entries = List.copyOf(Objects.requireNonNull(entries, "entries"));
        source = optionalText(source);
        if (mergedSourceCount < 1) {
            throw new IllegalArgumentException("merged source count must be positive");
        }
    }

    public static EchoStandaloneRegistryContentDefinition block(
            String id,
            String addon,
            String blockstate,
            String model,
            String texture,
            String lang
    ) {
        return new EchoStandaloneRegistryContentDefinition(
                "blocks", id, addon, "", blockstate, model, texture, lang,
                List.of(), List.of(), List.of(), "", true, 1
        );
    }

    public static EchoStandaloneRegistryContentDefinition item(
            String id,
            String addon,
            String model,
            String texture,
            String lang,
            List<String> recipes,
            List<String> lootTables,
            boolean searchVisible
    ) {
        return new EchoStandaloneRegistryContentDefinition(
                "items", id, addon, "", "", model, texture, lang,
                List.of(), List.copyOf(recipes), List.copyOf(lootTables), "", searchVisible, 1
        );
    }

    public static EchoStandaloneRegistryContentDefinition entity(
            String id,
            String addon,
            String model,
            String texture,
            String lang
    ) {
        return new EchoStandaloneRegistryContentDefinition(
                "entities", id, addon, "", "", model, texture, lang,
                List.of(), List.of(), List.of(), "", true, 1
        );
    }

    public static EchoStandaloneRegistryContentDefinition recipe(
            String id,
            String addon,
            String type,
            List<String> inputs,
            List<String> outputs,
            String source
    ) {
        return new EchoStandaloneRegistryContentDefinition(
                "recipes", id, addon, type, "", "", "", "",
                List.copyOf(inputs), List.copyOf(outputs), List.of(), source, false, 1
        );
    }

    public static EchoStandaloneRegistryContentDefinition lootTable(
            String id,
            String addon,
            List<String> entries,
            String source
    ) {
        return new EchoStandaloneRegistryContentDefinition(
                "lootTables", id, addon, "", "", "", "", "",
                List.of(), List.of(), List.copyOf(entries), source, false, 1
        );
    }

    public static EchoStandaloneRegistryContentDefinition tag(
            String id,
            String addon,
            String kind,
            List<String> values,
            String source,
            int mergedSourceCount
    ) {
        return new EchoStandaloneRegistryContentDefinition(
                "tags", id, addon, kind, "", "", "", "",
                List.of(), List.of(), List.copyOf(values), source, false, mergedSourceCount
        );
    }

    public List<String> requiredAssetIssues() {
        if ("blocks".equals(registry)) {
            return missingAssets(List.of(
                    asset("blockstate", blockstate),
                    asset("model", model),
                    asset("texture", texture),
                    asset("lang", lang)
            ));
        }
        if ("items".equals(registry) || "entities".equals(registry)) {
            return missingAssets(List.of(
                    asset("model", model),
                    asset("texture", texture),
                    asset("lang", lang)
            ));
        }
        return List.of();
    }

    private static List<String> missingAssets(List<AssetField> fields) {
        return fields.stream()
                .filter(field -> field.value().isBlank())
                .map(field -> "missing " + field.name())
                .toList();
    }

    private static AssetField asset(String name, String value) {
        return new AssetField(name, optionalText(value));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private record AssetField(String name, String value) {
    }
}
