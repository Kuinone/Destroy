package petrolpark.mc.destroy.compat.jei.category;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeRegistration;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroyRecipeTypes;
import petrolpark.mc.destroy.compat.jei.animation.AnimatedDynamo;
import petrolpark.mc.destroy.content.processing.discstamping.DiscElectroplatingRecipe;

/**
 * JEI category for ELECTROLYSIS Basin-based processing. Extends Create's {@link BasinCategory}
 * and adds an {@link AnimatedDynamo} overlay (with basin=true · dynamo sits above a Basin).
*/
public class ElectrolysisCategory extends BasinCategory {

    private final AnimatedDynamo dynamo;

    
    protected final RecipeType<RecipeHolder<BasinRecipe>> type;

    public ElectrolysisCategory(Info<BasinRecipe> info) {
        super(info, false);
        dynamo = new AnimatedDynamo(true, false);
        type = info.recipeType();
    }

    @Override
    public void draw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        super.draw(recipe, recipeSlotsView, graphics, mouseX, mouseY);
        dynamo.draw(graphics, getBackground().getWidth() / 2 + 3, 34);
    }

    /**
 * Add dynamically-generated {@link DiscElectroplatingRecipe} copies — one per music disc
 * (items with {@link DataComponents#JUKEBOX_PLAYABLE}) — in addition to the statically-declared
 * ELECTROLYSIS recipes. Excludes the BLANK_MUSIC_DISC and non-original templates.
*/
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        super.registerRecipes(registration);

        List<RecipeHolder<BasinRecipe>> discRecipes = new ArrayList<>();
        int[] counter = { 0 };
        CreateJEI.<DiscElectroplatingRecipe>consumeTypedRecipes(holder -> {
            if (!(holder.value() instanceof DiscElectroplatingRecipe recipe)) return;
            if (!recipe.original) return;
            Item blankDisc = DestroyItems.BLANK_MUSIC_DISC.get();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item == blankDisc) continue;
                ItemStack stack = item.getDefaultInstance();
                if (!stack.has(DataComponents.JUKEBOX_PLAYABLE)) continue;
                BasinRecipe copy = recipe.copyWithDisc(stack);
                ResourceLocation syntheticId = Destroy.asResource(
                    "disc_electroplating_" + BuiltInRegistries.ITEM.getKey(item).getPath() + "_" + counter[0]++);
                discRecipes.add(new RecipeHolder<>(syntheticId, copy));
            }
        }, DestroyRecipeTypes.DISC_ELECTROPLATING.getType());

        if (!discRecipes.isEmpty()) registration.addRecipes(type, discRecipes);
    }
}
