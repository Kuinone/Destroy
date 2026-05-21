package petrolpark.mc.destroy.compat.jei.recipemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;

import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.compat.jei.MoleculeJEIIngredient;
import petrolpark.mc.destroy.compat.jei.category.GenericReactionCategory;
import petrolpark.mc.destroy.compat.jei.category.ReactionCategory;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe;

/**
 * JEI recipe manager plugin — surfaces Reactions / Generic Reactions when the user focuses on a
 * Molecule (custom JEI ingredient type registered via {@link MoleculeJEIIngredient}). Without
 * this plugin, JEI default behaviour wouldn't show the same molecule across reaction recipes
 * because the molecule isn't a vanilla ItemStack/FluidStack ingredient.
 *
 * <p><b>Effect of partial port</b>: clicking a Molecule in JEI shows all Reaction + Generic
 * Reaction recipes that produce / consume / catalyse that molecule. Clicking a Mixture
 * FluidStack does NOT (yet) reveal which contained molecules are useful — players must hunt
 * via molecule directly. Acceptable degradation; full port deferred until DestroyJEI Mixture
 * infrastructure lands.</p>
*/
public class ChemicalSpeciesRecipeManagerPlugin implements IRecipeManagerPlugin {

    @SuppressWarnings("unused")
    private final IJeiHelpers helpers;

    public ChemicalSpeciesRecipeManagerPlugin(IJeiHelpers helpers) {
        this.helpers = helpers;
    }

