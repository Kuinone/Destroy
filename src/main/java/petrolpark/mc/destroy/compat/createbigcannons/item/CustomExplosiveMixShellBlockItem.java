package petrolpark.mc.destroy.compat.createbigcannons.item;

import net.minecraft.world.level.block.Block;

import petrolpark.mc.destroy.compat.createbigcannons.block.entity.CustomExplosiveMixShellBlockEntity;
import petrolpark.mc.destroy.config.DestroyConfigs;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.ExplosiveProperties.ExplosivePropertyCondition;
import petrolpark.mc.destroy.core.explosion.mixedexplosive.MixedExplosiveBlockItem;

/**
 * Item form of {@code custom_explosive_mix_shell}. Parallel to
 * {@link CustomExplosiveMixChargeBlockItem} but with the shell's wider explosion-condition set.
*/
public class CustomExplosiveMixShellBlockItem extends MixedExplosiveBlockItem {

    public CustomExplosiveMixShellBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public int getExplosiveInventorySize() {
        return DestroyConfigs.server().compat.customExplosiveMixShellSize.get();
    }

    @Override
    public ExplosivePropertyCondition[] getApplicableExplosionConditions() {
        return CustomExplosiveMixShellBlockEntity.EXPLOSIVE_PROPERTY_CONDITIONS;
    }
}
