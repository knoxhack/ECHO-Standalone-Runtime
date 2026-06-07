package dev.echo.standalone.runtime.compat;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public record EchoAdapterCoreModuleCoverageReport(
        List<EchoAdapterCoreModuleCoverageEntry> entries,
        List<String> graphIssues
) {
    public EchoAdapterCoreModuleCoverageReport {
        Objects.requireNonNull(entries, "entries");
        Objects.requireNonNull(graphIssues, "graphIssues");
        entries = entries.stream()
                .sorted(Comparator.comparing(EchoAdapterCoreModuleCoverageEntry::moduleId))
                .toList();
        graphIssues = List.copyOf(graphIssues);
    }

    public static EchoAdapterCoreModuleCoverageReport empty() {
        return new EchoAdapterCoreModuleCoverageReport(List.of(), List.of("module coverage was not scanned"));
    }

    public int totalCount() {
        return entries.size();
    }

    public int activeCount() {
        return count(EchoAdapterCoreModuleCoverageStatus.ACTIVE);
    }

    public int adapterGapCount() {
        return count(EchoAdapterCoreModuleCoverageStatus.ADAPTER_GAP);
    }

    public int unsupportedCount() {
        return count(EchoAdapterCoreModuleCoverageStatus.UNSUPPORTED);
    }

    public Optional<EchoAdapterCoreModuleCoverageEntry> find(String moduleId) {
        String normalized = EchoCompatText.requireText(moduleId, "moduleId");
        return entries.stream()
                .filter(entry -> entry.moduleId().equals(normalized))
                .findFirst();
    }

    public EchoAdapterCoreModuleCoverageEntry require(String moduleId) {
        return find(moduleId).orElseThrow(() -> new IllegalArgumentException("Unknown module coverage id: " + moduleId));
    }

    public Map<EchoAdapterCoreModuleCoverageStatus, Long> countsByStatus() {
        return entries.stream()
                .collect(Collectors.groupingBy(
                        EchoAdapterCoreModuleCoverageEntry::status,
                        () -> new java.util.EnumMap<>(EchoAdapterCoreModuleCoverageStatus.class),
                        Collectors.counting()
                ));
    }

    public Map<EchoAdapterCoreDomain, Long> activeCountsByDomain() {
        return entries.stream()
                .filter(EchoAdapterCoreModuleCoverageEntry::active)
                .flatMap(entry -> entry.adapterDomains().stream())
                .collect(Collectors.groupingBy(
                        domain -> domain,
                        () -> new java.util.EnumMap<>(EchoAdapterCoreDomain.class),
                        Collectors.counting()
                ));
    }

    public List<EchoAdapterCoreDomain> missingRequiredBetaDomains() {
        Map<EchoAdapterCoreDomain, Long> activeDomains = activeCountsByDomain();
        return EchoAdapterCoreContractLock.requiredBetaDomains().stream()
                .filter(domain -> !activeDomains.containsKey(domain))
                .toList();
    }

    public List<EchoAdapterCoreModuleCoverageEntry> standaloneModulesMissingRuntimeTargets() {
        return entries.stream()
                .filter(EchoAdapterCoreModuleCoverageEntry::standaloneDeclared)
                .filter(entry -> !EchoAdapterCoreContractLock.supportsEveryRuntime(entry.adapterRuntimes()))
                .toList();
    }

    public List<EchoAdapterCoreModuleCompatibilityReport> moduleReports() {
        return entries.stream()
                .map(entry -> new EchoAdapterCoreModuleCompatibilityReport(
                        entry.moduleId(),
                        entry.status(),
                        entry.standaloneDeclared(),
                        entry.adapterCoreDeclared(),
                        entry.adapterCoreProvider(),
                        entry.nativeEntrypointDeclared(),
                        entry.liveBindingAvailable(),
                        entry.adapterDomains(),
                        entry.adapterRuntimes(),
                        entry.adapterKeys(),
                        entry.gaps(),
                        entry.descriptorPath()
                ))
                .toList();
    }

    public List<EchoAdapterCoreModuleCompatibilityReport> incompleteModuleReports() {
        return moduleReports().stream()
                .filter(report -> !report.reportComplete())
                .toList();
    }

    public boolean contractLockedForBeta() {
        return adapterGapCount() == 0
                && unsupportedCount() == 0
                && graphIssues.isEmpty()
                && missingRequiredBetaDomains().isEmpty()
                && standaloneModulesMissingRuntimeTargets().isEmpty()
                && incompleteModuleReports().isEmpty();
    }

    public String summary() {
        return activeCount() + "/" + totalCount()
                + " active, gaps=" + adapterGapCount()
                + ", unsupported=" + unsupportedCount();
    }

    public String contractSummary() {
        return summary()
                + ", requiredDomainsMissing=" + missingRequiredBetaDomains().stream()
                .map(EchoAdapterCoreDomain::id)
                .toList()
                + ", runtimeTargetGaps=" + standaloneModulesMissingRuntimeTargets().size()
                + ", incompleteModuleReports=" + incompleteModuleReports().size();
    }

    private int count(EchoAdapterCoreModuleCoverageStatus status) {
        return (int) entries.stream()
                .filter(entry -> entry.status() == status)
                .count();
    }
}
