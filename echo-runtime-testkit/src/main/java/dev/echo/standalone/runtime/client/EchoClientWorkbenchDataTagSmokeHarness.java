package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCraftResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientWorkbenchDataTagSmokeHarness {
    private static final String PACK_ID = "client-workbench-data-tag-smoke";
    private static final String RECIPE_ID = "smoketag:tagged_salvage_plate";
    private static final String SCRAP_ID = "echoashfallprotocol:scrap_metal";
    private static final String OUTPUT_ID = "smoketag:tagged_salvage_plate";

    private EchoClientWorkbenchDataTagSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path packRoot = Path.of("resourcepacks", PACK_ID).toAbsolutePath();
        deleteRecursively(packRoot);
        writeFixturePack(packRoot);
        try {
            EchoClientRuntimeServices services = new EchoClientRuntimeServices();
            require(services.resourcePackSummaries().stream().anyMatch(pack -> pack.id().equals(PACK_ID)),
                    "Client resource pack service should mount the workbench data-tag fixture pack");
            services.startNewWorld("workbench-data-tag-smoke");
            require(services.session().quickMoveContainerSlotToPlayer(1).success(),
                    "Workbench data-tag smoke should move tagged scrap from the crash cache");

            EchoClientWorkbenchRecipeSummary summary = services.workbenchRecipeSummaries().stream()
                    .filter(recipe -> recipe.recipeId().equals(RECIPE_ID))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Workbench should expose a recipe backed by a data item tag"));
            require(summary.label().equals("Tagged Salvage Plate"),
                    "Tagged recipe should infer a readable output item label");
            require(summary.craftable(),
                    "Tagged recipe should be craftable once the tagged scrap item is in player inventory");

            EchoClientWorkbenchScreenModel model = services.workbenchScreenModel(RECIPE_ID);
            require(model != null, "Tagged recipe should expose a workbench detail model");
            require(model.selectedRecipe().ingredients().stream().anyMatch(slot -> slot.runtimeId().equals(SCRAP_ID)),
                    "Workbench detail should resolve #smoketag:salvage_inputs to scrap_metal");
            require(model.selectedRecipe().output().runtimeId().equals(OUTPUT_ID),
                    "Workbench detail should expose the inferred tagged recipe output");

            EchoItemCraftResult crafted = services.craftWorkbenchRecipe(RECIPE_ID);
            require(crafted != null && crafted.crafted(),
                    "Workbench should craft the datapack recipe resolved through an item tag");
            require(services.inventoryScreenModel().slots().stream()
                            .anyMatch(slot -> slot.runtimeId().equals(OUTPUT_ID) && slot.count() >= 1),
                    "Crafting the tagged recipe should add the inferred output item to inventory");
            System.out.println("client workbench data-tag smoke PASS recipe=" + RECIPE_ID
                    + " input=" + SCRAP_ID
                    + " output=" + OUTPUT_ID);
        } finally {
            deleteRecursively(packRoot);
        }
    }

    private static void writeFixturePack(Path packRoot) throws IOException {
        write(packRoot.resolve("pack.mcmeta"), """
                {
                  "pack": {
                    "pack_format": 15,
                    "description": "Client workbench data tag smoke"
                  }
                }
                """);
        write(packRoot.resolve("assets/smoketag/lang/en_us.json"), """
                {
                  "item.smoketag.tagged_salvage_plate": "Tagged Salvage Plate"
                }
                """);
        write(packRoot.resolve("data/smoketag/tags/items/salvage_inputs.json"), """
                {
                  "values": ["echoashfallprotocol:scrap_metal"]
                }
                """);
        write(packRoot.resolve("data/smoketag/recipes/tagged_salvage_plate.json"), """
                {
                  "type": "minecraft:crafting_shapeless",
                  "ingredients": [
                    { "tag": "smoketag:salvage_inputs" },
                    { "tag": "smoketag:salvage_inputs" }
                  ],
                  "result": {
                    "id": "smoketag:tagged_salvage_plate",
                    "count": 1
                  }
                }
                """);
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
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
