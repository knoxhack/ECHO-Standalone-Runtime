package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoLangResolver;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class EchoClientLanguageService {
    private static final String DEFAULT_LOCALE = EchoClientSettings.DEFAULT_LANGUAGE_CODE;

    private EchoMinecraftAssetResolver minecraftAssets;
    private EchoLangResolver langResolver;
    private final Map<String, Map<String, String>> cache = new HashMap<>();
    private String locale = DEFAULT_LOCALE;

    EchoClientLanguageService(EchoMinecraftAssetResolver minecraftAssets) {
        setMinecraftAssets(minecraftAssets);
    }

    void setMinecraftAssets(EchoMinecraftAssetResolver minecraftAssets) {
        this.minecraftAssets = minecraftAssets;
        this.langResolver = minecraftAssets == null ? null : new EchoLangResolver(minecraftAssets);
        cache.clear();
    }

    void applySettings(EchoClientSettings settings) {
        setLocale(settings == null ? DEFAULT_LOCALE : settings.languageCode());
    }

    void setLocale(String locale) {
        String normalized = EchoClientSettings.normalizeLanguageCode(locale);
        if (this.locale.equals(normalized)) {
            return;
        }
        this.locale = normalized;
        cache.clear();
    }

    String locale() {
        return locale;
    }

    String blockName(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return "Air";
        }
        return blockName(block.id(), block.displayName());
    }

    String blockName(String blockId, String fallback) {
        return translateId("block", blockId, fallback);
    }

    String itemName(String itemId, String fallback) {
        return translateId("item", itemId, fallback);
    }

    String translateId(String kind, String id, String fallback) {
        String[] parts = splitId(id);
        if (parts.length != 2) {
            return fallbackName(id, fallback);
        }
        String key = kind + "." + parts[0] + "." + parts[1].replace('/', '.');
        return namespaceLang(parts[0]).getOrDefault(key, fallbackName(parts[1], fallback));
    }

    private Map<String, String> namespaceLang(String namespace) {
        if (langResolver == null || minecraftAssets == null || namespace == null || namespace.isBlank()) {
            return Map.of();
        }
        return cache.computeIfAbsent(namespace, this::loadNamespaceLang);
    }

    private Map<String, String> loadNamespaceLang(String namespace) {
        try {
            Map<String, String> localized = langResolver.load(namespace, locale);
            if (!localized.isEmpty() || DEFAULT_LOCALE.equals(locale)) {
                return localized;
            }
            return langResolver.load(namespace, DEFAULT_LOCALE);
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("[echo-client] lang load failed for "
                    + namespace + "/" + locale + ": " + exception.getMessage());
            return Map.of();
        }
    }

    private static String fallbackName(String id, String fallback) {
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        String value = id == null || id.isBlank() ? "Unknown" : id;
        int separator = value.indexOf(':');
        if (separator >= 0 && separator < value.length() - 1) {
            value = value.substring(separator + 1);
        }
        value = value.replace('\\', '/');
        int slash = value.lastIndexOf('/');
        if (slash >= 0 && slash < value.length() - 1) {
            value = value.substring(slash + 1);
        }
        String[] words = value.toLowerCase(Locale.ROOT).split("[_\\-.]+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1));
            }
        }
        return result.isEmpty() ? "Unknown" : result.toString();
    }

    private static String[] splitId(String id) {
        if (id == null) {
            return new String[0];
        }
        String normalized = id.trim();
        int separator = normalized.indexOf(':');
        if (separator < 1 || separator == normalized.length() - 1) {
            return new String[0];
        }
        return new String[]{normalized.substring(0, separator), normalized.substring(separator + 1)};
    }
}
