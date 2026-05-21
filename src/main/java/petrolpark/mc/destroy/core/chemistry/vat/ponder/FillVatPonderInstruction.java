package petrolpark.mc.destroy.core.chemistry.vat.ponder;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;

/**
 * One-shot {@link PonderInstruction} that adds a {@link FluidStack} to a
 * {@link petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity VatControllerBlockEntity}
 * at the given {@link BlockPos} in a Ponder scene world. Used by Vat Ponder scenes (future S??
 * vatFluids / vatReading) to demonstrate filling a Vat with a specific fluid mixture.
*/
public class FillVatPonderInstruction extends PonderInstruction {

    public final BlockPos vatControllerPos;
    public final FluidStack fillStack;

    public FillVatPonderInstruction(BlockPos vatControllerPos, FluidStack fillStack) {
        this.vatControllerPos = vatControllerPos;
        this.fillStack = fillStack;
    }

    @Override
    public boolean isComplete() {
        return true;
    }

    @Override
    public void tick(PonderScene scene) {
        scene.getWorld().getBlockEntity(vatControllerPos, DestroyBlockEntityTypes.VAT_CONTROLLER.get()).ifPresent(vat ->
            vat.addFluid(fillStack, FluidAction.SIMULATE)
        );
    }
}
