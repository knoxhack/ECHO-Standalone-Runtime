package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.Objects;
import java.util.OptionalDouble;

public final class EchoVoxelPlayerController {
    private static final double WALK_SPEED = 4.2D;
    private static final double SPRINT_SPEED = 6.4D;
    private static final double CROUCH_SPEED = 1.9D;
    private static final double SWIM_SPEED = 2.4D;
    private static final double SWIM_SPRINT_SPEED = 3.1D;
    private static final double SWIM_CROUCH_SPEED = 1.2D;
    private static final double JUMP_VELOCITY = 7.8D;
    private static final double GRAVITY = 22.0D;
    private static final double TERMINAL_VELOCITY = -18.0D;
    private static final double SWIM_UPWARD_SPEED = 3.2D;
    private static final double SWIM_SINK_SPEED = -2.0D;
    private static final double SWIM_TERMINAL_VELOCITY = -3.0D;
    private static final double SWIM_DRAG = 0.55D;
    private static final double SWIM_BUOYANCY_ACCEL = 8.0D;
    private static final double MAX_STEP_HEIGHT = 1.0D;
    private static final double GROUND_SNAP_TOLERANCE = 0.08D;
    private static final int COLLISION_CLIP_ITERATIONS = 8;
    private EchoVoxelPlayerState state;

    public EchoVoxelPlayerController(EchoVoxelPlayerState initialState) {
        this.state = Objects.requireNonNull(initialState, "initialState");
    }

    public static EchoVoxelPlayerController spawnAt(
            EchoVoxelWorld world,
            double x,
            double z,
            double yawDegrees,
            double pitchDegrees
    ) {
        Objects.requireNonNull(world, "world");
        double y = standingY(world, x, z, 1.82D).orElse(4.0D);
        return new EchoVoxelPlayerController(new EchoVoxelPlayerState(
                x,
                y,
                z,
                0.0D,
                wrapDegrees(yawDegrees),
                clamp(pitchDegrees, -75.0D, 55.0D),
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        ));
    }

    public EchoVoxelPlayerState state() {
        return state;
    }

    public EchoVoxelPlayerState selectSlot(int selectedSlot) {
        state = new EchoVoxelPlayerState(
                state.x(),
                state.y(),
                state.z(),
                state.velocityY(),
                state.yawDegrees(),
                state.pitchDegrees(),
                state.grounded(),
                state.crouching(),
                state.sprinting(),
                selectedSlot,
                state.reach()
        );
        return state;
    }

    public EchoVoxelPlayerStep tick(EchoVoxelWorld world, EchoVoxelPlayerInput input, double seconds) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(input, "input");
        double dt = clamp(seconds, 0.0D, 0.1D);
        EchoVoxelPlayerState previous = state;
        if (dt == 0.0D && !input.wantsLook()) {
            return new EchoVoxelPlayerStep(previous, state, false, false, false, false, "idle");
        }

        double yaw = wrapDegrees(state.yawDegrees() + input.yawDeltaDegrees());
        double pitch = clamp(state.pitchDegrees() + input.pitchDeltaDegrees(), -75.0D, 55.0D);
        boolean crouching = input.crouch();
        boolean sprinting = input.sprint() && input.forward() && !crouching;
        double bodyHeight = crouching ? 1.35D : 1.82D;
        boolean inFluid = isBodyInFluid(world, state.x(), state.y(), state.z(), bodyHeight);
        double speed = inFluid
                ? (crouching ? SWIM_CROUCH_SPEED : sprinting ? SWIM_SPRINT_SPEED : SWIM_SPEED)
                : (crouching ? CROUCH_SPEED : sprinting ? SPRINT_SPEED : WALK_SPEED);
        double moveX = 0.0D;
        double moveZ = 0.0D;
        if (input.wantsHorizontalMovement()) {
            double yawRadians = Math.toRadians(yaw);
            double forwardX = Math.sin(yawRadians);
            double forwardZ = Math.cos(yawRadians);
            double rightX = Math.cos(yawRadians);
            double rightZ = -Math.sin(yawRadians);
            if (input.forward()) {
                moveX += forwardX;
                moveZ += forwardZ;
            }
            if (input.backward()) {
                moveX -= forwardX;
                moveZ -= forwardZ;
            }
            if (input.strafeRight()) {
                moveX += rightX;
                moveZ += rightZ;
            }
            if (input.strafeLeft()) {
                moveX -= rightX;
                moveZ -= rightZ;
            }
            double length = Math.hypot(moveX, moveZ);
            if (length > 0.0D) {
                moveX = moveX / length * speed * dt;
                moveZ = moveZ / length * speed * dt;
            }
        }

