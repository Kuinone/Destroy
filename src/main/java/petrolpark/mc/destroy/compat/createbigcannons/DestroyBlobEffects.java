package petrolpark.mc.destroy.compat.createbigcannons;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.EndFluidStack;
import rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell.FluidBlobEffectRegistry;

import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;

/**
 * Create Big Cannons compatibility — fluid-shell blob hit effects.
 *
 * <p>Pollution dispatch flows through {@link PollutionHelper#pollute(net.minecraft.world.level.Level,
 * BlockPos, FluidStack...)} which inspects the FluidStack's
 * {@link petrolpark.mc.destroy.DestroyDataComponents#MIXTURE} payload and routes pollution by
 * each Molecule's {@code GREENHOUSE_GAS / SMOG_CAUSING / OZONE_DEPLETING / ACID_RAIN_CAUSING /
 * RADIOACTIVE} tags. So a shell of acid mixture hitting a chunk increases ACID_RAIN; a shell of
 * radioactive sludge bumps RADIOACTIVITY at the impact chunk; etc. Same routing semantics as
 * Cooler/Vat venting and Open-Ended-Pipe spillage.</p>
 *
 * <p>1.21 EndFluidStack → NeoForge FluidStack conversion: EndFluidStack carries a
 * {@link net.minecraft.core.component.PatchedDataComponentMap}; FluidStack ctor
 * accepts {@link net.minecraft.core.component.DataComponentPatch} via {@code .asPatch()}.</p>
*/
public class DestroyBlobEffects {

    public static void registerBlobEffects() {
        FluidBlobEffectRegistry.registerAllHit(
            DestroyFluids.MIXTURE.get(),
            DestroyBlobEffects::onMixtureHit);
    }

    private static void onMixtureHit(FluidBlobEffectRegistry.OnHit.Context context) {
        EndFluidStack endStack = context.fstack();
        if (endStack == null || endStack.isEmpty()) return;
        FluidStack fluidStack = new FluidStack(
            endStack.fluid().builtInRegistryHolder(),
            endStack.amount(),
            endStack.components().asPatch());
        BlockPos pos = BlockPos.containing(context.result().getLocation());
        PollutionHelper.pollute(context.level(), pos, fluidStack);
    }
}
