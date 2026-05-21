package petrolpark.mc.destroy.compat.jei.category;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.compat.jei.animation.AnimatedDynamo;

/**
 * JEI category for ARC_FURNACE Basin-based processing. Extends Create's {@link BasinCategory}
 * so the recipe slot layout + heat-condition indicator rendering come for free — adds the
 * {@link AnimatedDynamo} visual overlay (with arcFurnace=true · shows the lid partial).
*/
public class ArcFurnaceCategory extends BasinCategory {

    private final AnimatedDynamo dynamo;

    
    protected final RecipeType<RecipeHolder<BasinRecipe>> type;

    public ArcFurnaceCategory(Info<BasinRecipe> info) {
        super(info, false);
        dynamo = new AnimatedDynamo(true, true);
        type = info.recipeType();
    }

    /**
 * Convert a vanilla {@link AbstractCookingRecipe} (SMELTING/BLASTING) into a synthetic
 * {@link BasinRecipe} for JEI display purposes — the ARC_FURNACE mode of the Dynamo can
 * process smelting/blasting recipes.
 *
 * @param holder RecipeHolder wrapping the source cooking recipe
 * @return synthetic BasinRecipe with single ingredient + single output · cooking-time preserved as processing duration
*/
    public static <R extends AbstractCookingRecipe> BasinRecipe toBasinRecipe(RecipeHolder<R> holder) {
        R recipe = holder.value();
        Minecraft mc = Minecraft.getInstance();
        return new StandardProcessingRecipe.Builder<BasinRecipe>(
                (ProcessingRecipeParams params) -> new BasinRecipe(params),
                Destroy.asResource("arc_furnace_" + holder.id().getPath()))
            .withItemIngredients(recipe.getIngredients())
            .output(recipe.getResultItem(mc.level.registryAccess()))
            .duration(recipe.getCookingTime())
            .build();
    }

    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
        dynamo.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    }
}
