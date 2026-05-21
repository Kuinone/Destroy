package petrolpark.mc.destroy.content.logistics.siphon;

import java.text.DecimalFormat;
import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.block.entity.IHaveLabGoggleInformation;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutingBehaviour;

/**
 * Siphon BE — redstone-pulsed fluid metering block. Each redstone falling edge adds the configured
 * per-pulse draw amount ({@code settings.getValue()}, scroll-value settable 1 mB–10 buckets) to
 * {@link #leftToDrain}; downstream extractors (pumps etc.) can only drain fluid while
 * {@code leftToDrain > 0}.
*/
public class SiphonBlockEntity extends SmartBlockEntity implements IHaveLabGoggleInformation {

    public SiphonFluidTankBehaviour tank;
    public int leftToDrain = 0;

    public ScrollValueBehaviour settings;

    public DestroyAdvancementBehaviour advancementBehaviour;

    public SiphonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new SiphonFluidTankBehaviour();
        behaviours.add(tank);

        settings = new SiphonScrollValueBehaviour()
            .between(1, 10000);
        settings.setValue(1000);
        behaviours.add(settings);

        behaviours.add(new PollutingBehaviour(this));
        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.SIPHON);
        behaviours.add(advancementBehaviour);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        leftToDrain = tag.getInt("LeftToDrain");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("LeftToDrain", leftToDrain);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            DestroyBlockEntityTypes.SIPHON.get(),
            (be, context) -> be.tank.getCapability());
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        containedFluidTooltip(tooltip, isPlayerSneaking, tank.getCapability());
        DestroyLang.builder().add(
            Component.translatable("block.destroy.siphon.drain_amount_remaining", leftToDrain))
            .style(ChatFormatting.WHITE).forGoggles(tooltip);
        return true;
    }

    public class SiphonFluidTankBehaviour extends GeniusFluidTankBehaviour {

        public SiphonFluidTankBehaviour() {
            super(GeniusFluidTankBehaviour.TYPE, SiphonBlockEntity.this, 1,
                DestroyConfigs.server().blocks.siphonCapacity.get(), false);
            capability = new SiphonFluidHandler(getPrimaryHandler());
        }

        public class SiphonFluidHandler extends InternalFluidHandler {

            public SiphonFluidHandler(IFluidHandler... handlers) {
                super(handlers, false);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                maxDrain = Math.min(maxDrain, leftToDrain);
                FluidStack drained = super.drain(maxDrain, action);
                if (action.execute()) {
                    leftToDrain -= drained.getAmount();
                    if (!drained.isEmpty()) checkAdvancement();
                }
                return drained;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                FluidStack toDrain = resource.copy();
                if (toDrain.isEmpty()) return FluidStack.EMPTY;
                toDrain.setAmount(Math.min(resource.getAmount(), leftToDrain));
                FluidStack drained = super.drain(toDrain, action);
                if (action.execute()) {
                    leftToDrain -= drained.getAmount();
                    if (!drained.isEmpty()) checkAdvancement();
                }
                return drained;
            }

            private void checkAdvancement() {
                if (leftToDrain == 0) {
                    advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.SIPHON);
                }
            }
        }
    }

    public class SiphonScrollValueBehaviour extends ScrollValueBehaviour {

        private static final DecimalFormat df = new DecimalFormat();
        static {
            df.setMinimumFractionDigits(1);
            df.setMaximumFractionDigits(1);
        }

        public SiphonScrollValueBehaviour() {
            super(Component.translatable("block.destroy.siphon.drain_amount"),
                SiphonBlockEntity.this, new SiphonSlot());
        }

        @Override
        public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
            return new ValueSettingsBoard(label, 100, 10,
                CreateLang.translatedOptions("generic.unit", "millibuckets", "buckets"),
                new ValueSettingsFormatter(this::formatSettings));
        }

        @Override
        public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
            int value = valueSetting.value();
            int multiplier = valueSetting.row() == 0 ? 1 : 100;
            if (!valueSetting.equals(getValueSettings())) playFeedbackSound(this);
            setValue(Math.max(1, value) * multiplier);
        }

        @Override
        public ValueSettings getValueSettings() {
            int row = 0;
            int value = this.value;

            if (value > 100) {
                value = value / 100;
                row = 1;
            }

            return new ValueSettings(row, value);
        }

        public MutableComponent formatSettings(ValueSettings settings) {
            return Component.literal(switch (settings.row()) {
                case 0 -> String.valueOf(settings.value()) + CreateLang.translateDirect("generic.unit.millibuckets").getString();
                case 1 -> df.format(settings.value() / 10f) + CreateLang.translateDirect("generic.unit.buckets").getString();
                default -> String.valueOf(settings.value());
            });
        }

        public static class SiphonSlot extends ValueBoxTransform.Sided {

            @Override
            protected Vec3 getSouthLocation() {
                return VecHelper.voxelSpace(8d, 8d, 14.5d);
            }

            @Override
            public Vec3 getLocalOffset(LevelAccessor level, BlockPos pos, BlockState state) {
                if (getSide().getAxis() == Direction.Axis.Y) {
                    return VecHelper.voxelSpace(8d, getSide() == Direction.UP ? 15.5d : 0.5d, 8d);
                }
                return super.getLocalOffset(level, pos, state);
            }
        }
    }
}
