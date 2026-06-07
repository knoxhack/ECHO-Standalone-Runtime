package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreParityMatrixEntry(
        EchoAdapterCoreDomain domain,
        String neoForgeFeature,
        String adapterBinding,
        String standaloneBehavior
) {
    public EchoAdapterCoreParityMatrixEntry {
        Objects.requireNonNull(domain, "domain");
        neoForgeFeature = EchoCompatText.requireText(neoForgeFeature, "neoForgeFeature");
        adapterBinding = EchoCompatText.requireText(adapterBinding, "adapterBinding");
        standaloneBehavior = EchoCompatText.requireText(standaloneBehavior, "standaloneBehavior");
    }
}
