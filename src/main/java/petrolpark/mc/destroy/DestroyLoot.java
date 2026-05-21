package petrolpark.mc.destroy;

import net.neoforged.bus.api.IEventBus;

/**
 * Dispatcher for Destroy's loot-subsystem registrations (LootItemConditionType, LootItemFunction,
 * NumberProviders). Called once from {@link Destroy} with the mod event bus.
*/
public class DestroyLoot {

    public static void register(IEventBus eventBus) {
        DestroyLootConditions.register(eventBus);
    }
}
