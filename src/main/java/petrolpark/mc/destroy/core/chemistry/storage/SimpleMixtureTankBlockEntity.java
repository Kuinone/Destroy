package petrolpark.mc.destroy.core.chemistry.storage;

import java.util.List;

import net.createmod.catnip.data.Couple;
import org.joml.Vector3f;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.core.block.entity.IHaveLabGoggleInformation;
import petrolpark.mc.destroy.core.chemistry.storage.SimpleMixtureTankRenderer.ISimpleMixtureTankRenderInformation;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;

/**
 * BE for all small placeable mixture tanks (beaker/flask/jar/measuring cylinder). Holds 1 fluid
 * tank with dynamic luminosity tracking (blocks holding luminescent fluids emit light).
*/
public abstract class SimpleMixtureTankBlockEntity extends SmartBlockEntity
    implements ISimpleMixtureTankRenderInformation<Void>, IHaveLabGoggleInformation {

    protected GeniusFluidTankBehaviour tank;
    public int luminosity = 0;

    public SimpleMixtureTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void onFluidStackChanged() {
        if (!tank.isEmpty() && hasLevel()) {
            int newLuminosity = getRenderedFluid(null).getFluid().getFluidType().getLightLevel();
            if (newLuminosity != luminosity && !getLevel().isClientSide()) {
                luminosity = newLuminosity;
                sendData();
            }
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new GeniusFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, 1000000, false);
        tank.whenFluidUpdates(this::onFluidStackChanged);
        behaviours.add(tank);
    }

    @Override
    public FluidStack getRenderedFluid(Void container) {
        return tank.getPrimaryTank().getRenderedFluid();
    }

    @Override
    public float getFluidLevel(Void container, float partialTicks) {
        return tank.getPrimaryTank().getFluidLevel().getValue(partialTicks);
    }

    public GeniusFluidTankBehaviour getTank() {
        return tank;
    }

    
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            DestroyBlockEntityTypes.SIMPLE_MIXTURE_TANK.get(),
            (be, context) -> be.tank.getCapability());
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            DestroyBlockEntityTypes.MEASURING_CYLINDER.get(),
            (be, context) -> be.tank.getCapability());
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        int prevLuminosity = luminosity;
        luminosity = tag.getInt("Luminosity");
        if (!clientPacket && prevLuminosity != luminosity && hasLevel()) {
            level.getChunkSource().getLightEngine().checkBlock(getBlockPos());
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("Luminosity", luminosity);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        DestroyLang.tankInfoTooltip(tooltip, DestroyLang.builder().add(getBlockState().getBlock().getName()),
            getTank().getPrimaryHandler());
        return true;
    }

    public static class SimplePlaceableMixtureTankBlockEntity extends SimpleMixtureTankBlockEntity {

        public SimplePlaceableMixtureTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
            // do NOT cache `SimplePlaceableMixtureTankBlock` as a field. SmartBlockEntity.<init>
            // (super) calls addBehaviours() BEFORE the subclass constructor body runs, so any field
            // initialized in the subclass ctor is still null when addBehaviours fires. Always read
            // the block from BlockState inside behaviour/method bodies instead.
        }

        private SimplePlaceableMixtureTankBlock block() {
            return (SimplePlaceableMixtureTankBlock) getBlockState().getBlock();
        }

        @Override
        public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
            super.addBehaviours(behaviours);
            // Now that tank is created, set its capacity from the block spec
            tank.setCapacity(block().getMixtureCapacity());
        }

        @Override
        public Couple<Vector3f> getFluidBoxDimensions() {
            return block().getFluidBoxDimensions();
        }
    }
}
