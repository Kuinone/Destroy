package petrolpark.mc.destroy.core.chemistry.storage;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.createmod.catnip.data.Iterate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import com.simibubi.create.foundation.utility.CreateLang;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.chemistry.legacy.ClientMixture;
import petrolpark.mc.destroy.chemistry.legacy.ReadOnlyMixture;
import petrolpark.mc.destroy.chemistry.minecraft.MixtureFluid;
import petrolpark.mc.destroy.config.DestroyAllConfigs;

/**
 * Item interface for carrying Mixture fluids.
 *
 * <p>1.21 migration notes:</p>
*/
public interface IMixtureStorageItem {

    /** Max mB this stack can hold. Implementations override to return per-item capacity (e.g., TEST_TUBE = 25 mB).*/
    int getCapacity(ItemStack stack);

    /** Display name when empty — subclasses provide the base item name.*/
    Component getNameRegardlessOfFluid(ItemStack stack);

    // ---- fill/empty interactions ----

    /** Try filling the item from an external fluid handler.*/
    default InteractionResult tryFill(ItemStack stack, IFluidHandlerItem itemTank, @Nullable IFluidHandler otherTank, int maxTransfer) {
        if (otherTank == null) return InteractionResult.PASS;
        for (boolean simulate : Iterate.trueAndFalse) {
            FluidStack drained = otherTank.drain(maxTransfer, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            if (drained.isEmpty()) return InteractionResult.FAIL;
            int filled = itemTank.fill(drained, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            if (filled == 0) return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /** Try filling completely from other tank.*/
    default InteractionResult tryFill(ItemStack stack, IFluidHandlerItem itemTank, @Nullable IFluidHandler otherTank) {
        int space = getCapacity(stack) - (itemTank.drain(getCapacity(stack), FluidAction.SIMULATE).getAmount());
        return tryFill(stack, itemTank, otherTank, Math.max(space, 1));
    }

    /** Try emptying the item into an external fluid handler.*/
    default InteractionResult tryEmpty(ItemStack stack, IFluidHandlerItem itemTank, @Nullable IFluidHandler otherTank, boolean infiniteFluid, int maxTransfer) {
        if (otherTank == null) return InteractionResult.PASS;
        for (boolean simulate : Iterate.trueAndFalse) {
            FluidStack drained = itemTank.drain(maxTransfer, simulate || infiniteFluid ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            if (drained.isEmpty()) return InteractionResult.FAIL;
            int filled = otherTank.fill(drained, simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
            if (filled == 0) return InteractionResult.FAIL;
        }
        return InteractionResult.SUCCESS;
    }

    /** Try emptying completely into other tank.*/
    default InteractionResult tryEmpty(ItemStack stack, IFluidHandlerItem itemTank, @Nullable IFluidHandler otherTank, boolean infiniteFluid) {
        int held = itemTank.drain(getCapacity(stack), FluidAction.SIMULATE).getAmount();
        return tryEmpty(stack, itemTank, otherTank, infiniteFluid, Math.max(held, 1));
    }

    /** Lookup the target tank at a block position. Returns null if no cap found.*/
    @Nullable
    default IFluidHandler getTank(Level level, BlockPos pos, BlockState state, @Nullable Direction face,
                                  Player player, InteractionHand hand, ItemStack stack, boolean filling) {
        if (state.getBlock() instanceof ISpecialMixtureContainerBlock specialBlock) {
            return specialBlock.getTankForMixtureStorageItems(this, level, pos, state, face, player, hand, stack, filling);
        }
        return level.getCapability(Capabilities.FluidHandler.BLOCK, pos, face);
    }

    /** Typical right-click behaviour: empty the item into the clicked block.*/
    static InteractionResult defaultUseOn(IMixtureStorageItem item, UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        IFluidHandlerItem itemTank = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemTank == null) return InteractionResult.PASS;
        IFluidHandler otherTank = item.getTank(level, pos, state, context.getClickedFace(),
            context.getPlayer(), context.getHand(), stack, true);
        InteractionResult result = item.tryEmpty(stack, itemTank, otherTank, context.getPlayer().isCreative());
        item.afterEmpty(level, pos, state, context.getClickedFace(), context.getPlayer(), context.getHand(), stack, result);
        return result;
    }

    /** Typical left-click behaviour: fill the item from the clicked block.*/
    static InteractionResult defaultAttack(IMixtureStorageItem item, Level level, BlockPos pos, BlockState state,
                                           Direction face, Player player, InteractionHand hand, ItemStack stack) {
        IFluidHandlerItem itemTank = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemTank == null) return InteractionResult.PASS;
        IFluidHandler otherTank = item.getTank(level, pos, state, face, player, hand, stack, false);
        InteractionResult result = item.tryFill(stack, itemTank, otherTank);
        item.afterFill(level, pos, state, face, player, hand, stack, result);
        return result;
    }

    default void afterEmpty(Level level, BlockPos pos, BlockState state, @Nullable Direction face, Player player, InteractionHand hand, ItemStack stack, InteractionResult result) {
        if (result == InteractionResult.SUCCESS) level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS);
    }

    default void afterFill(Level level, BlockPos pos, BlockState state, @Nullable Direction face, Player player, InteractionHand hand, ItemStack stack, InteractionResult result) {
        if (result == InteractionResult.SUCCESS) level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS);
    }

    static boolean isHolding(Player player, InteractionHand hand) {
        return player.getItemInHand(hand).getItem() instanceof IMixtureStorageItem;
    }

    // ---- content helpers ----

    default boolean isEmpty(ItemStack stack) {
        return getContents(stack).map(FluidStack::isEmpty).orElse(true);
    }

    default int getColor(ItemStack stack) {
        return getContents(stack).map(MixtureFluid::getTintColor).orElse(0xFFFFFFFF);
    }

    default Optional<FluidStack> getContents(ItemStack stack) {
        IFluidHandlerItem cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (cap == null) return Optional.empty();
        return Optional.of(cap.drain(getCapacity(stack), FluidAction.SIMULATE));
    }

    default void setContents(ItemStack stack, FluidStack fluidStack) {
        IFluidHandlerItem cap = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (cap != null) cap.fill(fluidStack, FluidAction.EXECUTE);
    }

    default Component getNameWithFluid(ItemStack stack) {
        FluidStack contents = getContents(stack).orElse(FluidStack.EMPTY);
        if (contents.isEmpty()) return Component.translatable(stack.getDescriptionId());
        return Component.translatable(stack.getDescriptionId() + ".filled", contents.getHoverName());
    }

    DecimalFormat df = _initDf();

    private static DecimalFormat _initDf() {
        DecimalFormat f = new DecimalFormat();
        f.setMinimumFractionDigits(1);
        f.setMaximumFractionDigits(1);
        return f;
    }

    /** Add mixture contents description to the item tooltip.*/
    default void addContentsDescription(ItemStack stack, List<Component> tooltip) {
        getContents(stack).ifPresent(fluidStack -> {
            if (fluidStack.isEmpty()) return;

            float temperature = 289f;
            tooltip.add(Component.literal(""));

            CompoundTag mixtureTag = fluidStack.get(DestroyDataComponents.MIXTURE);
            if (mixtureTag != null && !mixtureTag.isEmpty()) {
                ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(ClientMixture::new, mixtureTag);
                boolean iupac = DestroyAllConfigs.CLIENT.chemistry.iupacNames.get();
                temperature = mixture.getTemperature();
                tooltip.addAll(mixture.getContentsTooltip(iupac, false, false, fluidStack.getAmount(), df)
                    .stream().map(Component::copy).toList());
            }

            // (tied to DestroyLang port which defers Phase-5/Client). Use plain kelvin display.
            tooltip.add(2, Component.literal(" " + fluidStack.getAmount()).withStyle(ChatFormatting.GRAY)
                .append(CreateLang.translateDirect("generic.unit.millibuckets"))
                .append(" " + df.format(temperature) + "K"));
        });
    }
}
