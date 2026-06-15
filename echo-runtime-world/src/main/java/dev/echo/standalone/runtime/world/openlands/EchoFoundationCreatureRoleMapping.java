package dev.echo.standalone.runtime.world.openlands;

/**
 * Maps an Openlands creature ID to a canonical Foundation creature role.
 */
public record EchoFoundationCreatureRoleMapping(
        String creatureId,
        String legacyCategory,
        String foundationRole
) {
    public EchoFoundationCreatureRoleMapping {
        creatureId = creatureId == null || creatureId.isBlank() ? "" : creatureId.trim();
        legacyCategory = legacyCategory == null ? "" : legacyCategory.trim();
        foundationRole = foundationRole == null ? "" : foundationRole.trim();
    }
}
