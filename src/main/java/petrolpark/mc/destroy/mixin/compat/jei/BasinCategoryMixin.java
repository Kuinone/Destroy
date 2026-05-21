package petrolpark.mc.destroy.mixin.compat.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.compat.jei.category.BasinCategory;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import petrolpark.mc.destroy.compat.jei.animation.HeatConditionRenderer;

/**
 *
 * <p>Patches Create's {@link BasinCategory} so the {@code COOLED} heat condition (added by
 * {@link petrolpark.mc.destroy.mixin.compat.create.HeatConditionMixin}) renders correctly in
 * JEI:</p>
 *
 * <p>Injection points adapted to 1.21 BasinCategory structure:</p>
*/
@Mixin(BasinCategory.class)
public class BasinCategoryMixin {

    /**
 * Replace burner+blaze-cake catalyst slots with cooler slot when COOLED.
 * For other heat conditions, this still calls {@link HeatConditionRenderer#addHeatConditionSlots}
 * which handles HEATED/SUPERHEATED/NONE identically to Create's original code path —
 * so we can unconditionally cancel here without dropping non-COOLED behavior.
*/
    @Inject(
        method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/recipe/IFocusGroup;)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"
        ),
        cancellable = true,
        remap = false
    )
    protected void destroy$inSetRecipe(IRecipeLayoutBuilder builder, BasinRecipe recipe, IFocusGroup focuses, CallbackInfo ci) {
        HeatConditionRenderer.addHeatConditionSlots(builder, 134, 81, recipe.getRequiredHeat());
        ci.cancel();
    }

    /**
 * Replace the heat-condition text rendering. Triggers at the FIRST {@code Minecraft.getInstance()}
 * call inside {@code draw} (used to fetch the font for {@code drawString}). The original
 * {@code CreateLang.translateDirect} returns a raw "create.recipe.heat_requirement.cooled"
 * key for COOLED (no Create lang entry); our {@link HeatConditionRenderer#drawHeatConditionName}
 * routes COOLED through DestroyLang and others through CreateLang.
*/
    @SuppressWarnings("resource")
    @Inject(
        method = "draw(Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;getInstance()Lnet/minecraft/client/Minecraft;"
        ),
        cancellable = true
    )
    protected void destroy$inDraw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY, CallbackInfo ci) {
        HeatConditionRenderer.drawHeatConditionName(Minecraft.getInstance().font, graphics, 9, 86, recipe.getRequiredHeat());
        ci.cancel();
    }
}
