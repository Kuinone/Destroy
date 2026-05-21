package petrolpark.mc.destroy.content.processing.cooler;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyVoxelShapes;
import petrolpark.mc.destroy.content.processing.cooler.CoolerBlockEntity.ColdnessLevel;

/**
 * Refrigerstrayter (Cooler) — a block that sits beneath a Basin and applies "frosting" heat level
 * to the stack. Frosting is not a vanilla Create HeatLevel value; Destroy injects it via mixin
 *.
*/
public class CoolerBlock extends Block implements IBE<CoolerBlockEntity>, IWrenchable {

    public static final MapCodec<CoolerBlock> CODEC = simpleCodec(CoolerBlock::new);

    public static final EnumProperty<ColdnessLevel> COLD_LEVEL = EnumProperty.create("breeze", ColdnessLevel.class);

    public CoolerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
            .setValue(COLD_LEVEL, ColdnessLevel.IDLE)
            .setValue(BlazeBurnerBlock.HEAT_LEVEL, HeatLevel.NONE));
    }

    @Override
    protected MapCodec<CoolerBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COLD_LEVEL, BlazeBurnerBlock.HEAT_LEVEL);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        withBlockEntityDo(level, pos, be -> be.updateHeatLevel(state.getValue(COLD_LEVEL)));

        if (level.isClientSide()) return;
        BlockEntity blockEntity = level.getBlockEntity(pos.above());
        if (!(blockEntity instanceof BasinBlockEntity basin)) return;
        basin.notifyChangeOfContents();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult bhr) {
        if (!AllItems.CREATIVE_BLAZE_CAKE.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        withBlockEntityDo(level, pos, cooler -> {
            if (getColdnessLevelOf(state) == ColdnessLevel.FROSTING) {
                cooler.coolingTicks = 0;
                cooler.setColdnessOfBlock(ColdnessLevel.IDLE);
            } else {
                cooler.coolingTicks = Integer.MAX_VALUE;
                cooler.setColdnessOfBlock(ColdnessLevel.FROSTING);
            }
        });
        if (!player.isCreative()) stack.shrink(1);
        player.setItemInHand(hand, stack);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyVoxelShapes.COOLER;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context == CollisionContext.empty()) return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
        return getShape(state, level, pos, context);
    }

    public static ColdnessLevel getColdnessLevelOf(BlockState state) {
        return state.getValue(COLD_LEVEL);
    }

    @Override
    public Class<CoolerBlockEntity> getBlockEntityClass() {
        return CoolerBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CoolerBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.COOLER.get();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
