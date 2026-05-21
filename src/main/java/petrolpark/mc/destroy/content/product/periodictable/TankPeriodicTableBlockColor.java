package petrolpark.mc.destroy.content.product.periodictable;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block tint handler — returns {@link TankPeriodicTableBlock#color} for tintIndex 0 to color
 * the "fluid" inside the glass-like block model.
*/
public class TankPeriodicTableBlockColor implements BlockColor {

    public static final TankPeriodicTableBlockColor INSTANCE = new TankPeriodicTableBlockColor();

    @Override
    public int getColor(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        // drop level/pos null check; some 1.21 paths pass null and we want the tint anyway.
        if (tintIndex != 0) return -1;
        Block block = state.getBlock();
        if (!(block instanceof TankPeriodicTableBlock tankBlock)) return -1;
        return tankBlock.color;
    }
}
