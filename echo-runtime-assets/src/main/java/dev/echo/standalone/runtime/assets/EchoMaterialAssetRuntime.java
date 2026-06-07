package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Optional;

public final class EchoMaterialAssetRuntime {
    private final EchoAssetResolver resolver;

    public EchoMaterialAssetRuntime(EchoAssetResolver resolver) {
        this.resolver = resolver;
    }

    public Optional<String> loadMaterial(String namespace, String materialId) throws IOException {
        return resolver.loadText(namespace + ":materials/" + materialId + ".json");
    }
}
