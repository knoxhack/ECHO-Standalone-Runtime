package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoValidationCoreStandaloneAdapter {
    public static final String MODULE_ID = "echovalidationcore";
    public static final String PACK_VALIDATION_CONTRACT_ID = "echovalidationcore:data/pack_validation";
    public static final String DIAGNOSTIC_REPORT_CONTRACT_ID = "echovalidationcore:diagnostic/diagnostic_report";
    public static final String REPAIR_SUGGESTION_CONTRACT_ID = "echovalidationcore:diagnostic/repair_suggestion";
    public static final List<String> CONTRACT_IDS = List.of(
            PACK_VALIDATION_CONTRACT_ID,
            DIAGNOSTIC_REPORT_CONTRACT_ID,
            REPAIR_SUGGESTION_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "validationcore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("validationRuleCount", referenceProbe.get("cleanValidationRuleCount"));
        report.put("diagnosticCount", referenceProbe.get("cleanDiagnosticCount"));
        report.put("highestSeverity", referenceProbe.get("cleanHighestSeverity"));
        report.put("serviceCodeExecuted", true);
        report.put("validationEngineRoundTrip", referenceProbe.get("validationEngineRoundTrip"));
        report.put("packValidationRoundTrip", referenceProbe.get("packValidationRoundTrip"));
        report.put("diagnosticReportRoundTrip", referenceProbe.get("diagnosticReportRoundTrip"));
        report.put("repairSuggestionRoundTrip", referenceProbe.get("repairSuggestionRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "ValidationCore standalone adapter resolved pack validation, diagnostic report, and repair suggestion contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        ValidationEngine cleanEngine = ValidationEngine.empty();
        DiagnosticReport cleanReport = cleanEngine.validateAll(
                "ValidationCore Standalone Adapter Contract",
                List.of(),
                DiagnosticContext.workspace()
        );
        ValidationTarget target = new ValidationTarget(
                "agent4-validationcore-smoke",
                ValidationTargetKind.PACK_METADATA,
                "echo-standalone-runtime/validation/echo.pack.json",
                Map.of("id", MODULE_ID, "name", "ValidationCore")
        );
        ValidationEngine schemaEngine = ValidationEngine.of(List.of(ValidationRule.requiredField(
                "validation.required_schema",
                ValidationTargetKind.PACK_METADATA,
                "schema"
        )));
        ValidationResult validationResult = schemaEngine.validate(target, DiagnosticContext.workspace());
        Diagnostic blockingDiagnostic = validationResult.diagnostics().get(0);
        DiagnosticReport diagnosticReport = DiagnosticReport.of(
                "ValidationCore diagnostic bridge",
                DiagnosticContext.workspace(),
                List.of(blockingDiagnostic)
        );
        RepairSuggestion suggestion = RepairSuggestion.manual(
                "validation.add_schema",
                "Add schema id",
                "Declare the validation schema before the pack is accepted."
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cleanValidationRuleCount", cleanEngine.rules().size());
        result.put("cleanDiagnosticCount", cleanReport.diagnostics().size());
        result.put("cleanHighestSeverity", cleanReport.highestSeverity().serializedName());
        result.put("validationEngineRoundTrip", cleanEngine.rules().isEmpty()
                && cleanReport.diagnostics().isEmpty()
                && cleanReport.highestSeverity() == DiagnosticSeverity.INFO
                && schemaEngine.rules().size() == 1);
        result.put("packValidationRoundTrip", validationResult.target().id().equals("agent4-validationcore-smoke")
                && validationResult.diagnostics().size() == 1
                && blockingDiagnostic.code().equals("validation.required_schema")
                && blockingDiagnostic.blocking()
                && blockingDiagnostic.sourcePath().equals(target.sourcePath()));
        result.put("diagnosticReportRoundTrip", diagnosticReport.hasBlockingDiagnostics()
                && diagnosticReport.highestSeverity() == DiagnosticSeverity.ERROR
                && diagnosticReport.diagnostics().get(0).message().contains("schema"));
        result.put("repairSuggestionRoundTrip", suggestion.requiresConfirmation()
                && suggestion.risk().equals("manual_review")
                && suggestion.label().equals("Add schema id")
                && suggestion.summary().contains("validation schema"));
        result.put("blockingDiagnosticCount", diagnosticReport.diagnostics().size());
        result.put("repairSuggestionId", suggestion.id());
        return Map.copyOf(result);
    }

    private enum ValidationTargetKind {
        PACK_METADATA
    }

    private enum DiagnosticSeverity {
        INFO(0, "INFO"),
        ERROR(2, "ERROR");

        private final int rank;
        private final String serializedName;

        DiagnosticSeverity(int rank, String serializedName) {
            this.rank = rank;
            this.serializedName = serializedName;
        }

        private int rank() {
            return rank;
        }

        private String serializedName() {
            return serializedName;
        }
    }

    private record DiagnosticContext(String scope) {
        private DiagnosticContext {
            scope = scope == null || scope.isBlank() ? "workspace" : scope;
        }

        private static DiagnosticContext workspace() {
            return new DiagnosticContext("workspace");
        }
    }

    private record ValidationTarget(
            String id,
            ValidationTargetKind kind,
            String sourcePath,
            Map<String, String> fields
    ) {
        private ValidationTarget {
            id = requireText(id, "validation target id");
            Objects.requireNonNull(kind, "kind");
            sourcePath = requireText(sourcePath, "sourcePath");
            fields = Map.copyOf(fields == null ? Map.of() : fields);
        }
    }

    private record Diagnostic(
            DiagnosticSeverity severity,
            String code,
            boolean blocking,
            String targetId,
            String sourcePath,
            String message
    ) {
        private Diagnostic {
            Objects.requireNonNull(severity, "severity");
            code = requireText(code, "diagnostic code");
            targetId = requireText(targetId, "targetId");
            sourcePath = requireText(sourcePath, "sourcePath");
            message = requireText(message, "message");
        }
    }

    private record ValidationResult(ValidationTarget target, List<Diagnostic> diagnostics) {
        private ValidationResult {
            Objects.requireNonNull(target, "target");
            diagnostics = List.copyOf(diagnostics == null ? List.of() : diagnostics);
        }
    }

    private record DiagnosticReport(
            String title,
            DiagnosticContext context,
            List<Diagnostic> diagnostics
    ) {
        private DiagnosticReport {
            title = requireText(title, "report title");
            context = context == null ? DiagnosticContext.workspace() : context;
            diagnostics = List.copyOf(diagnostics == null ? List.of() : diagnostics);
        }

        private static DiagnosticReport of(String title, DiagnosticContext context, List<Diagnostic> diagnostics) {
            return new DiagnosticReport(title, context, diagnostics);
        }

        private boolean hasBlockingDiagnostics() {
            return diagnostics.stream().anyMatch(Diagnostic::blocking);
        }

        private DiagnosticSeverity highestSeverity() {
            return diagnostics.stream()
                    .map(Diagnostic::severity)
                    .max((left, right) -> Integer.compare(left.rank(), right.rank()))
                    .orElse(DiagnosticSeverity.INFO);
        }
    }

    private interface ValidationRule {
        boolean supports(ValidationTarget target);

        ValidationResult validate(ValidationTarget target, DiagnosticContext context);

        static ValidationRule requiredField(
                String code,
                ValidationTargetKind targetKind,
                String field
        ) {
            return new RequiredFieldValidationRule(code, targetKind, field);
        }
    }

    private record RequiredFieldValidationRule(
            String code,
            ValidationTargetKind targetKind,
            String field
    ) implements ValidationRule {
        private RequiredFieldValidationRule {
            code = requireText(code, "rule code");
            Objects.requireNonNull(targetKind, "targetKind");
            field = requireText(field, "field");
        }

        @Override
        public boolean supports(ValidationTarget target) {
            return target.kind() == targetKind;
        }

        @Override
        public ValidationResult validate(ValidationTarget target, DiagnosticContext context) {
            if (target.fields().containsKey(field) && !target.fields().get(field).isBlank()) {
                return new ValidationResult(target, List.of());
            }
            return new ValidationResult(target, List.of(new Diagnostic(
                    DiagnosticSeverity.ERROR,
                    code,
                    true,
                    target.id(),
                    target.sourcePath(),
                    "Missing required validation field: " + field
            )));
        }
    }

    private record RepairSuggestion(
            String id,
            String label,
            String summary,
            String risk,
            boolean requiresConfirmation,
            List<String> actions,
            List<String> relatedDocs
    ) {
        private RepairSuggestion {
            id = requireText(id, "repair suggestion id");
            label = requireText(label, "repair suggestion label");
            summary = summary == null ? "" : summary;
            risk = risk == null ? "" : risk;
            actions = List.copyOf(actions == null ? List.of() : actions);
            relatedDocs = List.copyOf(relatedDocs == null ? List.of() : relatedDocs);
        }

        private static RepairSuggestion manual(String id, String label, String summary) {
            return new RepairSuggestion(id, label, summary, "manual_review", true, List.of(), List.of());
        }
    }

    private static final class ValidationEngine {
        private final List<ValidationRule> rules;

        private ValidationEngine(List<ValidationRule> rules) {
            this.rules = List.copyOf(rules == null ? List.of() : rules);
        }

        private static ValidationEngine empty() {
            return new ValidationEngine(List.of());
        }

        private static ValidationEngine of(List<ValidationRule> rules) {
            return new ValidationEngine(rules);
        }

        private List<ValidationRule> rules() {
            return rules;
        }

        private ValidationResult validate(ValidationTarget target, DiagnosticContext context) {
            List<Diagnostic> diagnostics = rules.stream()
                    .filter(rule -> rule.supports(target))
                    .flatMap(rule -> rule.validate(target, context).diagnostics().stream())
                    .toList();
            return new ValidationResult(target, diagnostics);
        }

        private DiagnosticReport validateAll(
                String title,
                List<ValidationTarget> targets,
                DiagnosticContext context
        ) {
            List<Diagnostic> diagnostics = List.copyOf(targets == null ? List.of() : targets).stream()
                    .flatMap(target -> validate(target, context).diagnostics().stream())
                    .toList();
            return DiagnosticReport.of(title, context, diagnostics);
        }
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}
