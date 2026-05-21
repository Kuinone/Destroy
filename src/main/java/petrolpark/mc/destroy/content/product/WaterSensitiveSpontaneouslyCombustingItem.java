package petrolpark.mc.destroy.content.product;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import petrolpark.mc.destroy.core.explosion.SmartExplosion;

/**
 * 水/雨遇水即爆的活泼金属。用于 SODIUM_HYDRIDE（氢化钠）。入水 → {@link SmartExplosion} radius=2
 * irregularity=0.7 + 销毁 stack。
*/
public class WaterSensitiveSpontaneouslyCombustingItem extends SpontaneouslyCombustingItem {

    public WaterSensitiveSpontaneouslyCombustingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (checkForWater(stack, entity, isSelected)) stack.setCount(0);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (checkForWater(stack, entity, true)) entity.kill();
        return super.onEntityItemUpdate(stack, entity);
    }

    /**
 * @return {@code true} if water contact was detected and an explosion was triggered (caller
 * should consume the stack / kill the carrying item entity).
*/
    public boolean checkForWater(ItemStack stack, Entity entity, boolean rainSensitive) {
        boolean submerged = entity.isInWaterOrBubble();
        boolean rainHit = entity.isInWaterRainOrBubble()
            && (rainSensitive
                || (entity instanceof LivingEntity le && le.getOffhandItem() == stack));
        if (!submerged && !rainHit) return false;

        SmartExplosion.explode(entity.level(),
            new SmartExplosion(entity.level(), entity, null, null, entity.position(), 2f, 0.7f));
        return true;
    }
}
