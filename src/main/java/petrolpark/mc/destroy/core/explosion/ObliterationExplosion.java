package petrolpark.mc.destroy.core.explosion;

import javax.annotation.Nullable;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * {@link SmartExplosion} variant that routes block drops through the "obliteration" loot path
 * (see {@link ObliterationCondition}). Used by Destroy's explosives that should yield more /
 * different drops than vanilla blasting.
*/
public class ObliterationExplosion extends SmartExplosion {

    public ObliterationExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource,
                                 @Nullable ExplosionDamageCalculator damageCalculator,
                                 Vec3 position, float radius, float irregularity) {
        super(level, source, damageSource, damageCalculator, position, radius, irregularity);
    }

    @Override
    public boolean shouldDoObliterationDrops() {
        return true;
    }
}
