package petrolpark.mc.destroy.content.tool.syringe;

import java.util.List;

import com.simibubi.create.foundation.item.CustomUseEffectsItem;

import net.createmod.catnip.data.TriState;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;

import petrolpark.mc.destroy.DestroyDamageSources;
import petrolpark.mc.destroy.DestroyDataComponents;
import petrolpark.mc.destroy.DestroyItems;

/**
 * Base class for all syringe items (empty-needle placeholder + per-substance subclasses like
 * {@code AspirinSyringeItem}, {@code CisplatinSyringeItem}, {@code BabyBlueSyringeItem}).
 *
 * <p>Behaviour: player holds syringe in off-hand (main hand must be empty) and right-clicks to
 * inject self (32-tick hold animation); or left-clicks a living entity to inject them directly.
 * On successful inject, the stack is consumed and a plain {@link DestroyItems#SYRINGE} item is
 * placed back in the inventory (the "emptied" needle). Subclasses override {@link #onInject}
 * to apply their payload.
*/
public class SyringeItem extends Item implements CustomUseEffectsItem {

    public SyringeItem(Properties properties) {
        super(properties.stacksTo(1));
        DispenserBlock.registerBehavior(this, new SyringeDispenserBehaviour());
    }

    /** Vanilla {@code minecraft:item/generated} parent assigns
 * tintindex 0 to layer0 (the {@code syringe_overlay} texture, which encodes the colored "fluid
 * inside") and tintindex 1+ to subsequent layers (the syringe glass body). Subclasses override
 * this to differentiate aspirin / cisplatin / baby-blue / empty syringes visually.
 *
 * <p>Returns ARGB color (1.21 NeoForge BlockColor convention, see §7.24); alpha 0xFF for full
 * opacity. Default: 0xFFFFFFFF (no tint = empty syringe shows the bare overlay).</p>
 *
 * @param layer the texture layer index from the model JSON (0 = overlay, 1 = body, etc.)
*/
    public int getTintColor(int layer) {
        return 0xFFFFFFFF;
    }

    /**
 * Called when a player injects themselves, or an entity is injected by another entity.
 * Subclasses override to enact the payload (apply effects, award advancements, etc.).
 * @param itemStack the stack used to inject
 * @param level the level in which the injection is taking place
 * @param target the entity being injected
*/
    public void onInject(ItemStack itemStack, Level level, LivingEntity target) {
        // base: no-op
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.has(DestroyDataComponents.INJECTING)) { // continue if player is already injecting
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.PASS, itemStack);
        }

        if (hand == InteractionHand.OFF_HAND && player.getMainHandItem().isEmpty()) {
            // ensure player is using the syringe with their offhand + empty main hand
            itemStack.set(DestroyDataComponents.INJECTING, Boolean.TRUE);
            player.startUsingItem(hand);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStack);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, itemStack);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            onInject(stack, entity.level(), livingEntity);
            if (!player.isCreative()) {
                player.getInventory().removeItem(stack);
                player.getInventory().add(new ItemStack(DestroyItems.SYRINGE.get()));
            }
        }
        return false;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        onInject(itemStack, level, entity);
        if (entity instanceof Player player) {
            if (player.isCreative()) return itemStack;
        } else {
            return itemStack;
        }
        if (!(entity instanceof Player)) return itemStack;
        itemStack.remove(DestroyDataComponents.INJECTING);
        return new ItemStack(DestroyItems.SYRINGE.get());
    }

    @Override
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player)) return;
        itemStack.remove(DestroyDataComponents.INJECTING);
    }

    @Override
    public TriState shouldTriggerUseEffects(ItemStack stack, LivingEntity entity) {
        return TriState.TRUE;
    }

    @Override
    public boolean triggerUseEffects(ItemStack stack, LivingEntity entity, int count, RandomSource random) {
        if (entity.getTicksUsingItem() == 24 && !entity.level().isClientSide()) {
            entity.hurt(DestroyDamageSources.selfNeedle(entity.level()), 1f);
        }
        return true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    public class SyringeDispenserBehaviour extends OptionalDispenseItemBehavior {

        @Override
        public ItemStack execute(BlockSource blockSource, ItemStack stack) {
            List<LivingEntity> list = blockSource.level().getEntitiesOfClass(LivingEntity.class,
                new AABB(blockSource.pos().relative(blockSource.state().getValue(DispenserBlock.FACING))));
            if (!list.isEmpty()) {
                onInject(stack, blockSource.level(), list.get(0));
                setSuccess(true);
                return DestroyItems.SYRINGE.asStack();
            }
            return super.execute(blockSource, stack);
        }
    }
}
