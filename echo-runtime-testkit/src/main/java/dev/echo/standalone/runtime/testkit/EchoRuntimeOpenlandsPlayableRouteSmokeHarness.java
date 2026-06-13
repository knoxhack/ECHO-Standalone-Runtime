package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoOpenlandsFirstHourContract;
import dev.echo.standalone.runtime.app.EchoOpenlandsFirstHourResult;
import dev.echo.standalone.runtime.app.EchoOpenlandsFirstHourRuntime;
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
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class EchoRuntimeOpenlandsPlayableRouteSmokeHarness {
    private static final String MODULE_ID = "echoopenlandsprotocol";

    private EchoRuntimeOpenlandsPlayableRouteSmokeHarness() {
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

        EchoOpenlandsFirstHourContract contract = loadContract(dataRoot);
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoOpenlandsFirstHourResult result = new EchoOpenlandsFirstHourRuntime().run(
                services,
                contract,
                standaloneRoot.resolve("build/tmp/openlands-first-hour-save")
        );

        require(services.require(EchoOpenlandsFirstHourResult.class) == result,
                "Openlands first-hour result should be service-bound");
        require(result.firstHourComplete(), "Openlands first-hour route should complete: " + result.summary());
        require(result.runtimePlaytestPass(), "Openlands runtime playtest should pass");
        require(result.saveReloadPass(), "Openlands save/reload state should pass");
        require(result.waystoneSaveReloadPass(), "Openlands waystone state should survive reload");
        require(result.routeStepsCompleted().equals(contract.routeSteps()),
                "Openlands completed route should match source route");
        require(result.shelterScore() >= 55, "Openlands shelter score should allow first sleep");
        require(result.inventory().keySet().containsAll(List.of(
                        "branchwood_stick",
                        "fieldstone_piece",
                        "reed_fiber",
                        "berries",
                        "crude_axe",
                        "crude_pick",
                        "bedroll",
                        "region_rubbing")),
                "Openlands inventory should contain first-hour essentials");
        require(result.placedBlocks().containsAll(List.of(
                        "bedroll_block",
                        "campfire",
                        "chest",
                        "broken_waystone",
                        "waystone_plinth")),
                "Openlands placed block state should include shelter and waystone blocks");
        require(result.discoveredLandmarks().containsAll(List.of(
                        "road_marker",
                        "ruined_well",
                        "old_mine",
                        "broken_waystone_site")),
                "Openlands HoloMap discovery should include starter landmarks");
        require(result.saveFieldsPersisted().containsAll(contract.saveFields()),
                "Openlands persisted save fields should match source save acceptance");
        require(result.saveCommit().manifest().metadata().get("contractId")
                        .equals(EchoOpenlandsFirstHourRuntime.CONTRACT_ID),
                "Openlands save manifest should carry the runtime contract id");
        require(result.saveCommit().manifest().file("openlands/first-hour.properties").isPresent(),
                "Openlands save should include first-hour state");
        require(result.saveCommit().manifest().file("openlands/waystone.properties").isPresent(),
                "Openlands save should include waystone state");

        writeReport(standaloneRoot, openlandsRoot, contract, result);
        System.out.println("openlands playable route smoke PASS " + result.summary());
    }

    private static EchoOpenlandsFirstHourContract loadContract(Path dataRoot) throws IOException {
        Map<String, Object> blocks = readJson(dataRoot.resolve("blocks/mvp_blocks.json"));
        Map<String, Object> items = readJson(dataRoot.resolve("items/mvp_items.json"));
        Map<String, Object> recipes = readJson(dataRoot.resolve("recipes/mvp_recipes.json"));
        Map<String, Object> biomes = readJson(dataRoot.resolve("biomes/mvp_biomes.json"));
        Map<String, Object> structures = readJson(dataRoot.resolve("structures/mvp_landmarks.json"));
        Map<String, Object> creatures = readJson(dataRoot.resolve("creatures/mvp_creatures.json"));
        Map<String, Object> tutorials = readJson(dataRoot.resolve("tutorials/first_hour_prompts.json"));
        Map<String, Object> waystones = readJson(dataRoot.resolve("waystones/waystone_contract.json"));
        Map<String, Object> progression = readJson(dataRoot.resolve("progression/first_hour_route.json"));
        Map<String, Object> playtests = readJson(dataRoot.resolve("playtests/mvp_first_hour_acceptance.json"));

        TreeSet<String> blockIds = new TreeSet<>(ids(objectList(blocks.get("blocks"))));
        blockIds.addAll(List.of(
                "branchwood_planks",
                "wooden_door",
                "bedroll_block",
                "campfire",
                "torch",
                "chest"
        ));
        TreeSet<String> itemIds = new TreeSet<>(ids(objectList(items.get("items"))));
        itemIds.addAll(List.of(
                "branchwood_stick",
                "fieldstone_piece",
                "reed_fiber",
                "flint_shard",
                "fiber_binding",
                "crude_axe",
                "crude_pick",
                "crude_spade",
                "flint_knife",
                "torch_bundle",
                "pitch",
                "bedroll",
                "hide"
        ));
        TreeSet<String> recipeIds = new TreeSet<>(ids(objectList(recipes.get("recipes"))));
        recipeIds.addAll(List.of(
                "fiber_binding",
                "crude_axe",
                "crude_pick",
                "crude_spade",
                "flint_knife",
                "torch_bundle"
        ));

        return new EchoOpenlandsFirstHourContract(
                routeSteps(progression),
                blockIds,
                itemIds,
                recipeIds,
                ids(objectList(biomes.get("biomes"))),
                ids(objectList(structures.get("landmarks"))),
                ids(objectList(creatures.get("creatures"))),
                ids(objectList(tutorials.get("prompts"))),
                objectList(waystones.get("stateMachine")).stream()
                        .map(item -> text(item.get("state")))
                        .filter(value -> !value.isBlank())
                        .toList(),
                stringList(progression.get("saveLoadAcceptance")),
                ids(objectList(playtests.get("acceptanceScenarios"))),
                ids(objectList(playtests.get("saveLoadCheckpoints")))
        );
    }

    private static List<String> routeSteps(Map<String, Object> progression) {
        ArrayList<String> route = new ArrayList<>(stringList(progression.get("foundationMovedSteps")));
        for (Map<String, Object> item : objectList(progression.get("firstHour"))) {
            String id = text(item.get("id"));
            if (!id.isBlank() && !route.contains(id)) {
                route.add(id);
            }
        }
        return List.copyOf(route);
    }

    private static void writeReport(
            Path standaloneRoot,
            Path openlandsRoot,
            EchoOpenlandsFirstHourContract contract,
            EchoOpenlandsFirstHourResult result
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/openlands-playable-route.json");
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.openlands_playable_route.v1\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"PASS\",\n");
        json.append("  \"moduleId\": \"").append(MODULE_ID).append("\",\n");
        json.append("  \"moduleRoot\": \"").append(escape(standaloneRoot.relativize(openlandsRoot).toString().replace('\\', '/'))).append("\",\n");
        json.append("  \"contractId\": \"").append(EchoOpenlandsFirstHourRuntime.CONTRACT_ID).append("\",\n");
        json.append("  \"firstHourComplete\": ").append(result.firstHourComplete()).append(",\n");
        json.append("  \"runtimePlaytestPass\": ").append(result.runtimePlaytestPass()).append(",\n");
        json.append("  \"saveReloadPass\": ").append(result.saveReloadPass()).append(",\n");
        json.append("  \"waystoneSaveReloadPass\": ").append(result.waystoneSaveReloadPass()).append(",\n");
        json.append("  \"routeStepsCompleted\": ").append(stringArray(result.routeStepsCompleted())).append(",\n");
        json.append("  \"sourceRouteSteps\": ").append(stringArray(contract.routeSteps())).append(",\n");
        json.append("  \"inventoryItemCount\": ").append(result.inventory().size()).append(",\n");
        json.append("  \"placedBlockCount\": ").append(result.placedBlocks().size()).append(",\n");
        json.append("  \"shelterScore\": ").append(result.shelterScore()).append(",\n");
        json.append("  \"waystoneState\": \"").append(result.waystoneState()).append("\",\n");
        json.append("  \"restoredWaystoneState\": \"").append(result.restoredWaystoneState()).append("\",\n");
        json.append("  \"saveFilesWritten\": ").append(result.saveCommit().filesWritten()).append(",\n");
        json.append("  \"saveFieldsPersisted\": ").append(stringArray(result.saveFieldsPersisted().stream().sorted().toList())).append(",\n");
        json.append("  \"evidence\": {\n");
        json.append("    \"sourceContractsLoaded\": true,\n");
        json.append("    \"inventoryRuntimeState\": true,\n");
        json.append("    \"placedBlocksRuntimeState\": true,\n");
        json.append("    \"shelterRuntimeState\": true,\n");
        json.append("    \"holomapDiscoveryRuntimeState\": true,\n");
        json.append("    \"waystoneRuntimeState\": true,\n");
        json.append("    \"saveManifestWritten\": true,\n");
        json.append("    \"saveReloadVerified\": true\n");
        json.append("  }\n");
        json.append("}\n");
        Files.writeString(report, json.toString());
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
        json.append("]");
        return json.toString();
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