        double x = state.x();
        double y = state.y();
        double z = state.z();
        double velocityY = state.velocityY();
        boolean grounded = state.grounded();
        boolean jumped = false;
        if (inFluid) {
            grounded = false;
            if (input.jump() && !crouching) {
                velocityY = Math.min(
                        SWIM_UPWARD_SPEED,
                        Math.max(1.2D, velocityY * SWIM_DRAG + SWIM_BUOYANCY_ACCEL * dt)
                );
                jumped = true;
            } else if (crouching) {
                velocityY = Math.max(
                        SWIM_SINK_SPEED,
                        velocityY * SWIM_DRAG - SWIM_BUOYANCY_ACCEL * 0.35D * dt
                );
            } else {
                velocityY = Math.max(
                        SWIM_TERMINAL_VELOCITY,
                        Math.min(1.0D, velocityY * SWIM_DRAG + SWIM_BUOYANCY_ACCEL * 0.25D * dt)
                );
            }
        } else {
            if (input.jump() && grounded && !crouching) {
                velocityY = JUMP_VELOCITY;
                grounded = false;
                jumped = true;
            }
            if (!grounded || jumped) {
                velocityY = Math.max(TERMINAL_VELOCITY, velocityY - GRAVITY * dt);
            }
        }

        boolean collidedHorizontal = false;
        double newY = y;
        if (moveX != 0.0D) {
            HorizontalMoveResult move = moveHorizontally(world, x, newY, z, moveX, 0.0D, bodyHeight, grounded);
            x = move.x();
            newY = move.y();
            z = move.z();
            collidedHorizontal = collidedHorizontal || move.collided();
        }
        if (moveZ != 0.0D) {
            HorizontalMoveResult move = moveHorizontally(world, x, newY, z, 0.0D, moveZ, bodyHeight, grounded);
            x = move.x();
            newY = move.y();
            z = move.z();
            collidedHorizontal = collidedHorizontal || move.collided();
        }
        y = newY;

        boolean collidedVertical = false;
        double nextY = y + velocityY * dt;
        if (tryMove(world, x, nextY, z, bodyHeight).allowed()) {
            y = nextY;
            if (velocityY <= 0.0D && hasGroundBelow(world, x, y, z)) {
                OptionalDouble standY = standingY(world, x, z, bodyHeight);
                if (standY.isPresent() && Math.abs(standY.orElseThrow() - y) <= GROUND_SNAP_TOLERANCE) {
                    y = standY.orElseThrow();
                    velocityY = 0.0D;
                    grounded = true;
                } else {
                    grounded = false;
                }
            } else {
                grounded = false;
            }
        } else {
            collidedVertical = velocityY != 0.0D;
            if (velocityY < 0.0D) {
                y = standingY(world, x, z, bodyHeight).orElse(y);
                grounded = true;
            }
            velocityY = 0.0D;
        }

