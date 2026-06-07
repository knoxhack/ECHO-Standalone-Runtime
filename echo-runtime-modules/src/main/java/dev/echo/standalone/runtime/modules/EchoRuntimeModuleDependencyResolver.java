package dev.echo.standalone.runtime.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public final class EchoRuntimeModuleDependencyResolver {
    public EchoRuntimeModuleGraph resolve(List<EchoRuntimeModuleDescriptor> descriptors) {
        Objects.requireNonNull(descriptors, "descriptors");
        Map<String, EchoRuntimeModuleDescriptor> byId = new LinkedHashMap<>();
        List<EchoRuntimeModuleIssue> issues = new ArrayList<>();
        for (EchoRuntimeModuleDescriptor descriptor : descriptors.stream()
                .sorted(Comparator.comparing(EchoRuntimeModuleDescriptor::id))
                .toList()) {
            EchoRuntimeModuleDescriptor existing = byId.putIfAbsent(descriptor.id(), descriptor);
            if (existing != null) {
                issues.add(EchoRuntimeModuleIssue.error(
                        "ECHO-STANDALONE-MODULE-DUPLICATE-ID",
                        descriptor.id(),
                        "Duplicate runtime module id: " + descriptor.id()
                ));
            }
        }

        List<EchoRuntimeModuleGraph.Edge> edges = new ArrayList<>();
        for (EchoRuntimeModuleDescriptor descriptor : byId.values()) {
            for (String required : descriptor.requires()) {
                edges.add(new EchoRuntimeModuleGraph.Edge(descriptor.id(), required, "requires"));
                EchoRuntimeModuleDescriptor requiredDescriptor = byId.get(required);
                if (requiredDescriptor == null) {
                    issues.add(EchoRuntimeModuleIssue.error(
                            "ECHO-STANDALONE-MODULE-MISSING-DEPENDENCY",
                            descriptor.id(),
                            "Required module dependency is missing: " + required
                    ));
                    continue;
                }
                String versionRange = descriptor.requiresVersions().get(required);
                if (versionRange != null && !versionSatisfies(requiredDescriptor.version(), versionRange)) {
                    issues.add(EchoRuntimeModuleIssue.error(
                            "ECHO-STANDALONE-MODULE-DEPENDENCY-VERSION-MISMATCH",
                            descriptor.id(),
                            "Required module dependency " + required + " has version "
                                    + requiredDescriptor.version() + " outside range " + versionRange
                    ));
                }
            }
            for (String optional : descriptor.optional()) {
                edges.add(new EchoRuntimeModuleGraph.Edge(descriptor.id(), optional, "optional"));
                EchoRuntimeModuleDescriptor optionalDescriptor = byId.get(optional);
                String versionRange = descriptor.optionalVersions().get(optional);
                if (optionalDescriptor != null && versionRange != null && !versionSatisfies(optionalDescriptor.version(), versionRange)) {
                    issues.add(EchoRuntimeModuleIssue.warning(
                            "ECHO-STANDALONE-MODULE-OPTIONAL-DEPENDENCY-VERSION-MISMATCH",
                            descriptor.id(),
                            "Optional module dependency " + optional + " has version "
                                    + optionalDescriptor.version() + " outside range " + versionRange
                    ));
                }
            }
        }

        for (String moduleId : requiredDependencyCycleModules(byId)) {
            issues.add(EchoRuntimeModuleIssue.error(
                    "ECHO-STANDALONE-MODULE-DEPENDENCY-CYCLE",
                    moduleId,
                    "Required module dependencies contain a cycle involving: " + moduleId
            ));
        }

        List<String> sortedIds = byId.keySet().stream().sorted().toList();
        Set<String> failedModuleIds = new TreeSet<>();
        for (EchoRuntimeModuleIssue issue : issues) {
            if (issue.severity() == EchoRuntimeModuleIssue.Severity.ERROR && issue.moduleId() != null) {
                failedModuleIds.add(issue.moduleId());
            }
        }
        return new EchoRuntimeModuleGraph(sortedIds, edges, issues, failedModuleIds.stream().toList());
    }

    private static boolean versionSatisfies(String version, String rangeExpression) {
        try {
            return EchoRuntimeModuleVersionRange.parse(rangeExpression).contains(version);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static Set<String> requiredDependencyCycleModules(Map<String, EchoRuntimeModuleDescriptor> byId) {
        TreeSet<String> cycleModules = new TreeSet<>();
        Map<String, VisitState> states = new LinkedHashMap<>();
        List<String> path = new ArrayList<>();
        for (String moduleId : byId.keySet().stream().sorted().toList()) {
            detectRequiredDependencyCycles(moduleId, byId, states, path, cycleModules);
        }
        return cycleModules;
    }

    private static void detectRequiredDependencyCycles(
            String moduleId,
            Map<String, EchoRuntimeModuleDescriptor> byId,
            Map<String, VisitState> states,
            List<String> path,
            Set<String> cycleModules
    ) {
        VisitState state = states.get(moduleId);
        if (state == VisitState.VISITED) {
            return;
        }
        if (state == VisitState.VISITING) {
            addCycleModules(moduleId, path, cycleModules);
            return;
        }

        states.put(moduleId, VisitState.VISITING);
        path.add(moduleId);
        EchoRuntimeModuleDescriptor descriptor = byId.get(moduleId);
        if (descriptor != null) {
            for (String dependencyId : descriptor.requires().stream().sorted().toList()) {
                if (byId.containsKey(dependencyId)) {
                    detectRequiredDependencyCycles(dependencyId, byId, states, path, cycleModules);
                }
            }
        }
        path.remove(path.size() - 1);
        states.put(moduleId, VisitState.VISITED);
    }

    private static void addCycleModules(String repeatedModuleId, List<String> path, Set<String> cycleModules) {
        int index = path.indexOf(repeatedModuleId);
        if (index < 0) {
            cycleModules.add(repeatedModuleId);
            return;
        }
        cycleModules.addAll(path.subList(index, path.size()));
    }

    private enum VisitState {
        VISITING,
        VISITED
    }
}
