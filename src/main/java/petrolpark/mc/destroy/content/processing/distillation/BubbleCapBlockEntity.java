package petrolpark.mc.destroy.content.processing.distillation;

import java.text.DecimalFormat;
import java.util.List;

import com.simibubi.create.content.redstone.thresholdSwitch.ThresholdSwitchObservable;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.simibubi.create.foundation.fluid.CombinedTankWrapper;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import petrolpark.mc.destroy.Destroy;
import petrolpark.mc.destroy.DestroyAdvancementTrigger;
import petrolpark.mc.destroy.DestroyBlockEntityTypes;
import petrolpark.mc.destroy.DestroySoundEvents;
import petrolpark.mc.destroy.client.DestroyLang;
import petrolpark.mc.destroy.client.DestroyParticleTypes;
import petrolpark.mc.destroy.core.fluid.gasparticle.GasParticleData;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.config.DestroyClientChemistryConfigs;
import petrolpark.mc.destroy.core.block.entity.IHaveLabGoggleInformation;
import petrolpark.mc.destroy.core.chemistry.MixtureContentsDisplaySource;
import petrolpark.mc.destroy.core.data.advancement.DestroyAdvancementBehaviour;
import petrolpark.mc.destroy.core.fluid.GeniusFluidTankBehaviour;
import petrolpark.mc.destroy.core.pollution.PollutingBehaviour;

/**
 * Bubble Cap Block Entity — one tier of a {@link DistillationTower}. The bottom ("controller")
 * Bubble Cap owns the {@link DistillationTower} singleton for the column; other Bubble Caps
 * carry a reference. Each tier has 2 tanks: {@link #tank} (visible output, horizontally
 * extract-able) + {@link #internalTank} (hidden staging; fills {@link #tank} on a delay to create
 * the "fluid rising up the tower" visual).
*/
public class BubbleCapBlockEntity extends SmartBlockEntity implements IHaveLabGoggleInformation, ThresholdSwitchObservable {

    private static final DecimalFormat df = new DecimalFormat();
    static {
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(0);
    }

    private static final int TRANSFER_SPEED = 20; // The rate (mB/tick) at which Fluid is transferred from the internal Tank to the actual Tank

    private int fraction; // Where in the Tower this Bubble Cap is (0 = base (controller), 1 = first fraction, etc)
    private int ticksToFill; // How long before this Bubble Cap should start transferring from its internal Tank to its actual Tank (allowing for the illusion of Fluid 'moving up' the Tower)
    public FluidStack particleFluid; // Which Fluid to use if we have to make particles (S128+ wire)

    private boolean isController;
    private BlockPos towerControllerPos;
    private DistillationTower tower;

    protected SmartFluidTankBehaviour internalTank, tank;

    public DestroyAdvancementBehaviour advancementBehaviour;
    protected PollutingBehaviour pollutingBehaviour;

    private int initializationTicks;

    public BubbleCapBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        fraction = 0;
        isController = false;
        towerControllerPos = pos;
        ticksToFill = 0;
        particleFluid = FluidStack.EMPTY;
        initializationTicks = 3;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tank = new GeniusFluidTankBehaviour(SmartFluidTankBehaviour.OUTPUT, this, 1, getTankCapacity(), true)
            .whenFluidUpdates(this::notifyUpdate);
        internalTank = new GeniusFluidTankBehaviour(SmartFluidTankBehaviour.INPUT, this, 1, getTankCapacity(), true)
            .forbidExtraction()
            .forbidInsertion()
            .whenFluidUpdates(this::notifyUpdate);
        behaviours.add(tank);
        behaviours.add(internalTank);

        advancementBehaviour = new DestroyAdvancementBehaviour(this, DestroyAdvancementTrigger.DISTILL);
        behaviours.add(advancementBehaviour);

