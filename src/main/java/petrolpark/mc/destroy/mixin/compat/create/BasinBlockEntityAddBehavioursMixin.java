package petrolpark.mc.destroy.mixin.compat.create;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import petrolpark.mc.destroy.core.pollution.PollutingBehaviour;

/**
 * 给 Create Basin 挂 PollutingBehaviour。
 *
 * <p>注意类名带 {@code AddBehavioursMixin} 前缀——避开与 {@code Create-Library} 库
 * 可能存在的同目标 mixin 类名冲突（虽然 mixin 框架允许多个 mixin 共打同一目标，
 * 命名区分便于调试 / remap 输出）。</p>
*/
@Mixin(BasinBlockEntity.class)
public abstract class BasinBlockEntityAddBehavioursMixin extends SmartBlockEntity {

    public BasinBlockEntityAddBehavioursMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        throw new AssertionError();
    };

    @Inject(
        method = "Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;addBehaviours(Ljava/util/List;)V",
        at = @At("HEAD"),
        remap = false
    )
    public void destroy$addPollutingBehaviour(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        behaviours.add(new PollutingBehaviour(this));
    };
};
