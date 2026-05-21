package petrolpark.mc.destroy.core.item;

import petrolpark.mc.destroy.MoveToPetrolparkLibrary;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * Plain-item variant of {@link CombustibleBlockItem}.
*/
@MoveToPetrolparkLibrary
public class CombustibleItem extends Item {

    private int burnTime = -1;

    public CombustibleItem(Properties properties) {
        super(properties);
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, RecipeType<?> recipeType) {
        return this.burnTime;
    }
}
