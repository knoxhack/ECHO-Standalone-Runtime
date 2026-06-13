package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.world.EchoVoxelBlockState;

record EchoClientFluidBucketUse(
        boolean used,
        String action,
        String label,
        EchoVoxelBlockState state,
        int x,
        int y,
        int z,
        String reason
) {
    EchoClientFluidBucketUse {
        action = action == null ? "" : action.trim();
        label = label == null || label.isBlank() ? "Bucket" : label.trim();
        state = state == null ? EchoVoxelBlockState.AIR : state;
        reason = reason == null || reason.isBlank() ? "unknown" : reason.trim();
    }

    static EchoClientFluidBucketUse none(String reason) {
        return new EchoClientFluidBucketUse(false, "", "Bucket", EchoVoxelBlockState.AIR, 0, 0, 0, reason);
    }

    static EchoClientFluidBucketUse collected(String label, int x, int y, int z, EchoVoxelBlockState state) {
        return new EchoClientFluidBucketUse(true, "collect", label, state, x, y, z, "fluid_collected");
    }

    static EchoClientFluidBucketUse placed(String label, int x, int y, int z, EchoVoxelBlockState state) {
        return new EchoClientFluidBucketUse(true, "place", label, state, x, y, z, "fluid_placed");
    }
}
