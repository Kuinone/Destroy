package petrolpark.mc.destroy.content.product;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.content.tool.syringe.SyringeItem;

/**
 * Cisplatin syringe — removes {@link DestroyMobEffects#CANCER} from the target. Cancer is the
 * {@code UncurableMobEffect} applied by long-term radiation exposure; cisplatin is the only way
 * to remove it once contracted.
*/
public class CisplatinSyringeItem extends SyringeItem {

    public CisplatinSyringeItem(Properties properties) {
        super(properties);
    }

    
    @Override
    public int getTintColor(int layer) {
        if (layer == 0) return 0xFFAEDFDB;
        return 0xFFFFFFFF;
    }

    @Override
    public void onInject(ItemStack itemStack, Level level, LivingEntity target) {
        target.removeEffect(DestroyMobEffects.CANCER.getDelegate());
    }
}
