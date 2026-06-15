package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.world.openlands.EchoOpenlandsItemDefinition;

import java.util.List;

/**
 * Bootstrap helper that registers Openlands item definitions into the runtime item registry.
 */
public final class EchoOpenlandsItems {

    private EchoOpenlandsItems() {
    }

    public static void registerAll(EchoItemRegistry registry, List<EchoOpenlandsItemDefinition> items) {
        for (EchoOpenlandsItemDefinition item : items) {
            register(registry, item);
        }
    }

    public static void register(EchoItemRegistry registry, EchoOpenlandsItemDefinition item) {
        registry.register(toRuntimeDefinition(item));
    }

    private static EchoItemDefinition toRuntimeDefinition(EchoOpenlandsItemDefinition item) {
        return new EchoItemDefinition(
                new EchoItemId(item.id()),
                item.displayName(),
                inferCategory(item.useType()),
                item.stackSize() > 0 ? item.stackSize() : 64,
                1.0D,
                item.tags(),
                item.tooltipLines()
        );
    }

    private static EchoItemCategory inferCategory(String useType) {
        if (useType == null || useType.isBlank()) {
            return EchoItemCategory.MATERIAL;
        }
        return switch (useType.toLowerCase()) {
            case "food", "consumable" -> EchoItemCategory.CONSUMABLE;
            case "tool", "energy_component", "waystone_charge" -> EchoItemCategory.TOOL;
            case "equipment", "small_pack" -> EchoItemCategory.EQUIPMENT;
            case "quest", "route_binding" -> EchoItemCategory.QUEST;
            default -> EchoItemCategory.MATERIAL;
        };
    }
}
