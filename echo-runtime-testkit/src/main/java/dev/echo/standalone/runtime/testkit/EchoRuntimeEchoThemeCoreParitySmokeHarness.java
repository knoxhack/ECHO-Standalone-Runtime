package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoThemeCoreStandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoThemeCoreParitySmokeHarness {
    private EchoRuntimeEchoThemeCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoThemeCoreStandaloneAdapter standaloneAdapter = new EchoThemeCoreStandaloneAdapter();
        Map<String, Object> nativeApplication = standaloneAdapter.executeThemeApplication(
                EchoThemeCoreStandaloneAdapter.REFERENCE_THEME_ID,
                EchoThemeCoreStandaloneAdapter.REFERENCE_SURFACE_ID);
        Map<String, Object> standaloneApplication = standaloneAdapter.executeThemeApplication(
                EchoThemeCoreStandaloneAdapter.REFERENCE_THEME_ID,
                EchoThemeCoreStandaloneAdapter.REFERENCE_SURFACE_ID);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(standaloneAdapter.referenceApplicationPassed(nativeApplication),
                "native ThemeCore reference theme application should pass");
        require(standaloneAdapter.referenceApplicationPassed(standaloneApplication),
                "standalone ThemeCore theme application should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("themeApplicationExecuted")),
                "standalone activation should execute theme application");
        require(nativeApplication.get("adapterCoreContract").equals(standaloneApplication.get("adapterCoreContract")),
                "native and standalone AdapterCore contracts should match");
        require(nativeApplication.get("selectedThemeId").equals(standaloneApplication.get("selectedThemeId")),
                "native and standalone selected themes should match");
        require(nativeApplication.get("surfaceId").equals(standaloneApplication.get("surfaceId")),
                "native and standalone surfaces should match");
        require(nativeApplication.get("selectedTheme").equals(standaloneApplication.get("selectedTheme")),
                "native and standalone theme summaries should match");
        require(nativeApplication.get("colorTokens").equals(standaloneApplication.get("colorTokens")),
                "native and standalone color tokens should match");
        require(nativeApplication.get("textureTokens").equals(standaloneApplication.get("textureTokens")),
                "native and standalone texture tokens should match");
        require(nativeApplication.get("renderTokens").equals(standaloneApplication.get("renderTokens")),
                "native and standalone render tokens should match");
        require(nativeApplication.get("layoutTokens").equals(standaloneApplication.get("layoutTokens")),
                "native and standalone layout tokens should match");
        require(nativeApplication.get("surfaceAssets").equals(standaloneApplication.get("surfaceAssets")),
                "native and standalone surface asset bindings should match");
        require(nativeApplication.get("soundBindings").equals(standaloneApplication.get("soundBindings")),
                "native and standalone sound bindings should match");

        System.out.println("echothemecore parity smoke PASS contract="
                + nativeApplication.get("adapterCoreContract")
                + " theme="
                + nativeApplication.get("selectedThemeId")
                + " surfaceAssets="
                + ((List<?>) nativeApplication.get("surfaceAssets")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
