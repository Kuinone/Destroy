package petrolpark.mc.destroy.content.processing.extrusion;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;

/**
 * Trivial BE that exists solely to host a {@link DestroyAdvancementBehaviour} for the EXTRUDE
 * advancement. The ExtrudableMovementBehaviour looks up this BE when a block is pushed through
 * the die and awards the advancement to the contraption's owner.
*/
public class ExtrusionDieBlockEntity extends SmartBlockEntity {

    DestroyAdvancementBehaviour advancementBehaviour;

    public ExtrusionDieBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.EXTRUDE);
        behaviours.add(advancementBehaviour);
    }
}