        pollutingBehaviour = new PollutingBehaviour(this);
        behaviours.add(pollutingBehaviour);
    }

    
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            DestroyBlockEntityTypes.BUBBLE_CAP.get(),
            (be, side) -> {
                if (side == null) {
                    return new CombinedTankWrapper(be.tank.getCapability(), be.internalTank.getCapability());
                }
                if (side.getAxis() != Axis.Y) {
                    return be.tank.getCapability();
                }
                return null;
            });
    }

    @Override
    public void invalidate() {
        super.invalidate();
        removeFromDistillationTower();
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        if (!hasLevel()) return;
        fraction = compound.getInt("Fraction");
        int[] controllerPosArray = compound.getIntArray("DistillationTowerControllerPosition");
        if (controllerPosArray.length == 0) {
            towerControllerPos = getBlockPos();
        } else {
            towerControllerPos = new BlockPos(controllerPosArray[0], controllerPosArray[1], controllerPosArray[2]);
        }

        // Load Tower if this is the controller
        if (compound.contains("DistillationTower")) {
            isController = true;
            tower = new DistillationTower(compound.getCompound("DistillationTower"), getLevel(), getBlockPos());
        }
        // Load Tower if this isn't the controller
        BlockEntity be = getLevel().getBlockEntity(towerControllerPos);
        if (be instanceof BubbleCapBlockEntity controllerBubbleCap && controllerBubbleCap.isController) {
            tower = controllerBubbleCap.getDistillationTower();
        }

        if (clientPacket) {
            particleFluid = FluidStack.parseOptional(registries, compound.getCompound("ParticleFluid"));
        }

        ticksToFill = compound.getInt("TicksToFill");
        initializationTicks = compound.getInt("InitializationTicks");
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("Fraction", fraction);
        compound.putIntArray("DistillationTowerControllerPosition", List.of(towerControllerPos.getX(), towerControllerPos.getY(), towerControllerPos.getZ()));
        if (isController && tower != null) {
            compound.put("DistillationTower", tower.serializeNBT());
        }
        compound.putInt("TicksToFill", ticksToFill);
        if (clientPacket) {
            compound.put("ParticleFluid", particleFluid.saveOptional(registries));
        }
        compound.putInt("InitializationTicks", initializationTicks);
    }

    @Override
    public void tick() {
        super.tick();
        if (!particleFluid.isEmpty()) {
            if (getLevel().isClientSide()) {
                spawnParticles(particleFluid);
            }
            particleFluid = FluidStack.EMPTY;
        }
        if (initializationTicks > 0) initializationTicks--;
        if (ticksToFill > 0) {
            ticksToFill--;
            if (ticksToFill == 0) {
                DestroySoundEvents.DISTILLATION_TOWER_CONDENSE.playOnServer(getLevel(), getBlockPos());
            }
        }
        if (ticksToFill <= 0 && !internalTank.isEmpty()) {
            internalTank.allowInsertion();
            FluidStack transferredFluid = getInternalTank().drain(TRANSFER_SPEED, FluidAction.EXECUTE);
            internalTank.forbidInsertion();
            getTank().fill(transferredFluid, FluidAction.EXECUTE);
        }
        if (!hasLevel()) return;
        if (isController && tower != null) {
            tower.tick(getLevel());
        }
        sendData();
    }

    public void onDistill() {
        if (!isController) return;
        DestroySoundEvents.DISTILLATION_TOWER_BOIL.playOnServer(level, getBlockPos());
        advancementBehaviour.awardDestroyAdvancement(DestroyAdvancementTrigger.DISTILL);
    }

    public static int getTankCapacity() {
        return DestroyAllConfigs.SERVER.blocks.bubbleCapCapacity.get();
    }

    /** The rate (mB/tick) at which Fluid is transferred from internal Tank to actual Tank.*/
    public static int getTransferRate() {
        return TRANSFER_SPEED;
    }

    public SmartFluidTank getTank() {
        return tank.getPrimaryHandler();
    }

    public SmartFluidTank getInternalTank() {
        return internalTank.getPrimaryHandler();
    }

    public TankSegment getTankToRender() {
        return tank.getPrimaryTank();
    }

    public int getLuminosity() {
        if (getTank().isEmpty()) return 0;
        FluidStack fluidStack = getTank().getFluid();
        return fluidStack.getFluid().getFluidType().getLightLevel(fluidStack);
    }

    // ═════════════════════════════════════════════════════════════════════════════════════════
    // ThresholdSwitchObservable
    // ═════════════════════════════════════════════════════════════════════════════════════════

    @Override
    public int getMaxValue() {
        return getTank().getCapacity();
    }

    @Override
    public int getMinValue() {
        return 0;
    }

    @Override
    public int getCurrentValue() {
        return getTank().getFluidAmount();
    }

    @Override
    public MutableComponent format(int value) {
        return DestroyLang.translateDirect("gui.bubble_cap.bubble_cap_liquid_amount", value);
    }

    /**
 * Set the amount of time before Fluid from the internal Tank will start transferring to the
 * actual Tank. Used to create the illusion of Fluid "moving up" the Tower.
*/
    public void setTicksToFill(int ticks) {
        ticksToFill = ticks;
    }

    /**
 * Mark the Distillation Tower as containing this Bubble Cap, and let this Bubble Cap know
 * where in the Tower it is (including whether it is the controller).
*/
    public void addToDistillationTower(DistillationTower tower) {
        this.tower = tower;
        this.towerControllerPos = tower.getControllerPos();
        this.fraction = tower.getHeight();
        isController = (fraction == 0);
        sendData();
    }

    /**
 * Spawn 10 DISTILLATION gas particles rising out of this Bubble Cap's block center. Each
 * particle's upward lifetime is scaled by the Distillation Tower height
 * ({@code tower.getHeight() - 1.3f} — "distance to roughly just-below the top cap", so
 * particles fade right before exiting the column top). Client-only; only the
 * {@linkplain #isController controller} Bubble Cap emits (otherwise you'd get N× particles
 * for an N-tall tower).
*/
    public void spawnParticles(FluidStack fluidStack) {
        if (!(hasLevel() && getLevel().isClientSide() && isController)) return;
        Vec3 center = VecHelper.getCenterOf(getBlockPos());
        GasParticleData particleData = new GasParticleData(
            DestroyParticleTypes.DISTILLATION.get(), fluidStack, getDistillationTower().getHeight() - 1.3f);
        for (int i = 0; i < 10; i++) {
            getLevel().addParticle(particleData, center.x, center.y - 0.3f, center.z, 0, 0, 0);
        }
    }

    public void removeFromDistillationTower() {
        if (tower == null) return;
        tower.removeBubbleCap(this);
    }

    /**
 * Used when the Bubble Cap is placed or the Bubble Cap below is broken.
*/
    public void createOrAddToTower() {
        BlockEntity belowBE = getLevel().getBlockEntity(getBlockPos().below());
        if (belowBE == null || !(belowBE instanceof BubbleCapBlockEntity bubbleCapBelow) || bubbleCapBelow.getDistillationTower() == null) {
            tower = new DistillationTower(getLevel(), getBlockPos());
        } else {
            bubbleCapBelow.getDistillationTower().addBubbleCap(this);
        }
    }

    /**
 * Get the Distillation Tower of which this Bubble Cap is a part, assigning the Tower if
 * necessary.
*/
    public DistillationTower getDistillationTower() {
        if (!hasLevel()) {
            Destroy.LOGGER.warn("Tried to access Distillation Tower of Bubble Cap at " + getBlockPos().toShortString() + " but it has no assigned Level.");
            return null;
        }
        return tower;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (initializationTicks > 0) return false;
        DistillationTower clientTower = getDistillationTower();
        if (clientTower == null) return false;
        if (clientTower.getControllerBubbleCap() == null) return false;

        // Label this Bubble Cap
        if (isController) {
            DestroyLang.translate("tooltip.bubble_cap.reboiler")
                .style(ChatFormatting.WHITE)
                .forGoggles(tooltip);
        } else {
            DestroyLang.builder()
                .add(Component.translatable("block.destroy.bubble_cap"))
                .style(ChatFormatting.WHITE)
                .space()
                .add(Component.literal("" + fraction + "/" + (clientTower.getHeight() - 1)))
                .forGoggles(tooltip);
        }

        SmartFluidTank inputTank = clientTower.getControllerBubbleCap().getTank();

        // Add Fluid info header
        DestroyLang.fluidContainerInfoHeader(tooltip);

        // Add contents
        if (!isController) DestroyLang.tankInfoTooltip(tooltip, DestroyLang.translate("tooltip.bubble_cap.output_tank"), getTank());
        DestroyLang.tankInfoTooltip(tooltip, DestroyLang.translate("tooltip.bubble_cap.input_tank"), inputTank);

        // Map the config's TemperatureUnit enum to DestroyLang's TemperatureUnit enum (different
        // enum sets; same 3 conceptual values). Per TODO in DestroyClientChemistryConfigs S? —
        // unify once DestroyLang is mature.
        DestroyClientChemistryConfigs.TemperatureUnit configUnit = DestroyAllConfigs.CLIENT.chemistry.temperatureUnit.get();
        DestroyLang.TemperatureUnit langUnit = switch (configUnit) {
            case KELVIN -> DestroyLang.TemperatureUnit.KELVINS;
            case DEGREES_FAHRENHEIT -> DestroyLang.TemperatureUnit.DEGREES_FARENHEIT;
            case DEGREES_CELCIUS -> DestroyLang.TemperatureUnit.DEGREES_CELCIUS;
        };
        if (isController) DestroyLang.translate("tooltip.bubble_cap.reboiler_temperature",
            langUnit.of(DistillationTower.getTemperatureForDistillationTower(getLevel(), worldPosition), df)).forGoggles(tooltip);

        return true;
    }

    /**
 * DisplayLink source — reads this Bubble Cap's output tank.
 *
 * <p>Registration: {@link petrolpark.mc.destroy.DestroyDisplaySources#register}.</p>
*/
    public static class BubbleCapDisplaySource extends MixtureContentsDisplaySource {
        public BubbleCapDisplaySource() {
            super(false); // false = concentration mode
        }

        @Override
        public FluidStack getFluidStack(DisplayLinkContext context) {
            if (context.getSourceBlockEntity() instanceof BubbleCapBlockEntity bubbleCap) {
                return bubbleCap.getTank().getFluid();
            } else {
                return null;
            }
        }

        @Override
        public Component getName() {
            return DestroyLang.translate("display_source.bubble_cap").component();
        }
    }
}
