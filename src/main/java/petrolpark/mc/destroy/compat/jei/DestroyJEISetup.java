package petrolpark.mc.destroy.compat.jei;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.DestroyBlocks;

/**
 * Data-only helpers for the JEI plugin. This class is loaded without a guarantee that JEI is
 * installed, so it must never reference JEI types.
*/
public class DestroyJEISetup {

    /**
 * Any Item which can be filled with Explosives. Consumed by the future
 * {@code MixableExplosiveCategory} (T3b) to render the full list of mixable-explosive
 * containers in JEI.
*/
    public static final Collection<Supplier<ItemStack>> CUSTOM_MIX_EXPLOSIVES = new HashSet<>();

    static {
        CUSTOM_MIX_EXPLOSIVES.add(DestroyBlocks.CUSTOM_EXPLOSIVE_MIX::asStack);
    }

    private DestroyJEISetup() {
        // Static utility class
    }
}
