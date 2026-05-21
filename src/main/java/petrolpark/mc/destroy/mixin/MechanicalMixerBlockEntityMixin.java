package petrolpark.mc.destroy.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity;
import com.simibubi.create.content.processing.basin.BasinRecipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.core.chemistry.basinreaction.ReactionInBasinRecipe;
import petrolpark.mc.destroy.mixin.accessor.BasinOperatingBlockEntityAccessor;

/**
 * Inject Destroy chemistry-reaction support into Create's Mechanical Mixer recipe matching. When a
 * basin under a mixer contains only Mixture-typed fluids, attempt to dynamically synthesize a
 * {@link ReactionInBasinRecipe} from the available fluid + item content and add it to the matching
 * recipe list. This is the only way Mechanical Mixer + Basin can drive chemistry reactions for
 * recipes the player isn't manually feeding to a Vat.*/
@Mixin(MechanicalMixerBlockEntity.class)
public class MechanicalMixerBlockEntityMixin {

    @Inject(
        method = "getMatchingRecipes()Ljava/util/List;",
        at = @At(
            value = "INVOKE_ASSIGN",
            target = "Lcom/simibubi/create/content/processing/basin/BasinOperatingBlockEntity;getMatchingRecipes()Ljava/util/List;"
        ),
        remap = false,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private void destroy$inGetMatchingRecipes(
            CallbackInfoReturnable<List<Recipe<?>>> ci,
            List<Recipe<?>> matchingRecipes) {

        ((BasinOperatingBlockEntityAccessor) this).invokeGetBasin().ifPresent(basin -> {
            if (!basin.hasLevel()) return;

            IFluidHandler fluidHandler = basin.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, basin.getBlockPos(), null);
            IItemHandler itemHandler = basin.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK, basin.getBlockPos(), null);
            if (fluidHandler == null || itemHandler == null) return;

            boolean containsOnlyMixtures = true;
            List<ItemStack> availableItemStacks = new ArrayList<>();
            List<FluidStack> availableFluidStacks = new ArrayList<>();

            for (int tank = 0; tank < fluidHandler.getTanks(); tank++) {
                FluidStack fluidStack = fluidHandler.getFluidInTank(tank);
                if (DestroyFluids.isMixture(fluidStack)) {
                    availableFluidStacks.add(fluidStack);
                } else if (!fluidStack.isEmpty()) {
                    containsOnlyMixtures = false;
                }
            }

            // Don't try to react if there are non-Mixture fluids in the basin
            if (!containsOnlyMixtures) return;
            if (availableFluidStacks.isEmpty()) return;

            for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
                availableItemStacks.add(itemHandler.getStackInSlot(slot));
            }

            ReactionInBasinRecipe recipe = ReactionInBasinRecipe.create(
                availableFluidStacks, availableItemStacks, basin);
            if (recipe != null && BasinRecipe.match(basin, recipe)) {
                matchingRecipes.add(recipe);
            }
        });
    }
}
