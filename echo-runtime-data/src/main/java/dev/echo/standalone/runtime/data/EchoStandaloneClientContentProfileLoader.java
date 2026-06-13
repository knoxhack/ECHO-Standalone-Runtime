package dev.echo.standalone.runtime.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoStandaloneClientContentProfileLoader {
    public static final String ASHFALL_CRASH_SITE_RESOURCE =
            "/data/echoashfallprotocol/standalone/client_content/ashfall_crash_site.json";
    public static final String OPENLANDS_FIRST_HOUR_RESOURCE =
            "/data/echoopenlandsprotocol/standalone/client_content/openlands_first_hour.json";

    private EchoStandaloneClientContentProfileLoader() {
    }

    public static EchoStandaloneClientContentProfile loadAshfallCrashSite() {
        return loadClasspath(ASHFALL_CRASH_SITE_RESOURCE);
    }

    public static EchoStandaloneClientContentProfile loadOpenlandsFirstHour() {
        return loadClasspath(OPENLANDS_FIRST_HOUR_RESOURCE);
    }

    public static EchoStandaloneClientContentProfile loadClasspath(String resourcePath) {
        String normalized = resourcePath == null || resourcePath.isBlank() ? ASHFALL_CRASH_SITE_RESOURCE : resourcePath.trim();
        try (InputStream stream = EchoStandaloneClientContentProfileLoader.class.getResourceAsStream(normalized)) {
            if (stream == null) {
                throw new IllegalArgumentException("Missing client content profile resource: " + normalized);
            }
            return load(normalized, new String(stream.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new UncheckedIOException("Unable to load client content profile: " + normalized, exception);
        }
    }

    public static EchoStandaloneClientContentProfile load(String sourceLogicalId, String json) {
        Map<String, Object> object = EchoDataObjects.object(sourceLogicalId, json);
        return new EchoStandaloneClientContentProfile(
                string(object, "id"),
                worldTemplate(object(object, "worldTemplate")),
                entityCatalog(object(object, "entityCatalog")),
                hazardCatalog(object(object, "hazardCatalog")),
                starterLoadout(object(object, "starterLoadout")),
                interactionCatalog(object(object, "interactionCatalog"))
        );
    }

    private static EchoStandaloneClientContentProfile.WorldTemplate worldTemplate(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.WorldTemplate(
                string(object, "slotIdPrefix"),
                string(object, "displayName"),
                saveProfile(object(object, "saveProfile")),
                presentation(object(object, "presentation")),
                audioProfile(object(object, "audioProfile"))
        );
    }

    private static EchoStandaloneClientContentProfile.SaveProfile saveProfile(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.SaveProfile(
                string(object, "schema"),
                string(object, "profileId"),
                string(object, "displayName"),
                string(object, "packId"),
                integer(object, "formatVersion"),
                stringMap(object, "metadata")
        );
    }

    private static EchoStandaloneClientContentProfile.Presentation presentation(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.Presentation(
                string(object, "windowTitle"),
                string(object, "settingsFileComment"),
                string(object, "createWorldActionLabel"),
                string(object, "worldTypeLabel"),
                string(object, "packLabel"),
                string(object, "newWorldModalMessage"),
                string(object, "loadingInitialDetail"),
                string(object, "newWorldGenerationLabel"),
                string(object, "newWorldDetailSuffix"),
                string(object, "newWorldLoadingFooter"),
                string(object, "savedWorldLoadingFooter"),
                stringMap(object, "moduleSourceLabels"),
                string(object, "hostileDamageSourceId")
        );
    }

    private static EchoStandaloneClientContentProfile.AudioProfile audioProfile(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.AudioProfile(
                string(object, "deviceProfileId"),
                string(object, "volumeProfileId")
        );
    }

    private static EchoStandaloneClientContentProfile.EntityCatalog entityCatalog(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.EntityCatalog(
                entityDefinition(object(object, "fallbackHostile")),
                objectList(object, "spawnRules").stream()
                        .map(rule -> new EchoStandaloneClientContentProfile.SpawnRule(
                                stringList(rule, "biomeTags"),
                                entityDefinition(object(rule, "definition"))
                        ))
                        .toList(),
                objectList(object, "renderProfiles").stream()
                        .map(profile -> new EchoStandaloneClientContentProfile.RenderProfile(
                                string(profile, "definitionId"),
                                argb(profile, "argb"),
                                string(profile, "shape")
                        ))
                        .toList()
        );
    }

    private static EchoStandaloneClientContentProfile.EntityDefinition entityDefinition(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.EntityDefinition(
                string(object, "id"),
                string(object, "displayName"),
                integer(object, "maxHealth"),
                string(object, "aiProfile")
        );
    }

    private static EchoStandaloneClientContentProfile.HazardCatalog hazardCatalog(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.HazardCatalog(
                objectList(object, "rules").stream()
                        .map(rule -> new EchoStandaloneClientContentProfile.HazardRule(
                                stringList(rule, "biomeTags"),
                                hazardProfile(object(rule, "profile"))
                        ))
                        .toList()
        );
    }

    private static EchoStandaloneClientContentProfile.HazardProfile hazardProfile(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.HazardProfile(
                string(object, "id"),
                string(object, "label"),
                decimal(object, "exposurePerSecond"),
                integer(object, "damage")
        );
    }

    private static EchoStandaloneClientContentProfile.StarterLoadout starterLoadout(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.StarterLoadout(
                string(object, "playerInventoryId"),
                string(object, "playerInventoryLabel"),
                string(object, "openContainerId"),
                string(object, "openContainerLabel"),
                objectList(object, "items").stream()
                        .map(item -> new EchoStandaloneClientContentProfile.StarterItem(
                                string(item, "id"),
                                string(item, "displayName"),
                                string(item, "category"),
                                integer(item, "maxStackSize"),
                                stringList(item, "tags"),
                                stringList(item, "tooltipLines")
                        ))
                        .toList(),
                objectList(object, "openContainerStacks").stream()
                        .map(stack -> new EchoStandaloneClientContentProfile.StarterStack(
                                integer(stack, "slotIndex"),
                                string(stack, "itemId"),
                                integer(stack, "quantity")
                        ))
                        .toList(),
                objectList(object, "workbenchRecipes").stream()
                        .map(recipe -> new EchoStandaloneClientContentProfile.StarterRecipe(
                                string(recipe, "recipeId"),
                                intMap(recipe, "ingredients"),
                                string(recipe, "outputItemId"),
                                integer(recipe, "outputQuantity")
                        ))
                        .toList()
        );
    }

    private static EchoStandaloneClientContentProfile.InteractionCatalog interactionCatalog(Map<String, Object> object) {
        return new EchoStandaloneClientContentProfile.InteractionCatalog(
                objectList(object, "rules").stream()
                        .map(rule -> new EchoStandaloneClientContentProfile.InteractionRule(
                                stringList(rule, "matchTokens"),
                                string(rule, "command"),
                                EchoDataObjects.string(rule, "targetId", "")
                        ))
                        .toList()
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Expected object field: " + key);
        }
        return (Map<String, Object>) map;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> objectList(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("Expected object entry in field: " + key);
            }
            result.add((Map<String, Object>) map);
        }
        return List.copyOf(result);
    }

    private static List<String> stringList(Map<String, Object> object, String key) {
        return EchoDataObjects.stringList(object, key);
    }

    private static String string(Map<String, Object> object, String key) {
        String value = EchoDataObjects.string(object, key, "");
        if (value.isBlank()) {
            throw new IllegalArgumentException("Expected string field: " + key);
        }
        return value;
    }

    private static int integer(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (value instanceof Number number) {
            return Math.toIntExact(number.longValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        throw new IllegalArgumentException("Expected integer field: " + key);
    }

    private static double decimal(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text.trim());
        }
        throw new IllegalArgumentException("Expected decimal field: " + key);
    }

    private static int argb(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (value instanceof Number number) {
            return (int) number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            String normalized = text.trim();
            if (normalized.startsWith("#")) {
                normalized = normalized.substring(1);
            } else if (normalized.startsWith("0x") || normalized.startsWith("0X")) {
                normalized = normalized.substring(2);
            }
            return (int) Long.parseUnsignedLong(normalized, 16);
        }
        throw new IllegalArgumentException("Expected ARGB field: " + key);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> intMap(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) map).entrySet()) {
            String itemId = String.valueOf(entry.getKey());
            Object quantity = entry.getValue();
            if (quantity instanceof Number number) {
                result.put(itemId, Math.toIntExact(number.longValue()));
            } else {
                result.put(itemId, Integer.parseInt(String.valueOf(quantity)));
            }
        }
        return Map.copyOf(result);
    }

    private static Map<String, String> stringMap(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return Map.copyOf(result);
    }
}
