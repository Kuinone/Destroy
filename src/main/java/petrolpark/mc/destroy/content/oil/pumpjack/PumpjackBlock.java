package petrolpark.mc.destroy.content.oil.pumpjack;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyVoxelShapes;

/**
 * The Pumpjack controller block — {@link HorizontalDirectionalBlock} with an IBE-bound
 * {@link PumpjackBlockEntity} + {@link IWrenchable} (PASS-through wrench; structural cells
 * delegate rotation handling to their master via IPumpjackStructuralBlock.onSneakWrenched).
 * Auto-layouts a 4-cell multi-block on place (drilling head + counter-weight + top beam +
 * center controller).
*/
public class PumpjackBlock extends HorizontalDirectionalBlock implements IBE<PumpjackBlockEntity>, IWrenchable {

    public static final MapCodec<PumpjackBlock> CODEC = simpleCodec(PumpjackBlock::new);

    public PumpjackBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<PumpjackBlock> codec() {
        return CODEC;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.FAIL;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    public static Direction getFacing(BlockState state) {
        return state.getValue(FACING);
    }

    public static BlockPos getCamPos(BlockState state, BlockPos pos) {
        return pos.relative(getFacing(state), -1);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getCounterClockWise(Axis.Y);
        for (BlockPos pos : getStructuralBlocks(facing, context.getClickedPos()).keySet()) {
            // Don't place if the structural Blocks won't be able to fit
            if (!context.getLevel().getBlockState(pos).canBeReplaced()) return null;
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyVoxelShapes.getPumpJackShaper(IPumpjackStructuralBlock.Component.MIDDLE).get(state.getValue(FACING));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);

        // Check all the structural blocks are still present
        for (Entry<BlockPos, BlockState> entry : getStructuralBlocks(facing, pos).entrySet()) {
            BlockState occupiedState = level.getBlockState(entry.getKey());
            BlockState requiredState = entry.getValue();
            if (occupiedState == requiredState) continue;
            if (!occupiedState.canBeReplaced()) {
                level.destroyBlock(pos, false);
                return;
            }
            level.setBlockAndUpdate(entry.getKey(), requiredState);
        }
    }

    /**
 * Map of locations of structural Blocks to the Block States they should be.
 * 4-cell layout: controller (c), drilling-head (1, FRONT), top beam (2, TOP), counter-weight
 * (3, BACK via PumpjackCam).
*/
    public Map<BlockPos, BlockState> getStructuralBlocks(Direction facing, BlockPos pos) {
        return Map.of(
            pos.relative(facing, 1),
                DestroyBlocks.PUMPJACK_CAM.getDefaultState()
                    .setValue(DirectionalBlock.FACING, facing.getOpposite())
                    .setValue(IPumpjackStructuralBlock.COMPONENT, IPumpjackStructuralBlock.Component.BACK)
                    .setValue(RotatedPillarKineticBlock.AXIS, facing.getClockWise(Axis.Y).getAxis()),
            pos.above(),
                DestroyBlocks.PUMPJACK_STRUCTURAL.getDefaultState()
                    .setValue(DirectionalBlock.FACING, Direction.DOWN)
                    .setValue(IPumpjackStructuralBlock.COMPONENT, IPumpjackStructuralBlock.Component.TOP),
            pos.relative(facing.getOpposite(), 1),
                DestroyBlocks.PUMPJACK_STRUCTURAL.getDefaultState()
                    .setValue(DirectionalBlock.FACING, facing)
                    .setValue(IPumpjackStructuralBlock.COMPONENT, IPumpjackStructuralBlock.Component.FRONT)
        );
    }

    @Override
    public Class<PumpjackBlockEntity> getBlockEntityClass() {
        return PumpjackBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends PumpjackBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.PUMPJACK.get();
    }

}
