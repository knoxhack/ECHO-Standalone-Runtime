package dev.echo.standalone.runtime.assets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class EchoTextureAtlasStitcher {
    private final EchoMinecraftAssetResolver resolver;

    public EchoTextureAtlasStitcher(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public EchoTextureAtlasPlan plan(Collection<String> textureIds) {
        ArrayList<String> resolved = new ArrayList<>();
        ArrayList<String> missing = new ArrayList<>();
        for (String textureId : textureIds == null ? List.<String>of() : textureIds) {
            String[] parts = splitId(textureId);
            if (resolver.texture(parts[0], parts[1]).isPresent()) {
                resolved.add(textureId);
            } else {
                missing.add(textureId);
            }
        }
        return new EchoTextureAtlasPlan(resolved, missing, EchoMissingTexture.LOGICAL_ID);
    }

    private static String[] splitId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("texture id must not be blank");
        }
        int separator = id.indexOf(':');
        if (separator < 1 || separator == id.length() - 1) {
            throw new IllegalArgumentException("texture id must be namespaced: " + id);
        }
        return new String[]{id.substring(0, separator), id.substring(separator + 1)};
    }

    public record EchoTextureAtlasPlan(List<String> resolvedTextureIds, List<String> missingTextureIds, String missingTextureId) {
        public EchoTextureAtlasPlan {
            resolvedTextureIds = List.copyOf(resolvedTextureIds);
            missingTextureIds = List.copyOf(missingTextureIds);
            missingTextureId = missingTextureId == null || missingTextureId.isBlank()
                    ? EchoMissingTexture.LOGICAL_ID
                    : missingTextureId;
        }
    }
}
