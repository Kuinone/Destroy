package petrolpark.mc.destroy.content.product;

import java.util.Set;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.EffectCures;
import petrolpark.mc.destroy.core.item.DrinkItem;

public class MilkCartonItem extends DrinkItem {

    public MilkCartonItem(Properties properties) {
        super(properties);
    }

    /**
 * 1.21.1 note: {@code MobEffectInstance.isCurativeItem(ItemStack)} was removed. NeoForge replaced
 * item-based curing with {@link net.neoforged.neoforge.common.EffectCure} tokens; {@link EffectCures#MILK}
 * represents the milk-bucket cure. {@link MobEffectInstance#getCures()} exposes the set of cures
 * that the effect is vulnerable to.
*/
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Set<MobEffectInstance> effects = Set.copyOf(livingEntity.getActiveEffects());
        for (MobEffectInstance effect : effects) {
            if (effect.getCures().contains(EffectCures.MILK) && effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                livingEntity.removeEffect(effect.getEffect());
            }
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }
}
