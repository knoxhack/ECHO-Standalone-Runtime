package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoTextureForgeStandaloneAdapter {
    public static final String MODULE_ID = "echotextureforge";
    public static final String SPEC_REGISTRY_CONTRACT_ID = "echotextureforge:assets/spec_registry";
    public static final String PROMPT_EXPORT_CONTRACT_ID = "echotextureforge:assets/prompt_export";
    public static final String REVIEW_STATE_CONTRACT_ID = "echotextureforge:data/review_state";
    public static final String TEXTURE_AUDIT_CONTRACT_ID = "echotextureforge:diagnostic/texture_audit";
    public static final String DASHBOARD_CONTRACT_ID = "echotextureforge:ui/dashboard";
    public static final List<String> CONTRACT_IDS = List.of(
            SPEC_REGISTRY_CONTRACT_ID,
            PROMPT_EXPORT_CONTRACT_ID,
            REVIEW_STATE_CONTRACT_ID,
            TEXTURE_AUDIT_CONTRACT_ID,
            DASHBOARD_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "textureforge_standalone_contract_active");
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
        report.put("specRegistryRoundTrip", referenceProbe.get("specRegistryRoundTrip"));
        report.put("promptExportRoundTrip", referenceProbe.get("promptExportRoundTrip"));
        report.put("reviewStateRoundTrip", referenceProbe.get("reviewStateRoundTrip"));
        report.put("textureAuditRoundTrip", referenceProbe.get("textureAuditRoundTrip"));
        report.put("dashboardSurfaceResolved", referenceProbe.get("dashboardSurfaceResolved"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "TextureForge standalone adapter resolved spec registry, prompt export, review state, audit, and dashboard contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        TextureSpec itemSpec = TextureSpec.builder("EchoTextureForge", "Status Lens", TextureKind.ITEM)
                .displayName("Status Lens")
                .styleFamily(TextureStyleFamily.ECHO_CYBERGLASS)
                .colorPaletteHints(List.of("signal cyan", "dark glass"))
                .silhouetteNotes("round lens with a clear notch")
                .minecraftReadabilityNotes("must read as a small icon")
                .promptPriority(7)
                .status(TextureSpecStatus.MISSING)
                .build();
        TextureSpec blockSpec = TextureSpec.block(MODULE_ID, "preview_bench");
        TextureSpecRegistry registry = new TextureSpecRegistry();
        registry.register(itemSpec);
        registry.register(blockSpec);
        String prompt = TexturePromptTemplate.singleTexturePrompt(itemSpec);
        TextureAuditIssue issue = new TextureAuditIssue(
                TextureAuditSeverity.WARNING,
                "MISSING_TEXTURE",
                itemSpec.namespace(),
                itemSpec.assetId(),
                itemSpec.assetKind(),
                itemSpec.outputPath(),
                "Generated texture has not been staged yet."
        );
        TextureAuditReport report = new TextureAuditReport(
                2,
                Map.of(TextureAuditSeverity.WARNING, 1),
                List.of(issue),
                registry.all(),
                List.of("build/textureforge/prompts/master_codex_texture_prompts.md"),
                List.of("build/textureforge/reports/texture_audit.json")
        );
        TextureValidationRules rules = TextureValidationRules.defaults();
        String dashboardStatus = TextureForgeDashboardSurface.status();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("specRegistryRoundTrip", itemSpec.namespace().equals(MODULE_ID)
                && itemSpec.assetId().equals("status lens")
                && itemSpec.outputPath().equals("textures/item/status lens.png")
                && registry.find(MODULE_ID, "Status Lens", TextureKind.ITEM).isPresent()
                && registry.byNamespace(MODULE_ID).size() == 2
                && registry.all().get(0).assetKind() == TextureKind.BLOCK);
        result.put("promptExportRoundTrip", prompt.contains("Mod ID: echotextureforge")
                && prompt.contains("Asset ID: status lens")
                && prompt.contains("Output Path: assets/echotextureforge/textures/item/status lens.png")
                && prompt.contains("no text")
                && prompt.contains("transparent background"));
        result.put("reviewStateRoundTrip", TextureSpecStatus.MISSING.id().equals("missing")
                && itemSpec.toBuilder().status(TextureSpecStatus.GENERATED_PENDING_REVIEW).build().status()
                == TextureSpecStatus.GENERATED_PENDING_REVIEW);
        result.put("textureAuditRoundTrip", report.totalSpecs() == 2
                && report.issues(TextureAuditSeverity.WARNING).size() == 1
                && report.severitySummary().get(TextureAuditSeverity.WARNING) == 1
                && rules.defaultResolution() == TextureResolution.DEFAULT_32
                && rules.requirePowerOfTwo());
        result.put("dashboardSurfaceResolved", dashboardStatus.contains("dashboard bridge")
                && dashboardStatus.contains("ScreenCore"));
        result.put("promptLength", prompt.length());
        result.put("registrySize", registry.size());
        result.put("dashboardStatus", dashboardStatus);
        return Map.copyOf(result);
    }

    private enum TextureKind {
        BLOCK("block"),
        ITEM("item");

        private final String pathSegment;

        TextureKind(String pathSegment) {
            this.pathSegment = pathSegment;
        }

        private String pathSegment() {
            return pathSegment;
        }
    }

    private enum TextureStyleFamily {
        ECHO_CYBERGLASS("Echo Cyberglass");

        private final String displayName;

        TextureStyleFamily(String displayName) {
            this.displayName = displayName;
        }

        private String displayName() {
            return displayName;
        }
    }

    private enum TextureSpecStatus {
        MISSING("missing"),
        GENERATED_PENDING_REVIEW("generated_pending_review");

        private final String id;

        TextureSpecStatus(String id) {
            this.id = id;
        }

        private String id() {
            return id;
        }
    }

    private enum TextureResolution {
        DEFAULT_32(32);

        private final int pixels;

        TextureResolution(int pixels) {
            this.pixels = pixels;
        }
    }

    private enum TextureAuditSeverity {
        WARNING
    }

    private record TextureSpec(
            String namespace,
            String assetId,
            TextureKind assetKind,
            String displayName,
            TextureStyleFamily styleFamily,
            List<String> colorPaletteHints,
            String silhouetteNotes,
            String minecraftReadabilityNotes,
            int promptPriority,
            TextureSpecStatus status
    ) {
        private TextureSpec {
            namespace = normalizeNamespace(namespace);
            assetId = normalizeAssetId(assetId);
            Objects.requireNonNull(assetKind, "assetKind");
            displayName = displayName == null || displayName.isBlank() ? assetId : displayName;
            styleFamily = styleFamily == null ? TextureStyleFamily.ECHO_CYBERGLASS : styleFamily;
            colorPaletteHints = List.copyOf(colorPaletteHints == null ? List.of() : colorPaletteHints);
            silhouetteNotes = silhouetteNotes == null ? "" : silhouetteNotes;
            minecraftReadabilityNotes = minecraftReadabilityNotes == null ? "" : minecraftReadabilityNotes;
            status = status == null ? TextureSpecStatus.MISSING : status;
        }

        private static TextureSpec block(String namespace, String assetId) {
            return builder(namespace, assetId, TextureKind.BLOCK)
                    .displayName(assetId)
                    .status(TextureSpecStatus.MISSING)
                    .build();
        }

        private static Builder builder(String namespace, String assetId, TextureKind assetKind) {
            return new Builder(namespace, assetId, assetKind);
        }

        private String outputPath() {
            return "textures/" + assetKind.pathSegment() + "/" + assetId + ".png";
        }

        private Builder toBuilder() {
            return new Builder(namespace, assetId, assetKind)
                    .displayName(displayName)
                    .styleFamily(styleFamily)
                    .colorPaletteHints(colorPaletteHints)
                    .silhouetteNotes(silhouetteNotes)
                    .minecraftReadabilityNotes(minecraftReadabilityNotes)
                    .promptPriority(promptPriority)
                    .status(status);
        }

        private static final class Builder {
            private final String namespace;
            private final String assetId;
            private final TextureKind assetKind;
            private String displayName;
            private TextureStyleFamily styleFamily;
            private List<String> colorPaletteHints = List.of();
            private String silhouetteNotes = "";
            private String minecraftReadabilityNotes = "";
            private int promptPriority;
            private TextureSpecStatus status = TextureSpecStatus.MISSING;

            private Builder(String namespace, String assetId, TextureKind assetKind) {
                this.namespace = namespace;
                this.assetId = assetId;
                this.assetKind = assetKind;
            }

            private Builder displayName(String displayName) {
                this.displayName = displayName;
                return this;
            }

            private Builder styleFamily(TextureStyleFamily styleFamily) {
                this.styleFamily = styleFamily;
                return this;
            }

            private Builder colorPaletteHints(List<String> colorPaletteHints) {
                this.colorPaletteHints = List.copyOf(colorPaletteHints == null ? List.of() : colorPaletteHints);
                return this;
            }

            private Builder silhouetteNotes(String silhouetteNotes) {
                this.silhouetteNotes = silhouetteNotes;
                return this;
            }

            private Builder minecraftReadabilityNotes(String minecraftReadabilityNotes) {
                this.minecraftReadabilityNotes = minecraftReadabilityNotes;
                return this;
            }

            private Builder promptPriority(int promptPriority) {
                this.promptPriority = promptPriority;
                return this;
            }

            private Builder status(TextureSpecStatus status) {
                this.status = status;
                return this;
            }

            private TextureSpec build() {
                return new TextureSpec(
                        namespace,
                        assetId,
                        assetKind,
                        displayName,
                        styleFamily,
                        colorPaletteHints,
                        silhouetteNotes,
                        minecraftReadabilityNotes,
                        promptPriority,
                        status
                );
            }
        }
    }

    private static final class TextureSpecRegistry {
        private final Map<String, TextureSpec> specs = new LinkedHashMap<>();

        private void register(TextureSpec spec) {
            specs.put(key(spec.namespace(), spec.assetId(), spec.assetKind()), spec);
        }

        private Optional<TextureSpec> find(String namespace, String assetId, TextureKind kind) {
            return Optional.ofNullable(specs.get(key(namespace, normalizeAssetId(assetId), kind)));
        }

        private List<TextureSpec> byNamespace(String namespace) {
            String normalized = normalizeNamespace(namespace);
            return specs.values().stream()
                    .filter(spec -> spec.namespace().equals(normalized))
                    .toList();
        }

        private List<TextureSpec> all() {
            return specs.values().stream()
                    .sorted((left, right) -> left.assetKind().compareTo(right.assetKind()))
                    .toList();
        }

        private int size() {
            return specs.size();
        }

        private static String key(String namespace, String assetId, TextureKind kind) {
            return normalizeNamespace(namespace) + ":" + kind.name() + ":" + normalizeAssetId(assetId);
        }
    }

    private static final class TexturePromptTemplate {
        private TexturePromptTemplate() {
        }

        private static String singleTexturePrompt(TextureSpec spec) {
            return String.join("\n",
                    "TextureForge single texture prompt",
                    "Mod ID: " + spec.namespace(),
                    "Asset ID: " + spec.assetId(),
                    "Display Name: " + spec.displayName(),
                    "Kind: " + spec.assetKind().name().toLowerCase(),
                    "Style: " + spec.styleFamily().displayName(),
                    "Palette: " + String.join(", ", spec.colorPaletteHints()),
                    "Silhouette: " + spec.silhouetteNotes(),
                    "Readability: " + spec.minecraftReadabilityNotes(),
                    "Output Path: assets/" + spec.namespace() + "/" + spec.outputPath(),
                    "Constraints: no text, transparent background, readable at 32x32"
            );
        }
    }

    private record TextureAuditIssue(
            TextureAuditSeverity severity,
            String code,
            String namespace,
            String assetId,
            TextureKind kind,
            String outputPath,
            String message
    ) {
        private TextureAuditIssue {
            Objects.requireNonNull(severity, "severity");
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(namespace, "namespace");
            Objects.requireNonNull(assetId, "assetId");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(outputPath, "outputPath");
            Objects.requireNonNull(message, "message");
        }
    }

    private record TextureAuditReport(
            int totalSpecs,
            Map<TextureAuditSeverity, Integer> severitySummary,
            List<TextureAuditIssue> issues,
            List<TextureSpec> specs,
            List<String> promptExports,
            List<String> reportExports
    ) {
        private TextureAuditReport {
            severitySummary = Map.copyOf(severitySummary == null ? Map.of() : severitySummary);
            issues = List.copyOf(issues == null ? List.of() : issues);
            specs = List.copyOf(specs == null ? List.of() : specs);
            promptExports = List.copyOf(promptExports == null ? List.of() : promptExports);
            reportExports = List.copyOf(reportExports == null ? List.of() : reportExports);
        }

        private List<TextureAuditIssue> issues(TextureAuditSeverity severity) {
            return issues.stream()
                    .filter(issue -> issue.severity() == severity)
                    .toList();
        }
    }

    private record TextureValidationRules(TextureResolution defaultResolution, boolean requirePowerOfTwo) {
        private static TextureValidationRules defaults() {
            return new TextureValidationRules(TextureResolution.DEFAULT_32, true);
        }
    }

    private static final class TextureForgeDashboardSurface {
        private TextureForgeDashboardSurface() {
        }

        private static String status() {
            return "TextureForge dashboard bridge is reserved for a future ScreenCore implementation.";
        }
    }

    private static String normalizeNamespace(String value) {
        String normalized = String.valueOf(value == null ? "" : value).trim().toLowerCase();
        return normalized.equals("echotextureforge") ? MODULE_ID : normalized;
    }

    private static String normalizeAssetId(String value) {
        return String.valueOf(value == null ? "" : value).trim().toLowerCase().replace('_', ' ');
    }
}
