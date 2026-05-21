package petrolpark.mc.destroy.content.oil.pumpjack;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

/**
 * {@link BlockItem} for the Pumpjack — shows a visual hint (outline) around the 4-cell layout
 * when placement fails due to one of the surrounding cells being blocked.
*/
public class PumpjackBlockItem extends BlockItem {

    public PumpjackBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        InteractionResult result = super.place(context);
        // TODO — port showBounds via petrolpark-library OUTLINER audit
        // if (result == InteractionResult.FAIL && context.getLevel().isClientSide()) showBounds(context);
        return result;
    }
}
