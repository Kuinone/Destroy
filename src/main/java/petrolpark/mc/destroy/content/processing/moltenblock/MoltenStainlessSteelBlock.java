package petrolpark.mc.destroy.content.processing.moltenblock;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyItems;

public class MoltenStainlessSteelBlock extends AbstractMoltenBlock {

    public MoltenStainlessSteelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Item asItem() {
        return DestroyItems.MOLTEN_STAINLESS_STEEL_BUCKET.get();
    }

    @Override
    public BlockState getSolidifiedBlockState() {
        return DestroyBlocks.STAINLESS_STEEL_BLOCK.getDefaultState();
    }
}
