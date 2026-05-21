package petrolpark.mc.destroy.content.sandcastle;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Sand castle BE — tiny wrapper that hosts one {@link SentimentalBehaviour}. Behaviour tracks
 * the baby villager "owner" so jumping/breaking the castle can trigger their crying mob effect
 * and degrade gossip.
*/
public class SandCastleBlockEntity extends SmartBlockEntity {

    public SentimentalBehaviour sentimentalBehaviour;

    public SandCastleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        sentimentalBehaviour = new SentimentalBehaviour(this);
        behaviours.add(sentimentalBehaviour);
    }
}
