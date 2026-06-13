package dev.echo.nativeplatform.contracts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record EchoNativeMutationReceipt(
        String moduleId,
        String serviceId,
        String surface,
        String action,
        String target,
        EchoNativeLoadStatus status,
        EchoNativeRuntimeSide side,
        String receiptId,
        long sequence,
        Map<String, Object> evidence
) {
    public EchoNativeMutationReceipt {
        moduleId = requireText(moduleId, "moduleId");
        serviceId = requireText(serviceId, "serviceId");
        surface = requireText(surface, "surface");
        action = requireText(action, "action");
        target = target == null ? "" : target.trim();
        status = Objects.requireNonNull(status, "status");
        side = side == null ? EchoNativeRuntimeSide.UNKNOWN : side;
        receiptId = receiptId == null || receiptId.isBlank()
                ? moduleId + ":" + serviceId + ":" + surface + ":" + action + ":" + Math.max(0, sequence)
                : receiptId.trim();
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
        evidence = Map.copyOf(Objects.requireNonNullElse(evidence, Map.of()));
    }

    public static EchoNativeMutationReceipt from(
            String serviceId,
            EchoNativeServiceMutation mutation,
            EchoNativeLoadStatus status,
            long sequence
    ) {
        Objects.requireNonNull(mutation, "mutation");
        return new EchoNativeMutationReceipt(
                mutation.moduleId(),
                serviceId,
                mutation.surface(),
                mutation.action(),
                mutation.target(),
                status,
                mutation.side(),
                "",
                sequence,
                mutation.evidence()
        );
    }

    public boolean mutated() {
        return status == EchoNativeLoadStatus.MUTATED;
    }

    public Map<String, Object> toReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("moduleId", moduleId);
        report.put("serviceId", serviceId);
        report.put("surface", surface);
        report.put("action", action);
        report.put("target", target);
        report.put("status", status.name());
        report.put("side", side.name());
        report.put("receiptId", receiptId);
        report.put("sequence", sequence);
        report.put("evidence", evidence);
        return Map.copyOf(report);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
