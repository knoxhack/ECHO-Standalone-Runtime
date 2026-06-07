package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreAssetReferences(
        String blockstateId,
        String modelId,
        String textureId,
        String langKey,
        String langValue,
        String sourceLogicalId
) {
    public static final EchoAdapterCoreAssetReferences NONE =
            new EchoAdapterCoreAssetReferences("", "", "", "", "", "");

    public EchoAdapterCoreAssetReferences {
        blockstateId = optional(blockstateId);
        modelId = optional(modelId);
        textureId = optional(textureId);
        langKey = optional(langKey);
        langValue = optional(langValue);
        sourceLogicalId = optional(sourceLogicalId);
    }

    public boolean hasAssets() {
        return !blockstateId.isBlank() || !modelId.isBlank() || !textureId.isBlank();
    }

    public boolean hasLanguage() {
        return !langKey.isBlank() || !langValue.isBlank();
    }

    public boolean empty() {
        return !hasAssets() && !hasLanguage() && sourceLogicalId.isBlank();
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }
}
