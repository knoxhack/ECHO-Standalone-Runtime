package dev.echo.standalone.runtime.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StandaloneRuntimeMutationLedgerSink {
    private final ArrayList<StandaloneRuntimeMutationLedgerEntry> entries = new ArrayList<>();

    public synchronized StandaloneRuntimeMutationLedgerEntry append(
            String actionId,
            String eventName,
            String canonicalId,
            String runtimeHostId,
            Map<String, Object> inputPayload,
            Map<String, Object> beforeSummary,
            Map<String, Object> afterSummary,
            String resultStatus,
            String failureReason,
            boolean saveTouched,
            boolean feedbackEmitted
    ) {
        StandaloneRuntimeMutationLedgerEntry entry = new StandaloneRuntimeMutationLedgerEntry(
                entries.size() + 1,
                requireText(actionId, "actionId"),
                requireText(eventName, "eventName"),
                requireText(canonicalId, "canonicalId"),
                requireText(runtimeHostId, "runtimeHostId"),
                snapshot(inputPayload),
                snapshot(beforeSummary),
                snapshot(afterSummary),
                requireText(resultStatus, "resultStatus"),
                failureReason == null ? "" : failureReason,
                saveTouched,
                feedbackEmitted
        );
        entries.add(entry);
        return entry;
    }

    public synchronized List<StandaloneRuntimeMutationLedgerEntry> entries() {
        return List.copyOf(entries);
    }

    public synchronized int mutationCount() {
        return (int) entries.stream()
                .filter(entry -> "MUTATED".equals(entry.resultStatus()))
                .count();
    }

    public synchronized boolean hasSavedMutation() {
        return entries.stream().anyMatch(entry -> "MUTATED".equals(entry.resultStatus()) && entry.saveTouched());
    }

    public synchronized boolean hasVisibleMutation() {
        return entries.stream().anyMatch(entry -> "MUTATED".equals(entry.resultStatus()) && entry.feedbackEmitted());
    }

    public synchronized List<Map<String, Object>> snapshots() {
        return entries.stream()
                .map(StandaloneRuntimeMutationLedgerEntry::snapshot)
                .toList();
    }

    private static Map<String, Object> snapshot(Map<String, Object> source) {
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        if (source != null) {
            source.forEach((key, value) -> copy.put(requireText(key, "key"), value == null ? "" : value));
        }
        return Map.copyOf(copy);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    public record StandaloneRuntimeMutationLedgerEntry(
            int sequence,
            String actionId,
            String eventName,
            String canonicalId,
            String runtimeHostId,
            Map<String, Object> inputPayload,
            Map<String, Object> beforeSummary,
            Map<String, Object> afterSummary,
            String resultStatus,
            String failureReason,
            boolean saveTouched,
            boolean feedbackEmitted
    ) {
        public StandaloneRuntimeMutationLedgerEntry {
            if (sequence <= 0) {
                throw new IllegalArgumentException("sequence must be positive");
            }
            actionId = requireText(actionId, "actionId");
            eventName = requireText(eventName, "eventName");
            canonicalId = requireText(canonicalId, "canonicalId");
            runtimeHostId = requireText(runtimeHostId, "runtimeHostId");
            inputPayload = StandaloneRuntimeMutationLedgerSink.snapshot(inputPayload);
            beforeSummary = StandaloneRuntimeMutationLedgerSink.snapshot(beforeSummary);
            afterSummary = StandaloneRuntimeMutationLedgerSink.snapshot(afterSummary);
            resultStatus = requireText(resultStatus, "resultStatus");
            failureReason = Objects.requireNonNullElse(failureReason, "");
        }

        public Map<String, Object> snapshot() {
            return Map.ofEntries(
                    Map.entry("sequence", sequence),
                    Map.entry("actionId", actionId),
                    Map.entry("eventName", eventName),
                    Map.entry("canonicalId", canonicalId),
                    Map.entry("runtimeHostId", runtimeHostId),
                    Map.entry("resultStatus", resultStatus),
                    Map.entry("failureReason", failureReason),
                    Map.entry("saveTouched", saveTouched),
                    Map.entry("feedbackEmitted", feedbackEmitted),
                    Map.entry("before", beforeSummary),
                    Map.entry("after", afterSummary)
            );
        }
    }
}
