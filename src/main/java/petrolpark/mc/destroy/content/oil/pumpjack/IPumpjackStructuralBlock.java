package petrolpark.mc.destroy.content.oil.pumpjack;

import com.simibubi.create.api.equipment.goggles.IProxyHoveringInformation;
import com.simibubi.create.content.equipment.wrench.IWrenchable;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyVoxelShapes;

/**
 * Mix-in interface for structural Pumpjack blocks — the 4 visual-only cells surrounding a
 * {@link PumpjackBlock} controller. Uses the {@link Component} enum to tag which role each
 * structural cell plays (FRONT drilling-head / BACK counterweight / MIDDLE controller / TOP beam).
 * Centralizes the "my controller is adjacent via DirectionalBlock.FACING" resolution logic for
 * reuse by {@link PumpjackStructuralBlock} + {@link PumpjackCamBlock}.
*/
public interface IPumpjackStructuralBlock extends IProxyHoveringInformation, IWrenchable {

    public static final EnumProperty<Component> COMPONENT = EnumProperty.create("component", Component.class);

    public static VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (stillValid(level, pos, state)) {
            return DestroyVoxelShapes
                .getPumpJackShaper(state.getValue(COMPONENT))
                .get(level.getBlockState(getMaster(level, pos, state)).getValue(PumpjackBlock.FACING));
        }
        return DestroyVoxelShapes.BLOCK;
    }

    @Override
    public default InteractionResult onWrenched(BlockState state, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public default InteractionResult onSneakWrenched(BlockState state, UseOnContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();

        if (stillValid(level, clickedPos, state)) {
            BlockPos masterPos = getMaster(level, clickedPos, state);
            context = new UseOnContext(level,
                context.getPlayer(),
                context.getHand(),
                context.getItemInHand(),
                new BlockHitResult(
                    context.getClickLocation(),
                    context.getClickedFace(),
                    masterPos,
                    context.isInside()
                )
            );
            state = level.getBlockState(masterPos);
        }

        return IWrenchable.super.onSneakWrenched(state, context);
    }

    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (stillValid(level, pos, state)) level.destroyBlock(getMaster(level, pos, state), true);
    }

    public static void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (stillValid(level, pos, state)) {
            BlockPos masterPos = getMaster(level, pos, state);
            level.destroyBlockProgress(masterPos.hashCode(), masterPos, -1);
            if (!level.isClientSide() && player.isCreative()) level.destroyBlock(masterPos, false);
        }
    }

    public static BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos, Block thisBlock) {
        if (stillValid(level, currentPos, state)) {
            BlockPos masterPos = getMaster(level, currentPos, state);
            if (!level.getBlockTicks().hasScheduledTick(masterPos, DestroyBlocks.PUMPJACK.get())) {
                level.scheduleTick(masterPos, DestroyBlocks.PUMPJACK.get(), 1);
            }
            return state;
        }
        if (!(level instanceof Level realLevel) || realLevel.isClientSide()) return state;
        if (!realLevel.getBlockTicks().hasScheduledTick(currentPos, thisBlock))
            realLevel.scheduleTick(currentPos, thisBlock, 1);
        return state;
    }

    public static void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!stillValid(level, pos, state)) level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public default BlockPos getInformationSource(Level level, BlockPos pos, BlockState state) {
        return stillValid(level, pos, state) ? getMaster(level, pos, state) : pos;
    }

    /**
 * Get the position of the Pumpjack controller block to which this structural block is attached.
*/
    public static BlockPos getMaster(BlockGetter level, BlockPos pos, BlockState state) {
        Direction direction = state.getValue(DirectionalBlock.FACING);
        BlockPos targetedPos = pos.relative(direction);
        BlockState targetedState = level.getBlockState(targetedPos);
        // Recursion until we find the master
        if (targetedState.is(DestroyBlocks.PUMPJACK_STRUCTURAL.get()))
            return getMaster(level, targetedPos, targetedState);
        return targetedPos;
    }

    /**
 * Whether the controller Pumpjack block state still exists at the adjacent position this
 * structural block points to.
*/
    public static boolean stillValid(BlockGetter level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof IPumpjackStructuralBlock)) return false;
        Direction direction = state.getValue(DirectionalBlock.FACING);
        BlockPos targetedPos = pos.relative(direction);
        BlockState targetedState = level.getBlockState(targetedPos);
        return targetedState.getBlock() instanceof PumpjackBlock;
    }

    public enum Component implements StringRepresentable {
        FRONT, BACK, MIDDLE, TOP;

        @Override
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }
}
