package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.Optional;

public final class EchoLangRuntime {
    private final EchoAssetResolver resolver;

    public EchoLangRuntime(EchoAssetResolver resolver) {
        this.resolver = resolver;
    }

    public Optional<String> loadLanguage(String namespace, String locale) throws IOException {
        return resolver.loadText(namespace + ":lang/" + locale + ".json");
    }
}
