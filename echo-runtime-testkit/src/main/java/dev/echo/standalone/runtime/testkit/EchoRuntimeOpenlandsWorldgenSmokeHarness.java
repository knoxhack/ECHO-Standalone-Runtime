package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenContract;
import dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenResult;
import dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenRuntime;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class EchoRuntimeOpenlandsWorldgenSmokeHarness {
    private static final String MODULE_ID = "echoopenlandsprotocol";

    private EchoRuntimeOpenlandsWorldgenSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path modulesRoot = args.length > 1
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : echoModulesRoot(standaloneRoot);
        Path openlandsRoot = modulesRoot.resolve(MODULE_ID).toAbsolutePath().normalize();
        Path dataRoot = openlandsRoot.resolve(
                "src/main/resources/data/" + MODULE_ID + "/openlands").toAbsolutePath().normalize();
        require(Files.isDirectory(dataRoot), "Openlands data root should exist: " + dataRoot);

        EchoOpenlandsWorldgenContract contract = loadContract(dataRoot);
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoOpenlandsWorldgenResult result = new EchoOpenlandsWorldgenRuntime().run(
                services,
                contract,
                standaloneRoot.resolve("build/tmp/openlands-worldgen-save")
        );

        require(services.require(EchoOpenlandsWorldgenResult.class) == result,
                "Openlands worldgen result should be service-bound");
        require(result.worldgenComplete(), "Openlands worldgen should complete: " + result.summary());
        require(result.biomePalettesBound(), "Openlands biome palettes should bind");
        require(result.spawnTablesBound(), "Openlands spawn tables should bind");
        require(result.landmarkPoolsBound(), "Openlands landmark pools should bind");
        require(result.starterSpawnGuaranteesBound(), "Openlands starter spawn guarantees should bind");
        require(result.saveReloadPass(), "Openlands worldgen save/reload should pass");
        require(result.starterBiomeId().equals("meadows"), "Openlands starter biome should prefer meadows");
        require(result.starterCells().stream().anyMatch(cell -> cell.markerId().equals("branchwood_log")),
                "Openlands starter cells should include wood");
        require(result.starterCells().stream().filter(cell -> cell.markerId().equals("fieldstone_piece")).count() >= 2,
                "Openlands starter cells should include two loose stone nodes");
        require(result.starterCells().stream().anyMatch(cell -> cell.markerId().equals("reed_fiber")),
                "Openlands starter cells should include fiber");
        require(result.starterCells().stream().anyMatch(cell -> cell.markerId().equals("berries")),
                "Openlands starter cells should include food");
        require(result.landmarks().size() == contract.landmarks().size(),
                "Openlands generated landmark pool should cover every source landmark");
        require(result.creatureSpawns().size() == contract.biomes().stream()
                        .mapToInt(biome -> biome.spawnTable().size())
                        .sum(),
                "Openlands generated creature spawns should cover every biome spawn entry");
        require(result.evidenceIds().containsAll(List.of(
                        "biome_palettes_bound",
                        "spawn_tables_bound",
                        "landmark_pools_bound",
                        "starter_spawn_guarantees_bound")),
                "Openlands worldgen evidence ids should match the runtime adapter load plan");
        require(result.saveCommit().manifest().metadata().get("contractId")
                        .equals(EchoOpenlandsWorldgenRuntime.CONTRACT_ID),
                "Openlands worldgen save manifest should carry the runtime contract id");
        require(result.saveCommit().manifest().file("openlands/worldgen/summary.properties").isPresent(),
                "Openlands worldgen save should include summary state");
        require(result.saveCommit().manifest().file("openlands/worldgen/creature-spawns.tsv").isPresent(),
                "Openlands worldgen save should include creature spawn state");

        writeReport(standaloneRoot, openlandsRoot, contract, result);
        System.out.println("openlands worldgen smoke PASS " + result.summary());
    }

    private static EchoOpenlandsWorldgenContract loadContract(Path dataRoot) throws IOException {
        Map<String, Object> blocks = readJson(dataRoot.resolve("blocks/mvp_blocks.json"));
        Map<String, Object> items = readJson(dataRoot.resolve("items/mvp_items.json"));
        Map<String, Object> biomes = readJson(dataRoot.resolve("biomes/mvp_biomes.json"));
        Map<String, Object> structures = readJson(dataRoot.resolve("structures/mvp_landmarks.json"));
        Map<String, Object> creatures = readJson(dataRoot.resolve("creatures/mvp_creatures.json"));
        Map<String, Object> holomap = readJson(dataRoot.resolve("holomap/mvp_regions.json"));

        TreeSet<String> blockIds = new TreeSet<>(ids(objectList(blocks.get("blocks"))));
        blockIds.addAll(List.of(
                "fieldstone",
                "sand",
                "gravel",
                "clay",
                "branchwood_log",
                "branchwood_planks",
                "branchwood_beam",
                "wooden_door",
                "ladder",
                "campfire",
                "pitchlight",
                "field_crate",
                "cupral_vein",
                "tinveil_vein"
        ));
        TreeSet<String> itemIds = new TreeSet<>(ids(objectList(items.get("items"))));
        itemIds.addAll(List.of(
                "fieldstone_piece",
                "reed_fiber",
                "resin",
                "clay_lump"
        ));

        return new EchoOpenlandsWorldgenContract(
                blockIds,
                itemIds,
                stringList(object(biomes.get("spawnSafetyContract")).get("guarantees")),
                objectList(biomes.get("biomes")).stream()
                        .map(EchoRuntimeOpenlandsWorldgenSmokeHarness::biome)
                        .toList(),
                objectList(structures.get("landmarks")).stream()
                        .map(EchoRuntimeOpenlandsWorldgenSmokeHarness::landmark)
                        .toList(),
                objectList(creatures.get("creatures")).stream()
                        .map(EchoRuntimeOpenlandsWorldgenSmokeHarness::creature)
                        .toList(),
                stringListMap(object(holomap.get("starterRegionNamePools"))),
                ids(objectList(holomap.get("hintTypes")))
        );
    }

    private static EchoOpenlandsWorldgenContract.BiomeProfile biome(Map<String, Object> row) {
        return new EchoOpenlandsWorldgenContract.BiomeProfile(
                canonicalId(text(row.get("id"))),
                palette(object(row.get("blockPalette"))),
                canonicalList(row.get("resourceSet")),
                objectList(row.get("spawnTable")).stream()
                        .map(spawn -> new EchoOpenlandsWorldgenContract.SpawnEntry(
                                canonicalId(text(spawn.get("creature"))),
                                integer(spawn.get("weight")),
                                text(spawn.get("group")),
                                stringList(spawn.get("conditions"))
                        ))
                        .toList(),
                textMap(object(row.get("landmarkFrequency")))
        );
    }

    private static EchoOpenlandsWorldgenContract.Landmark landmark(Map<String, Object> row) {
        return new EchoOpenlandsWorldgenContract.Landmark(
                canonicalId(text(row.get("id"))),
                canonicalList(row.get("preferredBiomes")),
                canonicalList(row.get("blocks")),
                canonicalId(text(row.get("lootTable"))),
                text(row.get("holoMapHint")),
                text(row.get("tutorialHook"))
        );
    }

    private static EchoOpenlandsWorldgenContract.Creature creature(Map<String, Object> row) {
        return new EchoOpenlandsWorldgenContract.Creature(
                canonicalId(text(row.get("id"))),
                canonicalList(row.get("biomes")),
                text(row.get("category")),
                integer(row.get("health")),
                integer(row.get("damage"))
        );
    }

    private static Map<String, List<String>> palette(Map<String, Object> payload) {
        LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            result.put(entry.getKey(), canonicalList(entry.getValue()));
        }
        return Map.copyOf(result);
    }

    private static void writeReport(
            Path standaloneRoot,
            Path openlandsRoot,
            EchoOpenlandsWorldgenContract contract,
            EchoOpenlandsWorldgenResult result
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/openlands-worldgen.json");
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.openlands_worldgen.v1\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"PASS\",\n");
        json.append("  \"moduleId\": \"").append(MODULE_ID).append("\",\n");
        json.append("  \"moduleRoot\": \"").append(escape(standaloneRoot.relativize(openlandsRoot).toString().replace('\\', '/'))).append("\",\n");
        json.append("  \"contractId\": \"").append(EchoOpenlandsWorldgenRuntime.CONTRACT_ID).append("\",\n");
        json.append("  \"worldgenComplete\": ").append(result.worldgenComplete()).append(",\n");
        json.append("  \"biomePalettesBound\": ").append(result.biomePalettesBound()).append(",\n");
        json.append("  \"spawnTablesBound\": ").append(result.spawnTablesBound()).append(",\n");
        json.append("  \"landmarkPoolsBound\": ").append(result.landmarkPoolsBound()).append(",\n");
        json.append("  \"starterSpawnGuaranteesBound\": ").append(result.starterSpawnGuaranteesBound()).append(",\n");
        json.append("  \"saveReloadPass\": ").append(result.saveReloadPass()).append(",\n");
        json.append("  \"sourceBiomeCount\": ").append(contract.biomes().size()).append(",\n");
        json.append("  \"sourceLandmarkCount\": ").append(contract.landmarks().size()).append(",\n");
        json.append("  \"sourceCreatureCount\": ").append(contract.creatures().size()).append(",\n");
        json.append("  \"starterBiomeId\": \"").append(escape(result.starterBiomeId())).append("\",\n");
        json.append("  \"starterRegionName\": \"").append(escape(result.starterRegionName())).append("\",\n");
        json.append("  \"starterCellCount\": ").append(result.starterCells().size()).append(",\n");
        json.append("  \"generatedLandmarkCount\": ").append(result.landmarks().size()).append(",\n");
        json.append("  \"generatedCreatureSpawnCount\": ").append(result.creatureSpawns().size()).append(",\n");
        json.append("  \"sourceResourceIds\": ").append(stringArray(sourceResources(contract))).append(",\n");
        json.append("  \"oreResourceIds\": ").append(stringArray(oreResources(contract))).append(",\n");
        json.append("  \"caveResourceIds\": ").append(stringArray(caveResources(contract))).append(",\n");
        json.append("  \"sourceLandmarkIds\": ").append(stringArray(contract.landmarkIds().stream().sorted().toList())).append(",\n");
        json.append("  \"caveOrRuinLandmarkIds\": ").append(stringArray(caveOrRuinLandmarks(contract))).append(",\n");
        json.append("  \"spawnConditionIds\": ").append(stringArray(spawnConditions(result))).append(",\n");
        json.append("  \"starterCellMarkers\": ").append(stringArray(starterCellMarkers(result))).append(",\n");
        json.append("  \"starterGuarantees\": ").append(jsonStringMap(result.starterGuaranteeEvidence())).append(",\n");
        json.append("  \"evidenceIds\": ").append(stringArray(result.evidenceIds().stream().sorted().toList())).append(",\n");
        json.append("  \"normalizedPaletteMarkerCount\": ").append(result.normalizedPaletteMarkers().size()).append(",\n");
        json.append("  \"saveFilesWritten\": ").append(result.saveCommit().filesWritten()).append(",\n");
        json.append("  \"evidence\": {\n");
        json.append("    \"sourceContractsLoaded\": true,\n");
        json.append("    \"biomePaletteRuntimeBindings\": true,\n");
        json.append("    \"creatureSpawnRuntimeBindings\": true,\n");
        json.append("    \"landmarkPoolRuntimeBindings\": true,\n");
        json.append("    \"starterSpawnGuaranteeRuntimeState\": true,\n");
        json.append("    \"oreResourceRuntimeBindings\": ").append(!oreResources(contract).isEmpty()).append(",\n");
        json.append("    \"caveOrRuinRuntimeBindings\": ").append(!caveOrRuinLandmarks(contract).isEmpty()).append(",\n");
        json.append("    \"saveManifestWritten\": true,\n");
        json.append("    \"saveReloadVerified\": true\n");
        json.append("  }\n");
        json.append("}\n");
        Files.writeString(report, json.toString());
    }

    private static List<String> sourceResources(EchoOpenlandsWorldgenContract contract) {
        TreeSet<String> resources = new TreeSet<>();
        for (EchoOpenlandsWorldgenContract.BiomeProfile biome : contract.biomes()) {
            resources.addAll(biome.resourceSet());
        }
        return resources.stream().toList();
    }

    private static List<String> oreResources(EchoOpenlandsWorldgenContract contract) {
        return sourceResources(contract).stream()
                .filter(resource -> resource.endsWith("_ore") || resource.endsWith("_vein"))
                .sorted()
                .toList();
    }

    private static List<String> caveResources(EchoOpenlandsWorldgenContract contract) {
        return sourceResources(contract).stream()
                .filter(resource -> resource.contains("cave") || resource.contains("mine"))
                .sorted()
                .toList();
    }

    private static List<String> caveOrRuinLandmarks(EchoOpenlandsWorldgenContract contract) {
        return contract.landmarks().stream()
                .filter(landmark -> landmark.id().contains("mine")
                        || landmark.id().contains("cellar")
                        || landmark.id().contains("ruin")
                        || landmark.holoMapHint().contains("ore")
                        || landmark.holoMapHint().contains("cave")
                        || landmark.holoMapHint().contains("underground"))
                .map(EchoOpenlandsWorldgenContract.Landmark::id)
                .sorted()
                .toList();
    }

    private static List<String> spawnConditions(EchoOpenlandsWorldgenResult result) {
        TreeSet<String> conditions = new TreeSet<>();
        for (EchoOpenlandsWorldgenResult.GeneratedCreatureSpawn spawn : result.creatureSpawns()) {
            conditions.addAll(spawn.conditions());
        }
        return conditions.stream().toList();
    }

    private static List<String> starterCellMarkers(EchoOpenlandsWorldgenResult result) {
        return result.starterCells().stream()
                .map(cell -> cell.markerId() + "@" + cell.x() + "," + cell.z() + ":" + cell.purpose())
                .sorted()
                .toList();
    }

    private static Path echoModulesRoot(Path standaloneRoot) {
        String configured = System.getProperty("echo.modules.root");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("ECHO_MODULES_ROOT");
        }
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        Path workspaceRoot = standaloneRoot.getParent();
        if (workspaceRoot != null) {
            return workspaceRoot.resolve("ECHO-Modules/addons").toAbsolutePath().normalize();
        }
        return standaloneRoot.resolve("../ECHO-Modules/addons").toAbsolutePath().normalize();
    }

    private static Map<String, Object> readJson(Path path) throws IOException {
        require(Files.isRegularFile(path), "Missing Openlands contract file: " + path);
        Object parsed = new Json(Files.readString(path)).parse();
        if (!(parsed instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("JSON root must be an object: " + path);
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private static Set<String> ids(List<Map<String, Object>> rows) {
        TreeSet<String> ids = new TreeSet<>();
        for (Map<String, Object> row : rows) {
            String id = canonicalId(text(row.get("id")));
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return Set.copyOf(ids);
    }

    private static String canonicalId(String value) {
        String text = value == null ? "" : value.trim();
        int separator = text.indexOf(':');
        if (separator >= 0 && separator + 1 < text.length()) {
            return text.substring(separator + 1);
        }
        return text;
    }

    private static List<String> canonicalList(Object value) {
        ArrayList<String> result = new ArrayList<>();
        for (String item : stringList(value)) {
            String canonical = canonicalId(item);
            if (!canonical.isBlank()) {
                result.add(canonical);
            }
        }
        return List.copyOf(result);
    }

    private static List<Map<String, Object>> objectList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> object = object(item);
            if (!object.isEmpty()) {
                result.add(object);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            if (value instanceof String single && !single.isBlank()) {
                return List.of(single);
            }
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object item : list) {
            String text = text(item);
            if (!text.isBlank()) {
                result.add(text);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, String> textMap(Map<String, Object> payload) {
        TreeMap<String, String> result = new TreeMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            result.put(entry.getKey(), text(entry.getValue()));
        }
        return Map.copyOf(result);
    }

    private static Map<String, List<String>> stringListMap(Map<String, Object> payload) {
        TreeMap<String, List<String>> result = new TreeMap<>();
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            result.put(canonicalId(entry.getKey()), List.copyOf(stringList(entry.getValue())));
        }
        return Map.copyOf(result);
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = text(value);
        if (text.isBlank()) {
            return 0;
        }
        return Integer.parseInt(text);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        return json.append("]").toString();
    }

    private static String jsonStringMap(Map<String, String> values) {
        StringBuilder json = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            json.append("\"").append(escape(entry.getKey())).append("\": \"")
                    .append(escape(entry.getValue())).append("\"");
            if (++index < values.size()) {
                json.append(", ");
            }
        }
        return json.append("}").toString();
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private static final class Json {
        private static final Pattern CONTROL = Pattern.compile("[\\x00-\\x1F]");
        private final String text;
        private int index;

        private Json(String text) {
            this.text = stripBom(Objects.requireNonNull(text, "text"));
        }

        Object parse() {
            Object value = readValue();
            skipWhitespace();
            if (!end()) {
                throw error("Unexpected trailing JSON content");
            }
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (end()) {
                throw error("Unexpected end of JSON");
            }
            char current = text.charAt(index);
            return switch (current) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> readNumber();
            };
        }

        private Map<String, Object> readObject() {
            expect('{');
            LinkedHashMap<String, Object> values = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                index++;
                return values;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                values.put(key, readValue());
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    return values;
                }
                expect(',');
            }
        }

        private List<Object> readArray() {
            expect('[');
            ArrayList<Object> values = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                index++;
                return values;
            }
            while (true) {
                values.add(readValue());
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    return values;
                }
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder value = new StringBuilder();
            while (!end()) {
                char current = text.charAt(index++);
                if (current == '"') {
                    String result = value.toString();
                    if (CONTROL.matcher(result).find()) {
                        throw error("Control character in JSON string");
                    }
                    return result;
                }
                if (current == '\\') {
                    if (end()) {
                        throw error("Unterminated escape");
                    }
                    char escape = text.charAt(index++);
                    switch (escape) {
                        case '"', '\\', '/' -> value.append(escape);
                        case 'b' -> value.append('\b');
                        case 'f' -> value.append('\f');
                        case 'n' -> value.append('\n');
                        case 'r' -> value.append('\r');
                        case 't' -> value.append('\t');
                        case 'u' -> value.append(readUnicode());
                        default -> throw error("Unsupported JSON escape: " + escape);
                    }
                } else {
                    value.append(current);
                }
            }
            throw error("Unterminated JSON string");
        }

        private char readUnicode() {
            if (index + 4 > text.length()) {
                throw error("Invalid unicode escape");
            }
            String hex = text.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object readNumber() {
            int start = index;
            while (!end()) {
                char current = text.charAt(index);
                if ((current >= '0' && current <= '9')
                        || current == '-'
                        || current == '+'
                        || current == '.'
                        || current == 'e'
                        || current == 'E') {
                    index++;
                } else {
                    break;
                }
            }
            String value = text.substring(start, index);
            if (value.isBlank()) {
                throw error("Expected JSON value");
            }
            return value.contains(".") || value.contains("e") || value.contains("E")
                    ? Double.parseDouble(value)
                    : Long.parseLong(value);
        }

        private Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, index)) {
                throw error("Expected " + literal);
            }
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (!end() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        private void expect(char expected) {
            skipWhitespace();
            if (end() || text.charAt(index) != expected) {
                throw error("Expected '" + expected + "'");
            }
            index++;
        }

        private boolean peek(char expected) {
            return !end() && text.charAt(index) == expected;
        }

        private boolean end() {
            return index >= text.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at offset " + index);
        }

        private static String stripBom(String value) {
            return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
        }
    }
}
