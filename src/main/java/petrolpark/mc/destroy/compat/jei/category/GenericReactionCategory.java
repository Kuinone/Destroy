package petrolpark.mc.destroy.compat.jei.category;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroup;
import petrolpark.mc.destroy.chemistry.legacy.LegacyFunctionalGroupType;
import petrolpark.mc.destroy.chemistry.legacy.LegacyReaction;
import petrolpark.mc.destroy.chemistry.legacy.LegacySpecies;
import petrolpark.mc.destroy.chemistry.legacy.genericreaction.GenericReaction;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe;
import petrolpark.mc.destroy.core.chemistry.recipe.ReactionRecipe.GenericReactionRecipe;

/**
 * JEI category for {@link GenericReactionRecipe} · extends {@link ReactionCategory} with an
 * example-reaction-driven display. Used for reactions that accept any molecule containing a
 * specific functional group (e.g. "primary amine + acid → amide"). Each {@code GenericReaction}
 * contributes one example recipe to this category.
*/
public class GenericReactionCategory extends ReactionCategory<GenericReactionRecipe> {

    
    public static RecipeType<RecipeHolder<GenericReactionRecipe>> TYPE;

    /**
 * Each {@link LegacyFunctionalGroupType} mapped to the Set of {@link GenericReaction}s that
 * can produce it. The novel results of {@code GenericReaction.getExampleReaction} are used to
 * determine if a Generic Reaction can produce a Group.
*/
    public static Map<LegacyFunctionalGroupType<?>, Set<GenericReaction>> GROUP_RECIPES = new HashMap<>();

    /**
 * All example reactions (mapped from GenericReaction → GenericReactionRecipe) generated at
 * class-load.
*/
    public static Map<GenericReaction, GenericReactionRecipe> RECIPES = new HashMap<>();

    public GenericReactionCategory(Info<GenericReactionRecipe> info, IJeiHelpers helpers) {
        super(info, helpers);
        GenericReactionCategory.TYPE = info.recipeType();
    }

    @Override
    protected String getTranslationKey(ReactionRecipe recipe) {
        if (recipe instanceof GenericReactionRecipe gRecipe) {
            ResourceLocation id = gRecipe.getGenericReaction().id;
            return id.getNamespace() + ".generic_reaction." + id.getPath();
        }
        return "";
    }

    /** Generate every GenericReaction's example recipe at class-load.*/
    static {
        for (GenericReaction genericReaction : GenericReaction.GENERIC_REACTIONS) {
            LegacyReaction reaction = null;
            try {
                reaction = genericReaction.getExampleReaction();
            } catch (Throwable e) {
                Destroy.LOGGER.warn("Problem generating generic reaction " + genericReaction.id);
                throw e;
            }
            if (reaction != null) {
                RECIPES.put(genericReaction, GenericReactionRecipe.create(genericReaction));
                for (LegacySpecies product : reaction.getProducts()) {
                    if (product.isNovel()) {
                        for (LegacyFunctionalGroup<?> functionalGroup : product.getFunctionalGroups()) {
                            GROUP_RECIPES.merge(functionalGroup.getType(), Set.of(genericReaction), Sets::union);
                        }
                    }
                }
            }
        }
    }
}
