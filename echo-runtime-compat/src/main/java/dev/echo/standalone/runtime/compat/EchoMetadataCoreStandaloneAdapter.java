package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoMetadataCoreStandaloneAdapter {
    public static final String MODULE_ID = "echometadatacore";
    public static final String MODULE_MANIFEST_CONTRACT_ID = "echometadatacore:data/module_manifest";
    public static final String AI_METADATA_CONTRACT_ID = "echometadatacore:data/ai_metadata";
    public static final String METADATA_VALIDATION_CONTRACT_ID = "echometadatacore:diagnostic/metadata_validation";
    public static final String PACK_METADATA_SCAN_CONTRACT_ID = "echometadatacore:pack/metadata_scan";
    public static final List<String> CONTRACT_IDS = List.of(
            MODULE_MANIFEST_CONTRACT_ID,
            AI_METADATA_CONTRACT_ID,
            METADATA_VALIDATION_CONTRACT_ID,
            PACK_METADATA_SCAN_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "metadatacore_standalone_contract_active");
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
        report.put("manifestNormalizationRoundTrip", referenceProbe.get("manifestNormalizationRoundTrip"));
        report.put("schemaValidationRoundTrip", referenceProbe.get("schemaValidationRoundTrip"));
        report.put("conflictDetectionRoundTrip", referenceProbe.get("conflictDetectionRoundTrip"));
        report.put("fallbackScanRoundTrip", referenceProbe.get("fallbackScanRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "MetadataCore standalone adapter resolved manifest, AI metadata, validation, and pack scan contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        ModuleManifest manifest = MetadataNormalizer.minimalManifest(MODULE_ID, "", "1.0.0");
        List<MetadataIssue> schemaIssues = MetadataSchemaValidator.requireSchema(
                MODULE_ID,
                Map.of("id", MODULE_ID),
                "echo-standalone-runtime/metadata/echo.mod.json"
        );
        List<MetadataIssue> conflictIssues = MetadataConflictDetector.idMismatch(
                MODULE_ID,
                "wrongmetadata",
                "echo-standalone-runtime/metadata/echo.mod.json"
        );
        MetadataStatus fallback = MetadataFallbackResolver.resolve(MetadataStatus.MISSING, true);
        MetadataParseResult parseResult = new MetadataParseResult(
                MODULE_ID,
                MetadataFileKind.MODULE_MANIFEST,
                fallback,
                "echo-standalone-runtime/metadata/echo.mod.json",
                MetadataFallbackResolver.fallbackUsed(fallback),
                manifest,
                Map.of("schema", "echo.mod.v1", "id", MODULE_ID),
                List.of()
        );
        MetadataScanResult scan = new MetadataScanResult(
                "Echo",
                "addons",
                List.of(parseResult),
                Map.of(MODULE_ID, fallback),
                List.of()
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("manifestNormalizationRoundTrip", manifest.id().equals(MODULE_ID)
                && manifest.name().equals(MODULE_ID)
                && manifest.commonSide()
                && manifest.schemaId().equals(MetadataConstants.SCHEMA_ECHO_MOD_MANIFEST));
        result.put("schemaValidationRoundTrip", schemaIssues.size() == 1
                && schemaIssues.get(0).blocking()
                && schemaIssues.get(0).code().equals("metadata.schema_missing"));
        result.put("conflictDetectionRoundTrip", conflictIssues.size() == 1
                && conflictIssues.get(0).blocking()
                && conflictIssues.get(0).code().equals("metadata.id_mismatch"));
        result.put("fallbackScanRoundTrip", parseResult.valid()
                && parseResult.fallbackUsed()
                && scan.moduleStatuses().get(MODULE_ID) == MetadataStatus.FALLBACK
                && MetadataFallbackResolver.fallbackUsed(fallback));
        result.put("schemaDescriptorCount", MetadataConstants.SCHEMA_DESCRIPTORS.size());
        result.put("schemaIssueCount", schemaIssues.size());
        result.put("conflictIssueCount", conflictIssues.size());
        result.put("scanParseCount", scan.parseResults().size());
        return Map.copyOf(result);
    }

    private static final class MetadataConstants {
        private static final String SCHEMA_ECHO_MOD_MANIFEST = "echo.mod.v1";
        private static final List<String> SCHEMA_DESCRIPTORS = List.of(
                SCHEMA_ECHO_MOD_MANIFEST,
                "echo.ai_metadata.v1",
                "echo.pack_scan.v1"
        );

        private MetadataConstants() {
        }
    }

    private static final class MetadataNormalizer {
        private MetadataNormalizer() {
        }

        private static ModuleManifest minimalManifest(String moduleId, String name, String version) {
            return new ModuleManifest(
                    moduleId,
                    name == null || name.isBlank() ? moduleId : name,
                    version == null || version.isBlank() ? "0.0.0" : version,
                    MetadataConstants.SCHEMA_ECHO_MOD_MANIFEST,
                    List.of("common")
            );
        }
    }

    private static final class MetadataSchemaValidator {
        private MetadataSchemaValidator() {
        }

        private static List<MetadataIssue> requireSchema(
                String moduleId,
                Map<String, String> rawMetadata,
                String sourcePath
        ) {
            if (rawMetadata.containsKey("schema")) {
                return List.of();
            }
            return List.of(new MetadataIssue(
                    moduleId,
                    "metadata.schema_missing",
                    true,
                    sourcePath,
                    "Missing schema id."
            ));
        }
    }

    private static final class MetadataConflictDetector {
        private MetadataConflictDetector() {
        }

        private static List<MetadataIssue> idMismatch(
                String expectedModuleId,
                String declaredModuleId,
                String sourcePath
        ) {
            if (Objects.equals(expectedModuleId, declaredModuleId)) {
                return List.of();
            }
            return List.of(new MetadataIssue(
                    expectedModuleId,
                    "metadata.id_mismatch",
                    true,
                    sourcePath,
                    "Declared metadata id does not match module id."
            ));
        }
    }

    private static final class MetadataFallbackResolver {
        private MetadataFallbackResolver() {
        }

        private static MetadataStatus resolve(MetadataStatus status, boolean fallbackAllowed) {
            return status == MetadataStatus.MISSING && fallbackAllowed ? MetadataStatus.FALLBACK : status;
        }

        private static boolean fallbackUsed(MetadataStatus status) {
            return status == MetadataStatus.FALLBACK;
        }
    }

    private record ModuleManifest(
            String id,
            String name,
            String version,
            String schemaId,
            List<String> sides
    ) {
        private ModuleManifest {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(version, "version");
            Objects.requireNonNull(schemaId, "schemaId");
            sides = List.copyOf(sides == null ? List.of() : sides);
        }

        private boolean commonSide() {
            return sides.contains("common");
        }
    }

    private enum MetadataFileKind {
        MODULE_MANIFEST
    }

    private enum MetadataStatus {
        MISSING,
        FALLBACK
    }

    private record MetadataIssue(
            String moduleId,
            String code,
            boolean blocking,
            String sourcePath,
            String message
    ) {
        private MetadataIssue {
            Objects.requireNonNull(moduleId, "moduleId");
            Objects.requireNonNull(code, "code");
            Objects.requireNonNull(sourcePath, "sourcePath");
            Objects.requireNonNull(message, "message");
        }
    }

    private record MetadataParseResult(
            String moduleId,
            MetadataFileKind fileKind,
            MetadataStatus status,
            String sourcePath,
            boolean fallbackUsed,
            ModuleManifest manifest,
            Map<String, String> rawMetadata,
            List<MetadataIssue> issues
    ) {
        private MetadataParseResult {
            Objects.requireNonNull(moduleId, "moduleId");
            Objects.requireNonNull(fileKind, "fileKind");
            Objects.requireNonNull(status, "status");
            Objects.requireNonNull(sourcePath, "sourcePath");
            Objects.requireNonNull(manifest, "manifest");
            rawMetadata = Map.copyOf(rawMetadata == null ? Map.of() : rawMetadata);
            issues = List.copyOf(issues == null ? List.of() : issues);
        }

        private boolean valid() {
            return issues.stream().noneMatch(MetadataIssue::blocking);
        }
    }

    private record MetadataScanResult(
            String packId,
            String rootPath,
            List<MetadataParseResult> parseResults,
            Map<String, MetadataStatus> moduleStatuses,
            List<MetadataIssue> issues
    ) {
        private MetadataScanResult {
            Objects.requireNonNull(packId, "packId");
            Objects.requireNonNull(rootPath, "rootPath");
            parseResults = List.copyOf(parseResults == null ? List.of() : parseResults);
            moduleStatuses = Map.copyOf(moduleStatuses == null ? Map.of() : moduleStatuses);
            issues = List.copyOf(issues == null ? List.of() : issues);
        }
    }
}
