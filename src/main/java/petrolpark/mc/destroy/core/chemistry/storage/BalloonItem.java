package petrolpark.mc.destroy.core.chemistry.storage;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Gas-storage balloon — 500 mB capacity. Primary use: capture gas-phase Mixtures from Vat gas
 * tanks + transport them.
*/
public class BalloonItem extends Item implements IMixtureStorageItem {

    public static final int CAPACITY = 500;

    public BalloonItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public int getCapacity(ItemStack stack) {
        return CAPACITY;
    }

    @Override
    public Component getNameRegardlessOfFluid(ItemStack stack) {
        return Component.translatable(this.getDescriptionId());
    }

    @Override
    public Component getName(ItemStack stack) {
        return getNameWithFluid(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return IMixtureStorageItem.defaultUseOn(this, context);
    }
}
