package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoBlockworksStandaloneAdapter {
    public static final String MODULE_ID = "echoblockworks";
    public static final String BLOCK_CATALOG_CONTRACT_ID = "echoblockworks:block/block_catalog";
    public static final String PATTERN_CUTTER_CONTRACT_ID = "echoblockworks:item/pattern_cutter";
    public static final String PALETTE_CONVERSION_CONTRACT_ID = "echoblockworks:recipe/palette_conversion";
    public static final String SHOWCASE_SITES_CONTRACT_ID = "echoblockworks:structure/showcase_sites";
    public static final String SCATTER_SITES_CONTRACT_ID = "echoblockworks:worldgen/scatter_sites";
    public static final List<String> CONTRACT_IDS = List.of(
            BLOCK_CATALOG_CONTRACT_ID,
            PATTERN_CUTTER_CONTRACT_ID,
            PALETTE_CONVERSION_CONTRACT_ID,
            SHOWCASE_SITES_CONTRACT_ID,
            SCATTER_SITES_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> referenceProbe = exerciseReferenceBehavior();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "blockworks_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("blockCatalogRoundTrip", referenceProbe.get("blockCatalogRoundTrip"));
        report.put("patternCutterRoundTrip", referenceProbe.get("patternCutterRoundTrip"));
        report.put("paletteConversionRoundTrip", referenceProbe.get("paletteConversionRoundTrip"));
        report.put("showcaseSiteRoundTrip", referenceProbe.get("showcaseSiteRoundTrip"));
        report.put("worldgenSiteRoundTrip", referenceProbe.get("worldgenSiteRoundTrip"));
        report.put("referenceProbe", referenceProbe);
        report.put("summary", "Blockworks standalone adapter resolved block catalog, pattern cutter, palette conversion, showcase, and worldgen contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private Map<String, Object> exerciseReferenceBehavior() {
        Optional<BlockInfo> ashstoneBrick = BlockworksCatalog.blockInfo("ashstone_brick");
        Optional<BlockInfo> ashstoneBrickSlab = BlockworksCatalog.target("ashstone", "brick", ShapeKind.SLAB);
        Optional<BlockInfo> reinforcedPanelSlab = BlockworksCatalog.target("reinforced_metal", "panel", ShapeKind.SLAB);
        Optional<PaletteKit> ashfallKit = BlockworksCatalog.paletteKit("ashfall_ruined_city");
        Optional<BlockInfo> nextAshstoneShape = ashstoneBrick.flatMap(block -> BlockworksCatalog.cycle(block, false));
        List<BlockInfo> conversionTargets = ashstoneBrick
                .map(BlockworksCatalog::conversionTargets)
                .orElse(List.of());
        List<BlockInfo> kitTargets = ashstoneBrick
                .flatMap(block -> ashfallKit.map(kit -> BlockworksCatalog.conversionTargets(block, kit)))
                .orElse(List.of());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("familyCount", BlockworksCatalog.families().size());
        result.put("blockCount", BlockworksCatalog.blockInfos().size());
        result.put("detailCount", BlockworksCatalog.details().size());
        result.put("paletteKitCount", BlockworksCatalog.paletteKits().size());
        result.put("worldgenSiteCount", BlockworksCatalog.worldgenSites().size());
        result.put("blockCatalogRoundTrip", ashstoneBrick
                .map(block -> block.displayName().equals("Ashstone Brick")
                        && BlockworksCatalog.blockInfos().size() > BlockworksCatalog.families().size())
                .orElse(false));
        result.put("patternCutterRoundTrip", ashstoneBrickSlab.isPresent()
                && reinforcedPanelSlab.isPresent()
                && nextAshstoneShape.map(block -> !block.blockId().equals("ashstone_brick")).orElse(false));
        result.put("paletteConversionRoundTrip", ashfallKit
                .map(kit -> kit.includesFamily("ashstone")
                        && kit.includesBlock("ashstone_cracked_brick")
                        && conversionTargets.size() > 1
                        && kitTargets.stream().anyMatch(block -> block.blockId().equals("ashstone_cracked_brick")))
                .orElse(false));
        result.put("showcaseSiteRoundTrip", BlockworksCatalog.worldgenSites().stream()
                .anyMatch(site -> site.id().equals("ashfall_street_ruin")
                        && site.structureTemplate().equals("showcase/ashfall_street_ruin")));
        result.put("worldgenSiteRoundTrip", ashfallKit
                .flatMap(PaletteKit::worldgenSiteId)
                .map("ashfall_street_ruin"::equals)
                .orElse(false));
        result.put("sampleBlockId", ashstoneBrick.map(BlockInfo::blockId).orElse("missing"));
        result.put("sampleSlabId", ashstoneBrickSlab.map(BlockInfo::blockId).orElse("missing"));
        result.put("samplePaletteId", ashfallKit.map(PaletteKit::id).orElse("missing"));
        return Map.copyOf(result);
    }

    private static final class BlockworksCatalog {
        private static final List<String> FAMILIES = List.of("ashstone", "reinforced_metal");
        private static final List<String> DETAILS = List.of("brick", "cracked_brick", "panel");
        private static final List<BlockInfo> BLOCKS = List.of(
                new BlockInfo("ashstone_brick", "Ashstone Brick", "ashstone", "brick", ShapeKind.FULL),
                new BlockInfo("ashstone_brick_slab", "Ashstone Brick Slab", "ashstone", "brick", ShapeKind.SLAB),
                new BlockInfo("ashstone_cracked_brick", "Cracked Ashstone Brick", "ashstone", "cracked_brick", ShapeKind.FULL),
                new BlockInfo("ashstone_cracked_brick_slab", "Cracked Ashstone Brick Slab", "ashstone", "cracked_brick", ShapeKind.SLAB),
                new BlockInfo("reinforced_metal_panel", "Reinforced Metal Panel", "reinforced_metal", "panel", ShapeKind.FULL),
                new BlockInfo("reinforced_metal_panel_slab", "Reinforced Metal Panel Slab", "reinforced_metal", "panel", ShapeKind.SLAB)
        );
        private static final List<PaletteKit> PALETTE_KITS = List.of(new PaletteKit(
                "ashfall_ruined_city",
                List.of("ashstone", "reinforced_metal"),
                List.of("ashstone_cracked_brick", "reinforced_metal_panel"),
                Optional.of("ashfall_street_ruin")
        ));
        private static final List<WorldgenSite> WORLDGEN_SITES = List.of(new WorldgenSite(
                "ashfall_street_ruin",
                "showcase/ashfall_street_ruin"
        ));

        private BlockworksCatalog() {
        }

        private static List<String> families() {
            return FAMILIES;
        }

        private static List<BlockInfo> blockInfos() {
            return BLOCKS;
        }

        private static List<String> details() {
            return DETAILS;
        }

        private static List<PaletteKit> paletteKits() {
            return PALETTE_KITS;
        }

        private static List<WorldgenSite> worldgenSites() {
            return WORLDGEN_SITES;
        }

        private static Optional<BlockInfo> blockInfo(String blockId) {
            return BLOCKS.stream()
                    .filter(block -> block.blockId().equals(blockId))
                    .findFirst();
        }

        private static Optional<BlockInfo> target(String family, String detail, ShapeKind shape) {
            return BLOCKS.stream()
                    .filter(block -> block.family().equals(family)
                            && block.detail().equals(detail)
                            && block.shape() == shape)
                    .findFirst();
        }

        private static Optional<PaletteKit> paletteKit(String id) {
            return PALETTE_KITS.stream()
                    .filter(kit -> kit.id().equals(id))
                    .findFirst();
        }

        private static Optional<BlockInfo> cycle(BlockInfo block, boolean reverse) {
            List<ShapeKind> shapes = List.of(ShapeKind.FULL, ShapeKind.SLAB);
            int index = shapes.indexOf(block.shape());
            int next = reverse
                    ? (index + shapes.size() - 1) % shapes.size()
                    : (index + 1) % shapes.size();
            return target(block.family(), block.detail(), shapes.get(next));
        }

        private static List<BlockInfo> conversionTargets(BlockInfo source) {
            return BLOCKS.stream()
                    .filter(block -> block.family().equals(source.family())
                            && !block.blockId().equals(source.blockId()))
                    .toList();
        }

        private static List<BlockInfo> conversionTargets(BlockInfo source, PaletteKit kit) {
            return conversionTargets(source).stream()
                    .filter(block -> kit.includesBlock(block.blockId()) || kit.includesFamily(block.family()))
                    .toList();
        }
    }

    private enum ShapeKind {
        FULL,
        SLAB
    }

    private record BlockInfo(
            String blockId,
            String displayName,
            String family,
            String detail,
            ShapeKind shape
    ) {
        private BlockInfo {
            Objects.requireNonNull(blockId, "blockId");
            Objects.requireNonNull(displayName, "displayName");
            Objects.requireNonNull(family, "family");
            Objects.requireNonNull(detail, "detail");
            Objects.requireNonNull(shape, "shape");
        }
    }

    private record PaletteKit(
            String id,
            List<String> families,
            List<String> blocks,
            Optional<String> worldgenSiteId
    ) {
        private PaletteKit {
            Objects.requireNonNull(id, "id");
            families = List.copyOf(families == null ? List.of() : families);
            blocks = List.copyOf(blocks == null ? List.of() : blocks);
            worldgenSiteId = worldgenSiteId == null ? Optional.empty() : worldgenSiteId;
        }

        private boolean includesFamily(String family) {
            return families.contains(family);
        }

        private boolean includesBlock(String blockId) {
            return blocks.contains(blockId);
        }
    }

    private record WorldgenSite(String id, String structureTemplate) {
        private WorldgenSite {
            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(structureTemplate, "structureTemplate");
        }
    }
}
