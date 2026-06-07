package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoSchemaCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoschemacore";
    public static final String SCHEMA_REGISTRY_CONTRACT_ID = "echoschemacore:data/schema_registry";
    public static final String MOD_MANIFEST_SCHEMA_CONTRACT_ID = "echoschemacore:data/echo_mod_manifest_schema";
    public static final String PROMPT_BUNDLE_SCHEMA_CONTRACT_ID = "echoschemacore:data/prompt_bundle_schema";
    public static final List<String> CONTRACT_IDS = List.of(
            SCHEMA_REGISTRY_CONTRACT_ID,
            MOD_MANIFEST_SCHEMA_CONTRACT_ID,
            PROMPT_BUNDLE_SCHEMA_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        SchemaRegistry schemaRegistry = new SchemaRegistry();
        SchemaConstants.registerBuiltIns(schemaRegistry);
        Map<String, Object> referenceProbe = exerciseReferenceBehavior(schemaRegistry);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "schemacore_standalone_contract_active");
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
        report.put("builtinSchemaCount", schemaRegistry.descriptors().size());
        report.put("schemaKinds", schemaRegistry.descriptors().stream()
                .map(descriptor -> descriptor.kind().serializedName())
                .sorted()
                .toList());
        report.put("schemaRegistryRoundTrip", referenceProbe.get("schemaRegistryRoundTrip"));
        report.put("schemaLookupRoundTrip", referenceProbe.get("schemaLookupRoundTrip"));
        report.put("migrationHintRoundTrip", referenceProbe.get("migrationHintRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "SchemaCore standalone adapter resolved schema registry and schema descriptor contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior(SchemaRegistry registry) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("schemaRegistryRoundTrip", registry.descriptors().size() == SchemaConstants.BUILTIN_DESCRIPTORS.size()
                && registry.descriptors().stream().allMatch(descriptor -> descriptor.version().equals(SchemaConstants.INITIAL_VERSION)));
        result.put("schemaLookupRoundTrip", registry.find("echo.mod_manifest", SchemaDocumentKind.ECHO_MOD_MANIFEST, SchemaConstants.INITIAL_VERSION)
                .map(descriptor -> descriptor.name().equals("ECHO Module Manifest")
                        && descriptor.summary().contains("metadata contract"))
                .orElse(false)
                && registry.find("echo.prompt_bundle", SchemaDocumentKind.ECHO_PROMPT_BUNDLE, SchemaConstants.INITIAL_VERSION)
                .map(descriptor -> descriptor.name().equals("ECHO Prompt Bundle"))
                .orElse(false));
        result.put("migrationHintRoundTrip", registry.findByKind(SchemaDocumentKind.ECHO_REPAIR_PLAN).stream()
                .findFirst()
                .map(descriptor -> descriptor.compatibility() == SchemaCompatibility.CURRENT
                        && descriptor.migrationHint().isBlank()
                        && descriptor.docsPath().equals("docs/echo/schema/ECHO_SCHEMA_REGISTRY.md"))
                .orElse(false));
        result.put("schemaDescriptorCount", registry.descriptors().size());
        result.put("schemaKinds", registry.descriptors().stream()
                .map(descriptor -> descriptor.kind().serializedName())
                .sorted()
                .toList());
        return Map.copyOf(result);
    }

    private static final class SchemaConstants {
        private static final String INITIAL_VERSION = "1.0.0";
        private static final List<SchemaDescriptor> BUILTIN_DESCRIPTORS = List.of(
                descriptor("echo.mod_manifest", SchemaDocumentKind.ECHO_MOD_MANIFEST, "ECHO Module Manifest", "Optional module metadata contract for ECHO units."),
                descriptor("echo.ai_metadata", SchemaDocumentKind.ECHO_AI_METADATA, "ECHO AI Metadata", "Optional AI-readable module metadata contract."),
                descriptor("echo.pack_profile", SchemaDocumentKind.ECHO_PACK_PROFILE, "ECHO Pack Profile", "Pack profile contract for Launcher, Command Center, PackOS, and AI tools."),
                descriptor("echo.lockfile", SchemaDocumentKind.ECHO_LOCKFILE, "ECHO Lockfile", "Lockfile contract for known-good module and config snapshots."),
                descriptor("echo.repair_plan", SchemaDocumentKind.ECHO_REPAIR_PLAN, "ECHO Repair Plan", "Repair plan contract for future PackOS and recovery tooling."),
                descriptor("echo.prompt_bundle", SchemaDocumentKind.ECHO_PROMPT_BUNDLE, "ECHO Prompt Bundle", "Prompt bundle contract for Codex and CyberDex automation.")
        );

        private SchemaConstants() {
        }

        private static void registerBuiltIns(SchemaRegistry registry) {
            BUILTIN_DESCRIPTORS.forEach(registry::register);
        }

        private static SchemaDescriptor descriptor(
                String id,
                SchemaDocumentKind kind,
                String name,
                String summary
        ) {
            return new SchemaDescriptor(
                    id,
                    INITIAL_VERSION,
                    kind,
                    name,
                    summary,
                    SchemaCompatibility.CURRENT,
                    "",
                    "docs/echo/schema/ECHO_SCHEMA_REGISTRY.md"
            );
        }
    }

    private enum SchemaDocumentKind {
        ECHO_MOD_MANIFEST("echo_mod_manifest"),
        ECHO_AI_METADATA("echo_ai_metadata"),
        ECHO_PACK_PROFILE("echo_pack_profile"),
        ECHO_LOCKFILE("echo_lockfile"),
        ECHO_REPAIR_PLAN("echo_repair_plan"),
        ECHO_PROMPT_BUNDLE("echo_prompt_bundle");

        private final String serializedName;

        SchemaDocumentKind(String serializedName) {
            this.serializedName = serializedName;
        }

        private String serializedName() {
            return serializedName;
        }
    }

    private enum SchemaCompatibility {
        CURRENT
    }

    private record SchemaDescriptor(
            String id,
            String version,
            SchemaDocumentKind kind,
            String name,
            String summary,
            SchemaCompatibility compatibility,
            String migrationHint,
            String docsPath
    ) {
        private SchemaDescriptor {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(version, "version");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(summary, "summary");
            Objects.requireNonNull(compatibility, "compatibility");
            migrationHint = migrationHint == null ? "" : migrationHint;
            Objects.requireNonNull(docsPath, "docsPath");
        }
    }

    private static final class SchemaRegistry {
        private final Map<String, SchemaDescriptor> descriptorsByKey = new LinkedHashMap<>();

        private void register(SchemaDescriptor descriptor) {
            descriptorsByKey.putIfAbsent(key(descriptor.id(), descriptor.kind(), descriptor.version()), descriptor);
        }

        private java.util.Optional<SchemaDescriptor> find(String id, SchemaDocumentKind kind, String version) {
            return java.util.Optional.ofNullable(descriptorsByKey.get(key(id, kind, version)));
        }

        private List<SchemaDescriptor> findByKind(SchemaDocumentKind kind) {
            return descriptors().stream()
                    .filter(descriptor -> descriptor.kind() == kind)
                    .toList();
        }

        private List<SchemaDescriptor> descriptors() {
            return descriptorsByKey.values().stream()
                    .sorted((left, right) -> {
                        int kind = left.kind().serializedName().compareTo(right.kind().serializedName());
                        if (kind != 0) {
                            return kind;
                        }
                        int id = left.id().compareTo(right.id());
                        if (id != 0) {
                            return id;
                        }
                        return left.version().compareTo(right.version());
                    })
                    .toList();
        }

        private static String key(String id, SchemaDocumentKind kind, String version) {
            return id + "|" + kind.serializedName() + "|" + version;
        }
    }
}
