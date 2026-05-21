package petrolpark.mc.destroy.core.chemistry.storage.measuringcylinder;

import java.util.List;

import net.createmod.catnip.data.Couple;
import org.joml.Vector3f;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.core.chemistry.storage.SimpleMixtureTankBlockEntity;

/**
 * BE for MeasuringCylinder · same as SimpleMixtureTankBlockEntity but with fixed dimensions
 * specific to the measuring-cylinder model. Capacity from server config.
*/
public class MeasuringCylinderBlockEntity extends SimpleMixtureTankBlockEntity {

    public static final Couple<Vector3f> FLUID_BOX_DIMENSIONS =
        Couple.create(new Vector3f(6.5f, 2.5f, 6.5f), new Vector3f(9.5f, 11f, 9.5f));

    public MeasuringCylinderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        tank.setCapacity(DestroyAllConfigs.SERVER.blocks.measuringCylinderCapacity.get());
    }

    @Override
    public Couple<Vector3f> getFluidBoxDimensions() {
        return FLUID_BOX_DIMENSIONS;
    }
}
