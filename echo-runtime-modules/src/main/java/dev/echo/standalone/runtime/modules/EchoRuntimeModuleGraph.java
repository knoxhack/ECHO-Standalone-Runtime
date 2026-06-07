package dev.echo.standalone.runtime.modules;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

public record EchoRuntimeModuleGraph(
        List<String> moduleIds,
        List<Edge> dependencyEdges,
        List<EchoRuntimeModuleIssue> issues,
        List<String> failedModuleIds
) {
    public EchoRuntimeModuleGraph {
        Objects.requireNonNull(moduleIds, "moduleIds");
        Objects.requireNonNull(dependencyEdges, "dependencyEdges");
        Objects.requireNonNull(issues, "issues");
        Objects.requireNonNull(failedModuleIds, "failedModuleIds");
        moduleIds = moduleIds.stream().sorted().toList();
        dependencyEdges = dependencyEdges.stream()
                .sorted(Comparator.comparing(Edge::fromModuleId)
                        .thenComparing(Edge::toModuleId)
                        .thenComparing(Edge::kind))
                .toList();
        issues = List.copyOf(issues);
        failedModuleIds = failedModuleIds.stream().sorted().toList();
    }

    public boolean hasBlockingIssues() {
        return issues.stream().anyMatch(issue -> issue.severity() == EchoRuntimeModuleIssue.Severity.ERROR);
    }

    public List<String> dependencyOrderedModuleIds() {
        Map<String, Integer> dependencyCounts = new LinkedHashMap<>();
        Map<String, Set<String>> dependentsByDependency = new LinkedHashMap<>();
        for (String moduleId : moduleIds) {
            dependencyCounts.put(moduleId, 0);
            dependentsByDependency.put(moduleId, new TreeSet<>());
        }

        for (Edge edge : dependencyEdges) {
            if (!ordersModuleActivation(edge.kind())
                    || !dependencyCounts.containsKey(edge.fromModuleId())
                    || !dependencyCounts.containsKey(edge.toModuleId())) {
                continue;
            }
            dependencyCounts.computeIfPresent(edge.fromModuleId(), (ignored, count) -> count + 1);
            dependentsByDependency.get(edge.toModuleId()).add(edge.fromModuleId());
        }

        PriorityQueue<String> ready = new PriorityQueue<>();
        dependencyCounts.forEach((moduleId, count) -> {
            if (count == 0) {
                ready.add(moduleId);
            }
        });

        List<String> ordered = new ArrayList<>();
        while (!ready.isEmpty()) {
            String moduleId = ready.poll();
            ordered.add(moduleId);
            for (String dependent : dependentsByDependency.getOrDefault(moduleId, Set.of())) {
                int nextCount = dependencyCounts.computeIfPresent(dependent, (ignored, count) -> count - 1);
                if (nextCount == 0) {
                    ready.add(dependent);
                }
            }
        }

        if (ordered.size() < moduleIds.size()) {
            TreeSet<String> remaining = new TreeSet<>(moduleIds);
            remaining.removeAll(ordered);
            ordered.addAll(remaining);
        }
        return List.copyOf(ordered);
    }

    private static boolean ordersModuleActivation(String kind) {
        return kind.equals("requires") || kind.equals("optional");
    }

    public record Edge(String fromModuleId, String toModuleId, String kind) {
        public Edge {
            fromModuleId = requireText(fromModuleId, "fromModuleId");
            toModuleId = requireText(toModuleId, "toModuleId");
            kind = requireText(kind, "kind");
        }

        private static String requireText(String value, String name) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(name + " must not be blank");
            }
            return value;
        }
    }
}
