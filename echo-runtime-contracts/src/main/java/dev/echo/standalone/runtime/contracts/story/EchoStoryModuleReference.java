package dev.echo.standalone.runtime.contracts.story;

import java.util.List;
import java.util.Objects;

public record EchoStoryModuleReference(
        String moduleId,
        String featureAudited,
        String behavior,
        List<String> contentIds
) {
    public EchoStoryModuleReference {
        moduleId = requireText(moduleId, "moduleId");
        featureAudited = requireText(featureAudited, "featureAudited");
        behavior = requireText(behavior, "behavior");
        Objects.requireNonNull(contentIds, "contentIds");
        contentIds = List.copyOf(contentIds);
        if (contentIds.isEmpty()) {
            throw new IllegalArgumentException("contentIds must not be empty");
        }
        for (String contentId : contentIds) {
            requireText(contentId, "contentId");
        }
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
