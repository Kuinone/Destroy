package petrolpark.mc.destroy.core.pollution.catalyticconverter;

import java.util.List;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutionHelper;

/**
 * Catalytic Converter BE — stores up to 1,000 buckets of polluting fluid; every 10 ticks, if
 * the tank is non-empty, releases all buffered fluid into the environment via
 * {@link PollutionHelper#pollute(net.minecraft.world.level.Level, BlockPos, float,
 * net.neoforged.neoforge.fluids.FluidStack...) PollutionHelper.pollute} with the configured
 * reduction multiplier. Awards {@link DestroyAdvancementTrigger#CATALYTIC_CONVERTER} on each flush.
*/
public class CatalyticConverterBlockEntity extends SmartBlockEntity {

    protected DestroyAdvancementBehaviour advancementBehaviour;
    protected GeniusFluidTankBehaviour tankBehaviour;
    protected int ticksToFlush;

    public CatalyticConverterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        ticksToFlush--;
        if (ticksToFlush <= 0) {
            ticksToFlush = 10;
            if (tankBehaviour.isEmpty()) return;
            float multiplier = DestroyConfigs.server().blocks.catalyticConverterReduction.getF();
            if (multiplier > 0f) PollutionHelper.pollute(level,
                getBlockPos().relative(getBlockState().getValue(CatalyticConverterBlock.FACING)),
                multiplier, tankBehaviour.getPrimaryHandler().getFluid());
            advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.CATALYTIC_CONVERTER);
            tankBehaviour.getPrimaryHandler().drain(1000000, FluidAction.EXECUTE);
            notifyUpdate();
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        ticksToFlush = tag.getInt("TicksToFlush");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("TicksToFlush", ticksToFlush);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tankBehaviour = new GeniusFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, 1000000, false);
        tankBehaviour.forbidExtraction();
        behaviours.add(tankBehaviour);

        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.CATALYTIC_CONVERTER);
        behaviours.add(advancementBehaviour);
    }

    /**
 * Fluid-handler capability exposure: accepts from ANY side and from the face opposite to
 * the Block's {@code FACING} direction (= the "input" face opposite the exhaust).
*/
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            DestroyBlockEntityTypes.CATALYTIC_CONVERTER.get(),
            (be, context) -> {
                if (context == null) return be.tankBehaviour.getCapability();
                Direction facing = be.getBlockState().getValue(CatalyticConverterBlock.FACING);
                return context == facing.getOpposite() ? be.tankBehaviour.getCapability() : null;
            });
    }
}
