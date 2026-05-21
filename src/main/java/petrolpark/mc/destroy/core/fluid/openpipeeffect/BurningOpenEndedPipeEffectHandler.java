package petrolpark.mc.destroy.core.fluid.openpipeeffect;

import com.simibubi.create.impl.effect.LavaEffectHandler;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Create {@link LavaEffectHandler} variant that only burns entities when the AABB is filled
 * with a specific Destroy fluid (e.g. NAPALM_SUNDAE, MOLTEN_CINNABAR). The vanilla lava handler
 * accepts any fluid; this variant adds a per-handler fluid identity gate.
*/
public class BurningOpenEndedPipeEffectHandler extends LavaEffectHandler {

    protected final Fluid fluid;

    public BurningOpenEndedPipeEffectHandler(Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        if (!canApplyEffects(fluid)) return;
        super.apply(level, area, fluid);
    }

    public boolean canApplyEffects(FluidStack fluid) {
        return fluid.getFluid().isSame(this.fluid);
    }
}