    @Override
    public <V> List<RecipeType<?>> getRecipeTypes(IFocus<V> focus) {
        List<RecipeType<?>> recipeTypes = new ArrayList<>();
        // Molecule focus only — Mixture-FluidStack drill-down deferred (see class javadoc).
        if (focus.getTypedValue().getType() == MoleculeJEIIngredient.TYPE) {
            recipeTypes.add(ReactionCategory.TYPE);
            recipeTypes.add(GenericReactionCategory.TYPE);
            // now also include MIXTURE_APPLICABLE_RECIPE_TYPES (currently empty stub —
            // when DestroyJEI loadCategories populates it, recipe types here become reachable).
            recipeTypes.addAll(petrolpark.mc.destroy.compat.jei.DestroyJEI.MIXTURE_APPLICABLE_RECIPE_TYPES.keySet());
        }
        return recipeTypes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        List<T> recipes = new ArrayList<>();

        // Mixture-FluidStack drill-down: deferred.
        // When DestroyJEI.MIXTURE_APPLICABLE_RECIPE_TYPES + MOLECULES_INPUT/OUTPUT are added,
        // re-port the focus.checkedCast(NeoForgeTypes.FLUID_STACK).ifPresent(...) branch here.

        LegacySpecies molecule = focus.checkedCast(MoleculeJEIIngredient.TYPE)
            .map(moleculeIngredient -> moleculeIngredient.getTypedValue().getIngredient())
            .orElse(null);
        if (molecule == null) return recipes;

        // JEI 1.21 + Create 6.x: Category T parameter is RecipeHolder<R>, not raw R.
        // We MUST wrap each recipe in a RecipeHolder before adding, otherwise downstream
        // CreateRecipeCategory.setRecipe(holder) cast fails with ClassCastException.
        // Counter for unique synthetic holder ids (JEI doesn't care about specific id, just needs
        // each holder to have one). Same pattern as DestroyJEI.loadCategories() reaction
        // registration uses.
        int[] counter = {0};

        switch (focus.getRole()) {
            case INPUT -> {
                if (recipeCategory instanceof GenericReactionCategory) {
                    molecule.getFunctionalGroups().forEach(group -> {
                        var maybeSet = LegacyFunctionalGroup.groupTypesAndReactions.get(group.getType());
                        Optional.ofNullable(maybeSet).ifPresent(set -> set.forEach(genericReaction -> {
                            ReactionRecipe recipe = GenericReactionCategory.RECIPES.get(genericReaction);
                            if (recipe != null) recipes.add((T) new net.minecraft.world.item.crafting.RecipeHolder<>(
                                petrolpark.mc.destroy.Destroy.asResource("plugin_input_generic_" + counter[0]++), recipe));
                        }));
                    });
                } else if (recipeCategory instanceof ReactionCategory) {
                    molecule.getReactantReactions().forEach(reaction -> {
                        ReactionRecipe r = ReactionCategory.RECIPES.get(reaction.getReactionDisplayedInJEI());
                        if (r != null) recipes.add((T) new net.minecraft.world.item.crafting.RecipeHolder<>(
                            petrolpark.mc.destroy.Destroy.asResource("plugin_input_reaction_" + counter[0]++), r));
                    });
                } else {
                    // non-Reaction processing recipes that consume this molecule (currently
                    // empty until DestroyJEI MOLECULES_INPUT/OUTPUT population lands).
                    var recipeUses = petrolpark.mc.destroy.compat.jei.DestroyJEI.MOLECULES_INPUT.get(molecule);
                    if (recipeUses != null) {
                        recipes.addAll(recipeUses.stream()
                            .filter(recipe -> recipe.getClass().equals(
                                petrolpark.mc.destroy.compat.jei.DestroyJEI.MIXTURE_APPLICABLE_RECIPE_TYPES.get(recipeCategory.getRecipeType())))
                            .map(recipe -> (T) new net.minecraft.world.item.crafting.RecipeHolder<>(
                                petrolpark.mc.destroy.Destroy.asResource("plugin_input_processing_" + counter[0]++), recipe))
                            .toList());
                    }
                }
            }
            case OUTPUT -> {
                if (recipeCategory instanceof GenericReactionCategory) {
                    molecule.getFunctionalGroups().forEach(group -> {
                        var maybeSet = GenericReactionCategory.GROUP_RECIPES.get(group.getType());
                        Optional.ofNullable(maybeSet).ifPresent(set -> set.forEach(genericReaction -> {
                            ReactionRecipe recipe = GenericReactionCategory.RECIPES.get(genericReaction);
                            if (recipe != null) recipes.add((T) new net.minecraft.world.item.crafting.RecipeHolder<>(
                                petrolpark.mc.destroy.Destroy.asResource("plugin_output_generic_" + counter[0]++), recipe));
                        }));
                    });
                } else if (recipeCategory instanceof ReactionCategory) {
                    molecule.getProductReactions().forEach(reaction -> {
                        ReactionRecipe r = ReactionCategory.RECIPES.get(reaction.getReactionDisplayedInJEI());
                        if (r != null) recipes.add((T) new net.minecraft.world.item.crafting.RecipeHolder<>(
                            petrolpark.mc.destroy.Destroy.asResource("plugin_output_reaction_" + counter[0]++), r));
                    });
                } else {
                    // non-Reaction processing recipes that produce this molecule.
                    var recipeProductions = petrolpark.mc.destroy.compat.jei.DestroyJEI.MOLECULES_OUTPUT.get(molecule);
                    if (recipeProductions != null) {
                        recipes.addAll(recipeProductions.stream()
                            .filter(recipe -> recipe.getClass().equals(
                                petrolpark.mc.destroy.compat.jei.DestroyJEI.MIXTURE_APPLICABLE_RECIPE_TYPES.get(recipeCategory.getRecipeType())))
                            .map(recipe -> (T) new net.minecraft.world.item.crafting.RecipeHolder<>(
                                petrolpark.mc.destroy.Destroy.asResource("plugin_output_processing_" + counter[0]++), recipe))
                            .toList());
                    }
                }
            }
            case CATALYST, RENDER_ONLY -> {
                // No catalyst/render lookups for molecules.
            }
        }
        return recipes;
    }

    @Override
    public <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
        return List.of();
    }
}
