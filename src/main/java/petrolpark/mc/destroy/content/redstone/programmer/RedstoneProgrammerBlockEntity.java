package petrolpark.mc.destroy.content.redstone.programmer;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Thin {@link SmartBlockEntity} wrapper that hosts {@link RedstoneProgrammerBehaviour}. Delegates
 * all logic to the behaviour; class exists for Create's BlockEntityType registration requirement.
*/
public class RedstoneProgrammerBlockEntity extends SmartBlockEntity {

    public RedstoneProgrammerBehaviour programmer;

    public RedstoneProgrammerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // Sidestep RedstoneProgrammerBlock dependency (not ported yet) — BlockStateProperties.POWERED
        // is the exact same BooleanProperty that RedstoneProgrammerBlock.POWERED aliases.
        programmer = new RedstoneProgrammerBehaviour(this,
            () -> getBlockState().getValue(BlockStateProperties.POWERED));
        behaviours.add(programmer);
    }
}
