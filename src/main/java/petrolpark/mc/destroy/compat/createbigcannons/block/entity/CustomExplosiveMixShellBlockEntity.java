package petrolpark.mc.destroy.compat.createbigcannons.block.entity;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBlockEntity;

import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.IDyeableMixedExplosiveBlockEntity;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.MixedExplosiveInventory;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;

/**
 * BE for {@code custom_explosive_mix_shell}. Extends CBC's {@link FuzedBlockEntity} so the
 * shell inherits fuze-slot behavior (right-click to install fuze, countdown on ignition). Layers
 * on our {@link IDyeableMixedExplosiveBlockEntity} surface — dye color + custom name + mixed
 * inventory payload.
 *
 * <p>Cannot inherit from the 1.21 ported {@code MixedExplosiveBlockEntity} (S209) because we must
 * extend CBC's {@code FuzedBlockEntity} for fuze integration. Duplicates the inv/color/name fields
 * + serialization logic inline.</p>
*/
public class CustomExplosiveMixShellBlockEntity extends FuzedBlockEntity implements IDyeableMixedExplosiveBlockEntity {

    public static final ExplosivePropertyCondition[] EXPLOSIVE_PROPERTY_CONDITIONS = new ExplosivePropertyCondition[] {
        ExplosiveProperties.CAN_EXPLODE,
        ExplosiveProperties.DROPS_EXPERIENCE,
        ExplosiveProperties.DROPS_HEADS,
        ExplosiveProperties.ENTITIES_PUSHED,
        ExplosiveProperties.EVAPORATES_FLUIDS,
        ExplosiveProperties.ITEMS_DESTROYED,
        ExplosiveProperties.OBLITERATES,
        ExplosiveProperties.SILK_TOUCH,
        ExplosiveProperties.UNDERWATER
    };

    protected MixedExplosiveInventory inv;
    protected int color;
    @Nullable
    protected Component customName;

    public CustomExplosiveMixShellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.color = 0xFFFFFF;
        this.inv = createInv();
    }

    public MixedExplosiveInventory createInv() {
        return new MixedExplosiveInventory(DestroyConfigs.server().compat.customExplosiveMixShellSize.get(), EXPLOSIVE_PROPERTY_CONDITIONS);
    }

    @Override
    public MixedExplosiveInventory getExplosiveInventory() {
        return inv;
    }

    @Override
    public void setExplosiveInventory(MixedExplosiveInventory inv) {
        this.inv = inv;
        setChanged();
    }

    @Override
    public int getColor() {
        return color;
    }

    @Override
    public void setColor(int color) {
        this.color = color;
        setChanged();
    }

    @Override
    public void onPlace(ItemStack blockItemStack, HolderLookup.Provider provider) {
        IDyeableMixedExplosiveBlockEntity.super.onPlace(blockItemStack, provider);
        if (blockItemStack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            customName = blockItemStack.getHoverName();
        }
    }

    @Override
    public ItemStack getFilledItemStack(ItemStack emptyItemStack, HolderLookup.Provider provider) {
        ItemStack stack = IDyeableMixedExplosiveBlockEntity.super.getFilledItemStack(emptyItemStack, provider);
        if (customName != null) stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, customName);
        return stack;
    }

    @Override
    public ExplosivePropertyCondition[] getApplicableExplosionConditions() {
        return EXPLOSIVE_PROPERTY_CONDITIONS;
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : getBlockState().getBlock().getName();
    }

    public ItemStack getFuze() {
        return getItem(0);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        color = tag.getInt("Color");
        if (tag.contains("CustomName")) {
            ComponentSerialization.CODEC
                .parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), tag.get("CustomName"))
                .resultOrPartial()
                .ifPresent(c -> customName = c);
        } else {
            customName = null;
        }
        inv = createInv();
        if (tag.contains("ExplosiveMix")) {
            inv.deserializeNBT(registries, tag.getCompound("ExplosiveMix"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Color", color);
        if (customName != null) {
            ComponentSerialization.CODEC
                .encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), customName)
                .resultOrPartial()
                .ifPresent(t -> tag.put("CustomName", t));
        }
        tag.put("ExplosiveMix", inv.serializeNBT(registries));
    }

    /** CBC explode hook — no-op; projectile (entity) handles detonation on impact.*/
    public void explode(@Nullable Player cause) {
        // shell on the ground doesn't detonate — fuze mechanic is CBC's job
    }
}
