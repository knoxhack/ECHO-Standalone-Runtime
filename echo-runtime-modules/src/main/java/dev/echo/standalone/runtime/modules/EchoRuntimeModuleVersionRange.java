package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Objects;

final class EchoRuntimeModuleVersionRange {
    private final String expression;
    private final Version lower;
    private final Version upper;
    private final boolean lowerInclusive;
    private final boolean upperInclusive;
    private final Version exact;

    private EchoRuntimeModuleVersionRange(
            String expression,
            Version lower,
            Version upper,
            boolean lowerInclusive,
            boolean upperInclusive,
            Version exact
    ) {
        this.expression = requireText(expression, "expression");
        this.lower = lower;
        this.upper = upper;
        this.lowerInclusive = lowerInclusive;
        this.upperInclusive = upperInclusive;
        this.exact = exact;
    }

    static EchoRuntimeModuleVersionRange parse(String expression) {
        String value = requireText(expression, "expression").trim();
        if ((value.startsWith("[") || value.startsWith("(")) && (value.endsWith("]") || value.endsWith(")"))) {
            boolean lowerInclusive = value.startsWith("[");
            boolean upperInclusive = value.endsWith("]");
            String body = value.substring(1, value.length() - 1);
            String[] parts = body.split(",", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid module version range: " + expression);
            }
            Version lower = parts[0].isBlank() ? null : Version.parse(parts[0].trim());
            Version upper = parts[1].isBlank() ? null : Version.parse(parts[1].trim());
            return new EchoRuntimeModuleVersionRange(value, lower, upper, lowerInclusive, upperInclusive, null);
        }
        return new EchoRuntimeModuleVersionRange(value, null, null, true, true, Version.parse(value));
    }

    boolean contains(String version) {
        Version candidate = Version.parse(version);
        if (exact != null) {
            return candidate.compareTo(exact) == 0;
        }
        if (lower != null) {
            int comparison = candidate.compareTo(lower);
            if (comparison < 0 || (comparison == 0 && !lowerInclusive)) {
                return false;
            }
        }
        if (upper != null) {
            int comparison = candidate.compareTo(upper);
            if (comparison > 0 || (comparison == 0 && !upperInclusive)) {
                return false;
            }
        }
        return true;
    }

    String expression() {
        return expression;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private record Version(List<Integer> parts) implements Comparable<Version> {
        private Version {
            Objects.requireNonNull(parts, "parts");
            parts = List.copyOf(parts);
        }

        static Version parse(String text) {
            String normalized = requireText(text, "version").trim();
            String[] rawParts = normalized.split("\\.");
            java.util.ArrayList<Integer> parsed = new java.util.ArrayList<>();
            for (String rawPart : rawParts) {
                StringBuilder digits = new StringBuilder();
                for (int index = 0; index < rawPart.length(); index++) {
                    char c = rawPart.charAt(index);
                    if (Character.isDigit(c)) {
                        digits.append(c);
                    } else {
                        break;
                    }
                }
                parsed.add(digits.isEmpty() ? 0 : Integer.parseInt(digits.toString()));
            }
            while (parsed.size() < 3) {
                parsed.add(0);
            }
            return new Version(parsed);
        }

        @Override
        public int compareTo(Version other) {
            int max = Math.max(parts.size(), other.parts.size());
            for (int index = 0; index < max; index++) {
                int left = index < parts.size() ? parts.get(index) : 0;
                int right = index < other.parts.size() ? other.parts.get(index) : 0;
                int comparison = Integer.compare(left, right);
                if (comparison != 0) {
                    return comparison;
                }
            }
            return 0;
        }
    }
}
