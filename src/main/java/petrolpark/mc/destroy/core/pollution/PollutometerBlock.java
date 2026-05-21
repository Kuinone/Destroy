package petrolpark.mc.destroy.core.pollution;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroyVoxelShapes;
import petrolpark.mc.destroy.core.pollution.pollutometer.PollutometerBlockEntity;

/**
 * Pollutometer Block — wrenchable directional readout block whose top-face anemometer/weathervane
 * spins based on pollution level of the player-selected {@link PollutometerBlockEntity#getSelector()
 * pollution type}. Powered by Display Link to broadcast progress via PercentOrProgressBarDisplaySource.
*/
public class PollutometerBlock extends Block implements IBE<PollutometerBlockEntity>, IWrenchable {

    // was BlockStateProperties.FACING (6 directions). Switched to HORIZONTAL_FACING
    // (4 directions) because the blockstate JSON only declares variants for N/E/S/W; placing
    // with `facing=up` or `facing=down` (e.g. when looking straight up/down at sky/floor)
    // falls back to vanilla's missing model → black/purple checker.
    // same blockstate JSON (4 horizontal variants only) so the 6-direction property was a port
    // bug — the rocker/anemometer top + base only makes sense horizontally placed anyway.
    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

    public PollutometerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(DIRECTION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return DestroyVoxelShapes.POLLUTOMETER.get(state.getValue(DIRECTION));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // getHorizontalDirection() always returns one of N/E/S/W (never UP/DOWN), so
        // placement when the player looks straight up/down still picks the nearest horizontal.
        return defaultBlockState().setValue(DIRECTION, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public Class<PollutometerBlockEntity> getBlockEntityClass() {
        return PollutometerBlockEntity.class;
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<? extends PollutometerBlockEntity> getBlockEntityType() {
        return DestroyBlockEntityTypes.POLLUTOMETER.get();
    }
}
