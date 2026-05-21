package petrolpark.mc.destroy;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DestroyAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, Destroy.MOD_ID);

    public static final DeferredHolder<Attribute, Attribute>
        EXTRA_INVENTORY_SIZE = registerRanged("extra_inventory_size", 0d, 0d, 256d, true),
        EXTRA_HOTBAR_SLOTS = registerRanged("extra_hotbar_slots", 0d, 0d, 32d, true);

    private static DeferredHolder<Attribute, Attribute> registerRanged(String name, double defaultValue, double min, double max, boolean syncable) {
        return ATTRIBUTES.register(name, () -> new RangedAttribute("attribute.name." + Destroy.MOD_ID + "." + name, defaultValue, min, max).setSyncable(syncable));
    }

    public static void register(IEventBus bus) {
        ATTRIBUTES.register(bus);
    }
}
