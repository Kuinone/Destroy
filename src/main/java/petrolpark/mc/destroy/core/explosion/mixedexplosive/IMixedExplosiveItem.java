package petrolpark.mc.destroy.core.explosion.mixedexplosive;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;

/**
 * Marker interface for Items that carry a {@link MixedExplosiveInventory} payload — e.g. the
 * {@code custom_explosive_mix} BlockItem, plus future grenade / stick-of-dynamite variants. The
 * payload survives pick-block, creative-mode duplication, and shulker-box storage because it rides
 * on the {@link DestroyDataComponents#EXPLOSIVE_MIX} DataComponent.
 *
 * <p><b>Downstream consumers</b> (future T2b port targets):</p>
 * <ul>
 * <li>{@link MixedExplosiveBlock} (future S?) — {@code setPlacedBy} reads inventory from the
 * placed item stack, {@code getCloneItemStack} writes inventory back for pick-block.</li>
 * <li>{@link FillMixedExplosiveItemRecipe} (future S?) — crafting-table recipe inserts
 * ingredients into an IMixedExplosiveItem's inventory.</li>
 * <li>{@code MixedExplosiveBlockEntity.onPlace / getFilledItemStack} (future S?) — BE ↔ Item
 * inventory round-trip.</li>
 * </ul>
*/
public interface IMixedExplosiveItem {

    /**
 * Read the raw DataComponent-backed inventory payload as a {@link CompoundTag}. Returns a
 * <b>copy</b> (owned by the caller) so mutations don't leak back into the stack's component.
 * If the stack's item is not {@code this} or no payload exists, returns a fresh empty tag.
*/
    default CompoundTag getExplosiveMixTag(ItemStack stack) {
        if (!stack.getItem().equals(this)) return new CompoundTag();
        CompoundTag stored = stack.get(DestroyDataComponents.EXPLOSIVE_MIX);
        return stored == null ? new CompoundTag() : stored.copy();
    }

    /**
 * Write a CompoundTag payload to the stack's {@link DestroyDataComponents#EXPLOSIVE_MIX}
 * component. No-op if the stack's item is not {@code this}.
*/
    default void setExplosiveMixTag(ItemStack stack, CompoundTag tag) {
        if (stack.getItem().equals(this)) stack.set(DestroyDataComponents.EXPLOSIVE_MIX, tag);
    }

    /**
 * Reconstruct a {@link MixedExplosiveInventory} from this stack's {@link
 * DestroyDataComponents#EXPLOSIVE_MIX} payload, using {@link #getExplosiveInventorySize} +
 * {@link #getApplicableExplosionConditions} for shape. The provider is required by 1.21
 * NeoForge's {@code INBTSerializable.deserializeNBT(HolderLookup.Provider, CompoundTag)}
 * contract.
 *
 * @param stack target stack (expected to carry {@code this} item)
 * @param provider registry lookup (typically {@code level.registryAccess()})
 * @return reconstructed inventory; empty if stack item mismatches or no payload
*/
    default MixedExplosiveInventory getExplosiveInventory(ItemStack stack, HolderLookup.Provider provider) {
        MixedExplosiveInventory inv = new MixedExplosiveInventory(getExplosiveInventorySize(), getApplicableExplosionConditions());
        if (!stack.getItem().equals(this)) return inv;
        CompoundTag tag = stack.get(DestroyDataComponents.EXPLOSIVE_MIX);
        if (tag != null) inv.deserializeNBT(provider, tag);
        return inv;
    }

    /**
 * Serialize a {@link MixedExplosiveInventory} into this stack's {@link
 * DestroyDataComponents#EXPLOSIVE_MIX} payload. No-op if stack item mismatches or {@code inv}
 * is null. Uses 1.21 {@code INBTSerializable.serializeNBT(HolderLookup.Provider)}.
 *
 * @param stack target stack (expected to carry {@code this} item)
 * @param inv inventory to write
 * @param provider registry lookup (typically {@code level.registryAccess()})
*/
    default void setExplosiveInventory(ItemStack stack, MixedExplosiveInventory inv, HolderLookup.Provider provider) {
        if (inv != null && stack.getItem().equals(this)) {
            stack.set(DestroyDataComponents.EXPLOSIVE_MIX, inv.serializeNBT(provider));
        }
    }

    /** Number of inventory slots for this explosive-mix variant (e.g. custom_explosive_mix: configured).*/
    int getExplosiveInventorySize();

    /** Condition thresholds checked at detonation time (which of CAN_EXPLODE / NO_FUSE / SILK_TOUCH / etc.
 * apply to this variant).*/
    ExplosivePropertyCondition[] getApplicableExplosionConditions();
}
