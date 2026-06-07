package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.world.EchoWorldPoi;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;
import java.util.Optional;

public final class EchoPlayerInteractionTargeter {
    private final EchoWorldRuntimeResult world;
    private final int range;

    public EchoPlayerInteractionTargeter(EchoWorldRuntimeResult world, int range) {
        this.world = Objects.requireNonNull(world, "world");
        if (range < 0) {
            throw new IllegalArgumentException("range must not be negative");
        }
        this.range = range;
    }

    public Optional<EchoPlayerInteractionTarget> target(EchoEntityState player, EchoPlayerFacing facing) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(facing, "facing");
        EchoWorldPosition playerPosition = player.worldPosition();
        EchoWorldPosition facingPosition = new EchoWorldPosition(
                playerPosition.x() + facing.deltaX(),
                playerPosition.y(),
                playerPosition.z() + facing.deltaZ()
        );
        return world.world().chunks().stream()
                .flatMap(chunk -> chunk.pointsOfInterest().stream())
                .map(poi -> toTarget(poi, playerPosition, facingPosition))
                .filter(target -> target.distance() <= range)
                .sorted(EchoPlayerInteractionTargeter::compareTargets)
                .findFirst();
    }

    private static int compareTargets(EchoPlayerInteractionTarget left, EchoPlayerInteractionTarget right) {
        int exact = Boolean.compare(right.exact(), left.exact());
        if (exact != 0) {
            return exact;
        }
        int facing = Boolean.compare(right.facing(), left.facing());
        if (facing != 0) {
            return facing;
        }
        int distance = Integer.compare(left.distance(), right.distance());
        if (distance != 0) {
            return distance;
        }
        return left.id().compareTo(right.id());
    }

    private static EchoPlayerInteractionTarget toTarget(
            EchoWorldPoi poi,
            EchoWorldPosition playerPosition,
            EchoWorldPosition facingPosition
    ) {
        int distance = Math.max(
                Math.abs(poi.position().x() - playerPosition.x()),
                Math.abs(poi.position().z() - playerPosition.z())
        );
        return new EchoPlayerInteractionTarget(
                poi.id(),
                poi.type(),
                poi.label(),
                poi.position(),
                distance,
                poi.position().equals(playerPosition),
                poi.position().equals(facingPosition)
        );
    }
}
