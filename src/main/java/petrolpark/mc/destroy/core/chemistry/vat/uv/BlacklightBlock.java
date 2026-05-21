package petrolpark.mc.destroy.core.chemistry.vat.uv;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyVoxelShapes;

/**
 * Blacklight UV lamp block — supplies UV light to an adjacent {@link
 * petrolpark.mc.destroy.core.chemistry.vat.Vat Vat} via the {@link IUVLampBlock} interface.
 * Can be placed on any face (6-direction FACING) + wrench-flipped to invert its 2-axis
 * orientation (FLIPPED property). Waterloggable.
 *
 * <p>**UV output semantics** (preserved 1:1): blacklight emits 100W UV out of the face opposite
 * to its {@code SIDE} property. All other faces output 0W. Adjacent Vat walls pick up the UV
 * via {@code IUVLampBlock.getUVPower(level, pos, face)} static dispatch.</p>
*/
public class BlacklightBlock extends Block implements IUVLampBlock, IWrenchable, TransformableBlock, ProperWaterloggedBlock {

    public static final MapCodec<BlacklightBlock> CODEC = simpleCodec(BlacklightBlock::new);

    public static final DirectionProperty SIDE = BlockStateProperties.FACING;
    public static final BooleanProperty FLIPPED = BooleanProperty.create("flipped");

    public BlacklightBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected MapCodec<BlacklightBlock> codec() {
        return CODEC;
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
        updateWater(level, state, currentPos);
        return state;
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return fluidState(state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FLIPPED) ? DestroyVoxelShapes.BLACKLIGHT_FLIPPED.get(state.getValue(SIDE)) : DestroyVoxelShapes.BLACKLIGHT.get(state.getValue(SIDE));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 3;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        boolean flipped = context.getClickedFace().getAxis() == Axis.Y && context.getHorizontalDirection().getAxis() == Axis.X;
        return withWater(defaultBlockState()
            .setValue(SIDE, context.getClickedFace().getOpposite())
            .setValue(FLIPPED, flipped),
            context);
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SIDE, FLIPPED, WATERLOGGED);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(FLIPPED, !state.getValue(FLIPPED)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public float getUVPower(Level level, BlockState blockState, BlockPos blockPos, Direction face) {
        if (face == blockState.getValue(SIDE).getOpposite()) return 100f;
        return 0f;
    }

    @Override
    public BlockState transform(BlockState state, StructureTransform transform) {
        // subsystem polish session. Returning unchanged state is Create's TransformableBlock
        // default behavior (block moves but orientation stays with world coordinates).
        return state;
    }
}
