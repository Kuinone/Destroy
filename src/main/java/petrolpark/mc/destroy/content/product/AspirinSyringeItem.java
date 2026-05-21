package petrolpark.mc.destroy.content.product;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.content.tool.syringe.SyringeItem;

/**
 * Aspirin syringe — heals the target + removes {@link DestroyMobEffects#HANGOVER}. If the removal
 * succeeded (the target actually had hangover), the injecting player gets the CURE_HANGOVER
 * advancement.
*/
public class AspirinSyringeItem extends SyringeItem {

    public AspirinSyringeItem(Properties properties) {
        super(properties);
    }

    
    @Override
    public int getTintColor(int layer) {
        if (layer == 0) return 0xFFFF1968;
        return 0xFFFFFFFF;
    }

    @Override
    public void onInject(ItemStack itemStack, Level level, LivingEntity target) {
        target.heal(DestroyConfigs.server().substances.aspirinHeal.getF());
        if (!target.removeEffect(DestroyMobEffects.HANGOVER.getDelegate())) return;
        if (target instanceof Player player) {
            DestroyAdvancementTrigger.CURE_HANGOVER.award(level, player);
        }
    }
}
