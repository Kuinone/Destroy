package petrolpark.mc.destroy.core.explosion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

/**
 * SmartExplosion variant that runs every affected block through a silk-touch loot context — drops
 * the exact block rather than its ore products. Delicate excavation.
*/
public class DelicateExplosion extends SmartExplosion {

    private final ItemStack silkTouch;

    public DelicateExplosion(Level level, Entity source, DamageSource damageSource,
                             ExplosionDamageCalculator damageCalculator,
                             Vec3 position, float radius, float irregularity) {
        super(level, source, damageSource, damageCalculator, position, radius, irregularity);
        silkTouch = new ItemStack(Items.NETHERITE_PICKAXE);
        silkTouch.enchant(
            level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH),
            1);
    }

    @Override
    public void explodeEntity(Entity entity, float strength) {
        super.explodeEntity(entity, strength * 0.3f);
    }

    @Override
    public void modifyLoot(BlockPos pos, LootParams.Builder builder) {
        builder.withParameter(LootContextParams.TOOL, silkTouch);
    }
}
