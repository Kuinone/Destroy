package petrolpark.mc.destroy.core.chemistry.storage;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
// IFluidHandlerItem import dropped (only the comment refs it now). The block-level
// FluidHandler cap returns IFluidHandler, never IFluidHandlerItem.

import petrolpark.mc.destroy.core.block.IPickUpPutDownBlock;

/**
 * Base for Destroy's "placeable mixture tank" blocks (beaker / flask / jar / measuring cylinder).
 * Carries fluid across place/break via block-entity → item copy.
*/
public abstract class PlaceableMixtureTankBlock<T extends BlockEntity> extends Block implements IPickUpPutDownBlock, IBE<T> {

    public PlaceableMixtureTankBlock(Properties properties) {
        super(properties);
    }

    public abstract int getMixtureCapacity();

    @Override
    protected abstract MapCodec<? extends PlaceableMixtureTankBlock<T>> codec();

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.getItem() instanceof IMixtureStorageItem mixtureItem && mixtureItem.getContents(pStack).isPresent()) {
            IFluidHandler fluidHandler = pLevel.getCapability(Capabilities.FluidHandler.BLOCK, pPos, null);
            if (fluidHandler != null) {
                fluidHandler.fill(mixtureItem.getContents(pStack).get(), FluidAction.EXECUTE);
            }
        }
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, Builder pParams) {
        BlockEntity be = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return Collections.singletonList(getFilledItemStack(be));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        if (level instanceof Level l) {
            IFluidHandler fluidHandler = l.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
            if (fluidHandler != null && asItem() instanceof IMixtureStorageItem mixtureItem) {
                ItemStack stack = new ItemStack(asItem());
                FluidStack content = fluidHandler.drain(mixtureItem.getCapacity(stack), FluidAction.SIMULATE);
                mixtureItem.setContents(stack, content);
                return stack;
            }
        }
        return new ItemStack(asItem());
    }

    public ItemStack getFilledItemStack(BlockEntity be) {
        if (be != null && asItem() instanceof IMixtureStorageItem mixtureItem) {
            ItemStack stack = new ItemStack(asItem());
            // was `(IFluidHandlerItem) ...getCapability(FluidHandler.BLOCK, ...)`. Wrong:
            // FluidHandler.BLOCK returns IFluidHandler (block-level), not IFluidHandlerItem (item-level).
            // The actual returned object is Create's SmartFluidTankBehaviour$InternalFluidHandler which
            // implements IFluidHandler — cast to IFluidHandlerItem fires ClassCastException at runtime.
            // Trigger seen: water spreading destroys a placed mixture tank → block.getDrops → here
            // (crash-2026-04-27_18.31.43-server.txt). getCloneItemStack above already uses the correct
            // IFluidHandler — just align this method.
            IFluidHandler fluidHandler = be.getLevel().getCapability(
                Capabilities.FluidHandler.BLOCK, be.getBlockPos(), null);
            if (fluidHandler != null) {
                mixtureItem.setContents(stack, fluidHandler.drain(mixtureItem.getCapacity(stack), FluidAction.SIMULATE));
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}
