package petrolpark.mc.destroy.core.explosion;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/**
 * Damage source marker used by {@link SmartExplosion} so consumers (mob-drop XP handler, future
 * obliteration hooks) can detect a SmartExplosion context via {@code instanceof} and pull the
 * triggering explosion.
*/
public class SmartExplosionDamageSource extends DamageSource {

    public final SmartExplosion explosion;

    public SmartExplosionDamageSource(Holder<DamageType> type, SmartExplosion explosion) {
        super(type, explosion.getDirectSourceEntity(), explosion.getIndirectSourceEntity(), explosion.getPosition());
        this.explosion = explosion;
    }
}
