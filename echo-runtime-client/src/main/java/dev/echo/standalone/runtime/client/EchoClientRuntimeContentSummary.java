package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

record EchoClientRuntimeContentSummary(
        List<EchoClientRuntimeContentRowSummary> rows,
        Map<String, Integer> domainCounts
) {
    EchoClientRuntimeContentSummary {
        rows = rows == null ? List.of() : List.copyOf(rows);
        domainCounts = domainCounts == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(domainCounts));
    }

    static EchoClientRuntimeContentSummary empty() {
        return new EchoClientRuntimeContentSummary(List.of(), Map.of());
    }

    static EchoClientRuntimeContentSummary fromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return empty();
        }
        ArrayList<EchoClientRuntimeContentRowSummary> summaries = new ArrayList<>();
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            EchoClientRuntimeContentRowSummary summary = EchoClientRuntimeContentRowSummary.fromRow(row);
            if (!summary.valid()) {
                continue;
            }
            summaries.add(summary);
            counts.merge(summary.domain(), 1, Integer::sum);
        }
        return summaries.isEmpty() ? empty() : new EchoClientRuntimeContentSummary(summaries, counts);
    }

    int rowCount() {
        return rows.size();
    }

    int domainCount() {
        return domainCounts.size();
    }

    boolean emptyContent() {
        return rows.isEmpty();
    }

    String summaryLabel() {
        if (rows.isEmpty()) {
            return "0 runtime row(s)";
        }
        return rowCount() + " runtime row(s), " + domainCount() + " domain(s)";
    }

    String domainBreakdownLabel(int limit) {
        List<String> summaries = topDomainSummaries(limit);
        if (summaries.isEmpty()) {
            return "No native runtime content imports";
        }
        return "Domains: " + String.join(", ", summaries);
    }

    List<String> topDomainSummaries(int limit) {
        if (limit <= 0 || domainCounts.isEmpty()) {
            return List.of();
        }
        ArrayList<String> summaries = new ArrayList<>();
        int added = 0;
        for (Map.Entry<String, Integer> entry : domainCounts.entrySet()) {
            if (added >= limit) {
                break;
            }
            summaries.add("Runtime " + EchoClientRuntimeContentRowSummary.domainLabel(entry.getKey())
                    + ": " + entry.getValue());
            added++;
        }
        return List.copyOf(summaries);
    }

    List<EchoClientRuntimeContentRowSummary> recentRows(int limit) {
        if (limit <= 0 || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientRuntimeContentRowSummary> recent = new ArrayList<>();
        for (int i = rows.size() - 1; i >= 0 && recent.size() < limit; i--) {
            recent.add(rows.get(i));
        }
        return List.copyOf(recent);
    }
}
