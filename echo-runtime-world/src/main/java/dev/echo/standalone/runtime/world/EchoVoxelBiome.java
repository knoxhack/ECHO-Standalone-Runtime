package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Objects;

public record EchoVoxelBiome(
        String id,
        String displayName,
        double temperature,
        double downfall,
        int fogColor,
        int grassColor,
        String ambientParticle,
        List<String> tags
) {
    public EchoVoxelBiome {
        id = EchoWorldText.requireText(id, "id");
        displayName = EchoWorldText.requireText(displayName, "displayName");
        if (!Double.isFinite(temperature)) {
            throw new IllegalArgumentException("temperature must be finite");
        }
        if (!Double.isFinite(downfall) || downfall < 0.0D) {
            throw new IllegalArgumentException("downfall must be finite and non-negative");
        }
        ambientParticle = ambientParticle == null || ambientParticle.isBlank()
                ? "minecraft:ash"
                : ambientParticle.trim();
        Objects.requireNonNull(tags, "tags");
        tags = tags.stream()
                .filter(tag -> tag != null && !tag.isBlank())
                .map(String::trim)
                .sorted()
                .toList();
    }

    public String path() {
        int separator = id.indexOf(':');
        return separator >= 0 ? id.substring(separator + 1) : id;
    }

    public boolean hasTag(String tag) {
        if (tag == null || tag.isBlank()) {
            return false;
        }
        return tags.contains(tag.trim());
    }
}
