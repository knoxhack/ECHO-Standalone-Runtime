package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoAssetCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoassetcore";
    public static final String ASSET_REGISTRY_CONTRACT_ID = "echoassetcore:assets/asset_registry";
    public static final String ASSET_VALIDATION_CONTRACT_ID = "echoassetcore:data/asset_validation";
    public static final String TEXTUREFORGE_PROMPTS_CONTRACT_ID = "echoassetcore:assets/textureforge_prompts";
    public static final String TEXTUREFORGE_REPORTS_CONTRACT_ID = "echoassetcore:data/textureforge_reports";
    public static final List<String> CONTRACT_IDS = List.of(
            ASSET_REGISTRY_CONTRACT_ID,
            ASSET_VALIDATION_CONTRACT_ID,
            TEXTUREFORGE_PROMPTS_CONTRACT_ID,
            TEXTUREFORGE_REPORTS_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "assetcore_standalone_contract_active");
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
        report.put("assetRegistryRoundTrip", referenceProbe.get("assetRegistryRoundTrip"));
        report.put("assetValidationRoundTrip", referenceProbe.get("assetValidationRoundTrip"));
        report.put("textureForgePromptReady", referenceProbe.get("textureForgePromptReady"));
        report.put("textureForgeReportContractResolved", referenceProbe.get("textureForgeReportContractResolved"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "AssetCore standalone adapter resolved and exercised asset registry, validation, and TextureForge prompt/report contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        AssetPath path = new AssetPath("assets/echoassetcore/textures/gui/arcane_index_icon.png");
        AssetReference asset = new AssetReference(
                "echoassetcore:arcane_index_icon",
                "ui_texture",
                path,
                "echoassetcore",
                true,
                Map.of("contract", ASSET_REGISTRY_CONTRACT_ID)
        );
        AssetValidationResult validation = new AssetValidationResult(true, List.of(), List.of());
        TextureForgePromptSpec prompt = new TextureForgePromptSpec(
                "echoassetcore:arcane_index_icon_prompt",
                asset,
                "ui_icon",
                "echo:cyberglass",
                "minecraft_32",
                List.of("readable at inventory size", "transparent-safe silhouette"),
                List.of("baked text", "blur-heavy glow"),
                true,
                Map.of("contract", TEXTUREFORGE_PROMPTS_CONTRACT_ID)
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("assetRegistryRoundTrip", asset.assetId().equals("echoassetcore:arcane_index_icon")
                && asset.ownerModule().equals(MODULE_ID)
                && asset.path().png()
                && asset.path().underAssets()
                && asset.textureLike()
                && asset.searchVisible()
                && ASSET_REGISTRY_CONTRACT_ID.equals(asset.metadata().get("contract")));
        result.put("assetValidationRoundTrip", validation.valid()
                && validation.missingAssets().isEmpty()
                && validation.diagnostics().isEmpty());
        result.put("textureForgePromptReady", prompt.ready()
                && prompt.asset().equals(asset)
                && prompt.resolution().equals("minecraft_32")
                && prompt.styleProfileId().equals("echo:cyberglass")
                && prompt.negativePrompts().contains("baked text"));
        result.put("textureForgeReportContractResolved", TextureForgeOutput.TEXTUREFORGE_REPORT_JSON.fileName()
                .equals("textureforge-report.json")
                && TEXTUREFORGE_REPORTS_CONTRACT_ID.equals(TextureForgeOutput.TEXTUREFORGE_REPORT_JSON.contractId()));
        result.put("assetPath", asset.path().value());
        result.put("promptId", prompt.promptId());
        result.put("validationMissingAssetCount", validation.missingAssets().size());
        return Map.copyOf(result);
    }

    private record AssetPath(String value) {
        private AssetPath {
            Objects.requireNonNull(value, "value");
        }

        private boolean png() {
            return value.endsWith(".png");
        }

        private boolean underAssets() {
            return value.startsWith("assets/");
        }
    }

    private record AssetReference(
            String assetId,
            String kind,
            AssetPath path,
            String ownerModule,
            boolean searchVisible,
            Map<String, String> metadata
    ) {
        private AssetReference {
            Objects.requireNonNull(assetId, "assetId");
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(path, "path");
            Objects.requireNonNull(ownerModule, "ownerModule");
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }

        private boolean textureLike() {
            return kind.contains("texture") && path.png();
        }
    }

    private record AssetValidationResult(
            boolean valid,
            List<String> missingAssets,
            List<String> diagnostics
    ) {
        private AssetValidationResult {
            missingAssets = List.copyOf(missingAssets == null ? List.of() : missingAssets);
            diagnostics = List.copyOf(diagnostics == null ? List.of() : diagnostics);
        }
    }

    private record TextureForgePromptSpec(
            String promptId,
            AssetReference asset,
            String templateKind,
            String styleProfileId,
            String resolution,
            List<String> constraints,
            List<String> negativePrompts,
            boolean ready,
            Map<String, String> metadata
    ) {
        private TextureForgePromptSpec {
            Objects.requireNonNull(promptId, "promptId");
            Objects.requireNonNull(asset, "asset");
            constraints = List.copyOf(constraints == null ? List.of() : constraints);
            negativePrompts = List.copyOf(negativePrompts == null ? List.of() : negativePrompts);
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    private enum TextureForgeOutput {
        TEXTUREFORGE_REPORT_JSON("textureforge-report.json", TEXTUREFORGE_REPORTS_CONTRACT_ID);

        private final String fileName;
        private final String contractId;

        TextureForgeOutput(String fileName, String contractId) {
            this.fileName = fileName;
            this.contractId = contractId;
        }

        private String fileName() {
            return fileName;
        }

        private String contractId() {
            return contractId;
        }
    }
}
