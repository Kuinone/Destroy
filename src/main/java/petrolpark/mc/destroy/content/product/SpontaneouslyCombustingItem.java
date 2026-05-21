package petrolpark.mc.destroy.content.product;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;

/**
 * Base class for items that spontaneously combust in the player's inventory / on the ground.

 *
 * <p>1.21 迁移要点：{@code entity.setSecondsOnFire(n)} 重命名 → {@link Entity#igniteForSeconds(float)}
 * （float 参数，精度升级）。行为不变。</p>
*/
public class SpontaneouslyCombustingItem extends Item {

    public SpontaneouslyCombustingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.getRandom().nextInt(1200) == 0) {
            stack.shrink(1);
            entity.igniteForSeconds(3);
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level().getRandom().nextInt(1200) == 0) {
            stack.shrink(1);
            BlockPos pos = BlockPos.containing(entity.position());
            entity.level().setBlockAndUpdate(pos, BaseFireBlock.getState(entity.level(), pos));
        }
        return super.onEntityItemUpdate(stack, entity);
    }
}
