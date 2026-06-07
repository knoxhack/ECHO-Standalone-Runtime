package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.Objects;
import java.util.Optional;

public record EchoAdapterCoreRegistryEntry(
        EchoAdapterCoreContentBinding binding,
        EchoAdapterCoreDomain domain,
        String displayName,
        EchoVoxelBlock liveVoxelBlock,
        EchoAdapterCoreAssetReferences assetReferences,
        EchoAdapterCoreRegistryMetadata registryMetadata
) {
    public EchoAdapterCoreRegistryEntry {
        Objects.requireNonNull(binding, "binding");
        Objects.requireNonNull(domain, "domain");
        displayName = EchoCompatText.requireText(displayName, "displayName");
        assetReferences = assetReferences == null ? EchoAdapterCoreAssetReferences.NONE : assetReferences;
        registryMetadata = registryMetadata == null ? EchoAdapterCoreRegistryMetadata.NONE : registryMetadata;
    }

    public EchoAdapterCoreRegistryEntry(
            EchoAdapterCoreContentBinding binding,
            EchoAdapterCoreDomain domain,
            String displayName,
            EchoVoxelBlock liveVoxelBlock
    ) {
        this(binding, domain, displayName, liveVoxelBlock, EchoAdapterCoreAssetReferences.NONE,
                EchoAdapterCoreRegistryMetadata.NONE);
    }

    public EchoAdapterCoreRegistryEntry(
            EchoAdapterCoreContentBinding binding,
            EchoAdapterCoreDomain domain,
            String displayName,
            EchoVoxelBlock liveVoxelBlock,
            EchoAdapterCoreAssetReferences assetReferences
    ) {
        this(binding, domain, displayName, liveVoxelBlock, assetReferences, EchoAdapterCoreRegistryMetadata.NONE);
    }

    public String contentId() {
        return binding.contentId();
    }

    public EchoAdapterCoreContentKind contentKind() {
        return binding.contentKind();
    }

    public String idFor(EchoAdapterCoreRuntimeKind runtimeKind) {
        return binding.idFor(runtimeKind);
    }

    public String standaloneRuntimeId() {
        return binding.standaloneRuntimeId();
    }

    public String liveVoxelId() {
        return binding.liveVoxelId();
    }

    public Optional<EchoVoxelBlock> voxelBlock() {
        return Optional.ofNullable(liveVoxelBlock);
    }

    public EchoVoxelBlock requireVoxelBlock() {
        if (liveVoxelBlock == null) {
            throw new IllegalStateException(contentId() + " does not expose a live voxel block");
        }
        return liveVoxelBlock;
    }
}
