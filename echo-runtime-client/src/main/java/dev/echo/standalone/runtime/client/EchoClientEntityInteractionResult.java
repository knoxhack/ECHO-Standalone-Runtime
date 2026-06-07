package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

record EchoClientEntityInteractionResult(
        boolean hit,
        String reason,
        EchoEntityId entityId,
        String definitionId,
        String displayName,
        EchoEntityKind kind,
        EchoWorldPosition position,
        double distance,
        EchoClientScreenRouteRequest route
) {
    private static final String ENTITY_INTERACTION_SCREEN = "echoscreencore:entity_interaction";

    EchoClientEntityInteractionResult {
        reason = reason == null || reason.isBlank() ? (hit ? "interacted" : "miss") : reason.trim();
        definitionId = definitionId == null ? "" : definitionId.trim();
        displayName = displayName == null || displayName.isBlank() ? definitionId : displayName.trim();
        route = route == null ? EchoClientScreenRouteRequest.NONE : route;
    }

    static EchoClientEntityInteractionResult miss(String reason) {
        return new EchoClientEntityInteractionResult(
                false,
                reason,
                null,
                "",
                "",
                null,
                null,
                0.0D,
                EchoClientScreenRouteRequest.NONE
        );
    }

    static EchoClientEntityInteractionResult hit(EchoEntityState entity, double distance) {
        return new EchoClientEntityInteractionResult(
                true,
                "interacted",
                entity.id(),
                entity.definition().definitionId(),
                entity.definition().displayName(),
                entity.definition().kind(),
                entity.worldPosition(),
                Math.max(0.0D, distance),
                new EchoClientScreenRouteRequest(
                        EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                        ENTITY_INTERACTION_SCREEN
                )
        );
    }
}
