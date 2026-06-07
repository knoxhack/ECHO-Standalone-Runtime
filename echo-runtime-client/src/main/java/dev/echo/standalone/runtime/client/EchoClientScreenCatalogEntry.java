package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.ui.EchoScreenRoute;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;

import java.util.List;
import java.util.Locale;

record EchoClientScreenCatalogEntry(
        String screenId,
        String title,
        String source,
        EchoScreenRoute route,
        String contentId,
        String adapterKey,
        String nativeLoaderId,
        String standaloneRuntimeId,
        List<String> lines
) {
    EchoClientScreenCatalogEntry {
        screenId = requireText(screenId, "screenId");
        title = requireText(title, "title");
        source = requireText(source, "source");
        if (route == null) {
            throw new IllegalArgumentException("route must not be null");
        }
        contentId = optionalText(contentId);
        adapterKey = optionalText(adapterKey);
        nativeLoaderId = optionalText(nativeLoaderId);
        standaloneRuntimeId = optionalText(standaloneRuntimeId);
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    static EchoClientScreenCatalogEntry screenCore(
            String screenId,
            String title,
            String route,
            String focusPath,
            String summary
    ) {
        return new EchoClientScreenCatalogEntry(
                screenId,
                title,
                "ScreenCore",
                new EchoScreenRoute(screenId, route, focusPath),
                screenId,
                route,
                route,
                screenId,
                List.of(summary, "Route: " + route, "Focus: " + focusPath)
        );
    }

    static EchoClientScreenCatalogEntry adapterCore(EchoAdapterCoreRegistryEntry entry) {
        return adapterCore(entry, EchoClientWorldPresentation.generic());
    }

    static EchoClientScreenCatalogEntry adapterCore(
            EchoAdapterCoreRegistryEntry entry,
            EchoClientWorldPresentation presentation
    ) {
        String screenId = entry.standaloneRuntimeId();
        String route = "adaptercore." + routeToken(screenId);
        String focusPath = route + ".primary";
        String nativeLoaderId = entry.idFor(EchoAdapterCoreRuntimeKind.ECHO_NATIVE_LOADER);
        EchoClientWorldPresentation safePresentation =
                presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        return new EchoClientScreenCatalogEntry(
                screenId,
                entry.displayName(),
                safePresentation.sourceLabel(entry.binding().moduleId()),
                new EchoScreenRoute(screenId, route, focusPath),
                entry.contentId(),
                entry.binding().adapterKey(),
                nativeLoaderId,
                entry.standaloneRuntimeId(),
                List.of(
                        "Content: " + entry.contentId(),
                        "Adapter: " + entry.binding().adapterKey(),
                        "Native: " + nativeLoaderId,
                        "Standalone: " + entry.standaloneRuntimeId()
                )
        );
    }

    EchoStaticScreen screen() {
        return new EchoStaticScreen(screenId, title, lines, route.focusPath());
    }

    String menuLabel() {
        return truncate(title, 30) + " (" + truncate(source, 10) + ")";
    }

    String diagnosticLabel() {
        return truncate(title, 18) + " -> " + truncate(route.route(), 16);
    }

    String tooltip() {
        if (contentId.isBlank()) {
            return title + " via " + route.route();
        }
        return title + " | " + contentId + " | " + route.route();
    }

    private static String routeToken(String value) {
        String normalized = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        boolean previousSeparator = false;
        for (int index = 0; index < normalized.length(); index++) {
            char ch = normalized.charAt(index);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                result.append(ch);
                previousSeparator = false;
            } else if (!previousSeparator) {
                result.append('.');
                previousSeparator = true;
            }
        }
        while (!result.isEmpty() && result.charAt(result.length() - 1) == '.') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.isEmpty() ? "screen" : result.toString();
    }

    private static String truncate(String value, int maxLength) {
        String text = optionalText(value);
        if (text.length() <= maxLength) {
            return text;
        }
        if (maxLength <= 3) {
            return text.substring(0, maxLength);
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    private static String requireText(String value, String name) {
        String text = optionalText(value);
        if (text.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return text;
    }

    private static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }
}
