package petrolpark.mc.destroy.content.product.fireretardant;

import com.petrolpark.PetrolparkRegistries;
import com.petrolpark.core.contamination.Contaminables;
import com.petrolpark.core.contamination.Contaminant;
import com.petrolpark.core.contamination.IContamination;
import com.petrolpark.core.contamination.ItemContamination;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.core.recipe.SingleFluidRecipe;

/**
 * Helper API for applying a "fireproof" {@link Contaminant} to {@link ItemStack}s based on
 * available flame-retardant fluid + recipes. The single {@code destroy:fireproof} Contaminant is
 * registered in a data pack; this helper looks it up via {@link PetrolparkRegistries} and
 * consults the {@link FlameRetardantApplicationRecipe} list to decide whether a given stack can
 * accept the coating + how much fluid to consume.
*/
public class FireproofingHelper {

    public static final ResourceLocation CONTAMINANT_RL = Destroy.asResource("fireproof");

    /**
 * Dummy empty RecipeInput used for `matches` calls that don't depend on any real slot
 * content. The FlameRetardantApplicationRecipe returns true unconditionally.
*/
    private static final RecipeInput EMPTY_INPUT = new SingleRecipeInput(ItemStack.EMPTY);

    /**
 * Look up the fireproof Contaminant Holder from the level's registry access. Returns null if
 * no data pack declared the {@code destroy:fireproof} Contaminant (pre-reload or missing data).
*/
    public static final Holder<Contaminant> getFireproofContaminant(RegistryAccess registryAccess) {
        return registryAccess.registry(PetrolparkRegistries.Keys.CONTAMINANT)
            .flatMap(r -> r.getHolder(CONTAMINANT_RL))
            .map(h -> (Holder<Contaminant>) h)
            .orElse(null);
    }

    public static boolean canApply(Level world, ItemStack stack) {
        return couldApply(world, stack)
            && DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.find(EMPTY_INPUT, world).isPresent();
    }

    public static boolean couldApply(Level world, ItemStack stack) {
        if (stack.has(DataComponents.FIRE_RESISTANT) || isFireproof(world.registryAccess(), stack)) return false;
        return Contaminables.ITEM.isContaminableStack(stack);
    }

    public static int getRequiredAmountForItem(Level world, ItemStack stack, FluidStack availableFluid) {
        if (!canApply(world, stack)) return -1;
        return world.getRecipeManager()
            .getRecipeFor(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.getType(), EMPTY_INPUT, world)
            .map(holder -> (SingleFluidRecipe) holder.value())
            .map(SingleFluidRecipe::getRequiredFluid)
            .filter(i -> i.ingredient().test(availableFluid))
            .map(SizedFluidIngredient::amount)
            .orElse(-1);
    }

    public static ItemStack fillItem(Level world, int requiredAmount, ItemStack stack, FluidStack availableFluid) {
        if (!canApply(world, stack)) return ItemStack.EMPTY;
        return world.getRecipeManager()
            .getRecipeFor(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.getType(), EMPTY_INPUT, world)
            .map(holder -> (SingleFluidRecipe) holder.value())
            .filter(r -> r.getRequiredFluid().ingredient().test(availableFluid))
            .map(r -> {
                availableFluid.shrink(100);
                ItemStack result = stack.copy();
                stack.shrink(1);
                apply(world, result);
                return result;
            })
            .orElse(ItemStack.EMPTY);
    }

    public static void apply(Level world, ItemStack stack) {
        Holder<Contaminant> holder = getFireproofContaminant(world.registryAccess());
        if (holder == null) return;
        IContamination<?, ?> contamination = ItemContamination.get(stack);
        if (contamination != null) contamination.contaminate(holder);
    }

    public static boolean isFireproof(RegistryAccess registryAccess, ItemStack stack) {
        Holder<Contaminant> holder = getFireproofContaminant(registryAccess);
        if (holder == null) return false;
        IContamination<?, ?> contamination = ItemContamination.get(stack);
        return contamination != null && contamination.has(holder);
    }
}
