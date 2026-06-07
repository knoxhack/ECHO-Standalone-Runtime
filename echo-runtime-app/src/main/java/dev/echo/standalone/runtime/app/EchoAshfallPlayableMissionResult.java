package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.audio.EchoAudioRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntimeResult;
import dev.echo.standalone.runtime.render.EchoRenderRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;
import java.util.Objects;

public record EchoAshfallPlayableMissionResult(
        EchoWorldRuntimeResult world,
        EchoEntityRuntimeResult entities,
        EchoItemRuntimeResult items,
        EchoGameplayRuntimeResult gameplay,
        EchoUiRuntimeResult ui,
        EchoInputRuntimeResult input,
        EchoPlayerControllerRuntimeResult player,
        EchoRenderRuntimeResult render,
        EchoAudioRuntimeResult audio,
        EchoTerminalShell terminalShell,
        List<EchoAshfallPlayableMissionStep> steps,
        List<EchoAshfallPlayableMissionObjective> objectives,
        EchoAshfallScavengerEncounterResult encounter,
        EchoItemCraftResult rewardCraft,
        EchoAshfallPlayableMissionReward reward,
        EchoAshfallFailRetryResult failRetry,
        EchoAshfallPlayableMissionSummary summary
) {
    public EchoAshfallPlayableMissionResult {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(render, "render");
        Objects.requireNonNull(audio, "audio");
        Objects.requireNonNull(terminalShell, "terminalShell");
        Objects.requireNonNull(steps, "steps");
        steps = List.copyOf(steps);
        Objects.requireNonNull(objectives, "objectives");
        objectives = List.copyOf(objectives);
        Objects.requireNonNull(encounter, "encounter");
        Objects.requireNonNull(rewardCraft, "rewardCraft");
        Objects.requireNonNull(reward, "reward");
        Objects.requireNonNull(failRetry, "failRetry");
        Objects.requireNonNull(summary, "summary");
    }
}
