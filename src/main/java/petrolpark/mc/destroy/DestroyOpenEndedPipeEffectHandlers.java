package petrolpark.mc.destroy;

import com.simibubi.create.api.effect.OpenPipeEffectHandler;

import net.minecraft.world.effect.MobEffectInstance;

import petrolpark.mc.destroy.core.fluid.openpipeeffect.BurningOpenEndedPipeEffectHandler;
import petrolpark.mc.destroy.core.fluid.openpipeeffect.EffectApplyingOpenEndedPipeEffectHandler;
import petrolpark.mc.destroy.core.pollution.PollutingOpenEndedPipeEffectHandler;

/**
 * Registers Destroy's Open-Ended Pipe {@link OpenPipeEffectHandler}s for fluids that should have
 * special-spray-AABB behavior (burn, effect application, pollute). Called from
 * {@link Destroy#Destroy} during mod construction so registry is populated before any pipe spray
 * effect is queried.
*/
public class DestroyOpenEndedPipeEffectHandlers {

    public static final void register() {
        // MIXTURE → PollutingOpenEndedPipeEffectHandler wired (pollution tracking
        // works; evaporation particle sync still TODO pending gasparticle subdir port).
        OpenPipeEffectHandler.REGISTRY.register(
            DestroyFluids.MIXTURE.getSource(),
            new PollutingOpenEndedPipeEffectHandler());

        OpenPipeEffectHandler.REGISTRY.register(
            DestroyFluids.NAPALM_SUNDAE.getSource(),
            new BurningOpenEndedPipeEffectHandler(DestroyFluids.NAPALM_SUNDAE.get()));

        OpenPipeEffectHandler.REGISTRY.register(
            DestroyFluids.MOLTEN_CINNABAR.getSource(),
            new BurningOpenEndedPipeEffectHandler(DestroyFluids.MOLTEN_CINNABAR.get()));

        OpenPipeEffectHandler.REGISTRY.register(
            DestroyFluids.PERFUME.getSource(),
            new EffectApplyingOpenEndedPipeEffectHandler(
                new MobEffectInstance(DestroyMobEffects.FRAGRANCE.getDelegate(), 21, 0, false, false, true),
                DestroyFluids.PERFUME.get()));

        OpenPipeEffectHandler.REGISTRY.register(
            DestroyFluids.UNDISTILLED_MOONSHINE.getSource(),
            new EffectApplyingOpenEndedPipeEffectHandler(
                new MobEffectInstance(DestroyMobEffects.INEBRIATION.getDelegate(), 21, 0, false, false, true),
                DestroyFluids.UNDISTILLED_MOONSHINE.get()));

        OpenPipeEffectHandler.REGISTRY.register(
            DestroyFluids.MOONSHINE.getSource(),
            new EffectApplyingOpenEndedPipeEffectHandler(
                new MobEffectInstance(DestroyMobEffects.INEBRIATION.getDelegate(), 21, 2, false, false, true),
                DestroyFluids.MOONSHINE.get()));
    }
}
