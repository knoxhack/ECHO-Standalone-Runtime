package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Optional;

public final class EchoThemeAssetRuntime {
    private final EchoAssetResolver resolver;

    public EchoThemeAssetRuntime(EchoAssetResolver resolver) {
        this.resolver = resolver;
    }

    public Optional<String> loadTheme(String namespace, String themeId) throws IOException {
        return resolver.loadText(namespace + ":themes/" + themeId + ".json");
    }
}
