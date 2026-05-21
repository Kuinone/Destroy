package petrolpark.mc.destroy.content.oil.pumpjack;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyBlocks;

/**
 * The counter-weight cell of a {@link PumpjackBlock} — kinetic ({@link AbstractShaftBlock}, has
 * a {@link PumpjackCamBlockEntity}) so it accepts Create shaft rotation input. Also an
 * {@link IPumpjackStructuralBlock} so it participates in the master-resolution + auto-destroy
 * chain.
*/
public class PumpjackCamBlock extends AbstractShaftBlock implements IPumpjackStructuralBlock {

    public static final MapCodec<PumpjackCamBlock> CODEC = simpleCodec(PumpjackCamBlock::new);

    public PumpjackCamBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<PumpjackCamBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMPONENT, DirectionalBlock.FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return IPumpjackStructuralBlock.getShape(state, level, pos, context);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    /**
 * 1.21 Block.getCloneItemStack signature: LevelReader.
*/
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return DestroyBlocks.PUMPJACK.asStack();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        level.removeBlockEntity(pos);
        IPumpjackStructuralBlock.onRemove(state, level, pos, newState, isMoving);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
        return true;
    }

    /**
 * 1.21 Block.playerWillDestroy returns BlockState.
*/
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        IPumpjackStructuralBlock.playerWillDestroy(level, pos, state, player);
        return super.playerWillDestroy(level, pos, state, player);
    }

    /**
 * 1.21 AbstractShaftBlock.updateShape is public.
*/
    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        updateWater(level, state, currentPos);
        return IPumpjackStructuralBlock.updateShape(state, facing, facingState, level, currentPos, facingPos, this);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        IPumpjackStructuralBlock.tick(state, level, pos, random);
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.PUMPJACK_CAM.get();
    }
}
