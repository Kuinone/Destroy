package petrolpark.mc.destroy.content.redstone.programmer;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import petrolpark.mc.destroy.DestroyBlocks;
import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.core.block.IPickUpPutDownBlock;

/**
 * {@link BlockItem} for the Redstone Programmer — acts as a "pocket programmer" in inventory:
 * opens the Programmer UI on right-click (even before placing), ticks its internal
 * {@link ItemStackRedstoneProgram} for RedstoneLinkNetworkHandler participation. Stack data
 * payload: unique UUID + serialized RedstoneProgram (stored in DataComponents).
*/
public class RedstoneProgrammerBlockItem extends BlockItem {

    public RedstoneProgrammerBlockItem(RedstoneProgrammerBlock block, Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player.isShiftKeyDown()) return super.onItemUseFirst(stack, context);
        openScreen(stack, context.getLevel(), player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        return IPickUpPutDownBlock.removeItemFromInventory(context, super.place(context));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        openScreen(stack, level, player);
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(usedHand), level.isClientSide);
    }

    public static void openScreen(ItemStack stack, Level level, Player player) {
        getProgram(stack, level, player).ifPresent(program -> {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new ItemStackRedstoneProgramMenuOpener(program), buffer -> {
                    program.write(buffer);
                    buffer.writeBoolean(false); // in Item form, programmer is never powered
                });
            }
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (entity instanceof LivingEntity player) {
            getProgram(stack, level, player).ifPresent(program -> {
                if (!level.isClientSide()) program.load(); // set-based, idempotent
                program.tick();
                setProgram(stack, program, level.registryAccess());
            });
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack from, ItemStack to, boolean slotChanged) {
        return !(from.getItem() instanceof RedstoneProgrammerBlockItem && to.getItem() instanceof RedstoneProgrammerBlockItem);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack from, ItemStack to) {
        return !(from.getItem() instanceof RedstoneProgrammerBlockItem && to.getItem() instanceof RedstoneProgrammerBlockItem);
    }

    
    public static void setProgram(ItemStack stack, RedstoneProgram program, HolderLookup.Provider provider) {
        stack.set(DestroyDataComponents.PROGRAMMER_PROGRAM, program.write(provider));
    }

    public static ItemStack withProgram(RedstoneProgram program, HolderLookup.Provider provider) {
        ItemStack stack = DestroyBlocks.REDSTONE_PROGRAMMER.asStack();
        setProgram(stack, program, provider);
        return stack;
    }

    /**
 * Get or lazily-create the {@link ItemStackRedstoneProgram} associated with this stack. Uses
 * {@link DestroyDataComponents#PROGRAMMER_UUID} as registry key; UUID is auto-generated +
 * stored on first access.
*/
    public static Optional<RedstoneProgram> getProgram(ItemStack stack, LevelAccessor level, LivingEntity player) {
        if (!(stack.getItem() instanceof RedstoneProgrammerBlockItem) || player == null) return Optional.empty();

        UUID uuid = stack.get(DestroyDataComponents.PROGRAMMER_UUID);
        if (uuid == null) {
            uuid = UUID.randomUUID();
            stack.set(DestroyDataComponents.PROGRAMMER_UUID, uuid);
        }

        ItemStackRedstoneProgram newProgram;
        CompoundTag programTag = stack.get(DestroyDataComponents.PROGRAMMER_PROGRAM);
        if (programTag == null) {
            newProgram = new ItemStackRedstoneProgram(player);
        } else {
            newProgram = RedstoneProgram.read(
                () -> new ItemStackRedstoneProgram(player),
                programTag,
                level.registryAccess());
        }

        if (level.isClientSide()) return Optional.of(newProgram); // client creates fresh per request

        ItemStackRedstoneProgram program = RedstoneProgrammerItemHandler.programs.get(level)
            .computeIfAbsent(uuid, u -> newProgram);
        return Optional.of(program);
    }

    public static class ItemStackRedstoneProgram extends RedstoneProgram {

        public int ttl;
        protected final LivingEntity player;

        public ItemStackRedstoneProgram(LivingEntity player) {
            super();
            this.player = player;
            ttl = RedstoneProgrammerItemHandler.TIMEOUT;
        }

        @Override
        public void load() {
            if (player != null && player.getOnPos() != null) super.load();
        }

        @Override
        public void tick() {
            ttl = RedstoneProgrammerItemHandler.TIMEOUT;
            super.tick();
        }

        @Override
        public boolean hasPower() {
            return false;
        }

        @Override
        public BlockPos getBlockPos() {
            return player.getOnPos();
        }

        @Override
        public boolean shouldTransmit() {
            return ttl > 0;
        }

        @Override
        public LevelAccessor getWorld() {
            return player.level();
        }
    }

    public record ItemStackRedstoneProgramMenuOpener(RedstoneProgram program) implements MenuProvider {

        @Override
        public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
            // wire to RedstoneProgrammerMenu.create (now ported, no longer deferred).
            return RedstoneProgrammerMenu.create(id, inv, program);
        }

        @Override
        public Component getDisplayName() {
            return Component.empty();
        }
    }
}
