package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayMissionStatus;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputAction;
import dev.echo.standalone.runtime.input.EchoInputControl;
import dev.echo.standalone.runtime.input.EchoInputEvent;
import dev.echo.standalone.runtime.input.EchoInputRuntime;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.player.EchoPlayerCameraRig;
import dev.echo.standalone.runtime.player.EchoPlayerController;
import dev.echo.standalone.runtime.player.EchoPlayerControllerResult;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntime;
import dev.echo.standalone.runtime.player.EchoPlayerControllerRuntimeResult;
import dev.echo.standalone.runtime.player.EchoPlayerControllerState;
import dev.echo.standalone.runtime.player.EchoPlayerFacing;
import dev.echo.standalone.runtime.player.EchoPlayerInteractionTargeter;
import dev.echo.standalone.runtime.player.EchoPlayerInventoryShortcuts;
import dev.echo.standalone.runtime.render.EchoRenderCamera;
import dev.echo.standalone.runtime.ui.EchoTerminalScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimePlayerControllerSmokeHarness {
    private static final EchoEntityId PLAYER_ID = new EchoEntityId("player-001");
    private static final EchoInventoryId PLAYER_PACK = new EchoInventoryId("inventory:player-001");
    private static final EchoItemId WATER_RATION = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);

    private EchoRuntimePlayerControllerSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoTerminalShell shell = new EchoTerminalShell();
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                new EchoTerminalScreen("terminal", "Ashfall Terminal", shell),
                EchoUiTheme.defaultTerminal()
        );
        EchoInputRuntimeResult input = new EchoInputRuntime().boot(services, ui, entities, gameplay, PLAYER_ID);
        EchoPlayerControllerRuntimeResult playerRuntime = new EchoPlayerControllerRuntime().boot(
                services,
                world,
                entities,
                gameplay,
                items,
                input,
                PLAYER_ID
        );
        EchoPlayerController controller = playerRuntime.controller();

        require(services.require(EchoPlayerControllerRuntimeResult.class) == playerRuntime,
                "player controller runtime result should be service-bound");
        require(services.require(EchoPlayerController.class) == controller,
                "player controller should be service-bound");
        require(services.require(EchoPlayerCameraRig.class) == playerRuntime.cameraRig(),
                "camera rig should be service-bound");
        require(services.require(EchoPlayerInteractionTargeter.class) == playerRuntime.targeter(),
                "targeter should be service-bound");
        require(services.require(EchoPlayerInventoryShortcuts.class) == playerRuntime.shortcuts(),
                "inventory shortcuts should be service-bound");

        EchoPlayerControllerState initial = controller.state();
        require(initial.player().worldPosition().equals(new EchoWorldPosition(0, 0, 0)),
                "player should start at the Ashfall drop pod");
        require(initial.facing() == EchoPlayerFacing.EAST, "player should start facing east");
        requireCamera(initial.camera(), 0.5D, 3.0D, 0.5D, "initial camera");
        require(initial.target().orElseThrow().id().equals("echoashfallprotocol:poi/drop_pod"),
                "initial target should be the drop pod");
        require(initial.target().orElseThrow().exact(), "initial target should be exact");

        EchoPlayerControllerResult terminal = controller.handle(mouse(0, "PRIMARY"));
        require(terminal.handled(), "terminal interaction should be handled");
        require(terminal.action().orElseThrow() == EchoInputAction.POINTER_PRIMARY,
                "terminal input should resolve to interact");
        require(terminal.interaction().orElseThrow().interactionId().equals("ashfall:activate_terminal"),
                "controller should activate the exact terminal target");
        require(terminal.interaction().orElseThrow().objectiveCompleted(),
                "terminal interaction should complete terminal objective");

        EchoPlayerControllerResult water = controller.handle(key(1, "DIGIT1"));
        require(water.handled(), "quick slot should be handled");
        require(water.shortcut().orElseThrow().used(), "quick slot should consume water");
        require(water.shortcut().orElseThrow().itemId().equals(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID),
                "quick slot should report clean water bottle");
        int waterRemainingAfterShortcut = items.operations().count(items.inventoryStore().require(PLAYER_PACK), WATER_RATION);
        require(waterRemainingAfterShortcut == 1,
                "quick slot should leave one water ration");

        EchoPlayerControllerResult eastOne = controller.handle(key(2, "D"));
        requireMove(eastOne, "1,0,0", "first east move");
        require(eastOne.hazard().orElseThrow().intensity() == 0.72D,
                "first move should apply toxic ash feedback");
        require(eastOne.hazard().orElseThrow().healthDamage() == 4,
                "first move should damage suit seals");
        requireCamera(eastOne.state().camera(), 1.5D, 3.0D, 0.5D, "first east camera");

        EchoPlayerControllerResult eastTwo = controller.handle(key(3, "D"));
        requireMove(eastTwo, "2,0,0", "second east move");
        require(eastTwo.state().target().orElseThrow().id().equals("ashfall:crash_cache"),
                "nearby target should become the crash cache");
        require(!eastTwo.state().target().orElseThrow().exact(),
                "crash cache should not be exact until the player reaches it");

        EchoPlayerControllerResult southOne = controller.handle(key(4, "S"));
        requireMove(southOne, "2,0,1", "south move to cache");
        requireCamera(southOne.state().camera(), 2.5D, 3.0D, 1.5D, "cache camera");
        require(southOne.state().target().orElseThrow().id().equals("ashfall:crash_cache"),
                "cache should be targeted at the cache position");
        require(southOne.state().target().orElseThrow().exact(),
                "cache target should be exact at the cache position");
        require(southOne.state().hazard().orElseThrow().message().contains("Toxic ash"),
                "state should retain the latest hazard feedback");

        EchoPlayerControllerResult cache = controller.handle(mouse(5, "PRIMARY"));
        require(cache.handled(), "cache interaction should be handled");
        require(cache.interaction().orElseThrow().interactionId().equals("ashfall:salvage_cache"),
                "controller should salvage the exact cache target");
        require(gameplay.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                "controller traversal should complete the Ashfall debug mission");
        require(gameplay.mission().completedObjectiveCount() == 3,
                "all three player-facing objectives should be complete");

        EchoPlayerControllerResult eastTraversal = controller.handle(key(6, "D"));
        requireMove(eastTraversal, "3,0,1", "east traversal");
        EchoPlayerControllerResult southTraversal = controller.handle(key(7, "S"));
        requireMove(southTraversal, "3,0,2", "south traversal");
        EchoPlayerControllerResult blocked = controller.handle(key(8, "S"));
        require(!blocked.handled(), "blocked collision should not be handled as movement");
        require(blocked.movement().orElseThrow().reason().equals("blocked_cell"),
                "blocked movement should report blocked_cell");
        require(blocked.state().player().worldPosition().key().equals("3,0,2"),
                "blocked collision should preserve player position");
        requireCamera(blocked.state().camera(), 3.5D, 3.0D, 2.5D, "blocked camera");

        EchoPlayerControllerResult focusTerminal = controller.handle(key(9, "BACKQUOTE"));
        require(focusTerminal.handled(), "controller should delegate Terminal focus");
        require(input.focus().terminalFocused(), "Terminal focus should be active");
        EchoPlayerControllerResult blockedByFocus = controller.handle(key(10, "A"));
        require(!blockedByFocus.handled(), "Terminal focus should block gameplay movement");
        require(blockedByFocus.effects().contains("terminal-focus-blocks-gameplay"),
                "focus-blocked movement should report the input focus reason");
        require(blockedByFocus.state().player().worldPosition().key().equals("3,0,2"),
                "focus-blocked movement should preserve player position");
        EchoPlayerControllerResult blurTerminal = controller.handle(key(11, "ESCAPE"));
        require(blurTerminal.handled(), "controller should delegate Terminal blur");
        require(!input.focus().terminalFocused(), "Terminal focus should be cleared");

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                initial,
                terminal,
                water,
                waterRemainingAfterShortcut,
                eastOne,
                eastTwo,
                southOne,
                cache,
                eastTraversal,
                southTraversal,
                blocked,
                focusTerminal,
                blockedByFocus,
                blurTerminal,
                gameplay,
                items
        );

        System.out.println("phase15.4 player controller smoke PASS position="
                + blocked.state().player().worldPosition().key()
                + " camera="
                + fixed(blocked.state().camera().x())
                + ","
                + fixed(blocked.state().camera().z())
                + " collision=" + blocked.movement().orElseThrow().reason()
                + " mission=" + gameplay.mission().status().name()
                + " health=" + blocked.state().player().health().currentHealth()
                + " target=" + southOne.state().target().orElseThrow().id());
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoPlayerControllerState initial,
            EchoPlayerControllerResult terminal,
            EchoPlayerControllerResult water,
            int waterRemainingAfterShortcut,
            EchoPlayerControllerResult eastOne,
            EchoPlayerControllerResult eastTwo,
            EchoPlayerControllerResult southOne,
            EchoPlayerControllerResult cache,
            EchoPlayerControllerResult eastTraversal,
            EchoPlayerControllerResult southTraversal,
            EchoPlayerControllerResult blocked,
            EchoPlayerControllerResult focusTerminal,
            EchoPlayerControllerResult blockedByFocus,
            EchoPlayerControllerResult blurTerminal,
            EchoGameplayRuntimeResult gameplay,
            EchoItemRuntimeResult items
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        int finalWaterCount = items.operations().count(items.inventoryStore().require(PLAYER_PACK), WATER_RATION);
        List<EchoPlayerControllerResult> movementRoute = List.of(eastOne, eastTwo, southOne, eastTraversal, southTraversal);

        write(root.resolve("runtime-player-controller.json"), """
                {
                  "schema": "echo.standalone.runtime_player_controller.v2",
                  "status": "PASS",
                  "phase": "15.4",
                  "summary": "Player controller booted service-bound camera, targeter, shortcuts, and controller services, then completed the deterministic Ashfall traversal route.",
                  "serviceBound": true,
                  "controllerBound": true,
                  "cameraRigBound": true,
                  "targeterBound": true,
                  "shortcutRuntimeBound": true,
                  "initialPosition": %s,
                  "initialFacing": "%s",
                  "initialTarget": %s,
                  "finalPosition": %s,
                  "finalCamera": %s,
                  "missionStatus": "%s",
                  "completedObjectives": %d,
                  "playerHealth": %d,
                  "terminalFocusCleared": %s
                }
                """.formatted(
                positionJson(initial.player().worldPosition()),
                initial.facing(),
                targetJson(initial.target().orElseThrow()),
                positionJson(blocked.state().player().worldPosition()),
                cameraJson(blocked.state().camera()),
                gameplay.mission().status(),
                gameplay.mission().completedObjectiveCount(),
                blocked.state().player().health().currentHealth(),
                true
        ));

        write(root.resolve("player-movement.json"), """
                {
                  "schema": "echo.standalone.player_movement.v2",
                  "status": "PASS",
                  "movementCount": %d,
                  "route": %s,
                  "firstEastPosition": "%s",
                  "cachePosition": "%s",
                  "finalTraversalPosition": "%s",
                  "facingAfterBlockedMove": "%s",
                  "allRouteMovesHandled": %s
                }
                """.formatted(
                movementRoute.size(),
                controllerResultsJson(movementRoute),
                eastOne.state().player().worldPosition().key(),
                southOne.state().player().worldPosition().key(),
                southTraversal.state().player().worldPosition().key(),
                blocked.state().facing(),
                movementRoute.stream().allMatch(result -> result.handled()
                        && result.movement().isPresent()
                        && result.movement().orElseThrow().moved())
        ));

        write(root.resolve("player-collision.json"), """
                {
                  "schema": "echo.standalone.player_collision.v2",
                  "status": "PASS",
                  "blockedMove": %s,
                  "blockedHandled": %s,
                  "blockedReason": "%s",
                  "positionPreserved": %s,
                  "cameraPreserved": %s,
                  "effects": %s
                }
                """.formatted(
                controllerResultJson(blocked),
                blocked.handled(),
                escape(blocked.movement().orElseThrow().reason()),
                blocked.state().player().worldPosition().key().equals("3,0,2"),
                fixed(blocked.state().camera().x()).equals("3.5")
                        && fixed(blocked.state().camera().z()).equals("2.5"),
                jsonStringArray(blocked.effects())
        ));

        write(root.resolve("player-camera.json"), """
                {
                  "schema": "echo.standalone.player_camera.v2",
                  "status": "PASS",
                  "initialCamera": %s,
                  "firstEastCamera": %s,
                  "cacheCamera": %s,
                  "blockedCamera": %s,
                  "cameraFollowsPlayer": %s,
                  "stableAfterCollision": %s
                }
                """.formatted(
                cameraJson(initial.camera()),
                cameraJson(eastOne.state().camera()),
                cameraJson(southOne.state().camera()),
                cameraJson(blocked.state().camera()),
                fixed(eastOne.state().camera().x()).equals("1.5")
                        && fixed(southOne.state().camera().z()).equals("1.5"),
                fixed(blocked.state().camera().x()).equals("3.5")
                        && fixed(blocked.state().camera().z()).equals("2.5")
        ));

        write(root.resolve("player-targeting.json"), """
                {
                  "schema": "echo.standalone.player_targeting.v2",
                  "status": "PASS",
                  "initialTarget": %s,
                  "nearbyCacheTarget": %s,
                  "exactCacheTarget": %s,
                  "terminalInteractionTarget": %s,
                  "cacheInteractionTarget": %s,
                  "targetingStable": %s
                }
                """.formatted(
                targetJson(initial.target().orElseThrow()),
                targetJson(eastTwo.state().target().orElseThrow()),
                targetJson(southOne.state().target().orElseThrow()),
                targetJson(terminal.target().orElseThrow()),
                targetJson(cache.target().orElseThrow()),
                initial.target().orElseThrow().id().equals("echoashfallprotocol:poi/drop_pod")
                        && eastTwo.state().target().orElseThrow().id().equals("ashfall:crash_cache")
                        && !eastTwo.state().target().orElseThrow().exact()
                        && southOne.state().target().orElseThrow().exact()
        ));

        write(root.resolve("player-inventory-shortcuts.json"), """
                {
                  "schema": "echo.standalone.player_inventory_shortcuts.v2",
                  "status": "PASS",
                  "shortcut": %s,
                  "waterRemainingAfterShortcut": %d,
                  "waterRemainingAfterCacheLoot": %d,
                  "quickSlotConsumedWater": %s,
                  "effects": %s
                }
                """.formatted(
                shortcutJson(water.shortcut().orElseThrow()),
                waterRemainingAfterShortcut,
                finalWaterCount,
                water.shortcut().orElseThrow().used()
                        && water.shortcut().orElseThrow().itemId().equals(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID)
                        && waterRemainingAfterShortcut == 1,
                jsonStringArray(water.effects())
        ));

        write(root.resolve("player-hazard-feedback.json"), """
                {
                  "schema": "echo.standalone.player_hazard_feedback.v2",
                  "status": "PASS",
                  "firstMoveHazard": %s,
                  "cacheMoveHazard": %s,
                  "retainedHazard": %s,
                  "finalPlayerHealth": %d,
                  "hazardFeedbackApplied": %s
                }
                """.formatted(
                hazardJson(eastOne.hazard().orElseThrow()),
                hazardJson(southOne.hazard().orElseThrow()),
                hazardJson(southOne.state().hazard().orElseThrow()),
                blocked.state().player().health().currentHealth(),
                eastOne.hazard().orElseThrow().intensity() == 0.72D
                        && eastOne.hazard().orElseThrow().healthDamage() == 4
                        && southOne.state().hazard().orElseThrow().message().contains("Toxic ash")
        ));

        write(root.resolve("player-debug-traversal.json"), """
                {
                  "schema": "echo.standalone.player_debug_traversal.v2",
                  "status": "PASS",
                  "terminalInteraction": %s,
                  "cacheInteraction": %s,
                  "focusTerminal": %s,
                  "blockedByFocus": %s,
                  "blurTerminal": %s,
                  "missionCompleted": %s,
                  "completedObjectives": %d,
                  "focusBlockedGameplay": %s,
                  "routeEffects": %s
                }
                """.formatted(
                interactionResultJson(terminal),
                interactionResultJson(cache),
                controllerResultJson(focusTerminal),
                controllerResultJson(blockedByFocus),
                controllerResultJson(blurTerminal),
                gameplay.mission().status() == EchoGameplayMissionStatus.COMPLETED,
                gameplay.mission().completedObjectiveCount(),
                blockedByFocus.effects().contains("terminal-focus-blocks-gameplay")
                        && !blockedByFocus.handled(),
                jsonStringArray(List.of(
                        "terminal:" + terminal.interaction().orElseThrow().reason(),
                        "shortcut:" + water.shortcut().orElseThrow().reason(),
                        "cache:" + cache.interaction().orElseThrow().reason(),
                        "collision:" + blocked.movement().orElseThrow().reason(),
                        "focus:" + blockedByFocus.effects().get(0)
                ))
        ));
    }

    private static String controllerResultsJson(List<EchoPlayerControllerResult> results) {
        return results.stream()
                .map(EchoRuntimePlayerControllerSmokeHarness::controllerResultJson)
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String controllerResultJson(EchoPlayerControllerResult result) {
        return """
                {
                  "handled": %s,
                  "action": %s,
                  "position": %s,
                  "facing": "%s",
                  "movement": %s,
                  "interaction": %s,
                  "shortcut": %s,
                  "hazard": %s,
                  "target": %s,
                  "effects": %s
                }""".formatted(
                result.handled(),
                result.action().map(action -> "\"" + action.name() + "\"").orElse("null"),
                positionJson(result.state().player().worldPosition()),
                result.state().facing(),
                result.movement().map(EchoRuntimePlayerControllerSmokeHarness::movementJson).orElse("null"),
                result.interaction().map(EchoRuntimePlayerControllerSmokeHarness::gameplayInteractionJson).orElse("null"),
                result.shortcut().map(EchoRuntimePlayerControllerSmokeHarness::shortcutJson).orElse("null"),
                result.hazard().map(EchoRuntimePlayerControllerSmokeHarness::hazardJson).orElse("null"),
                result.target().map(EchoRuntimePlayerControllerSmokeHarness::targetJson).orElse("null"),
                jsonStringArray(result.effects())
        ).strip();
    }

    private static String interactionResultJson(EchoPlayerControllerResult result) {
        return """
                {
                  "handled": %s,
                  "action": %s,
                  "target": %s,
                  "interaction": %s,
                  "effects": %s
                }""".formatted(
                result.handled(),
                result.action().map(action -> "\"" + action.name() + "\"").orElse("null"),
                result.target().map(EchoRuntimePlayerControllerSmokeHarness::targetJson).orElse("null"),
                result.interaction().map(EchoRuntimePlayerControllerSmokeHarness::gameplayInteractionJson).orElse("null"),
                jsonStringArray(result.effects())
        ).strip();
    }

    private static String movementJson(dev.echo.standalone.runtime.entity.EchoEntityMovementResult movement) {
        return """
                {
                  "entityId": "%s",
                  "from": %s,
                  "to": %s,
                  "moved": %s,
                  "reason": "%s"
                }""".formatted(
                escape(movement.entityId().value()),
                positionJson(movement.from()),
                positionJson(movement.to()),
                movement.moved(),
                escape(movement.reason())
        ).strip();
    }

    private static String gameplayInteractionJson(dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult interaction) {
        return """
                {
                  "interactionId": "%s",
                  "success": %s,
                  "objectiveCompleted": %s,
                  "experienceAwarded": %d,
                  "reason": "%s"
                }""".formatted(
                escape(interaction.interactionId()),
                interaction.success(),
                interaction.objectiveCompleted(),
                interaction.experienceAwarded(),
                escape(interaction.reason())
        ).strip();
    }

    private static String shortcutJson(dev.echo.standalone.runtime.player.EchoPlayerInventoryShortcutResult shortcut) {
        return """
                {
                  "slotIndex": %d,
                  "itemId": "%s",
                  "used": %s,
                  "reason": "%s"
                }""".formatted(
                shortcut.slotIndex(),
                escape(shortcut.itemId()),
                shortcut.used(),
                escape(shortcut.reason())
        ).strip();
    }

    private static String hazardJson(dev.echo.standalone.runtime.player.EchoPlayerHazardFeedback hazard) {
        return """
                {
                  "intensity": %s,
                  "exposureDelta": %s,
                  "healthDamage": %d,
                  "message": "%s"
                }""".formatted(
                Double.toString(hazard.intensity()),
                Double.toString(hazard.exposureDelta()),
                hazard.healthDamage(),
                escape(hazard.message())
        ).strip();
    }

    private static String targetJson(dev.echo.standalone.runtime.player.EchoPlayerInteractionTarget target) {
        return """
                {
                  "id": "%s",
                  "type": "%s",
                  "label": "%s",
                  "position": %s,
                  "distance": %d,
                  "exact": %s,
                  "facing": %s
                }""".formatted(
                escape(target.id()),
                escape(target.type()),
                escape(target.label()),
                positionJson(target.position()),
                target.distance(),
                target.exact(),
                target.facing()
        ).strip();
    }

    private static String cameraJson(EchoRenderCamera camera) {
        return """
                {
                  "cameraId": "%s",
                  "x": %s,
                  "y": %s,
                  "z": %s,
                  "zoom": %s,
                  "pitchDegrees": %s
                }""".formatted(
                escape(camera.cameraId()),
                Double.toString(camera.x()),
                Double.toString(camera.y()),
                Double.toString(camera.z()),
                Double.toString(camera.zoom()),
                Double.toString(camera.pitchDegrees())
        ).strip();
    }

    private static String positionJson(EchoWorldPosition position) {
        return """
                {"x": %d, "y": %d, "z": %d, "key": "%s"}""".formatted(
                position.x(),
                position.y(),
                position.z(),
                escape(position.key())
        ).trim();
    }

    private static String jsonStringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static EchoInputEvent key(long sequence, String key) {
        return EchoInputEvent.press(sequence, EchoInputControl.keyboard(key));
    }

    private static EchoInputEvent mouse(long sequence, String button) {
        return EchoInputEvent.press(sequence, EchoInputControl.mouse(button));
    }

    private static void requireMove(EchoPlayerControllerResult result, String expectedPosition, String label) {
        require(result.handled(), label + " should be handled");
        require(result.movement().orElseThrow().moved(), label + " should move");
        require(result.state().player().worldPosition().key().equals(expectedPosition),
                label + " should end at " + expectedPosition);
    }

    private static void requireCamera(EchoRenderCamera camera, double x, double y, double z, String label) {
        requireClose(camera.x(), x, label + " x");
        requireClose(camera.y(), y, label + " y");
        requireClose(camera.z(), z, label + " z");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireClose(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 0.001D) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }

    private static String fixed(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }
}
