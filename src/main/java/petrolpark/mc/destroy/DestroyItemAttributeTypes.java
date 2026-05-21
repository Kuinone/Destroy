package petrolpark.mc.destroy;

import java.util.function.Supplier;

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import petrolpark.mc.destroy.content.processing.trypolithography.CircuitPatternItemAttribute;

/**
 * Destroy's registered {@link ItemAttributeType}s (Create's item-filter attribute system). Each
 * entry registers a new attribute "dimension" that Brass Funnel / Belt filter UIs can use to
 * match/route items.
*/
public class DestroyItemAttributeTypes {

    private static final DeferredRegister<ItemAttributeType> TYPES =
        DeferredRegister.create(CreateRegistries.ITEM_ATTRIBUTE_TYPE, Destroy.MOD_ID);

    public static final Supplier<ItemAttributeType> IS_CIRCUIT_PATTERN_PUNCHED =
        TYPES.register("is_circuit_pattern_punched", CircuitPatternItemAttribute.Type::new);

    /** Register the DeferredRegister onto the mod event bus — called from Destroy.java ctor.*/
    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }
}
