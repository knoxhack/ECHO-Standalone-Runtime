package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientRuntimeContentSaveCodec {
    private EchoClientRuntimeContentSaveCodec() {
    }

    static String writeRows(List<Map<String, Object>> rows) {
        LinkedHashMap<String, Object> root = new LinkedHashMap<>();
        root.put("schema", "echo.client.runtime_content.v1");
        root.put("rows", rows == null ? List.of() : rows);
        StringBuilder builder = new StringBuilder();
        appendJson(builder, root);
        builder.append('\n');
        return builder.toString();
    }

    static List<Map<String, Object>> readRows(String text) {
        Object value = new Parser(text == null ? "" : text).parse();
        if (!(value instanceof Map<?, ?> root)) {
            throw new IllegalArgumentException("Client runtime content save must be a JSON object");
        }
        Object rows = root.get("rows");
        if (!(rows instanceof List<?> list) || list.isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add(stringMap(map));
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Object> stringMap(Map<?, ?> source) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), normalizeValue(entry.getValue()));
            }
        }
        return Map.copyOf(result);
    }

    private static Object normalizeValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return stringMap(map);
        }
        if (value instanceof List<?> list) {
            ArrayList<Object> result = new ArrayList<>();
            for (Object item : list) {
                result.add(normalizeValue(item));
            }
            return List.copyOf(result);
        }
        return value;
    }

    private static void appendJson(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
        } else if (value instanceof String string) {
            builder.append('"').append(escape(string)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            builder.append(value);
        } else if (value instanceof Map<?, ?> map) {
            builder.append('{');
            int index = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                if (index++ > 0) {
                    builder.append(',');
                }
                builder.append('"').append(escape(String.valueOf(entry.getKey()))).append("\":");
                appendJson(builder, entry.getValue());
            }
            builder.append('}');
        } else if (value instanceof Iterable<?> iterable) {
            builder.append('[');
            int index = 0;
            for (Object item : iterable) {
                if (index++ > 0) {
                    builder.append(',');
                }
                appendJson(builder, item);
            }
            builder.append(']');
        } else {
            builder.append('"').append(escape(String.valueOf(value))).append('"');
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text;
        }

        private Object parse() {
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
            char c = peek();
            return switch (c) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        yield readNumber();
                    }
                    throw error("Unexpected JSON token: " + c);
                }
            };
        }

        private Map<String, Object> readObject() {
            expect('{');
            LinkedHashMap<String, Object> object = new LinkedHashMap<>();
            skipWhitespace();
            if (tryConsume('}')) {
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                object.put(key, readValue());
                skipWhitespace();
                if (tryConsume('}')) {
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> readArray() {
            expect('[');
            ArrayList<Object> array = new ArrayList<>();
            skipWhitespace();
            if (tryConsume(']')) {
                return array;
            }
            while (true) {
                array.add(readValue());
                skipWhitespace();
                if (tryConsume(']')) {
                    return array;
                }
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (!end()) {
                char c = next();
                if (c == '"') {
                    return builder.toString();
                }
                if (c == '\\') {
                    if (end()) {
                        throw error("Unterminated escape sequence");
                    }
                    char escape = next();
                    switch (escape) {
                        case '"' -> builder.append('"');
                        case '\\' -> builder.append('\\');
                        case '/' -> builder.append('/');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> builder.append(readUnicodeEscape());
                        default -> throw error("Unsupported escape sequence: \\" + escape);
                    }
                } else {
                    builder.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private char readUnicodeEscape() {
            if (index + 4 > text.length()) {
                throw error("Incomplete unicode escape");
            }
            String hex = text.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object readNumber() {
            int start = index;
            if (peek() == '-') {
                index++;
            }
            while (!end() && Character.isDigit(peek())) {
                index++;
            }
            boolean decimal = false;
            if (!end() && peek() == '.') {
                decimal = true;
                index++;
                while (!end() && Character.isDigit(peek())) {
                    index++;
                }
            }
            if (!end() && (peek() == 'e' || peek() == 'E')) {
                decimal = true;
                index++;
                if (!end() && (peek() == '+' || peek() == '-')) {
                    index++;
                }
                while (!end() && Character.isDigit(peek())) {
                    index++;
                }
            }
            String value = text.substring(start, index);
            return decimal ? Double.parseDouble(value) : Long.parseLong(value);
        }

        private Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, index)) {
                throw error("Expected literal " + literal);
            }
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (!end() && Character.isWhitespace(peek())) {
                index++;
            }
        }

        private boolean tryConsume(char expected) {
            if (!end() && peek() == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (end() || next() != expected) {
                throw error("Expected '" + expected + "'");
            }
        }

        private char peek() {
            return text.charAt(index);
        }

        private char next() {
            return text.charAt(index++);
        }

        private boolean end() {
            return index >= text.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at index " + index);
        }
    }
}
