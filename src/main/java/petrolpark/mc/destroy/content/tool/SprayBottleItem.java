package petrolpark.mc.destroy.content.tool;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import petrolpark.mc.destroy.DestroyItems;
import petrolpark.mc.destroy.DestroySoundEvents;

import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Sprays a {@link PotionContents} cloud when used or when dispensed.
*/
public class SprayBottleItem extends Item {

    public final MobEffectInstance[] effects;

    public SprayBottleItem(Properties properties, MobEffectInstance... effects) {
        super(properties);
        this.effects = effects;
        DispenserBlock.registerBehavior(this, new SprayBottleDispenserBehaviour());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();

        spawnEffectCloud(context.getLevel(), context.getClickLocation(), player);
        DestroySoundEvents.SPRAY_BOTTLE_SPRAYS.play(context.getLevel(), player, context.getClickedPos());

        if (player != null && !player.isCreative()) {
            context.getItemInHand().shrink(1);
            player.getInventory().placeItemBackInInventory(DestroyItems.SPRAY_BOTTLE.asStack());
        }

        return InteractionResult.SUCCESS;
    }

    public void spawnEffectCloud(Level level, Vec3 position, @Nullable Player player) {
        AreaEffectCloud effectCloud = new AreaEffectCloud(level, position.x, position.y, position.z);
        if (player != null) effectCloud.setOwner(player);
        // Effect-only cloud (no base potion);
        // an unnamed Potion whose only effects were the custom ones we pass in.
        effectCloud.setPotionContents(new PotionContents(Optional.empty(), Optional.empty(), List.of(effects)));
        for (MobEffectInstance instance : effects) effectCloud.addEffect(instance);

        effectCloud.setRadius(3f);
        effectCloud.setRadiusOnUse(-0.5f);
        effectCloud.setWaitTime(10);
        effectCloud.setRadiusPerTick(-effectCloud.getRadius() / (float) effectCloud.getDuration());

        level.addFreshEntity(effectCloud);
    }

    public class SprayBottleDispenserBehaviour extends OptionalDispenseItemBehavior {

        @Override
        protected ItemStack execute(BlockSource blockSource, ItemStack stack) {
            Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
            Vec3 effectCloudPosition = Vec3.atBottomCenterOf(blockSource.pos().relative(direction));

            spawnEffectCloud(blockSource.level(), effectCloudPosition, null);
            DestroySoundEvents.SPRAY_BOTTLE_SPRAYS.play(blockSource.level(), null, blockSource.pos());

            // NeoForge 21.1: direct nullable IItemHandler lookup — no more LazyOptional. The
            // BlockEntity-level capability takes (Level, BlockPos, @Nullable Direction).
            IItemHandler inv = blockSource.level().getCapability(
                Capabilities.ItemHandler.BLOCK, blockSource.pos(), null);
            boolean couldInsertEmptyBottle = inv != null
                && ItemHandlerHelper.insertItem(inv, DestroyItems.SPRAY_BOTTLE.asStack(), false).isEmpty();
            if (!couldInsertEmptyBottle) {
                spawnItem(blockSource.level(), DestroyItems.SPRAY_BOTTLE.asStack(), 6, direction,
                    DispenserBlock.getDispensePosition(blockSource));
            }

            setSuccess(true);
            stack.shrink(1);
            return stack;
        }
    }
}
