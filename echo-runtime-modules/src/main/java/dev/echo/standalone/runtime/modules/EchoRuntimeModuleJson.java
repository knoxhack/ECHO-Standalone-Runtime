package dev.echo.standalone.runtime.modules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoRuntimeModuleJson {
    private final String text;
    private int index;

    private EchoRuntimeModuleJson(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        EchoRuntimeModuleJson parser = new EchoRuntimeModuleJson(stripUtf8Bom(text));
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.end()) {
            throw parser.error("Unexpected trailing JSON content");
        }
        return value;
    }

    private static String stripUtf8Bom(String text) {
        if (text != null && !text.isEmpty() && text.charAt(0) == '\uFEFF') {
            return text.substring(1);
        }
        return text;
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
