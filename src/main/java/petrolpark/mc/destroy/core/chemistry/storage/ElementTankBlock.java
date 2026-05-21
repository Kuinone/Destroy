package petrolpark.mc.destroy.core.chemistry.storage;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;

/**
 * Block holding an {@link ElementTankBlockEntity} — a small-capacity fluid tank that converts the
 * held fluid into a block when the fluid matches an {@code ELEMENT_TANK_FILLING} recipe. Used to
 * produce periodic-table displays (filled gas/liquid element samples).
*/
public class ElementTankBlock extends HorizontalDirectionalBlock implements IBE<ElementTankBlockEntity> {

    /** 1.21 Block abstract codec(). {@code simpleCodec} covers the Properties-only ctor.*/
    public static final MapCodec<ElementTankBlock> CODEC = simpleCodec(ElementTankBlock::new);

    public ElementTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public Class<ElementTankBlockEntity> getBlockEntityClass() {
        return ElementTankBlockEntity.class;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        IBE.onRemove(pState, pLevel, pPos, pState);
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public BlockEntityType<? extends ElementTankBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.ELEMENT_TANK.get();
    }
}
