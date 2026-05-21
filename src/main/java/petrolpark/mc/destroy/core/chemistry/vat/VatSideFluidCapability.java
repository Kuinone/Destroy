package petrolpark.mc.destroy.core.chemistry.vat;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import petrolpark.mc.destroy.DestroyFluids;
import petrolpark.mc.destroy.core.chemistry.vat.VatControllerBlockEntity.VatTankWrapper;

/**
 * {@link VatTankWrapper} subclass for a Vat side-cell's fluid I/O — exposes 3 inner handlers
 * (liquid output / gas output / input buffer) + dispatches drain calls by pipe-submerged state
 * (submerged → drain liquid, emerged → drain gas with air molar density).
*/
public class VatSideFluidCapability extends VatTankWrapper {

    private final VatSideBlockEntity vatSide;

    public IFluidHandler getLiquidOutput() {
        return itemHandler[0];
    }

    public IFluidHandler getGasOutput() {
        return itemHandler[1];
    }

    public IFluidHandler getInput() {
        return itemHandler[2];
    }

    public VatSideFluidCapability(VatSideBlockEntity vatSide, IFluidHandler liquidOutput, IFluidHandler gasOutput, IFluidHandler input) {
        super(vatSide::getController, input::fill, liquidOutput, gasOutput, input);
        this.vatSide = vatSide;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return vatSide.isPipeSubmerged(false, null)
            ? drainLiquidTank(maxDrain, action)
            : drainGasTankWithMolarDensity(maxDrain, DestroyFluids.AIR_MOLAR_DENSITY, action);
    }
}
