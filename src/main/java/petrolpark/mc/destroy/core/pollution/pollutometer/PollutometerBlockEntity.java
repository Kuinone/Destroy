package petrolpark.mc.destroy.core.pollution.pollutometer;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;

import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import petrolpark.mc.destroy.core.pollution.PollutometerBlock;

/**
 * Pollutometer BlockEntity — hosts a {@link ScrollOptionBehaviour} over
 * {@link PollutometerSelector} so the player can wrench-scroll between the 5 pollution types
 * (greenhouse / ozone-depletion / acid-rain / smog / radioactivity). The selector index is
 * persisted as an int NBT field {@code "PollutionType"}.
*/
public class PollutometerBlockEntity extends SmartBlockEntity {

    private PollutometerSelector selector;

    protected ScrollOptionBehaviour<PollutometerSelector> selectorScroll;

    private static final ValueBoxTransform.Sided slot = new ValueBoxTransform.Sided() {

        @Override
        public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
            return VecHelper.rotateCentered(getSouthLocation(),
                AngleHelper.horizontalAngle(getSide()),
                Direction.Axis.Y);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction == state.getValue(PollutometerBlock.DIRECTION);
        }

        @Override
        protected Vec3 getSouthLocation() {
            // the rendered geometry.
            return VecHelper.voxelSpace(8d, 6d, 12.75d);
        }
    };

    public PollutometerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.selector = PollutometerSelector.GREENHOUSE;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        selectorScroll = new ScrollOptionBehaviour<>(
            PollutometerSelector.class,
            petrolpark.mc.destroy.client.DestroyLang
                .translate("tooltip.pollutometer.pollution_type")
                .component(),
            this,
            slot);
        selectorScroll.withCallback(this::setSelectorByOrdinal);
        behaviours.add(selectorScroll);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        int ordinal = tag.getInt("PollutionType");
        setSelectorByOrdinal(ordinal);
        selectorScroll.value = ordinal;
        super.read(tag, registries, clientPacket);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putInt("PollutionType", selector.ordinal());
        super.write(tag, registries, clientPacket);
    }

    public PollutometerSelector getSelector() {
        return selector;
    }

    private void setSelectorByOrdinal(int ordinal) {
        PollutometerSelector[] values = PollutometerSelector.values();
        // Defensive clamp — in case the saved ordinal is stale (e.g. enum order change).
        this.selector = values[Math.floorMod(ordinal, values.length)];
    }
}
