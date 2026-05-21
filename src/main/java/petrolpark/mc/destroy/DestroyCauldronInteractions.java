package petrolpark.mc.destroy;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Cauldron-interaction maps for Destroy's cauldron variants. The backing maps plug into the
 * {@link AbstractCauldronBlock AbstractCauldronBlock} constructor via {@link
 * CauldronInteraction.InteractionMap}.
*/
public class DestroyCauldronInteractions {

    public static final CauldronInteraction.InteractionMap URINE = CauldronInteraction.newInteractionMap("urine");

    static {
        URINE.map().put(Items.GLASS_BOTTLE, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            if (!level.isClientSide) {
                Item item = itemStack.getItem();
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, DestroyItems.URINE_BOTTLE.asStack()));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState());
                level.playSound(null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        });
    }

    public static void register() {}
}
