package petrolpark.mc.destroy.mixin.compat.create;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.PipeConnection;

import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyFluids;

/** {@link PipeConnection#manageFlows} verifies
 * that the source's currently-provided fluid still matches the connection's tracked
 * {@code flow.fluid} via {@link FluidStack#isSameFluidSameComponents}; mismatch resets the
 * flow ({@code this.flow = Optional.empty()}).
 *
 * <p>For {@code destroy:mixture} fluids, components mutate every fill (weighted-merge of
 * different mixture stacks). Without this relaxation, a pipe connection between two tanks
 * carrying different mixtures resets its flow each tick → no progress.</p>
 *
 * <p>See {@link FluidNetworkMixin} javadoc for the broader user-reported symptom and root
 * cause analysis.</p>
*/
@Mixin(PipeConnection.class)
public abstract class PipeConnectionMixin {

    @WrapOperation(
        method = "manageFlows",
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
