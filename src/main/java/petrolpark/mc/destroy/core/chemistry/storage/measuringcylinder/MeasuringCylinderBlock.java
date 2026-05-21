package petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyVoxelShapes;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.chemistry.storage.PlaceableMixtureTankBlock;

/**
 1.21 migrations:
*/
public class MeasuringCylinderBlock extends PlaceableMixtureTankBlock<MeasuringCylinderBlockEntity> {

    public static final MapCodec<MeasuringCylinderBlock> CODEC = simpleCodec(MeasuringCylinderBlock::new);

    public MeasuringCylinderBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends PlaceableMixtureTankBlock<MeasuringCylinderBlockEntity>> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyVoxelShapes.MEASURING_CYLINDER;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        return MeasuringCylinderBlockItem.tryOpenTransferScreen(level, pos, state, hit.getDirection(),
            player, hand, stack, dynamicShape);
    }

    @Override
    public int getMixtureCapacity() {
        return DestroyAllConfigs.SERVER.blocks.measuringCylinderCapacity.get();
    }

    @Override
    public Class<MeasuringCylinderBlockEntity> getBlockEntityClass() {
        return MeasuringCylinderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MeasuringCylinderBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.MEASURING_CYLINDER.get();
    }
}
