package petrolpark.mc.destroy.mixin.compat.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;

import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyFluids;

/**
 *
 * <p>{@link FluidTransportBehaviour#tick} aggregates flows from multiple {@link
 * com.simibubi.create.content.fluids.PipeConnection PipeConnection}s into a single
 * "available flow" for the pipe segment. If two incoming flows have different fluid
 * identities, the pipe records a "colliding flow" and triggers {@code FluidReactions
 * .handlePipeFlowCollision} (which spits the fluid out as world fluid / drops it).</p>
 *
 * <p>For {@code destroy:mixture} fluids carrying different chemistry payloads, this collision
 * detection kicks in spuriously — two mixtures meeting in a pipe segment should merge (like
 * they would in a tank), not collide as incompatible fluids. Relax the comparison so any
 * two mixture stacks are treated as the same flow → pipe routes them through normally,
 * destination tank's {@link
 * petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour.GeniusFluidTank#fill} does the
 * proper molar-weighted merge.</p>
 *
 * <p>See {@link FluidNetworkMixin} javadoc for the user-reported symptom that triggered this
 * mixin family (3-tank pump chain stalling B→C while A→B is active).</p>
*/
@Mixin(FluidTransportBehaviour.class)
public abstract class FluidTransportBehaviourMixin {

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/neoforged/neoforge/fluids/FluidStack;isSameFluidSameComponents(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z"
        ),
        remap = false
    )
    private static boolean destroy$relaxMixtureIdentity(FluidStack a, FluidStack b, Operation<Boolean> original) {
        if (DestroyFluids.isMixture(a) && DestroyFluids.isMixture(b)) {
            return true;
        }
        return original.call(a, b);
    }
}
