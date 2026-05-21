package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.createmod.catnip.data.Iterate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.registries.DeferredHolder;

import petrolpark.mc.destroy.DestroyRecipeTypes;

/**
 * Crafting-table recipe that inserts valid explosive ingredients (items with registered
 * {@link ExplosiveProperties}) into an {@link IMixedExplosiveItem}'s inventory. Must have exactly
 * one mix-container item + at least one explosive ingredient. Leftover inventory space is allowed
 * (fewer than max explosives is fine).
 *
 * <p><b>Dependencies — all ready</b>:</p>
 * <ul>
 * <li>{@link IMixedExplosiveItem} (S208) — dual API surface with HolderLookup.Provider signature.</li>
 * <li>{@link MixedExplosiveInventory} (S204) — {@link MixedExplosiveInventory#canBeAdded} filter.</li>
 * <li>{@link ExplosiveProperties#ITEM_EXPLOSIVE_PROPERTIES} (S203) — validates ingredient items.</li>
 * </ul>
*/
public class FillMixedExplosiveItemRecipe extends CustomRecipe {

    /**
 * SimpleCraftingRecipeSerializer registered via {@link DestroyRecipeTypes.Registers#SERIALIZERS}
 * DeferredRegister. The Factory lambda receives {@link CraftingBookCategory} and returns a
 * new recipe instance.
*/
    public static final DeferredHolder<RecipeSerializer<?>, SimpleCraftingRecipeSerializer<FillMixedExplosiveItemRecipe>> SERIALIZER =
        DestroyRecipeTypes.Registers.SERIALIZERS.register("fill_mixed_explosive_item",
            () -> new SimpleCraftingRecipeSerializer<>(FillMixedExplosiveItemRecipe::new));

    public FillMixedExplosiveItemRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assemble(input, level.registryAccess()).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider provider) {
        boolean anyExplosiveFound = false;
        ItemStack mixItem = ItemStack.EMPTY;
        MixedExplosiveInventory inv = null;
        for (boolean findMixItem : Iterate.trueAndFalse) {
            for (int slot = 0; slot < input.size(); slot++) {
                ItemStack stack = input.getItem(slot);
                if (stack.getItem() instanceof IMixedExplosiveItem customMixItem) {
                    if (findMixItem) { // If we're looking for a mix container and we've found one
                        if (inv != null) return ItemStack.EMPTY; // Only one mix container allowed
                        else {
                            mixItem = stack;
                            inv = customMixItem.getExplosiveInventory(stack, provider);
                        }
                    }
                } else if (MixedExplosiveInventory.canBeAdded(stack)) {
                    anyExplosiveFound = true;
                    if (!findMixItem && inv != null && !ItemHandlerHelper.insertItem(inv, stack, false).isEmpty()) return ItemStack.EMPTY;
                } else if (!stack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }
        if (!anyExplosiveFound || mixItem.isEmpty()) return ItemStack.EMPTY; // If a mix Item or explosive was never found
        ItemStack result = mixItem.copy();
        if (result.getItem() instanceof IMixedExplosiveItem customMixItem) customMixItem.setExplosiveInventory(result, inv, provider); // Check should never fail
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER.get();
    }
}
