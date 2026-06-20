package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemDefinitionInference;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Runtime-owned creative inventory catalog used by Standalone client smokes and UI controllers.
 */
final class EchoClientCreativeInventoryController {
    static final String CREATIVE_RUNTIME_ITEM_TAG = "creative_runtime_item";

    CreativeInventoryModel model(List<CreativeTab> tabs) {
        return new CreativeInventoryModel(tabs);
    }

    CreativeInventoryModel modelFromRuntimeContentRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return model(List.of());
        }
        LinkedHashMap<String, CreativeTabBuilder> tabs = new LinkedHashMap<>();
        LinkedHashMap<String, CreativeEntry> entries = new LinkedHashMap<>();
        LinkedHashMap<String, LinkedHashSet<String>> explicitTabItemIds = new LinkedHashMap<>();

        for (Map<String, Object> row : rows) {
            Map<String, Object> metadata = map(row.get("metadata"));
            String contentId = firstText(row.get("contentId"), metadata.get("contentGraphId"));
            String domain = firstText(row.get("domain"), metadata.get("domain"));
            String contentKind = firstText(row.get("contentKind"), metadata.get("contentKind"));
            String graphKind = firstText(metadata.get("contentGraphKind"), contentKind);
            String moduleId = firstText(row.get("moduleId"), metadata.get("moduleId"), moduleFromContentId(contentId));
            String displayName = firstText(row.get("displayName"), metadata.get("displayName"), contentId);

            if (creativeTabRow(graphKind, domain, metadata)) {
                String tabId = normalizeQualified(contentId, moduleId + ":content_graph");
                CreativeTabBuilder builder = tabs.computeIfAbsent(
                        tabId,
                        ignored -> new CreativeTabBuilder(
                                moduleId,
                                tabId,
                                firstText(metadata.get("titleKey"), displayName)
                        )
                );
                builder.titleKey(firstText(metadata.get("titleKey"), displayName, builder.titleKey()));
                for (String itemId : stringList(metadata.get("itemIds"))) {
                    explicitTabItemIds.computeIfAbsent(tabId, ignored -> new LinkedHashSet<>())
                            .add(normalizeQualified(itemId, moduleId + ":creative_entry"));
                }
                continue;
            }

            if (!creativeEntryRow(contentKind, domain, graphKind)) {
                continue;
            }
            CreativeEntry entry = new CreativeEntry(
                    moduleId,
                    contentId,
                    displayName,
                    blockEntry(contentKind, domain, graphKind),
                    true
            );
            entries.putIfAbsent(entry.itemId(), entry);
        }

        for (Map<String, Object> row : rows) {
            Map<String, Object> metadata = map(row.get("metadata"));
            String contentId = normalizeQualified(
                    firstText(row.get("contentId"), metadata.get("contentGraphId")),
                    firstText(row.get("moduleId"), metadata.get("moduleId"), "echo") + ":creative_entry"
            );
            CreativeEntry entry = entries.get(contentId);
            if (entry == null) {
                continue;
            }
            for (String tabId : stringList(metadata.get("creativeTabs"))) {
                CreativeTabBuilder builder = tabs.computeIfAbsent(
                        normalizeQualified(tabId, entry.moduleId() + ":content_graph"),
                        normalizedTabId -> new CreativeTabBuilder(
                                moduleFromContentId(normalizedTabId).isBlank()
                                        ? entry.moduleId()
                                        : moduleFromContentId(normalizedTabId),
                                normalizedTabId,
                                displayNameFromId(normalizedTabId)
                        )
                );
                builder.add(entry);
            }
        }

        for (Map.Entry<String, LinkedHashSet<String>> itemIdsByTab : explicitTabItemIds.entrySet()) {
            CreativeTabBuilder builder = tabs.get(itemIdsByTab.getKey());
            if (builder == null) {
                continue;
            }
            for (String itemId : itemIdsByTab.getValue()) {
                CreativeEntry entry = entries.get(itemId);
                if (entry != null) {
                    builder.add(entry);
                }
            }
        }

        return model(tabs.values().stream()
                .map(CreativeTabBuilder::build)
                .toList());
    }

    CreativeSelectionResult selectEntry(
            EchoClientGameSession session,
            CreativeEntry entry,
            int slotIndex
    ) {
        if (session == null) {
            return CreativeSelectionResult.failed("", "", "missing_session");
        }
        if (entry == null) {
            return CreativeSelectionResult.failed("", "", "missing_entry");
        }
        int slot = Math.max(0, Math.min(EchoVoxelPlayerHotbar.HOTBAR_COUNT - 1, slotIndex));
        EchoItemDefinition definition = itemDefinition(entry);
        session.playerInventory().slot(slot).setStack(new EchoItemStack(definition, 1));
        if (entry.block()) {
            session.hotbar().assignSlot(slot, blockDefinition(entry), 1);
        } else {
            session.hotbar().assignSlot(slot, EchoVoxelBlock.AIR, 0);
        }
        session.hotbar().select(slot);
        session.player().selectSlot(slot);

        EchoClientSlotStack inventorySlot = session.inventoryScreenModel().slot(slot);
        boolean inventoryBacked = inventorySlot.runtimeId().equals(entry.itemId());
        boolean hotbarBacked = entry.block()
                ? session.hotbar().slot(slot).block().id().equals(entry.itemId())
                : session.hotbar().selectedSlot() == slot;
        boolean selected = inventoryBacked && hotbarBacked && session.player().state().selectedSlot() == slot;
        return new CreativeSelectionResult(
                entry.moduleId(),
                entry.itemId(),
                selected,
                inventoryBacked,
                hotbarBacked,
                selected ? "" : "selected_entry_not_reflected_in_inventory_or_hotbar"
        );
    }

    CreativePlayResult useSelectedEntry(
            EchoClientGameSession session,
            EchoClientGameplay gameplay,
            CreativeEntry entry,
            int slotIndex
    ) {
        if (session == null || gameplay == null || entry == null) {
            return CreativePlayResult.failed("", "", "missing_session_or_entry");
        }
        int slot = Math.max(0, Math.min(EchoVoxelPlayerHotbar.HOTBAR_COUNT - 1, slotIndex));
        if (entry.block()) {
            EchoVoxelBlock anchor = new EchoVoxelBlock(
                    "echo:creative_inventory_anchor",
                    "Creative Inventory Anchor",
                    0xFF6F7682,
                    true,
                    true,
                    1.0D
            );
            if (!placeAnchorOnLookRay(session, anchor)) {
                return CreativePlayResult.failed(entry.moduleId(), entry.itemId(), "missing_place_anchor");
            }
            gameplay.tick(EchoVoxelPlayerInput.idle(), new PlaceOnceInput(slot), 0.02D, session);
            List<String> feedback = gameplay.consumeFeedbackEvents().stream()
                    .map(event -> event.kind() + ":" + event.sourceId())
                    .toList();
            boolean placed = gameplay.isWorldDirty()
                    && feedback.stream().anyMatch(value -> value.equals("BLOCK_PLACE:" + entry.itemId()));
            return new CreativePlayResult(
                    entry.moduleId(),
                    entry.itemId(),
                    placed,
                    placed ? "block_place" : "block_place_missing",
                    gameplay.isWorldDirty(),
                    gameplay.dirtyChunkCount(),
                    feedback,
                    placed ? "" : "selected_block_did_not_place"
            );
        }

        int beforeExperience = session.progressionState().experience();
        gameplay.tick(EchoVoxelPlayerInput.idle(), new PlaceOnceInput(slot), 0.02D, session);
        EchoClientSelectedItemUse itemUse = gameplay.consumeSelectedItemUse();
        int afterExperience = session.progressionState().experience();
        boolean activated = itemUse.active() && afterExperience > beforeExperience;
        return new CreativePlayResult(
                entry.moduleId(),
                entry.itemId(),
                activated,
                activated ? "creative_item_activate" : "creative_item_activation_missing",
                false,
                0,
                List.of(itemUse.action() + ":" + itemUse.label()),
                activated ? "" : "selected_item_did_not_activate"
        );
    }

    static boolean isCreativeRuntimeItem(EchoItemDefinition definition) {
        return definition != null && definition.tagged(CREATIVE_RUNTIME_ITEM_TAG);
    }

    static EchoItemDefinition itemDefinition(CreativeEntry entry) {
        Objects.requireNonNull(entry, "entry");
        EchoItemCategory category = entry.block()
                ? EchoItemCategory.MATERIAL
                : EchoItemDefinitionInference.inferCategory(entry.itemId());
        ArrayList<String> tags = new ArrayList<>();
        tags.add("creative");
        tags.add(CREATIVE_RUNTIME_ITEM_TAG);
        tags.add(entry.moduleId());
        tags.add(entry.block() ? "block" : "item");
        return new EchoItemDefinition(
                new EchoItemId(entry.itemId()),
                entry.displayName(),
                category,
                64,
                1.0D,
                tags,
                List.of("Creative inventory entry", entry.block() ? "Placeable voxel block" : "Runtime item")
        );
    }

    static EchoVoxelBlock blockDefinition(CreativeEntry entry) {
        Objects.requireNonNull(entry, "entry");
        return new EchoVoxelBlock(
                entry.itemId(),
                entry.displayName(),
                colorFor(entry.itemId()),
                true,
                true,
                1.0D
        );
    }

    private static boolean placeAnchorOnLookRay(EchoClientGameSession session, EchoVoxelBlock targetBlock) {
        EchoVoxelPlayerState state = session.player().state();
        double yawRadians = Math.toRadians(state.yawDegrees());
        double pitchRadians = Math.toRadians(state.pitchDegrees());
        double horizontal = Math.cos(pitchRadians);
        double directionX = Math.sin(yawRadians) * horizontal;
        double directionY = Math.sin(pitchRadians);
        double directionZ = Math.cos(yawRadians) * horizontal;
        for (double distance = 2.0D; distance <= state.reach() - 0.25D; distance += 0.25D) {
            int x = (int) Math.floor(state.x() + directionX * distance);
            int y = (int) Math.floor(state.eyeY() + directionY * distance);
            int z = (int) Math.floor(state.z() + directionZ * distance);
            if (state.intersectsBlock(x, y, z)) {
                continue;
            }
            if (session.world().setBlockStateAt(x, y, z, EchoVoxelBlockState.of(targetBlock))) {
                return true;
            }
        }
        return false;
    }

    private static int colorFor(String id) {
        int hash = Math.abs(Objects.requireNonNull(id, "id").hashCode());
        int red = 80 + hash % 120;
        int green = 80 + (hash / 17) % 120;
        int blue = 80 + (hash / 31) % 120;
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    record CreativeInventoryModel(List<CreativeTab> tabs) {
        CreativeInventoryModel {
            tabs = tabs == null ? List.of() : List.copyOf(tabs);
        }

        List<CreativeEntry> entries() {
            return tabs.stream()
                    .flatMap(tab -> tab.entries().stream())
                    .toList();
        }

        List<CreativeEntry> entriesForModule(String moduleId) {
            String normalized = normalize(moduleId);
            return entries().stream()
                    .filter(entry -> entry.moduleId().equals(normalized))
                    .toList();
        }

        List<CreativeEntry> search(String query) {
            String normalized = normalize(query);
            if (normalized.isBlank()) {
                return entries();
            }
            return entries().stream()
                    .filter(CreativeEntry::searchable)
                    .filter(entry -> entry.moduleId().contains(normalized)
                            || entry.itemId().contains(normalized)
                            || entry.displayName().toLowerCase(Locale.ROOT).contains(normalized))
                    .toList();
        }

        boolean visibleParent(String moduleId) {
            return !entriesForModule(moduleId).isEmpty();
        }

        boolean visibleSearch(String moduleId) {
            String normalized = normalize(moduleId);
            return search(normalized).stream().anyMatch(entry -> entry.moduleId().equals(normalized));
        }
    }

    record CreativeTab(
            String moduleId,
            String tabId,
            String titleKey,
            List<CreativeEntry> entries,
            boolean searchExpected
    ) {
        CreativeTab {
            moduleId = normalize(moduleId);
            tabId = normalizeQualified(tabId, moduleId + ":native_modules");
            titleKey = titleKey == null ? "" : titleKey.trim();
            entries = entries == null ? List.of() : List.copyOf(entries);
        }
    }

    record CreativeEntry(
            String moduleId,
            String itemId,
            String displayName,
            boolean block,
            boolean searchable
    ) {
        CreativeEntry {
            moduleId = normalize(moduleId);
            itemId = normalizeQualified(itemId, moduleId + ":creative_entry");
            displayName = displayName == null || displayName.isBlank()
                    ? EchoItemDefinitionInference.inferDisplayName(itemId)
                    : displayName.trim();
        }
    }

    record CreativeSelectionResult(
            String moduleId,
            String itemId,
            boolean selected,
            boolean inventoryBacked,
            boolean hotbarBacked,
            String blocker
    ) {
        static CreativeSelectionResult failed(String moduleId, String itemId, String blocker) {
            return new CreativeSelectionResult(
                    normalize(moduleId),
                    normalize(itemId),
                    false,
                    false,
                    false,
                    blocker == null ? "selection_failed" : blocker
            );
        }
    }

    record CreativePlayResult(
            String moduleId,
            String itemId,
            boolean played,
            String mutation,
            boolean worldDirty,
            int dirtyChunkCount,
            List<String> feedbackEvents,
            String blocker
    ) {
        CreativePlayResult {
            moduleId = normalize(moduleId);
            itemId = normalize(itemId);
            mutation = mutation == null ? "" : mutation.trim();
            dirtyChunkCount = Math.max(0, dirtyChunkCount);
            feedbackEvents = feedbackEvents == null ? List.of() : List.copyOf(feedbackEvents);
            blocker = blocker == null ? "" : blocker.trim();
        }

        static CreativePlayResult failed(String moduleId, String itemId, String blocker) {
            return new CreativePlayResult(
                    moduleId,
                    itemId,
                    false,
                    "",
                    false,
                    0,
                    List.of(),
                    blocker == null ? "play_failed" : blocker
            );
        }
    }

    static List<CreativeEntry> dedupeEntries(List<CreativeEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        ArrayList<CreativeEntry> result = new ArrayList<>();
        for (CreativeEntry entry : entries) {
            if (entry != null && seen.add(entry.itemId())) {
                result.add(entry);
            }
        }
        return List.copyOf(result);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeQualified(String value, String fallback) {
        String normalized = normalize(value);
        if (normalized.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            return normalized;
        }
        return normalize(fallback);
    }

    private static boolean creativeTabRow(String graphKind, String domain, Map<String, Object> metadata) {
        return "echo:creative_tab".equals(normalize(graphKind))
                || Boolean.TRUE.equals(metadata.get("creativeTab"))
                || ("inventory".equals(normalize(domain))
                && "echo:creative_tab".equals(normalize(String.valueOf(metadata.get("contentGraphKind")))));
    }

    private static boolean creativeEntryRow(String contentKind, String domain, String graphKind) {
        String kind = normalize(contentKind).replace('-', '_').replace('.', '_');
        String normalizedDomain = normalize(domain);
        String normalizedGraphKind = normalize(graphKind);
        return kind.equals("block")
                || kind.equals("item")
                || normalizedDomain.equals("blocks")
                || normalizedDomain.equals("items")
                || normalizedGraphKind.equals("echo:block")
                || normalizedGraphKind.equals("echo:item");
    }

    private static boolean blockEntry(String contentKind, String domain, String graphKind) {
        String kind = normalize(contentKind).replace('-', '_').replace('.', '_');
        return kind.equals("block")
                || normalize(domain).equals("blocks")
                || normalize(graphKind).equals("echo:block");
    }

    private static String moduleFromContentId(String contentId) {
        String normalized = normalize(contentId);
        int colon = normalized.indexOf(':');
        return colon > 0 ? normalized.substring(0, colon) : "";
    }

    private static String displayNameFromId(String id) {
        String normalized = normalize(id);
        int colon = normalized.lastIndexOf(':');
        if (colon >= 0 && colon + 1 < normalized.length()) {
            normalized = normalized.substring(colon + 1);
        }
        int slash = normalized.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < normalized.length()) {
            normalized = normalized.substring(slash + 1);
        }
        StringBuilder result = new StringBuilder();
        for (String part : normalized.replace('-', '_').split("_+")) {
            if (part.isBlank()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.isEmpty() ? id : result.toString();
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = value == null ? "" : String.valueOf(value).trim();
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return Map.of();
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof Iterable<?> iterable)) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object item : iterable) {
            String text = normalize(String.valueOf(item));
            if (!text.isBlank()) {
                result.add(text);
            }
        }
        return List.copyOf(result);
    }

    private static final class CreativeTabBuilder {
        private final String moduleId;
        private final String tabId;
        private final LinkedHashMap<String, CreativeEntry> entries = new LinkedHashMap<>();
        private String titleKey;

        private CreativeTabBuilder(String moduleId, String tabId, String titleKey) {
            this.moduleId = normalize(moduleId);
            this.tabId = normalizeQualified(tabId, this.moduleId + ":content_graph");
            this.titleKey = titleKey == null ? "" : titleKey.trim();
        }

        private String titleKey() {
            return titleKey;
        }

        private void titleKey(String titleKey) {
            String clean = titleKey == null ? "" : titleKey.trim();
            if (!clean.isBlank()) {
                this.titleKey = clean;
            }
        }

        private void add(CreativeEntry entry) {
            if (entry != null) {
                entries.putIfAbsent(entry.itemId(), entry);
            }
        }

        private CreativeTab build() {
            return new CreativeTab(
                    moduleId,
                    tabId,
                    titleKey.isBlank() ? displayNameFromId(tabId) : titleKey,
                    List.copyOf(entries.values()),
                    true
            );
        }
    }

    private static final class PlaceOnceInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean pressed = true;

        private PlaceOnceInput(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        public int selectedHotbarSlot(int current) {
            return selectedSlot;
        }

        @Override
        public boolean consumeBreak() {
            return false;
        }

        @Override
        public boolean isCursorLocked() {
            return true;
        }

        @Override
        public boolean consumePlace() {
            boolean value = pressed;
            pressed = false;
            return value;
        }
    }
}
