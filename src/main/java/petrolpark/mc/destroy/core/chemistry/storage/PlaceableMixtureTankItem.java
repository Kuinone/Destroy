package petrolpark.mc.destroy.core.chemistry.storage;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.core.block.IPickUpPutDownBlock;

/**
 * Base item for Destroy's "placeable mixture tank" blocks — implements {@link IMixtureStorageItem}
 * so right-click-block fills/empties via the block's FluidHandler cap.
*/
public abstract class PlaceableMixtureTankItem<T extends PlaceableMixtureTankBlock<?>> extends BlockItem implements IMixtureStorageItem {

    protected final T tankBlock;

    public PlaceableMixtureTankItem(T block, Properties properties) {
        super(block, properties);
        this.tankBlock = block;
    }

    @Override
    public int getCapacity(ItemStack stack) {
        return tankBlock.getMixtureCapacity();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        InteractionResult result = IMixtureStorageItem.defaultUseOn(this, context);
        if (result == InteractionResult.PASS) return super.useOn(context);
        return result;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return getTank(level, pos, state, null, player, InteractionHand.MAIN_HAND,
            player.getItemInHand(InteractionHand.MAIN_HAND), false) == null;
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        // vanilla 1.21 client {@code MultiPlayerGameMode.performUseItemOn} +
        // server {@code ServerPlayerGameMode.useItemOn} both implement an in-creative
        // save-then-restore pattern around {@code stack.useOn(context)}:
        // if (isCreative()) { int saved = stack.getCount(); stack.useOn(ctx); stack.setCount(saved); }
        // This defeats any in-place shrink we do.
        // Workaround: combine instabuild-flip (so super.place's {@code consume(1, player)}
        // actually shrinks) + replace the inventory slot with a fresh ItemStack reference.
        // Vanilla's post-useOn {@code setCount(savedCount)} then operates on the orphaned old
        // ItemStack (still held by vanilla's local var) instead of the new in-slot stack.
        Player player = context.getPlayer();
        boolean wasInstabuild = player != null && player.getAbilities().instabuild;
        if (wasInstabuild) player.getAbilities().instabuild = false;
        try {
            net.minecraft.world.item.ItemStack origStack = context.getItemInHand();
            InteractionResult res = super.place(context);
            // For creative + successful place: detach the held stack ref from the inventory slot.
            if (wasInstabuild && res.consumesAction()) {
                int slot = (context.getHand() == net.minecraft.world.InteractionHand.OFF_HAND)
                    ? net.minecraft.world.entity.player.Inventory.SLOT_OFFHAND
                    : player.getInventory().selected;
                net.minecraft.world.item.ItemStack inSlot = player.getInventory().getItem(slot);
                // Only act if the slot still references the original stack we're operating on
                // (super.place's consume already shrunk it; might be count=0 now or count=N-1).
                if (inSlot == origStack) {
                    int newCount = inSlot.getCount();
                    player.getInventory().setItem(slot,
                        newCount > 0 ? inSlot.copyWithCount(newCount) : net.minecraft.world.item.ItemStack.EMPTY);
                }
            }
            return IPickUpPutDownBlock.removeItemFromInventory(context, res);
        } finally {
            if (wasInstabuild) player.getAbilities().instabuild = true;
        }
    }

    @Override
    public Component getNameRegardlessOfFluid(ItemStack stack) {
        return super.getName(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        return getNameWithFluid(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        addContentsDescription(stack, tooltip);
    }
}