        if (!hasGroundBelow(world, x, y, z) && velocityY <= 0.0D) {
            grounded = false;
        }
        state = new EchoVoxelPlayerState(
                x,
                y,
                z,
                velocityY,
                yaw,
                pitch,
                grounded,
                crouching,
                sprinting,
                state.selectedSlot(),
                state.reach()
        );
        boolean moved = Math.abs(previous.x() - state.x()) > 0.0001D
                || Math.abs(previous.y() - state.y()) > 0.0001D
                || Math.abs(previous.z() - state.z()) > 0.0001D
                || Math.abs(previous.yawDegrees() - state.yawDegrees()) > 0.0001D
                || Math.abs(previous.pitchDegrees() - state.pitchDegrees()) > 0.0001D
                || previous.crouching() != state.crouching()
                || previous.sprinting() != state.sprinting()
                || previous.grounded() != state.grounded();
        String reason = jumped
                ? (inFluid ? "swim" : "jump")
                : collidedHorizontal
                        ? "horizontal_collision"
                        : collidedVertical ? "vertical_collision" : inFluid && moved ? "fluid" : moved ? "moved" : "idle";
        return new EchoVoxelPlayerStep(previous, state, moved, jumped, collidedHorizontal, collidedVertical, reason);
    }

    private static MoveResult tryMove(EchoVoxelWorld world, double x, double y, double z, double bodyHeight) {
        return new MoveResult(canOccupy(world, x, y, z, bodyHeight));
    }

    private static HorizontalMoveResult moveHorizontally(
            EchoVoxelWorld world,
            double x,
            double y,
            double z,
            double dx,
            double dz,
            double bodyHeight,
            boolean grounded
    ) {
        double targetX = x + dx;
        double targetZ = z + dz;
        if (canOccupy(world, targetX, y, targetZ, bodyHeight)) {
            return new HorizontalMoveResult(targetX, y, targetZ, false);
        }
        if (grounded) {
            OptionalDouble steppedY = standingY(world, targetX, targetZ, bodyHeight);
            if (steppedY.isPresent()) {
                double candidateY = steppedY.orElseThrow();
                if (candidateY > y
                        && candidateY - y <= MAX_STEP_HEIGHT + 0.001D
                        && canOccupy(world, targetX, candidateY, targetZ, bodyHeight)) {
                    return new HorizontalMoveResult(targetX, candidateY, targetZ, false);
                }
            }
        }

        double allowedFraction = clippedMoveFraction(world, x, y, z, dx, dz, bodyHeight);
        if (allowedFraction > 0.0D) {
            return new HorizontalMoveResult(
                    x + dx * allowedFraction,
                    y,
                    z + dz * allowedFraction,
                    true
            );
        }
        return new HorizontalMoveResult(x, y, z, true);
    }

    private static double clippedMoveFraction(
            EchoVoxelWorld world,
            double x,
            double y,
            double z,
            double dx,
            double dz,
            double bodyHeight
    ) {
        double low = 0.0D;
        double high = 1.0D;
        for (int i = 0; i < COLLISION_CLIP_ITERATIONS; i++) {
            double mid = (low + high) * 0.5D;
            if (canOccupy(world, x + dx * mid, y, z + dz * mid, bodyHeight)) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return low < 0.001D ? 0.0D : low;
    }

    private static boolean canOccupy(EchoVoxelWorld world, double x, double y, double z, double bodyHeight) {
        return !world.collidesWithBox(
                x - EchoVoxelPlayerState.PLAYER_RADIUS,
                y + 0.02D,
                z - EchoVoxelPlayerState.PLAYER_RADIUS,
                x + EchoVoxelPlayerState.PLAYER_RADIUS,
                y + bodyHeight - 0.02D,
                z + EchoVoxelPlayerState.PLAYER_RADIUS
        );
    }

    private static boolean isBodyInFluid(EchoVoxelWorld world, double x, double y, double z, double bodyHeight) {
        int minX = floor(x - EchoVoxelPlayerState.PLAYER_RADIUS);
        int maxX = floor(Math.nextDown(x + EchoVoxelPlayerState.PLAYER_RADIUS));
        int minY = floor(y + 0.02D);
        int maxY = floor(Math.nextDown(y + bodyHeight - 0.02D));
        int minZ = floor(z - EchoVoxelPlayerState.PLAYER_RADIUS);
        int maxZ = floor(Math.nextDown(z + EchoVoxelPlayerState.PLAYER_RADIUS));
        for (int blockY = minY; blockY <= maxY; blockY++) {
            for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                for (int blockX = minX; blockX <= maxX; blockX++) {
                    if (EchoVoxelFluidRuntime.isFluid(world.blockStateAt(blockX, blockY, blockZ))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasGroundBelow(EchoVoxelWorld world, double x, double y, double z) {
        int minX = floor(x - EchoVoxelPlayerState.PLAYER_RADIUS);
        int maxX = floor(x + EchoVoxelPlayerState.PLAYER_RADIUS);
        int blockY = floor(y - 0.06D);
        int minZ = floor(z - EchoVoxelPlayerState.PLAYER_RADIUS);
        int maxZ = floor(z + EchoVoxelPlayerState.PLAYER_RADIUS);
        for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
            for (int blockX = minX; blockX <= maxX; blockX++) {
                if (world.blockAt(blockX, blockY, blockZ).solid()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static OptionalDouble standingY(EchoVoxelWorld world, double x, double z, double bodyHeight) {
        int minX = floor(x - EchoVoxelPlayerState.PLAYER_RADIUS);
        int maxX = floor(Math.nextDown(x + EchoVoxelPlayerState.PLAYER_RADIUS));
        int minZ = floor(z - EchoVoxelPlayerState.PLAYER_RADIUS);
        int maxZ = floor(Math.nextDown(z + EchoVoxelPlayerState.PLAYER_RADIUS));
        for (int blockY = world.chunkSize() - 1; blockY >= 0; blockY--) {
            for (int blockZ = minZ; blockZ <= maxZ; blockZ++) {
                for (int blockX = minX; blockX <= maxX; blockX++) {
                    if (!world.blockAt(blockX, blockY, blockZ).solid()) {
                        continue;
                    }
                    double candidateY = blockY + 1.0D;
                    if (canOccupy(world, x, candidateY, z, bodyHeight)) {
                        return OptionalDouble.of(candidateY);
                    }
                }
            }
        }
        return OptionalDouble.empty();
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static double wrapDegrees(double value) {
        double wrapped = value % 360.0D;
        return wrapped < 0.0D ? wrapped + 360.0D : wrapped;
    }

    private record MoveResult(boolean allowed) {
    }

    private record HorizontalMoveResult(double x, double y, double z, boolean collided) {
    }
}
