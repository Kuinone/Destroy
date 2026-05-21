package petrolpark.mc.destroy.compat.jei.recipemanager;

import java.util.Collections;
import java.util.List;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.client.Minecraft;

import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.compat.jei.category.FlameRetardantApplicationCategory;
import petrolpark.mc.destroy.content.product.fireretardant.FireproofingHelper;

/**
 * JEI recipe manager plugin — surfaces FlameRetardantApplication recipes when the user focuses on
 * an item that {@link FireproofingHelper#couldApply could be} fireproofed by the spout-application
 * recipe (lets JEI show "Use" recipe page even though the recipe doesn't list every applicable
 * input as a hardcoded ingredient).
 *
 * <p>Wire: register via {@code DestroyJEI.registerRecipeManagerPlugins(IRecipeRegistration)}
 * once that hook is added to DestroyJEI (deferred — same wiring needed for ItemReverseReaction
 * + ChemicalSpecies plugins; one-shot wire when the JEI plugin batch lands).</p>
*/
public class FireproofingRecipeManagerPlugin implements IRecipeManagerPlugin {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        if (focus.getRole() == RecipeIngredientRole.INPUT
            && focus.checkedCast(VanillaTypes.ITEM_STACK)
                .filter(f -> FireproofingHelper.couldApply(mc.level, f.getTypedValue().getIngredient()))
                .isPresent()) {
            return Collections.singletonList(FlameRetardantApplicationCategory.TYPE);
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if (recipeCategory.getRecipeType() == FlameRetardantApplicationCategory.TYPE) {
            return (List<T>) mc.getConnection().getRecipeManager()
                .getAllRecipesFor(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.getType());
        }
        return Collections.emptyList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory.getRecipeType() == FlameRetardantApplicationCategory.TYPE) {
            return (List<T>) mc.getConnection().getRecipeManager()
                .getAllRecipesFor(DestroyRecipeTypes.FLAME_RETARDANT_APPLICATION.getType());
        }
        return Collections.emptyList();
    }
}
