package dev.echo.standalone.runtime.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

final class EchoClientRuntimeContentFingerprint {
    static final String ALGORITHM = "sha256:echo.client.runtime_content.canonical.v1";
    static final String ALGORITHM_METADATA_KEY = "runtimeContentFingerprintAlgorithm";
    static final String FINGERPRINT_METADATA_KEY = "runtimeContentFingerprint";

    private EchoClientRuntimeContentFingerprint() {
    }

    static String fingerprint(List<Map<String, Object>> rows) {
        return sha256(canonicalRows(rows));
    }

    static String shortFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return "";
        }
        return fingerprint.length() <= 12 ? fingerprint : fingerprint.substring(0, 12);
    }

    private static String canonicalRows(List<Map<String, Object>> rows) {
        ArrayList<Map<String, Object>> safeRows = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                if (row != null && !row.isEmpty()) {
                    safeRows.add(row);
                }
            }
        }
        safeRows.sort(Comparator
                .comparing(EchoClientRuntimeContentFingerprint::contentId)
                .thenComparing(EchoClientRuntimeContentFingerprint::canonicalValue));
        StringBuilder builder = new StringBuilder();
        builder.append(ALGORITHM).append('\n');
        appendCanonicalValue(builder, safeRows);
        return builder.toString();
    }

    private static String canonicalValue(Object value) {
        StringBuilder builder = new StringBuilder();
        appendCanonicalValue(builder, value);
        return builder.toString();
    }

    private static void appendCanonicalValue(StringBuilder builder, Object value) {
        if (value == null) {
            builder.append("null");
            return;
        }
        if (value instanceof Map<?, ?> map) {
            builder.append('{');
            ArrayList<Map.Entry<?, ?>> entries = new ArrayList<>(map.entrySet());
            entries.removeIf(entry -> entry.getKey() == null);
            entries.sort(Comparator.comparing(entry -> String.valueOf(entry.getKey())));
            for (int index = 0; index < entries.size(); index++) {
                if (index > 0) {
                    builder.append(',');
                }
                Map.Entry<?, ?> entry = entries.get(index);
                appendEscaped(builder, String.valueOf(entry.getKey()));
                builder.append(':');
                appendCanonicalValue(builder, entry.getValue());
            }
            builder.append('}');
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            builder.append('[');
            int index = 0;
            for (Object item : iterable) {
                if (index++ > 0) {
                    builder.append(',');
                }
                appendCanonicalValue(builder, item);
            }
            builder.append(']');
            return;
        }
        if (value instanceof Number number) {
            builder.append("number:");
            builder.append(normalizedNumber(number));
            return;
        }
        if (value instanceof Boolean bool) {
            builder.append("boolean:");
            builder.append(bool);
            return;
        }
        appendEscaped(builder, String.valueOf(value));
    }

    private static void appendEscaped(StringBuilder builder, String value) {
        builder.append('"');
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            switch (ch) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(ch);
            }
        }
        builder.append('"');
    }

    private static String contentId(Map<String, Object> row) {
        Object value = row.get("contentId");
        return value == null ? "" : String.valueOf(value);
    }

    private static String normalizedNumber(Number number) {
        if (number instanceof Float || number instanceof Double) {
            double value = number.doubleValue();
            if (Double.isFinite(value) && value == Math.rint(value)) {
                return Long.toString((long) value);
            }
        }
        return number.toString();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(Character.forDigit((b >>> 4) & 0x0F, 16));
                builder.append(Character.forDigit(b & 0x0F, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }
}
