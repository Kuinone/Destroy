package petrolpark.mc.destroy;

import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.compat.jei.category.MysteriousItemConversionCategory;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import petrolpark.mc.destroy.config.DestroySubstancesConfigs;

/**
 * Registers Destroy items into Create's "Mysterious Item Conversion" JEI category — pure
 * cosmetic JEI hint that some items can be transformed into others (no real recipe; players
 * use right-click / world interaction).
*/
public class DestroyMysteriousItemConversions {

    public static void addAll() {
        MysteriousItemConversionCategory.RECIPES.add(ConversionRecipe.create(
            AllItems.EMPTY_BLAZE_BURNER.asStack(), DestroyBlocks.COOLER.asStack()));
        // Iodine → Dragon's Breath conversion.
        // DestroySubstancesConfigs.iodineDragonsBreath() (a SERVER config). Problem in 1.21
        // NeoForge: SERVER configs don't load until world-join, but addAll() runs from
        // FMLClientSetupEvent → MoConfigSpec.get() throws "Cannot get config value before
        // config is loaded". Wrapped in try/catch with default=true: the JEI hint shows by
        // default; if a world's server config has the toggle disabled the underlying gameplay
        // path stays disabled regardless (the mechanic check in IodineItem reads the same
        // config at use-time when it IS loaded). Worst case: the JEI hint is shown for a
        // disabled-by-server world — informational mismatch only, not a gameplay bug.
        if (iodineDragonsBreathSafe()) {
            MysteriousItemConversionCategory.RECIPES.add(ConversionRecipe.create(
                DestroyItems.IODINE.asStack(), new ItemStack(Items.DRAGON_BREATH)));
        }
        MysteriousItemConversionCategory.RECIPES.add(ConversionRecipe.create(
            DestroyItems.BUCKET_AND_SPADE.asStack(), DestroyItems.TEAR_BOTTLE.asStack()));
        MysteriousItemConversionCategory.RECIPES.add(ConversionRecipe.create(
            DestroyItems.MOLTEN_STAINLESS_STEEL_BUCKET.asStack(), DestroyBlocks.STAINLESS_STEEL_BLOCK.asStack()));
    }

    private static boolean iodineDragonsBreathSafe() {
        try {
            return DestroySubstancesConfigs.iodineDragonsBreath();
        } catch (IllegalStateException ex) {
            // Server config not yet loaded — default to true (JEI hint displayed).
            return true;
        }
    }
}
