package petrolpark.mc.destroy.mixin.compat.jei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.compat.jei.category.PackingCategory;
import com.simibubi.create.compat.jei.category.animations.AnimatedPress;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import net.minecraft.client.gui.GuiGraphics;

import petrolpark.mc.destroy.compat.jei.animation.AnimatedCooler;
import petrolpark.mc.destroy.content.processing.cooler.CoolerBlockEntity.ColdnessLevel;

/** compacting/
 * stainless_steel_block, compacting/packed_ice, compacting/borosilicate_glass …), replace
 * Create's animated blaze-burner render with our {@link AnimatedCooler} render so the JEI
 * recipe view shows the Refrigerstraytor + skull instead of an empty blaze burner.
*/
@Mixin(PackingCategory.class)
public class PackingCategoryMixin {

    private static final AnimatedPress destroy$press = new AnimatedPress(true);
    private static final AnimatedCooler destroy$cooler = new AnimatedCooler();

    @Inject(
        method = "draw(Lcom/simibubi/create/content/processing/basin/BasinRecipe;Lmezz/jei/api/gui/ingredient/IRecipeSlotsView;Lnet/minecraft/client/gui/GuiGraphics;DD)V",
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/processing/basin/BasinRecipe;getRequiredHeat()Lcom/simibubi/create/content/processing/recipe/HeatCondition;"
        ),
        cancellable = true,
        remap = false
    )
    private void destroy$inDraw(BasinRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY, CallbackInfo ci) {
        if ("COOLED".equals(recipe.getRequiredHeat().name())) {
            destroy$cooler.withColdness(ColdnessLevel.FROSTING).draw(graphics, 177 / 2 + 3, 55);
            destroy$press.draw(graphics, 177 / 2 + 3, 34);
            ci.cancel();
        }
    }
}
