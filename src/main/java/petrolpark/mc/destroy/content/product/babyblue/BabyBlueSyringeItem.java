package petrolpark.mc.destroy.content.product.babyblue;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroySubstancesConfigs;
import petrolpark.mc.destroy.content.tool.syringe.SyringeItem;

/**
 * Baby Blue syringe — injects {@link DestroyMobEffects#BABY_BLUE_HIGH} for a fixed duration &amp;
 * amplifier configured at item registration. Gated by
 * {@link DestroySubstancesConfigs#babyBlueEnabled()} static check (server-config disables the
 * whole "baby blue" drug addiction subsystem if false).
*/
public class BabyBlueSyringeItem extends SyringeItem {

    private final int duration;
    private final int amplifier;

    public BabyBlueSyringeItem(Properties properties, int babyBlueEffectDuration, int babyBlueEffectAmplifier) {
        super(properties);
        this.duration = babyBlueEffectDuration;
        this.amplifier = babyBlueEffectAmplifier;
    }

    
    @Override
    public int getTintColor(int layer) {
        if (layer == 0) return 0xFF86A8CA;
        return 0xFFFFFFFF;
    }

    @Override
    public void onInject(ItemStack itemStack, Level level, LivingEntity target) {
        if (DestroySubstancesConfigs.babyBlueEnabled()) {
            target.addEffect(new MobEffectInstance(DestroyMobEffects.BABY_BLUE_HIGH.getDelegate(), duration, amplifier));
        }
    }
}
