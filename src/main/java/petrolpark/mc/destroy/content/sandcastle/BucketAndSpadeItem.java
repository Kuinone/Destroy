package petrolpark.mc.destroy.content.sandcastle;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;

import petrolpark.mc.destroy.DestroyBlocks;

/**
 * Bucket &amp; spade — damaging item that places a {@link SandCastleBlock} when used on sand /
 * red sand / soul sand. Also dispensable (a dispenser fires it into the block above the face
 * it's pointing).
*/
public class BucketAndSpadeItem extends Item {

    private static final ImmutableList<Block> validBlocks = ImmutableList.of(Blocks.SAND, Blocks.RED_SAND, Blocks.SOUL_SAND);

    public BucketAndSpadeItem(Properties properties) {
        super(properties);
        DispenserBlock.registerBehavior(this, new BucketAndSpadeDispenseBehaviour());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level == null) return InteractionResult.PASS;
        BlockState blockUnderneath = level.getBlockState(context.getClickedPos());
        BlockPos posAbove = context.getClickedPos().above();
        if (buildSandcastle(level, posAbove, blockUnderneath, context.getItemInHand())) {
            LivingEntity holder = context.getPlayer();
            if (holder != null) {
                EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                context.getItemInHand().hurtAndBreak(1, holder, slot);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    /**
 * Construct a sandcastle without breaking the bucket and spade.
 * @return whether a sandcastle was successfully built
*/
    public static boolean buildSandcastle(Level level, BlockPos posAbove, BlockState stateUnderneath, ItemStack stack) {
        if (level == null) return false;
        if (canSandCastleBeBuiltOn(stateUnderneath, level.getBlockState(posAbove))) {
            if (level.setBlockAndUpdate(posAbove, getSandCastleForMaterial(stateUnderneath))) {
                level.playSound(null, posAbove, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 0.5f, 1f);
                return true;
            }
        }
        return false;
    }

    /** Determines whether a sand castle can be naturally placed on the given block.*/
    public static boolean canSandCastleBeBuiltOn(BlockState state, BlockState stateAbove) {
        return validBlocks.stream().anyMatch(state::is) && stateAbove.isAir();
    }

    /** Determines the block state of the sand castle to be built on the given block.*/
    public static BlockState getSandCastleForMaterial(BlockState stateUnderneath) {
        SandCastleBlock.Material material = SandCastleBlock.Material.SAND;
        if (stateUnderneath.is(Blocks.RED_SAND)) {
            material = SandCastleBlock.Material.RED_SAND;
        } else if (stateUnderneath.is(Blocks.SOUL_SAND)) {
            material = SandCastleBlock.Material.SOUL_SAND;
        }
        return DestroyBlocks.SAND_CASTLE.getDefaultState().setValue(SandCastleBlock.MATERIAL, material);
    }

    public static class BucketAndSpadeDispenseBehaviour extends OptionalDispenseItemBehavior {

        @Override
        protected ItemStack execute(BlockSource source, ItemStack stack) {
            ServerLevel level = source.level();
            BlockPos posAbove = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
            if (buildSandcastle(level, posAbove, level.getBlockState(posAbove.relative(Direction.DOWN)), stack)) {
                // 1.21 has no ItemStack.hurt(int, RandomSource, ServerPlayer) — implement damage
                // tick + break-on-overflow manually for the entity-less dispenser context.
                if (stack.isDamageableItem()) {
                    int newDamage = stack.getDamageValue() + 1;
                    if (newDamage >= stack.getMaxDamage()) {
                        stack.shrink(1);
                    } else {
                        stack.setDamageValue(newDamage);
                    }
                }
                setSuccess(true);
            } else {
                setSuccess(false);
            }
            return stack;
        }
    }
}
