package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Dyeable variant of {@link IMixedExplosiveBlockEntity} — layers on top a dye-color abstract
 * contract + tryDye user-interaction + reRender client-side section-dirty hook + color serialization
 * for clipboard copy. Used by {@link MixedExplosiveBlockEntity}'s future {@code implements} upgrade
 * and the full Simple Dyeable Nameable port variant.
*/
public interface IDyeableMixedExplosiveBlockEntity extends IMixedExplosiveBlockEntity {

    void setColor(int color);

    int getColor();

    /** Client-side hook: mark the block's chunk-section dirty when color changes, forcing re-render.*/
    @OnlyIn(Dist.CLIENT)
    static void reRender(Level level, BlockPos blockPos) {
        SectionPos pos = SectionPos.of(blockPos);
        if (level instanceof ClientLevel clientLevel) clientLevel.setSectionDirtyWithNeighbors(pos.x(), pos.y(), pos.z());
    }

    @Override
    default void onPlace(ItemStack blockItemStack, HolderLookup.Provider provider) {
        IMixedExplosiveBlockEntity.super.onPlace(blockItemStack, provider);
        // 1.21: any stack can carry DYED_COLOR component — no DyeableLeatherItem instanceof gate.
        // Apply color only when the stack actually has a DYED_COLOR DataComponent set.
        if (blockItemStack.has(DataComponents.DYED_COLOR)) {
            setColor(DyedItemColor.getOrDefault(blockItemStack, 0xFFFFFF));
        }
    }

    @Override
    default ItemStack getFilledItemStack(ItemStack emptyItemStack, HolderLookup.Provider provider) {
        // Apply color before the parent's inventory roundtrip so pick-block preserves both.
        emptyItemStack.set(DataComponents.DYED_COLOR, new DyedItemColor(getColor(), true));
        return IMixedExplosiveBlockEntity.super.getFilledItemStack(emptyItemStack, provider);
    }

    /**
 * Handle right-click with a dye item — mix the dye color into the block's current color using
 * the 1.21 DyedItemColor.applyDyes algorithm.
*/
    default InteractionResult tryDye(ItemStack dyeStack, HitResult target, Level level, BlockPos pos, Player player) {
        if (!(dyeStack.getItem() instanceof DyeItem dyeItem)) return InteractionResult.PASS;
        // Seed clone stack with current BE color so applyDyes mixes against it.
        ItemStack cloneStack = level.getBlockState(pos).getCloneItemStack(target, level, pos, player);
        cloneStack.set(DataComponents.DYED_COLOR, new DyedItemColor(getColor(), true));
        ItemStack dyedStack = DyedItemColor.applyDyes(cloneStack, List.of(dyeItem));
        setColor(DyedItemColor.getOrDefault(dyedStack, 0xFFFFFF));
        if (!player.isCreative()) dyeStack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    default boolean readFromClipboard(HolderLookup.Provider provider, CompoundTag tag, Player player, Direction side, boolean simulate) {
        boolean invCopied = IMixedExplosiveBlockEntity.super.readFromClipboard(provider, tag, player, side, simulate);
        if (tag.contains("Color", Tag.TAG_INT)) {
            if (!simulate) setColor(tag.getInt("Color"));
            return true;
        }
        return invCopied;
    }

    @Override
    default boolean writeToClipboard(HolderLookup.Provider provider, CompoundTag tag, Direction side) {
        IMixedExplosiveBlockEntity.super.writeToClipboard(provider, tag, side);
        tag.putInt("Color", getColor());
        return true;
    }
}
