package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoCompatRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityAiTickResult;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayHazardResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayWeatherResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkRuntimeResult;
import dev.echo.standalone.runtime.network.EchoNetworkSyncResult;
import dev.echo.standalone.runtime.render.EchoRenderRuntimeResult;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;
import java.util.Objects;

public record EchoAshfallVerticalSliceResult(
        EchoWorldRuntimeResult world,
        EchoEntityRuntimeResult entities,
        EchoItemRuntimeResult items,
        EchoGameplayRuntimeResult gameplay,
        EchoScriptingRuntimeResult scripting,
        EchoCompatRuntimeResult compatibility,
        EchoGameplayWeatherResult weather,
        EchoGameplayHazardResult hazard,
        List<EchoGameplayInteractionResult> interactions,
        EchoEntityMovementResult cacheMovement,
        EchoEntityAiTickResult hostileAi,
        EchoUiRuntimeResult ui,
        EchoRenderRuntimeResult render,
        EchoAudioRuntimeResult audio,
        EchoNetworkRuntimeResult network,
        EchoNetworkSyncResult entitySync,
        EchoNetworkSyncResult inventorySync,
        EchoAshfallVerticalSliceSaveRoundTrip saveRoundTrip,
        EchoAshfallVerticalSliceSummary summary,
        boolean rendererClosed,
        boolean audioClosed,
        boolean cleanExit
) {
    public EchoAshfallVerticalSliceResult {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");
        Objects.requireNonNull(scripting, "scripting");
        Objects.requireNonNull(compatibility, "compatibility");
        Objects.requireNonNull(weather, "weather");
        Objects.requireNonNull(hazard, "hazard");
        Objects.requireNonNull(interactions, "interactions");
        interactions = List.copyOf(interactions);
        Objects.requireNonNull(cacheMovement, "cacheMovement");
        Objects.requireNonNull(hostileAi, "hostileAi");
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(render, "render");
        Objects.requireNonNull(audio, "audio");
        Objects.requireNonNull(network, "network");
        Objects.requireNonNull(entitySync, "entitySync");
        Objects.requireNonNull(inventorySync, "inventorySync");
        Objects.requireNonNull(saveRoundTrip, "saveRoundTrip");
        Objects.requireNonNull(summary, "summary");
    }
}
