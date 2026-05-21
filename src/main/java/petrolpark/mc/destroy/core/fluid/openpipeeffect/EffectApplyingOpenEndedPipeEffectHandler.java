package petrolpark.mc.destroy.core.fluid.openpipeeffect;

import java.util.List;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Applies a fixed {@link MobEffectInstance} to all potion-affected living entities inside an
 * Open-Ended Pipe's spray AABB — throttled to once every 5 game ticks to avoid spamming effects
 * on per-tick. Used by Destroy fluids like PERFUME (FRAGRANCE effect), UNDISTILLED_MOONSHINE /
 * MOONSHINE (INEBRIATION effect).
*/
public class EffectApplyingOpenEndedPipeEffectHandler implements OpenPipeEffectHandler {

    protected final MobEffectInstance effect;
    @SuppressWarnings("unused")
    protected final Fluid fluid;

    public EffectApplyingOpenEndedPipeEffectHandler(MobEffectInstance effect, Fluid fluid) {
        this.effect = effect;
        this.fluid = fluid;
    }

    @Override
    public void apply(Level level, AABB area, FluidStack fluid) {
        if (level.getGameTime() % 5 != 0) return;
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions);
        for (LivingEntity entity : entities) entity.addEffect(new MobEffectInstance(effect));
    }
}
