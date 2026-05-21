package petrolpark.mc.destroy.compat.jei.animation;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.compat.jei.category.CreateRecipeCategory;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.foundation.utility.CreateLang;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.core.recipe.ingredient.fluid.RefrigerantDummyFluidIngredient;

/**
 * Heat-condition catalyst + label renderer for DistillationCategory (and any future processing
 * category that exposes a {@link HeatCondition}). Draws the heat-condition name in a text box
 * and adds catalyst slots for Blaze Burner / Cooler / Blaze-Cake-treat items.
*/
public class HeatConditionRenderer {

    /**
 * Draw the heat-condition label in the text box at (x, y).
*/
    public static void drawHeatConditionName(Font font, GuiGraphics graphics, int x, int y, HeatCondition requiredHeat) {
        MutableComponent name;
        if ("COOLED".equals(requiredHeat.name())) {
            name = DestroyLang.translate(requiredHeat.getTranslationKey()).component();
        } else {
            name = CreateLang.translate(requiredHeat.getTranslationKey()).component();
        }
        graphics.drawString(font, name, x, y, requiredHeat.getColor(), false);
    }

    /**
 * Add the catalyst slot(s) for the heat condition:
 * <ul>
 * <li>COOLED → Cooler block (refrigerant fluid slot degraded · see class javadoc).</li>
 * <li>HEATED / SUPERHEATED → Blaze Burner (RENDER_ONLY).</li>
 * <li>SUPERHEATED also → Blaze Cake / any {@code BLAZE_BURNER_FUEL_SPECIAL} tag item.</li>
 * <li>NONE → no slot.</li>
 * </ul>
*/
    @SuppressWarnings({"deprecation", "removal"}) // AllItemTags.BLAZE_BURNER_FUEL_SPECIAL · Create scheduled rename
    public static void addHeatConditionSlots(IRecipeLayoutBuilder builder, int x, int y, HeatCondition requiredHeat) {
        List<ItemStack> blazeTreatStacks = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.tag)) {
            blazeTreatStacks.add(new ItemStack(holder.value()));
        }

        if ("COOLED".equals(requiredHeat.name())) {
            builder
                .addSlot(RecipeIngredientRole.CATALYST, x, y)
                .addItemStack(DestroyBlocks.COOLER.asStack());
            // RefrigerantDummyFluidIngredient extends MoleculeTagFluidIngredient (singletons of
            // every REFRIGERANT-tagged molecule). SizedFluidIngredient wraps it for Create's
            // addFluidSlot signature.
            CreateRecipeCategory.addFluidSlot(
                builder, x + 19, y,
                new SizedFluidIngredient(RefrigerantDummyFluidIngredient.INSTANCE, 1000));
        } else if (requiredHeat != HeatCondition.NONE) {
            // Blaze Burner render-only for heated / superheated.
            builder
                .addSlot(RecipeIngredientRole.RENDER_ONLY, x, y)
                .addItemStack(AllBlocks.BLAZE_BURNER.asStack());
        }
        if (requiredHeat == HeatCondition.SUPERHEATED) {
            builder
                .addSlot(RecipeIngredientRole.CATALYST, x + 19, y)
                .addItemStacks(blazeTreatStacks);
        }
    }
}
