package petrolpark.mc.destroy.content.logistics.siphon;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;

/**
 * Siphon — redstone-triggered "drain N mB of the next fluid pulled through" block. On redstone
 * rising edge, increments the BE's {@code leftToDrain} by the configured per-pulse amount; a
 * downstream fluid pump's extraction stops when the counter reaches 0.
*/
public class SiphonBlock extends Block implements IBE<SiphonBlockEntity>, IWrenchable {

    public static final MapCodec<SiphonBlock> CODEC = simpleCodec(SiphonBlock::new);

    public SiphonBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<SiphonBlock> codec() {
        return CODEC;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.FAIL;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.POWERED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.POWERED,
            Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide()) {
            boolean flag = state.getValue(BlockStateProperties.POWERED);
            if (flag != level.hasNeighborSignal(pos)) {
                if (flag) {
                    level.scheduleTick(pos, this, 2);
                } else {
                    level.setBlock(pos, state.cycle(BlockStateProperties.POWERED), 2);
                    incrementDrainAmount(level, pos);
                }
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(BlockStateProperties.POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(BlockStateProperties.POWERED), 2);
        }
    }

    public void incrementDrainAmount(Level level, BlockPos pos) {
        withBlockEntityDo(level, pos, be -> {
            be.leftToDrain += be.settings.getValue();
        });
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, worldIn, pos, newState);
    }

    @Override
    public Class<SiphonBlockEntity> getBlockEntityClass() {
        return SiphonBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SiphonBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.SIPHON.get();
    }
}
