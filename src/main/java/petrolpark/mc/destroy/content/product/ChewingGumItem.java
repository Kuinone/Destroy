package petrolpark.mc.destroy.content.product;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ChewingGumItem extends Item {

    public ChewingGumItem(Properties properties) {
        super(properties);
    }

    /** 1.21.1: {@link Item#getUseDuration(ItemStack, LivingEntity)} took a {@link LivingEntity} parameter.*/
    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 36000; // Half an hour
    }
}
