package petrolpark.mc.destroy.content.processing.dynamo.arcfurnace;

import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;

import petrolpark.mc.destroy.DestroyRecipeTypes;

/**
 * Arc Furnace recipe — Basin processing variant triggered when a Dynamo sits atop a Carbon
 * Fiber Block (Arc Furnace configuration). Same BasinRecipe shape as
 * {@link petrolpark.mc.destroy.content.processing.dynamo.ElectrolysisRecipe} but distinguished
 * by {@link DestroyRecipeTypes#ARC_FURNACE} RecipeType. {@code getMaxInputCount()} bumped to 10
 * to support {@code destroy:mixing/stainless_steel_efficient_fluxed} (10 input slots).
*/
public class ArcFurnaceRecipe extends BasinRecipe {

    public ArcFurnaceRecipe(ProcessingRecipeParams params) {
        super(DestroyRecipeTypes.ARC_FURNACE, params);
    }

    @Override
    protected int getMaxInputCount() {
        return 10; // destroy:mixing/stainless_steel_efficient_fluxed has 10 inputs
    }
}
