package petrolpark.mc.destroy.compat.jei;

import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.component.DataComponentMap;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * JEI subtype interpreter for Mixture fluid stacks — distinguishes JEI ingredient identity by
 * the FluidStack's full DataComponentMap.
 * The proper per-component interpretation happens later in
 * {@code ChemicalSpeciesRecipeManagerPlugin}; this interpreter is just the coarse subtype key.
*/
public class MixtureFluidSubtypeInterpreter implements IIngredientSubtypeInterpreter<FluidStack> {

    @Override
    public String apply(FluidStack ingredient, UidContext context) {
        DataComponentMap components = ingredient.getComponents();
        if (components.isEmpty()) return IIngredientSubtypeInterpreter.NONE;
        return components.toString();
    }
}
