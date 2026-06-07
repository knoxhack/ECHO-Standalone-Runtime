package dev.echo.standalone.runtime.ui;

import dev.echo.standalone.runtime.assets.EchoAssetResolver;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class EchoUiThemeRuntime {
    private EchoUiTheme activeTheme;

    public EchoUiThemeRuntime() {
        this(EchoUiTheme.defaultTerminal());
    }

    public EchoUiThemeRuntime(EchoUiTheme activeTheme) {
        this.activeTheme = Objects.requireNonNull(activeTheme, "activeTheme");
    }

    public EchoUiTheme activeTheme() {
        return activeTheme;
    }

    public void activate(EchoUiTheme theme) {
        this.activeTheme = Objects.requireNonNull(theme, "theme");
    }

    public Optional<EchoUiTheme> loadTheme(EchoAssetResolver resolver, String namespace, String themeId) throws IOException {
        Objects.requireNonNull(resolver, "resolver");
        String logicalId = namespace + ":themes/" + themeId + ".json";
        Optional<String> themeText = resolver.loadText(logicalId);
        if (themeText.isEmpty()) {
            return Optional.empty();
        }
        EchoUiTheme theme = EchoUiThemeParser.parse(namespace + "-" + themeId, themeText.get());
        activate(theme);
        return Optional.of(theme);
    }
}
