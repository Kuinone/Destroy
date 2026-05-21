package petrolpark.mc.destroy.compat.createbigcannons.item;

import net.minecraft.world.level.block.Block;

import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CustomExplosiveMixChargeBlockEntity;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.MixedExplosiveBlockItem;

/**
 * Item form of {@code custom_explosive_mix_charge}. Extends the 1.21 port's
 * {@link MixedExplosiveBlockItem} so it automatically inherits {@code IMixedExplosiveItem}
 * contract — EXPLOSIVE_MIX DataComponent get/set, IMixedExplosiveItem MenuProvider integration,
 * DYED_COLOR on the stack.
*/
public class CustomExplosiveMixChargeBlockItem extends MixedExplosiveBlockItem {

    public CustomExplosiveMixChargeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getExplosiveInventorySize() {
        return DestroyConfigs.server().compat.customExplosiveMixChargeSize.get();
    }

    @Override
    public ExplosivePropertyCondition[] getApplicableExplosionConditions() {
        return CustomExplosiveMixChargeBlockEntity.EXPLOSIVE_PROPERTY_CONDITIONS;
    }
}
