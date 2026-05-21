package petrolpark.mc.destroy.content.product.periodictable;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * Item tint handler — reads {@link TankPeriodicTableBlockItem#getColor} for tintIndex 0.
 * Mirrors {@link TankPeriodicTableBlockColor} but for inventory/hotbar item renders.
 *
 * <p>Client-side registration via {@code RegisterColorHandlersEvent.Item} (1.21 NeoForge event).</p>
*/
public class TankPeriodicTableBlockItemColor implements ItemColor {

    public static final TankPeriodicTableBlockItemColor INSTANCE = new TankPeriodicTableBlockItemColor();

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex != 0) return -1;
        // use getBlock() approach so this works regardless of whether Registrate
        // produced a TankPeriodicTableBlockItem or a generic BlockItem.
        if (stack.getItem() instanceof BlockItem bi && bi.getBlock() instanceof TankPeriodicTableBlock tank) {
            return tank.color;
        }
        return -1;
    }
}
