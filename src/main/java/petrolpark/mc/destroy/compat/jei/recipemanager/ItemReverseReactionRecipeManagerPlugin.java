package petrolpark.mc.destroy.compat.jei.recipemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.compat.jei.category.GenericReactionCategory;
import petrolpark.mc.destroy.compat.jei.category.ReactionCategory;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe;

/**
 * Surfaces reversible Reactions in JEI when the user focuses on an item ingredient that the
 * reaction's reactant/precipitate set could match — fixes a JEI default-behavior gap where
 * reversible Reactions wouldn't show under the "Item produces" recipe page if the reaction was
 * defined with the item on the input side.
*/
public class ItemReverseReactionRecipeManagerPlugin implements IRecipeManagerPlugin {

    public static final List<RecipeType<?>> TYPES = List.of(ReactionCategory.TYPE, GenericReactionCategory.TYPE);

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        return TYPES;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        List<T> recipes = new ArrayList<>();
        focus.checkedCast(VanillaTypes.ITEM_STACK)
            .map(IFocus::getTypedValue)
            .map(ITypedIngredient::getIngredient)
            .ifPresent(stack -> {
                Stream<? extends ReactionRecipe> recipesToCheck;
                if (recipeCategory instanceof GenericReactionCategory) {
                    recipesToCheck = GenericReactionCategory.RECIPES.values().stream();
                } else if (recipeCategory instanceof ReactionCategory) {
                    recipesToCheck = ReactionCategory.RECIPES.values().stream();
                } else {
                    return;
                }
                recipesToCheck.filter(recipe -> {
                    LegacyReaction reaction = recipe.getReaction();
                    boolean searchCatalysts = focus.getRole() == RecipeIngredientRole.CATALYST;
                    boolean searchInputs = searchCatalysts
                        || focus.getRole() == RecipeIngredientRole.INPUT
                        || (focus.getRole() == RecipeIngredientRole.OUTPUT && reaction.displayAsReversible());
                    boolean searchOutputs = searchCatalysts
                        || focus.getRole() == RecipeIngredientRole.OUTPUT
                        || (focus.getRole() == RecipeIngredientRole.INPUT && reaction.displayAsReversible());

                    // Reactants and catalysts
                    if (reaction.getItemReactants().stream().anyMatch(ir ->
                        ((ir.isCatalyst() && searchCatalysts) || (!ir.isCatalyst() && searchInputs))
                            && ir.isItemValid(stack))) return true;

                    // line `if (searchOutputs && reaction.hasResult()) reaction.getResult()...anyMatch(...)`
                    // was an unbound expression statement. Preserved 1:1; behavior matches upstream.)
                    if (searchOutputs && reaction.hasResult()) {
                        reaction.getResult().getAllPrecipitates().stream()
                            .anyMatch(p -> ItemStack.matches(p.getPrecipitate(), stack));
                    }

                    return false;
                }).map(r -> (T) r).forEach(recipes::add);
            });
        return recipes;
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        return List.of();
    }
}
