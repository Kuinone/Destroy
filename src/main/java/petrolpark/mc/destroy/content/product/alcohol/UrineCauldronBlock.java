package petrolpark.mc.destroy.content.product.alcohol;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.DestroyCauldronInteractions;

/**
 * Full-level cauldron that holds urine (destroy:urine virtual fluid). Dropping a glass bottle in
 * interacts via {@link DestroyCauldronInteractions#URINE} to produce a {@code URINE_BOTTLE}; see
 * that class for the scripted interaction.
*/
public class UrineCauldronBlock extends AbstractCauldronBlock {

    public static final MapCodec<UrineCauldronBlock> CODEC =
        simpleCodec(p -> new UrineCauldronBlock(p, DestroyCauldronInteractions.URINE));

    public UrineCauldronBlock(Properties properties, CauldronInteraction.InteractionMap interactions) {
        super(properties, interactions);
    }

    @Override
    protected MapCodec<UrineCauldronBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean isFull(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return 2;
    }
}
