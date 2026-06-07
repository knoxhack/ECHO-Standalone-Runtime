package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelElement;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelFaceUv;
import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelBounds;
import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesh;
import dev.echo.standalone.runtime.render.EchoVoxelMeshDirection;
import dev.echo.standalone.runtime.render.EchoVoxelMeshFace;
import dev.echo.standalone.runtime.render.EchoVoxelMeshMaterial;
import dev.echo.standalone.runtime.render.EchoVoxelRenderBackendTarget;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientBlockModelChunkRenderSmokeHarness {
    private EchoClientBlockModelChunkRenderSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of("build/tmp/client-block-model-chunk-render-smoke").toAbsolutePath().normalize();
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
                    "end": "facetest:block/copper_pillar_end"
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
        write(root.resolve("assets/facetest/blockstates/trimmed_post.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/trimmed_post" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/rotated_trimmed_post.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/trimmed_post", "y": 90, "uvlock": true }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/x_rotated_trimmed_post.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/trimmed_post", "x": 90 }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/rotated_asymmetric_faces.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/asymmetric_faces", "y": 90, "uvlock": true }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/multipart_asymmetric_faces.json"), """
                {
                  "multipart": [
                    {
                      "apply": { "model": "facetest:block/asymmetric_faces", "y": 90 }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/block/asymmetric_faces.json"), """
                {
                  "textures": {
                    "up": "facetest:block/asym_up",
                    "down": "facetest:block/asym_down",
                    "north": "facetest:block/asym_north",
                    "east": "facetest:block/asym_east",
                    "south": "facetest:block/asym_south",
                    "west": "facetest:block/asym_west"
                  },
                  "elements": [
                    {
                      "from": [0, 0, 0],
                      "to": [16, 16, 16],
                      "faces": {
                        "up": { "texture": "#up", "cullface": "up" },
                        "down": { "texture": "#down", "cullface": "down" },
                        "north": {
                          "texture": "#north",
                          "uv": [0, 0, 4, 4],
                          "rotation": 90,
                          "tintindex": 0,
                          "cullface": "north"
                        },
                        "east": {
                          "texture": "#east",
                          "uv": [4, 0, 8, 4],
                          "rotation": 180,
                          "tintindex": 1,
                          "cullface": "east"
                        },
                        "south": {
                          "texture": "#south",
                          "uv": [8, 0, 12, 4],
                          "rotation": 270,
                          "cullface": "south"
                        },
                        "west": {
                          "texture": "#west",
                          "uv": [12, 0, 16, 4],
                          "cullface": "west"
                        }
                      }
                    }
                  ]
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
        write(root.resolve("assets/facetest/blockstates/rotating_bar.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/rotating_bar" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/rotating_bar.json"), """
                {
                  "textures": {
                    "all": "facetest:block/rotating_bar"
                  },
                  "elements": [
                    {
                      "from": [6, 0, 6],
                      "to": [10, 16, 10],
                      "rotation": { "origin": [8, 8, 8], "axis": "y", "angle": 45, "rescale": true },
                      "faces": {
                        "up": { "texture": "#all" },
                        "down": { "texture": "#all" },
                        "north": { "texture": "#all" },
                        "south": { "texture": "#all" },
                        "east": { "texture": "#all" },
                        "west": { "texture": "#all" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/element_cube.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/element_cube" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/element_cube.json"), """
                {
                  "textures": {
                    "all": "facetest:block/element_cube"
                  },
                  "elements": [
                    {
                      "from": [0, 0, 0],
                      "to": [16, 16, 16],
                      "faces": {
                        "up": { "texture": "#all", "cullface": "up" },
                        "down": { "texture": "#all", "cullface": "down" },
                        "north": { "texture": "#all", "cullface": "north" },
                        "south": { "texture": "#all", "cullface": "south" },
                        "east": { "texture": "#all", "cullface": "east" },
                        "west": { "texture": "#all", "cullface": "west" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/element_cube_no_cull.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/element_cube_no_cull" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/element_cube_no_cull.json"), """
                {
                  "textures": {
                    "all": "facetest:block/element_cube_no_cull"
                  },
                  "elements": [
                    {
                      "from": [0, 0, 0],
                      "to": [16, 16, 16],
                      "faces": {
                        "up": { "texture": "#all" },
                        "down": { "texture": "#all" },
                        "north": { "texture": "#all" },
                        "south": { "texture": "#all" },
                        "east": { "texture": "#all" },
                        "west": { "texture": "#all" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/multipart_stack.json"), """
                {
                  "multipart": [
                    {
                      "when": { "base": "true" },
                      "apply": { "model": "facetest:block/multipart_stack_base" }
                    },
                    {
                      "when": { "cap": "true" },
                      "apply": { "model": "facetest:block/multipart_stack_cap", "y": 90 }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/block/multipart_stack_base.json"), """
                {
                  "textures": {
                    "all": "facetest:block/multipart_stack_base"
                  },
                  "elements": [
                    {
                      "from": [0, 0, 0],
                      "to": [16, 4, 16],
                      "faces": {
                        "up": { "texture": "#all" },
                        "down": { "texture": "#all" },
                        "north": { "texture": "#all" },
                        "south": { "texture": "#all" },
                        "east": { "texture": "#all" },
                        "west": { "texture": "#all" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/block/multipart_stack_cap.json"), """
                {
                  "textures": {
                    "all": "facetest:block/multipart_stack_cap"
                  },
                  "elements": [
                    {
                      "from": [0, 4, 0],
                      "to": [4, 8, 16],
                      "faces": {
                        "up": { "texture": "#all" },
                        "down": { "texture": "#all" },
                        "north": { "texture": "#all" },
                        "south": { "texture": "#all" },
                        "east": { "texture": "#all" },
                        "west": { "texture": "#all" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/composite_frame.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/composite_frame" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/composite_frame.json"), """
                {
                  "textures": {
                    "base": "facetest:block/composite_frame_base",
                    "post": "facetest:block/composite_frame_post"
                  },
                  "elements": [
                    {
                      "from": [0, 0, 0],
                      "to": [16, 4, 16],
                      "faces": {
                        "up": { "texture": "#base", "uv": [0, 0, 8, 8] },
                        "down": { "texture": "#base" },
                        "north": { "texture": "#base" },
                        "south": { "texture": "#base" },
                        "east": { "texture": "#base" },
                        "west": { "texture": "#base" }
                      }
                    },
                    {
                      "from": [4, 4, 4],
                      "to": [12, 16, 12],
                      "faces": {
                        "up": { "texture": "#post", "uv": [8, 8, 16, 16] },
                        "down": { "texture": "#post" },
                        "north": { "texture": "#post" },
                        "south": { "texture": "#post" },
                        "east": { "texture": "#post" },
                        "west": { "texture": "#post" }
                      }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/dead_fern.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/dead_fern" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/dead_fern.json"), """
                {
                  "parent": "minecraft:block/cross",
                  "textures": {
                    "cross": "facetest:block/dead_fern"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_slab.json"), """
                {
                  "variants": {
                    "type=bottom": { "model": "facetest:block/service_slab" },
                    "type=top": { "model": "facetest:block/service_slab_top" },
                    "type=double": { "model": "facetest:block/service_slab_double" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_slab.json"), """
                {
                  "parent": "minecraft:block/slab",
                  "textures": {
                    "bottom": "facetest:block/service_slab_bottom",
                    "top": "facetest:block/service_slab_top",
                    "side": "facetest:block/service_slab_side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_slab_top.json"), """
                {
                  "parent": "minecraft:block/slab_top",
                  "textures": {
                    "bottom": "facetest:block/service_slab_bottom",
                    "top": "facetest:block/service_slab_top",
                    "side": "facetest:block/service_slab_side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_slab_double.json"), """
                {
                  "parent": "minecraft:block/cube_all",
                  "textures": {
                    "all": "facetest:block/service_slab_double"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_stairs.json"), """
                {
                  "variants": {
                    "facing=east,half=bottom,shape=straight": { "model": "facetest:block/service_stairs" },
                    "facing=north,half=top,shape=inner_left": { "model": "facetest:block/service_stairs_inner" },
                    "facing=south,half=bottom,shape=outer_right": { "model": "facetest:block/service_stairs_outer" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_stairs.json"), """
                {
                  "parent": "minecraft:block/stairs",
                  "textures": {
                    "bottom": "facetest:block/service_stairs_bottom",
                    "top": "facetest:block/service_stairs_top",
                    "side": "facetest:block/service_stairs_side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_stairs_inner.json"), """
                {
                  "parent": "minecraft:block/inner_stairs",
                  "textures": {
                    "bottom": "facetest:block/service_stairs_bottom",
                    "top": "facetest:block/service_stairs_top",
                    "side": "facetest:block/service_stairs_side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_stairs_outer.json"), """
                {
                  "parent": "minecraft:block/outer_stairs",
                  "textures": {
                    "bottom": "facetest:block/service_stairs_bottom",
                    "top": "facetest:block/service_stairs_top",
                    "side": "facetest:block/service_stairs_side"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_wall.json"), """
                {
                  "multipart": [
                    {
                      "when": { "up": "true" },
                      "apply": { "model": "facetest:block/service_wall_post" }
                    },
                    {
                      "when": { "north": "low" },
                      "apply": { "model": "facetest:block/service_wall_side" }
                    },
                    {
                      "when": { "east": "tall" },
                      "apply": { "model": "facetest:block/service_wall_side_tall", "y": 90 }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_wall_post.json"), """
                {
                  "parent": "minecraft:block/template_wall_post",
                  "textures": {
                    "wall": "facetest:block/service_wall"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_wall_side.json"), """
                {
                  "parent": "minecraft:block/template_wall_side",
                  "textures": {
                    "wall": "facetest:block/service_wall"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_wall_side_tall.json"), """
                {
                  "parent": "minecraft:block/template_wall_side_tall",
                  "textures": {
                    "wall": "facetest:block/service_wall"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_fence.json"), """
                {
                  "multipart": [
                    {
                      "apply": { "model": "facetest:block/service_fence_post" }
                    },
                    {
                      "when": { "north": "true" },
                      "apply": { "model": "facetest:block/service_fence_side" }
                    },
                    {
                      "when": { "east": "true" },
                      "apply": { "model": "facetest:block/service_fence_side", "y": 90 }
                    },
                    {
                      "when": { "south": "true" },
                      "apply": { "model": "facetest:block/service_fence_side", "y": 180 }
                    },
                    {
                      "when": { "west": "true" },
                      "apply": { "model": "facetest:block/service_fence_side", "y": 270 }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_fence_side_probe.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/service_fence_side" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_fence_post.json"), """
                {
                  "parent": "minecraft:block/fence_post",
                  "textures": {
                    "texture": "facetest:block/service_fence"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_fence_side.json"), """
                {
                  "parent": "minecraft:block/fence_side",
                  "textures": {
                    "texture": "facetest:block/service_fence"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_pane.json"), """
                {
                  "multipart": [
                    {
                      "apply": { "model": "facetest:block/service_pane_post" }
                    },
                    {
                      "when": { "north": "true" },
                      "apply": { "model": "facetest:block/service_pane_side" }
                    },
                    {
                      "when": { "east": "true" },
                      "apply": { "model": "facetest:block/service_pane_side", "y": 90 }
                    },
                    {
                      "when": { "south": "true" },
                      "apply": { "model": "facetest:block/service_pane_side_alt" }
                    },
                    {
                      "when": { "west": "true" },
                      "apply": { "model": "facetest:block/service_pane_side_alt", "y": 90 }
                    }
                  ]
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_pane_side_probe.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/service_pane_side" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_pane_noside_probe.json"), """
                {
                  "variants": {
                    "": { "model": "facetest:block/service_pane_noside" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_pane_post.json"), """
                {
                  "parent": "minecraft:block/template_glass_pane_post",
                  "textures": {
                    "pane": "facetest:block/service_pane",
                    "edge": "facetest:block/service_pane_edge"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_pane_side.json"), """
                {
                  "parent": "minecraft:block/template_glass_pane_side",
                  "textures": {
                    "pane": "facetest:block/service_pane",
                    "edge": "facetest:block/service_pane_edge"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_pane_side_alt.json"), """
                {
                  "parent": "minecraft:block/template_glass_pane_side_alt",
                  "textures": {
                    "pane": "facetest:block/service_pane",
                    "edge": "facetest:block/service_pane_edge"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_pane_noside.json"), """
                {
                  "parent": "minecraft:block/template_glass_pane_noside",
                  "textures": {
                    "pane": "facetest:block/service_pane",
                    "edge": "facetest:block/service_pane_edge"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_trapdoor.json"), """
                {
                  "variants": {
                    "facing=north,half=bottom,open=false": { "model": "facetest:block/service_trapdoor_bottom" },
                    "facing=south,half=top,open=false": { "model": "facetest:block/service_trapdoor_top" },
                    "facing=east,half=bottom,open=true": { "model": "facetest:block/service_trapdoor_open" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_trapdoor_bottom.json"), """
                {
                  "parent": "minecraft:block/template_orientable_trapdoor_bottom",
                  "textures": {
                    "texture": "facetest:block/service_trapdoor"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_trapdoor_top.json"), """
                {
                  "parent": "minecraft:block/template_trapdoor_top",
                  "textures": {
                    "texture": "facetest:block/service_trapdoor"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_trapdoor_open.json"), """
                {
                  "parent": "minecraft:block/template_orientable_trapdoor_open",
                  "textures": {
                    "texture": "facetest:block/service_trapdoor"
                  }
                }
                """);
        write(root.resolve("assets/facetest/blockstates/service_door.json"), """
                {
                  "variants": {
                    "facing=north,half=lower,hinge=left,open=false": { "model": "facetest:block/service_door_bottom_left" },
                    "facing=south,half=upper,hinge=right,open=false": { "model": "facetest:block/service_door_top_right" },
                    "facing=east,half=lower,hinge=left,open=true": { "model": "facetest:block/service_door_bottom_left_open" },
                    "facing=west,half=upper,hinge=right,open=true": { "model": "facetest:block/service_door_top_right_open" }
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_door_bottom_left.json"), """
                {
                  "parent": "minecraft:block/door_bottom_left",
                  "textures": {
                    "bottom": "facetest:block/service_door_bottom",
                    "top": "facetest:block/service_door_top"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_door_top_right.json"), """
                {
                  "parent": "minecraft:block/door_top_right",
                  "textures": {
                    "bottom": "facetest:block/service_door_bottom",
                    "top": "facetest:block/service_door_top"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_door_bottom_left_open.json"), """
                {
                  "parent": "minecraft:block/door_bottom_left_open",
                  "textures": {
                    "bottom": "facetest:block/service_door_bottom",
                    "top": "facetest:block/service_door_top"
                  }
                }
                """);
        write(root.resolve("assets/facetest/models/block/service_door_top_right_open.json"), """
                {
                  "parent": "minecraft:block/door_top_right_open",
                  "textures": {
                    "bottom": "facetest:block/service_door_bottom",
                    "top": "facetest:block/service_door_top"
                  }
                }
                """);

        EchoMinecraftAssetResolver minecraft = new EchoMinecraftAssetResolver(
                new EchoAssetRuntime(List.of(new EchoAssetMount(0, "smoke", root, "facetest")))
                        .load(new EchoDefaultRuntimeServiceRegistry(), List.of())
                        .resolver()
        );
        EchoClientTextureAtlas atlas = new EchoClientTextureAtlas();
        atlas.setMinecraftAssets(minecraft);

        EchoClientTextureAtlas.BlockRenderPlan pillar =
                atlas.planBlockModel(request("facetest:copper_pillar", Map.of()));
        require(pillar.resolved(), "Column blockstate/model should resolve into a chunk-render plan");
        require(pillar.modelId().equals("facetest:block/copper_pillar"),
                "Column render plan should keep the selected model id");
        require(pillar.templateKind().equals("cube_column"),
                "Column render plan should detect the minecraft cube_column parent");
        require(pillar.textureId(EchoVoxelMeshDirection.UP).orElse("").equals("facetest:block/copper_pillar_end"),
                "Column up face should use the JSON model end texture");
        require(pillar.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/copper_pillar_side"),
                "Column north face should use the JSON model side texture");
        require(pillar.atlasKey(EchoVoxelMeshDirection.UP).orElse("")
                        .equals("minecraft-texture/facetest/block/copper_pillar_end"),
                "Column up face should map to the model texture atlas key used by chunk upload");

        EchoClientTextureAtlas.BlockRenderPlan poweredOff =
                atlas.planBlockModel(request("facetest:stateful_panel", Map.of("powered", "false")));
        EchoClientTextureAtlas.BlockRenderPlan poweredOn =
                atlas.planBlockModel(request("facetest:stateful_panel", Map.of("powered", "true")));
        require(poweredOff.modelId().equals("facetest:block/stateful_panel_off"),
                "Stateful block render plan should select the powered=false model");
        require(poweredOn.modelId().equals("facetest:block/stateful_panel_on"),
                "Stateful block render plan should select the powered=true model");
        require(poweredOn.textureId(EchoVoxelMeshDirection.SOUTH).orElse("")
                        .equals("facetest:block/stateful_panel_on"),
                "Stateful chunk face should use the active-state model texture");

        EchoClientTextureAtlas.BlockRenderPlan trimmed =
                atlas.planBlockModel(request("facetest:trimmed_post", Map.of()));
        require(trimmed.resolved(), "Element-backed block model should resolve into a chunk-render plan");
        EchoBlockModelBounds bounds = trimmed.modelBounds();
        require(bounds.fromX() == 2.0D && bounds.toY() == 8.0D && bounds.toZ() == 12.0D,
                "Element-backed render plan should expose JSON model bounds");
        require(!bounds.fullCubeBounds(),
                "Element-backed render plan should not collapse trimmed models to a full cube");
        require(trimmed.textureId(EchoVoxelMeshDirection.UP).orElse("").equals("facetest:block/trimmed_post_cap"),
                "Element-backed render plan should use JSON face texture declarations for top faces");
        require(trimmed.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/trimmed_post_trim"),
                "Element-backed render plan should use JSON face texture declarations for side faces");
        require(trimmed.textureId(EchoVoxelMeshDirection.EAST).orElse("").equals("facetest:block/trimmed_post"),
                "Element-backed render plan should preserve explicit all-texture face declarations");
        require(trimmed.uvRotationDegrees(EchoVoxelMeshDirection.UP) == 90,
                "Element-backed render plan should preserve JSON face UV rotation declarations");
        EchoBlockModelFaceUv trimmedTopFaceUv = trimmed.uvRect(EchoVoxelMeshDirection.UP).orElseThrow();
        require(trimmedTopFaceUv.u1() == 4.0D && trimmedTopFaceUv.v1() == 4.0D
                        && trimmedTopFaceUv.u2() == 12.0D && trimmedTopFaceUv.v2() == 12.0D,
                "Element-backed render plan should preserve JSON face UV rectangle declarations");
        require(trimmed.tintIndex(EchoVoxelMeshDirection.UP).orElse(-1) == 0,
                "Element-backed render plan should preserve JSON face tintindex declarations");
        require(trimmed.tintIndex(EchoVoxelMeshDirection.NORTH).isEmpty(),
                "Element-backed render plan should keep untinted JSON faces unmarked");

        EchoVoxelMeshMaterial material = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:trimmed_post",
                "facetest/trimmed_post",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "facetest:smoke_garden",
                0xFF22CC44,
                true
        );
        EchoVoxelMeshFace face = new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.UP, material);
        float[] corners = EchoClientChunkMesh.cornerOffsets(face.direction(), atlas.modelBounds(face));
        require(corners[1] == 0.5f && corners[4] == 0.5f && corners[7] == 0.5f && corners[10] == 0.5f,
                "Chunk mesh upload should use JSON element Y bounds for the UP face plane");
        require(corners[0] == 0.125f && corners[6] == 0.875f,
                "Chunk mesh upload should use JSON element X bounds for face corners");
        require(corners[2] == 0.25f && corners[5] == 0.75f,
                "Chunk mesh upload should use JSON element Z bounds for face corners");
        EchoClientChunkMesh.MeshData trimmedMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(0, 0, 0, direction, material))
                        .toList(),
                1
        ), atlas);
        EchoClientTextureAtlas.AtlasEntry topUv = atlas.get(face, EchoVoxelMeshDirection.UP);
        require(topUv.u1() == 0.25f && topUv.v1() == 0.25f
                        && topUv.u2() == 0.75f && topUv.v2() == 0.75f,
                "Chunk mesh upload should crop top-face atlas UVs from JSON face uv rectangles");
        require(trimmedMesh.vertices()[3] == topUv.u1() && trimmedMesh.vertices()[4] == topUv.v2(),
                "Chunk mesh upload should rotate top-face UV corners from JSON face rotation");
        require(trimmedMesh.vertices()[8] == 0x22 / 255.0f
                        && trimmedMesh.vertices()[9] == 0xCC / 255.0f
                        && trimmedMesh.vertices()[10] == 0x44 / 255.0f,
                "Chunk mesh upload should apply biome tint color to tint-indexed JSON model faces");
        int northColorOffset = 16 * 12 + 8;
        require(trimmedMesh.vertices()[northColorOffset] == 1.0f
                        && trimmedMesh.vertices()[northColorOffset + 1] == 1.0f
                        && trimmedMesh.vertices()[northColorOffset + 2] == 1.0f,
                "Chunk mesh upload should leave JSON model faces without tintindex untinted");

        EchoClientTextureAtlas.BlockRenderPlan rotatingBar =
                atlas.planBlockModel(request("facetest:rotating_bar", Map.of()));
        require(rotatingBar.resolved() && rotatingBar.modelElementDefinitions().size() == 1,
                "Rotated custom element model should resolve into one element definition");
        EchoBlockModelElement rotatingElement = rotatingBar.modelElementDefinitions().get(0);
        require(rotatingElement.rotation().isPresent()
                        && rotatingElement.rotation().orElseThrow().axis().equals("y")
                        && rotatingElement.rotation().orElseThrow().angleDegrees() == 45.0D
                        && rotatingElement.rotation().orElseThrow().rescale(),
                "Rotated custom element model should preserve JSON rotation axis, angle, and rescale metadata");
        EchoVoxelMeshMaterial rotatingBarMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:rotating_bar",
                "facetest/rotating_bar",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData rotatingBarMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(2, 0, 0, direction, rotatingBarMaterial))
                        .toList(),
                1
        ), atlas);
        require(rotatingBarMesh.vertexCount() == 24 && rotatingBarMesh.indexCount() == 36,
                "Rotated custom element chunk upload should still emit one six-face element");
        require(close(rotatingBarMesh.vertices()[0], 2.5D)
                        && close(rotatingBarMesh.vertices()[1], 1.0D)
                        && close(rotatingBarMesh.vertices()[2], 0.3232233D),
                "Rotated custom element chunk upload should rotate element vertices around the JSON origin");
        int rotatedNorthNormalOffset = 4 * 4 * 12 + 5;
        require(close(rotatingBarMesh.vertices()[rotatedNorthNormalOffset], 0.7071068D)
                        && close(rotatingBarMesh.vertices()[rotatedNorthNormalOffset + 1], 0.0D)
                        && close(rotatingBarMesh.vertices()[rotatedNorthNormalOffset + 2], -0.7071068D),
                "Rotated custom element chunk upload should rotate element face normals with the geometry");

        EchoClientTextureAtlas.BlockRenderPlan elementCube =
                atlas.planBlockModel(request("facetest:element_cube", Map.of()));
        require(elementCube.resolved() && elementCube.modelElements().size() == 1,
                "Full element-cube model should resolve into one custom element");
        require(elementCube.modelElementDefinitions().get(0).cullFaceForFace("up").orElse("").equals("up")
                        && elementCube.modelElementDefinitions().get(0).cullFaceForFace("north").orElse("").equals("north"),
                "Full element-cube model should preserve explicit JSON cullface declarations");
        EchoVoxelMeshMaterial elementCubeMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:element_cube",
                "facetest/element_cube",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData culledElementCubeMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                List.of(
                        new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.UP, elementCubeMaterial),
                        new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.NORTH, elementCubeMaterial)
                ),
                1
        ), atlas);
        require(culledElementCubeMesh.vertexCount() == 8 && culledElementCubeMesh.indexCount() == 12,
                "Custom element chunk upload should cull outer-boundary faces hidden by neighboring opaque blocks");
        int secondQuadNormalOffset = 4 * 12 + 5;
        require(culledElementCubeMesh.vertices()[secondQuadNormalOffset] == 0.0f
                        && culledElementCubeMesh.vertices()[secondQuadNormalOffset + 1] == 0.0f
                        && culledElementCubeMesh.vertices()[secondQuadNormalOffset + 2] == -1.0f,
                "Custom element chunk upload should preserve the remaining visible north face after culling");

        EchoClientTextureAtlas.BlockRenderPlan noCullElementCube =
                atlas.planBlockModel(request("facetest:element_cube_no_cull", Map.of()));
        require(noCullElementCube.resolved()
                        && noCullElementCube.modelElementDefinitions().get(0).cullFaceForFace("up").isEmpty(),
                "Custom element faces without cullface should remain explicitly uncullable");
        EchoVoxelMeshMaterial noCullElementCubeMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:element_cube_no_cull",
                "facetest/element_cube_no_cull",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData noCullElementCubeMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                List.of(
                        new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.UP, noCullElementCubeMaterial),
                        new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.NORTH, noCullElementCubeMaterial)
                ),
                1
        ), atlas);
        require(noCullElementCubeMesh.vertexCount() == 24 && noCullElementCubeMesh.indexCount() == 36,
                "Custom element chunk upload should keep boundary faces that omit JSON cullface");

        EchoClientTextureAtlas.BlockRenderPlan multipartStack =
                atlas.planBlockModel(request("facetest:multipart_stack", Map.of("base", "true", "cap", "true")));
        require(multipartStack.resolved() && multipartStack.templateKind().equals("multipart"),
                "Multipart blockstate render plan should compose matching apply models");
        require(multipartStack.modelElementDefinitions().size() == 2,
                "Multipart render plan should preserve every matching custom element apply");
        EchoBlockModelElement multipartCap = multipartStack.modelElementDefinitions().get(1);
        require(multipartCap.bounds().fromX() == 0.0D
                        && multipartCap.bounds().fromZ() == 0.0D
                        && multipartCap.bounds().toX() == 16.0D
                        && multipartCap.bounds().toZ() == 4.0D,
                "Multipart render plan should apply per-apply y rotation to composed element bounds");
        EchoVoxelMeshMaterial multipartStackMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:multipart_stack",
                "facetest/multipart_stack",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("base", "true", "cap", "true"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData multipartStackMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(8, 0, 0, direction, multipartStackMaterial))
                        .toList(),
                1
        ), atlas);
        require(multipartStackMesh.vertexCount() == 48 && multipartStackMesh.indexCount() == 72,
                "Multipart chunk upload should emit both matching custom element apply models");
        int multipartSecondElementUpVertex = 24 * 12;
        require(multipartStackMesh.vertices()[multipartSecondElementUpVertex] == 8.0f
                        && multipartStackMesh.vertices()[multipartSecondElementUpVertex + 1] == 0.5f
                        && multipartStackMesh.vertices()[multipartSecondElementUpVertex + 2] == 0.0f,
                "Multipart chunk upload should render the rotated second apply element");

        EchoClientTextureAtlas.BlockRenderPlan rotatedTrimmed =
                atlas.planBlockModel(request("facetest:rotated_trimmed_post", Map.of()));
        require(rotatedTrimmed.resolved(), "Rotated element-backed block model should resolve into a chunk-render plan");
        require(rotatedTrimmed.yRotationDegrees() == 90 && rotatedTrimmed.uvLock(),
                "Blockstate variant metadata should preserve y rotation and uvlock");
        EchoVoxelMeshMaterial rotatedTrimmedMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:rotated_trimmed_post",
                "facetest/rotated_trimmed_post",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData rotatedTrimmedMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(13, 0, 0, direction, rotatedTrimmedMaterial))
                        .toList(),
                1
        ), atlas);
        require(rotatedTrimmedMesh.vertexCount() == 24 && rotatedTrimmedMesh.indexCount() == 36,
                "Rotated element-backed upload should still emit one element box");
        require(rotatedTrimmedMesh.vertices()[0] == 13.25f
                        && rotatedTrimmedMesh.vertices()[1] == 0.5f
                        && rotatedTrimmedMesh.vertices()[2] == 0.125f,
                "Blockstate y rotation should rotate custom element bounds around the block center");

        EchoClientTextureAtlas.BlockRenderPlan rotatedAsymmetric =
                atlas.planBlockModel(request("facetest:rotated_asymmetric_faces", Map.of()));
        require(rotatedAsymmetric.resolved() && rotatedAsymmetric.yRotationDegrees() == 90,
                "Asymmetric rotated face fixture should preserve blockstate y rotation");
        require(rotatedAsymmetric.textureId(EchoVoxelMeshDirection.EAST).orElse("")
                        .equals("facetest:block/asym_north"),
                "Blockstate y rotation should remap original north texture onto world east face");
        require(rotatedAsymmetric.uvRotationDegrees(EchoVoxelMeshDirection.EAST) == 90,
                "Blockstate y rotation should remap original north UV rotation onto world east face");
        EchoBlockModelFaceUv rotatedAsymmetricEastUv =
                rotatedAsymmetric.uvRect(EchoVoxelMeshDirection.EAST).orElseThrow();
        require(rotatedAsymmetricEastUv.u1() == 0.0D
                        && rotatedAsymmetricEastUv.v1() == 0.0D
                        && rotatedAsymmetricEastUv.u2() == 4.0D
                        && rotatedAsymmetricEastUv.v2() == 4.0D,
                "Blockstate y rotation should remap original north UV rect onto world east face");
        require(rotatedAsymmetric.tintIndex(EchoVoxelMeshDirection.EAST).orElse(-1) == 0,
                "Blockstate y rotation should remap original north tint index onto world east face");
        EchoBlockModelElement rotatedAsymmetricElement =
                rotatedAsymmetric.modelElementDefinitions().get(0);
        EchoVoxelMeshMaterial rotatedAsymmetricMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:rotated_asymmetric_faces",
                "facetest/rotated_asymmetric_faces",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "facetest:smoke_garden",
                0xFF3366AA,
                true
        );
        EchoVoxelMeshFace rotatedAsymmetricEastFace =
                new EchoVoxelMeshFace(15, 0, 0, EchoVoxelMeshDirection.EAST, rotatedAsymmetricMaterial);
        require(atlas.uvRotationDegrees(
                        rotatedAsymmetricEastFace,
                        EchoVoxelMeshDirection.EAST,
                        rotatedAsymmetricElement
                ) == 90,
                "Element metadata lookup should query the inverse-rotated model face for UV rotation");
        require(atlas.tintIndex(
                        rotatedAsymmetricEastFace,
                        EchoVoxelMeshDirection.EAST,
                        rotatedAsymmetricElement
                ) == 0,
                "Element metadata lookup should query the inverse-rotated model face for tint");
        EchoClientChunkMesh.MeshData rotatedAsymmetricMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(15, 0, 0, direction, rotatedAsymmetricMaterial))
                        .toList(),
                1
        ), atlas);
        int rotatedAsymmetricEastColorOffset = 4 * 12 + 8;
        require(rotatedAsymmetricMesh.vertices()[rotatedAsymmetricEastColorOffset] == 0x33 / 255.0f
                        && rotatedAsymmetricMesh.vertices()[rotatedAsymmetricEastColorOffset + 1] == 0x66 / 255.0f
                        && rotatedAsymmetricMesh.vertices()[rotatedAsymmetricEastColorOffset + 2] == 0xAA / 255.0f,
                "Rotated asymmetric east face should render with original north tint metadata");

        EchoClientTextureAtlas.BlockRenderPlan multipartAsymmetric =
                atlas.planBlockModel(request("facetest:multipart_asymmetric_faces", Map.of()));
        require(multipartAsymmetric.resolved()
                        && multipartAsymmetric.templateKind().equals("multipart")
                        && multipartAsymmetric.modelElementDefinitions().size() == 1,
                "Multipart asymmetric rotated apply should resolve into one baked element");
        EchoBlockModelElement multipartAsymmetricElement =
                multipartAsymmetric.modelElementDefinitions().get(0);
        require(multipartAsymmetricElement.textureIdForFace("east").orElse("")
                        .equals("facetest:block/asym_north"),
                "Multipart y rotation should bake original north texture onto world east face");
        require(multipartAsymmetricElement.uvRotationDegreesForFace("east") == 90,
                "Multipart y rotation should bake original north UV rotation onto world east face");
        EchoBlockModelFaceUv multipartAsymmetricEastUv =
                multipartAsymmetricElement.uvRectForFace("east").orElseThrow();
        require(multipartAsymmetricEastUv.u1() == 0.0D
                        && multipartAsymmetricEastUv.v1() == 0.0D
                        && multipartAsymmetricEastUv.u2() == 4.0D
                        && multipartAsymmetricEastUv.v2() == 4.0D,
                "Multipart y rotation should bake original north UV rect onto world east face");
        require(multipartAsymmetricElement.tintIndexForFace("east").orElse(-1) == 0,
                "Multipart y rotation should bake original north tint index onto world east face");
        require(multipartAsymmetricElement.cullFaceForFace("east").orElse("").equals("east"),
                "Multipart y rotation should bake original north cullface into world east cullface");

        EchoClientTextureAtlas.BlockRenderPlan xRotatedTrimmed =
                atlas.planBlockModel(request("facetest:x_rotated_trimmed_post", Map.of()));
        require(xRotatedTrimmed.resolved(), "X-rotated element-backed block model should resolve into a chunk-render plan");
        require(xRotatedTrimmed.xRotationDegrees() == 90 && xRotatedTrimmed.yRotationDegrees() == 0,
                "Blockstate variant metadata should preserve x rotation independently of y rotation");
        EchoVoxelMeshMaterial xRotatedTrimmedMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:x_rotated_trimmed_post",
                "facetest/x_rotated_trimmed_post",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData xRotatedTrimmedMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(14, 0, 0, direction, xRotatedTrimmedMaterial))
                        .toList(),
                1
        ), atlas);
        require(xRotatedTrimmedMesh.vertexCount() == 24 && xRotatedTrimmedMesh.indexCount() == 36,
                "X-rotated element-backed upload should still emit one element box");
        require(xRotatedTrimmedMesh.vertices()[0] == 14.125f
                        && xRotatedTrimmedMesh.vertices()[1] == 0.75f
                        && xRotatedTrimmedMesh.vertices()[2] == 0.0f,
                "Blockstate x rotation should rotate custom element bounds around the block center");

        EchoClientTextureAtlas.BlockRenderPlan composite =
                atlas.planBlockModel(request("facetest:composite_frame", Map.of()));
        require(composite.resolved(), "Multi-element block model should resolve into a chunk-render plan");
        require(composite.modelElements().size() == 2,
                "Multi-element render plan should preserve every JSON element box");
        require(composite.modelElementDefinitions().size() == 2,
                "Multi-element render plan should preserve per-element face metadata");
        EchoBlockModelElement lowerElement = composite.modelElementDefinitions().get(0);
        EchoBlockModelElement upperElement = composite.modelElementDefinitions().get(1);
        require(lowerElement.textureIdForFace("up").orElse("").equals("facetest:block/composite_frame_base"),
                "Multi-element render plan should keep the lower element top texture");
        require(upperElement.textureIdForFace("up").orElse("").equals("facetest:block/composite_frame_post"),
                "Multi-element render plan should keep the upper element top texture independently");
        require(lowerElement.uvRectForFace("up").orElseThrow().u2() == 8.0D
                        && upperElement.uvRectForFace("up").orElseThrow().u1() == 8.0D,
                "Multi-element render plan should keep independent top-face UV rectangles");
        require(composite.modelBounds().fromX() == 0.0D
                        && composite.modelBounds().fromY() == 0.0D
                        && composite.modelBounds().fromZ() == 0.0D
                        && composite.modelBounds().toX() == 16.0D
                        && composite.modelBounds().toY() == 16.0D
                        && composite.modelBounds().toZ() == 16.0D,
                "Multi-element render plan should expose the combined model bounds");
        EchoVoxelMeshMaterial compositeMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:composite_frame",
                "facetest/composite_frame",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData compositeMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(4, 0, 0, direction, compositeMaterial))
                        .toList(),
                1
        ), atlas);
        require(compositeMesh.vertexCount() == 48 && compositeMesh.indexCount() == 72,
                "Multi-element upload should emit two boxes worth of textured quads");
        require(compositeMesh.vertices()[0] == 4.0f
                        && compositeMesh.vertices()[1] == 0.25f
                        && compositeMesh.vertices()[2] == 0.0f,
                "Multi-element upload should emit the lower slab element first");
        require(compositeMesh.vertices()[3] == 0.0f
                        && compositeMesh.vertices()[4] == 0.0f,
                "Multi-element upload should apply the lower element top-face UV rectangle");
        int secondElementUpVertex = 24 * 12;
        require(compositeMesh.vertices()[secondElementUpVertex] == 4.25f
                        && compositeMesh.vertices()[secondElementUpVertex + 1] == 1.0f
                        && compositeMesh.vertices()[secondElementUpVertex + 2] == 0.25f,
                "Multi-element upload should emit the upper post element separately");
        require(compositeMesh.vertices()[secondElementUpVertex + 3] == 0.5f
                        && compositeMesh.vertices()[secondElementUpVertex + 4] == 0.5f,
                "Multi-element upload should apply the upper element top-face UV rectangle independently");

        EchoClientTextureAtlas.BlockRenderPlan bottomSlab =
                atlas.planBlockModel(request("facetest:service_slab", Map.of("type", "bottom")));
        EchoClientTextureAtlas.BlockRenderPlan topSlab =
                atlas.planBlockModel(request("facetest:service_slab", Map.of("type", "top")));
        EchoClientTextureAtlas.BlockRenderPlan doubleSlab =
                atlas.planBlockModel(request("facetest:service_slab", Map.of("type", "double")));
        require(bottomSlab.resolved(), "Bottom slab blockstate/model should resolve into a chunk-render plan");
        require(bottomSlab.templateKind().equals("slab"),
                "Bottom slab render plan should detect the minecraft block/slab parent");
        require(topSlab.templateKind().equals("slab_top"),
                "Top slab render plan should detect the minecraft block/slab_top parent");
        require(doubleSlab.templateKind().equals("cube_all") && doubleSlab.modelBounds().fullCubeBounds(),
                "Double slab render plan should stay a full cube through the cube_all parent");
        require(bottomSlab.textureId(EchoVoxelMeshDirection.UP).orElse("").equals("facetest:block/service_slab_top"),
                "Bottom slab UP face should use the JSON top texture");
        require(bottomSlab.textureId(EchoVoxelMeshDirection.DOWN).orElse("").equals("facetest:block/service_slab_bottom"),
                "Bottom slab DOWN face should use the JSON bottom texture");
        require(bottomSlab.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_slab_side"),
                "Bottom slab side faces should use the JSON side texture");
        require(bottomSlab.modelBounds().fromY() == 0.0D && bottomSlab.modelBounds().toY() == 8.0D,
                "Bottom slab render plan should expose the lower half-cube model bounds");
        require(topSlab.modelBounds().fromY() == 8.0D && topSlab.modelBounds().toY() == 16.0D,
                "Top slab render plan should expose the upper half-cube model bounds");
        EchoVoxelMeshMaterial bottomSlabMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_slab",
                "facetest/service_slab",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("type", "bottom"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelMeshMaterial topSlabMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_slab",
                "facetest/service_slab",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("type", "top"),
                "",
                0xFFFFFFFF,
                false
        );
        float[] bottomSlabUp = EchoClientChunkMesh.cornerOffsets(
                EchoVoxelMeshDirection.UP,
                atlas.modelBounds(new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.UP, bottomSlabMaterial))
        );
        float[] topSlabDown = EchoClientChunkMesh.cornerOffsets(
                EchoVoxelMeshDirection.DOWN,
                atlas.modelBounds(new EchoVoxelMeshFace(0, 0, 0, EchoVoxelMeshDirection.DOWN, topSlabMaterial))
        );
        require(bottomSlabUp[1] == 0.5f && bottomSlabUp[4] == 0.5f
                        && bottomSlabUp[7] == 0.5f && bottomSlabUp[10] == 0.5f,
                "Chunk mesh upload should place bottom slab top faces at half block height");
        require(topSlabDown[1] == 0.5f && topSlabDown[4] == 0.5f
                        && topSlabDown[7] == 0.5f && topSlabDown[10] == 0.5f,
                "Chunk mesh upload should place top slab bottom faces at half block height");

        EchoClientTextureAtlas.BlockRenderPlan straightStairs =
                atlas.planBlockModel(request("facetest:service_stairs", Map.of(
                        "facing", "east",
                        "half", "bottom",
                        "shape", "straight"
                )));
        EchoClientTextureAtlas.BlockRenderPlan innerStairs =
                atlas.planBlockModel(request("facetest:service_stairs", Map.of(
                        "facing", "north",
                        "half", "top",
                        "shape", "inner_left"
                )));
        EchoClientTextureAtlas.BlockRenderPlan outerStairs =
                atlas.planBlockModel(request("facetest:service_stairs", Map.of(
                        "facing", "south",
                        "half", "bottom",
                        "shape", "outer_right"
                )));
        require(straightStairs.resolved() && straightStairs.templateKind().equals("stairs"),
                "Straight stair render plan should detect the minecraft block/stairs parent");
        require(innerStairs.resolved() && innerStairs.templateKind().equals("inner_stairs"),
                "Inner stair render plan should detect the minecraft block/inner_stairs parent");
        require(outerStairs.resolved() && outerStairs.templateKind().equals("outer_stairs"),
                "Outer stair render plan should detect the minecraft block/outer_stairs parent");
        require(straightStairs.textureId(EchoVoxelMeshDirection.UP).orElse("").equals("facetest:block/service_stairs_top"),
                "Stair UP face should use the JSON top texture");
        require(straightStairs.textureId(EchoVoxelMeshDirection.DOWN).orElse("").equals("facetest:block/service_stairs_bottom"),
                "Stair DOWN face should use the JSON bottom texture");
        require(straightStairs.textureId(EchoVoxelMeshDirection.WEST).orElse("").equals("facetest:block/service_stairs_side"),
                "Stair side faces should use the JSON side texture");
        EchoVoxelMeshMaterial straightStairMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_stairs",
                "facetest/service_stairs",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "east", "half", "bottom", "shape", "straight"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelChunkMesh straightStairSource = new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(0, 0, 0, direction, straightStairMaterial))
                        .toList(),
                1
        );
        EchoClientChunkMesh.MeshData straightStairMesh = EchoClientChunkMesh.meshData(straightStairSource, atlas);
        require(straightStairMesh.vertexCount() == 48,
                "Straight stair upload should replace six cube faces with two box sections");
        require(straightStairMesh.indexCount() == 72,
                "Straight stair upload should emit twelve textured quads worth of indices");
        require(straightStairMesh.vertices()[0] == 0.5f && straightStairMesh.vertices()[1] == 1.0f,
                "Straight east stair high section should occupy the east half of the block");
        int lowStairUpVertex = 24 * 12;
        require(straightStairMesh.vertices()[lowStairUpVertex] == 0.0f
                        && straightStairMesh.vertices()[lowStairUpVertex + 1] == 0.5f,
                "Straight east stair low section should keep its top face at half block height");

        EchoVoxelMeshMaterial innerStairMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_stairs",
                "facetest/service_stairs",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "north", "half", "top", "shape", "inner_left"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelMeshMaterial outerStairMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_stairs",
                "facetest/service_stairs",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "south", "half", "bottom", "shape", "outer_right"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData innerStairMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(1, 0, 0, direction, innerStairMaterial))
                        .toList(),
                1
        ), atlas);
        EchoClientChunkMesh.MeshData outerStairMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(2, 0, 0, direction, outerStairMaterial))
                        .toList(),
                1
        ), atlas);
        require(innerStairMesh.vertexCount() == 72 && innerStairMesh.indexCount() == 108,
                "Inner stair upload should emit three composed box sections");
        require(outerStairMesh.vertexCount() == 72 && outerStairMesh.indexCount() == 108,
                "Outer stair upload should emit three composed box sections");

        EchoClientTextureAtlas.BlockRenderPlan wallPost =
                atlas.planBlockModel(request("facetest:service_wall", Map.of(
                        "up", "true",
                        "north", "low",
                        "east", "tall"
                )));
        EchoClientTextureAtlas.BlockRenderPlan wallSide =
                atlas.planBlockModel(request("facetest:service_wall", Map.of(
                        "up", "false",
                        "north", "low"
                )));
        EchoClientTextureAtlas.BlockRenderPlan wallSideTall =
                atlas.planBlockModel(request("facetest:service_wall", Map.of(
                        "up", "false",
                        "east", "tall"
                )));
        require(wallPost.resolved() && wallPost.templateKind().equals("wall_post"),
                "Wall post render plan should detect the minecraft template_wall_post parent");
        require(wallSide.resolved() && wallSide.templateKind().equals("wall_side"),
                "Wall low-side render plan should detect the minecraft template_wall_side parent");
        require(wallSideTall.resolved() && wallSideTall.templateKind().equals("wall_side_tall"),
                "Wall tall-side render plan should detect the minecraft template_wall_side_tall parent");
        require(wallPost.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_wall"),
                "Wall render plan should use the JSON wall texture for side faces");
        EchoVoxelMeshMaterial wallMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_wall",
                "facetest/service_wall",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("up", "true", "north", "low", "east", "tall"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData wallMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(3, 0, 0, direction, wallMaterial))
                        .toList(),
                1
        ), atlas);
        require(wallMesh.vertexCount() == 72 && wallMesh.indexCount() == 108,
                "Wall upload should emit post plus low and tall side sections");
        require(wallMesh.vertices()[0] == 3.25f
                        && wallMesh.vertices()[1] == 1.0f
                        && wallMesh.vertices()[2] == 0.25f,
                "Wall post section should be centered inside the block");
        int lowWallArmUpVertex = 24 * 12;
        require(wallMesh.vertices()[lowWallArmUpVertex] == 3.3125f
                        && wallMesh.vertices()[lowWallArmUpVertex + 1] == 0.875f
                        && wallMesh.vertices()[lowWallArmUpVertex + 2] == 0.0f,
                "Wall low north arm should reach the north block edge below full height");
        int tallWallArmUpVertex = 48 * 12;
        require(wallMesh.vertices()[tallWallArmUpVertex] == 3.5f
                        && wallMesh.vertices()[tallWallArmUpVertex + 1] == 1.0f
                        && wallMesh.vertices()[tallWallArmUpVertex + 2] == 0.3125f,
                "Wall tall east arm should reach the east block edge at full height");

        EchoClientTextureAtlas.BlockRenderPlan fence =
                atlas.planBlockModel(request("facetest:service_fence", Map.of(
                        "north", "true",
                        "east", "true",
                        "south", "false",
                        "west", "false"
                )));
        EchoClientTextureAtlas.BlockRenderPlan fenceSideProbe =
                atlas.planBlockModel(request("facetest:service_fence_side_probe", Map.of()));
        require(fence.resolved() && fence.templateKind().equals("fence_post"),
                "Fence render plan should detect the minecraft block/fence_post parent");
        require(fenceSideProbe.resolved() && fenceSideProbe.templateKind().equals("fence_side"),
                "Fence side render plan should detect the minecraft block/fence_side parent");
        require(fence.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_fence"),
                "Fence render plan should use the JSON texture key for side faces");
        EchoVoxelMeshMaterial fenceMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_fence",
                "facetest/service_fence",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("north", "true", "east", "true", "south", "false", "west", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData fenceMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(4, 0, 0, direction, fenceMaterial))
                        .toList(),
                1
        ), atlas);
        require(fenceMesh.vertexCount() == 72 && fenceMesh.indexCount() == 108,
                "Fence upload should emit post plus two connected rail sections");
        require(fenceMesh.vertices()[0] == 4.375f
                        && fenceMesh.vertices()[1] == 1.0f
                        && fenceMesh.vertices()[2] == 0.375f,
                "Fence post section should be centered inside the block");
        int northFenceRailUpVertex = 24 * 12;
        require(fenceMesh.vertices()[northFenceRailUpVertex] == 4.4375f
                        && fenceMesh.vertices()[northFenceRailUpVertex + 1] == 0.9375f
                        && fenceMesh.vertices()[northFenceRailUpVertex + 2] == 0.0f,
                "Fence north rail should reach the north block edge below full height");
        int eastFenceRailUpVertex = 48 * 12;
        require(fenceMesh.vertices()[eastFenceRailUpVertex] == 4.4375f
                        && fenceMesh.vertices()[eastFenceRailUpVertex + 1] == 0.9375f
                        && fenceMesh.vertices()[eastFenceRailUpVertex + 2] == 0.4375f,
                "Fence east rail should start at the centered post and extend east");

        EchoClientTextureAtlas.BlockRenderPlan pane =
                atlas.planBlockModel(request("facetest:service_pane", Map.of(
                        "north", "true",
                        "east", "true",
                        "south", "false",
                        "west", "false"
                )));
        EchoClientTextureAtlas.BlockRenderPlan paneSideProbe =
                atlas.planBlockModel(request("facetest:service_pane_side_probe", Map.of()));
        EchoClientTextureAtlas.BlockRenderPlan paneNoSideProbe =
                atlas.planBlockModel(request("facetest:service_pane_noside_probe", Map.of()));
        require(pane.resolved() && pane.templateKind().equals("pane_post"),
                "Pane render plan should detect the minecraft template_glass_pane_post parent");
        require(paneSideProbe.resolved() && paneSideProbe.templateKind().equals("pane_side"),
                "Pane side render plan should detect the minecraft template_glass_pane_side parent");
        require(paneNoSideProbe.resolved() && paneNoSideProbe.templateKind().equals("pane_noside"),
                "Pane no-side render plan should detect the minecraft template_glass_pane_noside parent");
        require(pane.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_pane"),
                "Pane side faces should use the JSON pane texture");
        require(pane.textureId(EchoVoxelMeshDirection.UP).orElse("").equals("facetest:block/service_pane_edge"),
                "Pane vertical faces should use the JSON edge texture");
        EchoVoxelMeshMaterial paneMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_pane",
                "facetest/service_pane",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("north", "true", "east", "true", "south", "false", "west", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData paneMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(5, 0, 0, direction, paneMaterial))
                        .toList(),
                1
        ), atlas);
        require(paneMesh.vertexCount() == 48 && paneMesh.indexCount() == 72,
                "Pane upload should emit connected north and east thin panel sections");
        require(paneMesh.vertices()[0] == 5.4375f
                        && paneMesh.vertices()[1] == 1.0f
                        && paneMesh.vertices()[2] == 0.0f,
                "Pane north panel should be a thin vertical strip reaching the north edge");
        int eastPaneSectionUpVertex = 24 * 12;
        require(paneMesh.vertices()[eastPaneSectionUpVertex] == 5.4375f
                        && paneMesh.vertices()[eastPaneSectionUpVertex + 1] == 1.0f
                        && paneMesh.vertices()[eastPaneSectionUpVertex + 2] == 0.4375f,
                "Pane east panel should be a perpendicular thin strip starting at the center");
        EchoVoxelMeshMaterial isolatedPaneMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_pane",
                "facetest/service_pane",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("north", "false", "east", "false", "south", "false", "west", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData isolatedPaneMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(6, 0, 0, direction, isolatedPaneMaterial))
                        .toList(),
                1
        ), atlas);
        require(isolatedPaneMesh.vertexCount() == 48 && isolatedPaneMesh.indexCount() == 72,
                "Isolated pane upload should emit crossing no-connection panel strips");
        require(isolatedPaneMesh.vertices()[0] == 6.4375f
                        && isolatedPaneMesh.vertices()[2] == 0.0f
                        && isolatedPaneMesh.vertices()[14] == 1.0f,
                "Isolated pane north-south strip should span the full block depth");

        EchoClientTextureAtlas.BlockRenderPlan bottomTrapdoor =
                atlas.planBlockModel(request("facetest:service_trapdoor", Map.of(
                        "facing", "north",
                        "half", "bottom",
                        "open", "false"
                )));
        EchoClientTextureAtlas.BlockRenderPlan topTrapdoor =
                atlas.planBlockModel(request("facetest:service_trapdoor", Map.of(
                        "facing", "south",
                        "half", "top",
                        "open", "false"
                )));
        EchoClientTextureAtlas.BlockRenderPlan openTrapdoor =
                atlas.planBlockModel(request("facetest:service_trapdoor", Map.of(
                        "facing", "east",
                        "half", "bottom",
                        "open", "true"
                )));
        require(bottomTrapdoor.resolved() && bottomTrapdoor.templateKind().equals("trapdoor_bottom"),
                "Bottom trapdoor render plan should detect the minecraft trapdoor bottom parent");
        require(topTrapdoor.resolved() && topTrapdoor.templateKind().equals("trapdoor_top"),
                "Top trapdoor render plan should detect the minecraft trapdoor top parent");
        require(openTrapdoor.resolved() && openTrapdoor.templateKind().equals("trapdoor_open"),
                "Open trapdoor render plan should detect the minecraft trapdoor open parent");
        require(bottomTrapdoor.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_trapdoor"),
                "Trapdoor faces should use the JSON texture key");
        require(bottomTrapdoor.modelBounds().fromY() == 0.0D && bottomTrapdoor.modelBounds().toY() == 3.0D,
                "Bottom trapdoor render plan should expose the lower thin-panel bounds");
        require(topTrapdoor.modelBounds().fromY() == 13.0D && topTrapdoor.modelBounds().toY() == 16.0D,
                "Top trapdoor render plan should expose the upper thin-panel bounds");
        EchoVoxelMeshMaterial bottomTrapdoorMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_trapdoor",
                "facetest/service_trapdoor",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "north", "half", "bottom", "open", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelMeshMaterial topTrapdoorMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_trapdoor",
                "facetest/service_trapdoor",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "south", "half", "top", "open", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelMeshMaterial openTrapdoorMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_trapdoor",
                "facetest/service_trapdoor",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "east", "half", "bottom", "open", "true"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData bottomTrapdoorMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(7, 0, 0, direction, bottomTrapdoorMaterial))
                        .toList(),
                1
        ), atlas);
        EchoClientChunkMesh.MeshData topTrapdoorMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(8, 0, 0, direction, topTrapdoorMaterial))
                        .toList(),
                1
        ), atlas);
        EchoClientChunkMesh.MeshData openTrapdoorMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(9, 0, 0, direction, openTrapdoorMaterial))
                        .toList(),
                1
        ), atlas);
        require(bottomTrapdoorMesh.vertexCount() == 24 && bottomTrapdoorMesh.indexCount() == 36,
                "Bottom trapdoor upload should emit one thin box section");
        require(topTrapdoorMesh.vertexCount() == 24 && topTrapdoorMesh.indexCount() == 36,
                "Top trapdoor upload should emit one thin box section");
        require(openTrapdoorMesh.vertexCount() == 24 && openTrapdoorMesh.indexCount() == 36,
                "Open trapdoor upload should emit one vertical thin box section");
        require(bottomTrapdoorMesh.vertices()[0] == 7.0f
                        && bottomTrapdoorMesh.vertices()[1] == 0.1875f
                        && bottomTrapdoorMesh.vertices()[2] == 0.0f,
                "Bottom trapdoor upload should keep the panel in the lower three pixels");
        require(topTrapdoorMesh.vertices()[0] == 8.0f
                        && topTrapdoorMesh.vertices()[1] == 1.0f
                        && topTrapdoorMesh.vertices()[2] == 0.0f,
                "Top trapdoor upload should keep the panel against the top of the block");
        require(openTrapdoorMesh.vertices()[0] == 9.8125f
                        && openTrapdoorMesh.vertices()[1] == 1.0f
                        && openTrapdoorMesh.vertices()[2] == 0.0f,
                "Open east-facing trapdoor upload should rotate the panel upright at the east edge");

        EchoClientTextureAtlas.BlockRenderPlan bottomDoor =
                atlas.planBlockModel(request("facetest:service_door", Map.of(
                        "facing", "north",
                        "half", "lower",
                        "hinge", "left",
                        "open", "false"
                )));
        EchoClientTextureAtlas.BlockRenderPlan topDoor =
                atlas.planBlockModel(request("facetest:service_door", Map.of(
                        "facing", "south",
                        "half", "upper",
                        "hinge", "right",
                        "open", "false"
                )));
        EchoClientTextureAtlas.BlockRenderPlan openDoor =
                atlas.planBlockModel(request("facetest:service_door", Map.of(
                        "facing", "east",
                        "half", "lower",
                        "hinge", "left",
                        "open", "true"
                )));
        EchoClientTextureAtlas.BlockRenderPlan openTopDoor =
                atlas.planBlockModel(request("facetest:service_door", Map.of(
                        "facing", "west",
                        "half", "upper",
                        "hinge", "right",
                        "open", "true"
                )));
        require(bottomDoor.resolved() && bottomDoor.templateKind().equals("door_bottom_left"),
                "Lower closed door render plan should detect the minecraft door_bottom_left parent");
        require(topDoor.resolved() && topDoor.templateKind().equals("door_top_right"),
                "Upper closed door render plan should detect the minecraft door_top_right parent");
        require(openDoor.resolved() && openDoor.templateKind().equals("door_bottom_left_open"),
                "Lower open door render plan should detect the minecraft door_bottom_left_open parent");
        require(openTopDoor.resolved() && openTopDoor.templateKind().equals("door_top_right_open"),
                "Upper open door render plan should detect the minecraft door_top_right_open parent");
        require(bottomDoor.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_door_bottom"),
                "Lower door faces should use the JSON bottom texture");
        require(topDoor.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/service_door_top"),
                "Upper door faces should use the JSON top texture");
        require(bottomDoor.modelBounds().fromZ() == 0.0D && bottomDoor.modelBounds().toZ() == 3.0D,
                "Door render plan should expose the default thin-panel bounds");
        EchoVoxelMeshMaterial bottomDoorMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_door",
                "facetest/service_door",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelMeshMaterial topDoorMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_door",
                "facetest/service_door",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoVoxelMeshMaterial openDoorMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:service_door",
                "facetest/service_door",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                true,
                Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true"),
                "",
                0xFFFFFFFF,
                false
        );
        EchoClientChunkMesh.MeshData bottomDoorMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(10, 0, 0, direction, bottomDoorMaterial))
                        .toList(),
                1
        ), atlas);
        EchoClientChunkMesh.MeshData topDoorMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(11, 0, 0, direction, topDoorMaterial))
                        .toList(),
                1
        ), atlas);
        EchoClientChunkMesh.MeshData openDoorMesh = EchoClientChunkMesh.meshData(new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                        .map(direction -> new EchoVoxelMeshFace(12, 0, 0, direction, openDoorMaterial))
                        .toList(),
                1
        ), atlas);
        require(bottomDoorMesh.vertexCount() == 24 && bottomDoorMesh.indexCount() == 36,
                "Lower closed door upload should emit one vertical thin box section");
        require(topDoorMesh.vertexCount() == 24 && topDoorMesh.indexCount() == 36,
                "Upper closed door upload should emit one vertical thin box section");
        require(openDoorMesh.vertexCount() == 24 && openDoorMesh.indexCount() == 36,
                "Open door upload should emit one rotated vertical thin box section");
        require(bottomDoorMesh.vertices()[0] == 10.0f
                        && bottomDoorMesh.vertices()[1] == 1.0f
                        && bottomDoorMesh.vertices()[2] == 0.0f
                        && bottomDoorMesh.vertices()[14] == 0.1875f,
                "Closed north-facing lower door should occupy the north edge");
        require(topDoorMesh.vertices()[0] == 11.0f
                        && topDoorMesh.vertices()[1] == 1.0f
                        && topDoorMesh.vertices()[2] == 0.8125f
                        && topDoorMesh.vertices()[14] == 1.0f,
                "Closed south-facing upper door should occupy the south edge");
        require(openDoorMesh.vertices()[0] == 12.0f
                        && openDoorMesh.vertices()[1] == 1.0f
                        && openDoorMesh.vertices()[2] == 0.0f,
                "Open east-facing left-hinge door should rotate to the north edge");

        EchoClientTextureAtlas.BlockRenderPlan cross =
                atlas.planBlockModel(request("facetest:dead_fern", Map.of()));
        require(cross.resolved(), "Cross blockstate/model should resolve into a chunk-render plan");
        require(cross.templateKind().equals("cross"),
                "Cross render plan should detect the minecraft block/cross parent");
        require(cross.textureId(EchoVoxelMeshDirection.NORTH).orElse("").equals("facetest:block/dead_fern"),
                "Cross render plan should use the JSON cross texture for all faces");
        EchoVoxelMeshMaterial crossMaterial = new EchoVoxelMeshMaterial(
                "voxel:block/facetest:dead_fern",
                "facetest/dead_fern",
                0xFFFFFFFF,
                0xFFFFFFFF,
                EchoVoxelMaterialPattern.FLAT,
                false,
                Map.of(),
                "",
                0xFFFFFFFF,
                false
        );
        List<EchoVoxelMeshFace> crossSourceFaces = java.util.Arrays.stream(EchoVoxelMeshDirection.values())
                .map(direction -> new EchoVoxelMeshFace(0, 0, 0, direction, crossMaterial))
                .toList();
        EchoVoxelChunkMesh crossSource = new EchoVoxelChunkMesh(
                new EchoVoxelChunkId(0, 0, 0),
                EchoVoxelRenderBackendTarget.OPENGL,
                crossSourceFaces,
                1
        );
        EchoClientChunkMesh.MeshData crossMesh = EchoClientChunkMesh.meshData(crossSource, atlas);
        require(crossMesh.vertexCount() == 16,
                "Cross chunk upload should collapse repeated cube-side records into two double-sided quads");
        require(crossMesh.indexCount() == 24,
                "Cross chunk upload should emit four textured quads worth of indices");
        require(crossMesh.vertices()[0] == 0.0f
                        && crossMesh.vertices()[12] == 1.0f
                        && crossMesh.vertices()[14] == 1.0f
                        && crossMesh.vertices()[24] == 1.0f
                        && crossMesh.vertices()[25] == 1.0f
                        && crossMesh.vertices()[26] == 1.0f,
                "Cross chunk upload should emit a diagonal vertical plane through the block");

        System.out.println("client block model chunk render smoke PASS model=" + trimmed.modelId()
                + " bounds=" + bounds.fromX() + "," + bounds.fromY() + "," + bounds.fromZ()
                + "->" + bounds.toX() + "," + bounds.toY() + "," + bounds.toZ());
    }

    private static EchoClientTextureAtlas.BlockModelRequest request(
            String blockId,
            Map<String, String> stateProperties
    ) {
        String atlasKey = blockId.replace(':', '/');
        return new EchoClientTextureAtlas.BlockModelRequest(blockId, stateProperties, atlasKey);
    }

    private static void write(Path path, String text) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, text);
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static boolean close(float value, double expected) {
        return Math.abs(value - expected) < 0.0002D;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
