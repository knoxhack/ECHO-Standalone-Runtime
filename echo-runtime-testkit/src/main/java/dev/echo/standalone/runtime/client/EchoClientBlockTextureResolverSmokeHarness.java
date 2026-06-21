package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelFaceUv;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelBounds;
import dev.echo.standalone.runtime.assets.EchoItemTextureResolver;
import dev.echo.standalone.runtime.assets.EchoMissingTexture;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.render.EchoVoxelMeshDirection;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public final class EchoClientBlockTextureResolverSmokeHarness {
    private EchoClientBlockTextureResolverSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of("build/tmp/client-block-texture-resolver-smoke").toAbsolutePath().normalize();
        deleteRecursively(root);
        write(root.resolve("assets/facetest/blockstates/copper_pillar.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/copper_pillar" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/copper_pillar.json"), """
                {
                  "parent": "minecraft:block/cube_column",
                  "textures": {
                    "side": "facetest:block/copper_pillar_side",
                    "end": "facetest:block/copper_pillar_end",
                    "particle": "#side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/missing_panel.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/missing_panel" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/animated_panel_block.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/animated_panel_block" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/missing_panel.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/does_not_exist"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/animated_panel_block.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/animated_panel"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/weighted_panel.json"), """
                {
                  "variants": {
                    "": [
                      { "model": "facetest:block/weighted_panel_alt", "weight": 1 },
                      { "model": "facetest:block/weighted_panel", "weight": 3 }
                    ]
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/stateful_panel.json"), """
                {
                  "variants": {
                    "powered=false": { "model": "facetest:block/stateful_panel_off" },
                    "powered=true": { "model": "facetest:block/stateful_panel_on" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/stateful_panel_off.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/stateful_panel_off"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/stateful_panel_on.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/stateful_panel_on"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/uv_unlocked_y90.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/uv_marker", "y": 90 }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/uv_locked_y90.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/uv_marker", "y": 90, "uvlock": true }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/uv_unlocked_x90.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/uv_marker", "x": 90 }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/uv_locked_x90.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/uv_marker", "x": 90, "uvlock": true }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/uv_marker.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/uv_marker"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/weighted_panel_alt.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/weighted_panel_alt"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/pipe_post.json"), """
                {
                  "multipart": [
                    {
                      "when": { "north": "true" },
                      "apply": { "model": "facetest:block/pipe_side" }
                    },
                    {
                      "apply": [
                        { "model": "facetest:block/pipe_center" }
                      ]
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/block/pipe_side.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/pipe_side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/pipe_center.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/pipe_center"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/trimmed_post.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/trimmed_post" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/trimmed_post.json"), """
                {
                  "textures": {
                    "all": "facetest:block/trimmed_post",
                    "cap": "facetest:block/trimmed_post_cap",
                    "trim": "facetest:block/trimmed_post_trim"
                  },
                  "elements": [
                    {
                      "from": [2, 0, 4],
                      "to": [14, 8, 12],
                      "faces": {
                        "up": { "texture": "#cap", "uv": [4, 4, 12, 12], "rotation": 90, "tintindex": 0 },
                        "down": { "texture": "#cap" },
                        "north": { "texture": "#trim" },
                        "south": { "texture": "#trim" },
                        "east": { "texture": "#all" },
                        "west": { "texture": "#all" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/item/scrap_knife.json"), """
                {
                  "parent": "minecraft:item/handheld",
                  "textures": {
                    "layer0": "facetest:item/scrap_knife"
                  }
                }
                """);
        write(root.resolve("assets/facetest/textures/item/scrap_knife.png"), "fake-knife-texture");
        write(root.resolve("assets/facetest/models/item/clean_water_bottle.json"), """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "facetest:item/clean_water_bottle"
                  }
                }
                """);
        write(root.resolve("assets/facetest/textures/item/clean_water_bottle.png"), "fake-water-texture");
        write(root.resolve("assets/facetest/models/item/layered_badge.json"), """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "facetest:item/layered_badge_base",
                    "layer1": "facetest:item/layered_badge_overlay"
                  }
                }
                """);
        write(root.resolve("assets/facetest/textures/item/layered_badge_base.png"), "fake-badge-base-texture");
        write(root.resolve("assets/facetest/textures/item/layered_badge_overlay.png"), "fake-badge-overlay-texture");
        write(root.resolve("assets/facetest/models/item/override_tool.json"), """
                {
                  "parent": "minecraft:item/handheld",
                  "textures": {
                    "layer0": "facetest:item/override_tool"
                  },
                  "overrides": [
                    {
                      "predicate": { "damage": 0.25 },
                      "model": "facetest:item/override_tool_chipped"
                    },
                    {
                      "predicate": { "damage": 0.75 },
                      "model": "facetest:item/override_tool_broken"
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/item/override_tool_chipped.json"), """
                {
                  "parent": "minecraft:item/handheld",
                  "textures": {
                    "layer0": "facetest:item/override_tool_chipped"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/item/override_tool_broken.json"), """
                {
                  "parent": "minecraft:item/handheld",
                  "textures": {
                    "layer0": "facetest:item/override_tool_broken"
                  }
                }
                """);
        write(root.resolve("assets/facetest/textures/item/override_tool.png"), "fake-override-tool-texture");
        write(root.resolve("assets/facetest/textures/item/override_tool_chipped.png"), "fake-override-tool-chipped-texture");
        write(root.resolve("assets/facetest/textures/item/override_tool_broken.png"), "fake-override-tool-broken-texture");
        write(root.resolve("assets/facetest/models/item/missing_icon_tool.json"), """
                {
                  "parent": "minecraft:item/generated",
                  "textures": {
                    "layer0": "facetest:item/missing_icon_tool"
                  }
                }
                """);
        write(root.resolve("assets/facetest/textures/block/copper_pillar_side.png"), "fake-side-texture");
        write(root.resolve("assets/facetest/textures/block/copper_pillar_end.png"), "fake-end-texture");
        write(root.resolve("assets/facetest/textures/block/uv_marker.png"), "fake-uv-marker-texture");
        writeAnimatedStrip(
                root.resolve("assets/facetest/textures/block/animated_panel.png"),
                16,
                0xFFFF0000,
                0xFF0000FF
        );
        write(root.resolve("assets/facetest/textures/block/animated_panel.png.mcmeta"), """
                {
                  "animation": {
                    "frametime": 2,
                    "frames": [1, 0]
                  }
                }
                """);

        EchoMinecraftAssetResolver minecraft = new EchoMinecraftAssetResolver(
                new EchoAssetRuntime(List.of(new EchoAssetMount(0, "smoke", root, "facetest")))
                        .load(new EchoDefaultRuntimeServiceRegistry(), List.of())
                        .resolver()
        );
        EchoBlockTextureResolver resolver = new EchoBlockTextureResolver(minecraft);
        EchoBlockTextureResolver.EchoBlockTextureResolution resolution = resolver.resolve("facetest:copper_pillar");
        EchoItemTextureResolver itemResolver = new EchoItemTextureResolver(minecraft);

        require(resolution.resolved(), "cube_column model should resolve a default texture");
        require(resolution.templateKind().orElse("").equals("cube_column"),
                "cube_column parent should be detected through the model chain");
        require(resolution.textureIdForFace("up").orElse("").equals("facetest:block/copper_pillar_end"),
                "cube_column up face should use the end texture");
        require(resolution.textureIdForFace("down").orElse("").equals("facetest:block/copper_pillar_end"),
                "cube_column down face should use the end texture");
        require(resolution.textureIdForFace("north").orElse("").equals("facetest:block/copper_pillar_side"),
                "cube_column north face should use the side texture");
        require(resolution.textureIdForFace("east").orElse("").equals("facetest:block/copper_pillar_side"),
                "cube_column east face should use the side texture");
        require(resolution.textureIdForFace("south").orElse("").equals("facetest:block/copper_pillar_side"),
                "cube_column south face should use the side texture");
        require(resolution.textureIdForFace("west").orElse("").equals("facetest:block/copper_pillar_side"),
                "cube_column west face should use the side texture");

        byte[] directMissing = EchoMissingTexture.rgbaChecker(16);
        require(directMissing.length == 16 * 16 * 4,
                "Missing texture helper should emit RGBA pixels");
        require(unsigned(directMissing[0]) == 255
                        && unsigned(directMissing[1]) == 0
                        && unsigned(directMissing[2]) == 255
                        && unsigned(directMissing[3]) == 255,
                "Missing texture helper should start with an opaque magenta checker");

        EchoBlockTextureResolver.EchoBlockTextureResolution missingResolution = resolver.resolve("facetest:missing_panel");
        require(missingResolution.resolved(),
                "Model with a missing PNG should still resolve the declared texture id");
        require(minecraft.texture("facetest", "block/does_not_exist").isEmpty(),
                "Smoke fixture should intentionally omit the referenced model texture PNG");

        EchoBlockTextureResolver.EchoBlockTextureResolution weightedResolution = resolver.resolve("facetest:weighted_panel");
        require(weightedResolution.resolved(),
                "Weighted blockstate variant arrays should resolve their first model entry");
        require(weightedResolution.modelId().orElse("").equals("facetest:block/weighted_panel_alt"),
                "Weighted variant arrays should preserve the first declared model id");
        require(weightedResolution.textureId().orElse("").equals("facetest:block/weighted_panel_alt"),
                "Weighted variant arrays should resolve the selected model texture");

        EchoBlockTextureResolver.EchoBlockTextureResolution poweredOffResolution =
                resolver.resolve("facetest:stateful_panel", Map.of("powered", "false"));
        EchoBlockTextureResolver.EchoBlockTextureResolution poweredOnResolution =
                resolver.resolve("facetest:stateful_panel", Map.of("powered", "true"));
        require(poweredOffResolution.modelId().orElse("").equals("facetest:block/stateful_panel_off"),
                "Blockstate variants should select the model matching powered=false");
        require(poweredOnResolution.modelId().orElse("").equals("facetest:block/stateful_panel_on"),
                "Blockstate variants should select the model matching powered=true");
        require(poweredOnResolution.textureId().orElse("").equals("facetest:block/stateful_panel_on"),
                "State-aware blockstate resolution should drive active-state texture selection");
        EchoBlockTextureResolver.EchoBlockTextureResolution uvUnlockedYResolution =
                resolver.resolve("facetest:uv_unlocked_y90");
        EchoBlockTextureResolver.EchoBlockTextureResolution uvLockedYResolution =
                resolver.resolve("facetest:uv_locked_y90");
        require(uvUnlockedYResolution.yRotationDegrees() == 90 && !uvUnlockedYResolution.uvLock(),
                "Blockstate variants should preserve unlocked Y rotation metadata");
        require(uvLockedYResolution.yRotationDegrees() == 90 && uvLockedYResolution.uvLock(),
                "Blockstate variants should preserve locked Y rotation metadata");

        EchoBlockTextureResolver.EchoBlockTextureResolution multipartResolution = resolver.resolve("facetest:pipe_post");
        require(multipartResolution.resolved(),
                "Multipart blockstates should resolve matching apply models");
        require(multipartResolution.templateKind().orElse("").equals("multipart"),
                "Default multipart blockstates should resolve to a composite multipart plan");
        require(multipartResolution.modelId().orElse("").equals("facetest:block/pipe_side"),
                "Multipart blockstates should preserve the first apply model id for diagnostics");
        require(multipartResolution.modelElementDefinitions().size() == 2,
                "Default multipart blockstates should aggregate every apply model when no concrete state is supplied");
        require(multipartResolution.textureIdForFace("north").orElse("").equals("facetest:block/pipe_side"),
                "Multipart apply model texture should drive primary face texture resolution");
        require(multipartResolution.modelElementDefinitions().stream()
                        .anyMatch(element -> element.textureIdForFace("north")
                                .orElse("").equals("facetest:block/pipe_center")),
                "Default multipart blockstates should retain the unconditional apply texture as a composed element");
        EchoBlockTextureResolver.EchoBlockTextureResolution multipartFalseResolution =
                resolver.resolve("facetest:pipe_post", Map.of("north", "false"));
        require(multipartFalseResolution.modelId().orElse("").equals("facetest:block/pipe_center"),
                "Multipart blockstates should skip unmatched when clauses for concrete block states");
        require(multipartFalseResolution.textureId().orElse("").equals("facetest:block/pipe_center"),
                "Multipart fallback apply should drive texture selection when no when clause matches");

        EchoBlockTextureResolver.EchoBlockTextureResolution trimmedResolution = resolver.resolve("facetest:trimmed_post");
        require(trimmedResolution.resolved(),
                "Block model with JSON elements should resolve a texture");
        EchoBlockModelBounds bounds = trimmedResolution.modelBounds().orElseThrow();
        require(bounds.fromX() == 2.0D && bounds.fromY() == 0.0D && bounds.fromZ() == 4.0D
                        && bounds.toX() == 14.0D && bounds.toY() == 8.0D && bounds.toZ() == 12.0D,
                "Block model elements should expose renderer bounds from JSON");
        require(!bounds.fullCubeBounds(),
                "Trimmed model element should not be treated as a full cube");
        require(trimmedResolution.textureIdForFace("up").orElse("").equals("facetest:block/trimmed_post_cap"),
                "Element face texture declarations should drive custom model top faces");
        require(trimmedResolution.textureIdForFace("north").orElse("").equals("facetest:block/trimmed_post_trim"),
                "Element face texture declarations should drive custom model side faces");
        require(trimmedResolution.textureIdForFace("east").orElse("").equals("facetest:block/trimmed_post"),
                "Element face texture fallback should preserve per-face all texture declarations");
        require(trimmedResolution.uvRotationDegreesForFace("up") == 90,
                "Element face UV rotation declarations should be preserved for custom model top faces");
        EchoBlockModelFaceUv trimmedTopUv = trimmedResolution.uvRectForFace("up").orElseThrow();
        require(trimmedTopUv.u1() == 4.0D && trimmedTopUv.v1() == 4.0D
                        && trimmedTopUv.u2() == 12.0D && trimmedTopUv.v2() == 12.0D,
                "Element face UV rectangle declarations should be preserved for custom model top faces");
        require(trimmedResolution.tintIndexForFace("up").orElse(-1) == 0,
                "Element face tintindex declarations should be preserved for custom model top faces");
        require(trimmedResolution.tintIndexForFace("north").isEmpty(),
                "Element face tintindex declarations should not leak to untinted custom model faces");
        require(resolution.modelBoundsOrFullCube().fullCubeBounds(),
                "Parent-template cube models should retain full-cube renderer bounds");

        EchoClientTextureAtlas atlas = new EchoClientTextureAtlas();
        atlas.setMinecraftAssets(minecraft);
        ByteBuffer animatedTile = atlas.loadOrGenerateTile(
                "minecraft-texture/facetest/block/animated_panel",
                "",
                0xFFFFFFFF,
                "facetest:block/animated_panel"
        );
        require(pixel(animatedTile, 0, 0, 0) == 0
                        && pixel(animatedTile, 0, 0, 1) == 0
                        && pixel(animatedTile, 0, 0, 2) == 255
                        && pixel(animatedTile, 0, 0, 3) == 255,
                "Animated texture frame strips should load the first declared frame instead of the whole strip");
        require(atlas.resourcePackTileDecodeCount() == 1 && atlas.cachedResourcePackTileCount() == 1,
                "Resource-pack texture decode should populate the atlas tile cache");
        ByteBuffer animatedTileCached = atlas.loadOrGenerateTile(
                "minecraft-texture/facetest/block/animated_panel",
                "",
                0xFFFFFFFF,
                "facetest:block/animated_panel"
        );
        require(pixel(animatedTileCached, 0, 0, 2) == 255,
                "Cached animated texture tile should preserve decoded pixel data");
        require(atlas.resourcePackTileDecodeCount() == 1 && atlas.resourcePackTileCacheHitCount() == 1,
                "Repeated resource-pack texture ids should reuse the decoded atlas tile");
        int modelCacheHits = atlas.resourcePackTileCacheHitCount();
        atlas.loadOrGenerateTile("facetest/animated_panel_block", "facetest:animated_panel_block", 0xFFFFFFFF, "");
        int modelDecodeCount = atlas.resourcePackTileDecodeCount();
        int modelTileHits = atlas.resourcePackTileCacheHitCount();
        int modelResolutionHits = atlas.blockTextureResolutionCacheHitCount();
        require(modelDecodeCount == 1 && modelTileHits > modelCacheHits,
                "Model-resolved block textures should reuse already-decoded resource-pack tiles");
        atlas.loadOrGenerateTile("facetest/animated_panel_block", "facetest:animated_panel_block", 0xFFFFFFFF, "");
        require(atlas.resourcePackTileDecodeCount() == modelDecodeCount,
                "Repeated model-resolved block textures should not decode the resource-pack PNG again");
        require(atlas.resourcePackTileCacheHitCount() > modelTileHits,
                "Repeated model-resolved block textures should hit the decoded tile cache");
        require(atlas.blockTextureResolutionCacheHitCount() > modelResolutionHits
                        && atlas.cachedBlockTextureResolutionCount() > 0,
                "Repeated model-resolved block textures should reuse the block texture resolution cache");
        requireUvLockPlans(atlas);
        int plannedModelTileCount = atlas.plannedAtlasTileCount(
                Map.of("facetest/animated_panel_block", 0xFFFFFFFF),
                Map.of("voxel:block/facetest:animated_panel_block", "facetest/animated_panel_block"),
                Map.of("facetest/animated_panel_block", "facetest:animated_panel_block"),
                List.of(new EchoClientTextureAtlas.BlockModelRequest(
                        "facetest:animated_panel_block",
                        Map.of(),
                        "facetest/animated_panel_block"
                ))
        );
        require(plannedModelTileCount == 1 && atlas.lastRemovedBaseAtlasRequestCount() == 1,
                "Fully model-resolved blocks should not keep a duplicate base material atlas tile"
                        + " planned=" + plannedModelTileCount
                        + " removed=" + atlas.lastRemovedBaseAtlasRequestCount());
        requireMissingChecker(
                atlas.loadOrGenerateTile("minecraft-texture/facetest/block/does_not_exist", "", 0xFFFFFFFF,
                        "facetest:block/does_not_exist"),
                "Explicit model texture id fallback should be the missing checker"
        );
        requireMissingChecker(
                atlas.loadOrGenerateTile("facetest/missing_panel", "facetest:missing_panel", 0xFFAA8844, ""),
                "Model-resolved missing PNG fallback should be the missing checker"
        );

        EchoItemTextureResolver.EchoItemTextureResolution handheld = itemResolver.resolve("facetest:scrap_knife");
        require(handheld.resolved(),
                "Handheld item model should resolve a layer0 texture");
        require(handheld.templateKind().orElse("").equals("handheld"),
                "Handheld item model should detect the minecraft:item/handheld template");
        require(handheld.textureId().orElse("").equals("facetest:item/scrap_knife"),
                "Handheld item model should preserve the declared layer0 texture id");

        EchoItemTextureResolver.EchoItemTextureResolution generated = itemResolver.resolve("facetest:clean_water_bottle");
        require(generated.resolved(),
                "Generated item model should resolve a layer0 texture");
        require(generated.templateKind().orElse("").equals("generated"),
                "Generated item model should detect the minecraft:item/generated template");
        require(generated.textureId().orElse("").equals("facetest:item/clean_water_bottle"),
                "Generated item model should preserve the declared layer0 texture id");

        EchoItemTextureResolver.EchoItemTextureLayerResolution layered =
                itemResolver.resolveLayers("facetest:layered_badge");
        require(layered.resolved(),
                "Generated layered item model should resolve declared item texture layers");
        require(layered.textureIds().equals(List.of(
                        "facetest:item/layered_badge_base",
                        "facetest:item/layered_badge_overlay"
                )),
                "Generated layered item model should preserve layer0/layer1 order");

        EchoItemTextureResolver.EchoItemTextureLayerResolution overrideBase =
                itemResolver.resolveLayers("facetest:override_tool");
        require(overrideBase.modelId().orElse("").equals("facetest:item/override_tool")
                        && overrideBase.textureIds().equals(List.of("facetest:item/override_tool")),
                "Item model overrides should not apply when no item predicate state is supplied");
        EchoItemTextureResolver.EchoItemTextureLayerResolution overrideChipped =
                itemResolver.resolveLayers("facetest:override_tool", Map.of("damage", 0.50D));
        require(overrideChipped.modelId().orElse("").equals("facetest:item/override_tool_chipped")
                        && overrideChipped.textureIds().equals(List.of("facetest:item/override_tool_chipped")),
                "Item model overrides should select the matching threshold model");
        EchoItemTextureResolver.EchoItemTextureLayerResolution overrideBroken =
                itemResolver.resolveLayers("facetest:override_tool", Map.of("minecraft:damage", 0.90D));
        require(overrideBroken.modelId().orElse("").equals("facetest:item/override_tool_broken")
                        && overrideBroken.textureIds().equals(List.of("facetest:item/override_tool_broken")),
                "Item model overrides should let later higher-threshold matches refine the selected icon model");

        EchoClientSlotIconCache iconCache = new EchoClientSlotIconCache();
        iconCache.setMinecraftAssets(minecraft);
        EchoClientSlotIconCache.EchoClientSlotIconPlan overrideBrokenItemIconPlan =
                iconCache.planItemIcon("facetest:override_tool", Map.of("damage", 0.90D));
        require(overrideBrokenItemIconPlan.resolved()
                        && overrideBrokenItemIconPlan.sourceKind().equals("resource-pack")
                        && overrideBrokenItemIconPlan.source().equals(
                                "facetest:textures/item/override_tool_broken.png")
                        && overrideBrokenItemIconPlan.texturePath().equals("item/override_tool_broken"),
                "Slot item icon plans should apply item model predicates before choosing resource-pack textures");
        EchoClientSlotIconCache.EchoClientSlotIconPlan itemIconPlan =
                iconCache.planItemIcon("facetest:scrap_knife");
        require(itemIconPlan.resolved()
                        && itemIconPlan.sourceKind().equals("resource-pack")
                        && itemIconPlan.source().equals("facetest:textures/item/scrap_knife.png")
                        && itemIconPlan.texturePath().equals("item/scrap_knife"),
                "Slot item icon plan should resolve generated/handheld item texture from mounted resource packs");
        EchoClientSlotIconCache.EchoClientSlotIconPlan layeredItemIconPlan =
                iconCache.planItemIcon("facetest:layered_badge");
        require(layeredItemIconPlan.resolved()
                        && layeredItemIconPlan.sourceKind().equals("resource-pack-layers")
                        && layeredItemIconPlan.texturePath().equals(
                                "item/layered_badge_base,item/layered_badge_overlay")
                        && layeredItemIconPlan.source().contains(
                                "facetest:textures/item/layered_badge_base.png")
                        && layeredItemIconPlan.source().contains(
                                "facetest:textures/item/layered_badge_overlay.png"),
                "Slot item icon plan should preserve generated item layer stacks from mounted resource packs");
        EchoClientSlotIconCache.EchoClientSlotIconPlan missingItemIconPlan =
                iconCache.planItemIcon("facetest:missing_tool");
        require(!missingItemIconPlan.resolved()
                        && !missingItemIconPlan.missingTextureFallback()
                        && missingItemIconPlan.detail().contains("missing model"),
                "Slot item icon plan should diagnose missing item models before upload");
        EchoClientSlotIconCache.EchoClientSlotIconPlan missingDeclaredTexturePlan =
                iconCache.planItemIcon("facetest:missing_icon_tool");
        require(!missingDeclaredTexturePlan.resolved()
                        && missingDeclaredTexturePlan.missingTextureFallback()
                        && missingDeclaredTexturePlan.source().equals(EchoMissingTexture.LOGICAL_ID)
                        && missingDeclaredTexturePlan.texturePath().equals("item/missing_icon_tool"),
                "Slot item icon plan should select the visible missing texture for absent declared PNGs");
        requireSlotIconQueue(iconCache);

        System.out.println("client block texture resolver smoke PASS block=facetest:copper_pillar template="
                + resolution.templateKind().orElse("<missing>"));
    }

    private static void requireUvLockPlans(EchoClientTextureAtlas atlas) {
        EchoClientTextureAtlas.BlockRenderPlan unlockedY = atlas.planBlockModel(
                new EchoClientTextureAtlas.BlockModelRequest(
                        "facetest:uv_unlocked_y90",
                        Map.of(),
                        "facetest/uv_unlocked_y90"
                )
        );
        EchoClientTextureAtlas.BlockRenderPlan lockedY = atlas.planBlockModel(
                new EchoClientTextureAtlas.BlockModelRequest(
                        "facetest:uv_locked_y90",
                        Map.of(),
                        "facetest/uv_locked_y90"
                )
        );
        require(unlockedY.resolved() && lockedY.resolved(),
                "UV lock fixture block models should resolve before atlas planning assertions");
        require(unlockedY.yRotationDegrees() == 90 && !unlockedY.uvLock(),
                "Unlocked Y-rotated block render plan should retain model rotation metadata");
        require(lockedY.yRotationDegrees() == 90 && lockedY.uvLock(),
                "UV-locked Y-rotated block render plan should retain uvlock metadata");
        require(unlockedY.uvRotationDegrees(EchoVoxelMeshDirection.UP) == 270,
                "Unlocked Y-rotated top faces should rotate UVs with the model");
        require(lockedY.uvRotationDegrees(EchoVoxelMeshDirection.UP) == 0,
                "UV-locked Y-rotated top faces should keep world-locked UV orientation");
        require(unlockedY.uvRotationDegrees(EchoVoxelMeshDirection.NORTH) == 0
                        && lockedY.uvRotationDegrees(EchoVoxelMeshDirection.NORTH) == 0,
                "Y-rotated side faces should remain upright with or without uvlock");

        EchoClientTextureAtlas.BlockRenderPlan unlockedX = atlas.planBlockModel(
                new EchoClientTextureAtlas.BlockModelRequest(
                        "facetest:uv_unlocked_x90",
                        Map.of(),
                        "facetest/uv_unlocked_x90"
                )
        );
        EchoClientTextureAtlas.BlockRenderPlan lockedX = atlas.planBlockModel(
                new EchoClientTextureAtlas.BlockModelRequest(
                        "facetest:uv_locked_x90",
                        Map.of(),
                        "facetest/uv_locked_x90"
                )
        );
        require(unlockedX.xRotationDegrees() == 90 && !unlockedX.uvLock(),
                "Unlocked X-rotated block render plan should retain model rotation metadata");
        require(lockedX.xRotationDegrees() == 90 && lockedX.uvLock(),
                "UV-locked X-rotated block render plan should retain uvlock metadata");
        require(unlockedX.uvRotationDegrees(EchoVoxelMeshDirection.NORTH) == 180,
                "Unlocked X-rotated side faces should rotate UVs with the model");
        require(lockedX.uvRotationDegrees(EchoVoxelMeshDirection.NORTH) == 0,
                "UV-locked X-rotated side faces should keep world-locked UV orientation");
    }

    private static void requireSlotIconQueue(EchoClientSlotIconCache iconCache) {
        EchoClientSlotStack scrapKnife = new EchoClientSlotStack(
                0,
                EchoClientSlotStackKind.ITEM,
                "facetest:scrap_knife",
                "Scrap Knife",
                1,
                null,
                Map.of(),
                List.of("Queue smoke"),
                0,
                0
        );
        require(iconCache.cachedOrQueueItemIcon(scrapKnife.runtimeId(), scrapKnife.itemModelPredicates()) == 0,
                "Uncached item icon lookup should use fallback rendering while queuing prewarm work");
        require(iconCache.queuedIconCount() == 1 && iconCache.cachedIconCount() == 0,
                "Uncached item icon lookup should queue exactly one non-GL prewarm request");
        iconCache.cachedOrQueueItemIcon(scrapKnife.runtimeId(), scrapKnife.itemModelPredicates());
        require(iconCache.queuedIconCount() == 1,
                "Repeated uncached item icon lookup should dedupe queued prewarm requests");

        EchoClientSlotStack damagedTool = new EchoClientSlotStack(
                1,
                EchoClientSlotStackKind.ITEM,
                "facetest:override_tool",
                "Override Tool",
                1,
                null,
                Map.of("damage", 0.90D),
                List.of("Predicate queue smoke"),
                0,
                0
        );
        EchoClientInventoryScreenModel model = new EchoClientInventoryScreenModel(
                "test:slot-icon-queue",
                "Slot Icon Queue",
                List.of(scrapKnife, damagedTool),
                0
        );
        iconCache.queueSlotIcons(model);
        require(iconCache.queuedIconCount() == 2,
                "Slot icon queue should add predicate-specific item icons without duplicating existing slots");
        require(iconCache.lastPrewarmRequestCount() == 0 && iconCache.lastPrewarmLoadedCount() == 0,
                "Slot icon queue smoke should not touch the GL upload path");
    }

    private static void requireMissingChecker(ByteBuffer tile, String message) {
        int size = 64;
        require(tile != null && tile.capacity() == size * size * 4,
                message + " with a 64x64 RGBA tile");
        require(pixel(tile, 0, 0, 0) == 255
                        && pixel(tile, 0, 0, 1) == 0
                        && pixel(tile, 0, 0, 2) == 255
                        && pixel(tile, 0, 0, 3) == 255,
                message + " at the first magenta checker cell");
        require(pixel(tile, 16, 0, 0) == 0
                        && pixel(tile, 16, 0, 1) == 0
                        && pixel(tile, 16, 0, 2) == 0
                        && pixel(tile, 16, 0, 3) == 255,
                message + " at the first black checker cell");
    }

    private static int pixel(ByteBuffer tile, int x, int y, int channel) {
        return unsigned(tile.get((y * 64 + x) * 4 + channel));
    }

    private static int unsigned(byte value) {
        return value & 0xFF;
    }

    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }

    private static void writeAnimatedStrip(Path path, int frameSize, int... frameArgb) throws IOException {
        Files.createDirectories(path.getParent());
        BufferedImage image = new BufferedImage(frameSize, frameSize * frameArgb.length, BufferedImage.TYPE_INT_ARGB);
        for (int frame = 0; frame < frameArgb.length; frame++) {
            for (int y = 0; y < frameSize; y++) {
                for (int x = 0; x < frameSize; x++) {
                    image.setRGB(x, frame * frameSize + y, frameArgb[frame]);
                }
            }
        }
        ImageIO.write(image, "png", path.toFile());
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
