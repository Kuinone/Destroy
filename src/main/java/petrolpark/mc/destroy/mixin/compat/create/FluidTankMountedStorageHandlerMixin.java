package petrolpark.mc.destroy.mixin.compat.create;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorage;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour.GeniusFluidTank;

/** Without this, transferring a Mixture into a tank that's
 * mounted on a contraption fails the same NBT-equality check.
*/
@Mixin(FluidTankMountedStorage.Handler.class)
public abstract class FluidTankMountedStorageHandlerMixin extends FluidTank {

    public FluidTankMountedStorageHandlerMixin(int capacity) {
        super(capacity);
    }

    @Unique
    private GeniusFluidTank destroy$geniusFluidTank;

    @Unique
    private GeniusFluidTank destroy$getGeniusFluidTank() {
        if (destroy$geniusFluidTank == null) {
            destroy$geniusFluidTank = new GeniusFluidTank(this.capacity, f -> {});
        }
        return destroy$geniusFluidTank;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        GeniusFluidTank tank = destroy$getGeniusFluidTank();
        tank.setFluid(fluid);
        int filled = tank.fill(resource, action);

        if (filled > 0) {
            fluid = tank.getFluid();
            onContentsChanged();
        }

        return filled;
    }

    /**
 * Fix Create bug — empty fluid (e.g., after train tank drained) sometimes carries non-EMPTY
 * fluid type with NBT data, causing {@link IllegalStateException} on stack operations.
 * Returning {@link FluidStack#EMPTY} when amount==0 normalises this.
*/
    @Override
    public @NotNull FluidStack getFluid() {
        return this.fluid.isEmpty() ? FluidStack.EMPTY : this.fluid;
    }
}
