package dev.echo.standalone.runtime.packos;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record EchoRuntimePackMountPlan(
        String packId,
        List<EchoRuntimePackMount> mounts,
        String theme
) {
    public EchoRuntimePackMountPlan {
        packId = requireText(packId, "packId");
        Objects.requireNonNull(mounts, "mounts");
        theme = requireText(theme, "theme");
        mounts = mounts.stream()
                .sorted(Comparator.comparingInt(EchoRuntimePackMount::order))
                .toList();
    }

    public static EchoRuntimePackMountPlan from(EchoRuntimePackProfile profile) {
        java.util.ArrayList<EchoRuntimePackMount> mounts = new java.util.ArrayList<>();
        int order = 0;
        mounts.add(new EchoRuntimePackMount(order++, "asset", "runtime/defaults", "runtime-defaults"));
        for (String assetPack : profile.assetPacks()) {
            mounts.add(new EchoRuntimePackMount(order++, "asset", assetPack, "pack-profile"));
        }
        for (String dataPack : profile.dataPacks()) {
            mounts.add(new EchoRuntimePackMount(order++, "data", dataPack, "pack-profile"));
        }
        mounts.add(new EchoRuntimePackMount(order, "theme", profile.theme(), "pack-profile"));
        return new EchoRuntimePackMountPlan(profile.packId(), mounts, profile.theme());
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
