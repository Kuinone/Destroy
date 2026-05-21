package petrolpark.mc.destroy.content.product.alcohol;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import petrolpark.mc.destroy.DestroyMobEffects;
import petrolpark.mc.destroy.config.DestroyAllConfigs;
import petrolpark.mc.destroy.config.DestroySubstancesConfigs;
import petrolpark.mc.destroy.core.item.DrinkItem;

/**
 * 1.21.1 notes:
*/
public class AlcoholicDrinkItem extends DrinkItem {

    private final int strength;

    /**
 * @param properties
 * @param strength How many levels of the Inebriation effect this item adds
*/
    public AlcoholicDrinkItem(Properties properties, int strength) {
        super(properties);
        this.strength = strength;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entityLiving) {
        super.finishUsingItem(stack, level, entityLiving);

        if (entityLiving instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverplayer, stack);
            serverplayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (!level.isClientSide) {
            if (DestroySubstancesConfigs.alcoholEnabled()) {
                DestroyMobEffects.increaseEffectLevel(entityLiving, DestroyMobEffects.INEBRIATION.getDelegate(), strength, DestroyAllConfigs.SERVER.substances.inebriationDuration.get());
            }
            DestroyMobEffects.increaseEffectLevel(entityLiving, DestroyMobEffects.FULL_BLADDER.getDelegate(), strength, DestroyAllConfigs.SERVER.substances.inebriationDuration.get());
        }

        if (stack.isEmpty()) {
            return new ItemStack(Items.GLASS_BOTTLE);
        } else {
            if (entityLiving instanceof Player player && !player.getAbilities().instabuild) {
                ItemStack itemstack = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(itemstack)) {
                    player.drop(itemstack, false);
                }
            }
            return stack;
        }
    }

    /** Number of levels of the Inebriation effect this item should add.*/
    public int getStrength() {
        return this.strength;
    }
}
