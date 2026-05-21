package petrolpark.mc.destroy.content.logistics.creativepump;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Creative Pump BE — infinite-source pump whose speed is player-settable via a scroll-value
 * behaviour (vs. the vanilla Pump where speed is driven by a Kinetic network).这条路径依赖 KineticBlockEntity 的 speed 字段从 0 变到非零（玩家挂上轴 / 启动主轴）。</p>
 *
 * <p>Creative Pump 通过 {@link #getSpeed()} 返回 {@link #simulatedSpeed}（默认 16，玩家可滚轮调）覆盖
 * 速度查询，但 KineticBlockEntity 内部的 speed 字段始终是 0（无动力源）。因此
 * {@code onSpeedChanged} 从未触发 → {@code updatePressureChange} 从未自动运行 → 相邻管道拿不到压力
 * → 无法建立 flow → 泵摆设无效。</p>
 *
 * <p>玩家通过滚轮调速度时，{@link ScrollValueBehaviour} 的 callback 会调一次
 * {@code updatePressureChange}（这是为什么"调过速度"的泵能传输），但放置后默认值 16 是构造时
 * {@code setValue(16)} 设的，那时 {@code level == null}（BE 还没 attach），callback 内的
 * {@code updatePressureChange} 被 if 跳过了。</p>
 *
 * <p>第二个症状（放管道后泵还往老位置丢流体）是同一根因的衍生：泵从未"重新分发压力"过——一旦初始
 * 状态确定（OpenEndedPipe target），就缺乏 wipePressure + sidesToUpdate=true 的二次触发机制。
 * Vanilla pump 每次速度脉冲（启停）都触发；creative pump 速度恒定不脉冲，所以网络一旦快照就停留
 * 在那一刻——即便 {@link com.simibubi.create.content.fluids.FluidPropagator#propagateChangedPipe}
 * 因为 pipe.onPlace 触发了 wipePressure，泵后续的 IDLE-phase manageFlows 重建 source 时没有
 * 重新分发压力 → 相邻管道还是没压力，BFS 在 pipe 那站住。</p>
 *
 * <h3>修复</h3>
 *
 * <p>覆盖 {@link com.simibubi.create.foundation.blockEntity.SmartBlockEntity#initialize()}
 * （BE attach 到 level 后第一次 tick 调用，level 已就绪，{@link ScrollValueBehaviour#setValue}
 * 时机的限制不再适用）：</p>
 *
 * <ul>
 * <li>调 {@link PumpBlockEntity#updatePressureChange()}：BFS 走相邻管道分发压力 + 自身
 * wipePressure + 两侧 {@code sidesToUpdate=true} → 下 tick 必定执行
 * {@code distributePressureTo} 给相邻管道压力。这是 vanilla pump 在 onSpeedChanged 干的事情，
 * creative pump 在 initialize 自己干。</li>
 * </ul>
 *
 * <p>这一条同时治两个症状：</p>
 * <ol>
 * <li>放置后立即 transfer：initialize 触发 updatePressureChange → 压力到管道 → flow 建立。</li>
 * <li>topology 变化时正常重建：当玩家放管道时，{@code FluidPropagator.propagateChangedPipe} →
 * {@code updatePipesOnSide(pump)} → {@code wipePressure} 已经会清空 network targets。但
 * creative pump 缺乏定期"刷新分发"，所以一旦 wipePressure 后无 kinetic 脉冲来再触发一次完整
 * updatePressureChange——其实 updatePipesOnSide 单方向 setTrue 也够了，但稳妥起见 initialize
 * 打底。</li>
 * </ol>
*/
public class CreativePumpBlockEntity extends PumpBlockEntity {

    public ScrollValueBehaviour pumpSpeedBehaviour;
    protected int simulatedSpeed = 16;

    /** Each tick, compare current
 * front+back adjacent states to these snapshots; on change, force {@link #updatePressureChange()}
 * so the pump's network state gets re-derived from current world topology instead of the
 * cached state from when flow was first established.
 *
 * <p>BlockState instances are interned in Minecraft (same state value = same reference), so
 * reference inequality (!=) is reliable for state-change detection.</p>
 *
 * <p>Initialized to {@code null} sentinels; first tick after BE attach captures the initial
 * snapshot without firing update (which would conflict with {@link #initialize()}'s already-
 * scheduled updatePressureChange).</p>
*/
    private BlockState lastFrontState;
    private BlockState lastBackState;
    private boolean stateTrackerInitialized = false;

    public CreativePumpBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        pumpSpeedBehaviour = new ScrollValueBehaviour(Component.translatable("block.destroy.creative_pump.speed"), this, new CreativePumpValueSlot()) {
            @Override
            public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
                return new ValueSettingsBoard(label, max, 16, ImmutableList.of(Component.literal("\u2192").withStyle(ChatFormatting.BOLD)), new ValueSettingsFormatter(this::formatSettings));
            }

            public MutableComponent formatSettings(ValueSettings settings) {
                return CreateLang.number(Math.max(1, settings.value())).component();
            }
        }
            .between(1, AllConfigs.server().kinetics.maxRotationSpeed.get())
            .withCallback(i -> {
                simulatedSpeed = i;
                if (level != null && !level.isClientSide)
                    updatePressureChange();
            });
        pumpSpeedBehaviour.setValue(16);
        behaviours.add(pumpSpeedBehaviour);
    }

    @Override
    public float getSpeed() {
        return simulatedSpeed;
    }

    /** Triggers {@link PumpBlockEntity#updatePressureChange} on the
 * first {@link com.simibubi.create.foundation.blockEntity.SmartBlockEntity#tick} (when level
 * is set and BE is fully wired). This emulates what vanilla {@link PumpBlockEntity} does in
 * its {@code onSpeedChanged} → {@code updatePressureChange} chain when the kinetic network
 * first delivers non-zero speed; for Creative Pump kinetic speed stays 0 forever, so we have
 * to drive this manually.
 *
 * <p>{@code updatePressureChange} does three things at once:</p>
 * <ol>
 * <li>Calls {@link com.simibubi.create.content.fluids.FluidPropagator#propagateChangedPipe}
 * on the front + back adjacent positions — wipes adjacent pipe pressures, walks the
 * pipe network discovering pumps and notifies them.</li>
 * <li>Calls {@code wipePressure} on this pump's own behaviour — clears stale pipe
 * connections / sources / networks if any survived the BE construction.</li>
 * <li>Sets BOTH {@code sidesToUpdate} flags to true → next tick's {@code distributePressureTo}
 * fires for both sides → adjacent pipes get pressure.</li>
 * </ol>
*/
    @Override
    public void initialize() {
        super.initialize();
        if (level != null && !level.isClientSide) {
            updatePressureChange();
        }
    }

    /**
 *
 * <p><b>修复策略</b>: 由 creative pump 自己每 tick 监测前后两侧相邻 BlockState 是否变化，变化时
 * 主动调 {@link #updatePressureChange()}——这条路径在 vanilla pump 通过 onSpeedChanged 速度脉冲
 * 触发；creative pump 速度恒定无脉冲，缺这一环。</p>
 *
 * <p>{@code updatePressureChange()} 等同于 vanilla pump 在速度变化时干的事：propagateChangedPipe
 * 双侧 + behaviour.wipePressure + sidesToUpdate 双侧 setTrue。这会清空 network targets（包括
 * 任何 stale OpenEndedPipe 引用）+ 重新分发压力到新的相邻管道+ 下 tick BFS 从头建立新拓扑。</p>
 *
 * <p>BlockState 实例在 Minecraft 中是 interned 的（同一 state value 共享同一引用），所以引用
 * 不等（!=）是可靠的状态变化检测——比 {@code .equals(...)} 还便宜。</p>
 *
 * <p><b>性能</b>: 每 tick 两个 {@code level.getBlockState(adjacentPos)} 调用 + 引用比较，几乎零
 * 开销。{@code updatePressureChange()} 只在变化时触发，正常情况下完全静默。</p>
*/
    @Override
    public void tick() {
        super.tick();
        if (level == null || level.isClientSide) return;

        Direction front = getFront();
        if (front == null) return;

        BlockState currentFront = level.getBlockState(worldPosition.relative(front));
        BlockState currentBack = level.getBlockState(worldPosition.relative(front.getOpposite()));

        if (!stateTrackerInitialized) {
            // First tick after BE attach: capture initial snapshot, don't fire (initialize()
            // already calls updatePressureChange).
            lastFrontState = currentFront;
            lastBackState = currentBack;
            stateTrackerInitialized = true;
            return;
        }

        // BlockState instances are interned — reference inequality detects any state change.
        if (currentFront != lastFrontState || currentBack != lastBackState) {
            lastFrontState = currentFront;
            lastBackState = currentBack;
            updatePressureChange();
        }
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        simulatedSpeed = compound.getInt("SimulatedSpeed");
        pumpSpeedBehaviour.setValue(simulatedSpeed);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("SimulatedSpeed", simulatedSpeed);
    }

    public class CreativePumpValueSlot extends ValueBoxTransform.Sided {

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return state.getValue(CreativePumpBlock.FACING).getAxis() != direction.getAxis();
        }

        @Override
        protected Vec3 getSouthLocation() {
            // push z from 12.5 → 13.5 (just 1 voxel out from model's z=13 edge).
            // The pump's voxel shape is (3,0,3,13,16,13) so the visible model body extends
            // to z=13; the original z=12.5 placed the value box center INSIDE the model and
            // got occluded by the model faces. First attempt at z=15.5 (matching
            // DynamiteBlockEntity / SpeedControllerBlockEntity) caused the box to look
            // "浮空" (floating/detached) — those blocks have full-block 16x16 voxel shapes,
            // but the pump body is narrower (10x16x10). 13.5 puts the box flush against the
            // model surface, neither buried nor floating. Reported bugs:
            // "鼠标移上去显示转速的选择框被模型盖住了", then ' regression
            // "动力泵的这个选择框变成浮空的了，实际上移动大约一个像素就够了".
            return VecHelper.voxelSpace(8d, 8d, 13.5d);
        }
    }
}
