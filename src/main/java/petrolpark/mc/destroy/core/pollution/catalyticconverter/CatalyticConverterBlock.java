package petrolpark.mc.destroy.core.pollution.catalyticconverter;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyVoxelShapes;

/**
 * Catalytic Converter — directional fluid-consuming block that dumps polluting fluids into the
 * environment with a reduction multiplier (vs. releasing them raw). Upward-face default for
 * vertical exhaust pipes.
*/
public class CatalyticConverterBlock extends DirectionalBlock implements IBE<CatalyticConverterBlockEntity>, IWrenchable {

    public static final MapCodec<CatalyticConverterBlock> CODEC = simpleCodec(CatalyticConverterBlock::new);

    public CatalyticConverterBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<CatalyticConverterBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyVoxelShapes.CATALYTIC_CONVERER.get(state.getValue(FACING));
    }

    @Override
    public Class<CatalyticConverterBlockEntity> getBlockEntityClass() {
        return CatalyticConverterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CatalyticConverterBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.CATALYTIC_CONVERTER.get();
    }
}
